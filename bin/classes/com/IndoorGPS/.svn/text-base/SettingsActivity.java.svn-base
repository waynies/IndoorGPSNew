package com.IndoorGPS;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity
{
	private final static String TAG = "Settings Activity->";

	private ListPreference appMode;
	private ListPreference maxAPPref;
	private ListPreference sampleCount;
	private CheckBoxPreference fourOrientation;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		// load xml from resource
		addPreferencesFromResource(R.xml.settings_pref);

		// get a reference to preference
		appMode = (ListPreference) getPreferenceScreen().findPreference("app_mode_pref");
		maxAPPref = (ListPreference) getPreferenceScreen().findPreference("maxAPPref");
		sampleCount = (ListPreference) getPreferenceScreen().findPreference("sampleCount");
		fourOrientation = (CheckBoxPreference) getPreferenceScreen().findPreference("4oTraining");
	}
	
	/*@Override
	public boolean onPreferenceChange(Preference preference, Object newValue)
	{
		String key = preference.getKey();
		if(key.equals("maxAPPref"))
		{
			Log.d(TAG, "changed");
		}
		return false;
	}*/
}