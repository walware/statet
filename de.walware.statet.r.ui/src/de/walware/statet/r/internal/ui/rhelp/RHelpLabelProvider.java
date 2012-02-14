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

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Image;

import de.walware.ecommons.ltk.ui.IElementLabelProvider;
import de.walware.ecommons.ui.SharedUIResources;

import de.walware.statet.r.core.renv.IREnv;
import de.walware.statet.r.core.renv.IREnvConfiguration;
import de.walware.statet.r.core.rhelp.IRHelpKeyword;
import de.walware.statet.r.core.rhelp.IRHelpPage;
import de.walware.statet.r.core.rhelp.IRHelpSearchMatch;
import de.walware.statet.r.core.rhelp.IRHelpSearchMatch.MatchFragment;
import de.walware.statet.r.core.rhelp.IRPackageHelp;
import de.walware.statet.r.ui.RUI;


public class RHelpLabelProvider extends StyledCellLabelProvider implements ILabelProvider {
	
	
	public static final String HIGHLIGHT_BG_COLOR_NAME = "org.eclipse.jdt.ui.ColoredLabels.match_highlight"; //$NON-NLS-1$
	
	public static final Styler HIGHLIGHT_STYLE = StyledString.createColorRegistryStyler(null, HIGHLIGHT_BG_COLOR_NAME);
	
	private static final String TITLE_SEP = " – "; //$NON-NLS-1$
	
	public static final int WITH_TITLE = 0x1;
	public static final int WITH_QUALIFIER = 0x2;
	
	public static final int HEADER = 0x20;
	public static final int TOOLTIP = 0x10;
	
	
	public static void append(final StyledString string, final MatchFragment fragment) {
		final String text = fragment.getText();
		int startIdx = 0;
		int endIdx = 0;
		while ((startIdx = text.indexOf(IRHelpSearchMatch.PRE_TAGS_PREFIX, endIdx)) >= 0) {
			if (startIdx > endIdx) {
				string.append(text.substring(endIdx, startIdx));
			}
			final int m = (text.charAt(startIdx+8)) - ('A');
			startIdx += 10;
			endIdx = text.indexOf(IRHelpSearchMatch.POST_TAGS[m], startIdx);
			if (endIdx < 0) {
				return;
			}
			string.append(text.substring(startIdx, endIdx), HIGHLIGHT_STYLE);
			endIdx += 11;
		}
		if (endIdx < text.length()) {
			string.append(text.substring(endIdx));
		}
	}
	
	
	private boolean fTooltip;
	private final boolean fWithTitle;
	private boolean fWithQualifier;
	
	private Object fFocusObject;
	
	private final Styler fDefaultStyler;
	
	private Image fPackageImage;
	private Image fPageImage;
	private Image fLineImage;
	
	
	public RHelpLabelProvider() {
		this(WITH_TITLE);
	}
	
	public RHelpLabelProvider(final int style) {
		fTooltip = ((style & TOOLTIP) != 0);;
		fWithQualifier = ((style & WITH_QUALIFIER) != 0);
		fWithTitle = ((style & (WITH_TITLE | TOOLTIP)) != 0);
		if ((style & HEADER) != 0) {
			fDefaultStyler = IElementLabelProvider.TITLE_STYLER;
		}
		else {
			fDefaultStyler = null;
		}
	}
	
	
	@Override
	public void initialize(final ColumnViewer viewer, final ViewerColumn column) {
		super.initialize(viewer, column);
		
		if (viewer instanceof TableViewer) {
			fWithQualifier = true;
		}
	}
	
	@Override
	public void dispose() {
		super.dispose();
		fPackageImage = null;
		fPageImage = null;
		fLineImage = null;
	}
	
	public void setFocusObject(final Object object) {
		fFocusObject = object;
	}
	
	
	@Override
	public Image getImage(Object element) {
		if (element instanceof RHelpSearchUIMatch) {
			element = ((RHelpSearchUIMatch) element).getRHelpMatch().getPage();
		}
		if (element instanceof IRHelpPage) {
			if (fPageImage == null || fPageImage.isDisposed()) {
				fPageImage = RUI.getImage(RUI.IMG_OBJ_R_HELP_PAGE);
			}
			return fPageImage;
		}
		else if (element instanceof IRPackageHelp) {
			if (fPackageImage == null || fPackageImage.isDisposed()) {
				fPackageImage = RUI.getImage(RUI.IMG_OBJ_R_PACKAGE);
			}
			return fPackageImage;
		}
		else if (element instanceof IREnvConfiguration || element instanceof IREnv) {
			return RUI.getImage(RUI.IMG_OBJ_R_RUNTIME_ENV);
		}
		else {
			return null;
		}
	}
	
