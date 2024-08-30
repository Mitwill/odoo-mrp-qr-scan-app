package com.mitwill.mrp.views;

import static com.mitwill.mrp.models.WorkCenter.WORK_CENTER_ID;
import static com.mitwill.mrp.models.WorkCenter.WORK_CENTER_NAME;
import static com.mitwill.mrp.utils.PreferenceUtils.Preference;
import static com.mitwill.mrp.utils.PreferenceUtils.keyUserId;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.DefaultDecoderFactory;
import com.mitwill.mrp.R;
import com.mitwill.mrp.adapters.QuentityImageListAdapter;
import com.mitwill.mrp.core.rpc.helper.OArguments;
import com.mitwill.mrp.core.rpc.helper.utils.gson.OdooResult;
import com.mitwill.mrp.core.rpc.listeners.IOdooResponse;
import com.mitwill.mrp.core.rpc.listeners.OdooError;
import com.mitwill.mrp.datas.OConstants;
import com.mitwill.mrp.datas.SnackbarUtils;
import com.mitwill.mrp.interfaces.ActionDialogClickListener;
import com.mitwill.mrp.interfaces.RecyclerViewItemClickListener;
import com.mitwill.mrp.models.BatchOrder;
import com.mitwill.mrp.models.DoneQuantity;
import com.mitwill.mrp.models.WorkOrder;
import com.mitwill.mrp.retrofit.apiinterface.APIClient;
import com.mitwill.mrp.retrofit.apiinterface.ApiInterface;
import com.mitwill.mrp.services.ServiceHandler;
import com.mitwill.mrp.utils.BatchOrderScanResult;
import com.mitwill.mrp.utils.InvalidBatchQRcodeException;
import com.mitwill.mrp.utils.InvalidWorkQRcodeException;
import com.mitwill.mrp.utils.PreferenceUtils;
import com.mitwill.mrp.utils.ServiceCallInfoUtils;
import com.mitwill.mrp.utils.Utils;
import com.mitwill.mrp.utils.WorkOrderScanResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QRScanActivity extends BaseActivity implements ActionDialogClickListener, RecyclerViewItemClickListener {


    private List<String> actionKeyList;
    private List<String> actionList;
    private ActionDialogClickListener actionDialogClickListener;
    private static final String TAG = QRScanActivity.class.getSimpleName();
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 2;
    private static final int MY_PERMISSIONS_REQUEST_STORAGE = 3;
    private static final int MY_PERMISSIONS_REQUEST_MEDIA = 4;
    private static final int SELECT_IMAGE = 1889;
    private Float DONE_QUANTITIES;
    private Boolean IS_FULL_QUANTITY;
    private List<String> imageList;
    private Dialog dialog = null;
    private RecyclerView rvImagePreview;
    private QuentityImageListAdapter quentityImageListAdapter;
    private RecyclerViewItemClickListener recyclerViewItemClickListener;

    private ApiInterface apiInterface;
    private SharedPreferences sharedPreferences;

    @BindView(R.id.activity_qrscanner_qrview)
    DecoratedBarcodeView barcodeView;

    @BindView(R.id.activity_qrscanner_img_qr)
    ImageView imgQRCode;

    @BindView(R.id.btn_confirm_batch)
    Button btnConfirmBatch;

    @BindView(R.id.toolbar)
    Toolbar toolbar;


    @BindView(R.id.work_center_name)
    TextView textWorkCenter;

    @BindView(R.id.batch_order_id)
    TextView textBatchOrderId;

    @BindView(R.id.bmo_number)
    TextView textBmoNumber;

    @BindView(R.id.camera_closed)
    ImageView imgCamera_closed;

    int batchOrderNumber = 0, workCenterId = 0;
    Map<String, String> map;

    ServiceHandler callService;
    ProgressDialog progressDialog;
    BatchOrderScanResult batchOrderScanResult;
    private WorkOrderScanResult workOrderScanResult;
//    ActivityResultLauncher<Intent> launchSomeActivity;
    ArrayList<Image> imageListData = new ArrayList<>();

    private int QRType;
    private boolean isPermissionEnabled = false;
    private final BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if (result.getText() == null) {
                return;
            }
            stopScanning();
            HandleScannedResult(result);
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
        }
    };

    @OnClick({R.id.activity_qrscanner_img_qr, R.id.btn_confirm_batch, R.id.camera_closed})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.activity_qrscanner_img_qr:
                startScanning();
                break;
            case R.id.btn_confirm_batch:
                if (Utils.netConnect(this)) {
//                    Utils.openBeforeActionDialog(QRScanActivity.this,this,false);
                    openQuantityDialog();
                } else {
                    SnackbarUtils.displaySnackbar(this.findViewById(android.R.id.content),
                            getString(R.string.check_connection), OConstants.SnackbarType.SNACKBAR_TYPE_ERROR.iValue, false, Snackbar.LENGTH_LONG);
                }
                break;
            case R.id.camera_closed:
                stopScanning();
                break;
            default:
                break;
        }
    }

    private void callService(Boolean isActionRequired, String action, int QRType, boolean isShow) {
        disableButton();
        callService = new ServiceHandler();
        String strModelName = null;
        OArguments arguments = null;
        HashMap<String, Object> data = new HashMap<>();
        ServiceCallInfoUtils serviceCallInfoUtils = new ServiceCallInfoUtils();

        if (OConstants.QR_TYPE_BATCH_ORDER == QRType) {
            strModelName = BatchOrder.MODEL_NAME;
            arguments = validateQRCodeResult(serviceCallInfoUtils, isActionRequired, false, action);
        } else {
            strModelName = WorkOrder.MODEL_NAME;
            arguments = validateQRCodeResult(serviceCallInfoUtils, isActionRequired, true, action);
        }
        if (isShow) {
            showProgressDialog();
        }
        if (batchOrderScanResult != null && batchOrderScanResult.isMrpQrCodeScan()) {
            //mrp QR code
            callService.callMethod(DoneQuantity.MODEL_NAME, DoneQuantity.METHOD_NAME, arguments, data, new IOdooResponse() {
                @Override
                public void onResponse(OdooResult response) {
                    HandleResponse(response, isActionRequired, QRType);
                }

                @Override
                public void onError(OdooError error) {
                    HideProgress();
                    Utils.openErrorDialog(QRScanActivity.this, error.getMessage(), error.getServerTrace(), true);
                }
            });
        } else {
            //other codes
            callService.callMethod(strModelName, serviceCallInfoUtils.getMethodName(), arguments, data, new IOdooResponse() {
                @Override
                public void onResponse(OdooResult response) {
                    HandleResponse(response, isActionRequired, QRType);
                }

                @Override
                public void onError(OdooError error) {
                    HideProgress();
                    Utils.openErrorDialog(QRScanActivity.this, error.getMessage(), error.getServerTrace(), true);
                }
            });
        }
    }

    private OArguments validateQRCodeResult(ServiceCallInfoUtils serviceCallInfoUtils, Boolean isActionRequired,
                                            Boolean isWorkOrderResult, String action) {
        if (isWorkOrderResult) {
            if (workOrderScanResult.isValidWorkOrderScan()) {
                if (isActionRequired) {
                    return serviceCallInfoUtils.getArgumentForScannedWorkOrderResult
                            (workOrderScanResult, true, action, DONE_QUANTITIES);
                } else {
                    return serviceCallInfoUtils.getArgumentForScannedWorkOrderResult
                            (workOrderScanResult, false, action, DONE_QUANTITIES);
                }
            } else {
                try {
                    throw new InvalidWorkQRcodeException(getResources().getString(R.string.scan_valid_qr_for_work));
                } catch (InvalidWorkQRcodeException e) {
                    SnackbarUtils.displaySnackbar(QRScanActivity.this.findViewById(android.R.id.content),
                            getResources().getString(R.string.scan_valid_qr_for_work), OConstants.SnackbarType.SNACKBAR_TYPE_ERROR.iValue, false, Snackbar.LENGTH_LONG);
                }
            }
        } else {
            if (batchOrderScanResult.isValidBatchOrderId()) {
                if (isActionRequired) {
                    return serviceCallInfoUtils.getArgumentForScannedResult
                            (workCenterId, batchOrderScanResult, true, DONE_QUANTITIES, action);
                } else {
                    return serviceCallInfoUtils.getArgumentForScannedResult
                            (workCenterId, batchOrderScanResult, false, DONE_QUANTITIES, action);
                }
            } else {
                try {
                    throw new InvalidBatchQRcodeException(getResources().getString(R.string.scan_valid_qr_for_batch));
                } catch (InvalidBatchQRcodeException e) {
                    SnackbarUtils.displaySnackbar(QRScanActivity.this.findViewById(android.R.id.content),
                            getResources().getString(R.string.scan_valid_qr_for_batch), OConstants.SnackbarType.SNACKBAR_TYPE_ERROR.iValue, false, Snackbar.LENGTH_LONG);
                }
            }
        }
        return null;
    }

    private void HandleResponse(OdooResult response, Boolean isActionAdded, int QRType) {
        HideProgress();
        if (response.containsKey(OConstants.ApiConstant.DIALOGUE) && response.containsKey(OConstants.ApiConstant.ACTIONS)) {
            String dialogDescription = response.getString(OConstants.ApiConstant.DIALOGUE);
            OdooResult actionObject = response.getMap(OConstants.ApiConstant.ACTIONS);

            actionKeyList = new ArrayList<>();
            actionList = new ArrayList<>();

            JSONObject resobj = null;
            try {
                resobj = new JSONObject(actionObject.toString());
                //using iterator get all key of object and get all values from that object.
                Iterator<?> keys = resobj.keys();
                while (keys.hasNext()) {
                    String key = (String) keys.next();
                    actionKeyList.add(key);
                    actionList.add(String.valueOf(resobj.get(key)));
                }
                if (actionKeyList.size() > 0) {
                    actionKeyList.add(0, OConstants.ACTION_KEY_CANCEL);
                    actionList.add(0, OConstants.ACTION_VALUE_CANCEL);
                }
                if (!isActionAdded) {
                    Utils.openActionDialog(QRScanActivity.this, OConstants.ACTION,
                            dialogDescription, actionList, actionDialogClickListener, QRType);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (response.containsKey(OConstants.ApiConstant.ERROR)) {
            String strMessage = response.getString(OConstants.ApiConstant.ERROR);
            int type = OConstants.SnackbarType.SNACKBAR_TYPE_ERROR.iValue;

            if (strMessage.length() > 80) {
                Utils.openErrorDialog(QRScanActivity.this, OConstants.ERROR, strMessage);
            } else {
                SnackbarUtils.displaySnackbar(this.findViewById(android.R.id.content),
                        strMessage, type, false, Snackbar.LENGTH_LONG);
            }
        } else if (response.containsKey(OConstants.ApiConstant.SUCCESS)) {
            String successMessage = response.getString(OConstants.ApiConstant.SUCCESS);
            startActivity(new Intent(QRScanActivity.this, SuccessOrderActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    .putExtra(OConstants.BundleParams.IS_SUBCONTRACTOR, false)
                    .putExtra(OConstants.BundleParams.ORDER_ID, textBatchOrderId.getText().toString())
                    .putExtra(OConstants.BundleParams.SUCCESS_MESSAGE, successMessage));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrscan);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        apiInterface = APIClient.getClient().create(ApiInterface.class);

        sharedPreferences = getSharedPreferences(Preference, MODE_PRIVATE);

        requestPermission();
        initData();
//        launchSomeActivity = registerForActivityResult(
//                new ActivityResultContracts.StartActivityForResult(),
//                result -> {
//                    if (result.getResultCode() == Activity.RESULT_OK) {
//                        Intent data = result.getData();
//                        imageListData.clear();
//                        imageList.clear();
//                        if (data != null) {
//                            imageListData = data.getParcelableArrayListExtra(Constants.EXTRA_IMAGES);
//                            for (Image image : imageListData) {
//                                imageList.add(Utils.createCopyAndReturnRealPath(QRScanActivity.this,image.getUri()));
//                            }
//                            setRecyclerviewAdapter();
//                        }
//
//                    }
//                });
    }

    private void initData() {
        actionDialogClickListener = this;
        imageList = new ArrayList<>();
        recyclerViewItemClickListener = this;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.containsKey(OConstants.KEY_WORK_CENTER_DATA)) {
                map = (Map<String, String>) extras.getSerializable(OConstants.KEY_WORK_CENTER_DATA);
            }
        }
        if (map != null) {
            workCenterId = Integer.parseInt(map.get(WORK_CENTER_ID));
            textWorkCenter.setText(getResources().getString(R.string.work_center) + " " + map.get(WORK_CENTER_NAME));
        }
        disableButton();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult:data " + data + "  requestCode:" + requestCode + "   resultCode:" + resultCode);
        if(resultCode == Activity.RESULT_OK){
//            Intent myData = data.getData();
//            imageListData.clear();
//            imageList.clear();
//            if (myData != null) {
//                imageListData = myData.getParcelableArrayListExtra(Constants.EXTRA_IMAGES);
//                for (Image image : imageListData) {
//                    imageList.add(Utils.createCopyAndReturnRealPath(QRScanActivity.this,image.getUri()));
//                }
//                setRecyclerviewAdapter();
//            }

            Uri myData = data.getData();
            if(myData!=null){
                imageList.add(Utils.createCopyAndReturnRealPath(QRScanActivity.this,myData));
            }
            setRecyclerviewAdapter();

        }
    }

    void requestPermission() {

        // if (ContextCompat.checkSelfPermission(this,
        //         Manifest.permission.CAMERA)
        //         != PackageManager.PERMISSION_GRANTED) {

        //     if (ActivityCompat.shouldShowRequestPermissionRationale(this,
        //             Manifest.permission.CAMERA)) {

        //         ActivityCompat.requestPermissions(this,
        //                 new String[]{Manifest.permission.CAMERA},
        //                 MY_PERMISSIONS_REQUEST_CAMERA);

        //         Log.d(TAG, "requestPermission showing the dialog: ");
        //     } else {

        //         ActivityCompat.requestPermissions(this,
        //                 new String[]{Manifest.permission.CAMERA},
        //                 MY_PERMISSIONS_REQUEST_CAMERA);

        //         Log.d(TAG, "requestPermission: ");
        //     }
        // } else {
        //     isPermissionEnabled = true;
        // }
        int currentAPIVersion = Build.VERSION.SDK_INT;

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_MEDIA_IMAGES)
                != PackageManager.PERMISSION_GRANTED){

                    if (ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED) {

                        ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.CAMERA},
                                MY_PERMISSIONS_REQUEST_CAMERA);
                    }
                    else{
                        if (currentAPIVersion >= Build.VERSION_CODES.TIRAMISU){
                            ActivityCompat.requestPermissions(this,
                                    new String[] {  Manifest.permission.READ_MEDIA_IMAGES },
                                    MY_PERMISSIONS_REQUEST_MEDIA);
                        } else {
                            ActivityCompat.requestPermissions(this,
                                    new String[] {  Manifest.permission.WRITE_EXTERNAL_STORAGE },
                                    MY_PERMISSIONS_REQUEST_STORAGE);
                        }
                    }
        } else {
            Log.e("444 ------>>>> permission enable", String.valueOf(isPermissionEnabled));
            isPermissionEnabled = true;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        barcodeView.pause();
    }

    @Override
    public void onResume() {
        super.onResume();
        barcodeView.resume();
    }

    private void startScanning() {
        if (isPermissionEnabled) {
            barcodeView.getBarcodeView().setDecoderFactory(new DefaultDecoderFactory());
            barcodeView.decodeSingle(callback);
            disableButton();
            imgQRCode.setVisibility(View.INVISIBLE);
            textBatchOrderId.setVisibility(View.INVISIBLE);
            imgCamera_closed.setVisibility(View.VISIBLE);
        } else {
            requestPermission();
        }
    }

    private void stopScanning() {
        imgQRCode.setVisibility(View.VISIBLE);
        imgCamera_closed.setVisibility(View.INVISIBLE);
    }

    private void disableButton() {
        btnConfirmBatch.setEnabled(false);
        btnConfirmBatch.setBackgroundColor(getResources().getColor(R.color.colorDarkGrey));
    }

    private void enableButton() {
        btnConfirmBatch.setEnabled(true);
        btnConfirmBatch.setBackgroundColor(getResources().getColor(R.color.android_green));
    }

    private void HandleScannedResult(BarcodeResult result) {
        try {
            JSONObject jsonObject = new JSONObject(result.getText());
            // subcontractor
            if (jsonObject.has(WorkOrderScanResult.QR_CODE_TYPE)) {
                workOrderScanResult = new WorkOrderScanResult(jsonObject);
                if (workOrderScanResult.isValidWorkOrderScan()) {
                    enableButton();
                    QRType = OConstants.QR_TYPE_WORK_ORDER;
                    textBatchOrderId.setVisibility(View.VISIBLE);
                    textBatchOrderId.setText(getResources().getString(R.string.work_order_id) + "" + String.format("%02d", workOrderScanResult.getWork_order_id()));
                } else {
                    throw new InvalidWorkQRcodeException(getResources().getString(R.string.scan_valid_qr_for_work));
                }
            } else {
                // normal user
                batchOrderScanResult = new BatchOrderScanResult(jsonObject);
                if (batchOrderScanResult.isValidBatchOrderId()) {
                    QRType = OConstants.QR_TYPE_BATCH_ORDER;
                    textBmoNumber.setVisibility(View.VISIBLE);
                    textBmoNumber.setText(getResources().getString(R.string.bmo_number) + " " + batchOrderScanResult.get_bmo_number());
                    textBatchOrderId.setVisibility(View.VISIBLE);
                    textBatchOrderId.setText(getResources().getString(R.string.batch_order_id) + "" + String.format("%02d", batchOrderScanResult.getBatchordre_id()));
                    enableButton();
                } else {
                    throw new InvalidBatchQRcodeException(getResources().getString(R.string.scan_valid_qr_for_batch));
                }
            }
        } catch (JSONException e) {
            SnackbarUtils.displaySnackbar(QRScanActivity.this.findViewById(android.R.id.content),
                    getResources().getString(R.string.scan_valid_qr), OConstants.SnackbarType.SNACKBAR_TYPE_ERROR.iValue, false, Snackbar.LENGTH_LONG);
            e.printStackTrace();

        } catch (InvalidBatchQRcodeException e) {
            SnackbarUtils.displaySnackbar(QRScanActivity.this.findViewById(android.R.id.content),
                    getResources().getString(R.string.scan_valid_qr_for_batch), OConstants.SnackbarType.SNACKBAR_TYPE_ERROR.iValue, false, Snackbar.LENGTH_LONG);
            e.printStackTrace();
        } catch (InvalidWorkQRcodeException e) {
            SnackbarUtils.displaySnackbar(QRScanActivity.this.findViewById(android.R.id.content),
                    getResources().getString(R.string.scan_valid_qr_for_work), OConstants.SnackbarType.SNACKBAR_TYPE_ERROR.iValue, false, Snackbar.LENGTH_LONG);
            e.printStackTrace();
        }
    }

    private void showProgressDialog() {
        progressDialog = new ProgressDialog(QRScanActivity.this);
        progressDialog.setMessage(getResources().getString(R.string.please_wait));
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();
    }

    private void HideProgress() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void onDialogActionClick(int position, int QRType, boolean is_full_quantity, float done_quantity) {
        IS_FULL_QUANTITY = is_full_quantity;
        DONE_QUANTITIES = done_quantity;
        if (position != 0) {
            callService(true, actionKeyList.get(position), QRType, true);
        }
    }

    public void initialcallService(Boolean is_full_quantity, Float done_quantity, boolean isShow) {
        IS_FULL_QUANTITY = is_full_quantity;
        DONE_QUANTITIES = done_quantity;
        callService(false, "", QRType, isShow);
    }


    private void openQuantityDialog() {

        dialog = new Dialog(this, R.style.MaterialDialogSheet);

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

        rvImagePreview = (RecyclerView) dialog.findViewById(R.id.rvImagePreview);
        Button btnBrowse = (Button) dialog.findViewById(R.id.btnBrowse);

        editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtView.setVisibility(View.GONE);
            }
        });

        btnBrowse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
