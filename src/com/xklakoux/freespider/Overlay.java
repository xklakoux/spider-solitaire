package com.xklakoux.freespider;

import android.app.Activity;
import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.FrameLayout;

/**
 * @author artur
 * 
 */
public class Overlay extends FrameLayout {

	Activity context;

	public Overlay(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = (Activity) context;
	}

	private float startX;
	private float startY;

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		Log.d("TAG", "interrceptingg");

		final int action = MotionEventCompat.getActionMasked(event);

		switch (action) {
		case MotionEvent.ACTION_DOWN:
			Log.d("TAG", "down");
			startX = event.getX();
			startY = event.getY();
			break;
		case MotionEvent.ACTION_MOVE:
			Log.d("TAG", "move");
			startX = event.getX();
			startY = event.getY();
			break;
		case MotionEvent.ACTION_UP:
			Log.d("TAG", "up");
			float endX = event.getX();
			float endY = event.getY();
			if (isAClick(startX, endX, startY, endY)) {
				//				if (context.getActionBar().isShowing()) {
				//					context.getActionBar().hide();
				//				} else {
				//					context.getActionBar().show();
				//				}
				return true;
			} else {
				return false;
			}
		}
		return false;
	}

	private boolean isAClick(float startX, float endX, float startY, float endY) {
		float differenceX = Math.abs(startX - endX);
		float differenceY = Math.abs(startY - endY);
		Log.d("TAG", "X: " + differenceX + " Y: " + differenceY);
		if (differenceX > 7 || differenceY > 7) {
			return false;
		}
		return true;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		Log.d("TAG", "onTouchEvent");

		final int action = MotionEventCompat.getActionMasked(event);

		switch (action) {
		case MotionEvent.ACTION_DOWN:
			Log.d("TAG", "onTouchEventdown");
			startX = event.getX();
			startY = event.getY();
			break;
		case MotionEvent.ACTION_MOVE:
			Log.d("TAG", "onTouchEventmove");
			startX = event.getX();
			startY = event.getY();
			break;
		case MotionEvent.ACTION_UP:
			Log.d("TAG", "onTouchEventup");
			float endX = event.getX();
			float endY = event.getY();
			if (isAClick(startX, endX, startY, endY)) {
				if (context.getActionBar().isShowing()) {
					context.getActionBar().hide();
				} else {
					context.getActionBar().show();
				}
				return true;
			} else {
				return false;
			}
		}
		return true;
	}
}
