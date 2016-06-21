package com.simpsolution.clipit;

import static android.content.Context.VIBRATOR_SERVICE;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

/**
 *
 * @author maytan
 */
public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {

    private SharedPreferences.OnSharedPreferenceChangeListener listener;
    /**
     * Called when the activity is first created.
     * @param icicle
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public boolean onPreferenceChange(Preference prfrnc, Object o) {
        String key = prfrnc.getKey();
        boolean bool = (Boolean) o;
        switch(key) {
            case "pref_key_clipit_timeout":
                if(bool){
                    getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                } else {
                    getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                }
                return true;
                
            case "pref_key_clipit_vibes":
                if(bool){
                    ((Vibrator)getActivity().getSystemService(VIBRATOR_SERVICE)).vibrate(800);
                }
                return true;
        }
        return false;
    }
    
}
