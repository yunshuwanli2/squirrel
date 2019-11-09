package com.app.squirrel.activity;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.app.squirrel.R;
import com.app.squirrel.http.CallBack.HttpCallback;
import com.app.squirrel.http.HttpClientProxy;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class WelcomeActivity extends AppCompatActivity implements View.OnClickListener, HttpCallback<JSONObject> {

    public static void JumpAct(Activity context) {
        Intent intent = new Intent(context, WelcomeActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);


        findViewById(R.id.ll_dry_garbage).setOnClickListener(this);
        findViewById(R.id.ll_harmful_garbage).setOnClickListener(this);
        findViewById(R.id.ll_recy_garbage).setOnClickListener(this);
        findViewById(R.id.ll_wet_garbage).setOnClickListener(this);

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
            default:
                break;
        }
    }

    boolean isLogin;

    private void requestOpen(int numb) {
        if (!isLogin) {
            LoginActivity.JumpAct(this);
            return;
        }
        String url = "wxApi/openDoor";
        Map<String, Object> para = new HashMap<>();
        para.put("number", numb);
        HttpClientProxy.getInstance().getAsyn(url, 1, para, this);
    }

    @Override
    public void onSucceed(int requestId, JSONObject result) {

    }

    @Override
    public void onFail(int requestId, String errorMsg) {

    }
}
