/*******************************************************************************
 * Copyright (c) 2007-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
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

import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.graphics.Image;

import de.walware.ecommons.ltk.IModelElement;
import de.walware.ecommons.ltk.ui.IElementLabelProvider;

import de.walware.statet.base.ui.StatetImages;

import de.walware.rj.data.RList;

import de.walware.statet.r.core.model.ArgsDefinition;
import de.walware.statet.r.core.model.IRClass;
import de.walware.statet.r.core.model.IRClassExtension;
import de.walware.statet.r.core.model.IRLangElement;
import de.walware.statet.r.core.model.IRMethod;
import de.walware.statet.r.core.model.IRSlot;
import de.walware.statet.r.core.model.ArgsDefinition.Arg;


/**
 * Label Provider for R elements
 */
public class RLabelProvider extends StyledCellLabelProvider implements IElementLabelProvider {
	
	
	private Styler fDefaultStyler;
	
	
	public RLabelProvider() {
		this(false);
	}
	
	public RLabelProvider(final boolean title) {
		if (title) {
			fDefaultStyler = IElementLabelProvider.TITLE_STYLER;
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
			case IRLangElement.R_S4CLASS_EXTENSION:
				text = (name != null) ? new StringBuilder(name) : new StringBuilder();
				appendClassExtDetail(text, (IRClassExtension) element);
				return text.toString();
			}
		
		case IModelElement.C1_VARIABLE:
			switch (element.getElementType() & IModelElement.MASK_C2) {
			case IRLangElement.R_S4SLOT:
				text = (name != null) ? new StringBuilder(name) : new StringBuilder();
				appendSlotDetail(text, (IRSlot) element);
				return text.toString();
			}
		
		}
		return name;
	}
	
	public void decorateText(final StringBuilder text, final IModelElement element) {
		switch (element.getElementType() & IModelElement.MASK_C1) {
		
		case IModelElement.C1_METHOD:
			appendMethodDetail(text, (IRMethod) element);
			return;
		
		case IModelElement.C1_CLASS:
			switch (element.getElementType() & IModelElement.MASK_C2) {
			case IRLangElement.R_S4CLASS_EXTENSION:
				appendClassExtDetail(text, (IRClassExtension) element);
				return;
			}
		
		case IModelElement.C1_VARIABLE:
			switch (element.getElementType() & IModelElement.MASK_C2) {
			case IRLangElement.R_S4SLOT:
				appendSlotDetail(text, (IRSlot) element);
				return;
			}
		
		}
	}
	
	public StyledString getStyledText(final IModelElement element) {
		final StyledString text;
		final String name;
		
		switch (element.getElementType() & IModelElement.MASK_C1) {
		
		case IModelElement.C1_METHOD:
			text = new StyledString();
			name = element.getElementName().getDisplayName();
			if (name != null) {
				text.append(name);
			}
			appendMethodDetail(text, (IRMethod) element);
			return text;
		
		case IModelElement.C1_CLASS:
			switch (element.getElementType() & IModelElement.MASK_C2) {
			case IRLangElement.R_S4CLASS_EXTENSION:
				text = new StyledString();
				name = element.getElementName().getDisplayName();
				if (name != null) {
					text.append(name);
				}
				appendClassExtDetail(text, (IRClassExtension) element);
				return text;
			}
		
		case IModelElement.C1_VARIABLE:
			switch (element.getElementType() & IModelElement.MASK_C2) {
			case IRLangElement.R_S4SLOT:
				text = new StyledString();
				name = element.getElementName().getDisplayName();
				if (name != null) {
					text.append(name);
				}
				appendSlotDetail(text, (IRSlot) element);
				return text;
			}
		
		}
		return null;
	}
	
	public void decorateStyledText(final StyledString text, final IModelElement element) {
		decorateStyledText(text, element, null);
	}
	
	public void decorateStyledText(final StyledString text, final IModelElement element, final RList elementAttr) {
		switch (element.getElementType() & IModelElement.MASK_C1) {
		
		case IModelElement.C1_METHOD:
			appendMethodDetail(text, (IRMethod) element);
			return;
		
		case IModelElement.C1_CLASS:
			switch (element.getElementType() & IModelElement.MASK_C2) {
			case IRLangElement.R_S4CLASS_EXTENSION:
				appendClassExtDetail(text, (IRClassExtension) element);
				return;
			}
		
		case IModelElement.C1_VARIABLE:
			switch (element.getElementType() & IModelElement.MASK_C2) {
			case IRLangElement.R_S4SLOT:
				appendSlotDetail(text, (IRSlot) element);
				return;
			}
		
		}
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
			case IRLangElement.R_GENERIC_FUNCTION:
				return RUI.getImage(RUI.IMG_OBJ_GENERIC_FUNCTION);
			case IRLangElement.R_S4METHOD:
				return RUI.getImage(RUI.IMG_OBJ_METHOD);
//				case IRLangElement.R_COMMON_FUNCTION:
			default:
				return RUI.getImage( ((modelElement.getElementType() & 0xf) != 0x1) ?
						RUI.IMG_OBJ_COMMON_FUNCTION : RUI.IMG_OBJ_COMMON_LOCAL_FUNCTION);
			}
		case IModelElement.C1_CLASS:
			switch (modelElement.getElementType() & IModelElement.MASK_C2) {
			case IRLangElement.R_S4CLASS:
				return StatetImages.getImage(StatetImages.OBJ_CLASS);
			case IRLangElement.R_S4CLASS_EXTENSION:
				return StatetImages.getImage(StatetImages.OBJ_CLASS_EXT);
			default:
				return null;
			}
		
		case IModelElement.C1_VARIABLE:
			switch (modelElement.getElementType() & IModelElement.MASK_C2) {
			case IRLangElement.R_S4SLOT:
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
	
	@Override
	public void update(final ViewerCell cell) {
		final Object cellElement = cell.getElement();
		
		if (cellElement instanceof IModelElement) {
			final IModelElement modelElement = (IModelElement) cellElement;
			
			cell.setImage(getImage(modelElement));
			final StyledString styledText = getStyledText(modelElement);
			if (styledText != null) {
				cell.setText(styledText.getString());
				cell.setStyleRanges(styledText.getStyleRanges());
			}
			else {
				cell.setText(getText(modelElement));
				cell.setStyleRanges(null);
			}
		}
		else {
			updateUnknownElement(cell);
		}
		super.update(cell);
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
				case IRLangElement.R_S4CLASS:
					text = new StringBuilder();
					appendClassMLText(text, (IRClass) modelElement);
					return text.toString(); }
				case IRLangElement.R_S4CLASS_EXTENSION: {
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
				text.append(", "); //$NON-NLS-1$
			}
			appendArg(text, args.get(last));
		}
		text.append(')');
	}
	
	protected void appendMethodDetail(final StyledString text, final IRMethod method) {
		final ArgsDefinition args = method.getArgsDefinition();
		final boolean showTypes = (method.getElementType() == IRLangElement.R_S4METHOD);
		text.append('(', fDefaultStyler);
		if (args == null) {
			text.append("<unknown>", fDefaultStyler);
		}
		else if (args.size() > 0) {
			final int last = args.size() - 1;
			if (showTypes) {
				for (int i = 0; i < last; i++) {
					appendArgWithType(text, args.get(i));
					text.append(", ", fDefaultStyler); //$NON-NLS-1$
				}
				appendArgWithType(text, args.get(last));
			}
			else {
				for (int i = 0; i < last; i++) {
					appendArg(text, args.get(i));
					text.append(", ", fDefaultStyler); //$NON-NLS-1$
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
			text.append(" : "+arg.className, StyledString.DECORATIONS_STYLER); //$NON-NLS-1$
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
			sb.append("( \n"); //$NON-NLS-1$
			final int last = args.size() - 1;
			sb.append("  "); //$NON-NLS-1$
			for (int i = 0; i < last; i++) {
				appendArgLong(sb, args.get(i));
				sb.append(",\n  "); //$NON-NLS-1$
			}
			appendArgLong(sb, args.get(last));
			sb.append("\n)"); //$NON-NLS-1$
		}
		else {
			sb.append("()"); //$NON-NLS-1$
		}
	}
	
	private void appendArgLong(final StringBuilder sb, final Arg arg) {
		if (arg.name != null) {
			sb.append(arg.name);
		}
		if (arg.className != null) {
			sb.append(" : "); //$NON-NLS-1$
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
			sb.append("\n  extends "); //$NON-NLS-1$
			final int last = extendedClassNames.size() - 1;
			for (int i = 0; i < last; i++) {
				sb.append(extendedClassNames.get(i));
				sb.append(", "); //$NON-NLS-1$
			}
			sb.append(extendedClassNames.get(last));
		}
	}
	
	protected void appendClassExtDetail(final StyledString text, final IRClassExtension ext) {
		final String command = ext.getExtCommand();
		if (command != null) {
			if (command.equals("setIs")) { //$NON-NLS-1$
				text.append("\u200A->\u200A"); //$NON-NLS-1$
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
				sb.append("\u200A->\u200A"); //$NON-NLS-1$
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
			text.append("\n  "); //$NON-NLS-1$
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
			text.append(" : "+type, StyledString.DECORATIONS_STYLER); //$NON-NLS-1$
		}
	}
	
	protected void appendSlotDetail(final StringBuilder text, final IRSlot slot) {
	}
	
}
