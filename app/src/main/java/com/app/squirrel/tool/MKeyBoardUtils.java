package com.app.squirrel.tool;

import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class MKeyBoardUtils {
    public static void openKeybord(EditText mEditText, Context mContext) {
        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mEditText, InputMethodManager.RESULT_SHOWN);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    public static void closeKeybord(EditText mEditText, Context mContext) {
        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
    }

    /**
     * 实现文本复制功能
     * @param content
     */
    public static  void copy(String content, Context context){
        // 得到剪贴板管理器
        if(context!=null){
            ClipboardManager cmb = (ClipboardManager)context.getSystemService(Context.CLIPBOARD_SERVICE);
            cmb.setText(content.trim());
        }

    }

    public static void hideSoftKeyboard(Activity activity) {
        try {
            View view = activity.getCurrentFocus();
            if (null != view) {
                InputMethodManager inputMethodManager = (InputMethodManager) activity
                        .getSystemService(Activity.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
            }
        } catch (Exception e) {
        }

    }
}
