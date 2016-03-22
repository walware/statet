/*=============================================================================#
 # Copyright (c) 2008-2016 Stephan Wahlbrink (WalWare.de) and others.
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

import de.walware.jcommons.collections.ImCollections;

import de.walware.ecommons.ltk.core.model.ISourceElement;
import de.walware.ecommons.ltk.core.model.ISourceUnitModelInfo;
import de.walware.ecommons.ltk.core.model.IWorkspaceSourceUnit;

import de.walware.statet.r.core.IRProject;
import de.walware.statet.r.core.RProjects;
import de.walware.statet.r.core.rsource.ast.RAstNode;
import de.walware.statet.r.internal.core.FilteredFrame;
import de.walware.statet.r.internal.core.RCorePlugin;
import de.walware.statet.r.internal.core.RProject;
import de.walware.statet.r.internal.core.sourcemodel.RModelManager;


/**
 * R LTK model
 */
public final class RModel {
	
	
	public static final String R_TYPE_ID= "R"; //$NON-NLS-1$
	@Deprecated
	public static final String TYPE_ID= R_TYPE_ID;
	
	
	public static final RElementName GLOBAL_ENV_NAME= RElementName.create(RElementName.SCOPE_SEARCH_ENV, ".GlobalEnv");
	
	
	/**
	 * @return the manager for the R model
	 */
	public static IRModelManager getRModelManager() {
		return RCorePlugin.getDefault().getRModelManager();
	}
	
	public static IRModelInfo getRModelInfo(final ISourceUnitModelInfo modelInfo) {
		if (modelInfo != null) {
			if (modelInfo instanceof IRModelInfo) {
				return (IRModelInfo) modelInfo;
			}
			for (final Object aAttachment : modelInfo.getAttachments()) {
				if (aAttachment instanceof IRModelInfo) {
					return (IRModelInfo) aAttachment;
				}
			}
		}
		return null;
	}
	
	
	public static IRFrameInSource searchFrame(RAstNode node) {
		while (node != null) {
			final List<Object> attachments= node.getAttachments();
			for (final Object attachment : attachments) {
				if (attachment instanceof IRFrameInSource) {
					return (IRFrameInSource) attachment;
				}
			}
			node= node.getRParent();
		}
		return null;
	}
	
	
	private static boolean isValidPkgFrame(final IRFrame frame) {
		return (frame.getFrameType() == IRFrame.PACKAGE
				&& frame.getElementName().getSegmentName() != null );
	}
	
	private static boolean isValidFrame(final IRFrame frame, final String pkgName) {
		return (pkgName == null
				|| (isValidPkgFrame(frame)
						&& frame.getElementName().getSegmentName().equals(pkgName) ));
	}
	
	private static boolean isValidFrame(final IRFrame frame, final Set<String> pkgNames) {
		return (pkgNames == null
				|| (isValidPkgFrame(frame)
						&& pkgNames.contains(frame.getElementName().getSegmentName()) ));
	}
	
	public static List<IRFrame> createDirectFrameList(final IRFrame frame,
			final RElementName expliciteScope) {
		final ArrayList<IRFrame> list= new ArrayList<>();
		final String pkgName= (expliciteScope != null && RElementName.isPackageFacetScopeType(expliciteScope.getType())) ?
				expliciteScope.getSegmentName() : null;
		int idx= 0;
		if (isValidFrame(frame, pkgName)) {
			list.add(frame);
		}
		while (idx < list.size()) {
			final List<? extends IRFrame> ps= list.get(idx++).getPotentialParents();
			for (final IRFrame parent : ps) {
				if (isValidFrame(parent, pkgName) && !list.contains(parent)) {
					list.add(parent);
				}
			}
		}
		return list;
	}
	
	public static List<IRFrame> createDirectFrameList(final IRFrame frame) {
		return createDirectFrameList(frame, null);
	}
	
