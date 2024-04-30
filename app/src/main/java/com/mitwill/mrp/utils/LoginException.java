package com.mitwill.mrp.utils;

import com.mitwill.mrp.core.rpc.listeners.OdooError;

public class LoginException {

    public static void loginFail(OdooError error) throws LoginFailException {

        if (error.getMessage().equals("")) {
            throw new LoginFailException(error.getServerTrace());
        } else {
            throw new LoginFailException(error.getMessage());
        }
    }
}