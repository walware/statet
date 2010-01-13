/*******************************************************************************
 * Copyright (c) 2007-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.ui;

import java.util.List;

import org.apache.commons.collections.primitives.IntList;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.graphics.Image;

import de.walware.ecommons.ltk.IElementName;
import de.walware.ecommons.ltk.IModelElement;
import de.walware.ecommons.ltk.ui.IElementLabelProvider;

import de.walware.statet.base.ui.StatetImages;

import de.walware.rj.data.RArray;
import de.walware.rj.data.RDataFrame;
import de.walware.rj.data.RDataUtil;
import de.walware.rj.data.REnvironment;
import de.walware.rj.data.RFactorStore;
import de.walware.rj.data.RIntegerStore;
import de.walware.rj.data.RList;
import de.walware.rj.data.RObject;
import de.walware.rj.data.RReference;
import de.walware.rj.data.RStore;

import de.walware.statet.r.core.data.ICombinedRElement;
import de.walware.statet.r.core.model.ArgsDefinition;
import de.walware.statet.r.core.model.IRClass;
import de.walware.statet.r.core.model.IRClassExtension;
import de.walware.statet.r.core.model.IRElement;
import de.walware.statet.r.core.model.IRFrame;
import de.walware.statet.r.core.model.IRMethod;
import de.walware.statet.r.core.model.IRSlot;
import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.core.model.ArgsDefinition.Arg;
import de.walware.statet.r.nico.RWorkspace.ICombinedEnvironment;


/**
 * Label Provider for R elements
 */
public class RLabelProvider extends StyledCellLabelProvider implements IElementLabelProvider, ILabelProvider {
	
	
	public static final int NAMESPACE = 0x01;
	public static final int TITLE =     0x02;
	public static final int LONG =      0x04;
	public static final int COUNT =     0x08;
	public static final int ASSIST =    0x10;
	
	
	private final StringBuilder fTextBuilder = new StringBuilder(100);
	private final Styler fDefaultStyler;
	
	private int fStyle;
	
	
	public RLabelProvider() {
		this(0);
	}
	
	public RLabelProvider(final int style) {
		if ((fStyle & LONG) != 0) {
			fStyle |= COUNT;
		}
		fStyle = style;
		if ((fStyle & TITLE) != 0) {
			fDefaultStyler = IElementLabelProvider.TITLE_STYLER;
		}
		else {
			fDefaultStyler = null;
		}
	}
	
	
	protected final StringBuilder getTextBuilder() {
		fTextBuilder.setLength(0);
		return fTextBuilder;
	}
	
	
	public Image getImage(final IModelElement element) {
		final IModelElement modelElement = element;
		switch (modelElement.getElementType() & IModelElement.MASK_C1) {
		
		case IModelElement.C1_SOURCE:
			return RUI.getImage(RUI.IMG_OBJ_R_SCRIPT);
			
		case IModelElement.C1_IMPORT:
			return StatetImages.getImage(StatetImages.OBJ_IMPORT);
			
		case IModelElement.C1_METHOD:
			switch (modelElement.getElementType() & IModelElement.MASK_C2) {
			case IRElement.R_GENERIC_FUNCTION:
				return RUI.getImage(RUI.IMG_OBJ_GENERIC_FUNCTION);
			case IRElement.R_S4METHOD:
				return RUI.getImage(RUI.IMG_OBJ_METHOD);
//				case IRLangElement.R_COMMON_FUNCTION:
			default:
				return RUI.getImage( ((modelElement.getElementType() & 0xf) != 0x1) ?
						RUI.IMG_OBJ_COMMON_FUNCTION : RUI.IMG_OBJ_COMMON_LOCAL_FUNCTION);
			}
		case IModelElement.C1_CLASS:
			switch (modelElement.getElementType() & IModelElement.MASK_C2) {
			case IRElement.R_S4CLASS:
				return StatetImages.getImage(StatetImages.OBJ_CLASS);
			case IRElement.R_S4CLASS_EXTENSION:
				return StatetImages.getImage(StatetImages.OBJ_CLASS_EXT);
			default:
				return null;
			}
		
		case IModelElement.C1_VARIABLE:
			if (element instanceof ICombinedRElement) {
				return getImage((ICombinedRElement) element);
			}
			switch (modelElement.getElementType() & IModelElement.MASK_C2) {
			case IRElement.R_S4SLOT:
				return RUI.getImage(RUI.IMG_OBJ_SLOT);
//				case IRLangElement.R_COMMON_VARIABLE:
			default:
				return RUI.getImage( ((modelElement.getElementType() & 0xf) != 0x1) ?
						RUI.IMG_OBJ_GENERAL_VARIABLE : RUI.IMG_OBJ_GENERAL_LOCAL_VARIABLE);
			}
		
		default:
			return null;
		}
	}
	
