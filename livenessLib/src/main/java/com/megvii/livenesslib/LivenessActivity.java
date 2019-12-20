package com.megvii.livenesslib;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;


/**
 * 主界面
 *
 * @author chaochaowu
 */
public class LivenessActivity extends FragmentActivity {
    public static void JumpAct(Activity context) {
        Intent intent = new Intent(context, LivenessActivity.class);
        context.startActivity(intent);
    }

    private static final String TAG = "MainActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);
        getSupportFragmentManager().beginTransaction().replace(R.id.content, new FaceDetectFragment2()).commitAllowingStateLoss();
    }


}
