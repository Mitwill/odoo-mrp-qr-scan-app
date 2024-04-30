package com.mitwill.mrp.models;

public class UserAccess {
    private UserAccess() {
    }
    public static final String MODEL_NAME = "res.users";
    public static final String METHOD_NAME = "has_group";
    public static final String SUBCONTRACTOR_ARGUMENT= "subcontractor_mrp.group_mrp_subcontractor";
}
