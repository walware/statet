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

import de.walware.ecommons.ltk.LTK;
import de.walware.ecommons.ltk.LTKUtil;
import de.walware.ecommons.ltk.ast.IAstNode;
import de.walware.ecommons.ltk.core.ElementSet;
import de.walware.ecommons.ltk.core.model.ISourceStructElement;
import de.walware.ecommons.ltk.core.refactoring.CommonRefactoringDescriptor;
import de.walware.ecommons.ltk.core.refactoring.RefactoringChange;
import de.walware.ecommons.ltk.core.refactoring.RefactoringMessages;
import de.walware.ecommons.ltk.core.refactoring.SourceUnitChange;
import de.walware.ecommons.ltk.core.refactoring.TextChangeCompatibility;

import de.walware.statet.r.core.RCodeStyleSettings;
import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.RUtil;
import de.walware.statet.r.core.model.ArgsDefinition;
import de.walware.statet.r.core.model.ArgsDefinition.Arg;
import de.walware.statet.r.core.model.IRElement;
import de.walware.statet.r.core.model.IRMethod;
import de.walware.statet.r.core.model.IRModelInfo;
import de.walware.statet.r.core.model.IRModelManager;
import de.walware.statet.r.core.model.IRSourceUnit;
import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.core.rlang.RTerminal;
import de.walware.statet.r.core.rsource.RHeuristicTokenScanner;
import de.walware.statet.r.core.rsource.ast.FDef;
import de.walware.statet.r.core.rsource.ast.NodeType;
import de.walware.statet.r.core.rsource.ast.RAst;
import de.walware.statet.r.core.rsource.ast.RAstNode;
import de.walware.statet.r.internal.core.refactoring.Messages;

public class FunctionToS4MethodRefactoring extends Refactoring {
	
	
	public class Variable {
		
		
		private final Arg fArg;
		private boolean fAsGenericArgument;
		private boolean fAsGenericArgumentDefault;
		private String fArgumentType;
		
		
		public Variable(final Arg arg) {
			fArg = arg;
		}
		
		
		void init(final boolean enable) {
			fAsGenericArgumentDefault = fAsGenericArgument = enable;
		}
		
		public String getName() {
			return  (fArg.name != null) ? fArg.name : ""; //$NON-NLS-1$
		}
		
		public boolean getUseAsGenericArgumentDefault() {
			return fAsGenericArgumentDefault;
		}
		
		public boolean getUseAsGenericArgument() {
			return fAsGenericArgument;
		}
		
		public void setUseAsGenericArgument(final boolean enable) {
			fAsGenericArgument = enable;
		}
		
		public String getArgumentType() {
			return fArgumentType;
		}
		
		public void setArgumentType(final String typeName) {
			if (typeName != null && typeName.trim().length() > 0) {
				fArgumentType = typeName;
			}
			else {
				fArgumentType = null;
			}
		}
		
	}
	
	
	private final RRefactoringAdapter fAdapter = new RRefactoringAdapter();
	private final ElementSet fElementSet;
	
	private IRegion fSelectionRegion;
	private IRegion fOperationRegion;
	
