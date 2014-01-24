/*=============================================================================#
 # Copyright (c) 2012-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.rtm.base.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.services.IDisposable;
import org.osgi.framework.BundleContext;

import de.walware.ecommons.ICommonStatusConstants;
import de.walware.ecommons.emf.ui.forms.EFColors;
import de.walware.ecommons.ui.util.ImageRegistryUtil;


public class RtModelUIPlugin extends AbstractUIPlugin {
	
	
	public static final String PLUGIN_ID = "de.walware.rtm.base.ui"; //$NON-NLS-1$
	
	public static final String R_GRAPHICS_PERSPECTIVE_ID = "de.walware.statet.rtm.base.perspectives.RGraphics"; //$NON-NLS-1$
	
	public static final String R_TASK_EDITOR_CONTEXT_ID = "de.walware.statet.rtm.contexts.RTaskEditor"; //$NON-NLS-1$
	
	public static final String RUN_R_TASK_COMMAND_ID = "de.walware.statet.rtm.commands.RunRTask"; //$NON-NLS-1$
	
	public static final String OBJ_UNKOWN_TYPE_IMAGE_ID = PLUGIN_ID + "/obj/rtype-unknown"; //$NON-NLS-1$
	
	public static final String OBJ_REXPR_TYPE_IMAGE_ID = PLUGIN_ID + "/obj/rtype-expr"; //$NON-NLS-1$
	public static final String OBJ_DATAFRAME_TYPE_IMAGE_ID = PLUGIN_ID + "/obj/rtype-dataframe"; //$NON-NLS-1$
	public static final String OBJ_COLUMN_TYPE_IMAGE_ID = PLUGIN_ID + "/obj/rtype-column"; //$NON-NLS-1$
	public static final String OBJ_COLOR_TYPE_IMAGE_ID = PLUGIN_ID + "/obj/rtype-color"; //$NON-NLS-1$
	public static final String OBJ_TEXT_TYPE_IMAGE_ID = PLUGIN_ID + "/obj/rtype-text"; //$NON-NLS-1$
	
	public static final boolean DEBUG = false;
	
	
	/** The shared instance */
	private static RtModelUIPlugin gPlugin;
	
	/**
	 * Returns the shared plug-in instance
	 *
	 * @return the shared instance
	 */
	public static RtModelUIPlugin getDefault() {
		return gPlugin;
	}
	
	
	private boolean fStarted;
	
	private final List<IDisposable> fDisposables = new ArrayList<IDisposable>();
	
	private EFColors fFormColors;
	
	
	public RtModelUIPlugin() {
	}
	
	
	@Override
	public void start(final BundleContext context) throws Exception {
		super.start(context);
		gPlugin = this;
		
		fStarted = true;
	}
	
	@Override
	public void stop(final BundleContext context) throws Exception {
		try {
			synchronized (this) {
				fStarted = false;
			}
			if (fFormColors != null) {
				fFormColors.dispose();
				fFormColors = null;
			}
			
			for (final IDisposable listener : fDisposables) {
				try {
					listener.dispose();
				}
				catch (final Throwable e) {
					getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, ICommonStatusConstants.INTERNAL_ERROR, "An error occured when disposing the plug-in.", e)); 
				}
			}
			fDisposables.clear();
		}
		finally {
			gPlugin = null;
			super.stop(context);
		}
	}
	
	
	public void addStoppingListener(final IDisposable listener) {
		if (listener == null) {
			throw new NullPointerException();
		}
		synchronized (this) {
			if (!fStarted) {
				throw new IllegalStateException("The plug-in is not started.");
			}
			fDisposables.add(listener);
		}
	}
	
	
	@Override
	protected void initializeImageRegistry(final ImageRegistry reg) {
		if (!fStarted) {
			throw new IllegalStateException("The plug-in is not started.");
		}
		final ImageRegistryUtil util = new ImageRegistryUtil(this);
		
		reg.put(OBJ_UNKOWN_TYPE_IMAGE_ID, ImageDescriptor.getMissingImageDescriptor());
		
		util.register(OBJ_REXPR_TYPE_IMAGE_ID, ImageRegistryUtil.T_OBJ, "rtyped-expr.png");
		util.register(OBJ_DATAFRAME_TYPE_IMAGE_ID, ImageRegistryUtil.T_OBJ, "rtyped-dataframe.png");
		util.register(OBJ_COLUMN_TYPE_IMAGE_ID, ImageRegistryUtil.T_OBJ, "rtyped-column.png");
		util.register(OBJ_COLOR_TYPE_IMAGE_ID, ImageRegistryUtil.T_OBJ, "rtyped-color.png");
		util.register(OBJ_TEXT_TYPE_IMAGE_ID, ImageRegistryUtil.T_OBJ, "rtyped-text.png");
	}
	
	public EFColors getFormColors(final Display display) {
		if (fFormColors == null) {
			if (!fStarted) {
				throw new IllegalStateException("The plug-in is not started.");
			}
			fFormColors = new EFColors(display);
			fFormColors.markShared();
		}
		return fFormColors;
	}
	
}
