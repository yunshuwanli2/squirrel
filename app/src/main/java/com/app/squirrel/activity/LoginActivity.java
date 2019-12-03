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
import com.app.squirrel.application.MApplication;
import com.app.squirrel.facedetect.FaceDetectFragment;
import com.app.squirrel.fragment.BaseFragment;
import com.app.squirrel.fragment.Login1Fragment;
import com.app.squirrel.fragment.Login3Fragment;
import com.app.squirrel.tool.L;

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
    LinearLayout ll_switch3;
    TextView tv_switch1;
    TextView tv_switch2;
    TextView tv_switch3;
    TextView tv_date;
    BaseFragment currentFragment;
    SafeHandler mSafeHandle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        tv_date = findViewById(R.id.tv_date);
        findViewById(R.id.tv_back).setOnClickListener(this);

        ll_switch1 = findViewById(R.id.ll_switch1);
        ll_switch2 = findViewById(R.id.ll_switch2);
        ll_switch3 = findViewById(R.id.ll_switch3);
        tv_switch1 = findViewById(R.id.tv_switch1);
        tv_switch2 = findViewById(R.id.tv_switch2);
        tv_switch3 = findViewById(R.id.tv_switch3);

        ll_switch1.setOnClickListener(this);
        ll_switch2.setOnClickListener(this);
        ll_switch3.setOnClickListener(this);
        mSafeHandle = new SafeHandler(this);

        switchScanCodeLogin();
    }

    void setScanCodePressedBg() {
        ll_switch1.setBackgroundColor(getResources().getColor(R.color.lanse3));
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

    void setFacePressedBg() {
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

    void setNumbPressedBg() {
        ll_switch2.setBackgroundColor(getResources().getColor(R.color.lanse3));
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
        }, 1000);

    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_back) {
            finish();
        }
        switch (v.getId()) {
            case R.id.tv_back:
                break;
            case R.id.ll_switch1:
                switchScanCodeLogin();
                break;
            case R.id.ll_switch2:
                switchPhoneLogin();
                break;
            case R.id.ll_switch3:
                switchFaceDetect();
                break;
            default:
                break;
        }
    }

    Login1Fragment login1Fragment;
    Login3Fragment login2Fragment;
    FaceDetectFragment faceDetectFragment;

    private void switchScanCodeLogin() {
        if (login1Fragment == null) {
            login1Fragment = new Login1Fragment();
        }
        if (currentFragment == login1Fragment) return;
        setScanCodePressedBg();
        setNumbNormarBg();
        setFaceNormaBg();
        switchFragment(login1Fragment);
    }

    private void switchPhoneLogin() {
        if (login2Fragment == null) {
            login2Fragment = new Login3Fragment();
        }
        if (currentFragment == login2Fragment) return;
        setNumbPressedBg();
        setFaceNormaBg();
        setScanCodeNormarBg();
        switchFragment(login2Fragment);
    }

    private void switchFaceDetect() {
        if (faceDetectFragment == null) {
            faceDetectFragment = new FaceDetectFragment();
        }
        if (currentFragment == faceDetectFragment) return;
        setFacePressedBg();
        setNumbNormarBg();
        setScanCodeNormarBg();

        switchFragment(faceDetectFragment);
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
