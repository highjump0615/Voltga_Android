package com.diyin.Voltga;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.diyin.Voltga.api.API_Manager;
import com.diyin.Voltga.utils.CommonUtils;
import com.diyin.Voltga.widget.NoDefaultSpinner;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends Activity implements View.OnClickListener {

    private static final String TAG = ProfileActivity.class.getSimpleName();

    private EditText mEditName;
    private EditText mEditHeight;
    private EditText mEditWeight;
    private TextView mTextLbs;

    private NoDefaultSpinner mspinAge;
    private NoDefaultSpinner mspinEthnicity;
    private NoDefaultSpinner mspinBody;
    private NoDefaultSpinner mspinPractice;
    private NoDefaultSpinner mspinStatus;
    private EditText mEditIntro;

    private String[] mAgeArray;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // init UI
        ImageView imageBack = (ImageView) findViewById(R.id.image_back);
        imageBack.setOnClickListener(this);

        Button button = (Button) findViewById(R.id.but_save);
        button.setOnClickListener(this);

        mspinEthnicity = (NoDefaultSpinner) findViewById(R.id.spin_ethnicity);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.ethnicity_array, R.layout.profile_spinner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mspinEthnicity.setAdapter(adapter);

        selectString(mspinEthnicity,
                getResources().getStringArray(R.array.ethnicity_array),
                CommonUtils.mSelfUser.user_ethnicity);

        mspinBody = (NoDefaultSpinner) findViewById(R.id.spin_body);
        adapter = ArrayAdapter.createFromResource(this, R.array.body_array, R.layout.profile_spinner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mspinBody.setAdapter(adapter);

        selectString(mspinBody,
                getResources().getStringArray(R.array.body_array),
                CommonUtils.mSelfUser.user_body);

        mspinPractice = (NoDefaultSpinner) findViewById(R.id.spin_practice);
        adapter = ArrayAdapter.createFromResource(this, R.array.practice_array, R.layout.profile_spinner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mspinPractice.setAdapter(adapter);

        selectString(mspinPractice,
                getResources().getStringArray(R.array.practice_array),
                CommonUtils.mSelfUser.user_practice);

        mspinStatus = (NoDefaultSpinner) findViewById(R.id.spin_status);
        adapter = ArrayAdapter.createFromResource(this, R.array.status_array, R.layout.profile_spinner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mspinStatus.setAdapter(adapter);

        selectString(mspinStatus,
                getResources().getStringArray(R.array.status_array),
                CommonUtils.mSelfUser.user_status);

        // age spinner
        List<String> listAge = new ArrayList<String>();
        for (int i = 21; i <= 50; i++) {
            listAge.add(String.valueOf(i));
        }
        int nSize = listAge.size();
        mAgeArray = (String[]) listAge.toArray(new String[nSize]);
        mspinAge = (NoDefaultSpinner) findViewById(R.id.spin_age);
        adapter = new ArrayAdapter<CharSequence>(this, R.layout.profile_spinner, mAgeArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mspinAge.setAdapter(adapter);

        selectString(mspinAge, mAgeArray, CommonUtils.mSelfUser.user_age.toString());

        mEditName = (EditText) findViewById(R.id.edit_username);
        mEditName.setText(CommonUtils.mSelfUser.user_name);

        mEditHeight = (EditText) findViewById(R.id.edit_height);
//        mEditHeight.addTextChangedListener(mTextWatcher);
        mEditHeight.setText(CommonUtils.mSelfUser.user_height);

        mEditWeight = (EditText) findViewById(R.id.edit_weight);
        mEditWeight.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s != null && s.length() > 0) {
                    mTextLbs.setVisibility(View.VISIBLE);
                } else {
                    mTextLbs.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mTextLbs = (TextView) findViewById(R.id.text_lbs);

        if (CommonUtils.mSelfUser.user_weight > 0) {
            mEditWeight.setText(CommonUtils.mSelfUser.user_weight.toString());
        }

        mEditIntro = (EditText) findViewById(R.id.edit_intro);
        mEditIntro.setText(CommonUtils.mSelfUser.user_intro);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.pop_in, R.anim.pop_out);
    }

//    private TextWatcher mTextWatcher = new TextWatcher() {
//        @Override
//        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
//        }
//
//        @Override
//        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
//        }
//
//        @Override
//        public void afterTextChanged(Editable editable) {
//            // convert . to '
//            mEditHeight.removeTextChangedListener(mTextWatcher);
//
////            String strText = editable.toString()..replace(".", "'");
////            mEditHeight.setText(strText);
//            int nIndex = editable.toString().indexOf(",");
//            if (nIndex >= 0) {
//                editable.replace(nIndex, nIndex + 1, "'", 0, 1);
//            }
//
//            mEditHeight.addTextChangedListener(mTextWatcher);
//        }
//    };

    private void selectString(Spinner spinner, String[] strArray, String strObj) {
        // set selection
        int i = 0;
        for (String strItem : strArray) {
            if (strObj.equals(strItem)) {
                spinner.setSelection(i);
                break;
            }
            i++;
        }
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
                break;
        }
    }

    private void onSave() {
        // Check data integrity
        if (mspinAge.getSelectedItemPosition() < 0) {
            CommonUtils.createErrorAlertDialog(this, "", "Please select age.").show();
            return;
        }

        if (TextUtils.isEmpty(mEditHeight.getText().toString())) {
            CommonUtils.createErrorAlertDialog(this, "", "Please input height.").show();
            return;
        }

        if (TextUtils.isEmpty(mEditWeight.getText().toString())) {
            CommonUtils.createErrorAlertDialog(this, "", "Please input weight.").show();
            return;
        }

        if (mspinEthnicity.getSelectedItemPosition() < 0) {
            CommonUtils.createErrorAlertDialog(this, "", "Please select ethnicity.").show();
            return;
        }

        if (mspinBody.getSelectedItemPosition() < 0) {
            CommonUtils.createErrorAlertDialog(this, "", "Please select body.").show();
            return;
        }

        if (mspinPractice.getSelectedItemPosition() < 0) {
            CommonUtils.createErrorAlertDialog(this, "", "Please select practice.").show();
            return;
        }

        if (mspinStatus.getSelectedItemPosition() < 0) {
            CommonUtils.createErrorAlertDialog(this, "", "Please select status.").show();
            return;
        }

        if (TextUtils.isEmpty(mEditIntro.getText().toString())) {
            CommonUtils.createErrorAlertDialog(this, "", "Please input intro.").show();
            return;
        }

        CommonUtils.mSelfUser.user_age = Integer.parseInt((String) mspinAge.getSelectedItem());
        CommonUtils.mSelfUser.user_height = mEditHeight.getText().toString();
        CommonUtils.mSelfUser.user_weight = Double.parseDouble(mEditWeight.getText().toString());
        CommonUtils.mSelfUser.user_ethnicity = (String) mspinEthnicity.getSelectedItem();
        CommonUtils.mSelfUser.user_body = (String) mspinBody.getSelectedItem();
        CommonUtils.mSelfUser.user_practice = (String) mspinPractice.getSelectedItem();
        CommonUtils.mSelfUser.user_status = (String) mspinStatus.getSelectedItem();
        CommonUtils.mSelfUser.user_intro = mEditIntro.getText().toString();

        mProgressDialog = ProgressDialog.show(this, "", "Saving...");

        API_Manager.getInstance().saveUserProfileWithUserObj(
                CommonUtils.mSelfUser,
                new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        mProgressDialog.dismiss();

                        try {
                            String strRes = response.getString(API_Manager.WEBAPI_RETURN_RESULT);

                            if (strRes.equals(API_Manager.WEBAPI_RETURN_SUCCESS)) {
                                onBackPressed();
                            } else {
                                CommonUtils.createErrorAlertDialog(ProfileActivity.this, "Error", response.getString(API_Manager.WEBAPI_RETURN_MESSAGE)).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        super.onSuccess(statusCode, headers, response);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        mProgressDialog.dismiss();
                        CommonUtils.createErrorAlertDialog(ProfileActivity.this, "Error", throwable.getMessage()).show();

                        super.onFailure(statusCode, headers, throwable, errorResponse);
                    }
                }
        );
    }
}
