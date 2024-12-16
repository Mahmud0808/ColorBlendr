package com.drdisagree.colorblendr.utils

import android.annotation.SuppressLint
import android.os.Build
import android.os.Environment
import android.text.TextUtils
import com.topjohnwu.superuser.Shell
import java.io.File
import java.io.FileInputStream
import java.util.Locale
import java.util.Properties

/**
 * Source: [...](https://github.com/Blankj/AndroidUtilCode/blob/master/lib/utilcode/src/main/java/com/blankj/utilcode/util/RomUtils.java)
 */
@Suppress("unused")
object RomUtil {

    private val ROM_HUAWEI = arrayOf("huawei")
    private val ROM_VIVO = arrayOf("vivo")
    private val ROM_XIAOMI = arrayOf("xiaomi")
    private val ROM_OPPO = arrayOf("oppo")
    private val ROM_LEECO = arrayOf("leeco", "letv")
    private val ROM_360 = arrayOf("360", "qiku")
    private val ROM_ZTE = arrayOf("zte")
    private val ROM_ONEPLUS = arrayOf("oneplus")
    private val ROM_NUBIA = arrayOf("nubia")
    private val ROM_COOLPAD = arrayOf("coolpad", "yulong")
    private val ROM_LG = arrayOf("lg", "lge")
    private val ROM_GOOGLE = arrayOf("google")
    private val ROM_SAMSUNG = arrayOf("samsung")
    private val ROM_MEIZU = arrayOf("meizu")
    private val ROM_LENOVO = arrayOf("lenovo")
    private val ROM_SMARTISAN = arrayOf("smartisan", "deltainno")
    private val ROM_HTC = arrayOf("htc")
    private val ROM_SONY = arrayOf("sony")
    private val ROM_GIONEE = arrayOf("gionee", "amigo")
    private val ROM_MOTOROLA = arrayOf("motorola")

    private const val VERSION_PROPERTY_HUAWEI = "ro.build.version.emui"
    private const val VERSION_PROPERTY_VIVO = "ro.vivo.os.build.display.id"
    private const val VERSION_PROPERTY_XIAOMI = "ro.build.version.incremental"
    private const val VERSION_PROPERTY_OPPO = "ro.build.version.opporom"
    private const val VERSION_PROPERTY_LEECO = "ro.letv.release.version"
    private const val VERSION_PROPERTY_360 = "ro.build.uiversion"
    private const val VERSION_PROPERTY_ZTE = "ro.build.MiFavor_version"
    private const val VERSION_PROPERTY_ONEPLUS = "ro.rom.version"
    private const val VERSION_PROPERTY_NUBIA = "ro.build.rom.id"
    private const val UNKNOWN = "unknown"

    private var bean: RomInfo? = null

    val isHuawei: Boolean
        /**
         * Return whether the rom is made by huawei.
         *
         * @return `true`: yes<br></br>`false`: no
         */
        get() = ROM_HUAWEI[0] == romInfo!!.name

    val isVivo: Boolean
        /**
         * Return whether the rom is made by vivo.
         *
         * @return `true`: yes<br></br>`false`: no
         */
        get() = ROM_VIVO[0] == romInfo!!.name

    val isXiaomi: Boolean
        /**
         * Return whether the rom is made by xiaomi.
         *
         * @return `true`: yes<br></br>`false`: no
         */
        get() = ROM_XIAOMI[0] == romInfo!!.name

    val isOppo: Boolean
        /**
         * Return whether the rom is made by oppo.
         *
         * @return `true`: yes<br></br>`false`: no
         */
        get() = ROM_OPPO[0] == romInfo!!.name

    val isLeeco: Boolean
        /**
         * Return whether the rom is made by leeco.
         *
         * @return `true`: yes<br></br>`false`: no
         */
        get() = ROM_LEECO[0] == romInfo!!.name

    /**
     * Return whether the rom is made by 360.
     *
     * @return `true`: yes<br></br>`false`: no
     */
    fun is360(): Boolean {
        return ROM_360[0] == romInfo!!.name
    }

    val isZte: Boolean
        /**
         * Return whether the rom is made by zte.
         *
         * @return `true`: yes<br></br>`false`: no
         */
        get() = ROM_ZTE[0] == romInfo!!.name

    val isOneplus: Boolean
        /**
         * Return whether the rom is made by oneplus.
         *
         * @return `true`: yes<br></br>`false`: no
         */
        get() = ROM_ONEPLUS[0] == romInfo!!.name

    val isNubia: Boolean
        /**
         * Return whether the rom is made by nubia.
         *
         * @return `true`: yes<br></br>`false`: no
         */
        get() = ROM_NUBIA[0] == romInfo!!.name

