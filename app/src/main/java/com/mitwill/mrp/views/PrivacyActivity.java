package com.mitwill.mrp.views;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import com.mitwill.mrp.R;
import com.mitwill.mrp.datas.OConstants;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PrivacyActivity extends BaseActivity {



//    @BindView(R.id.webview)
//    WebView webview;
//
//    String urlToOpen = "https://mitwilltextiles.com/android-app-privacy-policy/";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy);
        ButterKnife.bind(this);
//        webview.getSettings().setJavaScriptEnabled(true);
//        webview.loadUrl(urlToOpen);
    }



    @Override
    public void onBackPressed() {
        super.onBackPressed();
        
    }
}
