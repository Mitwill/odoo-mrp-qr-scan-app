package com.mitwill.mrp.utils;

import android.content.SharedPreferences;
import android.util.Log;


public class PreferenceUtils {

    public static final String TAG = PreferenceUtils.class.getSimpleName();

    public static final String Preference = "MitwillMRPPref";
    public static final String keyUserName = "username";
    public static final String keyPassword = "password";
    public static final String keyUserId = "userid";

    public static void setPreference(SharedPreferences preferences, String key, String value) {
        SharedPreferences.Editor myEdit = preferences.edit();
        myEdit.putString(key, value);
        myEdit.apply();
    }

    public static String getPreference(SharedPreferences sharedPreferences, String key) {
        Log.d(TAG, "getPreference: ");
        String returnprefKey = "";
        if (sharedPreferences.contains(key)) {
            returnprefKey = sharedPreferences.getString(key, "");
        }
        return returnprefKey;
    }
}
