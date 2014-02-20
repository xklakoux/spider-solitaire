package com.xklakoux.freespider;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Stack;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnDragListener;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ArrayAdapter;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;
import com.xklakoux.freespider.enums.Difficulty;
import com.xklakoux.freespider.enums.GameState;
import com.xklakoux.freespider.enums.Number;
import com.xklakoux.freespider.enums.Suit;

public class Game extends Activity implements OnSharedPreferenceChangeListener {

	private StatsManager statsManager;

	private GameState gameState = GameState.NOT_STARTED;

	private final static Stack<Move> moves = new Stack<Move>();

	public final String TAG = Game.class.getSimpleName();

	private RelativeLayout root;

	static List<Pile> pileLayouts;

	public static String GAME_WON = "won";
	public static String GAME_STILL_NOT_WON = "still not won";

	private Deck deck;
	private TextView winner;
	private ScrollView scrollPiles;
	private LinearLayout piles;
	private RelativeLayout hollowPile;
	private RelativeLayout statsLayout;

	private Difficulty chosenDifficulty;

	private String chosenOrientation;
	private float chosenAnimationSpeed;
	private boolean chosenSound;
	private boolean chosenUnrestrictedDeal;
	private boolean chosenUnrestrictedUndo;
	private boolean chosenHints;

	private SharedPreferences prefs;

	boolean onCreate = false;

	int cardWidth;
	int cardHeight;

