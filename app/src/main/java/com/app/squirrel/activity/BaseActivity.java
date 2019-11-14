package com.app.squirrel.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.app.squirrel.tool.ActivityManager;

import org.greenrobot.eventbus.EventBus;

public class BaseActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityManager.addActivity(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityManager.removeActivity(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }


    @Override
    public void onStart() {
        super.onStart();
        if (getEventBusSetting()) {
            if (!EventBus.getDefault().isRegistered(this)) {
                EventBus.getDefault().register(this);
            }
        }

    }

    @Override
    public void onStop() {
        if (getEventBusSetting()) {
            if (EventBus.getDefault().isRegistered(this))
                EventBus.getDefault().unregister(this);
        }
        super.onStop();
    }

    public boolean getEventBusSetting() {
        return false;
    }

}
