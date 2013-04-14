/*******************************************************************************
 * Copyright (c) 2012-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.emf.ui.forms;

import java.util.List;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.swt.graphics.Image;

import de.walware.ecommons.emf.core.util.RuleSet;



public interface IEFModelDescriptor {
	
	
	String getModelPluginID();
	
	String getEditorPluginID();
	String getEditorID();
	
	
	Image getImage();
	
	String getName();
	
	
	String getDefaultContentTypeID();
	
	String getDefaultFileExtension();
	
	List<String> getFileExtensions();
	
	
	AdapterFactory createItemProviderAdapterFactory();
	
	RuleSet getRuleSet();
	
	
}
