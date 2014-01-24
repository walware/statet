/*=============================================================================#
 # Copyright (c) 2010-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.debug.ui.preferences;

import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import de.walware.ecommons.ui.SharedUIResources;

import de.walware.statet.r.core.renv.IREnvConfiguration;
import de.walware.statet.r.ui.RUI;


public class REnvLabelProvider extends ColumnLabelProvider {
	
	
	private final IObservableValue fDefault;
	
	private Image fEnvIcon;
	private Image fEnvDefaultIcon;
	private Image fEnvRemoteIcon;
	private Image fEnvRemoteDefaultIcon;
	
	
	public REnvLabelProvider(final IObservableValue defaultValue) {
		fDefault = defaultValue;
	}
	
	
	private Image createIcon(final Image baseImage) {
		return new DecorationOverlayIcon(baseImage, new ImageDescriptor[] {
				null, null, null, null, null},
				new Point(baseImage.getBounds().width+4, baseImage.getBounds().height)).createImage();
	}
	
	private Image createDefaultIcon(final Image baseImage) {
		return new DecorationOverlayIcon(baseImage, new ImageDescriptor[] {
				null, null, null, SharedUIResources.getImages().getDescriptor(SharedUIResources.OVR_DEFAULT_MARKER_IMAGE_ID), null},
				new Point(baseImage.getBounds().width+4, baseImage.getBounds().height)).createImage();
	}
	
	
	@Override
	public void dispose() {
		if (fEnvIcon != null) {
			fEnvIcon.dispose();
			fEnvIcon = null;
		}
		if (fEnvDefaultIcon != null) {
			fEnvDefaultIcon.dispose();
			fEnvDefaultIcon = null;
		}
		if (fEnvRemoteIcon != null) {
			fEnvRemoteIcon.dispose();
			fEnvRemoteIcon = null;
		}
		if (fEnvRemoteDefaultIcon != null) {
			fEnvRemoteDefaultIcon.dispose();
			fEnvRemoteDefaultIcon = null;
		}
	}
	
	@Override
	public Image getImage(final Object element) {
		final IREnvConfiguration config = (IREnvConfiguration) element;
		if (config.getType() == IREnvConfiguration.USER_REMOTE_TYPE) {
			if (fDefault.getValue() == config) {
				if (fEnvRemoteDefaultIcon == null) {
					fEnvRemoteDefaultIcon = createDefaultIcon(RUI.getImage(RUI.IMG_OBJ_R_REMOTE_ENV));
				}
				return fEnvRemoteDefaultIcon;
			}
			else {
				if (fEnvRemoteIcon == null) {
					fEnvRemoteIcon = createIcon(RUI.getImage(RUI.IMG_OBJ_R_REMOTE_ENV));
				}
				return fEnvRemoteIcon;
			}
		}
		else {
			if (fDefault.getValue() == config) {
				if (fEnvDefaultIcon == null) {
					fEnvDefaultIcon = createDefaultIcon(RUI.getImage(RUI.IMG_OBJ_R_RUNTIME_ENV));
				}
				return fEnvDefaultIcon;
			}
			else {
				if (fEnvIcon == null) {
					fEnvIcon = createIcon(RUI.getImage(RUI.IMG_OBJ_R_RUNTIME_ENV));
				}
				return fEnvIcon;
			}
		}
	}
	
	@Override
	public String getText(final Object element) {
		final IREnvConfiguration config = (IREnvConfiguration) element;
		return config.getName();
	}
	
	
}
