/*******************************************************************************
 * Copyright (c) 2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.ui;

import org.eclipse.help.IContextProvider;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.IWorkbenchPart3;

import de.walware.statet.r.internal.ui.help.EnrichedRHelpContext;


/**
 * 
 */
public class RUIHelp {
	
	
	public static IContextProvider createEnrichedRHelpContextProvider(IWorkbenchPart3 part, String contextId) {
		
		return new EnrichedRHelpContext.Provider(part, contextId);
	}

	public static IContextProvider createEnrichedRHelpContextProvider(ISourceViewer viewer, String contextId) {
		
		return new EnrichedRHelpContext.Provider(viewer, contextId);
	}

}
