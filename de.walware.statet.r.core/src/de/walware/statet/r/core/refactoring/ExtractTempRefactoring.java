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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import org.eclipse.jface.text.Region;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.osgi.util.NLS;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.ReplaceEdit;

import de.walware.ecommons.ltk.AstInfo;
import de.walware.ecommons.ltk.LTK;
import de.walware.ecommons.ltk.ast.AstSelection;
import de.walware.ecommons.ltk.core.ElementSet;
import de.walware.ecommons.ltk.core.refactoring.CommonRefactoringDescriptor;
import de.walware.ecommons.ltk.core.refactoring.RefactoringChange;
import de.walware.ecommons.ltk.core.refactoring.RefactoringMessages;
import de.walware.ecommons.ltk.core.refactoring.SourceUnitChange;
import de.walware.ecommons.ltk.core.refactoring.TextChangeCompatibility;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.RUtil;
import de.walware.statet.r.core.model.IRModelInfo;
import de.walware.statet.r.core.model.IRModelManager;
import de.walware.statet.r.core.model.IRSourceUnit;
import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.core.rsource.ast.FDef;
import de.walware.statet.r.core.rsource.ast.GenericVisitor;
import de.walware.statet.r.core.rsource.ast.RAst;
import de.walware.statet.r.core.rsource.ast.RAstNode;
import de.walware.statet.r.core.source.RHeuristicTokenScanner;
import de.walware.statet.r.internal.core.refactoring.Messages;

public class ExtractTempRefactoring extends Refactoring {
	
	
	private class OccurrencesSearcher extends GenericVisitor {
		
		private final int fStart = fExpression.getOffset();
		
		@Override
		public void visitNode(final RAstNode node) throws InvocationTargetException {
			if (node.getEndOffset() < fStart) {
				return;
			}
			if (fExpression.equalsValue(node)) {
				fOccurrencesList.add(node);
			}
			else {
				node.acceptInRChildren(this);
			}
		}
		
		@Override
		public void visit(final FDef node) throws InvocationTargetException {
		}
		
	}
	
	
	private final RRefactoringAdapter fAdapter = new RRefactoringAdapter();
	private final ElementSet fElementSet;
	
	private IRegion fSelectionRegion;
	private IRegion fOperationRegion;
	
	private final IRSourceUnit fSourceUnit;
	private RAstNode fExpression;
	
	private RAstNode fContainer;
	private List<RAstNode> fOccurrencesList;
	private String fTempName = "";
	private boolean fReplaceAllOccurrences = true;
	
	
	/**
	 * Creates a new extract temp refactoring.
	 * @param su the source unit
	 * @param region (selected) regino of the expression to extract
	 */
	public ExtractTempRefactoring(final IRSourceUnit su, final IRegion region) {
		fSourceUnit = su;
		fElementSet = new ElementSet(new Object[] { su });
		
		if (region != null && region.getOffset() >= 0 && region.getLength() >= 0) {
			fSelectionRegion = region;
		}
	}
	
	
	@Override
	public String getName() {
		return Messages.ExtractTemp_label;
	}
	
	public String getIdentifier() {
		return RRefactoring.EXTRACT_TEMP_REFACTORING_ID;
	}
	
	
	public void setTempName(final String newName) {
		fTempName = newName;
	}
	
	public String getTempName() {
		return fTempName;
	}
	
	public void setReplaceAllOccurrences(final boolean enable) {
		fReplaceAllOccurrences = enable;
	}
	
	public int getAllOccurrencesCount() {
		return (fOccurrencesList != null) ? fOccurrencesList.size() : null;
	}
	
	public boolean getReplaceAllOccurrences() {
		return fReplaceAllOccurrences;
	}
	
	@Override
	public RefactoringStatus checkInitialConditions(final IProgressMonitor monitor) throws CoreException {
		final SubMonitor progress = SubMonitor.convert(monitor, 6);
		try {
			if (fSelectionRegion != null) {
				fSourceUnit.connect(progress.newChild(1));
				try {
					final AbstractDocument document = fSourceUnit.getDocument(monitor);
					final RHeuristicTokenScanner scanner = fAdapter.getScanner(fSourceUnit);
					
					final IRModelInfo modelInfo = (IRModelInfo) fSourceUnit.getModelInfo(RModel.TYPE_ID, IRModelManager.MODEL_FILE, progress.newChild(1));
					if (modelInfo != null) {
						final IRegion region = fAdapter.trimToAstRegion(document, fSelectionRegion,
								scanner );
						final AstInfo ast = modelInfo.getAst();
						if (ast != null) {
							fExpression = (RAstNode) AstSelection.search(ast.root,
									region.getOffset(), region.getOffset()+region.getLength(),
									AstSelection.MODE_COVERING_SAME_LAST ).getCovering();
						}
					}
					
					if (fExpression != null) {
						final IRegion region = new Region(fExpression.getOffset(), fExpression.getLength());
						fOperationRegion = fAdapter.expandSelectionRegion(document,
								region, fSelectionRegion, scanner );
					}
				}
				finally {
					fSourceUnit.disconnect(progress.newChild(1));
				}
			}
			
			if (fExpression == null) {
				return RefactoringStatus.createFatalErrorStatus(Messages.ExtractTemp_error_InvalidSelection_message);
			}
			final RefactoringStatus result = new RefactoringStatus();
			fAdapter.checkInitialToModify(result, fElementSet);
			progress.worked(1);
			
			if (result.hasFatalError()) {
				return result;
			}
			
			checkExpression(result);
			progress.worked(2);
			return result;
		}
		finally {
			progress.done();
		}
	}
	
