package com.priv.yswl.base.permission;

import java.util.List;

/**
 * 权限申请回调
 */
public interface PermissionListener {
    /**
     * 同意
     */
    void onGranted();

    /**
     * 拒绝授权
     *
     * @param deniedPermission
     */
    void onDenied(List<String> deniedPermission);

    /**
     * 拒绝授权 且勾选了不在询问
     *
     * @param deniedPermission
     */
    void onDeniedForever(List<String> deniedPermission);
}