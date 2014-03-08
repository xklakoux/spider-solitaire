/**
 * 
 */
package com.xklakoux.freespider;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Stack;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.AudioManager;
import android.media.SoundPool;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.squareup.otto.Bus;
import com.xklakoux.solitariolib.StatsManager;
import com.xklakoux.solitariolib.enums.GameState;
import com.xklakoux.solitariolib.views.Card;
import com.xklakoux.solitariolib.views.CardSerializer;

/**
 * @author artur
 *
 */
public class Game extends Application {

	public final static int SOUND_PUT_CARD = 0;

	private static StatsManager statsManager;
	static private SharedPreferences settings;
	static private Context context;
	static private Bus bus;
	static private int uniqueId;
	private static Stack<Move> moves = new Stack<Move>();
	private static GameState gameState = GameState.NOT_STARTED;
	//	private static Deck deck;

	public static String GAME_STILL_NOT_WON = "still not won";
	public static String GAME_WON = "won";

	private static int setsCompleted = 0;
	private static int cardsDealt = 0;

	final public static int NUMBER_OF_SETS = 8;
	final public static int FULL_NUMBER_SET = 13;
	final public static int START_CARDS_DEAL_COUNT = 54;

	private final static SoundPool sp = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
	private static int drawSoundId;

	@Override
	public void onCreate() {
		super.onCreate();
		context = getApplicationContext();
		settings = PreferenceManager.getDefaultSharedPreferences(context);
		bus = new Bus();
		drawSoundId = sp.load(Game.getAppContext(), R.raw.draw, 1);

		Gson gson = new Gson();
		Type collectionType = new TypeToken<Stack<Move>>() {
		}.getType();

		String previousMoves = Game.getSettings().getString("moves", null);
		if (previousMoves != null) {
			gameState = GameState.STARTED;
			moves = gson.fromJson(previousMoves, collectionType);
		}
	}

	public static SharedPreferences getSettings() {
		return settings;
	}

	public static Context getAppContext() {
		return context;
	}

	public static Bus getBus() {
		return bus;
	}
	public static int getUniqueId() {
		return uniqueId++;
	}

	public static Stack<Move> getMoves() {
		return moves;
	}

	public static GameState getState() {
		return gameState;
	}

	public static void setState(GameState state) {
		gameState = state;
	}

	public static void addCardDealt() {
		cardsDealt++;
	}

	public static void setCompleted() {
		setsCompleted++;
		if (setsCompleted == NUMBER_OF_SETS) {
			Game.getBus().post(Game.GAME_WON);
		}
	}

	public static boolean isFullDeal() {
		if (cardsDealt >= START_CARDS_DEAL_COUNT && cardsDealt % 10 == 4) {
			return true;
		}
		return false;
	}

	public static boolean isStartDeal() {
		if (cardsDealt == START_CARDS_DEAL_COUNT) {
			return true;
		}
		return false;
	}

	public static void setUndid() {
		setsCompleted--;
		Game.getBus().post(Game.GAME_STILL_NOT_WON);
	}

	/**
	 * 
	 */
	public static void init(List<Card> cards) {
		addToSharedPreferences(cards);
		setsCompleted = 0;
		cardsDealt = 0;
		Game.getBus().post(Game.GAME_STILL_NOT_WON);
	}

	private static void addToSharedPreferences(List<Card> cards) {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(Card.class, new CardSerializer());
		Gson gson = gsonBuilder.create();
		String json = gson.toJson(cards);
		Editor editor = Game.getSettings().edit();
		editor.putString("draw", json);
		editor.commit();
	}

	public static StatsManager getStatsManager() {
		return statsManager;
	}

	public static void setStatsManager(StatsManager manager) {
		statsManager = manager;
	}

	public static void playSound(int sound) {
		switch(sound) {
		case SOUND_PUT_CARD:
			sp.play(drawSoundId, 1, 1, 0, 0, 1);
			break;

		}
	}

	public static Move getLastMove() {
		return moves.get(moves.size() - 1);
	}
}
