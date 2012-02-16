/*******************************************************************************
 * Copyright (c) 2008-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core.refactoring;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.text.AbstractDocument;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.osgi.util.NLS;
import org.eclipse.text.edits.ReplaceEdit;

import de.walware.ecommons.ltk.AstInfo;
import de.walware.ecommons.ltk.ISourceUnit;
import de.walware.ecommons.ltk.ISourceUnitManager;
import de.walware.ecommons.ltk.LTK;
import de.walware.ecommons.ltk.ast.AstSelection;
import de.walware.ecommons.ltk.core.refactoring.CommonRefactoringDescriptor;
import de.walware.ecommons.ltk.core.refactoring.RefactoringChange;
import de.walware.ecommons.ltk.core.refactoring.RefactoringElementSet;
import de.walware.ecommons.ltk.core.refactoring.RefactoringMessages;
import de.walware.ecommons.ltk.core.refactoring.SourceUnitChange;
import de.walware.ecommons.ltk.core.refactoring.TextChangeCompatibility;
import de.walware.ecommons.ltk.core.refactoring.TextChangeManager;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.RProject;
import de.walware.statet.r.core.model.IRFrame;
import de.walware.statet.r.core.model.IRFrameInSource;
import de.walware.statet.r.core.model.IRModelInfo;
import de.walware.statet.r.core.model.IRModelManager;
import de.walware.statet.r.core.model.IRWorkspaceSourceUnit;
import de.walware.statet.r.core.model.RElementAccess;
import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.core.rlang.RTerminal;
import de.walware.statet.r.core.rsource.RHeuristicTokenScanner;
import de.walware.statet.r.core.rsource.ast.NodeType;
import de.walware.statet.r.core.rsource.ast.RAst;
import de.walware.statet.r.core.rsource.ast.RAstNode;
import de.walware.statet.r.internal.core.refactoring.Messages;

public class RenameInWorkspaceRefactoring extends Refactoring {
	
	
	public static enum Mode {
		COMPLETE,
		CURRENT_AND_REFERENCING,
		CURRENT,
		LOCAL,
	};
	
	private static final int FOUND_NONE = 0;
	private static final int FOUND_READ = 1;
	private static final int FOUND_WRITE = 2;
	
	
	private final RRefactoringAdapter fAdapter = new RRefactoringAdapter();
	private final RefactoringElementSet fElementSet;
	
	private IRegion fSelectionRegion;
	
	private final IRWorkspaceSourceUnit fSourceUnit;
	
	private RAstNode fInitialSymbolNode;
	private RElementAccess fInitialAccess;
	private RElementName fName;
	private RElementName fNamespace;
	private EnumSet<Mode> fAvailableModes;
	
	private Mode fMode;
	private String fVariableName;
	private Change[] fChanges;
	
	
	
	/**
	 * Creates a new rename refactoring
	 * 
	 * @param unit the source unit
	 * @param region (selected) region of an occurrence of the variable
	 */
	public RenameInWorkspaceRefactoring(final IRWorkspaceSourceUnit su, final IRegion region) {
		fSourceUnit = su;
		fElementSet = new RefactoringElementSet(new Object[] { su });
		
		if (region != null && region.getOffset() >= 0 && region.getLength() >= 0) {
			fSelectionRegion = region;
		}
	}
	
	/**
	 * Creates a new rename refactoring
	 * 
	 * @param unit the source unit
	 * @param region (selected) region of an occurrence of the variable
	 */
	public RenameInWorkspaceRefactoring(final IRWorkspaceSourceUnit su, final RAstNode node) {
		fSourceUnit = su;
		fElementSet = new RefactoringElementSet(new Object[] { su });
		
		if (node.getNodeType() == NodeType.SYMBOL || node.getNodeType() == NodeType.STRING_CONST) {
			fInitialSymbolNode = node;
		}
	}
	
	
	@Override
	public String getName() {
		return Messages.RenameInWorkspace_label;
	}
	
	public String getIdentifier() {
		return RRefactoring.RENAME_IN_WORKSPACE_REFACTORING_ID;
	}
	
	@Override
	public RefactoringStatus checkInitialConditions(final IProgressMonitor monitor) throws CoreException {
		final SubMonitor progress = SubMonitor.convert(monitor, 6);
		try {
			RAstNode node = null;
			if (fSelectionRegion != null) {
				fSourceUnit.connect(progress.newChild(1));
				try {
					final AbstractDocument document = fSourceUnit.getDocument(monitor);
					final RHeuristicTokenScanner scanner = fAdapter.getScanner(fSourceUnit);
					
					final IRegion region = fAdapter.trimToAstRegion(document,
							fSelectionRegion, scanner );
					
					final IRModelInfo modelInfo = (IRModelInfo) fSourceUnit.getModelInfo(RModel.TYPE_ID, IRModelManager.MODEL_FILE, progress.newChild(1));
					if (modelInfo != null) {
						final AstInfo ast = modelInfo.getAst();
						if (ast != null) {
							node = (RAstNode) AstSelection.search(ast.root,
									region.getOffset(), region.getOffset()+region.getLength(),
									AstSelection.MODE_COVERING_SAME_LAST ).getCovering();
						}
					}
				}
				finally {
					fSourceUnit.disconnect(progress.newChild(1));
				}
			}
			
			if (node != null) {
				fInitialSymbolNode = RRefactoringAdapter.getPotentialNameNode(node, false);
			}
			
			if (fInitialSymbolNode == null) {
				return RefactoringStatus.createFatalErrorStatus(Messages.RenameInWorkspace_error_InvalidSelection_message);
			}
			if (fSourceUnit.getResource() == null || fSourceUnit.getResource().getProject() == null) {
				return RefactoringStatus.createFatalErrorStatus("The file is not in the workspace");
			}
			final RefactoringStatus result = new RefactoringStatus();
			fAdapter.checkInitialToModify(result, fElementSet);
			if (result.hasFatalError()) {
				return result;
			}
			
			checkVariable(result);
			return result;
		}
		finally {
			progress.done();
		}
	}
	
	private void checkVariable(final RefactoringStatus result) {
		final RElementAccess currentAccess = RRefactoringAdapter.searchElementAccessOfNameNode(fInitialSymbolNode);
		if (currentAccess == null) {
			result.merge(RefactoringStatus.createFatalErrorStatus("Failed to detect variable information."));
			return;
		}
		if (currentAccess.getType() != RElementName.MAIN_DEFAULT || currentAccess.getNextSegment() != null) {
			result.merge(RefactoringStatus.createFatalErrorStatus(Messages.RenameInWorkspace_error_InvalidSelection_message)); // no common variable
			return;
		}
		if (currentAccess.getSegmentName() == null || currentAccess.getSegmentName().isEmpty()) {
			result.merge(RefactoringStatus.createFatalErrorStatus(Messages.RenameInWorkspace_error_InvalidSelection_message));
			return;
		}
		final IRFrame frame = currentAccess.getFrame();
		if (frame == null || (frame.getFrameType() != IRFrame.PACKAGE && frame.getFrameType() != IRFrame.PROJECT)) {
			fMode = Mode.LOCAL;
			fAvailableModes = EnumSet.of(Mode.LOCAL);
		}
		else {
			fMode = Mode.COMPLETE;
			fAvailableModes = EnumSet.of(Mode.COMPLETE, Mode.CURRENT_AND_REFERENCING, Mode.CURRENT);
		}
		fInitialAccess = currentAccess;
		fName = RElementName.cloneSegment(currentAccess);
		fNamespace = (currentAccess.getNamespace() != null) ? RElementName.cloneSegment(currentAccess.getNamespace()) : null;
		fVariableName = currentAccess.getDisplayName();
	}
	
	public String getCurrentName() {
		return fName.getDisplayName();
	}
	
	public String getNewName() {
		return fVariableName;
	}
	
	public RefactoringStatus checkNewName(final String name) {
		final String message = fAdapter.validateIdentifier(name, "The variable name");
		if (message != null) {
			return RefactoringStatus.createFatalErrorStatus(message);
		}
		return new RefactoringStatus();
	}
	
	public void setNewName(final String name) {
		fVariableName = name;
	}
	
	public EnumSet<Mode> getAvailableModes() {
		return fAvailableModes;
	}
	
	public Mode getMode() {
		return fMode;
	}
	
	public void setMode(final Mode mode) {
		fMode = mode;
	}
	
	@Override
	public RefactoringStatus checkFinalConditions(final IProgressMonitor monitor) throws CoreException {
		final SubMonitor progress = SubMonitor.convert(monitor, RefactoringMessages.Common_FinalCheck_label, 100);
		try {
			final RefactoringStatus status = checkNewName(fVariableName);
			
			if (fMode == Mode.LOCAL) {
				fChanges = createLocalChanges(progress.newChild(90));
				
				fAdapter.checkFinalToModify(status, fElementSet, progress.newChild(2));
			}
			else {
				final TextChangeManager manager = new TextChangeManager();
				createChanges(status, manager, progress.newChild(90));
				fChanges = manager.getAllChanges();
				
				final RefactoringElementSet elements = new RefactoringElementSet(manager.getAllSourceUnits());
				fAdapter.checkFinalToModify(status, elements, progress.newChild(2));
			}
			
			return status;
		}
		catch (final BadLocationException e) {
			throw new CoreException(new Status(IStatus.ERROR, RCore.PLUGIN_ID, "Unexpected error (concurrent change?)", e));
		}
		finally {
			progress.done();
		}
	}
	
	@Override
	public Change createChange(final IProgressMonitor monitor) throws CoreException {
		final SubMonitor progress = SubMonitor.convert(monitor, RefactoringMessages.Common_CreateChanges_label, 3);
		try {
			final Map<String, String> arguments = new HashMap<String, String>();
			final String description = NLS.bind(Messages.RenameInWorkspace_Descriptor_description, '`'+getNewName()+'`');
			final IProject resource = fElementSet.getSingleProject();
			final String project = (resource != null) ? resource.getName() : null;
			final String source = (project != null) ? NLS.bind(RefactoringMessages.Common_Source_Project_label, project) : RefactoringMessages.Common_Source_Workspace_label;
			final int flags = 0;
			final String comment = ""; //$NON-NLS-1$
			final CommonRefactoringDescriptor descriptor = new CommonRefactoringDescriptor(
					getIdentifier(), project, description, comment, arguments, flags);
			
			return new RefactoringChange(descriptor,
					Messages.RenameInWorkspace_label, 
					fChanges);
		}
		finally {
			progress.done();
		}
	}
	
	
	private Change[] createLocalChanges(final SubMonitor progress) {
		final TextFileChange change = new SourceUnitChange(fSourceUnit);
		if (fSourceUnit.getWorkingContext() == LTK.EDITOR_CONTEXT) {
			change.setSaveMode(TextFileChange.LEAVE_DIRTY);
		}
		
		fSourceUnit.connect(progress.newChild(1));
		try {
			final RElementAccess[] accessList = fInitialAccess.getAllInUnit();
			
			final String unquoted = RRefactoringAdapter.getUnquotedIdentifier(fVariableName);
			final String quoted = RRefactoringAdapter.getQuotedIdentifier(fVariableName);
			final boolean isQuoted = (fVariableName.charAt(0) == '`');
			
			for (int i = 0; i < accessList.length; i++) {
				final RAstNode nameNode = accessList[i].getNameNode();
				final String text = (isQuoted && nameNode.getNodeType() == NodeType.SYMBOL && nameNode.getOperator(0) == RTerminal.SYMBOL) ?
						fVariableName : unquoted;
				final IRegion nameRegion = RAst.getElementNameRegion(nameNode);
				TextChangeCompatibility.addTextEdit(change, Messages.RenameInWorkspace_Changes_ReplaceOccurrence_name,
						new ReplaceEdit(nameRegion.getOffset(), nameRegion.getLength(), text));
			}
			return new Change[] { change };
		}
		finally {
			fSourceUnit.disconnect(progress.newChild(1));
		}
	}
	
	private void createChanges(final RefactoringStatus status, final TextChangeManager manager, final SubMonitor progress) throws BadLocationException, CoreException {
		progress.beginTask(RefactoringMessages.Common_CreateChanges_label, 100);
		
		final List<RProject> allProjects = new ArrayList<RProject>();
		final List<List<ISourceUnit>> allProjectsSus = new ArrayList<List<ISourceUnit>>();
		final List<RProject> definitionProjects = new ArrayList<RProject>();
		final List<RProject> textChangeProjects = new ArrayList<RProject>();
		
		try {
			final Set<String> packages = new HashSet<String>();
			packages.add(null);
			String specificPackage;
			if (fNamespace != null && fNamespace.getType() == RElementName.MAIN_PACKAGE) {
				specificPackage = fNamespace.getDisplayName();
				// search for specified package
				packages.add(specificPackage);
			}
			else {
				specificPackage = null;
				// search in default frames
			}
			
			final RProject initialProject = RProject.getRProject(fSourceUnit.getResource().getProject());
			{	// start with current project
				allProjects.add(initialProject);
				final List<ISourceUnit> sus = loadSus(initialProject, allProjectsSus, true, progress.newChild(3));
				sus.add(fSourceUnit);
				final TextFileChange textFileChange = manager.get(fSourceUnit);
				if (fSourceUnit.getWorkingContext() == LTK.EDITOR_CONTEXT) {
					textFileChange.setSaveMode(TextFileChange.LEAVE_DIRTY);
				}
			}
			
			progress.worked(5);
			
			{	// referenced projects
				final SubMonitor p = progress.newChild(40);
				for (int i = 0; i < allProjects.size(); i++) {
					p.setWorkRemaining(allProjects.size()-i);
					
					final RProject project = allProjects.get(i);
					List<ISourceUnit> sus;
					if (i < allProjectsSus.size()) {
						sus = allProjectsSus.get(i);
					}
					else {
						sus = loadSus(project, allProjectsSus, false, progress.newChild(3));
					}
					if (sus != null) {
						final int found = searchDefinition(project, sus, specificPackage, progress.newChild(5));
						if (found > 0) {
							definitionProjects.add(project);
							if (specificPackage == null) {
								final String packageName = project.getPackageName();
								if (packageName != null) {
									packages.add("package:"+packageName);
								}
							}
							if (found == 2) {
								continue;
							}
						}
						if (fMode == Mode.COMPLETE) {
							addReferencedProjects(project, allProjects);
						}
					}
				}
			}
			
			if (fMode == Mode.COMPLETE && definitionProjects.isEmpty()) {
				status.merge(RefactoringStatus.createWarningStatus(Messages.RenameInWorkspace_warning_NoDefinition_message));
			}
				
			// definitions?
			if (!definitionProjects.isEmpty()) {
				textChangeProjects.addAll(definitionProjects);
			}
			else {
				for (int i = 0; i < allProjects.size(); i++) {
					if (allProjectsSus.get(i) != null) {
						textChangeProjects.add(allProjects.get(i));
					}
				}
			}
			progress.worked(5);
			
			{	// referencing occurrences - create text changes
				final SubMonitor p = progress.newChild(40);
				for (int i = 0; i < textChangeProjects.size(); i++) {
					p.setWorkRemaining(textChangeProjects.size()-i);
					final RProject project = textChangeProjects.get(i);
					final int idx = allProjects.indexOf(project);
					List<ISourceUnit> sus;
					if (idx >= 0) {
						sus = allProjectsSus.get(idx);
					}
					else {
						allProjects.add(project);
						sus = loadSus(project, allProjectsSus, false, progress.newChild(3));
					}
					createChanges(sus, manager, packages, progress.newChild(5));
					if (fMode != Mode.CURRENT) {
						addReferencingProjects(project, textChangeProjects);
					}
				}
			}
			
			packages.remove(null);
			if (packages.size() > 1) {
				status.merge(RefactoringStatus.createWarningStatus(Messages.RenameInWorkspace_warning_MultipleDefinitions_message));
			}
		}
		finally {
			for (final List<ISourceUnit> sus : allProjectsSus) {
				if (sus != null) {
					for (final ISourceUnit su : sus) {
						try {
							su.disconnect(progress.newChild(1));
						}
						catch (final Exception e) {}
					}
				}
			}
		}
	}
	
	private void addReferencedProjects(final RProject initialProject, final List<RProject> projects) throws CoreException {
		final IProject[] referencedProjects = initialProject.getProject().getReferencedProjects();
		for (final IProject referencedProject : referencedProjects) {
			if (referencedProject.isOpen()) {
				final RProject project = RProject.getRProject(referencedProject);
				if (project != null && !projects.contains(project)) {
					projects.add(project);
				}
			}
		}
	}
	
	private void addReferencingProjects(final RProject initialProject, final List<RProject> projects) throws CoreException {
		final IProject[] referencedProjects = initialProject.getProject().getReferencingProjects();
		for (final IProject referencedProject : referencedProjects) {
			if (referencedProject.isOpen()) {
				final RProject project = RProject.getRProject(referencedProject);
				if (project != null && !projects.contains(project)) {
					projects.add(project);
				}
			}
		}
	}
	
	private List<ISourceUnit> loadSus(final RProject project, final List<List<ISourceUnit>> projectsSus, final boolean force, final SubMonitor progress) throws CoreException {
		final List<String> suIds = RCore.getRModelManager().findReferencingSourceUnits(project.getProject(), fName);
		final ISourceUnitManager suManager = LTK.getSourceUnitManager(); 
		if (suIds != null && suIds.size() > 0) {
			int remaining = suIds.size();
			final List<ISourceUnit> sus = new ArrayList<ISourceUnit>();
			projectsSus.add(sus);
			for (final String suId : suIds) {
				progress.setWorkRemaining(3*(remaining--));
				if (!suId.equals(fSourceUnit.getId())) {
					ISourceUnit su = null;
					try {
						su = suManager.getSourceUnit(RModel.TYPE_ID, LTK.PERSISTENCE_CONTEXT, suId, true, progress.newChild(1));
						if (su != null) {
							final ISourceUnit su2 = suManager.getSourceUnit(RModel.TYPE_ID, LTK.EDITOR_CONTEXT, su, true, progress.newChild(1));
							if (su2 != null) {
								su = su2;
							}
							sus.add(su);
						}
					}
					catch (final Throwable e) {
						if (su != null) {
							su.disconnect(progress.newChild(1));
						}
						throw new CoreException(new Status(IStatus.ERROR, RCore.PLUGIN_ID, 1, NLS.bind("An error occurred when looking for ''{0}''.", suId), e));
					}
				}
			}
			return sus;
		}
		else if (force) {
			final List<ISourceUnit> sus = new ArrayList<ISourceUnit>(1);
			projectsSus.add(sus);
			return sus;
		}
		else {
			projectsSus.add(null);
			return null;
		}
	}
	
	private int searchDefinition(final RProject project, final List<ISourceUnit> sus,
			final String specificPackage, final SubMonitor progress) {
		final String packageName = project.getPackageName();
		if (specificPackage != null && packageName != null
				&& specificPackage.equals("package:"+packageName)) {
			progress.setWorkRemaining(2);
			for (final ISourceUnit su : sus) {
				searchDefinition(su, null, progress.newChild(1));
				searchDefinition(su, specificPackage, progress.newChild(1));
			}
			return 2;
		}
		else {
			progress.setWorkRemaining(1);
			boolean found = false;
			for (final ISourceUnit su : sus) {
				found |= searchDefinition(su, specificPackage, progress.newChild(1));
			}
			return (found) ? 1 : 0;
		}
	}
	
	
	private boolean searchDefinition(final ISourceUnit su, final String specificPackage, final SubMonitor progress) {
		progress.setWorkRemaining(10);
		su.connect(progress.newChild(1));
		try {
			final IRModelInfo modelInfo = (IRModelInfo) su.getModelInfo(RModel.TYPE_ID, IRModelManager.MODEL_FILE, progress.newChild(3));
			final IRFrame frame;
			if (specificPackage == null) {
				frame = modelInfo.getTopFrame();
			}
			else {
				frame = modelInfo.getReferencedFrames().get(specificPackage);
			}
			if (frame instanceof IRFrameInSource) {
				final List<? extends RElementAccess> allAccess = ((IRFrameInSource) frame).getAllAccessOf(fName.getSegmentName());
				if (allAccess != null) {
					for (final RElementAccess access : allAccess) {
						if (access.isWriteAccess() && access.getNextSegment() == null) {
							return true;
						}
					}
				}
			}
			progress.setWorkRemaining(1);
			return false;
		}
		finally {
			su.disconnect(progress.newChild(1));
		}
	}
	
	
	private void createChanges(final List<ISourceUnit> sus, final TextChangeManager manager,
			final Set<String> packages,
			final SubMonitor progress) throws BadLocationException {
		if (sus != null) {
			int remaining = sus.size();
			for (final ISourceUnit su : sus) {
				progress.setWorkRemaining(remaining--);
				final TextFileChange change = manager.get(su);
				createChanges(su, change, packages, progress.newChild(1));
			}
		}
	}
	
	private int createChanges(final ISourceUnit su, final TextFileChange change,
			final Set<String> packages, final SubMonitor progress) throws BadLocationException {
		progress.setWorkRemaining(10);
		int found = FOUND_NONE;
		su.connect(progress.newChild(1));
		try {
			final IRModelInfo modelInfo = (IRModelInfo) su.getModelInfo(RModel.TYPE_ID, IRModelManager.MODEL_FILE, progress.newChild(1));
			
			final String unquoted = RRefactoringAdapter.getUnquotedIdentifier(fVariableName);
			final String quoted = RRefactoringAdapter.getQuotedIdentifier(fVariableName);
			final boolean isQuoted = (fVariableName.charAt(0) == '`');
			
			final List<List<? extends RElementAccess>> allFrameAccess = new ArrayList<List<? extends RElementAccess>>();
			for (final String packageId : packages) {
				final IRFrame frame;
				if (packageId == null) {
					frame = modelInfo.getTopFrame();
				}
				else {
					frame = modelInfo.getReferencedFrames().get(packageId);
				}
				if (frame instanceof IRFrameInSource) {
					final List<? extends RElementAccess> allAccess = ((IRFrameInSource) frame).getAllAccessOf(fName.getSegmentName());
					if (allAccess != null && allAccess.size() > 0) {
						allFrameAccess.add(allAccess);
					}
				}
			}
			for (final List<? extends RElementAccess> allAccess : allFrameAccess) {
				for (final RElementAccess access : allAccess) {
					found |= (access.isWriteAccess() && access.getNextSegment() == null) ? FOUND_WRITE : FOUND_READ;
					final RAstNode nameNode = access.getNameNode();
					final String text = (isQuoted && nameNode.getNodeType() == NodeType.SYMBOL && nameNode.getOperator(0) == RTerminal.SYMBOL) ?
							fVariableName : unquoted;
					final IRegion nameRegion = RAst.getElementNameRegion(nameNode);
					TextChangeCompatibility.addTextEdit(change, Messages.RenameInWorkspace_Changes_ReplaceOccurrence_name,
							new ReplaceEdit(nameRegion.getOffset(), nameRegion.getLength(), text));
				}
			}
			progress.setWorkRemaining(1);
			return found;
		}
		finally {
			su.disconnect(progress.newChild(1));
		}
	}
	
}
