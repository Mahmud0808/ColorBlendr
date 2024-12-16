package com.drdisagree.colorblendr.service;

interface IShizukuConnection {
    void destroy() = 16777114;
    void exit() = 1;
    void applyFabricatedColors(String jsonString) = 2;
    void removeFabricatedColors() = 3;
    String getCurrentSettings() = 4;
    void applyFabricatedColorsSamsung(String jsonString, String paletteArray) = 5;
    boolean isThemedIconEnabledSamsung() = 6;
    void enableThemedIconSamsung(boolean isThemed) = 7;
    void removeFabricatedColorsSamsung() = 8;
}