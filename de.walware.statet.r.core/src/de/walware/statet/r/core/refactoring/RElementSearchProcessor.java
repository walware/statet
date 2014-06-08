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

package de.walware.statet.r.core.refactoring;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.osgi.util.NLS;

import de.walware.ecommons.collections.ConstArrayList;
import de.walware.ecommons.ltk.IModelManager;
import de.walware.ecommons.ltk.ISourceUnitManager;
import de.walware.ecommons.ltk.LTK;
import de.walware.ecommons.ltk.core.model.ISourceUnit;

import de.walware.statet.r.core.IRProject;
import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.RProjects;
import de.walware.statet.r.core.model.IRFrame;
import de.walware.statet.r.core.model.IRFrameInSource;
import de.walware.statet.r.core.model.IRModelInfo;
import de.walware.statet.r.core.model.IRSourceUnit;
import de.walware.statet.r.core.model.RElementAccess;
import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.internal.core.refactoring.Messages;


public class RElementSearchProcessor {
	
	public static final int WARN_NO_DEFINITION=             0x10000;
	public static final int WARN_MULTIPLE_DEFINITION=       0x20000;
	
	public static final int ALLOW_SUB_NAMEDPART=            0x01000;
	
	
	public static enum Mode {
		
		WORKSPACE (Messages.SearchScope_Workspace_label),
		CURRENT_AND_REFERENCING_PROJECTS (Messages.SearchScope_CurrentAndReferencingProjects_label),
		CURRENT_PROJECT (Messages.SearchScope_CurrentProject_label),
		CURRENT_FILE (Messages.SearchScope_CurrentFile_label),
		LOCAL_FRAME (Messages.SearchScope_LocalFrame_label);
		
		
		private final String label;
		
		
		private Mode(final String label) {
			this.label= label;
		}
		
		
		public String getLabel() {
			return this.label;
		}
		
	};
	
	
	private static List<Mode> MODES_LOCAL= new ConstArrayList<>(
			Mode.LOCAL_FRAME);
	
	private static List<Mode> MODES_GLOBAL= new ConstArrayList<>(
			Mode.WORKSPACE,
			Mode.CURRENT_AND_REFERENCING_PROJECTS,
			Mode.CURRENT_PROJECT,
			Mode.CURRENT_FILE);
	
	
	private final IRSourceUnit initialSourceUnit;
	
	private List<Mode> availableModes;
	private Mode mode;
	
	protected final RElementName name;
	protected final RElementName mainName;
	protected final RElementName namespace;
	protected final String namespaceFrameId;
	
	private final int flags;
	
	private final List<IRProject> allProjects= new ArrayList<>();
	private final List<List<ISourceUnit>> allProjectsSourceUnits= new ArrayList<>();
	protected final List<IRProject> definitionProjects= new ArrayList<>();
	protected final Set<String> definitionFrameIds= new HashSet<>();
	protected final List<IRProject> matchProjects= new ArrayList<>();
	
	private IStatus status;
	
	
	/**
	 * Creates a search processor initialized by the specified element access
	 * 
	 * @param sourceUnit the source unit of the element access
	 * @param name the name of the element to search
	 * @param mainAccess the access element for the element to search (must match the name)
	 * @param mode
	 * @param flags
	 */
	public RElementSearchProcessor(final RElementName name,
			final IRSourceUnit sourceUnit, final RElementAccess mainAccess,
			final Mode mode, final int flags) {
		this.initialSourceUnit= sourceUnit;
		this.mode= mode;
		this.flags= flags;
		
		this.name= name;
		this.status= Status.OK_STATUS;
		
		validateName();
		
		if (this.status.getSeverity() < IStatus.ERROR) { 
			init(mainAccess);
		}
		
		if (this.status.getSeverity() < IStatus.ERROR) {
			this.mainName= RElementName.cloneSegment(mainAccess);
			this.namespace= (mainAccess.getNamespace() != null) ?
					RElementName.cloneSegment(mainAccess.getNamespace()) :
					null;
			this.namespaceFrameId= (this.namespace != null && this.namespace.getType() == RElementName.MAIN_PACKAGE) ?
					this.namespace.getDisplayName() :
					null;
		}
		else {
			this.mainName= null;
			this.namespace= null;
			this.namespaceFrameId= null;
		}
	}
	
