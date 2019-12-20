package com.priv.yswl.base.network.okhttp;


import com.priv.yswl.base.MApplication;
import com.priv.yswl.base.network.Auth.TrustAllSSL;

import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.OkHttpClient;

public class OkHttpClientManager {

    public static volatile OkHttpClient mOKHttpClient;
    public static final int readTimeout = 30;
    public static final int writeTimeout = 30;
    public static final int connectTimeout = 30;

    public static void init() {

//        Observable.just(0).subscribeOn(Schedulers.newThread()).subscribe(new Action1<Integer>() {
//            @Override
//            public void call(Integer integer) {
//                getSingleInstance();
//            }
//        });
        MApplication.getApplication().getGolbalHander().post(new Runnable() {
            @Override
            public void run() {
                getSingleInstance();
            }
        });
    }

    public static OkHttpClient getSingleInstance() {
        if (mOKHttpClient == null) {
            synchronized (OkHttpClient.class) {
                if (mOKHttpClient == null) {
                    mOKHttpClient = getInstance();
                }
            }
        }
        return mOKHttpClient;
    }

    private static OkHttpClient getInstance() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
//                .cookieJar(new MyCookieJar())
                .cache(new Cache(MApplication.getApplication().getCacheDir(), 1024 * 1024))
                .connectTimeout(connectTimeout, TimeUnit.SECONDS)
                .readTimeout(readTimeout, TimeUnit.SECONDS)
                .writeTimeout(writeTimeout, TimeUnit.SECONDS)
                .addInterceptor(new HeaderInterceptor());
//                .addNetworkInterceptor(new NewNetWorkInterceptor())
//                .hostnameVerifier(new TrustAllSSL.TrustAllHostnameVerifier())
//                .sslSocketFactory(TrustAllSSL.getSSLSocketFactory(),new TrustAllSSL.TrustAllManager());
        if (MApplication.getApplication().getDebugSetting()) {
            builder.addNetworkInterceptor(new HttpLog());
        }
        return builder.build();
    }
    private static OkHttpClient getInstance2() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .cache(new Cache(MApplication.getApplication().getCacheDir(), 512 * 1024))
                .connectTimeout(connectTimeout, TimeUnit.SECONDS)
                .readTimeout(readTimeout, TimeUnit.SECONDS)
                .writeTimeout(writeTimeout, TimeUnit.SECONDS)
                .addInterceptor(new HeaderInterceptor());
//                .addNetworkInterceptor(new NewNetWorkInterceptor());
//                .hostnameVerifier(new TrustAllSSL.TrustAllHostnameVerifier());
        if (MApplication.getApplication().getDebugSetting()) {
            builder.addNetworkInterceptor(new HttpLog());
            builder.sslSocketFactory(TrustAllSSL.getSSLSocketFactory());
        }
        return builder.build();
    }


}
