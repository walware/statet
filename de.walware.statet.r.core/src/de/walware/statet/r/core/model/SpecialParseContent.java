/*******************************************************************************
 * Copyright (c) 2009-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core.model;

import de.walware.ecommons.ltk.SourceContent;


public class SpecialParseContent extends SourceContent {
	
	
	public final int offset;
	
	
	public SpecialParseContent(final long stamp, final String content, final int offset) {
		super(stamp, content);
		this.offset = offset;
	}
	
	
}
