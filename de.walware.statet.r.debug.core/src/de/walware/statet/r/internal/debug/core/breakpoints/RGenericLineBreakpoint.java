/*=============================================================================#
 # Copyright (c) 2010-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.debug.core.breakpoints;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IBreakpoint;

import de.walware.statet.r.debug.core.breakpoints.IRLineBreakpoint;


public abstract class RGenericLineBreakpoint extends RBreakpoint implements IRLineBreakpoint {
	
	
	public static final String ELEMENT_TYPE_MARKER_ATTR = "de.walware.statet.r.debug.markers.ElementTypeAttribute"; //$NON-NLS-1$
	public static final String ELEMENT_ID_MARKER_ATTR = "de.walware.statet.r.debug.markers.ElementIdAttribute"; //$NON-NLS-1$
	public static final String ELEMENT_LABEL_MARKER_ATTR = "de.walware.statet.r.debug.markers.ElementLabelAttribute"; //$NON-NLS-1$
	public static final String SUB_LABEL_MARKER_ATTR = "de.walware.statet.r.debug.markers.SubLabelAttribute"; //$NON-NLS-1$
//	public static final String REXPR_INDEX_MARKER_ATTR = "de.walware.statet.r.debug.markers.RExprIndexAttribute"; //$NON-NLS-1$
	
	public static final String CONDITION_ENABLED_MARKER_ATTR = "de.walware.statet.r.debug.markers.ConditionEnabledAttribute"; //$NON-NLS-1$
	public static final String CONDITION_EXPR_MARKER_ATTR = "de.walware.statet.r.debug.markers.ConditionExprAttribute"; //$NON-NLS-1$
	
	
	public static class CachedData {
		
		private final long fStamp;
		
		private final String fElementId;
		
		private final int[] fRExpressionIndex;
		
		public CachedData(final long stamp, final String elementId, final int[] rExpressionIndex) {
			fStamp = stamp;
			fElementId = elementId;
			fRExpressionIndex = rExpressionIndex;
		}
		
		
		public long getStamp() {
			return fStamp;
		}
		
		public String getElementId() {
			return fElementId;
		}
		
		public int[] getRExpressionIndex() {
			return fRExpressionIndex;
		}
		
	}
	
	
	private static final String[] POSITION_ATTRIBUTES = new String[] {
			IMarker.LINE_NUMBER, IMarker.CHAR_START, IMarker.CHAR_END };
	private static final String[] ELEMENT_ATTRIBUTES = new String[] {
			ELEMENT_TYPE_MARKER_ATTR, ELEMENT_ID_MARKER_ATTR, ELEMENT_LABEL_MARKER_ATTR };
	
	public static void updatePosition(final IMarker marker,
			final int lineNumber, final int charStart, final int charEnd)
			throws CoreException {
		if (lineNumber != marker.getAttribute(IMarker.LINE_NUMBER, -1)
				|| charStart != marker.getAttribute(IMarker.CHAR_START, -1)
				|| charEnd != marker.getAttribute(IMarker.CHAR_END, -1) ) {
			marker.setAttributes(POSITION_ATTRIBUTES, new Object[] {
					Integer.valueOf(lineNumber), Integer.valueOf(charStart), Integer.valueOf(charEnd)
			});
		}
	}
	
	public static void updateElementInfo(final IMarker marker,
			final int elementType, final String elementId,
			final String elementLabel, final String subLabel)
			throws CoreException {
		if (elementType != marker.getAttribute(ELEMENT_TYPE_MARKER_ATTR, -1)
				|| ((elementId != null) ? !elementId.equals(marker.getAttribute(ELEMENT_ID_MARKER_ATTR, null)) :
							null != marker.getAttribute(ELEMENT_ID_MARKER_ATTR, null))
				|| ((elementLabel != null) ? !elementLabel.equals(marker.getAttribute(ELEMENT_LABEL_MARKER_ATTR, null)) :
							null != marker.getAttribute(ELEMENT_LABEL_MARKER_ATTR, null)) ) {
			marker.setAttributes(ELEMENT_ATTRIBUTES, new Object[] {
					Integer.valueOf(elementType), elementId, elementLabel
			});
		}
		if ((subLabel != null) ? !subLabel.equals(marker.getAttribute(SUB_LABEL_MARKER_ATTR, null)) :
				null != marker.getAttribute(SUB_LABEL_MARKER_ATTR, null) ) {
			marker.setAttribute(SUB_LABEL_MARKER_ATTR, subLabel);
		}
//		if (rExpressionIndex != null) {
//			final String value = encodeIntArray(rExpressionIndex);
//			if (!value.equals(marker.getAttribute(REXPR_INDEX_MARKER_ATTR, null)) ) {
//				marker.setAttribute(REXPR_INDEX_MARKER_ATTR, value);
//			}
//		}
//		else if (marker.getAttribute(REXPR_INDEX_MARKER_ATTR, null) != null) {
//			marker.setAttribute(REXPR_INDEX_MARKER_ATTR, null);
//		}
	}
	
	
//	private static String encodeIntArray(int[] path) {
//		final StringBuilder sb = new StringBuilder(path.length*3);
//		sb.append(path.length);
//		sb.append('[');
//		if (path.length > 0) {
//			sb.append(path[0]);
//			for (int i = 1; i < path.length; i++) {
//				sb.append(',');
//				sb.append(path[i]);
//			}
//		}
//		return sb.toString();
//	}
//	
//	private static int[] parseIntArray(final String s) {
//		int idx1;
//		int idx2;
//		idx1 = s.indexOf('[');
//		final int[] index = new int[Integer.parseInt(s.substring(0, idx1))];
//		final int last = index.length-1;
//		if (last >= 0) {
//			idx1++;
//			for (int i = 0; i < last; i++) {
//				idx2 = s.indexOf(',', idx1);
//				index[i] = Integer.parseInt(s.substring(idx1, idx2));
//				idx1 = idx2+1;
//			}
//			index[last] = Integer.parseInt(s.substring(idx1, s.length()));
//		}
//		return index;
//	}
	
	
	private final AtomicReference<CachedData> fCachedData = new AtomicReference<RGenericLineBreakpoint.CachedData>();
	
	
	protected RGenericLineBreakpoint() {
	}
	
	
	/**
	 * Adds the standard attributes of a line breakpoint to the given attribute map.
	 * The standard attributes are:
	 * <ol>
	 * <li>IBreakpoint.ID</li>
	 * <li>IBreakpoint.ENABLED</li>
	 * <li>IMarker.LINE_NUMBER</li>
	 * <li>IMarker.CHAR_START</li>
	 * <li>IMarker.CHAR_END</li>
	 * <li>ELEMENT_LABEL_MARKER_ATTR</li>
	 * </ol>
	 */
	protected void addStandardLineBreakpointAttributes(final Map<String, Object> attributes,
			final boolean enabled,
			final int lineNumber, final int charStart, final int charEnd,
			final int elementType, final String elementId, final String elementLabel, final String subLabel) {
		attributes.put(IBreakpoint.ID, getModelIdentifier());
		attributes.put(IBreakpoint.ENABLED, Boolean.valueOf(enabled));
		attributes.put(IMarker.LINE_NUMBER, new Integer(lineNumber));
		attributes.put(IMarker.CHAR_START, new Integer(charStart));
		attributes.put(IMarker.CHAR_END, new Integer(charEnd));
		attributes.put(ELEMENT_TYPE_MARKER_ATTR, new Integer(elementType));
		if (elementId != null) {
			attributes.put(ELEMENT_ID_MARKER_ATTR, elementId);
		}
		if (elementLabel != null) {
			attributes.put(ELEMENT_LABEL_MARKER_ATTR, elementLabel);
		}
		if (subLabel != null) {
			attributes.put(SUB_LABEL_MARKER_ATTR, elementLabel);
		}
//		if (rExpressionIndex != null) {
//			attributes.put(REXPR_INDEX_MARKER_ATTR, encodeIntArray(rExpressionIndex));
//		}
	}
	
	
	@Override
	public int getLineNumber() throws CoreException {
		return ensureMarker().getAttribute(IMarker.LINE_NUMBER, -1);
	}
	
	@Override
	public int getCharStart() throws CoreException {
		return ensureMarker().getAttribute(IMarker.CHAR_START, -1);
	}
	
	@Override
	public int getCharEnd() throws CoreException {
		return ensureMarker().getAttribute(IMarker.CHAR_END, -1);
	}
	
	@Override
	public int getElementType() throws CoreException {
		return ensureMarker().getAttribute(ELEMENT_TYPE_MARKER_ATTR, -1);
	}
	
	public String getElementId() throws CoreException {
		return ensureMarker().getAttribute(ELEMENT_ID_MARKER_ATTR, null);
	}
	
	@Override
	public String getElementLabel() throws CoreException {
		return ensureMarker().getAttribute(ELEMENT_LABEL_MARKER_ATTR, null);
	}
	
	@Override
	public String getSubLabel() throws CoreException {
		return ensureMarker().getAttribute(SUB_LABEL_MARKER_ATTR, null);
	}
	
	
	@Override
	public void setConditionEnabled(final boolean enabled) throws CoreException {
		ensureMarker().setAttribute(CONDITION_ENABLED_MARKER_ATTR, enabled);
	}
	
	@Override
	public boolean isConditionEnabled() throws CoreException {
		return ensureMarker().getAttribute(CONDITION_ENABLED_MARKER_ATTR, false);
	}
	
	@Override
	public void setConditionExpr(final String code) throws CoreException {
		final IMarker marker = ensureMarker();
		if (code != null && code.trim().length() > 0) {
			marker.setAttribute(CONDITION_EXPR_MARKER_ATTR, code);
		}
		else {
			if (marker.getAttribute(CONDITION_EXPR_MARKER_ATTR, (String) null) != null) {
				marker.setAttribute(CONDITION_ENABLED_MARKER_ATTR, false);
			}
			ensureMarker().setAttribute(CONDITION_EXPR_MARKER_ATTR, (String) null);
		}
	}
	
	@Override
	public String getConditionExpr() throws CoreException {
		return ensureMarker().getAttribute(CONDITION_EXPR_MARKER_ATTR, ""); //$NON-NLS-1$
	}
	
	
	public void setCachedData(final CachedData data) {
		if (checkTimestamp(data)) {
			fCachedData.set(data);
		}
	}
	
	public CachedData getCachedData() {
		final CachedData cachedData = fCachedData.get();
		if (cachedData != null && !checkTimestamp(cachedData)) {
			fCachedData.compareAndSet(cachedData, null);
		}
		return cachedData;
	}
	
	private boolean checkTimestamp(final CachedData data) {
		final IMarker marker = getMarker();
		if (marker != null) {
			final IResource resource = marker.getResource();
			if (resource != null) {
				return (data.getStamp() == resource.getLocalTimeStamp());
			}
		}
		return false;
	}
	
}
