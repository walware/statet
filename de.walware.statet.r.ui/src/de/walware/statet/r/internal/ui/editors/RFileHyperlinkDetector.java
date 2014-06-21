/*=============================================================================#
 # Copyright (c) 2009-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

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

import de.walware.ecommons.io.FileUtil;
import de.walware.ecommons.ltk.core.model.ISourceUnit;
import de.walware.ecommons.ltk.core.model.IWorkspaceSourceUnit;
import de.walware.ecommons.ltk.ui.sourceediting.ISourceEditor;
import de.walware.ecommons.text.ui.OpenFileHyperlink;

import de.walware.statet.r.core.IRProject;
import de.walware.statet.r.core.RProjects;
import de.walware.statet.r.core.rsource.IRDocumentPartitions;


public class RFileHyperlinkDetector extends AbstractHyperlinkDetector {
	
	
	public RFileHyperlinkDetector() {
	}
	
	
	@Override
	public IHyperlink[] detectHyperlinks(final ITextViewer textViewer, final IRegion region,
			final boolean canShowMultipleHyperlinks) {
		try {
			final ISourceEditor editor= (ISourceEditor) getAdapter(ISourceEditor.class);
			if (editor == null) {
				return null;
			}
			
			final List<IHyperlink> hyperlinks= new ArrayList<>(4);
			final IDocument document= textViewer.getDocument();
			int start= 0;
			int stop= 0;
			
			final ITypedRegion partition= TextUtilities.getPartition(document,
					editor.getPartitioning().getPartitioning(), region.getOffset(), false );
			if (partition != null && partition.getLength() > 3) {
				if (partition.getType().equals(IRDocumentPartitions.R_COMMENT)
						|| partition.getType().equals(IRDocumentPartitions.R_ROXYGEN) ) {
					boolean quote= false;
					start= region.getOffset();
					{	final int bound= partition.getOffset()+1;
						{	final char c= document.getChar(start);
							if (c <= 0x22 || c == '>' || c == '<') {
								return null;
							}
						}
						while (start > bound) {
							final char c= document.getChar(start-1);
							if (c <= 0x22 || c == '>' || c == '<') {
								if (c == '"') {
									quote= true;
								}
								break;
							}
							start--;
						}
					}
					{	final int bound= partition.getOffset()+partition.getLength();
						stop= region.getOffset()+1;
						while (stop < bound) {
							final char c= document.getChar(stop);
							if (c <= 0x22 || c == '>' || c == '<') {
								if (quote || c != '"') {
									break;
								}
							}
							stop++;
						}
					}
				}
				else if (partition.getType().equals(IRDocumentPartitions.R_STRING)) {
					start= partition.getOffset()+1;
					stop= partition.getOffset()+partition.getLength();
					if (document.getChar(stop-1) == document.getChar(partition.getOffset())) {
						stop--;
					}
				}
				
				if (start >= stop) {
					return null;
				}
				IContainer relativeBase= null;
				
				final ISourceUnit su= editor.getSourceUnit();
				if (su instanceof IWorkspaceSourceUnit) {
					final IResource resource= ((IWorkspaceSourceUnit) su).getResource();
					final IProject project= resource.getProject();
					if (project != null) {
						final IRProject rProject= RProjects.getRProject(project);
						if (rProject != null) {
							relativeBase= rProject.getBaseContainer();
						}
					}
					if (relativeBase == null) {
						relativeBase= resource.getParent();
					}
				}
				final IFileStore store= FileUtil.getLocalFileStore(
						document.get(start, stop-start), relativeBase );
				if (store != null) {
					final IFileInfo info= store.fetchInfo();
					if (info.exists() && !store.fetchInfo().isDirectory()) {
						hyperlinks.add(new OpenFileHyperlink(new Region(start, stop-start), store));
					}
				}
			}
			if (!hyperlinks.isEmpty()) {
				return hyperlinks.toArray(new IHyperlink[hyperlinks.size()]);
			}
		}
		catch (final BadLocationException | CoreException e) {}
		return null;
	}
	
}
