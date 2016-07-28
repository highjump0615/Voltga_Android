package com.diyin.Voltga;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.diyin.Voltga.api.API_Manager;
import com.diyin.Voltga.utils.CommonUtils;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class UserActivity extends Activity implements View.OnClickListener {

    private static final String TAG = UserActivity.class.getSimpleName();

    private LinearLayout mLayoutAction;
    private boolean mbActionOn = false;

    private ImageView mImgPublicPhoto;
    private ImageView mImgPrivatePhoto1;
    private ImageView mImgPrivatePhoto2;
    private ImageView mImgPrivatePhoto3;

    public ArrayList<String> mUrlPhotoList = new ArrayList<String>();
    private ProgressDialog mProgressDialog;

    private TextView mtextUnlock;
    private TextView mtextBlock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        // AdMob
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        // init view
        findViewById(R.id.image_back).setOnClickListener(this);

        Button button = (Button) findViewById(R.id.but_action);
        button.setOnClickListener(this);

        ImageView imgBackground = (ImageView) findViewById(R.id.img_background);
        ImageLoader.getInstance().displayImage(CommonUtils.getUserPhoto(CommonUtils.mSelectedUser.user_public_photo, false), imgBackground, CommonUtils.mImgOptions);

        mImgPublicPhoto = (ImageView) findViewById(R.id.img_publicphoto);
        mImgPublicPhoto.setOnClickListener(this);
        mImgPrivatePhoto1 = (ImageView) findViewById(R.id.img_privatephoto1);
        mImgPrivatePhoto1.setOnClickListener(this);
        mImgPrivatePhoto2 = (ImageView) findViewById(R.id.img_privatephoto2);
        mImgPrivatePhoto2.setOnClickListener(this);
        mImgPrivatePhoto3 = (ImageView) findViewById(R.id.img_privatephoto3);
        mImgPrivatePhoto3.setOnClickListener(this);

        ImageLoader.getInstance().displayImage(CommonUtils.getUserPhoto(CommonUtils.mSelectedUser.user_public_photo, true), mImgPublicPhoto, CommonUtils.mImgOptions);

        mUrlPhotoList.add(CommonUtils.getUserPhoto(CommonUtils.mSelectedUser.user_public_photo, false));

        if (!CommonUtils.mSelectedUser.lockedMe() ||
                CommonUtils.mSelectedUser.user_id.equals(CommonUtils.mSelfUser.user_id)) {

            if (CommonUtils.getUserPhoto(CommonUtils.mSelectedUser.user_private_photo1, false).length() > 0) {
                mUrlPhotoList.add(CommonUtils.getUserPhoto(CommonUtils.mSelectedUser.user_private_photo1, false));
                ImageLoader.getInstance().displayImage(CommonUtils.getUserPhoto(CommonUtils.mSelectedUser.user_private_photo1, true), mImgPrivatePhoto1, CommonUtils.mImgOptions);
            }

            if (CommonUtils.getUserPhoto(CommonUtils.mSelectedUser.user_private_photo2, false).length() > 0) {
                mUrlPhotoList.add(CommonUtils.getUserPhoto(CommonUtils.mSelectedUser.user_private_photo2, false));
                ImageLoader.getInstance().displayImage(CommonUtils.getUserPhoto(CommonUtils.mSelectedUser.user_private_photo2, true), mImgPrivatePhoto2, CommonUtils.mImgOptions);
            }

            if (CommonUtils.getUserPhoto(CommonUtils.mSelectedUser.user_private_photo3, false).length() > 0) {
                mUrlPhotoList.add(CommonUtils.getUserPhoto(CommonUtils.mSelectedUser.user_private_photo3, false));
                ImageLoader.getInstance().displayImage(CommonUtils.getUserPhoto(CommonUtils.mSelectedUser.user_private_photo3, true), mImgPrivatePhoto3, CommonUtils.mImgOptions);
            }
        }

        mLayoutAction = (LinearLayout) findViewById(R.id.layout_action);
        showActionMenu();

        // profile text
        String strProfile = CommonUtils.mSelectedUser.user_name + "\n";
        if (CommonUtils.mSelectedUser.user_age > 0)
            strProfile += CommonUtils.mSelectedUser.user_age.toString() + ", ";
        if (!TextUtils.isEmpty(CommonUtils.mSelectedUser.user_height) &&
                !CommonUtils.mSelectedUser.user_height.equals("0"))
            strProfile += CommonUtils.mSelectedUser.user_height + ", ";
        if (CommonUtils.mSelectedUser.user_weight > 0)
            strProfile += CommonUtils.mSelectedUser.user_weight.toString() + "lbs,\n";
        if (!TextUtils.isEmpty(CommonUtils.mSelectedUser.user_ethnicity))
            strProfile += CommonUtils.mSelectedUser.user_ethnicity + ", ";
        if (!TextUtils.isEmpty(CommonUtils.mSelectedUser.user_body))
            strProfile += CommonUtils.mSelectedUser.user_body + ",\n";
        if (!TextUtils.isEmpty(CommonUtils.mSelectedUser.user_practice))
            strProfile += CommonUtils.mSelectedUser.user_practice + ", ";
        if (!TextUtils.isEmpty(CommonUtils.mSelectedUser.user_status))
            strProfile += CommonUtils.mSelectedUser.user_status;

        TextView textView = (TextView) findViewById(R.id.text_profile);
        textView.setText(strProfile);

        textView = (TextView) findViewById(R.id.text_intro);
        textView.setText(CommonUtils.mSelectedUser.user_intro);

        //
        // action
        //
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.layout_mention);
        linearLayout.setOnClickListener(this);
        linearLayout = (LinearLayout) findViewById(R.id.layout_unlock);
        linearLayout.setOnClickListener(this);
        linearLayout = (LinearLayout) findViewById(R.id.layout_like);
        linearLayout.setOnClickListener(this);
        linearLayout = (LinearLayout) findViewById(R.id.layout_block);
        linearLayout.setOnClickListener(this);

        mtextUnlock = (TextView) findViewById(R.id.text_unlock);
        mtextBlock = (TextView) findViewById(R.id.text_block);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.pop_in, R.anim.pop_out);
    }

    private void showActionMenu() {
        if (CommonUtils.mSelectedUser.user_id.equals(CommonUtils.mSelfUser.user_id)) {
            mLayoutAction.setVisibility(View.GONE);
            return;
        }

        if (mbActionOn) {
            mLayoutAction.setVisibility(View.VISIBLE);
        } else {
            mLayoutAction.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View view) {

        int id = view.getId();

        Intent intent;

        switch (id) {
            case R.id.image_back:
                onBackPressed();
                break;

            case R.id.but_action:
                mbActionOn = !mbActionOn;
                showActionMenu();
                break;

            case R.id.img_publicphoto:
                intent = new Intent(this, PhotoActivity.class);
                intent.putExtra("urlList", mUrlPhotoList);
                intent.putExtra("currentIndex", 0);
                startActivity(intent);
                break;

            case R.id.img_privatephoto1:
                if (mUrlPhotoList.size() > 1) {
                    intent = new Intent(this, PhotoActivity.class);
                    intent.putExtra("urlList", mUrlPhotoList);
                    intent.putExtra("currentIndex", 1);
                    startActivity(intent);
                }
                break;

            case R.id.img_privatephoto2:
                if (mUrlPhotoList.size() > 2) {
                    intent = new Intent(this, PhotoActivity.class);
                    intent.putExtra("urlList", mUrlPhotoList);
                    intent.putExtra("currentIndex", 2);
                    startActivity(intent);
                }
                break;

            case R.id.img_privatephoto3:
                if (mUrlPhotoList.size() > 3) {
                    intent = new Intent(this, PhotoActivity.class);
                    intent.putExtra("urlList", mUrlPhotoList);
                    intent.putExtra("currentIndex", 3);
                    startActivity(intent);
                }
                break;

            case R.id.layout_mention:
                onMention();
                break;

            case R.id.layout_unlock:
                onUnlock();
                break;

            case R.id.layout_like:
                onLike();
                break;

            case R.id.layout_block:
                onBlock();
                break;
        }
    }

    private void onMention() {
        updateActionView();

        String strText = CommonUtils.mHomeActivity.mCurrentActivity.mChatFragment.mEditText.getText().toString() + "@" + CommonUtils.mSelectedUser.user_name + " ";
        CommonUtils.mHomeActivity.mCurrentActivity.mChatFragment.mEditText.setText(strText);
        CommonUtils.mHomeActivity.mCurrentActivity.setSegmentIndex(CommonUtils.mHomeActivity.mCurrentActivity.CURRENT_CHAT);
        onBackPressed();

        // hide the panel
        mbActionOn = false;
        showActionMenu();
    }

    private void onUnlock() {
        int nFlag = CommonUtils.mSelectedUser.user_relation_to;
        int nbUnlock;

        if (CommonUtils.mSelectedUser.lockedByMe()) {
            nFlag |= CommonUtils.RelationType_IsUnlock;
            nbUnlock = 1;
        } else {
            nFlag &= ~CommonUtils.RelationType_IsUnlock;
            nbUnlock = 0;
        }

        mProgressDialog = ProgressDialog.show(this, "", "Action...");

        final int finalNFlag = nFlag;
        API_Manager.getInstance().unlockWithUserID(
                CommonUtils.mSelfUser.user_id.toString(),
                CommonUtils.mSelfUser.user_name,
                CommonUtils.mSelectedUser.user_id.toString(),
                CommonUtils.mSelfUser.user_place_id.toString(),
                String.valueOf(nFlag),
                String.valueOf(nbUnlock),
                new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                        mProgressDialog.dismiss();

                        try {
                            String strRes = response.getString(API_Manager.WEBAPI_RETURN_RESULT);

                            if (strRes.equals(API_Manager.WEBAPI_RETURN_SUCCESS)) {
                                CommonUtils.mSelectedUser.user_relation_to = finalNFlag;

                                if (CommonUtils.mSelectedUser.lockedByMe()) {
                                    Toast.makeText(UserActivity.this, "Lock has been sent.", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(UserActivity.this, "Unlock has been sent.", Toast.LENGTH_LONG).show();
                                }

                                updateActionView();
                            } else {
                                CommonUtils.createErrorAlertDialog(UserActivity.this, "Error", response.getString(API_Manager.WEBAPI_RETURN_MESSAGE)).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        super.onSuccess(statusCode, headers, response);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        mProgressDialog.dismiss();

                        CommonUtils.createErrorAlertDialog(UserActivity.this, "Error", throwable.getMessage()).show();

                        super.onFailure(statusCode, headers, throwable, errorResponse);
                    }
                }
        );

        // hide the panel
        mbActionOn = false;
        showActionMenu();
    }

    private void onLike() {
        int nFlag = CommonUtils.mSelectedUser.user_relation_to;
        //nFlag |= CommonUtils.RelationType_IsLike;

        mProgressDialog = ProgressDialog.show(this, "", "Action...");

        final int finalNFlag = nFlag;
        API_Manager.getInstance().likeWithUserID(
                CommonUtils.mSelfUser.user_id.toString(),
                CommonUtils.mSelfUser.user_name,
                CommonUtils.mSelectedUser.user_id.toString(),
                CommonUtils.mSelfUser.user_place_id.toString(),
                Integer.toString(nFlag),
                new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                        mProgressDialog.dismiss();

                        try {
                            String strRes = response.getString(API_Manager.WEBAPI_RETURN_RESULT);

                            if (strRes.equals(API_Manager.WEBAPI_RETURN_SUCCESS)) {
                                CommonUtils.mSelectedUser.user_relation_to = finalNFlag;

                                if (CommonUtils.mSelectedUser.likedByMe()) {
                                    Toast.makeText(UserActivity.this, "Like has been sent.", Toast.LENGTH_LONG).show();
                                }

                                updateActionView();
                            } else {
                                CommonUtils.createErrorAlertDialog(UserActivity.this, "Error", response.getString(API_Manager.WEBAPI_RETURN_MESSAGE)).show();
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

                        CommonUtils.createErrorAlertDialog(UserActivity.this, "Error", throwable.getMessage()).show();
                    }
                }
        );

        // hide the panel
        mbActionOn = false;
        showActionMenu();
    }

    private void onBlock() {
        int nFlag = CommonUtils.mSelectedUser.user_relation_to;
        int nbBlock;

        if (CommonUtils.mSelectedUser.blockedByMe()) {
            nFlag &= ~CommonUtils.RelationType_IsBlock;
            nbBlock = 0;
        } else {
            nFlag |= CommonUtils.RelationType_IsBlock;
            nbBlock = 1;
        }

        mProgressDialog = ProgressDialog.show(this, "", "Action...");

        final int finalNFlag = nFlag;
        API_Manager.getInstance().blockWithUserID(
                CommonUtils.mSelfUser.user_id.toString(),
                CommonUtils.mSelectedUser.user_id.toString(),
                CommonUtils.mSelfUser.user_place_id.toString(),
                String.valueOf(nFlag),
                String.valueOf(nbBlock),
                new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                        mProgressDialog.dismiss();

                        try {
                            String strRes = response.getString(API_Manager.WEBAPI_RETURN_RESULT);

                            if (strRes.equals(API_Manager.WEBAPI_RETURN_SUCCESS)) {
                                CommonUtils.mSelectedUser.user_relation_to = finalNFlag;

                                if (CommonUtils.mSelectedUser.blockedByMe()) {
                                    Toast.makeText(UserActivity.this, "Block has been sent.", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(UserActivity.this, "Unblock has been sent.", Toast.LENGTH_LONG).show();
                                }

                                updateActionView();
                            } else {
                                CommonUtils.createErrorAlertDialog(UserActivity.this, "Error", response.getString(API_Manager.WEBAPI_RETURN_MESSAGE)).show();
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

                        CommonUtils.createErrorAlertDialog(UserActivity.this, "Error", throwable.getMessage()).show();
                    }
                }
        );

        // hide the panel
        mbActionOn = false;
        showActionMenu();
    }

    private void updateActionView() {
        if (CommonUtils.mSelectedUser.blockedByMe()) {
            mtextBlock.setText("unblock");
        } else {
            mtextBlock.setText("block");
        }

        if (CommonUtils.mSelectedUser.lockedByMe()) {
            mtextUnlock.setText("unlock");
        } else {
            mtextUnlock.setText("lock");
        }
    }
}
