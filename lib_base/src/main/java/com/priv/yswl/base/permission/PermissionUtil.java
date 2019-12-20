package com.priv.yswl.base.permission;

import android.Manifest;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

/**
 * 权限申请入口类
 */
public class PermissionUtil {

    private static final String TAG = "PermissionsUtil";
    public static String[] READ_WRITE_CAMERA_PERMISSION = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.CAMERA
    };

    private PermissionFragment fragment;

    public PermissionUtil(@NonNull FragmentActivity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            fragment = getPermissionsFragment(activity);
    }

    private PermissionFragment getPermissionsFragment(FragmentActivity activity) {
        if(activity==null)return null;
        PermissionFragment fragment = (PermissionFragment) activity.getSupportFragmentManager().findFragmentByTag(TAG);
        boolean isNewInstance = fragment == null;
        if (isNewInstance) {
            fragment = new PermissionFragment();
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            fragmentManager
                    .beginTransaction()
                    .add(fragment, TAG)
                    .commit();
            fragmentManager.executePendingTransactions();
        }

        return fragment;
    }

    /**
     * 外部调用申请权限
     *
     * @param permissions 申请的权限 允许为空
     * @param listener    监听权限接口 不允许为空
     */
    public void requestPermissions(String[] permissions, @NonNull PermissionListener listener) {
        if (Build.VERSION.SDK_INT >= 26 || fragment != null) {
            fragment.setListener(listener);
            fragment.requestPermissions(permissions);
        } else {
            listener.onGranted();
        }

    }

}