/*******************************************************************************
 * Copyright (c) 2005-2006 StatET-Project (www.walware.de/goto/statet).
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

import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.BufferedRuleBasedScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

import de.walware.eclipsecommon.preferences.ICombinedPreferenceStore;
import de.walware.eclipsecommon.ui.util.ColorManager;
import de.walware.statet.ui.StatetUiPreferenceConstants;


/**
 * Basis für CodeScanners
 */
public abstract class StatextTextScanner extends BufferedRuleBasedScanner {

	
	protected ColorManager fColorManager;
	protected ICombinedPreferenceStore fPreferenceStore;
	protected String[] fTokenNames; 
	
	private Map<String, IToken> fTokenMap = new HashMap<String, IToken>();
	
	
	public StatextTextScanner(ColorManager colorManager, ICombinedPreferenceStore preferenceStore) {
		super();
		fColorManager = colorManager;
		fPreferenceStore = preferenceStore;
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

	
	public IToken getToken(String key) {
		IToken token = fTokenMap.get(key);
		if (token == null) {
			token = addToken(key);
		}
		return token;
	}
	
	private IToken addToken(String key) {
		
		String colorKey = key + StatetUiPreferenceConstants.TS_COLOR_SUFFIX;
		RGB rgb = PreferenceConverter.getColor(fPreferenceStore, colorKey);
		fColorManager.bindColor(colorKey, rgb);
		
		IToken token = new Token(createTextAttribute(key));
		fTokenMap.put(key, token);
		return token;
	}

	
	/**
	 * Create a text attribute based on the given color, bold, italic, strikethrough and underline preference keys.
	 *
	 * @param colorKey the color preference key
	 * @param boldKey the bold preference key
	 * @param italicKey the italic preference key
	 * @param strikethroughKey the strikethrough preference key
	 * @param underlineKey the italic preference key
	 * @return the created text attribute
	 * @since 3.0
	 */
	private TextAttribute createTextAttribute(String key) {
		
		Color color = fColorManager.getColor(key + StatetUiPreferenceConstants.TS_COLOR_SUFFIX);

		int style = fPreferenceStore.getBoolean(key + StatetUiPreferenceConstants.TS_BOLD_SUFFIX) ? 
				SWT.BOLD : SWT.NORMAL;
		if (fPreferenceStore.getBoolean(key + StatetUiPreferenceConstants.TS_ITALIC_SUFFIX))
			style |= SWT.ITALIC;

		if (fPreferenceStore.getBoolean(key + StatetUiPreferenceConstants.TS_STRIKETHROUGH_SUFFIX))
			style |= TextAttribute.STRIKETHROUGH;

		if (fPreferenceStore.getBoolean(key + StatetUiPreferenceConstants.TS_UNDERLINE_SUFFIX))
			style |= TextAttribute.UNDERLINE;

		return new TextAttribute(color, null, style);
	}
	
	public void adaptToPreferenceChange(PropertyChangeEvent event) {
		String p = event.getProperty();
		for (String name : fTokenNames) {
			if (p.startsWith(name)) {
				Token token = (Token) fTokenMap.get(name);
				if (p.endsWith(StatetUiPreferenceConstants.TS_COLOR_SUFFIX)) {
					RGB rgb = PreferenceConverter.getColor(fPreferenceStore, p);
					fColorManager.bindColor(p, rgb);
				}
				token.setData(createTextAttribute(name));
				return;
			}
		}
	}
	
}
