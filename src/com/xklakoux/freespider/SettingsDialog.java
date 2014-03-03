/**
 * 
 */
package com.xklakoux.freespider;

import com.xklakoux.freespider.R;
import com.xklakoux.freespider.R.drawable;
import com.xklakoux.freespider.R.xml;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
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

			final PreferenceScreen cardReverseList = (PreferenceScreen) findPreference(Constant.SETT_REVERSE);
			cardReverseList.setIcon(getIcon(Constant.SETT_REVERSE, Constant.DEFAULT_REVERSE, "reverse_"));

			final PreferenceScreen cardImageList = (PreferenceScreen) findPreference(Constant.SETT_CARD_SET);
			cardImageList.setIcon(getIcon(Constant.SETT_CARD_SET, Constant.DEFAULT_CARD_SET, "spades_13_"));

			final PreferenceScreen backgroundImageList = (PreferenceScreen) findPreference(Constant.SETT_BACKGROUND);
			backgroundImageList.setIcon(getIcon(Constant.SETT_BACKGROUND, Constant.DEFAULT_BACKGROUND, "background_"));

			createPreferencesList(cardReverseList, "reverse_icon_", "Reverse");
			createPreferencesList(cardImageList, "spades_13_icon_", "Set");
			createPreferencesList(backgroundImageList, "background_icon_", "Background");

			final CheckBoxPreference unrestrictedDeal = (CheckBoxPreference) findPreference(Constant.SETT_UNRES_DEAL);
			final CheckBoxPreference unrestrictedUndo = (CheckBoxPreference) findPreference(Constant.SETT_UNRES_UNDO);
			final CheckBoxPreference hints = (CheckBoxPreference) findPreference(Constant.SETT_HINTS);
			final CheckBoxPreference sounds = (CheckBoxPreference) findPreference(Constant.SETT_SOUNDS);

			final ListPreference orientation = (ListPreference) findPreference(Constant.SETT_ORIENTATION);
			final ListPreference animation = (ListPreference) findPreference(Constant.SETT_ANIMATION);
			final ListPreference difficulty = (ListPreference) findPreference(Constant.SETT_DIFFICULTY);

			unrestrictedDeal.setOnPreferenceChangeListener(new MyOnPreferenceChangeListener());
			unrestrictedUndo.setOnPreferenceChangeListener(new MyOnPreferenceChangeListener());
			hints.setOnPreferenceChangeListener(new MyOnPreferenceChangeListener());
			sounds.setOnPreferenceChangeListener(new MyOnPreferenceChangeListener());

			orientation.setOnPreferenceChangeListener(new MyOnPreferenceChangeListener());
			animation.setOnPreferenceChangeListener(new MyOnPreferenceChangeListener());
			difficulty.setOnPreferenceChangeListener(new MyOnPreferenceChangeListener());

		}

		int getIcon(String key, String def, String prefix) {
			String res = Game.getSettings().getString(key, def);
			return Utils.getResId(prefix + res, R.drawable.class);
		}

		private void createPreferencesList(final PreferenceScreen screen, final String resourceName, String name) {
			for (int i = 0;; i++) {
				final int index = i;
				Preference pref = new Preference(getActivity());
				// final int iconResId = Utils.getResId(resourceName +"icon_"
				// +i, R.drawable.class);
				final int resId = Utils.getResId(resourceName + i, R.drawable.class);

				if (resId < 0) {
					break;
				}
				pref.setIcon(getResources().getDrawable(resId));
				pref.setTitle(name + " " + (i + 1));
				pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference preference) {
						SharedPreferences customSharedPreference = Game.getSettings();
						SharedPreferences.Editor editor = customSharedPreference.edit();
						editor.putString(screen.getKey(), "" + index);
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

		public class MyOnPreferenceChangeListener implements OnPreferenceChangeListener {

			public MyOnPreferenceChangeListener() {
			}

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				SharedPreferences customSharedPreference = Game.getSettings();
				SharedPreferences.Editor editor = customSharedPreference.edit();

				if (newValue instanceof Boolean) {
					editor.putBoolean(preference.getKey(), (Boolean) newValue);

				} else if (newValue instanceof String) {
					editor.putString(preference.getKey(), (String) newValue);
				}
				editor.commit();

				return true;
			}

		}

	}

}
