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

	public static int calculateMarginTop(Pile pile) {
		int marginTop = 0;
		for (int i = 0; i < pile.getCardsCount(); i++) {
			Card card = pile.getCardAt(i);
			float tempMargin = card.isFaceup() ? context.getResources().getDimension(R.dimen.card_stack_margin_up)
					: context.getResources().getDimension(R.dimen.card_stack_margin_down);
			marginTop += tempMargin;
		}
		return marginTop;
	}


}
