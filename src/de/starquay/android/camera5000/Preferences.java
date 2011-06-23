package de.starquay.android.camera5000;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.hardware.Camera.Parameters;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.text.InputType;
import android.text.method.DigitsKeyListener;
import android.widget.EditText;

public class Preferences extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	
	String[] clone;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		fillPreferencesWithValues();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}
	
	private void fillPreferencesWithValues() {
		Bundle b = getIntent().getExtras();
		
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		
		/**
		 * BURST Settings
		 */
		
		/** Burst Mode */
		//TODO: not necessary
		String key = this.getString(R.string.key_burstMode);
		CheckBoxPreference burstMode = (CheckBoxPreference) findPreference(key);
		/** Number of pictures */
		key = this.getString(R.string.key_burstNumberValue);
		EditTextPreference etp = (EditTextPreference) findPreference(key);
		etp.setSummary(settings.getString(key, "not set"));
		// only numbers!
		EditText myEditText = (EditText)etp.getEditText(); 
		myEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
		/** Seconds between pictures */
		key = this.getString(R.string.key_burstIntervalValue);
		etp = (EditTextPreference) findPreference(key);
		etp.setSummary(settings.getString(key, "not set"));
		myEditText = (EditText)etp.getEditText(); 
		myEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
		
		
		/** 
		 * All Camera Settings 
		 */
		//FIXME: Problem: Camera with other skills!
		
		/** Color Effects */
		key = this.getString(R.string.key_colorEffect);
		String defaultVal;
		String[] entries;
		if(b.containsKey(key)) {
			defaultVal = Parameters.EFFECT_NONE;
			entries = b.getStringArray(key);
			buildListPreference(key, settings, defaultVal, entries, null);
		} else {
			//remove this preference from list
			//TODO: for all skills
		}
		/** Scene Modes */
		key = this.getString(R.string.key_sceneMode);
		if(b.containsKey(key)) {
			defaultVal = Parameters.SCENE_MODE_AUTO;
			entries = b.getStringArray(key);
			buildListPreference(key, settings, defaultVal, entries, null);
		}
		/** White Balance */
		key = this.getString(R.string.key_whiteBalance);
		if(b.containsKey(key)) {
			defaultVal = Parameters.WHITE_BALANCE_AUTO;
			entries = b.getStringArray(key);
			buildListPreference(key, settings, defaultVal, entries, null);
		}
		/** Focus Mode */
		key = this.getString(R.string.key_focusMode);
		if(b.containsKey(key)) {
			defaultVal = Parameters.FOCUS_MODE_AUTO;
			entries = b.getStringArray(key);
			buildListPreference(key, settings, defaultVal, entries, null);
		}
		/** Picture Quality */
		key = this.getString(R.string.key_picQuali);
		defaultVal = "problems with resolution:(";
		entries = b.getStringArray(key);
		if(b.containsKey(key)) {
			String[] entryValues = new String[entries.length];
			for(int i = 0; i < entries.length; i++)
				entryValues[i] = i+"";
			//TODO: is this save?
			buildListPreference(key, settings, defaultVal, entries, entryValues);
		}
		
	}
	
	private void buildListPreference(String key, SharedPreferences settings, String defaultVal, String[] entries, String[] entryValues) {
		if(entryValues == null)
			entryValues = entries;
		ListPreference lp = (ListPreference) findPreference(key);
		lp.setSummary(settings.getString(key, defaultVal));
		// Sets the human-readable entries to be shown in the list.
		lp.setEntries(entries);
		// The array to find the value to save for a preference when an entry from entries is selected.
		lp.setEntryValues(entryValues);

	}

	/**
	 * Update the summary fields of each pref with the actual value
	 * see: http://stackoverflow.com/questions/3827356/setting-ui-preference-summary-field-to-the-value-of-the-preference
	 */
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		Preference pref = findPreference(key);
		if(pref instanceof EditTextPreference) {
			EditTextPreference textPre = (EditTextPreference) pref;
			pref.setSummary(textPre.getText());
		} else if(pref instanceof ListPreference) {
			ListPreference lp = (ListPreference) pref;
			lp.setSummary(lp.getValue());
		}
		
	}

}