	@Override
	public String getText(Object element) {
		if (element instanceof RHelpSearchUIMatch) {
			element = ((RHelpSearchUIMatch) element).getRHelpMatch().getPage();
		}
		if (element instanceof IRHelpPage) {
			final StringBuilder sb = new StringBuilder(32);
			final IRHelpPage page = (IRHelpPage) element;
			sb.append(page.getName());
			if (fTooltip) {
				sb.append(" {"); //$NON-NLS-1$
				sb.append(page.getPackage().getName());
				sb.append("}\n"); //$NON-NLS-1$
				sb.append(page.getTitle());
			}
			else {
				if (fWithTitle && page.getTitle().length() > 0) {
					sb.append(TITLE_SEP);
					sb.append(page.getTitle());
				}
			}
			return sb.toString();
		}
		else if (element instanceof IRPackageHelp) {
			final StringBuilder sb = new StringBuilder(32);
			final IRPackageHelp packageHelp = (IRPackageHelp) element;
			if (fTooltip) {
				sb.append(packageHelp.getName());
				sb.append(" ["); //$NON-NLS-1$
				sb.append(packageHelp.getVersion());
				sb.append("]\n"); //$NON-NLS-1$
				sb.append(packageHelp.getTitle());
			}
			else {
				sb.append(packageHelp.getName());
				if ((element == fFocusObject)
						&& packageHelp.getTitle().length() > 0) {
					sb.append(TITLE_SEP);
					sb.append(packageHelp.getTitle());
				}
			}
			return sb.toString();
		}
		else if (element instanceof IREnvConfiguration) {
			final IREnvConfiguration rEnv = (IREnvConfiguration) element;
			return rEnv.getName();
		}
		else if (element instanceof IREnv) {
			final IREnv rEnv = (IREnv) element;
			final String name = rEnv.getName();
			return (name != null) ? name : ""; //$NON-NLS-1$
		}
		else if (element instanceof IRHelpKeyword.Group) {
			final IRHelpKeyword.Group group = (IRHelpKeyword.Group) element;
			return group.getLabel() + TITLE_SEP + group.getDescription();
		}
		else if (element instanceof IRPackageHelp) {
			final IRHelpKeyword keyword = (IRHelpKeyword) element;
			return keyword.getKeyword() + TITLE_SEP + keyword.getDescription();
		}
		else if (element instanceof IRHelpSearchMatch.MatchFragment) {
			final IRHelpSearchMatch.MatchFragment fragment = (MatchFragment) element;
			return IRHelpSearchMatch.ALL_TAGS_PATTERN.matcher(fragment.getText())
					.replaceAll(""); //$NON-NLS-1$
		}
		else if (element instanceof Object[]) {
			final Object[] array = (Object[]) element;
			return array[array.length-1].toString();
		}
		else {
			return element.toString();
		}
	}
	
	@Override
	public boolean useNativeToolTip(final Object object) {
		return true;
	}
	
	@Override
	public String getToolTipText(final Object element) {
		final boolean wasTooltip = fTooltip;
		try {
			fTooltip = true;
			return getText(element);
		}
		finally {
			fTooltip = wasTooltip;
		}
	}
	
	public StyledString getStyledText(final Object element) {
		final StyledString text = new StyledString();
		
		if (element instanceof RHelpSearchUIMatch) {
			append(text, ((RHelpSearchUIMatch) element).getRHelpMatch());
		}
		else if (element instanceof IRHelpPage) {
			append(text, (IRHelpPage) element);
		}
		else if (element instanceof IRPackageHelp) {
			append(text, (IRPackageHelp) element);
		}
		else if (element instanceof IREnvConfiguration) {
			final IREnvConfiguration rEnv = (IREnvConfiguration) element;
			text.append(rEnv.getName());
		}
		else if (element instanceof IREnv) {
			final IREnv rEnv = (IREnv) element;
			append(text, rEnv);
		}
		else if (element instanceof Object[]) {
			final Object[] array = (Object[]) element;
			text.append(array[array.length-1].toString());
		}
		else {
			text.append(element.toString());
		}
		
		return text;
	}
	
	protected void append(final StyledString text, final IRHelpSearchMatch match) {
		final IRHelpPage page = match.getPage();
		text.append(page.getName(), fDefaultStyler);
		if (fWithTitle && page.getTitle().length() > 0) {
			text.append(TITLE_SEP, fDefaultStyler);
			text.append(page.getTitle(), fDefaultStyler);
		}
		if (fWithQualifier) {
			text.append(" - ", StyledString.QUALIFIER_STYLER); //$NON-NLS-1$
			text.append(page.getPackage().getName(), StyledString.QUALIFIER_STYLER);
		}
		else if (match.getMatchesCount() > 0){
			text.append(" (", StyledString.COUNTER_STYLER); //$NON-NLS-1$
			text.append(Integer.toString(match.getMatchesCount()), StyledString.COUNTER_STYLER);
			text.append(")", StyledString.COUNTER_STYLER); //$NON-NLS-1$
		}
	}
	
