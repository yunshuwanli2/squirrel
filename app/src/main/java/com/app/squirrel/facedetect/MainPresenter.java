package com.app.squirrel.facedetect;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Message;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.app.squirrel.BuildConfig;
import com.app.squirrel.application.MApplication;
import com.app.squirrel.application.SquirrelApplication;
import com.app.squirrel.facedetect.entry.FaceSearchBean;
import com.app.squirrel.facedetect.entry.FaceppBean;
import com.app.squirrel.facedetect.entry.FacesetTokenBean;
import com.app.squirrel.facedetect.util.Utils;
import com.app.squirrel.http.CallBack.HttpCallback;
import com.app.squirrel.http.HttpClientProxy;
import com.app.squirrel.tool.L;
import com.app.squirrel.tool.ToastUtil;
import com.app.squirrel.tool.UserManager;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author chaochaowu
 */
public class MainPresenter implements MainContract.Presenter {

    private static final String TAG = "MainPresenter";


    private MainContract.View mView;
    RequestQueue mQueue;

    public MainPresenter(MainContract.View mView) {
        this.mView = mView;
        mQueue = Volley.newRequestQueue(MApplication.getApplication());
    }

    @Override
    public void getDetectResultFromServer(final Bitmap photo) {
        final String s = Utils.base64(photo);
        String url = "https://api-cn.faceplusplus.com/facepp/v3/detect";

        mView.showProgress();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                mView.hideProgress();
                L.e(TAG, "face detect request  onResponse:" + response);
                FaceppBean faceppBean = FaceppBean.jsonToBean(response);
                handleDetectResult(photo, faceppBean);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mView.hideProgress();
                L.e(TAG, "face detect request onErrorResponse:" + new String(error.networkResponse.data));
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                map.put("api_key", BuildConfig.API_KEY);
                map.put("api_secret", BuildConfig.API_SECRET);
                map.put("image_base64", s);
                map.put("return_attributes", "gender,age,facequality");
                return map;
            }
        };
        mQueue.add(stringRequest);

    }

    @Override
    public void getDetectResultFromServer(final File file) {
    }

    private void handleDetectResult(Bitmap photo, FaceppBean faceppBean) {
        List<FaceppBean.FacesBean> faces = faceppBean.getFaces();
        if (faces == null || faces.size() == 0) {
            mView.displayFaceInfo(null);
        } else {
            Bitmap photoMarkedFaces = markFacesInThePhoto(photo, faces);
            mView.displayPhoto(photoMarkedFaces);
            mView.displayFaceInfo(faces);
            FaceppBean.FacesBean.AttributesBean attributesBean = faces.get(0).getAttributes();
            FaceppBean.FacesBean.AttributesBean.FacequalityBean facequalityBean = attributesBean.getFacequality();
            if (facequalityBean != null && facequalityBean.getThreshold() >= 60) {
                requestFaceSet(faces.get(0).getFace_token());
            }
        }
    }

    private void requestFaceSet(final String currFaceToken) {
        String url = "wxApi/fetchFaceSet";
        mView.showProgress();
        HttpClientProxy.getInstance().getAsyn(url, 2, null, new HttpCallback<JSONObject>() {
            @Override
            public void onSucceed(int requestId, JSONObject result) {

                L.e(TAG, "fetchFaceSet request onSucceed:" + result.toString());
                if (result.optString("code").equals("0")) {
                    final List<FacesetTokenBean> tokenBeans = FacesetTokenBean.jsonToBeans(result.optJSONArray("data"));
                    if (tokenBeans == null || tokenBeans.size() == 0) {
                        ToastUtil.showToast("请登录小程序录入人脸信息");
                        return;
                    }
                    final String url_search = "https://api-cn.faceplusplus.com/facepp/v3/search";
                    final AtomicBoolean searchTag = new AtomicBoolean(false);
                    for (int i = 0; i < tokenBeans.size(); i++) {
                        final int index = i;
                        if (searchTag.get()) {
                            break;
                        }
                        StringRequest stringRequest = new StringRequest(Request.Method.POST, url_search, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                mView.hideProgress();
                                L.e(TAG, "Face search request onResponse:" + response);
                                FaceSearchBean searchBean = FaceSearchBean.jsonToBean(response);
                                if (searchBean != null && searchBean.getResults() != null && searchBean.getResults().get(0) != null) {
                                    FaceSearchBean.ResultsBean resultsBean = searchBean.getResults().get(0);
                                    if (resultsBean.getConfidence() >= 60) {
                                        ToastUtil.showToast("登录成功");
                                        EventBus.getDefault().postSticky(new Message());
                                        mView.finshActivity();
                                        searchTag.set(true);
                                    } else {
                                        if (index == (tokenBeans.size() - 1)) {
                                            ToastUtil.showToast("未检测到相关信息");
                                        }

                                    }
                                }

                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                L.e(TAG, "Face search request onErrorResponse:" + new String(error.networkResponse.data));
                                mView.hideProgress();
                                if (index == (tokenBeans.size() - 1)) {
                                    ToastUtil.showToast("登录失败");
                                }
                            }
                        }) {
                            @Override
                            protected Map<String, String> getParams() throws AuthFailureError {
                                Map<String, String> map = new HashMap();
                                map.put("api_key", BuildConfig.API_KEY);
                                map.put("api_secret", BuildConfig.API_SECRET);
                                map.put("face_token", currFaceToken);
                                map.put("faceset_token", tokenBeans.get(index).getFacesetToken());
                                return map;
                            }
                        };
                        mQueue.add(stringRequest);
                    }
                }

            }

            @Override
            public void onFail(int requestId, String errorMsg) {
                L.e(TAG, "fetchFaceSet request onFail:" + errorMsg);
                mView.hideProgress();
            }
        });
    }

    private Bitmap markFacesInThePhoto(Bitmap bitmap, List<FaceppBean.FacesBean> faces) {
        Bitmap tempBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(tempBitmap);
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);

        for (FaceppBean.FacesBean face : faces) {
            FaceppBean.FacesBean.FaceRectangleBean faceRectangle = face.getFace_rectangle();
            int top = faceRectangle.getTop();
            int left = faceRectangle.getLeft();
            int height = faceRectangle.getHeight();
            int width = faceRectangle.getWidth();
            canvas.drawRect(left, top, left + width, top + height, paint);
        }
        return tempBitmap;
    }


}
