package com.simpsolution.clipit;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 *
 * @author maytan
 */
public class SettingsFragment extends PreferenceFragment {

    /**
     * Called when the activity is first created.
     * @param icicle
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.preferences);
    }
    
}