	protected void append(final StyledString text, final IRHelpPage page) {
		text.append(page.getName(), fDefaultStyler);
		if (fWithTitle && page.getTitle().length() > 0) {
			text.append(TITLE_SEP, fDefaultStyler);
			text.append(page.getTitle(), fDefaultStyler);
		}
		if (fWithQualifier) {
			text.append(" - ", StyledString.QUALIFIER_STYLER); //$NON-NLS-1$
			text.append(page.getPackage().getName(), StyledString.QUALIFIER_STYLER);
		}
	}
	
	protected void append(final StyledString text, final IRPackageHelp packageHelp) {
		text.append(packageHelp.getName(), fDefaultStyler);
		if (packageHelp == fFocusObject && packageHelp.getTitle().length() > 0) {
			text.append(TITLE_SEP, fDefaultStyler);
			text.append(packageHelp.getTitle(), fDefaultStyler);
		}
	}
	
	protected void append(final StyledString text, final IREnv rEnv) {
		final String name = rEnv.getName();
		text.append((name != null) ? name : "", fDefaultStyler); //$NON-NLS-1$
	}
	
	
	@Override
	public void update(final ViewerCell cell) {
		final StyledString text = new StyledString();
		Image image = null;
		
		final Object element = cell.getElement();
		if (element instanceof RHelpSearchUIMatch) {
			final IRHelpSearchMatch match = ((RHelpSearchUIMatch) element).getRHelpMatch();
			if (fPageImage == null) {
				fPageImage = RUI.getImage(RUI.IMG_OBJ_R_HELP_PAGE);
			}
			image = fPageImage;
			append(text, match);
		}
		else if (element instanceof IRHelpPage) {
			final IRHelpPage page = (IRHelpPage) element;
			if (fPageImage == null) {
				fPageImage = RUI.getImage(RUI.IMG_OBJ_R_HELP_PAGE);
			}
			image = fPageImage;
			append(text, page);
		}
		else if (element instanceof IRPackageHelp) {
			final IRPackageHelp packageHelp = (IRPackageHelp) element;
			if (fPackageImage == null) {
				fPackageImage = RUI.getImage(RUI.IMG_OBJ_R_PACKAGE);
			}
			image = fPackageImage;
			append(text, packageHelp);
		}
		else if (element instanceof IREnvConfiguration) {
			final IREnvConfiguration rEnv = (IREnvConfiguration) element;
			image = RUI.getImage(RUI.IMG_OBJ_R_RUNTIME_ENV);
			text.append(rEnv.getName());
		}
		else if (element instanceof IREnv) {
			final IREnv rEnv = (IREnv) element;
			image = RUI.getImage(RUI.IMG_OBJ_R_RUNTIME_ENV);
			append(text, rEnv);
		}
		else if (element instanceof IRHelpKeyword.Group) {
			final IRHelpKeyword.Group group = (IRHelpKeyword.Group) element;
			text.append(group.getLabel());
			text.append(" - " + group.getDescription(), StyledString.QUALIFIER_STYLER); //$NON-NLS-1$
		}
		else if (element instanceof IRHelpKeyword) {
			final IRHelpKeyword keyword = (IRHelpKeyword) element;
			text.append(keyword.getKeyword());
			text.append(" - " + keyword.getDescription(), StyledString.QUALIFIER_STYLER); //$NON-NLS-1$
		}
		else if (element instanceof IRHelpSearchMatch.MatchFragment) {
			final IRHelpSearchMatch.MatchFragment fragment = (MatchFragment) element;
			if (fLineImage == null) {
				fLineImage = SharedUIResources.getImages().get(SharedUIResources.OBJ_LINE_MATCH_IMAGE_ID);
			}
			image = fLineImage;
			text.append(fragment.getFieldLabel(), StyledString.QUALIFIER_STYLER);
			text.append(": ", StyledString.QUALIFIER_STYLER); //$NON-NLS-1$
			append(text, fragment);
		}
		else if (element instanceof Object[]) {
			final Object[] array = (Object[]) element;
			text.append(array[array.length-1].toString());
		}
		else {
			text.append(element.toString());
		}
		
		cell.setImage(image);
		cell.setText(text.getString());
		cell.setStyleRanges(text.getStyleRanges());
		
		super.update(cell);
	}
	
	@Override
	protected StyleRange prepareStyleRange(StyleRange styleRange, final boolean applyColors) {
		if (!applyColors && styleRange.background != null) {
			styleRange = super.prepareStyleRange(styleRange, applyColors);
			styleRange.borderStyle = SWT.BORDER_DOT;
			return styleRange;
		}
		return super.prepareStyleRange(styleRange, applyColors);
	}
	
}
