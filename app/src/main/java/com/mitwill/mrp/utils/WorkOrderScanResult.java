package com.mitwill.mrp.utils;

import com.mitwill.mrp.datas.OConstants;

import org.json.JSONException;
import org.json.JSONObject;


public class WorkOrderScanResult {
    public static final String TAG = WorkOrderScanResult.class.getSimpleName();
    private static final String WORK_ORDER_ID = "work_order_id";
    public static final String QR_CODE_TYPE = "qr_code_type";
    public static final String MRP_ID = "mrp_id";
    public static final String LABEL_QR_CODE = "labelqrcode";
    private int wo_id = 0,mrp_id = 0;
    private boolean isValidWorkOrderScan = false;

    public WorkOrderScanResult(JSONObject jsonObject) throws InvalidWorkQRcodeException {
        try {

            if (jsonObject.has(WORK_ORDER_ID)) {
                this.wo_id = jsonObject.getInt(WORK_ORDER_ID);

                if(jsonObject.has(QR_CODE_TYPE)){
                    String QR_TYPE_WORK_ORDER = "work_order";
                    if (QR_TYPE_WORK_ORDER.equals(jsonObject.getString(QR_CODE_TYPE)) && this.wo_id > 0){
                        isValidWorkOrderScan = true;
                    } else {
                        isValidWorkOrderScan = false;
                        throw new InvalidWorkQRcodeException(OConstants.INVALID_QR);
                    }
                } else {
                    isValidWorkOrderScan = false;
                    throw new InvalidWorkQRcodeException(OConstants.INVALID_QR);
                }
            } else {
                isValidWorkOrderScan = false;
                throw new InvalidWorkQRcodeException(OConstants.INVALID_QR);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public boolean isValidWorkOrderScan() {
        return isValidWorkOrderScan;
    }

    public int getWork_order_id() {
        return wo_id;
    }

    public int get_mrp_id() {
        return mrp_id;
    }

}
