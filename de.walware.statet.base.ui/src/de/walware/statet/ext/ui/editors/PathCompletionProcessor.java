/*******************************************************************************
 * Copyright (c) 2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.ext.ui.editors;

import java.util.ArrayList;
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
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension2;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension3;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.statet.base.internal.ui.StatetUIPlugin;


/**
 * Content assist processor for completion of path for local file system resources.
 */
public abstract class PathCompletionProcessor implements IContentAssistProcessor {
	
	
	private static final String[] DEFAULT_BOUNDS = new String[] { "\"", "\'" }; //$NON-NLS-1$ //$NON-NLS-2$
	
	
	private class ResourceProposal implements ICompletionProposal, ICompletionProposalExtension2, ICompletionProposalExtension3 {
		
		private IFileStore fFileStore;
		private boolean fIsDirectory;
		private String fName;
		private Image fImage;
		
		private String fCompletion;
		private IRegion fTarget;
		private int fBeginIdx;
		
		
		public ResourceProposal(IFileStore fileStore, String prefix, IContainer workspaceRef, IRegion target, int beginIdx) {
			fFileStore = fileStore;
			fIsDirectory = fFileStore.fetchInfo().isDirectory();
			StringBuilder name = new StringBuilder(fFileStore.getName());
			if (prefix != null) {
				name.insert(0, prefix);
			}
			if (fIsDirectory) {
				name.append(fPathSeparator);
			}
			if (workspaceRef != null) {
				IResource member = workspaceRef.findMember(fFileStore.getName(), false);
				if (member != null) {
					fImage = StatetUIPlugin.getDefault().getWorkbenchLabelProvider().getImage(member);
				}
			}
			if (fImage == null) {
				fImage = PlatformUI.getWorkbench().getSharedImages().getImage(
					fIsDirectory ? ISharedImages.IMG_OBJ_FOLDER : ISharedImages.IMG_OBJ_FILE);
			}
			fName = name.toString();
			
			fTarget = target;
			fBeginIdx = beginIdx;
		}
		
		public void createCompletion(IDocument document) {
			if (fCompletion == null) {
				fCompletion = checkPathCompletion(fName.substring(fBeginIdx));
				try {
					int offset = fTarget.getOffset();
					if (offset >= 2
							&& document.getChar(offset-1) == '\\' && document.getChar(offset-2) != '\\') {
							fCompletion = '\\'+fCompletion;
					}
				} catch (BadLocationException e) {
				}
			}
		}
		
		
		public Image getImage() {
			return fImage;
		}
		
		public String getDisplayString() {
			return fName;
		}
		
		public void selected(ITextViewer viewer, boolean smartToggle) {
		}
		
		public void unselected(ITextViewer viewer) {
		}
		
		public String getAdditionalProposalInfo() {
			return null;
		}
		
		public IContextInformation getContextInformation() {
			return null;
		}
		
		public IInformationControlCreator getInformationControlCreator() {
			return null;
		}
		
		public boolean validate(IDocument document, int offset, DocumentEvent event) {
			return false;
		}
		
		public void apply(ITextViewer viewer, char trigger, int stateMask, int offset) {
			apply(viewer.getDocument());
			if (fIsDirectory && viewer instanceof ITextOperationTarget) {
				final ITextOperationTarget target = (ITextOperationTarget) viewer;
				Display.getCurrent().asyncExec(new Runnable() {
					public void run() {
						if (target.canDoOperation(ISourceViewer.CONTENTASSIST_PROPOSALS)) {
							target.doOperation(ISourceViewer.CONTENTASSIST_PROPOSALS);
						}
					}
				});
			}
		}
		
		public void apply(IDocument document) {
			createCompletion(document);
			try {
				document.replace(fTarget.getOffset(), fTarget.getLength(), fCompletion);
			} catch (BadLocationException e) {
				StatusManager.getManager().handle(new Status(Status.ERROR, StatetUIPlugin.PLUGIN_ID, -1,
						"An error occurred while inserting the path completion.", e), StatusManager.SHOW | StatusManager.LOG);
			}
		}
		
		public Point getSelection(IDocument document) {
			return new Point(fTarget.getOffset()+fCompletion.length(), 0);
		}
		
		public int getPrefixCompletionStart(IDocument document, int completionOffset) {
			return fTarget.getOffset();
		}
		
		public CharSequence getPrefixCompletionText(IDocument document, int completionOffset) {
			createCompletion(document);
			return fCompletion;
		}
		
	}
	
	
	private String fPathSeparator;
	private String fPathSeparatorBackup;
	
	
	public PathCompletionProcessor() {
		 fPathSeparator = System.getProperty("file.separator"); //$NON-NLS-1$
	}
	
	
	public char[] getCompletionProposalAutoActivationCharacters() {
		return new char[] { '/', '\\', ':' };
	}
	
