package com.drdisagree.colorblendr.ui.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.drdisagree.colorblendr.ui.repositories.ColorRepository;
import com.drdisagree.colorblendr.utils.OverlayManager;

import java.util.List;

public class ColorsViewModel extends AndroidViewModel {

    private final ColorRepository colorRepository;
    private final MutableLiveData<List<Integer>> wallpaperColorsLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Integer>> basicColorsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> monetSeedColorLiveData = new MutableLiveData<>();

    public ColorsViewModel(@NonNull Application application) {
        super(application);
        colorRepository = new ColorRepository(application);
        loadInitialData();
    }

    private void loadInitialData() {
        loadWallpaperColors();
        loadBasicColors();
        monetSeedColorLiveData.setValue(colorRepository.getMonetSeedColor());
    }

    public LiveData<List<Integer>> getWallpaperColorsLiveData() {
        return wallpaperColorsLiveData;
    }

    public LiveData<List<Integer>> getBasicColorsLiveData() {
        return basicColorsLiveData;
    }

    public LiveData<Integer> getMonetSeedColorLiveData() {
        return monetSeedColorLiveData;
    }

    public void loadWallpaperColors() {
        wallpaperColorsLiveData.setValue(colorRepository.getWallpaperColorList());
    }

    public void loadBasicColors() {
        basicColorsLiveData.setValue(colorRepository.getBasicColors());
    }

    public void setMonetSeedColor(int color) {
        colorRepository.setMonetSeedColor(color);
        monetSeedColorLiveData.setValue(color);
        colorRepository.setLastUpdated(System.currentTimeMillis());
        OverlayManager.applyFabricatedColors(getApplication().getApplicationContext());
    }

    public void setMonetSeedColorEnabled(boolean enabled) {
        colorRepository.setMonetSeedColorEnabled(enabled);
    }

    public boolean isMonetSeedColorEnabled() {
        return colorRepository.isMonetSeedColorEnabled();
    }
}
