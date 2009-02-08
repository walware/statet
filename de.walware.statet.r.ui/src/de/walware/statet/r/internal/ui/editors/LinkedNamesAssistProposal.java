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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension2;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension5;
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

import de.walware.ecommons.ui.text.sourceediting.AssistInvocationContext;
import de.walware.ecommons.ui.text.sourceediting.DefaultBrowserInformationInput;

import de.walware.statet.base.ui.StatetImages;
import de.walware.statet.base.ui.sourceeditors.StatextEditor1;

import de.walware.statet.r.core.model.IElementAccess;
import de.walware.statet.r.core.rsource.ast.RAst;
import de.walware.statet.r.internal.ui.RUIMessages;
import de.walware.statet.r.internal.ui.RUIPlugin;


public class LinkedNamesAssistProposal implements ICompletionProposal, ICompletionProposalExtension2, ICompletionProposalExtension5 {
	
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
	
	
	public static final int IN_FILE = 1;
	public static final int IN_FILE_PRECEDING = 2;
	public static final int IN_FILE_FOLLOWING = 3;
	
	
	private int fMode;
	private AssistInvocationContext fContext;
	private IElementAccess fAccess;
	private String fLabel;
	private String fDescription;
	private String fValueSuggestion;
	private int fRelevance;
	
	
	public LinkedNamesAssistProposal(final int mode,
			final AssistInvocationContext invocationContext, final IElementAccess access) {
		fMode = mode;
		switch (mode) {
		case IN_FILE:
			fLabel = RUIMessages.Proposal_RenameInFile_label;
			fDescription = RUIMessages.Proposal_RenameInFile_description;
			fValueSuggestion = null;
			break;
		case IN_FILE_PRECEDING:
			fLabel = RUIMessages.Proposal_RenameInFilePrecending_label;
			fDescription = RUIMessages.Proposal_RenameInFilePrecending_description;
			fValueSuggestion = null;
			break;
		case IN_FILE_FOLLOWING:
			fLabel = RUIMessages.Proposal_RenameInFileFollowing_label;
			fDescription = RUIMessages.Proposal_RenameInFileFollowing_description;
			fValueSuggestion = null;
			break;
		default:
			throw new IllegalArgumentException();
		}
		fContext = invocationContext;
		fAccess = access;
		fRelevance = 8;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void selected(final ITextViewer textViewer, final boolean smartToggle) {
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void unselected(final ITextViewer textViewer) {
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean validate(final IDocument document, final int offset, final DocumentEvent event) {
		return false;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void apply(final ITextViewer viewer, final char trigger, final int stateMask, final int offset) {
		try {
			Point seletion = viewer.getSelectedRange();
			
			final IDocument document = viewer.getDocument();
			final LinkedPositionGroup group = new LinkedPositionGroup();
			final IElementAccess[] allInUnit = fAccess.getAllInUnit();
			Arrays.sort(allInUnit, IElementAccess.NAME_POSITION_COMPARATOR);
			int current = -1;
			for (int i = 0; i < allInUnit.length; i++) {
				if (fAccess == allInUnit[i]) {
					current = i;
					break;
				}
			}
			if (current < 0) {
				return;
			}
			int idx = 0;
			idx = add(group, document, allInUnit[current], idx);
			if (fMode == IN_FILE || fMode == IN_FILE_FOLLOWING) {
				for (int i = current+1; i < allInUnit.length; i++) {
					idx = add(group, document, allInUnit[i], idx);
				}
			}
			if (fMode == IN_FILE || fMode == IN_FILE_PRECEDING) {
				for (int i = 0; i < current; i++) {
					idx = add(group, document, allInUnit[i], idx);
				}
			}
			
			if (group.isEmpty()) {
				return;
			}
			
			final LinkedModeModel model = new LinkedModeModel();
			model.addGroup(group);
			model.forceInstall();
			final StatextEditor1 editor = (StatextEditor1) fContext.getEditor();
			if (editor != null) {
				editor.getTextEditToolSynchronizer().install(model);
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
	
	private int add(final LinkedPositionGroup group, final IDocument document, final IElementAccess access, final int idx) throws BadLocationException {
		final Position position = RAst.getElementNamePosition(access.getNameNode());
		if (position != null) {
			group.addPosition(new LinkedPosition(document, position.getOffset(), position.getLength(), idx));
			return idx+1;
		}
		return idx;
	}
	
	public void apply(final IDocument document) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Point getSelection(final IDocument document) {
		return null;
	}
	
	
	public int getRelevance() {
		return fRelevance;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String getDisplayString() {
		return fLabel;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Image getImage() {
		return StatetImages.getImage(StatetImages.CONTENTASSIST_CORRECTION_LINKEDRENAME);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String getAdditionalProposalInfo() {
		return fDescription;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Object getAdditionalProposalInfo(final IProgressMonitor monitor) {
		return new DefaultBrowserInformationInput(null, getDisplayString(), fDescription, 
				DefaultBrowserInformationInput.FORMAT_TEXT_INPUT);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public IContextInformation getContextInformation() {
		return null;
	}
	
}
