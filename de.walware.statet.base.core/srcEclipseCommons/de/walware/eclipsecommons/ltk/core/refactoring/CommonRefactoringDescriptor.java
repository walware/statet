/*******************************************************************************
 * Copyright (c) 2000-2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package de.walware.eclipsecommons.ltk.core.refactoring;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import de.walware.eclipsecommons.ltk.internal.core.refactoring.RefactoringMessages;


/**
 * Default implementation of a refactoring descriptor.
 * This refactoring descriptor can only be used as temporary storage to transfer
 * refactoring descriptor data. {@link #createRefactoring(RefactoringStatus)} always returns null. 
 */
public final class CommonRefactoringDescriptor extends RefactoringDescriptor {
	
	
	/** The map of arguments */
	private final Map<String, String> fArguments;
	
	
	/**
	 * Creates a new default refactoring descriptor.
	 * 
	 * @param id
	 *     the unique id of the refactoring
	 * @param project
	 *     the project name, or <code>null</code>
	 * @param description
	 *     the description
	 * @param comment
	 *     the comment, or <code>null</code>
	 * @param arguments
	 *     the argument map
	 * @param flags
	 *     the flags
	 */
	public CommonRefactoringDescriptor(final String id, final String project, final String description, final String comment, final Map<String, String> arguments, final int flags) {
		super(id, project, description, comment, flags);
		
		fArguments = Collections.unmodifiableMap(new HashMap(arguments));
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * @return this implementation always <code>null</code>
	 */
	@Override
	public Refactoring createRefactoring(final RefactoringStatus status) throws CoreException {
		status.merge(RefactoringStatus.createFatalErrorStatus(
				RefactoringMessages.Common_error_CannotCreateFromDescr_message)); // default refactoring descriptor
		return null;
	}
	
	/**
	 * Returns the argument map
	 * 
	 * @return the argument map.
	 */
	public Map<String, String> getArguments() {
		return fArguments;
	}
	
}
