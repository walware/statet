/*******************************************************************************
 * Copyright (c) 2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.eclipsecommons.ltk;


/**
 * Interface for factories creation {@link ISourceUnit}.
 * Can be registered via extension point <code>de.walware.statet.base.workingContexts</code>
 */
public interface ISourceUnitFactory {
	
	
	ISourceUnit getUnit(Object from, String typeId, WorkingContext context, boolean create);
	
}
