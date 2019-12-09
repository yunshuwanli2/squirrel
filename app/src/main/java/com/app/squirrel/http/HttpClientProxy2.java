package com.app.squirrel.http;


import android.os.Build;
import android.text.TextUtils;

import com.app.squirrel.application.MApplication;
import com.app.squirrel.http.CallBack.HttpCallback;
import com.app.squirrel.http.okhttp.IRequestMethod;
import com.app.squirrel.http.okhttp.OkHttpClientManager;
import com.app.squirrel.tool.L;
import com.app.squirrel.tool.ToastUtil;

import java.io.IOException;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by kangpAdministrator on 2017/5/3 0003.
 * Emial kangpeng@yunhetong.net
 */

public class HttpClientProxy2 implements IRequestMethod<String> {
    private static final String TAG = HttpClientProxy2.class.getSimpleName();
    private volatile static HttpClientProxy2 instance;

    private static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");//mdiatype 这个需要和服务端保持一致
    private static final MediaType MEDIA_TYPE_MARKDOWN = MediaType.parse("text/x-markdown; charset=utf-8");//mdiatype 这个需要和服务端保持一致
    private static final MediaType MEDIA_TYPE_STREAM = MediaType.parse("application/octet-stream");

    public static final String BASE_URL = MApplication.getApplication().getBaseUrl_Https();//请求接口根地址


    private HttpClientProxy2() {
    }

    public static HttpClientProxy2 getInstance() {
        if (null == instance) {
            synchronized (HttpClientProxy2.class) {
                if (null == instance) {
                    instance = new HttpClientProxy2();
                }
            }
        }
        return instance;
    }


    @Override
    public void getAsyn(String url, final int requestId, Map<String, Object> paramsMap, final HttpCallback<String> httpCallback) {
        StringBuilder tempParams = new StringBuilder();
        try {
            String requestUrl;
            if (url.startsWith("http://") || url.startsWith("https://")) {
                requestUrl = url;
            } else {
                requestUrl = String.format("%s/%s?%s", BASE_URL, url, tempParams.toString());
            }
            final String finalUrl = requestUrl;
            Request request = new Request.Builder().url(requestUrl).build();
            OkHttpClientManager.getSingleInstance().newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    L.e(TAG, "url:" + finalUrl + "\n msg:" + e.getMessage());
                    callbackFial(requestId, httpCallback);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    analysisResponse(requestId, response, httpCallback);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void postJSONAsyn(String url, int requestId, String params, HttpCallback<String> httpCallback) {

    }

    @Override
    public void postAsyn(String url, int requestId, Map<String, Object> params, HttpCallback<String> httpCallback) {

    }

    @Override
    public void postMultipart(String url, int requestId, Map<String, Object> paramsMap, HttpCallback<String> httpCallback) {

    }


    /**
     * 统一为请求添加头信息
     *
     * @return
     */
    private Request.Builder addHeaders() {
        Request.Builder builder = new Request.Builder()
                .addHeader("Connection", "keep-alive")
                .addHeader("platform", "2")
                .addHeader("phoneModel", Build.MODEL)
                .addHeader("systemVersion", Build.VERSION.RELEASE)
                .addHeader("appVersion", "3.2.0");
        return builder;
    }


    private void analysisResponse(final int requestId, Response response, final HttpCallback<String> callback) {
        int code = response.code();
        String msg = response.message();
        String result = null;
        try {
            if (response.isSuccessful()) {
                result = response.body().string().trim();
                if (!TextUtils.isEmpty(result)) {
                    L.e(TAG, "onSucceed url:" + response.request().url() + "\n +responseCode:" + code + "\n message:" + msg);
                } else {
                    code = -103;
                    msg = "数据为空";
                }
            }
        } catch (Exception e) {
            L.e(TAG, "Exception " + e);
            msg = "系统错误,请重试";
        } finally {
            response.close();
        }
        final int newcode = code;
        final String newMsg = msg;
        final String newResult = result;
        L.d(TAG, "onSucceed data:" + result);
        MApplication.getApplication().getGolbalHander().post(new Runnable() {
            @Override
            public void run() {
                callback(requestId, newcode, newResult, newMsg, callback);
            }
        });

    }

    private void callbackFial(final int requestId, final HttpCallback httpCallback) {
        MApplication.getApplication().getGolbalHander().post(new Runnable() {
            @Override
            public void run() {
                if (httpCallback != null) {
                    ToastUtil.showToast("访问失败,请检查网络设置！");
                    httpCallback.onFail(requestId, "访问失败,请检查网络设置！");
                }
            }
        });
    }

    private void callback(int requestId, int code, String str, String msg, HttpCallback<String> httpCallback) {
        if (null == httpCallback) return;
        switch (code) {
            case 200:
                httpCallback.onSucceed(requestId, str);
                break;
            case 600:
                //帐号其它地方登录
                break;
            case -102:
                //-102 与服务端协商定义 比如用户名密码错误，访问无权限等
                ToastUtil.showToast(msg);
                break;
            case 401:
                //TODO 登录超时，需要重新登录
                break;
            case -101:
                httpCallback.onFail(requestId, "网络错误" + msg);
                ToastUtil.showToast("网络错误" + msg);
                break;
            case 404:
                httpCallback.onFail(requestId, msg);
                ToastUtil.showToast(msg);
                break;
            case -103:
                httpCallback.onFail(requestId, msg);
                ToastUtil.showToast(msg);
                break;
            default:
                httpCallback.onFail(requestId, "Undefined error " + msg);
                ToastUtil.showToast("Undefined error " + msg + code);
                break;
        }
    }
}
