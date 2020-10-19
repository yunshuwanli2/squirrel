package com.priv.yswl.base.tool;

import com.hjq.toast.ToastUtils;


public class ToastUtil {

    private static long lastClickTime;

    // 防止连续点击按钮
    public synchronized static boolean isFastClick() {
        long time = System.currentTimeMillis();
        if (time - lastClickTime < 1900) {
            return true;
        }
        lastClickTime = time;
        return false;
    }


    public static void showToast(String str) {
        ToastUtils.show(str);
    }

    public static void showToast(int resId) {
        ToastUtils.show(resId);
    }

}
