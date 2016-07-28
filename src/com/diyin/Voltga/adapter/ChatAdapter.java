package com.diyin.Voltga.adapter;

import android.content.Context;
import android.content.Intent;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.diyin.Voltga.PhotoActivity;
import com.diyin.Voltga.R;
import com.diyin.Voltga.UserActivity;
import com.diyin.Voltga.data.ChatObj;
import com.diyin.Voltga.utils.CommonUtils;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class ChatAdapter extends BaseAdapter implements View.OnClickListener {

    private static final String TAG = ChatAdapter.class.getSimpleName();

    private static LayoutInflater mInflater = null;

    private ArrayList<ChatObj> mChatList;
    public ArrayList<String> mUrlPhotoList = new ArrayList<String>();

    private Context mContext;

    public ChatAdapter(Context ctx, ArrayList<ChatObj> values) {
        mInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mChatList = values;
        mContext = ctx;
    }

    @Override
    public int getCount() {
        return mChatList.size();
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
        View vi = view;

        final ChatObj chatObj = mChatList.get(i);
        ChatObj nextObj = null;

        if (i < mChatList.size() - 1) nextObj = mChatList.get(i + 1);

        if (chatObj.chat_user_id.equals(CommonUtils.mSelfUser.user_id)) {
            vi = mInflater.inflate(R.layout.layout_chat_mine_item, null);
        } else {
            vi = mInflater.inflate(R.layout.layout_chat_other_item, null);
        }

        ImageView imgPhoto = (ImageView) vi.findViewById(R.id.img_photo);
        ImageLoader.getInstance().displayImage(CommonUtils.getUserPhoto(chatObj.chat_user.user_public_photo, true), imgPhoto, CommonUtils.mImgOptions);
        imgPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CommonUtils.mSelectedUser = chatObj.chat_user;
                CommonUtils.moveNextActivity(CommonUtils.mHomeActivity, UserActivity.class, false);
            }
        });

        imgPhoto.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                String strText = CommonUtils.mHomeActivity.mCurrentActivity.mChatFragment.mEditText.getText().toString() + "@" + chatObj.chat_user.user_name + " ";
                CommonUtils.mHomeActivity.mCurrentActivity.mChatFragment.mEditText.setText(strText);

                return true;
            }
        });

        TextView textChat = (TextView) vi.findViewById(R.id.text_chat);
        ImageView imgChat = (ImageView) vi.findViewById(R.id.img_chat);
        TextView textTime = (TextView) vi.findViewById(R.id.text_send_time);
        imgChat.setOnClickListener(this);

        if (chatObj.chat_type.equals(ChatObj.TYPE_TEXT)) {
            String strText = chatObj.chat_content;
            int nBracketStart = -1, nBracketEnd = 0;

            SpannableStringBuilder builder = new SpannableStringBuilder(strText);

            do {
                nBracketStart = strText.indexOf("@", nBracketEnd);

                if (nBracketStart >= 0) {
                    nBracketEnd = strText.indexOf(" ", nBracketStart);
                    if (nBracketEnd <= nBracketStart) break;

                    //CharSequence chatName = "@" + builder.subSequence(nBracketStart + 1, nBracketEnd);
                    //builder.replace(nBracketStart, nBracketEnd + 1, chatName);
                    //nBracketEnd--;

                    //ForegroundColorSpan blueSpan = new ForegroundColorSpan(Color.BLUE);
                    UnderlineSpan underlineSpan = new UnderlineSpan();
                    builder.setSpan(underlineSpan, nBracketStart, nBracketEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

            } while (nBracketStart >= 0);

            textChat.setText(builder);

            textChat.setVisibility(View.VISIBLE);
            imgChat.setVisibility(View.GONE);
        } else if (chatObj.chat_type.equals(ChatObj.TYPE_IMAGE)) {

            final float scale = mContext.getResources().getDisplayMetrics().density;
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) imgChat.getLayoutParams();
            layoutParams.width = (int) (chatObj.chat_image_width * scale + mContext.getResources().getDimension(R.dimen.chat_bubble_image_horizontal_padding));
            layoutParams.height = (int) (chatObj.chat_image_height * scale + mContext.getResources().getDimension(R.dimen.chat_bubble_image_vertical_padding));
            imgChat.setLayoutParams(layoutParams);

            ImageLoader.getInstance().displayImage(chatObj.chat_media_url, imgChat, CommonUtils.mImgOptions);
            imgChat.setTag(chatObj.chat_media_url);

            textChat.setVisibility(View.GONE);
            imgChat.setVisibility(View.VISIBLE);
        }

        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);

        long nowTime = calendar.getTimeInMillis();
        long curTime = chatObj.chat_created.getTime();

        if (nextObj != null) {
            // Calculate time space between two chat sending time.
            long nextTime = nextObj.chat_created.getTime();

            // check 10 minute
            if (nextTime - curTime >= 10 * 60 * 1000) {
                textTime.setText(CommonUtils.getFormattedDateString(chatObj.chat_created, "MM-dd-yyyy      HH:mm"));
                textTime.setVisibility(View.VISIBLE);
            } else {
                textTime.setVisibility(View.GONE);
            }
        } else if (nowTime - curTime >= 10 * 60 * 1000) {
            textTime.setText(CommonUtils.getFormattedDateString(chatObj.chat_created, "MM-dd-yyyy      HH:mm"));
            textTime.setVisibility(View.VISIBLE);
        } else {
            textTime.setVisibility(View.GONE);
        }

        return vi;
    }

    @Override
    public void onClick(View view) {
        int nId = view.getId();

        switch (nId) {
            case R.id.img_chat:

                mUrlPhotoList.clear();
                mUrlPhotoList.add((String) view.getTag());

                Intent intent = new Intent(CommonUtils.mHomeActivity, PhotoActivity.class);
                intent.putExtra("urlList", mUrlPhotoList);
                CommonUtils.mHomeActivity.startActivity(intent);
                break;
        }
    }

}
