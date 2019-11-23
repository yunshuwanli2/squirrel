package com.app.squirrel.jpush;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;

import com.app.squirrel.http.okhttp.MDeviceUtil;
import com.app.squirrel.http.okhttp.MSPUtils;
import com.app.squirrel.tool.L;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import cn.jpush.android.api.CmdMessage;
import cn.jpush.android.api.CustomMessage;
import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.JPushMessage;
import cn.jpush.android.api.NotificationMessage;
import cn.jpush.android.service.JPushMessageReceiver;

public class SqRecive extends JPushMessageReceiver {

    private static final String TAG = "SqRecive";

    public void onMessage(Context var1, CustomMessage var2) {
        super.onMessage(var1, var2);
        L.d(TAG, "[onMessage]: "+var2.toString());
        if (!TextUtils.isEmpty(var2.message)) {
            try {
                JSONObject jsonObject = new JSONObject(var2.message);
                String token = jsonObject.optString("token");
                if (!TextUtils.isEmpty(token)) {
                    MSPUtils.put("token", token);
                    EventBus.getDefault().postSticky(new Message());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    public void onNotifyMessageOpened(Context var1, NotificationMessage var2) {
        super.onNotifyMessageOpened(var1, var2);
        L.d(TAG, "[onNotifyMessageOpened]");
    }

    public void onNotifyMessageArrived(Context var1, NotificationMessage var2) {
        super.onNotifyMessageArrived(var1, var2);
        L.d(TAG, "[onNotifyMessageArrived]");


    }

    public void onNotifyMessageDismiss(Context var1, NotificationMessage var2) {
        L.d(TAG, "[onNotifyMessageDismiss]");
    }

    public void onRegister(Context var1, String var2) {
        L.d(TAG, "[onRegister]");
    }

    public void onConnected(Context var1, boolean var2) {
        L.d(TAG, "[onConnected]");
        String ANDROID_ID = MDeviceUtil.getMAC(var1);
        L.d(TAG, "[ANDROID_ID]"+ANDROID_ID);
        JPushInterface.setAlias(var1,10, ANDROID_ID);
//        String UID= JPushInterface.getUdid(var1);
//        JPushInterface.getAlias(var1,1);
//        L.e(TAG,"UID:"+UID);
    }

    public void onCommandResult(Context var1, CmdMessage var2) {
        L.d(TAG, "[onCommandResult]");
    }

    public void onMultiActionClicked(Context var1, Intent var2) {
        super.onMultiActionClicked(var1, var2);
        L.d(TAG, "[onMultiActionClicked]");
    }

    public void onTagOperatorResult(Context var1, JPushMessage var2) {
        L.d(TAG, "[onTagOperatorResult]");
    }

    public void onCheckTagOperatorResult(Context var1, JPushMessage var2) {
        L.d(TAG, "[onCheckTagOperatorResult]");
    }

    public void onAliasOperatorResult(Context var1, JPushMessage var2) {
        L.d(TAG, "[onAliasOperatorResult]: "+var2.toString());

    }

    public void onMobileNumberOperatorResult(Context var1, JPushMessage var2) {
        L.d(TAG, "[onMobileNumberOperatorResult]");
    }
}