	protected void validateName() {
		if (this.name == null) {
			addStatus(IStatus.ERROR, "The operation is unavailable on the current selection.");
			return;
		}
		
		RElementName nameSegment= this.name;
		ITER_SEGMENTS: do {
			if (nameSegment.getSegmentName() == null) {
				addStatus(IStatus.ERROR, "The operation is unavailable on the current selection (invalid name).");
				break ITER_SEGMENTS;
			}
			nameSegment= nameSegment.getNextSegment();
		} while (nameSegment != null);
		
		nameSegment= this.name.getNextSegment();
		ITER_SEGMENTS: while (nameSegment != null) {
			switch (nameSegment.getType()) {
			case RElementName.SUB_NAMEDPART:
				if ((this.flags & ALLOW_SUB_NAMEDPART) == 0) {
					addStatus(IStatus.ERROR, "The operation is unavailable on the current selection (sub element).");
					break ITER_SEGMENTS;
				}
				break;
			default:
				addStatus(IStatus.ERROR, "The operation is unavailable on the current selection (unsupported sub element).");
				break ITER_SEGMENTS;
			}
			nameSegment= nameSegment.getNextSegment();
		}
	}
	
	protected void init(final RElementAccess access) {
		if (access == null) {
			throw new NullPointerException("elementAccess"); //$NON-NLS-1$
		}
		{	RElementName subName= this.name;
			RElementAccess subAccess= access;
			do {
				if (subAccess == null
						|| !subName.getSegmentName().equals(subAccess.getSegmentName())) {
					throw new IllegalArgumentException("elementAccess does not match to elementName"); //$NON-NLS-1$
				}
				subName= subName.getNextSegment();
				subAccess= subAccess.getNextSegment();
			} while (subName != null);
		}
		if (access.getType() != RElementName.MAIN_DEFAULT) {
			addStatus(IStatus.ERROR, "The operation is unavailable on the current selection.");
			return;
		}
		if (access.getSegmentName() == null || access.getSegmentName().isEmpty()) {
			addStatus(IStatus.ERROR, "The operation is unavailable on the current selection.");
			return;
		}
		
		this.availableModes= getAvailableModes(access);
		if (this.mode != null) {
			if (!this.availableModes.contains(this.mode)) {
				this.mode= this.availableModes.get(0);
				this.status= new Status(IStatus.WARNING, RCore.PLUGIN_ID,
						NLS.bind("Scope changed to: '{0}'.", this.mode) );
			}
		}
		else {
			this.mode= this.availableModes.get(0);
		}
	}
	
	public IRSourceUnit getInitialSourceUnit() {
		return this.initialSourceUnit;
	}
	
	public IStatus getStatus() {
		return this.status;
	}
	
	protected List<Mode> getAvailableModes(final RElementAccess access) {
		final IRFrame frame= access.getFrame();
		if (frame == null || (frame.getFrameType() != IRFrame.PACKAGE && frame.getFrameType() != IRFrame.PROJECT)) {
			return MODES_LOCAL;
		}
		else {
			return MODES_GLOBAL;
		}
	}
	
	public List<Mode> getAvailableModes() {
		return this.availableModes;
	}
	
	public void setMode(final Mode mode) {
		if (!getAvailableModes().contains(mode)) {
			throw new IllegalArgumentException("mode"); //$NON-NLS-1$
		}
		this.mode= mode;
	}
	
	public Mode getMode() {
		return this.mode;
	}
	
	public String getModeLabel() {
		switch (this.mode) {
		case WORKSPACE:
			return "workspace";
		case CURRENT_AND_REFERENCING_PROJECTS:
			return NLS.bind("project ''{0}'' and referencing projects", getInitialProjectName());
		case CURRENT_PROJECT:
			return NLS.bind("project ''{0}''", getInitialProjectName());
		case CURRENT_FILE:
			return NLS.bind("file ''{0}''", this.initialSourceUnit.getElementName().getDisplayName());
		case LOCAL_FRAME:
			return "local frame";
		default:
			return ""; //$NON-NLS-1$
		}
	}
	
