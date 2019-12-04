package com.app.squirrel.facedetect;

import android.graphics.Bitmap;

import com.app.squirrel.activity.LoginActivity;
import com.app.squirrel.facedetect.entry.FaceppBean;

import java.io.File;
import java.util.List;

/**
 * @author chaochaowu
 */
public interface MainContract {

    interface View {

        void showProgress();

        void hideProgress();

        void displayPhoto(Bitmap photo);

        void displayFaceInfo(List<FaceppBean.FacesBean> faces);

        void finshActivity();

    }

    interface Presenter {
        void getDetectResultFromServer(Bitmap photo);
    }
}
