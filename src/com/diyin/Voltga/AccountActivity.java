package com.diyin.Voltga;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
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

public class AccountActivity extends FragmentActivity implements View.OnClickListener, CommonUtils.OnTabActivityResultListener, ActionSheet.ActionSheetListener {

    private static final String TAG = AccountActivity.class.getSimpleName();

    private static final int PUBLIC_PHOTO = 0;
    private static final int PRIVATE_PHOTO1 = 1;
    private static final int PRIVATE_PHOTO2 = 2;
    private static final int PRIVATE_PHOTO3 = 3;

    private ImageView mImgPublicPhoto;
    private ImageView mImgPrivatePhoto1;
    private ImageView mImgPrivatePhoto2;
    private ImageView mImgPrivatePhoto3;

    private Uri mFileUri;
    private Bitmap mNewBitmap = null;

    private ProgressDialog mProgressDialog;
    private int mSelectedButtonID;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        // init UI
        Button button = (Button) findViewById(R.id.but_account);
        button.setOnClickListener(this);

        button = (Button) findViewById(R.id.but_profile);
        button.setOnClickListener(this);

        mImgPublicPhoto = (ImageView) findViewById(R.id.but_photo);
        mImgPublicPhoto.setOnClickListener(this);

        mImgPrivatePhoto1 = (ImageView) findViewById(R.id.but_private_photo1);
        mImgPrivatePhoto1.setOnClickListener(this);

        mImgPrivatePhoto2 = (ImageView) findViewById(R.id.but_private_photo2);
        mImgPrivatePhoto2.setOnClickListener(this);

        mImgPrivatePhoto3 = (ImageView) findViewById(R.id.but_private_photo3);
        mImgPrivatePhoto3.setOnClickListener(this);

