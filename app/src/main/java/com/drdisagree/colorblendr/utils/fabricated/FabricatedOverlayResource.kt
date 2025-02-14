package com.drdisagree.colorblendr.utils.fabricated

import android.os.Parcel
import android.os.Parcelable
import android.util.TypedValue
import com.drdisagree.colorblendr.data.common.Constant

open class FabricatedOverlayResource : Parcelable {
    val overlayName: String
    val targetPackage: String
    val sourcePackage: String
    private var entries: MutableMap<String, FabricatedOverlayEntry> = HashMap()

    constructor(
        overlayName: String,
        targetPackage: String,
        sourcePackage: String = Constant.FABRICATED_OVERLAY_SOURCE_PACKAGE
    ) {
        this.overlayName = overlayName
        this.targetPackage = targetPackage
        this.sourcePackage = sourcePackage
    }

    fun setInteger(name: String, value: Int) {
        this.setInteger(name, value, null)
    }

    @Suppress("SameParameterValue")
    private fun setInteger(name: String, value: Int, configuration: String?) {
        val formattedName = formatName(name, "integer")
        entries[formattedName] = FabricatedOverlayEntry(
            formattedName,
            TypedValue.TYPE_INT_DEC,
            value,
            configuration
        )
    }

    fun setBoolean(name: String, value: Boolean) {
        this.setBoolean(name, value, null)
    }

    @Suppress("SameParameterValue")
    private fun setBoolean(name: String, value: Boolean, configuration: String?) {
        val formattedName = formatName(name, "bool")
        entries[formattedName] = FabricatedOverlayEntry(
            formattedName,
            TypedValue.TYPE_INT_BOOLEAN,
            if (value) 1 else 0,
            configuration
        )
    }

    fun setDimension(name: String, value: Int) {
        this.setDimension(name, value, null)
    }

    @Suppress("SameParameterValue")
    private fun setDimension(name: String, value: Int, configuration: String?) {
        val formattedName = formatName(name, "dimen")
        entries[formattedName] = FabricatedOverlayEntry(
            formattedName,
            TypedValue.TYPE_DIMENSION,
            value,
            configuration
        )
    }

    fun setAttribute(name: String, value: Int) {
        this.setAttribute(name, value, null)
    }

    @Suppress("SameParameterValue")
    private fun setAttribute(name: String, value: Int, configuration: String?) {
        val formattedName = formatName(name, "attr")
        entries[formattedName] = FabricatedOverlayEntry(
            formattedName,
            TypedValue.TYPE_ATTRIBUTE,
            value,
            configuration
        )
    }

    fun setColor(name: String, value: Int) {
        this.setColor(name, value, null)
    }

    fun setColor(name: String, value: Int, configuration: String?) {
        val formattedName = formatName(name, "color")
        entries[formattedName] = FabricatedOverlayEntry(
            formattedName,
            TypedValue.TYPE_INT_COLOR_ARGB8,
            value,
            configuration
        )
    }

    fun getColor(name: String): Int {
        val formattedName = formatName(name, "color")
        val entry = entries[formattedName]

        return entry?.resourceValue ?: throw RuntimeException("No entry found for $formattedName")
    }

    fun getEntries(): Map<String, FabricatedOverlayEntry> {
        return entries
    }

    fun setEntries(entries: MutableMap<String, FabricatedOverlayEntry>) {
        this.entries = entries
    }

    private fun formatName(name: String, type: String): String {
        return if (name.contains(":") && name.contains("/")) {
            name
        } else {
            "$targetPackage:$type/$name"
        }
    }

    @Suppress("DEPRECATION")
    protected constructor(`in`: Parcel) {
        overlayName = `in`.readString().toString()
        targetPackage = `in`.readString().toString()
        sourcePackage = `in`.readString().toString()
        `in`.readMap(entries, FabricatedOverlayEntry::class.java.classLoader)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(overlayName)
        dest.writeString(targetPackage)
        dest.writeString(sourcePackage)
        dest.writeMap(entries)
    }

    companion object CREATOR : Parcelable.Creator<FabricatedOverlayResource> {
        override fun createFromParcel(parcel: Parcel): FabricatedOverlayResource {
            return FabricatedOverlayResource(parcel)
        }

        override fun newArray(size: Int): Array<FabricatedOverlayResource?> {
            return arrayOfNulls(size)
        }
    }
}
