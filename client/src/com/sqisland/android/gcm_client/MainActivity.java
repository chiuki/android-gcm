package com.sqisland.android.gcm_client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import com.google.android.gcm.GCMRegistrar;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class MainActivity extends Activity {
  private GCMReceiver mGCMReceiver;
  private IntentFilter mOnRegisteredFilter;

  private TextView mStatus;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    mStatus = (TextView) findViewById(R.id.status);

    mGCMReceiver = new GCMReceiver();
    mOnRegisteredFilter = new IntentFilter();
    mOnRegisteredFilter.addAction(Constants.ACTION_ON_REGISTERED);

    if (Constants.SENDER_ID == null) {
      mStatus.setText("Missing SENDER_ID");
      return;
    }
    if (Constants.SERVER_URL == null) {
      mStatus.setText("Missing SERVER_URL");
      return;
    }

    GCMRegistrar.checkDevice(this);
    GCMRegistrar.checkManifest(this);
    final String regId = GCMRegistrar.getRegistrationId(this);
    if (!regId.equals("")) {
      sendIdToServer(regId);
    } else {
      GCMRegistrar.register(this, Constants.SENDER_ID);
    }
  }

  private void sendIdToServer(String regId) {
    String status = getString(R.string.gcm_registration, regId);
    mStatus.setText(status);
    (new SendRegistrationIdTask(regId)).execute();
  }

  @Override
  public void onResume() {
    super.onResume();
    registerReceiver(mGCMReceiver, mOnRegisteredFilter);
  }

  @Override
  public void onPause() {
    super.onPause();
    unregisterReceiver(mGCMReceiver);
  }

  private class GCMReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
      String regId = intent.getStringExtra(Constants.FIELD_REGISTRATION_ID);
      sendIdToServer(regId);
    }
  }

  private final class SendRegistrationIdTask extends
      AsyncTask<String, Void, HttpResponse> {
    private String mRegId;

    public SendRegistrationIdTask(String regId) {
      mRegId = regId;
    }

    @Override
    protected HttpResponse doInBackground(String... regIds) {
      String url = Constants.SERVER_URL + "/register";
      HttpPost httppost = new HttpPost(url);

      try {
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
        nameValuePairs.add(new BasicNameValuePair("reg_id", mRegId));
        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
        HttpClient httpclient = new DefaultHttpClient();
        return httpclient.execute(httppost);
      } catch (ClientProtocolException e) {
        Log.e(Constants.TAG, e.getMessage(), e);
      } catch (IOException e) {
        Log.e(Constants.TAG, e.getMessage(), e);
      }

      return null;
    }

    @Override
    protected void onPostExecute(HttpResponse response) {
      if (response == null) {
        Log.e(Constants.TAG, "HttpResponse is null");
        return;
      }

      StatusLine httpStatus = response.getStatusLine();
      if (httpStatus.getStatusCode() != 200) {
        Log.e(Constants.TAG, "Status: " + httpStatus.getStatusCode());
        mStatus.setText(httpStatus.getReasonPhrase());
        return;
      }

      String status = getString(R.string.server_registration, mRegId);
      mStatus.setText(status);
    }
  }
}
