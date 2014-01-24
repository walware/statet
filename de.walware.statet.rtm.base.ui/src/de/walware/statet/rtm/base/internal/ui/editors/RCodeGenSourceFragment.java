/*=============================================================================#
 # Copyright (c) 2012-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.rtm.base.internal.ui.editors;

import java.util.UUID;

import org.eclipse.jface.text.AbstractDocument;
import org.eclipse.jface.text.Document;

import de.walware.ecommons.text.ISourceFragment;


/**
 * Source fragment of an R process
 */
public class RCodeGenSourceFragment implements ISourceFragment {
	
	
	private final String fId;
	
	private final String fName;
	private final String fFullName;
	private final AbstractDocument fDocument;
	
	
	public RCodeGenSourceFragment(final String name, final String fullName) {
		fName = name;
		fFullName = fullName;
		fDocument = new Document();
		
		fId = "rtask:" + fFullName + '-' + UUID.randomUUID(); //$NON-NLS-1$
	}
	
	
	@Override
	public String getId() {
		return fId;
	}
	
	@Override
	public String getName() {
		return fName;
	}
	
	@Override
	public String getFullName() {
		return fFullName;
	}
	
	@Override
	public AbstractDocument getDocument() {
		return fDocument;
	}
	
	
	@Override
	public Object getAdapter(final Class required) {
		return null;
	}
	
	
	@Override
	public int hashCode() {
		return fId.hashCode();
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof RCodeGenSourceFragment)) {
			return false;
		}
		final RCodeGenSourceFragment other = (RCodeGenSourceFragment) obj;
		return (fId.equals(other.fId)
				&& fFullName.equals(other.fFullName)
				&& fDocument.equals(other.fDocument) );
	}
	
	@Override
	public String toString() {
		return fFullName;
	}
	
}
