/**
 * 
 */
package com.xklakoux.freespider.enums;

/**
 * @author artur
 * 
 */
public enum Suit {

	HEARTS("hearts"), DIAMONDS("diamonds"), CLUBS("clubs"), SPADES("spades");
	final public String name;

	public String getName() {
		return name;
	}

	Suit(String name) {
		this.name = name;
	}
}
