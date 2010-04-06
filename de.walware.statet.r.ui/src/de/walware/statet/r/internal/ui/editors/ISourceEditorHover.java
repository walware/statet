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

package de.walware.statet.r.internal.ui.editors;

import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;

import de.walware.ecommons.ltk.ui.sourceediting.ISourceEditor;


public interface ISourceEditorHover {
	
	
	public void setEditor(final ISourceEditor editor);
	
	public IRegion getHoverRegion(final int offset);
	
	public IInformationControlCreator getHoverControlCreator();
	
	public Object getHoverInfo(final IRegion hoverRegion);
	
}
