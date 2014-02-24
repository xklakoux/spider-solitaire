/**
 * 
 */
package com.xklakoux.freespider.enums;

/**
 * @author artur
 * 
 */
public enum Suit {

	SPADES("spades"), HEARTS("hearts"), CLUBS("clubs"), DIAMONDS("diamonds");
	final public String name;

	public String getName() {
		return name;
	}

	Suit(String name) {
		this.name = name;
	}
}
