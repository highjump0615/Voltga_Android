package com.diyin.Voltga;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;

public class PhotoFragmentAdapter extends FragmentPagerAdapter {

    public ArrayList<String> mUrlList;
    private Activity mParentActivity;

    public PhotoFragmentAdapter(Activity activity, FragmentManager fm) {
        super(fm);

        mParentActivity = activity;
    }

    @Override
    public Fragment getItem(int i) {
        return PhotoFragment.newInstance(mParentActivity, mUrlList.get(i));
    }

    @Override
    public int getCount() {
        return mUrlList.size();
    }
}
