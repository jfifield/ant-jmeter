package org.programmerplanet.ant.taskdefs.jmeter;

/**
 * Used as an inner tag to capture arguments for the JVM.
 */
public final class Arg {
	
	private String value;

	public void setValue(String arg) {
		value = arg;
	}

	public String getValue() {
		return value;
	}
}
