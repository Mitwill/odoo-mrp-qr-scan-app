package com.mitwill.mrp.views;

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

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.navigation.NavigationView;
import com.mitwill.mrp.BuildConfig;
import com.mitwill.mrp.R;
import com.mitwill.mrp.datas.OConstants;
import com.mitwill.mrp.views.fragments.WorkCenterFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.mitwill.mrp.utils.PreferenceUtils.Preference;

public class MainActivity extends BaseActivity {

    public static final String TAG = MainActivity.class.getSimpleName();
    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;
    @BindView(R.id.nav_view)
    NavigationView navigationView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    private AppBarConfiguration mAppBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        // Passing each menu ID as a set of Ids because each menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_work_center, R.id.nav_log_out)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

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
                    Glide.with(MainActivity.this)
                            .load(finalUserImg)
                            .apply(RequestOptions.circleCropTransform())
                            .into(imageView);
                } else {
                    Glide.with(MainActivity.this)
                            .load(R.drawable.ic_action_user)
                            .apply(RequestOptions.circleCropTransform())
                            .into(imageView);
                }
            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                menuItem.setChecked(true);
                drawer.closeDrawers();
                switch (menuItem.getItemId()) {
                    case R.id.nav_log_out:
                        displayDialog();
                        break;
                }
                return false;
            }
        });
    }

    private Bitmap convertBase64ToBitmap(String b64) {
        byte[] imageAsBytes = Base64.decode(b64.getBytes(), Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);
    }

    private void displayDialog() {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
        builder1.setMessage(getResources().getString(R.string.dialog_sure_to_log_out));
        builder1.setCancelable(true);
        builder1.setTitle("Log out");
        builder1.setIcon(R.drawable.ic_alert);
        builder1.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        SharedPreferences preferences = getSharedPreferences(Preference, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.clear();
                        editor.apply();
                        finish();
                        startActivity(new Intent(MainActivity.this, LoginScreenActivity.class));
                    }
                });

        builder1.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();
        alert11.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.android_green));
        alert11.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorDarkGrey));
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        Fragment myFragment = getSupportFragmentManager().findFragmentById(R.id.container);
        if (myFragment != null && myFragment instanceof WorkCenterFragment) {
            DispalyAlert();
        } else {
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getSupportFragmentManager().popBackStack();
            } else {
                DispalyAlert();

            }

        }
    }

    private void DispalyAlert() {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
        builder1.setMessage(getResources().getString(R.string.sure_to_close_app));
        builder1.setCancelable(true);
        builder1.setTitle("Exit");
        builder1.setIcon(R.drawable.ic_alert);
        builder1.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        finish();
                    }
                });

        builder1.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();
        alert11.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.android_green));
        alert11.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorDarkGrey));

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
