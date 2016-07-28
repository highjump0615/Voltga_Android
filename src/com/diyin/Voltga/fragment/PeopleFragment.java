package com.diyin.Voltga.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.diyin.Voltga.CurrentActivity;
import com.diyin.Voltga.R;
import com.diyin.Voltga.UserActivity;
import com.diyin.Voltga.adapter.PeopleAdapter;
import com.diyin.Voltga.api.API_Manager;
import com.diyin.Voltga.data.UserObj;
import com.diyin.Voltga.utils.CommonUtils;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshGridView;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Timer;

public class PeopleFragment extends Fragment {

    private static final String TAG = PeopleFragment.class.getSimpleName();

    private CurrentActivity mActivity;
    PullToRefreshGridView mPullRefreshGridView;

    private PeopleAdapter mAdapter;
    private ProgressDialog mProgressDialog;

    public Timer mFakeTimer;
    private ArrayList<UserObj> mPeopleList = new ArrayList<UserObj>();

    private boolean bInProgress = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_people, container, false);

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (CurrentActivity) activity;
        mActivity.mPeopleFragment = this;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mPullRefreshGridView = (PullToRefreshGridView) view.findViewById(R.id.grid_people);

        mAdapter = new PeopleAdapter(mActivity, mPeopleList);
        mPullRefreshGridView.setAdapter(mAdapter);

        getPeopleInfo(true);

        mPullRefreshGridView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<GridView>() {

            @Override
            public void onPullDownToRefresh(PullToRefreshBase<GridView> refreshView) {
                getPeopleInfo(false);
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<GridView> refreshView) {

            }
        });

        mPullRefreshGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                UserObj userObj = mPeopleList.get(i);

                CommonUtils.mSelectedUser = userObj;
                CommonUtils.moveNextActivity(CommonUtils.mHomeActivity, UserActivity.class, false);
            }
        });

        mPullRefreshGridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

                String strText = mActivity.mChatFragment.mEditText.getText().toString() + "@" + mPeopleList.get(i).user_name + " ";
                mActivity.mChatFragment.mEditText.setText(strText);
                mActivity.setSegmentIndex(mActivity.CURRENT_CHAT);

                return true;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        getPeopleInfo(false);
    }

    public void onChangedPlace() {
        mPeopleList.clear();
        mAdapter.notifyDataSetChanged();
        getPeopleInfo(true);
    }

    private void getPeopleInfo(boolean bShowProgress) {

        if (bInProgress) {
            return;
        }
        bInProgress = true;

//        if (bShowLoading) {
        // get Place data
        if (bShowProgress && mPeopleList.size() == 0) {
            mProgressDialog = ProgressDialog.show(mActivity, "", "Loading...");
        }
//        }

        API_Manager.getInstance().getPeopleWithUserID(
                CommonUtils.mSelfUser.user_id.toString(),
                CommonUtils.mSelfUser.user_place_id.toString(),
                new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        if (mProgressDialog != null) {
                            mProgressDialog.dismiss();
                        }

                        mPullRefreshGridView.onRefreshComplete();

                        try {
                            String strRes = response.getString(API_Manager.WEBAPI_RETURN_RESULT);

                            if (strRes.equals(API_Manager.WEBAPI_RETURN_SUCCESS)) {
                                CommonUtils.mbChangedPlace = false;

                                JSONArray peopleArray = response.getJSONObject(API_Manager.WEBAPI_RETURN_VALUES).getJSONArray("users");

                                mPeopleList.clear();

                                for (int i = 0; i < peopleArray.length(); i++) {
                                    JSONObject jsonObject = peopleArray.getJSONObject(i);
                                    UserObj pObj = new UserObj(jsonObject);

                                    if (pObj.user_id.equals(CommonUtils.mSelfUser.user_id))
                                        mPeopleList.add(0, pObj);
                                    else
                                        mPeopleList.add(pObj);
                                }

                                mAdapter.notifyDataSetChanged();
                            } else {
                                CommonUtils.createErrorAlertDialog(mActivity, "Error", response.getString(API_Manager.WEBAPI_RETURN_MESSAGE)).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        bInProgress = false;

                        super.onSuccess(statusCode, headers, response);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        super.onFailure(statusCode, headers, throwable, errorResponse);

                        mPullRefreshGridView.onRefreshComplete();

                        if (mProgressDialog != null) {
                            mProgressDialog.dismiss();
                        }

                        CommonUtils.createErrorAlertDialog(mActivity, "Error", throwable.getMessage()).show();
                        bInProgress = false;
                    }
                }
        );
    }
}
