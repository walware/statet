/*******************************************************************************
 * Copyright (c) 2012-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.core.refactoring;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.DeleteParticipant;
import org.eclipse.osgi.util.NLS;

import de.walware.statet.r.core.RProject;
import de.walware.statet.r.internal.core.RCorePlugin;
import de.walware.statet.r.internal.core.sourcemodel.RModelIndex;


public class RModelDeleteParticipant extends DeleteParticipant {
	
	
	private class DeleteProjectChange extends Change {
		
		
		private final IProject fProject;
		
		
		public DeleteProjectChange(final IProject project) {
			fProject = project;
		}
		
		
		@Override
		public String getName() {
			return NLS.bind(Messages.RModel_DeleteProject_name, fProject.getName());
		}
		
		@Override
		public Object getModifiedElement() {
			return fProject;
		}
		
		@Override
		public void initializeValidationData(final IProgressMonitor monitor) {
		}
		
		@Override
		public RefactoringStatus isValid(final IProgressMonitor monitor) throws CoreException,
				OperationCanceledException {
			final RefactoringStatus status = new RefactoringStatus();
			return status;
		}
		
		@Override
		public Change perform(final IProgressMonitor monitor) throws CoreException {
			final RModelIndex index = RCorePlugin.getDefault().getRModelManager().getIndex();
			index.updateProjectConfigRemoved(fProject);
			
			return null;
		}
		
		
		@Override
		public String toString() {
			return getName();
		}
		
	}
	
	
	private IProject fProject;
	
	
	public RModelDeleteParticipant() {
	}
	
	
	@Override
	public String getName() {
		return Messages.RModel_DeleteParticipant_name;
	}
	
	@Override
	protected boolean initialize(final Object element) {
		if (element instanceof IProject) {
			try {
				if (((IProject) element).hasNature(RProject.NATURE_ID)) {
					fProject = (IProject) element;
					return true;
				}
			}
			catch (final CoreException e) {}
		}
		return false;
	}
	
	@Override
	public RefactoringStatus checkConditions(final IProgressMonitor monitor,
			final CheckConditionsContext context)
			throws OperationCanceledException {
		final RefactoringStatus status = new RefactoringStatus();
		return status;
	}
	
	@Override
	public Change createChange(final IProgressMonitor monitor) throws CoreException,
			OperationCanceledException {
		if (fProject != null) {
			return new DeleteProjectChange(fProject);
		}
		return null;
	}
	
}
