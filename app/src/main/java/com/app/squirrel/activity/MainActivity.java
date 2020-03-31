package com.app.squirrel.activity;

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.app.squirrel.BuildConfig;
import com.app.squirrel.R;
import com.app.squirrel.tool.UserManager;
//import com.priv.arcsoft.ArcSoftFaceActivity;
import com.priv.yswl.base.BaseActivity;
import com.priv.yswl.base.network.CallBack.HttpCallback;
import com.priv.yswl.base.network.HttpClientProxy;
import com.priv.yswl.base.permission.PermissionListener;
import com.priv.yswl.base.permission.PermissionUtil;
import com.priv.yswl.base.tool.GsonUtil;
import com.priv.yswl.base.tool.L;
import com.priv.yswl.base.tool.ToastUtil;
import com.serial.Rs232Callback;
import com.serial.Rs232OutService;
import com.tencent.bugly.crashreport.CrashReport;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.app.squirrel.activity.MainActivity.SafeHandler.MSG_UPDATE_TIME;
import static com.priv.yswl.base.permission.PermissionUtil.READ_WRITE_CAMERA_PERMISSION;

public class MainActivity extends BaseActivity implements View.OnClickListener, HttpCallback<JSONObject> {

    private static final String TAG = MainActivity.class.getSimpleName();
    public static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// HH:mm:ss
    private static final long PLC_STATUES_TIME_DELAY = 3 * 60 * 1000;
    private static final long WARN_TIME_DELAY = 5 * 1000;//五秒
    private static final long PRC_STATUS_TIME_DELAY = 5 * 1000;//五秒
    private static final long UPDATE_TIME_TIME_DELAY = 1000;//更新时间1秒
    private static final long USER_AUTO_LOGOUT_TIME = 2 * 60 * 1000;//更新时间1秒
    private int openNumb = -1;
    public HandlerThread mHandleThread;
    public SafeHandler mSafeHandle;
    public TextView tv_date;
    public LinearLayout loginOrout;
    public TextView tv_harm_hint;
    public TextView tv_wet_hint;
    public TextView tv_recy_hint;
    public TextView tv_dry_hint;
    public ImageView iv_wet_img;
    public ImageView iv_recy_img;
    public ImageView iv_dry_img;
    public ImageView iv_harm_img;
    public Rs232OutService rs232OutService;

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
        mSafeHandle.removeMessages(SafeHandler.MSG_OVERTIME_USER_LOGOUT);
        mSafeHandle.sendEmptyMessage(SafeHandler.MSG_OVERTIME_USER_LOGOUT);

