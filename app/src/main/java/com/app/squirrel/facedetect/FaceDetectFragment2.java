package com.app.squirrel.facedetect;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.app.squirrel.BuildConfig;
import com.app.squirrel.activity.LoginActivity;
import com.app.squirrel.activity.MainActivity;
import com.app.squirrel.application.MApplication;
import com.app.squirrel.facedetect.entry.FaceSearchBean;
import com.app.squirrel.facedetect.entry.FaceppBean;
import com.app.squirrel.facedetect.entry.FacesetTokenBean;
import com.app.squirrel.facedetect.entry.PreviewFrameBean;
import com.app.squirrel.facedetect.util.Utils;
import com.app.squirrel.fragment.BaseFragment;
import com.app.squirrel.http.CallBack.HttpCallback;
import com.app.squirrel.http.HttpClientProxy;
import com.app.squirrel.http.okhttp.MScreenUtils;
import com.app.squirrel.tool.L;
import com.app.squirrel.tool.ToastUtil;
import com.app.squirrel.tool.UserManager;
import com.megvii.livenesslib.FaceMask;
import com.megvii.livenesslib.util.ICamera;
import com.megvii.livenesslib.util.IMediaPlayer;
import com.megvii.livenesslib.util.Screen;
import com.megvii.livenesslib.util.SensorUtil;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 */
public class FaceDetectFragment2 extends BaseFragment {

    private static final String TAG = FaceDetectFragment2.class.getSimpleName();

    private TextureView camerapreview;
    private FaceMask mFaceMask;// 画脸位置的类（调试时会用到）
    private ProgressBar mProgressBar;// 网络上传请求验证时出现的ProgressBar

    private IMediaPlayer mIMediaPlayer;// 多媒体工具类
    private ICamera mICamera;// 照相机工具类
    private Camera mCamera;
    private SensorUtil sensorUtil;
    private PreviewFrameBean previewFrameBean;

