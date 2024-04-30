package com.mitwill.mrp.views;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.mitwill.mrp.R;
import com.mitwill.mrp.datas.OConstants;
import com.mitwill.mrp.datas.SnackbarUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SuccessOrderActivity extends BaseActivity {

    /*
     *   Success Screen to acknowledge
     *   about the order has been successfully
     *   been placed.
     * */

    private ProgressDialog dialog;  // Progressbar for the webservice calling to show waiting dialog
    private Boolean isSubcontractor = false;

    @BindView(R.id.activity_success_order_btn_done)
    Button btnSuccessOrder;

    @BindView(R.id.activity_success_order_tv_order_id)
    TextView tvOrderId;

    @BindView(R.id.tv_success_placeholder_text)
    TextView tvSuccessPlaceholderText;

    @Override
    protected void onPause() {
        super.onPause();
        hideProgressDialog();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if ( dialog != null && dialog.isShowing() ) {
            showProgressDialog();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_success_order);
        ButterKnife.bind(this);
        getBundleParams();
    }

    private void getBundleParams() {
        try {
            if(getIntent().getExtras()!= null){
                isSubcontractor = (Boolean) getIntent().getExtras().get(OConstants.BundleParams.IS_SUBCONTRACTOR);
                tvOrderId.setText(getIntent().getExtras().getString(OConstants.BundleParams.ORDER_ID));
                tvSuccessPlaceholderText.setText(getIntent().getExtras().getString(OConstants.BundleParams.SUCCESS_MESSAGE));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    @OnClick({R.id.activity_success_order_btn_done})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.activity_success_order_btn_done:
                onBackPressed();
                break;
            default:
                    break;
        }
    }


    //#pragma mark -utility methods
    private void showProgressDialog() {
        if ( dialog == null ) {
            dialog = new ProgressDialog( this );
        }
        dialog.setMessage( getString( R.string.please_wait ) );
        dialog.setCancelable( false );
        dialog.show();
    }

    private void hideProgressDialog() {
        if ( dialog != null && dialog.isShowing() ) {
            dialog.dismiss();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (isSubcontractor){
            startActivity(new Intent(SuccessOrderActivity.this ,SubContractorActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        }else{
            startActivity(new Intent(SuccessOrderActivity.this ,MainActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        }
    }
}
