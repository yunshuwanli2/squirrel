package com.app.squirrel.jpush;

import com.google.gson.Gson;

public class BasePushBean {

    public String  type;
    public String  msg;

    public static BasePushBean getBasePush(String json){
        return new Gson().fromJson(json,BasePushBean.class);
    }
}
