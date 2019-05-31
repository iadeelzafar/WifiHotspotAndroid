package com.example.wifihotspot;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity {

  WifiHotspotManager wifiHotspotManager;
  TextView wifiApState;
  private final int MY_PERMISSIONS_ACCESS_COARSE_LOCATION = 101;
  private final int MY_PERMISSIONS_ACCESS_FINE_LOCATION = 102;

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
    if(isMobileDataEnabled(this))
    {
      Toast.makeText(this, "You need to disable mobile data ", Toast.LENGTH_LONG).show();
      enableDisableMobileData();
    }
    switch (item.getItemId()) {
      case 1:
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
              == PackageManager.PERMISSION_GRANTED) {
            wifiHotspotManager.turnOnHotspot();
          }
        else {
             //Show rationale and request permission.
             //No explanation needed; request the permission
            ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                MY_PERMISSIONS_ACCESS_FINE_LOCATION);
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

  public void enableDisableMobileData() {
    Intent intent = new Intent();
    intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings$DataUsageSummaryActivity"));
    startActivity(intent);
  }

  public static boolean isMobileDataEnabled(Context context) {
    boolean enabled = false;
    ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    try {
      Class cmClass = Class.forName(cm.getClass().getName());
      Method method = cmClass.getDeclaredMethod("getMobileDataEnabled");
      method.setAccessible(true);
      enabled = (Boolean) method.invoke(cm);
    } catch (Exception e) {
      Log.e("DANG ",e.toString());
    }
    return enabled;
  }
}
