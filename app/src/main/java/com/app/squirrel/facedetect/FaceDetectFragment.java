package com.app.squirrel.facedetect;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.app.squirrel.R;
import com.app.squirrel.facedetect.entry.FaceppBean;
import com.bumptech.glide.Glide;
import com.gc.materialdesign.views.ButtonRectangle;
import com.gc.materialdesign.views.ProgressBarCircularIndeterminate;
import com.priv.yswl.base.BaseFragment;
import com.priv.yswl.base.MApplication;
import com.priv.yswl.base.tool.ImgUtil;
import com.priv.yswl.base.tool.ToastUtil;

import java.io.File;
import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 *
 */
public class FaceDetectFragment extends BaseFragment implements MainContract.View {

    public FaceDetectFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_face_detect, container, false);
    }

    private static final int PERMISSIONS_REQUEST_CODE = 1;
    private static final int CAMERA_REQUEST_CODE = 2;
    ImageView imageView;
    ProgressBarCircularIndeterminate progressBar;
    ButtonRectangle button;
    File mTmpFile;
    Uri imageUri;
    Bitmap photo = null;
    //    MainPresenter mPresenter;
    FaceppBean.FacesBean face;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imageView = view.findViewById(R.id.imageView);
        progressBar = view.findViewById(R.id.progressBar);
        button = view.findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
            }
        });
//        mPresenter = new MainPresenter(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length > 0) {
                for (int grantResult : grantResults) {
                    if (grantResult == PackageManager.PERMISSION_DENIED) {
                        return;
                    }
                }
                takePhoto();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CAMERA_REQUEST_CODE:
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 2;
                    photo = BitmapFactory.decodeFile(mTmpFile.getAbsolutePath(), options);
                    int bitmapDegree = ImgUtil.getBitmapDegree(mTmpFile.getAbsolutePath());
                    if (bitmapDegree != 0) {
                        photo = ImgUtil.rotateBitmapByDegree(this.photo, bitmapDegree);
                    }
                    displayPhoto(this.photo);
//                    mPresenter.getDetectResultFromServer(this.photo);
                    break;
                default:
                    break;
            }
        }
    }

    public static boolean checkAndRequestPermission(Context context, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || context.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ((Activity) context).requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, requestCode);
                return false;
            }
        }
        return true;
    }

    private void takePhoto() {
        if (!checkAndRequestPermission(getActivity(), PERMISSIONS_REQUEST_CODE)) {
            return;
        }
        Intent intent = new Intent();
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        mTmpFile = new File(getCachePicturePath());
        String authority = getActivity().getPackageName() + ".provider";
        imageUri = FileProvider.getUriForFile(getActivity(), authority, mTmpFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, CAMERA_REQUEST_CODE);
    }

    public static String getCachePicturePath() {
        String path;
        File tempFile;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/SQ/";
        } else {
            path = MApplication.getApplication().getCacheDir() + "/SQ/";
        }
        String filePath = path + "YSWL.jpg";
        tempFile = new File(filePath);
        tempFile.getParentFile().mkdirs();
        return filePath;

    }

    @Override
    public void displayPhoto(Bitmap photo) {
        Glide.with(this).load(photo).into(imageView);
    }

    @Override
    public void displayFaceInfo(List<FaceppBean.FacesBean> faces) {
        this.face = null;
        if (faces == null) {
            ToastUtil.showToast("未检测到面部信息");
        } else {
            this.face = faces.get(0);
        }
    }

    @Override
    public void finshActivity() {
        getActivity().finish();
    }

    @Override
    public void showProgress() {
        button.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgress() {
        button.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }
}
