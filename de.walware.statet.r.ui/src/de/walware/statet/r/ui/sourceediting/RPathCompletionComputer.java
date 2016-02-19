/*=============================================================================#
 # Copyright (c) 2007-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.ui.sourceediting;

import java.util.List;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextUtilities;

import de.walware.ecommons.ltk.core.model.ISourceUnit;
import de.walware.ecommons.ltk.core.model.IWorkspaceSourceUnit;
import de.walware.ecommons.ltk.ui.sourceediting.ISourceEditor;
import de.walware.ecommons.ltk.ui.sourceediting.assist.AssistInvocationContext;
import de.walware.ecommons.ltk.ui.sourceediting.assist.AssistProposalCollector;
import de.walware.ecommons.ltk.ui.sourceediting.assist.ContentAssist;
import de.walware.ecommons.ltk.ui.sourceediting.assist.PathCompletionComputor;
import de.walware.ecommons.net.resourcemapping.IResourceMapping;
import de.walware.ecommons.net.resourcemapping.IResourceMappingManager;
import de.walware.ecommons.net.resourcemapping.ResourceMappingOrder;
import de.walware.ecommons.net.resourcemapping.ResourceMappingUtils;
import de.walware.ecommons.ts.ITool;

import de.walware.statet.nico.ui.console.ConsolePageEditor;

import de.walware.statet.r.console.core.RProcess;
import de.walware.statet.r.core.IRProject;
import de.walware.statet.r.core.RProjects;
import de.walware.statet.r.core.RUtil;
import de.walware.statet.r.ui.RUI;


/**
 * Completion computer for path in R code.
 * 
 * Supports workspace properties of a R tool process
 */
public class RPathCompletionComputer extends PathCompletionComputor {
	
	
	private RProcess associatedTool;
	
	private IContainer baseResource;
	private IFileStore baseFileStore;
	
	
	public RPathCompletionComputer() {
	}
	
	
	@Override
	public String getPluginId() {
		return RUI.PLUGIN_ID;
	}
	
	@Override
	public void sessionStarted(final ISourceEditor editor, final ContentAssist assist) {
		this.associatedTool= null;
		this.baseResource= null;
		this.baseFileStore= null;
		if (editor instanceof ConsolePageEditor) {
			final ITool tool= (ITool) editor.getAdapter(ITool.class);
			if (tool instanceof RProcess) {
				this.associatedTool= (RProcess) tool;
			}
		}
		else {
			final ISourceUnit su= editor.getSourceUnit();
			if (su instanceof IWorkspaceSourceUnit) {
				final IResource resource= ((IWorkspaceSourceUnit) su).getResource();
				final IRProject project= RProjects.getRProject(resource.getProject());
				this.baseResource= (project != null) ? project.getBaseContainer() : null;
				if (this.baseResource == null) {
					this.baseResource= resource.getParent();
				}
				if (this.baseResource != null) {
					try {
						this.baseFileStore= EFS.getStore(this.baseResource.getLocationURI());
					}
					catch (final CoreException e) {
					}
				}
			}
		}
		
		super.sessionStarted(editor, assist);
	}
	
	@Override
	public void sessionEnded() {
		super.sessionEnded();
		this.associatedTool= null;
	}
	
	@Override
	protected boolean getIsWindows() {
		if ((this.associatedTool != null)) {
			return this.associatedTool.getWorkspaceData().isWindows();
		}
		return super.getIsWindows();
	}
	
	@Override
	protected char getDefaultFileSeparator() {
		if (this.associatedTool != null) {
			return this.associatedTool.getWorkspaceData().getFileSeparator();
		}
		return super.getDefaultFileSeparator();
	}
	
	@Override
	protected IRegion getContentRange(final AssistInvocationContext context, final int mode)
			throws BadLocationException {
		final IDocument document= context.getSourceViewer().getDocument();
		final int offset= context.getInvocationOffset();
		final ITypedRegion partition= TextUtilities.getPartition(document,
				getEditor().getDocumentContentInfo().getPartitioning(), offset, true);
		int start= partition.getOffset();
		int end= partition.getOffset() + partition.getLength();
		if (start == end) {
			return null;
		}
		
		final char bound= document.getChar(start);
		if (bound == '\"' || bound == '\'') {
			start++;
		}
		else {
			return null;
		}
		if (start > offset) {
			return null;
		}
		if (end > start && document.getChar(end-1) == bound) {
			if (end == offset) {
				return null;
			}
			end--;
		}
		
		return new Region(start, end-start);
	}
	
