package com.mitwill.mrp.utils;

import com.mitwill.mrp.core.rpc.helper.OArguments;
import com.mitwill.mrp.datas.OConstants;
import com.mitwill.mrp.models.UserAccess;
import com.mitwill.mrp.models.WorkOrder;

import org.json.JSONObject;

import java.util.HashMap;

import static com.mitwill.mrp.datas.OConstants.WS_GET_QR_CODE_DETAILS;
import static com.mitwill.mrp.datas.OConstants.WS_GET_MASK_QR_CODE;
import static com.mitwill.mrp.models.BatchOrder.BATCH_ORDER_ID;
import static com.mitwill.mrp.models.BatchOrder.MRP_ID;
import static com.mitwill.mrp.models.WorkCenter.WORK_CENTER_ID;
import static com.mitwill.mrp.models.BatchOrder.DONE_QUANTITY;

public class ServiceCallInfoUtils {

    public String methodName = WS_GET_QR_CODE_DETAILS;
    public OArguments getArgumentForScannedResult(int workCenterId, BatchOrderScanResult batchOrderScanResult,
                                                  Boolean isActionRequired,Float done_quantity,String action) {
        OArguments arguments = new OArguments();
        HashMap<String, Object> dataList = new HashMap<>();
        dataList.put(WORK_CENTER_ID, workCenterId);
        dataList.put(BATCH_ORDER_ID, batchOrderScanResult.getBatchordre_id());

        dataList.put(DONE_QUANTITY,done_quantity);
        dataList.put(MRP_ID,batchOrderScanResult.get_mrp_id());
        int mrp_id = batchOrderScanResult.get_mrp_id();

        if (batchOrderScanResult.isLabelQRCode()) {
            dataList.put(MRP_ID, batchOrderScanResult.get_mrp_id());
            methodName = WS_GET_MASK_QR_CODE;
        }
        if (isActionRequired) {
            dataList.put(OConstants.ApiConstant.ACTION, action);
        }
        arguments.add(new JSONObject(dataList));
        return arguments;
    }

    public String getMethodName() {
        return methodName;
    }

    public OArguments getArgumentForUserAccess() {
        OArguments arguments = new OArguments();
        arguments.add(UserAccess.SUBCONTRACTOR_ARGUMENT);
        return arguments;
    }

    public OArguments getArgumentForScannedWorkOrderResult(WorkOrderScanResult workOrderScanResult,
                                                           Boolean isActionRequired, String action, float done_quantity) {
        methodName = WorkOrder.METHOD_NAME;
        OArguments arguments = new OArguments();
        HashMap<String, Object> dataList = new HashMap<>();
        dataList.put(WorkOrder.WORK_ORDER_ID, workOrderScanResult.getWork_order_id());
        if (done_quantity > 0) {
            dataList.put(DONE_QUANTITY,done_quantity);
        }
        dataList.put(MRP_ID,workOrderScanResult.get_mrp_id());
        if (isActionRequired) {
            dataList.put(WorkOrder.WORK_ORDER_ACTION, action);
        }
        arguments.add(new JSONObject(dataList));
        return arguments;
    }
}
