/*******************************************************************************
 * Copyright (c) 2006-2007 StatET-Project (www.walware.de/goto/statet).
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

	/**
	 * The types inside the Eclipse preference store
	 */
	public static enum Type { 
		STRING, 
		BOOLEAN, 
		DOUBLE,
		FLOAT,
		LONG,
		INT;
	}

	
/*-- Definition --------------------------------------------------------------*/
	
	private final String fQualifier;
	private final String fKey;
	private final Type fType;
	
	
	protected Preference(String qualifier, String key, Type type) {
		
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
	
	public abstract boolean isUsageType(Object obj);
	
	/**
	 * Converts object of type T (this Preference is designed for) in the value for the PreferenceStore (type specified by getStoreType)
	 *
	 * @param obj
	 * @return
	 */
	public Object usage2Store(T obj) {
		
		return obj;
	}
	
	/**
	 * Converts the value from the PreferenceStore into a object of type T (this Preference is designed for).
	 * 
	 * Impl. note: should always handle storage-objects of type StoreType and String
	 * 
	 * @param obj
	 * @return
	 */
	public abstract T store2Usage(Object obj);
	
	
	@Override
	public String toString() {
		
		return fQualifier + '/' + fKey;
	}

	
/*-- Implementation for common types -----------------------------------------*/	
	
	private static final char LIST_SEPARATOR_CHAR = ',';
	private static final Pattern LIST_SEPARATOR_PATTERN = Pattern.compile(","); 

	
	/**
	 * Default implementation for preferences of type String
	 */
	public static class StringPref extends Preference<String> {
		
		public StringPref(String qualifier, String key) {
			super(qualifier, key, Type.STRING);
		}
		
		@Override
		public boolean isUsageType(Object obj) {
			
			return (obj instanceof String);
		}
		@Override
		public String store2Usage(Object obj) {
			
			return (String) obj;
		}
	}
	
	/**
	 * Default implementation for preferences of type Boolean/boolean
	 */
	public static class BooleanPref extends Preference<Boolean> {
		
		public BooleanPref(String qualifier, String key) {
			super(qualifier, key, Type.BOOLEAN);
		}
		
		@Override
		public boolean isUsageType(Object obj) {
			
			return (obj instanceof Boolean);
		}
		@Override
		public Boolean store2Usage(Object obj) {
			
			if (obj != null) {
				if (obj instanceof Boolean) {
					return (Boolean) obj;
				}
				if (obj instanceof String) {
					return Boolean.valueOf((String) obj);
				}
			}
			return null;
		}
	}

	/**
	 * Default implementation for preferences of type Integer/int
	 */
	public static class IntPref extends Preference<Integer> {
		
		public IntPref(String qualifier, String key) {
			super(qualifier, key, Type.INT);
		}

		@Override
		public boolean isUsageType(Object obj) {
			
			return (obj instanceof Integer);
		}
		@Override
		public Integer store2Usage(Object obj) {
			
			if (obj != null) {
				if (obj instanceof Integer) {
					return (Integer) obj;
				}
				if (obj instanceof String) {
					return Integer.valueOf((String) obj);
				}
			}
			return null;
		}
	}
	
	/**
	 * Default implementation for preferences of type Long/long
	 */
	public static class LongPref extends Preference<Long> {
		
		public LongPref(String qualifier, String key) {
			super(qualifier, key, Type.LONG);
		}

		@Override
		public boolean isUsageType(Object obj) {
			
			return (obj instanceof Long);
		}
		@Override
		public Long store2Usage(Object obj) {
			
			if (obj != null) {
				if (obj instanceof Boolean) {
					return (Long) obj;
				}
				if (obj instanceof String) {
					return Long.valueOf((String) obj);
				}
			}
			return null;
		}
	}
	
	/**
	 * Default implementation for preferences of type Float/float
	 */
	public static class FloatPref extends Preference<Float> {
		
		public FloatPref(String qualifier, String key) {
			super(qualifier, key, Type.FLOAT);
		}
		
		@Override
		public boolean isUsageType(Object obj) {
			
			return (obj instanceof Float);
		}
		@Override
		public Float store2Usage(Object obj) {
			
			if (obj != null) {
				if (obj instanceof Float) {
					return (Float) obj;
				}
				if (obj instanceof String) {
					return Float.valueOf((String) obj);
				}
			}
			return null;
		}
	}
	
	/**
	 * Default implementation for preferences of type Double/double
	 */
	public static class DoublePref extends Preference<Double> {
		
		public DoublePref(String qualifier, String key) {
			super(qualifier, key, Type.DOUBLE);
		}
		
		@Override
		public boolean isUsageType(Object obj) {
			
			return (obj instanceof Double);
		}
		@Override
		public Double store2Usage(Object obj) {
			
			if (obj != null) {
				if (obj instanceof Double) {
					return (Double) obj;
				}
				if (obj instanceof String) {
					return Double.valueOf((String) obj);
				}
			}
			return null;
		}
	}
	
	/**
	 * Default implementation for preferences of type EnumSet.
	 */
	public static class EnumSetPref<E extends Enum<E>> extends Preference<EnumSet<E>> {
		
		private Class<E> fEnumType;
		
		public EnumSetPref(String qualifier, String key, Class<E> enumType) {
			super(qualifier, key, Type.STRING);
			fEnumType = enumType;
		}
		
		@Override
		public boolean isUsageType(Object obj) {
			
			return (obj instanceof EnumSet);
		}
		@Override
		public EnumSet<E> store2Usage(Object storedValue) {
			
			String[] values = LIST_SEPARATOR_PATTERN.split((String) storedValue);
			EnumSet<E> set = EnumSet.noneOf(fEnumType);
			for (String s : values) {
				if (s.length() > 0) {
					set.add(Enum.valueOf(fEnumType, s));
				}
			}
			return set;
		}
		
		public String usage2Store(EnumSet<E> set) {
			
			if (set.isEmpty()) {
				return "";
			}
			
			StringBuilder sb = new StringBuilder();
			for (E e : set) {
				sb.append(e.name());
				sb.append(LIST_SEPARATOR_CHAR);
			}
			return sb.substring(0, sb.length()-1);
		}
	}
	
	/**
	 * Default implementation for preferences of type List&lt;Enum&gt;
	 */
	public static class EnumListPref<E extends Enum<E>> extends Preference<List<E>> {
		
		private Class<E> fEnumType;
		
		public EnumListPref(String qualifier, String key, Class<E> enumType) {
			super(qualifier, key, Type.STRING);
			fEnumType = enumType;
		}
		
		@Override
		public boolean isUsageType(Object obj) {
			
			return (obj instanceof List);
		}
		@Override
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
		@Override
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

	/**
	 * Default implementation for preferences of type String-Array
	 */
	public static class StringArrayPref extends Preference<String[]> {
		
		public StringArrayPref(String qualifier, String key) {
			super(qualifier, key, Type.STRING);
		}
		
		
		@Override
		public boolean isUsageType(Object obj) {
			
			return (obj instanceof String[]);
		}
		@Override
		public String[] store2Usage(Object storedValue) {
			
			return LIST_SEPARATOR_PATTERN.split((String) storedValue);
		}
		@Override
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
	
	
}
