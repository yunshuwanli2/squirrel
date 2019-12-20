package com.megvii.livenesslib;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.megvii.livenesslib.util.DialogUtil;
import com.megvii.livenesslib.util.ICamera;
import com.megvii.livenesslib.util.IMediaPlayer;
import com.megvii.livenesslib.util.Screen;
import com.megvii.livenesslib.util.SensorUtil;

/**
 * face++ 中sdk提取的代码
 * 付费服务
 */
public class LivenessFragment extends Fragment {

    private static final String TAG = LivenessFragment.class.getSimpleName();

    private TextureView camerapreview;
    private FaceMask mFaceMask;// 画脸位置的类（调试时会用到）
    private ProgressBar mProgressBar;// 网络上传请求验证时出现的ProgressBar

    private Handler mainHandler;
    private IMediaPlayer mIMediaPlayer;// 多媒体工具类
    private ICamera mICamera;// 照相机工具类
    private DialogUtil mDialogUtil;
    private Camera mCamera;
    private SensorUtil sensorUtil;


    private FragmentActivity activity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (FragmentActivity) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.liveness_layout, null);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);
    }


    private void init(View view) {
        sensorUtil = new SensorUtil(activity);
        Screen.initialize(activity);
        mainHandler = new Handler();
        mIMediaPlayer = new IMediaPlayer(activity);
        mFaceMask = (FaceMask) view.findViewById(R.id.liveness_layout_facemask);
        mICamera = new ICamera();
        camerapreview = (TextureView) view.findViewById(R.id.liveness_layout_textureview);
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
                        Size previewsize = camera.getParameters().getPreviewSize();

                        count[0]++;
                        if (count[0] == 50){}
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
        mProgressBar = (ProgressBar) view.findViewById(R.id.liveness_layout_progressbar);
        mProgressBar.setVisibility(View.INVISIBLE);

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
        } else {
            mDialogUtil.showDialog("打开前置摄像头失败");
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
        mainHandler.removeCallbacksAndMessages(null);
        mICamera.closeCamera();
        mCamera = null;
        mIMediaPlayer.close();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDialogUtil.onDestory();
        sensorUtil.release();
    }


}