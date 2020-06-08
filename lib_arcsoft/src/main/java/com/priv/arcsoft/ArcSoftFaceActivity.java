package com.priv.arcsoft;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.priv.yswl.base.permission.PermissionListener;
import com.priv.yswl.base.permission.PermissionUtil;

import java.util.List;


/**
 * 主界面
 *
 */
public class ArcSoftFaceActivity extends FragmentActivity {
    public final static String isLogin_key = "isLogin";
    public final  static String isFace_key = "isFace";

    public static void JumpAct(Activity context, boolean isLogin, boolean isFace) {
        Intent intent = new Intent(context, ArcSoftFaceActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(isLogin_key, isLogin);
        intent.putExtra(isFace_key, isFace);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);

        final boolean isLogin = getIntent().getBooleanExtra(isLogin_key, false);
        final boolean isFace = getIntent().getBooleanExtra(isFace_key, false);
        PermissionUtil permissionUtil = new PermissionUtil(this);
        permissionUtil.requestPermissions(PermissionUtil.READ_WRITE_CAMERA_PERMISSION, new PermissionListener() {
            @Override
            public void onGranted() {
                FaceDetectFragment3 detectFragment3 = new FaceDetectFragment3();
                Bundle bundle = new Bundle();
                bundle.putBoolean(isLogin_key, isLogin);
                bundle.putBoolean(isFace_key, isFace);
                detectFragment3.setArguments(bundle);
                getSupportFragmentManager().beginTransaction().replace(R.id.content, detectFragment3).commitAllowingStateLoss();
            }

            @Override
            public void onDenied(List<String> deniedPermission) {

            }

            @Override
            public void onDeniedForever(List<String> deniedPermission) {

            }
        });

    }


}
