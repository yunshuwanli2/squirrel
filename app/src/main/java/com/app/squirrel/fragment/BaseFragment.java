package com.app.squirrel.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import org.greenrobot.eventbus.EventBus;

public class BaseFragment extends Fragment {

    protected Context mContext;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mContext = null;
    }

    @Override
    public void onStart() {
        if (getEventBusSetting()) {
            if (!EventBus.getDefault().isRegistered(this)) {
                EventBus.getDefault().register(this);
            }
        }
        super.onStart();
    }

    @Override
    public void onStop() {
        if (getEventBusSetting()) {
            if (EventBus.getDefault().isRegistered(this))
                EventBus.getDefault().unregister(this);
        }
        super.onStop();
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }


    public boolean getEventBusSetting() {
        return false;
    }

}
