package com.priv.yswl.base.permission;

import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by kangpAdministrator on 2018/7/17 0017.
 * Emial kangpeng@yunhetong.net
 */

public class PermissionFragment extends Fragment {
    /**
     * 申请权限的requestCode
     */
    private static final int PERMISSIONS_REQUEST_CODE = 1;

    /**
     * 权限监听接口
     */
    private PermissionListener mListener;

    public void setListener(PermissionListener mListener) {
        this.mListener = mListener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Fragment具有属性retainInstance，默认值为false。
//        当设备旋转时，fragment会随托管activity一起销毁并重建。
//        已保留的fragment不会随着activity一起被销毁；
//        相反，它会一直保留(进程不消亡的前提下)，并在需要时原封不动地传递给新的Activity。
        setRetainInstance(true);
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void requestPermissions(@NonNull String[] permissions) {
        List<String> requestPeimissions = new ArrayList<>();
        for (String permiss : permissions) {
            if (ContextCompat.checkSelfPermission(getActivity(), permiss)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPeimissions.add(permiss);
            }
        }

        if (requestPeimissions.isEmpty()) {
            //TODO 全部授权
            permissionAllGranted();
        } else {
            requestPermissions(requestPeimissions.toArray(new String[requestPeimissions.size()]), PERMISSIONS_REQUEST_CODE);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != PERMISSIONS_REQUEST_CODE) return;
        if (grantResults.length > 0) {
            List<String> deniedPermissionList = new ArrayList<>();
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    deniedPermissionList.add(permissions[i]);
                }
            }

            if (deniedPermissionList.isEmpty()) {
                //已经全部授权
                permissionAllGranted();
            } else {

                //勾选了对话框中”Don’t ask again”的选项, 返回false
                for (String deniedPermission : deniedPermissionList) {
                    boolean flag = shouldShowRequestPermissionRationale(deniedPermission);
                    if (!flag) {
                        //拒绝授权
                        permissionDeniedForever(deniedPermissionList);
                        return;
                    }
                }
                //拒绝授权
                permissionHasDenied(deniedPermissionList);

            }
        }
    }

    /**
     * 权限全部已经授权
     */
    private void permissionAllGranted() {
        if (mListener != null) {
            mListener.onGranted();
        }
    }

    /**
     * 有权限被拒绝
     *
     * @param deniedList 被拒绝的权限
     */
    private void permissionHasDenied(List<String> deniedList) {
        if (mListener != null) {
            mListener.onDenied(deniedList);
        }
    }

    /**
     * 权限被拒绝并且勾选了不在询问
     *
     * @param deniedList 勾选了不在询问的权限
     */
    private void permissionDeniedForever(List<String> deniedList) {
        if (mListener != null) {
            mListener.onDeniedForever(deniedList);
        }
    }

   /* @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof PermissionListener) {
            mListener = (PermissionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }
*/
    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}
