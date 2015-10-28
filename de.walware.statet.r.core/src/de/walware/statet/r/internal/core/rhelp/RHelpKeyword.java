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

import java.util.ArrayList;
import java.util.List;

import de.walware.jcommons.collections.ImCollections;
import de.walware.jcommons.collections.ImList;

import de.walware.statet.r.core.rhelp.IRHelpKeyword;


public final class RHelpKeyword implements IRHelpKeyword {
	
	
	private final String keyword;
	private final String description;
	private List<IRHelpKeyword> nested;
	
	
	public RHelpKeyword(final String keyword, final String description,
			final ImList<IRHelpKeyword> childs) {
		this.keyword= keyword;
		this.description= description;
		this.nested= childs;
	}
	
	public RHelpKeyword(final String keyword, final String description) {
		this.keyword= keyword;
		this.description= description;
		this.nested= new ArrayList<>();
	}
	
	
	@Override
	public String getKeyword() {
		return this.keyword;
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
		this.nested= ImCollections.toList(this.nested);
		for (int i= 0; i < this.nested.size(); i++) {
			((RHelpKeyword) this.nested.get(i)).freeze();
		}
	}
	
	
	@Override
	public int hashCode() {
		return this.keyword.hashCode();
	}
	
	@Override
	public String toString() {
		return this.keyword;
	}
	
}
