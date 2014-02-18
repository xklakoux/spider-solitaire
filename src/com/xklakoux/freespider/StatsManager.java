/**
 * 
 */
package com.xklakoux.freespider;

import android.content.Context;
import android.os.SystemClock;
import android.widget.Chronometer;
import android.widget.TextView;

/**
 * @author artur
 *
 */
public class StatsManager {

	public static final int DECK_COMPLETED = 0;
	public static final int MOVE = 1;
	public static final int CLEAR = 2;
	public static final int DECK_UNDID = 3;
	long timeWhenStopped = 0;

	private int score = 500;
	private int moves = 0;
	private final TextView tvScore;
	private final TextView tvMoves;
	private final Chronometer cTime;
	private final Context context;

	public StatsManager(Context context, TextView tvScore, TextView tvMoves, Chronometer cTime) {
		this.context = context;
		this.tvScore = tvScore;
		this.tvMoves = tvMoves;
		this.cTime = cTime;

		tvMoves.setText(""+moves);
		tvScore.setText(""+score);
	}

	public void updatePoints(int action) {
		switch(action) {
		case DECK_COMPLETED:
			score+=100;
			break;
		case DECK_UNDID:
			score-=100;
			break;
		case MOVE:
			score--;
			break;
		case CLEAR:
			score=500;
			break;
		}
		tvScore.setText(""+score);
	}


	public void updateMoves(int action) {
		switch(action) {
		case MOVE:
			moves++;
			updatePoints(MOVE);
			break;
		case CLEAR:
			moves=0;
			break;
		}
		tvMoves.setText(""+moves);
	}

	public void clearStatsAndGo() {
		updateMoves(CLEAR);
		updatePoints(CLEAR);
		cTime.setBase(SystemClock.elapsedRealtime());
		timeWhenStopped = 0;
		cTime.start();
	}


	public void onGameResume() {
		cTime.setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
		cTime.start();
	}

	public void onGamePause() {
		timeWhenStopped = cTime.getBase() - SystemClock.elapsedRealtime();
		cTime.stop();
	}
	public void timeStop() {
		cTime.stop();
	}
	public void timeStart() {
		cTime.start();
	}
}
