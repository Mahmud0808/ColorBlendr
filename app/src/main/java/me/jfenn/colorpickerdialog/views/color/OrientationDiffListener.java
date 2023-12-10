package me.jfenn.colorpickerdialog.views.color;

import android.content.Context;
import android.view.OrientationEventListener;

public abstract class OrientationDiffListener extends OrientationEventListener {

    private int rotation = -1;

    public OrientationDiffListener(Context context) {
        super(context);
    }

    @Override
    public void onOrientationChanged(int orientation) {
        int rotation;

        if (orientation <= 45 || orientation > 315)
            rotation = 0;
        else if (orientation <= 135)
            rotation = 1;
        else if (orientation <= 225)
            rotation = 2;
        else rotation = 3;

        if (this.rotation < 0) {
            this.rotation = rotation;
            return;
        }

        if (this.rotation != rotation)
            onRotationChanged(rotation);
    }

    public abstract void onRotationChanged(int rotation);

}
