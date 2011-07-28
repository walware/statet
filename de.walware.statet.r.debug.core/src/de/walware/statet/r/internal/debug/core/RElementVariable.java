/*******************************************************************************
 * Copyright (c) 2010-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.debug.core;

import org.eclipse.debug.core.DebugException;

import de.walware.rj.data.RObject;
import de.walware.rj.data.RReference;
import de.walware.statet.r.console.core.RWorkspace;
import de.walware.statet.r.core.data.ICombinedRElement;
import de.walware.statet.r.debug.core.IRElementVariable;


public class RElementVariable extends RVariable implements IRElementVariable {
	
	
	protected final RStackFrame fFrame;
	protected final int fStamp;
	
	protected ICombinedRElement fElement;
	
	RValue fValue;
	
	
	public RElementVariable(final ICombinedRElement element, final RStackFrame frame, final int stamp) {
		super(frame.getDebugTarget());
		fFrame = frame;
		fElement = element;
		fStamp = stamp;
	}
	
	
	public ICombinedRElement getElement() {
		if (fElement.getRObjectType() == RObject.TYPE_REFERENCE) {
			resolveRef();
		}
		return fElement;
	}
	
	public String getName() throws DebugException {
		return fElement.getElementName().getDisplayName();
	}
	
	public String getReferenceTypeName() throws DebugException {
		return fElement.getRClassName();
	}
	
	public synchronized RValue getValue() throws DebugException {
		if (fValue == null) {
			fValue = createValue();
		}
		return fValue;
	}
	
	protected ICombinedRElement resolveRef() {
		final RWorkspace workspace = getDebugTarget().getProcess().getWorkspaceData();
		if (workspace != null) {
			final RReference reference = (RReference) fElement;
			ICombinedRElement element = workspace.resolve(reference, true);
			if (element != null) {
				fFrame.registerReference(reference, fStamp);
				return element;
			}
			element = fFrame.loadReference(reference, fStamp);
			if (element != null) {
				fFrame.registerReference(reference, fStamp);
				return element;
			}
		}
		return fElement;
	}
	
	protected RValue createValue() {
		ICombinedRElement element = fElement;
		if (element.getRObjectType() == RObject.TYPE_REFERENCE) {
			element = resolveRef();
		}
		switch (element.getRObjectType()) {
		case RObject.TYPE_VECTOR:
			return new RVectorValue(this);
		case RObject.TYPE_ARRAY:
			return new RArrayValue(this);
		case RObject.TYPE_LIST:
		case RObject.TYPE_DATAFRAME:
		case RObject.TYPE_S4OBJECT:
		case RObject.TYPE_ENV:
			return new RListValue(this, element);
		case RObject.TYPE_FUNCTION:
			return new RFunctionValue(this);
		}
		return new RValue(this);
	}
	
}