	private Image getImage(final ICombinedRElement element) {
		switch (element.getRObjectType()) {
		case RObject.TYPE_NULL:
			return RUI.getImage(RUI.IMG_OBJ_NULL);
		case RObject.TYPE_FUNCTION:
			throw new IllegalArgumentException();
		case RObject.TYPE_LIST:
			return RUI.getImage(RUI.IMG_OBJ_LIST);
		case RObject.TYPE_DATAFRAME:
			return RUI.getImage(RUI.IMG_OBJ_DATAFRAME);
		case RObject.TYPE_VECTOR:
			return (element.getModelParent() != null && element.getModelParent().getRObjectType() == RObject.TYPE_DATAFRAME) ?
					RUI.getImage(RUI.IMG_OBJ_DATAFRAME_COLUMN) : RUI.getImage(RUI.IMG_OBJ_VECTOR);
		case RObject.TYPE_ARRAY:
			return RUI.getImage(RUI.IMG_OBJ_ARRAY);
		case RObject.TYPE_S4OBJECT:
			if (element.getData() != null) {
				return (element.getModelParent() != null && element.getModelParent().getRObjectType() == RObject.TYPE_DATAFRAME) ?
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
	
	
	public String getText(final IModelElement element) {
		final String name = element.getElementName().getDisplayName();
		
		final StringBuilder text;
		switch (element.getElementType() & IModelElement.MASK_C1) {
		
		case IModelElement.C1_METHOD:
			text = (name != null) ? new StringBuilder(name) : new StringBuilder();
			appendMethodDetail(text, (IRMethod) element);
			return text.toString();
		
		case IModelElement.C1_CLASS:
			switch (element.getElementType() & IModelElement.MASK_C2) {
			case IRElement.R_S4CLASS_EXTENSION:
				text = (name != null) ? new StringBuilder(name) : new StringBuilder();
				appendClassExtDetail(text, (IRClassExtension) element);
				return text.toString();
			}
		
		case IModelElement.C1_VARIABLE:
			switch (element.getElementType() & IModelElement.MASK_C2) {
			case IRElement.R_S4SLOT:
				text = (name != null) ? new StringBuilder(name) : new StringBuilder();
				appendSlotDetail(text, (IRSlot) element);
				return text.toString();
			}
		
		}
		return name;
	}
	
//	public void decorateText(final StringBuilder text, final IModelElement element) {
//		switch (element.getElementType() & IModelElement.MASK_C1) {
//		
//		case IModelElement.C1_METHOD:
//			appendMethodDetail(text, (IRMethod) element);
//			return;
//		
//		case IModelElement.C1_CLASS:
//			switch (element.getElementType() & IModelElement.MASK_C2) {
//			case IRLangElement.R_S4CLASS_EXTENSION:
//				appendClassExtDetail(text, (IRClassExtension) element);
//				return;
//			}
//		
//		case IModelElement.C1_VARIABLE:
//			switch (element.getElementType() & IModelElement.MASK_C2) {
//			case IRLangElement.R_S4SLOT:
//				appendSlotDetail(text, (IRSlot) element);
//				return;
//			}
//		
//		}
//	}
	
	public StyledString getStyledText(final IModelElement element) {
		return getStyledText(element, element.getElementName(), null);
	}
	
	public StyledString getStyledText(final IModelElement element, final IElementName elementName, final RList elementAttr) {
		final IElementName elementName0 = (elementName != null) ? elementName : element.getElementName();
		final StyledString text = new StyledString();
		final String name;
		
		switch (element.getElementType() & IModelElement.MASK_C1) {
		
		case IModelElement.C1_METHOD:
			if (fStyle != 0 && elementName0.getNextSegment() == null) {
				final String segmentName = elementName0.getSegmentName();
				int length;
				if (segmentName != null && ((length = segmentName.length()) > 2)
						&& (segmentName.charAt(length-2) == '<') && (segmentName.charAt(length-1) == '-')) {
					text.append(RElementName.create(RElementName.MAIN_DEFAULT, segmentName.substring(0, length-2)).getDisplayName(), fDefaultStyler);
					appendMethodDetailAssignmentSpecial(text,  (IRMethod) element);
					text.append(" <- value");
					break;
				}
			}
			name = elementName0.getDisplayName();
			if (name != null) {
				text.append(name, fDefaultStyler);
			}
			appendMethodDetail(text, (IRMethod) element);
			break;
		
		case IModelElement.C1_CLASS:
			name = elementName0.getDisplayName();
			if (name != null) {
				text.append(name, fDefaultStyler);
			}
			switch (element.getElementType() & IModelElement.MASK_C2) {
			case IRElement.R_S4CLASS_EXTENSION:
				appendClassExtDetail(text, (IRClassExtension) element);
			}
			break;
		
		case IModelElement.C1_VARIABLE:
			if (element instanceof ICombinedRElement) {
				if (elementName == null && ((ICombinedRElement) element).getRObjectType() == RObject.TYPE_ENV) {
					text.append(elementName0.getSegmentName(), fDefaultStyler);
				}
				else {
					name = elementName0.getDisplayName();
					if (name != null) {
						text.append(name, fDefaultStyler);
					}
				}
				decorateStyledText(text, (ICombinedRElement) element, elementName0, elementAttr);
				break;
			}
			name = elementName0.getDisplayName();
			if (name != null) {
				text.append(name, fDefaultStyler);
			}
			switch (element.getElementType() & IModelElement.MASK_C2) {
			case IRElement.R_S4SLOT:
				appendSlotDetail(text, (IRSlot) element);
			}
			break;
			
		default:
			name = elementName0.getDisplayName();
			if (name != null) {
				text.append(name, fDefaultStyler);
			}
			break;
		
		}
		
		if ((fStyle & NAMESPACE) != 0) {
			IElementName namespace = null;
			if (elementName0 != null) {
				namespace = elementName0.getNamespace();
			}
			if (namespace == null) {
				final IModelElement parent = element.getModelParent();
				if (parent != null) {
					final IRFrame frame = (IRFrame) parent.getAdapter(IRFrame.class);
					if (frame != null) {
						namespace = frame.getElementName();
					}
				}
			}
			if (namespace != null) {
				final StringBuilder textBuilder = getTextBuilder();
				textBuilder.append(" - "); //$NON-NLS-1$
				textBuilder.append(namespace.getDisplayName());
				text.append(textBuilder.toString(), StyledString.QUALIFIER_STYLER);
			}
		}
		return text;
	}
	
	private StyledString decorateStyledText(final StyledString text, final ICombinedRElement element, final IElementName elementName, final RList elementAttr) {
		switch (element.getRObjectType()) {
		case RObject.TYPE_NULL:
			break;
		case RObject.TYPE_FUNCTION:
			throw new IllegalArgumentException();
		case RObject.TYPE_VECTOR:
			appendVectorDetail(text, element, elementAttr);
			break;
		case RObject.TYPE_ARRAY:
			appendArrayDetail(text, element, elementAttr);
			break;
		case RObject.TYPE_DATAFRAME:
			appendDataframeDetail(text, element, elementAttr);
			break;
		case RObject.TYPE_LIST:
			appendListDetail(text, element, elementAttr);
			break;
		case RObject.TYPE_ENV:
			appendEnvDetail(text, element, elementAttr);
			break;
		case RObject.TYPE_S4OBJECT:
			appendS4ObjectDetail(text, element, elementAttr);
			break;
			
		case RObject.TYPE_REFERENCE:
		default:
			if ((fStyle & LONG) != 0) {
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
		
		return text;
	}
	
//	public void decorateStyledText(final StyledString text, final IModelElement element) {
//		decorateStyledText(text, element, null);
//	}
//	
//	public void decorateStyledText(final StyledString text, final IModelElement element, final RList elementAttr) {
//		switch (element.getElementType() & IModelElement.MASK_C1) {
//		
//		case IModelElement.C1_METHOD:
//			appendMethodDetail(text, (IRMethod) element);
//			break;
//		
//		case IModelElement.C1_CLASS:
//			switch (element.getElementType() & IModelElement.MASK_C2) {
//			case IRLangElement.R_S4CLASS_EXTENSION:
//				appendClassExtDetail(text, (IRClassExtension) element);
//				break;
//			}
//		
//		case IModelElement.C1_VARIABLE:
//			switch (element.getElementType() & IModelElement.MASK_C2) {
//			case IRLangElement.R_S4SLOT:
//				appendSlotDetail(text, (IRSlot) element);
//				break;
//			}
//			
//		}
//	}
	
	
	public Image getImage(final Object element) {
		if (element instanceof ICombinedRElement) {
			return getImage((ICombinedRElement) element);
		}
		if (element instanceof IModelElement) {
			return getImage((IModelElement) element);
		}
		return null;
	}
	
	public String getText(final Object element) {
		if (element instanceof ICombinedRElement) {
			return getText((ICombinedRElement) element);
		}
		if (element instanceof IModelElement) {
			return getText((IModelElement) element);
		}
		return null;
	}
	
	@Override
	public void update(final ViewerCell cell) {
		final Object cellElement = cell.getElement();
		if (cellElement instanceof IModelElement) {
			update(cell, (IModelElement) cellElement);
		}
		else {
			updateUnknownElement(cell);
		}
		super.update(cell);
	}
	
	public void update(final ViewerCell cell, final IModelElement element) {
		Image image = null;
		StyledString styledText = null;
		
		if (element instanceof ICombinedRElement) {
			final ICombinedRElement combined = (ICombinedRElement) element;
			if (combined.getRObjectType() == RObject.TYPE_REFERENCE) {
				final RObject realObject = ((RReference) element).getResolvedRObject();
				if (realObject instanceof ICombinedRElement) {
					image = getImage((ICombinedRElement) realObject);
					styledText = getStyledText((ICombinedRElement) realObject, element.getElementName(), null);
				}
			}
		}
		
		if (image == null) {
			image = getImage(element);
		}
		cell.setImage(image);
		
		if (styledText == null) {
			styledText = getStyledText(element, null, null);
		}
		if (styledText != null) {
			cell.setText(styledText.getString());
			cell.setStyleRanges(styledText.getStyleRanges());
		}
		else {
			cell.setText(getText(element));
			cell.setStyleRanges(null);
		}
	}
	
	protected void updateUnknownElement(final ViewerCell cell) {
		cell.setImage(null);
		cell.setText(cell.getElement().toString());
		cell.setStyleRanges(null);
	}
	
	
	@Override
	public String getToolTipText(final Object element) {
		final StringBuilder text;
		
		if (element instanceof IModelElement) {
			final IModelElement modelElement = (IModelElement) element;
			switch (modelElement.getElementType() & IModelElement.MASK_C1) {
			
			case IModelElement.C1_METHOD: {
				text = new StringBuilder();
				appendMethodMLText(text, (IRMethod) modelElement);
				return text.toString(); }
			
			case IModelElement.C1_CLASS:
				switch (modelElement.getElementType() & IModelElement.MASK_C2) {
				case IRElement.R_S4CLASS:
					text = new StringBuilder();
					appendClassMLText(text, (IRClass) modelElement);
					return text.toString(); }
				case IRElement.R_S4CLASS_EXTENSION: {
					text = new StringBuilder();
					appendClassExtMLText(text, (IRClassExtension) modelElement);
					return text.toString(); }
				default:
					return null;
			}
		}
		return null;
	}
	
	protected void appendMethodDetail(final StringBuilder text, final IRMethod method) {
		final ArgsDefinition args = method.getArgsDefinition();
		text.append('(');
		if (args != null && args.size() > 0) {
			final int last = args.size() - 1;
			for (int i = 0; i < last; i++) {
				appendArg(text, args.get(i));
				text.append(", "); 
			}
			appendArg(text, args.get(last));
		}
		text.append(')');
	}
	
	public void appendArgumentInformation(final StringBuilder text, final IntList idxs, final ArgsDefinition args) {
		if (args != null) {
			if (args.size() == 0) {
				text.append("<no arguments>");
			}
			else {
				final int last = args.size() - 1;
				idxs.add(text.length());
				for (int i = 0; i < last; i++) {
					appendArg(text, args.get(i));
					text.append(", "); //$NON-NLS-1$
					idxs.add(text.length()-1);
				}
				appendArg(text, args.get(last));
			}
		}
		else {
			text.append("<unkown>");
		}
	}
	
	protected void appendMethodDetail(final StyledString text, final IRMethod method) {
		final ArgsDefinition args = method.getArgsDefinition();
		final boolean showTypes = (method.getElementType() == IRElement.R_S4METHOD);
		text.append('(', fDefaultStyler);
		if (args == null) {
			text.append("<unknown>", fDefaultStyler);
		}
		else if (args.size() > 0) {
			final int last = args.size() - 1;
			if (showTypes) {
				for (int i = 0; i < last; i++) {
					appendArgWithType(text, args.get(i));
					text.append(", ", fDefaultStyler); 
				}
				appendArgWithType(text, args.get(last));
			}
			else {
				for (int i = 0; i < last; i++) {
					appendArg(text, args.get(i));
					text.append(", ", fDefaultStyler); 
				}
				appendArg(text, args.get(last));
			}
		}
		text.append(')', fDefaultStyler);
	}
	
	protected void appendMethodDetailAssignmentSpecial(final StyledString text, final IRMethod method) {
		final ArgsDefinition args = method.getArgsDefinition();
		final boolean showTypes = (method.getElementType() == IRElement.R_S4METHOD);
		text.append('(', fDefaultStyler);
		if (args == null) {
			text.append("<unknown>", fDefaultStyler);
		}
		else if (args.size() > 0) {
			final int last = args.size() - 2;
			if (showTypes) {
				for (int i = 0; i < last; i++) {
					appendArgWithType(text, args.get(i));
					text.append(", ", fDefaultStyler); 
				}
				appendArgWithType(text, args.get(last));
			}
			else {
				for (int i = 0; i < last; i++) {
					appendArg(text, args.get(i));
					text.append(", ", fDefaultStyler); 
				}
				appendArg(text, args.get(last));
			}
		}
		text.append(')', fDefaultStyler);
	}
	
	private void appendArg(final StyledString text, final Arg arg) {
		if (arg.name != null) {
			text.append(arg.name, fDefaultStyler);
		}
	}
	
	private void appendArg(final StringBuilder text, final Arg arg) {
		if (arg.name != null) {
			text.append(arg.name);
		}
	}
	
	private void appendArgWithType(final StyledString text, final Arg arg) {
		if (arg.name != null) {
			text.append(arg.name, fDefaultStyler);
		}
		if (arg.className != null) {
			text.append(" : "+arg.className, StyledString.DECORATIONS_STYLER); 
		}
	}
	
	protected void appendMethodMLText(final StringBuilder sb, final IRMethod method) {
		final ArgsDefinition args = method.getArgsDefinition();
		final String name = method.getElementName().getDisplayName();
		if (name != null) {
			sb.append(name);
		}
		if (args != null && args.size() > 0) {
			sb.ensureCapacity(sb.length() + 5 + args.size()*20);
			sb.append("( \n"); 
			final int last = args.size() - 1;
			sb.append("  "); 
			for (int i = 0; i < last; i++) {
				appendArgLong(sb, args.get(i));
				sb.append(",\n  "); 
			}
			appendArgLong(sb, args.get(last));
			sb.append("\n)"); 
		}
		else {
			sb.append("()"); 
		}
	}
	
	private void appendArgLong(final StringBuilder sb, final Arg arg) {
		if (arg.name != null) {
			sb.append(arg.name);
		}
		if (arg.className != null) {
			sb.append(" : "); 
			sb.append(arg.className);
		}
	}
	
	protected void appendClassMLText(final StringBuilder sb, final IRClass clazz) {
		final List<String> extendedClassNames = clazz.getExtendedClassNames();
		final String name = clazz.getElementName().getDisplayName();
		if (name != null) {
			sb.append(name);
		}
		if (extendedClassNames != null && !extendedClassNames.isEmpty()) {
			sb.ensureCapacity(sb.length() + 15 + extendedClassNames.size()*16);
			sb.append("\n  extends "); 
			final int last = extendedClassNames.size() - 1;
			for (int i = 0; i < last; i++) {
				sb.append(extendedClassNames.get(i));
				sb.append(", "); 
			}
			sb.append(extendedClassNames.get(last));
		}
	}
	
	protected void appendClassExtDetail(final StyledString text, final IRClassExtension ext) {
		final String command = ext.getExtCommand();
		if (command != null) {
			if (command.equals("setIs")) { //$NON-NLS-1$
				text.append("\u200A->\u200A"); 
			}
			else {
				return;
			}
			final String type = ext.getExtTypeName();
			if (type != null) {
				text.append(type);
			}
		}
	}
	
	protected void appendClassExtDetail(final StringBuilder sb, final IRClassExtension ext) {
		final String command = ext.getExtCommand();
		if (command != null) {
			if (command.equals("setIs")) { //$NON-NLS-1$
				sb.append("\u200A->\u200A"); 
			}
			else {
				return;
			}
			final String type = ext.getExtTypeName();
			if (type != null) {
				sb.append(type);
			}
		}
	}
	
	protected void appendClassExtMLText(final StringBuilder text, final IRClassExtension ext) {
		final String name = ext.getElementName().getDisplayName();
		if (name != null) {
			text.append(name);
		}
		final String command = ext.getExtCommand();
		if (command != null) {
			text.append("\n  "); 
			text.append(command);
			final String type = ext.getExtTypeName();
			if (type != null) {
				text.append(' ');
				text.append(type);
			}
			text.append(' ');
		}
	}
	
	protected void appendSlotDetail(final StyledString text, final IRSlot slot) {
		final String type = slot.getTypeName();
		if (type != null) {
			text.append(" : "+type, StyledString.DECORATIONS_STYLER); 
		}
	}
	
	protected void appendSlotDetail(final StringBuilder text, final IRSlot slot) {
	}
	
	
	protected void appendLongClassInfo(final StyledString text, final ICombinedRElement element, final RList elementAttr) {
		final StringBuilder textBuilder = getTextBuilder();
		textBuilder.append(" : "); 
		if (elementAttr != null) {
			final RObject object = elementAttr.get("class");
			if (object != null && object.getRObjectType() == RObject.TYPE_VECTOR && object.getData().getStoreType() == RStore.CHARACTER) {
				final RStore data = object.getData();
				if (data.getLength() > 0) {
					final int last = data.getLength() - 1;
					for (int i = 0; i < last; i++) {
						if (!data.isNA(i)) {
							textBuilder.append(data.getChar(i));
							textBuilder.append(", "); 
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
			textBuilder.append(" ("); 
			textBuilder.append(RDataUtil.getStoreMode(element.getData().getStoreType()));
			textBuilder.append(')');
		}
		text.append(textBuilder.toString(), StyledString.DECORATIONS_STYLER);
	}
	
	protected void appendVectorDetail(final StyledString text, final ICombinedRElement element, final RList elementAttr) {
		final byte datatype = element.getData().getStoreType();
		final StringBuilder textBuilder;
		if ((fStyle & LONG) != 0) {
			appendLongClassInfo(text, element, elementAttr);
			textBuilder = getTextBuilder();
		}
		else {
			textBuilder = getTextBuilder();
			textBuilder.append(" : "); 
			if (element.getRClassName().equals(element.getData().getBaseVectorRClassName())) {
				textBuilder.append(RDataUtil.getStoreAbbr(element.getData()));
			}
			else {
				textBuilder.append(element.getRClassName());
				textBuilder.append(" ("); 
				textBuilder.append(RDataUtil.getStoreAbbr(element.getData()));
				textBuilder.append(')');
			}
		}
		textBuilder.append(" ["); 
		textBuilder.append(Integer.toString(element.getLength()));
		textBuilder.append(']');
		if (datatype == RStore.FACTOR) {
			textBuilder.append(" ("); 
			textBuilder.append(((RFactorStore) element.getData()).getLevelCount());
			textBuilder.append(" levels)");
		}
		text.append(textBuilder.toString(), StyledString.DECORATIONS_STYLER);
	}
	
	protected void appendArrayDetail(final StyledString text, final ICombinedRElement element, final RList elementAttr) {
		final StringBuilder textBuilder;
		if ((fStyle & LONG) != 0) {
			appendLongClassInfo(text, element, elementAttr);
			textBuilder = getTextBuilder();
		}
		else {
			textBuilder = getTextBuilder();
			textBuilder.append(" : "); 
			if (element.getRClassName().equals(RObject.CLASSNAME_ARRAY)
					|| element.getRClassName().equals(RObject.CLASSNAME_MATRIX)) {
				textBuilder.append(RDataUtil.getStoreAbbr(element.getData()));
			}
			else {
				textBuilder.append(element.getRClassName());
				textBuilder.append(" ("); 
				textBuilder.append(RDataUtil.getStoreAbbr(element.getData()));
				textBuilder.append(')');
			}
		}
		textBuilder.append(" ["); 
		final RIntegerStore dim = ((RArray<?>) element).getDim();
		final int dimLength = dim.getLength();
		if (dimLength > 0) {
			textBuilder.append(Integer.toString(dim.getInt(0)));
			for (int i = 1; i < dimLength; i++) {
				textBuilder.append('×');
				textBuilder.append(Integer.toString(dim.getInt(i)));
			}
		}
		textBuilder.append(']');
		text.append(textBuilder.toString(), StyledString.DECORATIONS_STYLER);
	}
	
	protected void appendListDetail(final StyledString text, final ICombinedRElement element, final RList elementAttr) {
		if ((fStyle & COUNT) != 0) { // count info
			final StringBuilder textBuilder = getTextBuilder();
			textBuilder.append(" ("); 
			textBuilder.append(element.getLength());
			textBuilder.append(" items)");
			text.append(textBuilder.toString(), StyledString.COUNTER_STYLER);
		}
		
		if ((fStyle & LONG) != 0) {
			appendLongClassInfo(text, element, elementAttr);
		}
		else {
			if (!element.getRClassName().equals(RObject.CLASSNAME_LIST)) {
				final StringBuilder textBuilder = getTextBuilder();
				textBuilder.setLength(0);
				textBuilder.append(" : "); 
				textBuilder.append(element.getRClassName());
				text.append(textBuilder.toString(), StyledString.DECORATIONS_STYLER);
			}
		}
	}
	
	protected void appendEnvDetail(final StyledString text, final ICombinedRElement element, final RList elementAttr) {
		final ICombinedEnvironment envir = (ICombinedEnvironment) element;
		if ((fStyle & COUNT) != 0) { // count info
			final String countInfo = getEnvCountInfo(envir);
			text.append(countInfo, StyledString.COUNTER_STYLER);
		}
		
		if ((fStyle & LONG) != 0) {
			appendLongClassInfo(text, element, elementAttr);
		}
	}
	
	protected String getEnvCountInfo(final ICombinedEnvironment envir) {
		final StringBuilder textBuilder = getTextBuilder();
		textBuilder.append(" ("); 
		textBuilder.append(envir.getLength());
		textBuilder.append(')');
		return textBuilder.toString();
	}
	
	protected void appendDataframeDetail(final StyledString text, final ICombinedRElement element, final RList elementAttr) {
		final RDataFrame dataframe = (RDataFrame) element;
		if ((fStyle & COUNT) != 0) { // count info
			final StringBuilder textBuilder = getTextBuilder();
			textBuilder.append(" ("); 
			textBuilder.append(Integer.toString(dataframe.getColumnCount()));
			textBuilder.append(" var.)");
			text.append(textBuilder.toString(), StyledString.COUNTER_STYLER);
		}
		
		final StringBuilder textBuilder;
		if ((fStyle & LONG) != 0) {
			appendLongClassInfo(text, element, elementAttr);
			textBuilder = getTextBuilder();
		}
		else {
			textBuilder = getTextBuilder();
			if (!element.getRClassName().equals(RObject.CLASSNAME_DATAFRAME)) {
				textBuilder.append(" : "); 
				textBuilder.append(dataframe.getRClassName());
			}
		}
		textBuilder.append(" ["); 
		textBuilder.append(Integer.toString(dataframe.getRowCount()));
		textBuilder.append(']');
		text.append(textBuilder.toString(), StyledString.DECORATIONS_STYLER);
	}
	
	protected void appendS4ObjectDetail(final StyledString text, final ICombinedRElement element, final RList elementAttr) {
		if ((fStyle & LONG) != 0) {
			appendLongClassInfo(text, element, elementAttr);
		}
		else {
			final StringBuilder textBuilder = getTextBuilder();
			textBuilder.append(" : "); 
			textBuilder.append(element.getRClassName());
			text.append(textBuilder.toString(), StyledString.DECORATIONS_STYLER);
		}
	}
	
}
