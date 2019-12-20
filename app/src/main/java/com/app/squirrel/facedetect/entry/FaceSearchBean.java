package com.app.squirrel.facedetect.entry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.priv.yswl.base.tool.DateJsonDeserializer;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;

public class FaceSearchBean {
    public static FaceSearchBean jsonToBean(String jsonObject) {
        if (jsonObject == null) return null;
        Gson gson = new GsonBuilder().registerTypeAdapter(Date.class,
                new DateJsonDeserializer()).create();
        Type listum = new TypeToken<FaceSearchBean>() {
        }.getType();
        FaceSearchBean result = gson.fromJson(jsonObject, listum);
        return result;
    }

    /**
     * request_id : 1470481443,0d749845-7153-4f5e-a996-ffc5a1ac0a79
     * time_used : 1126
     * thresholds : {"1e-3":65.3,"1e-5":76.5,"1e-4":71.8}
     * results : [{"confidence":96.46,"user_id":"234723hgfd","face_token":"4dc8ba0650405fa7a4a5b0b5cb937f0b"}]
     */

    private String request_id;
    private int time_used;
    private ThresholdsBean thresholds;
    private List<ResultsBean> results;

    public String getRequest_id() {
        return request_id;
    }

    public void setRequest_id(String request_id) {
        this.request_id = request_id;
    }

    public int getTime_used() {
        return time_used;
    }

    public void setTime_used(int time_used) {
        this.time_used = time_used;
    }

    public ThresholdsBean getThresholds() {
        return thresholds;
    }

    public void setThresholds(ThresholdsBean thresholds) {
        this.thresholds = thresholds;
    }

    public List<ResultsBean> getResults() {
        return results;
    }

    public void setResults(List<ResultsBean> results) {
        this.results = results;
    }

    public static class ThresholdsBean {
        /**
         * 1e-3 : 65.3
         * 1e-5 : 76.5
         * 1e-4 : 71.8
         */

        @SerializedName("1e-3")
        private double _$1e3;
        @SerializedName("1e-5")
        private double _$1e5;
        @SerializedName("1e-4")
        private double _$1e4;

        public double get_$1e3() {
            return _$1e3;
        }

        public void set_$1e3(double _$1e3) {
            this._$1e3 = _$1e3;
        }

        public double get_$1e5() {
            return _$1e5;
        }

        public void set_$1e5(double _$1e5) {
            this._$1e5 = _$1e5;
        }

        public double get_$1e4() {
            return _$1e4;
        }

        public void set_$1e4(double _$1e4) {
            this._$1e4 = _$1e4;
        }
    }

    public static class ResultsBean {
        /**
         * confidence : 96.46
         * user_id : 234723hgfd
         * face_token : 4dc8ba0650405fa7a4a5b0b5cb937f0b
         */

        private double confidence;
        private String user_id;
        private String face_token;

        public double getConfidence() {
            return confidence;
        }

        public void setConfidence(double confidence) {
            this.confidence = confidence;
        }

        public String getUser_id() {
            return user_id;
        }

        public void setUser_id(String user_id) {
            this.user_id = user_id;
        }

        public String getFace_token() {
            return face_token;
        }

        public void setFace_token(String face_token) {
            this.face_token = face_token;
        }
    }
}
