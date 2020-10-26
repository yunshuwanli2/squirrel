package com.app.squirrel.jpush;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.app.squirrel.BuildConfig;
import com.app.squirrel.serial.Rs232OutService;
import com.app.squirrel.tool.UserManager;
import com.priv.yswl.base.tool.L;
import com.priv.yswl.base.tool.MDeviceUtil;

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
        L.d(TAG, "消息推送内容---[onMessage]: " + var2.toString());
        String content = var2.message;
        if (!TextUtils.isEmpty(content)) {
                BasePushBean date = BasePushBean.getBasePush(content);
                if(date.type.equals("1")){
                    LoginPushBean.DataBean loginPushBean =LoginPushBean.getUserInfo(content);
                    if(null == loginPushBean){
                        L.e(TAG,"推送消息 登录json为null");
                    }
                    String token = loginPushBean.getToken();
                    int isFace = loginPushBean.getIsFace();
                    if (!TextUtils.isEmpty(token)) {
                        UserManager.sendLoginEvenBus(token,isFace);
                    }
                }else if(date.type.equals("2")){
                    TimeSetPushBean.DataBean timeBean = TimeSetPushBean.getTimeInfo(content);
                    if(null==timeBean){
                        L.e(TAG,"推送消息 时间设置的json为null");
                        return;
                    }
                    String number = timeBean.getNumber();
                    if(TextUtils.isEmpty(number)){
                        L.e(TAG,"推送消息 number为null");
                        return;
                    }
                    String[] numbs = number.split(",");

                    for(String nub :numbs){
                        //TODO TEST kangpeng 版本发布需要注释打开
                        if(!BuildConfig.DEBUG){
                         Rs232OutService.setTime(Integer.parseInt(nub),timeBean.isIsOn(),timeBean.getTimes());
                        }
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                }else if(date.type.equals("3")){//垃圾桶置零
                    ZeroSetPushBean.DataBean dataBean = ZeroSetPushBean.getZeroSetInfo(content);
                    if(null == dataBean){
                        return;
                    }
                    String number = dataBean.getNumber();
                    if(TextUtils.isEmpty(number)){
                        L.e(TAG,"推送消息 桶子置零 number为null");
                        return;
                    }
                    String[] numbs = number.split(",");

                    for(String nub :numbs){
                        //TODO TEST kangpeng 版本发布需要注释打开
                        if(!BuildConfig.DEBUG){
                            Rs232OutService.reset(Integer.parseInt(nub));
                        }
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }


                }else if(date.type.equals("4")){//垃圾桶零点校准
                    ZeroCheckPushBean.DataBean dataBean = ZeroCheckPushBean.getZeroCheckInfo(content);
                    if(null == dataBean){
                        return;
                    }
                    String number = dataBean.getNumber();
                    if(TextUtils.isEmpty(number)){
                        L.e(TAG,"推送消息 零点校准 number为null");
                        return;
                    }
                    String[] numbs = number.split(",");

                    for(String nub :numbs){
                        //TODO TEST kangpeng 版本发布需要注释打开
                        if(!BuildConfig.DEBUG){
                            Rs232OutService.reset_0(Integer.parseInt(nub));
                        }
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }else if(date.type.equals("5")){//垃圾桶负载校准
                    WeighCheckPushBean.DataBean dataBean = WeighCheckPushBean.getWeighCheckInfo(content);
                    if(null == dataBean){
                        return;
                    }
                    String number = dataBean.getNumber();
                    if(TextUtils.isEmpty(number)){
                        L.e(TAG,"推送消息 垃圾负载number为null");
                        return;
                    }
                    String[] numbs = number.split(",");

                    for(String nub :numbs){
                        //TODO TEST kangpeng 版本发布需要注释打开
                        if(!BuildConfig.DEBUG){
                            Rs232OutService.resetWight(Integer.parseInt(nub),Integer.parseInt(dataBean.getWeight()));
                        }
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
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
        L.d(TAG, "[ANDROID_ID]" + ANDROID_ID);
        JPushInterface.setAlias(var1, 10, ANDROID_ID);
//        String UID= JPushInterface.getUdid(var1);
//        JPushInterface.getAlias(var1,1);
//        L.e(TAG,"UID:"+UID);
    }

    public void onCommandResult(Context var1, CmdMessage var2) {
//        L.d(TAG, "[onCommandResult]");
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
        L.d(TAG, "[onAliasOperatorResult]: " + var2.toString());

    }

    public void onMobileNumberOperatorResult(Context var1, JPushMessage var2) {
        L.d(TAG, "[onMobileNumberOperatorResult]");
    }
}
