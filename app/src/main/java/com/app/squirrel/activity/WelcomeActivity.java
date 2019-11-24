package com.app.squirrel.activity;

import android.os.CountDownTimer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.app.squirrel.R;
import com.app.squirrel.http.CallBack.HttpCallback;
import com.app.squirrel.http.HttpClientProxy;
import com.app.squirrel.http.okhttp.MSPUtils;
import com.app.squirrel.tool.L;
import com.app.squirrel.tool.ToastUtil;
import com.bumain.plc.ModbusService;
import com.bumain.plc.ModbusTest;
import com.bumain.plc.ModbusTime;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.app.squirrel.activity.WelcomeActivity.SafeHandler.MSG_UPDATE_TIME;
import static com.app.squirrel.application.SquirrelApplication.test;

public class WelcomeActivity extends BaseActivity implements View.OnClickListener, HttpCallback<JSONObject> {

    private static final String TAG = "WelcomeActivity";
    public static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// HH:mm:ss
    private int openNumb = -1;
    public HandlerThread mHandleThread;
    public SafeHandler mSafeHandle;
    public TextView tv_date;
    public TextView loginOrout;
    public TextView tv_harm_hint;
    public TextView tv_wet_hint;
    public TextView tv_recy_hint;
    public TextView tv_dry_hint;
    CountDownTimer timer;
    @Override
    public boolean getEventBusSetting() {
        return true;
    }


    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onEventMessage(Message message) {
        L.d(TAG, "[onEventMessage]");
        ToastUtil.showToast("登录成功");
        //
        EventBus.getDefault().removeStickyEvent(message);
        setLoginStatues();
        //登录成功2分钟后自动登出
        mSafeHandle.sendEmptyMessage(SafeHandler.MSG_OVERTIME_USER_LOGOUT);

        //开门
        if (openNumb == -1) return;
        requestOpen(openNumb);

    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        findViewById(R.id.ll_dry_garbage).setOnClickListener(this);
        findViewById(R.id.ll_harmful_garbage).setOnClickListener(this);
        findViewById(R.id.ll_recy_garbage).setOnClickListener(this);
        findViewById(R.id.ll_wet_garbage).setOnClickListener(this);
        tv_dry_hint = findViewById(R.id.tv_dry_hint);
        tv_harm_hint = findViewById(R.id.tv_harmful_hint);
        tv_recy_hint = findViewById(R.id.tv_recy_hint);
        tv_wet_hint = findViewById(R.id.tv_wet_hint);
        loginOrout = findViewById(R.id.tv_log_in_out);
        loginOrout.setOnClickListener(this);
        tv_date = findViewById(R.id.tv_date);
        mHandleThread = new HandlerThread(getClass().getSimpleName());
        mHandleThread.start();
        mSafeHandle = new SafeHandler(this, mHandleThread.getLooper());

    }

    @Override
    protected void onResume() {
        super.onResume();
        L.d(TAG, "[onResume]");
        mSafeHandle.sendEmptyMessageDelayed(SafeHandler.MSG_CHECK_PLC_STATUES,60*1000);
        L.d(TAG, "[MSG_CHECK_PLC_STATUES]");
        mSafeHandle.sendEmptyMessage(MSG_UPDATE_TIME);
        L.d(TAG, "[sendEmptyMessage ]MSG_UPDATE_TIME");
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSafeHandle.removeMessages(SafeHandler.MSG_CHECK_PLC_STATUES);
        mSafeHandle.removeMessages(SafeHandler.MSG_UPDATE_TIME);
        mSafeHandle.removeMessages(SafeHandler.MSG_OVERTIME_USER_LOGOUT);
    }

    @Override
    public void onSucceed(int requestId, JSONObject result) {
        L.d(TAG, "[onSucceed]"+result);
    }

    @Override
    public void onFail(int requestId, String errorMsg) {
        L.e(TAG, "[onFail]"+errorMsg);
    }


