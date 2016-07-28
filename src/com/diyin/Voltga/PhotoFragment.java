package com.diyin.Voltga;

import android.app.Activity;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.FloatMath;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.diyin.Voltga.utils.CommonUtils;
import com.diyin.Voltga.widget.TouchImageView;
import com.nostra13.universalimageloader.core.ImageLoader;

public class PhotoFragment extends Fragment implements View.OnClickListener {

    private String mstrUrl;
    private Activity mParentActivity;

    public static PhotoFragment newInstance(Activity activity, String url) {
        PhotoFragment fragment = new PhotoFragment();
        fragment.mstrUrl = url;
        fragment.mParentActivity = activity;

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        TouchImageView imageView = new TouchImageView(getActivity());
        imageView.setAdjustViewBounds(true);
        //imageView.setScaleType(ImageView.ScaleType.MATRIX);
        imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        RelativeLayout layout = new RelativeLayout(getActivity());
        layout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        layout.addView(imageView);

        ImageLoader.getInstance().displayImage(mstrUrl, imageView, CommonUtils.mImgOptions);

        imageView.setOnClickListener(this);

        return layout;
    }

    @Override
    public void onClick(View view) {
        mParentActivity.onBackPressed();
    }

}
