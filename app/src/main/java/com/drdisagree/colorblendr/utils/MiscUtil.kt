package com.drdisagree.colorblendr.utils

import android.content.Context
import android.content.Intent
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import org.json.JSONException
import org.json.JSONObject

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
}

inline fun <reified T : Parcelable> Intent.parcelable(key: String): T? = when {
    SDK_INT >= 33 -> getParcelableExtra(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelableExtra(key) as? T
}

inline fun <reified T : Parcelable> Bundle.parcelable(key: String): T? = when {
    SDK_INT >= 33 -> getParcelable(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelable(key) as? T
}