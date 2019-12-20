package com.priv.yswl.base.tool;

import android.content.Context;
import android.os.Environment;

import com.priv.yswl.base.MApplication;

import java.io.File;

import static android.os.Environment.DIRECTORY_DOWNLOADS;
import static android.os.Environment.DIRECTORY_MOVIES;
import static android.os.Environment.DIRECTORY_PICTURES;

public class FileSDCardUtil {
    public static FileSDCardUtil fileSDCardUtil;
    public static String DownAppDirs = "downAppDirs";//APP下载目录
    public static String ImagePicCacheDir = "/gxximages";//图片缓存目录
    public static String MusicCacheDir = "/gxxmusic";//音乐的缓存目录
    public static FileSDCardUtil getInstance() {
        if (fileSDCardUtil == null) {
            synchronized (FileSDCardUtil.class) {
                if (fileSDCardUtil == null) {
                    fileSDCardUtil = new FileSDCardUtil();
                }
            }
        }
        return fileSDCardUtil;
    }

    /**
     * 注释描述:返回下载的APP目录
     */
    public String saveDownAppToSDcarPrivateFiles(Context context) {
        String path = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {//此目录下的是外部存储下的files/downAppDirs目录
            File file = context.getExternalFilesDir(DownAppDirs);  //SDCard/Android/data/你的应用的包名/files/downAppDirs
            if (!file.exists()) {
                file.mkdirs();
            }
            path = file.getAbsolutePath();
        } else {
            path = context.getFilesDir().getAbsolutePath() + "/"+DownAppDirs;
            File file = new File(path);
            if (!file.exists()) {
                file.mkdirs();
            }
            path = file.getAbsolutePath();
        }
        return path;   //SDCard/Android/data/你的应用的包名/files/downAppDirs
    }


    public String saveToSDcarPrivateFiles(Context context,String fileName) {
        String path = null;
        //地址 /storage/emulated/0/Android/data/包名/files/fileName
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {//此目录下的是外部存储下的files/fileName目录
            File file = context.getExternalFilesDir(fileName);  //SDCard/Android/data/你的应用的包名/files/fileName
            if (!file.exists()) {
                file.mkdirs();
            }
            path = file.getAbsolutePath();
        } else {
            path = context.getFilesDir().getAbsolutePath() + "/"+fileName;
            File file = new File(path);
            if (!file.exists()) {
                file.mkdirs();
            }
            path = file.getAbsolutePath();
        }
        return path+"/";
    }

    /**
     * @Description: 创建保存图片的缓存目录
     */
    public String getDiskImagePicCacheDir() {
        return getDiskCacheDir(MApplication.getApplication(), ImagePicCacheDir);
    }

    /**
     * @description:获取音乐的缓存目录
     **/
    public String getDiskMusicCacheDir() {
        return getDiskCacheDir(MApplication.getApplication(), MusicCacheDir);
    }



    /**
     * 注释描述:获取缓存目录
     * @fileName 获取外部存储目录下缓存的 fileName的文件夹路径
     */
    public String getDiskCacheDir(Context context, String fileName) {
        String cachePath = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {//此目录下的是外部存储下的私有的fileName目录
            cachePath = context.getExternalCacheDir().getPath() + "/" + fileName;  //SDCard/Android/data/你的应用包名/cache/fileName
        } else {
            cachePath = context.getCacheDir().getPath()+ "/" + fileName;
        }
        File file = new File(cachePath);
        if (!file.exists()){
            file.mkdirs();
        }
        return file.getAbsolutePath(); //SDCard/Android/data/你的应用包名/cache/fileName/
    }

    /**
    * @description:获取外部存储目录下的 fileName的文件夹路径
    **/
    public String getDiskFileDir(Context context, String fileName){
        String cachePath = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {//此目录下的是外部存储下的私有的fileName目录
            cachePath = context.getExternalFilesDir(fileName).getAbsolutePath();  //mnt/sdcard/Android/data/com.my.app/files/fileName
        } else {
            cachePath = context.getFilesDir().getPath()+ "/" + fileName;        //data/data/com.my.app/files
        }
        File file = new File(cachePath);
        if (!file.exists()){
            file.mkdirs();
        }
        return file.getAbsolutePath();  //mnt/sdcard/Android/data/com.my.app/files/fileName
    }

    /**
     * 注释描述:从sdcard中删除文件
     */
    public boolean removeFileFromSDCard(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            try {
                file.delete();
                return true;
            } catch (Exception e) {
                return false;
            }
        } else {
            return false;
        }
    }


    /**
     * @description: 下载的图片保存的位置
     **/
    public String getDIRECTORY_PICTURESDir() {
        File file = new File(Environment.getExternalStoragePublicDirectory(DIRECTORY_PICTURES).getPath());// /storage/emulated/0/Pictures
        if (!file.exists()){
            file.mkdirs();
        }
        return file.getAbsolutePath();
    }


    /**
     * @description: 电影保存的位置
     **/
    public String getDIRECTORY_MOVIESDir() {
        File file = new File(Environment.getExternalStoragePublicDirectory(DIRECTORY_MOVIES).getPath());//   /storage/emulated/0/Movies
        if (!file.exists()){
            file.mkdirs();
        }
        return file.getAbsolutePath();
    }


    /**
     *创建时间:2019/7/21
     *注释描述://下载文件保存的位置
     */
    public String getDIRECTORY_DOWNLOADS(){
        File file = new File(Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS).getPath());//   /storage/emulated/0/downloads
        if (!file.exists()){
            file.mkdirs();
        }
        return file.getAbsolutePath();
    }

}