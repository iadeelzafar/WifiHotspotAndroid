package com.example.wifihotspot;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
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
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity {

  WifiHotspotManager wifiHotspotManager;
  TextView wifiApState;
  private final int MY_PERMISSIONS_ACCESS_COARSE_LOCATION = 101;
  private final int MY_PERMISSIONS_ACCESS_FINE_LOCATION = 102;
  private Task<LocationSettingsResponse> task;

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
            setupLocationServices();
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

  private void setupLocationServices()
  {
    LocationRequest mLocationRequest = new LocationRequest();
    mLocationRequest.setInterval(10);
    mLocationRequest.setSmallestDisplacement(10);
    mLocationRequest.setFastestInterval(10);
    mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    LocationSettingsRequest.Builder builder = new
        LocationSettingsRequest.Builder();
    builder.addLocationRequest(mLocationRequest);

    task= LocationServices.getSettingsClient(this).checkLocationSettings(builder.build());

    task.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
      @Override
      public void onComplete(Task<LocationSettingsResponse> task) {
        try {
          LocationSettingsResponse response = task.getResult(ApiException.class);
          // All location settings are satisfied. The client can initialize location
          // requests here.
          if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)
          wifiHotspotManager.turnOnHotspot();

        } catch (ApiException exception) {
          switch (exception.getStatusCode()) {
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
              // Location settings are not satisfied. But could be fixed by showing the
              // user a dialog.
              try {
                // Cast to a resolvable exception.
                ResolvableApiException resolvable = (ResolvableApiException) exception;
                // Show the dialog by calling startResolutionForResult(),
                // and check the result in onActivityResult().
                resolvable.startResolutionForResult(
                    MainActivity.this,
                    101);
              } catch (IntentSender.SendIntentException e) {
                // Ignore the error.
              } catch (ClassCastException e) {
                // Ignore, should be an impossible error.
              }
              break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
              // Location settings are not satisfied. However, we have no way to fix the
              // settings so we won't show the dialog.
              break;
          }
        }
      }
    });
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    final LocationSettingsStates states = LocationSettingsStates.fromIntent(data);
    switch (requestCode) {
      case 101:
        switch (resultCode) {
          case Activity.RESULT_OK:
            // All required changes were successfully made
            Toast.makeText(MainActivity.this,states.isLocationPresent()+"",Toast.LENGTH_SHORT).show();
            break;
          case Activity.RESULT_CANCELED:
            // The user was asked to change settings, but chose not to
            Toast.makeText(MainActivity.this,"Canceled",Toast.LENGTH_SHORT).show();
            break;
          default:
            break;
        }
        break;
    }
  }
}
