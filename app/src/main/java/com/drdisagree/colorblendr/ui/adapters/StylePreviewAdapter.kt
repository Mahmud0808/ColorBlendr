package com.drdisagree.colorblendr.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.common.Const.MONET_LAST_UPDATED
import com.drdisagree.colorblendr.common.Const.MONET_STYLE
import com.drdisagree.colorblendr.common.Const.MONET_STYLE_ORIGINAL_NAME
import com.drdisagree.colorblendr.config.RPrefs.putLong
import com.drdisagree.colorblendr.config.RPrefs.putString
import com.drdisagree.colorblendr.ui.widgets.StylePreviewWidget
import com.drdisagree.colorblendr.utils.ColorSchemeUtil.getStyleNameForRootless
import com.drdisagree.colorblendr.utils.ColorSchemeUtil.stringToEnumMonetStyle
import com.drdisagree.colorblendr.utils.MiscUtil.getOriginalString
import com.drdisagree.colorblendr.utils.MiscUtil.toPx

class StylePreviewAdapter(
    private val styleList: List<StyleData>
) : RecyclerView.Adapter<StylePreviewAdapter.StyleViewHolder>() {

    data class StyleData(
        val titleResId: Int,
        val descriptionResId: Int,
        val isEnabled: Boolean,
        var isSelected: Boolean
    )

    private var selectedPosition: Int = RecyclerView.NO_POSITION

    inner class StyleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val stylePreviewWidget: StylePreviewWidget =
            itemView.findViewById(R.id.style_preview_widget)

        fun bind(styleData: StyleData) {
            stylePreviewWidget.apply {
                setTitle(styleData.titleResId)
                setDescription(styleData.descriptionResId)
                isEnabled = styleData.isEnabled
                isSelected = styleData.isSelected

                if (styleData.isSelected) {
                    selectedPosition = bindingAdapterPosition
                }

                setOnClickListener {
                    if (selectedPosition != RecyclerView.NO_POSITION &&
                        selectedPosition != bindingAdapterPosition
                    ) {
                        Log.d("StylePreviewAdapter", "selectedPosition: $selectedPosition")
                        styleList[selectedPosition].isSelected = false
                        notifyItemChanged(selectedPosition)
                    }

                    styleList[bindingAdapterPosition].isSelected = true
                    notifyItemChanged(bindingAdapterPosition)
                    selectedPosition = bindingAdapterPosition

                    putLong(MONET_LAST_UPDATED, System.currentTimeMillis())
                    putString(
                        MONET_STYLE,
                        stringToEnumMonetStyle(
                            context,
                            styleData.titleResId.getOriginalString()
                        ).toString()
                    )
                    putString(
                        MONET_STYLE_ORIGINAL_NAME,
                        styleData.titleResId.getStyleNameForRootless()
                    )
                    applyColorScheme()
                }

                if (bindingAdapterPosition == 0) {
                    (layoutParams as ViewGroup.MarginLayoutParams).apply {
                        setMargins(
                            leftMargin,
                            context.toPx(12),
                            rightMargin,
                            bottomMargin
                        )
                        setLayoutParams(this)
                    }
                }
            }
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
}
