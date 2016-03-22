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

package de.walware.statet.r.debug.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IErrorReportingExpression;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import de.walware.statet.r.debug.core.breakpoints.IRExceptionBreakpoint;
import de.walware.statet.r.debug.core.breakpoints.IRLineBreakpoint;
import de.walware.statet.r.debug.core.breakpoints.IRMethodBreakpoint;
import de.walware.statet.r.internal.debug.core.breakpoints.RExceptionBreakpoint;
import de.walware.statet.r.internal.debug.core.breakpoints.RLineBreakpoint;
import de.walware.statet.r.internal.debug.core.breakpoints.RMethodBreakpoint;
import de.walware.statet.r.internal.debug.core.eval.REvaluationExpression;
import de.walware.statet.r.internal.debug.core.eval.REvaluationResult;
import de.walware.statet.r.internal.debug.core.model.RDebugTarget;
import de.walware.statet.r.nico.AbstractRDbgController;


public class RDebugModel {
	
	/**
	 * Identifier of the R debug model
	 */
	public static final @NonNull String IDENTIFIER=
			"de.walware.statet.r.debug"; //$NON-NLS-1$
	
	
	public static final @NonNull String R_LINE_BREAKPOINT_TYPE_ID=
			"de.walware.statet.r.debug.breakpoints.RLineBreakpoint"; //$NON-NLS-1$
	public static final @NonNull String R_METHOD_BREAKPOINT_TYPE_ID=
			"de.walware.statet.r.debug.breakpoints.RMethodBreakpoint"; //$NON-NLS-1$
	public static final @NonNull String R_EXCEPTION_BREAKPOINT_TYPE_ID=
			"de.walware.statet.r.debug.breakpoints.RExceptionBreakpoint"; //$NON-NLS-1$
	
	private static final @NonNull List<@NonNull IRLineBreakpoint> NO_R_LINE_BREAKPOINTS=
			Collections.EMPTY_LIST;
	
	
	/**
	 * Creates a new R line breakpoint.
	 * 
	 * @param resource
	 * @param lineNumber (1-based)
	 * @param charStart
	 * @param charEnd
	 * @param elementLabel
	 * @param path 
	 * @param temporary
	 * @return the new breakpoint
	 * @throws DebugException
	 */
	public static @NonNull IRLineBreakpoint createLineBreakpoint(
			final @NonNull IFile resource,
			final int lineNumber, final int charStart, final int charEnd,
			final int elementType, final String elementId, final String elementLabel, final String subLabel,
			final boolean temporary) throws CoreException {
		return new RLineBreakpoint(resource, lineNumber, charStart, charEnd,
				elementType, elementId, elementLabel, subLabel,
				temporary );
	}
	
	/**
	 * Creates a new R method breakpoint.
	 * 
	 * @param resource
	 * @param lineNumber (1-based)
	 * @param charStart
	 * @param charEnd
	 * @param elementLabel
	 * @param temporary
	 * @return the new breakpoint
	 * @throws DebugException
	 */
	public static @NonNull IRMethodBreakpoint createMethodBreakpoint(
			final @NonNull IFile resource,
			final int lineNumber, final int charStart, final int charEnd,
			final int elementType, final String elementId, final String elementLabel, final String subLabel,
			final boolean temporary) throws CoreException {
		return new RMethodBreakpoint(resource, lineNumber, charStart, charEnd,
				elementType, elementId, elementLabel, subLabel,
				temporary );
	}
	
	public static @NonNull IRExceptionBreakpoint createExceptionBreakpoint(final String exceptionId,
			final boolean temporary) throws CoreException {
		if (exceptionId == null) {
			throw new NullPointerException("exceptionId"); //$NON-NLS-1$
		}
		return new RExceptionBreakpoint(ResourcesPlugin.getWorkspace().getRoot(), exceptionId,
				temporary );
	}
	
	/**
	 * Returns all existing R line breakpoint in the specified source file.
	 * 
	 * @param file
	 * @param enabled if breakpoint must be enabled
	 * @return list of the list
	 * @throws CoreException
	 */
	public static @NonNull List<IRLineBreakpoint> getLineBreakpoints(
			final @NonNull IFile file)
			throws CoreException {
		final IBreakpointManager manager = DebugPlugin.getDefault().getBreakpointManager();
		final IBreakpoint[] breakpoints = manager.getBreakpoints(RDebugModel.IDENTIFIER);
		List<IRLineBreakpoint> matches = null;
		for (int i = 0; i < breakpoints.length; i++) {
			if (breakpoints[i] instanceof IRLineBreakpoint) {
				final IRLineBreakpoint breakpoint = (IRLineBreakpoint) breakpoints[i];
				final IMarker marker = breakpoint.getMarker();
				if (marker != null && marker.exists()
						&& file.equals(marker.getResource()) ) {
					if (matches == null) {
						matches= new ArrayList<>(4);
					}
					matches.add(breakpoint);
				}
			}
		}
		return (matches != null) ? matches : NO_R_LINE_BREAKPOINTS;
	}
	
