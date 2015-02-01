/*=============================================================================#
 # Copyright (c) 2009-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.ui.sourceediting;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.ui.PartInitException;

import de.walware.ecommons.ltk.AstInfo;
import de.walware.ecommons.ltk.LTK;
import de.walware.ecommons.ltk.ast.AstSelection;
import de.walware.ecommons.ltk.ast.IAstNode;
import de.walware.ecommons.ltk.core.model.ISourceElement;
import de.walware.ecommons.ltk.core.model.ISourceUnit;
import de.walware.ecommons.ltk.ui.sourceediting.ISourceEditor;
import de.walware.ecommons.ltk.ui.sourceediting.actions.AbstractOpenDeclarationHandler;

import de.walware.statet.r.core.model.IRModelInfo;
import de.walware.statet.r.core.model.IRModelManager;
import de.walware.statet.r.core.model.IRSourceUnit;
import de.walware.statet.r.core.model.RElementAccess;
import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.core.rlang.RTokens;
import de.walware.statet.r.core.rsource.IRDocumentPartitions;
import de.walware.statet.r.core.rsource.RHeuristicTokenScanner;
import de.walware.statet.r.core.rsource.ast.NodeType;
import de.walware.statet.r.core.rsource.ast.RAstNode;


public class ROpenDeclarationHandler extends AbstractOpenDeclarationHandler {
	
	
	public static RElementAccess searchAccess(final ISourceEditor editor, final IRegion region) {
		try {
			final IDocument document= editor.getViewer().getDocument();
			final RHeuristicTokenScanner scanner= (RHeuristicTokenScanner) LTK.getModelAdapter(
					editor.getModelTypeId(), RHeuristicTokenScanner.class );
			if (scanner == null) {
				return null;
			}
			final ITypedRegion partition= TextUtilities.getPartition(document,
					scanner.getPartitioningConfig().getPartitioning(), region.getOffset(), false);
			final ISourceUnit su= editor.getSourceUnit();
			if (su instanceof IRSourceUnit
					&& region.getOffset() < document.getLength()
					&& ( (scanner.getPartitioningConfig().getDefaultPartitionConstraint().matches(partition.getType())
							&& !RTokens.isRobustSeparator(document.getChar(region.getOffset()), false) )
						|| partition.getType() == IRDocumentPartitions.R_QUOTED_SYMBOL
						|| partition.getType() == IRDocumentPartitions.R_STRING )) {
				
				final IRModelInfo modelInfo= (IRModelInfo) su.getModelInfo(RModel.R_TYPE_ID,
						IRModelManager.MODEL_FILE, new NullProgressMonitor() );
				if (modelInfo != null) {
					final AstInfo astInfo= modelInfo.getAst();
					final AstSelection astSelection= AstSelection.search(astInfo.root,
							region.getOffset(), region.getOffset() + region.getLength(),
							AstSelection.MODE_COVERING_SAME_LAST );
					final IAstNode covering= astSelection.getCovering();
					if (covering instanceof RAstNode) {
						final RAstNode node= (RAstNode) covering;
						if (node.getNodeType() == NodeType.SYMBOL || node.getNodeType() == NodeType.STRING_CONST) {
							RAstNode current= node;
							do {
								final List<Object> attachments= current.getAttachments();
								for (final Object attachment : attachments) {
									if (attachment instanceof RElementAccess) {
										final RElementAccess access= (RElementAccess) attachment;
										if (access.getNameNode() == node) {
											return access;
										}
									}
								}
								current= current.getRParent();
							} while (current != null);
						}
					}
				}
			}
		}
		catch (final BadLocationException e) {
		}
		return null;
	}
	
	
	public ROpenDeclarationHandler() {
	}
	
	
	@Override
	public boolean execute(final ISourceEditor editor, final IRegion selection) {
		final RElementAccess access= searchAccess(editor, selection);
		if (access != null) {
			try {
				final List<ISourceElement> list= RModel.searchDeclaration(access, (IRSourceUnit) editor.getSourceUnit());
				final ROpenDeclaration open= new ROpenDeclaration();
				final ISourceElement element= open.selectElement(list, editor.getWorkbenchPart());
				if (element != null) {
					open.open(element, true);
					return true;
				}
			}
			catch (final PartInitException e) {
				logError(e, access.getDisplayName());
			}
			catch (final CoreException e) {
				return true; // cancelled
			}
		}
		return false;
	}
	
}