	public char[] getContextInformationAutoActivationCharacters() {
		return null;
	}
	
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
		try {
			IDocument document = viewer.getDocument();
			IRegion partition = getContentRange(document, offset);
			if (partition == null) {
				return new ICompletionProposal[0];
			}
			
			final String prefix = checkPrefix(
					document.get(partition.getOffset(), offset-partition.getOffset()) );
			
			boolean separatorAtEnd = false;
			String start = ""; //$NON-NLS-1$
			IFileStore baseStore = null;
			if (prefix != null) {
				char lastChar = (prefix.length() > 0) ? prefix.charAt(prefix.length()-1) : 0;
				separatorAtEnd = (lastChar == '\\' || lastChar == '/');
				
				IPath path;
				if (lastChar == '.'
					&& (prefix.length() == 1 || prefix.endsWith("\\.") || prefix.endsWith("/."))) { //$NON-NLS-1$ //$NON-NLS-2$
					if (prefix.length() <= 1) {
						path = new Path(""); //$NON-NLS-1$
					}
					else {
						path = new Path(prefix.substring(0, prefix.length()-2));
					}
					start = "."; //$NON-NLS-1$
					separatorAtEnd = true;
				}
				else if (lastChar == ':' && prefix.length() == 2) {
					path = new Path(prefix+fPathSeparator);
				}
				else {
					path = new Path(prefix);
					if (!path.isValidPath(prefix)) {
						path = null;
					}
					else if (!separatorAtEnd && path.segmentCount() > 0) {
						if (lastChar != '.'
							|| !(prefix.equals("..") || prefix.equals("."))) { //$NON-NLS-1$ //$NON-NLS-2$
							start = path.lastSegment();
							path = path.removeLastSegments(1);
						}
					}
					if (path.segmentCount() == 0 || start.length() > 0) {
						separatorAtEnd = true;
					}
				}
				
				if (path != null) {
					if (path.isAbsolute()) {
						baseStore = EFS.getStore(URIUtil.toURI(path));
					}
					else {
						baseStore = getRelativeBase();
						if (baseStore != null) {
							path = URIUtil.toPath(baseStore.toURI()).append(path).makeAbsolute();
							baseStore = EFS.getStore(URIUtil.toURI(path));
						}
					}
				}
			}
			
			if (baseStore == null || !baseStore.fetchInfo().exists()) {
				return new ICompletionProposal[0];
			}
			
			// Selection
			IRegion region;
			ITextSelection selection = (ITextSelection) viewer.getSelectionProvider().getSelection();
			if (selection.getLength() == 0) {
				region = new Region(offset, 0);
			} else {
				region = new Region(selection.getOffset(), selection.getLength());
			}
			// Separator type
			updatePathSeparator(prefix);
			
			List<ICompletionProposal> matches = new ArrayList<ICompletionProposal>();
			doAdd(matches, baseStore, start, (!separatorAtEnd) ? fPathSeparator : null, start.length(), region);
			if (start != null && start.length() > 0 && !start.equals(".")) { //$NON-NLS-1$
				baseStore = baseStore.getChild(start);
				if (baseStore.fetchInfo().exists()) {
					StringBuilder fullPrefix = new StringBuilder(baseStore.getName());
					if (!separatorAtEnd) {
						fullPrefix.insert(0, fPathSeparator);
					}
					fullPrefix.append(fPathSeparator);
					doAdd(matches, baseStore, null, fullPrefix.toString(), start.length(), region);
				}
			}
			if (matches.size() > 0) {
				return matches.toArray(new ICompletionProposal[matches.size()]);
			}
		}
		catch (BadLocationException e) {
			StatetUIPlugin.logError(-1, "Error while fetching text infos for resource completion proposals.", e); //$NON-NLS-1$
		}
		catch (CoreException e) {
			StatetUIPlugin.logError(-1, "Error while fetching file infos for resource completion proposals.", e); //$NON-NLS-1$
		}
		restorePathSeparator();
		return new ICompletionProposal[0];
	}
	
	protected String checkPrefix(String prefix) {
		char[] breakingChars = "\n\r+<>|?*\"\'".toCharArray(); //$NON-NLS-1$
		for (int i = 0; i < breakingChars.length; i++) {
			if (prefix.indexOf(breakingChars[i]) >= 0) {
				return null;
			}
		}
		return prefix;
	}
	
	private void updatePathSeparator(String prefix) {
		int lastBack = prefix.lastIndexOf('\\');
		int lastForw = prefix.lastIndexOf('/');
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
	
	protected void doAdd(List<ICompletionProposal> matches, IFileStore baseStore, String filterStart,
			String prefix, int startIdx, IRegion target) throws CoreException {
		IContainer[] workspaceRefs = ResourcesPlugin.getWorkspace().getRoot().findContainersForLocationURI(baseStore.toURI());
		IContainer workspaceRef = (workspaceRefs.length > 0) ? workspaceRefs[0] : null;
		String[] names = baseStore.childNames(EFS.NONE, new NullProgressMonitor());
		Arrays.sort(names, Collator.getInstance());
		for (String name : names) {
			if (filterStart == null || name.startsWith(filterStart)) {
				matches.add(new ResourceProposal(baseStore.getChild(name), prefix, workspaceRef, target, startIdx));
			}
		}
	}
	
	public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
		return null;
	}
	
	public IContextInformationValidator getContextInformationValidator() {
		return null;
	}
	
	public String getErrorMessage() {
		return null;
	}
	
	
	protected abstract IRegion getContentRange(IDocument document, int offset) throws BadLocationException;
	
	protected IFileStore getRelativeBase() {
		return null;
	}
	
	protected String checkPathCompletion(String completion) {
		return completion;
	}
	
}
