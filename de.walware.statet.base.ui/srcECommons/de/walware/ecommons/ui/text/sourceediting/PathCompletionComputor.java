/*******************************************************************************
 * Copyright (c) 2007-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.ui.text.sourceediting;

import java.util.Arrays;
import java.util.List;

import com.ibm.icu.text.Collator;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension3;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.ecommons.ui.ECommonsUI;

import de.walware.statet.base.internal.ui.StatetUIPlugin;


/**
 * Content assist processor for completion of path for local file system resources.
 */
public abstract class PathCompletionComputor implements IContentAssistComputer {
	
	
	protected class ResourceProposal extends CompletionProposalWithOverwrite implements ICompletionProposalExtension3 {
		
		private IFileStore fFileStore;
		private boolean fIsDirectory;
		/** The parent in the workspace, if in workspace */
		private IContainer fWorkspaceRef;
		
		private String fName;
		
		/** Final completion string */
		private String fCompletion;
		
		private IRegion fSelectionToSet;
		
		
		/**
		 * Creates a new completion proposal for a resource
		 * 
		 * @param offset the offset in the document where to insert the completion
		 * @param fileStore the EFS resource handle
		 * @param explicitName optional explicit name used instead of the name of the fileStore
		 * @param prefix optional prefix to prefix before the name
		 * @param workspaceRef the workspace resource handle, if the resource is in the workspace
		 */
		public ResourceProposal(final AssistInvocationContext context, 
				final int offset, final IFileStore fileStore, final String explicitName, final String prefix, final IContainer workspaceRef) {
			super(context, offset);
			fFileStore = fileStore;
			fIsDirectory = fFileStore.fetchInfo().isDirectory();
			fWorkspaceRef = workspaceRef;
			final StringBuilder name = new StringBuilder((explicitName != null) ? explicitName : fFileStore.getName());
			if (prefix != null) {
				name.insert(0, prefix);
			}
			if (fIsDirectory) {
				name.append(fPathSeparator);
			}
			fName = name.toString();
		}
		
		
		@Override
		protected String getPluginId() {
			return PathCompletionComputor.this.getPluginId();
		}
		
		public int getRelevance() {
			return 40;
		}
		
		public String getSortingString() {
			return fName;
		}
		
		public boolean isAutoInsertable() {
			return false;
		}
		
		private void createCompletion(final IDocument document) {
			if (fCompletion == null) {
				try {
					fCompletion = checkPathCompletion(document, getReplacementOffset(), fName);
				}
				catch (final BadLocationException e) {
					StatusManager.getManager().handle(new Status(IStatus.ERROR, ECommonsUI.PLUGIN_ID, -1,
							"An error occurred while creating the final path completion.", e), StatusManager.LOG);
				}
			}
		}
		
		@Override
		protected int computeReplacementLength(final int replacementOffset, final Point selection, final int caretOffset, final boolean overwrite) throws BadLocationException {
			int end = Math.max(caretOffset, selection.x + selection.y);
			if (overwrite) {
				final IDocument document = fContext.getSourceViewer().getDocument();
				final int length = document.getLength();
				while (end < length) {
					final char c = document.getChar(end);
					if (Character.isLetterOrDigit(c) || c == '_' || c == '.') {
						end++;
					}
					else {
						break;
					}
				}
				if (end >= length) {
					end = length;
				}
			}
			return (end - replacementOffset);
		}
		
		
		public Image getImage() {
			Image image = null;
			if (fWorkspaceRef != null) {
				final IResource member = fWorkspaceRef.findMember(fFileStore.getName(), true);
				if (member != null) {
					image = StatetUIPlugin.getDefault().getWorkbenchLabelProvider().getImage(member);
				}
			}
			if (image == null) {
				image = PlatformUI.getWorkbench().getSharedImages().getImage(
					fIsDirectory ? ISharedImages.IMG_OBJ_FOLDER : ISharedImages.IMG_OBJ_FILE);
			}
			return image;
		}
		
