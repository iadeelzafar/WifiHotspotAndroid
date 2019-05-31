package com.example.wifihotspot;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.util.Log;
import java.lang.reflect.Method;

public class WifiHotspotManager {
  private final WifiManager wifiManager;
  private Context context;
  private WifiManager.LocalOnlyHotspotReservation hotspotReservation;
  private boolean oreoenabled=false;

  public WifiHotspotManager(Context context) {
    this.context = context;
    wifiManager = (WifiManager) this.context.getSystemService(Context.WIFI_SERVICE);
  }


  public void showWritePermissionSettings(boolean force) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      if (force || !Settings.System.canWrite(this.context)) {
        Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
        intent.setData(Uri.parse("package:" + this.context.getPackageName()));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.context.startActivity(intent);
      }
    }
  }

  public boolean setWifiEnabled(WifiConfiguration wifiConfig, boolean enabled) {
    try {
      if (enabled) { // disable WiFi in any case
        wifiManager.setWifiEnabled(false);
      }

      Method method = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
      return (Boolean) method.invoke(wifiManager, wifiConfig, enabled);
    } catch (Exception e) {
      Log.e(this.getClass().toString(), "", e);
      return false;
    }
  }

  //Workaround to turn on hotspot for Oreo versions
  @RequiresApi(api = Build.VERSION_CODES.O)
  public void turnOnHotspot() {
    Log.v("DANG", "Coming 1");
    if(!oreoenabled) {
      wifiManager.startLocalOnlyHotspot(new WifiManager.LocalOnlyHotspotCallback() {

        @Override
        public void onStarted(WifiManager.LocalOnlyHotspotReservation reservation) {
          super.onStarted(reservation);
          Log.v("DANG", "Coming 2");
          hotspotReservation = reservation;
          oreoenabled = true;
        }

        @Override
        public void onStopped() {
          super.onStopped();
          Log.v("DANG", "Local Hotspot Stopped");
        }

        @Override
        public void onFailed(int reason) {
          super.onFailed(reason);
          Log.v("DANG", "Local Hotspot failed to start");
        }
      }, new Handler());
    }
  }

  //Workaround to turn off hotspot for Oreo versions
  @RequiresApi(api = Build.VERSION_CODES.O)
  public void turnOffHotspot() {
    if (hotspotReservation != null) {
      hotspotReservation.close();
      oreoenabled=false;
    }
  }

  public WIFI_AP_STATE getWifiApState() {
    try {
      Method method = wifiManager.getClass().getMethod("getWifiApState");

      int tmp = ((Integer) method.invoke(wifiManager));

      // Fix for Android 4
      if (tmp >= 10) {
        tmp = tmp - 10;
      }

      return WIFI_AP_STATE.class.getEnumConstants()[tmp];
    } catch (Exception e) {
      Log.e(this.getClass().toString(), "", e);
      return WIFI_AP_STATE.WIFI_AP_STATE_FAILED;
    }
  }

  public boolean isWifiApEnabled() {
    return getWifiApState() == WIFI_AP_STATE.WIFI_AP_STATE_ENABLED;
  }

  public WifiConfiguration getWifiApConfiguration() {
    try {
      Method method = wifiManager.getClass().getMethod("getWifiApConfiguration");
      return (WifiConfiguration) method.invoke(wifiManager);
    } catch (Exception e) {
      Log.e(this.getClass().toString(), "", e);
      return null;
    }
  }

  public boolean setWifiApConfiguration(WifiConfiguration wifiConfig) {
    try {
      Method method = wifiManager.getClass().getMethod("setWifiApConfiguration", WifiConfiguration.class);
      return (Boolean) method.invoke(wifiManager, wifiConfig);
    } catch (Exception e) {
      Log.e(this.getClass().toString(), "", e);
      return false;
    }
  }
}