	private IProject getInitialProject() {
		final Object resource= this.initialSourceUnit.getResource();
		return (resource instanceof IResource) ?
				((IResource) resource).getProject() :
				null;
	}
	
	private String getInitialProjectName() {
		final IProject project= getInitialProject();
		return (project != null) ? project.getName() : "-"; //$NON-NLS-1$
	}
	
	private IRProject getInitialRProject() {
		final IProject project= getInitialProject();
		return (project != null) ? RProjects.getRProject(project) : null;
	}
	
	
	protected void addStatus(final int severity, final String message) {
		this.status= new Status(severity, RCore.PLUGIN_ID, message);
	}
	
	public RElementName getElementName() {
		return this.name;
	}
	
	
	protected void clear() {
		this.allProjects.clear();
		this.allProjectsSourceUnits.clear();
		this.definitionProjects.clear();
		this.definitionFrameIds.clear();
		this.matchProjects.clear();
	}
	
	public void run(final SubMonitor progress) throws CoreException {
		if (this.status.getSeverity() >= IStatus.ERROR) {
			throw new IllegalStateException();
		}
		
		progress.beginTask(getTaskName(), 100);
		clear();
		begin(progress);
		
		try {
			this.definitionFrameIds.add(null);
			if (this.namespaceFrameId != null) {
				// search for specified package
				this.definitionFrameIds.add(this.namespaceFrameId);
			}
			
			{	// start with current project
				final IRProject initialProject= getInitialRProject();
				if (initialProject != null) {
					this.allProjects.add(initialProject);
					final List<ISourceUnit> sus= loadSus(initialProject, this.allProjectsSourceUnits, true,
							progress.newChild(3) );
					sus.add(this.initialSourceUnit);
				}
				else {
					this.allProjects.add(null);
					this.initialSourceUnit.connect(progress.newChild(1));
					this.allProjectsSourceUnits.add(Collections.<ISourceUnit>singletonList(this.initialSourceUnit));
				}
			}
			
			progress.worked(5);
			if (progress.isCanceled()) {
				throw new CoreException(Status.CANCEL_STATUS);
			}
			
			{	// referenced projects
				final SubMonitor p= progress.newChild(40);
				for (int i= 0; i < this.allProjects.size(); i++) {
					p.setWorkRemaining(this.allProjects.size()-i);
					
					final IRProject project= this.allProjects.get(i);
					if (project == null) {
						continue;
					}
					
					if (progress.isCanceled()) {
						throw new CoreException(Status.CANCEL_STATUS);
					}
					
					List<ISourceUnit> sus;
					if (i < this.allProjectsSourceUnits.size()) {
						sus= this.allProjectsSourceUnits.get(i);
					}
					else {
						sus= loadSus(project, this.allProjectsSourceUnits, false, progress.newChild(3));
					}
					if (sus != null) {
						final int found= searchDefinition(project, sus, progress.newChild(5));
						if (found > 0) {
							this.definitionProjects.add(project);
							if (this.namespaceFrameId == null) {
								final String packageName= project.getPackageName();
								if (packageName != null) {
									this.definitionFrameIds.add("package:"+packageName); //$NON-NLS-1$
								}
							}
							if (found == 2) { // || specificPackage == null 
								continue;
							}
						}
						if (this.mode == Mode.WORKSPACE) {
							addReferencedProjects(project, this.allProjects);
						}
					}
				}
			}
			
			if (this.definitionProjects.isEmpty()) {
				if ((this.flags & WARN_NO_DEFINITION) != 0 && this.mode == Mode.WORKSPACE) {
					addStatus(IStatus.WARNING, Messages.RenameInWorkspace_warning_NoDefinition_message);
				}
			}
			else if (this.definitionProjects.size() > 1) {
				if ((this.flags & WARN_NO_DEFINITION) != 0 && this.mode == Mode.WORKSPACE) {
					addStatus(IStatus.WARNING, Messages.RenameInWorkspace_warning_MultipleDefinitions_message);
				}
			}
			
			// definitions?
			if (!this.definitionProjects.isEmpty()) {
				this.matchProjects.addAll(this.definitionProjects);
			}
			else {
				for (int i= 0; i < this.allProjects.size(); i++) {
					final IRProject project;
					if (this.allProjectsSourceUnits.get(i) != null
							&& (project= this.allProjects.get(i)) != null) {
						this.matchProjects.add(project);
					}
				}
			}
			progress.worked(5);
			if (progress.isCanceled()) {
				throw new CoreException(Status.CANCEL_STATUS);
			}
			
			beginFinalProcessing(progress);
			
			{	// referencing occurrences - create text changes
				final SubMonitor p= progress.newChild(40);
				for (int i= 0; i < this.matchProjects.size(); i++) {
					p.setWorkRemaining((this.matchProjects.size()-i) * 10);
					
					final IRProject project= this.matchProjects.get(i);
					int idx= this.allProjects.indexOf(project);
					
					if (progress.isCanceled()) {
						throw new CoreException(Status.CANCEL_STATUS);
					}
					
					List<ISourceUnit> sus;
					if (idx >= 0) {
						sus= this.allProjectsSourceUnits.get(idx);
					}
					else {
						this.allProjects.add(project);
						idx= this.allProjectsSourceUnits.size();
						sus= loadSus(project, this.allProjectsSourceUnits, false, p.newChild(3));
					}
					process(project, sus, progress.newChild(5));
					switch (this.mode) {
					case WORKSPACE:
					case CURRENT_AND_REFERENCING_PROJECTS:
						addReferencingProjects(project, this.matchProjects);
						break;
					default:
						break;
					}
					
					if (sus != null) {
						this.allProjectsSourceUnits.set(idx, null);
						closeSus(sus, p.newChild(1));
					}
				}
			}
		}
		catch (final BadLocationException e) {
			throw new CoreException(new Status(IStatus.ERROR, RCore.PLUGIN_ID,
					"Unexpected error (concurrent change?)", e ));
		}
		finally {
			for (final List<ISourceUnit> sus : this.allProjectsSourceUnits) {
				if (sus != null) {
					closeSus(sus, progress.newChild(1));
				}
			}
		}
	}
	
