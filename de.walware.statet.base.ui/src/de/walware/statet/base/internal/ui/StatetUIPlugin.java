/*******************************************************************************
 * Copyright (c) 2005-2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.base.internal.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.services.IDisposable;
import org.osgi.framework.BundleContext;

import de.walware.eclipsecommons.ICommonStatusConstants;
import de.walware.eclipsecommons.ui.util.ColorManager;
import de.walware.eclipsecommons.ui.util.ImageRegistryUtil;
import de.walware.eclipsecommons.ui.util.UIAccess;

import de.walware.statet.base.ui.StatetImages;


/**
 * The main plugin class to be used in the desktop.
 */
public class StatetUIPlugin extends AbstractUIPlugin {
	
	/**
	 * Plugin-ID
	 * Value: @value
	 */
	public static final String PLUGIN_ID = "de.walware.statet.base.ui"; //$NON-NLS-1$
	
	
	public static void log(final IStatus status) {
		if (status != null) {
			getDefault().getLog().log(status);
		}
	}
	
	public static void logError(final int code, final String message, final Throwable e) {
		log(new Status(IStatus.ERROR, PLUGIN_ID, code, message, e));
	}
	
	public static void logUnexpectedError(final Throwable e) {
		log(new Status(IStatus.ERROR, PLUGIN_ID, ICommonStatusConstants.INTERNAL_ERROR, StatetMessages.InternalError_UnexpectedException, e));
	}
	
	
	/** The shared instance. */
	private static StatetUIPlugin gPlugin;
	
	/**
	 * Returns the shared instance.
	 */
	public static StatetUIPlugin getDefault() {
		return gPlugin;
	}
	
	
	private ColorManager fColorManager;
	private ImageRegistry fImageRegistry;
	private WorkbenchLabelProvider fWorkbenchLabelProvider;
	
	private List<IDisposable> fDisposables;
	
	
	/**
	 * The constructor.
	 */
	public StatetUIPlugin() {
		gPlugin = this;
	}
	
	
	/**
	 * This method is called upon plug-in activation
	 */
	@Override
	public void start(final BundleContext context) throws Exception {
		fDisposables = new ArrayList<IDisposable>();
		super.start(context);
	}
	
	/**
	 * This method is called when the plug-in is stopped
	 */
	@Override
	public void stop(final BundleContext context) throws Exception {
		try {
			for (final IDisposable d : fDisposables) {
				try {
					d.dispose();
				}
				catch (final Throwable e) {
					logError(-1, "Error occured when dispose module", e); //$NON-NLS-1$
				}
			}
			fDisposables = null;
			
			final Display display = UIAccess.getDisplay();
			if (display != null && !display.isDisposed()) {
				final ColorManager colorManager = fColorManager;
				final ImageRegistry imageRegistry = fImageRegistry;
				final WorkbenchLabelProvider workbenchLabeler = fWorkbenchLabelProvider;
				display.asyncExec(new Runnable() {
					public void run() {
						if (colorManager != null) {
							try {
								colorManager.dispose();
							}
							catch (final Exception e) {}
						}
						if (imageRegistry != null) {
							try {
								imageRegistry.dispose();
							}
							catch (final Exception e) {}
						}
						if (workbenchLabeler != null) {
							try {
								workbenchLabeler.dispose();
							}
							catch (final Exception e) {}
						}
					}
				});
			}
			else {
				if (fWorkbenchLabelProvider != null) {
					try {
						fWorkbenchLabelProvider.dispose();
					}
					catch (final Exception e) {}
				}
			}
			fColorManager = null;
			fImageRegistry = null;
			fWorkbenchLabelProvider = null;
		}
		finally {
			super.stop(context);
			gPlugin = null;
		}
	}
	
