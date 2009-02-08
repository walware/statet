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

package de.walware.ecommons.ui.text;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;


/**
 * 
 */
public class OperatorRule implements IRule {
	
	// Internal Classes for search-tree ---------------------------------------
	private class CharLevel {
		private CharLeaf[] list = new CharLeaf[0];
		
		private CharLeaf getChild(final char c) {
			for (int i = 0; i < list.length; i++) {
				if (list[i].leafChar == c)
					return list[i];
			}
			return null;
		}
		private void add(final String id, final char[] carray, final IToken token) {
			final CharLeaf leaf = getChild(carray[0]);
			if (leaf == null) {
				final CharLeaf[] extList = new CharLeaf[list.length+1];
				System.arraycopy(list, 0, extList, 0, list.length);
				extList[list.length] = new CharLeaf(id, carray, token);
				list = extList;
			} else
				leaf.add(id, carray, token);
		}
	}
	
	private class CharLeaf {
		private char leafChar;
		private IToken leafToken;
		private CharLevel nextLevel;
		private String leafId;
		
		private CharLeaf(final String id, final char[] chars, final IToken token) {
			leafChar = chars[0];
			add(id, chars, token);
		}
		private void add(final String id, final char[] chars, final IToken token) {
			// leafChar == chars[0];
			if (chars.length == 1) {
				leafId = id;
				leafToken = token;
			} else {
				final char[] nextChars = new char[chars.length-1];
				System.arraycopy(chars, 1, nextChars, 0, nextChars.length);
				if (nextLevel == null)
					nextLevel = new CharLevel();
				nextLevel.add(id, nextChars, token);
			}
		}
	}
	
	// ------------------------------------------------------------------------
	
	private CharLevel firstLevel;
	
	
	/**
	 * Creates new ROpRule.
	 */
	public OperatorRule(final char[] init) {
		firstLevel = new CharLevel();
		firstLevel.list = new CharLeaf[init.length];
		for (int i = 0; i < init.length; i++) {
			firstLevel.list[i] = new CharLeaf(null, new char[] { init[i] }, null);
		}
	}
	
	
	/**
	 * Adds an operator, linked with given token.
	 * 
	 * @param op the operator as <code>String</code> to detect.
	 * @param token the token, which should be returned, if operator is detected.
	 */
	public void addOp(final String op, final IToken token) {
		final char[] cOp = op.toCharArray();
		firstLevel.add(op, cOp, token);
	}
	public void addOps(final String[] ops, final IToken token) {
		for (int i = 0; i < ops.length; i++) {
			addOp(ops[i], token);
		}
	}
			
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.rules.IRule#evaluate(org.eclipse.jface.text.rules.ICharacterScanner)
	 */
	public IToken evaluate(final ICharacterScanner scanner) {
		final CharLeaf matchLeaf = searchLeaf(scanner);
		if (matchLeaf != null) {
			return matchLeaf.leafToken;
		}
		return Token.UNDEFINED;
	}
	
	public String searchString(final ICharacterScanner scanner) {
		final CharLeaf matchLeaf = searchLeaf(scanner);
		if (matchLeaf != null) {
			return matchLeaf.leafId;
		}
		return null;
	}
	
	private CharLeaf searchLeaf(final ICharacterScanner scanner) {
		CharLevel searchLevel = firstLevel;
		int level = 1;
		
		CharLeaf matchLeaf = null;
		int matchLevel = 0;
		
		for(;; level++) {
			final int c = scanner.read();
			CharLeaf leaf = null;
			if (c < 0) {
				level--;
				break;
			}
			if ((searchLevel == null) || (leaf = searchLevel.getChild((char)c)) == null) {
				break;
			}
			if (leaf.leafId != null) {
				matchLeaf = leaf;
				matchLevel = level;
			}
			searchLevel = leaf.nextLevel;
		}
		for (; level > matchLevel; level--) {
			scanner.unread();
		}
		return matchLeaf;
	}
	
}
