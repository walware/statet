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
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
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

import de.walware.statet.rtm.base.util.RtItemLabelUtils;
import de.walware.statet.rtm.ggplot.GGPlotPackage;
import de.walware.statet.rtm.ggplot.TextStyle;
import de.walware.statet.rtm.ggplot.core.RtGGPlotCorePlugin;

/**
 * This is the item provider adapter for a {@link de.walware.statet.rtm.ggplot.TextStyle} object.
 * <!-- begin-user-doc -->
 * <!-- end-user-doc -->
 * @generated
 */
public class TextStyleItemProvider
	extends ItemProviderAdapter
	implements
		IEditingDomainItemProvider,
		IStructuredItemContentProvider,
		ITreeItemContentProvider,
		IItemLabelProvider,
		IItemPropertySource {
	/**
	 * This constructs an instance from a factory and a notifier.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public TextStyleItemProvider(AdapterFactory adapterFactory) {
		super(adapterFactory);
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

			addSizePropertyDescriptor(object);
			addColorPropertyDescriptor(object);
			addFontFamilyPropertyDescriptor(object);
			addFontFacePropertyDescriptor(object);
			addHJustPropertyDescriptor(object);
			addVJustPropertyDescriptor(object);
			addAnglePropertyDescriptor(object);
		}
		return itemPropertyDescriptors;
	}

	/**
	 * This adds a property descriptor for the Size feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addSizePropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_PropSizeProvider_size_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_PropSizeProvider_size_feature", "_UI_PropSizeProvider_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 GGPlotPackage.Literals.PROP_SIZE_PROVIDER__SIZE,
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
	 * This adds a property descriptor for the Font Family feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addFontFamilyPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_TextStyle_fontFamily_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_TextStyle_fontFamily_feature", "_UI_TextStyle_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 GGPlotPackage.Literals.TEXT_STYLE__FONT_FAMILY,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Font Face feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addFontFacePropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_TextStyle_fontFace_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_TextStyle_fontFace_feature", "_UI_TextStyle_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 GGPlotPackage.Literals.TEXT_STYLE__FONT_FACE,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the HJust feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addHJustPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_TextStyle_hJust_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_TextStyle_hJust_feature", "_UI_TextStyle_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 GGPlotPackage.Literals.TEXT_STYLE__HJUST,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the VJust feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addVJustPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_TextStyle_vJust_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_TextStyle_vJust_feature", "_UI_TextStyle_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 GGPlotPackage.Literals.TEXT_STYLE__VJUST,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Angle feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addAnglePropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_TextStyle_angle_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_TextStyle_angle_feature", "_UI_TextStyle_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 GGPlotPackage.Literals.TEXT_STYLE__ANGLE,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This returns TextStyle.gif.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object getImage(Object object) {
		return overlayImage(object, getResourceLocator().getImage("full/obj16/TextStyle")); //$NON-NLS-1$
	}

	/**
	 * This returns the label text for the adapted class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	@Override
	public String getText(Object object) {
		EObject eObj = (EObject) object;
		EObject eContainer = eObj.eContainer();
		EReference eContainmentFeature = eObj.eContainmentFeature();
		if (eContainer != null && eContainmentFeature != null) {
			String s = getString(RtItemLabelUtils.getLabelKey(eContainmentFeature));
			if (s != null) {
				return s;
			}
		}
		
		String label = eContainmentFeature == null ? null : eContainmentFeature.getName();
		return label == null || label.length() == 0 ?
			getString("_UI_TextStyle_type") : //$NON-NLS-1$
			getString("_UI_TextStyle_type") + " " + label; //$NON-NLS-1$ //$NON-NLS-2$
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

		switch (notification.getFeatureID(TextStyle.class)) {
			case GGPlotPackage.TEXT_STYLE__SIZE:
			case GGPlotPackage.TEXT_STYLE__COLOR:
			case GGPlotPackage.TEXT_STYLE__FONT_FAMILY:
			case GGPlotPackage.TEXT_STYLE__FONT_FACE:
			case GGPlotPackage.TEXT_STYLE__HJUST:
			case GGPlotPackage.TEXT_STYLE__VJUST:
			case GGPlotPackage.TEXT_STYLE__ANGLE:
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
