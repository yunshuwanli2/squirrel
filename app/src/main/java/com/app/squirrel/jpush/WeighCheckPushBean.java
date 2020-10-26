package com.app.squirrel.jpush;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

public class WeighCheckPushBean extends BasePushBean{

    /**
     * data : {"number":""} //WeightCheck
     */

    private DataBean data;

    public static WeighCheckPushBean.DataBean getWeighCheckInfo(String json){
        JSONObject jsonObject=null;
        try {
            jsonObject =  new JSONObject(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(jsonObject ==null)return null;
        String json2 = jsonObject.optString("data");
        return new Gson().fromJson(json2, WeighCheckPushBean.DataBean.class);
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * number :"",
         * weight: ""
         */

        private String number;
        private String weight;

        public String getWeight() {
            return weight;
        }

        public void setWeight(String weight) {
            this.weight = weight;
        }

        public String getNumber() {
            return number;
        }

        public void setNumber(String number) {
            this.number = number;
        }
    }
}
