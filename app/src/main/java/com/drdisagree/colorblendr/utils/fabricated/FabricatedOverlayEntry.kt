package com.drdisagree.colorblendr.utils.fabricated

import android.os.Build
import android.os.Parcel
import android.os.Parcelable

open class FabricatedOverlayEntry : Parcelable {
    var resourceName: String?
    var resourceType: Int
    var resourceValue: Int
    private var configuration: String?

    constructor(
        resourceName: String?,
        resourceType: Int,
        resourceValue: Int,
        configuration: String? = null
    ) {
        this.resourceName = resourceName
        this.resourceType = resourceType
        this.resourceValue = resourceValue
        this.configuration = configuration
    }

    fun getConfiguration(): String? {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) null else configuration
    }

    fun setConfiguration(configuration: String?) {
        this.configuration = configuration
    }

    protected constructor(`in`: Parcel) {
        resourceName = `in`.readString()
        resourceType = `in`.readInt()
        resourceValue = `in`.readInt()
        configuration = `in`.readString()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(resourceName)
        dest.writeInt(resourceType)
        dest.writeInt(resourceValue)
        dest.writeString(configuration)
    }

    companion object CREATOR : Parcelable.Creator<FabricatedOverlayEntry> {
        override fun createFromParcel(parcel: Parcel): FabricatedOverlayEntry {
            return FabricatedOverlayEntry(parcel)
        }

        override fun newArray(size: Int): Array<FabricatedOverlayEntry?> {
            return arrayOfNulls(size)
        }
    }
}
