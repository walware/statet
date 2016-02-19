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

package de.walware.statet.r.internal.core.pkgmanager;

import java.util.ArrayList;
import java.util.List;

import de.walware.statet.r.core.pkgmanager.IRView;


class RView implements IRView {
	
	
	private final String fName;
	private String fTopic;
	
	private final List<String> fPkgs= new ArrayList<>();
	
	
	public RView(final String name) {
		fName = name;
		fTopic = ""; //$NON-NLS-1$
	}
	
	
	@Override
	public String getName() {
		return fName;
	}
	
	public void setTopic(final String topic) {
		fTopic = topic;
	}
	
	@Override
	public String getTopic() {
		return fTopic;
	}
	
	@Override
	public List<String> getPkgList() {
		return fPkgs;
	}
	
	
	@Override
	public int hashCode() {
		return fName.hashCode();
	}
	
	@Override
	public boolean equals(final Object obj) {
		return (this == obj || (obj instanceof IRView 
				&& fName.equals(((IRView) obj).getName()) ));
	}
	
}
