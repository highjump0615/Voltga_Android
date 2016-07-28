package com.diyin.Voltga.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.diyin.Voltga.CurrentActivity;
import com.diyin.Voltga.R;
import com.diyin.Voltga.UserActivity;
import com.diyin.Voltga.data.NotificationObj;
import com.diyin.Voltga.utils.CommonUtils;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

public class NotifyAdapter extends BaseAdapter {

    public static final int NotificationValue_IsUnlock = 0x1;          // 0: Lock,     1: UnLock
    public static final int NotificationValue_IsBlock = 0x2;          // 0: UnBlock,  1: Block

    public static final int NotificationType_Mention = 0x1;
    public static final int NotificationType_Unlock = 0x2;
    public static final int NotificationType_Like = 0x3;

    private static LayoutInflater mInflater = null;
    private ArrayList<NotificationObj> mNotifyList;
    private Typeface mTypeface;
    private Context mContext;

    public NotifyAdapter(Context ctx, ArrayList<NotificationObj> values) {
        mInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mNotifyList = values;
        //mTypeface = Typeface.createFromAsset(ctx.getAssets(), "font/times_new_roman.ttf");
        mContext = ctx;
    }

    @Override
    public int getCount() {
        return mNotifyList.size();
    }

    @Override
    public Object getItem(int i) {
        return i;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        final NotificationObj notifyObj = mNotifyList.get(i);

        View vi = view;
        if (vi == null) {
            vi = mInflater.inflate(R.layout.layout_notify_item, null);
        }

        ImageView imgPhoto = (ImageView) vi.findViewById(R.id.img_photo);
        ImageView imgOnline = (ImageView) vi.findViewById(R.id.img_online);
        TextView textContent = (TextView) vi.findViewById(R.id.text_content);

        if (notifyObj.notification_fromuserobj != null && notifyObj.notification_fromuserobj.user_public_photo != null)
            ImageLoader.getInstance().displayImage(CommonUtils.getUserPhoto(notifyObj.notification_fromuserobj.user_public_photo, true), imgPhoto, CommonUtils.mImgOptions);

        if (notifyObj.notification_fromuserobj.user_is_active.equals(1)) {
            imgOnline.setVisibility(View.VISIBLE);
        } else {
            imgOnline.setVisibility(View.GONE);
        }

        String strMsg = "";
        if (notifyObj.notification_type == NotificationType_Like) {
            strMsg = " liked you";
        } else if (notifyObj.notification_type == NotificationType_Mention) {
            strMsg = " mentioned you";
        } else if (notifyObj.notification_type == NotificationType_Unlock) {
            strMsg = " unlocked you";
        }

        //textContent.setTypeface(mTypeface);
        textContent.setText(CommonUtils.getColorNormalString(mContext, notifyObj.notification_fromuserobj.user_name, strMsg));

        vi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CommonUtils.mSelectedUser = notifyObj.notification_fromuserobj;
                CommonUtils.moveNextActivity(CommonUtils.mHomeActivity, UserActivity.class, false);

                // TODO: check if read
                /*if (CommonUtils.mApplicationIconBadgeNumber > 0)
                    ((CurrentActivity) mContext).setNotificationCount(--CommonUtils.mApplicationIconBadgeNumber);*/
            }
        });

        imgPhoto.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                String strText = CommonUtils.mHomeActivity.mCurrentActivity.mChatFragment.mEditText.getText().toString() + "@" + notifyObj.notification_fromuserobj.user_name + " ";
                CommonUtils.mHomeActivity.mCurrentActivity.mChatFragment.mEditText.setText(strText);
                CommonUtils.mHomeActivity.mCurrentActivity.setSegmentIndex(CommonUtils.mHomeActivity.mCurrentActivity.CURRENT_CHAT);

                return true;
            }
        });

//        if (true) {
//            vi.setBackgroundResource(R.drawable.notification_exist_item_bg);
//        } else {
        vi.setBackgroundResource(R.drawable.place_item_bg);
//        }

        return vi;
    }

}
