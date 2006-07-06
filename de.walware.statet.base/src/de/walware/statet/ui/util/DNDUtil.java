/*******************************************************************************
 * Copyright (c) 2005-2006 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.ui.util;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Control;

import de.walware.eclipsecommons.ui.util.UIAccess;

import de.walware.statet.internal.ui.StatetMessages;


public class DNDUtil {


	public static abstract class SimpleTextDropAdapter extends DropTargetAdapter {
		
		protected abstract StyledText getTextWidget();

		public void dragEnter(DropTargetEvent e) {
			
			if (e.detail == DND.DROP_DEFAULT) {
				e.detail = DND.DROP_COPY;
			}
		}
		
		public void dragOperationChanged(DropTargetEvent e) {
			
			if (e.detail == DND.DROP_DEFAULT) {
				e.detail = DND.DROP_COPY;
			}
		}
		
		public void drop(DropTargetEvent e) {
			
			getTextWidget().insert((String)e.data);
		}
	}
	
	
	public static boolean setContent(Clipboard clipboard, Object[] datas, Transfer[] tranfers) {

		while (true) {
			try {
				clipboard.setContents(datas, tranfers);
				return true;
			}
			catch (SWTError e) {
				if (e.code != DND.ERROR_CANNOT_SET_CLIPBOARD)
					throw e;
	
				if (!MessageDialog.openQuestion(
						UIAccess.getActiveWorkbenchShell(true), 
						StatetMessages.CopyToClipboard_error_title,
						StatetMessages.CopyToClipboard_error_message))
					return false;
			}
		}
	}
	
	/**
	 * for common options (DEFAULT, MOVE, COPY).  
	 * @param viewer
	 * @param listener
	 * @param transferTypes
	 */
	public static void addDropSupport(Control control, DropTargetListener listener,
			Transfer[] transferTypes) {
		
		addDropSupport(control, new DropTargetListener[] { listener }, 
				DND.DROP_DEFAULT | DND.DROP_MOVE | DND.DROP_COPY, 
				transferTypes);
	}
	
	public static void addDropSupport(Control control, DropTargetListener[] listeners,
			int operations, Transfer[] transferTypes) {

		final DropTarget dropTarget = new DropTarget(control, operations);
		dropTarget.setTransfer(transferTypes);
		for (DropTargetListener listener : listeners) {
			dropTarget.addDropListener(listener);
		}
		
		control.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				e.widget.removeDisposeListener(this);
				dropTarget.dispose();
			}
		});
	}
}
