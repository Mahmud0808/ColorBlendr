package com.drdisagree.colorblendr.data.domain

import android.util.Log
import com.drdisagree.colorblendr.data.common.Utilities.customColorEnabled
import com.drdisagree.colorblendr.data.common.Utilities.getWallpaperColorList
import com.drdisagree.colorblendr.data.common.Utilities.setSeedColorValue
import com.drdisagree.colorblendr.data.common.Utilities.updateColorAppliedTimestamp
import com.drdisagree.colorblendr.data.config.Prefs
import com.drdisagree.colorblendr.theme.ThemePrefs
import com.drdisagree.colorblendr.theme.scheme.ResolvedTheme
import com.drdisagree.colorblendr.utils.community.CommunityThemeApplier
import com.drdisagree.colorblendr.utils.community.CommunityVotes
import com.drdisagree.colorblendr.utils.manager.OverlayManager.applyFabricatedColors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

// Defers user color/setting selections: changes are staged in memory (never
// persisted) and only restyle the in-app theme, until the user applies them —
// committing the prefs and pushing the overlay — or discards them.
object PreviewController {

    private val TAG = PreviewController::class.java.simpleName

    private val _previewTheme = MutableStateFlow<ResolvedTheme?>(null)
    val previewTheme = _previewTheme.asStateFlow()

    private val _isApplying = MutableStateFlow(false)
    val isApplying = _isApplying.asStateFlow()

    val isPreviewActive: Boolean
        get() = Prefs.isStagingActive || _previewTheme.value != null

    // Own scope: callers' composition scopes get cancelled when applying the
    // overlay recreates the activity, which must not abort these jobs.
    @OptIn(ExperimentalCoroutinesApi::class)
    private val controllerScope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO.limitedParallelism(1)
    )

    // Conflated so rapid updates (e.g. slider drags) only compute the latest.
    private val previewRequests = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    // Sticky so a conflated request cannot lose a pending refresh.
    @Volatile
    private var refreshOthersPending = false

    private val sessionLock = Any()

    private val busy = AtomicBoolean(false)

    @Volatile
    private var previewSession = 0

    private fun endPreviewSession() {
        synchronized(sessionLock) {
            previewSession++
            _previewTheme.value = null
        }
    }

    @Volatile
    private var systemUpdatePending = false

    fun markSystemUpdatePending() {
        systemUpdatePending = true
    }

    private fun takeSystemUpdatePending(): Boolean {
        val pending = systemUpdatePending
        systemUpdatePending = false
        return pending
    }

    private fun syncWallpaperSeed() {
        if (!customColorEnabled()) {
            getWallpaperColorList().firstOrNull()?.let { setSeedColorValue(it) }
        }
    }

    init {
        controllerScope.launch {
            previewRequests.collect {
                // A request that outlived its preview session (already
                // applied or discarded) must not resurrect the preview.
                if (!Prefs.isStagingActive) return@collect

                val session = previewSession
                val refreshOthers = refreshOthersPending
                refreshOthersPending = false
                try {
                    val theme = ThemePrefs.resolve().precompute()
                    synchronized(sessionLock) {
                        if (session != previewSession || !Prefs.isStagingActive) return@collect
                        _previewTheme.value = theme
                    }
                    if (refreshOthers) {
                        // Palette/style pages regenerate from the staged prefs.
                        RefreshCoordinator.triggerRefresh()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error generating preview colors", e)
                }
            }
        }
    }

    // Call before writing prefs from a preview-able user selection; the
    // writes land in the staging layer instead of storage.
    fun beginPreview() {
        Prefs.beginStaging()
    }

    // refreshOthers reloads the palette/style viewmodels; pass false for
    // high-frequency updates like slider drags and true on the final value.
    fun updatePreview(refreshOthers: Boolean = true) {
        if (refreshOthers) refreshOthersPending = true
        previewRequests.tryEmit(Unit)
    }

    fun applyChanges() {
        if (!isPreviewActive || !busy.compareAndSet(false, true)) return

        _isApplying.value = true
        controllerScope.launch {
            try {
                Prefs.commitStaged()
                if (takeSystemUpdatePending()) syncWallpaperSeed()
                updateColorAppliedTimestamp()
                applyFabricatedColors()

                // Count the apply if this preview came from a community
                // creation; server dedupes per device.
                CommunityThemeApplier.pendingCreationId?.let { creationId ->
                    CommunityThemeApplier.pendingCreationId = null
                    CommunityVotes.reportApply(creationId)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error applying preview colors", e)
            } finally {
                endPreviewSession()
                _isApplying.value = false
                busy.set(false)
            }
        }
    }

    fun discardChanges() {
        if (busy.get()) return

        controllerScope.launch {
            abandonPreview()
        }
    }

    // Drops any active preview, then runs an out-of-band apply (e.g. restoring
    // a just-updated style) with the same "applying" dialog + overlay push
    // applyChanges shows.
    fun reapply(prepare: suspend () -> Unit) {
        if (!busy.compareAndSet(false, true)) return

        _isApplying.value = true
        controllerScope.launch {
            try {
                abandonPreview(applyPending = false)
                if (takeSystemUpdatePending()) syncWallpaperSeed()
                prepare()
                updateColorAppliedTimestamp()
                applyFabricatedColors()
            } catch (e: Exception) {
                Log.e(TAG, "Error reapplying colors", e)
            } finally {
                _isApplying.value = false
                busy.set(false)
            }
        }
    }

    // Synchronous discard for flows that take over from an active preview
    // (master switch, backup restore).
    fun abandonPreview(applyPending: Boolean = true) {
        CommunityThemeApplier.pendingCreationId = null
        Prefs.discardStaged()
        endPreviewSession()
        RefreshCoordinator.triggerRefresh()

        if (applyPending && takeSystemUpdatePending()) {
            syncWallpaperSeed()
            controllerScope.launch {
                updateColorAppliedTimestamp()
                applyFabricatedColors()
            }
        }
    }
}
