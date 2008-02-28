/*******************************************************************************
 * Copyright (c) 2007-2008 WalWare/StatET-Project (www.walware.de/goto/statet).
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
import java.util.Map.Entry;

import org.eclipse.jface.text.AbstractDocument;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionViewer;

import de.walware.eclipsecommons.ltk.AstInfo;
import de.walware.eclipsecommons.ltk.IModelElement;
import de.walware.eclipsecommons.ltk.IModelElementDelta;
import de.walware.eclipsecommons.ltk.ui.IModelElementInputListener;
import de.walware.eclipsecommons.preferences.IPreferenceAccess;
import de.walware.eclipsecommons.preferences.PreferencesUtil;
import de.walware.eclipsecommons.preferences.Preference.BooleanPref;
import de.walware.eclipsecommons.preferences.Preference.IntPref;
import de.walware.eclipsecommons.preferences.SettingsChangeNotifier.ChangeListener;

import de.walware.statet.base.core.StatetCore;
import de.walware.statet.base.ui.sourceeditors.IFoldingStructureProvider;
import de.walware.statet.base.ui.sourceeditors.StatextEditor1;
import de.walware.statet.r.core.rmodel.IRSourceUnit;
import de.walware.statet.r.core.rsource.ast.Block;
import de.walware.statet.r.core.rsource.ast.CForLoop;
import de.walware.statet.r.core.rsource.ast.CIfElse;
import de.walware.statet.r.core.rsource.ast.CRepeatLoop;
import de.walware.statet.r.core.rsource.ast.CWhileLoop;
import de.walware.statet.r.core.rsource.ast.FDef;
import de.walware.statet.r.core.rsource.ast.GenericVisitor;
import de.walware.statet.r.core.rsource.ast.RAstNode;
import de.walware.statet.r.ui.RUI;
import de.walware.statet.r.ui.editors.REditor;
import de.walware.statet.r.ui.editors.REditorOptions;


/**
 *
 */
public class DefaultRFoldingProvider implements IFoldingStructureProvider, IModelElementInputListener, ChangeListener {
	
	
	public static final String NODE = RUI.PLUGIN_ID + "/editor.r/folding.default"; //$NON-NLS-1$
	
	public static final BooleanPref PREF_OTHERBLOCKS_ENABLED = new BooleanPref(
			NODE, "other_blocks.enabled"); //$NON-NLS-1$
	public static final IntPref PREF_MINLINES_NUM = new IntPref(
			NODE, "min_lines.num"); //$NON-NLS-1$
	
	
	protected static final IRegion createRegion(final FoldingStructureComputationContext ctx, final int startLine, final int endLine) throws BadLocationException {
		final int startOffset = ctx.fDocument.getLineOffset(startLine);
		final int endOffset = ctx.fDocument.getLineOffset(endLine)+ctx.fDocument.getLineLength(endLine);
		return new Region(startOffset, endOffset-startOffset);
	}
	
	private static class ElementFinder extends GenericVisitor {
		
		private final FoldingStructureComputationContext fContext;
		private final FoldingConfiguration fConfig;
		
		public ElementFinder(final FoldingStructureComputationContext ctx, final FoldingConfiguration config) {
			fContext = ctx;
			fConfig = config;
		}
		
		private void create(final int startOffset, final int stopOffset) throws InvocationTargetException {
			try {
				final AbstractDocument doc = fContext.fDocument;
				final int startLine = doc.getLineOfOffset(startOffset);
				final int stopLine = doc.getLineOfOffset(stopOffset);
				if (stopLine - startLine + 1 >= fConfig.minLines) {
					final IRegion region = createRegion(fContext, startLine, stopLine);
					fContext.addFoldingRegion(
							new Position(region.getOffset(), region.getLength()),
							new ProjectionAnnotation());
				}
			}
			catch (final BadLocationException e) {
				throw new InvocationTargetException(e);
			}
			
		}
		
		@Override
		public void visit(final Block node) throws InvocationTargetException {
			final RAstNode parent = node.getParent();
			if (parent != null) {
				switch (parent.getNodeType()) {
				case F_DEF:
				case C_IF:
				case C_FOR:
				case C_WHILE:
				case C_REPEAT:
					break;
					
				default:
					if (fConfig.enableOtherBlocks) {
						create(node.getStartOffset(), node.getStopOffset());
					}
					break;
				}
			}
			node.acceptInRChildren(this);
		}
		
		@Override
		public void visit(final FDef node) throws InvocationTargetException {
			create(node.getArgsCloseOffset(), node.getStopOffset());
			super.visit(node);
		}
		
