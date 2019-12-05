package com.app.squirrel.application;

import com.app.squirrel.http.okhttp.MSPUtils;
import com.app.squirrel.http.okhttp.OkHttpClientManager;

import cat.ereza.customactivityoncrash.CustomActivityOnCrash;
import cn.jpush.android.api.JPushInterface;

/**
 * Created by admin on 2019/10/24.
 */

public class SquirrelApplication extends MApplication {
    private static final String TAG ="SquirrelApplication" ;

  public static boolean test = false;
    @Override
    public boolean getDebugSetting() {
        return true;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        init();
        OkHttpClientManager.init();
        CustomActivityOnCrash.install(this);//自定义奔溃界面初始化
    }

    private void init() {
        MSPUtils.clear(this);
        JPushInterface.setDebugMode(getDebugSetting());
        JPushInterface.init(this);
    }

    @Override
    public String getBaseUrl_Https() {
//        return "http://www.bumain.com:8090/";
        return "http://60.205.177.220:8090";
    }
}
