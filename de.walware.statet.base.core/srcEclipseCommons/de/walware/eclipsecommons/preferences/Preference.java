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

package de.walware.eclipsecommons.preferences;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.regex.Pattern;


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
public abstract class Preference<T> {

	private static final char LIST_SEPARATOR_CHAR = ',';
	private static final Pattern LIST_SEPARATOR_PATTERN = Pattern.compile(","); 
	
	
	/**
	 * The types inside the Eclipse preference store
	 */
	public enum Type { 
		STRING, 
		BOOLEAN, 
		DOUBLE,
		FLOAT,
		LONG,
		INT;
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
		
		public BooleanPref(String qualifier, String key) {
			super(qualifier, key, Type.BOOLEAN);
		}
	}

	/**
	 * Default implementation for preferences of type Integer/int
	 */
	public static class IntPref extends Preference<Integer> {
		
		public IntPref(String qualifier, String key) {
			super(qualifier, key, Type.INT);
		}
	}
	
	/**
	 * Default implementation for preferences of type Long/long
	 */
	public static class LongPref extends Preference<Long> {
		
		public LongPref(String qualifier, String key) {
			super(qualifier, key, Type.LONG);
		}
	}
	
	/**
	 * Default implementation for preferences of type Float/float
	 */
	public static class FloatPref extends Preference<Float> {
		
		public FloatPref(String qualifier, String key) {
			super(qualifier, key, Type.FLOAT);
		}
	}
	
	/**
	 * Default implementation for preferences of type Double/double
	 */
	public static class DoublePref extends Preference<Double> {
		
		public DoublePref(String qualifier, String key) {
			super(qualifier, key, Type.DOUBLE);
		}
	}
	
	public static class EnumSetPref<T extends Enum<T>> extends Preference<EnumSet<T>> {
		
		private Class<T> fEnumType;
		
		public EnumSetPref(String qualifier, String key, Class<T> enumType) {
			super(qualifier, key, Type.STRING);
			fEnumType = enumType;
		}
		
		public EnumSet<T> store2Usage(Object storedValue) {
			
			String[] values = LIST_SEPARATOR_PATTERN.split((String) storedValue);
			EnumSet<T> set = EnumSet.noneOf(fEnumType);
			for (String s : values) {
				if (s.length() > 0) {
					set.add(Enum.valueOf(fEnumType, s));
				}
			}
			return set;
		}
		
		public String usage2Store(EnumSet<T> set) {
			
			if (set.isEmpty()) {
				return "";
			}
			
			StringBuilder sb = new StringBuilder();
			for (T e : set) {
				sb.append(e.name());
				sb.append(LIST_SEPARATOR_CHAR);
			}
			return sb.substring(0, sb.length()-1);
		}
	}
	
	public static class EnumListPref<E extends Enum<E>> extends Preference<List<E>> {
		
		private Class<E> fEnumType;
		
		public EnumListPref(String qualifier, String key, Class<E> enumType) {
			super(qualifier, key, Type.STRING);
			fEnumType = enumType;
		}
		
		public ArrayList<E> store2Usage(Object storedValue) {
			
			String[] values = LIST_SEPARATOR_PATTERN.split((String) storedValue);
			ArrayList<E> list = new ArrayList<E>(values.length);
			for (String s : values) {
				if (s.length() > 0) {
					list.add(Enum.valueOf(fEnumType, s));
				}
			}
			return list;
		}
		
		public String usage2Store(List<E> list) {
			
			if (list.isEmpty()) {
				return "";
			}
			
			StringBuilder sb = new StringBuilder();
			for (E e : list) {
				sb.append(e.name());
				sb.append(LIST_SEPARATOR_CHAR);
			}
			return sb.substring(0, sb.length()-1);
		}
	}

	public static class StringArrayPref extends Preference<String[]> {
		
		public StringArrayPref(String qualifier, String key) {
			super(qualifier, key, Type.STRING);
		}
		
		public String[] store2Usage(Object storedValue) {
			
			return LIST_SEPARATOR_PATTERN.split((String) storedValue);
		}
		
		public String usage2Store(String[] array) {
			
			if (array.length == 0) {
				return "";
			}
			
			StringBuilder sb = new StringBuilder();
			for (String s : array) {
				sb.append(s);
				sb.append(',');
			}
			return sb.substring(0, sb.length()-1);
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
	
	public Type getStoreType() {
		
		return fType;
	}
	
	public Object usage2Store(T obj) {
		
		return obj;
	}
	
	@SuppressWarnings("unchecked")
	public T store2Usage(Object obj) {
		
		return (T) obj;
	}
	
	@Override
	public String toString() {
		
		return fQualifier + '/' + fKey;
	}
	
}
