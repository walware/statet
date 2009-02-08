/*******************************************************************************
 * Copyright (c) 2005-2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.ui.util;

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

import de.walware.ecommons.internal.ui.Messages;


public class DNDUtil {
	
	
	public static abstract class SimpleTextDropAdapter extends DropTargetAdapter {
		
		protected abstract StyledText getTextWidget();
		
		@Override
		public void dragEnter(final DropTargetEvent e) {
			if (e.detail == DND.DROP_DEFAULT && (e.operations & DND.DROP_COPY) != 0) {
				e.detail = DND.DROP_COPY;
			}
		}
		
		@Override
		public void dragOperationChanged(final DropTargetEvent e) {
			if (e.detail == DND.DROP_DEFAULT && (e.operations & DND.DROP_COPY) != 0) {
				e.detail = DND.DROP_COPY;
			}
		}
		
		@Override
		public void dragOver(final DropTargetEvent event) {
			event.feedback = DND.FEEDBACK_SCROLL | DND.FEEDBACK_SELECT;
		}
		
		@Override
		public void drop(final DropTargetEvent e) {
			getTextWidget().insert((String)e.data);
		}
	}
	
	
	public static boolean setContent(final Clipboard clipboard, final Object[] datas, final Transfer[] tranfers) {
		while (true) {
			try {
				clipboard.setContents(datas, tranfers);
				return true;
			}
			catch (final SWTError e) {
				if (e.code != DND.ERROR_CANNOT_SET_CLIPBOARD)
					throw e;
				
				if (!MessageDialog.openQuestion(
						UIAccess.getActiveWorkbenchShell(true),
						Messages.CopyToClipboard_error_title,
						Messages.CopyToClipboard_error_message))
					return false;
			}
		}
	}
	
	/**
	 * for common options (DEFAULT, MOVE, COPY).
	 * 
	 * @param viewer
	 * @param listener
	 * @param transferTypes
	 */
	public static void addDropSupport(final Control control, final DropTargetListener listener,
			final Transfer[] transferTypes) {
		addDropSupport(control, new DropTargetListener[] { listener },
				DND.DROP_DEFAULT | DND.DROP_MOVE | DND.DROP_COPY,
				transferTypes);
	}
	
	public static void addDropSupport(final Control control, final DropTargetListener[] listeners,
			final int operations, final Transfer[] transferTypes) {
		final DropTarget dropTarget = new DropTarget(control, operations);
		dropTarget.setTransfer(transferTypes);
		for (final DropTargetListener listener : listeners) {
			dropTarget.addDropListener(listener);
		}
		
		control.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(final DisposeEvent e) {
				e.widget.removeDisposeListener(this);
				dropTarget.dispose();
			}
		});
	}
	
	
	private DNDUtil() {}
	
}
