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

import com.app.squirrel.R;
import com.app.squirrel.serial.Rs232Callback;
import com.app.squirrel.serial.Rs232OutService;
import com.app.squirrel.tool.UserManager;
import com.hjq.permissions.OnPermission;
import com.hjq.permissions.XXPermissions;
import com.priv.arcsoft.ArcSoftFaceActivity;
import com.priv.yswl.base.BaseActivity;
import com.priv.yswl.base.network.CallBack.HttpCallback;
import com.priv.yswl.base.network.HttpClientProxy;
import com.priv.yswl.base.permission.PermissionUtil;
import com.priv.yswl.base.tool.GsonUtil;
import com.priv.yswl.base.tool.L;
import com.priv.yswl.base.tool.MDeviceUtil;
import com.priv.yswl.base.tool.ToastUtil;

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


public class MainActivity extends BaseActivity implements View.OnClickListener, HttpCallback<JSONObject> {

    private static final String TAG = MainActivity.class.getSimpleName();
    public static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// HH:mm:ss
    private static final long PLC_STATUES_TIME_DELAY = 3 * 60 * 1000;
    private static final long WARN_TIME_DELAY = 5 * 1000;//五秒
    private static final long PRC_STATUS_TIME_DELAY = 5 * 1000;//五秒
    private static final long TIME_GET_BORD_INFO = 5 * 60 * 1000;//更新时间5分钟
    private static final long UPDATE_TIME_TIME_DELAY = 1000;//更新时间1秒
    private static final long USER_AUTO_LOGOUT_TIME = 2 * 60 * 1000;//更新时间1秒
    private static final int ID_REQUEST_RECORD_OPERATE = 0x11;
    private static final int ID_REQUEST_RECORD_FULL_STATUS = 0x12;
    private static final int ID_REQUEST_WARN = 0x13;
    private static final int ID_REQUEST_UPDATE_BORD_INFO = 0x14;
    private int openNumb = -1;
    public HandlerThread mHandleThread;
    public SafeHandler mSafeHandle;
    public TextView tv_date,tv_deviceId;
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
        EventBus.getDefault().removeStickyEvent(message);

        int isFace = message.arg1;
        String token = (String) message.obj;
        L.d(TAG,"token:"+token+ " isFace:" + isFace );

        UserManager.loginLocal(token, isFace);

        setLoginStatues();

        ToastUtil.showToast("登录成功");
        //登录成功2分钟后自动登出
        mSafeHandle.removeMessages(SafeHandler.MSG_OVERTIME_USER_LOGOUT);
        mSafeHandle.sendEmptyMessage(SafeHandler.MSG_OVERTIME_USER_LOGOUT);

