package com.app.squirrel.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.TextView;

import com.app.squirrel.R;
import com.app.squirrel.fragment.FindFragment;
import com.app.squirrel.fragment.HomeFragment;
import com.app.squirrel.fragment.MallFragment;
import com.app.squirrel.fragment.MineFragment;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 首页Activity
 */
public class MainActivity extends FragmentActivity implements View.OnClickListener {

    @BindView(R.id.tv_home)
    TextView tvHome;
    @BindView(R.id.tv_find)
    TextView tvFind;
    @BindView(R.id.tv_mall)
    TextView tvMall;
    @BindView(R.id.tv_mine)
    TextView tvMine;

    private Fragment homeFragment;//首页
    private Fragment findFragment;//发现
    private Fragment mallFragment;//商城
    private Fragment mineFragment;//我的

    private Fragment currentFragment;//当前Fragment
    private List<Fragment> fragmentList;//fragment集合

    @Override
    public void onAttachFragment(Fragment fragment) {
        //当前的界面的保存状态，只是从新让新的Fragment指向了原本未被销毁的fragment，它就是onAttach方法对应的Fragment对象
        if (homeFragment == null && fragment instanceof HomeFragment) {
            homeFragment = (HomeFragment) fragment;
        } else if (findFragment == null && fragment instanceof FindFragment) {
            findFragment = (FindFragment) fragment;
        } else if (mallFragment == null && fragment instanceof MallFragment) {
            mallFragment = (MallFragment) fragment;
        } else if (mineFragment == null && fragment instanceof MineFragment) {
            mineFragment = (MineFragment) fragment;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        fragmentList = new ArrayList<>();
        homeFragment = new HomeFragment();
        findFragment = new FindFragment();
        mallFragment = new MallFragment();
        mineFragment = new MineFragment();
        fragmentList.add(homeFragment);
        fragmentList.add(findFragment);
        fragmentList.add(mallFragment);
        fragmentList.add(mineFragment);
        switchFragment(fragmentList.get(0));
        tvHome.setOnClickListener(this);
        tvFind.setOnClickListener(this);
        tvMall.setOnClickListener(this);
        tvMine.setOnClickListener(this);
    }

    //Fragment切换
    private void switchFragment(Fragment targetFragment) {
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction();
        if (!targetFragment.isAdded()) {
            if (currentFragment != null) {
                transaction
                        .hide(currentFragment);
            }
            transaction.add(R.id.main_fl_content, targetFragment)
                    .commit();
        } else {
            transaction
                    .hide(currentFragment)
                    .show(targetFragment)
                    .commit();
        }
        currentFragment = targetFragment;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_home:
                switchFragment(fragmentList.get(0));
                break;
            case R.id.tv_find:
                switchFragment(fragmentList.get(1));
                break;
            case R.id.tv_mall:
                switchFragment(fragmentList.get(2));
                break;
            case R.id.tv_mine:
                switchFragment(fragmentList.get(3));
                break;
        }
    }
}
