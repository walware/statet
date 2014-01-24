/*=============================================================================#
 # Copyright (c) 2009-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.ui.editors;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.apache.commons.collections.primitives.IntList;
import org.eclipse.jface.text.contentassist.IContextInformationExtension;
import org.eclipse.swt.graphics.Image;

import de.walware.ecommons.ltk.ui.sourceediting.assist.IAssistInformationProposal;

import de.walware.statet.r.core.model.ArgsDefinition;
import de.walware.statet.r.core.model.IRMethod;
import de.walware.statet.r.ui.RLabelProvider;


public class RArgumentListContextInformation implements IAssistInformationProposal,
		IContextInformationExtension {
	
	
	private final int fOffset;
	private final ArgsDefinition fArgs;
	
	private final String fInformation;
	private final int[] fInformationIndexes;
	
	
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
	
	@Override
	public String getContextDisplayString() {
		return getInformationDisplayString();
	}
	
	@Override
	public int getContextInformationPosition() {
		return Math.max(fOffset, 0);
	}
	
	@Override
	public Image getImage() {
		return null;
	}
	
	@Override
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
