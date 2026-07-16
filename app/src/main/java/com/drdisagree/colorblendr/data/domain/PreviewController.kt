package com.drdisagree.colorblendr.data.domain

import android.util.Log
import com.drdisagree.colorblendr.utils.community.CommunityVotes
import com.drdisagree.colorblendr.utils.community.CommunityThemeApplier
import com.drdisagree.colorblendr.data.common.Utilities.accurateShadesEnabled
import com.drdisagree.colorblendr.data.common.Utilities.getAccentSaturation
import com.drdisagree.colorblendr.data.common.Utilities.getBackgroundLightness
import com.drdisagree.colorblendr.data.common.Utilities.getBackgroundSaturation
import com.drdisagree.colorblendr.data.common.Utilities.getCurrentMonetStyle
import com.drdisagree.colorblendr.data.common.Utilities.pitchBlackThemeEnabled
import com.drdisagree.colorblendr.data.common.Utilities.updateColorAppliedTimestamp
import com.drdisagree.colorblendr.data.config.Prefs
import com.drdisagree.colorblendr.utils.colors.ColorUtil.generateModifiedColors
import com.drdisagree.colorblendr.utils.colors.computeSystemColorMap
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

// Defers user color/setting selections: changes are staged in memory (never
// persisted) and only restyle the in-app theme, until the user applies them —
// committing the prefs and pushing the overlay — or discards them.
object PreviewController {

    private val TAG = PreviewController::class.java.simpleName

    data class PreviewColors(
        val lightMap: Map<String, Int>,
        val darkMap: Map<String, Int>,
        val paletteLight: List<List<Int>>,
        val paletteDark: List<List<Int>>
    )

    private val _previewColors = MutableStateFlow<PreviewColors?>(null)
    val previewColors = _previewColors.asStateFlow()

    private val _isApplying = MutableStateFlow(false)
    val isApplying = _isApplying.asStateFlow()

    val isPreviewActive: Boolean
        get() = Prefs.isStagingActive || _previewColors.value != null

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

    init {
        controllerScope.launch {
            previewRequests.collect {
                // A request that outlived its preview session (already
                // applied or discarded) must not resurrect the preview.
                if (!Prefs.isStagingActive) return@collect

                val refreshOthers = refreshOthersPending
                refreshOthersPending = false
                try {
                    _previewColors.value = buildPreviewColors()
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
        controllerScope.launch {
            _isApplying.value = true
            try {
                Prefs.commitStaged()
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
                _previewColors.value = null
                _isApplying.value = false
            }
        }
    }

    fun discardChanges() {
        controllerScope.launch {
            abandonPreview()
        }
    }

    // Drops any active preview, then runs an out-of-band apply (e.g. restoring
    // a just-updated style) with the same "applying" dialog + overlay push
    // applyChanges shows.
    fun reapply(prepare: suspend () -> Unit) {
        controllerScope.launch {
            _isApplying.value = true
            try {
                abandonPreview()
                prepare()
                updateColorAppliedTimestamp()
                applyFabricatedColors()
            } catch (e: Exception) {
                Log.e(TAG, "Error reapplying colors", e)
            } finally {
                _isApplying.value = false
            }
        }
    }

    // Synchronous discard for flows that take over from an active preview
    // (master switch, backup restore).
    fun abandonPreview() {
        CommunityThemeApplier.pendingCreationId = null
        Prefs.discardStaged()
        _previewColors.value = null
        RefreshCoordinator.triggerRefresh()
    }

    fun buildPreviewColors(): PreviewColors {
        val style = getCurrentMonetStyle()
        val accentSaturation = getAccentSaturation()
        val backgroundSaturation = getBackgroundSaturation()
        val backgroundLightness = getBackgroundLightness()
        val pitchBlackTheme = pitchBlackThemeEnabled()
        val accurateShades = accurateShadesEnabled()

        val paletteLight = generateModifiedColors(
            style = style,
            accentSaturation = accentSaturation,
            backgroundSaturation = backgroundSaturation,
            backgroundLightness = backgroundLightness,
            pitchBlackTheme = pitchBlackTheme,
            accurateShades = accurateShades,
            modifyPitchBlack = false,
            isDark = false
        )
        val paletteDark = generateModifiedColors(
            style = style,
            accentSaturation = accentSaturation,
            backgroundSaturation = backgroundSaturation,
            backgroundLightness = backgroundLightness,
            pitchBlackTheme = pitchBlackTheme,
            accurateShades = accurateShades,
            modifyPitchBlack = false,
            isDark = true
        )

        return PreviewColors(
            lightMap = computeSystemColorMap(paletteLight, paletteDark, isDark = false),
            darkMap = computeSystemColorMap(paletteLight, paletteDark, isDark = true),
            paletteLight = paletteLight,
            paletteDark = paletteDark
        )
    }
}
