/*=============================================================================#
 # Copyright (c) 2014-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.core.sourcemodel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.walware.statet.r.core.IRProject;
import de.walware.statet.r.core.model.IRClass;
import de.walware.statet.r.core.model.IRElement;
import de.walware.statet.r.core.model.IRFrame;
import de.walware.statet.r.core.model.IRFrameInSource;
import de.walware.statet.r.core.model.IRLangElement;
import de.walware.statet.r.core.model.IRMethod;
import de.walware.statet.r.core.model.IRModelInfo;
import de.walware.statet.r.core.model.IRSourceUnit;
import de.walware.statet.r.internal.core.builder.ExportedRClass;
import de.walware.statet.r.internal.core.builder.ExportedRElement;
import de.walware.statet.r.internal.core.builder.ExportedRMethod;
import de.walware.statet.r.internal.core.builder.RUnitElement;


public class RModelIndexOrder {
	
	
	protected static class Result {
		
		public final String unitId;
		public final RUnitElement exportedElement;
		public final Set<String> defaultNames;
		
		public Result(final RUnitElement root, final Set<String> defaultNames) {
			this.unitId= root.getId();
			this.exportedElement= root;
			this.defaultNames= defaultNames;
		}
		
	}
	
	
	protected final IRProject rProject;
	protected final String projectName;
	
	protected final List<Result> updated= new ArrayList<>();
	
	protected final List<String> removed= new ArrayList<>();
	
	protected final List<String> modelTypeIds;
	protected final boolean isFullBuild;
	
	
	public RModelIndexOrder(final IRProject rProject,
			final List<String> modelTypeIds, final boolean isFullBuild) {
		this.rProject= rProject;
		this.projectName= rProject.getProject().getName();
		this.modelTypeIds= modelTypeIds;
		this.isFullBuild= isFullBuild;
	}
	
	
	protected Result createResult(final IRSourceUnit sourceUnit, final IRModelInfo model) {
		if (model == null) {
			return null;
		}
		
		final IRFrameInSource topFrame= model.getTopFrame();
		final List<? extends IRLangElement> children= topFrame.getModelChildren(null);
		final ArrayList<IRLangElement> exports= new ArrayList<>(children.size());
		final RUnitElement root= new RUnitElement(sourceUnit, exports);
		for (final IRLangElement element : children) {
			final int type= element.getElementType();
			switch (type & IRElement.MASK_C1) {
			case IRElement.C1_METHOD:
				exports.add(new ExportedRMethod(root, (IRMethod) element));
				break;
			case IRElement.C1_CLASS:
				exports.add(new ExportedRClass(root, (IRClass) element));
				break;
			case IRElement.C1_VARIABLE:
				exports.add(new ExportedRElement(root, element));
				break;
			default:
				continue;
			}
		}
		final Set<String> names= new HashSet<>();
		names.addAll(model.getTopFrame().getAllAccessNames());
		final Map<String, ? extends IRFrame> frames= model.getReferencedFrames();
		for (final IRFrame frame : frames.values()) {
			names.addAll(((IRFrameInSource) frame).getAllAccessNames());
		}
		
		return new Result(root, names);
	}
	
	protected void addRemovedUnit(final String unitId) {
		if (!this.isFullBuild) {
			this.removed.add(unitId);
		}
	}
	
}
