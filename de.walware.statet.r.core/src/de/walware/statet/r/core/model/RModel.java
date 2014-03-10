/*=============================================================================#
 # Copyright (c) 2008-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.core.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

import de.walware.ecommons.ltk.ISourceElement;
import de.walware.ecommons.ltk.IWorkspaceSourceUnit;

import de.walware.statet.r.core.IRProject;
import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.RProjects;
import de.walware.statet.r.core.rsource.ast.RAstNode;
import de.walware.statet.r.internal.core.FilteredFrame;
import de.walware.statet.r.internal.core.RProject;


/**
 * R LTK model
 */
public class RModel {
	
	
	public static final String R_TYPE_ID= "r"; //$NON-NLS-1$
	@Deprecated
	public static final String TYPE_ID= R_TYPE_ID;
	
	
	public static IRFrameInSource searchFrame(RAstNode node) {
		while (node != null) {
			final Object[] attachments= node.getAttachments();
			for (final Object attachment : attachments) {
				if (attachment instanceof IRFrameInSource) {
					return (IRFrameInSource) attachment;
				}
			}
			node= node.getRParent();
		}
		return null;
	}
	
	public static List<IRFrame> createDirectFrameList(final IRFrame frame) {
		final ArrayList<IRFrame> list= new ArrayList<>();
		int idx= 0;
		list.add(frame);
		while (idx < list.size()) {
			final List<? extends IRFrame> ps= list.get(idx++).getPotentialParents();
			for (final IRFrame p : ps) {
				if (!list.contains(p)) {
					list.add(p);
				}
			}
		}
		return list;
	}
	
	public static List<IRFrame> createProjectFrameList(IRProject project1,
			final IRSourceUnit exclude, Set<String> packages) throws CoreException {
		final ArrayList<IRFrame> list= new ArrayList<>();
		final IRModelManager manager= RCore.getRModelManager();
		if (project1 == null && exclude instanceof IWorkspaceSourceUnit) {
			project1= RProjects.getRProject(((IWorkspaceSourceUnit) exclude).getResource().getProject());
		}
		if (project1 == null) {
			return list;
		}
		if (packages == null) {
			packages= new HashSet<>();
		}
		IRFrame frame;
		
		frame= manager.getProjectFrame(project1);
		if (frame != null) {
			if (frame.getFrameType() == IRFrame.PACKAGE) {
				packages.add(frame.getElementName().getSegmentName());
			}
			list.add(new FilteredFrame(frame, exclude));
		}
		
		final List<IRProject> projects= new ArrayList<>();
		try {
			final IProject[] referencedProjects= project1.getProject().getReferencedProjects();
			for (final IProject referencedProject : referencedProjects) {
				final IRProject rProject= RProject.getRProject(referencedProject);
				if (rProject != null) {
					projects.add(rProject);
				}
			}
		} catch (final CoreException e) {}
		for (int i= 0; i < projects.size(); i++) {
			final IRProject project= projects.get(i);
			frame= manager.getProjectFrame(project);
			if (frame != null) {
				if (frame.getFrameType() == IRFrame.PACKAGE) {
					packages.add(frame.getElementName().getSegmentName());
				}
				list.add(frame);
			}
			try {
				final IProject[] referencedProjects= project.getProject().getReferencedProjects();
				for (final IProject referencedProject : referencedProjects) {
					final IRProject rProject= RProject.getRProject(referencedProject);
					if (rProject != null && !projects.contains(rProject)) {
						projects.add(rProject);
					}
				}
			} catch (final CoreException e) {}
		}
		return list;
	}
	
	public static List<ISourceElement> searchDeclaration(final RElementAccess access,
			final IRSourceUnit su) throws CoreException {
		assert (access != null);
		final List<ISourceElement> list= new ArrayList<>();
		
		final IRFrame suFrame= access.getFrame();
		final List<IRFrame> directFrames= RModel.createDirectFrameList(suFrame);
		for (final IRFrame frame : directFrames) {
			if (checkFrame(frame, access, list)) {
				return list;
			}
		}
		final List<IRFrame> projectFrames= RModel.createProjectFrameList(null, su, null);
		for (final IRFrame frame : projectFrames) {
			if (checkFrame(frame, access, list)) {
				return list;
			}
		}
		return list;
	}
	
	private static boolean checkFrame(final IRFrame frame, final RElementAccess access, final List<ISourceElement> list) {
		final List<? extends IRElement> elements= frame.getModelChildren(null);
		for (final IRElement element : elements) {
			final RElementName name= element.getElementName();
			if (name != null && name.equals(access)
					&& element instanceof ISourceElement) {
				list.add((ISourceElement) element);
			}
		}
		
		if (!list.isEmpty()) {
			final ISourceElement first= list.get(0);
			switch (first.getElementType()  & IRElement.MASK_C2) {
			case IRElement.R_S4METHOD:
			case IRElement.R_GENERAL_VARIABLE:
				return false;
			default:
				return true;
			}
		}
		return false;
	}
	
	
	private RModel() {}
	
}
