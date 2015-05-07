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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

import de.walware.ecommons.ltk.LTK;
import de.walware.ecommons.ltk.core.ElementSet;
import de.walware.ecommons.ltk.core.refactoring.CommonRefactoringDescriptor;
import de.walware.ecommons.ltk.core.refactoring.RefactoringChange;
import de.walware.ecommons.ltk.core.refactoring.RefactoringMessages;
import de.walware.ecommons.ltk.core.refactoring.SourceUnitChange;
import de.walware.ecommons.ltk.core.refactoring.TextChangeCompatibility;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.RUtil;
import de.walware.statet.r.core.model.IRFrame;
import de.walware.statet.r.core.model.IRSourceUnit;
import de.walware.statet.r.core.model.RElementAccess;
import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.core.rsource.ast.Assignment;
import de.walware.statet.r.core.rsource.ast.NodeType;
import de.walware.statet.r.core.rsource.ast.RAst;
import de.walware.statet.r.core.rsource.ast.RAstNode;
import de.walware.statet.r.core.source.RHeuristicTokenScanner;
import de.walware.statet.r.internal.core.refactoring.Messages;

public class InlineTempRefactoring extends Refactoring {
	
	
	private final RRefactoringAdapter fAdapter = new RRefactoringAdapter();
	private final ElementSet fElementSet;
	
	private IRegion fSelectionRegion;
	
	private final IRSourceUnit fSourceUnit;
	
	private RAstNode fSymbolNode;
	private RElementAccess[] fAccessList;
	private Assignment fAssignmentNode;
	
	
	/**
	 * Creates a new inline constant refactoring.
	 * @param su the source unit
	 * @param region (selected) region of an occurrence of the variable
	 */
	public InlineTempRefactoring(final IRSourceUnit su, final IRegion region) {
		fSourceUnit = su;
		fElementSet = new ElementSet(new Object[] { su });
		
		if (region != null && region.getOffset() >= 0 && region.getLength() >= 0) {
			fSelectionRegion = region;
		}
	}
	
	
	@Override
	public String getName() {
		return Messages.InlineTemp_label;
	}
	
	public String getIdentifier() {
		return RRefactoring.INLINE_TEMP_REFACTORING_ID;
	}
	
	public int getReferencesCount() {
		return (fAccessList != null) ? fAccessList.length : -1;
	}
	
