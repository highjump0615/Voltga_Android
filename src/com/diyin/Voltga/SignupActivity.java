package com.diyin.Voltga;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.baoyz.actionsheet.ActionSheet;
import com.diyin.Voltga.api.API_Manager;
import com.diyin.Voltga.data.UserObj;
import com.diyin.Voltga.utils.CommonUtils;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignupActivity extends FragmentActivity implements View.OnClickListener,
        ActionSheet.ActionSheetListener {

    private static final String TAG = SignupActivity.class.getSimpleName();

    private ImageView mButPhoto;
    private Uri mFileUri;
    private Bitmap mNewBitmap = null;

    private EditText mEditUsername;
    private EditText mEditPassword;
    private EditText mEditCPassword;
    private EditText mEditEmail;

    private ProgressDialog mProgressDialog;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // init view
        Button button = (Button) findViewById(R.id.but_signin);
        button.setOnClickListener(this);

        button = (Button) findViewById(R.id.but_done);
        button.setOnClickListener(this);

        mButPhoto = (ImageView) findViewById(R.id.but_photo);
        mButPhoto.setOnClickListener(this);

        mEditUsername = (EditText) findViewById(R.id.edit_username);
        mEditPassword = (EditText) findViewById(R.id.edit_password);
        mEditCPassword = (EditText) findViewById(R.id.edit_cpassword);
        mEditEmail = (EditText) findViewById(R.id.edit_email);
    }

    private void onSignup() {

        String strUsername = mEditUsername.getText().toString();
        String strPassword = mEditPassword.getText().toString();
        String strCPassword = mEditCPassword.getText().toString();
        String strEmail = mEditEmail.getText().toString();

        // Check data integrity
        if (TextUtils.isEmpty(strEmail)) {
            CommonUtils.createErrorAlertDialog(this, "", "Please input the email address correctly, it will be used to recovery password.").show();
            return;
        }

        if (TextUtils.isEmpty(strPassword) || TextUtils.isEmpty(strCPassword) || !strPassword.equals(strCPassword)) {
            CommonUtils.createErrorAlertDialog(this, "", "Please input password correctly.").show();
            return;
        }

        if (mNewBitmap == null) {
            CommonUtils.createErrorAlertDialog(this, "", "Please select profile picture.").show();
            return;
        }

        if (TextUtils.isEmpty(strUsername)) {
            CommonUtils.createErrorAlertDialog(this, "", "Please input user name. It must be an unique one.").show();
            return;
        } else {
            // check white space
            Pattern patter = Pattern.compile("\\s");
            Matcher matcher = patter.matcher(strUsername);
            boolean bFoundSpace = matcher.find();
            if (bFoundSpace) {
                CommonUtils.createErrorAlertDialog(this, "", "Username should not contain space.").show();
                return;
            }
        }

        mProgressDialog = ProgressDialog.show(this, "", "Processing image...");

        // set Image Thumbnail
        final Bitmap bmpThumbnail = CommonUtils.getThumbnail(mNewBitmap);

        // token save

        mProgressDialog.setMessage("Signing up...");

        API_Manager.getInstance().userSignUpWithUserName(
                strUsername,
                strEmail,
                strPassword,
                SplashActivity.getRegistrationId(this),
                mNewBitmap,
                bmpThumbnail,
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

                                // change cache
                                try {
                                    ImageLoader.getInstance().getMemoryCache().put(CommonUtils.getUserPhoto(CommonUtils.mSelfUser.user_public_photo, false), mNewBitmap);
                                    ImageLoader.getInstance().getDiskCache().save(CommonUtils.getUserPhoto(CommonUtils.mSelfUser.user_public_photo, false), mNewBitmap);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                CommonUtils.moveNextActivity(SignupActivity.this, HomeActivity.class, true);

                                mNewBitmap.recycle();
                            } else {
                                CommonUtils.createErrorAlertDialog(SignupActivity.this, "Error", response.getString(API_Manager.WEBAPI_RETURN_MESSAGE)).show();
                            }

                            bmpThumbnail.recycle();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        super.onSuccess(statusCode, headers, response);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        mProgressDialog.dismiss();

                        CommonUtils.createErrorAlertDialog(SignupActivity.this, "Error", throwable.getMessage()).show();

                        super.onFailure(statusCode, headers, throwable, errorResponse);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        mProgressDialog.dismiss();

                        CommonUtils.createErrorAlertDialog(SignupActivity.this, "Error", throwable.getMessage()).show();

                        super.onFailure(statusCode, headers, responseString, throwable);
                    }
                }
        );

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mNewBitmap != null && !mNewBitmap.isRecycled())
            mNewBitmap.recycle();
    }

    @Override
    public void onClick(View view) {

        int id = view.getId();

        switch (id) {
            case R.id.but_signin:
                CommonUtils.moveNextActivity(this, SigninActivity.class, false);
                break;

            case R.id.but_done:
                onSignup();
                break;

            case R.id.but_photo:
                ActionSheet.createBuilder(this, getSupportFragmentManager())
                        .setCancelButtonTitle("Cancel")
                        .setOtherButtonTitles("Photo Album", "Camera          ")
                        .setOtherButtonImageResources(R.drawable.album_icon, R.drawable.camera_icon)
                        .setCancelableOnTouchOutside(true)
                        .setListener(this)
                        .show();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        /*if (requestCode == CommonUtils.CAMERA_OPEN_FOR_CHAT_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                mNewBitmap = (Bitmap) data.getExtras().get("data");
                if (mNewBitmap != null)
                    mButPhoto.setImageBitmap(mNewBitmap);
            } else if (resultCode == RESULT_CANCELED) {
                // User cancelled the photoImage capture
                mFileUri = null;
            } else {
                // Image capture failed, advise user
                mFileUri = null;
            }
        } else */if (requestCode == CommonUtils.CAMERA_OPEN_FOR_CHAT_REQUEST_CODE
                    || requestCode == CommonUtils.GALLERY_OPEN_FOR_PHOTO_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                if (data == null) {
                    onBackPressed();
                } else {
                    Object object = data.getData();
                    if (object != null) {
                        mFileUri = (Uri) object;
                    }

                    String mimeType = null;
                    String realFilePath;
                    Log.d(TAG, "selected fileName = " + mFileUri.getPath());

                    if (mFileUri.getPath().contains("/external/")) {
                        mimeType = CommonUtils.getMimeType(this, mFileUri);
                        realFilePath = CommonUtils.convertImageUriToFile(this, mFileUri);
                    } else {
                        realFilePath = mFileUri.getPath();
                        String extension = MimeTypeMap.getFileExtensionFromUrl(realFilePath);
                        if (extension != null) {
                            MimeTypeMap mime = MimeTypeMap.getSingleton();
                            mimeType = mime.getMimeTypeFromExtension(extension);
                        }
                    }

                    if (mimeType != null) {
                        if (mimeType.contains("image")) {
                            if (mNewBitmap != null) {
                                mNewBitmap.recycle();
                            }

                            mNewBitmap = CommonUtils.getBitmapFromUri(Uri.parse(realFilePath));
                            if (mNewBitmap != null)
                                mButPhoto.setImageBitmap(mNewBitmap);

                            CommonUtils.deleteFile(realFilePath);
                            return;
                        }
                    }

                    CommonUtils.deleteFile(realFilePath);
                    mNewBitmap = null;
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDismiss(ActionSheet actionSheet, boolean isCancel) {

    }

    @Override
    public void onOtherButtonClick(ActionSheet actionSheet, int index) {
        if (index == 1) { // camera
            try {
                File outputFile = CommonUtils.getOutputMediaFile(this);
                if (outputFile == null) return;

                mFileUri = Uri.fromFile(outputFile);

                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                //if (CommonUtils.isIntentAvailable(this, new Intent("com.android.camera.action.CROP"))) {
                    cameraIntent.putExtra("crop", "true");
                    cameraIntent.putExtra("aspectX", 1);
                    cameraIntent.putExtra("aspectY", 1);
                    //intent.putExtra("outputX", 320);
                    //intent.putExtra("outputY", 320);
                    cameraIntent.putExtra("scale", 1);
                //}

                cameraIntent.putExtra("return-data", false/*return_data*/);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mFileUri);
                cameraIntent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());

                startActivityForResult(cameraIntent, CommonUtils.CAMERA_OPEN_FOR_CHAT_REQUEST_CODE);
            } catch (ActivityNotFoundException anfe) {
                CommonUtils.createErrorAlertDialog(this, "Alert", "Your device doesn't support capturing images!").show();
            }
        } else if (index == 0) {
            File outputFile = CommonUtils.getOutputMediaFile(this);
            if (outputFile == null) return;

            mFileUri = Uri.fromFile(outputFile);

            Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);

            //if (CommonUtils.isIntentAvailable(this, new Intent("com.android.camera.action.CROP"))) {
                intent.putExtra("crop", "true");
                intent.putExtra("aspectX", 1);
                intent.putExtra("aspectY", 1);
                //intent.putExtra("outputX", 320);
                //intent.putExtra("outputY", 320);
                intent.putExtra("scale", 1);
            //}

            intent.setType("image/*");
            intent.putExtra("return-data", false/*return_data*/);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mFileUri);
            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
            //intent.putExtra("noFaceDetection",!faceDetection); // lol, negative boolean noFaceDetection
            /*if (circleCrop) {
                intent.putExtra("circleCrop", true);
            }*/

            startActivityForResult(intent, CommonUtils.GALLERY_OPEN_FOR_PHOTO_REQUEST_CODE);
        }
    }

}
