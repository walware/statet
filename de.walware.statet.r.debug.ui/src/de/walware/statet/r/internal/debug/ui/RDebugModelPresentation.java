/*******************************************************************************
 * Copyright (c) 2010-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.debug.ui;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugModelPresentationExtension;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.IValueDetailListener;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;

import de.walware.ecommons.ui.util.ImageDescriptorRegistry;

import de.walware.rj.data.RObject;
import de.walware.rj.data.RReference;

import de.walware.statet.r.core.data.ICombinedRElement;
import de.walware.statet.r.debug.core.IRDebugTarget;
import de.walware.statet.r.debug.core.IRDimVariable;
import de.walware.statet.r.debug.core.IRElementVariable;
import de.walware.statet.r.debug.core.IRIndexVariable;
import de.walware.statet.r.debug.core.IRStackFrame;
import de.walware.statet.r.debug.core.IRThread;
import de.walware.statet.r.debug.core.RDebugModel;
import de.walware.statet.r.debug.core.breakpoints.IRBreakpoint;
import de.walware.statet.r.debug.core.breakpoints.IRBreakpointStatus;
import de.walware.statet.r.debug.core.breakpoints.IRLineBreakpoint;
import de.walware.statet.r.debug.core.breakpoints.IRMethodBreakpoint;
import de.walware.statet.r.debug.core.breakpoints.IRMethodBreakpointStatus;
import de.walware.statet.r.debug.core.sourcelookup.IRSourceLookupMatch;
import de.walware.statet.r.debug.core.sourcelookup.RRuntimeSourceFragment;
import de.walware.statet.r.ui.RLabelProvider;
import de.walware.statet.r.ui.RUI;


public class RDebugModelPresentation extends LabelProvider
		implements IDebugModelPresentation, IDebugModelPresentationExtension {
	
	
	private static boolean fIsResourcesInitilized = true;
	
	private static ImageDescriptorRegistry fImageDescriptorRegistry;
	
	private static RLabelProvider gLabelProvider = new RLabelProvider();
	
	private static boolean gCheckLength = Platform.getWS().equals(Platform.WS_GTK);
	
	private static void initResources() {
		getImageDescriptorRegistry();
		fIsResourcesInitilized = true;
	}
	
	protected static ImageDescriptorRegistry getImageDescriptorRegistry() {
		if (fImageDescriptorRegistry == null) {
			fImageDescriptorRegistry = RDebugUIPlugin.getDefault().getImageDescriptorRegistry();
		}
		return fImageDescriptorRegistry;
	}
	
	
	public RDebugModelPresentation() {
	}
	
	
	@Override
	public boolean requiresUIThread(final Object element) {
		return !fIsResourcesInitilized;
	}
	
	
	@Override
	public Image getImage(final Object element) {
		if (!fIsResourcesInitilized) {
			initResources();
		}
		try {
			if (element instanceof IRBreakpoint) {
				return getImage((IRBreakpoint) element);
			}
			if (element instanceof IRThread) {
				return getImage((IRThread) element);
			}
			if (element instanceof IRElementVariable) {
				ICombinedRElement rElement = ((IRElementVariable) element).getElement();
				if (rElement.getRObjectType() == RObject.TYPE_REFERENCE) {
					final RObject resolved = ((RReference) rElement).getResolvedRObject();
					if (resolved instanceof ICombinedRElement) {
						rElement = (ICombinedRElement) resolved;
					}
				}
				return gLabelProvider.getImage(rElement);
			}
			if (element instanceof IRIndexVariable) {
				return RUI.getImage(RUI.IMG_OBJ_ATOMIC);
			}
			if (element instanceof IRDimVariable) {
				return RDebugUIPlugin.getDefault().getImageRegistry().get(RDebugUIPlugin.IMG_OBJ_DIM);
			}
		}
		catch (final CoreException e) {}
		return null;
	}
	
	@Override
	public String getText(final Object element) {
		String text = null;
		try {
			if (element instanceof IRDebugTarget) {
				text = getText((IRDebugTarget) element);
			}
			else if (element instanceof IRBreakpoint) {
				text = getText((IRBreakpoint) element);
			}
			else if (element instanceof IRThread) {
				text = getText((IRThread) element);
			}
			else if (element instanceof IRStackFrame) {
				text = getText((IRStackFrame) element);
			}
		}
		catch (final CoreException e) {}
		if (gCheckLength && text != null && text.length() > 2000) {
			return text.substring(0, 2000);
		}
		return text;
	}
	
	
	protected String getText(final IRDebugTarget target) throws CoreException {
		final StringBuilder sb = new StringBuilder();
		if (target.isTerminated()) {
			sb.append("<terminated> ");
		}
		else if (target.isDisconnected()) {
			sb.append("<disconntected> ");
		}
		sb.append(target.getName());
		if (target.isSuspended()) {
			sb.append(" (Suspended)");
		}
		return sb.toString();
	}
	
	
	protected Image getImage(final IRThread thread) throws CoreException {
		if (thread.isSuspended() || thread.getDebugTarget().isSuspended()) {
			return DebugUITools.getImage(IDebugUIConstants.IMG_OBJS_THREAD_SUSPENDED);
		}
		return DebugUITools.getImage(IDebugUIConstants.IMG_OBJS_THREAD_RUNNING);
	}
	
	protected String getText(final IRThread thread) throws CoreException {
		final StringBuilder sb = new StringBuilder(thread.getName());
		if (thread.isSuspended()) {
			final IRBreakpointStatus breakpointStatus = getBreakpointStatus(thread);
			if (breakpointStatus != null) {
				int a = 0;
				if (breakpointStatus instanceof IRMethodBreakpointStatus) {
					final IRMethodBreakpointStatus mbStatus = (IRMethodBreakpointStatus) breakpointStatus;
					if (mbStatus.isEntry()) {
						sb.append(" (Suspended at entry of ");
						a = 1;
					}
					else if (mbStatus.isExit()) {
						sb.append(" (Suspended at exit of ");
						a = 1;
					}
				}
				if (a == 0) {
					sb.append(" (Suspended in ");
					a = 1;
				}
				
				final String label = breakpointStatus.getLabel();
				sb.append((label != null) ? label : "?");
				sb.append(" - hit breakpoint");
				sb.append(")");
			}
			else {
				sb.append(" (Suspended)");
			}
		}
		else if (thread.isStepping()) {
			sb.append(" (Stepping)");
		}
//		else if (thread.isEvaluation()) {
//			return name + " (Evaluating)";
//		}
		else {
			sb.append(" (Running)");
		}
		return sb.toString();
	}
	
	private IRBreakpointStatus getBreakpointStatus(final IRThread thread) {
		try {
			final IStackFrame frame = thread.getTopStackFrame();
			if (frame != null) {
				return (IRBreakpointStatus) frame.getAdapter(IRBreakpointStatus.class);
			}
		} catch (final DebugException e) {
		}
		return null;
	}
	
	protected String getText(final IRStackFrame frame) throws CoreException {
		final StringBuilder sb = new StringBuilder(32);
		if (frame.getPosition() >= 0) {
			sb.append(frame.getPosition());
			sb.append(": ");
		}
		sb.append(frame.getName());
		
		final String fileName = frame.getInfoFileName();
		if (fileName != null) {
			sb.append("  ·  ");
			sb.append(fileName);
			final int number = frame.getInfoLineNumber();
			if (number >= 0) {
				sb.append("#");
				sb.append(number);
			}
		}
		return sb.toString();
	}
	
	
	protected Image getImage(final IRBreakpoint breakpoint) throws CoreException {
		final int flags = computeBreakpointAdornmentFlags(breakpoint);
		final String imageKey;
		if ((flags & RBreakpointImageDescriptor.SCRIPT) != 0) {
			imageKey = ((flags & RBreakpointImageDescriptor.ENABLED) != 0) ?
					RDebugUIPlugin.IMG_OBJ_R_TOPLEVEL_BREAKPOINT :
					RDebugUIPlugin.IMG_OBJ_R_TOPLEVEL_BREAKPOINT_DISABLED;
		}
		else {
			imageKey = ((flags & RBreakpointImageDescriptor.ENABLED) != 0) ?
					RDebugUIPlugin.IMG_OBJ_R_BREAKPOINT :
					RDebugUIPlugin.IMG_OBJ_R_BREAKPOINT_DISABLED;
		}
		final ImageDescriptor descriptor = new RBreakpointImageDescriptor(
				RDebugUIPlugin.getDefault().getImageRegistry().getDescriptor(imageKey), flags );
		return getImageDescriptorRegistry().get(descriptor);
	}
	
	protected String getText(final IRBreakpoint breakpoint) throws CoreException {
		final IResource resource = breakpoint.getMarker().getResource();
		final StringBuilder label = new StringBuilder();
		if (resource != null) {
			label.append(resource.getName());
		}
		if (breakpoint instanceof IRLineBreakpoint) {
			final IRLineBreakpoint lineBreakpoint = (IRLineBreakpoint) breakpoint;
			
			try {
				final int lineNumber = lineBreakpoint.getLineNumber();
				label.append(" ["); //$NON-NLS-1$
				label.append(NLS.bind(Messages.Breakpoint_Line_label, Integer.toString(lineNumber)));
				label.append(']');
			}
			catch (final CoreException e) {}
			
			label.append(" - "); //$NON-NLS-1$
			final String subLabel = lineBreakpoint.getSubLabel();
			if (subLabel != null) {
				label.append(subLabel);
				label.append(' ');
				label.append(Messages.Breakpoint_SubLabel_copula);
				label.append(' ');
			}
			switch (lineBreakpoint.getElementType()) {
			case IRLineBreakpoint.R_COMMON_FUNCTION_ELEMENT_TYPE:
				label.append(Messages.Breakpoint_Function_prefix);
				label.append(' ');
				break;
			case IRLineBreakpoint.R_S4_METHOD_ELEMENT_TYPE:
				label.append(Messages.Breakpoint_S4Method_prefix);
				label.append(' ');
				break;
			case IRLineBreakpoint.R_TOPLEVEL_COMMAND_ELEMENT_TYPE:
				label.append(Messages.Breakpoint_ScriptLine_prefix);
				label.append(' ');
			}
			final String elementLabel = lineBreakpoint.getElementLabel();
			if (elementLabel != null) {
				label.append(elementLabel);
			}
			else {
				label.append("?"); //$NON-NLS-1$
			}
		}
		return label.toString();
	}
	
	/**
	 * Returns the adornment flags for the given breakpoint.
	 * These flags are used to render appropriate overlay icons for the breakpoint.
	 */
	private int computeBreakpointAdornmentFlags(final IRBreakpoint breakpoint)  {
		int flags = 0;
		try {
			if (breakpoint.isEnabled()) {
				flags |= RBreakpointImageDescriptor.ENABLED;
			}
			if (breakpoint.isInstalled()) {
				flags |= RBreakpointImageDescriptor.INSTALLED;
			}
			
			if (breakpoint.getBreakpointType() == RDebugModel.R_LINE_BREAKPOINT_TYPE_ID) {
				final IRLineBreakpoint lineBreakpoint = (IRLineBreakpoint) breakpoint;
				if (lineBreakpoint.getElementType() == IRLineBreakpoint.R_TOPLEVEL_COMMAND_ELEMENT_TYPE) {
					flags |= RBreakpointImageDescriptor.SCRIPT;
				}
				else if (lineBreakpoint.isConditionEnabled()) {
					flags |= RBreakpointImageDescriptor.CONDITIONAL;
				}
			}
			else if (breakpoint.getBreakpointType() == RDebugModel.R_METHOD_BREAKPOINT_TYPE_ID) {
				final IRMethodBreakpoint methodBreakpoint = (IRMethodBreakpoint) breakpoint;
				if (methodBreakpoint.isConditionEnabled()) {
					flags |= RBreakpointImageDescriptor.CONDITIONAL;
				}
				if (methodBreakpoint.isEntry()) {
					flags |= RBreakpointImageDescriptor.ENTRY;
				}
				if (methodBreakpoint.isExit()) {
					flags |= RBreakpointImageDescriptor.EXIT;
				}
			}
		}
		catch (final CoreException e) {
		}
		return flags;
	}
	
	
	@Override
	public IEditorInput getEditorInput(Object element) {
		if (element instanceof IMarker) {
			element = DebugPlugin.getDefault().getBreakpointManager().getBreakpoint((IMarker) element);
		}
		if (element instanceof IRBreakpoint) {
			element = ((IRBreakpoint) element).getMarker().getResource();
		}
		if (element instanceof IRSourceLookupMatch) {
			element = ((IRSourceLookupMatch) element).getElement();
		}
		if (element instanceof IFile) {
			return new FileEditorInput((IFile) element);
		}
		if (element instanceof IFileStore) {
			return new FileStoreEditorInput((IFileStore) element);
		}
		if (element instanceof RRuntimeSourceFragment) {
			return new RRuntimeSourceEditorInput((RRuntimeSourceFragment) element);
		}
		return null;
	}
	
	@Override
	public String getEditorId(final IEditorInput input, final Object element) {
		try {
			if (input instanceof IFileEditorInput) {
				return IDE.getEditorDescriptor(((IFileEditorInput) input).getFile()).getId();
			}
			else if (input instanceof RRuntimeSourceEditorInput) {
				return RUI.R_EDITOR_ID;
			}
			else {
				return IDE.getEditorDescriptor(input.getName()).getId();
			}
		}
		catch (final PartInitException e) {}
		return null;
	}
	
	@Override
	public void setAttribute(final String attribute, final Object value) {
	}
	
	@Override
	public void computeDetail(final IValue value, final IValueDetailListener listener) {
	}
	
}
