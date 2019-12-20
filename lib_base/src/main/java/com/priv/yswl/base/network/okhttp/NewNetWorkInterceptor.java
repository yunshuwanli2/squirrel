package com.priv.yswl.base.network.okhttp;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;


/**
 * Created by nixn@yunhetong.net on 2016/10/10.
 */

public class NewNetWorkInterceptor implements Interceptor {
    public static final String TAG = "NewNetWorkInterceptor";

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request oldRequest = chain.request();
        Request.Builder builder = oldRequest.newBuilder();
        addHeaders(builder);
        Request newRequest = builder.build();
        return chain.proceed(newRequest);
    }

    private Request.Builder addHeaders(Request.Builder builder) {
        return builder
                .addHeader("Content-Type", "application/json; charset=utf-8");
    }
}
