package com.app.squirrel.activity;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.app.squirrel.R;
import com.app.squirrel.tool.UserManager;
import com.priv.arcsoft.ArcSoftFaceActivity;
import com.priv.yswl.base.BaseActivity;

public class FaceLoginPreActivity extends BaseActivity implements View.OnClickListener {
    public static void JumpAct(Activity context) {
        Intent intent = new Intent(context, FaceLoginPreActivity.class);
        context.startActivity(intent);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_face);

        findViewById(R.id.tv_back).setOnClickListener(this);
        findViewById(R.id.btn_face_login).setOnClickListener(this);
        ((Button)findViewById(R.id.btn_face_login)).performClick();
        finish();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.tv_back) {
            finish();
        } else if (view.getId() == R.id.btn_face_login) {
            ArcSoftFaceActivity.JumpAct(this, UserManager.isLogin(), UserManager.isFace());
        }
    }
}
