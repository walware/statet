/*******************************************************************************
 * Copyright (c) 2006-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.ltk.core.refactoring;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.ChangeDescriptor;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.RefactoringChangeDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;


/**
 * Change with support for refactoring descriptors.
 */
public final class RefactoringChange extends CompositeChange {
	
	
	/** The refactoring descriptor */
	private final RefactoringDescriptor fDescriptor;
	
	
	/**
	 * Creates a new dynamic validation refactoring change.
	 * 
	 * @param descriptor
	 *     the refactoring descriptor
	 * @param name
	 *     the name of the change
	 */
	public RefactoringChange(final RefactoringDescriptor descriptor, final String name) {
		super(name);
		assert (descriptor != null);
		fDescriptor = descriptor;
	}
	
	/**
	 * Creates a new dynamic validation refactoring change.
	 * 
	 * @param descriptor
	 *     the refactoring descriptor
	 * @param name
	 *     the name of the change
	 * @param changes
	 *     the changes
	 */
	public RefactoringChange(final RefactoringDescriptor descriptor, final String name, final Change[] changes) {
		super(name, changes);
		assert (descriptor != null);
		fDescriptor = descriptor;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public ChangeDescriptor getDescriptor() {
		return new RefactoringChangeDescriptor(fDescriptor);
	}
	
}
