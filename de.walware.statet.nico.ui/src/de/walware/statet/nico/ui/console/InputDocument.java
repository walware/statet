/*******************************************************************************
 * Copyright (c) 2005-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.ui.console;

import java.util.ArrayList;
import java.util.regex.Pattern;

import org.eclipse.jface.text.AbstractDocument;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPartitioningException;
import org.eclipse.jface.text.DefaultLineTracker;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.GapTextStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.IDocumentPartitionerExtension2;
import org.eclipse.jface.text.IDocumentPartitioningListener;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TypedRegion;

import de.walware.statet.nico.internal.ui.Messages;
import de.walware.statet.nico.internal.ui.NicoUIPlugin;


public class InputDocument extends AbstractDocument {
	
	
	private static Pattern gLineSeparatorPattern = Pattern.compile("\\r[\\n]?|\\n"); //$NON-NLS-1$
	
	
	private static class MasterDocument extends AbstractDocument {
		
		
		public MasterDocument() {
			setTextStore(new GapTextStore(128, 512, 0.1f));
			setLineTracker(new DefaultLineTracker());
			completeInitialization();
		}
		
	}
	
	
	private class PartitionerMapper implements IDocumentPartitioner {
		
		private IDocumentPartitioner fMasterPartitioner;
		
		public PartitionerMapper(final IDocumentPartitioner partitioner) {
			fMasterPartitioner = partitioner;
		}
		
		public void connect(final IDocument document) {
			throw new UnsupportedOperationException();
		}
		
		public void disconnect() {
			throw new UnsupportedOperationException();
		}
		
		public void documentAboutToBeChanged(final DocumentEvent event) {
			throw new UnsupportedOperationException();
		}
		
		public boolean documentChanged(final DocumentEvent event) {
			throw new UnsupportedOperationException();
		}
		
		public ITypedRegion[] computePartitioning(final int offset, final int length) {
			return remap(fMasterPartitioner.computePartitioning(fOffsetInMaster+offset, length));
		}
		
		public String getContentType(final int offset) {
			return fMasterPartitioner.getContentType(fOffsetInMaster+offset);
		}
		
		public String[] getLegalContentTypes() {
			return fMasterPartitioner.getLegalContentTypes();
		}
		
		public ITypedRegion getPartition(final int offset) {
			return remap(fMasterPartitioner.getPartition(fOffsetInMaster+offset));
		}
	}
	
	private class PartitionerMapper2 extends PartitionerMapper implements IDocumentPartitionerExtension2 {
		
		private IDocumentPartitionerExtension2 fMasterPartitioner2;
		
		public PartitionerMapper2(final IDocumentPartitioner partitioner) {
			super(partitioner);
			fMasterPartitioner2 = (IDocumentPartitionerExtension2) partitioner;
		}
		
		public ITypedRegion[] computePartitioning(final int offset, final int length, final boolean includeZeroLengthPartitions) {
			return fMasterPartitioner2.computePartitioning(fOffsetInMaster+offset, length, includeZeroLengthPartitions);
		}
		
		public String getContentType(final int offset, final boolean preferOpenPartitions) {
			return fMasterPartitioner2.getContentType(fOffsetInMaster+offset, preferOpenPartitions);
		}
		
		public String[] getManagingPositionCategories() {
			return fMasterPartitioner2.getManagingPositionCategories();
		}
		
		public ITypedRegion getPartition(final int offset, final boolean preferOpenPartitions) {
			return fMasterPartitioner2.getPartition(fOffsetInMaster+offset, preferOpenPartitions);
		}
		
	}
	
	
	private final MasterDocument fMaster;
	private int fOffsetInMaster = 0;
	private boolean fIgnoreMasterChanges = false;
	
	
	InputDocument() {
		fMaster = new MasterDocument();
		setTextStore(new GapTextStore(128, 512, 0.1f));
		setLineTracker(new DefaultLineTracker());
		completeInitialization();
		
		addDocumentListener(new IDocumentListener() {
			public void documentAboutToBeChanged(final DocumentEvent event) {
			}
			public void documentChanged(final DocumentEvent event) {
				try {
					fIgnoreMasterChanges = true;
					fMaster.replace(fOffsetInMaster+event.fOffset, event.fLength, event.fText);
					fIgnoreMasterChanges = false;
				} catch (final BadLocationException e) {
					NicoUIPlugin.logError(NicoUIPlugin.INTERNAL_ERROR, Messages.Console_error_UnexpectedException_message, e);
				}
			}
		});
		fMaster.addDocumentListener(new IDocumentListener() {
			public void documentAboutToBeChanged(final DocumentEvent event) {
				if (!fIgnoreMasterChanges) {
					fOffsetInMaster = fOffsetInMaster - event.fLength + event.fText.length();
				}
			}
			public void documentChanged(final DocumentEvent event) {
			}
		});
		fMaster.addDocumentPartitioningListener(new IDocumentPartitioningListener() {
			@SuppressWarnings("deprecation")
			public void documentPartitioningChanged(final IDocument document) {
				fireDocumentPartitioningChanged(new Region(0, getLength()));
			}
		});
	}
	
	
	public void setPrefix(final String text) {
		try {
			fMaster.replace(0, fOffsetInMaster, text);
		} catch (final BadLocationException e) {
			NicoUIPlugin.logError(NicoUIPlugin.INTERNAL_ERROR, Messages.Console_error_UnexpectedException_message, e);
		}
	}
	
	public IDocument getMasterDocument() {
		return fMaster;
	}
	
	public int getOffsetInMasterDocument() {
		return fOffsetInMaster;
	}
	
	
	@Override
	public void set(final String text) {
		super.set(checkText(text));
	}
	
	@Override
	public void set(final String text, final long modificationStamp) {
		super.set(checkText(text), modificationStamp);
	}
	
	@Override
	public void replace(final int offset, final int length, final String text, final long modificationStamp) throws BadLocationException {
		super.replace(offset, length, checkText(text), modificationStamp);
	}
	
// Method above is called in super
//	@Override
//	public void replace(final int offset, final int length, final String text) throws BadLocationException {
//		super.replace(offset, length, checkText(text));
//	}
	
	protected String checkText(final String text) {
		return gLineSeparatorPattern.matcher(text).replaceAll(""); //$NON-NLS-1$
	}
	
	
	@Override
	public void setInitialLineDelimiter(final String lineDelimiter) {
		fMaster.setInitialLineDelimiter(lineDelimiter);
		super.setInitialLineDelimiter(lineDelimiter);
	}
	
	
	@Override
	public void setDocumentPartitioner(final IDocumentPartitioner partitioner) {
		fMaster.setDocumentPartitioner(partitioner);
	}
	
	@Override
	public void setDocumentPartitioner(final String partitioning, final IDocumentPartitioner partitioner) {
		fMaster.setDocumentPartitioner(partitioning, partitioner);
	}
	
	@Override
	public String[] getPartitionings() {
		return fMaster.getPartitionings();
	}
	
	@Override
	public IDocumentPartitioner getDocumentPartitioner() {
		return getDocumentPartitioner(DEFAULT_PARTITIONING);
	}
	
	@Override
	public IDocumentPartitioner getDocumentPartitioner(final String partitioning) {
		final IDocumentPartitioner masterPartitioner = fMaster.getDocumentPartitioner(partitioning);
		if (masterPartitioner instanceof IDocumentPartitionerExtension2) {
			return new PartitionerMapper2(masterPartitioner);
		}
		else {
			return new PartitionerMapper(masterPartitioner);
		}
	}
	
	@Override
	public String[] getLegalContentTypes() {
		return fMaster.getLegalContentTypes();
	}
	
	@Override
	public String[] getLegalContentTypes(final String partitioning) throws BadPartitioningException {
		return fMaster.getLegalContentTypes(partitioning);
	}
	
	@Override
	public ITypedRegion[] computePartitioning(final int offset, final int length) throws BadLocationException {
		return remap(fMaster.computePartitioning(fOffsetInMaster+offset, length));
	}
	
	@Override
	public ITypedRegion[] computePartitioning(final String partitioning, final int offset, final int length, final boolean includeZeroLengthPartitions) throws BadLocationException, BadPartitioningException {
		return remap(fMaster.computePartitioning(partitioning, fOffsetInMaster+offset, length, includeZeroLengthPartitions));
	}
	
	@Override
	public ITypedRegion getPartition(final int offset) throws BadLocationException {
		return remap(fMaster.getPartition(fOffsetInMaster+offset));
	}
	
	@Override
	public ITypedRegion getPartition(final String partitioning, final int offset, final boolean preferOpenPartitions) throws BadLocationException, BadPartitioningException {
		return remap(fMaster.getPartition(partitioning, fOffsetInMaster+offset, preferOpenPartitions));
	}
	
	@Override
	public String getContentType(final int offset) throws BadLocationException {
		return fMaster.getContentType(fOffsetInMaster+offset);
	}
	
	@Override
	public String getContentType(final String partitioning, final int offset, final boolean preferOpenPartitions) throws BadLocationException, BadPartitioningException {
		return fMaster.getContentType(partitioning, fOffsetInMaster+offset, preferOpenPartitions);
	}
	
	
	private ITypedRegion[] remap(final ITypedRegion[] masterRegions) {
		final ArrayList<IRegion> regions = new ArrayList<IRegion>(masterRegions.length);
		for (final ITypedRegion masterRegion : masterRegions) {
			final ITypedRegion region = remap(masterRegion);
			if (region != null) {
				regions.add(region);
			}
		}
		return regions.toArray(new ITypedRegion[regions.size()]);
	}
	
	private ITypedRegion remap(final ITypedRegion masterRegion) {
		int offset = masterRegion.getOffset()-fOffsetInMaster;
		int length = masterRegion.getLength();
		if (offset+length >= 0) {
			if (offset < 0) {
				length = length+offset;
				offset = 0;
			}
//			System.out.println("partitiong mapping: ("+masterRegion.getOffset()+", "+masterRegion.getLength()+") -["+fOffsetInMaster+"]-> ("+offset+", "+length+")");
			return new TypedRegion(offset, length, masterRegion.getType());
		}
		final Exception e = new BadPartitioningException("Could not map typed region."); //$NON-NLS-1$
		NicoUIPlugin.logError(NicoUIPlugin.INTERNAL_ERROR, Messages.Console_error_UnexpectedException_message, e);
//		System.out.println("partition mapping: failed");
		return null;
	}
	
}
