/**
 * Copyright (c) 2012-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 */
package de.walware.statet.rtm.ggplot.provider;

import java.util.Collection;
import java.util.List;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.ResourceLocator;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.edit.provider.ComposeableAdapterFactory;
import org.eclipse.emf.edit.provider.IEditingDomainItemProvider;
import org.eclipse.emf.edit.provider.IItemLabelProvider;
import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
import org.eclipse.emf.edit.provider.IItemPropertySource;
import org.eclipse.emf.edit.provider.IStructuredItemContentProvider;
import org.eclipse.emf.edit.provider.ITreeItemContentProvider;
import org.eclipse.emf.edit.provider.ItemPropertyDescriptor;
import org.eclipse.emf.edit.provider.ItemProviderAdapter;
import org.eclipse.emf.edit.provider.ViewerNotification;

import de.walware.statet.rtm.base.util.RtItemLabelUtils.LabelGenerator;
import de.walware.statet.rtm.ggplot.GGPlot;
import de.walware.statet.rtm.ggplot.GGPlotFactory;
import de.walware.statet.rtm.ggplot.GGPlotPackage;
import de.walware.statet.rtm.ggplot.core.RtGGPlotCorePlugin;


/**
 * This is the item provider adapter for a {@link de.walware.statet.rtm.ggplot.GGPlot} object.
 * <!-- begin-user-doc -->
 * <!-- end-user-doc -->
 * @generated
 */
