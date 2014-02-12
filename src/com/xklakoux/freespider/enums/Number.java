/**
 * 
 */
package com.xklakoux.freespider.enums;

/**
 * @author artur
 *
 */
public enum Number {
	KING(13), QUEEN(12), JACK(11), TEN(10), NINE(9), EIGHT(8), SEVEN(7), SIX(6), FIVE(5), FOUR(4), THREE(3), TWO(2), ACE(1);

	public final int id;

	private Number(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

}
