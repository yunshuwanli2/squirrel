package com.app.squirrel.facedetect;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.app.squirrel.BuildConfig;
import com.app.squirrel.facedetect.entry.FaceppBean;
import com.app.squirrel.facedetect.util.Utils;
import com.app.squirrel.http.CallBack.HttpCallback;
import com.app.squirrel.http.HttpClientProxy;

import org.json.JSONObject;

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
        map.put("image_base64", s);
        map.put("return_attributes", "gender,age,facequality");
        mView.showProgress();
        HttpClientProxy.getInstance().postAsyn(url, 1, map, new HttpCallback<JSONObject>() {
            @Override
            public void onSucceed(int requestId, JSONObject result) {
                FaceppBean faceppBean = FaceppBean.jsonToList(result);
                handleDetectResult(photo, faceppBean);
            }

            @Override
            public void onFail(int requestId, String errorMsg) {
                mView.hideProgress();
            }
        });
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


            }
        }
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
