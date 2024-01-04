package com.drdisagree.colorblendr.ui.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.HashMap;
import java.util.Map;

public class SharedViewModel extends ViewModel {

    private final MutableLiveData<Map<String, Boolean>> booleanStates = new MutableLiveData<>();

    public LiveData<Map<String, Boolean>> getBooleanStates() {
        return booleanStates;
    }

    public void setBooleanState(String viewId, boolean state) {
        Map<String, Boolean> currentStates = booleanStates.getValue();
        if (currentStates == null) {
            currentStates = new HashMap<>();
        }
        currentStates.put(viewId, state);
        booleanStates.setValue(currentStates);
    }

    private final MutableLiveData<Map<String, Integer>> visibilityStates = new MutableLiveData<>();

    public LiveData<Map<String, Integer>> getVisibilityStates() {
        return visibilityStates;
    }

    public void setVisibilityState(String viewId, int visibility) {
        Map<String, Integer> currentStates = visibilityStates.getValue();
        if (currentStates == null) {
            currentStates = new HashMap<>();
        }
        currentStates.put(viewId, visibility);
        visibilityStates.setValue(currentStates);
    }
}