//                pickImageFromGallery();
            }
        });

        okay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                boolean isValidQuantity;

                if (editText.getText().toString().matches("")) {
                    IS_FULL_QUANTITY = true;
                    DONE_QUANTITIES = 0f;
                    isValidQuantity = true;
                } else if (Float.parseFloat(editText.getText().toString()) > Float.MAX_VALUE) {
                    txtView.setVisibility(View.VISIBLE);
                    isValidQuantity = false;
                } else {
                    IS_FULL_QUANTITY = true;
                    DONE_QUANTITIES = Float.parseFloat(editText.getText().toString());
                    isValidQuantity = true;
                }

                if (isValidQuantity) {
                    dialog.dismiss();
                    if (imageList != null && imageList.size() > 0) {
                        uploadMultipleImages(false);
                    } else {
                        initialcallService(IS_FULL_QUANTITY, DONE_QUANTITIES, true);
                    }
                }
            }
        });
        dialog.show();
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_IMAGE);
    }

//    private void pickImageFromGallery() {
//        Intent  intent = new Intent(QRScanActivity.this, ImagePickerActivity.class);
//        intent.putExtra(Constants.EXTRA_CONFIG, getImagePickerConfig());
//        launchSomeActivity.launch(intent);
//    }
//
//    private ImagePickerConfig getImagePickerConfig() {
//        ImagePickerConfig imagePickerConfig = new ImagePickerConfig();
//        imagePickerConfig.setFolderMode(false);
//        imagePickerConfig.setMultipleMode(true);
//        imagePickerConfig.setShowNumberIndicator(true);
//        imagePickerConfig.setAlwaysShowDoneButton(false);
//        imagePickerConfig.setCameraOnly(false);
//        imagePickerConfig.setSelectedImages(imageListData);
//        imagePickerConfig.setStatusBarColor("#0097A7");
//        imagePickerConfig.setToolbarColor("#00BCD4");
//        imagePickerConfig.setProgressIndicatorColor("#00BCD4");
//        //imagePickerConfig.setSelectedIndicatorColor("#00BCD4");
//        imagePickerConfig.setShowCamera(true);
//        imagePickerConfig.setMaxSize(5);
//        return imagePickerConfig;
//
//    }

    @Override
    public void onItemClick(int position) {
        imageList.remove(position);
        imageListData.remove(position);
        if (imageList.size() > 0) {
            quentityImageListAdapter.notifyDataSetChanged();
            rvImagePreview.setVisibility(View.GONE);
        }
    }

    @SuppressWarnings("InvalidSetHasFixedSize")
    private void setRecyclerviewAdapter() {
        quentityImageListAdapter = new QuentityImageListAdapter(QRScanActivity.this, imageList, recyclerViewItemClickListener);
        rvImagePreview.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(QRScanActivity.this, LinearLayoutManager.HORIZONTAL, false);
        rvImagePreview.setLayoutManager(layoutManager);
        rvImagePreview.setAdapter(quentityImageListAdapter);
        rvImagePreview.setVisibility(View.VISIBLE);
    }
    private void uploadMultipleImages(boolean isShow) {
        showProgressDialog();
        sharedPreferences = getSharedPreferences(Preference, MODE_PRIVATE);
        String userId = PreferenceUtils.getPreference(sharedPreferences, keyUserId);

        RequestBody uid = RequestBody.create(userId, MediaType.parse("text/plain"));


        MultipartBody.Part[] uploadImages = new MultipartBody.Part[imageList.size()];

        for (int index = 0; index < imageList.size(); index++) {
            File file = new File(imageList.get(index));
            RequestBody imageAttachment = RequestBody.create(MediaType.parse("image/*"), file);
            uploadImages[index] = MultipartBody.Part.createFormData("images"+index, file.getName(), imageAttachment);
        }
        Call<Object> call = null;
        if (OConstants.QR_TYPE_WORK_ORDER == QRType) {
            RequestBody workOrderId = RequestBody.create(String.valueOf(workOrderScanResult.getWork_order_id()), MediaType.parse("text/plain"));
            call = apiInterface.uploadImage(uploadImages, workOrderId, uid);
        } else {
            if (batchOrderScanResult.isValidBatchOrderId() && batchOrderScanResult.isMrpQrCodeScan()) {
                RequestBody manufactureOrderId = RequestBody.create(String.valueOf(batchOrderScanResult.get_mrp_id()), MediaType.parse("text/plain"));
                call = apiInterface.uploadImageManufacture(uploadImages, manufactureOrderId, uid);
            } else {
                RequestBody batchOrderId = RequestBody.create(String.valueOf(batchOrderScanResult.getBatchordre_id()), MediaType.parse("text/plain"));
                call = apiInterface.uploadImageBatch(uploadImages, batchOrderId, uid);
            }
        }

        //finally performing the call
        call.enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                if (imageList != null) {
                    imageList.clear();
                }
                //Toast.makeText()
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        if (OConstants.ApiConstant.SUCCESS_CODE == response.code()) {
                            initialcallService(IS_FULL_QUANTITY, DONE_QUANTITIES, isShow);
                        } else {
                            HideProgress();
                        }
                    } else {
                        HideProgress();
                    }
                } else {
                    HideProgress();
                    if (OConstants.ApiConstant.INTERNAL_ERROR == response.code()) {
                        ResponseBody res = response.errorBody();
                        JSONObject json = null;
                        try {
                            json = new JSONObject(new String(res.bytes()));
                        } catch (JSONException | IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            if (json != null) {
                                Utils.openErrorDialog(QRScanActivity.this, OConstants.ERROR, json.getString("message"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                Utils.openErrorDialog(QRScanActivity.this, OConstants.ERROR, getString(R.string.error_something_went_wrong));
            }
        });
    }


}