	@Override
	public RefactoringStatus checkInitialConditions(final IProgressMonitor monitor) throws CoreException {
		final SubMonitor progress = SubMonitor.convert(monitor, 6);
		try {
			if (fSelectionRegion != null) {
				fSymbolNode = fAdapter.searchPotentialNameNode(fSourceUnit, fSelectionRegion,
						true, progress.newChild(4) );
				}
			if (fSymbolNode == null) {
				return RefactoringStatus.createFatalErrorStatus(Messages.InlineTemp_error_InvalidSelection_message);
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
		final RElementAccess currentAccess = RElementAccess.getMainElementAccessOfNameNode(fSymbolNode);
		if (currentAccess == null) {
			result.merge(RefactoringStatus.createFatalErrorStatus("Failed to detect variable information."));
			return;
		}
		if (currentAccess.getType() != RElementName.MAIN_DEFAULT || currentAccess.getNextSegment() != null) {
			result.merge(RefactoringStatus.createFatalErrorStatus(Messages.InlineTemp_error_InvalidSelection_message)); // no common variable
			return;
		}
		final IRFrame frame = currentAccess.getFrame();
		if (frame != null
				&& (frame.getFrameType() == IRFrame.PACKAGE || frame.getFrameType() == IRFrame.EXPLICIT)) {
			result.merge(RefactoringStatus.createFatalErrorStatus(Messages.InlineTemp_error_InvalidSelectionNotLocal_message));
			return;
		}
		
		final RElementAccess[] allInUnit = currentAccess.getAllInUnit();
		Arrays.sort(allInUnit, RElementAccess.NAME_POSITION_COMPARATOR);
		int current = -1;
		for (int i = 0; i < allInUnit.length; i++) {
			if (currentAccess == allInUnit[i]) {
				current = i;
				break;
			}
		}
		if (current < 0) {
			throw new IllegalStateException();
		}
		RElementAccess writeAccess = null;
		while (current >= 0) {
			final RElementAccess access = allInUnit[current];
			if (access.isWriteAccess()) {
				writeAccess = access;
				break;
			}
			current--;
		}
		if (writeAccess == null) {
			result.merge(RefactoringStatus.createFatalErrorStatus(Messages.InlineTemp_error_MissingDefinition_message));
			return;
		}
		final RAstNode node = writeAccess.getNode();
		switch (node != null ? node.getNodeType() : NodeType.DUMMY) {
		case A_LEFT:
		case A_EQUALS:
		case A_RIGHT:
			break;
		case F_DEF_ARG:
			result.merge(RefactoringStatus.createFatalErrorStatus(Messages.InlineTemp_error_InvalidSelectionParameter_message));
			return;
		default:
			result.merge(RefactoringStatus.createFatalErrorStatus(Messages.InlineTemp_error_InvalidSelectionNoArrow_message));
			return;
		}
		final Assignment assignment = (Assignment) node;
		final RAstNode source = assignment.getSourceChild();
		
		if (RAst.hasErrors(source)) {
			result.merge(RefactoringStatus.createWarningStatus(Messages.InlineTemp_warning_ValueSyntaxError_message));
		}
		
		final int start = current;
		current++;
		while (current < allInUnit.length) {
			if (allInUnit[current].isWriteAccess()) {
				break;
			}
			current++;
		}
		fAccessList = new RElementAccess[current-start];
		System.arraycopy(allInUnit, start, fAccessList, 0, fAccessList.length);
		fAssignmentNode = assignment;
	}
	
	public String getVariableName() {
		if (fAccessList != null) {
			return fAccessList[0].getSegmentName();
		}
		return null;
	}
	
	@Override
	public RefactoringStatus checkFinalConditions(final IProgressMonitor monitor) throws CoreException {
		final SubMonitor progress = SubMonitor.convert(monitor, RefactoringMessages.Common_FinalCheck_label, 100);
		try {
			final RefactoringStatus status = new RefactoringStatus();
			fAdapter.checkFinalToModify(status, fElementSet, progress.newChild(2));
			return status;
		}
		finally {
			progress.done();
		}
	}
	
	@Override
	public Change createChange(final IProgressMonitor monitor) throws CoreException {
		try {
			final SubMonitor progress = SubMonitor.convert(monitor, RefactoringMessages.Common_CreateChanges_label, 3);
			
			final TextFileChange textFileChange = new SourceUnitChange(fSourceUnit);
			if (fSourceUnit.getWorkingContext() == LTK.EDITOR_CONTEXT) {
				textFileChange.setSaveMode(TextFileChange.LEAVE_DIRTY);
			}
			createChanges(textFileChange, progress.newChild(1));
			
			final Map<String, String> arguments = new HashMap<String, String>();
			final String description = NLS.bind(Messages.InlineTemp_Descriptor_description,
					RUtil.formatVarName(getVariableName()) );
			final IProject resource = fElementSet.getSingleProject();
			final String project = (resource != null) ? resource.getName() : null;
			final String source = (project != null) ? NLS.bind(RefactoringMessages.Common_Source_Project_label, project) : RefactoringMessages.Common_Source_Workspace_label;
			final int flags = 0;
			final String comment = ""; //$NON-NLS-1$
			final CommonRefactoringDescriptor descriptor = new CommonRefactoringDescriptor(
					getIdentifier(), project, description, comment, arguments, flags);
			
			return new RefactoringChange(descriptor,
					Messages.InlineTemp_label, 
					new Change[] { textFileChange });
		}
		catch (final BadLocationException e) {
			throw new CoreException(new Status(IStatus.ERROR, RCore.PLUGIN_ID, "Unexpected error (concurrent change?)", e));
		}
		finally {
			monitor.done();
		}
	}
	
	private void createChanges(final TextFileChange change, final SubMonitor progress) throws BadLocationException {
		final RAstNode value = fAssignmentNode.getSourceChild();
		
		fSourceUnit.connect(progress.newChild(1));
		try {
			final AbstractDocument doc = fSourceUnit.getDocument(progress.newChild(1));
			final String text = doc.get(value.getOffset(), value.getLength());
			final String text2 = "("+text+")"; //$NON-NLS-1$ //$NON-NLS-2$
			// Check parent
			final RAstNode parent = fAssignmentNode.getRParent();
			if (parent.getNodeType() == NodeType.BLOCK || parent.getNodeType() == NodeType.SOURCELINES) {
				final RHeuristicTokenScanner scanner = fAdapter.getScanner(fSourceUnit);
				final IRegion assignmentRegion = fAdapter.expandWhitespaceBlock(doc, fAssignmentNode, scanner);
				TextChangeCompatibility.addTextEdit(change, Messages.InlineTemp_Changes_DeleteAssignment_name,
						new DeleteEdit(assignmentRegion.getOffset(), assignmentRegion.getLength()));
			}
			else {
				final TextEdit edit = new ReplaceEdit(fAssignmentNode.getOffset(), fAssignmentNode.getLength(),
						requireParentheses(fAssignmentNode, value) ? text2 : text);
				TextChangeCompatibility.addTextEdit(change, Messages.InlineTemp_Changes_ReplaceAssignment_name, edit);
			}
			for (int i = 1; i < fAccessList.length; i++) {
				final RAstNode node = fAccessList[i].getNode();
				final TextEdit edit = new ReplaceEdit(node.getOffset(), node.getLength(),
						requireParentheses(node, value) ? text2 : text);
				TextChangeCompatibility.addTextEdit(change, Messages.InlineTemp_Changes_ReplaceReference_name, edit);
			}
		}
		finally {
			fSourceUnit.disconnect(progress.newChild(1));
		}
	}
	
	private boolean requireParentheses(final RAstNode oldValue, final RAstNode newValue) {
		final RAstNode parent = oldValue.getRParent();
		if (parent != null) {
			return ((parent.getNodeType().opPrec > 15)
					&& (parent.getNodeType().opPrec < newValue.getNodeType().opPrec));
		}
		return false;
	}
	
}
