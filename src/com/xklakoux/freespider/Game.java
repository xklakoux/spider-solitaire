package com.xklakoux.freespider;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Stack;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.DisplayMetrics;
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
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ArrayAdapter;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.xklakoux.freespider.enums.Difficulty;
import com.xklakoux.freespider.enums.GameState;
import com.xklakoux.freespider.enums.Number;
import com.xklakoux.freespider.enums.Suit;

public class Game extends Activity {

	private static final int START_CARDS_DEAL_COUNT = 54;

	private StatsManager statsManager;

	private GameState gameState = GameState.NOT_STARTED;

	private final static Stack<Move> moves = new Stack<Move>();

	public final String TAG = Game.class.getSimpleName();

	private final int FULL_NUMBER_SET = 13;

	private static int decksCompleted = 0;

	final public int LEVEL_EASY = 1;
	final public int LEVEL_MEDIUM = 2;
	final public int LEVEL_HARD = 4;

	final public int NUMBER_OF_DECKS = 8;

	private RelativeLayout root;

	static List<Pile> pileLayouts;
	final List<LinkedList<Card>> piles = new LinkedList<LinkedList<Card>>();

	private ImageView deck;
	private TextView winner;

	private Difficulty chosenDifficulty = Difficulty.valueOf(App.getSettings().getString("difficulty", "MEDIUM"));

	public List<Card> allCards = new LinkedList<Card>();

	private int cardsDealt = 0;
	boolean dealingRightNow = false;

