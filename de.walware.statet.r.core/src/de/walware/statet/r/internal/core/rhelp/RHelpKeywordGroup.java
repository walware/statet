/*=============================================================================#
 # Copyright (c) 2010-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.core.rhelp;

import java.util.Collections;
import java.util.List;

import de.walware.ecommons.collections.ConstArrayList;

import de.walware.statet.r.core.rhelp.IRHelpKeyword;


public class RHelpKeywordGroup implements IRHelpKeyword.Group {
	
	
	private final String label;
	private final String description;
	private List<IRHelpKeyword> nested;
	
	
	public RHelpKeywordGroup(final String label, final String description,
			final List<IRHelpKeyword> keywords) {
		this.label= label;
		this.description= description;
		this.nested= keywords;
	}
	
	
	@Override
	public String getLabel() {
		return this.label;
	}
	
	@Override
	public String getDescription() {
		return this.description;
	}
	
	@Override
	public List<IRHelpKeyword> getNestedKeywords() {
		return this.nested;
	}
	
	@Override
	public IRHelpKeyword getNestedKeyword(final String keyword) {
		for (int i= 0; i < this.nested.size(); i++) {
			final IRHelpKeyword node= this.nested.get(i);
			if (node.getKeyword().equals(keyword)) {
				return node;
			}
		}
		return null;
	}
	
	public void freeze() {
		if (this.nested.size() == 0) {
			this.nested= Collections.emptyList();
		}
		else {
			this.nested= new ConstArrayList<IRHelpKeyword>(this.nested);
			for (int i= 0; i < this.nested.size(); i++) {
				((RHelpKeyword) this.nested.get(i)).freeze();
			}
		}
	}
	
	
	@Override
	public int hashCode() {
		return this.label.hashCode();
	}
	
	@Override
	public boolean equals(final Object obj) {
		return (this == obj || (obj instanceof IRHelpKeyword.Group
				&& this.label.equals(((RHelpKeywordGroup) obj).getLabel()) ));
	}
	
}