	private void closeSus(final List<ISourceUnit> sus, final SubMonitor progress) {
		progress.setWorkRemaining(sus.size());
		for (final ISourceUnit su : sus) {
			try {
				su.disconnect(progress.newChild(1));
			}
			catch (final Exception e) {}
		}
	}
	
	private void addReferencedProjects(final IRProject initialProject, final List<IRProject> projects)
			throws CoreException {
		final IProject[] referencedProjects= initialProject.getProject().getReferencedProjects();
		for (final IProject referencedProject : referencedProjects) {
			if (referencedProject.isOpen()) {
				final IRProject project= RProjects.getRProject(referencedProject);
				if (project != null && !projects.contains(project)) {
					projects.add(project);
				}
			}
		}
	}
	
	private void addReferencingProjects(final IRProject initialProject, final List<IRProject> projects)
			throws CoreException {
		final IProject[] referencedProjects= initialProject.getProject().getReferencingProjects();
		for (final IProject referencedProject : referencedProjects) {
			if (referencedProject.isOpen()) {
				final IRProject project= RProjects.getRProject(referencedProject);
				if (project != null && !projects.contains(project)) {
					projects.add(project);
				}
			}
		}
	}
	
	private List<ISourceUnit> loadSus(final IRProject project, final List<List<ISourceUnit>> projectsSus,
			final boolean force, final SubMonitor progress) throws CoreException {
		final ISourceUnitManager suManager= LTK.getSourceUnitManager(); 
		List<ISourceUnit> sourceUnits= RCore.getRModelManager().findReferencingSourceUnits(
				project, this.mainName, progress.newChild(10) );
		if (sourceUnits == null && force) {
			sourceUnits= new ArrayList<>(1);
		}
		if (sourceUnits != null) {
			projectsSus.add(sourceUnits);
			
			int remaining= sourceUnits.size();
			for (int i= 0; i < sourceUnits.size(); i++) {
				progress.setWorkRemaining(3*(remaining--));
				
				final ISourceUnit sourceUnit= sourceUnits.get(i);
				if (!sourceUnit.equals(this.initialSourceUnit.getId())) {
					try {
						final ISourceUnit editUnit= suManager.getSourceUnit(sourceUnit.getModelTypeId(),
								LTK.EDITOR_CONTEXT, sourceUnit, true, progress.newChild(1) );
						if (editUnit != null) {
							sourceUnits.set(i, editUnit);
						}
					}
					catch (final Throwable e) {
						throw new CoreException(new Status(IStatus.ERROR, RCore.PLUGIN_ID, 1,
								NLS.bind("An error occurred when looking for ''{0}''.", sourceUnit), e ));
					}
				}
			}
			
			return sourceUnits;
		}
		else {
			projectsSus.add(null);
			return null;
		}
	}
	
