package com.drdisagree.colorblendr.ui.adapters

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.drdisagree.colorblendr.BuildConfig
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.data.models.AboutAppModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView


class AboutAppAdapter(
    var context: Context,
    private var itemList: ArrayList<AboutAppModel>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_ABOUT_APP -> {
                AboutViewHolder(
                    LayoutInflater.from(
                        context
                    ).inflate(R.layout.view_about_app, parent, false)
                )
            }

            TYPE_CREDITS_HEADER -> {
                HeaderViewHolder(
                    LayoutInflater.from(
                        context
                    ).inflate(R.layout.view_about_app_credits_header, parent, false)
                )
            }

            TYPE_CREDITS_ITEM -> {
                ItemViewHolder(
                    LayoutInflater.from(
                        context
                    ).inflate(R.layout.view_about_app_credits_item, parent, false)
                )
            }

            else -> throw RuntimeException("There is no type that matches the type $viewType. + make sure you are using types correctly.")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is AboutViewHolder -> {
                try {
                    holder.appIcon.setImageDrawable(
                        context.packageManager.getApplicationIcon(
                            context.packageName
                        )
                    )
                } catch (ignored: PackageManager.NameNotFoundException) {
                    // Unlikely to happen, but just in case
                    holder.appIcon.setImageResource(R.mipmap.ic_launcher)
                }
                holder.versionCode.text = context.getString(
                    R.string.version_codes,
                    BuildConfig.VERSION_NAME,
                    BuildConfig.VERSION_CODE
                )
                holder.btnNews.setOnClickListener { openUrl("https://t.me/DrDsProjects") }
                holder.btnSupport.setOnClickListener { openUrl("https://t.me/DrDsProjectsChat") }
                holder.btnGithub.setOnClickListener { openUrl("https://github.com/Mahmud0808/ColorBlendr") }
                holder.developer.setOnClickListener { openUrl("https://github.com/Mahmud0808") }
                holder.buymeacoffee.setOnClickListener { openUrl("https://buymeacoffee.com/drdisagree") }
            }

            is HeaderViewHolder -> {
                holder.header.text = itemList[position].title

                if (itemList[position].title.isEmpty()) {
                    holder.itemView.visibility = View.GONE
                    holder.itemView.setLayoutParams(RecyclerView.LayoutParams(0, 0))
                }
            }

            is ItemViewHolder -> {
                holder.title.text = itemList[position].title
                holder.desc.text = itemList[position].desc
                holder.image.setRoundImageUrl(itemList[position].icon)
                holder.clickableContainer.setOnClickListener { openUrl(itemList[position].url) }
                holder.container.background = if (holder.getBindingAdapterPosition() == 1) {
                    holder.divider.visibility = View.VISIBLE
                    ContextCompat.getDrawable(context, R.drawable.bg_container_top)
                } else if (holder.getBindingAdapter() != null && holder.getBindingAdapterPosition() == holder.getBindingAdapter()!!.itemCount - 1) {
                    holder.divider.visibility = View.GONE
                    ContextCompat.getDrawable(context, R.drawable.bg_container_bottom)
                } else {
                    holder.divider.visibility = View.VISIBLE
                    ContextCompat.getDrawable(context, R.drawable.bg_container_mid)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun getItemViewType(position: Int): Int {
        return itemList[position].viewType
    }

    internal class AboutViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var appIcon: ImageView = itemView.findViewById(R.id.app_icon)
        var versionCode: MaterialTextView = itemView.findViewById(R.id.version_code)
        var btnNews: MaterialButton = itemView.findViewById(R.id.btn_news)
        var btnSupport: MaterialButton = itemView.findViewById(R.id.btn_support)
        var btnGithub: MaterialButton = itemView.findViewById(R.id.btn_github)
        var developer: LinearLayout = itemView.findViewById(R.id.developer)
        var buymeacoffee: LinearLayout = itemView.findViewById(R.id.buymeacoffee)
    }

    internal class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var header: MaterialTextView = itemView.findViewById(R.id.header)
    }

    internal class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var image: ImageView = itemView.findViewById(R.id.image)
        var title: MaterialTextView = itemView.findViewById(R.id.title)
        var desc: MaterialTextView = itemView.findViewById(R.id.desc)
        var container: ViewGroup = itemView.findViewById(R.id.container)
        var clickableContainer: ViewGroup = itemView.findViewById(R.id.clickable_container)
        var divider: View = itemView.findViewById(R.id.divider)
    }

    private fun ImageView.setRoundImageUrl(url: String) {
        Glide.with(context).load(url.replace("http://", "https://"))
            .apply(RequestOptions.centerCropTransform())
            .apply(RequestOptions().override(48, 48))
            .placeholder(R.drawable.ic_user_account)
            .error(R.drawable.ic_user_account)
            .circleCrop()
            .transition(DrawableTransitionOptions.withCrossFade()).into(this)
    }

    private fun openUrl(url: String) {
        try {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(url)
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        const val TYPE_ABOUT_APP = 0
        const val TYPE_CREDITS_HEADER = 1
        const val TYPE_CREDITS_ITEM = 2
    }
}
