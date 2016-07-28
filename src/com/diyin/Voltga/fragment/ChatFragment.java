package com.diyin.Voltga.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import com.baoyz.actionsheet.ActionSheet;
import com.diyin.Voltga.CurrentActivity;
import com.diyin.Voltga.R;
import com.diyin.Voltga.adapter.ChatAdapter;
import com.diyin.Voltga.api.API_Manager;
import com.diyin.Voltga.data.ChatObj;
import com.diyin.Voltga.utils.CommonUtils;
import com.diyin.Voltga.utils.SystemHelper;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

public class ChatFragment extends Fragment implements View.OnClickListener, ActionSheet.ActionSheetListener {

    private static final String TAG = ChatFragment.class.getSimpleName();

    private CurrentActivity mActivity;

    public EditText mEditText;
    private Button mButtonSend;
    private ImageView mButtonPhoto;

    private PullToRefreshListView mPullRefreshListView;
    private ChatAdapter mAdapter;

    private int mnBaseId;

    public boolean mbLoop = false;
    public boolean mbInLoadMore = false;
    private ArrayList<ChatObj> maryMessage = new ArrayList<ChatObj>();

    private boolean mbReadMore;
    private boolean mbReadMore_;
    private int mnChats = 0;

    private Handler mHandler = new Handler();

