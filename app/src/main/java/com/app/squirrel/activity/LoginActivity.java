package com.app.squirrel.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.app.squirrel.R;
import com.app.squirrel.application.MApplication;
import com.app.squirrel.fragment.BaseFragment;
import com.app.squirrel.fragment.Login1Fragment;
import com.app.squirrel.fragment.Login2Fragment;
import com.app.squirrel.tool.L;
import com.app.squirrel.tool.ToastUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.Date;

import cn.jpush.android.api.NotificationMessage;
import cn.jpush.android.service.JPushMessageReceiver;

public class LoginActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "LoginActivity";

    public static void JumpAct(Activity context) {
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
    }

    LinearLayout ll_switch1;
    LinearLayout ll_switch2;
    TextView tv_switch1;
    TextView tv_switch2;
    BaseFragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        findViewById(R.id.tv_back).setOnClickListener(this);

        ll_switch1 = findViewById(R.id.ll_switch1);
        ll_switch2 = findViewById(R.id.ll_switch2);
        tv_switch1 = findViewById(R.id.tv_switch1);
        tv_switch2 = findViewById(R.id.tv_switch2);

        ll_switch1.setOnClickListener(this);
        ll_switch2.setOnClickListener(this);

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
        TextView tv_date = findViewById(R.id.tv_date);
        Date date = new Date(System.currentTimeMillis());
        tv_date.setText(WelcomeActivity.TIME_FORMAT.format(date));
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public boolean getEventBusSetting() {
        return true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN,sticky=true)
    public void onLoginSuccEventMessage(Message message) {
        L.d(TAG, "[onEventMessage]");
        ToastUtil.showToast("登录成功");
        MApplication.getApplication().getGolbalHander().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 2000);

        EventBus.getDefault().postSticky(message);
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
            default:
                break;
        }
    }

    Login1Fragment login1Fragment;
    Login2Fragment login2Fragment;

    private void switchScanCodeLogin() {
        setScanCodePressedBg();
        setNumbNormarBg();
        if (login1Fragment == null) {
            login1Fragment = new Login1Fragment();
        }
        switchFragment(login1Fragment);
    }

    private void switchPhoneLogin() {
        setNumbPressedBg();
        setScanCodeNormarBg();
        if (login2Fragment == null) {
            login2Fragment = new Login2Fragment();
        }
        switchFragment(login2Fragment);
    }


    private void switchFragment(BaseFragment targetFragment) {
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction();

        if (currentFragment == null) {
            currentFragment = targetFragment;
            transaction
                    .replace(R.id.content, targetFragment)
                    .addToBackStack(null)
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
