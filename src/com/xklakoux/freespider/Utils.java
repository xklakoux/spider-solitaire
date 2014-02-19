/**
 * 
 */
package com.xklakoux.freespider;

import java.lang.reflect.Field;

import android.content.Context;

/**
 * @author artur
 *
 */
public class Utils {
	private static Context context = App.getAppContext();

	public static int getResId(String variableName, Class<?> c) {

		try {
			Field idField = c.getDeclaredField(variableName);
			return idField.getInt(idField);
		} catch (Exception e) {
			return -1;
		}
	}

}
