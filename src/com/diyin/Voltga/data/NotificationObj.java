package com.diyin.Voltga.data;

import org.json.JSONException;
import org.json.JSONObject;

public class NotificationObj {

    public Integer notification_id;
    public Integer notification_placeid;
    public Integer notification_fromid;
    public Integer notification_toid;
    public Integer notification_type;
    public String notification_message;
    public UserObj notification_fromuserobj;

    public NotificationObj(JSONObject object) {
        try {
            notification_id = object.getInt("notification_id");
            notification_placeid = object.getInt("notification_place_id");
            notification_fromid = object.getInt("notification_from_user_id");
            notification_toid = object.getInt("notification_to_user_id");
            notification_fromuserobj = new UserObj(object.getJSONObject("notification_fromuserobj"));
            notification_type = object.getInt("notification_type");
            //notification_message = object.getString("notification_message");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
