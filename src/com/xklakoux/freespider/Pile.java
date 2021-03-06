/**
 * 
 */
package com.xklakoux.freespider;

import android.content.Context;
import android.util.AttributeSet;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;

import com.xklakoux.solitariolib.SettingsConstant;
import com.xklakoux.solitariolib.StatsManager;
import com.xklakoux.solitariolib.views.BasePile;
import com.xklakoux.solitariolib.views.Card;


public class Pile extends BasePile{

	private static final String TAG = Pile.class.getSimpleName();

	LayoutInflater inflater;

	public Pile(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void addCard(Card movedCard) {

		int marginTop = calculateMarginTop(this);

		MarginLayoutParams marginParams = new MarginLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		marginParams.setMargins(0, marginTop, 0, 0);
		BasePile.LayoutParams layoutParams = new BasePile.LayoutParams(marginParams);
		movedCard.setLayoutParams(layoutParams);
		addView(movedCard);
	}

	private int calculateMarginTop(BasePile pile) {
		int marginTop = 0;
		Context context = Game.getAppContext();
		for (int i = 0; i < pile.getCardsCount(); i++) {
			Card card = pile.getCardAt(i);
			float tempMargin = card.isFaceup() ? context.getResources().getDimension(R.dimen.card_stack_margin_up)
					: context.getResources().getDimension(R.dimen.card_stack_margin_down);
			marginTop += tempMargin;
		}
		return marginTop;
	}

	@Override
	protected boolean onCardsDrag(View v, DragEvent event) {
		Card draggedCard = (Card) event.getLocalState();
		if (draggedCard==null || draggedCard.getParent() == null || this==draggedCard.getParent()) {
			return false;
		}
		BasePile draggedParent = (BasePile) draggedCard.getParent();

		switch (event.getAction()) {
		case DragEvent.ACTION_DRAG_STARTED:
			break;
		case DragEvent.ACTION_DRAG_ENTERED:
			setHighlight(true);
			break;
		case DragEvent.ACTION_DRAG_EXITED:

			setHighlight(false);
			break;
		case DragEvent.ACTION_DROP:
			setHighlight(false);

			if(!fitOnEachother(getLastCard(), draggedCard)){
				break;
			}
			moveCards(draggedParent,draggedCard);
			return true;
		case DragEvent.ACTION_DRAG_ENDED:
			setHighlight(false);
			int indexOfDragged = draggedParent.indexOfCard(draggedCard);
			for (int i = indexOfDragged; i < draggedParent.getCardsCount(); i++) {
				Card card = draggedParent.getCardAt(i);
				card.setVisibility(View.VISIBLE);
			}
			return false;
		default:
			break;
		}
		return true;
	}

	//	@Override
	//	private void moveCards(BasePile draggedParent,Card draggedCard) {
	//		int indexOfDragged = draggedParent.indexOfCard(draggedCard);
	//		int movedCards = 0;
	//		for (int i = indexOfDragged; i < draggedParent.getCardsCount();) {
	//			draggedParent.getCardAt(i).setVisibility(View.VISIBLE);
	//			draggedParent.moveCard(this, draggedParent.getCardAt(i));
	//			movedCards++;
	//		}
	//		draggedParent.checkState(false);
	//
	//		int indexOfDraggedParent = (Integer) draggedParent.getTag();
	//		int indexOfLandingParent = (Integer) getTag();
	//
	//		Game.getMoves().add(new Move(movedCards, indexOfDraggedParent, indexOfLandingParent, draggedCard.getSuit(),
	//				Move.ACTION_MOVE));
	//		if (checkFullSetAndClear(true)) {
	//			Game.getLastMove().setCompleted(true);
	//		}
	//
	//		Game.getLastMove().setUncover(draggedParent.uncoverLastCard());
	//
	//		Game.getStatsManager().updateMoves(StatsManager.MOVE);
	//
	//		if (Game.getSettings().getBoolean(SettingsConstant.SOUNDS, true)) {
	//			Game.playSound(Game.SOUND_PUT_CARD);
	//		}
	//	}

	public boolean checkFullSetAndClear(boolean animation) {
		refresh();
		int lastIndex = getCardsCount() - 1;
		int counter = 1;
		Card referenceCard = getLastCard();
		for (int i = lastIndex - 1; i >= 0; i--) {
			Card card = getCardAt(i);
			if (!(referenceCard.getSuit() == card.getSuit())
					|| !(referenceCard.getRank().getId() == (card.getRank().getId() - 1)) || !card.isFaceup()) {

				if (Game.getSettings().getBoolean(SettingsConstant.HINTS, true)) {
					for (int j = i; j >= 0; j--) {
						Card c = getCardAt(j);
						if (c.isFaceup()) {
							c.setColorFilter(getResources().getColor(R.color.dim));
						} else {
							break;
						}
					}
				}
				break;
			}
			referenceCard = card;
			counter++;
		}

		float step = getResources().getDimension(R.dimen.card_stack_margin_up);

		if (counter == Game.FULL_NUMBER_SET) {
			for (int i = lastIndex; i > lastIndex - Game.FULL_NUMBER_SET; i--) {
				Card card = getCardAt(i);
				float deltaY = -(i - (lastIndex - Game.FULL_NUMBER_SET + 1)) * step;

				if (animation) {

					Animation trans = new TranslateAnimation(0.0f, 0.0f, 0.0f, deltaY);
					trans.setInterpolator(new DecelerateInterpolator(2.0f));
					trans.setDuration(500);
					trans.setFillAfter(true);
					trans.setAnimationListener(new FullSetClearAnimationListener(this, card));
					card.startAnimation(trans);
				} else {
					removeView(card);
					Game.getLastMove().setCompletedUncovered(uncoverLastCard());
					checkFullSetAndClear(false);
				}
			}

			Game.setCompleted();
			Game.getStatsManager().updatePoints(StatsManager.SET_COMPLETED);
			return true;
		}
		return false;
	}

	class FullSetClearAnimationListener implements AnimationListener {

		private final BasePile container;
		private final Card card;

		public FullSetClearAnimationListener(BasePile container, Card card) {
			this.container = container;
			this.card = card;
		}

		@Override
		public void onAnimationEnd(Animation animation) {
			container.removeView(card);
			Game.getLastMove().setCompletedUncovered(container.uncoverLastCard());
			container.checkState(true);
			// if (chosenSound) {
			// sp.play(drawSoundId, 1, 1, 0, 0, 1);
			// }
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
		}

		@Override
		public void onAnimationStart(Animation animation) {
			card.setOnDragListener(null);
		}

	}

	@Override
	public void moveCard(BasePile toPile, Card movedCard) {
		removeView(movedCard);
		toPile.addCard(movedCard);
	}

	@Override
	public void removeLastCard() {
		super.removeLastCard();
		checkFullSetAndClear(false);
	}

	@Override
	public void removeCard(Card card) {
		super.removeCard(card);
	}

	@Override
	protected boolean fitOnEachother(Card cardBelow, Card cardOver) {
		if (!isEmpty()) {
			Card lastCard = getLastCard();
			if (lastCard.getRank().getId() != cardOver.getRank().getId() + 1 || !lastCard.isFaceup()) {
				return false;
			}
		}
		return true;
	}

	@Override
	protected void moveCards(BasePile draggedParent, Card draggedCard) {
		int indexOfDragged = draggedParent.indexOfCard(draggedCard);
		int movedCards = 0;
		for (int i = indexOfDragged; i < draggedParent.getCardsCount();) {
			draggedParent.getCardAt(i).setVisibility(View.VISIBLE);
			draggedParent.moveCard(this, draggedParent.getCardAt(i));
			movedCards++;
		}
		checkFullSetAndClear(false);

		int indexOfDraggedParent = (Integer) draggedParent.getTag();
		int indexOfLandingParent = (Integer) getTag();

		Game.getMoves().add(new Move(movedCards, indexOfDraggedParent, indexOfLandingParent, draggedCard.getSuit(),
				Move.ACTION_MOVE));
		if (checkFullSetAndClear(true)) {
			Game.getLastMove().setCompleted(true);
		}

		Game.getLastMove().setUncover(draggedParent.uncoverLastCard());

		Game.getStatsManager().updateMoves(StatsManager.MOVE);

		if (Game.getSettings().getBoolean(SettingsConstant.SOUNDS, true)) {
			Game.playSound(Game.SOUND_PUT_CARD);
		}
	}
}
