/*******************************************************************************
 * Copyright (c) 2007-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.rhelp;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.graphics.RGB;

import de.walware.ecommons.text.HtmlParseInput;
import de.walware.ecommons.text.ui.presentation.ITextPresentationConstants;
import de.walware.ecommons.text.ui.settings.TextStyleManager;

import de.walware.statet.r.ui.RUIPreferenceConstants;
import de.walware.statet.r.ui.text.r.RCodeScanner2;


public class RHelpRCodeScanner extends RCodeScanner2 {
	
	
	private static class CssTextStyleManager extends TextStyleManager {
		
		public CssTextStyleManager(final IPreferenceStore preferenceStore, final String stylesGroupId) {
			super(null, preferenceStore, stylesGroupId);
		}
		
		
		@Override
		protected Object createTextAttribute(String key) {
			if (key != null) {
				key = resolveUsedKey(key);
				
				if (key.equals(RUIPreferenceConstants.R.TS_DEFAULT_ROOT)) {
					return null;
				}
			}
			else {
				key = RUIPreferenceConstants.R.TS_DEFAULT_ROOT;
			}
			
			final StringBuilder sb = new StringBuilder(32);
			final RGB rgb = PreferenceConverter.getColor(fPreferenceStore, key + ITextPresentationConstants.TEXTSTYLE_COLOR_SUFFIX);
			sb.append("color: "); //$NON-NLS-1$
			RHelpUIServlet.appendCssColor(sb, rgb);
			sb.append("; "); //$NON-NLS-1$
			if (fPreferenceStore.getBoolean(key + ITextPresentationConstants.TEXTSTYLE_BOLD_SUFFIX)) {
				sb.append("font-weight: bold; "); //$NON-NLS-1$
			}
			if (fPreferenceStore.getBoolean(key + ITextPresentationConstants.TEXTSTYLE_ITALIC_SUFFIX)) {
				sb.append("font-style: italic; "); //$NON-NLS-1$
			}
			final boolean strikethrough = fPreferenceStore.getBoolean(key + ITextPresentationConstants.TEXTSTYLE_STRIKETHROUGH_SUFFIX);
			final boolean underline = fPreferenceStore.getBoolean(key + ITextPresentationConstants.TEXTSTYLE_UNDERLINE_SUFFIX);
			if (strikethrough || underline) {
				sb.append("text-decoration:"); //$NON-NLS-1$
				if (strikethrough) {
					sb.append(" line-through"); //$NON-NLS-1$
				}
				if (underline) {
					sb.append(" underline"); //$NON-NLS-1$
				}
				sb.append("; "); //$NON-NLS-1$
			}
			
			return sb.substring(0, sb.length()-1);
		}
		
	}
	
	
	public RHelpRCodeScanner(final IPreferenceStore preferenceStore) {
		super(new RTokenScannerLexer(), new CssTextStyleManager(preferenceStore,
				RUIPreferenceConstants.R.TS_GROUP_ID));
	}
	
	
	public void setCode(final String html) {
		getLexer().reset(new HtmlParseInput(html));
	}
	
	public String getDefaultStyle() {
		return (String) ((CssTextStyleManager) getTextStyleManager()).createTextAttribute(null);
	}
	
}
