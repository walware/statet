/*******************************************************************************
 * Copyright (c) 2009-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.debug.ui.actions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.IWorkbenchPart;

import de.walware.ecommons.collections.ConstList;
import de.walware.ecommons.ltk.ast.AstSelection;
import de.walware.ecommons.ltk.ui.sourceediting.AssistInvocationContext;
import de.walware.ecommons.ltk.ui.sourceediting.IInfoHover;
import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.core.runtime.ToolStatus;
import de.walware.statet.nico.ui.NicoUITools;
import de.walware.statet.r.console.core.IRDataAdapter;
import de.walware.statet.r.core.model.RElementAccess;
import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.core.rsource.ast.RAstNode;
import de.walware.statet.r.debug.core.IRStackFrame;
import de.walware.statet.r.internal.debug.ui.RDebugUIUtils;
import de.walware.statet.r.ui.rtool.RElementInfoHoverCreator;
import de.walware.statet.r.ui.rtool.RElementInfoTask;


public class RDebugHover implements IInfoHover {
	
	
	private IInformationControlCreator fControlCreator;
	
	
	public RDebugHover() {
	}
	
	
	@Override
	public Object getHoverInfo(final AssistInvocationContext context) {
		final IWorkbenchPart part = context.getEditor().getWorkbenchPart();
		final ToolProcess process = NicoUITools.getTool(part);
		if (process == null) {
			return null;
		}
		final AstSelection selection = context.getAstSelection();
		RAstNode node = (RAstNode) selection.getCovering();
		if (node == null) {
			return null;
		}
		RElementAccess access = null;
		while (node != null && access == null) {
			if (Thread.interrupted()) {
				return null;
			}
			final Object[] attachments = node.getAttachments();
			for (int i = 0; i < attachments.length; i++) {
				if (attachments[i] instanceof RElementAccess) {
					access = (RElementAccess) attachments[i];
					final RElementName e = getElementAccessOfRegion(access, context);
					if (Thread.interrupted() || e == null) {
						return null;
					}
					final RElementInfoTask info = new RElementInfoTask(e) {
						@Override
						protected int getFramePosition(final IRDataAdapter r,
								final IProgressMonitor monitor) throws CoreException {
							if (process.getToolStatus() == ToolStatus.STARTED_SUSPENDED) {
								final IRStackFrame frame = RDebugUIUtils.getFrame(part, process);
								if (frame != null && frame.getPosition() > 0) {
									return frame.getPosition();
								}
							}
							return 0;
						}
					};
					return info.load(process, context.getSourceViewer().getTextWidget());
				}
			}
			node = node.getRParent();
		}
		return null;
	}
	
	private RElementName getElementAccessOfRegion(final RElementAccess access, final IRegion region) {
		int segmentCount = 0;
		RElementAccess current = access;
		while (current != null) {
			segmentCount++;
			final RAstNode nameNode = current.getNameNode();
			if (nameNode != null
					&& nameNode.getOffset() <= region.getOffset()
					&& nameNode.getStopOffset() >= region.getOffset()+region.getLength() ) {
				final RElementName[] segments = new RElementName[segmentCount];
				RElementAccess segment = access;
				for (int i = 0; i < segments.length; i++) {
					if (segment.getSegmentName() == null) {
						return null;
					}
					switch (segment.getType()) {
					case RElementName.MAIN_DEFAULT:
					case RElementName.SUB_NAMEDSLOT:
					case RElementName.SUB_NAMEDPART:
						segments[i] = segment;
						break;
					case RElementName.SUB_INDEXED_S:
					case RElementName.SUB_INDEXED_D:
						return null; // not yet supported
					case RElementName.MAIN_CLASS:
						if (segmentCount != 1) {
							return null;
						}
						segments[i] = segment;
						break;
					default:
//					case RElementName.MAIN_PACKAGE:
//					case RElementName.MAIN_ENV:
						return null;
					}
					segment = segment.getNextSegment();
				}
				return RElementName.concat(new ConstList<RElementName>(segments));
			}
			current = current.getNextSegment();
		}
		
		return null;
	}
	
	@Override
	public IInformationControlCreator getHoverControlCreator() {
		if (fControlCreator == null) {
			fControlCreator = new RElementInfoHoverCreator();
		}
		return fControlCreator;
	}
	
}
