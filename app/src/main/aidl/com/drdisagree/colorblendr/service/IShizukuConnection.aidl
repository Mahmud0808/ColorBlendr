package com.drdisagree.colorblendr.service;

interface IShizukuConnection {
    void destroy() = 16777114;
    void exit() = 1;
    String applyFabricatedColors(String jsonString) = 2;
    String removeFabricatedColors() = 3;
    String getCurrentSettings() = 4;
    String run(String command) = 5;
}