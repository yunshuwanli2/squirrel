package com.app.squirrel.activity;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.app.squirrel.R;

public class LoginActivity extends FragmentActivity implements View.OnClickListener {


    public static void JumpAct(Activity context) {
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        findViewById(R.id.btn_switch).setOnClickListener(this);
        findViewById(R.id.tv_back).setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_back) {
            finish();
        }
        if (v.getId() == R.id.btn_switch) {
            Login4NumbActivity.JumpAct(this);
            finish();
        }
    }
}
