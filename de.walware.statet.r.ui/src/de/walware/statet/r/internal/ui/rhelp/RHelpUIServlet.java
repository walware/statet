/*******************************************************************************
 * Copyright (c) 2010-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.rhelp;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.internal.editors.text.EditorsPlugin;

import de.walware.ecommons.preferences.PreferencesUtil;
import de.walware.ecommons.preferences.SettingsChangeNotifier.ChangeListener;
import de.walware.ecommons.ui.util.LayoutUtil;
import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.r.core.rhelp.RHelpServlet;
import de.walware.statet.r.internal.ui.RIdentifierGroups;
import de.walware.statet.r.internal.ui.RUIPlugin;
import de.walware.statet.r.ui.RUIPreferenceConstants;


public class RHelpUIServlet extends RHelpServlet implements IPropertyChangeListener, ChangeListener {
	
	private static final long serialVersionUID = 1L;
	
	
	public static final String BROWSE_TARGET = "browse"; //$NON-NLS-1$
	public static final String INFO_TARGET = "info"; //$NON-NLS-1$
	
	
	private static final String[] COLORED_PRE_TAGS = {
		"<span style=\"background: #ceccf7;\">", //$NON-NLS-1$
		"<span style=\"background: #ffffcf;\">", //$NON-NLS-1$
		"<span style=\"background: aquamarine\">", //$NON-NLS-1$
		"<span style=\"background: palegreen\">", //$NON-NLS-1$
		"<span style=\"background: coral\">", //$NON-NLS-1$
		"<span style=\"background: wheat\">", //$NON-NLS-1$
		"<span style=\"background: khaki\">", //$NON-NLS-1$
		"<span style=\"background: lime\">", //$NON-NLS-1$
		"<span style=\"background: deepskyblue\">" //$NON-NLS-1$
	};
	private static final String[] COLORED_POST_TAGS = { "</span>" }; //$NON-NLS-1$
	
	public static void appendCssColor(final StringBuilder sb, final Color color) {
		sb.append('#');
		String s = Integer.toHexString(color.getRed());
		if (s.length() == 1) {
			sb.append('0');
		}
		sb.append(s);
		s = Integer.toHexString(color.getGreen());
		if (s.length() == 1) {
			sb.append('0');
		}
		sb.append(s);
		s = Integer.toHexString(color.getBlue());
		if (s.length() == 1) {
			sb.append('0');
		}
		sb.append(s);
	}
	
	public static void appendCssColor(final StringBuilder sb, final RGB color) {
		sb.append('#');
		String s = Integer.toHexString(color.red);
		if (s.length() == 1) {
			sb.append('0');
		}
		sb.append(s);
		s = Integer.toHexString(color.green);
		if (s.length() == 1) {
			sb.append('0');
		}
		sb.append(s);
		s = Integer.toHexString(color.blue);
		if (s.length() == 1) {
			sb.append('0');
		}
		sb.append(s);
	}
	
	
	public static class Browse extends RHelpUIServlet {
		
		private static final long serialVersionUID = 1L;
		
		@Override
		protected void collectCss(final StringBuilder sb) {
			final FontDescriptor docFontDescr = JFaceResources.getFontDescriptor("de.walware.statet.base.themes.DocViewFont"); //$NON-NLS-1$
			final FontData fontData = docFontDescr.getFontData()[0];
			final RGB docBackgroundColor = JFaceResources.getColorRegistry().getRGB("de.walware.statet.base.themes.DocViewBackgroundColor"); //$NON-NLS-1$
			final Color borderColor = Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW);
			sb.append("body { font-family: '"); //$NON-NLS-1$
			sb.append(fontData.getName());
			sb.append("'; font-size: "); //$NON-NLS-1$
			sb.append(fontData.getHeight());
			sb.append("pt; background: "); //$NON-NLS-1$
			appendCssColor(sb, docBackgroundColor);
			sb.append("; }\n" + //$NON-NLS-1$
					"@media screen {\n" + //$NON-NLS-1$
					"body { margin: "); //$NON-NLS-1$
			sb.append(LayoutUtil.defaultVSpacing());
			sb.append("px "); //$NON-NLS-1$
			sb.append(LayoutUtil.defaultHSpacing());
			sb.append("px; }\n" + //$NON-NLS-1$
					"div.toc { display: inline; float: right; border: 1px solid "); //$NON-NLS-1$
			appendCssColor(sb, borderColor);
			sb.append("; }\n"); //$NON-NLS-1$
			sb.append("span.mnemonic, div.toc a.mnemonic { text-decoration: underline; }\n" + //$NON-NLS-1$
					"hr { border: 0; height: 1px; background: " ); //$NON-NLS-1$
			appendCssColor(sb, borderColor);
			sb.append("; }\n" + //$NON-NLS-1$
					"}\n" ); // @media //$NON-NLS-1$
			super.collectCss(sb);
			
			updateSearchTags();
		}
		
		private void updateSearchTags() {
			final RGB rgb = PreferenceConverter.getColor(
					EditorsPlugin.getDefault().getPreferenceStore(),
					"searchResultIndicationColor"); //$NON-NLS-1$
			if (rgb != null) {
				final StringBuilder sb = new StringBuilder("<span style=\"background: "); //$NON-NLS-1$
				appendCssColor(sb, rgb);
				sb.append(";\">"); //$NON-NLS-1$
				COLORED_PRE_TAGS[0] = sb.toString();
			}
		}
		
	}
	
	public static class Info extends RHelpUIServlet {
		
		private static final long serialVersionUID = 1L;
		
		@Override
		protected void collectCss(final StringBuilder sb) {
			final Display display = Display.getCurrent();
			final FontDescriptor docFontDescr = JFaceResources.getDialogFontDescriptor();
			final FontData fontData = docFontDescr.getFontData()[0];
			final Color infoForegroundColor = display.getSystemColor(SWT.COLOR_INFO_FOREGROUND);
			final Color infoBackgroundColor = display.getSystemColor(SWT.COLOR_INFO_BACKGROUND);
			final int vIndent = Math.max(1, LayoutUtil.defaultVSpacing() / 4);
			final int hIndent = Math.max(3, LayoutUtil.defaultHSpacing() / 2);
			sb.append("body { font-family: '"); //$NON-NLS-1$
			sb.append(fontData.getName());
			sb.append("'; font-size: "); //$NON-NLS-1$
			sb.append(fontData.getHeight());
			sb.append("pt; color: "); //$NON-NLS-1$
			appendCssColor(sb, infoForegroundColor);
			sb.append("; background: "); //$NON-NLS-1$
			appendCssColor(sb, infoBackgroundColor);
			sb.append("; margin: 0 "); //$NON-NLS-1$
			sb.append(hIndent);
			sb.append("px "); //$NON-NLS-1$
			sb.append(vIndent);
			sb.append("px; }\n" + //$NON-NLS-1$
					"hr { visibility:hidden; }\n" + //$NON-NLS-1$
					"h2, h3#description { display: none; }\n" + //$NON-NLS-1$
					"h3 { font-size: 90%; margin-bottom: 0.4em; }\n" + //$NON-NLS-1$
					"p, pre { margin-top: 0.4em; margin-bottom: 0.4em; }\n" ); //$NON-NLS-1$
			super.collectCss(sb);
		}
		
	}
	
	
	private String fCssStyle;
	
	private RHelpRCodeScanner fRCodeScanner;
	
	
	public RHelpUIServlet() {
	}
	
	
	@Override
	public void init(final ServletConfig config) throws ServletException {
		super.init(config);
		
		fRCodeScanner = new RHelpRCodeScanner(RUIPlugin.getDefault().getEditorPreferenceStore());
		
		EditorsUI.getPreferenceStore().addPropertyChangeListener(this);
		JFaceResources.getFontRegistry().addListener(this);
		PreferencesUtil.getSettingsChangeNotifier().addChangeListener(this);
		updateStyles();
	}
	
	@Override
	public void propertyChange(final PropertyChangeEvent event) {
		if (event.getProperty().equals("de.walware.statet.base.themes.DocViewFont") //$NON-NLS-1$
				|| event.getProperty().equals("de.walware.statet.base.themes.DocViewBackgroundColor") //$NON-NLS-1$
				|| event.getProperty().equals(JFaceResources.DIALOG_FONT)
				|| event.getProperty().equals("searchResultIndicationColor") ) { //$NON-NLS-1$
			updateStyles();
		}
	}
	
	@Override
	public void settingsChanged(final Set<String> groupIds) {
		if (groupIds.contains(RUIPreferenceConstants.R.TS_GROUP_ID)
				|| groupIds.contains(RIdentifierGroups.GROUP_ID)) {
			final Map<String, Object> options = new HashMap<String, Object>();
			synchronized (fRCodeScanner) {
				fRCodeScanner.handleSettingsChanged(groupIds, options);
			}
		}
	}
	
	@Override
	public void destroy() {
		super.destroy();
		
		final IPreferenceStore preferenceStore = EditorsUI.getPreferenceStore();
		if (preferenceStore != null) {
			preferenceStore.removePropertyChangeListener(this);
		}
		JFaceResources.getFontRegistry().removeListener(this);
		PreferencesUtil.getSettingsChangeNotifier().removeChangeListener(this);
	}
	
	private void updateStyles() {
		final StringBuilder sb = new StringBuilder(1024);
		UIAccess.getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				collectCss(sb);
			}
		});
		sb.append(".header { display: none; }"); //$NON-NLS-1$
		fCssStyle = sb.toString();
	}
	
	protected void collectCss(final StringBuilder sb) {
	}
	
	@Override
	protected String[] getHightlightPreTags() {
		return COLORED_PRE_TAGS;
	}
	
	@Override
	protected String[] getHightlightPostTags() {
		return COLORED_POST_TAGS;
	}
	
	@Override
	protected void customizeCss(final PrintWriter writer) {
//		updateStyles();
		writer.print(fCssStyle);
	}
	
	@Override
	protected void customizePageHtmlHeader(final HttpServletRequest req, final PrintWriter writer) {
		customizeHtmlHeader(req, writer, true);
	}
	
	@Override
	protected void customizeIndexHtmlHeader(final HttpServletRequest req, final PrintWriter writer) {
		customizeHtmlHeader(req, writer, false);
	}
	
	protected void customizeHtmlHeader(final HttpServletRequest req, final PrintWriter writer,
			final boolean page) {
		writer.println("<script type=\"text/javascript\">/* <![CDATA[ */"); //$NON-NLS-1$
		writer.println("function keyNavHandler(event) {"); //$NON-NLS-1$
		writer.println("if (!event) event = window.event;"); //$NON-NLS-1$
		writer.println("if (event.which) { key = event.which } else if (event.keyCode) { key = event.keyCode };"); //$NON-NLS-1$
		writer.println("if (!event.ctrlKey && !event.altKey) { var anchor = 0;"); //$NON-NLS-1$
		if (page) {
			writer.println("if (key == 68) anchor = \"#description\"; " + //$NON-NLS-1$
					"else if (key == 85) anchor = \"#usage\"; " + //$NON-NLS-1$
					"else if (key == 65) anchor = \"#arguments\"; " + //$NON-NLS-1$
					"else if (key == 73) anchor = \"#details\"; " + //$NON-NLS-1$
					"else if (key == 86) anchor = \"#value\"; " + //$NON-NLS-1$
					"else if (key == 79) anchor = \"#authors\"; " + //$NON-NLS-1$
					"else if (key == 82) anchor = \"#references\"; " + //$NON-NLS-1$
					"else if (key == 69) anchor = \"#examples\"; " + //$NON-NLS-1$
					"else if (key == 83) anchor = \"#seealso\";"); //$NON-NLS-1$
		}
		else {
			writer.println("if (key >= 65 && key <= 90) anchor = \"#idx\"+String.fromCharCode(key+32);"); //$NON-NLS-1$
		}
		writer.println("if (anchor) { window.location.hash = anchor; event.cancelBubble = true; return false; }"); //$NON-NLS-1$
		writer.println("} return true; }"); //$NON-NLS-1$
		writer.println("document.onkeydown = keyNavHandler;"); //$NON-NLS-1$
		writer.println("/* ]]> */</script>"); //$NON-NLS-1$
		
		if ("hover".equals(req.getParameter("style"))) { //$NON-NLS-1$ //$NON-NLS-2$
			writer.println("<style type=\"text/css\">body { overflow: hidden; }</style>"); //$NON-NLS-1$
		}
	}
	
	@Override
	protected void printRCode(final PrintWriter writer, final String html) {
		synchronized (fRCodeScanner) {
			fRCodeScanner.setCode(html);
			writer.write("<span style=\""); //$NON-NLS-1$
			writer.write(fRCodeScanner.getDefaultStyle());
			writer.write("\">"); //$NON-NLS-1$
			
			IToken token;
			int currentIdx = 0;
			while (!(token = fRCodeScanner.nextToken()).isEOF()) {
				final String data = (String) token.getData();
				if (data != null) {
					final int tokenIdx = fRCodeScanner.getTokenOffset();
					if (tokenIdx > currentIdx) {
						writer.write(html, currentIdx, tokenIdx-currentIdx);
					}
					writer.write("<span style=\""); //$NON-NLS-1$
					writer.write(data);
					writer.write("\">"); //$NON-NLS-1$
					writer.write(html, tokenIdx, fRCodeScanner.getTokenLength());
					writer.write("</span>"); //$NON-NLS-1$
					currentIdx = tokenIdx + fRCodeScanner.getTokenLength();
				}
			}
			writer.write(html, currentIdx, html.length()-currentIdx);
			
			writer.write("</span>"); //$NON-NLS-1$
		}
	}
	
}
