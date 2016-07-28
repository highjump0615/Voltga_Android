package com.diyin.Voltga.data;

import com.diyin.Voltga.utils.CommonUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class UserObj {

    public Integer user_id;
    public String user_name;

    public String user_public_photo;
    public String user_private_photo1;
    public String user_private_photo2;
    public String user_private_photo3;

    public Integer user_age;
    public String user_height;
    public Double user_weight;
    public String user_ethnicity;
    public String user_body;
    public String user_practice;
    public String user_intro;
    public String user_status;
    public String user_phone;
    public String user_password;
    public String user_email;

    public Integer user_is_active = 0;
    public Integer user_place_id;

    public Integer user_relation_to = 0;       // self to other
    public Integer user_relation_from = 0;     // other to self

    public UserObj() {
    }

    public UserObj(JSONObject object) {
        try {
            user_id = object.getInt("user_id");
            user_name = object.getString("user_name");

            user_public_photo = object.getString("user_public_photo");
            user_private_photo1 = object.getString("user_private_photo1");
            user_private_photo2 = object.getString("user_private_photo2");
            user_private_photo3 = object.getString("user_private_photo3");

            user_age = object.getInt("user_age");
            user_height = object.getString("user_height");
            user_weight = object.getDouble("user_weight");
            user_ethnicity = object.getString("user_ethnicity");
            user_body = object.getString("user_body");
            user_practice = object.getString("user_practice");
            user_intro = object.getString("user_intro");
            user_status = object.getString("user_status");
            user_phone = object.getString("user_phone");
            user_password = object.getString("user_password");
            user_email = object.getString("user_email");

            user_is_active = object.getInt("user_is_active");
            user_place_id = object.getInt("user_place_id");

            if (object.has("user_relation_from"))
                user_relation_from = object.getInt("user_relation_from");
            if (object.has("user_relation_to"))
                user_relation_to = object.getInt("user_relation_to");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public UserObj currentUser() {
        UserObj newObj = new UserObj();

        newObj.user_id = user_id;
        newObj.user_name = user_name;
        newObj.user_age = user_age;
        newObj.user_height = user_height;
        newObj.user_weight = user_weight;
        newObj.user_ethnicity = user_ethnicity;
        newObj.user_body = user_body;
        newObj.user_practice = user_practice;
        newObj.user_intro = user_intro;
        newObj.user_status = user_status;
        newObj.user_phone = user_phone;
        newObj.user_password = user_password;
        newObj.user_email = user_email;
        newObj.user_is_active = user_is_active;

        newObj.user_public_photo = user_public_photo;
        newObj.user_private_photo1 = user_private_photo1;
        newObj.user_private_photo2 = user_private_photo2;
        newObj.user_private_photo3 = user_private_photo3;

        newObj.user_place_id = user_place_id;

        newObj.user_relation_from = user_relation_from;
        newObj.user_relation_to = user_relation_to;

        return newObj;
    }

    public void setUser(UserObj obj) {
        user_id = obj.user_id;
        user_name = obj.user_name;
        user_age = obj.user_age;
        user_height = obj.user_height;
        user_weight = obj.user_weight;
        user_ethnicity = obj.user_ethnicity;
        user_body = obj.user_body;
        user_practice = obj.user_practice;
        user_intro = obj.user_intro;
        user_status = obj.user_status;
        user_phone = obj.user_phone;
        user_password = obj.user_password;
        user_email = obj.user_email;
        user_is_active = obj.user_is_active;

        user_public_photo = obj.user_public_photo;
        user_private_photo1 = obj.user_private_photo1;
        user_private_photo2 = obj.user_private_photo2;
        user_private_photo3 = obj.user_private_photo3;

        user_place_id = obj.user_place_id;

        user_relation_from = obj.user_relation_from;
        user_relation_to = obj.user_relation_to;
    }

    public boolean lockedMe() {
        int nResult = user_relation_from.intValue() & CommonUtils.RelationType_IsUnlock;

        if (nResult == 0) {
            return true;
        } else {
            return false;
        }
    }

    public boolean lockedByMe() {
        int nResult = user_relation_to.intValue() & CommonUtils.RelationType_IsUnlock;

        if (nResult == 0) {
            return true;
        } else {
            return false;
        }
    }

    public boolean blockedByMe() {
        int nResult = user_relation_to.intValue() & CommonUtils.RelationType_IsBlock;

        if (nResult == 0) {
            return false;
        } else {
            return true;
        }
    }

    public boolean mentionedByMe() {
        /*int nResult = user_relation_to.intValue() & CommonUtils.RelationType_IsMention;

        if (nResult == 0) {
            return false;
        } else {
            return true;
        }*/
        return true;
    }

    public boolean likedByMe() {
        /*int nResult = user_relation_to.intValue() & CommonUtils.RelationType_IsLike;

        if (nResult == 0) {
            return false;
        } else {
            return true;
        }*/
        return true;
    }
}
