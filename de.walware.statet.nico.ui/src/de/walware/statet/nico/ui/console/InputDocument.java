/*******************************************************************************
 * Copyright (c) 2005-2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.ui.console;

import java.util.ArrayList;
import java.util.regex.Pattern;

import org.eclipse.jface.text.AbstractDocument;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPartitioningException;
import org.eclipse.jface.text.CopyOnWriteTextStore;
import org.eclipse.jface.text.DefaultLineTracker;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.GapTextStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.IDocumentPartitionerExtension2;
import org.eclipse.jface.text.IDocumentPartitioningListener;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.TypedRegion;

import de.walware.statet.nico.internal.ui.Messages;
import de.walware.statet.nico.internal.ui.NicoUIPlugin;


public class InputDocument extends AbstractDocument {

	
	private static Pattern gLineSeparatorPattern = Pattern.compile("\\r[\\n]?|\\n"); //$NON-NLS-1$

	
	private class PartitionerMapper implements IDocumentPartitioner {

		private IDocumentPartitioner fMasterPartitioner;
		
		public PartitionerMapper(IDocumentPartitioner partitioner) {
			
			fMasterPartitioner = partitioner;
		}
		
		public void connect(IDocument document) {
			
			throw new UnsupportedOperationException();
		}

		public void disconnect() {
			
			throw new UnsupportedOperationException();
		}

		public void documentAboutToBeChanged(DocumentEvent event) {
			
			throw new UnsupportedOperationException();
		}

		public boolean documentChanged(DocumentEvent event) {
			
			throw new UnsupportedOperationException();
		}


		public ITypedRegion[] computePartitioning(int offset, int length) {
			
			return remap(fMasterPartitioner.computePartitioning(fOffsetInMaster+offset, length));
		}

		public String getContentType(int offset) {
			
			return fMasterPartitioner.getContentType(fOffsetInMaster+offset);
		}

		public String[] getLegalContentTypes() {
			
			return fMasterPartitioner.getLegalContentTypes();
		}

		public ITypedRegion getPartition(int offset) {
			
			return remap(fMasterPartitioner.getPartition(fOffsetInMaster+offset));
		}		
	}
	
	private class PartitionerMapper2 extends PartitionerMapper implements IDocumentPartitionerExtension2 {

		
		private IDocumentPartitionerExtension2 fMasterPartitioner2;

		public PartitionerMapper2(IDocumentPartitioner partitioner) {
			
			super(partitioner);
			fMasterPartitioner2 = (IDocumentPartitionerExtension2) partitioner;
		}
		
		public ITypedRegion[] computePartitioning(int offset, int length, boolean includeZeroLengthPartitions) {
			
			return fMasterPartitioner2.computePartitioning(fOffsetInMaster+offset, length, includeZeroLengthPartitions);
		}

		public String getContentType(int offset, boolean preferOpenPartitions) {
			
			return fMasterPartitioner2.getContentType(fOffsetInMaster+offset, preferOpenPartitions);
		}

		public String[] getManagingPositionCategories() {
			
			return fMasterPartitioner2.getManagingPositionCategories();
		}

		public ITypedRegion getPartition(int offset, boolean preferOpenPartitions) {
			
			return fMasterPartitioner2.getPartition(fOffsetInMaster+offset, preferOpenPartitions);
		}
		
	}
	
	
	private Document fMaster;
	private int fOffsetInMaster = 0;
	private boolean fIgnoreMasterChanges = false;
	
	
	InputDocument() {
		
		fMaster = new Document();
		setTextStore(new CopyOnWriteTextStore(new GapTextStore(64, 256, 0.1f)));
		setLineTracker(new DefaultLineTracker());
		completeInitialization();
		
		addDocumentListener(new IDocumentListener() {
			public void documentAboutToBeChanged(DocumentEvent event) {
			}
			public void documentChanged(DocumentEvent event) {
				try {
					fIgnoreMasterChanges = true;
					fMaster.replace(fOffsetInMaster+event.fOffset, event.fLength, event.fText);
					fIgnoreMasterChanges = false;
				} catch (BadLocationException e) {
					NicoUIPlugin.logError(NicoUIPlugin.INTERNAL_ERROR, Messages.Console_error_UnexpectedException_message, e);
				}
			}
		});
		fMaster.addDocumentListener(new IDocumentListener() {
			public void documentAboutToBeChanged(DocumentEvent event) {
				if (!fIgnoreMasterChanges) {
					fOffsetInMaster = fOffsetInMaster - event.fLength + event.fText.length();
				}
			}
			public void documentChanged(DocumentEvent event) {
			}
		});
		fMaster.addDocumentPartitioningListener(new IDocumentPartitioningListener() {
			@SuppressWarnings("deprecation")
			public void documentPartitioningChanged(IDocument document) {
				fireDocumentPartitioningChanged(); // the new methods seems not work for us
			}
		});
	}
	
	
	public void setPrefix(String text) {
		
		try {
			fMaster.replace(0, fOffsetInMaster, text);
		} catch (BadLocationException e) {
			NicoUIPlugin.logError(NicoUIPlugin.INTERNAL_ERROR, Messages.Console_error_UnexpectedException_message, e);
		}
	}
	
	public IDocument getMasterDocument() {
		
		return fMaster;
	}
	
	
	@Override
	public void set(String text) {
		
		super.set(checkText(text));
	}
	
	@Override
	public void set(String text, long modificationStamp) {

		super.set(checkText(text), modificationStamp);
	}
	
	@Override
	public void replace(int offset, int length, String text, long modificationStamp) throws BadLocationException {
	
		super.replace(offset, length, checkText(text), modificationStamp);
	}
	
	@Override
	public void replace(int offset, int length, String text) throws BadLocationException {
		
		super.replace(offset, length, checkText(text));
	}
	
	protected String checkText(String text) {
		
		return gLineSeparatorPattern.matcher(text).replaceAll(""); //$NON-NLS-1$
	}
	

	@Override
	public void setInitialLineDelimiter(String lineDelimiter) {
		
		fMaster.setInitialLineDelimiter(lineDelimiter);
		super.setInitialLineDelimiter(lineDelimiter);
	}
	

	@Override
	public void setDocumentPartitioner(IDocumentPartitioner partitioner) {
		
		fMaster.setDocumentPartitioner(partitioner);
	}
	
	@Override
	public void setDocumentPartitioner(String partitioning, IDocumentPartitioner partitioner) {
		
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
	public IDocumentPartitioner getDocumentPartitioner(String partitioning) {
		
		IDocumentPartitioner masterPartitioner = fMaster.getDocumentPartitioner(partitioning);
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
	public String[] getLegalContentTypes(String partitioning) throws BadPartitioningException {

		return fMaster.getLegalContentTypes(partitioning);
	}
	
	@Override
	public ITypedRegion[] computePartitioning(int offset, int length) throws BadLocationException {
		
		return remap(fMaster.computePartitioning(fOffsetInMaster+offset, length));
	}

	@Override
	public ITypedRegion[] computePartitioning(String partitioning, int offset, int length, boolean includeZeroLengthPartitions) throws BadLocationException, BadPartitioningException {
		
		return remap(fMaster.computePartitioning(partitioning, fOffsetInMaster+offset, length, includeZeroLengthPartitions));
	}
	
	@Override
	public ITypedRegion getPartition(int offset) throws BadLocationException {
		
		return remap(fMaster.getPartition(fOffsetInMaster+offset));
	}
	
	@Override
	public ITypedRegion getPartition(String partitioning, int offset, boolean preferOpenPartitions) throws BadLocationException, BadPartitioningException {
		
		return remap(fMaster.getPartition(partitioning, fOffsetInMaster+offset, preferOpenPartitions));
	}

	@Override
	public String getContentType(int offset) throws BadLocationException {
		
		return fMaster.getContentType(fOffsetInMaster+offset);
	}

	@Override
	public String getContentType(String partitioning, int offset, boolean preferOpenPartitions) throws BadLocationException, BadPartitioningException {
		
		return fMaster.getContentType(partitioning, fOffsetInMaster+offset, preferOpenPartitions);
	}
	

	private ITypedRegion[] remap(ITypedRegion[] masterRegions) {
		
		ArrayList<IRegion> regions = new ArrayList<IRegion>(masterRegions.length);
		for (ITypedRegion masterRegion : masterRegions) {
			ITypedRegion region = remap(masterRegion);
			if (region != null) {
				regions.add(region);
			}
		}
		return regions.toArray(new ITypedRegion[regions.size()]);
	}
	
	private ITypedRegion remap(ITypedRegion masterRegion) {
		
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
		Exception e = new BadPartitioningException("Could not map typed region.");
		NicoUIPlugin.logError(NicoUIPlugin.INTERNAL_ERROR, Messages.Console_error_UnexpectedException_message, e);
//		System.out.println("partition mapping: failed");
		return null;
	}
}
