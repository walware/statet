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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
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
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.GroupCategory;
import org.eclipse.ltk.core.refactoring.GroupCategorySet;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.osgi.util.NLS;
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
import de.walware.statet.r.core.model.IRFrame;
import de.walware.statet.r.core.model.IRFrameInSource;
import de.walware.statet.r.core.model.IRModelInfo;
import de.walware.statet.r.core.model.IRModelManager;
import de.walware.statet.r.core.model.IRSourceUnit;
import de.walware.statet.r.core.model.RElementAccess;
import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.core.rlang.RTerminal;
import de.walware.statet.r.core.rsource.RHeuristicTokenScanner;
import de.walware.statet.r.core.rsource.ast.GenericVisitor;
import de.walware.statet.r.core.rsource.ast.NodeType;
import de.walware.statet.r.core.rsource.ast.RAst;
import de.walware.statet.r.core.rsource.ast.RAstNode;
import de.walware.statet.r.internal.core.refactoring.Messages;

public class RenameInRegionRefactoring extends Refactoring {
	
	
	private class VariableSearcher extends GenericVisitor {
		
		private final int fStart = fSelectionRegion.getOffset();
		private final int fStop = fSelectionRegion.getOffset()+fSelectionRegion.getLength();
		
		@Override
		public void visitNode(final RAstNode node) throws InvocationTargetException {
			if (node.getOffset() >= fStop || node.getStopOffset() < fStart) {
				return;
			}
			final Object[] attachments = node.getAttachments();
			for (final Object attachment : attachments) {
				if (attachment instanceof RElementAccess) {
					final RElementAccess access = (RElementAccess) attachment;
					if (access.getType() != RElementName.MAIN_DEFAULT) {
						continue;
					}
					final RAstNode nameNode = access.getNameNode();
					if (nameNode != null
							&& nameNode.getOffset() >= fStart && nameNode.getStopOffset() <= fStop) {
						add(access);
					}
				}
			}
			node.acceptInRChildren(this);
		}
		
		private void add(final RElementAccess access) {
			final IRFrame frame = access.getFrame();
			if (!(frame instanceof IRFrameInSource)
					|| frame.getFrameType() == IRFrame.PACKAGE) {
				return;
			}
			Map<String, Variable> map = fVariablesList.get(frame);
			if (map == null) {
				map = new HashMap<String, Variable>();
				fVariablesList.put(frame, map);
			}
			final String name = access.getSegmentName();
			Variable variable = map.get(name);
			if (variable == null) {
				variable = new Variable(frame, name);
				map.put(name, variable);
			}
			variable.fAccessList.add(access);
		}
		
	}
	
	public class Variable {
		
		
		private final Object fParent;
		
		private final String fName;
		private String fNewName;
		
		private final List<RElementAccess> fAccessList;
		
		private Map<String, Variable> fSubVariables = Collections.emptyMap();
		
		public Variable(final Object parent, final String name) {
			fParent = parent;
			fName = name;
			fAccessList = new ArrayList<RElementAccess>();
		}
		
		
		public Object getParent() {
			return fParent;
		}
		
		public String getName() {
			return fName;
		}
		
		public String getNewName() {
			return fNewName;
		}
		
		public void setNewName(final String name) {
			if (!fName.equals(name)) {
				fNewName = name;
			}
			else {
				fNewName = null;
			}
		}
		
		public int getOccurrencesCount() {
			return fAccessList.size();
		}
		
		
		public Map<String, Variable> getSubVariables() {
			return fSubVariables;
		}
		
	}
	
	
	private final RRefactoringAdapter fAdapter = new RRefactoringAdapter();
	private final ElementSet fElementSet;
	
	private IRegion fSelectionRegion;
	
	private final IRSourceUnit fSourceUnit;
	
	private Map<IRFrame, Map<String, Variable>> fVariablesList;
	
	
	/**
	 * Creates a new rename refactoring.
	 * @param unit the source unit
	 * @param region (selected) region
	 */
	public RenameInRegionRefactoring(final IRSourceUnit su, final IRegion region) {
		fSourceUnit = su;
		fElementSet = new ElementSet(new Object[] { su });
		
		if (region != null && region.getOffset() >= 0 && region.getLength() >= 0) {
			fSelectionRegion = region;
		}
	}
	
	
	@Override
	public String getName() {
		return Messages.RenameInRegion_label;
	}
	
	public String getIdentifier() {
		return RRefactoring.RENAME_IN_REGION_REFACTORING_ID;
	}
	
