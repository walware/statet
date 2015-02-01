/*=============================================================================#
 # Copyright (c) 2009-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.ui.editors;

import org.eclipse.jface.text.AbstractDocument;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationPresenter;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;

import de.walware.ecommons.ltk.AstInfo;
import de.walware.ecommons.text.PartialStringParseInput;

import de.walware.statet.r.core.model.ArgsDefinition;
import de.walware.statet.r.core.rsource.ast.FCall;
import de.walware.statet.r.core.rsource.ast.FCall.Args;
import de.walware.statet.r.core.rsource.ast.RAst;
import de.walware.statet.r.core.rsource.ast.RAst.ReadedFCallArgs;
import de.walware.statet.r.core.rsource.ast.RScanner;


public class RContextInformationValidator implements IContextInformationValidator, IContextInformationPresenter {
	
	
	private ITextViewer fViewer;
	private int fStartOffset;
	
	private IContextInformation fInfo;
	
	private RArgumentListContextInformation fArgInfo;
	private int fCurrentParameter;
//	private RScanner fScanner;
	private long fScannedArgsStamp;
	private Args fScannedArgs;
	
	private int fLastPresentation = -2;
	
	
	public RContextInformationValidator() {
	}
	
	
	@Override
	public boolean isContextInformationValid(final int offset) {
		if (fInfo == null) {
			return false;
		}
		final IDocument document = fViewer.getDocument();
		if (offset < fStartOffset || offset > document.getLength()) {
			return false;
		}
		if (fArgInfo != null) {
			final Args args = getScannedArgs();
			if (args != null) {
				return (offset <= args.getStopOffset());
			}
		}
		return (offset == fStartOffset);
	}
	
	@Override
	public void install(final IContextInformation info, final ITextViewer viewer, final int offset) {
		fScannedArgs = null;
		fLastPresentation = -2;
		if (info instanceof RArgumentListContextInformation) {
			fInfo = fArgInfo = (RArgumentListContextInformation) info;
			fCurrentParameter = -1;
		}
		else {
			fInfo = info;
			fArgInfo = null;
			return;
		}
		fViewer = viewer;
		fStartOffset = offset;
	}
	
	@Override
	public boolean updatePresentation(final int offset, final TextPresentation presentation) {
		if (fArgInfo != null) {
			final ArgsDefinition args = fArgInfo.getArguments();
			if (args != null && args.size() > 0) {
				final int argIndex = getCurrentArgInFDef(offset);
				final int[] idxs = fArgInfo.getInformationDisplayStringArgumentIdxs();
				if (argIndex >= 0 && argIndex < idxs.length) {
					if (argIndex == fLastPresentation) {
						return false;
					}
					final int start = idxs[argIndex];
					final int stop = (argIndex+1 < idxs.length) ? idxs[argIndex+1] : fArgInfo.getInformationDisplayString().length();
					presentation.clear();
					presentation.addStyleRange(new StyleRange(start, stop-start, null, null, SWT.BOLD));
					fLastPresentation = argIndex;
					return true;
				}
			}
		}
		if (fLastPresentation == -1) {
			return false;
		}
		presentation.clear();
		presentation.addStyleRange(new StyleRange(0, fInfo.getInformationDisplayString().length(),
				null, null, SWT.NORMAL));
		fLastPresentation = -1;
		return true;
	}
	
	private FCall.Args getScannedArgs() {
		final AbstractDocument document = (AbstractDocument) fViewer.getDocument();
		final long stamp = document.getModificationStamp();
		if (fScannedArgs == null || fScannedArgsStamp != stamp) {
			try {
				final String text = document.get(fStartOffset, Math.min(1000, document.getLength()-fStartOffset));
				final RScanner scanner = new RScanner(new PartialStringParseInput(text, fStartOffset),
						AstInfo.LEVEL_MODEL_DEFAULT );
				fScannedArgs = scanner.scanFCallArgs(fStartOffset, text.length(), true);
				fScannedArgsStamp = stamp;
			}
			catch (final Exception e) {
				fScannedArgs = null;
			}
		}
		
		return fScannedArgs;
	}
	
	private int getCurrentArgInFDef(final int offset) {
		final int call = getCurrentArgInFCall(offset);
		if (call >= 0) {
			final ReadedFCallArgs args = RAst.readArgs(getScannedArgs(), fArgInfo.getArguments());
			if (args.argsNode2argsDef.length == 0) {
				return 0;
			}
			return args.argsNode2argsDef[call];
		}
		return -1;
	}
	
	private int getCurrentArgInFCall(final int offset) {
		final Args args = getScannedArgs();
		if (args != null) {
			final int last = args.getChildCount()-1;
			if (last == -1) {
				return 0;
			}
			for (int i = 0; i < last; i++) {
				if (offset <= args.getSeparatorOffset(i)) {
					return i;
				}
			}
			return last;
		}
		return -1;
	}
	
}
