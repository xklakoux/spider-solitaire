/**
 * 
 */
package com.xklakoux.freespider;

import java.lang.reflect.Field;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;

/**
 * @author artur
 *
 */
public class Utils {
	private static Context context = Game.getAppContext();

	public static int getResId(String variableName, Class<?> c) {

		try {
			Field idField = c.getDeclaredField(variableName);
			return idField.getInt(idField);
		} catch (Exception e) {
			return -1;
		}
	}



	public static int getStatusBarOffset(Activity activity, View v) {
		DisplayMetrics displayMetrics = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		int offsetY = displayMetrics.heightPixels - v.getMeasuredHeight();
		return offsetY;
	}

}