	public Map<IRFrame, Map<String, Variable>> getVariables() {
		return fVariablesList;
	}
	
	
	@Override
	public RefactoringStatus checkInitialConditions(final IProgressMonitor monitor) throws CoreException {
		final SubMonitor progress = SubMonitor.convert(monitor, 6);
		RAstNode rootNode = null;
		try {
			if (fSelectionRegion != null) {
				fSourceUnit.connect(progress.newChild(1));
				try {
					final AbstractDocument document = fSourceUnit.getDocument(monitor);
					final RHeuristicTokenScanner scanner = fAdapter.getScanner(fSourceUnit);
					
					final IRModelInfo modelInfo = (IRModelInfo) fSourceUnit.getModelInfo(RModel.TYPE_ID, IRModelManager.MODEL_FILE, progress.newChild(1));
					if (modelInfo != null) {
						final IRegion region = fAdapter.trimToAstRegion(document,
								fSelectionRegion, scanner );
						final AstInfo ast = modelInfo.getAst();
						if (ast != null) {
							rootNode = (RAstNode) AstSelection.search(ast.root,
									region.getOffset(), region.getOffset()+region.getLength(),
									AstSelection.MODE_COVERING_SAME_LAST ).getCovering();
						}
					}
				}
				finally {
					fSourceUnit.disconnect(progress.newChild(1));
				}
			}
			
			if (rootNode == null) {
				return RefactoringStatus.createFatalErrorStatus(Messages.ExtractTemp_error_InvalidSelection_message);
			}
			final RefactoringStatus result = new RefactoringStatus();
			fAdapter.checkInitialToModify(result, fElementSet);
			progress.worked(1);
			
			if (result.hasFatalError()) {
				return result;
			}
			
			searchVariables(rootNode, result);
			progress.worked(2);
			return result;
		}
		finally {
			progress.done();
		}
	}
	
	private void searchVariables(final RAstNode rootNode, final RefactoringStatus result) {
		fVariablesList = new HashMap<IRFrame, Map<String,Variable>>();
		final VariableSearcher searcher = new VariableSearcher();
		try {
			rootNode.acceptInR(searcher);
		}
		catch (final InvocationTargetException e) {}
		for (final Map<String, Variable> map : fVariablesList.values()) {
			for (final Variable var : map.values()) {
				checkVariables(var);
			}
		}
	}
	
