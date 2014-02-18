/**
 * 
 */
package com.xklakoux.freespider;

import com.squareup.otto.Bus;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * @author artur
 *
 */
public class App extends Application {

	static private SharedPreferences settings;
	static private Context context;
	static private Bus bus;

	@Override
	public void onCreate() {
		super.onCreate();
		settings = getApplicationContext().getSharedPreferences("preferences", 0);
		context = getApplicationContext();
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

}
