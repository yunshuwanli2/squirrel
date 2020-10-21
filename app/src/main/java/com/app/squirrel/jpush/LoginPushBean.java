package com.app.squirrel.jpush;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginPushBean extends BasePushBean {

    /**
     *  public static ArrayList<JokeInfo> jsonToList(String json) {
     *         JSONArray objarray = null;
     *         try {
     *             objarray = new JSONArray(json);
     *         } catch (JSONException e) {
     *             return null;
     *         }
     *         Gson gson = new GsonBuilder().registerTypeAdapter(Date.class, new DateJsonDeserializer()).create();
     *         ArrayList<JokeInfo> list = gson.fromJson(objarray.toString(), new TypeToken<List<JokeInfo>>() {
     *         }.getType());
     *         return list;
     *     }
     *
     *
     *
     */

    /**
     * data : {"headImg":"","nickName":"","phone":"1871872831"}
     */

    private DataBean data;

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static DataBean getUserInfo(String json){
        JSONObject jsonObject=null;
        try {
             jsonObject =  new JSONObject(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(jsonObject ==null)return null;
        String json2 = jsonObject.optString("data");
        return new Gson().fromJson(json2, DataBean.class);
    }
    public static class DataBean {
        /**
         * headImg :
         * nickName :
         * phone : 1871872831
         */

        private String headImg;
        private String nickName;
        private String phone;
        private String token;
        private int isFace;

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public int getIsFace() {
            return isFace;
        }

        public void setIsFace(int isFace) {
            this.isFace = isFace;
        }

        public String getHeadImg() {
            return headImg;
        }

        public void setHeadImg(String headImg) {
            this.headImg = headImg;
        }

        public String getNickName() {
            return nickName;
        }

        public void setNickName(String nickName) {
            this.nickName = nickName;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }
    }
}
