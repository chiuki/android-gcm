package com.sqisland.android.gcm_client;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class MessageActivity extends Activity {
  private TextView mMessageView;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_message);
    mMessageView = (TextView) findViewById(R.id.message);
  }

  @Override
  public void onResume() {
    super.onResume();
    String msg = getIntent().getStringExtra(Constants.FIELD_MESSAGE);
    mMessageView.setText(msg);
  }
}