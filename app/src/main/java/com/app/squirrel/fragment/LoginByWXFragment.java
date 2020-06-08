package com.app.squirrel.fragment;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.app.squirrel.R;
import com.app.squirrel.activity.FaceLoginPreActivity;
import com.app.squirrel.facedetect.FaceDetectActivity;
import com.priv.yswl.base.BaseFragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class LoginByWXFragment extends BaseFragment implements View.OnClickListener {


    public LoginByWXFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login1, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.ll_face_login).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        FaceLoginPreActivity.JumpAct(getActivity());
    }
}
