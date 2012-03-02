/*******************************************************************************
 * Copyright (c) 2010-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.debug.ui;

import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;

import de.walware.statet.base.ui.StatetDebugImages;


/**
 * A RBreakpointImageDescriptor consists of a main icon and several adornments. The adornments
 * are computed according to flags set on creation of the descriptor.
 */
public class RBreakpointImageDescriptor extends CompositeImageDescriptor {
	
//	/** Flag to render the is out of synch adornment */
//	public final static int IS_OUT_OF_SYNCH= 			0x0001;
//	/** Flag to render the may be out of synch adornment */
//	public final static int MAY_BE_OUT_OF_SYNCH= 		0x0002;
	/** Flag to render the enabled breakpoint adornment */
	public final static int ENABLED =                       0x00000001;
	/** Flag to render the installed breakpoint adornment */
	public final static int INSTALLED =                     0x00000002;
	/** Flag to render the script breakpoint adornment */
	public final static int SCRIPT =                        0x00000010;
	/** Flag to render the conditional breakpoint adornment */
	public final static int CONDITIONAL =                   0x00000020;
	/** Flag to render the entry method breakpoint adornment */
	public final static int ENTRY =                         0x00000100;
	/** Flag to render the exit method breakpoint adornment */
	public final static int EXIT =                          0x00000200;
//	/** Flag to render the caught breakpoint adornment */
//	public final static int CAUGHT=						0x0080;
//	/** Flag to render the uncaught breakpoint adornment */
//	public final static int UNCAUGHT=					0x0100;
//	/** Flag to render the scoped breakpoint adornment */
//	public final static int SCOPED=						0x0200;
//	
//	/** Flag to render the owning a monitor thread adornment */
//	public final static int OWNS_MONITOR=				0x0400;
//	/** Flag to render the owned monitor adornment */
//	public final static int OWNED_MONITOR=				0x0800;
//	/** Flag to render the in contention monitor adornment */
//	public final static int CONTENTED_MONITOR=			0x1000;
//	/** Flag to render the in contention for monitor thread adornment */
//	public final static int IN_CONTENTION_FOR_MONITOR=	0x2000;
//	/** Flag to render the in deadlock adornment */
//	public final static int IN_DEADLOCK= 				0x8000;
//	
//	/** Flag to render the synchronized stack frame adornment */
//	public final static int SYNCHRONIZED=				0x4000;
	
	
	private final ImageDescriptor fBaseImage;
	private final int fFlags;
	private Point fSize;
	
	
	/**
	 * Create a new RBreakpointImageDescriptor.
	 * 
	 * @param baseImage an image descriptor used as the base image
	 * @param flags flags indicating which adornments are to be rendered
	 * 
	 */
	public RBreakpointImageDescriptor(final ImageDescriptor baseImage, final int flags) {
		if (baseImage == null) {
			throw new NullPointerException("baseImage");
		}
		fBaseImage = baseImage;
		fFlags = flags;
	}
	
	
	@Override
	protected Point getSize() {
		if (fSize == null) {
			final ImageData data = getBaseImage().getImageData();
			fSize = new Point(data.width, data.height);
		}
		return fSize;
	}
	
	@Override
	protected void drawCompositeImage(final int width, final int height) {
		ImageData bg = getBaseImage().getImageData();
		if (bg == null) {
			bg = DEFAULT_IMAGE_DATA;
		}
		drawImage(bg, 0, 0);
		drawOverlays();
	}
	
	protected void drawOverlays() {
		final int flags = getFlags();
		if ((flags & (INSTALLED | SCRIPT)) == INSTALLED) {
			int x = 0;
			int y = getSize().y;
			final ImageData data = StatetDebugImages.getImageRegistry().getDescriptor(
					((flags & ENABLED) != 0) ?
							StatetDebugImages.OVR_BREAKPOINT_INSTALLED :
							StatetDebugImages.OVR_BREAKPOINT_INSTALLED_DISABLED )
					.getImageData();
			y -= data.height;
			drawImage(data, x, y);
		}
//		if ((flags & CAUGHT) != 0) {
//			if ((flags & ENABLED) !=0) {
//			data= getImageData(JavaDebugImages.IMG_OVR_CAUGHT_BREAKPOINT);
//			} else {
//				data= getImageData(JavaDebugImages.IMG_OVR_CAUGHT_BREAKPOINT_DISABLED);
//			}
//			x= 0;
//			y= 0;
//			drawImage(data, x, y);
//		}
//		if ((flags & UNCAUGHT) != 0) {
//			if ((flags & ENABLED) !=0) {
//				data= getImageData(JavaDebugImages.IMG_OVR_UNCAUGHT_BREAKPOINT);
//			} else {
//				data= getImageData(JavaDebugImages.IMG_OVR_UNCAUGHT_BREAKPOINT_DISABLED);
//			}
//			x= data.width;
//			y= data.height;
//			drawImage(data, x, y);
//		}
//		if ((flags & SCOPED) != 0) {
//			if ((flags & ENABLED) !=0) {
//				data= getImageData(JavaDebugImages.IMG_OVR_SCOPED_BREAKPOINT);
//			} else {
//				data= getImageData(JavaDebugImages.IMG_OVR_SCOPED_BREAKPOINT_DISABLED);
//			}
//			x= 0;
//			y= getSize().y;
//			y-= data.height;
//			drawImage(data, x, y);
//		}
		if ((flags & CONDITIONAL) != 0) {
			int x = 0;
			int y = 0;
			final ImageData data = StatetDebugImages.getImageRegistry().getDescriptor(
					((flags & ENABLED) != 0) ?
							StatetDebugImages.OVR_BREAKPOINT_CONDITIONAL :
							StatetDebugImages.OVR_BREAKPOINT_CONDITIONAL_DISABLED )
					.getImageData();
			drawImage(data, x, y);
		}
		if ((flags & ENTRY) == ENTRY) {
			int x = getSize().x;
			int y = 0;
			final ImageData data = StatetDebugImages.getImageRegistry().getDescriptor(
					((flags & ENABLED) != 0) ?
							StatetDebugImages.OVR_METHOD_BREAKPOINT_ENTRY :
							StatetDebugImages.OVR_METHOD_BREAKPOINT_ENTRY_DISABLED )
					.getImageData();
			x -= data.width;
			x -= 1;
			drawImage(data, x, y);
		}
		if ((flags & EXIT) == EXIT){
			int x = getSize().x;
			int y = getSize().y;
			final ImageData data = StatetDebugImages.getImageRegistry().getDescriptor(
					((flags & ENABLED) != 0) ?
							StatetDebugImages.OVR_METHOD_BREAKPOINT_EXIT :
							StatetDebugImages.OVR_METHOD_BREAKPOINT_EXIT_DISABLED )
					.getImageData();
			x -= data.width;
			x -= 1;
			y -= data.height;
			drawImage(data, x, y);
		}
	}
	
	protected final ImageDescriptor getBaseImage() {
		return fBaseImage;
	}
	
	protected final int getFlags() {
		return fFlags;
	}
	
	
	@Override
	public int hashCode() {
		return getBaseImage().hashCode() | getFlags();
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof RBreakpointImageDescriptor)){
			return false;
		}
		
		final RBreakpointImageDescriptor other = (RBreakpointImageDescriptor) obj;
		return (getBaseImage().equals(other.getBaseImage()) && getFlags() == other.getFlags());
	}
	
}
