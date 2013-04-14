/*******************************************************************************
 * Copyright (c) 2012-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.rtm.base.ui.rexpr;


import java.util.List;

import org.eclipse.core.databinding.observable.IObserving;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;

import de.walware.ecommons.emf.core.util.EFeatureReference;
import de.walware.ecommons.emf.core.util.IContext;
import de.walware.ecommons.emf.core.util.IEMFEditPropertyContext;
import de.walware.ecommons.emf.core.util.RuleSet;
import de.walware.ecommons.ltk.IElementName;
import de.walware.ecommons.ltk.ui.IElementNameProvider;

import de.walware.statet.rtm.base.ui.RtModelUIPlugin;
import de.walware.statet.rtm.base.ui.rexpr.RExprWidget.TypeDef;
import de.walware.statet.rtm.base.util.RExprType;
import de.walware.statet.rtm.rtdata.RtDataPackage;
import de.walware.statet.rtm.rtdata.types.RTypedExpr;

import de.walware.rj.data.RObject;

import de.walware.statet.r.core.model.IRElement;
import de.walware.statet.r.core.model.RElementName;


public class DefaultRExprTypeUIAdapters {
	
	
	public static class DirectName implements IElementNameProvider {
		
		@Override
		public IElementName getElementName(final Object selectionElement) {
			IElementName elementName;
			if (selectionElement instanceof IRElement) {
				elementName = ((IRElement) selectionElement).getElementName();
			}
			else if (selectionElement instanceof RElementName) {
				elementName = (IElementName) selectionElement;
			}
			else {
				return null;
			}
			return elementName;
		}
		
	}
	
	public static class LastName implements IElementNameProvider {
		
		@Override
		public IElementName getElementName(final Object selectionElement) {
			IElementName elementName;
			if (selectionElement instanceof IRElement) {
				elementName = ((IRElement) selectionElement).getElementName();
			}
			else if (selectionElement instanceof RElementName) {
				elementName = (IElementName) selectionElement;
			}
			else {
				return null;
			}
			while (elementName.getNextSegment() != null) {
				elementName = elementName.getNextSegment();
			}
			return elementName;
		}
		
	}
	
	static IElementNameProvider DIRECT_NAME = new DirectName();
	static IElementNameProvider LAST_NAME = new LastName();
	
	
	public static class RObjectTypeAdapter extends RExprTypeUIAdapter {
		
		
		public RObjectTypeAdapter(final RExprType type, final Image image) {
			super(type, image);
		}
		
		
		protected boolean isValidRObject(final RObject rObject) {
			return (rObject.getRObjectType() == RObject.TYPE_DATAFRAME);
		}
		
		@Override
		public int isValidElement(final Object element) {
			if (element instanceof IRElement) {
				final IRElement rElement = (IRElement) element;
				if (rElement.getElementName() == null) {
					return PRIORITY_INVALID;
				}
				if (element instanceof RObject) {
					if (isValidRObject((RObject) element)) {
						return PRIORITY_DEFAULT + 10;
					}
					return PRIORITY_INVALID;
				}
				return PRIORITY_DEFAULT;
			}
			return PRIORITY_INVALID;
		}
		
	}
	
	public static class RDataframeColumnTypeAdapter extends RExprTypeUIAdapter {
		
		
		public RDataframeColumnTypeAdapter(final RExprType type, final Image image) {
			super(type, image);
		}
		
		
		protected boolean isValidRObject(final RObject rObject, final IRElement parent) {
			return ((rObject.getRObjectType() == RObject.TYPE_VECTOR)
					&& (parent instanceof RObject)
					&& (((RObject) parent).getRObjectType() == RObject.TYPE_DATAFRAME) );
		}
		
		@Override
		protected int isValidElements(final List<?> elements) {
			final Object first = elements.get(0);
			if (first instanceof IRElement) {
				final IRElement parent = ((IRElement) first).getModelParent();
				if (parent instanceof RObject) {
					if (((RObject) parent).getRObjectType() != RObject.TYPE_DATAFRAME) {
						return PRIORITY_INVALID;
					}
					for (final Object element : elements) {
						if ((!(element instanceof IRElement))) {
							return PRIORITY_INVALID;
						}
						final IRElement rElement = (IRElement) element;
						if (rElement.getElementName() == null
								|| rElement.getModelParent() != parent) {
							return PRIORITY_INVALID;
						}
						if (element instanceof RObject
								&& ((RObject) element).getRObjectType() != RObject.TYPE_VECTOR) {
							return PRIORITY_INVALID;
						}
					}
					return PRIORITY_DEFAULT + 10;
				}
				else if (parent != null) {
					for (final Object element : elements) {
						if ((!(element instanceof IRElement))) {
							return PRIORITY_INVALID;
						}
						final IRElement rElement = (IRElement) element;
						if (rElement.getElementName() == null
								|| rElement.getModelParent() != parent) {
							return PRIORITY_INVALID;
						}
						switch (rElement.getElementName().getType()) {
						case RElementName.SUB_NAMEDPART:
						case RElementName.SUB_INDEXED_D:
							break;
						default:
							return PRIORITY_INVALID;
						}
					}
					return PRIORITY_DEFAULT;
				}
				else {
					for (final Object element : elements) {
						if ((!(element instanceof IRElement))) {
							return PRIORITY_INVALID;
						}
						final IRElement rElement = (IRElement) element;
						RElementName elementName = rElement.getElementName();
						if (elementName == null) {
							return PRIORITY_INVALID;
						}
						if (elementName.getNextSegment() != null) {
							RElementName parentName = null;
							do {
								parentName = elementName;
								elementName = elementName.getNextSegment();
							} while (elementName.getNextSegment() != null);
							switch (elementName.getType()) {
							case RElementName.SUB_NAMEDPART:
							case RElementName.SUB_INDEXED_D:
								break;
							default:
								return PRIORITY_INVALID;
							}
						}
						else {
							switch (elementName.getType()) {
							case RElementName.MAIN_DEFAULT:
							case RElementName.MAIN_OTHER:
							case RElementName.SUB_NAMEDPART:
							case RElementName.SUB_INDEXED_D:
								break;
							default:
								return PRIORITY_INVALID;
							}
						}
					}
					return PRIORITY_DEFAULT;
				}
			}
			return PRIORITY_INVALID;
		}
		
		@Override
		public List<String> getInputExprs(final Object input, final IContext context) {
			final List<?> elements = getElements(input);
			
			final Object first = elements.get(0);
			IElementNameProvider nameProvider;
			if (first instanceof IRElement) {
				nameProvider = DIRECT_NAME;
				final IRElement parent = ((IRElement) first).getModelParent();
				if (parent instanceof RObject) {
					if (checkParent(parent, input, context) == false) {
						return null;
					}
				}
			}
			else {
				nameProvider = LAST_NAME;
			}
			
			return collectElementName(elements, nameProvider);
		}
		
		protected boolean checkParent(final IRElement parent, final Object input, final IContext context) {
			final IElementName parentName = (input instanceof IElementNameProvider) ?
					((IElementNameProvider) input).getElementName(parent) : parent.getElementName();
			if (!(parentName instanceof RElementName)) {
				return true;
			}
			return checkParent((RElementName) parentName, context, "data"); //$NON-NLS-1$
		}
		
		protected boolean checkParent(final RElementName parentName, final IContext context, final String parentField) {
			if (parentName == null) {
				return true;
			}
			final IEMFEditPropertyContext propertyContext = (IEMFEditPropertyContext) context.getAdapter(IEMFEditPropertyContext.class);
			final RuleSet ruleSet = (RuleSet) context.getAdapter(RuleSet.class);
			if (propertyContext == null || ruleSet == null) {
				return true;
			}
			final EObject owner = (EObject) ((IObserving) propertyContext.getPropertyObservable()).getObserved();
			final List<EFeatureReference> parents = (List<EFeatureReference>) ruleSet.get(
					owner, propertyContext.getEFeature(), (parentField + RuleSet.PARENT_FEATURES_ID_SUFFIX) );
			if (parents == null) {
				return true;
			}
			if (parents.isEmpty()) {
				return false;
			}
			EFeatureReference change = null;
			for (int i = 0; i < parents.size(); i++) {
				final EFeatureReference parent = parents.get(i);
				final Object value = parent.getValue();
				if (value == null) {
					continue;
				}
				if (matches(value, parentName)) {
					return true;
				}
				else {
					break;
				}
			}
			for (int i = parents.size() - 1; i >= 0; i--) {
				final EFeatureReference parent = parents.get(i);
				if (parent.getValue() == null) {
					change = parent;
					break;
				}
			}
			final String newExpr = parentName.getDisplayName();
			if (change == null) {
				final String oldExpr = ((RTypedExpr) parents.get(0).getValue()).getExpr();
				final IShellProvider shellProvider = (IShellProvider) context.getAdapter(IShellProvider.class);
				final MessageDialog dialog = new MessageDialog((shellProvider != null) ? shellProvider.getShell() : null,
						"Change Dataframe", null,
						NLS.bind("The column seems to belong to a different dataframe than specified in ''{0}''.\n\n" +
								"Do you want to adapt the expression for the dataframe to ''{2}''?",
								new Object[] { parentField, oldExpr, newExpr }),
						MessageDialog.QUESTION,
						new String[] { "Change dataframe", "Ignore", "Cancel" }, 0);
				switch (dialog.open()) {
				case 0:
					change = parents.get(0);
					break;
				case 2:
					return false;
				default:
					return true;
				}
			}
			final Command command = SetCommand.create(propertyContext.getEditingDomain(),
					change.getEObject(), change.getEFeature(),
					new RTypedExpr(RTypedExpr.R, newExpr) );
			propertyContext.getEditingDomain().getCommandStack().execute(command);
			return true;
		}
		
		protected boolean matches(final Object obj, final RElementName expectedName) {
			if (obj instanceof RTypedExpr) {
				final RTypedExpr rExpr = (RTypedExpr) obj;
				if (rExpr.getExpr().equals(expectedName.getDisplayName())) {
					return true;
				}
				final IElementName parsedName = RElementName.parseDefault(rExpr.getExpr());
				if (parsedName == null) {
					return true;
				}
				if (!parsedName.equals(expectedName)) {
					return false;
				}
			}
			return true;
		}
		
		@Override
		public int isMoveValid(final Object input, final IContext sourceContext, final IContext context) {
			final IEMFEditPropertyContext propertyContext = (IEMFEditPropertyContext) context.getAdapter(IEMFEditPropertyContext.class);
			final IEMFEditPropertyContext sourcePropertyContext = (IEMFEditPropertyContext) sourceContext.getAdapter(IEMFEditPropertyContext.class);
			if (propertyContext != null && sourcePropertyContext != null
					&& propertyContext.getEditingDomain() == sourcePropertyContext.getEditingDomain()) {
				if (propertyContext.getPropertyObservable() == sourcePropertyContext.getPropertyObservable()) {
					return PRIORITY_DEFAULT + 10;
				}
				final EObject owner = (EObject) ((IObserving) propertyContext.getPropertyObservable()).getObserved();
				if (owner != null) {
					final RuleSet ruleSet = (RuleSet) sourcePropertyContext.getAdapter(RuleSet.class);
					final EObject sourceObject = (EObject) ((IObserving) sourcePropertyContext.getPropertyObservable()).getObserved();
					if (ruleSet != null && sourceObject != null) {
						final List<EFeatureReference> disjoints = (List<EFeatureReference>) ruleSet.get(
								owner, propertyContext.getEFeature(), RuleSet.DISJOINT_FEATURES_ID );
						if (disjoints != null) {
							for (final EFeatureReference disjoint : disjoints) {
								if (disjoint.getEObject() == sourceObject
										&& disjoint.getEFeature() == sourcePropertyContext.getEFeature() ) {
									return PRIORITY_DEFAULT + 10;
								}
							}
						}
					}
				}
				return PRIORITY_DEFAULT;
			}
			return PRIORITY_INVALID;
		}
		
	}
	
	public static class CommonRExprAdapter extends RExprTypeUIAdapter {
		
		public CommonRExprAdapter(final RExprType type, final Image image) {
			super(type, image);
		}
		
		
		@Override
		public int isValidInput(final Object input, final IContext context) {
			return PRIORITY_DEFAULT;
		}
		
	}
	
	public static class LabelTextAdapter extends RExprTypeUIAdapter {
		
		public LabelTextAdapter(final RExprType type, final Image image) {
			super(type, image);
		}
		
		
		@Override
		public String adopt(final String typeKey, final String expr) {
			if (typeKey == RTypedExpr.R) {
				return expr;
			}
			return super.adopt(typeKey, expr);
		}
		
	}
	
	public static class LabelRExprAdapter extends RExprTypeUIAdapter {
		
		public LabelRExprAdapter(final RExprType type, final Image image) {
			super(type, image);
		}
		
		
		@Override
		public String adopt(final String typeKey, final String expr) {
			if (typeKey == RTypedExpr.CHAR) {
				return expr;
			}
			return super.adopt(typeKey, expr);
		}
		
	}
	
	public static class ColorRExprAdapter extends RExprTypeUIAdapter {
		
		public ColorRExprAdapter(final RExprType type, final Image image) {
			super(type, image);
		}
		
		
		@Override
		public TypeDef createWidgetDef() {
			return new ColorType(this);
		}
		
	}
	
	public static class AlphaRExprAdapter extends RExprTypeUIAdapter {
		
		public AlphaRExprAdapter(final RExprType type, final Image image) {
			super(type, image);
		}
		
		
		@Override
		public TypeDef createWidgetDef() {
			return new AlphaType(this);
		}
		
	}
	
	public static class FontFamilyRExprAdapter extends RExprTypeUIAdapter {
		
		public FontFamilyRExprAdapter(final RExprType type, final Image image) {
			super(type, image);
		}
		
		
		@Override
		public TypeDef createWidgetDef() {
			return new FontFamilyType(this);
		}
		
	}
	
	
	protected static final RExprTypeUIAdapter DATAFRAME_ADAPTER = new RObjectTypeAdapter(
			RExprType.DATAFRAME_TYPE,
			RtModelUIPlugin.getDefault().getImageRegistry().get(RtModelUIPlugin.OBJ_DATAFRAME_TYPE_IMAGE_ID) );
	
	protected static final RExprTypeUIAdapter EXPR_VALUE_ADAPTER = new CommonRExprAdapter(
			RExprType.EXPR_VALUE_TYPE,
			RtModelUIPlugin.getDefault().getImageRegistry().get(RtModelUIPlugin.OBJ_REXPR_TYPE_IMAGE_ID) );
	
	protected static final RExprTypeUIAdapter TEXT_VALUE_ADAPTER = new RExprTypeUIAdapter(
			RExprType.TEXT_VALUE_TYPE,
			RtModelUIPlugin.getDefault().getImageRegistry().get(RtModelUIPlugin.OBJ_TEXT_TYPE_IMAGE_ID) );
	
	protected static final RExprTypeUIAdapter LABEL_TEXT_VALUE_ADAPTER = new LabelTextAdapter(
			RExprType.TEXT_VALUE_TYPE,
			RtModelUIPlugin.getDefault().getImageRegistry().get(RtModelUIPlugin.OBJ_TEXT_TYPE_IMAGE_ID) );
	
	protected static final RExprTypeUIAdapter LABEL_EXPR_VALUE_ADAPTER = new LabelRExprAdapter(
			RExprType.EXPR_VALUE_TYPE,
			RtModelUIPlugin.getDefault().getImageRegistry().get(RtModelUIPlugin.OBJ_REXPR_TYPE_IMAGE_ID) );
	
	protected static final RExprTypeUIAdapter COLOR_EXPR_VALUE_ADAPTER = new ColorRExprAdapter(
			RExprType.EXPR_COLOR_VALUE_TYPE,
			RtModelUIPlugin.getDefault().getImageRegistry().get(RtModelUIPlugin.OBJ_COLOR_TYPE_IMAGE_ID) );
	
	protected static final RExprTypeUIAdapter ALHPA_EXPR_VALUE_ADAPTER = new AlphaRExprAdapter(
			RExprType.EXPR_ALPHA_VALUE_TYPE,
			RtModelUIPlugin.getDefault().getImageRegistry().get(RtModelUIPlugin.OBJ_REXPR_TYPE_IMAGE_ID) );
	
	protected static final RExprTypeUIAdapter FONT_FAMILY_EXPR_VALUE_ADAPTER = new FontFamilyRExprAdapter(
			RExprType.EXPR_FONT_FAMILY_VALUE_TYPE,
			RtModelUIPlugin.getDefault().getImageRegistry().get(RtModelUIPlugin.OBJ_REXPR_TYPE_IMAGE_ID) );
	
	protected static final RExprTypeUIAdapter FUNCTION_ADAPTER = new RExprTypeUIAdapter(
			RExprType.EXPR_FUNCTION_TYPE,
			RtModelUIPlugin.getDefault().getImageRegistry().get(RtModelUIPlugin.OBJ_REXPR_TYPE_IMAGE_ID) );
	
	protected static final RExprTypeUIAdapter DATAFRAME_COLUMN_ADAPTER = new RDataframeColumnTypeAdapter(
			RExprType.DATAFRAME_COLUMN_TYPE,
			RtModelUIPlugin.getDefault().getImageRegistry().get(RtModelUIPlugin.OBJ_COLUMN_TYPE_IMAGE_ID) );
	
	protected static final RExprTypeUIAdapter DATAFRAME_COLUMNS_ADAPTER = new RDataframeColumnTypeAdapter(
			RExprType.DATAFRAME_COLUMN_TYPE,
			RtModelUIPlugin.getDefault().getImageRegistry().get(RtModelUIPlugin.OBJ_COLUMN_TYPE_IMAGE_ID) ) {
		@Override
		protected boolean isMulti() {
			return true;
		}
	};
	
	
	/* with link */
	
	protected static final RExprTypeUIAdapter LABEL_TEXT_VALUE_LINK_ADAPTER = new LabelTextAdapter(
			RExprType.TEXT_VALUE_TYPE,
			RtModelUIPlugin.getDefault().getImageRegistry().get(RtModelUIPlugin.OBJ_TEXT_TYPE_IMAGE_ID) ) {
		@Override
		public TypeDef createWidgetDef() {
			return new HyperlinkType(this);
		}
	};
	
	protected static final RExprTypeUIAdapter LABEL_EXPR_VALUE_LINK_ADAPTER = new LabelRExprAdapter(
			RExprType.EXPR_VALUE_TYPE,
			RtModelUIPlugin.getDefault().getImageRegistry().get(RtModelUIPlugin.OBJ_REXPR_TYPE_IMAGE_ID) ) {
		@Override
		public TypeDef createWidgetDef() {
			return new HyperlinkType(this);
		}
	};
	
	protected static final RExprTypeUIAdapter DATAFRAME_COLUMN_LINK_ADAPTER = new RDataframeColumnTypeAdapter(
			RExprType.DATAFRAME_COLUMN_TYPE,
			RtModelUIPlugin.getDefault().getImageRegistry().get(RtModelUIPlugin.OBJ_COLUMN_TYPE_IMAGE_ID) ) {
		@Override
		public TypeDef createWidgetDef() {
			return new HyperlinkType(this);
		}
	};
	
	
	public RExprTypeUIAdapter getUIAdapter(final RExprType type, final EStructuralFeature eFeature) {
		if (type == RExprType.DATAFRAME_TYPE) {
			return DATAFRAME_ADAPTER;
		}
		if (type == RExprType.EXPR_VALUE_TYPE) {
			if (eFeature.getEType() == RtDataPackage.Literals.RLABEL) {
				return LABEL_EXPR_VALUE_ADAPTER;
			}
			return EXPR_VALUE_ADAPTER;
		}
		if (type == RExprType.TEXT_VALUE_TYPE) {
			if (eFeature.getEType() == RtDataPackage.Literals.RLABEL) {
				return LABEL_TEXT_VALUE_ADAPTER;
			}
			return TEXT_VALUE_ADAPTER;
		}
		if (type == RExprType.EXPR_LABEL_VALUE_TYPE) {
			return LABEL_EXPR_VALUE_ADAPTER;
		}
		if (type == RExprType.EXPR_COLOR_VALUE_TYPE) {
			return COLOR_EXPR_VALUE_ADAPTER;
		}
		if (type == RExprType.EXPR_ALPHA_VALUE_TYPE) {
			return ALHPA_EXPR_VALUE_ADAPTER;
		}
		if (type == RExprType.EXPR_FONT_FAMILY_VALUE_TYPE) {
			return FONT_FAMILY_EXPR_VALUE_ADAPTER;
		}
		if (type == RExprType.EXPR_FUNCTION_TYPE) {
			return FUNCTION_ADAPTER;
		}
		if (type == RExprType.DATAFRAME_COLUMN_TYPE) {
			if (eFeature.isMany()) {
				return DATAFRAME_COLUMNS_ADAPTER;
			}
			return DATAFRAME_COLUMN_ADAPTER;
		}
		
		return null;
	}
	
}
