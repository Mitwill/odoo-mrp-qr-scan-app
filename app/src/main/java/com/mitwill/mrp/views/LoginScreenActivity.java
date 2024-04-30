package com.mitwill.mrp.views;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.snackbar.Snackbar;
import com.mitwill.mrp.BuildConfig;
import com.mitwill.mrp.R;
import com.mitwill.mrp.core.rpc.Odoo;
import com.mitwill.mrp.core.rpc.handler.OdooVersionException;
import com.mitwill.mrp.core.rpc.helper.OArguments;
import com.mitwill.mrp.core.rpc.helper.utils.gson.OdooResult;
import com.mitwill.mrp.core.rpc.listeners.IOdooConnectionListener;
import com.mitwill.mrp.core.rpc.listeners.IOdooLoginCallback;
import com.mitwill.mrp.core.rpc.listeners.IOdooResponse;
import com.mitwill.mrp.core.rpc.listeners.OdooError;
import com.mitwill.mrp.core.support.OUser;
import com.mitwill.mrp.datas.OConstants;
import com.mitwill.mrp.datas.SnackbarUtils;
import com.mitwill.mrp.models.UserAccess;
import com.mitwill.mrp.services.ServiceHandler;
import com.mitwill.mrp.utils.PreferenceUtils;
import com.mitwill.mrp.utils.ServiceCallInfoUtils;
import com.mitwill.mrp.utils.Utils;

import java.util.HashMap;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.mitwill.mrp.utils.LoginException.loginFail;
import static com.mitwill.mrp.utils.PreferenceUtils.Preference;
import static com.mitwill.mrp.utils.PreferenceUtils.keyPassword;
import static com.mitwill.mrp.utils.PreferenceUtils.keyUserId;
import static com.mitwill.mrp.utils.PreferenceUtils.keyUserName;
import static com.mitwill.mrp.utils.Utils.openErrorDialog;

public class LoginScreenActivity extends BaseActivity {
    private static final String TAG = LoginScreenActivity.class.getSimpleName();
    private ServiceHandler callService;

    @BindView(R.id.edtUserName)
    EditText edtUserName;

    @BindView(R.id.edtPassword)
    EditText edtPassword;

    @BindView(R.id.tvVersion)
    TextView tvVersion;

    @BindView(R.id.linearLayout)
    LinearLayout linearLayout1;

    @BindView(R.id.linearLayout2)
    LinearLayout linearLayout2;

    @BindView(R.id.btnLogin)
    Button btnLogin;

    @BindView(R.id.progress_bar)
    ProgressBar progress_bar;

    @BindView(R.id.constraintLayout1)
    ConstraintLayout constraintLayout;

    @BindView(R.id.checkboxPrivacy)
    CheckBox checkboxPrivacy;

    @BindView(R.id.textViewPolicy)
    TextView textViewPolicy;

    String username, password;
    ProgressDialog progressDialog;

    Context mContext = null;
    SharedPreferences sharedPreferences;

