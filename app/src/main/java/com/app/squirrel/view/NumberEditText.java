package com.app.squirrel.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;

/**
 * 输入数字的键盘，会通过过滤OK键禁止软键盘的弹出
 */
public class NumberEditText extends android.support.v7.widget.AppCompatEditText implements View.OnKeyListener, View.OnTouchListener {
    public NumberEditText(Context context) {
        this(context, null);
    }

    public NumberEditText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NumberEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initType();
    }

    protected void initType() {
        this.setSelectAllOnFocus(true);
        this.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
        this.setOnTouchListener(this);
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_ENTER
                || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            return true;
        }
        return false;
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }
}
