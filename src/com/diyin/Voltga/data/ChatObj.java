package com.diyin.Voltga.data;

import android.text.TextUtils;

import com.diyin.Voltga.api.API_Manager;
import com.diyin.Voltga.utils.CommonUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class ChatObj {

    public static final int TYPE_TEXT = 0;
    public static final int TYPE_IMAGE = 1;

    public Integer chat_id;
    public Integer chat_place_id;
    public Integer chat_user_id;
    public String chat_msg_id;
    public Integer chat_type;
    public String chat_content;
    public String chat_media_url = "";
    public Date chat_created;
    public Integer chat_image_width;
    public Integer chat_image_height;

    public UserObj chat_user;

    public ChatObj() {
        chat_created = CommonUtils.currentDate();
    }

    public ChatObj(JSONObject object) {
        try {
            chat_id = object.getInt("chat_id");
            chat_place_id = object.getInt("chat_place_id");
            chat_user_id = object.getInt("chat_user_id");
            chat_msg_id = object.getString("chat_msg_id");
            chat_type = object.getInt("chat_type");
            chat_content = object.getString("chat_content");
            chat_created = CommonUtils.currentDate();

            if (!TextUtils.isEmpty(object.getString("chat_media_url"))) {
                chat_media_url = API_Manager.FILE_BASE_PATH + object.getString("chat_media_url");
            }

            //TimeZone timeZone = TimeZone.getDefault();

            // convert string to date
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            format.setTimeZone(TimeZone.getTimeZone("gmt"));

            try {
                chat_created = format.parse(object.getString("chat_created"));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            chat_user = new UserObj(object.getJSONObject("chat_user"));

            chat_image_width = object.getInt("chat_image_width");
            chat_image_height = object.getInt("chat_image_height");

        } catch (JSONException e) {
//            e.printStackTrace();
        }
    }

}
