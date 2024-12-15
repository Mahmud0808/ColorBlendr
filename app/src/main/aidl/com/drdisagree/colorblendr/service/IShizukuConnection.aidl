package com.drdisagree.colorblendr.service;

interface IShizukuConnection {
    void destroy() = 16777114;
    void exit() = 1;
    void applyFabricatedColors(String jsonString) = 2;
    void removeFabricatedColors() = 3;
    String getCurrentSettings() = 4;
    void applyFabricatedColorsSamsung(String paletteArray) = 5;
    void applyThemedIconSamsung(boolean isThemed) = 6;
    void removeFabricatedColorsSamsung() = 7;
}