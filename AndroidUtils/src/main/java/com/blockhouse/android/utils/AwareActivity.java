/**
 *
 */
package com.blockhouse.android.utils;

import android.app.Activity;
import android.os.Build;

import java.util.ArrayList;

/**
 * @author mbarr
 * Date Created: Oct 11, 2013
 *
 */
public abstract class AwareActivity extends Activity implements HttpQueryListener<ArrayList> {

     private boolean isRunning = false;

     private String serial = Build.SERIAL;

     private boolean isPOST = false;

     @Override
     protected void onPause() {
          updateHeartbeat("onPause");
          super.onPause();
          isRunning = false;
     }

     @Override
     protected void onResume() {
          updateHeartbeat("onResume");
          super.onResume();
          isRunning = true;
     }

     /**
      * Returns if the activity is currently running.
      * @return True if onResume() has been called, without onPause(). False otherwise.
      */
     public boolean isRunning() {
          return isRunning;
     }

     public void setRunning(boolean val){isRunning = val; }

     public String getSerial() {
          return serial;
     }

     protected void updateHeartbeat(String method){
          ArrayList<String> parameters = new ArrayList<String>();
          parameters.add(HttpQuery.CMD_QUERY);
          parameters.add("heartbeat");
          parameters.add("user");
          parameters.add(getSerial());
          parameters.add("time");
          parameters.add("now");
          parameters.add("method");
          parameters.add(method);
          HttpQuery.makeRequest(this, HttpQuery.STND_URL + "AndroidServlet", parameters);
     }

     public boolean isPOST() {
          return isPOST;
     }

     public void setPOST(boolean POST) {
          isPOST = POST;
     }

     @Override
     public void queryComplete(ArrayList resp) {

     }
}
