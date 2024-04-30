package com.mitwill.mrp.services;

import android.content.Context;
import android.util.Log;

import com.mitwill.mrp.R;
import com.mitwill.mrp.core.rpc.Odoo;
import com.mitwill.mrp.core.rpc.handler.OdooVersionException;
import com.mitwill.mrp.core.rpc.helper.OArguments;
import com.mitwill.mrp.core.rpc.listeners.IOdooLoginCallback;
import com.mitwill.mrp.core.rpc.listeners.IOdooResponse;

import java.util.HashMap;

public class ServiceHandler {

    public static final String TAG = ServiceHandler.class.getSimpleName();
    public static Odoo odoo;

    public static void setOdoo(Odoo odoo) {
        ServiceHandler.odoo = odoo;
    }

    private Odoo createOdooInstance(Context context) {
        try {
            odoo = Odoo.createInstance(context, context.getResources().getString(R.string.server_url));
            return odoo;
        } catch (OdooVersionException ex) {
            throw new NullPointerException();
        }
    }

    public void login(Context context, String username, String paasword, IOdooLoginCallback callback) {

        try {
            Odoo.DEFAULT_MAX_RETRIES = 3;
            Odoo.REQUEST_TIMEOUT_MS = 3000;
            Odoo.quickConnect(
                    context,
                    context.getResources().getString(R.string.server_url),
                    username,
                    paasword,
                    context.getResources().getString(R.string.database_name),
                    callback);

        } catch (NullPointerException ex) {
            Log.d(TAG, "Odoo Instance Not found: ");
        } catch (Exception e) {
            Log.d(TAG, "onQuickConnect: " + e.getMessage());
        }
    }

    public void callMethod(String model, String method, OArguments arguments, HashMap<String, Object> data, IOdooResponse callback) {
        if (odoo != null) {
            odoo.withRetryPolicy(3000, 3).callMethod(model, method, arguments, data, callback);
        }
    }
}

