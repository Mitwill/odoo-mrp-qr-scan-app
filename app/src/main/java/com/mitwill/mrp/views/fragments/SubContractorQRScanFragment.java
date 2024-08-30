package com.mitwill.mrp.views.fragments;


import static android.content.Context.MODE_PRIVATE;
import static com.mitwill.mrp.utils.PreferenceUtils.Preference;
import static com.mitwill.mrp.utils.PreferenceUtils.keyUserId;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.loader.content.CursorLoader;
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
import com.mitwill.mrp.interfaces.RecyclerViewItemClickListener;
import com.mitwill.mrp.models.WorkOrder;
import com.mitwill.mrp.retrofit.apiinterface.APIClient;
import com.mitwill.mrp.retrofit.apiinterface.ApiInterface;
import com.mitwill.mrp.services.ServiceHandler;
import com.mitwill.mrp.utils.InvalidWorkQRcodeException;
import com.mitwill.mrp.utils.PreferenceUtils;
import com.mitwill.mrp.utils.ServiceCallInfoUtils;
import com.mitwill.mrp.utils.Utils;
import com.mitwill.mrp.utils.WorkOrderScanResult;
import com.mitwill.mrp.views.QRScanActivity;
import com.mitwill.mrp.views.SuccessOrderActivity;
//import com.opensooq.supernova.gligar.GligarPicker;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * A simple {@link Fragment} subclass.
 */
public class SubContractorQRScanFragment extends Fragment implements View.OnClickListener, RecyclerViewItemClickListener {

    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 2;
    private static final int MY_PERMISSIONS_REQUEST_STORAGE = 3;
    private static final int MY_PERMISSIONS_REQUEST_MEDIA = 4;
    private static final int SELECT_IMAGE = 1889;
    private Boolean IS_FULL_QUANTITY;
    private Float DONE_QUANTITIES;
    private int multipleImageRequestCode = 100;
    private List<String> imageList;
    private RecyclerView rvImagePreview;
    private QuentityImageListAdapter quentityImageListAdapter;
    private RecyclerViewItemClickListener recyclerViewItemClickListener;

    @BindView(R.id.fragment_subcontractor_qrscanner_qrview)
    DecoratedBarcodeView barcodeView;

    @BindView(R.id.fragment_subcontractor_qrscanner_img_qr)
    ImageView imgQRCode;

    @BindView(R.id.btn_confirm_batch)
    Button btnConfirmBatch;

    @BindView(R.id.work_order_id)
    TextView textWorkOrderId;

    @BindView(R.id.camera_closed)
    ImageView imgCameraClosed;

    private ProgressDialog progressDialog;
    private WorkOrderScanResult workOrderScanResult;
    private boolean isPermissionEnabled = false;
    private Dialog dialog = null;

    private ApiInterface apiInterface;
    private SharedPreferences sharedPreferences;
//    ActivityResultLauncher<Intent> launchSomeActivity;
    ArrayList<Image> imageListData = new ArrayList<>();

    private BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if (result.getText() == null) {
                SnackbarUtils.displaySnackbar(getActivity().findViewById(android.R.id.content),
                        getResources().getString(R.string.scan_valid_qr_for_work), OConstants.SnackbarType.SNACKBAR_TYPE_ERROR.iValue, false, Snackbar.LENGTH_LONG);
                return;
            }
            stopScanning();
            handleScannedResult(result);
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
            //This is the method of the barcode callback
        }
    };

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

    public SubContractorQRScanFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sub_contractor_qrscan, container, false);
        ButterKnife.bind(this, view);

        requestPermission();
        disableButton();
        imgQRCode.setOnClickListener(this);
        btnConfirmBatch.setOnClickListener(this);
        imgCameraClosed.setOnClickListener(this);
        imageList = new ArrayList<>();
        recyclerViewItemClickListener = this;
        apiInterface = APIClient.getClient().create(ApiInterface.class);
        sharedPreferences = getActivity().getSharedPreferences(Preference, MODE_PRIVATE);
        String userId = PreferenceUtils.getPreference(sharedPreferences, keyUserId);
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
//                                imageList.add(Utils.createCopyAndReturnRealPath(getActivity(),image.getUri()));
//                            }
//                            setRecyclerviewAdapter();
//                        }
//
//                    }
//                });
        return view;
    }

