package com.example.wifihotspot;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

  WifiHotspotManager wifiHotspotManager;
  TextView wifiApState;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    wifiHotspotManager = new WifiHotspotManager(this);
    wifiApState = (TextView) findViewById(R.id.wifiState);
    wifiHotspotManager.showWritePermissionSettings(true);
    updateWifiApState();

  }

  public boolean onCreateOptionsMenu(Menu menu) {

    menu.add(0, 1, 0, "Open AP");
    menu.add(0, 2, 0, "Close AP");
    return super.onCreateOptionsMenu(menu);
  }


  @Override public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case 1:
        wifiHotspotManager.setWifiEnabled(null, true);
        updateWifiApState();
        break;
      case 2:
        wifiHotspotManager.setWifiEnabled(null, false);
        updateWifiApState();
        break;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  protected void onResume() {
    super.onResume();

    wifiHotspotManager.showWritePermissionSettings(false);
  }

  private void updateWifiApState()
  {
    if(wifiHotspotManager.isWifiApEnabled())
    wifiApState.setText("Wifi AP Turned on");
    else
      wifiApState.setText("Wifi AP Turned off");
  }
}