	boolean onCreate = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.game_layout);
		findViewsSetListenersAndManagers();
		setupDeckMeasurements();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putAll(outState);
	}

	private void findViewsSetListenersAndManagers() {
		winner = (TextView) findViewById(R.id.winner);

		pileLayouts = new LinkedList<Pile>();
		pileLayouts.add((Pile) findViewById(R.id.pile0));
		pileLayouts.add((Pile) findViewById(R.id.pile1));
		pileLayouts.add((Pile) findViewById(R.id.pile2));
		pileLayouts.add((Pile) findViewById(R.id.pile3));
		pileLayouts.add((Pile) findViewById(R.id.pile4));
		pileLayouts.add((Pile) findViewById(R.id.pile5));
		pileLayouts.add((Pile) findViewById(R.id.pile6));
		pileLayouts.add((Pile) findViewById(R.id.pile7));
		pileLayouts.add((Pile) findViewById(R.id.pile8));
		pileLayouts.add((Pile) findViewById(R.id.pile9));

		for (Pile rl : pileLayouts) {
			rl.setOnDragListener(new PileOnDragListener());
		}

		deck = (ImageView) findViewById(R.id.deck);
		deck.setOnClickListener(new OnDeckClickListener());
		deck.setVisibility(View.INVISIBLE);
		root = (RelativeLayout) findViewById(R.id.root);

		TextView score = (TextView) findViewById(R.id.tvScore);
		TextView movesCount = (TextView) findViewById(R.id.tvMoves);
		Chronometer time = (Chronometer) findViewById(R.id.tvTimeElapsed);
		statsManager = new StatsManager(Game.this, score, movesCount, time);
	}

	class OnDeckClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {

			if (!cardsCorrect()) {
				return;
			}

			if (gameState != GameState.STARTED) {
				return;
			}

			int i = 0;
			gameState = GameState.DEALING;
			for (Pile rl : pileLayouts) {
				int count = rl.getChildCount();
				if (count > 1) {
					Card possiblyCovered = (Card) rl.getChildAt(count - 1);
					if (!possiblyCovered.isFaceup()) {
						Toast.makeText(Game.this, R.string.uncover_all_cards_first, Toast.LENGTH_SHORT).show();
						return;
					}
				}
				Card card = allCards.get(0);
				dealCard(rl, card, true, i++, false);
				allCards.remove(0);
			}

			if (allCards.isEmpty()) {
				deck.setVisibility(View.INVISIBLE);
			}
			moves.add(new Move(Move.ACTION_DEAL));
		}

		/**
		 * @return
		 */
		private boolean cardsCorrect() {
			for (Pile rl : pileLayouts) {
				if (rl.getChildCount() < 2) {
					Toast.makeText(Game.this, R.string.all_tableaus_should_be_filled, Toast.LENGTH_SHORT).show();
					return false;
				} else {
					int count = rl.getChildCount();
					Card possiblyCovered = (Card) rl.getChildAt(count - 1);
					if (!possiblyCovered.isFaceup()) {
						Toast.makeText(Game.this, R.string.uncover_all_cards_first, Toast.LENGTH_SHORT).show();
						return false;
					}
				}
			}
			return true;
		}

	}

	private int getStatusBarOffset() {
		DisplayMetrics displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		int offsetY = displayMetrics.heightPixels - root.getMeasuredHeight();
		return offsetY;
	}

	private void dealCard(Pile rl, Card card, boolean faceup, int hundredMillisecondOffset,
			boolean isNewGameDeal) {

		int[] deckLocation = new int[2];
		deck.getLocationOnScreen(deckLocation);
		Log.d(TAG + " deckLocations", deckLocation[0] + " " + deckLocation[1]);

		int statusBarOffsetY = getStatusBarOffset();

		int[] location = new int[2];
		rl.getChildAt(rl.getChildCount() - 1).getLocationOnScreen(location);
		Log.d(TAG + " locations", location[0] + " " + location[1]);

		final Card fakeCard = new Card(Game.this, Suit.SPADES, Number.ACE);
		root.addView(fakeCard);

		ViewTreeObserver vto = pileLayouts.get(0).getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@SuppressWarnings("deprecation")
			@Override
			public void onGlobalLayout() {
				pileLayouts.get(0).getViewTreeObserver().removeGlobalOnLayoutListener(this);
				fakeCard.getLayoutParams().width = pileLayouts.get(0).getMeasuredWidth();
				fakeCard.setScaleType(ScaleType.CENTER_INSIDE);

			}
		});

		float horizontalMargin = getResources().getDimension(R.dimen.activity_horizontal_margin);
		float verticalMargin = getResources().getDimension(R.dimen.activity_vertical_margin);

		int cardsOffset = hundredMillisecondOffset / pileLayouts.size();
		float stackMargin = 0;
		if (isNewGameDeal) {
			stackMargin = getResources().getDimension(R.dimen.card_stack_margin_down) * cardsOffset;

		} else {
			stackMargin = getResources().getDimension(R.dimen.card_stack_margin_up);
		}
		float fromX = deckLocation[0] - horizontalMargin;
		float toX = location[0] - horizontalMargin;
		float fromY = deckLocation[1] - statusBarOffsetY - verticalMargin;
		float toY = location[1] - statusBarOffsetY - verticalMargin + stackMargin;

		Animation anim = new TranslateAnimation(Animation.ABSOLUTE, fromX, Animation.ABSOLUTE, toX, Animation.ABSOLUTE,
				fromY, Animation.ABSOLUTE, toY);

		anim.setInterpolator(new DecelerateInterpolator(2.0f));
		anim.setDuration(400);
		anim.setStartOffset(hundredMillisecondOffset++ * 50);
		anim.setAnimationListener(new DealAnimationListener(rl, card, root, fakeCard));
		fakeCard.setAnimation(anim);

		card.setFaceup(faceup);
	}

	class DealAnimationListener implements AnimationListener {

		private final Pile container;
		private final Card card;
		private final Card fakeAnimCard;
		private final RelativeLayout root;

		public DealAnimationListener(Pile container, Card card, RelativeLayout root, Card fakeAnimCard) {
			this.container = container;
			this.card = card;
			this.fakeAnimCard = fakeAnimCard;
			this.root = root;
		}

		@Override
		public void onAnimationEnd(Animation animation) {
			addCard(container, card);
			root.removeView(fakeAnimCard);
			cardsDealt++;
			if (cardsDealt == START_CARDS_DEAL_COUNT) {
				gameState = GameState.STARTED;
				statsManager.clearStatsAndGo();
			}
			Log.d(TAG, "endin'");
			gameState = GameState.STARTED;
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
		}

		@Override
		public void onAnimationStart(Animation animation) {
			Log.d(TAG, "dealin'");
			gameState = GameState.DEALING;
		}

	}

	private void setupDeckMeasurements() {
		ViewTreeObserver vto = pileLayouts.get(0).getChildAt(0).getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@SuppressWarnings("deprecation")
			@Override
			public void onGlobalLayout() {
				pileLayouts.get(0).getViewTreeObserver().removeGlobalOnLayoutListener(this);
				deck.getLayoutParams().width = pileLayouts.get(0).getChildAt(0).getMeasuredWidth();
				deck.getLayoutParams().height = pileLayouts.get(0).getChildAt(0).getMeasuredHeight();
				deck.setAdjustViewBounds(true);
				if (onCreate && gameState == GameState.NOT_STARTED) {
					setupNewGame();
				}
			}
		});

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
			clubs = 2;
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

	private void dealNewGame() {

		for (int k = 0; k < START_CARDS_DEAL_COUNT; k++) {
			Pile rl = pileLayouts.get(k % 10);

			Card card = allCards.get(0);

			boolean faceUp = START_CARDS_DEAL_COUNT - k <= pileLayouts.size();
			dealCard(rl, card, faceUp, k, true);

			allCards.remove(0);
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
			if (gameState == GameState.STARTED) {
				showNewGameDialog();
			} else if (gameState == GameState.DEALING) {
				// do nothing
			} else {
				setupNewGame();
			}
			return true;
		case R.id.action_settings:
			showSettings();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void showNewGameDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(Game.this);
		builder.setMessage("Current progress will be lost. Are you sure you want to start new game?");
		builder.setTitle("Start new game");
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				setupNewGame();
			}
		});
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	private void setupNewGame() {

		chosenDifficulty = Difficulty.valueOf(App.getSettings().getString("difficulty", "MEDIUM"));
		moves.clear();
		decksCompleted = 0;
		cardsDealt = 0;
		for (Pile pileLayout : pileLayouts) {
			pileLayout.removeViews(1, pileLayout.getChildCount() - 1);
		}
		allCards = chooseDecks(chosenDifficulty);
		Collections.shuffle(allCards);
		dealNewGame();
		winner.setVisibility(View.GONE);
		deck.setVisibility(View.VISIBLE);
		statsManager.timeStop();
	}

	private void showSettings() {
		AlertDialog.Builder builderSingle = new AlertDialog.Builder(Game.this);
		builderSingle.setTitle("Settings");
		final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(Game.this,
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

				switch (which) {
				case 0:
					AlertDialog.Builder builderInner = new AlertDialog.Builder(Game.this);
					final ArrayAdapter<String> difficultyAdapter = new ArrayAdapter<String>(Game.this,
							android.R.layout.select_dialog_singlechoice);
					difficultyAdapter.add("Easy");
					difficultyAdapter.add("Medium");
					difficultyAdapter.add("Hard");
					builderInner.setAdapter(difficultyAdapter, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							String value = difficultyAdapter.getItem(which).toUpperCase(Locale.getDefault());
							chosenDifficulty = Difficulty.valueOf(value);
							App.getSettings().edit().putString("difficulty", value).commit();
							dialog.dismiss();
						}
					});
					builderInner.setTitle("Difficulty");
					builderInner.show();
					break;
				}
			}
		});
		builderSingle.show();
	}

	private void undo() {
		if (moves.isEmpty() || gameState == GameState.FINISHED) {
			return;
		}
		int amount;
		Card cardToUncover;
		int indexOfDragged;

		Move move = moves.pop();
		Pile landingContainer = pileLayouts.get(move.getFrom());
		Pile draggedParent = pileLayouts.get(move.getTo());
		int indexToUncover = landingContainer.getChildCount() - 1;
		switch (move.getAction()) {
		case Move.ACTION_COMPLETE:
			decksCompleted--;
			amount = Number.values().length - move.getAmount();
			for (Number num : Number.values()) {
				if (amount > 0) {
					Card card = new Card(Game.this, move.getSuit(), num);
					card.setFaceup(true);
					addCard(draggedParent, card);
				} else {
					Card card = new Card(Game.this, move.getSuit(), num);
					card.setFaceup(true);
					addCard(landingContainer, card);

				}
				statsManager.updateMoves(StatsManager.MOVE);
				amount--;
			}

			break;
		case Move.ACTION_COMPLETE_UNCOVER:
			decksCompleted--;

			cardToUncover = (Card) landingContainer.getChildAt(indexToUncover);
			cardToUncover.setFaceup(false);

			amount = Number.values().length - move.getAmount();
			for (Number num : Number.values()) {
				if (amount > 0) {
					Card card = new Card(Game.this, move.getSuit(), num);
					card.setFaceup(true);
					addCard(draggedParent, card);
				} else {
					Card card = new Card(Game.this, move.getSuit(), num);
					card.setFaceup(true);
					addCard(landingContainer, card);

				}
				statsManager.updateMoves(StatsManager.MOVE);
				amount--;
			}
			break;
		case Move.ACTION_MOVE:
			indexOfDragged = draggedParent.getChildCount() - move.getAmount();

			for (int i = indexOfDragged; i < draggedParent.getChildCount();) {
				moveCard(draggedParent, landingContainer, (Card) draggedParent.getChildAt(i));
			}
			statsManager.updateMoves(StatsManager.MOVE);
			break;

		case Move.ACTION_MOVE_UNCOVER:
			indexOfDragged = draggedParent.getChildCount() - move.getAmount();

			cardToUncover = (Card) landingContainer.getChildAt(indexToUncover);
			cardToUncover.setFaceup(false);

			for (int i = indexOfDragged; i < draggedParent.getChildCount();) {
				moveCard(draggedParent, landingContainer, (Card) draggedParent.getChildAt(i));
			}
			statsManager.updateMoves(StatsManager.MOVE);

			break;

		case Move.ACTION_DEAL:
			if (allCards.isEmpty()) {
				deck.setVisibility(View.VISIBLE);
			}
			for (Pile rl : pileLayouts) {

				Card card = (Card) rl.getChildAt(rl.getChildCount() - 1);
				card.setFaceup(false);
				allCards.add(0, card);
				rl.removeViewAt(rl.getChildCount() - 1);
			}
			statsManager.updateMoves(StatsManager.MOVE);
			break;
		}

	}

	private void moveCard(Pile draggedParent, Pile landingContainer, Card movedCard) {
		draggedParent.removeView(movedCard);
		addCard(landingContainer, movedCard);
	}

	private void addCard(Pile layout, Card movedCard) {

		int marginTop = calculateMarginTop(layout);

		MarginLayoutParams marginParams = new MarginLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		marginParams.setMargins(0, marginTop, 0, 0);
		Pile.LayoutParams layoutParams = new Pile.LayoutParams(marginParams);
		movedCard.setLayoutParams(layoutParams);
		layout.addView(movedCard);
	}

	private int calculateMarginTop(Pile rl) {
		int marginTop = 0;
		for (int i = 1; i < rl.getChildCount(); i++) {
			Card card = (Card) rl.getChildAt(i);
			float tempMargin = card.isFaceup() ? getResources().getDimension(R.dimen.card_stack_margin_up)
					: getResources().getDimension(R.dimen.card_stack_margin_down);
			marginTop += tempMargin;
		}
		return marginTop;
	}

	public static List<Pile> getPiles() {
		return pileLayouts;
	}

	public static Stack<Move> getMoves() {
		return moves;
	}

	class PileOnDragListener implements OnDragListener {

		@Override
		public boolean onDrag(View v, DragEvent event) {

			Card draggedCard = (Card) event.getLocalState();
			if (draggedCard == null) {
				return false;
			}
			Pile draggedParent = (Pile) draggedCard.getParent();
			if (draggedParent == null) {
				return false;
			}
			int indexOfDragged = draggedParent.indexOfChild(draggedCard);

			Pile landingContainer = (Pile) v;

			switch (event.getAction()) {
			case DragEvent.ACTION_DRAG_STARTED:
				break;
			case DragEvent.ACTION_DRAG_ENTERED:
				break;
			case DragEvent.ACTION_DRAG_EXITED:
				break;
			case DragEvent.ACTION_DROP:
				Log.d(TAG, "drop");
				if (landingContainer.getChildCount() != 1) {
					Card lastCard = (Card) landingContainer.getChildAt(landingContainer.getChildCount() - 1);
					if (lastCard.getNumber().getId() != draggedCard.getNumber().getId() + 1 || !lastCard.isFaceup()) {
						return false;
					}
				}
				int movedCards = 0;
				for (int i = indexOfDragged; i < draggedParent.getChildCount();) {
					draggedParent.getChildAt(i).setVisibility(View.VISIBLE);
					moveCard(draggedParent, landingContainer, (Card) draggedParent.getChildAt(i));
					movedCards++;
				}

				int indexOfDraggedParent = pileLayouts.indexOf(draggedParent);
				int indexOfLandingParent = pileLayouts.indexOf(landingContainer);

				if (checkFullSetAndClear(landingContainer)) {
					if (draggedParent.getChildCount() > 2) {
						Card previousCard = (Card) draggedParent.getChildAt(indexOfDragged - 1);
						if (previousCard.isFaceup()) {
							moves.add(new Move(movedCards, indexOfDraggedParent, indexOfLandingParent, draggedCard
									.getSuit(), Move.ACTION_COMPLETE));
						} else {
							moves.add(new Move(movedCards, indexOfDraggedParent, indexOfLandingParent, draggedCard
									.getSuit(), Move.ACTION_COMPLETE_UNCOVER));
							previousCard.setFaceup(true);
						}
					} else {
						moves.add(new Move(movedCards, indexOfDraggedParent, indexOfLandingParent, draggedCard
								.getSuit(), Move.ACTION_COMPLETE));
					}
				} else {
					if (draggedParent.getChildCount() > 2) {
						Card previousCard = (Card) draggedParent.getChildAt(indexOfDragged - 1);
						if (previousCard.isFaceup()) {
							moves.add(new Move(movedCards, indexOfDraggedParent, indexOfLandingParent, draggedCard
									.getSuit(), Move.ACTION_MOVE));
						} else {
							moves.add(new Move(movedCards, indexOfDraggedParent, indexOfLandingParent, draggedCard
									.getSuit(), Move.ACTION_MOVE_UNCOVER));
							previousCard.setFaceup(true);
						}
					} else {
						moves.add(new Move(movedCards, indexOfDraggedParent, indexOfLandingParent, draggedCard
								.getSuit(), Move.ACTION_MOVE));
					}
				}
				statsManager.updateMoves(StatsManager.MOVE);
				return true;
			case DragEvent.ACTION_DRAG_ENDED:
				for (int i = indexOfDragged; i < draggedParent.getChildCount(); i++) {
					Card card = (Card) draggedParent.getChildAt(i);
					card.setVisibility(View.VISIBLE);
				}
				return false;
			default:
				break;
			}
			return true;
		}

		private boolean checkFullSetAndClear(Pile container) {
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

				float step = getResources().getDimension(R.dimen.card_stack_margin_up);

				if (counter == FULL_NUMBER_SET) {
					for (int i = lastIndex; i > lastIndex - FULL_NUMBER_SET; i--) {
						Card card = (Card) container.getChildAt(i);
						float deltaY = -(i - (lastIndex - FULL_NUMBER_SET + 1)) * step;

						Animation trans = new TranslateAnimation(0.0f, 0.0f, 0.0f, deltaY);
						trans.setInterpolator(new AccelerateInterpolator());
						trans.setDuration(1000);
						trans.setFillAfter(true);
						trans.setAnimationListener(new MyAnimationListener(container, card));
						card.startAnimation(trans);

					}

					decksCompleted++;
					statsManager.updatePoints(StatsManager.DECK_COMPLETED);
					if (decksCompleted == NUMBER_OF_DECKS) {
						gameWon();
					}
					return true;
				}
			}
			return false;
		}

	}

	private void gameWon() {
		winner.setVisibility(View.VISIBLE);
		gameState = GameState.FINISHED;
		statsManager.timeStop();
	}

	class MyAnimationListener implements AnimationListener {

		private final Pile container;
		private final Card card;

		public MyAnimationListener(Pile container, Card card) {
			this.container = container;
			this.card = card;
		}

		@Override
		public void onAnimationEnd(Animation animation) {
			container.removeView(card);
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
		}

		@Override
		public void onAnimationStart(Animation animation) {
		}

	}

	@Override
	protected void onResume() {
		super.onStart();
		if (gameState == GameState.STARTED) {
			statsManager.onGameResume();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		statsManager.onGamePause();
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		onCreate = true;
		setupDeckMeasurements();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		setupDeckMeasurements();
	}

}