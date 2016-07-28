package com.diyin.Voltga.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.diyin.Voltga.R;
import com.diyin.Voltga.data.UserObj;
import com.diyin.Voltga.utils.CommonUtils;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

public class PeopleAdapter extends BaseAdapter {

    private static final String TAG = PeopleAdapter.class.getSimpleName();

    private static LayoutInflater mInflater = null;
    private ArrayList<UserObj> mPeopleList;

    public PeopleAdapter(Context ctx, ArrayList<UserObj> values) {
        mInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mPeopleList = values;
    }

    @Override
    public int getCount() {
        return mPeopleList.size();
    }

    @Override
    public Object getItem(int i) {
        return i;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        UserObj userObj = mPeopleList.get(i);

        View vi = view;
        if (vi == null) {
            vi = mInflater.inflate(R.layout.layout_people_item, null);
        }

        ImageView imgPhoto = (ImageView) vi.findViewById(R.id.img_photo);
        ImageView imgOnline = (ImageView) vi.findViewById(R.id.img_online);

        ImageLoader.getInstance().displayImage(CommonUtils.getUserPhoto(userObj.user_public_photo, true), imgPhoto, CommonUtils.mImgOptions);

        if (userObj.user_is_active.equals(1)) {
            imgOnline.setVisibility(View.VISIBLE);
        } else {
            imgOnline.setVisibility(View.GONE);
        }

        return vi;
    }
}