    val isCoolpad: Boolean
        /**
         * Return whether the rom is made by coolpad.
         *
         * @return `true`: yes<br></br>`false`: no
         */
        get() = ROM_COOLPAD[0] == romInfo!!.name

    val isLg: Boolean
        /**
         * Return whether the rom is made by lg.
         *
         * @return `true`: yes<br></br>`false`: no
         */
        get() = ROM_LG[0] == romInfo!!.name

    val isGoogle: Boolean
        /**
         * Return whether the rom is made by google.
         *
         * @return `true`: yes<br></br>`false`: no
         */
        get() = ROM_GOOGLE[0] == romInfo!!.name

    val isSamsung: Boolean
        /**
         * Return whether the rom is made by samsung.
         *
         * @return `true`: yes<br></br>`false`: no
         */
        get() = ROM_SAMSUNG[0] == romInfo!!.name

    val isMeizu: Boolean
        /**
         * Return whether the rom is made by meizu.
         *
         * @return `true`: yes<br></br>`false`: no
         */
        get() = ROM_MEIZU[0] == romInfo!!.name

    val isLenovo: Boolean
        /**
         * Return whether the rom is made by lenovo.
         *
         * @return `true`: yes<br></br>`false`: no
         */
        get() = ROM_LENOVO[0] == romInfo!!.name

    val isSmartisan: Boolean
        /**
         * Return whether the rom is made by smartisan.
         *
         * @return `true`: yes<br></br>`false`: no
         */
        get() = ROM_SMARTISAN[0] == romInfo!!.name

    val isHtc: Boolean
        /**
         * Return whether the rom is made by htc.
         *
         * @return `true`: yes<br></br>`false`: no
         */
        get() = ROM_HTC[0] == romInfo!!.name

    val isSony: Boolean
        /**
         * Return whether the rom is made by sony.
         *
         * @return `true`: yes<br></br>`false`: no
         */
        get() = ROM_SONY[0] == romInfo!!.name

    val isGionee: Boolean
        /**
         * Return whether the rom is made by gionee.
         *
         * @return `true`: yes<br></br>`false`: no
         */
        get() = ROM_GIONEE[0] == romInfo!!.name

    val isMotorola: Boolean
        /**
         * Return whether the rom is made by motorola.
         *
         * @return `true`: yes<br></br>`false`: no
         */
        get() = ROM_MOTOROLA[0] == romInfo!!.name

    val isOneUI: Boolean
        /**
         * Return whether the rom is oneui.
         *
         * @return `true`: yes<br></br>`false`: no
         */
        get() {
            if (!isSamsung) return false

            return try {
                Build.VERSION::class.java.getDeclaredField("SEM_PLATFORM_INT").getInt(null) >= 90000
            } catch (e: Exception) {
                false
            }
        }

