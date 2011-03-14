/*******************************************************************************
 * Copyright (c) 2006-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

import de.walware.statet.nico.internal.ui.NicoUIPlugin;
import de.walware.statet.nico.internal.ui.ToolSourceProvider;


/**
 * Public Nico-UI services.
 * <p>
 * Access via static methods.
 */
public class NicoUI {
	
	/**
	 * Plugin-ID
	 * Value: @value
	 */
	public static final String PLUGIN_ID = "de.walware.statet.nico.ui"; //$NON-NLS-1$
	
	
	public static final String TOOL_SOURCE_ID = ToolSourceProvider.ACTIVE_TOOL_NAME;
	
	
	public static final String LOCTOOL_CANCEL_IMAGE_ID = PLUGIN_ID + "/imgage/loctool/cancel";  //$NON-NLS-1$
	public static final String LOCTOOLD_CANCEL_IMAGE_ID = PLUGIN_ID + "/image/loctoold/cancel";  //$NON-NLS-1$
	
	public static final String LOCTOOL_PAUSE_IMAGE_ID = PLUGIN_ID + "/image/loctool/pause"; //$NON-NLS-1$
	public static final String LOCTOOLD_PAUSE_IMAGE_ID = PLUGIN_ID + "/image/loctoold/pause"; //$NON-NLS-1$
	
	public static final String OBJ_TASK_CONSOLECOMMAND_IMAGE_ID = PLUGIN_ID + "/image/obj/task.consolecommand"; //$NON-NLS-1$
	public static final String OBJ_TASK_DUMMY_IMAGE_ID = PLUGIN_ID + "/image/obj/task.commanddummy"; //$NON-NLS-1$
	
	public static final String OBJ_CONSOLECOMMAND_IMAGE_ID = PLUGIN_ID + "/image/obj/consolecommand"; //$NON-NLS-1$
	
	public static final String HISTORY_VIEW_ID = "de.walware.statet.nico.views.HistoryView"; //$NON-NLS-1$
	public static final String QUEUE_VIEW_ID = "de.walware.statet.nico.views.QueueView"; //$NON-NLS-1$
	
	public static final String PAUSE_COMMAND_ID = "de.walware.statet.nico.commands.PauseEngine"; //$NON-NLS-1$
	public static final String DISCONNECT_COMMAND_ID = "de.walware.statet.nico.commands.DisconnectEngine"; //$NON-NLS-1$
	public static final String RECONNECT_COMMAND_ID = "de.walware.statet.nico.commands.ReconnectEngine"; //$NON-NLS-1$
	
	public static final String CANCEL_CURRENT_COMMAND_ID = "de.walware.statet.nico.commands.CancelCurrent"; //$NON-NLS-1$
	public static final String CANCEL_ALL_COMMAND_ID = "de.walware.statet.nico.commands.CancelAll"; //$NON-NLS-1$
	public static final String CANCEL_PAUSE_COMMAND_ID = "de.walware.statet.nico.commands.CancelCurrentAndPause"; //$NON-NLS-1$
	
	
	public static ImageDescriptor getImageDescriptor(final String key) {
		return NicoUIPlugin.getDefault().getImageRegistry().getDescriptor(key);
	}
	
	public static Image getImage(final String key) {
		return NicoUIPlugin.getDefault().getImageRegistry().get(key);
	}
	
	public static IToolRegistry getToolRegistry() {
		return NicoUIPlugin.getDefault().getToolRegistry();
	}
	
	
	private NicoUI() {}
	
}
