package com.drdisagree.colorblendr.utils.manager.backend

interface OverlayBackend {
    fun apply(): Boolean
    fun remove(): Boolean
}
