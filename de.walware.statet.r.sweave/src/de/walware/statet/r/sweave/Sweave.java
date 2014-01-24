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

package de.walware.statet.r.sweave;


/**
 * 
 */
public interface Sweave {
	
	/**
	 * Content type id for Sweave (R/LaTeX) sources
	 */
	public static final String LTX_R_CONTENT_ID = "de.walware.statet.r.contentTypes.LtxRweave"; //$NON-NLS-1$
	
	/**
	 * Model type id for Sweave (R/LaTeX) documents
	 */
	public static final String LTX_R_MODEL_TYPE_ID = "ltx-rweave"; //$NON-NLS-1$
	
	public static final String RWEAVETEX_DOC_PROCESSING_LAUNCHCONFIGURATION_ID = "de.walware.statet.r.launchConfigurationTypes.RweaveTexCreation"; //$NON-NLS-1$
	
}
