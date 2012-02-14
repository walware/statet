/*******************************************************************************
 * Copyright (c) 2007-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.sweave.text;

import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.DocumentRewriteSession;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IPositionUpdater;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;

import de.walware.ecommons.text.Partitioner;

import de.walware.statet.r.internal.sweave.SweavePlugin;


/**
 * Partitioner for partitioning with categories.
 * Used together with {@link MultiCatPartitionScanner}.
 */
public class CatPartitioner extends Partitioner {
	
	
	public static final String CONTROL_CAT = "control"; //$NON-NLS-1$
	
	
	private class CatPositionUpdater implements IPositionUpdater {
		
		public boolean active;
		
		public CatPositionUpdater() {
		}
		
		@Override
		public void update(final DocumentEvent event) {
			if (active) {
				updatePositions(event);
			}
		}
	}
	
	
	private final String fId;
	private final CatPositionUpdater fUpdater;
	
	
	/**
	 * @param scanner
	 * @param legalContentTypes
	 */
	public CatPartitioner(final MultiCatPartitionScanner scanner, final String[] legalContentTypes) {
		super(scanner, legalContentTypes);
		
		fId = scanner.getId();
		fUpdater = new CatPositionUpdater();
	}
	
	@Override
	public void connect(final IDocument document, final boolean delayInitialization) {
		document.addPositionCategory(fId);
		document.addPositionUpdater(fUpdater);
		fUpdater.active = false;
		super.connect(document, delayInitialization);
	}
	
	@Override
	public void disconnect() {
		if (fDocument != null) {
			try {
				fDocument.removePositionUpdater(fUpdater);
				fDocument.removePositionCategory(fId);
			} catch (final BadPositionCategoryException e) {
			}
		}
		super.disconnect();
	}
	
	@Override
	public IRegion documentChanged2(final DocumentEvent event) {
		if (getActiveRewriteSession() == null) {
			updatePositions(event);
		}
		return super.documentChanged2(event);
	}
	
	@Override
	public void startRewriteSession(final DocumentRewriteSession session) throws IllegalStateException {
		super.startRewriteSession(session);
		fUpdater.active = true;
	}
	
	@Override
	public void stopRewriteSession(final DocumentRewriteSession session) {
		fUpdater.active = false;
		super.stopRewriteSession(session);
	}
	
	protected void updatePositions(final DocumentEvent event) {
		final int eventOffset = event.getOffset();
		final int deleteOffset = eventOffset+event.getLength();
		final int diff = (event.getText() == null ? 0 :
				event.getText().length()) - event.getLength();
		final IDocument doc = event.getDocument();
		
		Position[] category;
		try {
			category = doc.getPositions(fId);
			ITER_POS : for (int i = 0; i < category.length; i++) {
				final Position pos = category[i];
				if (pos.offset + pos.length <= eventOffset) {
					continue ITER_POS;
				}
				if (pos.offset <= deleteOffset || pos.isDeleted) {
					pos.delete();
					doc.removePosition(fId, pos);
					continue ITER_POS;
				}
				pos.offset += diff;
			}
		} catch (final BadPositionCategoryException e) {
			SweavePlugin.logError(-1, "Error when updateting cat positions.", e);
		}
	}
	
}
