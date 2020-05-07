package com.app.squirrel.fragment;


import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatEditText;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.app.squirrel.R;
import com.app.squirrel.tool.UserManager;
import com.priv.yswl.base.BaseFragment;
import com.priv.yswl.base.network.CallBack.HttpCallback;
import com.priv.yswl.base.network.HttpClientProxy;
import com.priv.yswl.base.tool.L;
import com.priv.yswl.base.tool.MKeyBoardUtils;
import com.priv.yswl.base.tool.MSPUtils;
import com.priv.yswl.base.tool.ToastUtil;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * A simple {@link Fragment} subclass.
 */
public class Login3Fragment extends BaseFragment implements View.OnClickListener, HttpCallback<JSONObject> {

    private static final String TAG = "Login2Fragment";

    public Login3Fragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login3, container, false);
    }

    AppCompatEditText editTextNumb;
    ImageView ivAgree;
    boolean isAgree;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        editTextNumb = view.findViewById(R.id.et_number);
        ivAgree = view.findViewById(R.id.iv_check);

        view.findViewById(R.id.ll_agree_rule).setOnClickListener(this);
        view.findViewById(R.id.tv_login).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_login:
                login();
                MKeyBoardUtils.hideSoftKeyboard(getActivity());
                break;
            case R.id.ll_agree_rule:
                isAgree = !isAgree;
                int id = isAgree ? R.mipmap.ic_checked : R.mipmap.ic_check_normol;
                ivAgree.setImageResource(id);
                break;
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
        int isFace = 0;
        String code = result.optString("code");
        if (code.equals("0")) {
            JSONObject data = result.optJSONObject("data");
            token = data.optString("token");
            isFace = data.optInt("isFace");
        }
        if (!TextUtils.isEmpty(token)) {
            L.d(TAG, "获取token成功，postSticky");
            UserManager.login(token,isFace);
        } else {
            L.d(TAG, "获取token失败");
            ToastUtil.showToast(result.optString("msg"));
        }

    }

    @Override
    public void onFail(int requestId, String errorMsg) {
        L.e(TAG, "[onFail]" + errorMsg);
        ToastUtil.showToast("请求失败，请检查网络连接");
    }
}