	private final IRSourceUnit fSourceUnit;
	private IRMethod fFunction;
	
//	private RAstNode fContainer;
	private List<Variable> fVariablesList;
	private String fFunctionName = ""; //$NON-NLS-1$
	private boolean fGenerateGeneric = true;
	
	
	/**
	 * Creates a new converting refactoring.
	 * @param su the source unit
	 * @param region (selected) region of the function to convert
	 */
	public FunctionToS4MethodRefactoring(final IRSourceUnit su, final IRegion selection) {
		fSourceUnit = su;
		fElementSet = new ElementSet(new Object[] { su });
		
		if (selection != null && selection.getOffset() >= 0 && selection.getLength() >= 0) {
			fSelectionRegion = selection;
		}
	}
	
	
	@Override
	public String getName() {
		return Messages.FunctionToS4Method_label;
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
	
	public void setGenerateGeneric(final boolean enable) {
		fGenerateGeneric = enable;
	}
	
	public boolean getGenerateGeneric() {
		return fGenerateGeneric;
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
						ISourceStructElement element = LTKUtil.getCoveringSourceElement(
								modelInfo.getSourceElement(), region );
						while (element != null) {
							if (element instanceof IRMethod) {
								fFunction = (IRMethod) element;
								break;
							}
							element = element.getSourceParent();
						}
					}
					
					if (fFunction != null) {
						final ISourceStructElement source = (ISourceStructElement) fFunction;
						fOperationRegion = fAdapter.expandSelectionRegion(document,
								source.getSourceRange(), fSelectionRegion, scanner );
					}
				}
				finally {
					fSourceUnit.disconnect(progress.newChild(1));
				}
			}
			
			if (fFunction == null) {
				return RefactoringStatus.createFatalErrorStatus(Messages.FunctionToS4Method_error_InvalidSelection_message);
			}
			final RefactoringStatus result = new RefactoringStatus();
			fAdapter.checkInitialToModify(result, fElementSet);
			progress.worked(1);
			
			if (result.hasFatalError()) {
				return result;
			}
			
			checkFunction(result);
			progress.worked(2);
			return result;
		}
		finally {
			progress.done();
		}
	}
	
	private void checkFunction(final RefactoringStatus result) {
		if ((fFunction.getElementType() & IRElement.MASK_C2) != IRElement.R_COMMON_FUNCTION
				&& (fFunction.getElementType() & IRElement.MASK_C2) != IRElement.R_COMMON_FUNCTION) {
			result.merge(RefactoringStatus.createFatalErrorStatus(Messages.FunctionToS4Method_error_SelectionAlreadyS4_message));
			return;
		}
		final RAstNode node = (RAstNode) fFunction.getAdapter(IAstNode.class);
		if (RAst.hasErrors(node)) {
			result.merge(RefactoringStatus.createWarningStatus(Messages.FunctionToS4Method_warning_SelectionSyntaxError_message));
		}
//		if (fSelectionRegion != null
//				&& (fSelectionRegion.getOffset() != fOperationRegion.getOffset() || fSelectionRegion.getLength() != fOperationRegion.getLength())) {
//			result.merge(RefactoringStatus.createWarningStatus("The selected code does not equal exactly the found expression(s)."));
//		}
		
		RElementName elementName = fFunction.getElementName();
		while (elementName.getNextSegment() != null) {
			elementName = elementName.getNamespace();
		}
		fFunctionName = elementName.getDisplayName();
		
		final ArgsDefinition argsDef = fFunction.getArgsDefinition();
		final int count = (argsDef != null) ? argsDef.size() : 0;
		fVariablesList = new ArrayList<Variable>(count);
		boolean dots = false;
		for (int i = 0; i < count; i++) {
			final Arg arg = argsDef.get(i);
			final Variable variable = new Variable(arg);
			if (variable.getName().equals(RTerminal.S_ELLIPSIS)) {
				dots = true;
				variable.init(true);
			}
			else {
				variable.init(!dots);
			}
			fVariablesList.add(variable);
		}
	}
	
	public RefactoringStatus checkFunctionName(final String newName) {
		if (newName == null || newName.isEmpty()) {
			return RefactoringStatus.createFatalErrorStatus(
					NLS.bind(Messages.RIdentifiers_error_EmptyFor_message, "The function name"));
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
			final String description = NLS.bind(Messages.FunctionToS4Method_Descriptor_description,
					RUtil.formatVarName(varName) );
			final IProject resource = fElementSet.getSingleProject();
			final String project = (resource != null) ? resource.getName() : null;
			final String source = (project != null) ? NLS.bind(RefactoringMessages.Common_Source_Project_label, project) : RefactoringMessages.Common_Source_Workspace_label;
			final int flags = 0;
			final String comment = ""; //$NON-NLS-1$
			final CommonRefactoringDescriptor descriptor = new CommonRefactoringDescriptor(
					getIdentifier(), project, description, comment, arguments, flags);
			
			return new RefactoringChange(descriptor,
					Messages.FunctionToS4Method_label, 
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
			
			RAstNode firstParentChild = (RAstNode) fFunction.getAdapter(IAstNode.class);
			while (true) {
				final RAstNode parent = firstParentChild.getRParent();
				if (parent == null
						|| parent.getNodeType() == NodeType.SOURCELINES || parent.getNodeType() == NodeType.BLOCK) {
					break;
				}
				firstParentChild = parent;
			}
			
			final IRegion region = fAdapter.expandWhitespaceBlock(document, fOperationRegion, scanner);
			final int insertOffset = fAdapter.expandWhitespaceBlock(document, 
					fAdapter.expandSelectionRegion(document,
							new Region(firstParentChild.getOffset(), 0), fOperationRegion, scanner ),
					scanner ).getOffset();
			final FDef fdefNode = (FDef) fFunction.getAdapter(FDef.class);
			final IRegion fbodyRegion = fAdapter.expandWhitespaceBlock(document,
					fAdapter.expandSelectionRegion(document,
							fdefNode.getContChild(), fOperationRegion, scanner ),
					scanner );
			
			TextChangeCompatibility.addTextEdit(change, Messages.FunctionToS4Method_Changes_DeleteOld_name,
					new DeleteEdit(region.getOffset(), region.getLength()));
			
			final String nl = document.getDefaultLineDelimiter();
			final String argAssign = codeStyle.getArgAssignString();
			
			final StringBuilder sb = new StringBuilder();
			sb.append("setGeneric(\""); //$NON-NLS-1$
			sb.append(fFunctionName);
			sb.append("\","); //$NON-NLS-1$
			sb.append(nl);
			sb.append("function("); //$NON-NLS-1$
			boolean dots = false;
			for (final Variable variable : fVariablesList) {
				if (variable.getName().equals(RTerminal.S_ELLIPSIS)) {
					dots = true;
				}
				if (variable.getUseAsGenericArgument()) {
					sb.append(RElementName.create(RElementName.MAIN_DEFAULT, variable.getName()).getDisplayName());
					sb.append(", "); //$NON-NLS-1$
				}
			}
			if (!dots) {
				sb.append("..., "); //$NON-NLS-1$
			}
			sb.delete(sb.length()-2, sb.length());
			sb.append(')');
			if (codeStyle.getNewlineFDefBodyBlockBefore()) {
				sb.append(nl);
			}
			else {
				sb.append(' ');
			}
			sb.append('{');
			sb.append(nl);
			sb.append("standardGeneric(\""); //$NON-NLS-1$
			sb.append(fFunctionName);
			sb.append("\")"); //$NON-NLS-1$
			sb.append(nl);
			sb.append("})"); //$NON-NLS-1$
			sb.append(nl);
			sb.append(nl);
			final String genericDef = RRefactoringAdapter.indent(sb, document, firstParentChild.getOffset(), fSourceUnit);
			TextChangeCompatibility.addTextEdit(change, Messages.FunctionToS4Method_Changes_AddGenericDef_name,
					new InsertEdit(insertOffset, genericDef));
			
			sb.setLength(0);
			sb.append("setMethod(\""); //$NON-NLS-1$
			sb.append(fFunctionName);
			sb.append("\","); //$NON-NLS-1$
			sb.append(nl);
			sb.append("signature("); //$NON-NLS-1$
			boolean hasType = false;
			for (final Variable variable : fVariablesList) {
				if (variable.getUseAsGenericArgument() && variable.getArgumentType() != null) {
					hasType = true;
					sb.append(RElementName.create(RElementName.MAIN_DEFAULT, variable.getName()).getDisplayName());
					sb.append(argAssign);
					sb.append("\""); //$NON-NLS-1$
					sb.append(variable.getArgumentType());
					sb.append("\", "); //$NON-NLS-1$
				}
			}
			if (hasType) {
				sb.delete(sb.length()-2, sb.length());
			}
			sb.append("),"); //$NON-NLS-1$
			sb.append(nl);
			sb.append("function("); //$NON-NLS-1$
			final FDef.Args argsNode = fdefNode.getArgsChild();
			for (final Variable variable : fVariablesList) {
				sb.append(RElementName.create(RElementName.MAIN_DEFAULT, variable.getName()).getDisplayName());
				final FDef.Arg argNode = argsNode.getChild(variable.fArg.index);
				if (argNode.hasDefault()) {
					sb.append(argAssign);
					sb.append(document.get(argNode.getDefaultChild().getOffset(), argNode.getDefaultChild().getLength()));
				}
				sb.append(", "); //$NON-NLS-1$
			}
			if (!fVariablesList.isEmpty()) {
				sb.delete(sb.length()-2, sb.length());
			}
			sb.append(')');
			if (codeStyle.getNewlineFDefBodyBlockBefore()
					|| fdefNode.getContChild().getNodeType() != NodeType.BLOCK) {
				sb.append(nl);
			}
			else {
				sb.append(' ');
			}
			sb.append(document.get(fbodyRegion.getOffset(), fbodyRegion.getLength()).trim());
			sb.append(")"); //$NON-NLS-1$
			sb.append(nl);
			final String methodDef = RRefactoringAdapter.indent(sb, document, firstParentChild.getOffset(), fSourceUnit);
			TextChangeCompatibility.addTextEdit(change, Messages.FunctionToS4Method_Changes_AddMethodDef_name,
					new InsertEdit(insertOffset, methodDef));
		}
		finally {
			fSourceUnit.disconnect(progress.newChild(1));
		}
	}
	
}
