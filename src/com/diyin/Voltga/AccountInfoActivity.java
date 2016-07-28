package com.diyin.Voltga;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.diyin.Voltga.api.API_Manager;
import com.diyin.Voltga.data.UserObj;
import com.diyin.Voltga.utils.CommonUtils;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

public class AccountInfoActivity extends Activity implements View.OnClickListener {

    private EditText mEditEmail;
    private EditText mEditCUPassword;
    private EditText mEditPassword;
    private EditText mEditCPassword;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accountinfo);

        // init UI
        ImageView imageBack = (ImageView) findViewById(R.id.image_back);
        imageBack.setOnClickListener(this);

        Button button = (Button) findViewById(R.id.but_save);
        button.setOnClickListener(this);

        mEditEmail = (EditText) findViewById(R.id.edit_email);
        mEditEmail.setText(CommonUtils.mSelfUser.user_email);

        mEditCUPassword = (EditText) findViewById(R.id.edit_cupassword);
        mEditPassword = (EditText) findViewById(R.id.edit_password);
        mEditCPassword = (EditText) findViewById(R.id.edit_cpassword);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.pop_in, R.anim.pop_out);
    }

    @Override
    public void onClick(View view) {

        int nId = view.getId();

        switch (nId) {
            case R.id.image_back:
                onBackPressed();
                break;

            case R.id.but_save:
                onSave();
        }
    }

    private void onSave() {
        String strEmail = mEditEmail.getText().toString();
        String strCUPassword = mEditCUPassword.getText().toString();
        String strPassword = mEditPassword.getText().toString();
        String strCPassword = mEditCPassword.getText().toString();

        // Check data integrity
        if (TextUtils.isEmpty(strEmail)) {
            CommonUtils.createErrorAlertDialog(this, "", "Please input email address.").show();
            return;
        }

        if (TextUtils.isEmpty(strCUPassword)) {
            CommonUtils.createErrorAlertDialog(this, "", "Please input current password").show();
            return;
        }

        if (TextUtils.isEmpty(strPassword) || TextUtils.isEmpty(strCPassword) || !strPassword.equals(strCPassword)) {
            CommonUtils.createErrorAlertDialog(this, "", "Please input new password correctly.").show();
            return;
        }

        if (!strCUPassword.equals(CommonUtils.mSelfUser.user_password)) {
            CommonUtils.createErrorAlertDialog(this, "", "Current password does not match. Please input correctly.").show();
            return;
        }

        final UserObj userObj = CommonUtils.mSelfUser.currentUser();
        userObj.user_email = mEditEmail.getText().toString();
        userObj.user_password = mEditPassword.getText().toString();

        mProgressDialog = ProgressDialog.show(this, "", "Saving...");

        API_Manager.getInstance().saveUserProfileWithUserObj(
                userObj,
                new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        mProgressDialog.dismiss();

                        try {
                            String strRes = response.getString(API_Manager.WEBAPI_RETURN_RESULT);

                            if (strRes.equals(API_Manager.WEBAPI_RETURN_SUCCESS)) {
                                CommonUtils.mSelfUser.setUser(userObj);
                                onBackPressed();
                            } else {
                                CommonUtils.createErrorAlertDialog(AccountInfoActivity.this, "Error", response.getString(API_Manager.WEBAPI_RETURN_MESSAGE)).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        super.onSuccess(statusCode, headers, response);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        mProgressDialog.dismiss();
                        CommonUtils.createErrorAlertDialog(AccountInfoActivity.this, "Error", throwable.getMessage()).show();

                        super.onFailure(statusCode, headers, throwable, errorResponse);
                    }
                }
        );
    }

}
