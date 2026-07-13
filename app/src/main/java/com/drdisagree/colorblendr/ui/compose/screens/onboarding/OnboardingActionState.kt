package com.drdisagree.colorblendr.ui.compose.screens.onboarding

sealed interface OnboardingActionState {
    data object Idle : OnboardingActionState
    data object Connecting : OnboardingActionState
    data class Error(val message: String) : OnboardingActionState
}
