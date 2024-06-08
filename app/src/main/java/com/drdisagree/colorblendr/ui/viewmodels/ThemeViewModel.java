package com.drdisagree.colorblendr.ui.viewmodels;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.drdisagree.colorblendr.ui.repositories.ThemeRepository;
import com.drdisagree.colorblendr.utils.ColorUtil;
import com.drdisagree.colorblendr.utils.OverlayManager;

import java.util.ArrayList;

public class ThemeViewModel extends AndroidViewModel {

    private final String TAG = ThemeViewModel.class.getSimpleName();
    private final ThemeRepository themeRepository;
    private final MutableLiveData<Integer> accentSaturation = new MutableLiveData<>();
    private final MutableLiveData<Integer> backgroundSaturation = new MutableLiveData<>();
    private final MutableLiveData<Integer> backgroundLightness = new MutableLiveData<>();
    private final MutableLiveData<ArrayList<ArrayList<Integer>>> colorPalette = new MutableLiveData<>();

    public ThemeViewModel(@NonNull Application application) {
        super(application);
        themeRepository = new ThemeRepository(application);
        loadInitialData();
    }

    private void loadInitialData() {
        accentSaturation.setValue(themeRepository.getAccentSaturation());
        backgroundSaturation.setValue(themeRepository.getBackgroundSaturation());
        backgroundLightness.setValue(themeRepository.getBackgroundLightness());
        loadModifiedColors();
    }

    @SuppressWarnings("all")
    public int getAccentSaturation() {
        return accentSaturation.getValue();
    }

    public void updateAccentSaturation(int value) {
        accentSaturation.setValue(value);
        loadModifiedColors();
    }

    public void setAccentSaturation(int value) {
        themeRepository.setAccentSaturation(value);
        updateAccentSaturation(value);
        applyFabricatedColors();
    }

    public void resetAccentSaturation() {
        themeRepository.resetAccentSaturation();
        updateAccentSaturation(100);
        applyFabricatedColors();
    }

    @SuppressWarnings("all")
    public int getBackgroundSaturation() {
        return backgroundSaturation.getValue();
    }

    public void updateBackgroundSaturation(int value) {
        backgroundSaturation.setValue(value);
        loadModifiedColors();
    }

    public void setBackgroundSaturation(int value) {
        themeRepository.setBackgroundSaturation(value);
        updateBackgroundSaturation(value);
        applyFabricatedColors();
    }

    public void resetBackgroundSaturation() {
        themeRepository.resetBackgroundSaturation();
        updateBackgroundSaturation(100);
        applyFabricatedColors();
    }

    @SuppressWarnings("all")
    public int getBackgroundLightness() {
        return backgroundLightness.getValue();
    }

    public void updateBackgroundLightness(int value) {
        backgroundLightness.setValue(value);
        loadModifiedColors();
    }

    public void setBackgroundLightness(int value) {
        themeRepository.setBackgroundLightness(value);
        updateBackgroundLightness(value);
        applyFabricatedColors();
    }

    public void resetBackgroundLightness() {
        themeRepository.resetBackgroundLightness();
        updateBackgroundLightness(100);
        applyFabricatedColors();
    }

    public boolean getPitchBlackTheme() {
        return themeRepository.getPitchBlackTheme();
    }

    public boolean getAccurateShades() {
        return themeRepository.getAccurateShades();
    }

    public boolean isNotShizukuMode() {
        return themeRepository.isNotShizukuMode();
    }

    public void loadModifiedColors() {
        colorPalette.setValue(generateModifiedColors());
    }

    public MutableLiveData<ArrayList<ArrayList<Integer>>> getColorPaletteLiveData() {
        return colorPalette;
    }

    @SuppressWarnings("all")
    private ArrayList<ArrayList<Integer>> generateModifiedColors() {
        try {
            return ColorUtil.generateModifiedColors(
                    getApplication().getApplicationContext(),
                    themeRepository.getMonetStyle(),
                    accentSaturation.getValue(),
                    backgroundSaturation.getValue(),
                    backgroundLightness.getValue(),
                    getPitchBlackTheme(),
                    getAccurateShades()
            );
        } catch (Exception e) {
            Log.e(TAG, "Error generating modified colors", e);
            return null;
        }
    }

    private void applyFabricatedColors() {
        themeRepository.setMonetLastUpdated();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            try {
                OverlayManager.applyFabricatedColors(getApplication().getApplicationContext());
            } catch (Exception ignored) {
            }
        }, 200);
    }
}