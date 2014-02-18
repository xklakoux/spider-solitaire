/**
 * 
 */
package com.xklakoux.freespider;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.widget.Toast;

/**
 * @author artur
 *
 */
public class SettingsDialog extends PreferenceActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getFragmentManager().beginTransaction().replace(android.R.id.content,
				new SettingsFragment()).commit();
		PreferenceManager.setDefaultValues(SettingsDialog.this, R.xml.settings, false);

	}

	public static class SettingsFragment extends PreferenceFragment {

		@Override
		public void onCreate(Bundle savedInstanceState) {

			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.settings);

			// Get the custom preference
			Preference customPref = findPreference("customPref");
			customPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {

				@Override
				public boolean onPreferenceClick(Preference preference) {
					Toast.makeText(getActivity(), "The custom preference has been clicked",
							Toast.LENGTH_LONG).show();
					SharedPreferences customSharedPreference = getActivity().getSharedPreferences(
							"myCustomSharedPrefs", Activity.MODE_PRIVATE);
					SharedPreferences.Editor editor = customSharedPreference.edit();
					editor.putString("myCustomPref", "The preference has been clicked");
					editor.commit();
					return true;
				}

			});

		}

	}
}
