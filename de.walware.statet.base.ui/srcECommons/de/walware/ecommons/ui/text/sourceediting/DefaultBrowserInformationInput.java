/*******************************************************************************
 * Copyright (c) 2008-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.ui.text.sourceediting;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.internal.text.html.BrowserInformationControlInput;
import org.eclipse.jface.internal.text.html.HTMLPrinter;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.FontData;

import de.walware.ecommons.IDisposable;

import de.walware.statet.base.internal.ui.StatetUIPlugin;


/**
 * 
 */
public class DefaultBrowserInformationInput extends BrowserInformationControlInput {
	
	
	private static Formatter FORMATTER;
	
	private static class Formatter implements IPropertyChangeListener, IDisposable {
		
		private static final Pattern TAB_PATTERN = Pattern.compile("\\\t"); //$NON-NLS-1$
		
		private String STYLE_SHEET;
		
		public Formatter() {
			JFaceResources.getFontRegistry().addListener(this);
			StatetUIPlugin.getDefault().registerPluginDisposable(this);
			updateStyleSheet();
		}
		
		public void dispose() {
			JFaceResources.getFontRegistry().removeListener(this);
			FORMATTER = null;
		}
		
		public void propertyChange(final PropertyChangeEvent event) {
			if (event.getProperty().equals(JFaceResources.DIALOG_FONT)) {
				updateStyleSheet();
			}
		}
		
		private void updateStyleSheet() {
			String style =
				// Font definitions
				"html         { font-family: sans-serif; font-size: 9pt; font-style: normal; font-weight: normal; }\n"+
				"body, h1, h2, h3, h4, h5, h6, p, table, td, caption, th, ul, ol, dl, li, dd, dt { font-size: 1em; }\n"+
				"pre          { font-family: monospace; }\n"+
				// Margins
				"html         { margin: 0px; padding: 0px }"+
				"body         { overflow: auto; margin-top: 0.25em; margin-bottom: 0.5em; margin-left: 0.25em; margin-right: 0.25em; }\n"+
				"h1           { margin-top: 0.3em; margin-bottom: 0.04em; }\n"+
				"h2           { margin-top: 2em; margin-bottom: 0.25em; }\n"+
				"h3           { margin-top: 1.7em; margin-bottom: 0.25em; }\n"+
				"h4           { margin-top: 2em; margin-bottom: 0.3em; }\n"+
				"h5           { margin-top: 0px; margin-bottom: 0px; }\n"+
				"p            { margin-top: 1em; margin-bottom: 1em; }\n"+
//				"pre          { margin-left: 0.6em; }\n"+
				"ul           { margin-top: 0px; margin-bottom: 1em; }\n"+
				"li           { margin-top: 0px; margin-bottom: 0px; }\n"+
				"li p         { margin-top: 0px; margin-bottom: 0px; }\n"+
				"ol           { margin-top: 0px; margin-bottom: 1em; }\n"+
				"dl           { margin-top: 0px; margin-bottom: 1em; }\n"+
				"dt           { margin-top: 0px; margin-bottom: 0px; font-weight: bold; }\n"+
				"dd           { margin-top: 0px; margin-bottom: 0px; }\n"+
				// Styles and colors
				"a:link       { color: #0000FF; }\n"+
				"a:hover      { color: #000080; }\n"+
				"a:visited    { text-decoration: underline; }\n"+
				"h4           { font-style: italic; }\n"+
				"strong       { font-weight: bold; }\n"+
				"em           { font-style: italic; }\n"+
				"var          { font-style: italic; }\n"+
				"th           { font-weight: bold; }\n";
			try {
				final FontData[] fontData = JFaceResources.getFontRegistry().getFontData(JFaceResources.DIALOG_FONT);
				if (fontData != null && fontData.length > 0) {
					style = style.replace("9pt", fontData[0].getHeight()+"pt");
					style = style.replace("sans-serif", "sans-serif, '"+fontData[0].getName()+"'");
				}
			}
			catch (final Throwable e) {
			}
			STYLE_SHEET = style;
		}
		
		String format(String content, final int formatting) {
			final StringBuffer s;
			switch (formatting) {
			case FORMAT_NONE:
				return content;
			case FORMAT_TEXT_INPUT: 
				content = HTMLPrinter.convertToHTMLContent(content);
				s = new StringBuffer(content.length()+1000);
				s.append(content);
				break;
			case FORMAT_SOURCE_INPUT:
				content = HTMLPrinter.convertToHTMLContent(content);
				final Matcher matcher = TAB_PATTERN.matcher(content);
				if (matcher.find()) {
					content = matcher.replaceAll("    "); //$NON-NLS-1$
				}
				s = new StringBuffer(content.length()+1000);
				s.append("<pre>"); //$NON-NLS-1$
				s.append(content);
				s.append("</pre>"); //$NON-NLS-1$
				break;
			case FORMAT_HTMLBODY_INPUT:
				s = new StringBuffer(content.length()+1000);
				s.append(content);
				break;
			default:
				throw new IllegalArgumentException("Unsupported format"); //$NON-NLS-1$
			}
			
			HTMLPrinter.insertPageProlog(s, 0, STYLE_SHEET);
			HTMLPrinter.addPageEpilog(s);
			return s.toString();
		}
		
	};
	
	
	public static final int FORMAT_NONE = 0;
	public static final int FORMAT_HTMLBODY_INPUT = 1;
	public static final int FORMAT_TEXT_INPUT = 2;
	public static final int FORMAT_SOURCE_INPUT = 3;
	
	
	private String fName;
	private String fHtml;
	
	
	public DefaultBrowserInformationInput(final BrowserInformationControlInput previous, final String name, final String content, final int formatting) {
		super(previous);
		
		fName = name;
		fHtml = getFormatter().format(content, formatting);
	}
	
	protected Formatter getFormatter() {
		synchronized (DefaultBrowserInformationInput.class) {
			if (FORMATTER == null) {
				FORMATTER = new Formatter();
			}
			return FORMATTER;
		}
	}
	
	
	@Override
	public String getInputName() {
		return fName;
	}
	
	@Override
	public Object getInputElement() {
		return fHtml;
	}
	
	@Override
	public String getHtml() {
		return fHtml;
	}
	
}
