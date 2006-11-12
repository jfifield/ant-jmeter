package org.programmerplanet.ant.taskdefs.jmeter;

/**
 * Used as an inner tag to capture arguments for runtime JMeter properties.
 *
 * @author <a href="mailto:xknight@users.sourceforge.net">Christopher Ottley</a>
 */
public final class Property {
	
	private String name = "";
	private String value = "";

	public void setName(String arg) {
		name = arg.trim();
	}

	public String getName() {
		return name;
	}

	public void setValue(String arg) {
		value = arg.trim();
	}

	public String getValue() {
		return value;
	}

	public boolean isValid() {
		return (!name.equals("") && !value.equals(""));
	}

	public String toString() {
		return name + "=" + value;
	}
}
