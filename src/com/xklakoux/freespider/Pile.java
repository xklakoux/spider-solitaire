/**
 * 
 */
package com.xklakoux.freespider;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

/**
 * @author artur
 * 
 */
public class Pile extends RelativeLayout {

	private static final String TAG = Pile.class.getSimpleName();

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

	public Card getCardAt(int index) {
		return (Card) super.getChildAt(index + 1);
	}

	public int getCardsCount() {
		return super.getChildCount() - 1;
	}

	public int indexOfCard(Card child) {
		return super.indexOfChild(child) - 1;
	}

	public Card getLastCard() {
		if (getCardsCount() > 0) {
			return (Card) super.getChildAt(super.getChildCount() - 1);
		}
		return null;
	}

	public boolean isEmpty() {
		if (getCardsCount() > 0) {
			return false;
		}
		return true;
	}

	public View getLastTrueChild() {
		return super.getChildAt(super.getChildCount() - 1);
	}

	public ImageView getFirstCardSpot() {
		return (ImageView) super.getChildAt(0);
	}

	public void init() {
		inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.pile_layout, this, true);
	}

	public boolean uncoverLastCard() {
		if (getCardsCount() > 0) {
			Card lastCard = getLastCard();
			if (!lastCard.isFaceup()) {
				lastCard.setFaceup(true);
				return true;
			}
		}
		return false;
	}

	/**
	 * 
	 */
	public void removeLastCard() {
		super.removeViewAt(super.getChildCount()-1);
	}

	public void addCard(Card movedCard) {

		int marginTop = Utils.calculateMarginTop(this);

		MarginLayoutParams marginParams = new MarginLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		marginParams.setMargins(0, marginTop, 0, 0);
		Pile.LayoutParams layoutParams = new Pile.LayoutParams(marginParams);
		movedCard.setLayoutParams(layoutParams);
		addView(movedCard);
	}

	public void moveCard(Pile toPile, Card movedCard) {
		removeView(movedCard);
		toPile.addCard(movedCard);
	}

	public void refreshResources() {
		for(int i=0;i<getCardsCount();i++) {
			Card c = getCardAt(i);
			c.setFaceup(c.isFaceup());
		}
	}


}
