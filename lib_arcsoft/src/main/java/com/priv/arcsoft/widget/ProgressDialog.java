package com.priv.arcsoft.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.priv.arcsoft.R;


public class ProgressDialog extends AlertDialog {
    private ProgressBar progressBar;
    private TextView tvProgress;
    private int maxProgress = 100;

    public ProgressDialog(@NonNull Context context) {
        super(context);
        progressBar = (ProgressBar) LayoutInflater.from(context).inflate(R.layout.dialog_horizontal_progress_bar, null);
        progressBar.setMax(maxProgress);
        tvProgress = new TextView(context);
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(progressBar);
        linearLayout.addView(tvProgress);
        setView(linearLayout, 50, 50, 50, 50);
        setCanceledOnTouchOutside(false);
        setTitle("please wait...");
    }


    public void setMaxProgress(int max) {
        if (max > 0) {
            this.maxProgress = max;
            if (progressBar != null) {
                progressBar.setMax(max);
            }
        }
    }


}
