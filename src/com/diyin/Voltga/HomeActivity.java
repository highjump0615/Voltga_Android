package com.diyin.Voltga;

import android.app.Activity;
import android.app.LocalActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.diyin.Voltga.api.API_Manager;
import com.diyin.Voltga.utils.CommonUtils;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

public class HomeActivity extends Activity {

    private static final String TAG = HomeActivity.class.getSimpleName();

    // LocationManager
    private LocalActivityManager mlam;
    private TabHost mTabHost;

    public CurrentActivity mCurrentActivity = null;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initLocation();

        Resources res = getResources(); // Resource object to get Drawables
        mTabHost = (TabHost) findViewById(R.id.tabhost); // The activity TabHost
        mlam = new LocalActivityManager(this, false);
        mlam.dispatchCreate(savedInstanceState);
        mTabHost.setup(mlam);

        TabHost.TabSpec spec; // Reusable TabSpec for each tab
        Intent intent; // Reusable Intent for each tab

        mTabHost.getTabWidget().setDividerDrawable(R.drawable.tab_normal_bg);

        // Create an Intent to launch an Activity for the tab (to be reused)
        intent = new Intent().setClass(this, PlaceActivity.class);
        spec = mTabHost.newTabSpec("places")
                .setIndicator(createTabView("places"))
                .setContent(intent);
        mTabHost.addTab(spec);

        // Do the same for the other tabs

        intent = new Intent().setClass(this, CurrentActivity.class);
        spec = mTabHost.newTabSpec("current")
                .setIndicator(createTabView("current"))
                .setContent(intent);
        mTabHost.addTab(spec);

        intent = new Intent().setClass(this, AccountActivity.class);
        spec = mTabHost
                .newTabSpec("account")
                .setIndicator(createTabView("me"))
                .setContent(intent);
        mTabHost.addTab(spec);

        //set tab which one you want open first time 0 or 1 or 2
        mTabHost.setCurrentTab(0);

        mTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                int i = mTabHost.getCurrentTab();

                if (mCurrentActivity != null) {
                    if (i == 1) {   // Current Activity
                        CommonUtils.mCurrentSubActivity = mCurrentActivity;
                        mCurrentActivity.setPlaceName();
                        mCurrentActivity.updateFragment();
                    } else {
                        CommonUtils.mCurrentSubActivity = null;
                        mCurrentActivity.mChatFragment.stopGetMessage();
                    }
                }
            }
        });

        CommonUtils.mHomeActivity = this;

        intent = new Intent();
        intent.setAction("com.android.home.action.UPDATE_BADGE");
        intent.putExtra("com.android.home.intent.extra.badge.ACTIVITY_NAME", getPackageName() + "." + HomeActivity.class.getSimpleName());
        intent.putExtra("com.android.home.intent.extra.badge.SHOW_MESSAGE", true);
        intent.putExtra("com.android.home.intent.extra.badge.MESSAGE", "99");
        intent.putExtra("com.android.home.intent.extra.badge.PACKAGE_NAME", getPackageName());

        sendBroadcast(intent);
    }

    private View createTabView(String text) {
        View view = LayoutInflater.from(this).inflate(R.layout.tab_button, null);
        TextView tv = (TextView) view.findViewById(R.id.textView);
        tv.setText(text);

        ImageView iv = (ImageView) view.findViewById(R.id.imageView);

        if (text.equals("places")) {
            iv.setImageResource(R.drawable.ic_tab_place);
        } else if (text.equals("current")) {
            iv.setImageResource(R.drawable.ic_tab_current);
        } else if (text.equals("me")) {
            iv.setImageResource(R.drawable.ic_tab_account);
        }

        return view;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (CommonUtils.mSelfUser == null) {
            CommonUtils.moveNextActivity(this, SplashActivity.class, true);
            return;
        }

        mlam.dispatchResume();

        CommonUtils.mbReadyToStop = true;
        CommonUtils.setOnlineState(1);

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
                                changeTabBackgroundWithNotificationCount(CommonUtils.mApplicationIconBadgeNumber);
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

    @Override
    protected void onPause() {
        super.onPause();
        mlam.dispatchPause(isFinishing());

        if (mCurrentActivity != null && mCurrentActivity.mChatFragment != null) {
            mCurrentActivity.mChatFragment.stopGetMessage();
        }

        if (CommonUtils.mbReadyToStop) {
            CommonUtils.setOnlineState(0);
        }
    }

    public void setHomeCurrentTab(int nIndex) {
        mTabHost.setCurrentTab(nIndex);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Activity subActivity = CommonUtils.mCurrentSubActivity;

        if (subActivity == null) {
            return;
        }

        if (subActivity instanceof CommonUtils.OnTabActivityResultListener) {
            CommonUtils.OnTabActivityResultListener listener = (CommonUtils.OnTabActivityResultListener) subActivity;
            listener.onTabActivityResult(requestCode, resultCode, data);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void initLocation() {
        LocationManager locationManager;
        String serviceName = Context.LOCATION_SERVICE;

        locationManager = (LocationManager) getSystemService(serviceName);

        boolean bGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean bNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        Location location = null;
        String strProvider = "";

        if (bGpsEnabled) {
            strProvider = LocationManager.GPS_PROVIDER;
            location = locationManager.getLastKnownLocation(strProvider);
        }

        if (location == null && bNetworkEnabled) {
            strProvider = LocationManager.NETWORK_PROVIDER;
            location = locationManager.getLastKnownLocation(strProvider);
        }

        updateNewLocation(location);

        if (strProvider.length() > 0) {
            locationManager.requestLocationUpdates(strProvider, 200000, 100, mLocationListener);
        }
    }

    public void changeTabBackgroundWithNotificationCount(int count) {
        View view = mTabHost.getTabWidget().getChildTabViewAt(1);
        ImageView iv = (ImageView) view.findViewById(R.id.imageView);

        if (count > 0) {
            iv.setImageResource(R.drawable.ic_tab_current_notify);
        } else {
            iv.setImageResource(R.drawable.ic_tab_current);
        }
    }

    private final LocationListener mLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            updateNewLocation(location);
        }

        public void onProviderDisabled(String provider) {
            updateNewLocation(null);
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    private void updateNewLocation(Location location) {

        if (location != null) {
            CommonUtils.mCurrentLocation = location;
        } else {
            /*if (BuildConfig.DEBUG) {
                location = new Location("reverseGeocoded");
                location.setLongitude(115.25);
                location.setLatitude(39.26);

                CommonUtils.mCurrentLocation = location;
            }*/

            Toast.makeText(HomeActivity.this, "Can't get location info", Toast.LENGTH_LONG).show();
        }
    }

}
