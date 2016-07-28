package com.diyin.Voltga;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.diyin.Voltga.adapter.PlaceAdapter;
import com.diyin.Voltga.api.API_Manager;
import com.diyin.Voltga.data.PlaceObj;
import com.diyin.Voltga.utils.CommonUtils;
import com.diyin.Voltga.widget.SearchPanel;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Timer;

public class PlaceActivity extends Activity implements SearchPanel.SearchListener,
        AdapterView.OnItemClickListener {

    private static final String TAG = SearchPanel.class.getSimpleName();

    private PullToRefreshListView mPullRefreshListView;
    private PlaceAdapter mAdapter;
    private SearchPanel mSearchPanel;

    public Timer mFakeTimer;

    private ProgressDialog mProgressDialog;

    private ArrayList<PlaceObj> mPlaceList = new ArrayList<PlaceObj>();

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place);

        // AdMob
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        // init view
        mSearchPanel = (SearchPanel) findViewById(R.id.search_panel);
        mSearchPanel.setSearchListener(this);

        // init list
        mPullRefreshListView = (PullToRefreshListView) findViewById(R.id.pull_refresh_list);

        for (PlaceObj item : CommonUtils.mPlaceList) {
            mPlaceList.add(item);
        }

        mAdapter = new PlaceAdapter(PlaceActivity.this, mPlaceList);
        mPullRefreshListView.setAdapter(mAdapter);

        mPullRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {

            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                getPlaceInfo();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
            }
        });
        mPullRefreshListView.setOnItemClickListener(this);
    }

    @Override
    protected void onDestroy() {
        mPullRefreshListView.setAdapter(null);
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (CommonUtils.mPlaceList.size() == 0) {
            mProgressDialog = ProgressDialog.show(PlaceActivity.this, "", "Loading...");
            getPlaceInfo();
        } else {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        return super.onCreateView(name, context, attrs);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        int index = position - 1;
        PlaceObj placeObj = mPlaceList.get(index);

        CommonUtils.mCurrentPlace = placeObj;
        mPlaceList.remove(index);
        mPlaceList.add(0, placeObj);

        if (!CommonUtils.mSelfUser.user_place_id.equals(CommonUtils.mCurrentPlace.place_id)) {
            // place has changed
            CommonUtils.mnCurrentChatBaseNo = -1;

            mProgressDialog = ProgressDialog.show(this, "", "Loading...");
            API_Manager.getInstance().setCurrentPlaceWithUserID(
                    CommonUtils.mSelfUser.user_id.toString(),
                    CommonUtils.mCurrentPlace.place_id.toString(),
                    new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            mProgressDialog.dismiss();

                            try {
                                String strRes = response.getString(API_Manager.WEBAPI_RETURN_RESULT);

                                if (strRes.equals(API_Manager.WEBAPI_RETURN_SUCCESS)) {
                                    CommonUtils.mbChangedPlace = true;
                                    CommonUtils.mbChangedPlaceNotification = true;
                                    CommonUtils.mbChangedPlaceChat = true;

                                    CommonUtils.mSelfUser.user_place_id = CommonUtils.mCurrentPlace.place_id;
                                    //mAdapter.notifyDataSetChanged();

                                    getPlaceInfo();
                                    CommonUtils.mHomeActivity.setHomeCurrentTab(1);
                                } else {
                                    CommonUtils.createErrorAlertDialog(PlaceActivity.this, "Error", response.getString(API_Manager.WEBAPI_RETURN_MESSAGE)).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            super.onSuccess(statusCode, headers, response);
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            super.onFailure(statusCode, headers, throwable, errorResponse);

                            mProgressDialog.dismiss();

                            CommonUtils.createErrorAlertDialog(PlaceActivity.this, "Error", throwable.getMessage()).show();
                        }
                    }
            );
        } else {
            CommonUtils.mHomeActivity.setHomeCurrentTab(1);
        }
    }

    private void getPlaceInfo() {
        /* GET ALL PLACE DATA */
        API_Manager.getInstance().getPlaces(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                if (mProgressDialog != null) {
                    mProgressDialog.dismiss();
                }

                mPullRefreshListView.onRefreshComplete();

                try {
                    String strRes = response.getString(API_Manager.WEBAPI_RETURN_RESULT);

                    if (strRes.equals(API_Manager.WEBAPI_RETURN_SUCCESS)) {
                        JSONArray placeArray = response.getJSONObject(API_Manager.WEBAPI_RETURN_VALUES).getJSONArray("places");

                        CommonUtils.mPlaceList.clear();
                        mPlaceList.clear();

                        for (int i = 0; i < placeArray.length(); i++) {
                            JSONObject jsonObject = placeArray.getJSONObject(i);
                            PlaceObj pObj = new PlaceObj(jsonObject);

                            if (pObj.place_id.equals(CommonUtils.mSelfUser.user_place_id)) {
                                CommonUtils.mPlaceList.add(0, pObj);
                                mPlaceList.add(0, pObj);
                            } else {
                                CommonUtils.mPlaceList.add(pObj);
                                mPlaceList.add(pObj);
                            }
                        }

                        mAdapter.notifyDataSetChanged();
                    } else {
                        CommonUtils.createErrorAlertDialog(PlaceActivity.this, "Error", response.getString(API_Manager.WEBAPI_RETURN_MESSAGE)).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                super.onSuccess(statusCode, headers, response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);

                mPullRefreshListView.onRefreshComplete();

                if (mProgressDialog != null) {
                    mProgressDialog.dismiss();
                }
                CommonUtils.createErrorAlertDialog(PlaceActivity.this, "Error", throwable.getMessage()).show();
            }
        });
    }

    /*
    Search Panel
     */
    @Override
    public void onAutoSuggestion(String query) {
        mPlaceList.clear();

        query = query.toLowerCase();

        for (PlaceObj pObj : CommonUtils.mPlaceList) {
            if (pObj.place_name.toLowerCase().contains(query))
                mPlaceList.add(pObj);
        }

        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClickSearchResult(String query) {
        mPullRefreshListView.requestFocus();
    }

    @Override
    public void onItemClick(int position) {

    }

    @Override
    public void onClear() {
        mPlaceList.clear();

        for (PlaceObj placeObj : CommonUtils.mPlaceList)
            mPlaceList.add(placeObj);

        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onSearchAll() {

    }

}
