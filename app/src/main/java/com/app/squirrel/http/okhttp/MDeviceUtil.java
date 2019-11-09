package com.app.squirrel.http.okhttp;

import android.app.UiModeManager;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.text.TextUtils;


import com.app.squirrel.application.MApplication;

import static android.content.Context.UI_MODE_SERVICE;

public class MDeviceUtil {

    public static String getDeviceInfo(Context context) {
        if (context == null) return null;
        String device_id = null;
        try {

            android.telephony.TelephonyManager tm = (android.telephony.TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);
            device_id = tm.getDeviceId();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            android.net.wifi.WifiManager wifi = (android.net.wifi.WifiManager) context
                    .getSystemService(Context.WIFI_SERVICE);
            String mac = wifi.getConnectionInfo().getMacAddress();

            if (TextUtils.isEmpty(device_id)) {
                device_id = mac;
            }
            if (TextUtils.isEmpty(device_id)) {
                device_id = android.provider.Settings.Secure.getString(
                        context.getContentResolver(),
                        android.provider.Settings.Secure.ANDROID_ID);
            }
            return device_id;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    private static String MAC = null;

    public static String getMAC() {
        if (MAC == null)
            MAC = MDeviceUtil.getDeviceInfo(MApplication.getApplication());
        return MAC;
    }

    public static String getDeviceInfo() {
        String systemInfo = System.getProperty("http.agent")
                + Build.MODEL + Build.VERSION.RELEASE;
        return systemInfo + MScreenUtils.getScreenInfo(MApplication.getApplication());
    }

    /**
     * 设备是tv
     * @param context
     * @return
     */
    public static boolean isTvOrMoble(Context context) {
        UiModeManager uiModeManager = (UiModeManager) context.getSystemService(UI_MODE_SERVICE);
        return uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION;
    }

}
