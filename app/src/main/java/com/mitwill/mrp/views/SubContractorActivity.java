package com.mitwill.mrp.views;

import androidx.annotation.NonNull;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.navigation.NavigationView;
import com.mitwill.mrp.BuildConfig;
import com.mitwill.mrp.R;
import com.mitwill.mrp.datas.OConstants;
import com.mitwill.mrp.views.fragments.SubContractorQRScanFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.mitwill.mrp.utils.PreferenceUtils.Preference;

public class SubContractorActivity extends BaseActivity {

    public static final String TAG = SubContractorActivity.class.getSimpleName();
    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;
    @BindView(R.id.nav_view_subcontractor)
    NavigationView navViewSubcontractor;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    private AppBarConfiguration mAppBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_contractor);

        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        setupNavigationView();
        setupDrawer();

    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_subcontractor_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private void setupNavigationView() {
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_qr_scan, R.id.nav_log_out)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_subcontractor_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navViewSubcontractor, navController);
        navViewSubcontractor.setNavigationItemSelectedListener(menuItem -> {
            menuItem.setChecked(true);
            drawer.closeDrawers();
            switch (menuItem.getItemId()) {
                case R.id.nav_log_out:
                    displayLogoutDialog();
                    break;
                default:
                    break;
            }
            return false;
        });
    }

    private void setupDrawer() {
        Bitmap userImg = null;
        if (!OConstants.userImage.equals("") && !OConstants.userImage.equals("false")) {
            userImg = convertBase64ToBitmap(OConstants.userImage);
        }

        Bitmap finalUserImg = userImg;
        drawer.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
                TextView textView = drawerView.findViewById(R.id.textview_title);
                textView.setText(OConstants.USERNAME);
                TextView tvVersion = drawerView.findViewById(R.id.tvVersion);
                tvVersion.setText(BuildConfig.VERSION_NAME);

                ImageView imageView = drawerView.findViewById(R.id.image_user);
                if (finalUserImg != null) {
                    Glide.with(SubContractorActivity.this)
                            .load(finalUserImg)
                            .apply(RequestOptions.circleCropTransform())
                            .into(imageView);
                } else {
                    Glide.with(SubContractorActivity.this)
                            .load(R.drawable.ic_action_user)
                            .apply(RequestOptions.circleCropTransform())
                            .into(imageView);
                }
            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                //Add your logic when drawer will open
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                //Add your logic when drawer will close
            }

            @Override
            public void onDrawerStateChanged(int newState) {
                //Add your logic when drawer state changes
            }
        });
    }

    private Bitmap convertBase64ToBitmap(String b64) {
        byte[] imageAsBytes = Base64.decode(b64.getBytes(), Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);
    }

    private void displayLogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SubContractorActivity.this);
        builder.setMessage(getResources().getString(R.string.dialog_sure_to_log_out));
        builder.setCancelable(true);
        builder.setTitle(getString(R.string.label_logout));
        builder.setIcon(R.drawable.ic_alert);
        builder.setPositiveButton(getString(R.string.label_yes), ((DialogInterface dialog, int which) -> {
            dialog.cancel();
            SharedPreferences preferences = getSharedPreferences(Preference, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.clear();
            editor.apply();
            startActivity(new Intent(SubContractorActivity.this, LoginScreenActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        }));
        builder.setNegativeButton(getString(R.string.label_no), ((DialogInterface dialog, int which) -> {
            dialog.cancel();
        }));

        AlertDialog alert = builder.create();
        alert.show();
        alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.android_green));
        alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorDarkGrey));
    }

    private void exitApplicationAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SubContractorActivity.this);
        builder.setMessage(getResources().getString(R.string.sure_to_close_app));
        builder.setCancelable(true);
        builder.setTitle(getString(R.string.label_exit));
        builder.setIcon(R.drawable.ic_alert);
        builder.setPositiveButton(getString(R.string.label_yes), ((DialogInterface dialog, int which) -> {
            dialog.cancel();
            finish();
        }));
        builder.setNegativeButton(getString(R.string.label_no), ((DialogInterface dialog, int which) -> {
            dialog.cancel();
        }));

        AlertDialog alert = builder.create();
        alert.show();
        alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.android_green));
        alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorDarkGrey));

    }

    @Override
    public void onBackPressed() {
        Fragment myFragment = getSupportFragmentManager().findFragmentById(R.id.container);
        if (myFragment != null && myFragment instanceof SubContractorQRScanFragment) {
            exitApplicationAlert();
        } else {
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getSupportFragmentManager().popBackStack();
            } else {
                exitApplicationAlert();
            }

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
