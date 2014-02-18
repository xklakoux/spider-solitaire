/**
 * 
 */
package com.xklakoux.freespider;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.widget.BaseAdapter;

/**
 * @author artur
 * 
 */
public class SettingsDialog extends PreferenceActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
		PreferenceManager.setDefaultValues(SettingsDialog.this, R.xml.settings, false);

	}

	public static class SettingsFragment extends PreferenceFragment {

		@Override
		public void onCreate(Bundle savedInstanceState) {

			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.settings);

			// Get the custom preference
			final PreferenceScreen cardReverseList = (PreferenceScreen) findPreference(Constant.SETT_REVERSE);
			cardReverseList.setIcon(getIcon(Constant.SETT_REVERSE, Constant.DEFAULT_REVERSE, "reverse_"));

			final PreferenceScreen cardImageList = (PreferenceScreen) findPreference(Constant.SETT_CARD_SET);
			cardImageList.setIcon(getIcon(Constant.SETT_CARD_SET, Constant.DEFAULT_CARD_SET, "spades_13_"));

			final PreferenceScreen backgroundImageList = (PreferenceScreen) findPreference(Constant.SETT_BACKGROUND);
			backgroundImageList.setIcon(getIcon(Constant.SETT_BACKGROUND, Constant.DEFAULT_BACKGROUND,"background_"));


			createPreferencesList(cardReverseList, "reverse_","Reverse");
			createPreferencesList(cardImageList, "spades_13_","Set");
			createPreferencesList(backgroundImageList, "background_","Background");


		}

		int getIcon(String key, String def, String prefix) {
			String res = App.getSettings().getString(key, def);
			return Utils.getResId(prefix+res, R.drawable.class);
		}


		private void createPreferencesList(final PreferenceScreen screen,final String resourceName, String name) {
			for(int i=0;;i++) {
				final int index = i;
				Preference pref = new Preference(getActivity());
				final int resId = Utils.getResId(resourceName+i, R.drawable.class);
				if(resId<0) {
					break;
				}
				pref.setIcon(getResources().getDrawable(resId));
				pref.setTitle(name +" "+ (i+1));
				pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference preference) {
						SharedPreferences customSharedPreference = App.getSettings();
						SharedPreferences.Editor editor = customSharedPreference.edit();
						editor.putString(screen.getKey(),""+index);
						editor.commit();
						screen.setIcon(getResources().getDrawable(resId));
						BaseAdapter adapter = (BaseAdapter) getPreferenceScreen().getRootAdapter();
						adapter.notifyDataSetChanged();
						screen.getDialog().dismiss();
						return true;
					}
				});
				screen.addPreference(pref);
			}
		}



		@Override
		public void onDestroy() {
			super.onDestroy();
			App.getBus().post(new Object());
		}

	}

}
