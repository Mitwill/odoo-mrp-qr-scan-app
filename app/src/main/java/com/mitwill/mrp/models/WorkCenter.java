package com.mitwill.mrp.models;

public class WorkCenter {

    public static final String MODEL_NAME = "mrp.workcenter";
    public static final String WORK_CENTER_ID = "workcenter_id";
    public static final String WORK_CENTER_NAME = "workcenter_name";

    public String workCenterName;
    public String workCenterId;

    public WorkCenter() {
    }

    public WorkCenter(String workCenterId, String workCenter) {
        this.workCenterId = workCenterId;
        this.workCenterName = workCenter;
    }

    public String getWorkCenterId() {
        return workCenterId;
    }

    public void setWorkCenterId(String workCenterId) {
        this.workCenterId = workCenterId;
    }

    public String getWorkCenterName() {
        return workCenterName;
    }

    public void setWorkCenterName(String workCenterName) {
        this.workCenterName = workCenterName;
    }
}