	/**
	 * Returns all existing R line breakpoint in the specified source line.
	 * 
	 * @param file
	 * @param lineNumber (1-based) line number
	 * @param enabled if breakpoint must be enabled
	 * @return list of the list
	 * @throws CoreException
	 */
	public static @NonNull List<@NonNull IRLineBreakpoint> getLineBreakpoints(
			final @NonNull IFile file,
			final int lineNumber)
			throws CoreException {
		final IBreakpointManager manager = DebugPlugin.getDefault().getBreakpointManager();
		final IBreakpoint[] breakpoints = manager.getBreakpoints(RDebugModel.IDENTIFIER);
		List<@NonNull IRLineBreakpoint> matches = null;
		for (int i = 0; i < breakpoints.length; i++) {
			if (breakpoints[i] instanceof IRLineBreakpoint) {
				final IRLineBreakpoint breakpoint = (IRLineBreakpoint) breakpoints[i];
				final IMarker marker = breakpoint.getMarker();
				if (marker != null && marker.exists()
						&& file.equals(marker.getResource())
						&& lineNumber == breakpoint.getLineNumber() ) {
					if (matches == null) {
						matches= new ArrayList<>(4);
					}
					matches.add(breakpoint);
				}
			}
		}
		return (matches != null) ? matches : NO_R_LINE_BREAKPOINTS;
	}
	
	public static @Nullable IRExceptionBreakpoint getExpressionBreakpoint(
			final @NonNull String expressionId)
			throws DebugException {
		final IBreakpointManager manager = DebugPlugin.getDefault().getBreakpointManager();
		final IBreakpoint[] breakpoints = manager.getBreakpoints(RDebugModel.IDENTIFIER);
		for (int i= 0; i < breakpoints.length; i++) {
			if (breakpoints[i] instanceof IRExceptionBreakpoint) {
				final IRExceptionBreakpoint breakpoint= (IRExceptionBreakpoint) breakpoints[i];
				if (breakpoint.getExceptionId().equals(expressionId)) {
					return breakpoint;
				}
			}
		}
		return null;
	}
	
	
	public static @NonNull IErrorReportingExpression createExpression(
			final @NonNull IREvaluationResult result) {
		return new REvaluationExpression((REvaluationResult) result);
	}
	
//	/**
//	 * Returns all existing R line breakpoint in the specified source line.
//	 * 
//	 * @param file
//	 * @param lineNumber (1-based)
//	 * @return list of the list
//	 * @throws CoreException
//	 */
//	public static List<IRLineBreakpoint> getRLineBreakpoints(final IFile file,
//			I) throws CoreException {
//		final IBreakpointManager manager = DebugPlugin.getDefault().getBreakpointManager();
//		final IBreakpoint[] breakpoints = manager.getBreakpoints(RDebugModel.IDENTIFIER);
//		List<IRLineBreakpoint> matches = null;
//		for (int i = 0; i < breakpoints.length; i++) {
//			if (breakpoints[i] instanceof IRLineBreakpoint) {
//				final IRLineBreakpoint breakpoint = (IRLineBreakpoint) breakpoints[i];
//				final IMarker marker = breakpoint.getMarker();
//				if (marker != null && marker.exists()
//						&& file.equals(marker.getResource())
//						&& lineNumber == breakpoint.getLineNumber() ) {
//					if (matches == null) {
//						matches = new ArrayList<IRLineBreakpoint>(4);
//					}
//					matches.add(breakpoint);
//				}
//			}
//		}
//		return (matches != null) ? matches : NO_R_LINE_BREAKPOINTS;
//	}
	
	
	/**
	 * Creates a new R debug target for a controller based on {@link AbstractRDbgController}.
	 * The debug target initializes the debug mode of the controller.
	 * 
	 * @param controller the controller
	 * @return a debug target for the controller
	 */
	public static @NonNull IRDebugTarget createRDebugTarget(
			final @NonNull AbstractRDbgController controller) {
		return new RDebugTarget(controller);
	}
	
}
