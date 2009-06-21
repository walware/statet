/*******************************************************************************
 * Copyright (c) 2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.editors;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.apache.commons.collections.primitives.IntList;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationExtension;
import org.eclipse.swt.graphics.Image;

import de.walware.statet.r.core.model.ArgsDefinition;
import de.walware.statet.r.core.model.IRMethod;
import de.walware.statet.r.ui.RLabelProvider;


public class RArgumentListContextInformation implements IContextInformation,
		IContextInformationExtension {
	
	
	private int fOffset;
	private ArgsDefinition fArgs;
	
	private String fInformation;
	private int[] fInformationIndexes;
	
	
	public RArgumentListContextInformation(final int offset, final IRMethod method) {
		fOffset = offset;
		fArgs = method.getArgsDefinition();
		final StringBuilder sb = new StringBuilder();
		final IntList idxs = new ArrayIntList();
		new RLabelProvider().appendArgumentInformation(sb, idxs, fArgs);
		fInformation = sb.toString();
		fInformationIndexes = idxs.toArray();
	}
	
	
	public ArgsDefinition getArguments() {
		return fArgs;
	}
	
	public String getContextDisplayString() {
		return getInformationDisplayString();
	}
	
	public int getContextInformationPosition() {
		return Math.max(fOffset, 0);
	}
	
	public Image getImage() {
		return null;
	}
	
	public String getInformationDisplayString() {
		return fInformation;
	}
	
	public int[] getInformationDisplayStringArgumentIdxs() {
		return fInformationIndexes;
	}
	
	@Override
	public boolean equals(final Object obj) {
		// prevent stacking of context information at the same position
		return true;
	}
	
}
