/*=============================================================================#
 # Copyright (c) 2007-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.ui;

import static de.walware.ecommons.ltk.core.model.IModelElement.MASK_C1;
import static de.walware.ecommons.ltk.core.model.IModelElement.MASK_C2;
import static de.walware.ecommons.ltk.core.model.IModelElement.SHIFT_C1;

import java.util.List;

import org.apache.commons.collections.primitives.IntList;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Image;

import de.walware.ecommons.debug.ui.WaDebugImages;
import de.walware.ecommons.ltk.IElementName;
import de.walware.ecommons.ltk.LTKUtil;
import de.walware.ecommons.ltk.core.model.IModelElement;
import de.walware.ecommons.ltk.ui.IElementLabelProvider;
import de.walware.ecommons.models.core.util.IElementPartition;

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

import de.walware.statet.r.console.core.RWorkspace.ICombinedREnvironment;
import de.walware.statet.r.core.data.ICombinedRElement;
import de.walware.statet.r.core.model.ArgsDefinition;
import de.walware.statet.r.core.model.ArgsDefinition.Arg;
import de.walware.statet.r.core.model.IRClass;
import de.walware.statet.r.core.model.IRClassExtension;
import de.walware.statet.r.core.model.IRElement;
import de.walware.statet.r.core.model.IRFrame;
import de.walware.statet.r.core.model.IRMethod;
import de.walware.statet.r.core.model.IRSlot;
import de.walware.statet.r.core.model.IRSourceUnit;
import de.walware.statet.r.core.model.RElementName;


/**
 * Label Provider for R elements
 */
