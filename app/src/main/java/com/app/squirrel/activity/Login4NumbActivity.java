package com.app.squirrel.activity;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.app.squirrel.R;
import com.app.squirrel.http.CallBack.HttpCallback;
import com.app.squirrel.http.HttpClientProxy;
import com.app.squirrel.http.okhttp.MSPUtils;
import com.app.squirrel.tool.L;
import com.app.squirrel.tool.ToastUtil;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class Login4NumbActivity extends BaseActivity implements View.OnClickListener, HttpCallback<JSONObject> {

    private static final String TAG = "Login4NumbActivity";

    public static void JumpAct(Activity context) {
        Intent intent = new Intent(context, Login4NumbActivity.class);
        context.startActivity(intent);
    }

    EditText editTextNumb;
    ImageView ivAgree;
    boolean isAgree;
    StringBuffer stringBuffer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login4_numb);
        stringBuffer = new StringBuffer();
        editTextNumb = findViewById(R.id.et_number);
        editTextNumb.setShowSoftInputOnFocus(false);
        ivAgree = findViewById(R.id.iv_check);
        findViewById(R.id.btn_0).setOnClickListener(this);
        findViewById(R.id.btn_1).setOnClickListener(this);
        findViewById(R.id.btn_2).setOnClickListener(this);
        findViewById(R.id.btn_3).setOnClickListener(this);
        findViewById(R.id.btn_4).setOnClickListener(this);
        findViewById(R.id.btn_5).setOnClickListener(this);
        findViewById(R.id.btn_6).setOnClickListener(this);
        findViewById(R.id.btn_7).setOnClickListener(this);
        findViewById(R.id.btn_8).setOnClickListener(this);
        findViewById(R.id.btn_9).setOnClickListener(this);
        findViewById(R.id.btn_delete_one).setOnClickListener(this);
        findViewById(R.id.btn_clean).setOnClickListener(this);
        findViewById(R.id.ll_agree_rule).setOnClickListener(this);
        findViewById(R.id.tv_login).setOnClickListener(this);
        findViewById(R.id.btn_switch).setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_0:
                append(0);
                break;
            case R.id.btn_1:
                append(1);
                break;
            case R.id.btn_2:
                append(2);
                break;
            case R.id.btn_3:
                append(3);
                break;
            case R.id.btn_4:
                append(4);
                break;
            case R.id.btn_5:
                append(5);
                break;
            case R.id.btn_6:
                append(6);
                break;
            case R.id.btn_7:
                append(7);
                break;
            case R.id.btn_8:
                append(8);
                break;
            case R.id.btn_9:
                append(9);
                break;
            case R.id.btn_delete_one:
                deleteOne();
                break;
            case R.id.btn_clean:
                clean();
                break;
            case R.id.tv_login:
                login();
                break;
            case R.id.btn_switch:
                LoginActivity.JumpAct(this);
                finish();
                break;
            case R.id.ll_agree_rule:
                isAgree = !isAgree;
                int id = isAgree ? R.mipmap.ic_checked : R.mipmap.ic_check_normol;
                ivAgree.setImageResource(id);
                break;
        }
    }

    private void setEditTextNumbText() {
        String curr = stringBuffer.toString();
        editTextNumb.setText(curr);

    }

    private void append(int numb) {
        if (stringBuffer.length() < 11) {
            stringBuffer.append(numb);
            setEditTextNumbText();
        }
    }

    private void clean() {
        stringBuffer.setLength(0);
        setEditTextNumbText();
    }

    private void deleteOne() {
        if (stringBuffer.length() - 1 > 0) {
            stringBuffer.deleteCharAt(stringBuffer.length() - 1);
            setEditTextNumbText();
        }
    }

    private void login() {
        String num = editTextNumb.getText().toString();
        if (!isAgree) {
            ToastUtil.showToast("请同意用户协议");
            return;
        }
        if (TextUtils.isEmpty(num)) {
            ToastUtil.showToast("手机号码有为空");
            return;
        }
        if (!isChinaPhoneLegal(num)) {
            ToastUtil.showToast("手机号码格式错误");
            return;
        }
        String url = "wxApi/auth";
        Map<String, Object> para = new HashMap<>();
        para.put("phone", num);
        HttpClientProxy.getInstance().getAsyn(url, 1, para, this);
    }


    public static boolean isChinaPhoneLegal(String str)
            throws PatternSyntaxException {
        String regExp = "^((13[0-9])|(15[^4])|(18[0-9])|(17[0-8])|(147,145))\\d{8}$";
        Pattern p = Pattern.compile(regExp);
        Matcher m = p.matcher(str);
        return m.matches();
    }


    @Override
    public void onSucceed(int requestId, JSONObject result) {
        L.e(TAG, "[onSucceed] result:" + result);

        String token = null;
        String code = result.optString("code");
        if (code.equals("0")) {
            JSONObject data = result.optJSONObject("data");
            token = data.optString("token");
        }
        if (!TextUtils.isEmpty(token)) {
            MSPUtils.clear(this);
            MSPUtils.put("token", token);
            finish();
        } else {
            L.e(TAG, "获取token失败");
            ToastUtil.showToast(result.optString("msg"));
        }

    }

    @Override
    public void onFail(int requestId, String errorMsg) {
        L.e(TAG, "[onFail]" + errorMsg);
        ToastUtil.showToast("请求失败，请检查网络连接");
    }
}
