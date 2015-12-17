/*=============================================================================#
 # Copyright (c) 2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.ui;

import java.util.List;

import de.walware.ecommons.ltk.core.model.IModelElement;

import de.walware.statet.r.core.model.IRFrame;
import de.walware.statet.r.core.model.IRMethod;
import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.ui.sourceediting.RFrameSearchPath;
import de.walware.statet.r.ui.sourceediting.RFrameSearchPath.RFrameIterator;


public class FCallNamePattern {
	
	
	private final RElementName elementName;
	
	private final String packageName;
	
	private final String assignName;
	private final int assignLength;
	
	
	public FCallNamePattern(final RElementName name) {
		this.elementName= name;
		
		this.packageName= (name.getScope() != null
						&& RElementName.isPackageFacetScopeType(name.getScope().getType()) ) ?
				name.getScope().getSegmentName() : null;
		
		if (this.elementName.getNextSegment() == null) {
			this.assignName= this.elementName.getSegmentName();
			this.assignLength= this.assignName.length();
		}
		else {
			this.assignName= null;
			this.assignLength= 0;
		}
	}
	
	
	public final RElementName getElementName() {
		return this.elementName;
	}
	
	public boolean matches(final RElementName candidateName) {
		String candidate0;
		return (candidateName != null
				&& (this.elementName.equals(candidateName)
						|| (this.assignName != null && candidateName.getNextSegment() == null
								&& this.assignLength == (candidate0= candidateName.getSegmentName()).length() - 2
								&& this.elementName.getType() == candidateName.getType()
								&& candidate0.charAt(this.assignLength) == '<' && candidate0.charAt(this.assignLength + 1) == '-'
								&& candidate0.regionMatches(false, 0, this.assignName, 0, this.assignLength) )));
	}
	
	
	public void searchFDef(final RFrameSearchPath searchPath) {
		ITER_FRAMES: for (final RFrameIterator iter= searchPath.iterator(); iter.hasNext(); ) {
			final IRFrame frame= iter.next();
			if (this.packageName != null) {
				final RElementName frameName= frame.getElementName();
				if (!(frameName != null
						&& RElementName.isPackageFacetScopeType(frameName.getType())
						&& packageName.equals(frameName.getSegmentName()) )) {
					continue ITER_FRAMES;
				}
			}
			final List<? extends IModelElement> elements= frame.getModelChildren(null);
			for (final IModelElement candidate : elements) {
				if (candidate.getModelTypeId() == RModel.R_TYPE_ID
						&& (candidate.getElementType() & IModelElement.MASK_C1) == IModelElement.C1_METHOD
						&& matches((RElementName) candidate.getElementName()) ) {
					handleMatch((IRMethod) candidate, frame, iter);
				}
			}
		}
	}
	
	protected void handleMatch(final IRMethod element, final IRFrame frame,
			final RFrameIterator iterator) {
	}
	
}
