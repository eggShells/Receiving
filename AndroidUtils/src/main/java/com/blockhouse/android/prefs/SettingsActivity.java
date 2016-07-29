/**
 * 
 */
package com.blockhouse.android.prefs;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.blockhouse.android.utils.AwareActivity;

/**
 * @author mbarr
 * Date Created: Jul 26, 2013
 *
 */
public class SettingsActivity extends AwareActivity implements OnSharedPreferenceChangeListener {
     
     public static final String EXTRA_WASCHANGED = "waschanged";
     
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);

         // Display the fragment as the main content.
         getFragmentManager().beginTransaction()
                 .replace(android.R.id.content, new SettingsFragment((Integer) getIntent().getExtras().get("prefId")))
                 .commit();
     }
     
     @Override
     protected void onResume() {
         super.onResume();
         PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
     }

     @Override
     protected void onPause() {
         super.onPause();
         PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
         finish();
     }

     @Override
     public void onSharedPreferenceChanged(SharedPreferences inSharedPreferences, String inKey) {
          Intent retIntent = new Intent();
          retIntent.putExtra(EXTRA_WASCHANGED, true);
          setResult(RESULT_OK, retIntent);
     }

}
