/**
 * 
 */
package com.xklakoux.freespider;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnDragListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;

/**
 * @author artur
 * 
 */
public abstract class BasePile extends RelativeLayout implements OnDragListener{

	private static final String TAG = BasePile.class.getSimpleName();

	LayoutInflater inflater;

	public BasePile(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
		setOnDragListener(this);
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

	public ImageView getLastTrueChild() {
		return (ImageView) super.getChildAt(super.getChildCount() - 1);
	}

	public ImageView getFirstCardSpot() {
		return (ImageView) super.getChildAt(0);
	}

	public void init() {
		inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.pile_layout, this, true);
	}

	public void removeLastCard() {
		super.removeViewAt(super.getChildCount() - 1);
	}

	public void addCard(Card movedCard) {
		addView(movedCard);
	}

	public void moveCard(BasePile toPile, Card movedCard) {
		removeView(movedCard);
		toPile.addCard(movedCard);

	}

	public void refresh() {
		for (int i = 0; i < getCardsCount(); i++) {
			Card c = getCardAt(i);
			c.setFaceup(c.isFaceup());
			c.setColorFilter(Color.TRANSPARENT);
		}
	}

	public void setHighlight(boolean highlight) {
		if (highlight) {
			getLastTrueChild().setColorFilter(getResources().getColor(R.color.highlight));
		} else {
			getLastTrueChild().setColorFilter(getResources().getColor(android.R.color.transparent));
		}
	}

	abstract protected boolean canBeDropped(BasePile draggedParent, Card draggedCard);

	abstract protected boolean onCardsDrag(View v, DragEvent event);

	@Override
	public boolean onDrag(View v, DragEvent event) {
		return onCardsDrag(v, event);
	}

	public void removeCard(Card card) {
		super.removeView(card);
	}

}
