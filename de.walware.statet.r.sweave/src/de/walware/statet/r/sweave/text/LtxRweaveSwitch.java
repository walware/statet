/*******************************************************************************
 * Copyright (c) 2012-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.sweave.text;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextUtilities;



public enum LtxRweaveSwitch {
	
	
	LTX,
	CHUNK_CONTROL,
	R,
	
	ERROR;
	
	
	public static final LtxRweaveSwitch get(final IDocument document, final int offset) {
		try {
			return get(TextUtilities.getPartition(document,
					Rweave.LTX_R_PARTITIONING, offset, true ).getType() );
		}
		catch (final BadLocationException e) {
			return ERROR;
		}
	}
	
	public static final LtxRweaveSwitch get(final String partitionType) {
		if (Rweave.R_PARTITION_CONSTRAINT.matches(partitionType)) {
			return R;
		}
		if (Rweave.CHUNK_CONTROL_CONTENT_TYPE.matches(partitionType)) {
			return CHUNK_CONTROL;
		}
		return LTX;
	}
	
}
