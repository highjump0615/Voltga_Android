package com.diyin.Voltga;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;

import java.util.ArrayList;

public class PhotoActivity extends FragmentActivity {

    ViewPager mPager;
    PhotoFragmentAdapter mImageAdapter;

    public ArrayList<String> mUrlPhotoList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        mUrlPhotoList = (ArrayList<String>) getIntent().getSerializableExtra("urlList");
        int nCurrentIndex = getIntent().getIntExtra("currentIndex", 0);

        mImageAdapter = new PhotoFragmentAdapter(this, getSupportFragmentManager());
        mImageAdapter.mUrlList = mUrlPhotoList;

        mPager = (ViewPager) findViewById(R.id.pager_photo);
        mPager.setAdapter(mImageAdapter);
        mPager.setCurrentItem(nCurrentIndex);
    }
}
