package com.diyin.Voltga;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import com.diyin.Voltga.api.API_Manager;
import com.diyin.Voltga.data.UserObj;
import com.diyin.Voltga.utils.CommonUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

public class SplashActivity extends Activity {

    static final String TAG = SplashActivity.class.getSimpleName();

    public static final String EXTRA_MESSAGE = "strMessage";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    /**
     * Substitute you own sender ID here. This is the project number you got
     * from the API Console, as described in "Getting Started."
     */
    String SENDER_ID = "60086411811";

    //GCM
    private GoogleCloudMessaging gcm;
    private AtomicInteger msgId = new AtomicInteger();
    private SharedPreferences prefs;

    private String regid;

    public Timer mTransitionTimer;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);

        // init view
        TextView textView = (TextView) findViewById(R.id.text_voltga);

        Typeface arialFont = Typeface.createFromAsset(getAssets(), "font/arial_rounded_mt_bold.ttf");
        textView.setTypeface(arialFont);

        textView = (TextView) findViewById(R.id.text_subtitle);

        Typeface chalkFont = Typeface.createFromAsset(getAssets(), "font/chalkduster.ttf");
        textView.setTypeface(chalkFont);

        // Check device for Play Services APK. If check succeeds, proceed with
        // GCM registration.
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(this);

            if (regid.isEmpty()) {
                registerInBackground();
            }

            // Test
            //sendMessage("Hi rabbit, over!");
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }

        // Check location information
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        openGPSSettings();
                    }
                });
            }
        };
        mTransitionTimer = new Timer();
        mTransitionTimer.schedule(task, 5000);

        // init image loader
        ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(getApplicationContext())
                .memoryCache(new LruMemoryCache(2 * 1024 * 1024))
                .memoryCacheSize(2 * 1024 * 1024)
                .build();
        ImageLoader.getInstance().init(configuration);
        CommonUtils.mImgOptions = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.img_default_user)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();
    }

    // You need to do the Play Services APK check here too.
    @Override
    protected void onResume() {
        super.onResume();

        try {
            GcmIntentService.clearBadge(this);
        } catch (Exception e) {
        }
    }

    private void failedToLogin(String strError) {
        if (TextUtils.isEmpty(strError)) {
            CommonUtils.moveNextActivity(SplashActivity.this, SignupActivity.class, true);
        } else {
            Dialog dialog = new AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage(strError)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            CommonUtils.moveNextActivity(SplashActivity.this, SignupActivity.class, true);
                        }
                    }).create();
            dialog.show();
        }
    }

    private void openGPSSettings() {
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER)) {
            checkSigninState();
            return;
        }

        AlertDialog alert = new AlertDialog.Builder(this)
                .setTitle("")
                .setMessage("You need to enable your location service to use this app.")
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(intent, CommonUtils.LOCATION_SETTING_CODE);
                    }

                }).create();

        alert.show();
    }

    private void checkSigninState() {
        // load preference
        SharedPreferences preferences = getSharedPreferences(CommonUtils.PREF_NAME, Context.MODE_PRIVATE);
        String strUserId = preferences.getString(CommonUtils.PREF_USERID, "");
        if (!TextUtils.isEmpty(strUserId)) {
            API_Manager.getInstance().getUserWithUserID(
                    strUserId,
                    new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            try {
                                String strRes = response.getString(API_Manager.WEBAPI_RETURN_RESULT);

                                if (strRes.equals(API_Manager.WEBAPI_RETURN_SUCCESS)) {

                                    CommonUtils.mSelfUser = new UserObj(response.getJSONObject(API_Manager.WEBAPI_RETURN_VALUES).getJSONObject("user"));

                                    // Save user phone and flag of signed into NSUserDefaults
                                    SharedPreferences preferences = getSharedPreferences(CommonUtils.PREF_NAME, Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = preferences.edit();
                                    editor.putString(CommonUtils.PREF_USERID, CommonUtils.mSelfUser.user_id.toString());
                                    editor.apply();

                                    CommonUtils.moveNextActivity(SplashActivity.this, HomeActivity.class, true);

//                                                    [[NSNotificationCenter defaultCenter] postNotificationName:SET_TOTAL_NOTIFICATION object:nil];
                                } else {
                                    failedToLogin(response.getString(API_Manager.WEBAPI_RETURN_MESSAGE));
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                failedToLogin(null);
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            super.onFailure(statusCode, headers, throwable, errorResponse);

                            failedToLogin(throwable.getMessage());
                        }
                    }
            );
        } else {
            CommonUtils.moveNextActivity(SplashActivity.this, SignupActivity.class, true);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == CommonUtils.LOCATION_SETTING_CODE) {
            checkSigninState();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }

            return false;
        }
        return true;
    }

    /**
     * Gets the current registration ID for application on GCM service.
     * <p/>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     * registration ID.
     */
    public static String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }

        Log.i(TAG, "RegisterID = " + registrationId);
        return registrationId;
    }

    /**
     * @return Application's {@code SharedPreferences}.
     */
    private static SharedPreferences getGCMPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return context.getSharedPreferences(HomeActivity.class.getSimpleName(), Context.MODE_PRIVATE);
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    /**
     * Registers the application with GCM servers asynchronously.
     * <p/>
     * Stores the registration ID and app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(SplashActivity.this);
                    }
                    regid = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;

                    // You should send the registration ID to your server over HTTP,
                    // so it can use GCM/HTTP or CCS to send messages to your app.
                    // The request to your server should be authenticated if your app
                    // is using accounts.
                    sendRegistrationIdToBackend();

                    // For this demo: we don't need to send it because the device
                    // will send upstream messages to a server that echo back the
                    // message using the 'from' address in the message.

                    // Persist the regID - no need to register again.
                    storeRegistrationId(SplashActivity.this, regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                //Toast.makeText(SplashActivity.this, msg, Toast.LENGTH_LONG).show();
                Log.i(TAG, msg);
            }
        }.execute(null, null, null);
    }

    /**
     * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP
     * or CCS to send messages to your app. Not needed for this demo since the
     * device sends upstream messages to a server that echoes back the message
     * using the 'from' address in the message.
     */
    private void sendRegistrationIdToBackend() {
        // Your implementation here.
    }

    /**
     * Stores the registration ID and app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId   registration ID
     */
    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);

        Log.i(TAG, "Saving regId on app version " + appVersion);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.apply();
    }

    public void sendMessage(String message) {
        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... params) {
                String msg = "";
                try {
                    Bundle data = new Bundle();
                    data.putString(EXTRA_MESSAGE, params[0]);
                    data.putString("my_action", "com.google.android.gcm.demo.app.ECHO_NOW");
                    String id = Integer.toString(msgId.incrementAndGet());

                    gcm.send(SENDER_ID + "@gcm.googleapis.com", id, data);

                    msg = "Sent message";
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {

            }
        }.execute(message);
    }

}
