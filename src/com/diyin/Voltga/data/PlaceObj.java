package com.diyin.Voltga.data;

import com.diyin.Voltga.api.API_Manager;

import org.json.JSONException;
import org.json.JSONObject;

public class PlaceObj {

    public Integer place_id;
    public String place_name;
    public String place_street;
    public String place_city;
    public String place_state;
    public String place_zip;
    public String place_phone;
    public String place_picture;
    public UserObj place_user;

    public PlaceObj(JSONObject object) {
        try {
            place_id = object.getInt("place_id");
            place_name = object.getString("place_name");
            place_street = object.getString("place_street");
            place_city = object.getString("place_city");
            place_state = object.getString("place_state");
            place_zip = object.getString("place_zip");
            place_phone = object.getString("place_phone");
            place_picture = API_Manager.FILE_BASE_PATH + "place/" + object.getString("place_photo");

            Object userObject = object.get("user");
            if (userObject != null && userObject instanceof JSONObject)
                place_user = new UserObj(object.getJSONObject("user"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
