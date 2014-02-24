/**
 * 
 */
package com.xklakoux.freespider;

import com.squareup.otto.Bus;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * @author artur
 *
 */
public class App extends Application {

	static private SharedPreferences settings;
	static private Context context;
	static private Bus bus;
	static private SettingsEvent event;
	static private int uniqueId;

	@Override
	public void onCreate() {
		super.onCreate();
		context = getApplicationContext();
		settings = PreferenceManager.getDefaultSharedPreferences(context);
		bus = new Bus();

	}

	public static SharedPreferences getSettings() {
		return settings;
	}

	public static Context getAppContext() {
		return context;
	}

	public static Bus getBus() {
		return bus;
	}
	public static int getUniqueId() {
		return uniqueId++;
	}
}
