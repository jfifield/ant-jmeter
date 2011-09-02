/*
 * Copyright 2007 Joseph Fifield
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.programmerplanet.ant.taskdefs.jmeter;

/**
 * Used as an inner tag to capture arguments for runtime JMeter properties.
 * 
 * @author <a href="mailto:xknight@users.sourceforge.net">Christopher Ottley</a>
 * @author <a href="mailto:jfifield@programmerplanet.org">Joseph Fifield</a>
 */
public final class Property {

	private String name = "";
	private String value = "";
	private boolean remote = false;

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

	public void setRemote(boolean remote) {
		this.remote = remote;
	}

	public boolean isRemote() {
		return remote;
	}

	public boolean isValid() {
		return (!name.equals("") && !value.equals(""));
	}

	public String toString() {
		return name + "=" + value;
	}

}
