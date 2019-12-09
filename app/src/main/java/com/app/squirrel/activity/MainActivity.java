package com.app.squirrel.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.app.squirrel.BuildConfig;
import com.app.squirrel.R;
import com.app.squirrel.application.MApplication;
import com.app.squirrel.application.SquirrelApplication;
import com.app.squirrel.facedetect.FaceDetectActivity;
import com.app.squirrel.facedetect.entry.FaceppBean;
import com.app.squirrel.facedetect.util.Utils;
import com.app.squirrel.http.CallBack.HttpCallback;
import com.app.squirrel.http.HttpClientProxy;
import com.app.squirrel.http.okhttp.MSPUtils;
import com.app.squirrel.tool.GsonUtil;
import com.app.squirrel.tool.L;
import com.app.squirrel.tool.ToastUtil;
import com.app.squirrel.tool.UserManager;
import com.bumain.plc.ModbusService;
import com.bumain.plc.ModbusTime;
import com.megvii.livenesslib.LivenessActivity;
import com.serotonin.modbus4j.exception.ErrorResponseException;
import com.serotonin.modbus4j.exception.ModbusInitException;
import com.serotonin.modbus4j.exception.ModbusTransportException;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import permission.PermissionListener;
import permission.PermissionUtil;

import static com.app.squirrel.activity.MainActivity.SafeHandler.MSG_UPDATE_TIME;
import static com.app.squirrel.application.SquirrelApplication.test;
import static permission.PermissionUtil.READ_WRITE_CAMERA_PERMISSION;

public class MainActivity extends BaseActivity implements View.OnClickListener, HttpCallback<JSONObject> {

