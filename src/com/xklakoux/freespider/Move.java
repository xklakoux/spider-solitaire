package com.xklakoux.freespider;

import com.xklakoux.freespider.enums.Suit;

/**
 * @author artur
 * 
 */
public class Move {

	public static final int ACTION_MOVE_UNCOVER = 0;
	public static final int ACTION_COMPLETE = 1;
	public static final int ACTION_COMPLETE_UNCOVER = 2;
	public static final int ACTION_MOVE = 3;
	public static final int ACTION_DEAL = 4;

	private int amount;
	private int from;
	private int to;
	private Suit suit;
	private final int action;

	public Move(int amount, int from, int to, Suit suit, int action) {
		super();
		this.suit = suit;
		this.amount = amount;
		this.from = from;
		this.to = to;
		this.action = action;
	}

	public Move(int from, int action) {
		super();
		this.from = from;
		this.action = action;
	}

	public Move(int action) {
		super();
		this.action = action;
	}

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	public int getFrom() {
		return from;
	}

	public void setFrom(int from) {
		this.from = from;
	}

	public int getTo() {
		return to;
	}

	public void setTo(int to) {
		this.to = to;
	}

	public int getAction() {
		return action;
	}

	public Suit getSuit() {
		return suit;
	};
}
