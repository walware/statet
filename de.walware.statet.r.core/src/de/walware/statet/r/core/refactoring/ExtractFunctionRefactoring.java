/*=============================================================================#
 # Copyright (c) 2008-2014 Stephan Wahlbrink (WalWare.de) and others.
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
import java.util.Collections;
import java.util.Comparator;
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
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.ReplaceEdit;

import de.walware.ecommons.ltk.AstInfo;
import de.walware.ecommons.ltk.LTK;
import de.walware.ecommons.ltk.ast.AstSelection;
import de.walware.ecommons.ltk.ast.IAstNode;
import de.walware.ecommons.ltk.core.ElementSet;
import de.walware.ecommons.ltk.core.refactoring.CommonRefactoringDescriptor;
import de.walware.ecommons.ltk.core.refactoring.RefactoringChange;
import de.walware.ecommons.ltk.core.refactoring.RefactoringMessages;
import de.walware.ecommons.ltk.core.refactoring.SourceUnitChange;
import de.walware.ecommons.ltk.core.refactoring.TextChangeCompatibility;

import de.walware.statet.r.core.RCodeStyleSettings;
import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.RUtil;
import de.walware.statet.r.core.model.IRFrame;
import de.walware.statet.r.core.model.IRFrameInSource;
import de.walware.statet.r.core.model.IRModelInfo;
import de.walware.statet.r.core.model.IRModelManager;
import de.walware.statet.r.core.model.IRSourceUnit;
import de.walware.statet.r.core.model.RElementAccess;
import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.core.rsource.RHeuristicTokenScanner;
import de.walware.statet.r.core.rsource.ast.Assignment;
import de.walware.statet.r.core.rsource.ast.Block;
import de.walware.statet.r.core.rsource.ast.FDef;
import de.walware.statet.r.core.rsource.ast.GenericVisitor;
import de.walware.statet.r.core.rsource.ast.NodeType;
import de.walware.statet.r.core.rsource.ast.RAst;
import de.walware.statet.r.core.rsource.ast.RAstNode;
import de.walware.statet.r.internal.core.refactoring.Messages;

public class ExtractFunctionRefactoring extends Refactoring {
	
	
	private class VariableSearcher extends GenericVisitor {
		
		private final int fStart = fOperationRegion.getOffset();
		private final int fStop = fOperationRegion.getOffset()+fOperationRegion.getLength();
		
		@Override
		public void visitNode(final RAstNode node) throws InvocationTargetException {
			if (node.getOffset() >= fStop || node.getStopOffset() < fStart) {
				return;
			}
			final Object[] attachments = node.getAttachments();
			for (final Object attachment : attachments) {
				if (attachment instanceof RElementAccess) {
					final RElementAccess access = (RElementAccess) attachment;
					if (access.getType() != RElementName.MAIN_DEFAULT
							|| access.getSegmentName() == null) {
						continue;
					}
					final RAstNode nameNode = access.getNameNode();
					if (nameNode.getOffset() >= fStart && nameNode.getStopOffset() <= fStop) {
						add(access);
					}
				}
			}
			node.acceptInRChildren(this);
		}
		
		@Override
		public void visit(final FDef node) throws InvocationTargetException {
		}
		
		private void add(final RElementAccess access) {
			final IRFrame frame = access.getFrame();
			if (!(frame instanceof IRFrameInSource)
					|| frame.getFrameType() == IRFrame.PACKAGE) {
				return;
			}
			final String name = access.getSegmentName();
			Variable variable = fVariablesMap.get(name);
			if (variable == null) {
				variable = new Variable(name);
				fVariablesMap.put(name, variable);
			}
			variable.checkAccess(access);
		}
		
	}
	
	public class Variable {
		
		
		private final String fName;
		private boolean fAsArgument;
		private boolean fAsArgumentDefault;
		private RElementAccess fFirstAccess;
		private RElementAccess fLastAccess;
		
		
		public Variable(final String name) {
			fName = name;
		}
		
		
		void checkAccess(final RElementAccess access) {
			if (fFirstAccess == null
					|| access.getNode().getOffset() < fFirstAccess.getNode().getOffset()) {
				fFirstAccess = access;
				fAsArgumentDefault = fAsArgument = (!access.isWriteAccess() && !access.isFunctionAccess());
			}
			if (fLastAccess == null
					|| access.getNode().getOffset() > fLastAccess.getNode().getOffset()) {
				fLastAccess = access;
			}
		}
		
		public String getName() {
			return fName;
		}
		
		public boolean getUseAsArgumentDefault() {
			return fAsArgumentDefault;
		}
		
		public boolean getUseAsArgument() {
			return fAsArgument;
		}
		
		public void setUseAsArgument(final boolean enable) {
			fAsArgument = enable;
		}
		
	}
	
	
	private final RRefactoringAdapter fAdapter = new RRefactoringAdapter();
	private final ElementSet fElementSet;
	
	private IRegion fSelectionRegion;
	private IRegion fOperationRegion;
	
	private final IRSourceUnit fSourceUnit;
	private RAstNode[] fExpressions;
	
//	private RAstNode fContainer;
	private Map<String, Variable> fVariablesMap;
	private List<Variable> fVariablesList;
	private String fFunctionName;
	
	
	/**
	 * Creates a new extract function refactoring.
	 * @param su the source unit
	 * @param region (selected) region of the statements to extract
	 */
	public ExtractFunctionRefactoring(final IRSourceUnit su, final IRegion selection) {
		fSourceUnit = su;
		fElementSet = new ElementSet(new Object[] { su });
		
		if (selection != null && selection.getOffset() >= 0 && selection.getLength() >= 0) {
			fSelectionRegion = selection;
		}
	}
	
	
	@Override
	public String getName() {
		return Messages.ExtractFunction_label;
	}
	
	public String getIdentifier() {
		return RRefactoring.EXTRACT_FUNCTION_REFACTORING_ID;
	}
	
	
	public void setFunctionName(final String newName) {
		fFunctionName = newName;
	}
	
	public String getFunctionName() {
		return fFunctionName;
	}
	
	public List<Variable> getVariables() {
		return fVariablesList;
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
						final IRegion region = fAdapter.trimToAstRegion(document, fSelectionRegion, scanner);
						final AstInfo ast = modelInfo.getAst();
						if (ast != null) {
							final AstSelection astSelection = AstSelection.search(ast.root,
									region.getOffset(), region.getOffset()+region.getLength(),
									AstSelection.MODE_COVERING_SAME_LAST );
							final IAstNode covering = astSelection.getCovering();
							if (covering instanceof RAstNode) {
								final RAstNode rCovering = (RAstNode) covering;
								if ((rCovering.getOffset() == region.getOffset() && rCovering.getLength() == region.getLength())
										|| (rCovering.getNodeType() != NodeType.SOURCELINES && rCovering.getNodeType() != NodeType.BLOCK)) {
									fExpressions = new RAstNode[] { rCovering };
								}
								else {
									final int count = rCovering.getChildCount();
									final List<RAstNode> childList = new ArrayList<RAstNode>(count);
									int i = 0;
									for (; i < count; i++) {
										final RAstNode child = rCovering.getChild(i);
										if (child == astSelection.getChildFirstTouching()) {
											break;
										}
									}
									for (; i < count; i++) {
										final RAstNode child = rCovering.getChild(i);
										childList.add(child);
										if (child == astSelection.getChildLastTouching()) {
											break;
										}
									}
									if (!childList.isEmpty()) {
										fExpressions = childList.toArray(new RAstNode[childList.size()]);
									}
								}
							}
						}
					}
					
					if (fExpressions != null) {
						final IRegion region = new Region(fExpressions[0].getOffset(), fExpressions[fExpressions.length-1].getStopOffset()-fExpressions[0].getOffset());
						fOperationRegion = fAdapter.expandSelectionRegion(document, region, fSelectionRegion, scanner);
					}
				}
				finally {
					fSourceUnit.disconnect(progress.newChild(1));
				}
			}
			
			if (fExpressions == null) {
				return RefactoringStatus.createFatalErrorStatus(Messages.ExtractFunction_error_InvalidSelection_message);
			}
			final RefactoringStatus result = new RefactoringStatus();
			fAdapter.checkInitialToModify(result, fElementSet);
			progress.worked(1);
			
			if (result.hasFatalError()) {
				return result;
			}
			
			checkExpressions(result);
			progress.worked(2);
			return result;
		}
		finally {
			progress.done();
		}
	}
	
	private void checkExpressions(final RefactoringStatus result) {
		for (final RAstNode node : fExpressions) {
			if (RAst.hasErrors(node)) {
				result.merge(RefactoringStatus.createWarningStatus(Messages.ExtractFunction_warning_SelectionSyntaxError_message));
				break;
			}
		}
		if (fSelectionRegion != null
				&& (fSelectionRegion.getOffset() != fOperationRegion.getOffset() || fSelectionRegion.getLength() != fOperationRegion.getLength())) {
			result.merge(RefactoringStatus.createWarningStatus(Messages.ExtractFunction_warning_ChangedRange_message));
		}
		
		fVariablesMap = new HashMap<String, Variable>();
		final VariableSearcher searcher = new VariableSearcher();
		try {
			for (final RAstNode node : fExpressions) {
				node.acceptInR(searcher);
			}
		} catch (final InvocationTargetException e) {}
		fVariablesList = new ArrayList<Variable>(fVariablesMap.values());
		Collections.sort(fVariablesList, new Comparator<Variable>() {
			@Override
			public int compare(final Variable o1, final Variable o2) {
				return RElementAccess.NAME_POSITION_COMPARATOR.compare(o1.fFirstAccess, o2.fFirstAccess);
			}
		});
		fFunctionName = ""; //$NON-NLS-1$
	}
	
	public RefactoringStatus checkFunctionName(final String name) {
		final String message = fAdapter.validateIdentifier(name, "The function name");
		if (message != null) {
			return RefactoringStatus.createFatalErrorStatus(message);
		}
		return new RefactoringStatus();
	}
	
	
	@Override
	public RefactoringStatus checkFinalConditions(final IProgressMonitor monitor) throws CoreException {
		final SubMonitor progress = SubMonitor.convert(monitor, RefactoringMessages.Common_FinalCheck_label, 100);
		try {
			final RefactoringStatus status = checkFunctionName(fFunctionName);
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
			final String varName = RRefactoringAdapter.getUnquotedIdentifier(fFunctionName);
			final String description = NLS.bind(Messages.ExtractFunction_Descriptor_description,
					RUtil.formatVarName(varName) );
			final IProject resource = fElementSet.getSingleProject();
			final String project = (resource != null) ? resource.getName() : null;
			final String source = (project != null) ? NLS.bind(RefactoringMessages.Common_Source_Project_label, project) : RefactoringMessages.Common_Source_Workspace_label;
			final int flags = 0;
			final String comment = ""; //$NON-NLS-1$
			final CommonRefactoringDescriptor descriptor = new CommonRefactoringDescriptor(
					getIdentifier(), project, description, comment, arguments, flags);
			
			return new RefactoringChange(descriptor,
					Messages.ExtractFunction_label, 
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
			final AbstractDocument document = fSourceUnit.getDocument(progress.newChild(1));
			final RHeuristicTokenScanner scanner = fAdapter.getScanner(fSourceUnit);
			final RCodeStyleSettings codeStyle = RRefactoringAdapter.getCodeStyle(fSourceUnit);
			
			final String nl = document.getDefaultLineDelimiter();
			final String defAssign = " <- ";
			final String argAssign = " = ";
			
			RAstNode firstParentChild = fExpressions[0];
			while (true) {
				final RAstNode parent = firstParentChild.getRParent();
				if (parent == null
						|| parent.getNodeType() == NodeType.SOURCELINES || parent.getNodeType() == NodeType.BLOCK) {
					break;
				}
				firstParentChild = parent;
			}
			
			int startOffset;
			int stopOffset;
			final RAstNode lastNode;
			if (fExpressions.length == 1 && fExpressions[0].getNodeType() == NodeType.BLOCK) {
				final Block block = (Block) fExpressions[0];
				startOffset = block.getOffset()+1;
				stopOffset = block.getBlockCloseOffset();
				if (stopOffset == Integer.MIN_VALUE) {
					stopOffset = block.getStopOffset();
				}
				lastNode = (block.getChildCount() > 0) ?
						block.getChild(block.getChildCount()-1) : block.getChild(123);
			}
			else {
				startOffset = fExpressions[0].getOffset();
				lastNode = fExpressions[fExpressions.length-1];
				stopOffset = lastNode.getStopOffset();
			}
			
			final StringBuilder sb = new StringBuilder();
			sb.append(fFunctionName);
			sb.append(defAssign);
			sb.append("function("); //$NON-NLS-1$
			boolean hasArguments = false;
			for (final Variable variable : fVariablesList) {
				if (variable.getUseAsArgument()) {
					sb.append(variable.getName());
					sb.append(", "); //$NON-NLS-1$
					hasArguments = true;
				}
			}
			if (hasArguments) {
				sb.delete(sb.length()-2, sb.length());
			}
			sb.append(')');
			if (codeStyle.getNewlineFDefBodyBlockBefore()) {
				sb.append(nl);
			}
			else {
				sb.append(' ');
			}
			sb.append('{');
			sb.append(nl);
			if (startOffset < stopOffset) {
				final IRegion lastLine = document.getLineInformationOfOffset(stopOffset-1);
				stopOffset = Math.min(stopOffset, lastLine.getOffset()+lastLine.getLength());
			}
			sb.append(document.get(startOffset, stopOffset-startOffset));
			sb.append(nl);
			sb.append("}"); //$NON-NLS-1$
			sb.append(nl);
			final String fdef = RRefactoringAdapter.indent(sb, document, firstParentChild.getOffset(), fSourceUnit);
			
			final IRegion region = fAdapter.expandWhitespaceBlock(document, fOperationRegion, scanner);
			final int insertOffset = fAdapter.expandWhitespaceBlock(document, 
					fAdapter.expandSelectionRegion(document,
							new Region(firstParentChild.getOffset(), 0), fOperationRegion, scanner ),
					scanner ).getOffset();
			if (insertOffset == region.getOffset()) {
				TextChangeCompatibility.addTextEdit(change, Messages.ExtractFunction_Changes_ReplaceOldWithFunctionDef_name,
						new ReplaceEdit(insertOffset, region.getLength(), fdef) );
			}
			else {
				TextChangeCompatibility.addTextEdit(change, Messages.ExtractFunction_Changes_DeleteOld_name,
						new DeleteEdit(region.getOffset(), region.getLength()) );
				TextChangeCompatibility.addTextEdit(change, Messages.ExtractFunction_Changes_AddFunctionDef_name,
						new InsertEdit(insertOffset, fdef) );
			}
			
			sb.setLength(0);
			if (firstParentChild == fExpressions[0] && lastNode.getNodeType() == NodeType.A_LEFT) {
				final Assignment assignment = (Assignment) lastNode;
				sb.append(document.get(assignment.getTargetChild().getOffset(), assignment.getTargetChild().getLength()));
				sb.append(" "); //$NON-NLS-1$
				sb.append(assignment.getOperator(0).text);
				sb.append(" "); //$NON-NLS-1$
			}
			sb.append(fFunctionName);
			sb.append("("); //$NON-NLS-1$
			for (final Variable variable : fVariablesList) {
				if (variable.getUseAsArgument()) {
					sb.append(variable.getName());
					sb.append(argAssign);
					sb.append(variable.getName());
					sb.append(", "); //$NON-NLS-1$
				}
			}
			if (hasArguments) {
				sb.delete(sb.length()-2, sb.length());
			}
			sb.append(")"); //$NON-NLS-1$
			if (firstParentChild == fExpressions[0]) {
				sb.append(nl);
			}
			
			TextChangeCompatibility.addTextEdit(change, Messages.ExtractFunction_Changes_AddFunctionCall_name, 
					new InsertEdit(region.getOffset()+region.getLength(), sb.toString()) );
		}
		finally {
			fSourceUnit.disconnect(progress.newChild(1));
		}
	}
	
}
