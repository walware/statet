/**
 * Copyright (c) 2012-2014 Stephan Wahlbrink (WalWare.de) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 */
package de.walware.statet.rtm.ggplot.ui;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.eclipse.emf.common.EMFPlugin;
import org.eclipse.emf.common.ui.EclipseUIPlugin;
import org.eclipse.emf.common.util.ResourceLocator;
import org.eclipse.jface.resource.ImageRegistry;

import de.walware.ecommons.ui.util.ImageRegistryUtil;

import de.walware.statet.rtm.base.core.RtModelCorePlugin;

/**
 * This is the central singleton for the RtGGPlot editor plugin.
 * <!-- begin-user-doc -->
 * <!-- end-user-doc -->
 * @generated
 */
public final class RtGGPlotEditorPlugin extends EMFPlugin {
	
	static final String RT_ID = "de.walware.statet.rtm.ftable"; //$NON-NLS-1$
	static final String IMG_OBJ_GGPLOT_TASK = RT_ID + ".images.GGPlotTaskObj"; //$NON-NLS-1$
	
	
	/**
	 * Keep track of the singleton.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final RtGGPlotEditorPlugin INSTANCE = new RtGGPlotEditorPlugin();
	
	/**
	 * Keep track of the singleton.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static Implementation plugin;

	/**
	 * Create the instance.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public RtGGPlotEditorPlugin() {
		super
			(new ResourceLocator [] {
				RtModelCorePlugin.INSTANCE,
			});
	}

	/**
	 * Returns the singleton instance of the Eclipse plugin.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the singleton instance.
	 * @generated
	 */
	@Override
	public ResourceLocator getPluginResourceLocator() {
		return plugin;
	}
	
	/**
	 * Returns the singleton instance of the Eclipse plugin.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the singleton instance.
	 * @generated
	 */
	public static Implementation getPlugin() {
		return plugin;
	}
	
	/**
	 * The actual implementation of the Eclipse <b>Plugin</b>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static class Implementation extends EclipseUIPlugin {
		/**
		 * Creates an instance.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public Implementation() {
			super();
	
			// Remember the static instance.
			//
			plugin = this;
		}
		
		
		@Override
		protected Object doGetImage(final String key) throws IOException {
			final URL url = new URL(getBaseURL() + "icons/" + key + ".png"); //$NON-NLS-1$ //$NON-NLS-2$
			final InputStream inputStream = url.openStream();
			inputStream.close();
			return url;
		}
		
		@Override
		protected void initializeImageRegistry(final ImageRegistry reg) {
			final ImageRegistryUtil util = new ImageRegistryUtil(this);
			
			util.register(IMG_OBJ_GGPLOT_TASK, ImageRegistryUtil.T_OBJ, "ggplot_task.png"); //$NON-NLS-1$
		}
		
	}

}
