package com.drdisagree.colorblendr.ui.adapters

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.data.common.Utilities.clearOriginalStyleName
import com.drdisagree.colorblendr.data.common.Utilities.getCurrentCustomStyle
import com.drdisagree.colorblendr.data.common.Utilities.getCurrentMonetStyle
import com.drdisagree.colorblendr.data.common.Utilities.resetCustomStyle
import com.drdisagree.colorblendr.data.common.Utilities.setCurrentCustomStyle
import com.drdisagree.colorblendr.data.common.Utilities.setCurrentMonetStyle
import com.drdisagree.colorblendr.data.common.Utilities.setOriginalStyleName
import com.drdisagree.colorblendr.data.config.Prefs.toPrefs
import com.drdisagree.colorblendr.data.enums.MONET
import com.drdisagree.colorblendr.data.models.CustomStyleModel
import com.drdisagree.colorblendr.data.models.StyleModel
import com.drdisagree.colorblendr.ui.fragments.StylesFragment
import com.drdisagree.colorblendr.ui.widgets.StylePreviewWidget
import com.drdisagree.colorblendr.utils.app.BackupRestore
import com.drdisagree.colorblendr.utils.app.MiscUtil.toPx
import com.drdisagree.colorblendr.utils.colors.ColorSchemeUtil.getStyleNameForRootless
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class StylePreviewAdapter(
    private val fragment: StylesFragment,
    private var styleList: MutableList<StyleModel>
) : RecyclerView.Adapter<StylePreviewAdapter.StyleViewHolder>() {

    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())
    private var selectedPosition: Int = RecyclerView.NO_POSITION
    private var selectedStyle: MONET? = getCurrentMonetStyle()
    private var selectedCustomStyle: String? = getCurrentCustomStyle()

    init {
        val noCustomStyle = !styleList.any { it.customStyle?.styleId == selectedCustomStyle }

        if (noCustomStyle) {
            resetCustomStyle()
            selectedCustomStyle = null
        }

        if (selectedStyle == null && selectedCustomStyle == null) {
            selectedStyle = MONET.TONAL_SPOT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StyleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.item_style_preview,
            parent,
            false
        )
        return StyleViewHolder(view)
    }

    override fun onBindViewHolder(holder: StyleViewHolder, position: Int) {
        holder.bind(styleList[position])
    }

    override fun getItemCount(): Int = styleList.size

    fun addStyle(newStyle: StyleModel) {
        selectedCustomStyle = newStyle.customStyle!!.styleId

        if (selectedPosition != RecyclerView.NO_POSITION) {
            notifyItemChanged(selectedPosition)
        }

        styleList.add(newStyle)
        notifyItemInserted(styleList.size - 1)

        setCurrentCustomStyle(newStyle.customStyle.styleId)
    }

    fun updateStyle(style: StyleModel) {
        val position = styleList.indexOfFirst {
            it.customStyle?.styleId == style.customStyle?.styleId
        }

        if (position != -1) {
            styleList[position] = style
            notifyItemChanged(position)
        }
    }

    fun removeStyle(customStyle: CustomStyleModel?) {
        val position = styleList.indexOfFirst {
            it.customStyle?.styleId == customStyle?.styleId
        }

        if (position != -1) {
            if (styleList[position].customStyle?.styleId == selectedCustomStyle) {
                selectedCustomStyle = null
                val newPosition = styleList.indexOfFirst { it.monetStyle == selectedStyle }
                notifyItemChanged(newPosition)
            }

            styleList.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    inner class StyleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val stylePreviewWidget: StylePreviewWidget =
            itemView.findViewById(R.id.style_preview_widget)

        fun bind(styleData: StyleModel) {
            stylePreviewWidget.apply {
                if (styleData.customStyle == null) {
                    setupStylePreview(styleData)
                } else {
                    setupCustomStylePreview(styleData)
                }

                (layoutParams as ViewGroup.MarginLayoutParams).apply {
                    if (bindingAdapterPosition == 0) {
                        setMargins(
                            leftMargin,
                            context.toPx(12),
                            rightMargin,
                            bottomMargin
                        )
                    } else {
                        setMargins(
                            leftMargin,
                            0,
                            rightMargin,
                            bottomMargin
                        )
                    }
                    setLayoutParams(this)
                }
            }
        }

        private fun StylePreviewWidget.setupStylePreview(styleData: StyleModel) {
            setTitle(styleData.titleResId)
            setDescription(styleData.descriptionResId)
            isEnabled = styleData.isEnabled
            isSelected = styleData.monetStyle == selectedStyle && selectedCustomStyle == null

            if (isSelected) {
                selectedPosition = bindingAdapterPosition
            }

            coroutineScope.launch {
                // call again because recyclerview recycles views and
                // they don't get updated if we don't call this
                resetCustomColors()
                setColorPreview()
            }

            setOnClickListener {
                selectedStyle = styleData.monetStyle
                selectedCustomStyle = null

                updateItemView()

                coroutineScope.launch {
                    // update preferences and apply colors
                    setCurrentMonetStyle(styleData.monetStyle)
                    resetCustomStyle()
                    setOriginalStyleName(styleData.titleResId.getStyleNameForRootless())
                    applyColorScheme()
                }
            }

            setOnLongClickListener(null)
        }

        private fun StylePreviewWidget.setupCustomStylePreview(styleData: StyleModel) {
            val customStyle = styleData.customStyle!!
            val prefsMap = customStyle.prefsGson.toPrefs()

            setTitle(customStyle.styleName)
            setDescription(customStyle.description)
            isEnabled = styleData.isEnabled
            isSelected = customStyle.styleId == selectedCustomStyle

            if (isSelected) {
                selectedPosition = bindingAdapterPosition
            }

            // call after setting title
            setCustomColors(customStyle.palette)

            setOnClickListener {
                selectedCustomStyle = customStyle.styleId

                updateItemView()

                coroutineScope.launch {
                    // restore theme preferences
                    BackupRestore.restorePrefsMap(prefsMap)

                    // update preferences and apply colors
                    setCurrentCustomStyle(customStyle.styleId)
                    clearOriginalStyleName()
                    applyColorScheme()
                }
            }

            val popupMenu = PopupMenu(context, this, Gravity.END, 0, R.style.MyPopupMenu).apply {
                menuInflater.inflate(R.menu.custom_style_menu, menu)

                setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.edit -> {
                            fragment.showNewStyleDialog(
                                title = customStyle.styleName,
                                desc = customStyle.description,
                                callback = { title, desc ->
                                    customStyle.styleName = title
                                    customStyle.description = desc

                                    coroutineScope.launch {
                                        fragment.editCustomStyle(
                                            title = title,
                                            description = desc,
                                            styleId = customStyle.styleId
                                        )
                                    }
                                }
                            )
                            true
                        }

                        R.id.delete -> {
                            coroutineScope.launch {
                                fragment.deleteCustomStyle(styleId = customStyle.styleId)
                            }
                            true
                        }

                        else -> false
                    }
                }
            }

            setOnLongClickListener {
                popupMenu.show()
                true
            }
        }

        private fun updateItemView() {
            if (selectedPosition != RecyclerView.NO_POSITION &&
                selectedPosition != bindingAdapterPosition
            ) {
                // update previous selected position
                notifyItemChanged(selectedPosition)
            }

            // update current selected position
            notifyItemChanged(bindingAdapterPosition)
            selectedPosition = bindingAdapterPosition
        }
    }
}
