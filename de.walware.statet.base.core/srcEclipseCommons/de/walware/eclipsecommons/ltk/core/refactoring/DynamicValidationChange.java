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

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import de.walware.eclipsecommons.ltk.internal.core.refactoring.RefactoringMessages;

import de.walware.statet.base.internal.core.BaseCorePlugin;


public class DynamicValidationChange extends CompositeChange implements IResourceChangeListener {
	
	// 30 minutes
	private static final long LIFE_TIME = 30 * 60 * 1000;
	
	
	private RefactoringStatus fValidationState = null;
	private long fTimeStamp;
	
	
	public DynamicValidationChange(final Change change) {
		super(change.getName());
		add(change);
		markAsSynthetic();
	}
	
	public DynamicValidationChange(final String name) {
		super(name);
		markAsSynthetic();
	}
	
	public DynamicValidationChange(final String name, final Change[] changes) {
		super(name, changes);
		markAsSynthetic();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initializeValidationData(final IProgressMonitor pm) {
		super.initializeValidationData(pm);
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
		fTimeStamp= System.currentTimeMillis();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		super.dispose();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public RefactoringStatus isValid(final IProgressMonitor pm) throws CoreException {
		if (fValidationState == null) {
			return super.isValid(pm);
		}
		return fValidationState;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Change createUndoChange(final Change[] childUndos) {
		final DynamicValidationChange result= new DynamicValidationChange(getName());
		for (int i= 0; i < childUndos.length; i++) {
			result.add(childUndos[i]);
		}
		return result;
	}
	
	public void resourceChanged(final IResourceChangeEvent event) {
		if (System.currentTimeMillis() - fTimeStamp < LIFE_TIME) {
			return;
		}
		fValidationState = RefactoringStatus.createFatalErrorStatus(
				RefactoringMessages.DynamicValidationState_WorkspaceChanged_message);
		
		// remove listener from workspace tracker
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		
		// clear up the children to not hang onto too much memory
		final Change[] children = clear();
		for (int i= 0; i < children.length; i++) {
			final Change change = children[i];
			SafeRunner.run(new ISafeRunnable() {
				public void run() throws Exception {
					change.dispose();
				}
				public void handleException(final Throwable exception) {
					BaseCorePlugin.logError(-1, "Disposing refactoring member failed.", exception); //$NON-NLS-1$
				}
			});
		}
	}
	
}