//    private void pickImageFromGallery() {
//        Intent  intent = new Intent(getActivity(), ImagePickerActivity.class);
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
//        imagePickerConfig.setShowCamera(true);
//        imagePickerConfig.setSelectedImages(imageListData);
//        imagePickerConfig.setStatusBarColor("#0097A7");
//        imagePickerConfig.setToolbarColor("#00BCD4");
//        imagePickerConfig.setProgressIndicatorColor("#00BCD4");
//        imagePickerConfig.setMaxSize(5);
//        return imagePickerConfig;
//
//    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_IMAGE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("SUBCOntractor QR SCAN FRAGMENT", "onActivityResult:data " + data + "  requestCode:" + requestCode + "   resultCode:" + resultCode);
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
                imageList.add(Utils.createCopyAndReturnRealPath(getActivity(),myData));
            }
            setRecyclerviewAdapter();

        }
    }


    void requestPermission() {
        // if (ContextCompat.checkSelfPermission(getActivity(),
        //         Manifest.permission.CAMERA)
        //         != PackageManager.PERMISSION_GRANTED) {

        //     ActivityCompat.requestPermissions(getActivity(),
        //             new String[]{Manifest.permission.CAMERA},
        //             MY_PERMISSIONS_REQUEST_CAMERA);
        // }

        int currentAPIVersion = Build.VERSION.SDK_INT;
        
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.READ_MEDIA_IMAGES)
                != PackageManager.PERMISSION_GRANTED){

                    if (ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED) {

                        ActivityCompat.requestPermissions(getActivity(),
                                new String[]{Manifest.permission.CAMERA},
                                MY_PERMISSIONS_REQUEST_CAMERA);
                    }
                    else{
                        if (currentAPIVersion >= Build.VERSION_CODES.TIRAMISU){
                            ActivityCompat.requestPermissions(getActivity(),
                                    new String[] {  Manifest.permission.READ_MEDIA_IMAGES },
                                    MY_PERMISSIONS_REQUEST_MEDIA);
                        } else {
                            ActivityCompat.requestPermissions(getActivity(),
                                    new String[] {  Manifest.permission.WRITE_EXTERNAL_STORAGE },
                                    MY_PERMISSIONS_REQUEST_STORAGE);
                        }
                    }
        }
         else {
            isPermissionEnabled = true;
        }
    }

    private void startScanning() {
        if (isPermissionEnabled) {
            barcodeView.getBarcodeView().setDecoderFactory(new DefaultDecoderFactory());
            barcodeView.decodeSingle(callback);
            disableButton();
            imgQRCode.setVisibility(View.INVISIBLE);
            textWorkOrderId.setVisibility(View.INVISIBLE);
            imgCameraClosed.setVisibility(View.VISIBLE);
        } else {
            requestPermission();
        }
    }

    private void stopScanning() {
        imgQRCode.setVisibility(View.VISIBLE);
        imgCameraClosed.setVisibility(View.INVISIBLE);
    }

    private void enableButton() {
        btnConfirmBatch.setEnabled(true);
        btnConfirmBatch.setBackgroundColor(getResources().getColor(R.color.android_green));
    }

    private void disableButton() {
        btnConfirmBatch.setEnabled(false);
        btnConfirmBatch.setBackgroundColor(getResources().getColor(R.color.colorDarkGrey));
    }

    private void showProgressDialog() {
        progressDialog = new ProgressDialog(getActivity());
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fragment_subcontractor_qrscanner_img_qr:
                startScanning();
                break;
            case R.id.btn_confirm_batch:
//                verifyWorkOrderDetails();
                openQuantityDialog();
                break;
            case R.id.camera_closed:
                stopScanning();
                break;
            default:
                break;
        }
    }

    public void verifyWorkOrderDetails(boolean isShow) {
        if (Utils.netConnect(getActivity())) {
            callService(isShow);
//            Utils.openBeforeActionDialog(getActivity(), this);
        } else {
            SnackbarUtils.displaySnackbar(getActivity().findViewById(android.R.id.content),
                    getString(R.string.check_connection), OConstants.SnackbarType.SNACKBAR_TYPE_ERROR.iValue, false, Snackbar.LENGTH_LONG);
        }
    }

    private void handleScannedResult(BarcodeResult result) {
        try {
            JSONObject jsonObject = new JSONObject(result.getText());
            workOrderScanResult = new WorkOrderScanResult(jsonObject);
            if (workOrderScanResult.isValidWorkOrderScan()) {
                textWorkOrderId.setVisibility(View.VISIBLE);
                textWorkOrderId.setText(getResources().getString(R.string.work_order_id) + "" + String.format("%02d", workOrderScanResult.getWork_order_id()));
                enableButton();
            } else {
                SnackbarUtils.displaySnackbar(getActivity().findViewById(android.R.id.content),
                        getResources().getString(R.string.scan_valid_qr_for_batch), OConstants.SnackbarType.SNACKBAR_TYPE_ERROR.iValue, false, Snackbar.LENGTH_LONG);
                throw new InvalidWorkQRcodeException(getResources().getString(R.string.scan_valid_qr_for_work));
            }
        } catch (JSONException e) {
            e.printStackTrace();

            SnackbarUtils.displaySnackbar(getActivity().findViewById(android.R.id.content),
                    getResources().getString(R.string.scan_valid_qr), OConstants.SnackbarType.SNACKBAR_TYPE_ERROR.iValue, false, Snackbar.LENGTH_LONG);

        } catch (InvalidWorkQRcodeException e) {
            SnackbarUtils.displaySnackbar(getActivity().findViewById(android.R.id.content),
                    getResources().getString(R.string.scan_valid_qr_for_work), OConstants.SnackbarType.SNACKBAR_TYPE_ERROR.iValue, false, Snackbar.LENGTH_LONG);
            e.printStackTrace();
        }
    }

    private void callService(boolean isShow) {
        disableButton();
        ServiceHandler callService = new ServiceHandler();
        String strModelName = WorkOrder.MODEL_NAME;
        if (workOrderScanResult.isValidWorkOrderScan()) {
            HashMap<String, Object> data = new HashMap<>();
            if (isShow) {
                showProgressDialog();
            }
            ServiceCallInfoUtils serviceCallInfoUtils = new ServiceCallInfoUtils();
            OArguments arguments;
            arguments = serviceCallInfoUtils.getArgumentForScannedWorkOrderResult(workOrderScanResult, false, "", DONE_QUANTITIES);
            callService.callMethod(strModelName, serviceCallInfoUtils.getMethodName(), arguments, data, new IOdooResponse() {
                @Override
                public void onResponse(OdooResult response) {
                    handleResponse(response);
                }

                @Override
                public void onError(OdooError error) {
                    HideProgress();
                    Utils.openErrorDialog(getActivity(), error.getMessage(), error.getServerTrace(), true);
                }
            });
        }
    }

    private void handleResponse(OdooResult response) {
        HideProgress();
        if (response.containsKey(OConstants.ApiConstant.ERROR)) {
            String strMessage = response.getString(OConstants.ApiConstant.ERROR);
            int type = OConstants.SnackbarType.SNACKBAR_TYPE_ERROR.iValue;

            if (strMessage.length() > 80) {
                Utils.openErrorDialog(getActivity(), OConstants.ERROR, strMessage);
            } else {
                SnackbarUtils.displaySnackbar(getActivity().findViewById(android.R.id.content),
                        strMessage, type, false, Snackbar.LENGTH_LONG);
            }
        } else if (response.containsKey(OConstants.ApiConstant.SUCCESS)) {
            String successMessage = response.getString(OConstants.ApiConstant.SUCCESS);
            startActivity(new Intent(getActivity(), SuccessOrderActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    .putExtra(OConstants.BundleParams.IS_SUBCONTRACTOR, true)
                    .putExtra(OConstants.BundleParams.ORDER_ID, textWorkOrderId.getText().toString())
                    .putExtra(OConstants.BundleParams.SUCCESS_MESSAGE, successMessage));
        } else {
            String strMessage = getString(R.string.error_something_went_wrong);
            int type = OConstants.SnackbarType.SNACKBAR_TYPE_ERROR.iValue;

            SnackbarUtils.displaySnackbar(getActivity().findViewById(android.R.id.content),
                    strMessage, type, false, Snackbar.LENGTH_LONG);
        }
    }

    public void initialcallService(Boolean is_full_quantity, Float done_quantity) {
        IS_FULL_QUANTITY = is_full_quantity;
        DONE_QUANTITIES = done_quantity;
        //  callService(isShow);
    }

    public void openQuantityDialog() {

        dialog = new Dialog(getActivity(), R.style.MaterialDialogSheet);
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
//                    initialcallService(IS_FULL_QUANTITY, DONE_QUANTITIES);
//                    verifyWorkOrderDetails();
                    dialog.dismiss();
                    if (imageList != null && imageList.size() > 0) {
                        uploadMultipleImages(false);
                    } else {
                        verifyWorkOrderDetails(true);
                    }
                }
            }
        });
        dialog.show();
        // imageList
    }

    @Override
    public void onItemClick(int position) {
        imageList.remove(position);
        imageListData.remove(position);
        if (imageList.size() > 0) {
            quentityImageListAdapter.notifyDataSetChanged();
            rvImagePreview.setVisibility(View.VISIBLE);
        } else {
            rvImagePreview.setVisibility(View.GONE);
        }
    }

    @SuppressWarnings("InvalidSetHasFixedSize")
    private void setRecyclerviewAdapter() {
        quentityImageListAdapter = new QuentityImageListAdapter(getActivity(), imageList, recyclerViewItemClickListener);
        rvImagePreview.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        rvImagePreview.setLayoutManager(layoutManager);
        rvImagePreview.setAdapter(quentityImageListAdapter);
        rvImagePreview.setVisibility(View.VISIBLE);
    }

    private void uploadMultipleImages(boolean isShow) {
        showProgressDialog();
        sharedPreferences = getActivity().getSharedPreferences(Preference, MODE_PRIVATE);
        String userId = PreferenceUtils.getPreference(sharedPreferences, keyUserId);

        RequestBody workOrderId = RequestBody.create(String.valueOf(workOrderScanResult.getWork_order_id()), MediaType.parse("text/plain"));
        RequestBody uid = RequestBody.create(userId, MediaType.parse("text/plain"));

        MultipartBody.Part[] uploadImages = new MultipartBody.Part[imageList.size()];

        for (int index = 0; index < imageList.size(); index++) {
            File file = new File(imageList.get(index));
            RequestBody imageAttachment = RequestBody.create(MediaType.parse("image/*"), file);
            uploadImages[index] = MultipartBody.Part.createFormData("images"+index, file.getName(), imageAttachment);
        }

        Call<Object> call = apiInterface.uploadImage(uploadImages, workOrderId, uid);

        //finally performing the call
        call.enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                //Toast.makeText()
                if (imageList != null) {
                    imageList.clear();
                }
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        if (OConstants.ApiConstant.SUCCESS_CODE == response.code()) {
                            verifyWorkOrderDetails(isShow);
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
                                Utils.openErrorDialog(getActivity(), OConstants.ERROR, json.getString("message"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                Log.e("response failure", call.toString());
                Utils.openErrorDialog(getActivity(), OConstants.ERROR, getString(R.string.error_something_went_wrong));
            }
        });
    }
}
