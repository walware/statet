/*******************************************************************************
 * Copyright (c) 2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core.model;

import java.util.Map;

import de.walware.eclipsecommons.ltk.ISourceUnitModelInfo;


/**
 * Container for model information of a R source unit
 */
public interface IRModelInfo extends ISourceUnitModelInfo {
	
	
	public Map<String, ? extends IEnvirInSource> getSourceFrames();
	
}
