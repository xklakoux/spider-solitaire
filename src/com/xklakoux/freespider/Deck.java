/**
 * 
 */
package com.xklakoux.freespider;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.xklakoux.freespider.R;
import com.xklakoux.freespider.enums.Difficulty;
import com.xklakoux.freespider.enums.Number;
import com.xklakoux.freespider.enums.Suit;

/**
 * @author artur
 * 
 */
public class Deck extends RelativeLayout {

	private static final String TAG = Deck.class.getSimpleName();

	private List<Card> cards = new LinkedList<Card>();
	private final List<ImageView> tens = new LinkedList<ImageView>();

	LayoutInflater inflater;

	public Deck(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private Deck(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

	}

	private Deck(Context context) {
		super(context);

	}

	public Card getCard() {
		Card card = null;
		if (!cards.isEmpty()) {
			card = cards.get(0);
			cards.remove(0);
		}
		refresh();
		return card;
	}

	public void refresh() {
		int i = 0;

		String reverseResName = Game.getSettings().getString(Constant.SETT_REVERSE, Constant.DEFAULT_REVERSE);
		for (ImageView ten : tens) {
			ten.setImageResource((Utils.getResId("reverse_" + reverseResName, R.drawable.class)));

			if (cards.size() > i * 10 + 1) {
				tens.get(i).setVisibility(View.VISIBLE);
			} else {
				tens.get(i).setVisibility(View.INVISIBLE);
			}
			i++;
		}
	}

	public void addCard(Card card) {
		cards.add(0, card);
		refresh();
	}

	public void init() {

		inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.deck_layout, this, true);

		tens.add((ImageView) v.findViewById(R.id.ten5));
		tens.add((ImageView) v.findViewById(R.id.ten4));
		tens.add((ImageView) v.findViewById(R.id.ten3));
		tens.add((ImageView) v.findViewById(R.id.ten2));
		tens.add((ImageView) v.findViewById(R.id.ten1));

		setLooks();
	}

	/**
	 * 
	 */
	private void setLooks() {
		float size = Game.getAppContext().getResources().getDimension(R.dimen.card_stack_margin_down);
		float topMargin = 0;
		float leftMargin = 0;

		int orientation = Game.getAppContext().getResources().getConfiguration().orientation;

		if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
			topMargin = size;
		} else {
			leftMargin = size;

		}

		int i = 0;
		String reverseResName = Game.getSettings().getString(Constant.SETT_REVERSE, Constant.DEFAULT_REVERSE);

		for (ImageView ten : tens) {
			ten.setImageResource((Utils.getResId("reverse_" + reverseResName, R.drawable.class)));
			MarginLayoutParams marginParams = new MarginLayoutParams(ten.getLayoutParams());
			marginParams.setMargins((int) leftMargin * i, (int) topMargin * i, 0, 0);
			Deck.LayoutParams layoutParams = new Deck.LayoutParams(marginParams);
			ten.setLayoutParams(layoutParams);
			ten.bringToFront();
			i++;
		}
	}

	/**
	 * @param chosenDifficulty
	 */
	public void initialize(Difficulty chosenDifficulty, Context context) {

		cards.clear();

		int[] suits = new int[] {0, 0, 0, 0};
		switch (chosenDifficulty) {
		case EASY:
			suits[0] = 8;
			break;
		case MEDIUM:
			suits[0] = 4;
			suits[1] = 4;
			break;
		case HARD:
			suits[0] = 2;
			suits[1] = 2;
			suits[2] = 2;
			suits[3] = 2;
			break;
		}

		for (int i = 0; i < suits.length; i++) {
			for (int j = 0; j < suits[i]; j++) {
				for (Number num : Number.values()) {
					cards.add(new Card(context, Suit.values()[i], num));

				}
			}
		}
		Collections.shuffle(cards);

		Game.init(cards);

		refresh();
	}




	public void setSize(int cardWidth, int cardHeight) {
		for (ImageView ten : tens) {
			ten.getLayoutParams().width = cardWidth;
			ten.getLayoutParams().height = cardHeight;
		}
		setLooks();
	}



	public ImageView getLastVisible() {
		RelativeLayout rl = (RelativeLayout) getChildAt(0);
		int index = 0;
		if ((cards.size() / 10) >= 4) {
			index = 4;
		} else {
			index = cards.size() / 10;
		}
		return (ImageView) rl.getChildAt(index);
	}

	public boolean isEmpty() {
		return cards.isEmpty();
	}

	public void setCards(List<Card> cards) {
		this.cards = cards;
		refresh();
	}

}


