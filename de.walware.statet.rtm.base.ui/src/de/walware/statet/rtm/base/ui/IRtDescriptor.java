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

package de.walware.statet.rtm.base.ui;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import de.walware.ecommons.emf.ui.forms.IEFModelDescriptor;

import de.walware.statet.rtm.base.core.AbstractRCodeGenerator;


public interface IRtDescriptor extends IEFModelDescriptor {
	
	
	String getTaskId();
	
	String getAssociatedPerspectiveId();
	
	EPackage getEPackage();
	
	EObject createInitialModelObject();
	
	AbstractRCodeGenerator createCodeGenerator();
	
}