public class GGPlotItemProvider
	extends ItemProviderAdapter
	implements IEditingDomainItemProvider, IStructuredItemContentProvider, ITreeItemContentProvider, IItemLabelProvider, IItemPropertySource {
	
	
	private static String[] FEATURE_LABEL_NAMES = new String[] {
		"data", //$NON-NLS-1$
		"xVar", //$NON-NLS-1$
		"yVar", //$NON-NLS-1$
	};
	
	private final LabelGenerator fLabel;
	
	
	/**
	 * This constructs an instance from a factory and a notifier.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	public GGPlotItemProvider(AdapterFactory adapterFactory) {
		super(adapterFactory);
		
		fLabel = new LabelGenerator(getResourceLocator(), GGPlotPackage.eINSTANCE.getGGPlot(), FEATURE_LABEL_NAMES);
	}

	/**
	 * This returns the property descriptors for the adapted class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public List<IItemPropertyDescriptor> getPropertyDescriptors(Object object) {
		if (itemPropertyDescriptors == null) {
			super.getPropertyDescriptors(object);

			addDataPropertyDescriptor(object);
			addXVarPropertyDescriptor(object);
			addYVarPropertyDescriptor(object);
			addDataFilterPropertyDescriptor(object);
			addMainTitlePropertyDescriptor(object);
			addAxXLimPropertyDescriptor(object);
			addAxYLimPropertyDescriptor(object);
			addAxXLabelPropertyDescriptor(object);
			addAxYLabelPropertyDescriptor(object);
		}
		return itemPropertyDescriptors;
	}

	/**
	 * This adds a property descriptor for the Data feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addDataPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_PropDataProvider_data_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_PropDataProvider_data_feature", "_UI_PropDataProvider_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 GGPlotPackage.Literals.PROP_DATA_PROVIDER__DATA,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the XVar feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addXVarPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_PropXVarProvider_xVar_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_PropXVarProvider_xVar_feature", "_UI_PropXVarProvider_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 GGPlotPackage.Literals.PROP_XVAR_PROVIDER__XVAR,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the YVar feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addYVarPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_PropYVarProvider_yVar_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_PropYVarProvider_yVar_feature", "_UI_PropYVarProvider_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 GGPlotPackage.Literals.PROP_YVAR_PROVIDER__YVAR,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Data Filter feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addDataFilterPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_GGPlot_dataFilter_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_GGPlot_dataFilter_feature", "_UI_GGPlot_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 GGPlotPackage.Literals.GG_PLOT__DATA_FILTER,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Main Title feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addMainTitlePropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_GGPlot_mainTitle_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_GGPlot_mainTitle_feature", "_UI_GGPlot_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 GGPlotPackage.Literals.GG_PLOT__MAIN_TITLE,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Ax XLim feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addAxXLimPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_GGPlot_axXLim_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_GGPlot_axXLim_feature", "_UI_GGPlot_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 GGPlotPackage.Literals.GG_PLOT__AX_XLIM,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Ax YLim feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addAxYLimPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_GGPlot_axYLim_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_GGPlot_axYLim_feature", "_UI_GGPlot_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 GGPlotPackage.Literals.GG_PLOT__AX_YLIM,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Ax XLabel feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addAxXLabelPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_GGPlot_axXLabel_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_GGPlot_axXLabel_feature", "_UI_GGPlot_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 GGPlotPackage.Literals.GG_PLOT__AX_XLABEL,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Ax YLabel feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addAxYLabelPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_GGPlot_axYLabel_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_GGPlot_axYLabel_feature", "_UI_GGPlot_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 GGPlotPackage.Literals.GG_PLOT__AX_YLABEL,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This specifies how to implement {@link #getChildren} and is used to deduce an appropriate feature for an
	 * {@link org.eclipse.emf.edit.command.AddCommand}, {@link org.eclipse.emf.edit.command.RemoveCommand} or
	 * {@link org.eclipse.emf.edit.command.MoveCommand} in {@link #createCommand}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Collection<? extends EStructuralFeature> getChildrenFeatures(Object object) {
		if (childrenFeatures == null) {
			super.getChildrenFeatures(object);
			childrenFeatures.add(GGPlotPackage.Literals.GG_PLOT__MAIN_TITLE_STYLE);
			childrenFeatures.add(GGPlotPackage.Literals.GG_PLOT__FACET);
			childrenFeatures.add(GGPlotPackage.Literals.GG_PLOT__AX_XLABEL_STYLE);
			childrenFeatures.add(GGPlotPackage.Literals.GG_PLOT__AX_YLABEL_STYLE);
			childrenFeatures.add(GGPlotPackage.Literals.GG_PLOT__AX_XTEXT_STYLE);
			childrenFeatures.add(GGPlotPackage.Literals.GG_PLOT__AX_YTEXT_STYLE);
			childrenFeatures.add(GGPlotPackage.Literals.GG_PLOT__LAYERS);
		}
		return childrenFeatures;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EStructuralFeature getChildFeature(Object object, Object child) {
		// Check the type of the specified child object and return the proper feature to use for
		// adding (see {@link AddCommand}) it as a child.

		return super.getChildFeature(object, child);
	}

	/**
	 * This returns GGPlot.gif.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object getImage(Object object) {
		return overlayImage(object, getResourceLocator().getImage("full/obj16/GGPlot")); //$NON-NLS-1$
	}

	/**
	 * This returns the label text for the adapted class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	@Override
	public String getText(Object object) {
		return fLabel.createLabel((GGPlot) object);
	}

	/**
	 * This handles model notifications by calling {@link #updateChildren} to update any cached
	 * children and by creating a viewer notification, which it passes to {@link #fireNotifyChanged}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void notifyChanged(Notification notification) {
		updateChildren(notification);

		switch (notification.getFeatureID(GGPlot.class)) {
			case GGPlotPackage.GG_PLOT__DATA:
			case GGPlotPackage.GG_PLOT__XVAR:
			case GGPlotPackage.GG_PLOT__YVAR:
			case GGPlotPackage.GG_PLOT__DATA_FILTER:
			case GGPlotPackage.GG_PLOT__MAIN_TITLE:
			case GGPlotPackage.GG_PLOT__AX_XLIM:
			case GGPlotPackage.GG_PLOT__AX_YLIM:
			case GGPlotPackage.GG_PLOT__AX_XLABEL:
			case GGPlotPackage.GG_PLOT__AX_YLABEL:
				fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(), false, true));
				return;
			case GGPlotPackage.GG_PLOT__MAIN_TITLE_STYLE:
			case GGPlotPackage.GG_PLOT__FACET:
			case GGPlotPackage.GG_PLOT__AX_XLABEL_STYLE:
			case GGPlotPackage.GG_PLOT__AX_YLABEL_STYLE:
			case GGPlotPackage.GG_PLOT__AX_XTEXT_STYLE:
			case GGPlotPackage.GG_PLOT__AX_YTEXT_STYLE:
			case GGPlotPackage.GG_PLOT__LAYERS:
				fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(), true, false));
				return;
		}
		super.notifyChanged(notification);
	}

	/**
	 * This adds {@link org.eclipse.emf.edit.command.CommandParameter}s describing the children
	 * that can be created under this object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected void collectNewChildDescriptors(Collection<Object> newChildDescriptors, Object object) {
		super.collectNewChildDescriptors(newChildDescriptors, object);

		newChildDescriptors.add
			(createChildParameter
				(GGPlotPackage.Literals.GG_PLOT__FACET,
				 GGPlotFactory.eINSTANCE.createGridFacetLayout()));

		newChildDescriptors.add
			(createChildParameter
				(GGPlotPackage.Literals.GG_PLOT__FACET,
				 GGPlotFactory.eINSTANCE.createWrapFacetLayout()));

		newChildDescriptors.add
			(createChildParameter
				(GGPlotPackage.Literals.GG_PLOT__LAYERS,
				 GGPlotFactory.eINSTANCE.createGeomAblineLayer()));

		newChildDescriptors.add
			(createChildParameter
				(GGPlotPackage.Literals.GG_PLOT__LAYERS,
				 GGPlotFactory.eINSTANCE.createGeomBarLayer()));

		newChildDescriptors.add
			(createChildParameter
				(GGPlotPackage.Literals.GG_PLOT__LAYERS,
				 GGPlotFactory.eINSTANCE.createGeomBoxplotLayer()));

		newChildDescriptors.add
			(createChildParameter
				(GGPlotPackage.Literals.GG_PLOT__LAYERS,
				 GGPlotFactory.eINSTANCE.createGeomHistogramLayer()));

		newChildDescriptors.add
			(createChildParameter
				(GGPlotPackage.Literals.GG_PLOT__LAYERS,
				 GGPlotFactory.eINSTANCE.createGeomLineLayer()));

		newChildDescriptors.add
			(createChildParameter
				(GGPlotPackage.Literals.GG_PLOT__LAYERS,
				 GGPlotFactory.eINSTANCE.createGeomPointLayer()));

		newChildDescriptors.add
			(createChildParameter
				(GGPlotPackage.Literals.GG_PLOT__LAYERS,
				 GGPlotFactory.eINSTANCE.createGeomTextLayer()));

		newChildDescriptors.add
			(createChildParameter
				(GGPlotPackage.Literals.GG_PLOT__LAYERS,
				 GGPlotFactory.eINSTANCE.createGeomSmoothLayer()));

		newChildDescriptors.add
			(createChildParameter
				(GGPlotPackage.Literals.GG_PLOT__LAYERS,
				 GGPlotFactory.eINSTANCE.createGeomTileLayer()));

		newChildDescriptors.add
			(createChildParameter
				(GGPlotPackage.Literals.GG_PLOT__LAYERS,
				 GGPlotFactory.eINSTANCE.createGeomViolinLayer()));
	}

	/**
	 * Return the resource locator for this item provider's resources.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ResourceLocator getResourceLocator() {
		return RtGGPlotCorePlugin.INSTANCE;
	}

}
