package com.app.squirrel.tool;

import android.os.Message;

import com.priv.yswl.base.tool.MSPUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.concurrent.atomic.AtomicBoolean;

public class UserManager {

    private static AtomicBoolean loginSta = new AtomicBoolean(false);

    public static boolean isLogin() {
        return loginSta.get();
    }

    public static void setLoginStatus(boolean a) {
        loginSta.set(a);
    }

    public static void login(String token,int isFace){
        MSPUtils.put("token", token);
        UserManager.setLoginStatus(true);
        Message message = Message.obtain();
        message.arg1 = isFace;
        EventBus.getDefault().postSticky(message);
    }


}
