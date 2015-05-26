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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.osgi.util.NLS;
import org.eclipse.text.edits.ReplaceEdit;

import de.walware.ecommons.collections.ImList;
import de.walware.ecommons.ltk.LTK;
import de.walware.ecommons.ltk.core.ElementSet;
import de.walware.ecommons.ltk.core.model.ISourceUnit;
import de.walware.ecommons.ltk.core.refactoring.CommonRefactoringDescriptor;
import de.walware.ecommons.ltk.core.refactoring.RefactoringChange;
import de.walware.ecommons.ltk.core.refactoring.RefactoringMessages;
import de.walware.ecommons.ltk.core.refactoring.SourceUnitChange;
import de.walware.ecommons.ltk.core.refactoring.TextChangeCompatibility;
import de.walware.ecommons.ltk.core.refactoring.TextChangeManager;

import de.walware.statet.r.core.IRProject;
import de.walware.statet.r.core.RUtil;
import de.walware.statet.r.core.model.IRFrame;
import de.walware.statet.r.core.model.IRFrameInSource;
import de.walware.statet.r.core.model.IRModelInfo;
import de.walware.statet.r.core.model.IRModelManager;
import de.walware.statet.r.core.model.IRSourceUnit;
import de.walware.statet.r.core.model.IRWorkspaceSourceUnit;
import de.walware.statet.r.core.model.RElementAccess;
import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.core.refactoring.RElementSearchProcessor.Mode;
import de.walware.statet.r.core.rlang.RTerminal;
import de.walware.statet.r.core.rsource.ast.NodeType;
import de.walware.statet.r.core.rsource.ast.RAst;
import de.walware.statet.r.core.rsource.ast.RAstNode;
import de.walware.statet.r.internal.core.refactoring.Messages;

	
public class RenameInWorkspaceRefactoring extends Refactoring {
	
	
	private static final int FOUND_NONE= 0;
	private static final int FOUND_READ= 1;
	private static final int FOUND_WRITE= 2;
	
	
	private class SearchProcessor extends RElementSearchProcessor {
		
		
		TextChangeManager changeManager;
		
		
		public SearchProcessor(final RElementName elementName,
				final IRSourceUnit sourceUnit, final RElementAccess mainAccess) {
			super(elementName, sourceUnit, mainAccess, null, WARN_NO_DEFINITION);
		}
		
		
		@Override
		public void begin(final SubMonitor progress) {
			this.changeManager= new TextChangeManager();
		}
		
		@Override
		public void beginFinalProcessing(final SubMonitor progress) {
			progress.subTask(RefactoringMessages.Common_CreateChanges_label);
			
			final IRSourceUnit initialSu= getInitialSourceUnit();
			final TextFileChange textFileChange= this.changeManager.get(initialSu);
			if (initialSu.getWorkingContext() == LTK.EDITOR_CONTEXT) {
				textFileChange.setSaveMode(TextFileChange.LEAVE_DIRTY);
			}
		}
		
		@Override
		public void process(final IRProject project, final List<ISourceUnit> sus,
				final SubMonitor progress) throws BadLocationException {
			if (sus != null) {
				int remaining= sus.size();
				for (final ISourceUnit su : sus) {
					progress.setWorkRemaining(remaining--);
					final TextFileChange change= this.changeManager.get(su);
					createChanges(su, change, this.definitionFrameIds, progress.newChild(1));
				}
			}
		}
		
	}
	
	
	private final RRefactoringAdapter adapter= new RRefactoringAdapter();
	private final ElementSet elementSet;
	
	private IRegion selectionRegion;
	
	private final IRWorkspaceSourceUnit sourceUnit;
	
	private RAstNode initialSymbolNode;
	private RElementAccess initialAccess;
	private SearchProcessor searchProcessor;
	
	private String newName;
	private Change[] changes;
	
	
	/**
	 * Creates a new rename refactoring
	 * 
	 * @param su the source unit
	 * @param region (selected) region of an occurrence of the variable
	 */
	public RenameInWorkspaceRefactoring(final IRWorkspaceSourceUnit su, final IRegion region) {
		this.sourceUnit= su;
		this.elementSet= new ElementSet(new Object[] { su });
		
		if (region != null && region.getOffset() >= 0 && region.getLength() >= 0) {
			this.selectionRegion= region;
		}
	}
	
