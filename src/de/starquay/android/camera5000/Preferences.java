package de.starquay.android.camera5000;

import android.content.SharedPreferences;
import android.hardware.Camera.Parameters;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

public class Preferences extends PreferenceActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setPreferenceScreen(createPreferenceHierarchy());
	}
	
	private PreferenceScreen createPreferenceHierarchy() {
		Bundle b = getIntent().getExtras();
		
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		
		/** ROOT */
		PreferenceScreen root = getPreferenceManager().createPreferenceScreen(this);
		root.setKey("FreqCapPrefs");
		
		/** All Camera Settings */
		PreferenceCategory catCameraParas = new PreferenceCategory(this);
		catCameraParas.setTitle("Camera");
		root.addPreference(catCameraParas);
		
		/** Color Effects */
		if(b.containsKey("Color Effects")) {
			ListPreference colorFxPrefList = new ListPreference(this);
			catCameraParas.addPreference(colorFxPrefList);
			colorFxPrefList.setKey("colorEffectsPrefList");
			colorFxPrefList.setTitle("Color Effects");
			colorFxPrefList.setSummary(settings.getString("colorEffectsPrefList", Parameters.EFFECT_NONE));
			colorFxPrefList.setDialogTitle("Color Effects");
			colorFxPrefList.setDialogMessage("The following color effects are supported by your camera");
			String[] sdf = b.getStringArray("Color Effects");
			// Sets the human-readable entries to be shown in the list.
			CharSequence[] cs = new CharSequence[sdf.length];
			for(int i = 0; i < sdf.length; i++)
				cs[i] = sdf[i];
			colorFxPrefList.setEntries(R.array.ColorEffects);
			// The array to find the value to save for a preference when an entry from entries is selected.
			colorFxPrefList.setEntryValues(R.array.ColorEffects);
		}
		if(b.containsKey("Scene Modes")) {
			ListPreference sceneModesPrefList = new ListPreference(this);
			sceneModesPrefList.setKey("sceneModesPrefList");
			sceneModesPrefList.setTitle("Scene Modes");
			sceneModesPrefList.setSummary(settings.getString("sceneModesPrefList", Parameters.SCENE_MODE_AUTO));
			sceneModesPrefList.setDialogTitle("Scene Modes");
			sceneModesPrefList.setDialogMessage("The following Scene Modes are supported by your camera");
			String[] ds = b.getStringArray("Scene Modes");
			CharSequence[] waaaaaa = {"Waa", "aaa", "aaaah"};
			sceneModesPrefList.setEntries(waaaaaa);
			sceneModesPrefList.setEntryValues(waaaaaa);
			catCameraParas.addPreference(sceneModesPrefList);
		}

		return root;
		
	}

}
