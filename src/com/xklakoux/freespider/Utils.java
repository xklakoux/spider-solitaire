/**
 * 
 */
package com.xklakoux.freespider;

import java.lang.reflect.Field;

/**
 * @author artur
 *
 */
public class Utils {
	public static int getResId(String variableName, Class<?> c) {

		try {
			Field idField = c.getDeclaredField(variableName);
			return idField.getInt(idField);
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}
}
