/**
 * 
 */
package com.xklakoux.freespider.enums;

/**
 * @author artur
 * 
 */
public enum Difficulty {
	EASY("Easy"), MEDIUM("Medium"), HARD("Hard");
	private String name;

	private Difficulty(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}



}
