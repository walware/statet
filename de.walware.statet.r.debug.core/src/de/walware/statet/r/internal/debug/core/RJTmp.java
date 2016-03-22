/*=============================================================================#
 # Copyright (c) 2012-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.debug.core;

import de.walware.statet.r.core.model.RElementName;

public class RJTmp {
	
	
	public static final String CREATE_ID= "rj:::tmp.createId"; //$NON-NLS-1$
	public static final String SET= "rj:::tmp.set"; //$NON-NLS-1$
	public static final String REMOVE= "rj:::tmp.remove"; //$NON-NLS-1$
	public static final String REMOVE_ALL= "rj:::tmp.removeAll"; //$NON-NLS-1$
	
	public static final String PREFIX_PAR= "prefix"; //$NON-NLS-1$
	public static final String ID_PAR= "id"; //$NON-NLS-1$
	public static final String NAME_PAR= "name"; //$NON-NLS-1$
	public static final String VALUE_PAR= "value"; //$NON-NLS-1$
	
	public static final String ENV= "rj::.rj.tmp"; //$NON-NLS-1$
	
	public static final RElementName PKG_NAME= RElementName.create(RElementName.SCOPE_NS, "rj");
	public static final RElementName ENV_NAME= RElementName.create(RElementName.MAIN_DEFAULT, ".rj.tmp");
	
}
