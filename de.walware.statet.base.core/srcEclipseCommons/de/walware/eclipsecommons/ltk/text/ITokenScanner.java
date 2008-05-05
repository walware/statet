/*******************************************************************************
 * Copyright (c) 2005 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.eclipsecommons.ltk.text;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;


public interface ITokenScanner {
	
	/**
	 * Returned by all methods when the requested position could not be found, or if a
	 * {@link BadLocationException} was thrown while scanning.
	 */
	int NOT_FOUND = -1;
	
	/**
	 * Special bound parameter that means either -1 (backward scanning) or
	 * <code>fDocument.getLength()</code> (forward scanning).
	 */
	int UNBOUND = -2;
	
	/**
	 * Array-index for opening peer.
	 */
	int OPENING_PEER = 0;
	/**
	 * Array-index for closing peer.
	 */
	int CLOSING_PEER = 1;
	
	/**
	 * @param document the document to scan
	 * @param partition the partition to scan in
	 */
	void configure(IDocument document, String partition);
	
//	/**
//	 * @param document the document to scan
//	 * @param offset offset, where looking for the partition to scan in
//	 * @throws BadLocationException 
//	 */
//	void configure(IDocument document, int offset) throws BadLocationException;
	
	/**
	 * Returns the position of the closing peer character (forward search). Any scopes introduced by opening peers
	 * are skipped. All peers accounted for must reside in the default partition.
	 * 
	 * <p>Note that <code>start</code> must not point to the opening peer, but to the first
	 * character being searched.</p>
	 * 
	 * @param start the start position
	 * @param pair pair with the opening and closing peer character
	 * @return the matching peer character position, or <code>NOT_FOUND</code>
	 */
	int findClosingPeer(int start, char[] pair);
	
	/**
	 * Returns the position of the closing peer character (forward search). Any scopes introduced by opening peers
	 * are skipped. All peers accounted for must reside in the default partition.
	 * 
	 * <p>Note that <code>start</code> must not point to the opening peer, but to the first
	 * character being searched.</p>
	 * 
	 * @param start the start position
	 * @param pair pair with the opening and closing peer character
	 * @param escapeChar char escaping the charaters in parameter <code>pair</code>
	 * @return the matching peer character position, or <code>NOT_FOUND</code>
	 */
	int findClosingPeer(int start, char[] pair, char escapeChar);
	
	/**
	 * Returns the position of the opening peer character (backward search). Any scopes introduced by closing peers
	 * are skipped. All peers accounted for must reside in the default partition.
	 * 
	 * <p>Note that <code>start</code> must not point to the closing peer, but to the first
	 * character being searched.</p>
	 * 
	 * @param start the start position
	 * @param pair pair with the opening and closing peer character
	 * @return the matching peer character position, or <code>NOT_FOUND</code>
	 */
	int findOpeningPeer(int start, char[] pair);
	
	/**
	 * Returns the position of the opening peer character (backward search). Any scopes introduced by closing peers
	 * are skipped. All peers accounted for must reside in the default partition.
	 * 
	 * <p>Note that <code>start</code> must not point to the closing peer, but to the first
	 * character being searched.</p>
	 * 
	 * @param start the start position
	 * @param pair pair with the opening and closing peer character
	 * @param escapeChar char escaping the charaters in parameter <code>pair</code>
	 * @return the matching peer character position, or <code>NOT_FOUND</code>
	 */
	int findOpeningPeer(int start, char[] pair, char escapeChar);
	
}
