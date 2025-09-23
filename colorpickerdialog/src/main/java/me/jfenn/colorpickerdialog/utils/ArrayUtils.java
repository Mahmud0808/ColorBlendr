package me.jfenn.colorpickerdialog.utils;

import java.util.Arrays;

public class ArrayUtils {

    public static <T> T[] push(T[] arr, T item) {
        T[] newArr = Arrays.copyOf(arr, arr.length + 1);
        newArr[newArr.length - 1] = item;
        return newArr;
    }

    public static <T> T[] pop(T[] arr) {
        return Arrays.copyOf(arr, arr.length - 1);
    }

}