	public static Set<String> createImportedPackageList(final IRModelInfo modelInfo) {
		final Set<String> importedPackages= new HashSet<>();
		importedPackages.add("base"); //$NON-NLS-1$
		
		if (modelInfo != null) {
			final IPackageReferences packages= modelInfo.getReferencedPackages();
			for (final String name : packages.getAllPackageNames()) {
				if (packages.isImported(name)) {
					importedPackages.add(name);
				}
			}
		}
		
		return importedPackages;
	}
	
	public static List<IRFrame> createProjectFrameList(IRProject project1,
			final IRSourceUnit scope, 
			final boolean pkgImports, final boolean projectDependencies,
			Set<String> importedPackages, Set<String> pkgNames)
			throws CoreException {
		final ArrayList<IRFrame> list= new ArrayList<>();
		final IRModelManager manager= getRModelManager();
		if (project1 == null && scope instanceof IWorkspaceSourceUnit) {
			if (pkgImports && importedPackages == null) {
				importedPackages= createImportedPackageList(
						(IRModelInfo) scope.getModelInfo(R_TYPE_ID, RModelManager.MODEL_FILE, null ));
			}
			project1= RProjects.getRProject(((IWorkspaceSourceUnit) scope).getResource().getProject());
		}
		if (pkgImports && importedPackages == null) {
			importedPackages= ImCollections.emptySet();
		}
		
		if (pkgNames == null) {
			pkgNames= new HashSet<>();
		}
		
		if (project1 != null) {
			{	final IRFrame frame= manager.getProjectFrame(project1);
				if (frame != null) {
					if (projectDependencies || (pkgImports && isValidFrame(frame, importedPackages))) {
						if (isValidPkgFrame(frame)) {
							pkgNames.add(frame.getElementName().getSegmentName());
						}
						list.add(new FilteredFrame(frame, scope));
					}
				}
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
				final IRFrame frame= manager.getProjectFrame(project);
				if (frame != null) {
					if (projectDependencies || (pkgImports && isValidFrame(frame, importedPackages))) {
						if (isValidPkgFrame(frame)) {
							pkgNames.add(frame.getElementName().getSegmentName());
						}
						list.add(frame);
					}
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
		}
		
		if (pkgImports && importedPackages != null) {
			for (final String pkgName : importedPackages) {
				if (!pkgNames.contains(pkgName)) {
					final IRFrame frame= manager.getPkgProjectFrame(pkgName);
					if (frame != null) {
						list.add(frame);
					}
				}
			}
		}
		
		return list;
	}
	
	public static List<IRFrame> createProjectFrameList(final IRProject project1,
			final IRSourceUnit scope) throws CoreException {
		return createProjectFrameList(project1, scope, true, true, null, null);
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
		final List<IRFrame> projectFrames= RModel.createProjectFrameList(null, su);
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
	
	
	public static RElementName getFQElementName(final IRElement var) {
		final List<RElementName> segments= getFQFullName(var, 0);
		return (segments != null) ? RElementName.create(segments) : null;
	}
	
	private static List<RElementName> getFQFullName(final IRElement var, int count) {
		if (var != null) {
			final RElementName elementName= var.getElementName();
			if (elementName != null) {
				{	RElementName segment= elementName;
					do {
						count++;
						segment= segment.getNextSegment();
					} while (segment != null);
				}
				List<RElementName> segments;
				final RElementName scope= elementName.getScope();
				if (scope != null) {
					if (RElementName.isScopeType(scope.getType())) {
						segments= new ArrayList<>(count + 1);
						segments.add(scope);
					}
					else {
						segments= getFQFullName(var.getModelParent(), count);
					}
				}
				else {
					if (RElementName.isScopeType(elementName.getType())) {
						segments= new ArrayList<>(count);
					}
					else {
						segments= getFQFullName(var.getModelParent(), count);
					}
				}
				if (segments != null) {
					RElementName segment= elementName;
					do {
						segments.add(segment);
						segment= segment.getNextSegment();
					} while (segment != null);
					return segments;
				}
			}
		}
		return null;
	}
	
	
	private RModel() {}
	
}
