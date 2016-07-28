package com.diyin.Voltga.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;

import com.diyin.Voltga.HomeActivity;
import com.diyin.Voltga.R;
import com.diyin.Voltga.api.API_Manager;
import com.diyin.Voltga.data.PlaceObj;
import com.diyin.Voltga.data.UserObj;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.nostra13.universalimageloader.core.DisplayImageOptions;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class CommonUtils {

    private static final String TAG = CommonUtils.class.getSimpleName();

    public static final int LOCATION_SETTING_CODE = 120;
    public static final int GALLERY_OPEN_FOR_PHOTO_REQUEST_CODE = 100;
    public static final int GALLERY_OPEN_FOR_CHAT_REQUEST_CODE = 110;
    public static final int CAMERA_OPEN_FOR_CHAT_REQUEST_CODE = 120;
    public static final int CROP_OPEN_FOR_CHAT_REQUEST_CODE = 130;


    public static HomeActivity mHomeActivity;
    public static Activity mCurrentSubActivity = null;

    public static UserObj mSelfUser = null;
    public static UserObj mSelectedUser = null;

    public static PlaceObj mCurrentPlace = null;
    public static int mnCurrentChatBaseNo = -1;

    public static boolean mbChangedPlace = false;
    public static boolean mbChangedPlaceNotification = false;
    public static boolean mbChangedPlaceChat = false;

    public static boolean mbReadyToStop = true;

    public static ArrayList<PlaceObj> mPlaceList = new ArrayList<PlaceObj>();

    public static final String PREF_NAME = "voltga_pref";
    public static final String PREF_USERID = "voltga_userid";

    public static Location mCurrentLocation = null;

    public static int mBadgeNumber = 0;
    public static int mApplicationIconBadgeNumber = 0;

    /*
     *  RelationType enum
     */
    public static final int RelationType_IsUnlock = 0x1;      // 0: Lock,     1: UnLock
    public static final int RelationType_IsBlock = 0x2;      // 0: UnBlock,  1: Block

    public static DisplayImageOptions mImgOptions;


    public interface OnTabActivityResultListener {
        public void onTabActivityResult(int requestCode, int resultCode, Intent data);
    }

    /**
     * Move to destination activity class with animate transition.
     */
    public static void moveNextActivity(Activity source, Class<?> destinationClass, boolean removeSource) {
        Intent intent = new Intent(source, destinationClass);

        if (removeSource) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        }

        source.startActivity(intent);

        if (removeSource) {
            source.finish();
        }

        source.overridePendingTransition(R.anim.anim_in, R.anim.anim_out);

        if (source == mHomeActivity) {
            CommonUtils.mbReadyToStop = false;
        }
    }

    /**
     * directory name to store captured images and videos
     */
    private static final String IMAGE_DIRECTORY_NAME = "voltga_captured_image";

    public static File getOutputMediaFile(Context context) {
        // External sdcard location
//        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), IMAGE_DIRECTORY_NAME);
//
//        // Create the storage directory if it does not exist
//        if (!mediaStorageDir.exists())
//        {
//            if (!mediaStorageDir.mkdirs())
//            {
//                Log.d(TAG, "Oops! Failed create " + IMAGE_DIRECTORY_NAME + " directory");
//                return null;
//            }
//        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
//        File mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
        File mediaFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + "IMG_" + timeStamp + ".jpg");

        return mediaFile;
    }

    public static String getMimeType(Context context, Uri fileUri) {
        ContentResolver cr = context.getContentResolver();
        String mimeType = cr.getType(fileUri);

        Log.d(TAG, "returned mime_type = " + mimeType);
        return mimeType;
    }

    /**
     * Convert image uri to file
     */
    public static String/*File*/ convertImageUriToFile(Context context, Uri imageUri) {
        Cursor cursor = null;
        try {
            String[] projection = {MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID/*, MediaStore.Images.ImageColumns.ORIENTATION*/};
            cursor = context.getContentResolver().query(
                    imageUri,
                    projection, // Which columns to return
                    null,       // WHERE clause; which rows to return (all rows)
                    null,       // WHERE clause selection arguments (none)
                    null);      // Order-by clause (ascending by name)

            int file_ColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            //int orientation_ColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.ORIENTATION);

            if (cursor.moveToFirst()) {
                //String orientation = cursor.getString(orientation_ColumnIndex);
                return cursor.getString(file_ColumnIndex)/*new File(cursor.getString(file_ColumnIndex))*/;
            }
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * Get bitmap from internal image file.
     */
    public static Bitmap getBitmapFromUri(Uri fileUri) {
        // bitmap factory
//        BitmapFactory.Options options = new BitmapFactory.Options();
//
//        // downsizing photoImage as it throws OutOfMemory Exception for larger
//        // images
//        options.inSampleSize = 8;
//        options.inMutable = true;
//
//        return BitmapFactory.decodeFile(fileUri.getPath(), options);

        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        // 开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(fileUri.getPath(), newOpts);// 此时返回bm为空

        if (bitmap != null)
            bitmap.recycle();

        newOpts.inJustDecodeBounds = false;
        newOpts.inPurgeable = true;
        newOpts.inInputShareable = true;

        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        // 现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
        float hh = 480f;// 这里设置高度为800f
        float ww = 480f;// 这里设置宽度为480f
        // 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;// be=1表示不缩放
        if (w >= h && w > ww) {// 如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {// 如果高度高的话根据宽度固定大小缩放
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;// 设置缩放比例
        // 重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        bitmap = BitmapFactory.decodeFile(fileUri.getPath(), newOpts);

        return bitmap;
    }


    /**
     * Create error AlertDialog.
     */
    public static Dialog createErrorAlertDialog(final Context context, String title, String message) {
        return new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).create();
    }

    public static String getUserPhoto(String filename, boolean bThumbnail) {

        String strUrl = "";

        if (!filename.equals("null") && filename.length() > 0) {
            if (bThumbnail) {
                strUrl = API_Manager.FILE_BASE_PATH + "user/thumb/" + filename;
            } else {
                strUrl = API_Manager.FILE_BASE_PATH + "user/" + filename;
            }
        }

        return strUrl;
    }

    public static Bitmap getThumbnail(Bitmap origin) {
        Bitmap bmpThumbnail = origin;

        int nImgWidth = origin.getWidth();
        int nImgHeight = origin.getHeight();

        if (nImgWidth > 150 || nImgHeight > 150) {
            bmpThumbnail = Bitmap.createScaledBitmap(origin, 150, 150, false);

//            bmpThumbnail = compressBitmap(bmpThumbnail, 10 * 1024);
        }

        return bmpThumbnail;
    }

    public static byte[] compressBitmap(Bitmap origin, int nTargetSize) {
        int nRatio = 90;

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        origin.compress(Bitmap.CompressFormat.JPEG, 100, stream);

        int nSize = stream.toByteArray().length;

        while (nSize > nTargetSize) {
            stream.reset();
            origin.compress(Bitmap.CompressFormat.JPEG, nRatio, stream);
            nRatio -= 10;

            nSize = stream.toByteArray().length;
        }

        return stream.toByteArray();
    }

    public static void deleteFile(String strFilePath) {
        File file = new File(strFilePath);
        file.delete();
    }

    public static void setOnlineState(int bOnline) {

        if (bOnline != mSelfUser.user_is_active) {
            mSelfUser.user_is_active = bOnline;
            API_Manager.getInstance().setUserOnlineWithUserID(
                    mSelfUser.user_id.toString(),
                    mSelfUser.user_is_active.toString(),
                    new JsonHttpResponseHandler() {
                    }
            );
        }
    }

    public static Date currentDate() {
        Date currentDate = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone("gmt"));
        String strTime = format.format(currentDate);

        SimpleDateFormat formatLocal = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try {
            currentDate = formatLocal.parse(strTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return currentDate;
    }

    /**
     * Get the string that are represents "<u>underlineStr</u> normalStr"
     */
    public static SpannableString getNormalUnderlineString(String normalStr, String underlineStr) {
        SpannableString ss;

        if (TextUtils.isEmpty(normalStr))
            normalStr = "";

        if (TextUtils.isEmpty(underlineStr)) {
            ss = new SpannableString(normalStr);
        } else {
            int pos = underlineStr.length();

            ss = new SpannableString(normalStr + underlineStr);
            //ss.setSpan(new ForegroundColorSpan(Color.WHITE), 0, pos, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ss.setSpan(new UnderlineSpan(), normalStr.length(), normalStr.length() + underlineStr.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            //ss.setSpan(new ForegroundColorSpan(Color.GRAY), pos + 2, boldStr.length() + normalStr.length() + 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return ss;
    }

    /**
     * Get the string that are represents "<b>colorStr</b> normalStr"
     */
    public static SpannableString getColorNormalString(Context context, String colorStr, String normalStr) {
        SpannableString ss;

        if (TextUtils.isEmpty(colorStr)) {
            ss = new SpannableString(normalStr);
        } else {
            int pos = colorStr.length();

            ss = new SpannableString(colorStr + " " + normalStr);
            ss.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.tint_color)), 0, pos, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ss.setSpan(new StyleSpan(Typeface.BOLD), 0, pos, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ss.setSpan(new ForegroundColorSpan(Color.GRAY), pos + 1, colorStr.length() + normalStr.length() + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return ss;
    }

    /**
     * @param date
     * @return
     */
    public static String getFormattedDateString(Date date, String format) {
        Calendar cal = Calendar.getInstance();
        TimeZone timeZone = cal.getTimeZone();

        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        dateFormat.setTimeZone(timeZone);

        return dateFormat.format(date);
    }

    public static boolean isIntentAvailable(Context context, Intent intent) {
        final PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, 0/*PackageManager.MATCH_DEFAULT_ONLY*/);

        return list.size() > 0;
    }

}
