package com.diyin.Voltga;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.TypedValue;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.diyin.Voltga.api.API_Manager;
import com.diyin.Voltga.fragment.ChatFragment;
import com.diyin.Voltga.fragment.NotifyFragment;
import com.diyin.Voltga.fragment.PeopleFragment;
import com.diyin.Voltga.utils.CommonUtils;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import info.hoang8f.android.segmented.SegmentedGroup;

public class CurrentActivity extends FragmentActivity implements RadioGroup.OnCheckedChangeListener,
        CommonUtils.OnTabActivityResultListener {

    private static final String TAG = CurrentActivity.class.getSimpleName();

    private RelativeLayout mLayoutPeople;
    private RelativeLayout mLayoutChat;
    private RelativeLayout mLayoutNotify;

    public PeopleFragment mPeopleFragment = null;
    public ChatFragment mChatFragment = null;
    public NotifyFragment mNotifyFragment = null;

    private RadioButton mSegmentPeople;
    private RadioButton mSegmentChat;
    private RadioButton mSegmentNotify;

    private TextView mTextNotificationCount;

    public FragmentManager mFragmentManager;
    private FragmentTransaction mFragmentTransaction;

    private SegmentedGroup mSegmented;

    public int CURRENT_PEOPLE = R.id.btn_segment_people;
    public int CURRENT_CHAT = R.id.btn_segment_chat;
    public int CURRENT_NOTIFY = R.id.btn_segment_notify;

    public TextView mTextPlaceName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current);

        // AdMob
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        // init view
        //mTextPlaceName = (TextView) findViewById(R.id.text_placename);

        mSegmentPeople = (RadioButton) findViewById(R.id.btn_segment_people);
        mSegmentChat = (RadioButton) findViewById(R.id.btn_segment_chat);
        mSegmentNotify = (RadioButton) findViewById(R.id.btn_segment_notify);

        mLayoutPeople = (RelativeLayout) findViewById(R.id.layout_frg_people);
        mLayoutChat = (RelativeLayout) findViewById(R.id.layout_frg_chat);
        mLayoutNotify = (RelativeLayout) findViewById(R.id.layout_frg_notify);

        mTextNotificationCount = (TextView) findViewById(R.id.text_notification_count);
        mTextNotificationCount.setBackgroundDrawable(getDefaultBackground());

        mFragmentManager = getSupportFragmentManager();

        mSegmented = (SegmentedGroup) findViewById(R.id.segmented);
        mSegmented.setOnCheckedChangeListener(this);

        setPlaceName();
        CommonUtils.mHomeActivity.mCurrentActivity = this;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (CommonUtils.mCurrentSubActivity == this) {
            updateFragment();
        }

        /* Get GET BADGE BASED ON CURRENT PLACEID AND SELFUSER ID */
        API_Manager.getInstance().getBadgesWithUserID(
                String.valueOf(CommonUtils.mSelfUser.user_id),
                String.valueOf(CommonUtils.mSelfUser.user_place_id),
                new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        try {
                            String strRes = response.getString(API_Manager.WEBAPI_RETURN_RESULT);

                            if (strRes.equals(API_Manager.WEBAPI_RETURN_SUCCESS)) {
                                CommonUtils.mApplicationIconBadgeNumber = response.getInt("values");
                                setNotificationCount(CommonUtils.mApplicationIconBadgeNumber);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        super.onSuccess(statusCode, headers, response);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        super.onFailure(statusCode, headers, throwable, errorResponse);
                    }
                }
        );
    }

    public void updateFragment() {

        if (mSegmentPeople.isChecked()) {
            mLayoutPeople.setVisibility(View.VISIBLE);
            mLayoutChat.setVisibility(View.GONE);
            mLayoutNotify.setVisibility(View.GONE);

            if (mChatFragment != null)
                mChatFragment.stopGetMessage();

            if (mPeopleFragment != null)
                mPeopleFragment.onResume();

        } else if (mSegmentChat.isChecked()) {
            mLayoutPeople.setVisibility(View.GONE);
            mLayoutChat.setVisibility(View.VISIBLE);
            mLayoutNotify.setVisibility(View.GONE);

            if (mChatFragment != null) {
                mChatFragment.onResume();
                mChatFragment.startGetMessage();
            }

        } else if (mSegmentNotify.isChecked()) {
            mLayoutPeople.setVisibility(View.GONE);
            mLayoutChat.setVisibility(View.GONE);
            mLayoutNotify.setVisibility(View.VISIBLE);

            if (mChatFragment != null)
                mChatFragment.stopGetMessage();

            setNotificationCount(0);
        }
    }

    // TODO
    public void setNotificationCount(int count) {
        // Change notification count of SegmentControl
        if (count > 0) {
            mTextNotificationCount.setText(String.valueOf(count));
            mTextNotificationCount.setVisibility(View.VISIBLE);

            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mTextNotificationCount.getLayoutParams();
            if (mTextNotificationCount.getText().toString().length() > 2) {
                params.width = RelativeLayout.LayoutParams.WRAP_CONTENT;
            } else {
                params.width = (int) getResources().getDimension(R.dimen.current_segmented_badge_diameter);
            }

            mTextNotificationCount.setLayoutParams(params);
        } else {
            mTextNotificationCount.setVisibility(View.INVISIBLE);
        }

        // Change background of TabBar
        CommonUtils.mHomeActivity.changeTabBackgroundWithNotificationCount(count);

        // Send count to server
        API_Manager.getInstance().removeBadgesWithUserID(CommonUtils.mSelfUser.user_id.toString(),
                CommonUtils.mSelfUser.user_place_id.toString(),
                new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        super.onSuccess(statusCode, headers, response);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        super.onFailure(statusCode, headers, throwable, errorResponse);
                    }
                });
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {
        updateFragment();
    }

    public void setSegmentIndex(int nIndex) {
        mSegmented.check(nIndex);
    }

    public void setPlaceName() {
//        if (CommonUtils.mCurrentPlace != null) {
//            mTextPlaceName.setText(CommonUtils.mCurrentPlace.place_name);
//        }

        if (CommonUtils.mbChangedPlace) {
            mPeopleFragment.onChangedPlace();
        }

        if (CommonUtils.mbChangedPlaceNotification) {
            mNotifyFragment.onChangedPlace();
        }
    }

    @Override
    public void onTabActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == CommonUtils.GALLERY_OPEN_FOR_CHAT_REQUEST_CODE || requestCode == CommonUtils.CAMERA_OPEN_FOR_CHAT_REQUEST_CODE) {
            mChatFragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    private ShapeDrawable getDefaultBackground() {

        int r = dipToPixels(8);
        float[] outerR = new float[] {r, r, r, r, r, r, r, r};

        RoundRectShape rr = new RoundRectShape(outerR, null, null);
        ShapeDrawable drawable = new ShapeDrawable(rr);
        drawable.getPaint().setColor(getResources().getColor(R.color.badge_color));

        return drawable;
    }

    private int dipToPixels(int dip) {
        Resources r = getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, r.getDisplayMetrics());
        return (int) px;
    }

}
