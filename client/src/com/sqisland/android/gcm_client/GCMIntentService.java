package com.sqisland.android.gcm_client;

import com.google.android.gcm.GCMBaseIntentService;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

public class GCMIntentService extends GCMBaseIntentService {
  @Override
  protected void onRegistered(Context context, String regId) {
    Intent intent = new Intent(Constants.ACTION_ON_REGISTERED);
    intent.putExtra(Constants.FIELD_REGISTRATION_ID, regId);
    context.sendBroadcast(intent);
  }

  @Override
  protected void onUnregistered(Context context, String regId) {
    Log.i(Constants.TAG, "onUnregistered: "+ regId);
  }

  @Override
  protected void onMessage(Context context, Intent intent) {
    String msg = intent.getStringExtra(Constants.FIELD_MESSAGE);

    NotificationManager manager = (NotificationManager) context
        .getSystemService(Context.NOTIFICATION_SERVICE);
    Notification notification = prepareNotification(context, msg);
    manager.notify(R.id.notification_id, notification);
  }

  private Notification prepareNotification(Context context, String msg) {
    long when = System.currentTimeMillis();
    Notification notification = new Notification(R.drawable.ic_stat_cloud, msg,
        when);
    notification.flags |= Notification.FLAG_AUTO_CANCEL;

    Intent intent = new Intent(context, MessageActivity.class);
    // Set a unique data uri for each notification to make sure the activity
    // gets updated
    intent.setData(Uri.parse(msg));
    intent.putExtra(Constants.FIELD_MESSAGE, msg);
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
        | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
        0);
    String title = context.getString(R.string.app_name);
    notification.setLatestEventInfo(context, title, msg, pendingIntent);

    return notification;
  }

  @Override
  protected void onError(Context context, String errorId) {
    Toast.makeText(context, errorId, Toast.LENGTH_LONG).show();
  }
}