package com.diyin.Voltga;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.util.ArrayList;

public class GcmIntentService extends IntentService {

    private static final String TAG = GcmIntentService.class.getSimpleName();

    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder mBuilder;
    private static ArrayList<String> mUnreadNotificationArray = new ArrayList<String>();

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM
             * will be extended in the future with new message types, just ignore
             * any message types you're not interested in, or that you don't
             * recognize.
             */
            if (GoogleCloudMessaging.
                    MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                sendNotification("Send error: " + extras.toString());
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_DELETED.equals(messageType)) {
                sendNotification("Deleted messages on server: " +
                        extras.toString());
                // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                // This loop represents the service doing some work.
                /*for (int i = 0; i < 5; i++) {
                    Log.i(TAG, "Working... " + (i + 1)
                            + "/5 @ " + SystemClock.elapsedRealtime());
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                    }
                }*/
                //Log.i(TAG, "Completed work @ " + SystemClock.elapsedRealtime());

                String message = extras.getString(SplashActivity.EXTRA_MESSAGE);

                // Post notification of received message.
                sendNotification(message/*"Received: " + extras.toString()*/);
                //Log.i(TAG, "Received: " + extras.toString());
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(String msg) {
        // inbox style
        mUnreadNotificationArray.add(msg);

        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, SplashActivity.class), 0);

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();

        // Moves events into the expanded layout
        for (String line : mUnreadNotificationArray) {
            inboxStyle.addLine(line);
        }

        mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(getString(R.string.app_name)/*"GCM Notification"*/)
                        .setStyle(inboxStyle/*new NotificationCompat.BigTextStyle().bigText(msg)*/)
                        .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .setAutoCancel(true)
                        .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());

        try {
            incrementBadge(getApplicationContext());
        } catch (Exception e) {
        }
    }

    /* Badge function was enabled in only Samsung products */
    public static int getBadge(Context context) {
        // This is the content uri for the BadgeProvider
        Uri uri = Uri.parse("content://com.sec.badge/apps");

        Cursor cursor = context.getContentResolver().query(uri, null, "package IS ?", new String[]{context.getPackageName()}, null);

        // This indicates the provider doesn't exist and you probably aren't running
        // on a Samsung phone running TWLauncher. This has to be outside of try/finally block
        if (cursor == null) {
            return -1;
        }

        int badgeCount = 0;
        try {
            if (!cursor.moveToFirst()) {
                // No results. Nothing to query
                return -1;
            }

            do {
                String pkg = cursor.getString(1);
                String clazz = cursor.getString(2);
                badgeCount = cursor.getInt(3);
                Log.d("BadgeTest", "package: " + pkg + ", class: " + clazz + ", count: " + String.valueOf(badgeCount));
            } while (cursor.moveToNext());
        } finally {
            cursor.close();
        }

        return badgeCount;
    }

    public static void incrementBadge(Context context) {
        int count = getBadge(context);
        setBadge(context, count + 1);
    }

    public static void setBadge(Context context, int count) {
        //context.getContentResolver().delete(Uri.parse("content://com.sec.badge/apps"), "package IS ?", new String[] {context.getPackageName()});

        ContentValues cv = new ContentValues();
        cv.put("package", context.getPackageName());

        // Name of your activity declared in the manifest as android.intent.action.MAIN.
        // Must be fully qualified name as shown below
        cv.put("class", context.getPackageName() + "." + SplashActivity.class.getSimpleName());
        cv.put("badgecount", count); // integer count you want to display

        if (getBadge(context) == -1) {
            // Execute insert
            context.getContentResolver().insert(Uri.parse("content://com.sec.badge/apps"), cv);
        } else {
            context.getContentResolver().update(Uri.parse("content://com.sec.badge/apps"), cv, "package IS ?", new String[]{context.getPackageName()});
        }
    }

    public static void clearBadge(Context context) {
        mUnreadNotificationArray.clear();

        ContentValues cv = new ContentValues();
        cv.put("badgecount", 0);
        context.getContentResolver().update(Uri.parse("content://com.sec.badge/apps"), cv, "package IS ?", new String[]{context.getPackageName()});
    }

}