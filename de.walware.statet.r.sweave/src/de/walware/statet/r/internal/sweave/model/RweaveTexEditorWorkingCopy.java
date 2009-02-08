/*******************************************************************************
 * Copyright (c) 2007-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.sweave.model;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.text.AbstractDocument;
import org.eclipse.jface.text.ITypedRegion;

import de.walware.ecommons.ltk.AstInfo;
import de.walware.ecommons.ltk.ECommonsLTK;
import de.walware.ecommons.ltk.GenericSourceUnitWorkingCopy;
import de.walware.ecommons.ltk.IProblemRequestor;
import de.walware.ecommons.ltk.ISourceUnit;
import de.walware.ecommons.ltk.ISourceUnitModelInfo;
import de.walware.ecommons.ltk.IWorkingBuffer;
import de.walware.ecommons.ltk.SourceContent;
import de.walware.ecommons.ltk.SourceDocumentRunnable;
import de.walware.ecommons.ltk.WorkingContext;
import de.walware.ecommons.ltk.ast.IAstNode;
import de.walware.ecommons.ltk.ui.FileBufferWorkingBuffer;
import de.walware.ecommons.text.SourceParseInput;
import de.walware.ecommons.text.StringParseInput;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.core.rsource.ast.RAst;
import de.walware.statet.r.core.rsource.ast.RScanner;
import de.walware.statet.r.internal.sweave.Rweave;


public class RweaveTexEditorWorkingCopy extends GenericSourceUnitWorkingCopy {
	
	
	private AstInfo<SweaveDocElement> fRAst;
	private final Object fModelLock = new Object();
	
	
	public RweaveTexEditorWorkingCopy(final ISourceUnit from) {
		super(from);
	}
	
	
	public WorkingContext getWorkingContext() {
		return ECommonsLTK.EDITOR_CONTEXT;
	}
	
	@Override
	protected IWorkingBuffer createWorkingBuffer(final SubMonitor progress) {
		return new FileBufferWorkingBuffer(this);
	}
	
	@Override
	protected final void register() {
		RCore.getRModelManager().registerDependentUnit(this);
	}
	
	@Override
	protected final void unregister() {
		RCore.getRModelManager().registerDependentUnit(this);
	}
	
	public void syncExec(final SourceDocumentRunnable runnable)
			throws InvocationTargetException {
		FileBufferWorkingBuffer.syncExec(runnable);
	}
	
	
	public AstInfo<? extends IAstNode> getAstInfo(final String type, final boolean ensureSync, final IProgressMonitor monitor) {
		if (type == null || type.equals(RModel.TYPE_ID)) {
			if (ensureSync) {
				reconcileR(0, monitor);
			}
			return fRAst;
		}
		return null;
	}
	
	public AstInfo<SweaveDocElement> reconcileR(final int level, final IProgressMonitor monitor) {
		synchronized (fModelLock) {
			final AbstractDocument document = getDocument(monitor);
			SourceContent content;
			ITypedRegion[] cats;
			do {
				content = getContent(monitor);
				cats = Rweave.R_TEX_CAT_UTIL.getCats(document, 0, content.text.length());
			} while (document.getModificationStamp() != content.stamp);
			
			final AstInfo<SweaveDocElement> old = fRAst;
			if (old == null || old.stamp != content.stamp) {
				final SourceParseInput input = new StringParseInput(content.text);
				final AstInfo<SweaveDocElement> ast = new AstInfo<SweaveDocElement>(RAst.LEVEL_MODEL_DEFAULT, content.stamp);
				final RScanner scanner = new RScanner(input, ast);
				
				ast.root = new SweaveDocElement();
				ast.root.fStartOffset = ast.root.fStopOffset = 0;
				for (int i = 0; i < cats.length; i++) {
					if (cats[i].getType() == Rweave.CONTROL_CAT) {
						final RChunkNode rChunk = new RChunkNode(ast.root);
						ast.root.fChildren.add(rChunk);
						rChunk.fStartOffset = cats[i].getOffset();
						rChunk.fStopOffset = cats[i].getOffset() + cats[i].getLength();
						if (++i < cats.length && cats[i].getType() == Rweave.R_CAT) {
							rChunk.fRSource = scanner.scanSourceRange(rChunk, cats[i].getOffset(), cats[i].getLength());
							rChunk.fStopOffset = rChunk.fRSource.getStopOffset();
							if (++i < cats.length && cats[i].getType() == Rweave.CONTROL_CAT) {
								rChunk.fStopOffset = cats[i].getOffset() + cats[i].getLength();
							}
						}
						if (i >= cats.length) {
							break;
						}
					}
					if (cats[i].getType() == Rweave.TEX_CAT && cats[i].getLength() > 0) {
						final OtherNode other = new OtherNode(ast.root);
						ast.root.fChildren.add(other);
						other.fStartOffset = cats[i].getOffset();
						other.fStopOffset = cats[i].getOffset() + cats[i].getLength();
					}
				}
				if (ast.root.fChildren.size() > 0) {
					ast.root.fStopOffset = ast.root.fChildren.get(ast.root.fChildren.size()-1).getStopOffset();
				}
				
				fRAst = ast;
				return ast;
			}
			else {
				return old;
			}
		}
	}
	
	public ISourceUnitModelInfo getModelInfo(final String type, final int syncLevel, final IProgressMonitor monitor) {
		return null;
	}
	
	public IProblemRequestor getProblemRequestor() {
		return null;
	}
	
}
