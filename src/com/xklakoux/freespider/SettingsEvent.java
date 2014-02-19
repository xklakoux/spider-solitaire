/**
 * 
 */
package com.xklakoux.freespider;

/**
 * @author artur
 *
 */
public class SettingsEvent {
	private String stringValue;
	private Boolean booleanValue;
	private String key;

	public SettingsEvent(String key, String stringValue) {
		super();
		this.stringValue = stringValue;
		this.key = key;
	}
	public SettingsEvent(String key, Boolean booleanValue) {
		super();
		this.booleanValue  = booleanValue;
		this.key = key;
	}


	public String getString() {
		return stringValue;
	}
	public void setValue(String value) {
		stringValue = value;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public Boolean getBoolean() {
		return booleanValue;
	}
	public void setValue(Boolean booleanValue) {
		this.booleanValue = booleanValue;
	}

}