        UserObj currentUser = CommonUtils.mSelfUser;
        ImageLoader.getInstance().displayImage(CommonUtils.getUserPhoto(currentUser.user_public_photo, false), mImgPublicPhoto, CommonUtils.mImgOptions);
        ImageLoader.getInstance().displayImage(CommonUtils.getUserPhoto(currentUser.user_private_photo1, false), mImgPrivatePhoto1, CommonUtils.mImgOptions);
        ImageLoader.getInstance().displayImage(CommonUtils.getUserPhoto(currentUser.user_private_photo2, false), mImgPrivatePhoto2, CommonUtils.mImgOptions);
        ImageLoader.getInstance().displayImage(CommonUtils.getUserPhoto(currentUser.user_private_photo3, false), mImgPrivatePhoto3, CommonUtils.mImgOptions);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mNewBitmap != null && !mNewBitmap.isRecycled()) {
            mNewBitmap.recycle();
            mNewBitmap = null;
        }
    }

    @Override
    public void onClick(View view) {

        int nId = view.getId();

        switch (nId) {
            case R.id.but_account:
                CommonUtils.moveNextActivity(CommonUtils.mHomeActivity, AccountInfoActivity.class, false);
                break;

            case R.id.but_profile:
                CommonUtils.moveNextActivity(CommonUtils.mHomeActivity, ProfileActivity.class, false);
                break;

            case R.id.but_photo:
                showActionSheet(PUBLIC_PHOTO);
                break;

            case R.id.but_private_photo1:
                showActionSheet(PRIVATE_PHOTO1);
                break;

            case R.id.but_private_photo2:
                showActionSheet(PRIVATE_PHOTO2);
                break;

            case R.id.but_private_photo3:
                showActionSheet(PRIVATE_PHOTO3);
                break;
        }
    }

    private void showActionSheet(int index) {
        mSelectedButtonID = index;

        ActionSheet.createBuilder(this, getSupportFragmentManager())
                .setCancelButtonTitle("Cancel")
                .setOtherButtonTitles("Photo Album", "Camera          ")
                .setOtherButtonImageResources(R.drawable.album_icon, R.drawable.camera_icon)
                .setCancelableOnTouchOutside(true)
                .setListener(this)
                .show();
    }

    @Override
    public void onTabActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == CommonUtils.GALLERY_OPEN_FOR_PHOTO_REQUEST_CODE + PUBLIC_PHOTO ||
                requestCode == CommonUtils.GALLERY_OPEN_FOR_PHOTO_REQUEST_CODE + PRIVATE_PHOTO1 ||
                requestCode == CommonUtils.GALLERY_OPEN_FOR_PHOTO_REQUEST_CODE + PRIVATE_PHOTO2 ||
                requestCode == CommonUtils.GALLERY_OPEN_FOR_PHOTO_REQUEST_CODE + PRIVATE_PHOTO3 /*||
                requestCode == CommonUtils.CAMERA_OPEN_FOR_CHAT_REQUEST_CODE + PUBLIC_PHOTO ||
                requestCode == CommonUtils.CAMERA_OPEN_FOR_CHAT_REQUEST_CODE + PRIVATE_PHOTO1 ||
                requestCode == CommonUtils.CAMERA_OPEN_FOR_CHAT_REQUEST_CODE + PRIVATE_PHOTO2 ||
                requestCode == CommonUtils.CAMERA_OPEN_FOR_CHAT_REQUEST_CODE + PRIVATE_PHOTO3*/) {

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

                    if (mimeType != null && mimeType.contains("image")) {
                        mNewBitmap = CommonUtils.getBitmapFromUri(Uri.parse(realFilePath));
                        CommonUtils.deleteFile(realFilePath);

                        processPhoto(requestCode - CommonUtils.GALLERY_OPEN_FOR_PHOTO_REQUEST_CODE);
                        return;
                    }

                    CommonUtils.deleteFile(realFilePath);
                }
            }
        }

    }

    private void processPhoto(int index) {
        mProgressDialog = ProgressDialog.show(this, "", "Processing image...");

        // set Image Thumbnail
        final Bitmap bmpThumbnail = CommonUtils.getThumbnail(mNewBitmap);

        mProgressDialog.setMessage("Uploading...");

        switch (index) {
            case PUBLIC_PHOTO:
                mImgPublicPhoto.setImageBitmap(mNewBitmap);

                API_Manager.getInstance().uploadPublicPhotoWithUserName(
                        CommonUtils.mSelfUser.user_name,
                        CommonUtils.mSelfUser.user_public_photo,
                        mNewBitmap,
                        bmpThumbnail,
                        new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                mProgressDialog.dismiss();

                                try {
                                    String strRes = response.getString(API_Manager.WEBAPI_RETURN_RESULT);

                                    if (strRes.equals(API_Manager.WEBAPI_RETURN_SUCCESS)) {

                                        // remove cache data
                                        ImageLoader.getInstance().getMemoryCache().remove(CommonUtils.getUserPhoto(CommonUtils.mSelfUser.user_public_photo, false));
                                        ImageLoader.getInstance().getDiskCache().remove(CommonUtils.getUserPhoto(CommonUtils.mSelfUser.user_public_photo, false));

                                        CommonUtils.mSelfUser.user_public_photo = response.getJSONObject(API_Manager.WEBAPI_RETURN_VALUES).getString("user_public_photo");
                                        ImageLoader.getInstance().displayImage(CommonUtils.getUserPhoto(CommonUtils.mSelfUser.user_public_photo, false), mImgPublicPhoto, CommonUtils.mImgOptions);

                                        // change cache
                                        /*try {
                                            ImageLoader.getInstance().getMemoryCache().put(CommonUtils.getUserPhoto(CommonUtils.mSelfUser.user_public_photo, false), mNewBitmap);
                                            ImageLoader.getInstance().getDiskCache().save(CommonUtils.getUserPhoto(CommonUtils.mSelfUser.user_public_photo, false), mNewBitmap);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }*/

                                    } else {
                                        CommonUtils.createErrorAlertDialog(AccountActivity.this, "Error", response.getString(API_Manager.WEBAPI_RETURN_MESSAGE)).show();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                if (mNewBitmap != null) {
                                    mNewBitmap.recycle();
                                    mNewBitmap = null;
                                }
                                if (bmpThumbnail != null) bmpThumbnail.recycle();

                                super.onSuccess(statusCode, headers, response);
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                                mProgressDialog.dismiss();

                                if (bmpThumbnail != null) bmpThumbnail.recycle();

                                CommonUtils.createErrorAlertDialog(AccountActivity.this, "Error", throwable.getMessage()).show();

                                super.onFailure(statusCode, headers, throwable, errorResponse);
                            }

                        }
                );

                break;

            case PRIVATE_PHOTO1:
                mImgPrivatePhoto1.setImageBitmap(mNewBitmap);

                API_Manager.getInstance().uploadPrivatePhotoWithUserName(
                        CommonUtils.mSelfUser.user_name,
                        CommonUtils.mSelfUser.user_private_photo1,
                        "1",
                        mNewBitmap,
                        bmpThumbnail,
                        new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                mProgressDialog.dismiss();

                                try {
                                    String strRes = response.getString(API_Manager.WEBAPI_RETURN_RESULT);

                                    if (strRes.equals(API_Manager.WEBAPI_RETURN_SUCCESS)) {
                                        // remove cache data
                                        ImageLoader.getInstance().getMemoryCache().remove(CommonUtils.getUserPhoto(CommonUtils.mSelfUser.user_private_photo1, false));
                                        ImageLoader.getInstance().getDiskCache().remove(CommonUtils.getUserPhoto(CommonUtils.mSelfUser.user_private_photo1, false));

                                        CommonUtils.mSelfUser.user_private_photo1 = response.getJSONObject(API_Manager.WEBAPI_RETURN_VALUES).getString("user_private_photo");
                                        ImageLoader.getInstance().displayImage(CommonUtils.getUserPhoto(CommonUtils.mSelfUser.user_private_photo1, false), mImgPrivatePhoto1, CommonUtils.mImgOptions);

                                        // change cache
                                        /*try {
                                            ImageLoader.getInstance().getMemoryCache().put(CommonUtils.getUserPhoto(CommonUtils.mSelfUser.user_private_photo1, false), mNewBitmap);
                                            ImageLoader.getInstance().getDiskCache().save(CommonUtils.getUserPhoto(CommonUtils.mSelfUser.user_private_photo1, false), mNewBitmap);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }*/

                                    } else {
                                        CommonUtils.createErrorAlertDialog(AccountActivity.this, "Error", response.getString(API_Manager.WEBAPI_RETURN_MESSAGE)).show();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                if (mNewBitmap != null) {
                                    mNewBitmap.recycle();
                                    mNewBitmap = null;
                                }
                                if (bmpThumbnail != null) bmpThumbnail.recycle();

                                super.onSuccess(statusCode, headers, response);
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                                mProgressDialog.dismiss();

                                if (bmpThumbnail != null) bmpThumbnail.recycle();

                                CommonUtils.createErrorAlertDialog(AccountActivity.this, "Error", throwable.getMessage()).show();

                                super.onFailure(statusCode, headers, throwable, errorResponse);
                            }
                        }
                );
                break;

            case PRIVATE_PHOTO2:
                mImgPrivatePhoto2.setImageBitmap(mNewBitmap);

                API_Manager.getInstance().uploadPrivatePhotoWithUserName(
                        CommonUtils.mSelfUser.user_name,
                        CommonUtils.mSelfUser.user_private_photo2,
                        "2",
                        mNewBitmap,
                        bmpThumbnail,
                        new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                mProgressDialog.dismiss();

                                try {
                                    String strRes = response.getString(API_Manager.WEBAPI_RETURN_RESULT);

                                    if (strRes.equals(API_Manager.WEBAPI_RETURN_SUCCESS)) {
                                        // remove cache data
                                        ImageLoader.getInstance().getMemoryCache().remove(CommonUtils.getUserPhoto(CommonUtils.mSelfUser.user_private_photo2, false));
                                        ImageLoader.getInstance().getDiskCache().remove(CommonUtils.getUserPhoto(CommonUtils.mSelfUser.user_private_photo2, false));

                                        CommonUtils.mSelfUser.user_private_photo2 = response.getJSONObject(API_Manager.WEBAPI_RETURN_VALUES).getString("user_private_photo");
                                        ImageLoader.getInstance().displayImage(CommonUtils.getUserPhoto(CommonUtils.mSelfUser.user_private_photo2, false), mImgPrivatePhoto2, CommonUtils.mImgOptions);

                                        // change cache
                                        /*try {
                                            ImageLoader.getInstance().getMemoryCache().put(CommonUtils.getUserPhoto(CommonUtils.mSelfUser.user_private_photo2, false), mNewBitmap);
                                            ImageLoader.getInstance().getDiskCache().save(CommonUtils.getUserPhoto(CommonUtils.mSelfUser.user_private_photo2, false), mNewBitmap);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }*/

                                    } else {
                                        CommonUtils.createErrorAlertDialog(AccountActivity.this, "Error", response.getString(API_Manager.WEBAPI_RETURN_MESSAGE)).show();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                if (mNewBitmap != null) {
                                    mNewBitmap.recycle();
                                    mNewBitmap = null;
                                }
                                if (bmpThumbnail != null) bmpThumbnail.recycle();

                                super.onSuccess(statusCode, headers, response);
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                                mProgressDialog.dismiss();

                                if (bmpThumbnail != null) bmpThumbnail.recycle();

                                CommonUtils.createErrorAlertDialog(AccountActivity.this, "Error", throwable.getMessage()).show();

                                super.onFailure(statusCode, headers, throwable, errorResponse);
                            }
                        }
                );

                break;

            case PRIVATE_PHOTO3:
                mImgPrivatePhoto3.setImageBitmap(mNewBitmap);

                API_Manager.getInstance().uploadPrivatePhotoWithUserName(
                        CommonUtils.mSelfUser.user_name,
                        CommonUtils.mSelfUser.user_private_photo3,
                        "3",
                        mNewBitmap,
                        bmpThumbnail,
                        new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                mProgressDialog.dismiss();

                                try {
                                    String strRes = response.getString(API_Manager.WEBAPI_RETURN_RESULT);

                                    if (strRes.equals(API_Manager.WEBAPI_RETURN_SUCCESS)) {
                                        // remove cache data
                                        ImageLoader.getInstance().getMemoryCache().remove(CommonUtils.getUserPhoto(CommonUtils.mSelfUser.user_private_photo3, false));
                                        ImageLoader.getInstance().getDiskCache().remove(CommonUtils.getUserPhoto(CommonUtils.mSelfUser.user_private_photo3, false));

                                        CommonUtils.mSelfUser.user_private_photo3 = response.getJSONObject(API_Manager.WEBAPI_RETURN_VALUES).getString("user_private_photo");
                                        ImageLoader.getInstance().displayImage(CommonUtils.getUserPhoto(CommonUtils.mSelfUser.user_private_photo3, false), mImgPrivatePhoto3, CommonUtils.mImgOptions);

                                        // change cache
                                        /*try {
                                            ImageLoader.getInstance().getMemoryCache().put(CommonUtils.getUserPhoto(CommonUtils.mSelfUser.user_private_photo3, false), mNewBitmap);
                                            ImageLoader.getInstance().getDiskCache().save(CommonUtils.getUserPhoto(CommonUtils.mSelfUser.user_private_photo3, false), mNewBitmap);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }*/

                                    } else {
                                        CommonUtils.createErrorAlertDialog(AccountActivity.this, "Error", response.getString(API_Manager.WEBAPI_RETURN_MESSAGE)).show();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                if (mNewBitmap != null) {
                                    mNewBitmap.recycle();
                                    mNewBitmap = null;
                                }
                                if (bmpThumbnail != null) bmpThumbnail.recycle();

                                super.onSuccess(statusCode, headers, response);
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                                mProgressDialog.dismiss();

                                if (bmpThumbnail != null) bmpThumbnail.recycle();

                                CommonUtils.createErrorAlertDialog(AccountActivity.this, "Error", throwable.getMessage()).show();

                                super.onFailure(statusCode, headers, throwable, errorResponse);
                            }
                        }
                );
                break;

            default:
                mProgressDialog.dismiss();
                break;
        }
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

                CommonUtils.mCurrentSubActivity = this;
                CommonUtils.mHomeActivity.startActivityForResult(cameraIntent, CommonUtils.GALLERY_OPEN_FOR_PHOTO_REQUEST_CODE/*CAMERA_OPEN_FOR_CHAT_REQUEST_CODE*/ + mSelectedButtonID);
                CommonUtils.mbReadyToStop = false;
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

            CommonUtils.mCurrentSubActivity = this;
            CommonUtils.mHomeActivity.startActivityForResult(intent, CommonUtils.GALLERY_OPEN_FOR_PHOTO_REQUEST_CODE + mSelectedButtonID);
            CommonUtils.mbReadyToStop = false;
        }
    }

}
