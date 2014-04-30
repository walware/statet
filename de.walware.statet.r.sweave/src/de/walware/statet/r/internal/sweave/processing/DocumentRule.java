/*=============================================================================#
 # Copyright (c) 2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.sweave.processing;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.jobs.ISchedulingRule;


public class DocumentRule implements ISchedulingRule {
	
	
	private final IFile sweaveFile;
	
	
	public DocumentRule(final IFile sweaveFile) {
		if (sweaveFile == null) {
			throw new NullPointerException("sweaveFile"); //$NON-NLS-1$
		}
		this.sweaveFile= sweaveFile;
	}
	
	
	private boolean equalRule(final DocumentRule other) {
		return (this.sweaveFile.equals(other.sweaveFile));
	}
	
	@Override
	public boolean contains(final ISchedulingRule rule) {
		return (rule instanceof DocumentRule && equalRule((DocumentRule) rule));
	}
	
	@Override
	public boolean isConflicting(final ISchedulingRule rule) {
		return (rule instanceof DocumentRule && equalRule((DocumentRule) rule));
	}
	
	
	@Override
	public int hashCode() {
		return this.sweaveFile.hashCode() + 9543;
	}
	
	@Override
	public boolean equals(final Object obj) {
		return (this == obj
				|| (obj instanceof DocumentRule && equalRule((DocumentRule) obj)) );
	}
	
}
