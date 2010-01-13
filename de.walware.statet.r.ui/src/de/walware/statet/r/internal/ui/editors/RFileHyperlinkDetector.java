/*******************************************************************************
 * Copyright (c) 2009-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.editors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlink;

import de.walware.ecommons.FileUtil;
import de.walware.ecommons.ltk.ISourceUnit;
import de.walware.ecommons.ui.text.OpenFileHyperlink;
import de.walware.ecommons.ui.text.sourceediting.ISourceEditor;

import de.walware.statet.r.core.RProject;
import de.walware.statet.r.core.rsource.IRDocumentPartitions;


public class RFileHyperlinkDetector extends AbstractHyperlinkDetector {
	
	
	public RFileHyperlinkDetector() {
	}
	
	
	public IHyperlink[] detectHyperlinks(final ITextViewer textViewer,
			final IRegion region, final boolean canShowMultipleHyperlinks) {
		try {
			final List<IHyperlink> hyperlinks = new ArrayList<IHyperlink>();
			final IDocument document = textViewer.getDocument();
			int start = 0;
			final ITypedRegion partition = TextUtilities.getPartition(document, IRDocumentPartitions.R_PARTITIONING, region.getOffset(), false);
			if (partition != null && partition.getLength() > 3) {
				String string = null;
				if (partition.getType().equals(IRDocumentPartitions.R_COMMENT) || partition.getType().equals(IRDocumentPartitions.R_ROXYGEN)) {
					start = region.getOffset();
					final int bound = partition.getOffset()+1;
					{	final char c = document.getChar(start-1);
						if (c <= 34 || c == '>' || c == '<') {
							return null;
						}
					}
					while (start > bound) {
						final char c = document.getChar(start-1);
						if (c <= 34 || c == '>' || c == '<') {
							break;
						}
						start--;
					}
					string = document.get(start, partition.getOffset()+partition.getLength()-start).trim();
				}
				else if (partition.getType().equals(IRDocumentPartitions.R_STRING)) {
					start = partition.getOffset()+1;
					int stop = 0;
					stop = partition.getOffset()+partition.getLength();
					if (document.getChar(stop-1) == document.getChar(partition.getOffset())) {
						stop--;
					}
					string = document.get(start, stop-start);
				}
				
				if (string == null || string.length() == 0) {
					return null;
				}
				IContainer relativeBase = null;
				
				final Object adapter = getAdapter(ISourceEditor.class);
				if (adapter instanceof ISourceEditor) {
					final ISourceUnit su = ((ISourceEditor) adapter).getSourceUnit();
					if (su != null) {
						final IResource resource = su.getResource();
						if (resource != null) {
							final IProject project = resource.getProject();
							if (project != null) {
								final RProject rProject = RProject.getRProject(project);
								if (rProject != null) {
									relativeBase = rProject.getBaseContainer();
								}
							}
							if (relativeBase == null) {
								relativeBase = resource.getParent();
							}
						}
					}
				}
				final IFileStore store = FileUtil.getLocalFileStore(string, relativeBase);
				if (store != null) {
					final IFileInfo info = store.fetchInfo();
					if (info.exists() && !store.fetchInfo().isDirectory()) {
						hyperlinks.add(new OpenFileHyperlink(new Region(start, string.length()), store));
					}
				}
			}
			if (!hyperlinks.isEmpty()) {
				return hyperlinks.toArray(new IHyperlink[hyperlinks.size()]);
			}
		}
		catch (final BadLocationException e) {}
		catch (final CoreException e) {}
		return null;
	}
	
}
