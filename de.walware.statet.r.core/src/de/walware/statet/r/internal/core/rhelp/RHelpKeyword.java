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

package de.walware.statet.r.internal.core.rhelp;

import java.util.Collections;
import java.util.List;

import de.walware.ecommons.collections.ConstList;

import de.walware.statet.r.core.rhelp.IRHelpKeyword;


public class RHelpKeyword implements IRHelpKeyword {
	
	
	private final String fKeyword;
	private final String fDescription;
	private List<IRHelpKeyword> fNested;
	
	
	public RHelpKeyword(final String keyword, final String description,
			final List<IRHelpKeyword> childs) {
		fKeyword = keyword;
		fDescription = description;
		fNested = childs;
	}
	
	
	@Override
	public String getKeyword() {
		return fKeyword;
	}
	
	@Override
	public String getDescription() {
		return fDescription;
	}
	
	@Override
	public List<IRHelpKeyword> getNestedKeywords() {
		return fNested;
	}
	
	@Override
	public IRHelpKeyword getNestedKeyword(final String keyword) {
		for (int i = 0; i < fNested.size(); i++) {
			final IRHelpKeyword node = fNested.get(i);
			if (node.getKeyword().equals(keyword)) {
				return node;
			}
		}
		return null;
	}
	
	public void freeze() {
		if (fNested.size() == 0) {
			fNested = Collections.emptyList();
		}
		else {
			fNested = new ConstList<IRHelpKeyword>(fNested);
			for (int i = 0; i < fNested.size(); i++) {
				((RHelpKeyword) fNested.get(i)).freeze();
			}
		}
	}
	
	
	@Override
	public int hashCode() {
		return fKeyword.hashCode();
	}
	
}