        if (isFace == 0) {
            loginOrout.postDelayed(new Runnable() {
                @Override
                public void run() {
                    ArcSoftFaceActivity.JumpAct(MainActivity.this, UserManager.isLogin(), UserManager.isFace());
                }
            }, 1000);
        }

    }

    private void requestPermiss() {
        L.d(TAG, "[requestPermiss]");
        XXPermissions.with(this)
                // 可设置被拒绝后继续申请，直到用户授权或者永久拒绝
                .constantRequest()
                // 支持请求6.0悬浮窗权限8.0请求安装权限
                //.permission(Permission.SYSTEM_ALERT_WINDOW, Permission.REQUEST_INSTALL_PACKAGES)
                // 不指定权限则自动获取清单中的危险权限
                .permission(PermissionUtil.READ_WRITE_CAMERA_PERMISSION)
                .request(new OnPermission() {

                    @Override
                    public void hasPermission(List<String> granted, boolean all) {

                    }

                    @Override
                    public void noPermission(List<String> denied, boolean quick) {

                    }
                });
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
        loginOrout = findViewById(R.id.ll_logout);
        iv_dry_img = findViewById(R.id.iv_dry_img);
        iv_harm_img = findViewById(R.id.iv_harm_img);
        iv_recy_img = findViewById(R.id.iv_rec_img);
        iv_wet_img = findViewById(R.id.iv_wet_img);
        loginOrout.setOnClickListener(this);
        loginOrout.setVisibility(View.INVISIBLE);
        tv_date = findViewById(R.id.tv_date);
        tv_deviceId = findViewById(R.id.tv_device_id);
        tv_deviceId.setText("设备ID: "+MDeviceUtil.getMAC(this));
        mHandleThread = new HandlerThread(TAG);
        mHandleThread.start();
        mSafeHandle = new SafeHandler(this, mHandleThread.getLooper());

        rs232OutService = new Rs232OutService(new MyCallback());
        requestPermiss();
    }

    @Override
    public void onStart() {
        super.onStart();
//        rs232OutService.start();
    }


    @Override
    protected void onResume() {
        super.onResume();
        mSafeHandle.sendEmptyMessage(MSG_UPDATE_TIME);
        L.d(TAG, "[onResume] sendEmptyMessage :MSG_UPDATE_TIME");
    }

    @Override
    protected void onPause() {
        super.onPause();
        L.d(TAG, "[onResume]");
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
        rs232OutService.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        rs232OutService.stop();
        mHandleThread.quit();
    }

    @Override
    public void onSucceed(int requestId, JSONObject result) {
        L.d(TAG, "requestId"+requestId+ " [onSucceed]" + result);
        if(requestId==100){
            L.d(TAG,"关门后记录成功");
        }
    }

    @Override
    public void onFail(int requestId, String errorMsg) {
        L.e(TAG, "requestId:"+requestId+" [onFail]" + errorMsg);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_dry_garbage:
                openNumb = 4;
                if (!UserManager.isLogin()) {
                    jumpLogin();
                    return;
                }
                openDoor(openNumb);
                break;
            case R.id.ll_harmful_garbage:
                openNumb = 3;
                if (!UserManager.isLogin()) {
                    jumpLogin();
                    return;
                }
                openDoor(openNumb);
                break;
            case R.id.ll_recy_garbage:
                openNumb = 1;
                if (!UserManager.isLogin()) {
                    jumpLogin();
                    return;
                }
                openDoor(openNumb);
                break;
            case R.id.ll_wet_garbage:
                openNumb = 2;
                if (!UserManager.isLogin()) {
                    jumpLogin();
                    return;
                }
                openDoor(openNumb);
                break;
            case R.id.ll_logout:
                setLogoutStatues();
                break;
            default:
                break;
        }
    }

    private void openDoor(int doorNumb) {
        L.d(TAG, "open :" + doorNumb);
//        rs232OutService.start();
//        rs232OutService.openDoor(doorNumb);
        recordOperateRequest(doorNumb,0,1);//单纯开门记录


//        TODO TEST TODAY 10.18
        if(doorNumb==1){
            recordOperateRequest(1,22,0);
            return;
        }
        if(doorNumb ==2){
            requestRecordFullStatus(2,true);
            return;
        }
        if(doorNumb == 3){
            requestWarn(3,2,"垃圾桶内有烟雾");
        }

    }

    /**
     * 子线程调用
     */
    private void jumpLogin() {
        LoginActivity.JumpAct(this);
//        ArcSoftFaceActivity.JumpAct(this);
    }

    //提交记录请求

    /**
     *
     * @param numb
     * @param weight int 单位g
     * @param openStatus
     */
    private void recordOperateRequest(int numb, int weight, int openStatus) {
        String url = "/wxApi/operateRecord";
        Map<String, Object> para = new HashMap<>();
        para.put("number", numb);
        para.put("weight", weight);
        para.put("siteCode", "A0001");
        para.put("openStatus", openStatus);
        String str = GsonUtil.GsonString(para);
        ToastUtil.showToast("请求参数为：" + str);
        HttpClientProxy.getInstance().postJSONAsyn(url, ID_REQUEST_RECORD_OPERATE, str, this);
    }



    private void requestRecordFullStatus(int doorNumb, boolean isFull) {
        String url = "wxApi/binfs";
        Map<String, Object> para = new HashMap<>();
        para.put("number", doorNumb);
        para.put("isFull", isFull);
        para.put("siteCode", "A0001");
        HttpClientProxy.getInstance().getAsyn(url, ID_REQUEST_RECORD_FULL_STATUS, para, this);
    }

    /**
     *
     * @param doorNumb
     * @param type
     * 1 温度过高
     * 2 烟雾警报
     * 3 满载警报
     * 4 灭火容器不足
     * 5 电机故障
     * @param remark
     * 报警提示信息。当type为1时，该字段为温度
     */
    private void requestWarn(int doorNumb, int type,String remark) {
        String url = "wxApi/receiveWarn";
        Map<String, Object> para = new HashMap<>();
        para.put("number", doorNumb);
        para.put("type", type);
        para.put("remark", "A0001");
        HttpClientProxy.getInstance().getAsyn(url, ID_REQUEST_WARN, para, this);
    }

    /**
     *
     * @param doorNumb
     * @param weight
     * @param temperature
     * @param smokeWarn
     * @param fireWarn
     * @param timeSet
     * @param times
     */
    private void requestUpdateBordInfo(int doorNumb,String weight,
                                       int temperature,String smokeWarn,
                                       String fireWarn,String timeSet,String times
    ) {
        String url = "wxApi/receiveBordInfo";
        Map<String, Object> para = new HashMap<>();
        para.put("number", doorNumb);
        para.put("weight", weight);
        para.put("temperature", temperature);
        para.put("smokeWarn", smokeWarn);
        para.put("fireWarn", fireWarn);
        para.put("timeSet", timeSet);
        para.put("times", times);
        HttpClientProxy.getInstance().getAsyn(url, ID_REQUEST_UPDATE_BORD_INFO, para, this);
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
        public static final int MSG_OVERTIME_USER_LOGOUT = 0x2;
        public static final int MSG_GET_BORD_INTO = 0x6;
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
                case MSG_GET_BORD_INTO:
                    postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Rs232OutService.getBoardInfo(1);
                            Rs232OutService.getBoardInfo(2);
                            Rs232OutService.getBoardInfo(3);
                            Rs232OutService.getBoardInfo(4);
                        }
                    },100);

                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            removeMessages(MSG_GET_BORD_INTO);
                            sendEmptyMessageDelayed(MSG_GET_BORD_INTO, TIME_GET_BORD_INFO);
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
            L.d(TAG,String.format(Locale.CHINA, "%d 号门打开" +
                            "，weight:%s,temperature:%d,smokeWarn:%s,fireWarn:%s,timeSet:%s,times:%s"
                    , numb, weight, temperature, smokeWarn, fireWarn, timeSet, times));
            requestUpdateBordInfo(numb, weight, temperature, smokeWarn, fireWarn, timeSet, times);

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
        public void onReceiveWeight(int numb, int weight, String timeID) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtil.showToast(numb + "号垃圾桶门关闭，重量：" + weight);
                    recordOperateRequest( numb, weight, 0);
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
            requestWarn(numb,1,msg);
        }

        /**
         * 烟雾报警
         */
        @Override
        public void onSmokeWarn(int numb, String msg) {
            ToastUtil.showToast(numb + "号垃圾桶" + msg);
            requestWarn(numb,2,msg);
        }

        /**
         * 满载报警
         */
        @Override
        public void onFullWarn(int numb, String msg) {
            ToastUtil.showToast(numb + "号垃圾桶" + msg);
            requestRecordFullStatus(numb, true);
            requestWarn(numb,3,msg);
        }

        /**
         * 灭火器溶剂不足报警
         */
        @Override
        public void onFireToolsEmptyWarn(int numb, String msg) {
            ToastUtil.showToast(numb + "号垃圾桶" + msg);
            requestWarn(numb,4,msg);
        }

        /**
         * 电机故障报警
         */
        @Override
        public void onMachineWarn(int numb, String msg) {
            ToastUtil.showToast(numb + "号垃圾桶" + msg);
            requestWarn(numb,5,msg);
        }
    }


}
