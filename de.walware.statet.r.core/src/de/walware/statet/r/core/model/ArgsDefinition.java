/*=============================================================================#
 # Copyright (c) 2008-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.core.model;

import java.io.Serializable;

import de.walware.jcommons.lang.Immutable;


/**
 * Formals of an R function
 */
public final class ArgsDefinition implements Immutable, Serializable {
	
	
	private static final long serialVersionUID = -3536570586490553543L;
	
	
	public static final int UNKNOWN=                        0x0_0000_0000;
	
	private static final int OBJ_SHIFT= 0;
	private static final int NAME_SHIFT= 8;
	private static final int OTHER_SHIFT= 16;
	private static final int FLAGS_SHIFT= 24;
	
	public static final int UNSPECIFIC_OBJ=                 0x01 << OBJ_SHIFT;
	public static final int UNSPECIFIC_NAME=                1 << 0 << NAME_SHIFT;
	
	public static final int PACKAGE_NAME=                   1 << 1 << NAME_SHIFT;
	
	public static final int HELP_TOPIC_NAME=                1 << 4 << NAME_SHIFT;
	
	public static final int CLASS_OBJ=                      1 << 5 << OBJ_SHIFT;
	public static final int CLASS_NAME=                     1 << 5 << NAME_SHIFT;
	
	public static final int METHOD_OBJ=                     1 << 6 << OBJ_SHIFT;
	public static final int METHOD_NAME=                    1 << 6 << NAME_SHIFT;
	
	public static final int FILE_NAME=                      1 << 1 << OTHER_SHIFT;
	
	public static final int NAME_AS_SYMBOL=                 1 << 1 << FLAGS_SHIFT;
	public static final int NAME_AS_STRING=                 1 << 2 << FLAGS_SHIFT;
	
	
	public static final class Arg implements Serializable {
		
		
		private static final long serialVersionUID = 5880323434513504465L;
		
		
		public final int index;
		public final String name;
		public final int type;
		public final String className;
//		String defaultAsCode;
		
		Arg(final int index, final String name, final int type, final String className) {
			this.index = index;
			this.name = name;
			this.type = type;
			this.className = className;
		}
		
	}
	
	
	protected final Arg[] fArgs;
	
	
	/**
	 * For more detailed definitions, use an {@link ArgsBuilder}.
	 */
	public ArgsDefinition(final String... argNames) {
		fArgs = new Arg[argNames.length];
		for (int i = 0; i < argNames.length; i++) {
			fArgs[i] = new Arg(i, argNames[i], 0, null);
		}
	}
	
	ArgsDefinition(final Arg[] args) {
		fArgs = args;
	}
	
	
	public int size() {
		return fArgs.length;
	}
	
	public boolean contains(final String argName) {
		for (int i = 0; i < fArgs.length; i++) {
			if (fArgs[i].name.equals(argName)) {
				return true;
			}
		}
		return false;
	}
	
	public Arg get(final String argName) {
		for (int i = 0; i < fArgs.length; i++) {
			if (fArgs[i].name.equals(argName)) {
				return fArgs[i];
			}
		}
		return null;
	}
	
	public Arg get(final int argIndex) {
		return fArgs[argIndex];
	}
	
	public int indexOf(final String argName) {
		for (int i = 0; i < fArgs.length; i++) {
			if (fArgs[i].name.equals(argName)) {
				return fArgs[i].index;
			}
		}
		return -1;
	}
	
}
