package com.xklakoux.freespider;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnDragListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xklakoux.freespider.enums.Difficulty;
import com.xklakoux.freespider.enums.GameState;
import com.xklakoux.freespider.enums.Number;
import com.xklakoux.freespider.enums.Suit;

public class GameActivity extends Activity {

	private GameState gameState = GameState.NOT_STARTED;

	private final static Stack<Move> moves = new Stack<Move>();

	public final String TAG = GameActivity.class.getSimpleName();

	private final int FULL_NUMBER_SET = 13;

	private static int decksCompleted = 0;

	final public int LEVEL_EASY = 1;
	final public int LEVEL_MEDIUM = 2;
	final public int LEVEL_HARD = 4;

	final public int NUMBER_OF_DECKS = 8;

	static List<RelativeLayout> pileLayouts;
	final List<LinkedList<Card>> piles = new LinkedList<LinkedList<Card>>();

	private ImageView deck;
	private TextView winner;

	private Difficulty chosenDifficulty;

	public List<Card> allCards = new LinkedList<Card>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.game_layout);

		findViewsAndSetListeners();
		Log.d(TAG, gameState.toString());
		if (gameState == GameState.NOT_STARTED) {
			chosenDifficulty = Difficulty.valueOf(App.getSettings().getString("difficulty", "MEDIUM"));
			setupNewGame();
			gameState = GameState.STARTED;
			Log.d(TAG, gameState.toString());

		}

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putAll(outState);
	}

	private void findViewsAndSetListeners() {
		winner = (TextView) findViewById(R.id.winner);

		deck = (ImageView) findViewById(R.id.deck);
		deck.setOnClickListener(new DeckClickListener());

		pileLayouts = new LinkedList<RelativeLayout>();
		pileLayouts.add((RelativeLayout) findViewById(R.id.pile0));
		pileLayouts.add((RelativeLayout) findViewById(R.id.pile1));
		pileLayouts.add((RelativeLayout) findViewById(R.id.pile2));
		pileLayouts.add((RelativeLayout) findViewById(R.id.pile3));
		pileLayouts.add((RelativeLayout) findViewById(R.id.pile4));
		pileLayouts.add((RelativeLayout) findViewById(R.id.pile5));
		pileLayouts.add((RelativeLayout) findViewById(R.id.pile6));
		pileLayouts.add((RelativeLayout) findViewById(R.id.pile7));
		pileLayouts.add((RelativeLayout) findViewById(R.id.pile8));
		pileLayouts.add((RelativeLayout) findViewById(R.id.pile9));

		for (RelativeLayout rl : pileLayouts) {
			rl.setOnDragListener(new CardOnDragListener());
		}
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);

		Log.d("TAG", "" + pileLayouts.get(0).getWidth());
		deck.getLayoutParams().width = pileLayouts.get(0).getWidth();
		deck.requestLayout();
	}

	class CardOnDragListener implements OnDragListener {

		@Override
		public boolean onDrag(View v, DragEvent event) {
			View draggedCard;
			switch (event.getAction()) {
			case DragEvent.ACTION_DRAG_STARTED:
				break;
			case DragEvent.ACTION_DRAG_ENTERED:
				break;
			case DragEvent.ACTION_DRAG_EXITED:
				break;
			case DragEvent.ACTION_DROP:

				draggedCard = (View) event.getLocalState();
				draggedCard.setVisibility(View.VISIBLE);
				RelativeLayout draggedParent = (RelativeLayout) draggedCard.getParent();
				int indexOfDragged = draggedParent.indexOfChild(draggedCard);

				RelativeLayout landingContainer = (RelativeLayout) v;

				if (landingContainer.getChildCount() != 1) {
					Card lastCard = (Card) landingContainer.getChildAt(landingContainer.getChildCount() - 1);
					if (lastCard.getNumber().getId() != ((Card) draggedCard).getNumber().getId() + 1
							|| !lastCard.isFaceup()) {
						break;
					}
				}
				int movedCards = 0;

				Card card = (Card) draggedParent.getChildAt(indexOfDragged);
				for (int i = indexOfDragged; i < draggedParent.getChildCount();) {

					moveCard(draggedParent, landingContainer, (Card) draggedParent.getChildAt(i));
					movedCards++;
				}

				int indexOfDraggedParent = pileLayouts.indexOf(draggedParent);
				int indexOfLandingParent = pileLayouts.indexOf(landingContainer);

				if (checkFullSetAndClear(landingContainer)) {

					moves.add(new Move(movedCards, indexOfDraggedParent, indexOfLandingParent, card.getSuit(),
							Move.ACTION_COMPLETE));
				} else {

					moves.add(new Move(movedCards, indexOfDraggedParent, indexOfLandingParent, card.getSuit(),
							Move.ACTION_MOVE));
				}

				break;
			case DragEvent.ACTION_DRAG_ENDED:
				draggedCard = (View) event.getLocalState();
				draggedCard.setVisibility(View.VISIBLE);
			default:
				break;
			}
			return true;
		}

		boolean checkFullSetAndClear(RelativeLayout container) {
			if (container.getChildCount() > 1) {
				int lastIndex = container.getChildCount() - 1;
				int counter = 1;

				Card referenceCard = (Card) container.getChildAt(lastIndex);
				for (int i = lastIndex - 1; i > 0; i--) {
					Card card = (Card) container.getChildAt(i);
					if (!(referenceCard.getSuit() == card.getSuit())
							|| !(referenceCard.getNumber().getId() == (card.getNumber().getId() - 1))
							|| !card.isFaceup()) {
						break;
					}
					referenceCard = card;
					counter++;
				}
				Log.d(TAG, "counter " + counter);

				if (counter == FULL_NUMBER_SET) {
					container.removeViews(container.getChildCount() - FULL_NUMBER_SET, FULL_NUMBER_SET);
					decksCompleted++;
					if (decksCompleted == NUMBER_OF_DECKS) {
						winner.setVisibility(View.VISIBLE);
						gameState = GameState.FINISHED;
					}
					return true;
				}
			}
			return false;
		}
	}

	private List<Card> chooseDecks(Difficulty difficulty) {

		List<Card> cards = new LinkedList<Card>();

		int spades = 0, clubs = 0, diamonds = 0, hearts = 0;
		switch (difficulty) {
		case EASY:
			spades = 8;
			break;
		case MEDIUM:
			spades = 4;
			hearts = 4;
			break;
		case HARD:
			spades = 2;
			hearts = 2;
			spades = 2;
			diamonds = 2;
			break;
		}

		for (int a = 0; a < spades; a++) {
			for (Number num : Number.values()) {
				cards.add(new Card(this, Suit.SPADES, num));
			}
		}
		for (int a = 0; a < clubs; a++) {
			for (Number num : Number.values()) {
				cards.add(new Card(this, Suit.CLUBS, num));
			}
		}
		for (int a = 0; a < diamonds; a++) {
			for (Number num : Number.values()) {
				cards.add(new Card(this, Suit.DIAMONDS, num));
			}
		}
		for (int a = 0; a < hearts; a++) {
			for (Number num : Number.values()) {
				cards.add(new Card(this, Suit.HEARTS, num));
			}
		}

		return cards;
	}

	void setupTable() {
		for (int k = 0; k < 54; k++) {
			RelativeLayout rl = pileLayouts.get(k % 10);

			Card card = allCards.get(0);

			if (54 - k <= 10) {
				card.setFaceup(true);
			}

			addCard(rl, card);
			allCards.remove(0);
		}
	}

	class DeckClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			for (RelativeLayout rl : pileLayouts) {

				Card card = allCards.get(0);
				card.setFaceup(true);
				addCard(rl, card);

				allCards.remove(0);
			}
			if (allCards.isEmpty()) {
				deck.setVisibility(View.GONE);
			}
			moves.add(new Move(Move.ACTION_DEAL));
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.game_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		case R.id.action_undo:
			undo();
			return true;
		case R.id.action_new_game:
			setupNewGame();
			return true;
		case R.id.action_settings:
			showSettings();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void setupNewGame() {
		moves.clear();
		for (RelativeLayout pileLayout : pileLayouts) {
			pileLayout.removeViews(1, pileLayout.getChildCount() - 1);
		}
		allCards = chooseDecks(chosenDifficulty);
		Collections.shuffle(allCards);
		setupTable();
	}

	private void showSettings() {
		AlertDialog.Builder builderSingle = new AlertDialog.Builder(GameActivity.this);
		builderSingle.setTitle("Settings");
		final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(GameActivity.this,
				android.R.layout.simple_list_item_1);
		arrayAdapter.add("Set difficulty: " + chosenDifficulty.toString());
		builderSingle.setPositiveButton("Save", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				AlertDialog.Builder builderInner = new AlertDialog.Builder(GameActivity.this);
				final ArrayAdapter<String> difficultyAdapter = new ArrayAdapter<String>(GameActivity.this,
						android.R.layout.select_dialog_singlechoice);
				difficultyAdapter.add("Easy");
				difficultyAdapter.add("Medium");
				difficultyAdapter.add("Hard");
				builderInner.setAdapter(difficultyAdapter, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						String value = difficultyAdapter.getItem(which).toUpperCase();
						chosenDifficulty = Difficulty.valueOf(value);
						App.getSettings().edit().putString("difficulty", value).commit();
						dialog.dismiss();
					}
				});
				builderInner.setTitle("Difficulty");
				builderInner.show();
			}
		});
		builderSingle.show();
	}

	void undo() {
		if (moves.isEmpty() || gameState == gameState.FINISHED) {
			return;
		}

		Move move = moves.pop();
		RelativeLayout landingContainer = pileLayouts.get(move.getFrom());
		RelativeLayout draggedParent = pileLayouts.get(move.getTo());
		switch (move.getAction()) {
		case Move.ACTION_COMPLETE:
			decksCompleted--;
			int amount = Number.values().length - move.getAmount();
			for (Number num : Number.values()) {
				if (amount > 0) {
					Card card = new Card(GameActivity.this, move.getSuit(), num);
					card.setFaceup(true);
					addCard(draggedParent, card);
				} else {
					Card card = new Card(GameActivity.this, move.getSuit(), num);
					card.setFaceup(true);
					addCard(landingContainer, card);

				}
				amount--;
			}

			break;

		case Move.ACTION_MOVE:
			int indexOfDragged = draggedParent.getChildCount() - move.getAmount();

			for (int i = indexOfDragged; i < draggedParent.getChildCount();) {
				moveCard(draggedParent, landingContainer, (Card) draggedParent.getChildAt(i));
			}
			break;

		case Move.ACTION_UNCOVER:
			Card lastCard = (Card) landingContainer.getChildAt(landingContainer.getChildCount() - 1);
			lastCard.setFaceup(false);
			break;

		case Move.ACTION_DEAL:
			if (allCards.isEmpty()) {
				deck.setVisibility(View.VISIBLE);
			}
			for (RelativeLayout rl : pileLayouts) {

				Card card = (Card) rl.getChildAt(rl.getChildCount() - 1);
				card.setFaceup(false);
				allCards.add(0, card);
				rl.removeViewAt(rl.getChildCount() - 1);
			}
			break;
		}

	}

	private void moveCard(RelativeLayout draggedParent, RelativeLayout landingContainer, Card movedCard) {
		draggedParent.removeView(movedCard);
		addCard(landingContainer, movedCard);
	}

	private void addCard(RelativeLayout layout, Card movedCard) {

		int marginTop = calculateMarginTop(layout);

		MarginLayoutParams marginParams = new MarginLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT));
		marginParams.setMargins(0, marginTop, 0, 0);
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(marginParams);
		movedCard.setLayoutParams(layoutParams);
		layout.addView(movedCard);
	}

	private int calculateMarginTop(RelativeLayout rl) {
		int marginTop = 0;
		for (int i = 1; i < rl.getChildCount(); i++) {
			Card card = (Card) rl.getChildAt(i);
			float tempMargin = card.isFaceup() ? getResources().getDimension(R.dimen.card_stack_margin_up)
					: getResources().getDimension(R.dimen.card_stack_margin_down);
			marginTop += tempMargin;
		}
		return marginTop;
	}

	public static List<RelativeLayout> getPiles() {
		return pileLayouts;
	}

	public static Stack<Move> getMoves() {
		return moves;
	}
}