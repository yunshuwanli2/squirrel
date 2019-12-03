package com.app.squirrel.facedetect.entry;

import com.app.squirrel.tool.DateJsonDeserializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;

public class FacesetTokenBean {

    public static List<FacesetTokenBean> jsonToBeans(JSONArray jsonArray) {
        if (jsonArray == null) return null;
        Gson gson = new GsonBuilder().registerTypeAdapter(Date.class,
                new DateJsonDeserializer()).create();
        Type listum = new TypeToken<List<FacesetTokenBean>>() {
        }.getType();
        List<FacesetTokenBean> result = gson.fromJson(jsonArray.toString(), listum);
        return result;
    }
    /**
     * displayName : 测试物业
     * faceCount : 0
     * facesetToken : 794d089d4ef9dfc700d4f1b60aa125c8
     * outerId  : 101
     * tags :
     */

    private String displayName;
    private int faceCount;
    private String facesetToken;
    private String outerId;
    private String tags;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public int getFaceCount() {
        return faceCount;
    }

    public void setFaceCount(int faceCount) {
        this.faceCount = faceCount;
    }

    public String getFacesetToken() {
        return facesetToken;
    }

    public void setFacesetToken(String facesetToken) {
        this.facesetToken = facesetToken;
    }

    public String getOuterId() {
        return outerId;
    }

    public void setOuterId(String outerId) {
        this.outerId = outerId;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }
}
