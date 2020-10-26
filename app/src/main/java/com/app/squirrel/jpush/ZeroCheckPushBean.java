package com.app.squirrel.jpush;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

public class ZeroCheckPushBean extends BasePushBean{

    /**
     * data : {"number":""}
     */

    private DataBean data;
    public static ZeroCheckPushBean.DataBean getZeroCheckInfo(String json){
        JSONObject jsonObject=null;
        try {
            jsonObject =  new JSONObject(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(jsonObject ==null)return null;
        String json2 = jsonObject.optString("data");
        return new Gson().fromJson(json2, ZeroCheckPushBean.DataBean.class);
    }
    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * number :
         */

        private String number;

        public String getNumber() {
            return number;
        }

        public void setNumber(String number) {
            this.number = number;
        }
    }
}
