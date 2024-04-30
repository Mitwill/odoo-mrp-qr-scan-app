package com.mitwill.mrp.utils;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.mitwill.mrp.R;
import com.mitwill.mrp.interfaces.ActionDialogClickListener;
import com.mitwill.mrp.views.QRScanActivity;
import com.mitwill.mrp.views.fragments.SubContractorQRScanFragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class Utils {
    private static float DONE_QUANTITIES;
    private static boolean IS_FULL_QUANTITY;
    private static boolean IS_QUANTITY_ERROR = false;

    public static boolean netConnect(Context ctx) {
        ConnectivityManager cm;
        NetworkInfo info = null;
        try {
            cm = (ConnectivityManager)
                    ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
            info = cm.getActiveNetworkInfo();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return info != null;
    }

    public static void openErrorDialog(Context context, String errTitlemessage, String errMessage) {

        final Dialog dialog = new Dialog(context, R.style.MaterialDialogSheet);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_dialog_alert);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        dialog.getWindow().setAttributes(lp);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setGravity(Gravity.CENTER);
        dialog.setCancelable(true);

        TextView txtMessage = dialog.findViewById(R.id.dialog_text_message);
        TextView txtTitleMessage = dialog.findViewById(R.id.dialog_text_title);
        LinearLayout okay = dialog.findViewById(R.id.dialog_btn_okay);
        if (!errTitlemessage.equals("")) {
            txtTitleMessage.setText(errTitlemessage);
        }

        txtMessage.setText(errMessage);
        okay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public static void openErrorDialog(Context context, String errTitleMessage, String errMessage,
                                       Boolean isStackTrace) {

        final Dialog dialog = new Dialog(context, R.style.MaterialDialogSheet);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_dialog_alert);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        dialog.getWindow().setAttributes(lp);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setGravity(Gravity.CENTER);
        dialog.setCancelable(true);

        TextView txtMessage = dialog.findViewById(R.id.dialog_text_message);
        TextView txtTitleMessage = dialog.findViewById(R.id.dialog_text_title);
        TextView txtShowMoreDetails = dialog.findViewById(R.id.dialog_show_more_details);
        LinearLayout okay = dialog.findViewById(R.id.dialog_btn_okay);
        if (!errTitleMessage.equals("")) {
            txtTitleMessage.setText(errTitleMessage);
        }

        txtMessage.setText(errMessage);
        txtMessage.setVisibility(View.GONE);
        txtShowMoreDetails.setVisibility(View.VISIBLE);

        okay.setOnClickListener((View v) -> {
            dialog.dismiss();
        });
        txtShowMoreDetails.setOnClickListener((View v) -> {
            txtMessage.setVisibility(View.VISIBLE);
            txtShowMoreDetails.setVisibility(View.GONE);
        });
        dialog.show();
    }
    public static String createCopyAndReturnRealPath(
            @NonNull Context context, @NonNull Uri uri) {
        final ContentResolver contentResolver = context.getContentResolver();
        if (contentResolver == null)
            return null;

        // Create file path inside app's data dir
        String filePath = context.getApplicationInfo().dataDir + File.separator
                + System.currentTimeMillis();

        File file = new File(filePath);
        try {
            InputStream inputStream = contentResolver.openInputStream(uri);
            if (inputStream == null)
                return null;

            OutputStream outputStream = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while ((len = inputStream.read(buf)) > 0)
                outputStream.write(buf, 0, len);

            outputStream.close();
            inputStream.close();
        } catch (IOException ignore) {
            return null;
        }

        return file.getAbsolutePath();
    }
    public static void openActionDialog(Context context, String titleMessage, String descriptionMessage,
                                        List<String> actions, ActionDialogClickListener actionDialogClickListener, int QRType) {

        final Dialog dialog = new Dialog(context, R.style.MaterialDialogSheet);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_action_dialog);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        dialog.getWindow().setAttributes(lp);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setGravity(Gravity.CENTER);
        dialog.setCancelable(false);

        TextView txtMessage = dialog.findViewById(R.id.dialog_text_message);
        TextView txtTitleMessage = dialog.findViewById(R.id.dialog_text_title);
        LinearLayout dialogBtnContainer = dialog.findViewById(R.id.dialog_btn_container);
        dialogBtnContainer.setWeightSum(actions.size());

        if (!titleMessage.equals("")) {
            txtTitleMessage.setText(titleMessage);
        }
        txtMessage.setText(descriptionMessage);

        // Layout inflater
        LayoutInflater layoutInflater = dialog.getLayoutInflater();
        View view;

        for (int i = 0; i < actions.size(); i++) {
            // Add the text layout to the parent layout
            view = layoutInflater.inflate(R.layout.custom_action_button, dialogBtnContainer, false);

            // In order to get the view we have to use the new view with text_layout in it
            TextView textView = (TextView) view.findViewById(R.id.dialog_tv_action);
            textView.setText(actions.get(i));
            textView.setId(i);
            textView.setOnClickListener((View v) -> {
                actionDialogClickListener.onDialogActionClick(v.getId(), QRType, IS_FULL_QUANTITY, DONE_QUANTITIES);
                dialog.dismiss();
            });
            // Add the text view to the parent layout
            dialogBtnContainer.addView(textView);
        }
        dialog.show();
    }


    public static void openBeforeActionDialog(Context context, QRScanActivity qrScanActivity, Boolean isActionRequired) {

        final Dialog dialog = new Dialog(context, R.style.MaterialDialogSheet);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.activity_mrp_order);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        dialog.getWindow().setAttributes(lp);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setGravity(Gravity.CENTER);
        dialog.setCancelable(true);

        EditText editText = (EditText) dialog.findViewById(R.id.edittext);
        TextView txtView = (TextView) dialog.findViewById(R.id.error);
        txtView.setVisibility(View.GONE);
        LinearLayout okay = dialog.findViewById(R.id.dialog_btn_okay);

        editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtView.setVisibility(View.GONE);
            }
        });

        okay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (editText.getText().toString().matches("")) {

                    IS_FULL_QUANTITY = true;
                    DONE_QUANTITIES = 0f;
                    IS_QUANTITY_ERROR = false;
                } else if (Float.parseFloat(editText.getText().toString()) > Float.MAX_VALUE) {
                    txtView.setVisibility(View.VISIBLE);
                    IS_QUANTITY_ERROR = true;
                } else {

                    IS_FULL_QUANTITY = true;
                    DONE_QUANTITIES = Float.parseFloat(editText.getText().toString());
                    IS_QUANTITY_ERROR = false;
                }

                if (!isActionRequired && !IS_QUANTITY_ERROR) {
                    //  qrScanActivity.initialcallService(IS_FULL_QUANTITY, DONE_QUANTITIES);
                    dialog.dismiss();
                }

            }
        });
        dialog.show();

    }


    public static void openBeforeActionDialog(Context context, SubContractorQRScanFragment subContractorQRScanFragment) {

        final Dialog dialog = new Dialog(context, R.style.MaterialDialogSheet);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.activity_mrp_order);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        dialog.getWindow().setAttributes(lp);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setGravity(Gravity.CENTER);
        dialog.setCancelable(true);

        TextView txtView = (TextView) dialog.findViewById(R.id.error);
        txtView.setVisibility(View.GONE);
        EditText editText = (EditText) dialog.findViewById(R.id.edittext);
        LinearLayout okay = dialog.findViewById(R.id.dialog_btn_okay);


        editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtView.setVisibility(View.GONE);
            }
        });

        okay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (editText.getText().toString().matches("")) {

                    IS_FULL_QUANTITY = true;
                    DONE_QUANTITIES = 0f;
                    IS_QUANTITY_ERROR = false;
                } else if (Float.parseFloat(editText.getText().toString()) > Float.MAX_VALUE) {
                    txtView.setVisibility(View.VISIBLE);
                    IS_QUANTITY_ERROR = true;
                } else {

                    IS_FULL_QUANTITY = true;
                    DONE_QUANTITIES = Float.parseFloat(editText.getText().toString());
                    IS_QUANTITY_ERROR = false;
                }

                if (!IS_QUANTITY_ERROR) {
                    subContractorQRScanFragment.initialcallService(IS_FULL_QUANTITY, DONE_QUANTITIES);
                    dialog.dismiss();
                }
            }
        });
        dialog.show();

    }
}
