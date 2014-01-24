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

package de.walware.statet.r.sweave.text;

import static de.walware.statet.r.sweave.text.CatPartitioner.CONTROL_CAT;

import java.util.ArrayList;

import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TypedRegion;

import de.walware.statet.r.internal.sweave.SweavePlugin;
import de.walware.statet.r.sweave.text.MultiCatPartitionScanner.CatMarker;


/**
 * 
 */
public class MultiCatDocumentUtil {
	
	
	private final String fId;
	private final String[] fCatIds;
	
	
	public MultiCatDocumentUtil(final String partitioning, final String[] catIds) {
		fId = MultiCatPartitionScanner.ID_PREFIX+partitioning;
		fCatIds = catIds;
	}
	
	
	public ITypedRegion[] getCats(final IDocument document, final int offset, final int length) {
		try {
			final Position[] positions = document.getPositions(fId);
			int i = positions.length-1;
			if (i < 0) {
				return new ITypedRegion[] { new TypedRegion(0, document.getLength(), fCatIds[0]) };
			}
			
			final ArrayList<ITypedRegion> regions = new ArrayList<ITypedRegion>();
			final int rangeStop = offset+length;
			CatMarker catMarker;
			int catStart;
			int catEnd;
			int catType;
			boolean inControl;
			
			while (i >= 0 && positions[i].offset > offset) {
				i--;
			}
			if (i < 0) {
				catMarker = null;
				catStart = 0;
				catEnd = positions[++i].getOffset();
				catType = 0;
				inControl = false;
			}
			else {
				catMarker = (CatMarker) positions[i];
				if (offset < catMarker.getControlEnd()) {
					catStart = catMarker.getOffset();
					catType = -1;
					inControl = true;
				}
				else {
					catStart = catMarker.getControlEnd();
					catType = catMarker.type;
					inControl = false;
					i++;
				}
			}
			
			while (i < positions.length) {
				if (inControl) {
					catEnd = catMarker.getControlEnd();
					regions.add(new TypedRegion(catStart, catEnd-catStart, CONTROL_CAT));
					catStart = catEnd;
					catType = catMarker.type;
					i++;
					inControl = false;
				}
				else {
					catMarker = (CatMarker) positions[i];
					catEnd = catMarker.getOffset();
					regions.add(new TypedRegion(catStart, catEnd-catStart, fCatIds[catType]));
					catStart = catEnd;
					catType = -1;
					inControl = true;
				}
				if (catStart >= rangeStop) {
					return regions.toArray(new ITypedRegion[regions.size()]);
				}
			}
			if (document.getLength()-catStart > 0 || regions.size() == 0) {
				regions.add(new TypedRegion(catStart, document.getLength()-catStart,
						fCatIds[catType]));
			}
			return regions.toArray(new ITypedRegion[regions.size()]);
		} catch (final BadPositionCategoryException e) {
			SweavePlugin.logError(-1, "Error occurred when filter cats.", e); //$NON-NLS-1$
			return null;
		}
	}
	
	public ITypedRegion getCat(final IDocument document, final int offset) {
		try {
			final Position[] positions = document.getPositions(fId);
			int i = positions.length-1;
			if (i < 0) {
				return new TypedRegion(0, document.getLength(), fCatIds[0]);
			}
			
			CatMarker catMarker = null;
			while (i >= 0 && positions[i].offset > offset) {
				i--;
			}
			if (i < 0) {
				catMarker = (CatMarker) positions[0];
				return new TypedRegion(0, catMarker.getOffset(), fCatIds[0]);
			}
			catMarker = (CatMarker) positions[i];
			if (offset < catMarker.getControlEnd()) {
				return new TypedRegion(catMarker.getOffset(), catMarker.getControlEnd(), CONTROL_CAT);
			}
			
			final int catStart = catMarker.getControlEnd();
			final int catEnd = (++i < positions.length) ? ((CatMarker) positions[i]).getOffset() : document.getLength();
			return new TypedRegion(catStart, catEnd-catStart, fCatIds[catMarker.type]);
		} catch (final BadPositionCategoryException e) {
			SweavePlugin.logError(-1, "Error occurred when filter cats.", e); //$NON-NLS-1$
			return null;
		}
	}
	
}
