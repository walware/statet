/*******************************************************************************
 * Copyright (c) 2009-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.sweave.model;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.AbstractDocument;
import org.eclipse.jface.text.ITypedRegion;

import de.walware.ecommons.ltk.AstInfo;
import de.walware.ecommons.ltk.IModelManager;
import de.walware.ecommons.ltk.ISourceUnit;
import de.walware.ecommons.ltk.SourceContent;
import de.walware.ecommons.ltk.SourceUnitModelContainer;
import de.walware.ecommons.text.SourceParseInput;
import de.walware.ecommons.text.StringParseInput;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.rsource.ast.RAst;
import de.walware.statet.r.core.rsource.ast.RScanner;
import de.walware.statet.r.internal.sweave.Rweave;


public class RweaveTexSuModelContainer extends SourceUnitModelContainer {
	
	
	public RweaveTexSuModelContainer(final ISourceUnit su) {
		super(su);
	}
	
	
	@Override
	protected IModelManager getModelManager() {
		return RCore.getRModelManager();
	}
	
	
	@Override
	public AstInfo getAstInfo(final boolean ensureSync, final IProgressMonitor monitor) {
		if (ensureSync) {
			reconcileAst(IModelManager.AST, monitor);
		}
		return getCurrentAst();
	}
	
	
	public void reconcileAst(final int reconcileLevel, final IProgressMonitor monitor) {
		synchronized (this) {
			final ISourceUnit su = getSourceUnit();
			final AbstractDocument document = su.getDocument(monitor);
			SourceContent content;
			ITypedRegion[] cats;
			do {
				content = getSourceUnit().getContent(monitor);
				cats = Rweave.R_TEX_CAT_UTIL.getCats(document, 0, content.text.length());
			} while (document.getModificationStamp() != content.stamp);
			
			final AstInfo old = getCurrentAst();
			if (old == null || old.stamp != content.stamp) {
				final SourceParseInput input = new StringParseInput(content.text);
				SweaveDocElement root = new SweaveDocElement();
				final SweaveAstInfo ast = new SweaveAstInfo(RAst.LEVEL_MODEL_DEFAULT, content.stamp,
						root );
				final RScanner scanner = new RScanner(input, ast);
				
				root.fStartOffset = root.fStopOffset = 0;
				for (int i = 0; i < cats.length; i++) {
					if (cats[i].getType() == Rweave.CONTROL_CAT) {
						final RChunkNode rChunk = new RChunkNode(root);
						root.fChildren.add(rChunk);
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
						final OtherNode other = new OtherNode(root);
						root.fChildren.add(other);
						other.fStartOffset = cats[i].getOffset();
						other.fStopOffset = cats[i].getOffset() + cats[i].getLength();
					}
				}
				if (root.fChildren.size() > 0) {
					root.fStopOffset = root.fChildren.get(root.fChildren.size()-1).getStopOffset();
				}
				
				setAst(ast);
			}
		}
	}
	
}
