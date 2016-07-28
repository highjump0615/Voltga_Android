package com.diyin.Voltga.api;

import android.graphics.Bitmap;

import com.diyin.Voltga.data.ChatObj;
import com.diyin.Voltga.data.UserObj;
import com.diyin.Voltga.utils.CommonUtils;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.io.ByteArrayInputStream;

import static com.diyin.Voltga.utils.CommonUtils.mCurrentLocation;

public class API_Manager {

    public static final String API_PATH = "http://54.169.70.202/index.php";
    public static final String FILE_BASE_PATH = "http://54.169.70.202/uploads/";

    public static final String SIGNIN_ACTION                = "signin";
    public static final String SIGNUP_ACTION                = "signup";
    public static final String GETUSER_ACTION               = "getUser";
    public static final String GETPASSWORD_ACTION           = "getPassword";
    public static final String SAVEUSERPROFILE_ACTION       = "saveUserProfile";
    public static final String UPLOADPUBLICPHOTO_ACTION     = "uploadPublicPhoto";
    public static final String UPLOADPRIVATEPHOTO_ACTION    = "uploadPrivatePhoto";
    public static final String GETPLACES_ACTION             = "getPlaces";
    public static final String SETCURRENTPLACE_ACTION       = "setCurrentPlace";
    public static final String GETPEOPLE_ACTION             = "getPeople";
    public static final String ADDNOTIFICATION_ACTION       = "addNotification";
    public static final String LIKEUSER_ACTION              = "likeUser";
    public static final String UNLOCKUSER_ACTION            = "unlockUser";
    public static final String BLOCKUSER_ACTION             = "blockUser";
    public static final String GETBADGES_ACTION             = "getBadges";
    public static final String REMOVEBADGES_ACTION          = "removeBadges";
    public static final String GETNOTIFICATIONS_ACTION      = "getNotifications";
    public static final String GETALLCHATS_ACTION           = "getAllChats";
    public static final String UPLOADMEDIAONLY_ACTION       = "uploadMediaOnly";
    public static final String SENDCHAT_ACTION              = "sendChat";
    public static final String GETBASECHATNO_ACTION         = "getBaseChatNo";
    public static final String SETONLINESTATE_ACTION        = "setOnlineState";

    // WebAPI return objects.
    public static final String WEBAPI_RETURN_RESULT = "result";
    public static final String WEBAPI_RETURN_MESSAGE = "message";
    public static final String WEBAPI_RETURN_VALUES = "values";

    public static final String WEBAPI_RETURN_SUCCESS = "SUCCEED";
    public static final String WEBAPI_RETURN_FAILED = "FAILED";


    private static API_Manager mInstance = null;

    public static API_Manager getInstance() {
        if (mInstance == null) {
            mInstance = new API_Manager();
        }

        return mInstance;
    }

    public void userSignUpWithUserName(String user_name,
                                       String user_email,
                                       String user_password,
                                       String user_reg_id,
                                       Bitmap public_photo,
                                       Bitmap public_photo_thumb,
                                       AsyncHttpResponseHandler responseHandler) {

        RequestParams params = new RequestParams();
        params.put("action", SIGNUP_ACTION);
        params.put("user_name", user_name);
        params.put("user_email", user_email);
        params.put("user_password", user_password);
        params.put("user_reg_id", user_reg_id);

        sendToServiceByPost(API_PATH, params, public_photo, public_photo_thumb, responseHandler);
    }

    public void userSignInWithUserEmail(String user_email,
                                        String user_password,
                                        String user_reg_id,
                                        AsyncHttpResponseHandler responseHandler) {

        RequestParams params = new RequestParams();
        params.put("action", SIGNIN_ACTION);
        params.put("user_email", user_email);
        params.put("user_password", user_password);
        params.put("user_reg_id", user_reg_id);

        sendToServiceByPost(API_PATH, params, responseHandler);
    }

    public void getUserWithUserID(String user_id,
                                  AsyncHttpResponseHandler responseHandler) {

        RequestParams params = new RequestParams();
        params.put("action", GETUSER_ACTION);
        params.put("user_id", user_id);

        sendToServiceByPost(API_PATH, params, responseHandler);
    }

    public void getPlaces(AsyncHttpResponseHandler responseHandler) {
        RequestParams params = new RequestParams();
        params.put("action", GETPLACES_ACTION);

        if (mCurrentLocation != null) {
            params.put("place_latitude", mCurrentLocation.getLatitude());
            params.put("place_longitude", mCurrentLocation.getLongitude());
        }

        sendToServiceByPost(API_PATH, params, responseHandler);
    }

    public void getPasswordWithUserEmail(String user_email,
                                         AsyncHttpResponseHandler responseHandler) {
        RequestParams params = new RequestParams();
        params.put("action", GETPASSWORD_ACTION);
        params.put("user_email", user_email);

        sendToServiceByPost(API_PATH, params, responseHandler);
    }

