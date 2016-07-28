package com.diyin.Voltga.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.diyin.Voltga.R;
import com.diyin.Voltga.api.API_Manager;
import com.diyin.Voltga.data.PlaceObj;
import com.diyin.Voltga.utils.CommonUtils;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class PlaceAdapter extends BaseAdapter {

    private static LayoutInflater mInflater = null;
    private ArrayList<PlaceObj> mPlaceList;

    private ProgressDialog mProgressDialog;

    private Context mContext;

    public PlaceAdapter(Context ctx, ArrayList<PlaceObj> values) {
        mContext = ctx;

        mInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mPlaceList = values;
    }

    @Override
    public int getCount() {
        return mPlaceList.size();
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

        final PlaceObj placeObj = mPlaceList.get(i);

        View vi = view;
        if (vi == null) {
            vi = mInflater.inflate(R.layout.layout_place_item, null);
        }

        TextView textName = (TextView) vi.findViewById(R.id.text_name);
        textName.setText(placeObj.place_name);
        TextView textAddress = (TextView) vi.findViewById(R.id.text_address);
        textAddress.setText(placeObj.place_street + " " + placeObj.place_city);

        ImageView imageView = (ImageView) vi.findViewById(R.id.img_place);
        imageView.setImageResource(R.drawable.img_bar_default);
        ImageLoader.getInstance().displayImage(placeObj.place_picture, imageView, CommonUtils.mImgOptions);

        ImageView imageThumb = (ImageView) vi.findViewById(R.id.image_thumb);

        if (CommonUtils.mSelfUser.user_place_id.equals(placeObj.place_id)) {
            CommonUtils.mCurrentPlace = placeObj;
            //vi.setBackgroundColor(mContext.getResources().getColor(R.color.tint_color));
            vi.setActivated(true);
        } else {
            //vi.setBackgroundResource(R.drawable.place_item_bg);
            vi.setActivated(false);
        }

        if (placeObj.place_user != null) {
            imageThumb.setVisibility(View.VISIBLE);
            ImageLoader.getInstance().displayImage(CommonUtils.getUserPhoto(placeObj.place_user.user_public_photo, false), imageThumb, CommonUtils.mImgOptions);
        } else {
            imageThumb.setVisibility(View.INVISIBLE);
        }

        return vi;
    }



}
