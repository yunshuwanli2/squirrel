package com.priv.arcsoft;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.priv.yswl.base.permission.PermissionListener;
import com.priv.yswl.base.permission.PermissionUtil;

import java.util.List;


/**
 * 主界面
 *
 * @author chaochaowu
 */
public class ArcSoftFaceActivity extends FragmentActivity {
    public static void JumpAct(Activity context) {
        Intent intent = new Intent(context, ArcSoftFaceActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);
        PermissionUtil permissionUtil = new PermissionUtil(this);
        permissionUtil.requestPermissions(PermissionUtil.READ_WRITE_CAMERA_PERMISSION, new PermissionListener() {
            @Override
            public void onGranted() {
                getSupportFragmentManager().beginTransaction().replace(R.id.content, new MultiCameraFaceDetectFragment()).commitAllowingStateLoss();
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
