/*******************************************************************************
 * Copyright (c) 2006 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.eclipsecommon.preferences;


/**
 * Representing a single preference.
 * <p>
 * This package should help to manage the new preference system
 * with scopes and nodes introduced with Eclipse 3.1.
 *
 * @param <T> the type, which this preference can store
 * 		(normally, thats the same as the type property,
 * 		but not have to be)
 */
public class Preference<T> {


	/**
	 * The types inside the Eclipse preference store
	 */
	public enum Type { 
		STRING, 
		BOOLEAN, 
		DOUBLE,
		FLOAT,
		LONG,
		INT,
	}

	
	/**
	 * Default implementation for preferences of type String
	 */
	public static class StringPref extends Preference<String> {
		
		public StringPref(String qualifier, String key) {
			super(qualifier, key, Type.STRING);
		}
	}
	
	/**
	 * Default implementation for preferences of type Boolean/boolean
	 */
	public static class BooleanPref extends Preference<Boolean> {
		
		BooleanPref(String qualifier, String key) {
			super(qualifier, key, Type.BOOLEAN);
		}
	}

	/**
	 * Default implementation for preferences of type Integer/int
	 */
	public static class IntPref extends Preference<Integer> {
		
		IntPref(String qualifier, String key) {
			super(qualifier, key, Type.INT);
		}
	}
	
	/**
	 * Default implementation for preferences of type Long/long
	 */
	public static class LongPref extends Preference<Long> {
		
		LongPref(String qualifier, String key) {
			super(qualifier, key, Type.LONG);
		}
	}
	
	/**
	 * Default implementation for preferences of type Float/float
	 */
	public static class FloatPref extends Preference<Float> {
		
		FloatPref(String qualifier, String key) {
			super(qualifier, key, Type.FLOAT);
		}
	}
	
	/**
	 * Default implementation for preferences of type Double/double
	 */
	public static class DoublePref extends Preference<Double> {
		
		DoublePref(String qualifier, String key) {
			super(qualifier, key, Type.DOUBLE);
		}
	}
	
	
/*-- Instance ---------------------------------------------------------------*/
	
	private final String fQualifier;
	private final String fKey;
	private final Type fType;
	
	
	private Preference(String qualifier, String key, Type type) {
		
		fQualifier = qualifier;
		fKey = key;
		fType = type;
	}
	

	public String getQualifier() {
		
		return fQualifier;
	}
	
	public String getKey() {
		
		return fKey;
	}
	
	public Type getType() {
		
		return fType;
	}
	
	
	@Override
	public String toString() {
		
		return fQualifier + '/' + fKey;
	}
	
}
