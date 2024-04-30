package com.mitwill.mrp.views;

import android.content.IntentFilter;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.mitwill.mrp.R;
import com.mitwill.mrp.datas.OConstants;
import com.mitwill.mrp.datas.SnackbarUtils;
import com.mitwill.mrp.utils.NetworkStateReceiver;

public class BaseActivity extends AppCompatActivity implements NetworkStateReceiver.NetworkStateReceiverListener {

    public static final String TAG = BaseActivity.class.getSimpleName();
    boolean isConnected = true;
    private NetworkStateReceiver networkStateReceiver;

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            networkStateReceiver = new NetworkStateReceiver();
            networkStateReceiver.addListener(this);
            registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void networkAvailable() {
        if (!isConnected()) {
            setConnected(true);
            showSnackBar(getResources().getString(R.string.you_are_online), OConstants.SnackbarType.SNACKBAR_TYPE_SUCCESS.iValue, Snackbar.LENGTH_LONG);
        }
    }

    @Override
    public void networkUnavailable() {
        setConnected(false);
        showSnackBar(getResources().getString(R.string.you_are_offline), OConstants.SnackbarType.SNACKBAR_TYPE_ERROR.iValue, Snackbar.LENGTH_INDEFINITE);
    }

    private void showSnackBar(String message, int snackbarType, int duration) {
        SnackbarUtils.displaySnackbar(this.findViewById(android.R.id.content),
                message, snackbarType, false, duration);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            networkStateReceiver.removeListener(this);
            unregisterReceiver(networkStateReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
