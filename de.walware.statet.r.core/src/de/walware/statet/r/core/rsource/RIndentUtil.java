/*******************************************************************************
 * Copyright (c) 2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core.rsource;

import org.eclipse.jface.text.IDocument;

import de.walware.ecommons.ltk.text.IndentUtil;

import de.walware.statet.r.core.RCodeStyleSettings;
import de.walware.statet.r.core.RCodeStyleSettings.IndentationType;


/**
 * 
 */
public class RIndentUtil extends IndentUtil {
	
	
	public RIndentUtil(final IDocument document, final RCodeStyleSettings style) {
		super(document,
				style.getReplaceConservative() ? CONSERVE_STRATEGY : CORRECT_STRATEGY,
				(style.getIndentDefaultType() == IndentationType.TAB),
				style.getTabSize(),
				style.getIndentSpacesCount()
				);
	}
	
}
