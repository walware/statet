/*=============================================================================#
 # Copyright (c) 2009-2016 Stephan Wahlbrink (WalWare.de) and others.
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
	
	
	private final int offset;
	
	private final ArgsDefinition args;
	
	private final String information;
	private final int[] informationIndexes;
	
	
	public RArgumentListContextInformation(final int offset, final IRMethod method) {
		this.offset= offset;
		this.args= method.getArgsDefinition();
		final StringBuilder sb= new StringBuilder();
		final IntList idxs= new ArrayIntList();
		new RLabelProvider().appendArgumentInformation(sb, idxs, this.args);
		this.information= sb.toString();
		this.informationIndexes= idxs.toArray();
	}
	
	
	public ArgsDefinition getArguments() {
		return this.args;
	}
	
	@Override
	public Image getImage() {
		return null;
	}
	
	@Override
	public String getContextDisplayString() {
		return getInformationDisplayString();
	}
	
	@Override
	public int getContextInformationPosition() {
		return Math.max(this.offset, 0);
	}
	
	@Override
	public String getInformationDisplayString() {
		return this.information;
	}
	
	public int[] getInformationDisplayStringArgumentIdxs() {
		return this.informationIndexes;
	}
	
	@Override
	public boolean equals(final Object obj) {
		// prevent stacking of context information at the same position
		return true;
	}
	
}