    public void uploadPublicPhotoWithUserName(String user_name,
                                              String user_public_photo_old,
                                              Bitmap public_photo,
                                              Bitmap public_photo_thumb,
                                              AsyncHttpResponseHandler responseHandler) {
        RequestParams params = new RequestParams();
        params.put("action", UPLOADPUBLICPHOTO_ACTION);
        params.put("user_name", user_name);
        params.put("user_public_photo", user_public_photo_old);

        sendToServiceByPost(API_PATH, params, public_photo, public_photo_thumb, responseHandler);
    }

    public void uploadPrivatePhotoWithUserName(String user_name,
                                               String user_private_photo_old,
                                               String photo_index,
                                               Bitmap private_photo,
                                               Bitmap private_photo_thumb,
                                               AsyncHttpResponseHandler responseHandler) {
        RequestParams params = new RequestParams();
        params.put("action", UPLOADPRIVATEPHOTO_ACTION);
        params.put("user_name", user_name);
        params.put("user_private_photo", user_private_photo_old);
        params.put("photo_index", photo_index);

        sendToServiceByPost(API_PATH, params, private_photo, private_photo_thumb, responseHandler);
    }

    public void addNotificationWithUserID(String user_id,
                                          String opponent_id,
                                          String place_id,
                                          String notification_type,
                                          AsyncHttpResponseHandler responseHandler) {
        RequestParams params = new RequestParams();
        params.put("action", ADDNOTIFICATION_ACTION);
        params.put("user_id", user_id);
        params.put("opponent_id", opponent_id);
        params.put("place_id", place_id);
        params.put("notification_type", notification_type);

        sendToServiceByPost(API_PATH, params, responseHandler);
    }

    public void saveUserProfileWithUserObj(UserObj userObj,
                                           AsyncHttpResponseHandler responseHandler) {
        RequestParams params = new RequestParams();
        params.put("action", SAVEUSERPROFILE_ACTION);
        params.put("user_id", userObj.user_id.toString());
        params.put("user_name", userObj.user_name);
        params.put("user_age", userObj.user_age.toString());
        params.put("user_height", userObj.user_height);
        params.put("user_weight", userObj.user_weight.toString());
        params.put("user_ethnicity", userObj.user_ethnicity);
        params.put("user_body", userObj.user_body);
        params.put("user_practice", userObj.user_practice);
        params.put("user_intro", userObj.user_intro);
        params.put("user_status", userObj.user_status);
        params.put("user_phone", userObj.user_phone);
        params.put("user_password", userObj.user_password);
        params.put("user_email", userObj.user_email);
        params.put("user_place_id", userObj.user_place_id.toString());

        sendToServiceByPost(API_PATH, params, responseHandler);
    }

    public void getPeopleWithUserID(String user_id,
                                    String place_id,
                                    AsyncHttpResponseHandler responseHandler) {
        RequestParams params = new RequestParams();
        params.put("action", GETPEOPLE_ACTION);
        params.put("user_id", user_id);
        params.put("place_id", place_id);

        sendToServiceByPost(API_PATH, params, responseHandler);
    }

    public void likeWithUserID(String user_id,
                               String user_name,
                               String opponent_id,
                               String place_id,
                               String notify_value,
                               AsyncHttpResponseHandler responseHandler) {

        RequestParams params = new RequestParams();
        params.put("action", LIKEUSER_ACTION);
        params.put("user_id", user_id);
        params.put("user_name", user_name);
        params.put("opponent_id", opponent_id);
        params.put("place_id", place_id);
        params.put("notify_value", notify_value);

        sendToServiceByPost(API_PATH, params, responseHandler);
    }

    public void unlockWithUserID(String user_id,
                                 String user_name,
                                 String opponent_id,
                                 String place_id,
                                 String notify_value,
                                 String unlock_type,
                                 AsyncHttpResponseHandler responseHandler) {
        RequestParams params = new RequestParams();
        params.put("action", UNLOCKUSER_ACTION);
        params.put("user_id", user_id);
        params.put("user_name", user_name);
        params.put("opponent_id", opponent_id);
        params.put("place_id", place_id);
        params.put("notify_value", notify_value);
        params.put("unlock_type", unlock_type);

        sendToServiceByPost(API_PATH, params, responseHandler);
    }

    public void blockWithUserID(String user_id,
                                String opponent_id,
                                String place_id,
                                String notify_value,
                                String block_type,
                                AsyncHttpResponseHandler responseHandler) {
        RequestParams params = new RequestParams();
        params.put("action", BLOCKUSER_ACTION);
        params.put("user_id", user_id);
        params.put("opponent_id", opponent_id);
        params.put("place_id", place_id);
        params.put("notify_value", notify_value);
        params.put("unlock_type", block_type);

        sendToServiceByPost(API_PATH, params, responseHandler);
    }

