package de.starquay.android.camera5000;

import android.content.SharedPreferences;
import android.hardware.Camera.Parameters;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

public class Preferences extends PreferenceActivity {
	
	String[] clone;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		fillPreferenceHierarchy();
	}
	
	private void fillPreferenceHierarchy() {
		Bundle b = getIntent().getExtras();
		
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		
		/** All Camera Settings */
		
		/** Color Effects */
		String key = this.getString(R.string.key_colorEffect);
		String defaultVal = Parameters.EFFECT_NONE;
		String[] valArray = b.getStringArray(key);
		if(b.containsKey(key)) {
			ListPreference lp = (ListPreference) findPreference(key);
			lp.setSummary(settings.getString(key, defaultVal));
			// Sets the human-readable entries to be shown in the list.
			lp.setEntries(valArray);
			// The array to find the value to save for a preference when an entry from entries is selected.
			lp.setEntryValues(valArray);
		}
		/** Scene Modes */
		key = this.getString(R.string.key_sceneMode);
		defaultVal = Parameters.SCENE_MODE_AUTO;
		valArray = b.getStringArray(key);
		if(b.containsKey(key)) {
			ListPreference lp = (ListPreference) findPreference(key);
			lp.setSummary(settings.getString(key, defaultVal));
			// Sets the human-readable entries to be shown in the list.
			lp.setEntries(valArray);
			// The array to find the value to save for a preference when an entry from entries is selected.
			lp.setEntryValues(valArray);
		}
		
	}

}