	private final SoundPool sp = new SoundPool(54, AudioManager.STREAM_MUSIC, 0);
	int drawSoundId = sp.load(App.getAppContext(), R.raw.draw, 1);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.game_layout);
		initSettings();
		findViewsSetListenersAndManagers();
		fixLayoutOrientation(getResources().getConfiguration().orientation);
		getCardDimensions();
		App.getBus().register(this);

	}

	@SuppressLint("DefaultLocale")
	private void initSettings() {
		prefs = PreferenceManager.getDefaultSharedPreferences(App.getAppContext());
		prefs.registerOnSharedPreferenceChangeListener(this);

		chosenDifficulty = Difficulty.valueOf(prefs.getString(Constant.SETT_DIFFICULTY, "EASY").toUpperCase());

		chosenOrientation = prefs.getString(Constant.SETT_ORIENTATION, "auto");
		if (chosenOrientation.equals("horizontal")) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
		} else if (chosenOrientation.equals("vertical")) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
		} else {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
		}

		chosenSound = prefs.getBoolean(Constant.SETT_SOUNDS, true);
		chosenUnrestrictedDeal = prefs.getBoolean(Constant.SETT_UNRES_DEAL, false);
		chosenUnrestrictedUndo = prefs.getBoolean(Constant.SETT_UNRES_UNDO, false);

		chosenHints = prefs.getBoolean(Constant.SETT_HINTS, true);

		String speed = prefs.getString(Constant.SETT_ANIMATION, "slow");
		if (speed.equals("slow")) {
			chosenAnimationSpeed = 1.0f;
		} else if (speed.equals("fast")) {
			chosenAnimationSpeed = 0.5f;
		} else if (speed.equals("superfast")) {
			chosenAnimationSpeed = 0.0f;
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putAll(outState);
	}

	private void findViewsSetListenersAndManagers() {
		winner = (TextView) findViewById(R.id.winner);

		scrollPiles = (ScrollView) findViewById(R.id.scrollPiles);
		piles = (LinearLayout) findViewById(R.id.piles);
		hollowPile = (Pile) findViewById(R.id.hollowPile);
		statsLayout = (RelativeLayout) findViewById(R.id.stats);

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

		for (Pile pile : pileLayouts) {
			pile.setOnDragListener(new PileOnDragListener());
		}

		deck = (Deck) findViewById(R.id.deck);
		deck.setOnClickListener(new OnDeckClickListener());
		deck.setSize(cardWidth, cardHeight);

		root = (RelativeLayout) findViewById(R.id.root);

		TextView score = (TextView) findViewById(R.id.tvScore);
		TextView movesCount = (TextView) findViewById(R.id.tvMoves);
		Chronometer time = (Chronometer) findViewById(R.id.tvTimeElapsed);
		statsManager = new StatsManager(Game.this, score, movesCount, time);

		refreshResources();
	}

	@SuppressWarnings("deprecation")
	public void refreshResources() {

		for (Pile pile : pileLayouts) {
			pile.refresh();
		}
		deck.refresh();
		String backgroundResName = App.getSettings().getString(Constant.SETT_BACKGROUND, Constant.DEFAULT_BACKGROUND);
		root.setBackgroundDrawable((getResources().getDrawable((Utils.getResId("background_" + backgroundResName,
				R.drawable.class)))));

	}

	class OnDeckClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {

			if (deck.isEmpty()) {
				return;
			}
			if (!chosenUnrestrictedDeal && !cardsCorrect()) {
				return;
			}
			if (gameState != GameState.STARTED) {
				return;
			}
			int i = 0;
			gameState = GameState.DEALING;
			for (Pile pile : pileLayouts) {
				Card card = deck.getCard();
				dealCard(pile, card, true, i++, false);
			}

			moves.add(new Move(Move.ACTION_DEAL));
		}

		/**
		 * @return
		 */
		private boolean cardsCorrect() {
			for (Pile pile : pileLayouts) {
				if (pile.getCardsCount() < 1) {
					Toast.makeText(Game.this, R.string.all_tableaus_should_be_filled, Toast.LENGTH_SHORT).show();
					return false;
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

	private void dealCard(Pile pile, Card card, boolean faceup, int hundredMillisecondOffset, boolean isNewGameDeal) {

		int[] deckLocation = new int[2];
		deck.getLastVisible().getLocationOnScreen(deckLocation);
		int statusBarOffsetY = getStatusBarOffset();

		int[] location = new int[2];
		pile.getLastTrueChild().getLocationOnScreen(location);
		final Card fakeCard = new Card(Game.this, Suit.SPADES, Number.ACE);
		root.addView(fakeCard);
		setCardSize(fakeCard);
		fakeCard.setOnTouchListener(null);

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

		anim.setInterpolator(new DecelerateInterpolator());
		anim.setDuration((long) (700.0 * chosenAnimationSpeed) + 100);
		int startOffset = hundredMillisecondOffset * ((int) (100.0 * chosenAnimationSpeed));
		anim.setStartOffset(startOffset);
		anim.setAnimationListener(new DealAnimationListener(pile, card, root, fakeCard, isNewGameDeal));
		anim.setZAdjustment(Animation.ZORDER_TOP);
		fakeCard.setAnimation(anim);

		card.setFaceup(faceup);
	}

	class DealAnimationListener implements AnimationListener {

		private final Pile container;
		private final Card card;
		private final Card fakeAnimCard;
		private final RelativeLayout root;
		private final boolean isNewGameDeal;

		public DealAnimationListener(Pile container, Card card, RelativeLayout root, Card fakeAnimCard,
				boolean isNewGameDeal) {
			this.container = container;
			this.card = card;
			this.fakeAnimCard = fakeAnimCard;
			this.root = root;
			this.isNewGameDeal = isNewGameDeal;
		}

		@Override
		public void onAnimationEnd(Animation animation) {
			container.addCard(card);
			if (checkFullSetAndClear(container)) {
				Move lastMove = moves.get(moves.size() - 1);
				lastMove.setCompleted(true);
				lastMove.setSuit(card.getSuit());
				lastMove.setTo(pileLayouts.indexOf(container));
			}
			root.removeView(fakeAnimCard);

			deck.addCardDealt();
			// cardsDealt++;
			if (deck.isFullDeal()) {
				gameState = GameState.STARTED;
			}
			if (deck.isStartDeal()) {
				statsManager.clearStatsAndGo();
			}
			if (chosenSound) {
				sp.play(drawSoundId, 1, 1, 0, 0, 1);
			}
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
		}

		@Override
		public void onAnimationStart(Animation animation) {
			fakeAnimCard.setVisibility(View.VISIBLE);
			fakeAnimCard.bringToFront();
			root.requestLayout();
			root.invalidate();
		}
	}

	private void dealNewGame() {
		gameState = GameState.DEALING;
		for (int k = 0; k < Deck.START_CARDS_DEAL_COUNT; k++) {
			Pile pile = pileLayouts.get(k % 10);

			Card card = deck.getCard();

			boolean faceUp = Deck.START_CARDS_DEAL_COUNT - k <= pileLayouts.size();
			dealCard(pile, card, faceUp, k, true);

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
			undo(true);
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
		case R.id.action_restart:
			if (gameState == GameState.STARTED) {
				restartGameDialog();
			} else if (gameState == GameState.DEALING) {
				// do nothing
			}
			return true;
		case R.id.action_settings:
			Intent settingsActivity = new Intent(getBaseContext(), SettingsDialog.class);
			startActivity(settingsActivity);

			return true;
		case R.id.action_rules:
			showRulesDialog();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void showNewGameDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(Game.this);
		builder.setMessage(R.string.current_progress_will_be_lost_message);
		builder.setTitle(R.string.start_new_game_alert_dialog_title);
		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				setupNewGame();
			}
		});
		builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	private void restartGameDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(Game.this);
		builder.setMessage(R.string.current_progress_will_be_lost_message);
		builder.setTitle(R.string.restart_game_alert_dialog_title);
		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				restartGame();
			}
		});
		builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	private void showRulesDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(Game.this);
		builder.setMessage(R.string.rules_of_spider);
		builder.setTitle(R.string.spider_solitaire_rules);
		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		AlertDialog dialog = builder.create();
		dialog.setCanceledOnTouchOutside(true);
		dialog.show();
	}

	private void restartGame() {
		for (int i = 0; i < moves.size();) {
			undo(false);
		}
		moves.clear();
		statsManager.clearStatsAndGo();
	}

	private void setupNewGame() {
		gameState = GameState.DEALING;
		moves.clear();

		for (Pile pileLayout : pileLayouts) {
			pileLayout.removeViews(1, pileLayout.getCardsCount());
		}
		deck.initialize(chosenDifficulty, Game.this);
		dealNewGame();
		statsManager.setTimeZero();
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

	private void undo(boolean human) {
		if (moves.isEmpty()) {
			return;
		}

		winner.setVisibility(View.GONE);
		int indexOfFirstDragged;

		Move move = moves.pop();

		if (move.getAction() == Move.ACTION_DEAL && human && chosenUnrestrictedUndo) {
			return;
		}

		Pile landingContainer = pileLayouts.get(move.getFrom());
		Pile draggedParent = pileLayouts.get(move.getTo());

		if (move.isCompletedUncovered()) {
			draggedParent.getLastCard().setFaceup(false);
		}

		if (move.isCompleted()) {
			deck.setUndid();
			statsManager.updatePoints(StatsManager.SET_UNDID);

			for (Number num : Number.values()) {
				Card card = new Card(Game.this, move.getSuit(), num);
				card.setFaceup(true);
				draggedParent.addCard(card);
			}
		}

		if (move.isUncover()) {
			indexOfFirstDragged = landingContainer.getCardsCount() - move.getAmount();

			landingContainer.getLastCard().setFaceup(false);
		}

		switch (move.getAction()) {

		case Move.ACTION_MOVE:
			indexOfFirstDragged = draggedParent.getCardsCount() - move.getAmount();
			for (int i = indexOfFirstDragged; i < draggedParent.getCardsCount();) {
				draggedParent.moveCard(landingContainer, draggedParent.getCardAt(i));
			}
			break;

		case Move.ACTION_DEAL:

			for (int i = pileLayouts.size() - 1; i >= 0; i--) {
				Card card = pileLayouts.get(i).getLastCard();
				card.setFaceup(false);
				deck.addCard(card);
				pileLayouts.get(i).removeLastCard();
				checkFullSetAndClear(pileLayouts.get(i));
			}
			break;
		}

		statsManager.updateMoves(StatsManager.MOVE);

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
			int indexOfDragged = draggedParent.indexOfCard(draggedCard);

			Pile landingContainer = (Pile) v;
			ImageView glowing = landingContainer.getLastTrueChild();

			switch (event.getAction()) {
			case DragEvent.ACTION_DRAG_STARTED:
				break;
			case DragEvent.ACTION_DRAG_ENTERED:
				if (landingContainer != draggedParent) {
					glowing.setColorFilter(getResources().getColor(R.color.highlight));
				}
				break;
			case DragEvent.ACTION_DRAG_EXITED:
				glowing.setColorFilter(Color.TRANSPARENT);
				// }
				break;
			case DragEvent.ACTION_DROP:
				glowing.setColorFilter(Color.TRANSPARENT);
				if (landingContainer == draggedParent) {
					break;
				}
				if (!landingContainer.isEmpty()) {
					Card lastCard = landingContainer.getLastCard();

					if (lastCard.getNumber().getId() != draggedCard.getNumber().getId() + 1 || !lastCard.isFaceup()) {
						break;
					}
				}
				int movedCards = 0;
				for (int i = indexOfDragged; i < draggedParent.getCardsCount();) {
					draggedParent.getCardAt(i).setVisibility(View.VISIBLE);
					draggedParent.moveCard(landingContainer, draggedParent.getCardAt(i));
					movedCards++;
				}
				checkFullSetAndClear(draggedParent);

				int indexOfDraggedParent = pileLayouts.indexOf(draggedParent);
				int indexOfLandingParent = pileLayouts.indexOf(landingContainer);

				moves.add(new Move(movedCards, indexOfDraggedParent, indexOfLandingParent, draggedCard.getSuit(),
						Move.ACTION_MOVE));
				if (checkFullSetAndClear(landingContainer)) {
					moves.get(moves.size() - 1).setCompleted(true);
				}

				Move lastMove = moves.get(moves.size() - 1);
				lastMove.setUncover(draggedParent.uncoverLastCard());

				statsManager.updateMoves(StatsManager.MOVE);

				if (chosenSound) {
					sp.play(drawSoundId, 1, 1, 0, 0, 1);
				}
				return true;
			case DragEvent.ACTION_DRAG_ENDED:
				// if (!landingContainer.isEmpty()) {
				glowing.setColorFilter(Color.TRANSPARENT);
				// }
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

	}

	private boolean checkFullSetAndClear(Pile container) {
		container.refresh();
		int lastIndex = container.getCardsCount() - 1;
		int counter = 1;
		Card referenceCard = container.getLastCard();
		for (int i = lastIndex - 1; i >= 0; i--) {
			Card card = container.getCardAt(i);
			if (!(referenceCard.getSuit() == card.getSuit())
					|| !(referenceCard.getNumber().getId() == (card.getNumber().getId() - 1)) || !card.isFaceup()) {

				if (chosenHints) {
					for (int j = i; j >= 0; j--) {
						Card c = container.getCardAt(j);
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

		if (counter == Deck.FULL_NUMBER_SET) {
			for (int i = lastIndex; i > lastIndex - Deck.FULL_NUMBER_SET; i--) {
				Card card = container.getCardAt(i);
				float deltaY = -(i - (lastIndex - Deck.FULL_NUMBER_SET + 1)) * step;

				Animation trans = new TranslateAnimation(0.0f, 0.0f, 0.0f, deltaY);
				trans.setInterpolator(new DecelerateInterpolator(2.0f));
				trans.setDuration(500);
				trans.setFillAfter(true);
				trans.setAnimationListener(new FullSetClearAnimationListener(container, card));
				card.startAnimation(trans);

			}

			deck.setCompleted();
			statsManager.updatePoints(StatsManager.SET_COMPLETED);
			return true;
		}
		return false;
	}

	private void gameWon() {
		winner.setVisibility(View.VISIBLE);
		gameState = GameState.FINISHED;
		statsManager.timeStop();
	}

	class FullSetClearAnimationListener implements AnimationListener {

		private final Pile container;
		private final Card card;

		public FullSetClearAnimationListener(Pile container, Card card) {
			this.container = container;
			this.card = card;
		}

		@Override
		public void onAnimationEnd(Animation animation) {
			container.removeView(card);
			Move lastmove = moves.get(moves.size() - 1);
			lastmove.setCompletedUncovered(container.uncoverLastCard());
			checkFullSetAndClear(container);
			if (chosenSound) {
				sp.play(drawSoundId, 1, 1, 0, 0, 1);
			}
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
		getCardDimensions();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		getCardDimensions();
		fixLayoutOrientation(newConfig.orientation);
	}

	private void fixLayoutOrientation(int orientation) {

		if (orientation == Configuration.ORIENTATION_LANDSCAPE) {

			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
			params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
			params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			deck.setLayoutParams(params);

			params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.BELOW, 0);
			scrollPiles.setLayoutParams(params);

			hollowPile.setVisibility(View.INVISIBLE);
			piles.setWeightSum(11f);

			params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.LEFT_OF, deck.getId());
			params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			statsLayout.setLayoutParams(params);

		} else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0);
			params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
			deck.setLayoutParams(params);

			params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.BELOW, deck.getId());
			scrollPiles.setLayoutParams(params);

			hollowPile.setVisibility(View.GONE);
			piles.setWeightSum(10f);

			params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			params.addRule(RelativeLayout.LEFT_OF, 0);
			statsLayout.setLayoutParams(params);

		}
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

		initSettings();

		if (key.equals(Constant.SETT_DIFFICULTY)) {
			setupNewGame();

		} else if (key.equals(Constant.SETT_BACKGROUND)) {
			String backgroundResName = App.getSettings().getString(Constant.SETT_BACKGROUND,
					Constant.DEFAULT_BACKGROUND);
			root.setBackgroundDrawable((getResources().getDrawable((Utils.getResId("background_" + backgroundResName,
					R.drawable.class)))));

		} else if (key.equals(Constant.SETT_HINTS) || key.equals(Constant.SETT_CARD_SET)
				|| key.equals(Constant.SETT_REVERSE)) {

			for (Pile pile : pileLayouts) {
				pile.refresh();
				checkFullSetAndClear(pile);
			}
		}
	}

	private void getCardDimensions() {
		ViewTreeObserver vto = pileLayouts.get(0).getFirstCardSpot().getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@SuppressWarnings("deprecation")
			@Override
			public void onGlobalLayout() {
				// pileLayouts.get(0).getViewTreeObserver().removeGlobalOnLayoutListener(this);
				cardWidth = pileLayouts.get(0).getFirstCardSpot().getMeasuredWidth();
				cardHeight = pileLayouts.get(0).getFirstCardSpot().getMeasuredHeight();
				deck.setSize(cardWidth, cardHeight);
				if (onCreate && gameState == GameState.NOT_STARTED) {
					setupNewGame();
				}
			}
		});
	}

	private void setCardSize(Card card) {
		card.getLayoutParams().width = cardWidth;
		card.getLayoutParams().height = cardHeight;
	}

	@Subscribe
	public void answerAvailable(String string) {
		// TODO: React to the event somehow!
		if (string.equals(Game.GAME_WON)) {
			gameWon();
		} else if (string.equals(GAME_STILL_NOT_WON)) {
			winner.setVisibility(View.GONE);
		}
	}

}