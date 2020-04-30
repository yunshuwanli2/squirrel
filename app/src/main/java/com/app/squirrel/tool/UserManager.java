package com.app.squirrel.tool;

import android.os.Message;

import com.priv.yswl.base.tool.MSPUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.concurrent.atomic.AtomicBoolean;

public class UserManager {

    private static AtomicBoolean loginSta = new AtomicBoolean(false);


    private static AtomicBoolean isFace = new AtomicBoolean(false);

    public static boolean isLogin() {
        return loginSta.get();
    }

    public static void setLoginStatus(boolean a) {
        loginSta.set(a);
    }

    public static boolean isFace() {
        return isFace.get();
    }

    public static void setIsFace(int isFace) {
        if (isFace == 0) {
            UserManager.isFace.set(false);
        }else {
            UserManager.isFace.set(true);
        }

    }

    public static void login(String token, int isFace) {
        MSPUtils.put("token", token);
        UserManager.setLoginStatus(true);
        Message message = Message.obtain();
        message.arg1 = isFace;
        EventBus.getDefault().postSticky(message);
    }


}
