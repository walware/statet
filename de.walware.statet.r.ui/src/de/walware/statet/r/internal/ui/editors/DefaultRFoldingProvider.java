/*******************************************************************************
 * Copyright (c) 2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.editors;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.jface.text.AbstractDocument;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

import de.walware.eclipsecommons.ltk.AstAbortVisitException;
import de.walware.eclipsecommons.ltk.AstInfo;
import de.walware.eclipsecommons.ltk.ElementChangedEvent;
import de.walware.eclipsecommons.ltk.IElementChangedListener;

import de.walware.statet.ext.ui.editors.IFoldingStructureProvider;
import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.rmodel.IRSourceUnit;
import de.walware.statet.r.core.rsource.ast.FDef;
import de.walware.statet.r.core.rsource.ast.GenericVisitor;
import de.walware.statet.r.core.rsource.ast.RAstNode;
import de.walware.statet.r.ui.editors.REditor;


/**
 *
 */
public class DefaultRFoldingProvider implements IFoldingStructureProvider {
	
	
	protected static final IRegion createRegion(FoldingStructureComputationContext ctx, int startLine, int endLine) throws BadLocationException {
		final int startOffset = ctx.fDocument.getLineOffset(startLine);
		final int endOffset = ctx.fDocument.getLineOffset(endLine)+ctx.fDocument.getLineLength(endLine);
		return new Region(startOffset, endOffset-startOffset);
	}
	
	private static class ElementFinder extends GenericVisitor {
		
		private final FoldingStructureComputationContext fContext;
		
		public ElementFinder(FoldingStructureComputationContext ctx) {
			fContext = ctx;
		}
		
		@Override
		public void visit(FDef node) {
			try {
				int startLine = fContext.fDocument.getLineOfOffset(node.getArgsChild().getStopOffset());
				int stopLine = fContext.fDocument.getLineOfOffset(node.getContChild().getStopOffset());
				if (stopLine - startLine >= 3) {
					IRegion region = createRegion(fContext, startLine, stopLine);
					fContext.addFoldingRegion(
							new Position(region.getOffset(), region.getLength()),
							new ProjectionAnnotation());
				}
			} catch (BadLocationException e) {
				throw new AstAbortVisitException(e);
			}
			node.acceptInChildren(this);
		}
	}
	
	private class ElementChangeListener implements IElementChangedListener {
		public void elementChanged(ElementChangedEvent event) {
			Input input = fInput;
			if (event.delta.getModelElement() == input.fUnit) {
				synchronized (input) {
					update(input);
				}
			}
		}
	}

	protected static final class FoldingStructureComputationContext {
		
		public final ProjectionAnnotationModel fModel;
		public final AbstractDocument fDocument;
		public final AstInfo<RAstNode> fAst;
		public final LinkedHashMap<Position, ProjectionAnnotation> fTable = new LinkedHashMap<Position, ProjectionAnnotation>();
		
		public final boolean fIsInitial;
		
		protected FoldingStructureComputationContext(AbstractDocument document, AstInfo<RAstNode> ast, ProjectionAnnotationModel model,
				boolean isInitial) {
			fModel = model;
			fAst = ast;
			fDocument = document;
			fIsInitial = isInitial;
		}
		
		
		public void addFoldingRegion(Position position, ProjectionAnnotation ann) {
			if (!fTable.containsKey(position)) {
				fTable.put(position, ann);
			}
		}
		
	}
	
	private class Input {
		private final IRSourceUnit fUnit;
		private boolean fInitilized;
		
		Input(IRSourceUnit unit) {
			fUnit = unit;
			fInitilized = false;
		}
	}
	
	
	private REditor fEditor;
	private Input fInput;
	private ElementChangeListener fListener;
	

	public void install(ITextEditor editor, ProjectionViewer viewer) {
		fEditor = (REditor) editor;
		fListener = new ElementChangeListener();
		RCore.addRElementChangedListener(fListener, RCore.PRIMARY_WORKING_CONTEXT);
	}
	
	public void setEditorInput(IEditorInput editorInput) {
		Input input = new Input(fEditor.getRResourceUnit());
		synchronized (input) {
			fInput = input;
			update(input);
		}
	}
	
	public void uninstall() {
		if (fListener != null) {
			RCore.removeRElementChangedListener(fListener, RCore.PRIMARY_WORKING_CONTEXT);
			fListener = null;
		}
		fEditor = null;
	}
	
	protected FoldingStructureComputationContext createCtx(Input input) {
		ProjectionAnnotationModel model = (ProjectionAnnotationModel) fEditor.getAdapter(ProjectionAnnotationModel.class);
		if (input.fUnit == null || model == null) {
			return null;
		}
		AstInfo<RAstNode> ast = input.fUnit.getAstInfo(false, null);
		AbstractDocument document = input.fUnit.getDocument();
		if (ast == null || document == null || ast.stamp != document.getModificationStamp()) {
			return null;
		}
		return new FoldingStructureComputationContext(document, ast, model, !input.fInitilized);
	}
	
	private void update(Input input) {
		FoldingStructureComputationContext ctx;
		input.fUnit.connect();
		try {
			if (input != fInput) {
				return;
			}
			ctx = createCtx(input);
			if (ctx == null) {
				return;
			}
		}
		finally {
			input.fUnit.disconnect();
		}
		try {
			ctx.fAst.root.accept(new ElementFinder(ctx));
		}
		catch (AstAbortVisitException e) {
			return;
		}
		
		ProjectionAnnotation[] deletions;
		if (ctx.fIsInitial) {
			deletions = null;
			input.fInitilized = true;
		}
		else {
			Iterator iter = ctx.fModel.getAnnotationIterator();
			List<ProjectionAnnotation> del = new ArrayList<ProjectionAnnotation>();
			while (iter.hasNext()) {
				ProjectionAnnotation ann = (ProjectionAnnotation) iter.next();
				Position position = ctx.fModel.getPosition(ann);
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
		Iterator<Entry<Position, ProjectionAnnotation>> iter = ctx.fTable.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<Position, ProjectionAnnotation> next = iter.next();
			additions.put(next.getValue(), next.getKey());
		}
		ctx.fModel.modifyAnnotations(deletions, additions, null);
	}

}
