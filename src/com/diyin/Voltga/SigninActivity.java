package com.diyin.Voltga;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.diyin.Voltga.api.API_Manager;
import com.diyin.Voltga.data.UserObj;
import com.diyin.Voltga.utils.CommonUtils;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

public class SigninActivity extends Activity implements View.OnClickListener {

    private EditText mEditEmail;
    private EditText mEditPassword;

    private ProgressDialog mProgressDialog;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        // init view
        findViewById(R.id.image_back).setOnClickListener(this);

        Button button = (Button) findViewById(R.id.but_back);
        button.setOnClickListener(this);

        button = (Button) findViewById(R.id.but_done);
        button.setOnClickListener(this);

        button = (Button) findViewById(R.id.but_forget);
        button.setOnClickListener(this);

        mEditEmail = (EditText) findViewById(R.id.edit_email);
        mEditPassword = (EditText) findViewById(R.id.edit_password);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.pop_in, R.anim.pop_out);
    }

    @Override
    public void onClick(View view) {

        int id = view.getId();

        switch (id) {
            case R.id.image_back:
                onBackPressed();
                break;

            case R.id.but_done:
                onSignin();
                break;

            case R.id.but_forget:

                LayoutInflater inflater = this.getLayoutInflater();

                final View viewDialog = inflater.inflate(R.layout.dialog_forget, null);

                AlertDialog forgetDialog = new AlertDialog.Builder(this)
                        .setTitle(R.string.app_name)
                        .setView(viewDialog)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // forget password
                                EditText editEmail = (EditText) viewDialog.findViewById(R.id.edit_email);
                                API_Manager.getInstance().getPasswordWithUserEmail(
                                        editEmail.getText().toString(),
                                        new JsonHttpResponseHandler() {
                                            @Override
                                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                                                try {
                                                    String strRes = response.getString(API_Manager.WEBAPI_RETURN_RESULT);

                                                    if (strRes.equals(API_Manager.WEBAPI_RETURN_SUCCESS)) {
                                                        CommonUtils.createErrorAlertDialog(SigninActivity.this, "", "Please check your email box.").show();
                                                    } else {
                                                        CommonUtils.createErrorAlertDialog(SigninActivity.this, "Error", response.getString(API_Manager.WEBAPI_RETURN_MESSAGE)).show();
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
                                                CommonUtils.createErrorAlertDialog(SigninActivity.this, "Error", throwable.getMessage()).show();
                                            }
                                        }
                                );
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .create();

                forgetDialog.show();

                break;
        }
    }

    private void onSignin() {

        String strEmail = mEditEmail.getText().toString();
        String strPassword = mEditPassword.getText().toString();

        // Check data integrity
        if (TextUtils.isEmpty(strEmail)) {
            CommonUtils.createErrorAlertDialog(this, "", "Please input email address.").show();
            return;
        }

        if (TextUtils.isEmpty(strPassword)) {
            CommonUtils.createErrorAlertDialog(this, "", "Please input password.").show();
            return;
        }

        mProgressDialog = ProgressDialog.show(this, "", "Signing in...");

        API_Manager.getInstance().userSignInWithUserEmail(
                strEmail,
                strPassword,
                SplashActivity.getRegistrationId(this),
                new JsonHttpResponseHandler() {

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        mProgressDialog.dismiss();

                        try {
                            String strRes = response.getString(API_Manager.WEBAPI_RETURN_RESULT);

                            if (strRes.equals(API_Manager.WEBAPI_RETURN_SUCCESS)) {
                                CommonUtils.mSelfUser = new UserObj(response.getJSONObject(API_Manager.WEBAPI_RETURN_VALUES).getJSONObject("user"));

                                // Save user phone and flag of signed into NSUserDefaults
                                SharedPreferences preferences = getSharedPreferences(CommonUtils.PREF_NAME, Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putString(CommonUtils.PREF_USERID, CommonUtils.mSelfUser.user_id.toString());
                                editor.apply();

                                CommonUtils.moveNextActivity(SigninActivity.this, HomeActivity.class, true);
                            } else {
                                CommonUtils.createErrorAlertDialog(SigninActivity.this, "Error", response.getString(API_Manager.WEBAPI_RETURN_MESSAGE)).show();
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
                        CommonUtils.createErrorAlertDialog(SigninActivity.this, "Error", throwable.getMessage()).show();
                    }
                }
        );
    }
}
