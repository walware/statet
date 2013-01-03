/*******************************************************************************
 * Copyright (c) 2010-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui;

import org.eclipse.search.ui.ISearchPageScoreComputer;
import org.eclipse.ui.IEditorInput;

import de.walware.ecommons.ltk.ui.EditorUtility;

import de.walware.statet.r.core.model.IRElement;
import de.walware.statet.r.core.model.IRSourceUnit;
import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.ui.RUI;


public class RSearchPageScoreComputer implements ISearchPageScoreComputer {
	
	
	public RSearchPageScoreComputer() {
	}
	
	
	@Override
	public int computeScore(final String pageId, final Object input) {
		if (RUI.R_HELP_SEARCH_PAGE_ID.equals(pageId)) {
			if (input instanceof IRElement || input instanceof IRSourceUnit) {
				return 85;
			}
			if (input instanceof IEditorInput
					&& EditorUtility.isModelTypeEditorInput((IEditorInput) input, RModel.TYPE_ID)) {
				return 85;
			}
			return ISearchPageScoreComputer.LOWEST;
		}
		return ISearchPageScoreComputer.UNKNOWN;
	}
	
}
