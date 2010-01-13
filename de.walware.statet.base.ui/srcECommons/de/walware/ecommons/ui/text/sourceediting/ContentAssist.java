/*******************************************************************************
 * Copyright (c) 2009-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.ui.text.sourceediting;

import org.eclipse.jface.text.contentassist.ContentAssistant;


public class ContentAssist extends ContentAssistant {
	
	
	public ContentAssist() {
	}
	
	
	boolean isProposalPopupActive1() {
		return super.isProposalPopupActive();
	}
	
	boolean isContextInfoPopupActive1() {
		return super.isContextInfoPopupActive();
	}
	
	public void hide1() {
		super.hide();
	}
	
}
