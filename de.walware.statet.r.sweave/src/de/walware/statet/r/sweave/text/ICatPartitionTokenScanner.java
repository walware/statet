/*=============================================================================#
 # Copyright (c) 2007-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.sweave.text;

import org.eclipse.jface.text.rules.IPartitionTokenScanner;


/**
 * Required extension for token scanners for partitioning with categories.
 */
public interface ICatPartitionTokenScanner extends IPartitionTokenScanner {
	
	String[] getContentTypes();
	
	boolean isInCat();
	
	void setParent(MultiCatPartitionScanner parent);
	
}