    @OnClick(R.id.show_hide_password)
    public void showHidePassword(View view) {
        switch (view.getId()) {
            case R.id.show_hide_password:
                if (edtPassword.getTransformationMethod().equals(PasswordTransformationMethod.getInstance())) {
                    ((ImageView) (view)).setImageResource(R.drawable.ic_hide_password);
                    //Show Password
                    edtPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {
                    ((ImageView) (view)).setImageResource(R.drawable.ic_show_password);
                    //Hide Password
                    edtPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
                break;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);
        ButterKnife.bind(this);
        mContext = getApplicationContext();
        edtPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                hideKeyBoard();
                if (checkValidation()) {
                    login();
                }
                return false;
            }
        });
        tvVersion.setText(getString(R.string.version).concat(" ").concat(BuildConfig.VERSION_NAME));
        setProgressDialog();
        getCreds();

    }

    public void goToPrivacyLink()
    {
        // Create an Intent with ACTION_VIEW and the URL
        Intent intent = new Intent(this,PrivacyActivity.class);
            startActivity(intent);

    }
    private void getCreds() {
        sharedPreferences = getSharedPreferences(Preference, MODE_PRIVATE);
        username = PreferenceUtils.getPreference(sharedPreferences, keyUserName);
        password = PreferenceUtils.getPreference(sharedPreferences, keyPassword);
        if (!username.isEmpty() && !password.isEmpty()) {
            setViewInvisible();
            try {
                callLoginService(username, password);
            } catch (OdooVersionException e) {
                e.printStackTrace();
            }
        } else {
            setViewVisible();
        }
    }

    private void setViewInvisible() {
        progress_bar.setVisibility(View.VISIBLE);
        linearLayout1.setVisibility(View.INVISIBLE);
        linearLayout2.setVisibility(View.INVISIBLE);
        btnLogin.setVisibility(View.INVISIBLE);
    }

    private void setViewVisible() {
        progress_bar.setVisibility(View.INVISIBLE);
        linearLayout1.setVisibility(View.VISIBLE);
        linearLayout2.setVisibility(View.VISIBLE);
        btnLogin.setVisibility(View.VISIBLE);
    }

    @OnClick({R.id.btnLogin, R.id.constraintLayout1,R.id.textViewPolicy})
    public void onButtonClick(View view) {
        switch (view.getId()) {
            case R.id.btnLogin:
                hideKeyBoard();
                if (checkValidation()) {
                    login();
                }
                break;
            case R.id.textViewPolicy:
                goToPrivacyLink();
                break;
            case R.id.constraintLayout1:
                hideKeyBoard();

        }
    }

    private void hideKeyBoard() {
        if (this.getCurrentFocus() != null) {
            InputMethodManager inm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inm != null) {
                inm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }
        }
    }

    private void login() {
        username = edtUserName.getText().toString().trim();
        password = edtPassword.getText().toString().trim();
        showProgressDialog();
        try {
            callLoginService(username, password);
        } catch (OdooVersionException e) {
            e.printStackTrace();
        }
    }

    private void callLoginService(String username, String password) throws OdooVersionException {

        if (Utils.netConnect(this)) {

            Odoo.createInstance(this, getResources().getString(R.string.server_url)).setOnConnect(new IOdooConnectionListener() {
                @Override
                public void onConnect(Odoo odoo) {
                    odoo.authenticate(username, password, getResources().getString(R.string.database_name), new IOdooLoginCallback() {
                        @Override
                        public void onLoginSuccess(Odoo odoo, OUser user) {
                            Log.d(TAG, "onLoginSuccess: ");
                            OConstants.userImage = user.getAvatar();
                            handleLoginResponse(odoo, user, null);
                        }

                        @Override
                        public void onLoginFail(OdooError error) {
                            handleLoginResponse(null, null, error);
                        }
                    });
                }

                @Override
                public void onError(OdooError error) {

                    Log.d(TAG, "onLoginFail: " + error);
                    handleLoginResponse(null, null, error);
                }
            });
        } else {
            HideProgress();
            SnackbarUtils.displaySnackbar(this.findViewById(android.R.id.content),
                    getString(R.string.check_connection), OConstants.SnackbarType.SNACKBAR_TYPE_ERROR.iValue, false, Snackbar.LENGTH_LONG);
        }
    }

    private void handleLoginResponse(Odoo odoo, OUser user, OdooError error) {
        //Handle Error response
        if (error != null) {
            HideProgress();
            handleErrorResonse(error);
            return;
        }
        //Handle Success Response
        if (user != null) {
            handleSuccessResponse(odoo);
            PreferenceUtils.setPreference(sharedPreferences, keyUserId, user.getUserId().toString());
        }
    }

    private void handleSuccessResponse(Odoo odoo) {
        ServiceHandler.setOdoo(odoo);
        callUserAccessService();
    }

    private void handleErrorResonse(OdooError error) {
        if (error.getResponseCode() == 401) {
            SnackbarUtils.displaySnackbar(this.findViewById(android.R.id.content),
                    getResources().getString(R.string.error_invalid_username_or_password),
                    OConstants.SnackbarType.SNACKBAR_TYPE_ERROR.iValue, false, Snackbar.LENGTH_LONG);
            edtUserName.requestFocus();
        } else {
            try {
                loginFail(error);
            } catch (Exception m) {
                Log.d(TAG, "Exception occured: " + m);
                openErrorDialog(this, error.getMessage(), m.getMessage());
            }
        }
        setViewVisible();
        edtPassword.setText("");
    }

    private void setProgressDialog() {
        progressDialog = new ProgressDialog(LoginScreenActivity.this);
        progressDialog.setMessage(getResources().getString(R.string.please_wait));
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

    }

    private void showProgressDialog() {
        if (progressDialog != null) {
            progressDialog.show();
        }
    }

    private void HideProgress() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    /* Validation for Edit text not empty */
    private boolean checkValidation() {
        if (TextUtils.isEmpty(edtUserName.getText())) {
            SnackbarUtils.displaySnackbar(this.findViewById(android.R.id.content),
                    getResources().getString(R.string.error_provide_username),
                    OConstants.SnackbarType.SNACKBAR_TYPE_ERROR.iValue, false, Snackbar.LENGTH_LONG);
            edtUserName.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(edtPassword.getText())) {
            SnackbarUtils.displaySnackbar(this.findViewById(android.R.id.content),
                    getResources().getString(R.string.error_provide_password),
                    OConstants.SnackbarType.SNACKBAR_TYPE_ERROR.iValue, false, Snackbar.LENGTH_LONG);
            edtPassword.requestFocus();
            return false;
        }
        return true;
    }

    @Override
    public void networkUnavailable() {
        progress_bar.setVisibility(View.INVISIBLE);
        HideProgress();
        super.networkUnavailable();
    }

    @Override
    public void networkAvailable() {
        if (!isConnected()) {
            setProgressDialog();
            getCreds();
        }
        super.networkAvailable();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void callUserAccessService() {
        callService = new ServiceHandler();
        HashMap<String, Object> data = new HashMap<>();

        ServiceCallInfoUtils  serviceCallInfoUtils = new ServiceCallInfoUtils();
        OArguments arguments = serviceCallInfoUtils.getArgumentForUserAccess();

        callService.callMethod(UserAccess.MODEL_NAME, UserAccess.METHOD_NAME, arguments, data, new IOdooResponse() {
                @Override
                public void onResponse(OdooResult response) {
                    HandleUserAccessServiceResponse(response);
                }

                @Override
                public void onError(OdooError error) {
                    HideProgress();
                    Utils.openErrorDialog(LoginScreenActivity.this, error.getMessage(), error.getServerTrace(), true);
                }
        });
    }


    private void HandleUserAccessServiceResponse(OdooResult response) {
        HideProgress();
        if (response.containsKey(OConstants.ApiConstant.ERROR)) {
            String strMessage = response.getString(OConstants.ApiConstant.ERROR);
            int type = OConstants.SnackbarType.SNACKBAR_TYPE_ERROR.iValue;
            SnackbarUtils.displaySnackbar(this.findViewById(android.R.id.content),
                    strMessage, type, false, Snackbar.LENGTH_LONG);
        } else if (response.containsKey(OConstants.ApiConstant.RESULT)) {
            Boolean isSubContractor = response.getBoolean(OConstants.ApiConstant.RESULT);

            OConstants.USERNAME = username;
            PreferenceUtils.setPreference(sharedPreferences, keyUserName, username);
            PreferenceUtils.setPreference(sharedPreferences, keyPassword, password);

            Intent intent;
            if (isSubContractor){
                intent = new Intent(LoginScreenActivity.this,SubContractorActivity.class);
            }else{
                intent = new Intent(LoginScreenActivity.this,MainActivity.class);
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }

}