	@Override
	protected void initializeImageRegistry(final ImageRegistry reg) {
		fImageRegistry = reg;
		final ImageRegistryUtil util = new ImageRegistryUtil(this);
		util.register(StatetImages.LOCTOOL_FILTER, ImageRegistryUtil.T_LOCTOOL, "filter_view.gif"); //$NON-NLS-1$
		util.register(StatetImages.LOCTOOLD_FILTER, ImageRegistryUtil.T_LOCTOOL_D, "filter_view.gif"); //$NON-NLS-1$
		util.register(StatetImages.LOCTOOL_SORT_ALPHA, ImageRegistryUtil.T_LOCTOOL, "sort_alpha.gif"); //$NON-NLS-1$
		util.register(StatetImages.LOCTOOL_EXPANDALL, ImageRegistryUtil.T_LOCTOOL, "expandall.gif"); //$NON-NLS-1$
		util.register(StatetImages.LOCTOOL_COLLAPSEALL, ImageRegistryUtil.T_LOCTOOL, "collapseall.gif"); //$NON-NLS-1$
		util.register(StatetImages.LOCTOOL_SCROLLLOCK, ImageRegistryUtil.T_LOCTOOL, "scrolllock.gif"); //$NON-NLS-1$
		util.register(StatetImages.LOCTOOL_PAUSE, ImageRegistryUtil.T_LOCTOOL, "pause.gif"); //$NON-NLS-1$
		util.register(StatetImages.LOCTOOLD_PAUSE, ImageRegistryUtil.T_LOCTOOL_D, "pause.gif"); //$NON-NLS-1$
		util.register(StatetImages.lOCTOOL_SYNCHRONIZED, ImageRegistryUtil.T_LOCTOOL, "synced.png"); //$NON-NLS-1$
		
		util.register(StatetImages.CONTENTASSIST_TEMPLATE, ImageRegistryUtil.T_OBJ, "assist-template.png"); //$NON-NLS-1$
		util.register(StatetImages.CONTENTASSIST_CORRECTION_LINKEDRENAME, ImageRegistryUtil.T_OBJ, "assist-linked_rename.png"); //$NON-NLS-1$
		
		util.register(StatetImages.LAUNCHCONFIG_MAIN, ImageRegistryUtil.T_OBJ, "main_tab.gif"); //$NON-NLS-1$
		
		util.register(StatetImages.OBJ_TASK_CONSOLECOMMAND, ImageRegistryUtil.T_OBJ, "task-consolecommand.png"); //$NON-NLS-1$
		util.register(StatetImages.OBJ_TASK_DUMMY, ImageRegistryUtil.T_OBJ, "task-dummy.png"); //$NON-NLS-1$
		util.register(StatetImages.OBJ_CONSOLECOMMAND, ImageRegistryUtil.T_OBJ, "consolecommand.png"); //$NON-NLS-1$
		util.register(StatetImages.OBJ_IMPORT, ImageRegistryUtil.T_OBJ, "ltk-import.png"); //$NON-NLS-1$
		util.register(StatetImages.OBJ_CLASS, ImageRegistryUtil.T_OBJ, "ltk-class.png"); //$NON-NLS-1$
		util.register(StatetImages.OBJ_CLASS_EXT, ImageRegistryUtil.T_OBJ, "ltk-class_ext.png"); //$NON-NLS-1$
		
		util.register(StatetImages.OVR_DEFAULT_MARKER, ImageRegistryUtil.T_OVR, "default_marker.gif"); //$NON-NLS-1$
		
		UIAccess.getDisplay().syncExec(new Runnable() {
			public void run() {
				final Display display = Display.getCurrent();
				final int[] cross = new int[] { 
						 3,  3,  5,  3,  7,  5,  8,  5, 10,  3, 12,  3, 
						12,  5, 10,  7, 10,  8, 12, 10, 12, 12,
						10, 12,  8, 10,  7, 10,  5, 12,  3, 12,
						 3, 10,  5,  8,  5,  7,  3,  5,
				};
				final int[] right = new int[] { 
						 5,  3,  8,  3, 12,  7, 12,  8,  8, 12,  5, 12,
						 5, 11,  8,  8,  8,  7,  5,  4, 
				};
				final int[] left = new int[right.length];
				final int[] up = new int[right.length];
				final int[] down = new int[right.length];
				for (int i = 0; i < right.length; i = i+2) {
					final int j = i+1;
					final int x = right[i];
					final int y = right[j];
					left[i] = 16-x;
					left[j] = y;
					up[i] = y;
					up[j] = 16-x;
					down[i] = y;
					down[j] = x;
				}
				
				final Color border = display.getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW);
				final Color background = display.getSystemColor(SWT.COLOR_LIST_BACKGROUND);
				final Color hotRed = new Color(display, new RGB(252, 160, 160));
				final Color hotYellow = new Color(display, new RGB(252, 232, 160));
				final Color transparent = display.getSystemColor(SWT.COLOR_MAGENTA);
				
				final PaletteData palette = new PaletteData(new RGB[] { transparent.getRGB(), border.getRGB(), background.getRGB(), hotRed.getRGB(), hotYellow.getRGB() });
				final ImageData data = new ImageData(16, 16, 8, palette);
				data.transparentPixel = 0;
				
				{	// Close
					final Image image = new Image(display, data);
					image.setBackground(transparent);
					final GC gc = new GC(image);
					gc.setBackground(background);
					gc.fillPolygon(cross);
					gc.setForeground(border);
					gc.drawPolygon(cross);
					gc.dispose();
					
					reg.put(StatetImages.LOCTOOL_CLOSETRAY, image);
				}
				{	// Close hot
					final Image image = new Image(display, data);
					image.setBackground(transparent);
					final GC gc = new GC(image);
					gc.setBackground(hotRed);
					gc.fillPolygon(cross);
					gc.setForeground(border);
					gc.drawPolygon(cross);
					gc.dispose();
					
					reg.put(StatetImages.LOCTOOL_CLOSETRAY_H, image);
				}
				{	// Left
					final Image image = new Image(display, data);
					image.setBackground(transparent);
					final GC gc = new GC(image);
					gc.setBackground(background);
					gc.fillPolygon(left);
					gc.setForeground(border);
					gc.drawPolygon(left);
					gc.dispose();
					
					reg.put(StatetImages.LOCTOOL_LEFT, image);
				}
				{	// Left hot
					final Image image = new Image(display, data);
					image.setBackground(transparent);
					final GC gc = new GC(image);
					gc.setBackground(hotYellow);
					gc.fillPolygon(left);
					gc.setForeground(border);
					gc.drawPolygon(left);
					gc.dispose();
					
					reg.put(StatetImages.LOCTOOL_LEFT_H, image);
				}
				{	// Right
					final Image image = new Image(display, data);
					image.setBackground(transparent);
					final GC gc = new GC(image);
					gc.setBackground(background);
					gc.fillPolygon(right);
					gc.setForeground(border);
					gc.drawPolygon(right);
					gc.dispose();
					
					reg.put(StatetImages.LOCTOOL_RIGHT, image);
				}
				{	// Right hot
					final Image image = new Image(display, data);
					image.setBackground(transparent);
					final GC gc = new GC(image);
					gc.setBackground(hotYellow);
					gc.fillPolygon(right);
					gc.setForeground(border);
					gc.drawPolygon(right);
					gc.dispose();
					
					reg.put(StatetImages.LOCTOOL_RIGHT_H, image);
				}
				{	// Up
					final Image image = new Image(display, data);
					image.setBackground(transparent);
					final GC gc = new GC(image);
					gc.setBackground(background);
					gc.fillPolygon(up);
					gc.setForeground(border);
					gc.drawPolygon(up);
					gc.dispose();
					
					reg.put(StatetImages.LOCTOOL_UP, image);
				}
				{	// Up hot
					final Image image = new Image(display, data);
					image.setBackground(transparent);
					final GC gc = new GC(image);
					gc.setBackground(hotYellow);
					gc.fillPolygon(up);
					gc.setForeground(border);
					gc.drawPolygon(up);
					gc.dispose();
					
					reg.put(StatetImages.LOCTOOL_UP_H, image);
				}
				{	// Down
					final Image image = new Image(display, data);
					image.setBackground(transparent);
					final GC gc = new GC(image);
					gc.setBackground(background);
					gc.fillPolygon(down);
					gc.setForeground(border);
					gc.drawPolygon(down);
					gc.dispose();
					
					reg.put(StatetImages.LOCTOOL_DOWN, image);
				}
				{	// Down hot
					final Image image = new Image(display, data);
					image.setBackground(transparent);
					final GC gc = new GC(image);
					gc.setBackground(hotYellow);
					gc.fillPolygon(down);
					gc.setForeground(border);
					gc.drawPolygon(down);
					gc.dispose();
					
					reg.put(StatetImages.LOCTOOL_DOWN_H, image);
				}
				
				hotRed.dispose();
				hotYellow.dispose();
			}
		});
	}
	
	public synchronized ColorManager getColorManager() {
		if (fColorManager == null)
			fColorManager = new ColorManager();
		
		return fColorManager;
	}
	
	/**
	 * To access decoration.
	 */
	public WorkbenchLabelProvider getWorkbenchLabelProvider() {
		if (fWorkbenchLabelProvider == null) {
			fWorkbenchLabelProvider = new WorkbenchLabelProvider();
		}
		return fWorkbenchLabelProvider;
	}
	
	public void registerPluginDisposable(final IDisposable d) {
		final List<IDisposable> disposables = fDisposables;
		if (disposables != null) {
			disposables.add(d);
		}
		else {
			throw new IllegalStateException();
		}
	}
	
}
