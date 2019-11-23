package com.app.squirrel.activity;

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
import com.bumain.plc.ModbusService;

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
    public static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");// HH:mm:ss
    private int openNumb = -1;
    public HandlerThread mHandleThread;
    public SafeHandler mSafeHandle;
    public TextView tv_date;
    public TextView loginOrout;
    @Override
    public boolean getEventBusSetting() {
        return true;
    }


    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onEventMessage(Message message) {
        L.d(TAG, "[onEventMessage]");
        //
        setLoginStatues();
        //登录成功2分钟后自动登出
        mSafeHandle.sendEmptyMessageDelayed(SafeHandler.MSG_OVERTIME_USER_LOGOUT,2*60*1000);

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
        loginOrout = findViewById(R.id.tv_log_in_out);
        loginOrout.setOnClickListener(this);
        tv_date = findViewById(R.id.tv_date);
        mHandleThread = new HandlerThread(getClass().getSimpleName());
        mHandleThread.start();
        mSafeHandle = new SafeHandler(this, mHandleThread.getLooper());
        mSafeHandle.sendEmptyMessageDelayed(MSG_UPDATE_TIME, 1000);
    }

    @Override
    protected void onResume() {
        super.onResume();
        L.d(TAG, "[onResume]");
        mSafeHandle.sendEmptyMessageDelayed(SafeHandler.MSG_CHECK_PLC_STATUES,60*1000);
        L.d(TAG, "[MSG_CHECK_PLC_STATUES]");
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
                            activity.mSafeHandle.sendEmptyMessageDelayed(MSG_UPDATE_TIME, 30 * 1000);
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
                    activity.setLogoutStatues();
                    break;
            }

        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mSafeHandle.removeMessages(SafeHandler.MSG_CHECK_PLC_STATUES);
        mSafeHandle.removeMessages(SafeHandler.MSG_UPDATE_TIME);
        mSafeHandle.removeMessages(SafeHandler.MSG_OVERTIME_USER_LOGOUT);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_dry_garbage:
                openNumb = 4;
                requestOpen(4);
                break;
            case R.id.ll_harmful_garbage:
                openNumb = 3;
                requestOpen(3);
                break;
            case R.id.ll_recy_garbage:
                openNumb = 1;
                requestOpen(1);
                break;
            case R.id.ll_wet_garbage:
                openNumb = 2;
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
        L.d(TAG, "[requestOpen] numb" + numb);
        if (isLogin()) {
            LoginActivity.JumpAct(this);
            return;
        }
        boolean boo;
        if(test){
            boo = true;
        }else {
            boo = ModbusService.setOnOff(true,numb);
        }

        if(boo){
            if(!test){
                long weight =ModbusService.getWeight(numb);
                recordOperateRequest(1,numb,weight,1);
            }else {
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