    final static class SafeHandler extends Handler {
        public static final int MSG_UPDATE_TIME = 0x0;
        public static final int MSG_UPDATE_COUNTDOWN_TIME= 0x3;
        public static final int MSG_CHECK_PLC_STATUES = 0x1;
        public static final int MSG_OVERTIME_USER_LOGOUT = 0x2;
        private WeakReference<WelcomeActivity> mWeakReference;

        private SafeHandler(WelcomeActivity service, Looper looper) {
            super(looper);
            mWeakReference = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            final WelcomeActivity activity = mWeakReference.get();
            if (activity == null) return;
            switch (msg.what) {
                case MSG_UPDATE_TIME:
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Date date = new Date(System.currentTimeMillis());
                            activity.tv_date.setText(TIME_FORMAT.format(date));
                            activity.mSafeHandle.removeMessages(MSG_UPDATE_TIME);
                            activity.mSafeHandle.sendEmptyMessageDelayed(MSG_UPDATE_TIME,   1000);
                        }
                    });
                    break;
                case MSG_CHECK_PLC_STATUES:
                    L.d(TAG, "[MSG_CHECK_PLC_STATUES]");
                    //每隔一分钟检查 投放门的状态
                    if(!test){
                        for(int i=1;i<=4;i++){
//                            ModbusService.getWeight(i);
//                            ModbusService.isFull(i);
                            ModbusService.isOn(i);
                            if(ModbusService.isOn(i)){
                                ModbusService.setOnOff(false,i);
                            }
                        }
                    }

                    activity.mSafeHandle.removeMessages(MSG_CHECK_PLC_STATUES);
                    activity.mSafeHandle.sendEmptyMessageDelayed(MSG_CHECK_PLC_STATUES, 60 * 1000);
                    L.d(TAG, "sendEmptyMessageDelayed :[MSG_CHECK_PLC_STATUES]");
                    break;
                case MSG_OVERTIME_USER_LOGOUT:
                    L.d(TAG, "[MSG_OVERTIME_USER_LOGOUT]");
                    new CountDownTimer(2*60*1000,1000){
                        @Override
                        public void onTick(long millisUntilFinished) {

                        }

                        @Override
                        public void onFinish() {
activity.runOnUiThread(new Runnable() {
    @Override
    public void run() {
        activity.setLogoutStatues();
    }
});
                        }
                    }.start();

