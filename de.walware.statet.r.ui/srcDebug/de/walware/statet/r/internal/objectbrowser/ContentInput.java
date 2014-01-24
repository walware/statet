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

package de.walware.statet.r.internal.objectbrowser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.walware.ecommons.ltk.IModelElement;

import de.walware.statet.r.console.core.RWorkspace.ICombinedREnvironment;
import de.walware.statet.r.core.data.ICombinedRElement;


class ContentInput {
	
	
	List<? extends ICombinedREnvironment> searchEnvirs;
	boolean processChanged;
	boolean inputChanged;
	final boolean showCondensedUserspace;
	
	final IModelElement.Filter otherFilter;
	
	private final IModelElement.Filter envFilter;
	private final Map<ICombinedRElement, Object[]> envFiltered;
	
	ICombinedRElement[] rootElements;
	
	
	public ContentInput(final boolean processChanged, final boolean inputChanged,
			final boolean showCondensedUserspace,
			final IModelElement.Filter envFilter, final IModelElement.Filter otherFilter) {
		this.processChanged = processChanged;
		this.inputChanged = inputChanged;
		this.showCondensedUserspace = showCondensedUserspace;
		
		this.otherFilter = otherFilter;
		
		this.envFilter = envFilter;
		this.envFiltered = (envFilter != null) ? new HashMap<ICombinedRElement, Object[]>() : null;
	}
	
	
	public boolean hasEnvFilter() {
		return (this.envFilter != null);
	}
	
	public Object[] getEnvFilterChildren(final ICombinedRElement rElement) {
		Object[] children = this.envFiltered.get(rElement);
		if (children == null) {
			children = rElement.getModelChildren(this.envFilter).toArray();
			this.envFiltered.put(rElement, children);
		}
		return children;
	}
	
	public List<ICombinedRElement> filterEnvChildren(final List<? extends ICombinedRElement> children) {
		final List<ICombinedRElement> list = new ArrayList<ICombinedRElement>(children.size());
		for (final ICombinedRElement rElement : children) {
			if (this.envFilter.include(rElement)) {
				list.add(rElement);
			}
		}
		return list;
	}
	
}
