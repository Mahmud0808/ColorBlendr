package com.drdisagree.colorblendr.utils;

import java.util.ArrayList;

public class MiscUtil {

    public static ArrayList<ArrayList<Integer>> convertIntArrayToList(int[][] array) {
        ArrayList<ArrayList<Integer>> result = new ArrayList<>();

        for (int[] row : array) {
            ArrayList<Integer> rowList = new ArrayList<>();
            for (int value : row) {
                rowList.add(value);
            }

            result.add(rowList);
        }

        return result;
    }

    public static int[][] convertListToIntArray(ArrayList<ArrayList<Integer>> arrayList) {
        return arrayList.stream()
                .map(row -> row.stream().mapToInt(Integer::intValue).toArray())
                .toArray(int[][]::new);
    }
}
