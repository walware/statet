/*******************************************************************************
 * Copyright (c) 2005-2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.ext.ui.text;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.BufferedRuleBasedScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;

import de.walware.eclipsecommons.ui.util.ColorManager;

import de.walware.statet.base.ui.IStatetUIPreferenceConstants;


/**
 * Basis for CodeScanners
 */
public abstract class StatextTextScanner extends BufferedRuleBasedScanner {

	
	protected ColorManager fColorManager;
	protected IPreferenceStore fPreferenceStore;
	protected String[] fTokenNames; 
	private String fStylesContext;
	
	private Map<String, Token> fTokenMap = new HashMap<String, Token>();
	
	
	public StatextTextScanner(ColorManager colorManager, IPreferenceStore preferenceStore,
			String stylesContext) {
		super();
		fColorManager = colorManager;
		fPreferenceStore = preferenceStore;
		fStylesContext = stylesContext;
	}
	
	/**
	 * Must be called after the constructor has been called.
	 */
	protected void initialize() {
		List<IRule> rules = createRules();
		if (rules != null)
			setRules(rules.toArray(new IRule[rules.size()]));
		
		Set<String> tokens = fTokenMap.keySet();
		fTokenNames = tokens.toArray(new String[tokens.size()]);
	}

	/**
	 * Creates the list of rules controlling this scanner.
	 */
	abstract protected List<IRule> createRules();

	
	/**
	 * Token access for styles.
	 * @param key id and prefix for preference keys
	 * @return token with text style attribute
	 */
	public IToken getToken(String key) {
		Token token = fTokenMap.get(key);
		if (token == null) {
			token = new Token(createTextAttribute(key));
			fTokenMap.put(key, token);
		}
		return token;
	}
	
	private String resolveUsedKey(String key) {
		String use = key;
		while (true) {
			String test = fPreferenceStore.getString(use+IStatetUIPreferenceConstants.TS_USE_SUFFIX);
			if (test == null || test.equals("") || test.equals(use)) { //$NON-NLS-1$
				return use;
			}
			use = test;
		}
	}
	
	/**
	 * Create a text attribute based on the given color, bold, italic, strikethrough and underline preference keys.
	 *
	 * @param key the italic preference key
	 * @return the created text attribute
	 * @since 3.0
	 */
	private TextAttribute createTextAttribute(String key) {
		key = resolveUsedKey(key);
		
		RGB rgb = PreferenceConverter.getColor(fPreferenceStore, key + IStatetUIPreferenceConstants.TS_COLOR_SUFFIX);
		int style = fPreferenceStore.getBoolean(key + IStatetUIPreferenceConstants.TS_BOLD_SUFFIX) ? 
				SWT.BOLD : SWT.NORMAL;
		if (fPreferenceStore.getBoolean(key + IStatetUIPreferenceConstants.TS_ITALIC_SUFFIX))
			style |= SWT.ITALIC;
		if (fPreferenceStore.getBoolean(key + IStatetUIPreferenceConstants.TS_STRIKETHROUGH_SUFFIX))
			style |= TextAttribute.STRIKETHROUGH;
		if (fPreferenceStore.getBoolean(key + IStatetUIPreferenceConstants.TS_UNDERLINE_SUFFIX))
			style |= TextAttribute.UNDERLINE;

		return new TextAttribute(fColorManager.getColor(rgb), null, style);
	}
	
	public boolean handleSettingsChanged(Set<String> contexts, Object options) {
		if (contexts.contains(fStylesContext)) {
			for (Map.Entry<String, Token> token : fTokenMap.entrySet()) {
				token.getValue().setData(createTextAttribute(token.getKey()));
			}
			return true;
		}
		return false;
	}

}
