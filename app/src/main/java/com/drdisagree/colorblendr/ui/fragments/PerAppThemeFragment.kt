package com.drdisagree.colorblendr.ui.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.drdisagree.colorblendr.ColorBlendr.Companion.appContext
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.data.common.Constant.FABRICATED_OVERLAY_NAME_APPS
import com.drdisagree.colorblendr.data.common.Utilities.getAppListFilteringMethod
import com.drdisagree.colorblendr.data.common.Utilities.setAppListFilteringMethod
import com.drdisagree.colorblendr.data.common.Utilities.setShowPerAppThemeWarning
import com.drdisagree.colorblendr.data.common.Utilities.showPerAppThemeWarning
import com.drdisagree.colorblendr.data.enums.AppType
import com.drdisagree.colorblendr.data.models.AppInfoModel
import com.drdisagree.colorblendr.databinding.FragmentPerAppThemeBinding
import com.drdisagree.colorblendr.ui.adapters.AppListAdapter
import com.drdisagree.colorblendr.utils.FabricatedUtil.updateFabricatedAppList
import com.drdisagree.colorblendr.utils.MiscUtil.setToolbarTitle
import com.drdisagree.colorblendr.utils.OverlayManager.isOverlayEnabled
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import eightbitlab.com.blurview.RenderEffectBlur
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class PerAppThemeFragment : Fragment() {

    private lateinit var binding: FragmentPerAppThemeBinding
    private var appList: List<AppInfoModel>? = null
    private var adapter: AppListAdapter? = null

    private val packageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            initAppList(AppType.entries[getAppListFilteringMethod()])
        }
    }
    private val textWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        }

        override fun afterTextChanged(s: Editable) {
            if (binding.searchBox.search.text.toString().trim().isNotEmpty()) {
                binding.searchBox.clear.visibility = View.VISIBLE
                filterList(binding.searchBox.search.text.toString().trim())
            } else {
                binding.searchBox.clear.visibility = View.GONE
                filterList("")
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPerAppThemeBinding.inflate(inflater, container, false)

        setToolbarTitle(requireContext(), R.string.per_app_theme, true, binding.header.toolbar)

        // Warning
        if (!showPerAppThemeWarning()) {
            binding.warn.container.visibility = View.GONE
        }
        binding.warn.close.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                delay(50)
                setShowPerAppThemeWarning(false)
                binding.warn.container.animate()
                    .translationX(binding.warn.container.width * 2f).alpha(0f).withEndAction {
                        binding.warn.container.visibility = View.GONE
                    }.start()
            }
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.searchBox.filter.setOnClickListener { showFilterDialog() }

        initAppList(AppType.entries[getAppListFilteringMethod()])
        blurSearchView()
    }

    private fun initAppList(appType: AppType) {
        binding.recyclerView.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE
        binding.searchBox.search.removeTextChangedListener(textWatcher)

        CoroutineScope(Dispatchers.IO).launch {
            updateFabricatedAppList(appContext)
            appList = getAllInstalledApps(requireContext(), appType)
            adapter = AppListAdapter(appList!!)

            withContext(Dispatchers.Main) {
                try {
                    binding.recyclerView.adapter = adapter
                    binding.searchBox.search.addTextChangedListener(textWatcher)

                    binding.progressBar.visibility = View.GONE
                    binding.recyclerView.visibility = View.VISIBLE

                    binding.searchBox.clear.setOnClickListener {
                        binding.searchBox.search.setText("")
                        binding.searchBox.clear.visibility = View.GONE
                    }
                    if (binding.searchBox.search.text.toString().trim().isNotEmpty()) {
                        filterList(binding.searchBox.search.text.toString().trim { it <= ' ' })
                    }
                } catch (ignored: Exception) {
                    // Fragment was not attached to activity
                }
            }
        }
    }

    private fun filterList(query: String) {
        if (appList == null) {
            return
        }

        val startsWithNameList: MutableList<AppInfoModel> = ArrayList()
        val containsNameList: MutableList<AppInfoModel> = ArrayList()
        val startsWithPackageNameList: MutableList<AppInfoModel> = ArrayList()
        val containsPackageNameList: MutableList<AppInfoModel> = ArrayList()

        for (app in appList!!) {
            if (app.appName.lowercase(Locale.getDefault())
                    .startsWith(query.lowercase(Locale.getDefault()))
            ) {
                startsWithNameList.add(app)
            } else if (app.appName.lowercase(Locale.getDefault())
                    .contains(query.lowercase(Locale.getDefault()))
            ) {
                containsNameList.add(app)
            } else if (app.packageName.lowercase(Locale.getDefault()).startsWith(
                    query.lowercase(
                        Locale.getDefault()
                    )
                )
            ) {
                startsWithPackageNameList.add(app)
            } else if (app.packageName.lowercase(Locale.getDefault()).contains(
                    query.lowercase(
                        Locale.getDefault()
                    )
                )
            ) {
                containsPackageNameList.add(app)
            }
        }

        val filteredList: MutableList<AppInfoModel> = ArrayList()
        filteredList.addAll(startsWithNameList)
        filteredList.addAll(containsNameList)
        filteredList.addAll(startsWithPackageNameList)
        filteredList.addAll(containsPackageNameList)

        adapter = AppListAdapter(filteredList)
        binding.recyclerView.adapter = adapter
    }

    private fun blurSearchView() {
        val background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_searchbox)
        binding.searchBox.blurView.setupWith(binding.root, RenderEffectBlur())
            .setFrameClearDrawable(background)
            .setBlurRadius(20f)
        binding.searchBox.blurView.outlineProvider = ViewOutlineProvider.BACKGROUND
        binding.searchBox.blurView.clipToOutline = true
    }

    private fun showFilterDialog() {
        val items = arrayOf(
            getString(R.string.filter_system_apps),
            getString(R.string.filter_user_apps),
            getString(R.string.filter_launchable_apps),
            getString(R.string.filter_all)
        )

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.filter_app_category))
            .setSingleChoiceItems(
                items,
                getAppListFilteringMethod()
            ) { dialog: DialogInterface, which: Int ->
                setAppListFilteringMethod(which)
                initAppList(AppType.entries[which])
                dialog.dismiss()
            }
            .setCancelable(true)
            .show()
    }

    override fun onResume() {
        super.onResume()

        val intentFilterWithoutScheme = IntentFilter()
        intentFilterWithoutScheme.addAction(Intent.ACTION_PACKAGE_ADDED)
        intentFilterWithoutScheme.addAction(Intent.ACTION_PACKAGE_REMOVED)

        val intentFilterWithScheme = IntentFilter()
        intentFilterWithScheme.addAction(Intent.ACTION_PACKAGE_ADDED)
        intentFilterWithScheme.addAction(Intent.ACTION_PACKAGE_REMOVED)
        intentFilterWithScheme.addDataScheme("package")

        LocalBroadcastManager
            .getInstance(requireContext())
            .registerReceiver(packageReceiver, intentFilterWithoutScheme)
        LocalBroadcastManager
            .getInstance(requireContext())
            .registerReceiver(packageReceiver, intentFilterWithScheme)
    }

    override fun onDestroy() {
        try {
            LocalBroadcastManager
                .getInstance(requireContext())
                .unregisterReceiver(packageReceiver)
        } catch (ignored: Exception) {
            // Receiver was not registered
        }
        super.onDestroy()
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            parentFragmentManager.popBackStackImmediate()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        private fun getAllInstalledApps(context: Context, appType: AppType): List<AppInfoModel> {
            val appList: MutableList<AppInfoModel> = ArrayList()
            val packageManager = context.packageManager

            val applications = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

            for (appInfo in applications) {
                val packageName = appInfo.packageName

                if (appType == AppType.LAUNCHABLE) {
                    packageManager.getLaunchIntentForPackage(packageName) ?: continue
                }

                val appName = appInfo.loadLabel(packageManager).toString()
                val appIcon = appInfo.loadIcon(packageManager)
                val isSelected = isOverlayEnabled(
                    String.format(FABRICATED_OVERLAY_NAME_APPS, packageName)
                )

                val isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0

                val includeApp = when (appType) {
                    AppType.SYSTEM -> isSystemApp
                    AppType.USER -> !isSystemApp
                    AppType.LAUNCHABLE, AppType.ALL -> true
                }

                if (includeApp) {
                    val app = AppInfoModel(appName, packageName, appIcon)
                    app.isSelected = isSelected
                    appList.add(app)
                }
            }

            appList.sortWith(compareBy<AppInfoModel> { !it.isSelected }.thenBy { it.appName.lowercase() })

            return appList
        }
    }
}