		@Override
		public void visit(final CIfElse node) throws InvocationTargetException {
			if (fConfig.enableOtherBlocks) {
				create(node.getCondCloseOffset(), node.getStopOffset());
			}
			super.visit(node);
		}
		
		@Override
		public void visit(final CForLoop node) throws InvocationTargetException {
			if (fConfig.enableOtherBlocks) {
				create(node.getCondCloseOffset(), node.getStopOffset());
			}
			super.visit(node);
		}
		
		@Override
		public void visit(final CWhileLoop node) throws InvocationTargetException {
			if (fConfig.enableOtherBlocks) {
				create(node.getCondCloseOffset(), node.getStopOffset());
			}
			super.visit(node);
		}
		
		@Override
		public void visit(final CRepeatLoop node) throws InvocationTargetException {
			if (fConfig.enableOtherBlocks) {
				create(node.getStartOffset()+6, node.getStopOffset());
			}
			super.visit(node);
		}
		
	}
	
	protected static final class FoldingStructureComputationContext {
		
		public final ProjectionAnnotationModel fModel;
		public final AbstractDocument fDocument;
		public final AstInfo<RAstNode> fAst;
		public final LinkedHashMap<Position, ProjectionAnnotation> fTable = new LinkedHashMap<Position, ProjectionAnnotation>();
		
		public final boolean fIsInitial;
		
		protected FoldingStructureComputationContext(final AbstractDocument document, final AstInfo<RAstNode> ast, final ProjectionAnnotationModel model,
				final boolean isInitial) {
			fModel = model;
			fAst = ast;
			fDocument = document;
			fIsInitial = isInitial;
		}
		
		
		public void addFoldingRegion(final Position position, final ProjectionAnnotation ann) {
			if (!fTable.containsKey(position)) {
				fTable.put(position, ann);
			}
		}
		
	}
	
	protected static final class FoldingConfiguration {
		
		public boolean enableOtherBlocks;
		public int minLines;
		
	}
	
	private class Input {
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
	
	
	public void install(final StatextEditor1 editor, final ProjectionViewer viewer) {
		StatetCore.getSettingsChangeNotifier().addChangeListener(this);
		updateConfig();
		fEditor = (REditor) editor;
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
		StatetCore.getSettingsChangeNotifier().removeChangeListener(this);
		if (fEditor != null) {
			fEditor.getModelInputProvider().removeListener(this);
			fEditor = null;
		}
	}
	
	public void settingsChanged(final Set<String> contexts) {
		if (contexts.contains(REditorOptions.CONTEXT_ID)) {
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
		config.enableOtherBlocks = prefs.getPreferenceValue(PREF_OTHERBLOCKS_ENABLED);
		config.minLines = prefs.getPreferenceValue(PREF_MINLINES_NUM);
		if (config.minLines < 2) {
			config.minLines = 3;
		}
		fConfig = config;
	}
	
	protected FoldingStructureComputationContext createCtx(final Input input) {
		final ProjectionAnnotationModel model = (ProjectionAnnotationModel) fEditor.getAdapter(ProjectionAnnotationModel.class);
		if (input.fUnit == null || model == null) {
			return null;
		}
		final AstInfo<RAstNode> ast = (AstInfo<RAstNode>) input.fUnit.getAstInfo("r", false, null); //$NON-NLS-1$
		final AbstractDocument document = input.fUnit.getDocument();
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
				final Iterator<ProjectionAnnotation> iter = ctx.fModel.getAnnotationIterator();
				final List<ProjectionAnnotation> del = new ArrayList<ProjectionAnnotation>();
				while (iter.hasNext()) {
					final ProjectionAnnotation ann = iter.next();
					final Position position = ctx.fModel.getPosition(ann);
					if (ctx.fTable.remove(position) == null) {
						del.add(ann);
					}
				}
				deletions = del.toArray(new ProjectionAnnotation[del.size()]);
				if (ctx.fDocument.getModificationStamp() != ctx.fAst.stamp || input != fInput) {
					return;
				}
			}
			final LinkedHashMap<ProjectionAnnotation, Position> additions = new LinkedHashMap<ProjectionAnnotation, Position>();
			final Iterator<Entry<Position, ProjectionAnnotation>> iter = ctx.fTable.entrySet().iterator();
			while (iter.hasNext()) {
				final Entry<Position, ProjectionAnnotation> next = iter.next();
				additions.put(next.getValue(), next.getKey());
			}
			ctx.fModel.modifyAnnotations(deletions, additions, null);
			input.fUpdateStamp = ctx.fAst.stamp;
		}
	}
	
}