	private void checkExpression(final RefactoringStatus result) {
		switch (fExpression.getNodeType()) {
		case STRING_CONST:
		case NUM_CONST:
		case NULL_CONST:
		case SYMBOL:
		case BLOCK:
		case GROUP:
		case NS_GET:
		case NS_GET_INT:
		case SUB_INDEXED_S:
		case SUB_INDEXED_D:
		case SUB_NAMED_PART:
		case SUB_NAMED_SLOT:
		case POWER:
		case SIGN:
		case SEQ:
		case SPECIAL:
		case MULT:
		case ADD:
		case RELATIONAL:
		case NOT:
		case AND:
		case OR:
		case MODEL:
		case A_LEFT:
		case A_RIGHT:
		case A_EQUALS:
		case A_COLON:
		case C_IF:
		case C_FOR:
		case C_WHILE:
		case C_REPEAT:
		case F_DEF:
		case F_CALL:
			break;
		default:
			result.merge(RefactoringStatus.createFatalErrorStatus(Messages.ExtractTemp_error_InvalidSelectionType_message));
			return;
		}
		
		RAstNode parent = fExpression.getRParent();
		SEARCH_BLOCK: while (parent != null) {
			switch (parent.getNodeType()) {
			case SOURCELINES:
			case BLOCK:
				break SEARCH_BLOCK;
			case F_DEF_ARGS:
				result.merge(RefactoringStatus.createFatalErrorStatus(Messages.ExtractTemp_error_InvalidSelectionFHeader_message));
				return;
			default:
				parent = parent.getRParent();
				continue SEARCH_BLOCK;
			}
		}
		if (parent == null) {
			throw new IllegalStateException();
		}
		
		if (fSelectionRegion != null
				&& (fSelectionRegion.getOffset() != fOperationRegion.getOffset() || fSelectionRegion.getLength() != fOperationRegion.getLength())) {
			result.merge(RefactoringStatus.createWarningStatus(Messages.ExtractTemp_warning_ChangedRange_message));
		}
		
		fOccurrencesList= new ArrayList<>();
		try {
			parent.acceptInRChildren(new OccurrencesSearcher());
		}
		catch (final InvocationTargetException e) {}
		
		for (final RAstNode node : fOccurrencesList) {
			if (RAst.hasErrors(node)) {
				result.merge(RefactoringStatus.createWarningStatus(Messages.ExtractTemp_warning_OccurrencesSyntaxError_message));
				break;
			}
		}
		
		fContainer = parent;
	}
	
	public RefactoringStatus checkTempName(final String name) {
		final String message = fAdapter.validateIdentifier(name, "The local variable name");
		if (message != null) {
			return RefactoringStatus.createFatalErrorStatus(message);
		}
		return new RefactoringStatus();
	}
	
	
	@Override
	public RefactoringStatus checkFinalConditions(final IProgressMonitor monitor) throws CoreException {
		final SubMonitor progress = SubMonitor.convert(monitor, RefactoringMessages.Common_FinalCheck_label, 100);
		try {
			final RefactoringStatus status = checkTempName(fTempName);
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
			
			final Map<String, String> arguments= new HashMap<>();
			final String varName = RRefactoringAdapter.getUnquotedIdentifier(fTempName);
			final String description = NLS.bind(Messages.ExtractTemp_Descriptor_description,
					RUtil.formatVarName(varName) );
			final IProject resource = fElementSet.getSingleProject();
			final String project = (resource != null) ? resource.getName() : null;
			final String source = (project != null) ? NLS.bind(RefactoringMessages.Common_Source_Project_label, project) : RefactoringMessages.Common_Source_Workspace_label;
			final int flags = 0;
			final String comment = ""; //$NON-NLS-1$
			final CommonRefactoringDescriptor descriptor = new CommonRefactoringDescriptor(
					getIdentifier(), project, description, comment, arguments, flags);
			
			return new RefactoringChange(descriptor,
					Messages.ExtractTemp_label, 
					new Change[] { textFileChange });
		}
		catch (final BadLocationException e) {
			throw new CoreException(new Status(IStatus.ERROR, RCore.PLUGIN_ID, "Unexpected error (concurrent change?)", e));
		}
		finally {
			monitor.done();
		}
	}
	
	private void createChanges(final TextFileChange change, final SubMonitor progress) throws BadLocationException, CoreException {
		fSourceUnit.connect(progress.newChild(1));
		try {
			final AbstractDocument doc = fSourceUnit.getDocument(progress.newChild(1));
			
			final String defAssign = " <- ";
			final String text = doc.get(fExpression.getOffset(), fExpression.getLength());
			final String variableName = fTempName;
			final StringBuilder assignText = new StringBuilder();
			assignText.append(variableName);
			assignText.append(defAssign);
			assignText.append(text);
			
			RAstNode baseNode = fExpression;
			while (baseNode.getRParent() != fContainer) {
				baseNode = baseNode.getRParent();
			}
			final int assignOffset = RRefactoringAdapter.prepareInsertBefore(assignText, doc, baseNode.getOffset(), fSourceUnit);
			TextChangeCompatibility.addTextEdit(change, Messages.ExtractTemp_Changes_AddVariable_name,
					new InsertEdit(assignOffset, assignText.toString()) );
			
			for (int i = 0; i < fOccurrencesList.size(); i++) {
				final RAstNode node = fOccurrencesList.get(i);
				TextChangeCompatibility.addTextEdit(change, Messages.ExtractTemp_Changes_ReplaceOccurrence_name, 
						new ReplaceEdit(node.getOffset(), node.getLength(), variableName),
						(node == fExpression || fReplaceAllOccurrences));
			}
		}
		finally {
			fSourceUnit.disconnect(progress.newChild(1));
		}
	}
	
}