    val romInfo: RomInfo?
        /**
         * Return the ROM's information.
         *
         * @return the ROM's information
         */
        get() {
            if (bean != null) return bean
            bean = RomInfo()
            val brand = brand
            val manufacturer = manufacturer
            if (isRightRom(brand, manufacturer, *ROM_HUAWEI)) {
                bean!!.name = ROM_HUAWEI[0]
                val version = getRomVersion(VERSION_PROPERTY_HUAWEI)
                val temp =
                    version.split("_".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                if (temp.size > 1) {
                    bean!!.version = temp[1]
                } else {
                    bean!!.version = version
                }
                return bean
            }
            if (isRightRom(brand, manufacturer, *ROM_VIVO)) {
                bean!!.name = ROM_VIVO[0]
                bean!!.version = getRomVersion(VERSION_PROPERTY_VIVO)
                return bean
            }
            if (isRightRom(brand, manufacturer, *ROM_XIAOMI)) {
                bean!!.name = ROM_XIAOMI[0]
                bean!!.version = getRomVersion(VERSION_PROPERTY_XIAOMI)
                return bean
            }
            if (isRightRom(brand, manufacturer, *ROM_OPPO)) {
                bean!!.name = ROM_OPPO[0]
                bean!!.version = getRomVersion(VERSION_PROPERTY_OPPO)
                return bean
            }
            if (isRightRom(brand, manufacturer, *ROM_LEECO)) {
                bean!!.name = ROM_LEECO[0]
                bean!!.version = getRomVersion(VERSION_PROPERTY_LEECO)
                return bean
            }

            if (isRightRom(brand, manufacturer, *ROM_360)) {
                bean!!.name = ROM_360[0]
                bean!!.version = getRomVersion(VERSION_PROPERTY_360)
                return bean
            }
            if (isRightRom(brand, manufacturer, *ROM_ZTE)) {
                bean!!.name = ROM_ZTE[0]
                bean!!.version = getRomVersion(VERSION_PROPERTY_ZTE)
                return bean
            }
            if (isRightRom(brand, manufacturer, *ROM_ONEPLUS)) {
                bean!!.name = ROM_ONEPLUS[0]
                bean!!.version = getRomVersion(VERSION_PROPERTY_ONEPLUS)
                return bean
            }
            if (isRightRom(brand, manufacturer, *ROM_NUBIA)) {
                bean!!.name = ROM_NUBIA[0]
                bean!!.version = getRomVersion(VERSION_PROPERTY_NUBIA)
                return bean
            }

            if (isRightRom(brand, manufacturer, *ROM_COOLPAD)) {
                bean!!.name = ROM_COOLPAD[0]
            } else if (isRightRom(brand, manufacturer, *ROM_LG)) {
                bean!!.name = ROM_LG[0]
            } else if (isRightRom(brand, manufacturer, *ROM_GOOGLE)) {
                bean!!.name = ROM_GOOGLE[0]
            } else if (isRightRom(brand, manufacturer, *ROM_SAMSUNG)) {
                bean!!.name = ROM_SAMSUNG[0]
            } else if (isRightRom(brand, manufacturer, *ROM_MEIZU)) {
                bean!!.name = ROM_MEIZU[0]
            } else if (isRightRom(brand, manufacturer, *ROM_LENOVO)) {
                bean!!.name = ROM_LENOVO[0]
            } else if (isRightRom(brand, manufacturer, *ROM_SMARTISAN)) {
                bean!!.name = ROM_SMARTISAN[0]
            } else if (isRightRom(brand, manufacturer, *ROM_HTC)) {
                bean!!.name = ROM_HTC[0]
            } else if (isRightRom(brand, manufacturer, *ROM_SONY)) {
                bean!!.name = ROM_SONY[0]
            } else if (isRightRom(brand, manufacturer, *ROM_GIONEE)) {
                bean!!.name = ROM_GIONEE[0]
            } else if (isRightRom(brand, manufacturer, *ROM_MOTOROLA)) {
                bean!!.name = ROM_MOTOROLA[0]
            } else {
                bean!!.name = manufacturer
            }
            bean!!.version = getRomVersion("")
            return bean
        }

    private fun isRightRom(brand: String, manufacturer: String, vararg names: String): Boolean {
        for (name in names) {
            if (brand.contains(name) || manufacturer.contains(name)) {
                return true
            }
        }
        return false
    }

    private val manufacturer: String
        get() {
            try {
                val manufacturer = Build.MANUFACTURER
                if (!TextUtils.isEmpty(manufacturer)) {
                    return manufacturer.lowercase(Locale.getDefault())
                }
            } catch (ignore: Throwable) { /**/
            }
            return UNKNOWN
        }

    private val brand: String
        get() {
            try {
                val brand = Build.BRAND
                if (!TextUtils.isEmpty(brand)) {
                    return brand.lowercase(Locale.getDefault())
                }
            } catch (ignore: Throwable) { /**/
            }
            return UNKNOWN
        }

    private fun getRomVersion(propertyName: String): String {
        var ret = ""
        if (!TextUtils.isEmpty(propertyName)) {
            ret = getSystemProperty(propertyName)
        }
        if (TextUtils.isEmpty(ret) || ret == UNKNOWN) {
            try {
                val display = Build.DISPLAY
                if (!TextUtils.isEmpty(display)) {
                    ret = display.lowercase(Locale.getDefault())
                }
            } catch (ignore: Throwable) { /**/
            }
        }
        if (TextUtils.isEmpty(ret)) {
            return UNKNOWN
        }
        return ret
    }

    private fun getSystemProperty(name: String): String {
        val prop = getSystemPropertyByShell(name)
        if (!TextUtils.isEmpty(prop)) {
            return prop
        }
        return getSystemPropertyByStream(name)
    }

    private fun getSystemPropertyByShell(propName: String): String {
        return Shell.cmd("getprop $propName").exec().out[0]
    }

    private fun getSystemPropertyByStream(key: String): String {
        try {
            val prop = Properties()
            val `is` = FileInputStream(
                File(Environment.getRootDirectory(), "build.prop")
            )
            prop.load(`is`)
            return prop.getProperty(key, "")
        } catch (ignore: Exception) { /**/
        }
        return ""
    }

    private fun getSystemPropertyByReflect(key: String): String {
        try {
            @SuppressLint("PrivateApi") val clz = Class.forName("android.os.SystemProperties")
            val getMethod = clz.getMethod(
                "get",
                String::class.java,
                String::class.java
            )
            return getMethod.invoke(clz, key, "") as String
        } catch (e: Exception) { /**/
        }
        return ""
    }

    class RomInfo {
        var name: String? = null
        var version: String? = null

        override fun toString(): String {
            return "RomInfo{name=" + name +
                    ", version=" + version + "}"
        }
    }
}