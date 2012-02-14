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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.rules.IPartitionTokenScanner;
import org.eclipse.jface.text.rules.IToken;

import de.walware.ecommons.text.IPartitionScannerCallbackExt;
import de.walware.ecommons.text.Partitioner;

import de.walware.statet.r.internal.sweave.SweavePlugin;


/**
 * IPartitionTokenScanner supporting multiple token scanners, one for each category.
 */
public class MultiCatPartitionScanner implements IPartitionTokenScanner, IPartitionScannerCallbackExt {
	
	
	static final String ID_PREFIX = "cat:"; //$NON-NLS-1$
	
	
	static class CatMarker extends Position {
		
		int type;
		private int controlLength;
		
		public CatMarker(final int offset, final int length, final int type, final int catControlEnd) {
			super(offset, length);
			this.type = type;
			setControlEnd(catControlEnd);
		}
		
		final void setControlEnd(final int offset) {
			this.controlLength = offset - this.offset;
		}
		
		public final int getControlEnd() {
			return this.offset + this.controlLength;
		}
	}
	
	static int search(final Position[] positions, final int offset) {
		for (int i = positions.length-1; i >= 0; i--) {
			if (positions[i].offset < offset) {
				return i;
			}
		}
		return -1;
	}
	
	
	private final String fId;
	private final ICatPartitionTokenScanner[] fCatScanners;
	private int fCurrentCatScannerType;
	private ICatPartitionTokenScanner fCurrentScanner;
	private String[] fContentTypes;
	private ICatPartitionTokenScanner[] fContentTypeScanners;
	
	private IDocument fDocument;
	private int fRangeOffset;
	private int fRangeEnd;
	private Position[] fCatMarkers;
	private int fCatMarkerIdx;
	
	
	public MultiCatPartitionScanner(final String id,
			final ICatPartitionTokenScanner scanner1, final ICatPartitionTokenScanner scanner2) {
		fId = ID_PREFIX+id;
		fCatScanners = new ICatPartitionTokenScanner[] {
				scanner1, scanner2
		};
		
		final List<String[]> contentTypes = new ArrayList<String[]>();
		int count = 0;
		for (int i = 0; i < fCatScanners.length; i++) {
			fCatScanners[i].setParent(this);
			final String[] types = fCatScanners[i].getContentTypes();
			contentTypes.add(types);
			count += types.length;
		}
		fContentTypes = new String[count];
		fContentTypeScanners = new ICatPartitionTokenScanner[count];
		count = 0;
		for (int i = 0; i < fCatScanners.length; i++) {
			final String[] types = contentTypes.get(i);
			for (int j = 0; j < types.length; j++, count++) {
				fContentTypes[count] = types[j];
				fContentTypeScanners[count] = fCatScanners[i];
			}
		}
	}
	
	@Override
	public void setPartitionerCallback(final Partitioner partitioner) {
		for (final IPartitionTokenScanner scanner : fContentTypeScanners) {
			if (scanner instanceof IPartitionScannerCallbackExt) {
				((IPartitionScannerCallbackExt) scanner).setPartitionerCallback(partitioner);
			}
		}
	}
	
	
	public String getId() {
		return fId;
	}
	
	
	@Override
	public void setRange(final IDocument document, final int offset, final int length) {
		setPartialRange(document, offset, length, null, -1);
	}
	