	@Override
	protected IPath getRelativeBasePath() {
		if (this.associatedTool != null) {
			final IFileStore wd= this.associatedTool.getWorkspaceData().getWorkspaceDir();
			if (wd != null) {
				return URIUtil.toPath(wd.toURI());
			}
			return null;
		}
		if (this.baseResource != null) {
			return this.baseResource.getLocation();
		}
		return null;
	}
	
	@Override
	protected IFileStore getRelativeBaseStore() {
		if (this.associatedTool != null) {
			return this.associatedTool.getWorkspaceData().getWorkspaceDir();
		}
		if (this.baseFileStore != null) {
			return this.baseFileStore;
		}
		return null;
	}
	
	@Override
	protected IFileStore resolveStore(final IPath path) throws CoreException {
		if (this.associatedTool != null) {
			return this.associatedTool.getWorkspaceData().toFileStore(path);
		}
		return super.resolveStore(path);
	}
	
	@Override
	protected IStatus tryAlternative(final AssistInvocationContext context,
			final IPath path,
			final int startOffset, final String segmentPrefix, final String completionPrefix,
			final AssistProposalCollector proposals) throws CoreException {
		if (this.associatedTool != null) {
			final String address= this.associatedTool.getWorkspaceData().getRemoteAddress();
			if (address != null) {
				final IResourceMappingManager rmManager= ResourceMappingUtils.getManager();
				if (rmManager != null) {
					final List<IResourceMapping> mappings= rmManager
							.getResourceMappingsFor(address, ResourceMappingOrder.REMOTE);
					for (final IResourceMapping mapping : mappings) {
						IPath remotePath= mapping.getRemotePath();
						if (path.isEmpty()) {
							// remotePath
						}
						else if (path.isPrefixOf(remotePath)) {
							remotePath= remotePath.setDevice(null).makeRelative().removeFirstSegments(path.segmentCount());
						}
						else {
							continue;
						}
						final String name= remotePath.segment(0);
						if (segmentPrefix.isEmpty()
								|| (name != null && name.regionMatches(true, 0, segmentPrefix, 0, segmentPrefix.length())) ) {
							proposals.add(new ResourceProposal(context, startOffset,
									mapping.getFileStore(), remotePath.toString(), completionPrefix,
									null ));
						}
					}
					return Status.OK_STATUS;
				}
			}
		}
		return super.tryAlternative(context, path, startOffset, segmentPrefix,
				completionPrefix, proposals );
	}
	
	@Override
	protected String checkPrefix(final String prefix) {
		String unescaped= RUtil.unescapeCompletely(prefix);
		// keep a single (not escaped) backslash
		if (prefix.length() > 0 && prefix.charAt(prefix.length()-1) == '\\' && 
				(unescaped.isEmpty() || unescaped.charAt(unescaped.length()-1) != '\\')) {
			unescaped= unescaped + '\\';
		}
		return super.checkPrefix(unescaped);
	}
	
	@Override
	protected String checkPathCompletion(final IDocument document, final int completionOffset, String completion)
			throws BadLocationException {
		completion= RUtil.escapeCompletely(completion);
		int existingBackslashCount= 0;
		if (completionOffset >= 1) {
			if (document.getChar(completionOffset-1) == '\\') {
				existingBackslashCount++;
				if (completionOffset >= 2) {
					if (document.getChar(completionOffset-2) == '\\') {
						existingBackslashCount++;
					}
				}
			}
		}
		final boolean startsWithBackslash= (completion.length() >= 2 && 
				completion.charAt(0) == '\\' && completion.charAt(1) == '\\');
		if ((existingBackslashCount % 2) == 1) {
			if (startsWithBackslash) {
				completion= completion.substring(1);
			}
			else {
				completion= '\\' + completion;
			}
		}
		else if (existingBackslashCount > 0) {
			if (startsWithBackslash) {
				completion= completion.substring(2);
			}
		}
		return completion;
	}
	
}
