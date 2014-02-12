/**
 * 
 */
package com.xklakoux.freespider;

import android.app.Application;
import android.content.SharedPreferences;

/**
 * @author artur
 *
 */
public class App extends Application {

	static private SharedPreferences settings;

	@Override
	public void onCreate() {
		super.onCreate();
		settings = getApplicationContext().getSharedPreferences("preferences", 0);

	}

	public static SharedPreferences getSettings() {
		return settings;
	}


}
