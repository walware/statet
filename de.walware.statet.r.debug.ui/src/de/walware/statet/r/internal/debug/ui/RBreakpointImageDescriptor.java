/*=============================================================================#
 # Copyright (c) 2010-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.debug.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.ecommons.debug.ui.WaDebugImages;


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
	public final static int ENABLED=                       0x00000001;
	/** Flag to render the installed breakpoint adornment */
	public final static int INSTALLED=                     0x00000002;
	/** Flag to render the script breakpoint adornment */
	public final static int SCRIPT=                        0x00000010;
	/** Flag to render the conditional breakpoint adornment */
	public final static int CONDITIONAL=                   0x00000020;
	/** Flag to render the entry method breakpoint adornment */
	public final static int ENTRY=                         0x00000100;
	/** Flag to render the exit method breakpoint adornment */
	public final static int EXIT=                          0x00000200;
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
	
	
	private static ImageData getImageData(final ImageDescriptor descriptor) {
		ImageData data= descriptor.getImageData(); // getImageData can return null
		if (data == null) {
			data= DEFAULT_IMAGE_DATA;
			StatusManager.getManager().handle(new Status(IStatus.WARNING, RDebugUIPlugin.PLUGIN_ID,
					"Image data not available: " + descriptor.toString() )); //$NON-NLS-1$
		}
		return data;
	}
	
	
	private final ImageDescriptor baseImage;
	private final int flags;
	private final Point size;
	
	
	/**
	 * Create a new RBreakpointImageDescriptor.
	 * 
	 * @param baseImage an image descriptor used as the base image
	 * @param flags flags indicating which adornments are to be rendered
	 * 
	 */
	public RBreakpointImageDescriptor(final ImageDescriptor baseImage, final int flags,
			final Point size) {
		if (baseImage == null) {
			throw new NullPointerException("baseImage");
		}
		this.baseImage= baseImage;
		this.flags= flags;
		if (size != null) {
			this.size= size;
		}
		else {
			final ImageData data= getImageData(baseImage);
			this.size= new Point(data.width, data.height);
		}
	}
	
	
	protected final ImageDescriptor getBaseImage() {
		return this.baseImage;
	}
	
	protected final int getFlags() {
		return this.flags;
	}
	
	@Override
	protected final Point getSize() {
		return this.size;
	}
	
	@Override
	protected void drawCompositeImage(final int width, final int height) {
		{	final ImageData data= getImageData(getBaseImage());
			drawImage(data, 0, 0);
		}
		
		drawOverlays();
	}
	
	protected void drawOverlays() {
		final int flags= getFlags();
		if ((flags & (INSTALLED | SCRIPT)) == INSTALLED) {
			final int x= 0;
			int y= getSize().y;
			final ImageData data= getImageData(WaDebugImages.getImageRegistry().getDescriptor(
					((flags & ENABLED) != 0) ?
							WaDebugImages.OVR_BREAKPOINT_INSTALLED :
							WaDebugImages.OVR_BREAKPOINT_INSTALLED_DISABLED ));
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
			final int x= 0;
			final int y= 0;
			final ImageData data= getImageData(WaDebugImages.getImageRegistry().getDescriptor(
					((flags & ENABLED) != 0) ?
							WaDebugImages.OVR_BREAKPOINT_CONDITIONAL :
							WaDebugImages.OVR_BREAKPOINT_CONDITIONAL_DISABLED ));
			drawImage(data, x, y);
		}
		if ((flags & ENTRY) == ENTRY) {
			int x= getSize().x;
			final int y= 0;
			final ImageData data= getImageData(WaDebugImages.getImageRegistry().getDescriptor(
					((flags & ENABLED) != 0) ?
							WaDebugImages.OVR_METHOD_BREAKPOINT_ENTRY :
							WaDebugImages.OVR_METHOD_BREAKPOINT_ENTRY_DISABLED ));
			x -= data.width;
			x -= 1;
			drawImage(data, x, y);
		}
		if ((flags & EXIT) == EXIT){
			int x= getSize().x;
			int y= getSize().y;
			final ImageData data= getImageData(WaDebugImages.getImageRegistry().getDescriptor(
					((flags & ENABLED) != 0) ?
							WaDebugImages.OVR_METHOD_BREAKPOINT_EXIT :
							WaDebugImages.OVR_METHOD_BREAKPOINT_EXIT_DISABLED ));
			x -= data.width;
			x -= 1;
			y -= data.height;
			drawImage(data, x, y);
		}
	}
	
	
	@Override
	public int hashCode() {
		return (this.baseImage.hashCode() ^ this.flags) + this.size.hashCode();
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj != null && getClass().equals(obj.getClass())) {
			final RBreakpointImageDescriptor other= (RBreakpointImageDescriptor) obj;
			return (this.baseImage.equals(other.baseImage)
					&& this.flags == other.flags
					&& this.size.equals(other.size) );
		}
		return false;
	}
	
}
