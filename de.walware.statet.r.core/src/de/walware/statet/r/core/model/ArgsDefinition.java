/*******************************************************************************
 * Copyright (c) 2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core.model;


/**
 * Formals of an R function
 */
public final class ArgsDefinition {
	
	
	public static final int UNKNOWN = 0;
	public static final int UNSPECIFIC_OBJ =     0x001;
	public static final int UNSPECIFIC_NAME =    0x010;
	public static final int CLASS_OBJ =          0x002;
	public static final int CLASS_NAME =         0x020;
	public static final int METHOD_OBJ =         0x004;
	public static final int METHOD_NAME =        0x040;
	public static final int FILE_NAME =          0x100;
	
	
	public static final class Arg {
		
		public final int index;
		public final String name;
		public final int type;
		String defaultAsCode;
		
		Arg(final int index, final String name, final int type) {
			this.index = index;
			this.name = name;
			this.type = type;
		}
		
	}
	
	
	public final int eId;
	protected final Arg[] fArgs;
	
	
	/**
	 * For more detailed definitions, use an {@link ArgsBuilder}.
	 */
	public ArgsDefinition(final String... argNames) {
		this(-1, argNames);
	}
	
	ArgsDefinition(final int eId, final String... argNames) {
		this.eId = eId;
		fArgs = new Arg[argNames.length];
		for (int i = 0; i < argNames.length; i++) {
			fArgs[i] = new Arg(i, argNames[i], 0);
		}
	}
	
	ArgsDefinition(final int eId, final Arg[] args) {
		this.eId = eId;
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
