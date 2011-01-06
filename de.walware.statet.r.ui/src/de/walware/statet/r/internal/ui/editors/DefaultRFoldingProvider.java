/*******************************************************************************
 * Copyright (c) 2007-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.editors;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.AbstractDocument;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;

import de.walware.ecommons.ltk.IModelElement;
import de.walware.ecommons.ltk.IModelElementDelta;
import de.walware.ecommons.ltk.ui.IModelElementInputListener;
import de.walware.ecommons.ltk.ui.sourceediting.ISourceEditor;
import de.walware.ecommons.ltk.ui.sourceediting.ISourceEditorAddon;
import de.walware.ecommons.preferences.IPreferenceAccess;
import de.walware.ecommons.preferences.PreferencesUtil;
import de.walware.ecommons.preferences.SettingsChangeNotifier.ChangeListener;
import de.walware.ecommons.text.TextUtil;

import de.walware.statet.r.core.model.IRSourceUnit;
import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.core.rsource.ast.Block;
import de.walware.statet.r.core.rsource.ast.CForLoop;
import de.walware.statet.r.core.rsource.ast.CIfElse;
import de.walware.statet.r.core.rsource.ast.CRepeatLoop;
import de.walware.statet.r.core.rsource.ast.CWhileLoop;
import de.walware.statet.r.core.rsource.ast.DocuComment;
import de.walware.statet.r.core.rsource.ast.FDef;
import de.walware.statet.r.core.rsource.ast.GenericVisitor;
import de.walware.statet.r.core.rsource.ast.NodeType;
import de.walware.statet.r.core.rsource.ast.RAstInfo;
import de.walware.statet.r.core.rsource.ast.RAstNode;
import de.walware.statet.r.core.rsource.ast.SourceComponent;
import de.walware.statet.r.ui.editors.REditor;


/**
 * Provides code folding for R Scripts
 */
public class DefaultRFoldingProvider implements ISourceEditorAddon, IModelElementInputListener, ChangeListener {
	
	
	private static class ElementFinder extends GenericVisitor {
		
		private final FoldingStructureComputationContext fContext;
		private final FoldingConfiguration fConfig;
		
		public ElementFinder(final FoldingStructureComputationContext ctx, final FoldingConfiguration config) {
			fContext = ctx;
			fConfig = config;
		}
		
		private void createRCodeRegion(final int startOffset, final int stopOffset) throws InvocationTargetException {
			try {
				final AbstractDocument doc = fContext.fDocument;
				final int startLine = doc.getLineOfOffset(startOffset);
				int stopLine = doc.getLineOfOffset(stopOffset);
				final IRegion stopLineInfo = doc.getLineInformation(stopLine);
				if (stopLineInfo.getOffset() + stopLineInfo.getLength() > stopOffset) {
					stopLine--;
				}
				if (stopLine - startLine + 1 >= fConfig.minLines) {
					final int offset = doc.getLineOffset(startLine);
					fContext.addFoldingRegion(
							new Position(offset, doc.getLineOffset(stopLine)+doc.getLineLength(stopLine) - offset),
							new RProjectionAnnotation(RProjectionAnnotation.RCODE, false));
				}
			}
			catch (final BadLocationException e) {
				throw new InvocationTargetException(e);
			}
		}
		
		private void createRoxygenRegion(final int startOffset, final int stopOffset) throws InvocationTargetException {
			try {
				final AbstractDocument doc = fContext.fDocument;
				final int startLine = doc.getLineOfOffset(startOffset);
				final int stopLine = doc.getLineOfOffset(stopOffset);
				if (stopLine - startLine + 1 >= fConfig.minRoxygenLines) {
					final int offset = doc.getLineOffset(startLine);
					fContext.addFoldingRegion(
							new Position(offset, doc.getLineOffset(stopLine)+doc.getLineLength(stopLine)-offset),
							new RProjectionAnnotation(RProjectionAnnotation.ROXYGEN, fContext.fIsInitial && fConfig.collapseInitiallyRoxygen));
				}
			}
			catch (final BadLocationException e) {
				throw new InvocationTargetException(e);
			}
		}
		
		@Override
		public void visit(final SourceComponent node) throws InvocationTargetException {
			node.acceptInRChildren(this);
			node.acceptInRComments(this);
		}
		
		@Override
		public void visit(final Block node) throws InvocationTargetException {
			if (fConfig.enableOtherBlocks) {
				createRCodeRegion(node.getOffset(), node.getStopOffset());
			}
			node.acceptInRChildren(this);
		}
		
