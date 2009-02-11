/*******************************************************************************
 * Copyright (c) 2008-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.ltk.core.refactoring;

import org.eclipse.ltk.core.refactoring.participants.DeleteProcessor;


/**
 * Must be implemented for each language.
 */
public class CommonRefactoringFactory {
	
	
	public RefactoringAdapter createAdapter() {
		throw new UnsupportedOperationException();
	}
	
	public DeleteProcessor createDeleteProcessor(final Object[] elements, final RefactoringAdapter adapter) {
		throw new UnsupportedOperationException();
	}
	
}
