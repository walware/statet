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

package de.walware.statet.r.internal.sweave.editors;

import de.walware.ecommons.ltk.ISourceUnit;
import de.walware.ecommons.ltk.ui.sourceediting.SourceDocumentProvider;

import de.walware.statet.r.sweave.Sweave;


public class RweaveTexDocumentProvider extends SourceDocumentProvider<ISourceUnit> {
	
	
	public RweaveTexDocumentProvider() {
		super(Sweave.R_TEX_MODEL_TYPE_ID, new RweaveTexDocumentSetupParticipant());
	}
	
	
}
