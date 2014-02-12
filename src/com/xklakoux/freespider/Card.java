/**
 * 
 */
package com.xklakoux.freespider;

import android.content.ClipData;
import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

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

	public Card(Context context, Suit suit, Number number) {
		super(context);
		this.suit = suit;
		this.number = number;
		setAdjustViewBounds(true);
		setImageResource(R.drawable.reverse);
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
			setImageResource(Utils.getResId(suit.getName() + "_" + number.getId(), R.drawable.class));
		} else {
			setImageResource(R.drawable.reverse);
		}
	}

	class CardTouchListener implements OnTouchListener {

		public final String TAG = CardTouchListener.class.getSimpleName();

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {

				RelativeLayout owner = (RelativeLayout) v.getParent();
				int index = owner.indexOfChild(v);
				Card card = (Card) v;

				if (index == owner.getChildCount() - 1 && !card.isFaceup()) {
					card.setFaceup(true);

					GameActivity.getMoves().add(new Move(GameActivity.getPiles().indexOf(owner), Move.ACTION_UNCOVER));
					return false;
				}

				if (isValidMove((Card) v)) {
					ClipData data = ClipData.newPlainText("", "");

					DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
					v.startDrag(data, shadowBuilder, v, 0);
					v.setVisibility(View.INVISIBLE);
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		}

		boolean isValidMove(Card selectedCard) {
			ViewGroup owner = (ViewGroup) selectedCard.getParent();
			int index = owner.indexOfChild(selectedCard);

			if (!selectedCard.isFaceup()) {
				Log.d(TAG, "face down");
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
			Log.d(TAG, "valid move");
			return true;

		}


	}

}
