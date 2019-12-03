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
public class FaceDetectActivity extends FragmentActivity {
    public static void JumpAct(Activity context) {
        Intent intent = new Intent(context, FaceDetectActivity.class);
        context.startActivity(intent);
    }

    private static final String TAG = "MainActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);
        getSupportFragmentManager().beginTransaction().replace(R.id.content, new FaceDetectFragment()).commitAllowingStateLoss();
    }


}
