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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IErrorReportingExpression;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.core.model.IWatchExpression;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugModelPresentationExtension;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.IValueDetailListener;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;

import de.walware.ecommons.debug.core.model.IIndexedVariableItem;
import de.walware.ecommons.debug.core.model.IIndexedVariablePartition;
import de.walware.ecommons.debug.core.model.IVariableDim;
import de.walware.ecommons.debug.ui.ECommonsDebugUIImageDescriptor;
import de.walware.ecommons.debug.ui.ECommonsDebugUIResources;
import de.walware.ecommons.ui.SharedUIResources;
import de.walware.ecommons.ui.util.ImageDescriptorRegistry;

import de.walware.rj.data.RObject;
import de.walware.rj.data.RReference;

import de.walware.statet.r.core.data.ICombinedRElement;
import de.walware.statet.r.debug.core.IRDebugTarget;
import de.walware.statet.r.debug.core.IRElementVariable;
import de.walware.statet.r.debug.core.IRStackFrame;
import de.walware.statet.r.debug.core.IRThread;
import de.walware.statet.r.debug.core.IRVariable;
import de.walware.statet.r.debug.core.RDebugModel;
import de.walware.statet.r.debug.core.breakpoints.IRBreakpoint;
import de.walware.statet.r.debug.core.breakpoints.IRBreakpointStatus;
import de.walware.statet.r.debug.core.breakpoints.IRExceptionBreakpointStatus;
import de.walware.statet.r.debug.core.breakpoints.IRLineBreakpoint;
import de.walware.statet.r.debug.core.breakpoints.IRMethodBreakpoint;
import de.walware.statet.r.debug.core.breakpoints.IRMethodBreakpointStatus;
import de.walware.statet.r.debug.core.sourcelookup.IRSourceLookupMatch;
import de.walware.statet.r.debug.core.sourcelookup.RRuntimeSourceFragment;
import de.walware.statet.r.ui.RLabelProvider;
import de.walware.statet.r.ui.RUI;


