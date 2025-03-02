package com.drdisagree.colorblendr.utils.app

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.TypedArray
import android.graphics.Rect
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Parcelable
import android.util.TypedValue
import android.view.View
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.drdisagree.colorblendr.ColorBlendr.Companion.appContext
import com.google.android.material.appbar.MaterialToolbar
import org.json.JSONException
import org.json.JSONObject
import java.util.Locale


object MiscUtil {

    fun setToolbarTitle(
        context: Context,
        @StringRes title: Int,
        showBackButton: Boolean,
        toolbar: MaterialToolbar?
    ) {
        (context as AppCompatActivity).setSupportActionBar(toolbar)
        val actionBar = context.supportActionBar
        if (actionBar != null) {
            context.supportActionBar!!.setTitle(title)
            context.supportActionBar!!.setDisplayHomeAsUpEnabled(showBackButton)
            context.supportActionBar!!.setDisplayShowHomeEnabled(showBackButton)
        }
    }

    @Throws(JSONException::class)
    fun mergeJsonStrings(target: String?, source: String?): String {
        var targetTemp = target
        var sourceTemp = source

        if (target.isNullOrEmpty()) {
            targetTemp = JSONObject().toString()
        }

        if (source.isNullOrEmpty()) {
            sourceTemp = JSONObject().toString()
        }

        val targetJson = JSONObject(targetTemp!!)
        val sourceJson = JSONObject(sourceTemp!!)

        return mergeJsonObjects(targetJson, sourceJson).toString()
    }

    @Throws(JSONException::class)
    fun mergeJsonObjects(target: JSONObject, source: JSONObject): JSONObject {
        val keys = source.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            target.put(key, source[key])
        }
        return target
    }

    fun Int.getOriginalString(): String {
        val config = Configuration(appContext.resources.configuration)
        config.setLocale(Locale("en"))
        return appContext.createConfigurationContext(config).getString(this)
    }

    fun Context.toPx(dp: Int): Int = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp.toFloat(),
        resources.displayMetrics
    ).toInt()

    fun Context.getDialogPreferredPadding(): Int {
        val typedValue = TypedValue()
        appContext.theme.resolveAttribute(android.R.attr.dialogPreferredPadding, typedValue, true)
        val typedArray: TypedArray = obtainStyledAttributes(
            typedValue.data,
            intArrayOf(android.R.attr.dialogPreferredPadding)
        )
        val padding = typedArray.getDimensionPixelSize(0, toPx(20))
        typedArray.recycle()
        return padding
    }
}

inline fun <reified T : Parcelable> Intent.parcelable(key: String): T? = when {
    SDK_INT >= 33 -> getParcelableExtra(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelableExtra(key) as? T
}

inline fun <reified T : Parcelable> Bundle.parcelable(key: String): T? = when {
    SDK_INT >= 33 -> getParcelable(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelable(key) as? T
}

class DividerItemDecoration(context: Context?, private var spacing: Int) : ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect.bottom = spacing
    }
}