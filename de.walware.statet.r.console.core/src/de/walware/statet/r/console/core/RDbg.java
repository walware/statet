/*=============================================================================#
 # Copyright (c) 2011-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.console.core;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugEvent;

import de.walware.ecommons.io.FileUtil;
import de.walware.ecommons.ltk.core.model.ISourceStructElement;
import de.walware.ecommons.ltk.core.model.ISourceUnit;

import de.walware.rj.server.dbg.DbgRequest;
import de.walware.rj.server.dbg.SrcfileData;
import de.walware.rj.server.dbg.Srcref;

import de.walware.statet.r.core.model.IRElement;
import de.walware.statet.r.core.model.IRLangSourceElement;
import de.walware.statet.r.nico.IRSrcref;
import de.walware.statet.r.nico.RSrcref;


/**
 * Helper for rj.server.dbg in Eclipse/StatET
 */
public class RDbg {
	
	
	public static String getElementId(IRLangSourceElement element) {
		ISourceStructElement parent = element.getSourceParent();
		if (!(parent instanceof IRLangSourceElement)
				|| (parent.getElementType() & IRElement.MASK_C1) == IRElement.C1_SOURCE ) {
			return element.getId();
		}
		final StringBuilder sb = new StringBuilder(element.getId());
		element = (IRLangSourceElement) parent;
		while (true) {
			sb.insert(0, '/');
			sb.insert(0, element.getId());
			parent = element.getSourceParent();
			if (!(parent instanceof IRLangSourceElement)
					|| (parent.getElementType() & IRElement.MASK_C1) == IRElement.C1_SOURCE ) {
				return sb.toString();
			}
			element = (IRLangSourceElement) parent;
		}
	}
	
	public static long getTimestamp(final ISourceUnit su, final IProgressMonitor monitor) {
		if (su.isSynchronized() && su.getResource() != null) {
			final FileUtil fileUtil = FileUtil.getFileUtil(su.getResource());
			if (fileUtil != null) {
				try {
					return fileUtil.getTimeStamp(monitor)/1000;
				}
				catch (final CoreException e) {
				}
			}
		}
		return 0;
	}
	
	public static SrcfileData createRJSrcfileData(final IResource resource) {
		final IPath location = resource.getLocation();
		return new SrcfileData(resource.getFullPath().toPortableString(),
				(location != null) ? location.toString() : null,
				resource.getLocalTimeStamp()/1000 );
	}
	
	public static int[] createRJSrcref(final IRSrcref srcref) {
		if (srcref == null || srcref.getFirstLine() < 0) {
			return null;
		}
		final int[] array = new int[6];
		if (srcref.getFirstLine() >= 0) {
			array[Srcref.BEGIN_LINE] = srcref.getFirstLine() + 1;
		}
		else {
			array[Srcref.BEGIN_LINE] = Srcref.NA;
		}
		if (srcref.getFirstColumn() >= 0) {
			array[Srcref.BEGIN_COLUMN] = srcref.getFirstColumn() + 1;
		}
		else {
			array[Srcref.BEGIN_COLUMN] = Srcref.NA;
		}
		array[Srcref.BEGIN_BYTE] = Srcref.NA;
		
		if (srcref.getLastLine() >= 0) {
			array[Srcref.END_LINE] = srcref.getLastLine() + 1;
		}
		else {
			array[Srcref.END_LINE] = Srcref.NA;
		}
		if (srcref.getLastColumn() >= 0) {
			array[Srcref.END_COLUMN] = srcref.getLastColumn() + 1;
		}
		else {
			array[Srcref.END_COLUMN] = Srcref.NA;
		}
		array[Srcref.END_BYTE] = Srcref.NA;
		
		return array;
	}
	
	public static IRSrcref createStatetSrcref(final int[] data) {
		if (data == null || data.length < 6) {
			return null;
		}
		return new RSrcref(
				(data[Srcref.BEGIN_LINE] > 0) ? data[Srcref.BEGIN_LINE] - 1 : -1,
				(data[Srcref.BEGIN_COLUMN] > 0) ? data[Srcref.BEGIN_COLUMN] -1 : -1,
				(data[Srcref.END_LINE] > 0) ? data[Srcref.END_LINE] - 1 : -1,
				(data[Srcref.END_COLUMN] > 0) ? data[Srcref.END_COLUMN] : -1 );
	}
	
	public static int getResumeEventDetail(final byte op) {
		switch (op) {
		case DbgRequest.RESUME:
			return DebugEvent.CLIENT_REQUEST;
		case DbgRequest.STEP_INTO:
			return DebugEvent.STEP_INTO;
		case DbgRequest.STEP_OVER:
			return DebugEvent.STEP_OVER;
		case DbgRequest.STEP_RETURN:
			return DebugEvent.STEP_RETURN;
		default:
			return DebugEvent.UNSPECIFIED;
		}
	}
	
	
	private RDbg() {
	}
	
}
