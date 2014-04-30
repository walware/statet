/*=============================================================================#
 # Copyright (c) 2007-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.ui.editors;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;

import org.eclipse.jface.text.AbstractDocument;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;

import de.walware.ecommons.ltk.ast.IAstNode;
import de.walware.ecommons.ltk.ast.ICommonAstVisitor;
import de.walware.ecommons.ltk.ui.sourceediting.folding.FoldingAnnotation;
import de.walware.ecommons.ltk.ui.sourceediting.folding.FoldingEditorAddon.FoldingStructureComputationContext;
import de.walware.ecommons.ltk.ui.sourceediting.folding.FoldingProvider;
import de.walware.ecommons.ltk.ui.sourceediting.folding.NodeFoldingProvider;
import de.walware.ecommons.ltk.ui.sourceediting.folding.SimpleFoldingPosition;
import de.walware.ecommons.preferences.IPreferenceAccess;
import de.walware.ecommons.preferences.PreferencesUtil;

import de.walware.statet.r.core.rsource.ast.Block;
import de.walware.statet.r.core.rsource.ast.CForLoop;
import de.walware.statet.r.core.rsource.ast.CIfElse;
import de.walware.statet.r.core.rsource.ast.CRepeatLoop;
import de.walware.statet.r.core.rsource.ast.CWhileLoop;
import de.walware.statet.r.core.rsource.ast.DocuComment;
import de.walware.statet.r.core.rsource.ast.FDef;
import de.walware.statet.r.core.rsource.ast.GenericVisitor;
import de.walware.statet.r.core.rsource.ast.NodeType;
import de.walware.statet.r.core.rsource.ast.RAstNode;
import de.walware.statet.r.core.rsource.ast.SourceComponent;
import de.walware.statet.r.internal.ui.editors.DefaultRFoldingPreferences;


/**
 * Provides code folding for R Scripts
 */
public class RDefaultFoldingProvider implements FoldingProvider, NodeFoldingProvider {
	
	
	public static final String TYPE_RCODE= "de.walware.statet.r.folding.RCode"; //$NON-NLS-1$
	public static final String TYPE_ROXYGEN= "de.walware.statet.r.folding.Roxygen"; //$NON-NLS-1$
	
	
	private static class ElementFinder extends GenericVisitor implements ICommonAstVisitor {
		
		private final FoldingStructureComputationContext context;
		private final FoldingConfiguration config;
		
		public ElementFinder(final FoldingStructureComputationContext ctx, final FoldingConfiguration config) {
			this.context= ctx;
			this.config= config;
		}
		
		private void createRCodeRegion(final int startOffset, final int stopOffset) throws InvocationTargetException {
			if (startOffset == Integer.MIN_VALUE || stopOffset == Integer.MIN_VALUE) {
				return;
			}
			try {
				final AbstractDocument doc= this.context.document;
				final int startLine= doc.getLineOfOffset(startOffset);
				int stopLine= doc.getLineOfOffset(stopOffset);
				final IRegion stopLineInfo= doc.getLineInformation(stopLine);
				if (stopLineInfo.getOffset() + stopLineInfo.getLength() > stopOffset) {
					stopLine--;
				}
				if (stopLine - startLine + 1 >= this.config.minLines) {
					final int offset= doc.getLineOffset(startLine);
					this.context.addFoldingRegion(new FoldingAnnotation(TYPE_RCODE, false,
							new SimpleFoldingPosition(offset, doc.getLineOffset(stopLine) + doc.getLineLength(stopLine) - offset) ));
				}
			}
			catch (final BadLocationException e) {
				throw new InvocationTargetException(e);
			}
		}
		
