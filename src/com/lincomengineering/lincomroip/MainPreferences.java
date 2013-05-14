package com.lincomengineering.lincomroip;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class MainPreferences extends PreferenceActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.main_preferences);
	}
}