	/**
	 * Creates a new rename refactoring
	 * 
	 * @param su the source unit
	 * @param region (selected) region of an occurrence of the variable
	 */
	public RenameInWorkspaceRefactoring(final IRWorkspaceSourceUnit su, final RAstNode node) {
		this.sourceUnit= su;
		this.elementSet= new ElementSet(new Object[] { su });
		
		if (node.getNodeType() == NodeType.SYMBOL || node.getNodeType() == NodeType.STRING_CONST) {
			this.initialSymbolNode= node;
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
		final SubMonitor progress= SubMonitor.convert(monitor, 6);
		try {
			if (this.selectionRegion != null) {
				this.initialSymbolNode= this.adapter.searchPotentialNameNode(this.sourceUnit, this.selectionRegion,
						false, progress.newChild(4) );
				}
			if (this.initialSymbolNode == null) {
				return RefactoringStatus.createFatalErrorStatus(Messages.RenameInWorkspace_error_InvalidSelection_message);
			}
			if (this.sourceUnit.getResource() == null || this.sourceUnit.getResource().getProject() == null) {
				return RefactoringStatus.createFatalErrorStatus("The file is not in the workspace");
			}
			final RefactoringStatus result= new RefactoringStatus();
			this.adapter.checkInitialToModify(result, this.elementSet);
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
		this.initialAccess= RElementAccess.getMainElementAccessOfNameNode(this.initialSymbolNode);
		final RElementAccess subAccess= RElementAccess.getElementAccessOfNameNode(this.initialSymbolNode);
		final RElementName fullName= (this.initialAccess != null && subAccess != null) ?
				RElementName.cloneSegments(this.initialAccess, subAccess.getNextSegment(), false) : null;
		this.searchProcessor= new SearchProcessor(fullName, this.sourceUnit, this.initialAccess);
		if (this.searchProcessor.getStatus().getSeverity() >= IStatus.ERROR) {
			result.merge(RefactoringStatus.create(this.searchProcessor.getStatus()));
			return;
		}
		
		this.newName= fullName.getDisplayName();
	}
	
	public String getCurrentName() {
		return this.searchProcessor.getElementName().getDisplayName();
	}
	
	public String getNewName() {
		return this.newName;
	}
	
	public RefactoringStatus checkNewName(final String name) {
		final String message= this.adapter.validateIdentifier(name, "The variable name");
		if (message != null) {
			return RefactoringStatus.createFatalErrorStatus(message);
		}
		return new RefactoringStatus();
	}
	
	public void setNewName(final String name) {
		this.newName= name;
	}
	
	public List<Mode> getAvailableModes() {
		return this.searchProcessor.getAvailableModes();
	}
	
	public Mode getMode() {
		return this.searchProcessor.getMode();
	}
	
	public void setMode(final Mode mode) {
		this.searchProcessor.setMode(mode);
	}
	
	@Override
	public RefactoringStatus checkFinalConditions(final IProgressMonitor monitor) throws CoreException {
		final SubMonitor progress= SubMonitor.convert(monitor, RefactoringMessages.Common_FinalCheck_label, 100);
		try {
			final RefactoringStatus status= checkNewName(this.newName);
			if (status.getSeverity() >= IStatus.ERROR) {
				return status;
			}
			
			if (getMode() == Mode.LOCAL_FRAME) {
				this.changes= createLocalChanges(progress.newChild(90));
				
				this.adapter.checkFinalToModify(status, this.elementSet, progress.newChild(2));
				return status;
			}
			else {
				this.searchProcessor.run(progress.newChild(90));
				if (this.searchProcessor.getStatus() != Status.OK_STATUS) {
					status.merge(RefactoringStatus.create(this.searchProcessor.getStatus()));
				}
				final TextChangeManager changeManager= this.searchProcessor.changeManager;
				this.changes= changeManager.getAllChanges();
				
				final ElementSet elements= new ElementSet(changeManager.getAllSourceUnits());
				this.adapter.checkFinalToModify(status, elements, progress.newChild(2));
				return status;
			}
		}
		finally {
			progress.done();
		}
	}
	
	@Override
	public Change createChange(final IProgressMonitor monitor) throws CoreException {
		final SubMonitor progress= SubMonitor.convert(monitor, RefactoringMessages.Common_CreateChanges_label, 3);
		try {
			final Map<String, String> arguments= new HashMap<String, String>();
			final String description= NLS.bind(Messages.RenameInWorkspace_Descriptor_description,
					RUtil.formatVarName(getNewName()) );
			final IProject resource= this.elementSet.getSingleProject();
			final String project= (resource != null) ? resource.getName() : null;
			final String source= (project != null) ? NLS.bind(RefactoringMessages.Common_Source_Project_label, project) : RefactoringMessages.Common_Source_Workspace_label;
			final int flags= 0;
			final String comment= ""; //$NON-NLS-1$
			final CommonRefactoringDescriptor descriptor= new CommonRefactoringDescriptor(
					getIdentifier(), project, description, comment, arguments, flags);
			
			return new RefactoringChange(descriptor,
					Messages.RenameInWorkspace_label, 
					this.changes);
		}
		finally {
			progress.done();
		}
	}
	
	
	private Change[] createLocalChanges(final SubMonitor progress) {
		final TextFileChange change= new SourceUnitChange(this.sourceUnit);
		if (this.sourceUnit.getWorkingContext() == LTK.EDITOR_CONTEXT) {
			change.setSaveMode(TextFileChange.LEAVE_DIRTY);
		}
		
		this.sourceUnit.connect(progress.newChild(1));
		try {
			final ImList<? extends RElementAccess> accessList= this.initialAccess.getAllInUnit(false);
			
			final String unquoted= RRefactoringAdapter.getUnquotedIdentifier(this.newName);
			final String quoted= RRefactoringAdapter.getQuotedIdentifier(this.newName);
			final boolean isQuoted= (this.newName.charAt(0) == '`');
			
			for (final RElementAccess aAccess : accessList) {
				final RAstNode nameNode= aAccess.getNameNode();
				final String text= (isQuoted && nameNode.getNodeType() == NodeType.SYMBOL && nameNode.getOperator(0) == RTerminal.SYMBOL) ?
						this.newName : unquoted;
				final IRegion nameRegion= RAst.getElementNameRegion(nameNode);
				TextChangeCompatibility.addTextEdit(change, Messages.RenameInWorkspace_Changes_ReplaceOccurrence_name,
						new ReplaceEdit(nameRegion.getOffset(), nameRegion.getLength(), text));
			}
			return new Change[] { change };
		}
		finally {
			this.sourceUnit.disconnect(progress.newChild(1));
		}
	}
	
	
	private int createChanges(final ISourceUnit su, final TextFileChange change,
			final Set<String> definitionFrameIds, final SubMonitor progress) throws BadLocationException {
		progress.setWorkRemaining(10);
		int found= FOUND_NONE;
		su.connect(progress.newChild(1));
		try {
			final IRModelInfo modelInfo= (IRModelInfo) su.getModelInfo(RModel.TYPE_ID, IRModelManager.MODEL_FILE, progress.newChild(1));
			
			final String unquoted= RRefactoringAdapter.getUnquotedIdentifier(this.newName);
			final String quoted= RRefactoringAdapter.getQuotedIdentifier(this.newName);
			final boolean isQuoted= (this.newName.charAt(0) == '`');
			
			final List<List<? extends RElementAccess>> allFrameAccess= new ArrayList<List<? extends RElementAccess>>();
			for (final String frameId : definitionFrameIds) {
				final IRFrame frame;
				if (frameId == null) {
					frame= modelInfo.getTopFrame();
				}
				else {
					frame= modelInfo.getReferencedFrames().get(frameId);
				}
				if (frame instanceof IRFrameInSource) {
					final List<? extends RElementAccess> allAccess= ((IRFrameInSource) frame).getAllAccessOf(
							this.searchProcessor.mainName.getSegmentName(), false );
					if (allAccess != null && allAccess.size() > 0) {
						allFrameAccess.add(allAccess);
					}
				}
			}
			for (final List<? extends RElementAccess> allAccess : allFrameAccess) {
				for (final RElementAccess access : allAccess) {
					found|= (access.isWriteAccess() && access.getNextSegment() == null) ? FOUND_WRITE : FOUND_READ;
					final RAstNode nameNode= access.getNameNode();
					final String text= (isQuoted && nameNode.getNodeType() == NodeType.SYMBOL && nameNode.getOperator(0) == RTerminal.SYMBOL) ?
							this.newName : unquoted;
					final IRegion nameRegion= RAst.getElementNameRegion(nameNode);
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