    private ProgressDialog mProgressDialog;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (CurrentActivity) activity;
        mActivity.mChatFragment = this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        mbReadMore = mbReadMore_ = false;

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mButtonSend = (Button) view.findViewById(R.id.but_send);
        mButtonSend.setOnClickListener(this);
        mEditText = (EditText) view.findViewById(R.id.edit_text);
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (mEditText.getText().toString().length() > 0) {
                    mButtonSend.setEnabled(true);

//                    String strText = mEditText.getText().toString();
//                    int nBracketStart = -1, nBracketEnd = 0;
//
//                    SpannableStringBuilder builder = new SpannableStringBuilder(strText);
//
//                    do {
//                        nBracketStart = strText.indexOf("[", nBracketEnd);
//
//                        if (nBracketStart >= 0) {
//                            nBracketEnd = strText.indexOf("]", nBracketStart);
//                            ForegroundColorSpan blueSpan = new ForegroundColorSpan(Color.BLUE);
//                            builder.setSpan(blueSpan, nBracketStart, nBracketEnd + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//                        }
//
//                    } while (nBracketStart >= 0);
//
//                    mEditText.setText(builder);
                } else {
                    mButtonSend.setEnabled(false);
                }
            }
        });

        mButtonPhoto = (ImageView) view.findViewById(R.id.img_Photo);
        mButtonPhoto.setOnClickListener(this);

        mPullRefreshListView = (PullToRefreshListView) view.findViewById(R.id.pull_refresh_list);
        mAdapter = new ChatAdapter(view.getContext(), maryMessage);
        mPullRefreshListView.setAdapter(mAdapter);

        mPullRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {

            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                API_Manager.getInstance().getBaseChatNoWithPlaceID(
                        CommonUtils.mSelfUser.user_place_id.toString(),
                        Integer.toString(CommonUtils.mnCurrentChatBaseNo),
                        new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                try {
                                    String strRes = response.getString(API_Manager.WEBAPI_RETURN_RESULT);

                                    if (strRes.equals(API_Manager.WEBAPI_RETURN_SUCCESS)) {
                                        mnBaseId = response.getJSONObject(API_Manager.WEBAPI_RETURN_VALUES).getInt("base_chat_id");
                                        CommonUtils.mnCurrentChatBaseNo = mnBaseId;
                                        mbReadMore = true;
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                super.onSuccess(statusCode, headers, response);
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                                super.onFailure(statusCode, headers, throwable, errorResponse);

                                mPullRefreshListView.onRefreshComplete();
//                                CommonUtils.createErrorAlertDialog(mActivity, "Error", throwable.getMessage()).show();
                            }
                        }
                );
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        if (CommonUtils.mbChangedPlaceChat) {
            maryMessage.clear();
            mnChats = 0;
            mbReadMore = mbReadMore_ = false;
            CommonUtils.mbChangedPlaceChat = false;
            maryMessage.clear();
        }

        if (CommonUtils.mnCurrentChatBaseNo < 0) {
            startGetMessage();
        }
    }

    @Override
    public void onClick(View view) {

        int nId = view.getId();

        switch (nId) {
            case R.id.img_Photo:

                ActionSheet.createBuilder(mActivity, mActivity.mFragmentManager)
                        .setCancelButtonTitle("Cancel")
                        .setOtherButtonTitles("Photo Album", "Camera          ")
                        .setOtherButtonImageResources(R.drawable.album_icon, R.drawable.camera_icon)
                        .setCancelableOnTouchOutside(true)
                        .setListener(this)
                        .show();

                break;

            case R.id.but_send:
                onSendText();
                break;
        }
    }

    @Override
    public void onDismiss(ActionSheet actionSheet, boolean isCancel) {

    }

    @Override
    public void onOtherButtonClick(ActionSheet actionSheet, int index) {
//        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//        startActivityForResult(intent, CommonUtils.GALLERY_OPEN_FOR_CHAT_REQUEST_CODE);

        if (index == 1) { // camera
            try {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                cameraIntent.putExtra("return-data", true/*return_data*/);
                cameraIntent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());

                CommonUtils.mCurrentSubActivity = mActivity;
                CommonUtils.mHomeActivity.startActivityForResult(cameraIntent, CommonUtils.CAMERA_OPEN_FOR_CHAT_REQUEST_CODE);
                CommonUtils.mbReadyToStop = false;
            } catch (ActivityNotFoundException anfe) {
                CommonUtils.createErrorAlertDialog(getActivity(), "Alert", "Your device doesn't support capturing images!").show();
            }
        } else if (index == 0) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
            intent.setType("image/*");

            CommonUtils.mCurrentSubActivity = mActivity;
            CommonUtils.mHomeActivity.startActivityForResult(Intent.createChooser(intent, "Select Picture"), CommonUtils.GALLERY_OPEN_FOR_CHAT_REQUEST_CODE);
            CommonUtils.mbReadyToStop = false;
        }
    }

    public void startGetMessage() {
        mbLoop = true;

        if (CommonUtils.mnCurrentChatBaseNo < 0) {
            API_Manager.getInstance().getBaseChatNoWithPlaceID(
                    CommonUtils.mSelfUser.user_place_id.toString(),
                    "",
                    new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            try {
                                String strRes = response.getString(API_Manager.WEBAPI_RETURN_RESULT);

                                if (strRes.equals(API_Manager.WEBAPI_RETURN_SUCCESS)) {
                                    mnBaseId = response.getJSONObject(API_Manager.WEBAPI_RETURN_VALUES).getInt("base_chat_id");
                                    CommonUtils.mnCurrentChatBaseNo = mnBaseId;
                                    getMessage();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            super.onSuccess(statusCode, headers, response);
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            super.onFailure(statusCode, headers, throwable, errorResponse);
                        }
                    }
            );
        } else {
            getMessage();
        }
    }

    public void stopGetMessage() {
        mbLoop = false;
    }

    public void getMessage() {

        if (mbReadMore) {
            mbReadMore = false;
            mbReadMore_ = true;
        }

        final int nOldPlaceId = CommonUtils.mSelfUser.user_place_id;

        API_Manager.getInstance().getAllChatsWithUserID(
                CommonUtils.mSelfUser.user_id.toString(),
                CommonUtils.mSelfUser.user_place_id.toString(),
                Integer.toString(CommonUtils.mnCurrentChatBaseNo),
                new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

//                        Log.d(TAG, "getMessage: Success");

                        try {
                            String strRes = response.getString(API_Manager.WEBAPI_RETURN_RESULT);

                            if (CommonUtils.mSelfUser.user_place_id != nOldPlaceId) {
                                return;
                            }

                            if (strRes.equals(API_Manager.WEBAPI_RETURN_SUCCESS)) {

                                JSONArray chatArray = response.getJSONObject(API_Manager.WEBAPI_RETURN_VALUES).getJSONArray("chats");

                                for (int i = 0; i < chatArray.length(); i++) {

                                    JSONObject jsonObject = chatArray.getJSONObject(i);

                                    ChatObj newObj = new ChatObj(jsonObject);

                                    boolean bAdd = true;
                                    for (ChatObj chatObj : maryMessage) {
                                        if (chatObj.chat_msg_id.equals(newObj.chat_msg_id)) {
                                            chatObj.chat_created = newObj.chat_created;
                                            bAdd = false;
                                            break;
                                        }
                                    }

                                    if (bAdd) {
                                        //
                                        // add object according to its time
                                        //

                                        int nIndex;
                                        for (nIndex = 0; nIndex < maryMessage.size(); nIndex++) {
                                            ChatObj cObj = maryMessage.get(nIndex);
                                            if (cObj.chat_created.compareTo(newObj.chat_created) > 0) {
                                                break;
                                            }
                                        }

                                        maryMessage.add(nIndex, newObj);
                                    }
                                }

                                if (maryMessage.size() > 0) {
                                    reloadList();
                                }
                            }

                            if (mbLoop) {
//                                Log.d(TAG, "calling mGetMessageTask");
                                continueGetMsg();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        super.onSuccess(statusCode, headers, response);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        super.onFailure(statusCode, headers, throwable, errorResponse);
                        Log.d(TAG, "getMessage: Failed");

//                        CommonUtils.createErrorAlertDialog(mActivity, "Error", throwable.getMessage()).show();

                        if (mbLoop) {
                            continueGetMsg();
                        }
                    }
                }
        );
    }

    private Runnable mGetMsgTask = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "Delayed 500ms");
            getMessage();
        }
    };

    private void continueGetMsg() {
        mHandler.postDelayed(mGetMsgTask, 500);
    }

    private void reloadList() {
        mAdapter.notifyDataSetChanged();

        if (mnChats != maryMessage.size() && maryMessage.size() != 0) {
            mnChats = maryMessage.size();
            if (!mbReadMore_) {
                mPullRefreshListView.setSelection(maryMessage.size() - 1);
            } else {
                mbReadMore_ = false;
                mPullRefreshListView.onRefreshComplete();
            }
        } else if (mbReadMore_) {
            mPullRefreshListView.onRefreshComplete();
        }
    }

    private void onSendText() {

        String strText = mEditText.getText().toString();

        if (strText.length() > 140) {
            strText = strText.substring(0, 139);
        }

        final ChatObj chatObj = new ChatObj();
        chatObj.chat_user = CommonUtils.mSelfUser;
        chatObj.chat_user_id = CommonUtils.mSelfUser.user_id;
        chatObj.chat_place_id = CommonUtils.mSelfUser.user_place_id;
        chatObj.chat_type = ChatObj.TYPE_TEXT;

        // convert date to string
        Date currentDate = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        format.setTimeZone(TimeZone.getTimeZone("gmt"));
        String strTime = format.format(currentDate);

        chatObj.chat_msg_id = chatObj.chat_user_id.toString() + "_" + strTime;
        chatObj.chat_content = strText;

        API_Manager.getInstance().sendChatWithChatObj(
                chatObj,
                new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        super.onSuccess(statusCode, headers, response);

                        chatObj.chat_created = CommonUtils.currentDate();
                        Log.d(TAG, "sendChatWithChatOb: Success: " + chatObj.chat_created.toString());
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        super.onFailure(statusCode, headers, throwable, errorResponse);
                        Log.d(TAG, "sendChatWithChatOb: Failed");
                    }
                }
        );

        mEditText.setText("");
        mPullRefreshListView.requestFocus();
        SystemHelper.hideKeyboard(mActivity, mEditText);

        maryMessage.add(chatObj);
        reloadList();
    }

    private void sendPhoto(String media_url, int nWidth, int nHeight) {
        final ChatObj chatObj = new ChatObj();
        chatObj.chat_user = CommonUtils.mSelfUser;
        chatObj.chat_user_id = CommonUtils.mSelfUser.user_id;
        chatObj.chat_place_id = CommonUtils.mSelfUser.user_place_id;
        chatObj.chat_type = ChatObj.TYPE_IMAGE;

        // convert date to string
        Date currentDate = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        format.setTimeZone(TimeZone.getTimeZone("gmt"));
        String strTime = format.format(currentDate);

        chatObj.chat_msg_id = chatObj.chat_user_id.toString() + "_" + strTime;
        chatObj.chat_content = "Sent Photo";
        chatObj.chat_media_url = media_url;
        chatObj.chat_image_width = nWidth;
        chatObj.chat_image_height = nHeight;

        API_Manager.getInstance().sendChatWithChatObj(
                chatObj,
                new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        super.onSuccess(statusCode, headers, response);
                        Log.d(TAG, "sendChatWithChatOb: Success");

                        chatObj.chat_created = CommonUtils.currentDate();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        super.onFailure(statusCode, headers, throwable, errorResponse);
                        Log.d(TAG, "sendChatWithChatOb: Failed");
                    }


                }
        );

        chatObj.chat_media_url = API_Manager.FILE_BASE_PATH + media_url;

        maryMessage.add(chatObj);
        reloadList();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && data != null) {

            Bitmap imgChat = null;

            if (requestCode == CommonUtils.CAMERA_OPEN_FOR_CHAT_REQUEST_CODE) {
                imgChat = (Bitmap) data.getExtras().get("data");
            } else if (requestCode == CommonUtils.GALLERY_OPEN_FOR_CHAT_REQUEST_CODE) {
                try {
                    InputStream stream = mActivity.getContentResolver().openInputStream(data.getData());
                    imgChat = BitmapFactory.decodeStream(stream);
                    stream.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (imgChat != null) {
                double dWidth = imgChat.getWidth();
                final double dHeight = imgChat.getHeight();
                double dScale = 200.0 / (dWidth > dHeight ? dWidth : dHeight);
                dScale = dScale < 1.0 ? dScale : 1.0;
                final int nWidth = (int) (dWidth * dScale);
                final int nHeight = (int) (dHeight * dScale);

                imgChat = Bitmap.createScaledBitmap(imgChat, nWidth, nHeight, false);

                mProgressDialog = ProgressDialog.show(mActivity, "", "Uploading Image...");

                // compress image
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                imgChat.compress(Bitmap.CompressFormat.JPEG, 50, stream);

                API_Manager.getInstance().uploadMediaOnlyWithUserID(
                        CommonUtils.mSelfUser.user_id.toString(),
                        CommonUtils.mSelfUser.user_place_id.toString(),
                        ChatObj.TYPE_IMAGE,
                        imgChat,
                        new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                super.onSuccess(statusCode, headers, response);

                                mProgressDialog.dismiss();

                                try {
                                    String strRes = response.getString(API_Manager.WEBAPI_RETURN_RESULT);

                                    if (strRes.equals(API_Manager.WEBAPI_RETURN_SUCCESS)) {
                                        String strMediaUrl = response.getJSONObject(API_Manager.WEBAPI_RETURN_VALUES).getString("mediaurl");
                                        sendPhoto(strMediaUrl, nWidth, nHeight);
                                    } else {
                                        CommonUtils.createErrorAlertDialog(mActivity, "Error", response.getString(API_Manager.WEBAPI_RETURN_MESSAGE)).show();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                                super.onFailure(statusCode, headers, throwable, errorResponse);

                                mProgressDialog.dismiss();
                                CommonUtils.createErrorAlertDialog(mActivity, "Error", throwable.getMessage()).show();
                            }
                        }
                );
            }
        }
    }
}
