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
import org.eclipse.emf.edit.provider.ComposeableAdapterFactory;
import org.eclipse.emf.edit.provider.IEditingDomainItemProvider;
import org.eclipse.emf.edit.provider.IItemLabelProvider;
import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
import org.eclipse.emf.edit.provider.IItemPropertySource;
import org.eclipse.emf.edit.provider.IStructuredItemContentProvider;
import org.eclipse.emf.edit.provider.ITreeItemContentProvider;
import org.eclipse.emf.edit.provider.ItemPropertyDescriptor;
import org.eclipse.emf.edit.provider.ViewerNotification;

import de.walware.statet.rtm.ggplot.GGPlotPackage;
import de.walware.statet.rtm.ggplot.GeomBoxplotLayer;


/**
 * This is the item provider adapter for a {@link de.walware.statet.rtm.ggplot.GeomBoxplotLayer} object.
 * <!-- begin-user-doc -->
 * <!-- end-user-doc -->
 * @generated
 */
public class GeomBoxplotLayerItemProvider
		extends LayerItemProvider implements IEditingDomainItemProvider, IStructuredItemContentProvider, ITreeItemContentProvider, IItemLabelProvider, IItemPropertySource {

	/**
	 * This constructs an instance from a factory and a notifier.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	public GeomBoxplotLayerItemProvider(AdapterFactory adapterFactory) {
		super(adapterFactory, GGPlotPackage.Literals.GEOM_BOXPLOT_LAYER);
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

			addXVarPropertyDescriptor(object);
			addGroupVarPropertyDescriptor(object);
			addColorPropertyDescriptor(object);
			addFillPropertyDescriptor(object);
			addAlphaPropertyDescriptor(object);
		}
		return itemPropertyDescriptors;
	}

	/**
	 * This adds a property descriptor for the Group Var feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addGroupVarPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_PropGroupVarProvider_groupVar_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_PropGroupVarProvider_groupVar_feature", "_UI_PropGroupVarProvider_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 GGPlotPackage.Literals.PROP_GROUP_VAR_PROVIDER__GROUP_VAR,
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
	 * This adds a property descriptor for the Color feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addColorPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_PropColorProvider_color_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_PropColorProvider_color_feature", "_UI_PropColorProvider_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 GGPlotPackage.Literals.PROP_COLOR_PROVIDER__COLOR,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Fill feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addFillPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_PropFillProvider_fill_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_PropFillProvider_fill_feature", "_UI_PropFillProvider_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 GGPlotPackage.Literals.PROP_FILL_PROVIDER__FILL,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Alpha feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addAlphaPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_PropAlphaProvider_alpha_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_PropAlphaProvider_alpha_feature", "_UI_PropAlphaProvider_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 GGPlotPackage.Literals.PROP_ALPHA_PROVIDER__ALPHA,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This returns GeomBoxplotLayer.gif.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object getImage(Object object) {
		return overlayImage(object, getResourceLocator().getImage("full/obj16/GeomBoxplotLayer")); //$NON-NLS-1$
	}

	/**
	 * This returns the label text for the adapted class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	@Override
	public String getText(Object object) {
		return super.getText(object);
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

		switch (notification.getFeatureID(GeomBoxplotLayer.class)) {
			case GGPlotPackage.GEOM_BOXPLOT_LAYER__XVAR:
			case GGPlotPackage.GEOM_BOXPLOT_LAYER__GROUP_VAR:
			case GGPlotPackage.GEOM_BOXPLOT_LAYER__COLOR:
			case GGPlotPackage.GEOM_BOXPLOT_LAYER__FILL:
			case GGPlotPackage.GEOM_BOXPLOT_LAYER__ALPHA:
				fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(), false, true));
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
	}

}
