/*******************************************************************************
 * Copyright (c) 2011-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.sweave.editors;

import org.eclipse.jface.text.AbstractDocument;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPartitioningException;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.information.IInformationProvider;
import org.eclipse.jface.text.information.IInformationProviderExtension;
import org.eclipse.jface.text.information.IInformationProviderExtension2;

import de.walware.ecommons.ltk.ui.sourceediting.EditorInformationProvider;

import de.walware.statet.r.sweave.text.Rweave;


public class LtxRweaveInformationProvider implements
		IInformationProvider, IInformationProviderExtension, IInformationProviderExtension2 {
	
	
	private final EditorInformationProvider fRProvider;
	
	private EditorInformationProvider fActiveProvider;
	
	
	public LtxRweaveInformationProvider(final EditorInformationProvider rProvider) {
		fRProvider = rProvider;
	}
	
	
	@Override
	public IRegion getSubject(final ITextViewer textViewer, final int offset) {
		fActiveProvider = null;
		try {
			final ITypedRegion partition = ((AbstractDocument) textViewer.getDocument()).getPartition(
					Rweave.LTX_R_PARTITIONING, offset, true );
			if (Rweave.R_PARTITION_CONSTRAINT.matches(partition.getType())) {
				fActiveProvider = fRProvider;
				return fRProvider.getSubject(textViewer, offset);
			}
		}
		catch (final BadLocationException e) {}
		catch (final BadPartitioningException e) {}
		return null;
	}
	
	@Override
	public String getInformation(final ITextViewer textViewer, final IRegion subject) {
		return null;
	}
	
	@Override
	public Object getInformation2(final ITextViewer textViewer, final IRegion subject) {
		if (fActiveProvider == null) {
			return null;
		}
		return fActiveProvider.getInformation2(textViewer, subject);
	}
	
	@Override
	public IInformationControlCreator getInformationPresenterControlCreator() {
		if (fActiveProvider == null) {
			return null;
		}
		return fActiveProvider.getInformationPresenterControlCreator();
	}
	
}
