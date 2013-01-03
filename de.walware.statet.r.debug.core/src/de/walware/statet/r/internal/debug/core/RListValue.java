/*******************************************************************************
 * Copyright (c) 2010-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.debug.core;

import java.util.List;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;

import de.walware.ecommons.ltk.IModelElement;

import de.walware.statet.r.core.data.ICombinedRElement;


public class RListValue extends RValue {
	
	
	private RVariable[] fVariables;
	private final ICombinedRElement fElement; 
	
	
	public RListValue(final RElementVariable variable, final ICombinedRElement element) {
		super(variable);
		fElement = element;
	}
	
	
	@Override
	public String getValueString() throws DebugException {
		return "[" + fElement.getLength() + "]";
	}
	
	@Override
	public boolean hasVariables() throws DebugException {
		return fElement.hasModelChildren(null);
	}
	
	@Override
	public IVariable[] getVariables() throws DebugException {
		synchronized (this) {
			if (fVariables == null) {
				if (fVariable.fValue != this) {
					fVariables = NO_VARIABLES;
				}
				else {
					final List<? extends IModelElement> children = fElement.getModelChildren(null);
					fVariables = new RVariable[children.size()];
					for (int i = 0; i < fVariables.length; i++) {
						fVariables[i] = new RElementVariable((ICombinedRElement) children.get(i),
								fVariable.fFrame, fVariable.fStamp);
					}
				}
			}
			return fVariables;
		}
	}
	
}