		private void createRCodeContentRegion(final FDef node,
				final int startOffset, final int stopOffset) throws InvocationTargetException {
			if (startOffset == Integer.MIN_VALUE || stopOffset == Integer.MIN_VALUE) {
				return;
			}
			try {
				final AbstractDocument doc= this.context.document;
				final int startLine= doc.getLineOfOffset(startOffset);
				int stopLine= doc.getLineOfOffset(stopOffset);
				final IRegion stopLineInfo= doc.getLineInformation(stopLine);
				if (stopLineInfo.getOffset() + stopLineInfo.getLength() > stopOffset) {
					stopLine--;
				}
				if (stopLine - (startLine + 1) >= 0) {
					final int offset= doc.getLineOffset(startLine);
					this.context.addFoldingRegion(new FoldingAnnotation(TYPE_RCODE, false,
							new SimpleFoldingPosition(offset, doc.getLineOffset(stopLine) + doc.getLineLength(stopLine) - offset) ));
				}
			}
			catch (final BadLocationException e) {
				throw new InvocationTargetException(e);
			}
		}
		
		private void createRoxygenRegion(final int startOffset, final int stopOffset) throws InvocationTargetException {
			if (startOffset == Integer.MIN_VALUE || stopOffset == Integer.MIN_VALUE) {
				return;
			}
			try {
				final AbstractDocument doc= this.context.document;
				final int startLine= doc.getLineOfOffset(startOffset);
				final int stopLine= doc.getLineOfOffset(stopOffset);
				if (stopLine - startLine + 1 >= this.config.minRoxygenLines) {
					final int offset= doc.getLineOffset(startLine);
					this.context.addFoldingRegion(new FoldingAnnotation(TYPE_ROXYGEN,
							this.context.isInitial && this.config.collapseInitiallyRoxygen,
							new SimpleFoldingPosition(offset, doc.getLineOffset(stopLine) + doc.getLineLength(stopLine) - offset) ));
				}
			}
			catch (final BadLocationException e) {
				throw new InvocationTargetException(e);
			}
		}
		
		@Override
		public void visit(final IAstNode node) throws InvocationTargetException {
			if (node instanceof RAstNode) {
				((RAstNode) node).acceptInR(this);
			}
			else {
				final int count= node.getChildCount();
				for (int i= 0; i < count; i++) {
					final IAstNode child= node.getChild(i);
					if (child instanceof SourceComponent) {
						visit((SourceComponent) child);
					}
				}
			}
		}
		
		@Override
		public void visit(final SourceComponent node) throws InvocationTargetException {
			node.acceptInRChildren(this);
			node.acceptInRComments(this);
		}
		
		@Override
		public void visit(final Block node) throws InvocationTargetException {
			if (this.config.enableOtherBlocks) {
				createRCodeRegion(node.getOffset(), node.getStopOffset());
			}
			node.acceptInRChildren(this);
		}
		
		@Override
		public void visit(final FDef node) throws InvocationTargetException {
			node.getArgsChild().acceptInR(this);
			{
				final RAstNode body= node.getContChild();
				if (body.getNodeType() == NodeType.BLOCK
						&& node.getArgsCloseOffset() != Integer.MIN_VALUE) {
					createRCodeContentRegion(node, node.getArgsCloseOffset(), node.getStopOffset());
					body.acceptInRChildren(this);
				}
				else {
					body.acceptInR(this);
				}
			}
		}
		
		@Override
		public void visit(final CIfElse node) throws InvocationTargetException {
			if (this.config.enableOtherBlocks) {
				node.getCondChild().acceptInR(this);
				{
					final RAstNode body= node.getThenChild();
					if (body.getNodeType() == NodeType.BLOCK) {
						createRCodeRegion(node.getCondCloseOffset(), body.getStopOffset());
						body.acceptInRChildren(this);
					}
					else {
						body.acceptInR(this);
					}
				}
				if (node.hasElse()) {
					final RAstNode body= node.getElseChild();
					if (body.getNodeType() == NodeType.BLOCK) {
						createRCodeRegion(node.getElseOffset(), body.getStopOffset());
						body.acceptInRChildren(this);
					}
					else {
						body.acceptInR(this);
					}
				}
			}
			else {
				node.acceptInRChildren(this);
			}
		}
		
		@Override
		public void visit(final CForLoop node) throws InvocationTargetException {
			if (this.config.enableOtherBlocks) {
				node.getCondChild().acceptInR(this);
				{
					final RAstNode body= node.getContChild();
					if (body.getNodeType() == NodeType.BLOCK) {
						createRCodeRegion(node.getCondCloseOffset(), body.getStopOffset());
						body.acceptInRChildren(this);
					}
					else {
						body.acceptInR(this);
					}
				}
			}
			else {
				node.acceptInRChildren(this);
			}
		}
		
