package com.drdisagree.colorblendr.ui.compose.screens.palette

import androidx.annotation.StringRes
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.theme.token.Token
import com.drdisagree.colorblendr.theme.token.TokenCategory

val Token.displayName: String
    get() = name.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() }

@get:StringRes
val TokenCategory.labelRes: Int
    get() = when (this) {
        TokenCategory.SURFACE -> R.string.color_tokens_category_surface
        TokenCategory.PRIMARY -> R.string.color_tokens_category_primary
        TokenCategory.SECONDARY -> R.string.color_tokens_category_secondary
        TokenCategory.TERTIARY -> R.string.color_tokens_category_tertiary
        TokenCategory.ERROR -> R.string.color_tokens_category_error
        TokenCategory.FIXED -> R.string.color_tokens_category_fixed
        TokenCategory.FRAMEWORK -> R.string.color_tokens_category_framework
        TokenCategory.SYSTEM_UI -> R.string.color_tokens_category_system_ui
        TokenCategory.KEY_COLOR -> R.string.color_tokens_category_key_color
    }

@get:StringRes
val Token.usageRes: Int
    get() = when (this) {
        Token.PALETTE_KEY_COLOR_PRIMARY -> R.string.token_usage_palette_key_color_primary
        Token.PALETTE_KEY_COLOR_SECONDARY -> R.string.token_usage_palette_key_color_secondary
        Token.PALETTE_KEY_COLOR_TERTIARY -> R.string.token_usage_palette_key_color_tertiary
        Token.PALETTE_KEY_COLOR_NEUTRAL -> R.string.token_usage_palette_key_color_neutral
        Token.PALETTE_KEY_COLOR_NEUTRAL_VARIANT -> R.string.token_usage_palette_key_color_neutral_variant
        Token.PALETTE_KEY_COLOR_ERROR -> R.string.token_usage_palette_key_color_error
        Token.BACKGROUND -> R.string.token_usage_background
        Token.ON_BACKGROUND -> R.string.token_usage_on_background
        Token.SURFACE -> R.string.token_usage_surface
        Token.SURFACE_DIM -> R.string.token_usage_surface_dim
        Token.SURFACE_BRIGHT -> R.string.token_usage_surface_bright
        Token.SURFACE_CONTAINER_LOWEST -> R.string.token_usage_surface_container_lowest
        Token.SURFACE_CONTAINER_LOW -> R.string.token_usage_surface_container_low
        Token.SURFACE_CONTAINER -> R.string.token_usage_surface_container
        Token.SURFACE_CONTAINER_HIGH -> R.string.token_usage_surface_container_high
        Token.SURFACE_CONTAINER_HIGHEST -> R.string.token_usage_surface_container_highest
        Token.ON_SURFACE -> R.string.token_usage_on_surface
        Token.SURFACE_VARIANT -> R.string.token_usage_surface_variant
        Token.ON_SURFACE_VARIANT -> R.string.token_usage_on_surface_variant
        Token.OUTLINE -> R.string.token_usage_outline
        Token.OUTLINE_VARIANT -> R.string.token_usage_outline_variant
        Token.INVERSE_SURFACE -> R.string.token_usage_inverse_surface
        Token.INVERSE_ON_SURFACE -> R.string.token_usage_inverse_on_surface
        Token.SHADOW -> R.string.token_usage_shadow
        Token.SCRIM -> R.string.token_usage_scrim
        Token.SURFACE_TINT -> R.string.token_usage_surface_tint
        Token.PRIMARY -> R.string.token_usage_primary
        Token.PRIMARY_DIM -> R.string.token_usage_primary_dim
        Token.ON_PRIMARY -> R.string.token_usage_on_primary
        Token.PRIMARY_CONTAINER -> R.string.token_usage_primary_container
        Token.ON_PRIMARY_CONTAINER -> R.string.token_usage_on_primary_container
        Token.INVERSE_PRIMARY -> R.string.token_usage_inverse_primary
        Token.SECONDARY -> R.string.token_usage_secondary
        Token.SECONDARY_DIM -> R.string.token_usage_secondary_dim
        Token.ON_SECONDARY -> R.string.token_usage_on_secondary
        Token.SECONDARY_CONTAINER -> R.string.token_usage_secondary_container
        Token.ON_SECONDARY_CONTAINER -> R.string.token_usage_on_secondary_container
        Token.TERTIARY -> R.string.token_usage_tertiary
        Token.TERTIARY_DIM -> R.string.token_usage_tertiary_dim
        Token.ON_TERTIARY -> R.string.token_usage_on_tertiary
        Token.TERTIARY_CONTAINER -> R.string.token_usage_tertiary_container
        Token.ON_TERTIARY_CONTAINER -> R.string.token_usage_on_tertiary_container
        Token.ERROR -> R.string.token_usage_error
        Token.ERROR_DIM -> R.string.token_usage_error_dim
        Token.ON_ERROR -> R.string.token_usage_on_error
        Token.ERROR_CONTAINER -> R.string.token_usage_error_container
        Token.ON_ERROR_CONTAINER -> R.string.token_usage_on_error_container
        Token.CONTROL_ACTIVATED -> R.string.token_usage_control_activated
        Token.CONTROL_NORMAL -> R.string.token_usage_control_normal
        Token.CONTROL_HIGHLIGHT -> R.string.token_usage_control_highlight
        Token.TEXT_PRIMARY_INVERSE -> R.string.token_usage_text_primary_inverse
        Token.TEXT_SECONDARY_AND_TERTIARY_INVERSE -> R.string.token_usage_text_secondary_and_tertiary_inverse
        Token.TEXT_PRIMARY_INVERSE_DISABLE_ONLY -> R.string.token_usage_text_primary_inverse_disable_only
        Token.TEXT_SECONDARY_AND_TERTIARY_INVERSE_DISABLED -> R.string.token_usage_text_secondary_and_tertiary_inverse_disabled
        Token.TEXT_HINT_INVERSE -> R.string.token_usage_text_hint_inverse
        Token.GEN_WIDGET -> R.string.token_usage_gen_widget
        Token.PRIMARY_FIXED -> R.string.token_usage_primary_fixed
        Token.PRIMARY_FIXED_DIM -> R.string.token_usage_primary_fixed_dim
        Token.ON_PRIMARY_FIXED -> R.string.token_usage_on_primary_fixed
        Token.ON_PRIMARY_FIXED_VARIANT -> R.string.token_usage_on_primary_fixed_variant
        Token.SECONDARY_FIXED -> R.string.token_usage_secondary_fixed
        Token.SECONDARY_FIXED_DIM -> R.string.token_usage_secondary_fixed_dim
        Token.ON_SECONDARY_FIXED -> R.string.token_usage_on_secondary_fixed
        Token.ON_SECONDARY_FIXED_VARIANT -> R.string.token_usage_on_secondary_fixed_variant
        Token.TERTIARY_FIXED -> R.string.token_usage_tertiary_fixed
        Token.TERTIARY_FIXED_DIM -> R.string.token_usage_tertiary_fixed_dim
        Token.ON_TERTIARY_FIXED -> R.string.token_usage_on_tertiary_fixed
        Token.ON_TERTIARY_FIXED_VARIANT -> R.string.token_usage_on_tertiary_fixed_variant
        Token.WIDGET_BACKGROUND -> R.string.token_usage_widget_background
        Token.CLOCK_HOUR -> R.string.token_usage_clock_hour
        Token.CLOCK_MINUTE -> R.string.token_usage_clock_minute
        Token.CLOCK_SECOND -> R.string.token_usage_clock_second
        Token.WEATHER_TEMP -> R.string.token_usage_weather_temp
        Token.THEME_APP -> R.string.token_usage_theme_app
        Token.ON_THEME_APP -> R.string.token_usage_on_theme_app
        Token.THEME_APP_RING -> R.string.token_usage_theme_app_ring
        Token.THEME_NOTIF -> R.string.token_usage_theme_notif
        Token.ON_THEME_APP_RING -> R.string.token_usage_on_theme_app_ring
        Token.BRAND_A -> R.string.token_usage_brand_a
        Token.BRAND_B -> R.string.token_usage_brand_b
        Token.BRAND_C -> R.string.token_usage_brand_c
        Token.BRAND_D -> R.string.token_usage_brand_d
        Token.UNDER_SURFACE -> R.string.token_usage_under_surface
        Token.SHADE_ACTIVE -> R.string.token_usage_shade_active
        Token.ON_SHADE_ACTIVE -> R.string.token_usage_on_shade_active
        Token.ON_SHADE_ACTIVE_VARIANT -> R.string.token_usage_on_shade_active_variant
        Token.SHADE_INACTIVE -> R.string.token_usage_shade_inactive
        Token.ON_SHADE_INACTIVE -> R.string.token_usage_on_shade_inactive
        Token.ON_SHADE_INACTIVE_VARIANT -> R.string.token_usage_on_shade_inactive_variant
        Token.SHADE_DISABLED -> R.string.token_usage_shade_disabled
        Token.OVERVIEW_BACKGROUND -> R.string.token_usage_overview_background
    }
