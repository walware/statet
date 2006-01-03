/*******************************************************************************
 * Copyright (c) 2005 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.console;

import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;



public class InputSourceViewer extends SourceViewer {

	
	InputSourceViewer(Composite parent) {
		
		super(parent, null, null, false, SWT.SINGLE);
		
		initDND();
	}
	
	
	private void initDND() {
		
		// TODO: Drop Action, related to Eclipse-Bug #106372
//		DNDUtil.addDropSupport(this, xxx, 
//				DND.DROP_COPY, 
//				new Transfer[] { TextTransfer.getInstance() });
	}
	
    /**
     * Sets the tab width used by this viewer.
     * 
     * @param tabWidth
     *            the tab width used by this viewer
     */
    public void setTabWidth(int tabWidth) {
        StyledText styledText = getTextWidget();
        int oldWidth = styledText.getTabs();
        if (tabWidth != oldWidth) {
            styledText.setTabs(tabWidth);
        }
    }
}
