package com.app.squirrel.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.app.squirrel.R;
import com.app.squirrel.application.MApplication;
import com.app.squirrel.http.okhttp.MSPUtils;
import com.app.squirrel.jpush.SqRecive;
import com.app.squirrel.tool.L;
import com.app.squirrel.tool.ToastUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

import cn.jpush.android.api.NotificationMessage;
import cn.jpush.android.service.JPushMessageReceiver;

public class LoginActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "LoginActivity";

    public class LoginRecive extends JPushMessageReceiver {
        private static final String TAG = "LoginRecive";

        public LoginRecive() {
        }

        @Override
        public void onNotifyMessageArrived(Context var1, NotificationMessage var2) {
            super.onNotifyMessageArrived(var1, var2);
            L.d(TAG, "[onNotifyMessageArrived]" + var2.toString());

        }
    }

    public boolean loginActUIisAvailable;

    public static void JumpAct(Activity context) {
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        findViewById(R.id.btn_switch).setOnClickListener(this);
        findViewById(R.id.tv_back).setOnClickListener(this);


    }

    @Override
    protected void onResume() {
        super.onResume();

        TextView tv_date = findViewById(R.id.tv_date);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");// HH:mm:ss
        Date date = new Date(System.currentTimeMillis());
        tv_date.setText(simpleDateFormat.format(date));
    }

    @Override
    public void onStart() {
        super.onStart();
        loginActUIisAvailable = true;
    }

    @Override
    public boolean getEventBusSetting() {
        return true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMessage(Message message) {
        L.d(TAG, "[onEventMessage]");
        EventBus.getDefault().removeStickyEvent(message);

        ToastUtil.showToast("登录成功");
        MApplication.getApplication().getGolbalHander().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 2000);

    }

    @Override
    public void onStop() {
        super.onStop();
        loginActUIisAvailable = false;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_back) {
            finish();
        }
        if (v.getId() == R.id.btn_switch) {
            Login4NumbActivity.JumpAct(this);
            finish();
        }
    }
}
