package com.drdisagree.colorblendr.utils;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

import rikka.shizuku.Shizuku;
import rikka.shizuku.ShizukuRemoteProcess;

public class ShizukuShell {

    private static final String TAG = ShizukuShell.class.getSimpleName();
    private static List<String> mOutput;
    private static ShizukuRemoteProcess mProcess = null;
    private static String mCommand;

    public ShizukuShell(String command, List<String> output) {
        mOutput = output;
        mCommand = command;
    }

    public boolean isBusy() {
        return mOutput != null && mOutput.size() > 0 && !mOutput.get(mOutput.size() - 1).endsWith("Finish");
    }

    public void exec() {
        try {
            mProcess = Shizuku.newProcess(new String[]{"sh", "-c", mCommand}, null, "/");
            BufferedReader mInput = new BufferedReader(new InputStreamReader(mProcess.getInputStream()));
            BufferedReader mError = new BufferedReader(new InputStreamReader(mProcess.getErrorStream()));
            String line;

            while ((line = mInput.readLine()) != null) {
                mOutput.add(line);
            }

            while ((line = mError.readLine()) != null) {
                mOutput.add("Error: " + line);
            }

            mProcess.waitFor();
        } catch (Exception e) {
            Log.e(TAG, "Failed to execute shell command: " + mCommand, e);
        }
    }

    public void destroy() {
        if (mProcess != null) mProcess.destroy();
    }
}
