package com.app.squirrel.facedetect;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.app.squirrel.BuildConfig;
import com.app.squirrel.facedetect.entry.FaceppBean;
import com.app.squirrel.facedetect.entry.FacesetTokenBean;
import com.app.squirrel.facedetect.util.Utils;
import com.app.squirrel.http.CallBack.HttpCallback;
import com.app.squirrel.http.HttpClientProxy;
import com.app.squirrel.tool.L;
import com.app.squirrel.tool.ToastUtil;

import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author chaochaowu
 */
public class MainPresenter implements MainContract.Presenter {

    private static final String TAG = "MainPresenter";


    private MainContract.View mView;

    public MainPresenter(MainContract.View mView) {
        this.mView = mView;
    }

    @Override
    public void getDetectResultFromServer(final Bitmap photo) {
        String s = Utils.base64(photo);
        String url = "https://api-cn.faceplusplus.com/facepp/v3/detect";
        Map<String, Object> map = new HashMap();
        map.put("api_key", BuildConfig.API_KEY);
        map.put("api_secret", BuildConfig.API_SECRET);
//        map.put("image_base64", s);
        map.put("return_attributes", "gender,age,facequality");
        mView.showProgress();
        HttpClientProxy.getInstance().postAsyn(url, 1, map, new HttpCallback<JSONObject>() {
            @Override
            public void onSucceed(int requestId, JSONObject result) {
                FaceppBean faceppBean = FaceppBean.jsonToBean(result);
                handleDetectResult(photo, faceppBean);
            }

            @Override
            public void onFail(int requestId, String errorMsg) {
                mView.hideProgress();
            }
        });
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
        HttpClientProxy.getInstance().getAsyn(url, 2, null, new HttpCallback<JSONObject>() {
            @Override
            public void onSucceed(int requestId, JSONObject result) {
                if (result != null && result.optString("code").equals("0")) {
                    List<FacesetTokenBean> tokenBeans = FacesetTokenBean.jsonToBeans(result.optJSONArray("data"));
                    if (tokenBeans == null || tokenBeans.size() == 0) {
                        ToastUtil.showToast("请登录小程序录入人脸信息");
                        return;
                    }
                    final String url_search = "https://api-cn.faceplusplus.com/facepp/v3/search";
                    for (FacesetTokenBean tokenBean : tokenBeans) {
                        Map<String, Object> map = new HashMap();
                        map.put("api_key", BuildConfig.API_KEY);
                        map.put("api_secret", BuildConfig.API_SECRET);
                        map.put("face_token", currFaceToken);
                        map.put("faceset_token", tokenBean.getFacesetToken());

                        HttpClientProxy.getInstance().postAsyn(url_search, 1, map, new HttpCallback<JSONObject>() {
                            @Override
                            public void onSucceed(int requestId, JSONObject result) {
                                //TODO 其中有一个成功即可
                                mView.hideProgress();
                                if (true) {
                                    ToastUtil.showToast("请登录小程序录入人脸信息");
                                } else {
                                    ToastUtil.showToast("登录成功");
                                }

                            }

                            @Override
                            public void onFail(int requestId, String errorMsg) {
                                mView.hideProgress();
                                ToastUtil.showToast("登录失败");
                            }
                        });
                    }
                }

            }

            @Override
            public void onFail(int requestId, String errorMsg) {
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
