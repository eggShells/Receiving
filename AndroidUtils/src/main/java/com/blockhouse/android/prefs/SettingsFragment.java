/**
 * 
 */
package com.blockhouse.android.prefs;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * @author mbarr
 * Date Created: Jul 26, 2013
 *
 */
public class SettingsFragment extends PreferenceFragment {

     private final int preferencesXml;
     
     /**
      * Basic constructor
      * @param inPrefXml preferences.xml resource id to create settings fragment.
      */
     public SettingsFragment(int inPrefXml) {
          super();
          
          preferencesXml = inPrefXml;
     }
     
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);

         // Load the preferences from an XML resource
         addPreferencesFromResource(preferencesXml);
     }
}