                    break;
                case MSG_UPDATE_COUNTDOWN_TIME:
                    Message message = msg;
                    int time = message.arg2;
                     final int numb = message.arg1;
                     CountDownTimer timer =  new CountDownTimer(time*1000, 1000) {
                            public void onTick(final long millisUntilFinished) {
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if(numb == 1){
                                            activity.tv_recy_hint.setVisibility(View.VISIBLE);
                                            activity.tv_recy_hint.setText("开门后"+millisUntilFinished / 1000+"秒内关闭");
                                        }if(numb == 2){
                                            activity.tv_wet_hint.setVisibility(View.VISIBLE);
                                            activity.tv_wet_hint.setText("开门后"+millisUntilFinished / 1000+"秒内关闭");
                                        }if(numb == 3){
                                            activity.tv_harm_hint.setVisibility(View.VISIBLE);
                                            activity.tv_harm_hint.setText("开门后"+millisUntilFinished / 1000+"秒内关闭");
                                        }if(numb == 4){
                                            activity.tv_dry_hint.setVisibility(View.VISIBLE);
                                            activity.tv_dry_hint.setText("开门后"+millisUntilFinished / 1000+"秒内关闭");
                                        }
                                    }
                                });

                            }
                            public void onFinish() {
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {

                                        if(numb == 1){
                                            activity.tv_recy_hint.setVisibility(View.GONE);
                                        }if(numb == 2){
                                            activity.tv_wet_hint.setVisibility(View.GONE);
                                        }if(numb == 3){
                                            activity.tv_harm_hint.setVisibility(View.GONE);
                                        }if(numb == 4){
                                            activity.tv_dry_hint.setVisibility(View.GONE);
                                        }

                                    }
                                });
                            }
                        };
                     timer.start();
                     L.d(TAG, "[MSG_UPDATE_COUNTDOWN_TIME]");
                 break;
            }

        }
    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_dry_garbage:
                requestOpen(4);
                break;
            case R.id.ll_harmful_garbage:
                requestOpen(3);
                break;
            case R.id.ll_recy_garbage:
                requestOpen(1);
                break;
            case R.id.ll_wet_garbage:
                requestOpen(2);
                break;
            case R.id.tv_log_in_out:
                TextView view = (TextView) v;
                if (view.getText().equals("登录")) {
                    LoginActivity.JumpAct(this);
                } else {
                    setLogoutStatues();
                }
                break;
            default:
                break;
        }
    }

    private void requestOpen(int numb) {

        openNumb = numb;
        if (isLogin()) {
            LoginActivity.JumpAct(this);
            return;
        }
        L.d(TAG, "[requestOpen] numb" + numb);
        boolean boo;
        if(test){
            boo = true;
        }else {
            if(ModbusService.isOn(numb))return;
            boo = ModbusService.setOnOff(true,numb);
        }

        if(boo){

            if(!test){
                int time = getTime(ModbusService.getTime(numb));
Message message = Message.obtain();
message.what = SafeHandler.MSG_UPDATE_COUNTDOWN_TIME;
message.arg1 = numb;
message.arg2 = time;
                mSafeHandle.sendMessage(message);
                long weight =ModbusService.getWeight(numb);
                recordOperateRequest(1,numb,weight,1);
            }else {
                int time = 38;
                Message message = Message.obtain();
                message.what = SafeHandler.MSG_UPDATE_COUNTDOWN_TIME;
                message.arg1 = numb;
                message.arg2 = time;
                mSafeHandle.sendMessage(message);
                recordOperateRequest(1,numb,20,1);
            }


            final int openDoorNumb = numb;
            new Thread(){
                @Override
                public void run() {
                    super.run();
                    for (;;){
                        if(!test){
                            try {
                                Thread.sleep(1000);
                                boolean isOn = ModbusService.isOn(openDoorNumb);
                                if(!isOn){
                                    long weight =ModbusService.getWeight(openDoorNumb);
                                    recordOperateRequest(2,openDoorNumb,weight,0);
                                    break;
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                                L.d(TAG, "[ModbusService getWeight Exception] exc:" + e.getMessage());
                            }

                        }else {
                            recordOperateRequest(2,openDoorNumb,24,0);
                            break;
                        }

                    }
                }
            }.start();

        }

    }

    private int getTime(ModbusTime time){
        int startTime = time.getStartHour()*60*60+time.getStartMinute()*60+time.getStartSecond();
        int endTime = time.getEndHour()*60*60+time.getEndMinute()*60+time.getEndSecond();
        int second=endTime-startTime;
        return second;

    }
    private void recordOperateRequest(int requestId,int numb,float weight,int openStatus){
        String url = "/wxApi/operateRecord";
        Map<String, Object> para = new HashMap<>();
        para.put("number", numb);
        para.put("weight", weight);
        para.put("siteCode", 0);
        para.put("openStatus", openStatus);
        HttpClientProxy.getInstance().postJSONAsyn(url, requestId, para, this);
    }

    private void setLogoutStatues() {
        MSPUtils.clear(this);
        loginOrout.setText("登录");
    }

    private void setLoginStatues() {
        loginOrout.setText("退出");

    }

    private boolean isLogin() {
        return TextUtils.isEmpty(MSPUtils.getString("token", ""));
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        L.d(TAG, "[onKeyDown]");
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        L.d(TAG, "[onBackPressed]");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandleThread.quit();
    }

}
