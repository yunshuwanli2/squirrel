package com.app.squirrel.facedetect;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.FileProvider;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.app.squirrel.R;
import com.app.squirrel.facedetect.entry.FaceppBean;
import com.app.squirrel.facedetect.util.Utils;
import com.bumptech.glide.Glide;
import com.gc.materialdesign.views.ButtonRectangle;
import com.gc.materialdesign.views.ProgressBarCircularIndeterminate;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * 主界面
 *
 * @author chaochaowu
 */
public class FaceDetectActivity extends FragmentActivity implements MainContract.View {
    public static void JumpAct(Activity context) {
        Intent intent = new Intent(context, FaceDetectActivity.class);
        context.startActivity(intent);
    }

    private static final String TAG = "MainActivity";
    private static final int PERMISSIONS_REQUEST_CODE = 1;
    private static final int CAMERA_REQUEST_CODE = 2;

    ImageView imageView;
    ProgressBarCircularIndeterminate progressBar;
    ButtonRectangle button;
    File mTmpFile;
    Uri imageUri;
    Bitmap photo = null;
    MainPresenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_detect);

        imageView = findViewById(R.id.imageView);
        progressBar = findViewById(R.id.progressBar);
        button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
            }
        });
        mPresenter = new MainPresenter(this);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CAMERA_REQUEST_CODE:
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 2;
                    photo = BitmapFactory.decodeFile(mTmpFile.getAbsolutePath(), options);
                    int bitmapDegree = Utils.getBitmapDegree(mTmpFile.getAbsolutePath());
                    if (bitmapDegree != 0) {
                        photo = Utils.rotateBitmapByDegree(this.photo, bitmapDegree);
                    }
                    displayPhoto(this.photo);
                    mPresenter.getDetectResultFromServer(this.photo);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void takePhoto() {
        if (!Utils.checkAndRequestPermission(this, PERMISSIONS_REQUEST_CODE)) {
            return;
        }
        Intent intent = new Intent();
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/img";
        if (new File(path).exists()) {
            try {
                new File(path).createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        @SuppressLint("SimpleDateFormat")
        String filename = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        mTmpFile = new File(path, filename + ".jpg");
        mTmpFile.getParentFile().mkdirs();
        String authority = getPackageName() + ".provider";
        imageUri = FileProvider.getUriForFile(this, authority, mTmpFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, CAMERA_REQUEST_CODE);
    }

    @Override
    public void displayPhoto(Bitmap photo) {
        Glide.with(this).load(photo).into(imageView);
    }

    List<FaceppBean.FacesBean> faces = new ArrayList<>();

    @Override
    public void displayFaceInfo(List<FaceppBean.FacesBean> faces) {
        this.faces.clear();
        if (faces == null) {
            this.faces.add(new FaceppBean.FacesBean());
            Toast.makeText(this, "未检测到面部信息", Toast.LENGTH_LONG).show();
        } else {
            this.faces.addAll(faces);
        }
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
