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

package de.walware.statet.r.core.rmodel;

import java.util.ArrayList;
import java.util.List;

import de.walware.statet.r.core.rmodel.ArgsDefinition.Arg;


/**
 * Builder for {@link ArgsDefinition}
 */
public final class ArgsBuilder {
	
	
	private int fEId;
	private List<Arg> fArgs = new ArrayList<Arg>();
	
	
	ArgsBuilder(final int eId) {
		fEId = eId;
	}
	
	public ArgsBuilder() {
		fEId = -1;
	}
	
	
	public ArgsBuilder add(final String name) {
		fArgs.add(new Arg(fArgs.size(), name, 0));
		return this;
	}
	
	public ArgsBuilder add(final String... name) {
		for (int i = 0; i < name.length; i++) {
			fArgs.add(new Arg(fArgs.size(), name[i], 0));
		}
		return this;
	}
	
	public ArgsBuilder add(final String name, final int type) {
		fArgs.add(new Arg(fArgs.size(), name, type));
		return this;
	}
	
	
	public ArgsDefinition toDef() {
		return new ArgsDefinition(fEId, fArgs.toArray(new Arg[fArgs.size()]));
	}
	
}
