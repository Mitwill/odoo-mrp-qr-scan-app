package com.mitwill.mrp.utils;

import com.mitwill.mrp.datas.OConstants;

import org.json.JSONException;
import org.json.JSONObject;


public class BatchOrderScanResult {
    public static final String TAG = BatchOrderScanResult.class.getSimpleName();
    public static final String BATCH_ORDER_ID = "bmo_id";
    public static final String BMO_NUMBER = "bmo_number";
    public static final String MRP_ID = "mrp_id";
    public static final String LABEL_QR_CODE = "labelqrcode";
    public static final String MRP_NAME = "mrp_name";
    private int batch_order_id = 0, mrp_id = 0;
    private String bmo_number = "";
    private boolean isLabelQRCode = false;
    private boolean isMrpQrCodeScan = false;
    private boolean isValidBatchScan = false;

    public BatchOrderScanResult(JSONObject jsonObject) throws InvalidBatchQRcodeException {
        try {
            if (jsonObject.has(BATCH_ORDER_ID)) {
                this.batch_order_id = jsonObject.getInt(BATCH_ORDER_ID);
                if (this.batch_order_id <= 0) {
                    isValidBatchScan = false;
                    throw new InvalidBatchQRcodeException(OConstants.INVALID_QR);
                } else {
                    if (jsonObject.has(MRP_ID)) {
                        this.mrp_id = jsonObject.getInt(MRP_ID);
                        if (jsonObject.has(LABEL_QR_CODE)) {
                            //This is for MRP Order Mask code
                            this.isLabelQRCode = jsonObject.getBoolean(LABEL_QR_CODE);
                            isValidBatchScan = true;
                            isMrpQrCodeScan= false;
                        } else {
                            // This is for MRP QR Order code
                            isValidBatchScan = true;
                            isMrpQrCodeScan= true;
                        }

                        if (jsonObject.has(MRP_NAME)) {
                            //TODO handle chnages for MRP here
                            isValidBatchScan = true;
                        } 
                    } else {
                        //This is for the Batch Order QR code which contains only bmo_id
                        isValidBatchScan = true;
                    }
                }
            } else {
                isValidBatchScan = false;
                throw new InvalidBatchQRcodeException(OConstants.INVALID_QR);
            }

            if (jsonObject.has(BMO_NUMBER)) {
                this.bmo_number = jsonObject.getString(BMO_NUMBER);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public boolean isLabelQRCode() {
        return isLabelQRCode;
    }

    public boolean isValidBatchOrderId() {
        return isValidBatchScan;
    }

    public int getBatchordre_id() {
        return batch_order_id;
    }

    public int get_mrp_id() {
        return mrp_id;
    }

    public String get_bmo_number() {
        return bmo_number;
    }

    public boolean isMrpQrCodeScan() {
        return isMrpQrCodeScan;
    }

}