	@Override
	public void setPartialRange(final IDocument document, final int offset, final int length, final String contentType, int partitionOffset) {
		if (partitionOffset < 0) {
			partitionOffset = offset;
		}
		fDocument = document;
		fRangeOffset = offset;
		fRangeEnd = offset+length;
		
		// initial scanner
		fCurrentScanner = null;
//		if (contentType != null && contentType != IDocument.DEFAULT_CONTENT_TYPE) {
//			fCurrentScanner = getScanner(contentType);
//		}
		try {
			fCatMarkers = fDocument.getPositions(fId);
			if (fCurrentScanner == null) {
				fCurrentCatScannerType = 0;
				fCatMarkerIdx = search(fCatMarkers, partitionOffset);
				if (fCatMarkerIdx >= 0) {
					fCurrentCatScannerType = ((CatMarker) fCatMarkers[fCatMarkerIdx]).type;
					fCatMarkerIdx++;
				}
				else {
					fCatMarkerIdx = 0;
				}
				fCurrentScanner = fCatScanners[fCurrentCatScannerType];
			}
		}
		catch (final BadPositionCategoryException e) {
			SweavePlugin.logError(-1, "Error when read category markers", e);
			fCatMarkerIdx = -1;
			fCurrentScanner = fCatScanners[0];
		}
		fCurrentScanner.setPartialRange(document, offset, length, contentType, partitionOffset);
	}
	
	protected final ICatPartitionTokenScanner getScanner(final String contentType) {
		for (int i = 0; i < fContentTypes.length; i++) {
			if (contentType.equals(fContentTypes[i])) {
				return fContentTypeScanners[i];
			}
		}
		return null;
	}
	
	@Override
	public IToken nextToken() {
		if (!fCurrentScanner.isInCat()) {
			fCurrentCatScannerType ++;
			if (fCurrentCatScannerType >= fCatScanners.length) {
				fCurrentCatScannerType = 0;
			}
			final int offset = fCurrentScanner.getTokenOffset() + fCurrentScanner.getTokenLength();
			fCurrentScanner = fCatScanners[fCurrentCatScannerType];
			fCurrentScanner.setRange(fDocument, offset, fRangeEnd-offset);
		}
		final IToken token = fCurrentScanner.nextToken();
		cleanMarkers(fCurrentScanner.getTokenOffset()+fCurrentScanner.getTokenLength());
		
//		System.out.println(token.getData() + " (" + fCurrentScanner.getTokenOffset() + "," + fCurrentScanner.getTokenLength() + ")");
		
		return token;
	}
	
	protected void cleanMarkers(final int end) {
		if (fCatMarkerIdx < 0) {
			return;
		}
		Position marker = null;
		while (fCatMarkerIdx < fCatMarkers.length
				&& (marker = fCatMarkers[fCatMarkerIdx]) != null
				&& (marker.offset < end)) {
			fCatMarkerIdx++;
			marker.delete();
			
			try {
				fDocument.removePosition(fId, marker);
			} catch (final BadPositionCategoryException e) {
			}
		}
	}
	
	public void setControlMarker(final int startOffset, final int startLength, final int stopOffset, final int type) {
		if (fCatMarkerIdx < 0) {
			return;
		}
		Position marker = null;
		while (fCatMarkerIdx < fCatMarkers.length
				&& (marker = fCatMarkers[fCatMarkerIdx]) != null
				&& (marker.offset < startOffset)) {
			fCatMarkerIdx++;
			marker.delete();
			fDocument.removePosition(marker);
		}
		if (fCatMarkerIdx < fCatMarkers.length
				&& (marker = fCatMarkers[fCatMarkerIdx]) != null
				&& (marker.offset == startOffset)) {
			fCatMarkerIdx++;
			final CatMarker catMarker = (CatMarker) marker;
			catMarker.length = startLength;
			catMarker.type = type;
			catMarker.setControlEnd(stopOffset);
			return;
		}
		
		try {
			marker = new CatMarker(startOffset, startLength, type, stopOffset);
			fDocument.addPosition(fId, marker);
		}
		catch (final BadLocationException e) {
			SweavePlugin.logError(-1, "Error when update category markers", e);
		}
		catch (final BadPositionCategoryException e) {
			SweavePlugin.logError(-1, "Error when update category markers", e);
		}
	}
	
	@Override
	public int getTokenOffset() {
		return fCurrentScanner.getTokenOffset();
	}
	
	@Override
	public int getTokenLength() {
		return fCurrentScanner.getTokenLength();
	}
	
}
