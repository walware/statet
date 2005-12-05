/*******************************************************************************
 * Copyright (c) 2005 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.ext.ui.text;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;


/**
 * @author Stephan Wahlbrink
 */
public class OperatorRule implements IRule {
	
	// Internal Classes for search-tree ---------------------------------------
	private class CharLevel {
		private CharLeaf[] list = new CharLeaf[0];
		
		private CharLeaf contains(char c) {	
			for (int i = 0; i < list.length; i++) {
				if (list[i].leafChar == c)
					return list[i];
			}
			return null;
		}
		private void add(char[] carray, IToken token) {
			CharLeaf leaf = contains(carray[0]);
			if (leaf == null) {
				CharLeaf[] extList = new CharLeaf[list.length+1];
				System.arraycopy(list, 0, extList, 0, list.length);
				extList[list.length] = new CharLeaf(carray, token);
				list = extList;
			} else
				leaf.add(carray, token);
		}
	}

	private class CharLeaf {
		private char leafChar;
		private IToken leafToken;
		private CharLevel nextLevel;
		
		private CharLeaf(char[] chars, IToken token) {
			leafChar = chars[0];
			add(chars, token);
		}
		private void add(char[] chars, IToken token) {
			// leafChar == chars[0];
			if (chars.length == 1) {
				leafToken = token;
			} else {
				char[] nextChars = new char[chars.length-1];
				System.arraycopy(chars, 1, nextChars, 0, nextChars.length);
				if (nextLevel == null)
					nextLevel = new CharLevel();
				nextLevel.add(nextChars, token);
			}
		}
	}
	
	// ------------------------------------------------------------------------
	
	private CharLevel firstLevel;
	
	/**
	 * Creates new ROpRule.
	 */
	public OperatorRule(char[] init) {
		firstLevel = new CharLevel();
		firstLevel.list = new CharLeaf[init.length];
		for (int i = 0; i < init.length; i++) {
			firstLevel.list[i] = new CharLeaf(new char[] { init[i] }, null);
		}
	}
	
	/**
	 * Adds an operator, linked with given token.
	 * 
	 * @param op the operator as <code>String</code> to detect.
	 * @param token the token, which should be returned, if operator is detected.
	 */
	public void addOp(String op, IToken token) {
		char[] cOp = op.toCharArray();
		firstLevel.add(cOp, token);
	}
	public void addOps(String[] ops, IToken token) {
		for (int i = 0; i < ops.length; i++) {
			addOp(ops[i], token);
		}
	}
			
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.rules.IRule#evaluate(org.eclipse.jface.text.rules.ICharacterScanner)
	 */
	public IToken evaluate(ICharacterScanner scanner) {
		CharLevel searchLevel = firstLevel;
		int level = 1;

		CharLeaf matchLeaf = null;
		int matchLevel = 0;
		
		for(;; level++) {
			int c = scanner.read();
			CharLeaf leaf = null;
			if ( (c == ICharacterScanner.EOF) || (searchLevel == null) || (leaf = searchLevel.contains((char)c)) == null) {
				break;
			} else {
				if (leaf.leafToken != null) {
					matchLeaf = leaf;
					matchLevel = level;
				}
				searchLevel = leaf.nextLevel;
			}
		}
		for (; level > matchLevel; level--) {
			scanner.unread();
		}
		if (matchLeaf != null) {
			return matchLeaf.leafToken;
		}
		return Token.UNDEFINED;
	}
	
}