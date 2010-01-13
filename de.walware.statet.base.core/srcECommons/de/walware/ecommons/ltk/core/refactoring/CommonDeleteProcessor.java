/*******************************************************************************
 * Copyright (c) 2008-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.ltk.core.refactoring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.mapping.IResourceChangeDescriptionFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.DeleteProcessor;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.eclipse.ltk.core.refactoring.participants.ResourceChangeChecker;
import org.eclipse.ltk.core.refactoring.participants.SharableParticipants;
import org.eclipse.osgi.util.NLS;

import de.walware.ecommons.ltk.internal.core.refactoring.Messages;


public abstract class CommonDeleteProcessor extends DeleteProcessor {
	
	
	private RefactoringAdapter fAdapter;
	
	private RefactoringElementSet fElementsToDelete;
	
	private Change fDeleteChange;
	
	
	public CommonDeleteProcessor(final RefactoringElementSet elements, final RefactoringAdapter adapter) {
		assert (elements != null);
		assert (adapter != null);
		
		fElementsToDelete = elements;
		fAdapter = adapter;
	}
	
	
	@Override
	public abstract String getIdentifier();
	
	protected abstract String getRefactoringIdentifier();
	
	@Override
	public String getProcessorName() {
		return Messages.DeleteRefactoring_label; 
	}
	
	@Override
	public Object[] getElements() {
		return fElementsToDelete.getInitialObjects();
	}
	
	public Change getDeleteChange() {
		return fDeleteChange;
	}
	
	@Override
	public boolean isApplicable() throws CoreException {
		return fAdapter.canDelete(fElementsToDelete);
	}
	
	@Override
	public RefactoringStatus checkInitialConditions(final IProgressMonitor monitor) throws CoreException {
		final RefactoringStatus result = new RefactoringStatus();
		fAdapter.checkInitialForModification(result, fElementsToDelete);
		return result;
	}
	
	@Override
	public RefactoringStatus checkFinalConditions(final IProgressMonitor monitor, final CheckConditionsContext context) throws CoreException {
		final SubMonitor progress = SubMonitor.convert(monitor, RefactoringMessages.Common_FinalCheck_label, 1); 
		try{
			final RefactoringStatus result = new RefactoringStatus();
			
			recalculateElementsToDelete();
			
			fElementsToDelete.postProcess();
			fAdapter.checkFinalToDelete(result, fElementsToDelete);
			
			final TextChangeManager textManager = new TextChangeManager();
			
			fDeleteChange = fAdapter.createChangeToDelete(getProcessorName(), fElementsToDelete, textManager, progress.newChild(1));
			
			final ResourceChangeChecker checker = (ResourceChangeChecker) context.getChecker(ResourceChangeChecker.class);
			final IResourceChangeDescriptionFactory deltaFactory = checker.getDeltaFactory();
			fAdapter.buildDeltaToDelete(fElementsToDelete, deltaFactory);
			
			return result;
		}
		catch (final OperationCanceledException e) {
			throw e;
		}
		finally{
			progress.done();
		}
	}
	
	@Override
	public RefactoringParticipant[] loadParticipants(final RefactoringStatus status, final SharableParticipants shared) throws CoreException {
		final List<RefactoringParticipant> result = new ArrayList<RefactoringParticipant>();
		fAdapter.addParticipantsToDelete(fElementsToDelete, result, status, this, fElementsToDelete.getAffectedProjectNatures(), shared);
		return result.toArray(new RefactoringParticipant[result.size()]);
	}
	
	/*
	 * The set of elements that will eventually be deleted may be very different from the set
	 * originally selected - there may be fewer, more or different elements.
	 * This method is used to calculate the set of elements that will be deleted - if necessary, 
	 * it asks the user.
	 */
	protected void recalculateElementsToDelete() throws CoreException {
		//the sequence is critical here
		
		fElementsToDelete.removeElementsWithAncestorsOnList();
		
		fAdapter.confirmDeleteOfReadOnlyElements(fElementsToDelete, null);
	}
	
	@Override
	public Change createChange(final IProgressMonitor monitor) throws CoreException {
		try {
			monitor.beginTask(RefactoringMessages.Common_CreateChanges_label, 1);
			final Map<String, String> arguments = new HashMap<String, String>();
			final String description = (fElementsToDelete.getElementCount() == 1) ? 
					Messages.DeleteRefactoring_description_singular : Messages.DeleteRefactoring_description_plural;
			final IProject resource = fElementsToDelete.getSingleProject();
			final String project = (resource != null) ? resource.getName() : null;
			final String source = (project != null) ? NLS.bind(RefactoringMessages.Common_Source_Project_label, project) : RefactoringMessages.Common_Source_Workspace_label;
//			final String header = NLS.bind(RefactoringCoreMessages.JavaDeleteProcessor_header, new String[] { String.valueOf(fElements.length), source});
//			final int flags = JavaRefactoringDescriptor.JAR_MIGRATION | JavaRefactoringDescriptor.JAR_REFACTORING | RefactoringDescriptor.STRUCTURAL_CHANGE | RefactoringDescriptor.MULTI_CHANGE;
//			final JDTRefactoringDescriptorComment comment= new JDTRefactoringDescriptorComment(project, this, header);
//			if (fDeleteSubPackages)
//				comment.addSetting(RefactoringCoreMessages.JavaDeleteProcessor_delete_subpackages);
//		 	if (fAccessorsDeleted)
//				comment.addSetting(RefactoringCoreMessages.JavaDeleteProcessor_delete_accessors);
//			arguments.put(ATTRIBUTE_DELETE_SUBPACKAGES, Boolean.valueOf(fDeleteSubPackages).toString());
//			arguments.put(ATTRIBUTE_SUGGEST_ACCESSORS, Boolean.valueOf(fSuggestGetterSetterDeletion).toString());
//			arguments.put(ATTRIBUTE_RESOURCES, new Integer(fResources.length).toString());
//			for (int offset= 0; offset < fResources.length; offset++)
//				arguments.put(JavaRefactoringDescriptorUtil.ATTRIBUTE_ELEMENT + (offset + 1), JavaRefactoringDescriptorUtil.resourceToHandle(project, fResources[offset]));
//			arguments.put(ATTRIBUTE_ELEMENTS, new Integer(fModelElements.length).toString());
//			for (int offset= 0; offset < fModelElements.length; offset++)
//				arguments.put(JavaRefactoringDescriptorUtil.ATTRIBUTE_ELEMENT + (offset + fResources.length + 1), JavaRefactoringDescriptorUtil.elementToHandle(project, fModelElements[offset]));
			final int flags = 0;
			final String comment = ""; //$NON-NLS-1$
			
			final CommonRefactoringDescriptor descriptor = new CommonRefactoringDescriptor(
					getIdentifier(), project, description, comment, arguments, flags);
			
			return new DynamicValidationRefactoringChange(descriptor,
					Messages.DeleteRefactoring_label, 
					new Change[] { fDeleteChange });
		}
		finally {
			monitor.done();
		}
	}
	
}
