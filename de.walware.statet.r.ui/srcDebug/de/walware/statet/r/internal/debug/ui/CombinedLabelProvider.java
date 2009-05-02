/*******************************************************************************
 * Copyright (c) 2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.debug.ui;

import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Image;

import de.walware.ecommons.ltk.IElementName;
import de.walware.ecommons.ltk.ui.IElementLabelProvider;

import de.walware.rj.data.RArray;
import de.walware.rj.data.RDataFrame;
import de.walware.rj.data.RDataUtil;
import de.walware.rj.data.REnvironment;
import de.walware.rj.data.RFactorStore;
import de.walware.rj.data.RList;
import de.walware.rj.data.RObject;
import de.walware.rj.data.RReference;
import de.walware.rj.data.RStore;

import de.walware.statet.r.core.data.ICombinedRElement;
import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.nico.RWorkspace.ICombinedEnvironment;
import de.walware.statet.r.ui.RLabelProvider;
import de.walware.statet.r.ui.RUI;


public class CombinedLabelProvider extends StyledCellLabelProvider {
	
	
	protected final RLabelProvider fParent;
	private final StringBuilder fTextBuilder = new StringBuilder(100);
	
	private StyledString.Styler fDefaultStyler;
	private boolean fLong;
	
	
	public CombinedLabelProvider() {
		this(false, false);
	}
	
	public CombinedLabelProvider(final boolean longLabel, final boolean title) {
		fLong = longLabel;
		if (title) {
			fDefaultStyler = IElementLabelProvider.TITLE_STYLER;
		}
		fParent = new RLabelProvider(title);
	}
	
	
	protected final StringBuilder getTextBuilder() {
		fTextBuilder.setLength(0);
		return fTextBuilder;
	}
	
	@Override
	public void update(final ViewerCell cell) {
		final Object element = cell.getElement();
		update(cell, element, null);
	}
	
	public void update(final ViewerCell cell, final Object element, final String name0) {
		Image image = null;
		final StyledString text = new StyledString();
		if (element instanceof ICombinedRElement) {
			final ICombinedRElement object = (ICombinedRElement) element;
			String name = name0;
			if (name == null) {
				name = object.getElementName().getDisplayName();
			}
			if (name == null) {
				name = ""; //$NON-NLS-1$
			}
			if (object.getRObjectType() == RObject.TYPE_REFERENCE) {
				final RObject realObject = ((RReference) object).getResolvedRObject();
				if (realObject != null) {
					update(cell, realObject, name);
					return;
				}
			}
			
			image = getImage(object);
			if (object.getRObjectType() == RObject.TYPE_ENV && name0 == null && object instanceof ICombinedEnvironment) {
				text.append(((ICombinedEnvironment) object).getName(), fDefaultStyler);
			}
			else {
				text.append(name, fDefaultStyler);
			}
			decorateStyledText(text, object, object.getAttributes());
		}
		else {
			text.append((element != null) ? element.toString() : ""); //$NON-NLS-1$
		}
		cell.setImage(image);
		cell.setText(text.getString());
		cell.setStyleRanges(text.getStyleRanges());
	}
	
	
	public Image getImage(final ICombinedRElement element) {
		switch (element.getRObjectType()) {
		case RObject.TYPE_NULL:
			return RUI.getImage(RUI.IMG_OBJ_NULL);
		case RObject.TYPE_FUNCTION:
			return fParent.getImage(element);
		case RObject.TYPE_LIST:
			return RUI.getImage(RUI.IMG_OBJ_LIST);
		case RObject.TYPE_DATAFRAME:
			return RUI.getImage(RUI.IMG_OBJ_DATAFRAME);
		case RObject.TYPE_VECTOR:
			return (element.getParent() != null && ((ICombinedRElement) element.getParent()).getRObjectType() == RObject.TYPE_DATAFRAME) ?
					RUI.getImage(RUI.IMG_OBJ_DATAFRAME_COLUMN) : RUI.getImage(RUI.IMG_OBJ_VECTOR);
		case RObject.TYPE_ARRAY:
			return RUI.getImage(RUI.IMG_OBJ_ARRAY);
		case RObject.TYPE_S4OBJECT:
			if (element.getData() != null) {
				return (element.getParent() != null && ((ICombinedRElement) element.getParent()).getRObjectType() == RObject.TYPE_DATAFRAME) ?
						RUI.getImage(RUI.IMG_OBJ_S4OBJ_DATAFRAME_COLUMN) : RUI.getImage(RUI.IMG_OBJ_S4OBJ_VECTOR);
			}
			return RUI.getImage(RUI.IMG_OBJ_S4OBJ);
		case RObject.TYPE_ENV: {
				final REnvironment renv = (REnvironment) element;
				switch (renv.getSpecialType()) {
				case REnvironment.ENVTYPE_GLOBAL:
					return RUI.getImage(RUI.IMG_OBJ_GLOBALENV);
				case REnvironment.ENVTYPE_BASE:
				case REnvironment.ENVTYPE_PACKAGE:
					return RUI.getImage(RUI.IMG_OBJ_PACKAGEENV);
				case REnvironment.ENVTYPE_EMTPY:
					return RUI.getImage(RUI.IMG_OBJ_EMPTYENV);
				}
			}
			return RUI.getImage(RUI.IMG_OBJ_OTHERENV);
		case RObject.TYPE_REFERENCE:
		default:
			return RUI.getImage(RUI.IMG_OBJ_GENERAL_VARIABLE);
		}
	}
	
	public StyledString getStyleString(final ICombinedRElement element, final RList attributes) {
		final IElementName elementName = element.getElementName();
		String name;
		final boolean envInfo;
		if (elementName.getType() == RElementName.MAIN_SEARCH_ENV
				&& elementName.getNextSegment() != null) {
			envInfo = true;
			name = elementName.getNextSegment().getDisplayName();
		}
		else {
			envInfo = false;
			name = elementName.getSegmentName();
		}
		if (name == null) {
			name = ""; //$NON-NLS-1$
		}
		final StyledString text = new StyledString(name, fDefaultStyler);
		decorateStyledText(text, element, attributes);
		if (fLong && envInfo) {
			final StringBuilder textBuilder = getTextBuilder();
			textBuilder.append(" - ");
			textBuilder.append(elementName.getSegmentName());
			text.append(textBuilder.toString(), StyledString.QUALIFIER_STYLER);
		}
		return text;
	}
	
	public void decorateStyledText(final StyledString text, final ICombinedRElement element, final RList elementAttr) {
		switch (element.getRObjectType()) {
		case RObject.TYPE_NULL:
			return;
		case RObject.TYPE_VECTOR:
			appendVectorDetail(text, element, elementAttr);
			return;
		case RObject.TYPE_ARRAY:
			appendArrayDetail(text, element, elementAttr);
			return;
		case RObject.TYPE_DATAFRAME:
			appendDataframeDetail(text, element, elementAttr);
			return;
		case RObject.TYPE_LIST:
			appendListDetail(text, element, elementAttr);
			return;
		case RObject.TYPE_ENV:
			appendEnvDetail(text, element, elementAttr);
			return;
		case RObject.TYPE_FUNCTION:
			fParent.decorateStyledText(text, element, elementAttr);
			return;
			
		case RObject.TYPE_S4OBJECT:
			appendS4ObjectDetail(text, element, elementAttr);
			return;
			
		case RObject.TYPE_REFERENCE:
		default:
			if (fLong) {
				appendLongClassInfo(text, element, elementAttr);
			}
			else {
				final StringBuilder textBuilder = getTextBuilder();
				textBuilder.append(" : ");
				textBuilder.append(element.getRClassName());
				text.append(textBuilder.toString(), StyledString.DECORATIONS_STYLER);
			}
			break;
		}
	}
	
	protected void appendLongClassInfo(final StyledString text, final ICombinedRElement element, final RList elementAttr) {
		final StringBuilder textBuilder = getTextBuilder();
		textBuilder.append(" : "); //$NON-NLS-1$
		if (elementAttr != null) {
			final RObject object = elementAttr.get("class");
			if (object != null && object.getRObjectType() == RObject.TYPE_VECTOR && object.getData().getStoreType() == RStore.CHARACTER) {
				final RStore data = object.getData();
				if (data.getLength() > 0) {
					final int last = data.getLength() - 1;
					for (int i = 0; i < last; i++) {
						if (!data.isNA(i)) {
							textBuilder.append(data.getChar(i));
							textBuilder.append(", "); //$NON-NLS-1$
						}
					}
					textBuilder.append(data.getChar(last));
				}
			}
		}
		if (textBuilder.length() == 3) {
			textBuilder.append(element.getRClassName());
		}
		if (element.getData() != null && element.getData().getStoreType() > 0) {
			textBuilder.append(" ("); //$NON-NLS-1$
			textBuilder.append(RDataUtil.getStoreMode(element.getData().getStoreType()));
			textBuilder.append(')');
		}
		text.append(textBuilder.toString(), StyledString.DECORATIONS_STYLER);
	}
	
	protected void appendVectorDetail(final StyledString text, final ICombinedRElement element, final RList elementAttr) {
		final int datatype = element.getData().getStoreType();
		final StringBuilder textBuilder;
		if (fLong) {
			appendLongClassInfo(text, element, elementAttr);
			textBuilder = getTextBuilder();
		}
		else {
			textBuilder = getTextBuilder();
			textBuilder.append(" : "); //$NON-NLS-1$
			if (element.getRClassName().equals(element.getData().getBaseVectorRClassName())) {
				textBuilder.append(RDataUtil.getStoreAbbr(element.getData()));
			}
			else {
				textBuilder.append(element.getRClassName());
				textBuilder.append(" ("); //$NON-NLS-1$
				textBuilder.append(RDataUtil.getStoreAbbr(element.getData()));
				textBuilder.append(')');
			}
		}
		textBuilder.append(" ["); //$NON-NLS-1$
		textBuilder.append(Integer.toString(element.getLength()));
		textBuilder.append(']');
		if (datatype == RStore.FACTOR) {
			textBuilder.append(" ("); //$NON-NLS-1$
			textBuilder.append(((RFactorStore) element.getData()).getLevelCount());
			textBuilder.append(" levels)");
		}
		text.append(textBuilder.toString(), StyledString.DECORATIONS_STYLER);
	}
	
	protected void appendArrayDetail(final StyledString text, final ICombinedRElement element, final RList elementAttr) {
		final StringBuilder textBuilder;
		if (fLong) {
			appendLongClassInfo(text, element, elementAttr);
			textBuilder = getTextBuilder();
		}
		else {
			textBuilder = getTextBuilder();
			textBuilder.append(" : "); //$NON-NLS-1$
			if (element.getRClassName().equals(RObject.CLASSNAME_ARRAY)
					|| element.getRClassName().equals(RObject.CLASSNAME_MATRIX)) {
				textBuilder.append(RDataUtil.getStoreAbbr(element.getData()));
			}
			else {
				textBuilder.append(element.getRClassName());
				textBuilder.append(" ("); //$NON-NLS-1$
				textBuilder.append(RDataUtil.getStoreAbbr(element.getData()));
				textBuilder.append(')');
			}
		}
		textBuilder.append(" ["); //$NON-NLS-1$
		final int[] dim = ((RArray<?>) element).getDim();
		if (dim.length > 0) {
			textBuilder.append(Integer.toString(dim[0]));
			for (int i = 1; i < dim.length; i++) {
				textBuilder.append('×');
				textBuilder.append(Integer.toString(dim[i]));
			}
		}
		textBuilder.append(']');
		text.append(textBuilder.toString(), StyledString.DECORATIONS_STYLER);
	}
	
	protected void appendListDetail(final StyledString text, final ICombinedRElement element, final RList elementAttr) {
		{	// count info
			final StringBuilder textBuilder = getTextBuilder();
			textBuilder.append(" ("); //$NON-NLS-1$
			textBuilder.append(element.getLength());
			textBuilder.append(" items)");
			text.append(textBuilder.toString(), StyledString.COUNTER_STYLER);
		}
		
		if (fLong) {
			appendLongClassInfo(text, element, elementAttr);
		}
		else {
			if (!element.getRClassName().equals(RObject.CLASSNAME_LIST)) {
				final StringBuilder textBuilder = getTextBuilder();
				textBuilder.setLength(0);
				textBuilder.append(" : "); //$NON-NLS-1$
				textBuilder.append(element.getRClassName());
				text.append(textBuilder.toString(), StyledString.DECORATIONS_STYLER);
			}
		}
	}
	
	protected void appendEnvDetail(final StyledString text, final ICombinedRElement element, final RList elementAttr) {
		final ICombinedEnvironment envir = (ICombinedEnvironment) element;
		if (envir.getSpecialType() != REnvironment.ENVTYPE_AUTOLOADS) { // count info
			final String countInfo = getEnvCountInfo(envir);
			text.append(countInfo, StyledString.COUNTER_STYLER);
		}
		
		if (fLong) {
			appendLongClassInfo(text, element, elementAttr);
		}
	}
	
	protected String getEnvCountInfo(final ICombinedEnvironment envir) {
		final StringBuilder textBuilder = getTextBuilder();
		textBuilder.append(" ("); //$NON-NLS-1$
		textBuilder.append(envir.getLength());
		textBuilder.append(')');
		return textBuilder.toString();
	}
	
	protected void appendDataframeDetail(final StyledString text, final ICombinedRElement element, final RList elementAttr) {
		final RDataFrame dataframe = (RDataFrame) element;
		{	// count info
			final StringBuilder textBuilder = getTextBuilder();
			textBuilder.append(" ("); //$NON-NLS-1$
			textBuilder.append(Integer.toString(dataframe.getColumnCount()));
			textBuilder.append(" var.)");
			text.append(textBuilder.toString(), StyledString.COUNTER_STYLER);
		}
		
		final StringBuilder textBuilder;
		if (fLong) {
			appendLongClassInfo(text, element, elementAttr);
			textBuilder = getTextBuilder();
		}
		else {
			textBuilder = getTextBuilder();
			if (!element.getRClassName().equals(RObject.CLASSNAME_DATAFRAME)) {
				textBuilder.append(" : "); //$NON-NLS-1$
				textBuilder.append(dataframe.getRClassName());
			}
		}
		textBuilder.append(" ["); //$NON-NLS-1$
		textBuilder.append(Integer.toString(dataframe.getRowCount()));
		textBuilder.append(']');
		text.append(textBuilder.toString(), StyledString.DECORATIONS_STYLER);
	}
	
	protected void appendS4ObjectDetail(final StyledString text, final ICombinedRElement element, final RList elementAttr) {
		if (fLong) {
			appendLongClassInfo(text, element, elementAttr);
		}
		else {
			final StringBuilder textBuilder = getTextBuilder();
			textBuilder.append(" : "); //$NON-NLS-1$
			textBuilder.append(element.getRClassName());
			text.append(textBuilder.toString(), StyledString.DECORATIONS_STYLER);
		}
	}
	
}