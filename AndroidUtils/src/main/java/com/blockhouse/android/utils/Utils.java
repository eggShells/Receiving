/**
 * 
 */
package com.blockhouse.android.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Environment;
import android.view.WindowManager;

/**
 * @author mbarr
 * Date Created: Jun 3, 2013
 *
 */
public abstract class Utils {

     /**
      * Hides keyboard in the activity. Can cause issues if used onCreate with a ListView involved. Will cause focus to act improperly.
      * Use android:windowSoftInputMode="stateHidden" in AndroidManifest.xml instead.
      * @param activity
      */
     public static void hideKeyboard(Activity activity) {
          activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
     }
     
     /**
      * Checks with the server if there is an update available for the given app. Starts update activity if one is found.
      * @param updateActivity Activity to check the version of, and use to start update activity, if necessary.
      * @param appName Name of the application, must match RMAN on the server.
      */
     public static void checkForUpdates(final Activity updateActivity, final String appName) {
          final Context app = updateActivity.getApplicationContext();
          ArrayList<String> parameters = new ArrayList<String>();
          parameters.add(HttpQuery.CMD_CHECKUPDATE);
          parameters.add(appName);
          parameters.add(HttpQuery.PARAM_VERSION);
          try {
               parameters.add(app.getPackageManager().getPackageInfo(app.getPackageName(), 0).versionCode + "");
          } catch (NameNotFoundException e) {
               throw new RuntimeException("Unable to get version code.", e);
          }
          
          HttpQuery.makeRequest(new UpdateListener(updateActivity, appName), HttpQuery.STND_URL + "AndroidServlet", parameters, byte[].class);
     }
     
     /**
      * Reports to the server the beginning or end of an activity. Should be called from onResume() and onPause().
      * @param action Description of Activity
      * @param extra Any specifics about the activity (PO #)
      * @param startActivity True if onResume(), false otherwise.
      */
     public static void statusUpdate(final AwareActivity activity, final String action, final String extra, final boolean startActivity) {
          statusUpdate(activity, action, extra, startActivity, new StatusListener(activity, action, extra, startActivity));
     }
     
     /**
      * Reports to the server the beginning or end of an activity. 
      * @param action Description of Activity
      * @param extra Any specifics about the activity (PO #)
      * @param startActivity True if onResume(), false otherwise.
      * @param listener Listener for the response.
      */
     public static void statusUpdate(final AwareActivity activity, final String action, final String extra, final boolean startActivity, final StatusListener listener) {
          ArrayList<String> parameters = new ArrayList<String>();
          parameters.add(HttpQuery.CMD_STATUSUPDATE);
          parameters.add(action);
          parameters.add(HttpQuery.PARAM_EXTRA);
          parameters.add(extra);
          parameters.add(HttpQuery.PARAM_STARTACTIVITY);
          parameters.add(startActivity ? "1" : "0");

          HttpQuery.makeRequest(listener, HttpQuery.STND_URL + "AndroidServlet", parameters, String.class);
     }
     
     private static class UpdateListener implements HttpQueryListener<byte[]>, OnClickListener {

          private final String appName;
          private final Activity activity;
          private File apkFile;
          
          public UpdateListener(final Activity inActivity, final String inAppName) {
               appName = inAppName;
               activity = inActivity;
          }
          
          @Override
          public void queryComplete(byte[] inResp) {
               if (inResp == null || inResp.length <= 0) return;
               
               //Save apk file
               apkFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + appName + ".apk");
               apkFile.getParentFile().mkdirs();
               
               try {
                    if (apkFile.exists()) apkFile.delete();
                    
                    apkFile.createNewFile();
                    FileOutputStream saveApk = new FileOutputStream(apkFile);
                    
                    saveApk.write(inResp);
                    saveApk.flush();
                    saveApk.close();
                    
               } catch (Exception e) {
                    throw new RuntimeException("Unable to save apk.", e);
               }
               
               //Display Warning
               AlertDialog updateWarning = new AlertDialog.Builder(activity).create();
               updateWarning.setMessage("An update is required for this app to run. Please click \"Install\" on the next prompt.");
               updateWarning.setTitle("Update Required");
               updateWarning.setIcon(R.drawable.ic_dialog_alert_holo_light);
               updateWarning.setCancelable(false);
               updateWarning.setButton(AlertDialog.BUTTON_POSITIVE, "Okay", this);
               updateWarning.show();
          }

          @Override
          public void onClick(DialogInterface inArg0, int inArg1) {
             //create intent
               Intent updateApp = new Intent(Intent.ACTION_VIEW);
               updateApp.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
               
               activity.startActivity(updateApp);
               activity.finish();
          }          
     }
     
     private static class StatusListener implements HttpQueryListener<String>, OnClickListener {

          private final AwareActivity activity;
          private final String action;
          private final String extra;
          private final boolean startActivity;
          private ProgressDialog progDiag = null;
          
          public StatusListener(final AwareActivity inActivity, final String inAction, final String inExtra, final boolean inStartActivity) {
               activity = inActivity;
               action = inAction;
               extra = inExtra;
               startActivity = inStartActivity;
          }
          
          @Override
          public void queryComplete(String inResp) {
               if (progDiag != null && progDiag.isShowing()) {
//                    progDiag.hide();
                    progDiag.dismiss();
               }
               
               if (startActivity && inResp != null && !inResp.equals("")) {
                    if (activity.isRunning()) {
                         //Status is not good.
                         AlertDialog statusAlert = new AlertDialog.Builder(activity).create();
                         statusAlert.setMessage(inResp);
                         statusAlert.setTitle("Open Systems Unavailable");
                         statusAlert.setIcon(R.drawable.ic_dialog_alert_holo_light);
                         statusAlert.setCancelable(false);
                         statusAlert.setButton(AlertDialog.BUTTON_POSITIVE, "Try Again", this);
                         statusAlert.setButton(AlertDialog.BUTTON_NEGATIVE, "Exit", this);
                         statusAlert.show();
                    }
               }
          }

          @Override
          public void onClick(DialogInterface inDialog, int inWhich) {
               if (inWhich == AlertDialog.BUTTON_POSITIVE) {
                    //Try again
//                    statusUpdate(activity, action, extra, startActivity, this);
               }
               else {
                    //Finish
                    activity.finish();
               }
          }
          
     }
}
