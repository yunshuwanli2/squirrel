package com.megvii.livenesslib;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Base64;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.megvii.livenessdetection.DetectionConfig;
import com.megvii.livenessdetection.DetectionFrame;
import com.megvii.livenessdetection.Detector;
import com.megvii.livenessdetection.FaceQualityManager;
import com.megvii.livenessdetection.FaceQualityManager.FaceQualityErrorType;
import com.megvii.livenessdetection.bean.FaceIDDataStruct;
import com.megvii.livenessdetection.bean.FaceInfo;
import com.megvii.livenesslib.util.ConUtil;
import com.megvii.livenesslib.util.Constant;
import com.megvii.livenesslib.util.DialogUtil;
import com.megvii.livenesslib.util.ICamera;
import com.megvii.livenesslib.util.IDetection;
import com.megvii.livenesslib.util.IFile;
import com.megvii.livenesslib.util.IMediaPlayer;
import com.megvii.livenesslib.util.Screen;
import com.megvii.livenesslib.util.SensorUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.List;

/**
 *
 */
public class LivenessActivity extends FragmentActivity {

    private static final String TAG = LivenessActivity.class.getSimpleName();

    public static void JumpActForResult(Activity context) {
        Intent intent = new Intent(context, LivenessActivity.class);
        context.startActivityForResult(intent, 2);
    }

    private TextureView camerapreview;
    private FaceMask mFaceMask;// 画脸位置的类（调试时会用到）
    private ProgressBar mProgressBar;// 网络上传请求验证时出现的ProgressBar

    private Handler mainHandler;
    private IMediaPlayer mIMediaPlayer;// 多媒体工具类
    private ICamera mICamera;// 照相机工具类
    private DialogUtil mDialogUtil;
    private Camera mCamera;
    private SensorUtil sensorUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.liveness_layout);
        init();
    }


    private void init() {
        sensorUtil = new SensorUtil(this);
        Screen.initialize(this);
        mainHandler = new Handler();
        mIMediaPlayer = new IMediaPlayer(this);
        mDialogUtil = new DialogUtil(this);
        mFaceMask = (FaceMask) findViewById(R.id.liveness_layout_facemask);
        mICamera = new ICamera();
        camerapreview = (TextureView) findViewById(R.id.liveness_layout_textureview);
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
                        if (count[0] == 50)
                            LivenessActivity.this.setParmBackFinish(data);
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
        mProgressBar = (ProgressBar) findViewById(R.id.liveness_layout_progressbar);
        mProgressBar.setVisibility(View.INVISIBLE);

    }

    protected void setParmBackFinish(byte[] bytes) {
        Log.e(TAG, "setParmBackFinish");
        File file = IFile.byte2File(Constant.dirName,bytes);
        if (file.exists()) {
            Intent intent = new Intent();
            intent.putExtra("FACE_PRE_IMG", file.getAbsolutePath());
            setResult(1, intent);
            LivenessActivity.this.finish();
        } else {
            Log.e(TAG, "file not exists");
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        mCamera = mICamera.openCamera(this); // 任意可能被拒绝权限程序崩溃的代码
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
    protected void onPause() {
        super.onPause();
        mainHandler.removeCallbacksAndMessages(null);
        mICamera.closeCamera();
        mCamera = null;
        mIMediaPlayer.close();

        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDialogUtil.onDestory();
        sensorUtil.release();
    }


}