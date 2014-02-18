/**
 * 
 */
package com.xklakoux.freespider;

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

	@Override
	public void onCreate() {
		super.onCreate();
		settings = getApplicationContext().getSharedPreferences("preferences", 0);
		context = getApplicationContext();

	}

	public static SharedPreferences getSettings() {
		return settings;
	}

	public static Context getAppContext() {
		return context;
	}
}
