package com.app.squirrel.http.okhttp;

import com.app.squirrel.http.CallBack.HttpCallback;

import java.util.Map;



public interface IRequestMethod<T> {
//        void getSyn(String url, int requestId, ArrayMap<String, Object> params);
//
//        void postJSONSyn(String url, int requestId, ArrayMap<String, Object> params);
//
//        void postFormSyn(String url, int requestId, ArrayMap<String, Object> params);

    void getAsyn(String url, int requestId, Map<String, Object> params, final HttpCallback<T> httpCallback);

    void postJSONAsyn(String url, int requestId, Map<String, Object> params, final HttpCallback<T> httpCallback);

    void postAsyn(String url, int requestId, Map<String, Object> params, final HttpCallback<T> httpCallback);

    void postMultipart(String url, int requestId, Map<String, Object> paramsMap, final HttpCallback<T> httpCallback);

}