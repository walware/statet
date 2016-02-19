/*=============================================================================#
 # Copyright (c) 2005-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.nico.internal.ui.console;

import java.util.regex.Pattern;

import org.eclipse.jface.text.AbstractDocument;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultLineTracker;
import org.eclipse.jface.text.GapTextStore;

import de.walware.ecommons.text.core.util.AbstractFragmentDocument;
import de.walware.ecommons.text.core.util.AbstractSynchronizableDocument;


public class InputDocument extends AbstractFragmentDocument {
	
	
	private static class MasterDocument extends AbstractSynchronizableDocument {
		
		public MasterDocument(final Object lockObject) {
			super(lockObject);
			
			setTextStore(new GapTextStore(128, 512, 0.1f));
			setLineTracker(new DefaultLineTracker());
			completeInitialization();
		}
		
	}
	
	
	private static Pattern gLineSeparatorPattern = Pattern.compile("\\r[\\n]?|\\n"); //$NON-NLS-1$
	
	
	public InputDocument() {
		super();
		
		setTextStore(new GapTextStore(128, 512, 0.1f));
		setLineTracker(new DefaultLineTracker());
		completeInitialization();
	}
	
	
	@Override
	protected AbstractDocument createMasterDocument() {
		return new MasterDocument(getLockObject());
	}
	
	@Override
	public void setPrefix(final String text) {
		super.setPrefix(text);
	}
	
	
	protected String checkText(final String text) {
		return gLineSeparatorPattern.matcher(text).replaceAll(""); //$NON-NLS-1$
	}
	
	@Override
	public void set(final String text) {
		super.set(checkText(text));
	}
	
	@Override
	public void set(final String text, final long modificationStamp) {
		super.set(checkText(text), modificationStamp);
	}
	
	@Override
	public void replace(final int offset, final int length, final String text, final long modificationStamp) throws BadLocationException {
		super.replace(offset, length, checkText(text), modificationStamp);
	}
	
// Method above is called in super
//	@Override
//	public void replace(final int offset, final int length, final String text) throws BadLocationException {
//		super.replace(offset, length, checkText(text));
//	}
	
}
