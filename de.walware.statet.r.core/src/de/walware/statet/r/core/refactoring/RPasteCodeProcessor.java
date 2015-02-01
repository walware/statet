/*=============================================================================#
 # Copyright (c) 2008-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.core.refactoring;

import de.walware.ecommons.ltk.core.refactoring.CommonPasteCodeProcessor;
import de.walware.ecommons.ltk.core.refactoring.RefactoringDestination;


public class RPasteCodeProcessor extends CommonPasteCodeProcessor {
	
	
	public RPasteCodeProcessor(final String code, final RefactoringDestination destination,
			final RRefactoringAdapter adapter) {
		super(code, destination, adapter);
	}
	
	
	@Override
	public String getIdentifier() {
		return RRefactoring.PASTE_CODE_PROCESSOR_ID;
	}
	
	@Override
	protected String getRefactoringIdentifier() {
		return RRefactoring.PASTE_CODE_REFACTORING_ID;
	}
	
}
