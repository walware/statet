/*******************************************************************************
 * Copyright (c) 2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.editors;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension2;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.jface.text.link.LinkedModeUI;
import org.eclipse.jface.text.link.LinkedPosition;
import org.eclipse.jface.text.link.LinkedPositionGroup;
import org.eclipse.jface.text.link.LinkedModeUI.ExitFlags;
import org.eclipse.jface.text.link.LinkedModeUI.IExitPolicy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.texteditor.link.EditorLinkedModeUI;

import de.walware.statet.base.ui.StatetImages;
import de.walware.statet.base.ui.sourceeditors.ExtTextInvocationContext;
import de.walware.statet.base.ui.sourceeditors.StatextEditor1;
import de.walware.statet.r.core.rmodel.IElementAccess;
import de.walware.statet.r.core.rsource.ast.RAst;
import de.walware.statet.r.internal.ui.RUIMessages;
import de.walware.statet.r.internal.ui.RUIPlugin;


public class LinkedNamesAssistProposal implements ICompletionProposal, ICompletionProposalExtension2 {
	
	/**
	 * An exit policy that skips Backspace and Delete at the beginning and at the end
	 * of a linked position, respectively.
	 */
	public static class DeleteBlockingExitPolicy implements IExitPolicy {
		
		private IDocument fDocument;
		
		public DeleteBlockingExitPolicy(final IDocument document) {
			fDocument = document;
		}
		
		public ExitFlags doExit(final LinkedModeModel model, final VerifyEvent event,
				final int offset, final int length) {
			switch (event.character) {
			case SWT.BS:
				{	//skip backspace at beginning of linked position
					final LinkedPosition position = model.findPosition(new LinkedPosition(
							fDocument, offset, 0, LinkedPositionGroup.NO_STOP));
					if (position != null && offset <= position.getOffset() && length == 0) {
						event.doit = false;
					}
					return null;
				}
			case SWT.DEL:
				{	//skip delete at end of linked position
					final LinkedPosition position = model.findPosition(new LinkedPosition(
							fDocument, offset, 0, LinkedPositionGroup.NO_STOP));
					if (position != null && offset >= position.getOffset()+position.getLength() && length == 0) {
						event.doit = false;
					}
					return null;
				}
			}
			return null;
		}
	}
	
	
	private ExtTextInvocationContext fContext;
	private IElementAccess fAccess;
	private String fLabel;
	private String fDescription;
	private String fValueSuggestion;
	private int fRelevance;
	
	
	private LinkedNamesAssistProposal(final String label, final String description,
			final ExtTextInvocationContext invocationContext, final IElementAccess access, final String valueSuggestion) {
		fLabel = label;
		fDescription = description;
		fContext = invocationContext;
		fAccess = access;
		fValueSuggestion = valueSuggestion;
		fRelevance = 8;
	}
	
	public LinkedNamesAssistProposal(final ExtTextInvocationContext invocationContext, final IElementAccess access) {
		this(RUIMessages.Proposal_RenameInFile_label, RUIMessages.Proposal_RenameInFile_description,
				invocationContext, access, null);
	}
	
	
	public void apply(final ITextViewer viewer, final char trigger, final int stateMask, final int offset) {
		try {
			Point seletion = viewer.getSelectedRange();
			
			final IDocument document = viewer.getDocument();
			final LinkedPositionGroup group = new LinkedPositionGroup();
			final IElementAccess[] allInUnit = fAccess.getAllInUnit();
			Arrays.sort(allInUnit, IElementAccess.NAME_POSITION_COMPARATOR);
			final List<IElementAccess> all = Arrays.asList(allInUnit);
			final int currentIdx = all.indexOf(fAccess);
			if (currentIdx < 0) {
				return;
			}
			final int count = all.size();
			int idx = 0;
			ITER_ELEMENTS : for (int i = currentIdx; ; ) {
				final Position position = RAst.getElementNamePosition(all.get(i).getNameNode());
				if (position != null) {
					group.addPosition(new LinkedPosition(document, position.getOffset(), position.getLength(), idx++));
				}
				i++;
				if (i == count) {
					i = 0;
				}
				if (i == currentIdx) {
					break ITER_ELEMENTS;
				}
			}
			
			final LinkedModeModel model = new LinkedModeModel();
			model.addGroup(group);
			model.forceInstall();
			final StatextEditor1 editor = fContext.getEditor();
			if (editor != null) {
				editor.getEffectSynchronizer().install(model);
			}
			
			final LinkedModeUI ui = new EditorLinkedModeUI(model, viewer);
			ui.setExitPolicy(new DeleteBlockingExitPolicy(document));
			ui.setExitPosition(viewer, offset, 0, LinkedPositionGroup.NO_STOP);
			ui.enter();
			
			if (fValueSuggestion != null) {
				final Position position = RAst.getElementNamePosition(fAccess.getNameNode());
				document.replace(position.getOffset(), position.getLength(), fValueSuggestion);
				seletion = new Point(position.getOffset(), fValueSuggestion.length());
			}
			
			viewer.setSelectedRange(seletion.x, seletion.y); // by default full word is selected, restore original selection
		} catch (final BadLocationException e) {
			RUIPlugin.logError(-1, "Error initializing linked rename.", e); //$NON-NLS-1$
		}
	}
	
	public void apply(final IDocument document) {
		throw new UnsupportedOperationException();
	}
	
	public Point getSelection(final IDocument document) {
		return null;
	}
	
	
	public int getRelevance() {
		return fRelevance;
	}
	
	public String getDisplayString() {
		return fLabel;
	}
	
	public Image getImage() {
		return StatetImages.getImage(StatetImages.CONTENTASSIST_CORRECTION_LINKEDRENAME);
	}
	
	public String getAdditionalProposalInfo() {
		return fDescription;
	}
	
	public IContextInformation getContextInformation() {
		return null;
	}
	
	
	public void selected(final ITextViewer textViewer, final boolean smartToggle) {
	}
	
	public void unselected(final ITextViewer textViewer) {
	}
	
	public boolean validate(final IDocument document, final int offset, final DocumentEvent event) {
		return false;
	}
	
}
