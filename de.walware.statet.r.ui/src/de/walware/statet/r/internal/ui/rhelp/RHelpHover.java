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

package de.walware.statet.r.internal.ui.rhelp;

import java.util.List;

import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;

import de.walware.ecommons.ltk.ISourceUnit;
import de.walware.ecommons.ltk.ast.AstSelection;
import de.walware.ecommons.ltk.ui.sourceediting.AssistInvocationContext;
import de.walware.ecommons.ltk.ui.sourceediting.IInfoHover;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.model.IRFrame;
import de.walware.statet.r.core.model.IRFrameInSource;
import de.walware.statet.r.core.model.IRSourceUnit;
import de.walware.statet.r.core.model.RElementAccess;
import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.core.renv.IREnv;
import de.walware.statet.r.core.rhelp.IREnvHelp;
import de.walware.statet.r.core.rhelp.IRHelpManager;
import de.walware.statet.r.core.rhelp.IRHelpPage;
import de.walware.statet.r.core.rsource.ast.FCall;
import de.walware.statet.r.core.rsource.ast.NodeType;
import de.walware.statet.r.core.rsource.ast.RAstNode;


public class RHelpHover implements IInfoHover {
	
	
	private boolean fFocus;
	
	
	public RHelpHover() {
	}
	
	public RHelpHover(final boolean focus) {
		fFocus = focus;
	}
	
	
	@Override
	public Object getHoverInfo(final AssistInvocationContext context) {
		final AstSelection selection = context.getAstSelection();
		if (!(selection.getCovering() instanceof RAstNode)) {
			return null;
		}
		final RAstNode rNode = (RAstNode) selection.getCovering();
		RElementName name = searchName(rNode, context, true);
		if (Thread.interrupted()) {
			return null;
		}
		if (fFocus && name == null) {
			RAstNode parent;
			switch (rNode.getNodeType()) {
			case SYMBOL:
			case STRING_CONST:
				parent = rNode.getRParent();
				if (parent != null && parent.getNodeType() == NodeType.F_CALL_ARG
						&& ((FCall.Arg) parent).getNameChild() == rNode) {
					name = searchNameOfFunction(parent, context);
				}
				break;
			case F_CALL:
			case F_CALL_ARGS:
			case F_CALL_ARG:
				name = searchNameOfFunction(rNode, context);
				break;
			default:
				break;
			}
			if (Thread.interrupted()) {
				return null;
			}
		}
		if (name == null) {
			return null;
		}
		
		IREnv rEnv = null;
		if (context.getSourceUnit() instanceof IRSourceUnit) {
			rEnv = ((IRSourceUnit) context.getSourceUnit()).getREnv();
		}
		if (rEnv == null) {
			rEnv = RCore.getREnvManager().getDefault();
			if (rEnv == null) {
				return null;
			}
		}
		final IRHelpManager rHelpManager = RCore.getRHelpManager();
		final IREnvHelp help = rHelpManager.getHelp(rEnv);
		if (help != null) {
			Object helpObject;
			try {
				if (name.getType() == RElementName.MAIN_PACKAGE) {
					helpObject = help.getRPackage(name.getSegmentName());
				}
				else {
					final List<IRHelpPage> topics = help.getPagesForTopic(name.getSegmentName());
					if (topics == null || topics.isEmpty()) {
						return null;
					}
					if (topics.size() == 1) {
						helpObject = topics.get(0);
					}
					else {
						final IRFrameInSource frame = RModel.searchFrame((RAstNode) selection.getCovering());
						if (frame == null) {
							return null;
						}
						helpObject = searchFrames(topics, RModel.createDirectFrameList(frame));
						final ISourceUnit su = context.getSourceUnit();
						if (helpObject == null && su instanceof IRSourceUnit) {
							helpObject = searchFrames(topics, RModel.createProjectFrameList(null, (IRSourceUnit) su, null));
						}
					}
				}
			}
			finally {
				help.unlock();
			}
			if (Thread.interrupted() || helpObject == null) {
				return null;
			}
			final String httpUrl = rHelpManager.toHttpUrl(helpObject, RHelpUIServlet.INFO_TARGET);
			if (httpUrl != null) {
				return new RHelpInfoHoverCreator.Data(context.getSourceViewer().getTextWidget(),
						helpObject, httpUrl);
			}
		}
		return null;
	}
	