	private void checkVariables(final Variable var) {
		Map<String, RenameInRegionRefactoring.Variable> map = null;
		for (final RElementAccess access : var.fAccessList) {
			final RElementAccess next = access.getNextSegment();
			if (next != null && next.getSegmentName() != null
					&& (next.getType() == RElementName.SUB_NAMEDPART
							|| next.getType() == RElementName.SUB_NAMEDSLOT )) {
				if (map == null) {
					map = new HashMap<String, RenameInRegionRefactoring.Variable>();
				}
				Variable sub = map.get(next.getSegmentName());
				if (sub == null) {
					sub = new Variable(var, next.getSegmentName());
					map.put(next.getSegmentName(), sub);
				}
				next.getSegmentName();
				next.getFrame();
				sub.fAccessList.add(next);
			}
		}
		if (map != null) {
			var.fSubVariables = map;
		}
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
			final List<String> variableNames = createChanges(textFileChange, progress.newChild(1));
			
			final Map<String, String> arguments = new HashMap<String, String>();
			final String description = NLS.bind(Messages.RenameInRegion_Descriptor_description, toCommaSeparatedListing(variableNames));
			final IProject resource = fElementSet.getSingleProject();
			final String project = (resource != null) ? resource.getName() : null;
			final String source = (project != null) ? NLS.bind(RefactoringMessages.Common_Source_Project_label, project) : RefactoringMessages.Common_Source_Workspace_label;
			final int flags = 0;
			final String comment = ""; //$NON-NLS-1$
			final CommonRefactoringDescriptor descriptor = new CommonRefactoringDescriptor(
					getIdentifier(), project, description, comment, arguments, flags);
			
			return new RefactoringChange(descriptor,
					Messages.RenameInRegion_label,
					new Change[] { textFileChange });
		}
		catch (final BadLocationException e) {
			throw new CoreException(new Status(IStatus.ERROR, RCore.PLUGIN_ID, "Unexpected error (concurrent change?)", e));
		}
		finally {
			monitor.done();
		}
	}
	
	private List<String> createChanges(final TextFileChange change, final SubMonitor progress) throws BadLocationException {
		final List<String> names = new ArrayList<String>();
		int remaining = fVariablesList.size() + 3;
		progress.setWorkRemaining(remaining); remaining -= 3;
		fSourceUnit.connect(progress.newChild(2));
		try {
			for (final Map<String, Variable> frameList : fVariablesList.values()) {
				progress.setWorkRemaining(remaining--);
				createMainChanges(frameList, change, names);
			}
			return names;
		}
		finally {
			fSourceUnit.disconnect(progress.newChild(1));
		}
	}
	
	private void createMainChanges(final Map<String, Variable> frameList,
			final TextFileChange change, final List<String> names) {
		for (final Variable variable : frameList.values()) {
			if (variable.fNewName != null) {
				final RElementName oldName = RElementName.create(RElementName.MAIN_DEFAULT,
						RRefactoringAdapter.getUnquotedIdentifier(variable.fName) );
				final String oldDisplay = oldName.getDisplayName();
				final boolean isQuoted = (variable.fNewName.charAt(0) == '`');
				final GroupCategorySet set = new GroupCategorySet(new GroupCategory(
						((IRFrameInSource) variable.getParent()).getFrameId() + '$' + variable.fName,
						NLS.bind(Messages.RenameInRegion_Changes_VariableGroup_name, oldDisplay), "")); //$NON-NLS-1$
				final String message = NLS.bind(Messages.RenameInRegion_Changes_ReplaceOccurrence_name,
						oldDisplay);
				
				for (final RElementAccess access : variable.fAccessList) {
					final RAstNode nameNode = access.getNameNode();
					if (nameNode == null) {
						continue;
					}
					final String text = (isQuoted && nameNode.getNodeType() == NodeType.SYMBOL && nameNode.getOperator(0) == RTerminal.SYMBOL) ?
							variable.fNewName : RRefactoringAdapter.getUnquotedIdentifier(variable.fNewName);
					final IRegion nameRegion = RAst.getElementNameRegion(nameNode);
					TextChangeCompatibility.addTextEdit(change, message,
							new ReplaceEdit(nameRegion.getOffset(), nameRegion.getLength(), text), set);
					
				}
				names.add(oldDisplay);
			}
			if (!variable.fSubVariables.isEmpty()) {
				createSubChanges(variable, change, names);
			}
		}
	}
	
	private void createSubChanges(final Variable parent, final TextFileChange change, final List<String> names) {
		final String parentDisplay = RElementName.create(RElementName.MAIN_DEFAULT,
				RRefactoringAdapter.getUnquotedIdentifier(parent.fName) ).getDisplayName() ;
		for (final Variable variable : parent.fSubVariables.values()) {
			if (variable.fNewName != null) {
				final RElementName oldName = RElementName.create(RElementName.MAIN_DEFAULT,
						RRefactoringAdapter.getUnquotedIdentifier(variable.fName) );
				final String oldDisplay = oldName.getDisplayName();
				final boolean isQuoted = (variable.fNewName.charAt(0) == '`');
				final GroupCategorySet set = new GroupCategorySet(new GroupCategory(
						((IRFrameInSource) parent.getParent()).getFrameId() + '$' + parent.fName,
						NLS.bind(Messages.RenameInRegion_Changes_VariableGroup_name, parentDisplay), "")); //$NON-NLS-1$
				final String message = NLS.bind(Messages.RenameInRegion_Changes_ReplaceOccurrenceOf_name,
						oldDisplay, parentDisplay );
				
				for (final RElementAccess access : variable.fAccessList) {
					final RAstNode nameNode = access.getNameNode();
					if (nameNode == null) {
						continue;
					}
					final String text = (isQuoted && nameNode.getNodeType() == NodeType.SYMBOL && nameNode.getOperator(0) == RTerminal.SYMBOL) ?
							variable.fNewName : RRefactoringAdapter.getUnquotedIdentifier(variable.fNewName);
					final IRegion nameRegion = RAst.getElementNameRegion(nameNode);
					TextChangeCompatibility.addTextEdit(change, message,
							new ReplaceEdit(nameRegion.getOffset(), nameRegion.getLength(), text), set);
				}
				names.add(oldDisplay);
			}
		}
	}
	
	
	public static String toCommaSeparatedListing(final Collection<String> strings) {
		if (strings.size() == 0) {
			return ""; //$NON-NLS-1$
		}
		final StringBuilder sb = new StringBuilder();
		final Iterator<String> iterator = strings.iterator();
		sb.append(iterator.next());
		while (iterator.hasNext()) {
			sb.append(", "); //$NON-NLS-1$
			sb.append(iterator.next());
		}
		return sb.toString();
	}
	
}
