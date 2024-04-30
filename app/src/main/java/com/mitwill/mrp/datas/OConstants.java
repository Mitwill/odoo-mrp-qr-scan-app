package com.mitwill.mrp.datas;

public class OConstants {

    public static final int QR_TYPE_BATCH_ORDER = 0;
    public static final int QR_TYPE_WORK_ORDER = 1;
    public static final String ACTION = "Action";
    public static final String ERROR = "Error";
    public static final String ACTION_KEY_CANCEL = "cancel";
    public static final String ACTION_VALUE_CANCEL = "Cancel";
    public static final String WS_GET_WORKCENTERS = "get_workcenters";
    public static final String KEY_WORK_CENTER_DATA = "work_center_data";
    public static final String WS_GET_QR_CODE_DETAILS = "get_qrcode_details";
    public static final String WS_GET_MASK_QR_CODE = "get_mask_qr_code";
    public static String USERNAME = "USERNAME";
    public static String userImage = "";
    public static final String INVALID_QR = "Invalid QR code";
    public static final int MAX_IMAGE_SELECTION = 5;

    public enum SnackbarType {
        SNACKBAR_TYPE_WARNING(0),
        SNACKBAR_TYPE_ERROR(1),
        SNACKBAR_TYPE_SUCCESS(2);
        public final int iValue;

        SnackbarType(int value) {
            this.iValue = value;
        }
    }

    public final class ApiConstant {
        private ApiConstant() {
        }
        public static final String RESULT = "result";
        public static final String ERROR = "error";
        public static final String SUCCESS = "success";
        public static final String DIALOGUE = "dialogue";
        public static final String ACTIONS = "actions";
        public static final String ACTION = "action";

        public static final int SUCCESS_CODE  = 200;
        public static final int INTERNAL_ERROR  = 500;
    }

    public final class BundleParams {
        private BundleParams() {
        }
        public static final String IS_SUBCONTRACTOR = "is_subcontractor";
        public static final String ORDER_ID = "order_id";
        public static final String SUCCESS_MESSAGE = "success_message";
    }
}