public class RLabelProvider extends StyledCellLabelProvider
		implements IElementLabelProvider, ILabelProvider {
	
	
	public static final int NAMESPACE=                     0x001;
	public static final int HEADER=                        0x002;
	public static final int LONG=                          0x004;
	public static final int COUNT=                         0x008;
	public static final int ASSIST=                        0x010;
	public static final int NO_STORE_TYPE=                 0x020;
	public static final int RESOURCE_PATH=                 0x100;
	
	
	private final StringBuilder textBuilder= new StringBuilder(100);
	private final Styler defaultStyler;
	
	private int style;
	
	
	public RLabelProvider() {
		this(0);
	}
	
	public RLabelProvider(final int style) {
		if ((this.style & LONG) != 0) {
			this.style |= COUNT;
		}
		this.style= style;
		if ((this.style & HEADER) != 0) {
			this.defaultStyler= IElementLabelProvider.TITLE_STYLER;
		}
		else {
			this.defaultStyler= null;
		}
	}
	
	
	protected final StringBuilder getTextBuilder() {
		this.textBuilder.setLength(0);
		return this.textBuilder;
	}
	
	
	@Override
	public Image getImage(final IModelElement element) {
		switch ((element.getElementType() & MASK_C1) >> SHIFT_C1) {
		
		case (IModelElement.C1_SOURCE >> SHIFT_C1):
			return RUI.getImage(RUI.IMG_OBJ_R_SCRIPT);
			
		case (IModelElement.C1_IMPORT >> SHIFT_C1):
			return StatetImages.getImage(StatetImages.OBJ_IMPORT);
			
		case (IModelElement.C1_METHOD >> SHIFT_C1):
			switch (element.getElementType() & MASK_C2) {
			case IRElement.R_GENERIC_FUNCTION:
				return RUI.getImage(RUI.IMG_OBJ_GENERIC_FUNCTION);
			case IRElement.R_S4METHOD:
				return RUI.getImage(RUI.IMG_OBJ_METHOD);
//				case IRLangElement.R_COMMON_FUNCTION:
			default:
				return RUI.getImage( ((element.getElementType() & 0xf) != 0x1) ?
						RUI.IMG_OBJ_COMMON_FUNCTION : RUI.IMG_OBJ_COMMON_LOCAL_FUNCTION);
			}
		case (IModelElement.C1_CLASS >> SHIFT_C1):
			switch (element.getElementType() & MASK_C2) {
			case IRElement.R_S4CLASS:
				return StatetImages.getImage(StatetImages.OBJ_CLASS);
			case IRElement.R_S4CLASS_EXTENSION:
				return StatetImages.getImage(StatetImages.OBJ_CLASS_EXT);
			default:
				return null;
			}
		
		case (IModelElement.C1_VARIABLE >> SHIFT_C1):
			if (element instanceof ICombinedRElement) {
				return getImage((ICombinedRElement) element);
			}
			switch (element.getElementType() & MASK_C2) {
			case IRElement.R_S4SLOT:
				return RUI.getImage(RUI.IMG_OBJ_SLOT);
//				case IRLangElement.R_COMMON_VARIABLE:
			default:
				return RUI.getImage( ((element.getElementType() & 0xf) != 0x1) ?
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
				final REnvironment renv= (REnvironment) element;
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
		case RObject.TYPE_MISSING:
			return RUI.getImage(RUI.IMG_OBJ_MISSING);
		case RObject.TYPE_PROMISE:
			return RUI.getImage(RUI.IMG_OBJ_PROMISE);
		case RObject.TYPE_REFERENCE:
		default:
			return RUI.getImage(RUI.IMG_OBJ_GENERAL_VARIABLE);
		}
	}
	
	
	@Override
	public String getText(final IModelElement element) {
		final String name= element.getElementName().getDisplayName();
		
		final StringBuilder text;
		switch ((element.getElementType() & MASK_C1) >> SHIFT_C1) {
		
		case (IModelElement.C1_METHOD >> SHIFT_C1):
			text= (name != null) ? new StringBuilder(name) : new StringBuilder();
			appendMethodDetail(text, (IRMethod) element);
			return text.toString();
		
		case (IModelElement.C1_CLASS >> SHIFT_C1):
			switch (element.getElementType() & MASK_C2) {
			case IRElement.R_S4CLASS_EXTENSION:
				text= (name != null) ? new StringBuilder(name) : new StringBuilder();
				appendClassExtDetail(text, (IRClassExtension) element);
				return text.toString();
			}
			break;
		case (IModelElement.C1_VARIABLE >> SHIFT_C1):
			switch (element.getElementType() & MASK_C2) {
			case IRElement.R_S4SLOT:
				text= (name != null) ? new StringBuilder(name) : new StringBuilder();
				appendSlotDetail(text, (IRSlot) element);
				return text.toString();
			}
			break;
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
	
	@Override
	public StyledString getStyledText(final IModelElement element) {
		if (element instanceof IRElement) {
			final IRElement rElement= (IRElement) element;
			return getStyledText(rElement, rElement.getElementName(), null);
		}
		else if (element instanceof IRSourceUnit) {
			return getStyledText((IRSourceUnit) element);
		}
		else {
			final String name= element.getElementName().getDisplayName();
			final StyledString text= (name != null) ?
					new StyledString(name, this.defaultStyler) :
					new StyledString();
			return text;
		}
	}
	
	public StyledString getStyledText(final IRElement element, final RElementName elementName, final RList elementAttr) {
		final RElementName elementName0= (elementName != null) ? elementName : element.getElementName();
		final StyledString text= new StyledString();
		final String name;
		
		switch ((element.getElementType() & MASK_C1) >> SHIFT_C1) {
		
		case (IModelElement.C1_METHOD >> SHIFT_C1):
			if (this.style != 0 && elementName0.getNextSegment() == null) {
				final String segmentName= elementName0.getSegmentName();
				int length;
				if (segmentName != null && ((length= segmentName.length()) > 2)
						&& (segmentName.charAt(length-2) == '<') && (segmentName.charAt(length-1) == '-')) {
					text.append(RElementName.create(RElementName.MAIN_DEFAULT, segmentName.substring(0, length-2)).getDisplayName(), this.defaultStyler);
					appendMethodDetailAssignmentSpecial(text,  (IRMethod) element);
					text.append(" <- value"); //$NON-NLS-1$
					break;
				}
			}
			name= elementName0.getDisplayName();
			if (name != null) {
				text.append(name, this.defaultStyler);
			}
			appendMethodDetail(text, (IRMethod) element);
			break;
		
		case (IModelElement.C1_CLASS >> SHIFT_C1):
			name= elementName0.getDisplayName();
			if (name != null) {
				text.append(name, this.defaultStyler);
			}
			switch (element.getElementType() & MASK_C2) {
			case IRElement.R_S4CLASS_EXTENSION:
				appendClassExtDetail(text, (IRClassExtension) element);
			}
			break;
		
		case (IModelElement.C1_VARIABLE >> SHIFT_C1):
			if (element instanceof ICombinedRElement) {
				final ICombinedRElement cElement= (ICombinedRElement) element;
				if (cElement.getRObjectType() == RObject.TYPE_ENV && elementName == null) {
					text.append(elementName0.getSegmentName(), this.defaultStyler);
				}
				else {
					name= elementName0.getDisplayName();
					if (name != null) {
						text.append(name, this.defaultStyler);
					}
				}
				decorateStyledText(text, cElement, elementName0, elementAttr);
				break;
			}
			name= elementName0.getDisplayName();
			if (name != null) {
				text.append(name, this.defaultStyler);
			}
			switch (element.getElementType() & MASK_C2) {
			case IRElement.R_S4SLOT:
				appendSlotDetail(text, (IRSlot) element);
			}
			break;
			
		default:
			name= elementName0.getDisplayName();
			if (name != null) {
				text.append(name, this.defaultStyler);
			}
			break;
		
		}
		
		if ((this.style & NAMESPACE) != 0) {
			IElementName scope= elementName0.getScope();
			if (scope == null) {
				final IModelElement parent= element.getModelParent();
				if (parent != null) {
					final IRFrame frame= (IRFrame) parent.getAdapter(IRFrame.class);
					if (frame != null) {
						scope= frame.getElementName();
					}
				}
			}
			if (scope != null) {
				final StringBuilder textBuilder= getTextBuilder();
				textBuilder.append(" - "); //$NON-NLS-1$
				textBuilder.append(scope.getDisplayName());
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
			if ((this.style & LONG) != 0) {
				appendLongClassInfo(text, element, elementAttr);
			}
			else {
				final StringBuilder textBuilder= getTextBuilder();
				textBuilder.append(" : "); //$NON-NLS-1$
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
	
	public StyledString getStyledText(final IRSourceUnit sourceUnit) {
		final String name= sourceUnit.getElementName().getDisplayName();
		final StyledString text= (name != null) ?
				new StyledString(name, this.defaultStyler) :
				new StyledString();
		if ((this.style & RESOURCE_PATH) != 0) {
			final Object resource= sourceUnit.getResource();
			if (resource instanceof IResource) {
				appendPath(text, ((IResource) resource).getParent());
			}
		}
		return text;
	}
	
	
	@Override
	public Image getImage(final Object element) {
		final IModelElement modelElement= LTKUtil.getModelElement(element);
		if (modelElement != null) {
			if (modelElement instanceof ICombinedRElement) {
				return getImage((ICombinedRElement) modelElement);
			}
			return getImage(modelElement);
		}
		return null;
	}
	
	@Override
	public String getText(final Object element) {
		final IModelElement modelElement= LTKUtil.getModelElement(element);
		if (modelElement != null) {
//			if (modelElement instanceof ICombinedRElement) {
//				return getText((ICombinedRElement) modelElement);
//			}
			return getText(modelElement);
		}
		return null;
	}
	
	@Override
	public void update(final ViewerCell cell) {
		final Object element= cell.getElement();
		final IModelElement modelElement= LTKUtil.getModelElement(element);
		if (element instanceof IElementPartition) {
			update(cell, (IElementPartition) element, modelElement);
			super.update(cell);
		}
		else if (modelElement != null) {
			update(cell, modelElement);
			super.update(cell);
		}
		else {
			cell.setImage(null);
			cell.setText(element.toString());
			cell.setStyleRanges(null);
			super.update(cell);
		}
	}
	
	public void update(final ViewerCell cell, final IModelElement element) {
		Image image= null;
		StyledString styledText= null;
		
		if (element instanceof ICombinedRElement) {
			final ICombinedRElement combined= (ICombinedRElement) element;
			if (combined.getRObjectType() == RObject.TYPE_REFERENCE) {
				final RObject realObject= ((RReference) element).getResolvedRObject();
				if (realObject instanceof ICombinedRElement) {
					image= getImage((ICombinedRElement) realObject);
					styledText= getStyledText((ICombinedRElement) realObject, combined.getElementName(), null);
				}
			}
		}
		
		if (image == null) {
			image= getImage(element);
		}
		cell.setImage(image);
		
		if (styledText == null) {
			styledText= getStyledText(element);
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
	
	public void update(final ViewerCell cell, final IElementPartition partition, final IModelElement element) {
		cell.setImage(WaDebugImages.getImageRegistry().get(WaDebugImages.OBJ_VARIABLE_PARTITION));
		
		final StyledString text= new StyledString();
		text.append("["); //$NON-NLS-1$
		text.append(Long.toString((partition.getPartitionStart() + 1)));
		text.append(" ... "); //$NON-NLS-1$
		text.append(Long.toString(partition.getPartitionStart() + partition.getPartitionLength()));
		text.append("]"); //$NON-NLS-1$
		
		if (element instanceof RList) {
			final RList rList= (RList) element;
			String label= rList.getName(partition.getPartitionStart());
			if (label != null) {
				text.append("  "); //$NON-NLS-1$
				text.append(label, StyledString.QUALIFIER_STYLER);
				text.append(" ... ", StyledString.QUALIFIER_STYLER); //$NON-NLS-1$
				label= rList.getName(partition.getPartitionStart() + (partition.getPartitionLength() - 1));
				if (label != null) {
					text.append(label, StyledString.QUALIFIER_STYLER);
				}
			}
		}
		
		cell.setText(text.getString());
		cell.setStyleRanges(text.getStyleRanges());
	}
	
	@Override
	public String getToolTipText(final Object element) {
		final StringBuilder text;
		
		final IModelElement modelElement= LTKUtil.getModelElement(element);
		if (modelElement != null) {
			switch ((modelElement.getElementType() & MASK_C1) >> SHIFT_C1) {
			
			case (IModelElement.C1_METHOD >> SHIFT_C1): {
				text= new StringBuilder();
				appendMethodMLText(text, (IRMethod) modelElement);
				return text.toString(); }
			
			case (IModelElement.C1_CLASS >> SHIFT_C1):
				switch (modelElement.getElementType() & MASK_C2) {
				case IRElement.R_S4CLASS:
					text= new StringBuilder();
					appendClassMLText(text, (IRClass) modelElement);
					return text.toString();
				case IRElement.R_S4CLASS_EXTENSION:
					text= new StringBuilder();
					appendClassExtMLText(text, (IRClassExtension) modelElement);
					return text.toString();
				default:
					return null;
				}
			}
		}
		return null;
	}
	
	protected void appendMethodDetail(final StringBuilder text, final IRMethod method) {
		final ArgsDefinition args= method.getArgsDefinition();
		final boolean showTypes= (method.getElementType() == IRElement.R_S4METHOD);
		text.append('(');
		if (args == null) {
			text.append("<unknown>");
		}
		else if (args.size() > 0) {
			final int last= args.size() - 1;
			if (showTypes) {
				for (int i= 0; i < last; i++) {
					appendArgWithType(text, args.get(i));
					text.append(", "); //$NON-NLS-1$
				}
				appendArgWithType(text, args.get(last));
			}
			else {
				for (int i= 0; i < last; i++) {
					appendArg(text, args.get(i));
					text.append(", "); //$NON-NLS-1$
				}
				appendArg(text, args.get(last));
			}
		}
		text.append(')');
	}
	
	public void appendArgumentInformation(final StringBuilder text, final IntList idxs, final ArgsDefinition args) {
		if (args != null) {
			if (args.size() == 0) {
				text.append("<no arguments>");
			}
			else {
				final int last= args.size() - 1;
				idxs.add(text.length());
				for (int i= 0; i < last; i++) {
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
		final ArgsDefinition args= method.getArgsDefinition();
		final boolean showTypes= (method.getElementType() == IRElement.R_S4METHOD);
		text.append("(", this.defaultStyler); //$NON-NLS-1$
		if (args == null) {
			text.append("<unknown>", this.defaultStyler);
		}
		else if (args.size() > 0) {
			final int last= args.size() - 1;
			if (showTypes) {
				for (int i= 0; i < last; i++) {
					appendArgWithType(text, args.get(i));
					text.append(", ", this.defaultStyler); //$NON-NLS-1$
				}
				appendArgWithType(text, args.get(last));
			}
			else {
				for (int i= 0; i < last; i++) {
					appendArg(text, args.get(i));
					text.append(", ", this.defaultStyler); //$NON-NLS-1$
				}
				appendArg(text, args.get(last));
			}
		}
		text.append(")", this.defaultStyler); //$NON-NLS-1$
	}
	
	protected void appendMethodDetailAssignmentSpecial(final StyledString text, final IRMethod method) {
		final ArgsDefinition args= method.getArgsDefinition();
		final boolean showTypes= (method.getElementType() == IRElement.R_S4METHOD);
		text.append("(", this.defaultStyler); //$NON-NLS-1$
		if (args == null) {
			text.append("<unknown>", this.defaultStyler);
		}
		else if (args.size() > 0) {
			final int last= args.size() - 1;
			if (showTypes) {
				for (int i= 0; i < last; i++) {
					appendArgWithType(text, args.get(i));
					text.append(", ", this.defaultStyler); //$NON-NLS-1$
				}
				appendArgWithType(text, args.get(last));
			}
			else {
				for (int i= 0; i < last; i++) {
					appendArg(text, args.get(i));
					text.append(", ", this.defaultStyler); //$NON-NLS-1$
				}
				appendArg(text, args.get(last));
			}
		}
		text.append(")", this.defaultStyler); //$NON-NLS-1$
	}
	
	private void appendArg(final StyledString text, final Arg arg) {
		if (arg.name != null) {
			text.append(arg.name, this.defaultStyler);
		}
	}
	
	private void appendArg(final StringBuilder text, final Arg arg) {
		if (arg.name != null) {
			text.append(arg.name);
		}
	}
	
	private void appendArgWithType(final StyledString text, final Arg arg) {
		if (arg.name != null) {
			text.append(arg.name, this.defaultStyler);
		}
		if (arg.className != null) {
			text.append(" : ", StyledString.DECORATIONS_STYLER); //$NON-NLS-1$
			text.append(arg.className, StyledString.DECORATIONS_STYLER);
		}
	}
	
	private void appendArgWithType(final StringBuilder text, final Arg arg) {
		if (arg.name != null) {
			text.append(arg.name);
		}
		if (arg.className != null) {
			text.append(" : "); //$NON-NLS-1$
			text.append(arg.className);
		}
	}
	
	protected void appendMethodMLText(final StringBuilder sb, final IRMethod method) {
		final ArgsDefinition args= method.getArgsDefinition();
		final String name= method.getElementName().getDisplayName();
		if (name != null) {
			sb.append(name);
		}
		if (args != null && args.size() > 0) {
			sb.ensureCapacity(sb.length() + 5 + args.size()*20);
			sb.append("( \n"); //$NON-NLS-1$
			final int last= args.size() - 1;
			sb.append("  "); //$NON-NLS-1$
			for (int i= 0; i < last; i++) {
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
		final List<String> extendedClassNames= clazz.getExtendedClassNames();
		final String name= clazz.getElementName().getDisplayName();
		if (name != null) {
			sb.append(name);
		}
		if (extendedClassNames != null && !extendedClassNames.isEmpty()) {
			sb.ensureCapacity(sb.length() + 15 + extendedClassNames.size()*16);
			sb.append("\n  extends "); //$NON-NLS-1$
			final int last= extendedClassNames.size() - 1;
			for (int i= 0; i < last; i++) {
				sb.append(extendedClassNames.get(i));
				sb.append(", "); //$NON-NLS-1$
			}
			sb.append(extendedClassNames.get(last));
		}
	}
	
	protected void appendClassExtDetail(final StyledString text, final IRClassExtension ext) {
		final String command= ext.getExtCommand();
		if (command != null) {
			if (command.equals("setIs")) { //$NON-NLS-1$
				text.append("\u200A->\u200A"); //$NON-NLS-1$
			}
			else {
				return;
			}
			final String type= ext.getExtTypeName();
			if (type != null) {
				text.append(type);
			}
		}
	}
	
	protected void appendClassExtDetail(final StringBuilder sb, final IRClassExtension ext) {
		final String command= ext.getExtCommand();
		if (command != null) {
			if (command.equals("setIs")) { //$NON-NLS-1$
				sb.append("\u200A->\u200A"); //$NON-NLS-1$
			}
			else {
				return;
			}
			final String type= ext.getExtTypeName();
			if (type != null) {
				sb.append(type);
			}
		}
	}
	
	protected void appendClassExtMLText(final StringBuilder text, final IRClassExtension ext) {
		final String name= ext.getElementName().getDisplayName();
		if (name != null) {
			text.append(name);
		}
		final String command= ext.getExtCommand();
		if (command != null) {
			text.append("\n  "); //$NON-NLS-1$
			text.append(command);
			final String type= ext.getExtTypeName();
			if (type != null) {
				text.append(' ');
				text.append(type);
			}
			text.append(' ');
		}
	}
	
	protected void appendSlotDetail(final StyledString text, final IRSlot slot) {
		final String type= slot.getTypeName();
		if (type != null) {
			text.append(" : ", StyledString.DECORATIONS_STYLER); //$NON-NLS-1$
			text.append(type, StyledString.DECORATIONS_STYLER);
		}
	}
	
	protected void appendSlotDetail(final StringBuilder text, final IRSlot slot) {
	}
	
	
	protected void appendLongClassInfo(final StyledString text, final ICombinedRElement element, final RList elementAttr) {
		final StringBuilder textBuilder= getTextBuilder();
		textBuilder.append(" : "); //$NON-NLS-1$
		RStore classData= null;
		if (elementAttr != null) {
			final RObject object= elementAttr.get("class"); //$NON-NLS-1$
			if (object != null && object.getRObjectType() == RObject.TYPE_VECTOR && object.getData().getStoreType() == RStore.CHARACTER) {
				classData= object.getData();
				if (classData.getLength() > 0) {
					final int last= (int) classData.getLength() - 1;
					for (int i= 0; i < last; i++) {
						if (!classData.isNA(i)) {
							textBuilder.append(classData.getChar(i));
							textBuilder.append(", "); //$NON-NLS-1$
						}
					}
					textBuilder.append(classData.getChar(last));
				}
			}
		}
		if (textBuilder.length() == 3) {
			textBuilder.append(element.getRClassName());
		}
		if (element.getData() != null && element.getData().getStoreType() > 0
				&& !((classData != null) ?
						classData.contains(RDataUtil.getStoreClass(element.getData())) :
						element.getRClassName().equals(RDataUtil.getStoreClass(element.getData())) )) {
			textBuilder.append(" ("); //$NON-NLS-1$
			textBuilder.append(RDataUtil.getStoreAbbr(element.getData()));
			textBuilder.append(')');
		}
		text.append(textBuilder.toString(), StyledString.DECORATIONS_STYLER);
	}
	
	protected void appendVectorDetail(final StyledString text, final ICombinedRElement element, final RList elementAttr) {
		final byte datatype= element.getData().getStoreType();
		final StringBuilder sb;
		if ((this.style & LONG) != 0) {
			appendLongClassInfo(text, element, elementAttr);
			sb= getTextBuilder();
		}
		else {
			sb= getTextBuilder();
			sb.append(" : "); //$NON-NLS-1$
			if (element.getRClassName().equals(element.getData().getBaseVectorRClassName())) {
				if ((this.style & NO_STORE_TYPE) == 0) {
					sb.append(RDataUtil.getStoreAbbr(element.getData()));
				}
			}
			else {
				sb.append(element.getRClassName());
				if ((this.style & NO_STORE_TYPE) == 0) {
					sb.append(" ("); //$NON-NLS-1$
					sb.append(RDataUtil.getStoreAbbr(element.getData()));
					sb.append(')');
				}
			}
		}
		sb.append(" ["); //$NON-NLS-1$
		sb.append(element.getLength());
		sb.append(']');
		if ((this.style & NO_STORE_TYPE) == 0 && datatype == RStore.FACTOR) {
			sb.append(" ("); //$NON-NLS-1$
			sb.append(((RFactorStore) element.getData()).getLevelCount());
			sb.append(' ');
			sb.append("levels");
			sb.append(')');
		}
		text.append(sb.toString(), StyledString.DECORATIONS_STYLER);
	}
	
	protected void appendArrayDetail(final StyledString text, final ICombinedRElement element, final RList elementAttr) {
		final StringBuilder sb;
		if ((this.style & LONG) != 0) {
			appendLongClassInfo(text, element, elementAttr);
			sb= getTextBuilder();
		}
		else {
			sb= getTextBuilder();
			sb.append(" : "); //$NON-NLS-1$
			if (element.getRClassName().equals(RObject.CLASSNAME_ARRAY)
					|| element.getRClassName().equals(RObject.CLASSNAME_MATRIX)) {
				if ((this.style & NO_STORE_TYPE) == 0) {
					sb.append(RDataUtil.getStoreAbbr(element.getData()));
				}
			}
			else {
				sb.append(element.getRClassName());
				if ((this.style & NO_STORE_TYPE) == 0) {
					sb.append(" ("); //$NON-NLS-1$
					sb.append(RDataUtil.getStoreAbbr(element.getData()));
					sb.append(')');
				}
			}
		}
		sb.append(" ["); //$NON-NLS-1$
		final RIntegerStore dim= ((RArray<?>) element).getDim();
		final int dimLength= (int) dim.getLength();
		if (dimLength > 0) {
			sb.append(dim.getInt(0));
			for (int i= 1; i < dimLength; i++) {
				sb.append('×');
				sb.append(dim.getInt(i));
			}
		}
		sb.append(']');
		text.append(sb.toString(), StyledString.DECORATIONS_STYLER);
	}
	
	protected void appendListDetail(final StyledString text, final ICombinedRElement element, final RList elementAttr) {
		if ((this.style & COUNT) != 0) { // count info
			final StringBuilder sb= getTextBuilder();
			sb.append(" ("); //$NON-NLS-1$
			sb.append(element.getLength());
			sb.append(' ');
			sb.append("items");
			sb.append(')');
			text.append(sb.toString(), StyledString.COUNTER_STYLER);
		}
		
		if ((this.style & LONG) != 0) {
			appendLongClassInfo(text, element, elementAttr);
		}
		else {
			if (!element.getRClassName().equals(RObject.CLASSNAME_LIST)) {
				final StringBuilder sb= getTextBuilder();
				sb.setLength(0);
				sb.append(" : "); //$NON-NLS-1$
				sb.append(element.getRClassName());
				text.append(sb.toString(), StyledString.DECORATIONS_STYLER);
			}
		}
	}
	
	protected void appendEnvDetail(final StyledString text, final ICombinedRElement element, final RList elementAttr) {
		final ICombinedREnvironment envir= (ICombinedREnvironment) element;
		if ((this.style & COUNT) != 0) { // count info
			final String countInfo= getEnvCountInfo(envir);
			text.append(countInfo, StyledString.COUNTER_STYLER);
		}
		
		if ((this.style & LONG) != 0) {
			appendLongClassInfo(text, element, elementAttr);
		}
	}
	
	protected String getEnvCountInfo(final ICombinedREnvironment envir) {
		final StringBuilder textBuilder= getTextBuilder();
		textBuilder.append(" ("); //$NON-NLS-1$
		textBuilder.append(envir.getLength());
		textBuilder.append(')');
		return textBuilder.toString();
	}
	
	protected void appendDataframeDetail(final StyledString text, final ICombinedRElement element, final RList elementAttr) {
		final RDataFrame dataframe= (RDataFrame) element;
		if ((this.style & COUNT) != 0) { // count info
			final StringBuilder sb= getTextBuilder();
			sb.append(" ("); //$NON-NLS-1$
			sb.append(dataframe.getColumnCount());
			sb.append(' ');
			sb.append("var.");
			sb.append(')');
			text.append(sb.toString(), StyledString.COUNTER_STYLER);
		}
		
		final StringBuilder sb;
		if ((this.style & LONG) != 0) {
			appendLongClassInfo(text, element, elementAttr);
			sb= getTextBuilder();
		}
		else {
			sb= getTextBuilder();
			if (!element.getRClassName().equals(RObject.CLASSNAME_DATAFRAME)) {
				sb.append(" : "); //$NON-NLS-1$
				sb.append(dataframe.getRClassName());
			}
		}
		sb.append(" ["); //$NON-NLS-1$
		sb.append(dataframe.getRowCount());
		sb.append(']');
		text.append(sb.toString(), StyledString.DECORATIONS_STYLER);
	}
	
	protected void appendS4ObjectDetail(final StyledString text, final ICombinedRElement element, final RList elementAttr) {
		if ((this.style & LONG) != 0) {
			appendLongClassInfo(text, element, elementAttr);
		}
		else {
			final StringBuilder textBuilder= getTextBuilder();
			textBuilder.append(" : "); //$NON-NLS-1$
			textBuilder.append(element.getRClassName());
			text.append(textBuilder.toString(), StyledString.DECORATIONS_STYLER);
		}
	}
	
	protected void appendPath(final StyledString text, final IResource resource) {
		if (resource != null) {
			text.append(" - ", StyledString.QUALIFIER_STYLER); //$NON-NLS-1$
			text.append(resource.getFullPath().makeRelative().toString(), StyledString.QUALIFIER_STYLER);
		}
	}
	
}