		@Override
		public void visit(final FDef node) throws InvocationTargetException {
			node.getArgsChild().acceptInR(this);
			{
				final RAstNode body = node.getContChild();
				if (body.getNodeType() == NodeType.BLOCK) {
					createRCodeRegion(node.getArgsCloseOffset(), node.getStopOffset());
					body.acceptInRChildren(this);
				}
				else {
					body.acceptInR(this);
				}
			}
		}
		
		@Override
		public void visit(final CIfElse node) throws InvocationTargetException {
			if (fConfig.enableOtherBlocks) {
				node.getCondChild().acceptInR(this);
				{
					final RAstNode body = node.getThenChild();
					if (body.getNodeType() == NodeType.BLOCK) {
						createRCodeRegion(node.getCondCloseOffset(), body.getStopOffset());
						body.acceptInRChildren(this);
					}
					else {
						body.acceptInR(this);
					}
				}
				if (node.hasElse()) {
					final RAstNode body = node.getElseChild();
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
			if (fConfig.enableOtherBlocks) {
				node.getCondChild().acceptInR(this);
				{
					final RAstNode body = node.getContChild();
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
			if (fConfig.enableOtherBlocks) {
				node.getCondChild().acceptInR(this);
				{
					final RAstNode body = node.getContChild();
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
			if (fConfig.enableOtherBlocks) {
				{
					final RAstNode body = node.getContChild();
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
			if (fConfig.enableRoxygen) {
				createRoxygenRegion(node.getOffset(), node.getStopOffset());
			}
		}
		
	}
	
	protected static final class FoldingStructureComputationContext {
		
		public final ProjectionAnnotationModel fModel;
		public final AbstractDocument fDocument;
		public final RAstInfo fAst;
		public final SortedMap<Position, RProjectionAnnotation> fTable = new TreeMap<Position, RProjectionAnnotation>(TextUtil.POSITION_COMPARATOR);
		
		public final boolean fIsInitial;
		
		protected FoldingStructureComputationContext(final AbstractDocument document, final RAstInfo ast, final ProjectionAnnotationModel model,
				final boolean isInitial) {
			fModel = model;
			fAst = ast;
			fDocument = document;
			fIsInitial = isInitial;
		}
		
		
		public void addFoldingRegion(final Position position, final RProjectionAnnotation ann) {
			if (!fTable.containsKey(position)) {
				fTable.put(position, ann);
			}
		}
		
	}
	
	protected static final class RProjectionAnnotation extends ProjectionAnnotation {
		
		
		public static final int RCODE = 0x1;
		public static final int ROXYGEN = 0x2;
		
		
		private int rtype;
		
		
		public RProjectionAnnotation(final int type, final boolean collapse) {
			super(collapse);
			this.rtype = type;
		}
		
	}
	
	protected static final class FoldingConfiguration {
		
		public boolean enableOtherBlocks;
		public int minLines;
		
		public boolean enableRoxygen;
		public int minRoxygenLines;
		
		public boolean collapseInitiallyRoxygen;
		
	}
	
	private static final class Input {
		
		private final IRSourceUnit fUnit;
		private boolean fInitilized;
		private long fUpdateStamp;
		
		Input(final IRSourceUnit unit) {
			fUnit = unit;
			fInitilized = false;
			fUpdateStamp = Long.MIN_VALUE;
		}
		
	}
	
	
	private REditor fEditor;
	private FoldingConfiguration fConfig;
	private volatile Input fInput;
	
	
	public DefaultRFoldingProvider() {
	}
	
	
	public void install(final ISourceEditor editor) {
		PreferencesUtil.getSettingsChangeNotifier().addChangeListener(this);
		updateConfig();
		fEditor = (REditor) editor.getAdapter(ISourceEditor.class);
		fEditor.getModelInputProvider().addListener(this);
	}
	
	public void elementChanged(final IModelElement element) {
		final Input input = new Input((IRSourceUnit) element);
		synchronized (this) {
			fInput = input;
		}
	}
	
	public void elementInitialInfo(final IModelElement element) {
		final Input input = fInput;
		if (input.fUnit == element) {
			update(input, -1);
		}
	}
	
	public void elementUpdatedInfo(final IModelElement element, final IModelElementDelta delta) {
		final Input input = fInput;
		if (input.fUnit == element) {
			update(input, delta.getNewAst().stamp);
		}
	}
	
	public void uninstall() {
		PreferencesUtil.getSettingsChangeNotifier().removeChangeListener(this);
		if (fEditor != null) {
			fEditor.getModelInputProvider().removeListener(this);
			fEditor = null;
		}
	}
	
	public void settingsChanged(final Set<String> groupIds) {
		if (groupIds.contains(DefaultRFoldingPreferences.GROUP_ID)) {
			updateConfig();
			final Input input = fInput;
			if (input != null) {
				update(input, -1);
			}
		}
	}
	
	protected void updateConfig() {
		final FoldingConfiguration config = new FoldingConfiguration();
		final IPreferenceAccess prefs = PreferencesUtil.getInstancePrefs();
		config.enableOtherBlocks = prefs.getPreferenceValue(DefaultRFoldingPreferences.PREF_OTHERBLOCKS_ENABLED);
		config.minLines = prefs.getPreferenceValue(DefaultRFoldingPreferences.PREF_MINLINES_NUM);
		if (config.minLines < 2) {
			config.minLines = 2;
		}
		config.enableRoxygen = prefs.getPreferenceValue(DefaultRFoldingPreferences.PREF_ROXYGEN_ENABLED);
		config.minRoxygenLines = prefs.getPreferenceValue(DefaultRFoldingPreferences.PREF_ROXYGEN_MINLINES_NUM);
		if (config.minRoxygenLines < 2) {
			config.minLines = 2;
		}
		config.collapseInitiallyRoxygen = prefs.getPreferenceValue(DefaultRFoldingPreferences.PREF_ROXYGEN_COLLAPSE_INITIALLY_ENABLED);
		fConfig = config;
	}
	
	protected FoldingStructureComputationContext createCtx(final Input input) {
		final REditor editor = fEditor;
		if (editor == null) {
			return null;
		}
		final ProjectionAnnotationModel model = (ProjectionAnnotationModel) editor.getAdapter(ProjectionAnnotationModel.class);
		if (input.fUnit == null || model == null) {
			return null;
		}
		final IProgressMonitor monitor = new NullProgressMonitor();
		final RAstInfo ast = (RAstInfo) input.fUnit.getAstInfo(RModel.TYPE_ID, false, monitor);
		final AbstractDocument document = input.fUnit.getDocument(monitor);
		if (ast == null || document == null || ast.stamp != document.getModificationStamp()) {
			return null;
		}
		return new FoldingStructureComputationContext(document, ast, model, !input.fInitilized);
	}
	
	private void update(final Input input, final long stamp) {
		synchronized(input) {
			if (input.fUnit == null
					|| (stamp != -1 && input.fUpdateStamp == stamp)) { // already uptodate
				return;
			}
			FoldingStructureComputationContext ctx;
			if (input != fInput) {
				return;
			}
			ctx = createCtx(input);
			if (ctx == null) {
				return;
			}
			try {
				ctx.fAst.root.acceptInR(new ElementFinder(ctx, fConfig));
			}
			catch (final InvocationTargetException e) {
				return;
			}
			
			ProjectionAnnotation[] deletions;
			if (ctx.fIsInitial) {
				deletions = null;
				input.fInitilized = true;
			}
			else {
				final List<RProjectionAnnotation> del = new ArrayList<RProjectionAnnotation>();
				for (final Iterator<RProjectionAnnotation> iter = ctx.fModel.getAnnotationIterator(); iter.hasNext(); ) {
					final RProjectionAnnotation existingAnn = iter.next();
					final Position position = ctx.fModel.getPosition(existingAnn);
					final RProjectionAnnotation newAnn = ctx.fTable.remove(position);
					if (newAnn != null) {
						existingAnn.rtype = newAnn.rtype;
					}
					else {
						del.add(existingAnn);
					}
				}
				deletions = del.toArray(new RProjectionAnnotation[del.size()]);
				if (ctx.fDocument.getModificationStamp() != ctx.fAst.stamp || input != fInput) {
					return;
				}
			}
			final LinkedHashMap<RProjectionAnnotation, Position> additions = new LinkedHashMap<RProjectionAnnotation, Position>();
			for (final Iterator<Entry<Position, RProjectionAnnotation>> iter = ctx.fTable.entrySet().iterator(); iter.hasNext(); ) {
				final Entry<Position, RProjectionAnnotation> next = iter.next();
				additions.put(next.getValue(), next.getKey());
			}
			ctx.fModel.modifyAnnotations(deletions, additions, null);
			input.fUpdateStamp = ctx.fAst.stamp;
		}
	}
	
}
