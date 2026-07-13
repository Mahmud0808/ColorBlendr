package com.drdisagree.colorblendr.data.domain

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

// Process-wide scope for global work (overlay toggles etc.) that must survive
// activity recreation; composition scopes die mid-flight when applying colors
// recreates the activity.
object AppScope : CoroutineScope by CoroutineScope(SupervisorJob() + Dispatchers.Main)