        //开门
        if (openNumb == -1) return;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Message message = Message.obtain();
                message.what = SafeHandler.MSG_OPEN_DOOR;
                message.arg1 = openNumb;
                mSafeHandle.sendMessage(message);
            }
        });


    }

    private void requestPermiss() {
        L.d(TAG, "[requestPermiss]");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            PermissionUtil permissionUtil = new PermissionUtil(this);
            permissionUtil.requestPermissions(READ_WRITE_CAMERA_PERMISSION, new PermissionListener() {
                @Override
                public void onGranted() {
                    L.d(TAG, "[onGranted]");
                }

                @Override
                public void onDenied(List<String> deniedPermission) {

                }

                @Override
                public void onDeniedForever(List<String> deniedPermission) {

                }
            });
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        requestPermiss();
        findViewById(R.id.ll_dry_garbage).setOnClickListener(this);
        findViewById(R.id.ll_harmful_garbage).setOnClickListener(this);
        findViewById(R.id.ll_recy_garbage).setOnClickListener(this);
        findViewById(R.id.ll_wet_garbage).setOnClickListener(this);
        tv_dry_hint = findViewById(R.id.tv_dry_hint);
        tv_harm_hint = findViewById(R.id.tv_harmful_hint);
        tv_recy_hint = findViewById(R.id.tv_recy_hint);
        tv_wet_hint = findViewById(R.id.tv_wet_hint);
        loginOrout = findViewById(R.id.ll_logout);
        iv_dry_img = findViewById(R.id.iv_dry_img);
        iv_harm_img = findViewById(R.id.iv_harm_img);
        iv_recy_img = findViewById(R.id.iv_rec_img);
        iv_wet_img = findViewById(R.id.iv_wet_img);
        loginOrout.setOnClickListener(this);
        loginOrout.setVisibility(View.INVISIBLE);
        tv_date = findViewById(R.id.tv_date);
        mHandleThread = new HandlerThread(getClass().getSimpleName());
        mHandleThread.start();
        mSafeHandle = new SafeHandler(this, mHandleThread.getLooper());

        rs232OutService = new Rs232OutService(new MyCallback());
        //延迟打开串口
        loginOrout.postDelayed(new Runnable() {
            @Override
            public void run() {
                rs232OutService.open();
            }
        }, 1000);
    }

    @Override
    public void onStart() {
        super.onStart();
    }


    @Override
    protected void onResume() {
        super.onResume();
        L.d(TAG, "[onResume]");
        mSafeHandle.sendEmptyMessage(MSG_UPDATE_TIME);
        L.d(TAG, "[sendEmptyMessage MSG_UPDATE_TIME]");
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSafeHandle.removeMessages(SafeHandler.MSG_UPDATE_TIME);
        mSafeHandle.removeMessages(SafeHandler.MSG_OVERTIME_USER_LOGOUT);
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
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        rs232OutService.close();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        rs232OutService.close();
        mHandleThread.quit();
    }

    @Override
    public void onSucceed(int requestId, JSONObject result) {
        L.d(TAG, "[onSucceed]" + result);
    }

    @Override
    public void onFail(int requestId, String errorMsg) {
        L.e(TAG, "[onFail]" + errorMsg);
    }


    @Override
    public void onClick(View v) {
        Message message;
        switch (v.getId()) {
            case R.id.ll_dry_garbage:
                openNumb = 4;
                if (!UserManager.isLogin()) {
                    jumpLogin();
                    return;
                }
                message = Message.obtain();
                message.what = SafeHandler.MSG_OPEN_DOOR;
                message.arg1 = 4;
                mSafeHandle.sendMessage(message);

                break;
            case R.id.ll_harmful_garbage:
                openNumb = 3;
                if (!UserManager.isLogin()) {
                    jumpLogin();
                    return;
                }
                message = Message.obtain();
                message.what = SafeHandler.MSG_OPEN_DOOR;
                message.arg1 = 3;
                mSafeHandle.sendMessage(message);
                break;
            case R.id.ll_recy_garbage:
                openNumb = 1;
                if (!UserManager.isLogin()) {
                    jumpLogin();
                    return;
                }
                message = Message.obtain();
                message.what = SafeHandler.MSG_OPEN_DOOR;
                message.arg1 = 1;
                mSafeHandle.sendMessage(message);
                break;
            case R.id.ll_wet_garbage:
                openNumb = 2;
                if (!UserManager.isLogin()) {
                    jumpLogin();
                    return;
                }
                message = Message.obtain();
                message.what = SafeHandler.MSG_OPEN_DOOR;
                message.arg1 = 2;
                mSafeHandle.sendMessage(message);
                break;
            case R.id.ll_logout:
                setLogoutStatues();
                break;
            default:
                break;
        }
    }

    /**
     * 子线程调用
     */
    private void jumpLogin() {
        LoginActivity.JumpAct(this);

//        ArcSoftFaceActivity.JumpAct(this);
    }

    private boolean[] isOpen = {false, false, false, false};


    //提交记录请求
    private void recordOperateRequest(int requestId, int numb, String weight, int openStatus) {
        String url = "/wxApi/operateRecord";
        Map<String, Object> para = new HashMap<>();
        para.put("number", numb);
        para.put("weight", weight);
        para.put("siteCode", "A0001");
        para.put("openStatus", openStatus);
        String str = GsonUtil.GsonString(para);
        ToastUtil.showToast("请求参数为：" + str);
        HttpClientProxy.getInstance().postJSONAsyn(url, requestId, str, this);
    }

    //TODO 请求报警
    private void requestWarn(int doorNumb) {


    }

    private void requestRecoFullStatus(int doorNumb, boolean isFull) {
        String url = "wxApi/binfs";
        Map<String, Object> para = new HashMap<>();
        para.put("number", doorNumb);
        para.put("isFull", isFull);
        para.put("siteCode", "A0001");
        HttpClientProxy.getInstance().getAsyn(url, 3, para, this);
    }

    private void setLogoutStatues() {
        UserManager.setLoginStatus(false);
        loginOrout.setVisibility(View.INVISIBLE);
    }

    private void setLoginStatues() {
        loginOrout.setVisibility(View.VISIBLE);

    }

    final static class SafeHandler extends Handler {
        public static final int MSG_UPDATE_TIME = 0x0;
        public static final int MSG_OPEN_DOOR = 0x6;
        //        public static final int MSG_AUTO_CLOSE_DOOR = 0x3;
//        public static final int MSG_CHECK_PRC_STATUS = 0x1;
        public static final int MSG_OVERTIME_USER_LOGOUT = 0x2;
        private WeakReference<MainActivity> mWeakReference;

        private SafeHandler(MainActivity service, Looper looper) {
            super(looper);
            mWeakReference = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            final MainActivity activity = mWeakReference.get();
            if (activity == null) return;
            switch (msg.what) {
                //开门
                case MSG_OPEN_DOOR:
                    int doorNumb = msg.arg1;
                    Rs232OutService.openDoor(doorNumb);
                    break;
                //更新时间
                case MSG_UPDATE_TIME:
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Date date = new Date(System.currentTimeMillis());
                            activity.tv_date.setText(TIME_FORMAT.format(date));
                            removeMessages(MSG_UPDATE_TIME);
                            sendEmptyMessageDelayed(MSG_UPDATE_TIME, UPDATE_TIME_TIME_DELAY);
                        }
                    });
                    break;
