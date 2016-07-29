/**
 * 
 */
package com.blockhouse.android.utils;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.widget.Toast;

/**
 * @author mbarr
 * Date Created: May 17, 2013
 *
 */
public abstract class WifiUtils {

     public static final String waitingForWifi = "Waiting for WiFi connection...";
     
     public static boolean isWifiConnected(final Activity activity) {
          ConnectivityManager connManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
          NetworkInfo wifiConnection = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
          
          return wifiConnection.isConnected();
     }
     
     public static void notifyOnWifiConnection(final Activity activity, final BroadcastReceiver br) {
          IntentFilter intentFilter = new IntentFilter();
          intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
          activity.registerReceiver(br, intentFilter);
     }
     
     public static void removeNotifyOnWifiConnection(final Activity activity) {
          
     }
     
     /**
      * Ensures a valid connection by checking for the connection, then waiting if it does not exist.
      * @param activity Activity
      * @param e Callback listener to run when connection is ensured.
      * @return BroadcastReceiver if one is added, null otherwise.
      */
     public static BroadcastReceiver ensureConnection(final Activity activity, final OnConnectionListener e) {
          if (isWifiConnected(activity)) {
               e.onConnection();
          }
          else {
               Toast.makeText(activity.getApplicationContext(), waitingForWifi, Toast.LENGTH_LONG).show();
               final BroadcastReceiver ret = new BroadcastReceiver() {

                    @Override
                    public void onReceive(Context context, Intent intent) {
                         final String action = intent.getAction();
                         
                         if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
                              if (intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_ENABLED) == WifiManager.WIFI_STATE_ENABLED) {
                                   e.onConnection();
                              }
                         }
                         context.unregisterReceiver(this);
                    }

               };
               notifyOnWifiConnection(activity, ret);
               return ret;
          }
          return null;
     }
     
     public interface OnConnectionListener {
          
          public void onConnection();
          
     }
}
