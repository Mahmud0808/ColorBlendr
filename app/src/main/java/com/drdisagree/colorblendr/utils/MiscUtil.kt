package com.drdisagree.colorblendr.utils

import android.content.Context
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import org.json.JSONException
import org.json.JSONObject

object MiscUtil {

    fun convertListToIntArray(arrayList: ArrayList<ArrayList<Int>>): Array<IntArray> {
        return arrayList.map { row -> row.toIntArray() }.toTypedArray()
    }

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
