package com.app.squirrel.jpush;

import com.google.gson.Gson;
import com.priv.yswl.base.tool.GsonUtil;

import org.json.JSONException;
import org.json.JSONObject;

public class TimeSetPushBean {

    /**
     * data : {"number":"1,2,3,4","isOn":true,"times":"0930113018001200"}
     */

    private DataBean data;

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }
    public static DataBean getTimeInfo(String json){
        JSONObject jsonObject=null;
        try {
            jsonObject =  new JSONObject(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(jsonObject ==null)return null;
        String json2 = jsonObject.optString("data");
       return new Gson().fromJson(json2,DataBean.class);
    }

    public static class DataBean {
        /**
         * number : 1,2,3,4
         * isOn : true
         * times : 0930113018001200
         */

        private String number;
        private boolean isOn;
        private String times;

        public String getNumber() {
            return number;
        }

        public void setNumber(String number) {
            this.number = number;
        }

        public boolean isIsOn() {
            return isOn;
        }

        public void setIsOn(boolean isOn) {
            this.isOn = isOn;
        }

        public String getTimes() {
            return times;
        }

        public void setTimes(String times) {
            this.times = times;
        }
    }
}
