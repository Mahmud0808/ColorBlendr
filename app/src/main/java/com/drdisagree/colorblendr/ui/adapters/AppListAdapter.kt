package com.drdisagree.colorblendr.ui.adapters

import android.content.Context
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.RecyclerView
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.data.common.Const.FABRICATED_OVERLAY_NAME_APPS
import com.drdisagree.colorblendr.data.common.Const.SHIZUKU_THEMING_ENABLED
import com.drdisagree.colorblendr.data.common.Const.THEMING_ENABLED
import com.drdisagree.colorblendr.data.common.Const.saveSelectedFabricatedApps
import com.drdisagree.colorblendr.data.config.Prefs.getBoolean
import com.drdisagree.colorblendr.data.models.AppInfoModel
import com.drdisagree.colorblendr.utils.ColorUtil.getColorFromAttribute
import com.drdisagree.colorblendr.utils.OverlayManager.applyFabricatedColorsPerApp
import com.drdisagree.colorblendr.utils.OverlayManager.unregisterFabricatedOverlay
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AppListAdapter(private val appList: List<AppInfoModel>) :
    RecyclerView.Adapter<AppListAdapter.ViewHolder>() {

    private var context: Context? = null
    private val selectedApps: HashMap<String, Boolean> = HashMap()

    init {
        if (getBoolean(THEMING_ENABLED, false) ||
            getBoolean(SHIZUKU_THEMING_ENABLED, false)
        ) {
            for (appInfo: AppInfoModel in appList) {
                if (appInfo.isSelected) {
                    selectedApps[appInfo.packageName] = true
                }
            }

            saveSelectedFabricatedApps(selectedApps)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.view_app_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val appInfo: AppInfoModel = appList[position]
        holder.appIcon.setImageDrawable(appInfo.appIcon)
        holder.appName.text = appInfo.appName
        holder.packageName.text = appInfo.packageName

        holder.itemView.setOnClickListener {
            val isSelected: Boolean = appInfo.isSelected
            setSelected(holder, !isSelected)
            appInfo.isSelected = !isSelected

            selectedApps[appInfo.packageName] = !isSelected
            saveSelectedFabricatedApps(selectedApps)

            CoroutineScope(Dispatchers.Main).launch {
                if (isSelected) {
                    unregisterFabricatedOverlay(
                        String.format(
                            FABRICATED_OVERLAY_NAME_APPS,
                            appInfo.packageName
                        )
                    )
                } else {
                    applyFabricatedColorsPerApp(appInfo.packageName, null)
                }
            }
        }

        setSelected(holder, appInfo.isSelected)
    }

    override fun onViewAttachedToWindow(holder: ViewHolder) {
        super.onViewAttachedToWindow(holder)

        setSelected(holder, appList[holder.getBindingAdapterPosition()].isSelected)
    }

    override fun getItemCount(): Int {
        return appList.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var container: MaterialCardView = view.findViewById(R.id.container)
        var appIcon: ImageView = view.findViewById(R.id.app_icon)
        var appName: TextView = view.findViewById(R.id.title)
        var packageName: TextView = view.findViewById(R.id.summary)
        var iconView: ImageView = view.findViewById(R.id.icon)
    }

    private fun setSelected(holder: ViewHolder, isSelected: Boolean) {
        holder.iconView.setAlpha(if (isSelected) 1.0f else 0.2f)
        holder.iconView.setColorFilter(getIconColor(isSelected), PorterDuff.Mode.SRC_IN)
        holder.iconView.setImageResource(if (isSelected) R.drawable.ic_checked_filled else R.drawable.ic_checked_outline)
        holder.container.setCardBackgroundColor(getCardBackgroundColor(isSelected))
        holder.container.setStrokeWidth(if (isSelected) 0 else 2)
        holder.appName.setTextColor(getTextColor(isSelected))
        holder.packageName.setTextColor(getTextColor(isSelected))

        if (holder.getBindingAdapterPosition() == 0) {
            (holder.container.layoutParams as MarginLayoutParams).topMargin =
                (72 * context!!.resources.displayMetrics.density).toInt()
        } else {
            (holder.container.layoutParams as MarginLayoutParams).topMargin = 0
        }
    }

    @ColorInt
    private fun getCardBackgroundColor(isSelected: Boolean): Int {
        return if (isSelected) getColorFromAttribute(
            context!!,
            com.google.android.material.R.attr.colorPrimaryContainer
        ) else getColorFromAttribute(
            context!!, com.google.android.material.R.attr.colorSurfaceContainer
        )
    }

    @ColorInt
    private fun getIconColor(isSelected: Boolean): Int {
        return if (isSelected) getColorFromAttribute(
            context!!,
            com.google.android.material.R.attr.colorPrimary
        ) else getColorFromAttribute(
            context!!, com.google.android.material.R.attr.colorOnSurface
        )
    }

    @ColorInt
    private fun getTextColor(isSelected: Boolean): Int {
        return if (isSelected) getColorFromAttribute(
            context!!,
            com.google.android.material.R.attr.colorOnPrimaryContainer
        ) else getColorFromAttribute(
            context!!, com.google.android.material.R.attr.colorOnSurface
        )
    }
}
