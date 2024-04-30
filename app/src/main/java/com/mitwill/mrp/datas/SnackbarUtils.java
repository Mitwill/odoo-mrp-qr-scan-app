package com.mitwill.mrp.datas;

import android.graphics.Paint;
import android.view.Gravity;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;
import com.mitwill.mrp.R;

import static android.graphics.Color.rgb;
import static com.mitwill.mrp.datas.OConstants.SnackbarType;

public class SnackbarUtils {

    public static void displaySnackbar(View view, String message, int type, boolean needCallback, int duration) {

        if (!needCallback) {
            Snackbar snackbar = Snackbar
                    .make(view, message, Snackbar.LENGTH_LONG);

            SnackbarType enumType = SnackbarType.values()[type];

            switch (enumType) {
                case SNACKBAR_TYPE_WARNING:
                    snackbar.getView().setBackgroundColor(view.getResources().getColor(R.color.android_orange_dark));
                    break;
                case SNACKBAR_TYPE_ERROR:
                    snackbar.getView().setBackgroundColor(rgb(244, 67, 54));
                    break;
                case SNACKBAR_TYPE_SUCCESS:
                    snackbar.getView().setBackgroundColor(rgb(14, 157, 88));
                    break;
            }
            snackbar.setDuration(duration);
            snackbar.show();
        }
    }
}
