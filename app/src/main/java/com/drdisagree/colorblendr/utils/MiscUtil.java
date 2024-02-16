package com.drdisagree.colorblendr.utils;

import android.content.Context;

import androidx.annotation.StringRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

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

    public static String mergeJsonStrings(String target, String source) throws JSONException {
        if (target == null || target.isEmpty()) {
            target = new JSONObject().toString();
        }

        if (source == null || source.isEmpty()) {
            source = new JSONObject().toString();
        }

        JSONObject targetJson = new JSONObject(target);
        JSONObject sourceJson = new JSONObject(source);
        return mergeJsonObjects(targetJson, sourceJson).toString();
    }

    public static JSONObject mergeJsonObjects(JSONObject target, JSONObject source) throws JSONException {
        Iterator<String> keys = source.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            target.put(key, source.get(key));
        }
        return target;
    }
}