		/**
		 * {@inheritDoc}
		 */
		public String getDisplayString() {
			return fName;
		}
		
		/**
		 * {@inheritDoc}
		 */
		public String getAdditionalProposalInfo() {
			return null;
		}
		
		/**
		 * {@inheritDoc}
		 */
		public IContextInformation getContextInformation() {
			return null;
		}
		
		/**
		 * {@inheritDoc}
		 */
		public IInformationControlCreator getInformationControlCreator() {
			return null;
		}
		
		/**
		 * {@inheritDoc}
		 */
		public boolean validate(final IDocument document, final int offset, final DocumentEvent event) {
			final int replacementOffset = getReplacementOffset();
			if (offset < replacementOffset) {
				return false;
			}
			try {
				final String startsWith = document.get(replacementOffset, offset-replacementOffset);
				return fName.regionMatches(true, 0, startsWith, 0, startsWith.length());
			}
			catch (final BadLocationException e) {
				return false;
			}
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void doApply(final char trigger, final int stateMask, final int caretOffset, final int replacementOffset, final int replacementLength)
				throws BadLocationException {
			final SourceViewer viewer = fContext.getSourceViewer();
			final IDocument document = viewer.getDocument();
			final Point selectedRange = viewer.getSelectedRange();
			createCompletion(document);
			final Position newSelectionOffset = new Position(replacementOffset+replacementLength, 0);
			try {
				document.addPosition(newSelectionOffset);
				document.replace(replacementOffset, newSelectionOffset.getOffset()-replacementOffset, fCompletion);
				fSelectionToSet = new Region(newSelectionOffset.getOffset(), 0);
			}
			catch (final BadLocationException e) {
				StatusManager.getManager().handle(new Status(IStatus.ERROR, ECommonsUI.PLUGIN_ID, -1,
						"An error occurred while inserting the path completion.", e), StatusManager.SHOW | StatusManager.LOG);
				return;
			}
			finally {
				document.removePosition(newSelectionOffset);
			}
			if (fIsDirectory && viewer instanceof ITextOperationTarget) {
				final ITextOperationTarget target = viewer;
				Display.getCurrent().asyncExec(new Runnable() {
					public void run() {
						if (target.canDoOperation(ISourceViewer.CONTENTASSIST_PROPOSALS)) {
							target.doOperation(ISourceViewer.CONTENTASSIST_PROPOSALS);
						}
					}
				});
			}
		}
		
		/**
		 * {@inheritDoc}
		 */
		public Point getSelection(final IDocument document) {
			if (fSelectionToSet != null) {
				return new Point(fSelectionToSet.getOffset(), fSelectionToSet.getLength());
			}
			return null;
		}
		
		/**
		 * {@inheritDoc}
		 */
		public int getPrefixCompletionStart(final IDocument document, final int completionOffset) {
			return getReplacementOffset();
		}
		
		/**
		 * {@inheritDoc}
		 */
		public CharSequence getPrefixCompletionText(final IDocument document, final int completionOffset) {
			createCompletion(document);
			return fCompletion;
		}
		
	}
	
	
	private String fPathSeparator;
	private String fPathSeparatorBackup;
	
	
	public PathCompletionComputor() {
	}
	
	
	public abstract String getPluginId();
	/**
	 * {@inheritDoc}
	 */
	public void sessionStarted(final ISourceEditor editor) {
		fPathSeparator = getDefaultFileSeparator();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void sessionEnded() {
	}
	
	protected String getDefaultFileSeparator() {
		// use backslash only for local window systems
		return isWin() ? "\\" : "/"; //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	protected String getFileSeparator() {
		return fPathSeparator;
	}
	
	public char[] getCompletionProposalAutoActivationCharacters() {
		return new char[] { '/', '\\', ':' };
	}
	
	public char[] getContextInformationAutoActivationCharacters() {
		return null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public IStatus computeCompletionProposals(final AssistInvocationContext context, final int mode, final List<IAssistCompletionProposal> tenders, final IProgressMonitor monitor) {
		try {
			final IDocument document = context.getSourceViewer().getDocument();
			final int offset = context.getInvocationOffset();
			final IRegion partition = getContentRange(document, offset);
			if (partition == null) {
				return null;
			}
			
			String prefix = checkPrefix(
					document.get(partition.getOffset(), offset-partition.getOffset()) );
			
			if (prefix == null) {
				return null;
			}
			
			boolean needSeparatorBeforeStart = false; // including virtual separator
			String start = ""; //$NON-NLS-1$
			IFileStore baseStore = null;
			IPath path = null;
			
			final char lastChar = (prefix.length() > 0) ? prefix.charAt(prefix.length()-1) : 0;
			switch (lastChar) {
			case ':':
				prefix = prefix+fPathSeparator;
				if (Path.ROOT.isValidPath(prefix)) {
					path = new Path(prefix);
					needSeparatorBeforeStart = true;
				}
				break;
			case '.':
				// prevent that path segments '.' and '..' at end are resolved
				if (prefix.equals(".") || prefix.endsWith("\\.") || prefix.endsWith("/.")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					prefix = prefix.substring(0, prefix.length()-1);
					if (Path.ROOT.isValidPath(prefix)) {
						start = "."; //$NON-NLS-1$
						path = new Path(prefix);
					}
					break;
				}
				else if (prefix.equals("..") || prefix.endsWith("\\..") || prefix.endsWith("/..")) { // //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					prefix = prefix.substring(0, prefix.length()-2);
					if (Path.ROOT.isValidPath(prefix)) {
						start = ".."; //$NON-NLS-1$
						path = new Path(prefix);
					}
					break;
				}
				//$FALL-THROUGH$
			default:
				if (Path.ROOT.isValidPath(prefix)) {
					path = new Path(prefix);
					if (path.segmentCount() > 0 && lastChar != '\\' && lastChar != '/') {
						start = path.lastSegment();
						path = path.removeLastSegments(1);
						if (path == null) {
							path = new Path(""); //$NON-NLS-1$
						}
					}
				}
				break;
			}
			
			if (path != null) {
				// on Windows, path starting with path separator are relative to the device of current directory
				if (path.isAbsolute() && isWin() && path.getDevice() == null && !path.isUNC()) { 
					final IPath workspace = getRelativeBasePath();
					if (workspace != null) {
						path = path.setDevice(workspace.getDevice());
					}
				}
				baseStore = resolveStore(path);
			}
			
			updatePathSeparator(prefix);
			
			String completionPrefix = (needSeparatorBeforeStart) ? fPathSeparator : null;
			
			if (baseStore == null || !baseStore.fetchInfo().exists()) {
				if (path != null) {
					return tryAlternative(context, tenders, path, offset-start.length(), start, prefix, completionPrefix);
				}
				return null;
			}
			
			doAddChildren(context, tenders, baseStore, offset-start.length(), start, completionPrefix);
			if (start != null && start.length() > 0 && !start.equals(".")) { //$NON-NLS-1$
				baseStore = baseStore.getChild(start);
				if (baseStore.fetchInfo().exists()) {
					final StringBuilder prefixBuilder = new StringBuilder();
					if (completionPrefix != null) {
						prefixBuilder.append(completionPrefix);
					}
					prefixBuilder.append(baseStore.getName());
					prefixBuilder.append(fPathSeparator);
					completionPrefix = prefixBuilder.toString();
					doAddChildren(context, tenders, baseStore, offset-start.length(), null, completionPrefix);
				}
			}
			return Status.OK_STATUS;
		}
		catch (final BadLocationException e) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, ECommonsUI.PLUGIN_ID, -1,
					"An error occurred while preparing path completions.", e), StatusManager.LOG);
		}
		catch (final CoreException e) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, ECommonsUI.PLUGIN_ID, -1,
					"An error occurred while preparing path completions.", e), StatusManager.LOG);
		}
		restorePathSeparator();
		return null;
	}
	
	/**
	 * @param prefix to check
	 * @return the prefix, if valid, otherwise <code>null</code>
	 */
	protected String checkPrefix(final String prefix) {
		final char[] breakingChars = "\n\r+<>|?*\"".toCharArray(); //$NON-NLS-1$
		for (int i = 0; i < breakingChars.length; i++) {
			if (prefix.indexOf(breakingChars[i]) >= 0) {
				return null;
			}
		}
		return prefix;
	}
	
	private void updatePathSeparator(final String prefix) {
		final int lastBack = prefix.lastIndexOf('\\');
		final int lastForw = prefix.lastIndexOf('/');
		if (lastBack > lastForw) {
			fPathSeparatorBackup = fPathSeparator;
			fPathSeparator = "\\"; //$NON-NLS-1$
		}
		else if (lastForw > lastBack) {
			fPathSeparatorBackup = fPathSeparator;
			fPathSeparator = "/"; //$NON-NLS-1$
		}
		// else -1 == -1
	}
	
	private void restorePathSeparator() {
		if (fPathSeparatorBackup != null) {
			fPathSeparator = fPathSeparatorBackup;
			fPathSeparatorBackup = null;
		}
	}
	
	protected void doAddChildren(final AssistInvocationContext context, final List<IAssistCompletionProposal> matches, final IFileStore baseStore, 
			final int startOffset, final String startsWith, final String prefix) throws CoreException {
		final IContainer[] workspaceRefs = ResourcesPlugin.getWorkspace().getRoot().findContainersForLocationURI(baseStore.toURI());
		final IContainer workspaceRef = (workspaceRefs.length > 0) ? workspaceRefs[0] : null;
		final String[] names = baseStore.childNames(EFS.NONE, new NullProgressMonitor());
		Arrays.sort(names, Collator.getInstance());
		for (final String name : names) {
			if (startsWith == null || name.regionMatches(true, 0, startsWith, 0, startsWith.length())) {
				matches.add(new ResourceProposal(context, startOffset, baseStore.getChild(name), null, prefix, workspaceRef));
			}
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public IStatus computeContextInformation(final AssistInvocationContext context, final List<IAssistInformationProposal> tenders, final IProgressMonitor monitor) {
		return null;
	}
	
	
	protected abstract IRegion getContentRange(IDocument document, int offset) throws BadLocationException;
	
	protected IPath getRelativeBasePath() {
		return null;
	}
	
	protected IFileStore getRelativeBaseStore() {
		return null;
	}
	
	protected IFileStore resolveStore(final IPath path) throws CoreException {
		if (!path.isAbsolute()) {
			final IFileStore base = getRelativeBaseStore();
			if (base != null) {
				return base.getFileStore(path);
			}
			return null;
		}
		else {
			return EFS.getStore(URIUtil.toURI(path));
		}
	}
	
	protected IStatus tryAlternative(final AssistInvocationContext context, final List<IAssistCompletionProposal> matches, final IPath path,
			final int startOffset, final String startsWith, final String prefix, final String completionPrefix) throws CoreException {
		return null;
	}
	
	protected boolean isWin() {
		return Platform.getOS().startsWith("win"); //$NON-NLS-1$
	}
	
	/**
	 * Final check of completion string. 
	 * 
	 * E.g. to escape special chars.
	 * 
	 * @param document
	 * @param completionOffset
	 * @param completion
	 * 
	 * @return the checked completion string
	 * @throws BadLocationException
	 */
	protected String checkPathCompletion(final IDocument document, final int completionOffset, final String completion) 
			throws BadLocationException {
		return completion;
	}
	
}
