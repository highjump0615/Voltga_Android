package com.diyin.Voltga.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.diyin.Voltga.CurrentActivity;
import com.diyin.Voltga.R;
import com.diyin.Voltga.adapter.NotifyAdapter;
import com.diyin.Voltga.api.API_Manager;
import com.diyin.Voltga.data.NotificationObj;
import com.diyin.Voltga.utils.CommonUtils;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class NotifyFragment extends Fragment {

    private CurrentActivity mActivity;

    private NotifyAdapter mAdapter;
    private ArrayList<NotificationObj> mNotifyList = new ArrayList<NotificationObj>();

    PullToRefreshListView mPullRefreshListView;

    private ProgressDialog mProgressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_notify, container, false);

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (CurrentActivity) activity;
        mActivity.mNotifyFragment = this;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mPullRefreshListView = (PullToRefreshListView) view.findViewById(R.id.pull_refresh_list);

        mAdapter = new NotifyAdapter(view.getContext(), mNotifyList);
        mPullRefreshListView.setAdapter(mAdapter);

        getNotifyInfo();

        mPullRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {

            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                getNotifyInfo();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {

            }
        });
    }

    public void onChangedPlace() {
        mNotifyList.clear();
        mAdapter.notifyDataSetChanged();
        getNotifyInfo();
    }

    private void getNotifyInfo() {
        API_Manager.getInstance().getNotificationsWithUserID(
                CommonUtils.mSelfUser.user_id.toString(),
                CommonUtils.mSelfUser.user_place_id.toString(),

                new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        if (mProgressDialog != null) {
                            mProgressDialog.dismiss();
                        }

                        mPullRefreshListView.onRefreshComplete();

                        try {
                            String strRes = response.getString(API_Manager.WEBAPI_RETURN_RESULT);

                            if (strRes.equals(API_Manager.WEBAPI_RETURN_SUCCESS)) {
                                CommonUtils.mbChangedPlaceNotification = false;

                                JSONArray notifyArray = response.getJSONArray(API_Manager.WEBAPI_RETURN_VALUES);

                                mNotifyList.clear();

                                for (int i = 0; i < notifyArray.length(); i++) {
                                    JSONObject jsonObject = notifyArray.getJSONObject(i);
                                    NotificationObj pObj = new NotificationObj(jsonObject);

                                    if (pObj.notification_type > 0)
                                        mNotifyList.add(pObj);
                                }

                                mAdapter.notifyDataSetChanged();
                            } else {
                                CommonUtils.createErrorAlertDialog(mActivity, "Error", response.getString(API_Manager.WEBAPI_RETURN_MESSAGE)).show();
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
                        CommonUtils.createErrorAlertDialog(mActivity, "Error", throwable.getMessage()).show();
                    }
                }
        );
    }
}