    private static final String TAG = "MainActivity";
    public static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// HH:mm:ss
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
//        openDoor(openNumb);
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        findViewById(R.id.ll_dry_garbage).setOnClickListener(this);
        findViewById(R.id.ll_harmful_garbage).setOnClickListener(this);
        findViewById(R.id.ll_recy_garbage).setOnClickListener(this);
        findViewById(R.id.ll_wet_garbage).setOnClickListener(this);
        findViewById(R.id.wx_code).setOnClickListener(this);
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
        requestPermiss();
    }

    private void requestPermiss() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            PermissionUtil permissionUtil = new PermissionUtil(this);
            permissionUtil.requestPermissions(READ_WRITE_CAMERA_PERMISSION, new PermissionListener() {
                @Override
                public void onGranted() {

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
    protected void onResume() {
        super.onResume();
        L.d(TAG, "[onResume]");
        mSafeHandle.sendEmptyMessageDelayed(SafeHandler.MSG_CHECK_PLC_STATUES, 60 * 1000);
        L.d(TAG, "[sendEmptyMessage MSG_CHECK_PLC_STATUES]");
        mSafeHandle.sendEmptyMessage(MSG_UPDATE_TIME);
        L.d(TAG, "[sendEmptyMessage MSG_UPDATE_TIME]");
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
        L.d(TAG, "[onSucceed]" + result);
    }

    @Override
    public void onFail(int requestId, String errorMsg) {
        L.e(TAG, "[onFail]" + errorMsg);
    }


    final static class SafeHandler extends Handler {
        public static final int MSG_UPDATE_TIME = 0x0;
        public static final int MSG_OPEN_DOOR = 0x6;
        public static final int MSG_UPDATE_COUNTDOWN_TIME = 0x3;
        public static final int MSG_CHECK_PLC_STATUES = 0x1;
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
                case MSG_OPEN_DOOR:
                    int doorNumb = msg.arg1;
                    activity.openDoor(doorNumb);
                    break;
                case MSG_UPDATE_TIME:
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Date date = new Date(System.currentTimeMillis());
                            activity.tv_date.setText(TIME_FORMAT.format(date));
                            activity.mSafeHandle.removeMessages(MSG_UPDATE_TIME);
                            activity.mSafeHandle.sendEmptyMessageDelayed(MSG_UPDATE_TIME, 1000);
                        }
                    });
                    break;
                case MSG_CHECK_PLC_STATUES:
                    L.d(TAG, "[MSG_CHECK_PLC_STATUES]");
                    //每隔一分钟检查 投放门的状态
                    if (!test) {
                        for (int i = 1; i <= 4; i++) {
//                            ModbusService.getWeight(i);
//                            ModbusService.isFull(i);
                            try {
                                if (ModbusService.isOn(i)) {
                                    ModbusService.setOnOff(false, i);
                                    activity.isOpen[i - 1] = false;
                                }
                            } catch (ModbusTransportException | ModbusInitException | ErrorResponseException e) {
                                e.printStackTrace();
                                ToastUtil.showToast(e.getMessage());
                            }

                        }
                    } else {
                        for (int i = 1; i <= 4; i++) {
                            activity.isOpen[i - 1] = false;
                        }
                    }

                    activity.mSafeHandle.removeMessages(MSG_CHECK_PLC_STATUES);
                    activity.mSafeHandle.sendEmptyMessageDelayed(MSG_CHECK_PLC_STATUES, 60 * 1000);
                    L.d(TAG, "sendEmptyMessageDelayed :[MSG_CHECK_PLC_STATUES]");
                    break;
                case MSG_OVERTIME_USER_LOGOUT:
                    L.d(TAG, "[MSG_OVERTIME_USER_LOGOUT] begin");
                    new CountDownTimer(2 * 60 * 1000, 1000) {
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
                    CountDownTimer timer = new CountDownTimer(time * 1000, 1000) {
                        public void onTick(final long millisUntilFinished) {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (numb == 1) {
                                        activity.tv_recy_hint.setVisibility(View.VISIBLE);
                                        activity.iv_recy_img.setBackgroundResource(R.drawable.bg_dash);
                                        activity.tv_recy_hint.setText("开门后" + millisUntilFinished / 1000 + "秒内关闭");
                                    }
                                    if (numb == 2) {
                                        activity.iv_wet_img.setBackgroundResource(R.drawable.bg_dash);
                                        activity.tv_wet_hint.setVisibility(View.VISIBLE);
                                        activity.tv_wet_hint.setText("开门后" + millisUntilFinished / 1000 + "秒内关闭");
                                    }
                                    if (numb == 3) {
                                        activity.iv_harm_img.setBackgroundResource(R.drawable.bg_dash);
                                        activity.tv_harm_hint.setVisibility(View.VISIBLE);
                                        activity.tv_harm_hint.setText("开门后" + millisUntilFinished / 1000 + "秒内关闭");
                                    }
                                    if (numb == 4) {
                                        activity.iv_dry_img.setBackgroundResource(R.drawable.bg_dash);
                                        activity.tv_dry_hint.setVisibility(View.VISIBLE);
                                        activity.tv_dry_hint.setText("开门后" + millisUntilFinished / 1000 + "秒内关闭");
                                    }
                                }
                            });

                        }

                        public void onFinish() {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    if (numb == 1) {
                                        activity.tv_recy_hint.setVisibility(View.INVISIBLE);
                                        activity.iv_recy_img.setBackground(null);
                                    }
                                    if (numb == 2) {
                                        activity.tv_wet_hint.setVisibility(View.INVISIBLE);
                                        activity.iv_wet_img.setBackground(null);
                                    }
                                    if (numb == 3) {
                                        activity.tv_harm_hint.setVisibility(View.INVISIBLE);
                                        activity.iv_harm_img.setBackground(null);
                                    }
                                    if (numb == 4) {
                                        activity.iv_dry_img.setBackground(null);
                                        activity.tv_dry_hint.setVisibility(View.INVISIBLE);
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
            case R.id.wx_code:
                if (SquirrelApplication.getApplication().getDebugSetting()) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            ModbusService.setOnOff(true, 1);
                        }
                    }).start();
                }
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
//        LoginActivity.JumpAct(this);
        LivenessActivity.JumpActForResult(this);
    }

    boolean[] isOpen = {false, false, false, false};

    private void openDoor(int numb) {
        L.d(TAG, "[openDoor] numb" + numb);
        if (test) {
            if (isOpen[numb - 1]) return;
            isOpen[numb - 1] = true;
        } else {
            try {
                if (ModbusService.isOn(numb)) return;
                isOpen[numb - 1] = ModbusService.setOnOff(true, numb);
            } catch (ModbusTransportException | ModbusInitException | ErrorResponseException e) {
                e.printStackTrace();
                ToastUtil.showToast(e.getMessage());
            }

        }

        if (isOpen[numb - 1]) {
            L.e(TAG, "垃圾箱已打开，请尽快投递！");
            ToastUtil.showToast("垃圾箱已打开，请尽快投递！");
            if (!test) {
                int time = 0;
                long weight = 0;
                try {
                    time = getTime(ModbusService.getTime(numb));
                    Message message = Message.obtain();
                    message.what = SafeHandler.MSG_UPDATE_COUNTDOWN_TIME;
                    message.arg1 = numb;
                    message.arg2 = time;
                    mSafeHandle.sendMessage(message);
                    weight = ModbusService.getWeight(numb);
                    //提交服务器记录
                    recordOperateRequest(1, numb, weight, 1);
                } catch (ModbusTransportException | ErrorResponseException | ModbusInitException e) {
                    e.printStackTrace();
                    ToastUtil.showToast(e.getMessage());
                }


            } else {
                int time = 38;
                Message message = Message.obtain();
                message.what = SafeHandler.MSG_UPDATE_COUNTDOWN_TIME;
                message.arg1 = numb;
                message.arg2 = time;
                mSafeHandle.sendMessage(message);
                //提交服务器记录
                recordOperateRequest(1, numb, 20, 1);
            }


            final int openDoorNumb = numb;
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    for (; ; ) {
                        if (!test) {
                            try {
                                Thread.sleep(1000);
                                boolean isOn = ModbusService.isOn(openDoorNumb);
                                if (!isOn) {
                                    long weight = ModbusService.getWeight(openDoorNumb);
                                    recordOperateRequest(2, openDoorNumb, weight, 0);
                                    break;
                                }

                            } catch (ModbusTransportException e) {
                                e.printStackTrace();
                                L.e(TAG, "ModbusTransportException " + e.getMessage());
                            } catch (ModbusInitException e) {
                                e.printStackTrace();
                            } catch (ErrorResponseException e) {
                                e.printStackTrace();
                                L.e(TAG, "ErrorResponseException " + e.getMessage());
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                                L.d(TAG, "[ModbusService getWeight Exception] exc:" + e.getMessage());
                            }

                        } else {
                            recordOperateRequest(2, openDoorNumb, 24, 0);
                            break;
                        }

                    }
                }
            }.start();

        }

    }

    private int getTime(ModbusTime time) {
        int startTime = time.getStartHour() * 60 * 60 + time.getStartMinute() * 60 + time.getStartSecond();
        int endTime = time.getEndHour() * 60 * 60 + time.getEndMinute() * 60 + time.getEndSecond();
        int second = endTime - startTime;
        return second;

    }

    //提交记录请求
    private void recordOperateRequest(int requestId, int numb, float weight, int openStatus) {
        String url = "/wxApi/operateRecord";
        Map<String, Object> para = new HashMap<>();
        para.put("number", numb);
        para.put("weight", weight);
        para.put("siteCode", 0);
        para.put("openStatus", openStatus);
        String str = GsonUtil.GsonString(para);
        HttpClientProxy.getInstance().postJSONAsyn(url, requestId, str, this);
    }

    private void setLogoutStatues() {
        UserManager.setLoginStatus(false);
        loginOrout.setVisibility(View.INVISIBLE);
    }

    private void setLoginStatues() {
        loginOrout.setVisibility(View.VISIBLE);

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2 && data != null) {
            RequestQueue mQueue = Volley.newRequestQueue(MApplication.getApplication());
            final String imgPath = data.getStringExtra("FACE_PRE_IMG");
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2;
            Bitmap photo = BitmapFactory.decodeFile(imgPath, options);
            int bitmapDegree = Utils.getBitmapDegree(imgPath);
            if (bitmapDegree != 0) {
                photo = Utils.rotateBitmapByDegree(photo, bitmapDegree);
            }
            String base64 = Utils.base64(photo);
            String url = "https://api-cn.faceplusplus.com/facepp/v3/detect";
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    L.e(TAG, "face detect request  onResponse:" + response);
                    FaceppBean faceppBean = FaceppBean.jsonToBean(response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    L.e(TAG, "face detect request onErrorResponse:" + new String(error.networkResponse.data));
                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> map = new HashMap<String, String>();
                    map.put("api_key", BuildConfig.API_KEY);
                    map.put("api_secret", BuildConfig.API_SECRET);
                    map.put("image_base64", base64);
                    map.put("return_attributes", "gender,age,facequality");
                    L.d(TAG, "getParams:" + map.toString());
                    return map;
                }
            };
            mQueue.add(stringRequest);
        }
    }
}
