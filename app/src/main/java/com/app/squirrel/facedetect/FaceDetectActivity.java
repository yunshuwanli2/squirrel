package com.app.squirrel.facedetect;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.app.squirrel.R;


/**
 * 主界面
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
        setContentView(R.layout.activity_face);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content, new FaceDetectFragment())
                .commitAllowingStateLoss();
    }


}
