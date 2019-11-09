package com.app.squirrel.tool;

import com.app.squirrel.application.MApplication;

/**
 * Created by kangpAdministrator on 2017/6/7 0007.
 * Emial kangpeng@yunhetong.net
 */

public class L {
    private L() {
        /* cannot be instantiated */
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    /**
     * 打包注意
     * SessionAjaxCallback  agent svn version
     * LxUrl  地址
     * BaseCasAuthHandler  cas 地址
     * SVNCODE
     */
    public static final boolean DEGUG = MApplication.getApplication().getDebugSetting();

    public static void d(String tag, String msg) {
        if (DEGUG) android.util.Log.d(tag, msg);
    }
    public static void v(String tag, String msg) {
        if (DEGUG) android.util.Log.v(tag, msg);
    }
    public static void e(String tag, String msg) {
        if (DEGUG) {
            android.util.Log.e(tag, msg);
        }
    }


    public static void i(String tag, String msg) {
        if (DEGUG) android.util.Log.i(tag, msg);
    }


}
