package com.app.squirrel.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.app.squirrel.R;
import com.app.squirrel.fragment.LoginByCusrNumbFragment;
import com.app.squirrel.fragment.LoginByWXFragment;
import com.app.squirrel.fragment.LoginByLocalNumbFragment;
import com.arcsoft.face.ActiveFileInfo;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.enums.RuntimeABI;
import com.priv.arcsoft.common.Constants;
import com.priv.arcsoft.util.SoUtil;
import com.priv.yswl.base.BaseActivity;
import com.priv.yswl.base.BaseFragment;
import com.priv.yswl.base.MApplication;
import com.priv.yswl.base.tool.L;
import com.priv.yswl.base.tool.MDeviceUtil;
import com.priv.yswl.base.tool.ToastUtil;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;
import java.util.Date;

public class LoginActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "LoginActivity";

    public static void JumpAct(Activity context) {
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
    }


    final static class SafeHandler extends Handler {
        public static final int MSG_UPDATE_TIME = 0x5;
        public static final int MSG_OVERTIME_FINISH_ACTIVITY = 0x6;
        private WeakReference<LoginActivity> mWeakReference;

        private SafeHandler(LoginActivity loginActivity) {
            super();
            mWeakReference = new WeakReference<>(loginActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            final LoginActivity activity = mWeakReference.get();
            if (activity == null) return;
            switch (msg.what) {
                case MSG_UPDATE_TIME:
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Date date = new Date(System.currentTimeMillis());
                            activity.tv_date.setText(MainActivity.TIME_FORMAT.format(date));
                            activity.mSafeHandle.removeMessages(MSG_UPDATE_TIME);
                            activity.mSafeHandle.sendEmptyMessageDelayed(MSG_UPDATE_TIME, 1000);
                        }
                    });
                    break;
                case MSG_OVERTIME_FINISH_ACTIVITY:
                    activity.mSafeHandle.removeMessages(MSG_OVERTIME_FINISH_ACTIVITY);
                    L.d(TAG, "[MSG_OVERTIME_FINISH_ACTIVITY]");
                    activity.finish();
                    break;
            }

        }
    }

    LinearLayout ll_switch1;
    LinearLayout ll_switch2;
    //    LinearLayout ll_switch3;
    TextView tv_switch1;
    TextView tv_switch2;
    //    TextView tv_switch3;
    TextView tv_date;
    BaseFragment currentFragment;
    SafeHandler mSafeHandle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mSafeHandle = new SafeHandler(this);
        initUI();
        switchScanCodeLogin();
        initArcSoft();
    }

    private void initUI() {
        tv_date = findViewById(R.id.tv_date);
        findViewById(R.id.tv_back).setOnClickListener(this);
        ((TextView)findViewById(R.id.tv_device_id)).setText("设备ID: "+ MDeviceUtil.getMAC(this));

        ll_switch1 = findViewById(R.id.ll_switch1);
        ll_switch2 = findViewById(R.id.ll_switch2);
        tv_switch1 = findViewById(R.id.tv_switch1);
        tv_switch2 = findViewById(R.id.tv_switch2);
//        ll_switch3 = findViewById(R.id.ll_switch3);
//        tv_switch3 = findViewById(R.id.tv_switch3);
//        ll_switch3.setOnClickListener(this);

        ll_switch1.setOnClickListener(this);
        ll_switch2.setOnClickListener(this);
    }

    private void initArcSoft() {
        SoUtil.checkSoFile(this, SoUtil.LIBRARIES);
        RuntimeABI runtimeABI = FaceEngine.getRuntimeABI();
        L.d(TAG, "subscribe: getRuntimeABI() " + runtimeABI);

        int activeCode = FaceEngine.activeOnline(this, Constants.APP_ID, Constants.SDK_KEY);
        if (activeCode == ErrorInfo.MOK || activeCode == ErrorInfo.MERR_ASF_ALREADY_ACTIVATED) {
            L.d(TAG, "激活成功");
        } else {
            //
            ToastUtil.showToast("引擎激活失败" + activeCode);
            L.e(TAG, "引擎激活失败" + activeCode);
        }
        //获取虹软sdk的一些基本信息
        ActiveFileInfo activeFileInfo = new ActiveFileInfo();
        int res = FaceEngine.getActiveFileInfo(this, activeFileInfo);
        if (res == ErrorInfo.MOK) {
            L.d(TAG, activeFileInfo.toString());
        }
    }

    void setScanCodePressedBg() {
        ll_switch1.setBackground(getResources().getDrawable(R.drawable.bg_tab_login));
        tv_switch1.setTextColor(getResources().getColor(R.color.white));
        Drawable drawableLeft = getResources().getDrawable(R.mipmap.ic_sm_press);
        tv_switch1.setCompoundDrawablesWithIntrinsicBounds(drawableLeft, null, null, null);
    }

    void setScanCodeNormarBg() {
        ll_switch1.setBackgroundColor(getResources().getColor(R.color.white));
        tv_switch1.setTextColor(getResources().getColor(R.color.gray2));
        Drawable drawableLeft = getResources().getDrawable(R.mipmap.ic_sm);
        tv_switch1.setCompoundDrawablesWithIntrinsicBounds(drawableLeft, null, null, null);
    }

    /* void setFacePressedBg() {
         ll_switch3.setBackgroundColor(getResources().getColor(R.color.lanse3));
         tv_switch3.setTextColor(getResources().getColor(R.color.white));
         Drawable drawableLeft = getResources().getDrawable(R.mipmap.ic_face);
         tv_switch3.setCompoundDrawablesWithIntrinsicBounds(drawableLeft, null, null, null);
     }

     void setFaceNormaBg() {
         ll_switch3.setBackgroundColor(getResources().getColor(R.color.white));
         tv_switch3.setTextColor(getResources().getColor(R.color.gray2));
         Drawable drawableLeft = getResources().getDrawable(R.mipmap.ic_face);
         tv_switch3.setCompoundDrawablesWithIntrinsicBounds(drawableLeft, null, null, null);
     }
 */
    void setNumbPressedBg() {
        ll_switch2.setBackground(getResources().getDrawable(R.drawable.bg_tab_login));
        tv_switch2.setTextColor(getResources().getColor(R.color.white));
        Drawable drawableLeft = getResources().getDrawable(R.mipmap.ic_phone_press);
        tv_switch2.setCompoundDrawablesWithIntrinsicBounds(drawableLeft, null, null, null);
    }

    void setNumbNormarBg() {
        ll_switch2.setBackgroundColor(getResources().getColor(R.color.white));
        tv_switch2.setTextColor(getResources().getColor(R.color.gray2));
        Drawable drawableLeft = getResources().getDrawable(R.mipmap.ic_phone);
        tv_switch2.setCompoundDrawablesWithIntrinsicBounds(drawableLeft, null, null, null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSafeHandle.sendEmptyMessageDelayed(SafeHandler.MSG_OVERTIME_FINISH_ACTIVITY, 3 * 60 * 1000);
        mSafeHandle.sendEmptyMessage(SafeHandler.MSG_UPDATE_TIME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSafeHandle.removeMessages(SafeHandler.MSG_OVERTIME_FINISH_ACTIVITY);
        mSafeHandle.removeMessages(SafeHandler.MSG_UPDATE_TIME);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public boolean getEventBusSetting() {
        return true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onLoginSuccEventMessage(Message message) {
        L.d(TAG, "[onEventMessage]");
        MApplication.getApplication().getGolbalHander().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 500);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_back:
                finish();
                break;
            case R.id.ll_switch1:
                switchScanCodeLogin();
                break;
            case R.id.ll_switch2:
                switchPhoneLogin();
                break;
            default:
                break;
        }
    }

    LoginByWXFragment loginByWXFragment;
    LoginByCusrNumbFragment login2Fragment;


    private void switchScanCodeLogin() {
        if (loginByWXFragment == null) {
            loginByWXFragment = new LoginByWXFragment();
        }
        if (currentFragment == loginByWXFragment) return;
        setScanCodePressedBg();
        setNumbNormarBg();
//        setFaceNormaBg();
        switchFragment(loginByWXFragment);
    }

    private void switchPhoneLogin() {
        if (login2Fragment == null) {
            login2Fragment = new LoginByCusrNumbFragment();
        }
        if (currentFragment == login2Fragment) return;
        setNumbPressedBg();
//        setFaceNormaBg();
        setScanCodeNormarBg();
        switchFragment(login2Fragment);
    }

    private void switchFragment(BaseFragment targetFragment) {
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction();

        if (currentFragment == null) {
            currentFragment = targetFragment;
            transaction
                    .replace(R.id.content, targetFragment)
                    .commit();
        } else {
            if (currentFragment == targetFragment) return;
            if (!targetFragment.isAdded()) {
                transaction
                        .hide(currentFragment)
                        .add(R.id.content, targetFragment)
                        .commit();
            } else {
                transaction
                        .hide(currentFragment)
                        .show(targetFragment)
                        .commit();
            }
            currentFragment = targetFragment;
        }

    }

}
