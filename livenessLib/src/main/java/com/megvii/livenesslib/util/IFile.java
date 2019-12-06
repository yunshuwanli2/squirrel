package com.megvii.livenesslib.util;

import android.text.TextUtils;

import com.megvii.livenessdetection.DetectionFrame;
import com.megvii.livenessdetection.Detector;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 文件工具类
 */
public class IFile {

    public IFile() {
    }

    /**
     * 把图片保存到文件夹
     */
    public boolean save(Detector mDetector, String session,
                        JSONObject jsonObject) {
        List<DetectionFrame> frames = mDetector.getValidFrame();
        if (frames.size() == 0) {
            return false;
        }

        try {
            String dirPath = Constant.dirName + "/" + session;
            File dir = new File(dirPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            for (int i = 0; i < frames.size(); i++) {
                File file = new File(dir, session + "-" + i + ".jpg");
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                fileOutputStream.write(frames.get(i).getCroppedFaceImageData());
                JSONArray jsonArray = jsonObject.getJSONArray("imgs");
                jsonArray.put(file.getAbsoluteFile());
                fileOutputStream.flush();
                fileOutputStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 把活体图片保存到文件夹
     */
    public static boolean saveAuthLiveImg(Map<String, byte[]> images) {
        if (images.size() == 0) return false;
        try {

            int i = 0;
            for (String key : images.keySet()) {
                byte[] data = images.get(key);
                if (key.equals("image_best")) {
//                    byte2File(Constant.LIVE_BEST_IMG_PATH, data);
                } else if (key.equals("image_env")) {//全景
//                    byte2File(Constant.LIVE_FULL_IMG_PATH, data);
                } else {//动作图
                    i++;
//                    byte2File(Constant.LIVE_ACTION_IMG_PATH + i + ".jpg", data);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 把LOG保存到本地
     */
    public static boolean saveLog(String session, String name) {
        try {
            String dirPath = Constant.dirName + "/" + session;
            File dir = new File(dirPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = new File(dir, "Log.txt");
            FileOutputStream fileOutputStream = new FileOutputStream(file, true);
            String str = "\n" + session + ",  " + name;
            fileOutputStream.write(str.getBytes());
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static File byte2File(String fileName, byte[] bytes) {
        if (bytes == null||bytes.length == 0 || TextUtils.isEmpty(fileName)) return null;

        File file = new File(fileName);
        if (file.exists()) file.delete();

        FileOutputStream fileout = null;
        try {
            fileout = new FileOutputStream(file);
            fileout.write(bytes, 0, bytes.length);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileout != null) fileout.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;

    }
    public static boolean fileDelete(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return false;
        }
        return file.delete();
    }
}