	private int searchDefinition(final IRProject project, final List<ISourceUnit> sus,
			final SubMonitor progress) {
		final String packageName= project.getPackageName();
		if (this.namespaceFrameId != null && packageName != null
				&& this.namespaceFrameId.equals("package:"+packageName)) { //$NON-NLS-1$
			progress.setWorkRemaining(2);
			for (final ISourceUnit su : sus) {
				searchDefinition(su, null, progress.newChild(1));
				searchDefinition(su, this.namespaceFrameId, progress.newChild(1));
			}
			return 2;
		}
		else {
			progress.setWorkRemaining(1);
			boolean found= false;
			for (final ISourceUnit su : sus) {
				found |= searchDefinition(su, this.namespaceFrameId, progress.newChild(1));
			}
			return (found) ? 1 : 0;
		}
	}
	
	private boolean searchDefinition(final ISourceUnit su, final String specificPackage,
			final SubMonitor progress) {
		progress.setWorkRemaining(10);
		su.connect(progress.newChild(1));
		try {
			final IRModelInfo modelInfo= (IRModelInfo) su.getModelInfo(RModel.TYPE_ID,
					IModelManager.MODEL_FILE, progress.newChild(3) );
			if (modelInfo == null) {
				return false;
			}
			final IRFrame frame;
			if (specificPackage == null) {
				frame= modelInfo.getTopFrame();
			}
			else {
				frame= modelInfo.getReferencedFrames().get(specificPackage);
			}
			if (frame instanceof IRFrameInSource) {
				final List<? extends RElementAccess> allAccess= ((IRFrameInSource) frame).getAllAccessOf(
						this.mainName.getSegmentName() );
				if (allAccess != null) {
					for (final RElementAccess access : allAccess) {
						if (access.isWriteAccess() && access.getNextSegment() == null) {
							return true;
						}
					}
				}
			}
			progress.setWorkRemaining(1);
			return false;
		}
		finally {
			su.disconnect(progress.newChild(1));
		}
	}
	
	
	protected String getTaskName() {
		return NLS.bind(Messages.SearchProcessor_label, this.name.getDisplayName());
	}
	
	protected void begin(final SubMonitor progress) {
	}
	
	protected void beginFinalProcessing(final SubMonitor progress) {
	}
	
	protected void process(final IRProject project, final List<ISourceUnit> sus,
			final SubMonitor progress) throws BadLocationException {
	}
	
	protected RElementAccess searchMatch(RElementAccess access) {
		RElementName nameSegment= this.name.getNextSegment();
		while (nameSegment != null) {
			access= access.getNextSegment();
			if (access == null
					|| nameSegment.getType() != access.getType()
					|| !nameSegment.getSegmentName().equals(access.getSegmentName()) ) {
				return null;
			}
			nameSegment= nameSegment.getNextSegment();
		}
		return access;
	}
	
}