    private FragmentActivity activity;
    RequestQueue mQueue;
    HandlerThread mHandlerThread;
    SafeHandler mHandle;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (FragmentActivity) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(com.megvii.livenesslib.R.layout.liveness_layout, null);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);
        mQueue = Volley.newRequestQueue(MApplication.getApplication());
        mHandlerThread = new HandlerThread("FaceUpdateThread");
        mHandlerThread.start();
        mHandle = new SafeHandler(this, mHandlerThread.getLooper());
        previewFrameBean = new PreviewFrameBean();
        //两秒后抓取一帧
        mHandle.sendEmptyMessageDelayed(SafeHandler.MSG_CATCH_PRE_PICTURE, 2 * 1000);
    }


    private void init(View view) {
        sensorUtil = new SensorUtil(activity);
        Screen.initialize(activity);
        mIMediaPlayer = new IMediaPlayer(activity);
        mFaceMask = (FaceMask) view.findViewById(com.megvii.livenesslib.R.id.liveness_layout_facemask);
        mICamera = new ICamera();
        camerapreview = (TextureView) view.findViewById(com.megvii.livenesslib.R.id.liveness_layout_textureview);
        camerapreview.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
                Log.d(TAG, "[onSurfaceTextureAvailable]" + surfaceTexture);
                mHasSurface = true;
                doPreview();

                final int[] count = {0};
                mICamera.actionDetect(new PreviewCallback() {
                    @Override
                    public void onPreviewFrame(byte[] data, Camera camera) {
                        Log.d(TAG, "[onPreviewFrame] data:" + data);

                        count[0]++;
                        if (count[0] < Integer.MAX_VALUE && count[0] % 10 == 0) {
                            previewFrameBean.setFrame(data,
                                    camera.getParameters().getPreviewSize().width,
                                    camera.getParameters().getPreviewSize().height);
                        } else if (count[0] >= Integer.MAX_VALUE) {
                            count[0] = 0;
                        }
                    }
                });
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {
                Log.d(TAG, "[onSurfaceTextureSizeChanged] surfaceTexture:" + surfaceTexture);
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                Log.d(TAG, "[onSurfaceTextureDestroyed] surfaceTexture:" + surfaceTexture);
                mHasSurface = false;
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
                Log.d(TAG, "[onSurfaceTextureUpdated] surfaceTexture:" + surfaceTexture);
            }
        });
        mProgressBar = (ProgressBar) view.findViewById(com.megvii.livenesslib.R.id.liveness_layout_progressbar);
        mProgressBar.setVisibility(View.INVISIBLE);

    }


    final static class SafeHandler extends Handler {
        public static final int MSG_CATCH_PRE_PICTURE = 0x7;
        private WeakReference<FaceDetectFragment2> mWeakReference;

        private SafeHandler(FaceDetectFragment2 fragment2, Looper looper) {
            super(looper);
            mWeakReference = new WeakReference<>(fragment2);
        }

        @Override
        public void handleMessage(Message msg) {
            final FaceDetectFragment2 fragment2 = mWeakReference.get();
            if (fragment2 == null) return;
            switch (msg.what) {
                case MSG_CATCH_PRE_PICTURE:
                    int width = fragment2.previewFrameBean.getWidth();
                    int height = fragment2.previewFrameBean.getHeight();
                    byte[] data = (byte[]) fragment2.previewFrameBean.getFrame();
                    fragment2.setParmBackFinish(data, width, height);
                    removeMessages(MSG_CATCH_PRE_PICTURE);
                    break;
                default:
                    break;
            }

        }
    }


    protected void setParmBackFinish(byte[] bytes, int width, int height) {
        Log.e(TAG, "setParmBackFinish");
        Bitmap bmp = Utils.byte2Bitmap(bytes, width, height);
        if (bmp == null) return;
        final String s = Utils.base64(bmp);
        String url = "https://api-cn.faceplusplus.com/facepp/v3/detect";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                L.e(TAG, "face detect request  onResponse:" + response);
                FaceppBean faceppBean = FaceppBean.jsonToBean(response);
                handleDetectResult(faceppBean);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
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
                L.d(TAG, "getParams:" + map.toString());
                return map;
            }
        };
        mQueue.add(stringRequest);

    }

    private void handleDetectResult(FaceppBean faceppBean) {
        List<FaceppBean.FacesBean> faces = faceppBean.getFaces();
        if (faces == null || faces.size() == 0) {
            ToastUtil.showToast("没有检测到人脸");
            mHandle.sendEmptyMessage(SafeHandler.MSG_CATCH_PRE_PICTURE);
        } else {
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
                                L.e(TAG, "Face search request onResponse:" + response);
                                FaceSearchBean searchBean = FaceSearchBean.jsonToBean(response);
                                if (searchBean != null && searchBean.getResults() != null && searchBean.getResults().get(0) != null) {
                                    FaceSearchBean.ResultsBean resultsBean = searchBean.getResults().get(0);
                                    if (resultsBean.getConfidence() >= 60) {
                                        ToastUtil.showToast("登录成功");
                                        UserManager.setLoginStatus(true);
                                        EventBus.getDefault().postSticky(new Message());
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
//                                map.put("faceset_token", tokenBeans.get(index).getFacesetToken());
                                map.put("outer_id", tokenBeans.get(index).getOuterId());
                                L.d(TAG, "getParams:" + map.toString());
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
            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();
        mCamera = mICamera.openCamera(activity); // 任意可能被拒绝权限程序崩溃的代码
        if (mCamera != null) {
            CameraInfo cameraInfo = new CameraInfo();
            Camera.getCameraInfo(1, cameraInfo);
            mFaceMask.setFrontal(cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT);
            RelativeLayout.LayoutParams layout_params = mICamera.getLayoutParam();
            camerapreview.setLayoutParams(layout_params);
            mFaceMask.setLayoutParams(layout_params);
        }
    }


    private boolean mHasSurface = false;

    private void doPreview() {
        if (!mHasSurface)
            return;
        mICamera.startPreview(camerapreview.getSurfaceTexture());
    }

    @Override
    public void onPause() {
        super.onPause();
        mICamera.closeCamera();
        mCamera = null;
        mIMediaPlayer.close();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sensorUtil.release();
    }


}