		@Override
		public void visit(final CWhileLoop node) throws InvocationTargetException {
			if (this.config.enableOtherBlocks) {
				node.getCondChild().acceptInR(this);
				{
					final RAstNode body= node.getContChild();
					if (body.getNodeType() == NodeType.BLOCK) {
						createRCodeRegion(node.getCondCloseOffset(), body.getStopOffset());
						body.acceptInRChildren(this);
					}
					else {
						body.acceptInR(this);
					}
				}
			}
			else {
				node.acceptInRChildren(this);
			}
		}
		
		@Override
		public void visit(final CRepeatLoop node) throws InvocationTargetException {
			if (this.config.enableOtherBlocks) {
				{
					final RAstNode body= node.getContChild();
					if (body.getNodeType() == NodeType.BLOCK) {
						createRCodeRegion(node.getOffset(), body.getStopOffset());
						body.acceptInRChildren(this);
					}
					else {
						body.acceptInR(this);
					}
				}
			}
			else {
				node.acceptInRChildren(this);
			}
		}
		
		@Override
		public void visit(final DocuComment node) throws InvocationTargetException {
			if (this.config.enableRoxygen) {
				createRoxygenRegion(node.getOffset(), node.getStopOffset());
			}
		}
		
	}
	
	protected static final class FoldingConfiguration {
		
		public boolean enableOtherBlocks;
		public int minLines;
		
		public boolean enableRoxygen;
		public int minRoxygenLines;
		
		public boolean collapseInitiallyRoxygen;
		
		public boolean isRestoreStateEnabled;
		
	}
	
	
	private FoldingConfiguration fConfig;
	
	
	public RDefaultFoldingProvider() {
	}
	
	
	@Override
	public boolean checkConfig(final Set<String> groupIds) {
		if (groupIds == null || groupIds.contains(DefaultRFoldingPreferences.GROUP_ID)
				|| groupIds.contains(REditorOptions.FOLDING_SHARED_GROUP_ID) ) {
			final FoldingConfiguration config= new FoldingConfiguration();
			final IPreferenceAccess prefs= PreferencesUtil.getInstancePrefs();
			
			config.enableOtherBlocks= prefs.getPreferenceValue(
					DefaultRFoldingPreferences.PREF_OTHERBLOCKS_ENABLED );
			config.minLines= prefs.getPreferenceValue(
					DefaultRFoldingPreferences.PREF_MINLINES_NUM );
			if (config.minLines < 2) {
				config.minLines= 2;
			}
			config.enableRoxygen= prefs.getPreferenceValue(
					DefaultRFoldingPreferences.PREF_ROXYGEN_ENABLED );
			config.minRoxygenLines= prefs.getPreferenceValue(
					DefaultRFoldingPreferences.PREF_ROXYGEN_MINLINES_NUM );
			if (config.minRoxygenLines < 2) {
				config.minLines= 2;
			}
			config.collapseInitiallyRoxygen= prefs.getPreferenceValue(
					DefaultRFoldingPreferences.PREF_ROXYGEN_COLLAPSE_INITIALLY_ENABLED );
			
			config.isRestoreStateEnabled= prefs.getPreferenceValue(
					REditorOptions.FOLDING_RESTORE_STATE_ENABLED_PREF );
			
			this.fConfig= config;
			return true;
		}
		return false;
	}
	
	@Override
	public boolean isRestoreStateEnabled() {
		return this.fConfig.isRestoreStateEnabled;
	}
	
	@Override
	public boolean requiresModel() {
		return false;
	}
	
	@Override
	public void collectRegions(final FoldingStructureComputationContext ctx) throws InvocationTargetException {
		if (ctx.ast.root instanceof RAstNode) {
			((RAstNode) ctx.ast.root).acceptInR(new ElementFinder(ctx, this.fConfig));
		}
	}
	
	@Override
	public ICommonAstVisitor createVisitor(final FoldingStructureComputationContext ctx) {
		return new ElementFinder(ctx, this.fConfig);
	}
	
}
