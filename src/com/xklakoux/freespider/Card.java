/**
 * 
 */
package com.xklakoux.freespider;

import android.content.ClipData;
import android.content.Context;
import android.graphics.Point;
import android.support.v4.view.MotionEventCompat;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.xklakoux.freespider.enums.Number;
import com.xklakoux.freespider.enums.Suit;

/**
 * @author artur
 * 
 */
public class Card extends ImageView {

	public final String TAG = Card.class.getSimpleName();

	private Suit suit;
	private Number number;
	private boolean faceup = false;
	private final int reverseResourceId = (Utils.getResId("reverse_"+App.getSettings().getString(Constant.SETT_REVERSE, Constant.DEFAULT_REVERSE),R.drawable.class));


	public Card(Context context, Suit suit, Number number) {
		super(context);
		this.suit = suit;
		this.number = number;
		setAdjustViewBounds(true);
		setImageResource(reverseResourceId);
		setOnTouchListener(new CardTouchListener());
	}

	public Card(Context context, Card card) {
		super(context);
		suit = card.getSuit();
		number = card.getNumber();
		faceup = true;

	}

	public Suit getSuit() {
		return suit;
	}

	public void setSuit(Suit suit) {
		this.suit = suit;
	}

	public Number getNumber() {
		return number;
	}

	public void setNumber(Number number) {
		this.number = number;
	}

	public boolean isFaceup() {
		return faceup;
	}

	public void setFaceup(boolean faceup) {
		this.faceup = faceup;
		if (faceup) {
			String index = App.getSettings().getString(Constant.SETT_CARD_SET, Constant.DEFAULT_CARD_SET);
			setImageResource(Utils.getResId(suit.getName() + "_" + number.getId()+"_"+index, R.drawable.class));
		} else {
			String index = App.getSettings().getString(Constant.SETT_REVERSE, Constant.DEFAULT_REVERSE);
			setImageResource(Utils.getResId("reverse_"+index, R.drawable.class));
		}
	}

	class CardTouchListener implements OnTouchListener {

		public final String TAG = CardTouchListener.class.getSimpleName();

		@Override
		public boolean onTouch(View v, MotionEvent event) {

			final int action = MotionEventCompat.getActionMasked(event);

			switch (action) {
			case MotionEvent.ACTION_DOWN:

				Pile owner = (Pile) v.getParent();
				int index = owner.indexOfChild(v);
				Card card = (Card) v;

				//				if (index == owner.getChildCount() - 1 && !card.isFaceup()) {
				//					card.setFaceup(true);
				//					GameActivity.getMoves().add(new Move(GameActivity.getPiles().indexOf(owner), Move.ACTION_UNCOVER));
				//					return false;
				//				}

				if (isValidMove((Card) v)) {
					ClipData data = ClipData.newPlainText("", "");
					for (int i = 0; i < index; i++) {
						owner.getChildAt(i).setVisibility(View.INVISIBLE);
					}
					DragShadowBuilder shadowBuilder = new MyDragShadowBuilder(owner, card, event);
					v.startDrag(data, shadowBuilder, v, 0);
					for (int i = 0; i < index; i++) {
						owner.getChildAt(i).setVisibility(View.VISIBLE);
					}
					for (int i = index; i < owner.getChildCount(); i++) {
						owner.getChildAt(i).setVisibility(View.INVISIBLE);
					}
					return true;
				}

			}
			return false;
		}

		boolean isValidMove(Card selectedCard) {
			ViewGroup owner = (ViewGroup) selectedCard.getParent();
			int index = owner.indexOfChild(selectedCard);

			if (!selectedCard.isFaceup()) {
				return false;
			}

			Card referenceCard = selectedCard;
			for (int i = index + 1; i < owner.getChildCount(); i++) {
				Card card = (Card) owner.getChildAt(i);

				if (!(referenceCard.getSuit() == card.getSuit())
						|| !(referenceCard.getNumber().getId() - 1 == (card.getNumber().getId()))) {
					return false;
				}
				referenceCard = card;
			}
			return true;

		}

	}

	private class MyDragShadowBuilder extends View.DragShadowBuilder {

		Pile pile;
		Card card;
		MotionEvent event;

		private MyDragShadowBuilder(Pile pile, Card card, MotionEvent event) {
			super(pile);
			this.pile = pile;
			this.card = card;
			this.event = event;
		}

		@Override
		public void onProvideShadowMetrics(Point size, Point touch) {

			int width;
			int height;

			width = getView().getWidth();
			height = getView().getHeight();
			size.set(width, height);
			touch.set((int) event.getX(), calculateMarginTop(pile, card) + (int) event.getY());

		}

		private int calculateMarginTop(Pile pile, Card cardStart) {
			int marginTop = 0;
			for (int i = 0; i < pile.indexOfCard(cardStart); i++) {
				Card card = pile.getCardAt(i);
				float tempMargin = card.isFaceup() ? getResources().getDimension(R.dimen.card_stack_margin_up)
						: getResources().getDimension(R.dimen.card_stack_margin_down);
				marginTop += tempMargin;
			}
			return marginTop;
		}
	}

}