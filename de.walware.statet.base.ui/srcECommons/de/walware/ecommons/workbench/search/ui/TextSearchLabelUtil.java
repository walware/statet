/*=============================================================================#
 # Copyright (c) 2014-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.ecommons.workbench.search.ui;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.search.ui.text.Match;

import de.walware.ecommons.workbench.ui.DecoratingStyledLabelProvider;


public class TextSearchLabelUtil {
	
	
	public static final String HIGHLIGHT_COLOR_KEY= "org.eclipse.search.ui.match.highlight"; //$NON-NLS-1$
	
	public static final Styler HIGHLIGHT_STYLE= StyledString.createColorRegistryStyler(null, HIGHLIGHT_COLOR_KEY);
	
	public static final Collection<String> DEFAULT_SEARCH_LABEL_PROPERTIES;
	
	static {
		final Set<String> properties= new HashSet<>();
		properties.addAll(DecoratingStyledLabelProvider.DEFAULT_UPDATE_PROPERTIES);
		properties.add(TextSearchLabelUtil.HIGHLIGHT_COLOR_KEY);
		DEFAULT_SEARCH_LABEL_PROPERTIES= Collections.unmodifiableSet(properties);
	}
	
	
	private static final int MAX_SHOWN_LINE= 200;
	private static final int MIN_SHOWN_CONTEXT= 20;
	
	private static final String ELLIPSIS= " ... "; //$NON-NLS-1$
	
	
	public TextSearchLabelUtil() {
	}
	
	
	private int findStart(final String text, int offset, final int maxCount) {
		final int end= Math.min(offset + maxCount, text.length());
		while (offset < end) {
			final char c= text.charAt(offset);
			if (Character.isWhitespace(c)) {
				offset++;
			}
			else {
				break;
			}
		}
		return offset;
	}
	
	private int findEnd(final String text, int offset, final int maxCount) {
		final int start= Math.min(offset - maxCount, 0);
		while (offset > start) {
			final char c= text.charAt(offset - 1);
			if (Character.isWhitespace(c)) {
				offset--;
			}
			else {
				break;
			}
		}
		return offset;
	}
	
	public StyledString getStyledText(final LineElement<?> lineElement, final List<? extends Match> matches) {
		final int lineNumber= lineElement.getLine();
		
		final StyledString text= new StyledString(lineNumber + ": ", StyledString.QUALIFIER_STYLER); //$NON-NLS-1$
		
		final String lineText= lineElement.getText();
		int idx= findStart(lineText, 0, lineText.length()); // index in lineText
		
		for (int i= 0; i < matches.size() && text.length() < MAX_SHOWN_LINE; i++) {
			final Match match= matches.get(i);
			final int matchStart= Math.max(match.getOffset() - lineElement.getOffset(), 0);
			final int matchEnd= Math.min(match.getOffset() + match.getLength() - lineElement.getOffset(), lineElement.getLength());
			
			if (matchStart - idx < MIN_SHOWN_CONTEXT * 2 + 10) {
				text.append(lineText.substring(idx, matchStart));
			}
			else {
				text.append(lineText.substring(
						idx,
						findEnd(lineText, idx + MIN_SHOWN_CONTEXT, MIN_SHOWN_CONTEXT) ));
				text.append(ELLIPSIS, StyledString.QUALIFIER_STYLER);
				text.append(lineText.substring(
						findStart(lineText, matchStart - MIN_SHOWN_CONTEXT, MIN_SHOWN_CONTEXT),
						matchStart ));
			}
			
			text.append(lineText.substring(matchStart, matchEnd), TextSearchLabelUtil.HIGHLIGHT_STYLE);
			idx= matchEnd;
		}
		
		if (idx < lineText.length()) {
			if (lineText.length() - idx < MIN_SHOWN_CONTEXT + 10) {
				text.append(lineText.substring(idx, lineText.length()));
			}
			else {
				text.append(lineText.substring(
						idx,
						findEnd(lineText, idx + MIN_SHOWN_CONTEXT, MIN_SHOWN_CONTEXT) ));
				text.append(ELLIPSIS, StyledString.QUALIFIER_STYLER);
			}
		}
		
		return text;
	}
	
}
