package me.jfenn.colorpickerdialog.compose.dialogs

import me.jfenn.colorpickerdialog.R

enum class ColorPickerType {
    WHEEL,
    RGB,
    HSV,
    PRESETS,
    IMAGE
}

internal fun ColorPickerType.titleRes(): Int = when (this) {
    ColorPickerType.WHEEL -> R.string.colorPickerDialog_wheel
    ColorPickerType.RGB -> R.string.colorPickerDialog_rgb
    ColorPickerType.HSV -> R.string.colorPickerDialog_hsv
    ColorPickerType.PRESETS -> R.string.colorPickerDialog_preset
    ColorPickerType.IMAGE -> R.string.colorPickerDialog_image
}