	private IRHelpPage searchFrames(final List<IRHelpPage> helpPages, final List<IRFrame> frames) {
		if (frames == null) {
			return null;
		}
		for (final IRFrame frame : frames) {
			if (frame.getFrameType() == IRFrame.PACKAGE) {
				for (final IRHelpPage helpPage : helpPages) {
					if (helpPage.getPackage().getName().equals(
							frame.getElementName().getSegmentName() )) {
						return helpPage;
					}
				}
			}
		}
		return null;
	}
	
	@Override
	public IInformationControlCreator getHoverControlCreator() {
		return new RHelpInfoHoverCreator(fFocus);
	}
	
	static RElementName searchName(RAstNode rNode, final IRegion region, final boolean checkInterrupted) {
		RElementAccess access = null;
		while (rNode != null && access == null) {
			if (checkInterrupted && Thread.currentThread().isInterrupted()) {
				return null;
			}
			final Object[] attachments = rNode.getAttachments();
			for (int i = 0; i < attachments.length; i++) {
				if (attachments[i] instanceof RElementAccess) {
					access = (RElementAccess) attachments[i];
					final IRFrame frame = access.getFrame();
					if ((frame != null && frame.getFrameType() != IRFrame.FUNCTION)
							|| (access.getType() == RElementName.MAIN_PACKAGE) ) {
						final RElementName e = getElementAccessOfRegion(access, region);
						if (e != null) {
							return e;
						}
					}
				}
			}
			rNode = rNode.getRParent();
		}
		return null;
	}
	
	static RElementName searchNameOfFunction(RAstNode rNode, final IRegion region) {
		while (rNode != null) {
			if (rNode.getNodeType() == NodeType.F_CALL) {
				final FCall fcall = (FCall) rNode;
				if (fcall.getArgsOpenOffset() != Integer.MIN_VALUE
						&& fcall.getArgsOpenOffset() <= region.getOffset()) {
					final Object[] attachments = ((FCall) rNode).getAttachments();
					for (int i = 0; i < attachments.length; i++) {
						if (attachments[i] instanceof RElementAccess) {
							final RElementAccess access = (RElementAccess) attachments[i];
							final IRFrame frame = access.getFrame();
							if (access.getNode() == fcall
									&& frame != null && frame.getFrameType() != IRFrame.FUNCTION
									&& access.getNextSegment() == null) {
								switch (access.getType()) {
								case RElementName.MAIN_DEFAULT:
								case RElementName.MAIN_CLASS:
									return access;
								default:
									return null;
								}
							}
						}
					}
				}
				return null;
			}
			rNode = rNode.getRParent();
		}
		return null;
	}
	
	static RElementName getElementAccessOfRegion(final RElementAccess access, final IRegion region) {
		int segmentCount = 0;
		RElementAccess current = access;
		while (current != null) {
			segmentCount++;
			final RAstNode nameNode = current.getNameNode();
			if (nameNode != null
					&& nameNode.getOffset() <= region.getOffset()
					&& nameNode.getStopOffset() >= region.getOffset()+region.getLength() ) {
				final RElementAccess segment = access;
				if (segment.getSegmentName() == null
						|| segment.getNextSegment() != null) {
					return null;
				}
				switch (segment.getType()) {
				case RElementName.MAIN_DEFAULT:
				case RElementName.MAIN_CLASS:
				case RElementName.MAIN_PACKAGE:
					return segment;
				default:
					return null;
				}
			}
			current = current.getNextSegment();
		}
		
		return null;
	}
	
}
