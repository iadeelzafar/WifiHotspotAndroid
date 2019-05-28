package com.example.wifihotspot;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import java.lang.reflect.Method;

public class WifiHotspotManager {
  private final WifiManager mWifiManager;
  private Context context;

  public WifiHotspotManager(Context context) {
    this.context = context;
    mWifiManager = (WifiManager) this.context.getSystemService(Context.WIFI_SERVICE);
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
        mWifiManager.setWifiEnabled(false);
      }

      Method method = mWifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
      return (Boolean) method.invoke(mWifiManager, wifiConfig, enabled);
    } catch (Exception e) {
      Log.e(this.getClass().toString(), "", e);
      return false;
    }
  }

  public WIFI_AP_STATE getWifiApState() {
    try {
      Method method = mWifiManager.getClass().getMethod("getWifiApState");

      int tmp = ((Integer) method.invoke(mWifiManager));

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
      Method method = mWifiManager.getClass().getMethod("getWifiApConfiguration");
      return (WifiConfiguration) method.invoke(mWifiManager);
    } catch (Exception e) {
      Log.e(this.getClass().toString(), "", e);
      return null;
    }
  }

  public boolean setWifiApConfiguration(WifiConfiguration wifiConfig) {
    try {
      Method method = mWifiManager.getClass().getMethod("setWifiApConfiguration", WifiConfiguration.class);
      return (Boolean) method.invoke(mWifiManager, wifiConfig);
    } catch (Exception e) {
      Log.e(this.getClass().toString(), "", e);
      return false;
    }
  }
}
