/**
 * 
 */
package com.xklakoux.freespider;

import android.os.SystemClock;
import android.util.Log;
import android.widget.Chronometer;
import android.widget.TextView;

/**
 * @author artur
 *
 */
public class StatsManager {

	public static final int SET_COMPLETED = 0;
	public static final int MOVE = 1;
	public static final int CLEAR = 2;
	public static final int SET_UNDID = 3;
	long timeWhenStopped = 0;

	private int score = 500;
	private int moves = 0;
	private final TextView tvScore;
	private final TextView tvMoves;
	private final Chronometer cTime;

	public StatsManager( TextView tvScore, TextView tvMoves, Chronometer cTime) {
		this.tvScore = tvScore;
		this.tvMoves = tvMoves;
		this.cTime = cTime;

		tvMoves.setText(""+moves);
		tvScore.setText(""+score);
	}

	public void updatePoints(int action) {
		switch(action) {
		case SET_COMPLETED:
			score+=100;
			break;
		case SET_UNDID:
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

	public void onGameResume(long time) {
		cTime.setBase(SystemClock.elapsedRealtime() + time);
		cTime.start();
	}

	public void onGamePause() {
		timeWhenStopped = cTime.getBase() - SystemClock.elapsedRealtime();
		Log.d("debug","timeWhenStopeed " + timeWhenStopped);
		cTime.stop();
	}
	public void timeStop() {
		cTime.stop();
	}
	public void timeStart() {
		cTime.start();
	}

	public void setTimeZero() {
		cTime.setBase(SystemClock.elapsedRealtime());
		timeWhenStopped = 0;
		cTime.stop();
		updateMoves(CLEAR);
		updatePoints(CLEAR);
	}

	public long getTimeWhenStopped() {
		return cTime.getBase() - SystemClock.elapsedRealtime();
	}
}
