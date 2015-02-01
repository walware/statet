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

import java.util.ArrayList;
import java.util.List;

import de.walware.statet.r.core.model.ArgsDefinition.Arg;


/**
 * Builder for {@link ArgsDefinition}
 */
public final class ArgsBuilder {
	
	
	private final List<Arg> fArgs = new ArrayList<Arg>();
	
	
	public ArgsBuilder() {
	}
	
	
	public ArgsBuilder add(final String name) {
		fArgs.add(new Arg(fArgs.size(), name, 0, null));
		return this;
	}
	
	public ArgsBuilder add(final String... name) {
		for (int i = 0; i < name.length; i++) {
			fArgs.add(new Arg(fArgs.size(), name[i], 0, null));
		}
		return this;
	}
	
	public ArgsBuilder add(final String name, final int type) {
		fArgs.add(new Arg(fArgs.size(), name, type, null));
		return this;
	}
	
	public ArgsBuilder add(final String name, final int type, final String className) {
		fArgs.add(new Arg(fArgs.size(), name, type, className));
		return this;
	}
	
	
	public ArgsDefinition toDef() {
		return new ArgsDefinition(fArgs.toArray(new Arg[fArgs.size()]));
	}
	
}
