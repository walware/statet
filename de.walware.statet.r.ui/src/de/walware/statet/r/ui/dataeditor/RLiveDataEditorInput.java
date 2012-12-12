/*******************************************************************************
 * Copyright (c) 2010-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.ui.dataeditor;

import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IPersistableElement;

import de.walware.ecommons.ts.ITool;

import de.walware.statet.nico.core.runtime.ToolProcess;

import de.walware.rj.data.RArray;
import de.walware.rj.data.RObject;
import de.walware.rj.data.RStore;

import de.walware.statet.r.core.model.RElementName;


/**
 * Editor input of a R data elements in an R process.
 */
public class RLiveDataEditorInput extends PlatformObject implements IRDataEditorInput {
	
	
	public static boolean isSupported(final RObject element) {
		switch (element.getRObjectType()) {
		case RObject.TYPE_VECTOR:
		case RObject.TYPE_DATAFRAME:
			return true;
		case RObject.TYPE_ARRAY:
			return (((RArray<RStore>) element).getDim().getLength() == 2);
		default:
			return false;
		}
	}
	
	
	private final RProcessDataTableInput fInput;
	
	
	public RLiveDataEditorInput(final ToolProcess process, final RElementName elementName) {
		fInput = new RProcessDataTableInput(elementName, process);
	}
	
	
	@Override
	public ImageDescriptor getImageDescriptor() {
		return null;
	}
	
	@Override
	public String getName() {
		return fInput.getLastName();
	}
	
	@Override
	public String getToolTipText() {
		return NLS.bind("{0} in {1}", fInput.getElementName().getDisplayName(),
				fInput.getProcess().getLabel(ITool.LONG_LABEL));
	}
	
	@Override
	public boolean exists() {
		return !fInput.getProcess().isTerminated();
	}
	
	@Override
	public IPersistableElement getPersistable() {
		return null;
	}
	
	@Override
	public RProcessDataTableInput getRDataTableInput() {
		return fInput;
	}
	
	
	@Override
	public int hashCode() {
		return fInput.hashCode();
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof RLiveDataEditorInput)) {
			return false;
		}
		return (fInput.equals(((RLiveDataEditorInput) obj).fInput));
	}
	
}
