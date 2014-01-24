/*=============================================================================#
 # Copyright (c) 2013-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.ui;

import org.eclipse.ui.dialogs.SearchPattern;


public class RNameSearchPattern extends SearchPattern {
	
	
	private final StringBuilder sb = new StringBuilder();
	
	private boolean fuzzy;
	private boolean fuzzyLowerCase;
	
	
	public RNameSearchPattern() {
		super(SearchPattern.RULE_EXACT_MATCH
					| SearchPattern.RULE_PREFIX_MATCH | SearchPattern.RULE_CAMELCASE_MATCH
					| SearchPattern.RULE_PATTERN_MATCH | SearchPattern.RULE_BLANK_MATCH);
	}
	
	
	@Override
	public void setPattern(String stringPattern) {
		this.fuzzy = false;
		this.fuzzyLowerCase = false;
		if (!stringPattern.isEmpty()
				&& stringPattern.indexOf('_') < 0 && stringPattern.indexOf('.') < 0
				&& stringPattern.indexOf('*') < 0 && stringPattern.indexOf('?') < 0) {
			this.fuzzy = true;
			final char c = stringPattern.charAt(0);
			if (Character.isLowerCase(c)) {
				this.fuzzyLowerCase = true;
				stringPattern = Character.toUpperCase(c) + stringPattern.substring(1);
			}
		}
		super.setPattern(stringPattern);
	}
	
	private String prepareFuzzy(final String text) {
		this.sb.setLength(0);
		boolean innerSep = this.fuzzyLowerCase;
		for (int i = 0; i < text.length(); i++) {
			final char c = text.charAt(i);
			if (c == '_' || c == '.') {
				innerSep = true;
			}
			else if (innerSep) {
				innerSep = false;
				this.sb.append(Character.toUpperCase(c));
			}
			else {
				this.sb.append(c);
			}
		}
		return this.sb.toString();
	}
	
	
	@Override
	public boolean matches(String text) {
		if (this.fuzzy) {
			text = prepareFuzzy(text);
		}
		return super.matches(text);
	}
	
}
