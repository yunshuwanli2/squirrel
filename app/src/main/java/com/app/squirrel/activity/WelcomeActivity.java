package com.app.squirrel.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
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

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class WelcomeActivity extends BaseActivity implements View.OnClickListener, HttpCallback<JSONObject> {

    private static final String TAG = "WelcomeActivity";

    public static void JumpAct(Activity context) {
        Intent intent = new Intent(context, WelcomeActivity.class);
        context.startActivity(intent);
        context.finish();
    }

    @Override
    public boolean getEventBusSetting() {
        return false;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMessage(Message message) {


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        findViewById(R.id.ll_dry_garbage).setOnClickListener(this);
        findViewById(R.id.ll_harmful_garbage).setOnClickListener(this);
        findViewById(R.id.ll_recy_garbage).setOnClickListener(this);
        findViewById(R.id.ll_wet_garbage).setOnClickListener(this);
        findViewById(R.id.tv_logout).setOnClickListener(this);


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
            case R.id.tv_logout:
                //TODO 退出
                MSPUtils.clear(this);
                break;
            default:
                break;
        }
    }

    private void requestOpen(int numb) {
        if (TextUtils.isEmpty(MSPUtils.getString("token", ""))) {
            LoginActivity.JumpAct(this);
            return;
        }
        String url = "wxApi/openDoor";
        Map<String, Object> para = new HashMap<>();
        para.put("number", numb);
        HttpClientProxy.getInstance().getAsyn(url, 1, para, this);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public void onSucceed(int requestId, JSONObject result) {
        L.e(TAG, "[onSucceed] result:" + result);

    }

    @Override
    public void onFail(int requestId, String errorMsg) {
        L.e(TAG, "[onFail]" + errorMsg);
    }
}
