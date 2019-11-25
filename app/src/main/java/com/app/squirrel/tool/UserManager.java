package com.app.squirrel.tool;

import java.util.concurrent.atomic.AtomicBoolean;

public class UserManager {

    private static AtomicBoolean loginSta = new AtomicBoolean();
    public static boolean isLogin() {
        return loginSta.get();
    }

    public static void setLoginStatus(boolean a) {
        loginSta.set(a);
    }

}
