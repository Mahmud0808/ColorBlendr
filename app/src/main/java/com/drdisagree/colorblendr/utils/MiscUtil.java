package com.drdisagree.colorblendr.utils;

import android.content.Context;

import androidx.annotation.StringRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

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

    public static void setToolbarTitle(Context context, @StringRes int title, boolean showBackButton, MaterialToolbar toolbar) {
        ((AppCompatActivity) context).setSupportActionBar(toolbar);
        ActionBar actionBar = ((AppCompatActivity) context).getSupportActionBar();
        if (actionBar != null) {
            ((AppCompatActivity) context).getSupportActionBar().setTitle(title);
            ((AppCompatActivity) context).getSupportActionBar().setDisplayHomeAsUpEnabled(showBackButton);
            ((AppCompatActivity) context).getSupportActionBar().setDisplayShowHomeEnabled(showBackButton);
        }
    }
}