//
                case MSG_OVERTIME_USER_LOGOUT:
                    L.d(TAG, "[MSG_OVERTIME_USER_LOGOUT] begin");
                    new CountDownTimer(USER_AUTO_LOGOUT_TIME, 1000) {
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
            }

        }
    }

    class MyCallback implements Rs232Callback {
        @Override
        public void onReceiveOpen(int numb) {
            ToastUtil.showToast(numb + "号门已经打开！");
            isOpen[numb - 1] = true;
            if (numb == 1) {
                tv_recy_hint.setVisibility(View.VISIBLE);
                iv_recy_img.setBackgroundResource(R.drawable.bg_dash);
//               tv_recy_hint.setText("开门后" + millisUntilFinished / 1000 + "秒内关闭");
            }
            if (numb == 2) {
                iv_wet_img.setBackgroundResource(R.drawable.bg_dash);
                tv_wet_hint.setVisibility(View.VISIBLE);
            }
            if (numb == 3) {
                iv_harm_img.setBackgroundResource(R.drawable.bg_dash);
                tv_harm_hint.setVisibility(View.VISIBLE);
            }
            if (numb == 4) {
                iv_dry_img.setBackgroundResource(R.drawable.bg_dash);
                tv_dry_hint.setVisibility(View.VISIBLE);
            }

        }

        /**
         * @param weight      重量，有小数点，单位kg
         * @param temperature 温度
         * @param smokeWarn   烟雾报警，01有报警，00表示正常
         * @param fireWarn    灭火器状态，01有报警，00表示正常
         * @param timeSet     时间状态，01有报警，00表示正常
         * @param times       多个时间段，使用;分隔
         */
        @Override
        public void onReceiveBordInfo(int numb, String weight, int temperature, String smokeWarn,
                                      String fireWarn, String timeSet, String times) {
            ToastUtil.showToast(String.format(Locale.CHINA, "%d 号门打开" +
                            "，weight:%s,temperature:%d,smokeWarn:%s,fireWarn:%s,timeSet:%s,times:%s"
                    , numb, weight, temperature, smokeWarn, fireWarn, timeSet, times));

        }

        /**
         * 成功设置 垃圾桶至0，即清空,返回结果到后台
         */
        @Override
        public void onReset(int numb) {
            ToastUtil.showToast(numb + "号垃圾桶重置！");
        }

        /**
         * 成功设置 垃圾桶零点校准，返回结果到后台
         */
        @Override
        public void onReset0(int numb) {
            ToastUtil.showToast(numb + "号垃圾桶0点校准！");
        }

        /**
         * 成功设置 垃圾桶负载校准，返回结果到后台。
         */
        @Override
        public void onResetWeight(int numb) {
            ToastUtil.showToast(numb + "号垃圾桶负载校准！");
        }


        /**
         * 成功设置时间返回标志
         */
        @Override
        public void onSetTime(int numb) {
            ToastUtil.showToast(numb + "号垃圾桶设置时间成功！");
        }

        /**
         * 自动关门后，返回 重量
         * weight 重量
         * timeId 时间
         */
        @Override
        public void onReceiveWeight(int numb, String weight, String timeID) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtil.showToast(numb + "号垃圾桶门关闭，重量：" + weight);
                    isOpen[numb - 1] = false;
                    recordOperateRequest(1, numb, weight, 1);
                    if (numb == 1) {
                        tv_recy_hint.setVisibility(View.INVISIBLE);
                        iv_recy_img.setBackground(null);
                    }
                    if (numb == 2) {
                        tv_wet_hint.setVisibility(View.INVISIBLE);
                        iv_wet_img.setBackground(null);
                    }
                    if (numb == 3) {
                        tv_harm_hint.setVisibility(View.INVISIBLE);
                        iv_harm_img.setBackground(null);
                    }
                    if (numb == 4) {
                        iv_dry_img.setBackground(null);
                        tv_dry_hint.setVisibility(View.INVISIBLE);
                    }
                }
            });

        }

        /**
         * 火灾报警
         */
        @Override
        public void onFireWarn(int numb, String msg) {
            ToastUtil.showToast(numb + "号垃圾桶" + msg);

        }

        /**
         * 烟雾报警
         */
        @Override
        public void onSmokeWarn(int numb, String msg) {
            ToastUtil.showToast(numb + "号垃圾桶" + msg);
        }

        /**
         * 满载报警
         */
        @Override
        public void onFullWarn(int numb, String msg) {
            ToastUtil.showToast(numb + "号垃圾桶" + msg);
            requestRecoFullStatus(numb, true);
        }

        /**
         * 灭火器溶剂不足报警
         */
        @Override
        public void onFireToolsEmptyWarn(int numb, String msg) {
            ToastUtil.showToast(numb + "号垃圾桶" + msg);
        }

        /**
         * 电机故障报警
         */
        @Override
        public void onMachineWarn(int numb, String msg) {
            ToastUtil.showToast(numb + "号垃圾桶" + msg);
        }
    }

}
