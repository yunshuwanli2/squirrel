package com.app.squirrel.application;

import android.app.Application;

import cat.ereza.customactivityoncrash.CustomActivityOnCrash;

/**
 * Created by admin on 2019/10/24.
 */

public class SquirrelApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CustomActivityOnCrash.install(this);//自定义奔溃界面初始化
    }
}