    public void getBadgesWithUserID(String user_id,
                                    String place_id,
                                    AsyncHttpResponseHandler responseHandler) {
        RequestParams params = new RequestParams();
        params.put("action", GETBADGES_ACTION);
        params.put("user_id", user_id);
        params.put("place_id", place_id);

        sendToServiceByPost(API_PATH, params, responseHandler);
    }

    public void removeBadgesWithUserID(String user_id,
                                       String place_id,
                                       AsyncHttpResponseHandler responseHandler) {
        RequestParams params = new RequestParams();
        params.put("action", REMOVEBADGES_ACTION);
        params.put("user_id", user_id);
        params.put("place_id", place_id);

        sendToServiceByPost(API_PATH, params, responseHandler);
    }

    public void getNotificationsWithUserID(String user_id,
                                           String place_id,
                                           AsyncHttpResponseHandler responseHandler) {
        RequestParams params = new RequestParams();
        params.put("action", GETNOTIFICATIONS_ACTION);
        params.put("user_id", user_id);
        params.put("place_id", place_id);

        sendToServiceByPost(API_PATH, params, responseHandler);
    }

    public void getBaseChatNoWithPlaceID(String place_id,
                                         String base_no,
                                         AsyncHttpResponseHandler responseHandler) {
        RequestParams params = new RequestParams();
        params.put("action", GETBASECHATNO_ACTION);
        params.put("place_id", place_id);
        params.put("base_no", base_no);

        sendToServiceByPost(API_PATH, params, responseHandler);
    }

    public void getAllChatsWithUserID(String user_id,
                                      String place_id,
                                      String base_id,
                                      AsyncHttpResponseHandler responseHandler) {
        RequestParams params = new RequestParams();
        params.put("action", GETALLCHATS_ACTION);
        params.put("user_id", user_id);
        params.put("place_id", place_id);
        params.put("base_id", base_id);

        sendToServiceByPost(API_PATH, params, responseHandler);
    }

    public void uploadMediaOnlyWithUserID(String user_id,
                                          String place_id,
                                          Integer media_type,
                                          Bitmap media_data,
                                          AsyncHttpResponseHandler responseHandler) {
        RequestParams params = new RequestParams();
        params.put("action", UPLOADMEDIAONLY_ACTION);
        params.put("user_id", user_id);
        params.put("place_id", place_id);

        sendToServiceByPost(API_PATH, params, media_data, null, responseHandler);
    }

    public void setCurrentPlaceWithUserID(String user_id,
                                          String place_id,
                                          AsyncHttpResponseHandler responseHandler) {
        RequestParams params = new RequestParams();
        params.put("action", SETCURRENTPLACE_ACTION);
        params.put("user_id", user_id);
        params.put("place_id", place_id);

        sendToServiceByPost(API_PATH, params, responseHandler);
    }

    public void sendChatWithChatObj(ChatObj chat_obj,
                                    AsyncHttpResponseHandler responseHandler) {
        RequestParams params = new RequestParams();
        params.put("action", SENDCHAT_ACTION);
        params.put("chat_place_id", chat_obj.chat_place_id.toString());
        params.put("chat_user_id", chat_obj.chat_user_id.toString());
        params.put("chat_msg_id", "'" + chat_obj.chat_msg_id + "'");
        params.put("chat_type", chat_obj.chat_type.toString());
        params.put("chat_content", "'" + chat_obj.chat_content + "'");
        params.put("chat_media_url", "'" + chat_obj.chat_media_url + "'");
        params.put("chat_user_name", chat_obj.chat_user.user_name);

        sendToServiceByPost(API_PATH, params, responseHandler);
    }

    public void setUserOnlineWithUserID(String user_id,
                                        String state,
                                        AsyncHttpResponseHandler responseHandler) {
        RequestParams params = new RequestParams();
        params.put("action", SETONLINESTATE_ACTION);
        params.put("user_id", user_id);
        params.put("user_is_active", state);

        sendToServiceByPost(API_PATH, params, responseHandler);
    }

    private void sendToServiceByPost(String serviceAPIURL,
                                     RequestParams params,
                                     Bitmap media,
                                     Bitmap mediaThumb,
                                     AsyncHttpResponseHandler responseHandler) {

        AsyncHttpClient client = new AsyncHttpClient();

        if (media != null) {
            params.put("photo", new ByteArrayInputStream(CommonUtils.compressBitmap(media, 100 * 1024)), "photo");
        }

        if (mediaThumb != null) {
            params.put("photo_thumb", new ByteArrayInputStream(CommonUtils.compressBitmap(mediaThumb, 10 * 1024)), "photo_thumb");
        }

        client.post(serviceAPIURL, params, responseHandler);
    }

    private void sendToServiceByPost(String serviceAPIURL,
                                     RequestParams params,
                                     AsyncHttpResponseHandler responseHandler) {
        sendToServiceByPost(serviceAPIURL, params, null, null, responseHandler);
    }

}
