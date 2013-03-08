/*******************************************************************************
 * Copyright (c) 2010-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.debug.core.sourcelookup;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.sourcelookup.AbstractSourceLookupParticipant;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.jface.text.AbstractDocument;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ISynchronizable;
import org.eclipse.osgi.util.NLS;

import de.walware.ecommons.io.FileUtil;
import de.walware.ecommons.ltk.IModelElement;
import de.walware.ecommons.ltk.IModelManager;
import de.walware.ecommons.ltk.ISourceUnit;
import de.walware.ecommons.ltk.ISourceUnitModelInfo;
import de.walware.ecommons.ltk.LTK;

import de.walware.rj.server.dbg.FrameContext;
import de.walware.rj.server.dbg.Srcref;

import de.walware.statet.r.console.core.RDbg;
import de.walware.statet.r.console.core.RProcess;
import de.walware.statet.r.core.model.IRMethod;
import de.walware.statet.r.core.model.IRModelInfo;
import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.core.rsource.ast.Block;
import de.walware.statet.r.core.rsource.ast.FDef;
import de.walware.statet.r.core.rsource.ast.NodeType;
import de.walware.statet.r.core.rsource.ast.RAst;
import de.walware.statet.r.core.rsource.ast.RAst.AssignExpr;
import de.walware.statet.r.core.rsource.ast.RAstNode;
import de.walware.statet.r.debug.core.sourcelookup.IRSourceContainer;
import de.walware.statet.r.debug.core.sourcelookup.IRSourceLookupMatch;
import de.walware.statet.r.debug.core.sourcelookup.RRuntimeSourceFragment;
import de.walware.statet.r.internal.debug.core.RDebugCorePlugin;
import de.walware.statet.r.internal.debug.core.RStackFrame;
import de.walware.statet.r.nico.IRSrcref;


public class RSourceLookupParticipant extends AbstractSourceLookupParticipant {
	
	
	private static final boolean DEBUG = Boolean.parseBoolean(System.getProperty("de.walware.statet.r.debug.sourcelookup")); //$NON-NLS-1$
	
	private static final int QUALITY_EXACT_FILE_TIMESTAMP = 23;
	private static final int QUALITY_EXACT_FILE_CONTENT = 22;
	private static final int QUALITY_EXACT_FUNCTION_CONTENT = 13;
	private static final int QUALITY_POSITION_FOUND = 10;
	
	
	protected static class LookupData {
		
		final RStackFrame frame;
		final FrameContext context;
		
		RRuntimeSourceFragment fragment;
		
		List<IStatus> status;
		
		public LookupData(final RStackFrame frame) {
			this.frame = frame;
			context = this.frame.getContext();
		}
		
		public void addStatus(final IStatus status) {
			if (this.status == null) {
				this.status = new ArrayList<IStatus>();
			}
			this.status.add(status);
		}
		
	}
	
	protected static class SourceCorrection {
		
		int firstLine;
		int firstColumn;
		
		int bFirstLineCharOffset;
		int bFirstColumnCharOffset;
		int bExprLineOffset;
		int suFirstLineCharOffset;
		int suFirstColumnCharOffset;
		int suExprLineOffset;
		
	}
	
	protected static boolean equalTimestamp(final long suTimestamp, final long rTimestamp) {
		return (suTimestamp == rTimestamp
				|| Math.abs(suTimestamp - rTimestamp) == 3600 // R returns wrong timestamp under some conditions (daylight saving)
		);
	}
	
	protected class RSourceLookupMatch implements RStackFrame.PositionResolver, IRSourceLookupMatch {
		
		
		private final RStackFrame fFrame;
		
		private final Object fSourceElement;
		
		private FrameContext fCurrentContext;
		private int fQuality;
		
		private int fLineNumber;
		private int fCharStart;
		private int fCharEnd;
		
		
		public RSourceLookupMatch(final RStackFrame frame, final Object sourceElement) {
			fFrame = frame;
			fSourceElement = sourceElement;
		}
		
		
		@Override
		public Object getElement() {
			return fSourceElement;
		}
		
		@Override
		public synchronized int getLineNumber() {
			if (fCurrentContext != fFrame.getContext()) {
				update();
			}
			return fLineNumber;
		}
		
		@Override
		public synchronized int getCharStart() {
			if (fCurrentContext != fFrame.getContext()) {
				update();
			}
			return fCharStart;
		}
		
		@Override
		public synchronized int getCharEnd() {
			if (fCurrentContext != fFrame.getContext()) {
				update();
			}
			return fCharEnd;
		}
		
		public void install() {
			if (DEBUG) {
				RDebugCorePlugin.log(new Status(IStatus.INFO, RDebugCorePlugin.PLUGIN_ID, 0,
						"Installing " + toString(), null));
			}
			fFrame.setPositionResolver(fCurrentContext, this);
		}
		
		@Override
		public void select() {
			install();
		}
		
		public void update() {
			final LookupData data = new LookupData(fFrame);
			if (data.context != null) {
				checkPosition(data, this);
			}
		}
		
		
		@Override
		public int hashCode() {
			return fFrame.hashCode() + fSourceElement.hashCode();
		}
		
		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof RSourceLookupMatch)) {
				return false;
			}
			final RSourceLookupMatch other = (RSourceLookupMatch) obj;
			return (fFrame == other.fFrame
					&& fSourceElement.equals(other.fSourceElement) );
		}
		
		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder(getClass().getName());
			sb.append("\n").append("for ").append(fFrame.toString());
			sb.append("\n").append("source lookup result:");
			sb.append("\n\t").append("sourceElement= ").append(fSourceElement);
			sb.append("\n\t").append("lineNumber= ").append(fLineNumber);
			sb.append("\n\t").append("charStart= ").append(fCharStart);
			sb.append("\n\t").append("charEnd= ").append(fCharEnd);
			return sb.toString();
		}
		
	}
	
	
	/** Created by extension point */
	public RSourceLookupParticipant() {
	}
	
	
	@Override
	public String getSourceName(final Object object) throws CoreException {
		if (object instanceof RStackFrame) {
			final RStackFrame frame = (RStackFrame) object;
			final FrameContext context = frame.getContext();
			if (context.getFileName() != null) {
				return context.getFileName();
			}
		}
		return null;
	}
	
	@Override
	public Object[] findSourceElements(final Object object) throws CoreException {
		if (object instanceof RStackFrame) {
			final List<RSourceLookupMatch> matches = new ArrayList<RSourceLookupMatch>();
			final LookupData data = new LookupData((RStackFrame) object);
			try {
				boolean addedFile = false;
				int bestQuality = -1;
				final boolean findDuplicates = isFindDuplicates();
				if (DEBUG) {
					data.addStatus(new Status(IStatus.INFO, RDebugCorePlugin.PLUGIN_ID, 0,
							NLS.bind("Beginning R source lookup for {0}.", object), null));
				}
				if (data.context == null) {
					if (DEBUG) {
						data.addStatus(new Status(IStatus.INFO, RDebugCorePlugin.PLUGIN_ID, 0,
								"Context with detail is not available.", null));
					}
					return null;
				}
				
				URI fileUri = null;
				if (data.context.getFileName() != null) {
					IFile wsFile = null;
					IFileStore fileStore = null;
					try {
						final RProcess process = data.frame.getDebugTarget().getProcess();
						if (process.getWorkspaceData().isRemote()) {
							fileStore = process.getWorkspaceData().toFileStore(data.context.getFileName());
						}
						else {
							fileStore = FileUtil.getFileStore(data.context.getFileName());
						}
					}
					catch (final Exception e) {
						data.addStatus(new Status(IStatus.WARNING, RDebugCorePlugin.PLUGIN_ID, 0,
								NLS.bind("An error occured when looking up R sources ({0}).",
										data.context.getFileName() ), e ));
					}
					try {
						if (fileStore != null) {
							fileUri = fileStore.toURI();
						}
						if (fileUri == null) {
							fileUri = URIUtil.toURI(data.context.getFileName());
						}
					}
					catch (final Exception e) {
						data.addStatus(new Status(IStatus.WARNING, RDebugCorePlugin.PLUGIN_ID, 0,
								NLS.bind("An error occured when looking up R sources ({0}).",
										data.context.getFileName() ), e ));
					}
					if (fileUri != null && !fileUri.isAbsolute()) {
						fileUri = null;
					}
					if (DEBUG) {
						final StringBuilder sb = new StringBuilder();
						sb.append("Resolved filenames:\n");
						sb.append("fileStore= ").append(fileStore != null ? fileStore.toString() : "<missing>");
						sb.append("\n");
						sb.append("fileUri= ").append(fileStore != null ? fileUri.toString() : "<missing>");
						data.addStatus(new Status(IStatus.INFO, RDebugCorePlugin.PLUGIN_ID, 0,
								sb.toString(), null));
					}
					
					final IPath path = (data.context.getFilePath() != null) ? 
							Path.fromPortableString(data.context.getFilePath()) : null;
					boolean addedPath = false;
					try {
						if (fileUri != null) {
							final IFile[] files = ResourcesPlugin.getWorkspace().getRoot()
									.findFilesForLocationURI(fileUri, IContainer.INCLUDE_HIDDEN);
							List<Object> elements = null;
							if (files.length > 0 && path != null) {
								for (int i = 0; i < files.length; i++) {
									if (path.equals(files[i].getFullPath())) {
										addedPath = true;
										elements = findSourceElement(fileUri,
												new IFile[] { files[i] }, data );
										break;
									}
								}
							}
							if (elements == null) {
								elements = findSourceElement(fileUri, files, data);
							}
							if (elements != null) {
								addedFile = true;
								final RSourceLookupMatch match = new RSourceLookupMatch(data.frame, elements.get(0));
								checkPosition(data, match);
								matches.add(match);
								bestQuality = Math.max(bestQuality, match.fQuality);
							}
							else if (files.length > 0) {
								wsFile = files[0];
							}
						}
					}
					catch (final Exception e) {
						data.addStatus(new Status(IStatus.WARNING, RDebugCorePlugin.PLUGIN_ID, 0,
								NLS.bind("An error occured when looking up R sources ({0}).",
										fileUri.toString() ), e ));
					}
					
					// try workspacePath
					if (!addedPath && path != null) {
						try {
							if (fileUri != null) {
								final IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
								addedPath = true;
								List<Object> elements = null;
								if (file.exists()) {
									elements = findSourceElement(file.getLocationURI(),
											new IFile[] { file }, data );
								}
								if (elements == null) {
									elements = findSourceElement(path, data);
								}
								if (elements != null) {
									for (int i = 0; i < elements.size(); i++) {
										final RSourceLookupMatch match = new RSourceLookupMatch(data.frame, elements.get(i));
										checkPosition(data, match);
										matches.add(match);
										bestQuality = Math.max(bestQuality, match.fQuality);
										if (bestQuality >= QUALITY_EXACT_FILE_CONTENT) {
											break;
										}
									}
								}
							}
						}
						catch (final Exception e) {
							data.addStatus(new Status(IStatus.WARNING, RDebugCorePlugin.PLUGIN_ID, 0,
									NLS.bind("An error occured when looking up R sources ({0}).",
											path ), e ));
						}
					}
					
					// fallback method 1: real file (ws file / filestore)
					if ((bestQuality <= 0 || findDuplicates)
							&& !addedFile && wsFile != null) {
						try {
							if (wsFile.exists()) {
								addedFile = true;
								final RSourceLookupMatch match = new RSourceLookupMatch(data.frame, wsFile);
								checkPosition(data, match);
								matches.add(match);
								bestQuality = Math.max(bestQuality, match.fQuality);
							}
						}
						catch (final Exception e) {
							if (!findDuplicates && !matches.isEmpty()) {
								matches.clear();
							}
							data.addStatus(new Status(IStatus.WARNING, RDebugCorePlugin.PLUGIN_ID, 0,
									NLS.bind("An error occured when looking up R sources ({0}).",
											wsFile.toString() ), e ));
						}
					}
					if ((bestQuality <= 0 || findDuplicates)
							&& !addedFile && fileStore != null) {
						try {
							if (fileStore.fetchInfo().exists()) {
								addedFile = true;
								final RSourceLookupMatch match = new RSourceLookupMatch(data.frame, fileStore);
								checkPosition(data, match);
								matches.add(match);
								bestQuality = Math.max(bestQuality, match.fQuality);
							}
						}
						catch (final Exception e) {
							if (!findDuplicates && !matches.isEmpty()) {
								matches.clear();
							}
							data.addStatus(new Status(IStatus.WARNING, RDebugCorePlugin.PLUGIN_ID, 0,
									NLS.bind("An error occured when looking up R sources ({0}).",
											fileStore.toString() ), e));
						}
					}
				}
				
				// fallback method 2: runtime source
				if ((bestQuality <= 0 || findDuplicates)
						&& data.context.getSourceCode() != null) {
					try {
						createFragment(data);
						final RSourceLookupMatch match = new RSourceLookupMatch(data.frame, data.fragment);
						checkPosition(data, match);
						matches.add(match);
						bestQuality = Math.max(bestQuality, match.fQuality);
					}
					catch (final Exception e) {
						if (!findDuplicates && !matches.isEmpty()) {
							matches.clear();
						}
						data.addStatus(new Status(IStatus.WARNING, RDebugCorePlugin.PLUGIN_ID, 0,
								NLS.bind("An error occured when looking up R sources ({0}).",
										data.context.getSourceType() ), e));
					}
				}
				
				if (matches.isEmpty() && data.context.getPosition() <= 0) {
					// can we prevent opening the editor? 
					throw new DebugException(new Status(IStatus.INFO, RDebugCorePlugin.PLUGIN_ID,
							DebugException.NOT_SUPPORTED, "Not supported.", null));
				}
				else if (DEBUG) {
					if (matches.isEmpty()) {
						RDebugCorePlugin.log(new Status(IStatus.INFO, RDebugCorePlugin.PLUGIN_ID, 0,
								NLS.bind("Could not find R sources ({0}, {1}).",
										data.context.getSourceType(), data.context.getFileName() ), null));
					}
					else {
						RDebugCorePlugin.log(new Status(IStatus.INFO, RDebugCorePlugin.PLUGIN_ID, 0,
								NLS.bind("Could not find R sources ({0}, {1}).",
										data.context.getSourceType(), data.context.getFileName() ), null));
					}
				}
				
				if (matches.size() > 0
						&& (!findDuplicates || matches.size() == 1) ) {
					RSourceLookupMatch match = null;
					for (int i = 0; i < matches.size(); i++) {
						if (matches.get(i).fQuality >= bestQuality) {
							match = matches.get(i);
							break;
						}
					}
					if (!findDuplicates && matches.size() > 1) {
						matches.clear();
						matches.add(match);
					}
					match.install();
				}
				else {
					data.frame.setPositionResolver(data.context, null);
				}
				return matches.toArray();
			}
			finally {
				if (data.status != null) {
					RDebugCorePlugin.log(new MultiStatus(RDebugCorePlugin.PLUGIN_ID, 0,
							data.status.toArray(new IStatus[data.status.size()]),
							"R source lookup report.", null));
				}
			}
		}
		return null;
	}
	
	protected void createFragment(final LookupData data) {
		if (data.fragment == null) {
			if (data.context.getSourceCode() == null) {
				throw new IllegalStateException();
			}
			String name;
			String fullName;
			if (data.context.getSourceType() == 2
					&& data.context.getFileName() != null) {
				fullName = name = data.context.getFileName();
				int idx = name.lastIndexOf('/');
				{	final int idx2 = name.lastIndexOf('\\');
					if (idx2 > idx) {
						idx = idx2;
					}
				}
				if (idx >= 0) {
					name = name.substring(idx+1);
				}
			}
			else if (data.context.getCall() != null) {
				fullName = name = data.context.getCall();
				final int idx = name.indexOf('(');
				if (idx > 0) {
					name = name.substring(0, idx);
				}
			}
			else {
				fullName = name = "Frame #" + data.context.getPosition();
			}
			data.fragment = new RRuntimeSourceFragment(
					data.frame.getDebugTarget().getProcess(), name,
					fullName, data.context.getSourceCode() );
		}
	}
	
	protected List<Object> findSourceElement(final URI fileUri, final IFile[] fileInWorkspace, final LookupData data) {
		final ISourceContainer[] containers = getSourceContainers();
		for (int i = 0; i < containers.length; i++) {
			if (containers[i] instanceof IRSourceContainer) {
				try {
					final Object element = ((IRSourceContainer) containers[i]).findSourceElement(fileUri, fileInWorkspace);
					if (element != null) {
						if (DEBUG) {
							data.addStatus(new Status(IStatus.INFO, RDebugCorePlugin.PLUGIN_ID, 0,
									NLS.bind("Source element found in ''{0}'': {1}",
											containers[i].getName(), element.toString() ), null ));
						}
						final List<Object> list = new ArrayList<Object>(1);
						list.add(element);
						return list;
					}
				}
				catch (final Exception e) {
					data.addStatus(new Status(IStatus.WARNING, RDebugCorePlugin.PLUGIN_ID, 0,
							NLS.bind("An error occurred when the finding source element ''{0}'' in ''{1}''.",
									(fileUri != null) ? fileUri.toString() : "<missing>", containers[i].getName() ),
							e ));
				}
			}
		}
		return null;
	}
	
	protected List<Object> findSourceElement(final IPath path, final LookupData data) {
		final ISourceContainer[] containers = getSourceContainers();
		final List<Object> elements = new ArrayList<Object>();
		for (int i = 0; i < containers.length; i++) {
			int j = 0;
			if (containers[i] instanceof IRSourceContainer) {
				try {
					((IRSourceContainer) containers[i]).findSourceElement(path, elements);
					if (DEBUG) {
						for (; j < elements.size(); j++) {
							data.addStatus(new Status(IStatus.INFO, RDebugCorePlugin.PLUGIN_ID, 0,
									NLS.bind("Source element found in ''{0}'': {1}",
											containers[i].getName(), elements.get(j).toString() ), null ));
						}
					}
				}
				catch (final Exception e) {
					data.addStatus(new Status(IStatus.WARNING, RDebugCorePlugin.PLUGIN_ID, 0,
							NLS.bind("An error occurred when finding the source element ''{0}'' in ''{1}''.",
									(path != null) ? path.toString() : "<missing>", containers[i].getName() ),
									e ));
				}
			}
		}
		if (elements.size() > 0) {
			return elements;
		}
		return null;
	}
	
	protected int checkPosition(final LookupData data, final RSourceLookupMatch match) {
		final IProgressMonitor monitor = new NullProgressMonitor();
		try {
			match.fCurrentContext = data.context;
			match.fQuality = -1;
			match.fCharStart = -1;
			match.fCharEnd = -1;
			match.fLineNumber = -1;
			
			ISourceUnit su = null;
			ISourceUnit fragmentSu = null;
			try {
				su = LTK.getSourceUnitManager().getSourceUnit(RModel.TYPE_ID,
						LTK.PERSISTENCE_CONTEXT, match.fSourceElement, true, monitor);
			} catch (final Exception e) {}
			{	final ISourceUnit editorSu = LTK.getSourceUnitManager().getSourceUnit(RModel.TYPE_ID,
					LTK.EDITOR_CONTEXT, (su != null) ? su : match.fSourceElement, true, monitor);
				if (editorSu != null) {
					su = editorSu;
				}
			}
			if (su != null) {
				try {
					final AbstractDocument suDocument = su.getDocument(monitor);
					synchronized ((suDocument instanceof ISynchronizable) ?
							((ISynchronizable) suDocument).getLockObject() : new Object() ) {
						final SourceCorrection corr = new SourceCorrection();
						AbstractDocument bDocument = null;
						
						final IRSrcref exprSrcref = (data.context.getExprSrcref() != null) ?
								RDbg.createStatetSrcref(data.context.getExprSrcref()) : null;
						
						if (data.context.getFileTimestamp() != 0 && equalTimestamp(
								RDbg.getTimestamp(su, monitor), data.context.getFileTimestamp() )) {
							match.fQuality = QUALITY_EXACT_FILE_TIMESTAMP;
							bDocument = suDocument;
							final IRSrcref sourceSrcref = RDbg.createStatetSrcref(data.context.getSourceSrcref());
							if (sourceSrcref != null) {
								corr.suExprLineOffset = corr.bExprLineOffset = sourceSrcref.getFirstLine();
								corr.firstLine = sourceSrcref.getFirstLine();
								corr.firstColumn = sourceSrcref.getFirstColumn();
							}
						}
						else {
							final IRSrcref firstSrcref = (data.context.getFirstSrcref() != null) ?
									RDbg.createStatetSrcref(data.context.getFirstSrcref()) : null;
							final IRSrcref lastSrcref = (data.context.getLastSrcref() != null) ?
									RDbg.createStatetSrcref(data.context.getLastSrcref()) : null;
							
							if (data.context.getSourceType() < 3) {
								final String suCode = suDocument.get();
								if (suCode.equals(data.context.getSourceCode())) {
									match.fQuality = QUALITY_EXACT_FILE_CONTENT;
									bDocument = suDocument;
								}
								else {
									bDocument = new Document(data.context.getSourceCode());
									int end = -1;
									if (firstSrcref != null && lastSrcref != null
											&& firstSrcref.hasBeginDetail()
											&& lastSrcref.hasEndDetail() ) {
										corr.firstLine = firstSrcref.getFirstLine();
										corr.firstColumn = firstSrcref.getFirstColumn();
										corr.bFirstLineCharOffset = bDocument.getLineOffset(corr.firstLine);
										corr.bFirstColumnCharOffset = fixPosition(corr.firstLine,
												firstSrcref.getFirstColumn(),
												bDocument, bDocument, null, 0 );
										end = fixPosition(lastSrcref.getLastLine(),
												lastSrcref.getLastColumn(),
												bDocument, bDocument, null, 1 );
									}
									else if (exprSrcref != null
											&& exprSrcref.hasBeginDetail()
											&& exprSrcref.hasEndDetail() ) {
										corr.firstLine = exprSrcref.getFirstLine();
										corr.firstColumn = exprSrcref.getFirstColumn();
										corr.bFirstLineCharOffset = bDocument.getLineOffset(corr.firstLine);
										corr.bFirstColumnCharOffset = fixPosition(corr.firstLine,
												exprSrcref.getFirstColumn(),
												bDocument, bDocument, null, 0 );
										end = fixPosition(exprSrcref.getLastLine(),
												exprSrcref.getLastColumn(),
												bDocument, bDocument, null, 1 );
									}
									
									if (end >= 0) {
										final String bCode = bDocument.get(corr.bFirstColumnCharOffset,
												end - corr.bFirstColumnCharOffset );
										match.fQuality = searchCode(data, corr,
												suDocument, suCode, bDocument, bCode);
									}
								}
							}
							else if (data.context.getSourceType() == 3) {
								createFragment(data);
								fragmentSu = LTK.getSourceUnitManager().getSourceUnit(RModel.TYPE_ID,
										LTK.EDITOR_CONTEXT, data.fragment, true, monitor);
								if (fragmentSu != null
										&& firstSrcref != null
										&& firstSrcref.hasBeginDetail() ) {
									final ISourceUnitModelInfo modelInfo = fragmentSu.getModelInfo(
											RModel.TYPE_ID, IModelManager.MODEL_FILE, monitor);
									final RAstNode body = findFBody(modelInfo);
									if (body != null) {
										bDocument = fragmentSu.getDocument(monitor);
										corr.firstLine = firstSrcref.getFirstLine();
										corr.firstColumn = firstSrcref.getFirstColumn();
										corr.bFirstLineCharOffset = bDocument.getLineOffset(corr.firstLine);
										corr.bFirstColumnCharOffset = body.getOffset();
										corr.bExprLineOffset = bDocument.getLineOfOffset(corr.bFirstColumnCharOffset) -
												corr.firstLine;
										final String bCode = bDocument.get(corr.bFirstColumnCharOffset,
												body.getLength());
										match.fQuality = searchCode(data, corr,
												suDocument, suDocument.get(), bDocument, bCode);
									}
								}
							}
						}
						
						if (exprSrcref != null) {
							match.fLineNumber = exprSrcref.getFirstLine()
									+ corr.suExprLineOffset;
							if (match.fQuality >= QUALITY_POSITION_FOUND
									&& exprSrcref.hasBeginDetail()
									&& exprSrcref.hasEndDetail() ) {
								match.fCharStart = fixPosition(exprSrcref.getFirstLine(),
										exprSrcref.getFirstColumn(),
										bDocument, suDocument, corr, 0 );
								match.fCharEnd = fixPosition(exprSrcref.getLastLine(),
										exprSrcref.getLastColumn(),
										bDocument, suDocument, corr, 1 );
							}
						}
						return match.fQuality;
					}
				}
				finally {
					su.disconnect(monitor);
					if (fragmentSu != null) {
						fragmentSu.disconnect(monitor);
					}
				}
			}
		}
		catch (final Exception e) {
			data.addStatus(new Status(IStatus.ERROR, RDebugCorePlugin.PLUGIN_ID, 0,
					"An error occurred in advanced position detection.", e));
		}
		return 0;
	}
	
	private int searchCode(final LookupData data, final SourceCorrection corr,
			final AbstractDocument suDocument, final String suCode,
			final AbstractDocument bDocument, String bCode) throws BadLocationException {
		// First match
		int offset = suCode.indexOf(bCode);
		if (offset < 0
				&& bDocument.getDefaultLineDelimiter() != suDocument.getDefaultLineDelimiter() ) {
			bCode = bCode.replace(bDocument.getDefaultLineDelimiter(), suDocument.getDefaultLineDelimiter());
			offset = suCode.indexOf(bCode);
		}
		if (offset < 0) {
			return 0;
		}
		
		int firstLine = corr.firstLine;
		if (data.context.getSourceSrcref() != null
				&& data.context.getSourceSrcref()[Srcref.BEGIN_LINE] > 0) {
			firstLine += data.context.getSourceSrcref()[Srcref.BEGIN_LINE] - 1;
//			firstColumn += data.context.sourceSrcref.getBeginColumn();
		}
		// Prefer correct line start, if available
		if (offset >= 0
				&& offset < suDocument.getLength() && firstLine < suDocument.getNumberOfLines() ) {
			final IRegion line = suDocument.getLineInformation(firstLine);
			if (offset < line.getOffset()) {
				final int offset2 = suCode.indexOf(bCode, line.getOffset());
				if (offset2 >= 0 && offset2 < line.getOffset()+line.getLength()) {
					offset = offset2;
				}
			}
		}
		
		if (offset >= 0) {
			final int suLine = suDocument.getLineOfOffset(offset);
			corr.suExprLineOffset = suLine - corr.firstLine;
			corr.suFirstLineCharOffset = suDocument.getLineOffset(suLine);
			corr.suFirstColumnCharOffset = offset;
			return QUALITY_EXACT_FUNCTION_CONTENT;
		}
		return 0;
	}
	
	private Block findFBody(final ISourceUnitModelInfo modelInfo) {
		if (modelInfo instanceof IRModelInfo && modelInfo.getSourceElement() != null) {
			FDef fDef = null;
			final List<? extends IModelElement> children = modelInfo.getSourceElement().getSourceChildren(null);
			if (children.size() == 1) {
				final IModelElement modelElement = children.get(0);
				if (modelElement instanceof IRMethod) {
					fDef = (FDef) modelElement.getAdapter(FDef.class);
				}
			}
			if (fDef == null && (modelInfo.getAst().root instanceof RAstNode)) {
				final RAstNode node = ((RAstNode) modelInfo.getAst().root).getChild(0);
				if (node.getNodeType() == NodeType.F_DEF) {
					fDef = (FDef) node;
				}
				if (fDef == null) {
					final AssignExpr assign = RAst.checkAssign(node);
					if (assign != null && assign.valueNode != null
							&& assign.valueNode.getNodeType() == NodeType.F_DEF) {
						fDef = (FDef) assign.valueNode;
					}
				}
			}
			if (fDef != null) {
				final RAstNode body = fDef.getContChild();
				if (body.getNodeType() == NodeType.BLOCK) {
					return (Block) body;
				}
			}
		}
		return null;
	}
	
	private int fixPosition(final int line, final int column, final AbstractDocument bDocument,
			final AbstractDocument suDocument, final SourceCorrection corr, final int shift)
			throws BadLocationException {
		int pos = 0;
		{	int bLine = line;
			if (corr != null) {
				bLine += corr.bExprLineOffset;
			}
			final IRegion lineInfo = bDocument.getLineInformation(bLine);
			
			int currentColumn = 0;
			if (corr != null && line == corr.firstLine) {
				currentColumn += corr.firstColumn;
				if (currentColumn > column) {
					return -1;
				}
				pos += corr.bFirstColumnCharOffset - lineInfo.getOffset(); // lineInfo.getOffset == corr.bFirstLineCharOffset
			}
			while (currentColumn < column && pos < lineInfo.getLength() ) {
				final char c = bDocument.getChar(lineInfo.getOffset() + pos++);
				if (c == '\t') {
					currentColumn += 8 - (currentColumn % 8);
				}
				else {
					currentColumn++;
				}
			}
		}
		
		{	int suLine = line;
			if (corr != null) {
				suLine += corr.suExprLineOffset;
			}
			final IRegion lineInfo = suDocument.getLineInformation(suLine);
			
			if (corr != null && line == corr.firstLine) {
				pos += (corr.suFirstColumnCharOffset - corr.suFirstLineCharOffset) - (corr.bFirstColumnCharOffset - corr.bFirstLineCharOffset);
			}
			pos += shift;
			return lineInfo.getOffset() + Math.min(pos, lineInfo.getLength());
		}
	}
	
}
