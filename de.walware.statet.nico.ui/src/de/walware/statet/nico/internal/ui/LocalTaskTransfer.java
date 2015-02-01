/*=============================================================================#
 # Copyright (c) 2009-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.nico.internal.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.ecommons.ICommonStatusConstants;
import de.walware.ecommons.ts.IToolRunnable;

import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.ui.NicoUI;


/**
 * Transfer type for {@link IToolRunnable}
 */
public final class LocalTaskTransfer extends ByteArrayTransfer {
	
	// First attempt to create a UUID for the type name to make sure that
	// different Eclipse applications use different "types" of
	// <code>LocalTaskTransfer</code>
	private static final String TYPE_NAME = "de.walware.statet.nico-task-transfer-format" + (new Long(System.currentTimeMillis())).toString(); //$NON-NLS-1$;
	
	private static final int TYPEID = registerType(TYPE_NAME);
	
	private static final LocalTaskTransfer INSTANCE = new LocalTaskTransfer();
	
	
	/**
	 * The transfered data
	 */
	public static class Data {
		
		public final ToolProcess process;
		public IToolRunnable[] runnables;
		
		private Data(final ToolProcess process) {
			this.process = process;
		}
		
	}
	
	/**
	 * Returns the singleton.
	 * 
	 * @return the singleton
	 */
	public static LocalTaskTransfer getTransfer() {
		return INSTANCE;
	}
	
	
	private ToolProcess fProcess;
	private Data fData;
	
	
	/**
	 * Only the singleton instance of this class may be used. 
	 */
	protected LocalTaskTransfer() {
		// do nothing
	}
	
	
	/**
	 * Tests whether native drop data matches this transfer type.
	 * 
	 * @param result result of converting the native drop data to Java
	 * @return true if the native drop data does not match this transfer type.
	 *     false otherwise.
	 */
	private boolean isInvalidNativeType(final Object result) {
		return !(result instanceof byte[])
				|| !TYPE_NAME.equals(new String((byte[]) result));
	}
	
	/**
	 * Returns the type id used to identify this transfer.
	 * 
	 * @return the type id used to identify this transfer.
	 */
	@Override
	protected int[] getTypeIds() {
		return new int[] { TYPEID };
	}
	
	/**
	 * Returns the type name used to identify this transfer.
	 * 
	 * @return the type name used to identify this transfer.
	 */
	@Override
	protected String[] getTypeNames() {
		return new String[] { TYPE_NAME };
	}
	
	/**
	 * Overrides org.eclipse.swt.dnd.ByteArrayTransfer#javaToNative(Object, TransferData).
	 * Only encode the transfer type name since the selection is read and
	 * written in the same process.
	 * 
	 * @see org.eclipse.swt.dnd.ByteArrayTransfer#javaToNative(java.lang.Object, org.eclipse.swt.dnd.TransferData)
	 */
	@Override
	public void javaToNative(final Object object, final TransferData transferData) {
		if (object instanceof Data) {
			fData = (Data) object;
		}
		final byte[] check = TYPE_NAME.getBytes();
		super.javaToNative(check, transferData);
	}
	
	/**
	 * Overrides org.eclipse.swt.dnd.ByteArrayTransfer#nativeToJava(TransferData).
	 * Test if the native drop data matches this transfer type.
	 * 
	 * @see org.eclipse.swt.dnd.ByteArrayTransfer#nativeToJava(TransferData)
	 */
	@Override
	public Object nativeToJava(final TransferData transferData) {
		final Object result = super.nativeToJava(transferData);
		if (isInvalidNativeType(result)) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, NicoUI.PLUGIN_ID, ICommonStatusConstants.INTERNAL_ERROR,
					"invalid transfer type", null)); //$NON-NLS-1$
		}
		return fData;
	}
	
	
	/**
	 * Must be called by the drag adapter to initialize new dnd
	 * (usually in {@link DragSourceListener#dragStart(org.eclipse.swt.dnd.DragSourceEvent)})
	 * @param process
	 */
	public void init(final ToolProcess process) {
		fProcess = process;
	}
	
	/**
	 * Must be called by the drag adapter to create the transfer data
	 * for the current dnd
	 * 
	 * @return new data object or <code>null</code> if no current dnd
	 */
	public Data createData() {
		if (fProcess != null) {
			return new Data(fProcess);
		}
		return null;
	}
	
	/**
	 * Must be called by the drag adapter to finish current dnd
	 * (usually in {@link DragSourceListener#dragFinished(org.eclipse.swt.dnd.DragSourceEvent)})
	 */
	public void finished() {
		fProcess = null;
		fData = null;
	}
	
	/**
	 * Returns the main tool type for the current dnd
	 * so the drop adapter can check the type
	 * @return main type or <code>null</code> if no current dnd
	 */
	public String getMainType() {
		if (fProcess != null) {
			return fProcess.getMainType();
		}
		return null;
	}
	
}
