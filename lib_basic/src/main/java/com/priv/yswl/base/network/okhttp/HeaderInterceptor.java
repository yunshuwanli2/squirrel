package com.priv.yswl.base.network.okhttp;

import android.os.Build;

import com.priv.yswl.base.MApplication;
import com.priv.yswl.base.tool.L;
import com.priv.yswl.base.tool.MAppInfoUtil;
import com.priv.yswl.base.tool.MDeviceUtil;
import com.priv.yswl.base.tool.MSPUtils;
import com.priv.yswl.base.tool.MScreenUtils;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;


public class HeaderInterceptor implements Interceptor {
    private static final String TAG = "HeaderInterceptor";
    private static final String AGENT = "User-Agent";
    private static String MAC = null;

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request oldRequest = chain.request();
        Request.Builder builder = oldRequest.newBuilder();
        addHeaders3(builder);
        Request newRequest = builder.build();
        return chain.proceed(newRequest);
    }


    /**
     * 统一为请求添加头信息
     * 1. "deviceType":"1",
     * 1.     "deviceToken":"12312khdsjfhkj1",
     * 1.     "appVersion":"1.2",
     * 1.     "osVersion":"10.1.2",
     *
     * @return
     */
    private Request.Builder addHeaders(Request.Builder builder) {
        return builder
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .addHeader("deviceToken", getMAC())
                .addHeader("deviceType", "2")
                .addHeader("osVersion", Build.VERSION.RELEASE)
                .addHeader("appVersion", MAppInfoUtil.getVersionCode(MApplication.getApplication()) + "");
    }

    private Request.Builder addHeaders2(Request.Builder builder) {
        return builder
                .addHeader("Connection", "keep-alive")
                .addHeader("platform", "2")
                .addHeader("phoneModel", Build.MODEL)
                .addHeader("systemVersion", Build.VERSION.RELEASE)
                .addHeader("appVersion", "3.2.0");
    }

    private Request.Builder addHeaders3(Request.Builder builder) {
        return builder
                .addHeader("Connection", "keep-alive")
                .addHeader("appid", MDeviceUtil.getMAC(MApplication.getApplication()))
                .addHeader("token", MSPUtils.getString("token", ""));
    }

    public static String getMAC() {
        if (MAC == null)
            MAC = MDeviceUtil.getMacAddress();
        L.e(TAG, "mac " + MAC);
        return MAC;
    }

    public static String getDeviceInfo() {
        String systemInfo = System.getProperty("http.agent")
                + Build.MODEL + Build.VERSION.RELEASE;
        return systemInfo + MScreenUtils.getScreenInfo(MApplication.getApplication());
    }


}
