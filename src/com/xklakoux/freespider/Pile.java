/**
 * 
 */
package com.xklakoux.freespider;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * @author artur
 * 
 */
public class Pile extends RelativeLayout {
	LayoutInflater inflater;
	public Pile(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public Pile(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

	}

	public Pile(Context context) {
		super(context);
	}

	// @Override
	// public View getChildAt(int index) {
	// return super.getChildAt(index-1);
	// }

	public View getCardAt(int index) {
		return super.getChildAt(index - 1);
	}

	// @Override
	// public int getChildCount() {
	// return super.getChildCount()-1;
	// }

	public int getCardsCount() {
		return super.getChildCount() - 1;
	}

	public void init() {
		inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.pile_layout, this, true);
		//		edit_text = (EditText) findViewById(R.id.clearable_edit);
		//		btn_clear = (Button) findViewById(R.id.clearable_button_clear);
		//		btn_clear.setVisibility(RelativeLayout.INVISIBLE);
	}
}
