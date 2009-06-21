/*******************************************************************************
 * Copyright (c) 2007-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.ui.text.sourceediting;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.link.ILinkedModeListener;
import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.jface.text.link.LinkedPosition;
import org.eclipse.jface.text.link.LinkedModeUI.ExitFlags;
import org.eclipse.jface.text.link.LinkedModeUI.IExitPolicy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyEvent;

import de.walware.ecommons.text.PartitioningConfiguration;


/**
 * Control LinkedModel for auto inserted pairs
 */
public abstract class BracketLevel implements IExitPolicy {
	
	
	public static abstract class DefaultBracketLevel extends BracketLevel {
		
		public DefaultBracketLevel(final IDocument doc, final PartitioningConfiguration partitioning, final LinkedPosition position, final boolean consoleMode) {
			super(doc, partitioning, position, consoleMode);
		}
		
		protected abstract int getCloseChar();
		
		@Override
		public boolean insertCR(final int charOffset) {
			return true;
		}
		
		@Override
		public boolean matchesEnd(final char c, final int charOffset) throws BadLocationException {
			return (c == getCloseChar() && fPartitioning.getDefaultPartitionConstraint().matches(
					getPartitionType(charOffset)));
		}
		
	}
	
	
	private final LinkedPosition fPosition;
	
	protected final IDocument fDocument;
	protected final PartitioningConfiguration fPartitioning;
	protected final boolean fConsoleMode;
	
	
	public BracketLevel(final IDocument doc, final PartitioningConfiguration partitioning, final LinkedPosition position, final boolean consoleMode) {
		fPosition = position;
		fDocument = doc;
		fPartitioning = partitioning;
		fConsoleMode = consoleMode;
	}
	
	
	public ExitFlags doExit(final LinkedModeModel model, final VerifyEvent event,
			final int offset, final int length) {
		try {
			switch (event.character) {
			case 0x0A: // cr
			case 0x0D:
				if (fConsoleMode) {
					return new ExitFlags(ILinkedModeListener.EXIT_ALL, true);
				}
				if (insertCR(offset) || length > 0) {
					return new ExitFlags(ILinkedModeListener.NONE, true);
				}
				return null;
			case SWT.BS: // backspace
				if (offset == fPosition.offset && length == 0) {
					fDocument.replace(offset-1, 2, ""); //$NON-NLS-1$
					return new ExitFlags(ILinkedModeListener.NONE, false);
				}
				return null;
			}
			// don't enter the character if if its the closing peer
			if (offset == fPosition.offset+fPosition.length && length == 0
					&& matchesEnd(event.character, offset)) {
				return new ExitFlags(ILinkedModeListener.UPDATE_CARET, false);
			}
		}
		catch (final BadLocationException e) {
		}
		return null;
	}
	
	/**
	 * Utility methods
	 * @param offset
	 * @return
	 * @throws BadLocationException
	 */
	protected String getPartitionType(final int offset) throws BadLocationException {
		return TextUtilities.getPartition(fDocument, fPartitioning.getPartitioning(), offset, true).getType();
	}
	
	/**
	 * If return key event should exit the linked mode
	 * 
	 * @param charOffset event offset
	 * @return <code>true</code> to exit the linked mode or
	 *     <code>false</code> to don't handle this event in a
	 *     (default editor inserts the text)
	 * @throws BadLocationException
	 */
	protected abstract boolean insertCR(int charOffset) throws BadLocationException;
	
	/**
	 * If the char is part of the existing end of the language element
	 * (closing bracket).
	 * If <code>true</code>, the input is ignored but the caret is updated.
	 * @param c char of the event
	 * @param charOffset event offset
	 * @return
	 * @throws BadLocationException
	 */
	protected abstract boolean matchesEnd(char c, int charOffset) throws BadLocationException;
	
}
