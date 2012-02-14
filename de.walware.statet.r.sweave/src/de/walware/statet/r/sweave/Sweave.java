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

package de.walware.statet.r.sweave;


/**
 * 
 */
public interface Sweave {
	
	/**
	 * Content type id for Sweave (R/LaTeX) sources
	 */
	public static final String R_TEX_CONTENT_ID = "de.walware.statet.r.contentTypes.RweaveTex"; //$NON-NLS-1$
	
	/**
	 * Model type id for Sweave (R/LaTeX) documents
	 */
	public static final String R_TEX_MODEL_TYPE_ID = "rweave-tex"; //$NON-NLS-1$
	
	public static final String RWEAVETEX_DOC_PROCESSING_LAUNCHCONFIGURATION_ID = "de.walware.statet.r.launchConfigurationTypes.RweaveTexCreation"; //$NON-NLS-1$
	
}
