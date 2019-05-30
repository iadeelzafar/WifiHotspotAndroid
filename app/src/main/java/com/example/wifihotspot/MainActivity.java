package com.example.wifihotspot;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

  WifiHotspotManager wifiHotspotManager;
  TextView wifiApState;
  private final int MY_PERMISSIONS_ACCESS_COARSE_LOCATION = 101;

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
              == PackageManager.PERMISSION_GRANTED) {
            wifiHotspotManager.turnOnHotspot();
          } else {
            // Show rationale and request permission.
            // No explanation needed; request the permission
            ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                MY_PERMISSIONS_ACCESS_COARSE_LOCATION);
          }

        }
        else
        {wifiHotspotManager.setWifiEnabled(null, true);
        updateWifiApState();}
        break;
      case 2:
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
          wifiHotspotManager.turnOffHotspot();
        else
        { wifiHotspotManager.setWifiEnabled(null, false);
        updateWifiApState();}
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
