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

package de.walware.eclipsecommons.ltk.ui;


/**
 * Combines the selection and model information in one state.
 * This is the listener interface, provider is available in 
 * {@link PostSelectionWithElementInfoController}.
 */
public interface ISelectionWithElementInfoListener {
	
	
	public void stateChanged(LTKInputData state);
	
	public void inputChanged();
	
	
}
