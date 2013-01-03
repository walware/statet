/*******************************************************************************
 * Copyright (c) 2008-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.correction;

import java.util.Arrays;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.link.LinkedPositionGroup;

import de.walware.ecommons.ltk.ui.sourceediting.assist.AssistInvocationContext;
import de.walware.ecommons.ltk.ui.sourceediting.assist.LinkedNamesAssistProposal;

import de.walware.statet.r.core.model.RElementAccess;
import de.walware.statet.r.core.rsource.ast.RAst;
import de.walware.statet.r.internal.ui.RUIMessages;


public class RLinkedNamesAssistProposal extends LinkedNamesAssistProposal {
	
	
	public static final int IN_FILE = 1;
	public static final int IN_FILE_PRECEDING = 2;
	public static final int IN_FILE_FOLLOWING = 3;
	public static final int IN_CHUNK = 4;
	
	
	private final RElementAccess fAccess;
	private final int fMode;
	private final IRegion fRegion;
	
	
	public RLinkedNamesAssistProposal(final int mode,
			final AssistInvocationContext invocationContext, final RElementAccess access) {
		super(invocationContext);
		fMode = mode;
		fRegion = null;
		switch (mode) {
		case IN_FILE:
			init(RUIMessages.Proposal_RenameInFile_label,
					RUIMessages.Proposal_RenameInFile_description,
					90 );
			break;
		case IN_FILE_PRECEDING:
			init(RUIMessages.Proposal_RenameInFilePrecending_label,
					RUIMessages.Proposal_RenameInFilePrecending_description,
					85 );
			break;
		case IN_FILE_FOLLOWING:
			init(RUIMessages.Proposal_RenameInFileFollowing_label,
					RUIMessages.Proposal_RenameInFileFollowing_description,
					84 );
			break;
		default:
			throw new IllegalArgumentException();
		}
		fAccess = access;
	}
	
	public RLinkedNamesAssistProposal(final int mode,
			final AssistInvocationContext invocationContext, final RElementAccess access,
			final IRegion region) {
		super(invocationContext);
		fMode = mode;
		fRegion = region;
		switch (mode) {
		case IN_CHUNK:
			init(RUIMessages.Proposal_RenameInChunk_label,
					RUIMessages.Proposal_RenameInChunk_description,
					89 );
			break;
		default:
			throw new IllegalArgumentException();
		}
		fAccess = access;
	}
	
	
	@Override
	protected void collectPositions(final IDocument document, final LinkedPositionGroup group)
			throws BadLocationException {
		final RElementAccess[] allInUnit = fAccess.getAllInUnit();
		Arrays.sort(allInUnit, RElementAccess.NAME_POSITION_COMPARATOR);
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
		idx = addPosition(group, document, getPosition(allInUnit[current]), idx);
		if (fMode == IN_FILE || fMode == IN_FILE_FOLLOWING) {
			for (int i = current+1; i < allInUnit.length; i++) {
				idx = addPosition(group, document, getPosition(allInUnit[i]), idx);
			}
		}
		else if (fMode == IN_CHUNK) {
			final int regionOffset = fRegion.getOffset()+fRegion.getLength();
			for (int i = current+1; i < allInUnit.length; i++) {
				if (regionOffset > allInUnit[i].getNameNode().getOffset()) {
					idx = addPosition(group, document, getPosition(allInUnit[i]), idx);
				}
				else {
					break;
				}
			}
		}
		if (fMode == IN_FILE || fMode == IN_FILE_PRECEDING) {
			for (int i = 0; i < current; i++) {
				idx = addPosition(group, document, getPosition(allInUnit[i]), idx);
			}
		}
		else if (fMode == IN_CHUNK) {
			final int regionOffset = fRegion.getOffset();
			for (int i = 0; i < current; i++) {
				if (regionOffset <= allInUnit[i].getNameNode().getOffset()) {
					idx = addPosition(group, document, getPosition(allInUnit[i]), idx);
				}
			}
		}
	}
	
	private Position getPosition(final RElementAccess access) {
		return RAst.getElementNamePosition(access.getNameNode());
	}
	
}