public class RDebugModelPresentation extends LabelProvider
		implements IDebugModelPresentation, IDebugModelPresentationExtension {
	
	
	private static boolean gCheckLength = Platform.getWS().equals(Platform.WS_GTK);
	
	
	private static boolean isResourcesInitilized= false;
	
	
	private boolean isInitilized;
	
	private ImageRegistry imageRegistry;
	private ImageDescriptorRegistry imageDescriptorRegistry;
	
	private final RLabelProvider rLabelProvider = new RLabelProvider();
	
	private final Map<String, Object> attributes= new ConcurrentHashMap<>();
	
	
	public RDebugModelPresentation() {
	}
	
	
	@Override
	public void dispose() {
		super.dispose();
		
		this.rLabelProvider.dispose();
		this.attributes.clear();
	}
	
	
	@Override
	public void setAttribute(final String attribute, final Object value) {
		if (attribute == null) {
			return;
		}
		this.attributes.put(attribute, value);
	}
	
	protected final boolean isShowVariableTypeNames() {
		final Boolean value= (Boolean) this.attributes.get(DISPLAY_VARIABLE_TYPE_NAMES);
		return (value != null) ? value.booleanValue() : Boolean.FALSE;
	}
	
	
	@Override
	public boolean requiresUIThread(final Object element) {
		return (!RDebugModelPresentation.isResourcesInitilized);
	}
	
	private void init() {
		final RDebugUIPlugin plugin= RDebugUIPlugin.getDefault();
		this.imageRegistry= plugin.getImageRegistry();
		this.imageDescriptorRegistry= plugin.getImageDescriptorRegistry();
		this.isInitilized= true;
		RDebugModelPresentation.isResourcesInitilized= true;
	}
	
	
	@Override
	public Image getImage(final Object element) {
		if (!this.isInitilized) {
			init();
		}
		try {
			if (element instanceof IRBreakpoint) {
				return getImage((IRBreakpoint) element);
			}
			else if (element instanceof IErrorReportingExpression) {
				if (element instanceof IWatchExpression) {
					return null;
				}
				return this.imageRegistry.get(RDebugUIPlugin.IMG_OBJ_R_INSPECT_EXPRESSION);
			}
			else if (element instanceof IRThread) {
				return getImage((IRThread) element);
			}
			else if (element instanceof IVariable) {
				if (element instanceof IRElementVariable) {
					ICombinedRElement rElement = ((IRElementVariable) element).getElement();
					if (rElement.getRObjectType() == RObject.TYPE_REFERENCE) {
						final RObject resolved = ((RReference) rElement).getResolvedRObject();
						if (resolved instanceof ICombinedRElement) {
							rElement = (ICombinedRElement) resolved;
						}
					}
					return this.rLabelProvider.getImage(rElement);
				}
				if (element instanceof IIndexedVariablePartition) {
					return ECommonsDebugUIResources.INSTANCE.getImage(
							ECommonsDebugUIResources.OBJ_VARIABLE_PARTITION );
				}
				if (element instanceof IIndexedVariableItem) {
					return ECommonsDebugUIResources.INSTANCE.getImage(
							ECommonsDebugUIResources.OBJ_VARIABLE_ITEM );
				}
				if (element instanceof IVariableDim) {
					return ECommonsDebugUIResources.INSTANCE.getImage(
							ECommonsDebugUIResources.OBJ_VARIABLE_DIM );
				}
			}
		}
		catch (final CoreException e) {}
		return null;
	}
	
	@Override
	public String getText(final Object element) {
		String text= null;
		try {
			if (element instanceof IRDebugTarget) {
				text= getText((IRDebugTarget) element);
			}
			else if (element instanceof IRBreakpoint) {
				text= getText((IRBreakpoint) element);
			}
			else if (element instanceof IRThread) {
				text= getText((IRThread) element);
			}
			else if (element instanceof IRStackFrame) {
				text= getText((IRStackFrame) element);
			}
			else if (element instanceof IRVariable) {
				text= getText((IRVariable) element);
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
			final IRBreakpointStatus status= getBreakpointStatus(thread);
			if (status != null) {
				int a = 0;
				if (status instanceof IRMethodBreakpointStatus) {
					final IRMethodBreakpointStatus mbStatus= (IRMethodBreakpointStatus) status;
					if (mbStatus.isEntry()) {
						sb.append(" (Suspended at entry of ");
						a= 1;
					}
					else if (mbStatus.isExit()) {
						sb.append(" (Suspended at exit of ");
						a= 1;
					}
				}
				if (status instanceof IRExceptionBreakpointStatus) {
					sb.append(" (Suspended on ");
					a= 1;
				}
				if (a == 0) {
					sb.append(" (Suspended in ");
					a = 1;
				}
				
				final String label = status.getLabel();
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
		final IStackFrame frame = thread.getTopStackFrame();
		if (frame != null) {
			return frame.getAdapter(IRBreakpointStatus.class);
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
		final int flags= computeBreakpointAdornmentFlags(breakpoint);
		final String imageKey;
		if (breakpoint.getBreakpointType() == RDebugModel.R_EXCEPTION_BREAKPOINT_TYPE_ID) {
			imageKey= ((flags & ECommonsDebugUIImageDescriptor.ENABLED) != 0) ?
					RDebugUIPlugin.IMG_OBJ_R_EXCEPTION_BREAKPOINT :
					RDebugUIPlugin.IMG_OBJ_R_EXCEPTION_BREAKPOINT_DISABLED;
		}
		else if ((flags & ECommonsDebugUIImageDescriptor.SCRIPT) != 0) {
			imageKey= ((flags & ECommonsDebugUIImageDescriptor.ENABLED) != 0) ?
					RDebugUIPlugin.IMG_OBJ_R_TOPLEVEL_BREAKPOINT :
					RDebugUIPlugin.IMG_OBJ_R_TOPLEVEL_BREAKPOINT_DISABLED;
		}
		else {
			imageKey = ((flags & ECommonsDebugUIImageDescriptor.ENABLED) != 0) ?
					RDebugUIPlugin.IMG_OBJ_R_BREAKPOINT :
					RDebugUIPlugin.IMG_OBJ_R_BREAKPOINT_DISABLED;
		}
		final ImageDescriptor descriptor = new ECommonsDebugUIImageDescriptor(
				RDebugUIPlugin.getDefault().getImageRegistry().getDescriptor(imageKey), flags,
				SharedUIResources.INSTANCE.getIconDefaultSize() );
		return this.imageDescriptorRegistry.get(descriptor);
	}
	
	protected String getText(final IRBreakpoint breakpoint) throws CoreException {
		final StringBuilder text = new StringBuilder();
		if (breakpoint.getBreakpointType() == RDebugModel.R_EXCEPTION_BREAKPOINT_TYPE_ID) {
			return "R errors/stops";
		}
		if (breakpoint instanceof IRLineBreakpoint) {
			final IRLineBreakpoint lineBreakpoint = (IRLineBreakpoint) breakpoint;
			
			final IResource resource = breakpoint.getMarker().getResource();
			if (resource != null) {
				text.append(resource.getName());
			}
			
			try {
				final int lineNumber = lineBreakpoint.getLineNumber();
				text.append(" ["); //$NON-NLS-1$
				text.append(NLS.bind(Messages.Breakpoint_Line_label, Integer.toString(lineNumber)));
				text.append(']');
			}
			catch (final CoreException e) {}
			
			text.append(" - "); //$NON-NLS-1$
			final String subLabel = lineBreakpoint.getSubLabel();
			if (subLabel != null) {
				text.append(subLabel);
				text.append(' ');
				text.append(Messages.Breakpoint_SubLabel_copula);
				text.append(' ');
			}
			switch (lineBreakpoint.getElementType()) {
			case IRLineBreakpoint.R_COMMON_FUNCTION_ELEMENT_TYPE:
				text.append(Messages.Breakpoint_Function_prefix);
				text.append(' ');
				break;
			case IRLineBreakpoint.R_S4_METHOD_ELEMENT_TYPE:
				text.append(Messages.Breakpoint_S4Method_prefix);
				text.append(' ');
				break;
			case IRLineBreakpoint.R_TOPLEVEL_COMMAND_ELEMENT_TYPE:
				text.append(Messages.Breakpoint_ScriptLine_prefix);
				text.append(' ');
			}
			final String elementLabel = lineBreakpoint.getElementLabel();
			if (elementLabel != null) {
				text.append(elementLabel);
			}
			else {
				text.append("?"); //$NON-NLS-1$
			}
		}
		else {
			return null;
		}
		return text.toString();
	}
	
	/**
	 * Returns the adornment flags for the given breakpoint.
	 * These flags are used to render appropriate overlay icons for the breakpoint.
	 */
	private int computeBreakpointAdornmentFlags(final IRBreakpoint breakpoint)  {
		int flags = 0;
		try {
			if (breakpoint.isEnabled()) {
				flags |= ECommonsDebugUIImageDescriptor.ENABLED;
			}
			if (breakpoint.isInstalled()) {
				flags |= ECommonsDebugUIImageDescriptor.INSTALLED;
			}
			
			if (breakpoint.getBreakpointType() == RDebugModel.R_LINE_BREAKPOINT_TYPE_ID) {
				final IRLineBreakpoint lineBreakpoint = (IRLineBreakpoint) breakpoint;
				if (lineBreakpoint.getElementType() == IRLineBreakpoint.R_TOPLEVEL_COMMAND_ELEMENT_TYPE) {
					flags |= ECommonsDebugUIImageDescriptor.SCRIPT;
				}
				else if (lineBreakpoint.isConditionEnabled()) {
					flags |= ECommonsDebugUIImageDescriptor.CONDITIONAL;
				}
			}
			else if (breakpoint.getBreakpointType() == RDebugModel.R_METHOD_BREAKPOINT_TYPE_ID) {
				final IRMethodBreakpoint methodBreakpoint = (IRMethodBreakpoint) breakpoint;
				if (methodBreakpoint.isConditionEnabled()) {
					flags |= ECommonsDebugUIImageDescriptor.CONDITIONAL;
				}
				if (methodBreakpoint.isEntry()) {
					flags |= ECommonsDebugUIImageDescriptor.ENTRY;
				}
				if (methodBreakpoint.isExit()) {
					flags |= ECommonsDebugUIImageDescriptor.EXIT;
				}
			}
			else if (breakpoint.getBreakpointType() == RDebugModel.R_EXCEPTION_BREAKPOINT_TYPE_ID) {
			}
		}
		catch (final CoreException e) {
		}
		return flags;
	}
	
	
	protected String getText(final IRVariable variable) {
		final StringBuilder text= new StringBuilder();
		
		if (isShowVariableTypeNames()) {
			try {
				text.append(variable.getReferenceTypeName());
			}
			catch (final DebugException e) {
				text.append("<unknown type>"); //$NON-NLS-1$
			}
			text.append(' ');
		}
		
		text.append(variable.getName());
		
		String valueString= null;
		try {
			valueString= variable.getValue().getValueString();
		}
		catch (final DebugException e) {
			valueString= "<unknown>"; //$NON-NLS-1$
		}
		if (!valueString.isEmpty()) {
			text.append("= "); //$NON-NLS-1$
			text.append(valueString);
		}
		
		return text.toString();
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
	public void computeDetail(final IValue value, final IValueDetailListener listener) {
	}
	
}
