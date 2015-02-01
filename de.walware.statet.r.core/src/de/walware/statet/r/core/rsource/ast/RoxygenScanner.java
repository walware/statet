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

package de.walware.statet.r.core.rsource.ast;

import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS2_SYNTAX_SYMBOL_MISSING;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS2_SYNTAX_TOKEN_NOT_CLOSED;
import static de.walware.statet.r.internal.core.sourcemodel.RoxygenTagType.SCAN_MODE_FREETEXT;
import static de.walware.statet.r.internal.core.sourcemodel.RoxygenTagType.SCAN_MODE_RCODE;
import static de.walware.statet.r.internal.core.sourcemodel.RoxygenTagType.SCAN_MODE_SYMBOL;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;

import de.walware.ecommons.collections.ConstArrayList;
import de.walware.ecommons.ltk.AstInfo;
import de.walware.ecommons.text.IStringCache;
import de.walware.ecommons.text.NoStringCache;
import de.walware.ecommons.text.SourceParseInput;
import de.walware.ecommons.text.StringRegionParseInput;

import de.walware.statet.r.core.rlang.RTerminal;
import de.walware.statet.r.core.rsource.ast.Symbol.G;
import de.walware.statet.r.internal.core.sourcemodel.RoxygenTagType;


public class RoxygenScanner {
	
	
	private SourceParseInput fInput;
	private final IStringCache fStringCache;
	
	private final List<DocuTag> fList = new ArrayList<DocuTag>();
	private RoxygenTagType fCurrentTagType;
	private DocuTag fCurrentTag;
	private final List<RAstNode> fCurrentTagFragments = new ArrayList<RAstNode>(64);
	private int fFragmentMode;
	private final List<IRegion> fCodeRegions = new ArrayList<IRegion>();
	
	
	public RoxygenScanner(final IStringCache cache) {
		fStringCache = (cache != null) ? cache : NoStringCache.INSTANCE;
	}
	
	
	public void init(final SourceParseInput input) {
		if (input == null) {
			throw new NullPointerException();
		}
		fInput = input;
	}
	
	public void update(final SourceComponent component) {
		final List<RAstNode> comments = component.fComments;
		if (comments == null || comments.isEmpty()) {
			return;
		}
		for (final RAstNode comment : comments) {
			if (comment.getNodeType() == NodeType.DOCU_AGGREGATION) {
				update((DocuComment) comment);
			}
		}
	}
	
	public void update(final DocuComment comment) {
		if (comment.getOperator(0) != RTerminal.ROXYGEN_COMMENT) {
			return;
		}
		try {
			final int lineCount = comment.getChildCount();
			for (int lineIdx = 0; lineIdx < lineCount; lineIdx++) {
				final Comment line = comment.getChild(lineIdx);
				fInput.init(line.fStartOffset+2, line.fStopOffset);
				readLine();
			}
			finishTag();
			
			comment.fTags = new ConstArrayList<DocuTag>(fList);
		}
		finally {
			fList.clear();
			fCurrentTagFragments.clear();
			fCurrentTag = null;
		}
	}
	
	private void setFragmentMode(final int mode) {
		fFragmentMode = mode;
	}
	
	private void finishTag() {
		switch ((fFragmentMode & 0xf)) {
		case SCAN_MODE_RCODE:
			if (!fCodeRegions.isEmpty()) {
				try {
					final RScanner scanner = new RScanner(new StringRegionParseInput(
							fInput, fCodeRegions.toArray(new IRegion[fCodeRegions.size()]) ),
							AstInfo.LEVEL_MODEL_DEFAULT, fStringCache );
					final SourceComponent node = scanner.scanSourceRange(fCurrentTag, 0, fInput.getStopIndex());
					if (node != null) {
						fCurrentTagFragments.add(node);
					}
				}
				finally {
					fCodeRegions.clear();
				}
			}
		}
		fFragmentMode = 0;
		
		if (!fCurrentTagFragments.isEmpty()) {
			fCurrentTag.fFragments = fCurrentTagFragments.toArray(new RAstNode[fCurrentTagFragments.size()]);
			fCurrentTag.fStopOffset = fCurrentTag.fFragments[fCurrentTag.fFragments.length-1].getStopOffset();
			fCurrentTagFragments.clear();
		}
	}
	
	private void readLine() {
		int num = 0;
		while (true) {
			switch (fInput.get(++num)) {
			case SourceParseInput.EOF:
			case '\r':
			case '\n':
				return;
			case ' ':
			case '\t':
				continue;
			case '@':
				finishTag();
				fInput.consume(num);
				readTag();
				return;
			default:
				fInput.consume(num-1);
				if (fCurrentTag == null) {
					fList.add(fCurrentTag = new DocuTag(null));
					fCurrentTag.fStartOffset = fInput.getIndex();
					setFragmentMode(SCAN_MODE_FREETEXT);
				}
				readFragments();
				return;
			}
		}
	}
	
	private void readTag() {
		int num = 0;
		while (true) {
			final int c = fInput.get(++num);
			if (c < 0x41 || !isRoxygenTagChar(c)) {
				--num;
				final String tag = fInput.substring(1, num);
				fInput.consume(num);
				fList.add(fCurrentTag = new DocuTag(tag));
				fCurrentTag.fStartOffset = fInput.getIndex();
				fCurrentTagType = RoxygenTagType.TYPES.get(tag);
				setFragmentMode((fCurrentTagType != null) ? fCurrentTagType.getNextScanMode(0x0) : SCAN_MODE_FREETEXT);
				if (c > 0) {
					readFragments();
				}
				return;
			}
		}
	}
	
	private void readFragments() {
		while (true) {
			switch ((fFragmentMode & 0xf)) {
			case SCAN_MODE_SYMBOL:
				if (readWhitespace()) {
					return;
				}
				if (readSymbol()) {
					return;
				}
				break;
			case SCAN_MODE_FREETEXT:
				if (readWhitespace()) {
					return;
				}
				if (readText()) {
					return;
				}
				break;
			case SCAN_MODE_RCODE:
				if (readWhitespace()) {
					return;
				}
				if (readCode()) {
					return;
				}
			}
		}
	}
	
	private boolean readWhitespace() {
		int num = 0;
		while (true) {
			final int c = fInput.get(++num);
			if (c != ' ' && c != '\t') {
				fInput.consume(num-1);
				return (c < 0 || c == '\n' || c == '\r');
			}
		}
	}
	
	private boolean readSymbol() {
		final int c = fInput.get(1);
		if (c == '`') {
			return readSymbolGraveQuote();
		}
		else {
			final Symbol symbol;
			int num = 1;
			if ((c >= 0x41 && c <= 0x5A) // most frequent cases
					|| (c >= 0x61 && c <= 0x7A)
					|| Character.isLetterOrDigit(c)) { 
				LOOP : while (true) {
					final int next = fInput.get(++num);
					if ((next >= 0x41 && next <= 0x5A) // most frequent cases
							|| (next >= 0x61 && next <= 0x7A)
							|| (next >= 0x30 && next <= 0x39)
							|| (next == '.' || next == '_')
							|| Character.isLetterOrDigit(next)) { 
						continue LOOP;
					}
					num--;
					symbol = new Symbol.Std();
					symbol.fText = fInput.substring(1, num, fStringCache);
					symbol.fStartOffset = fInput.getIndex();
					symbol.fStopOffset = symbol.fStartOffset+num;
					addSymbol(symbol);
					fInput.consume(num);
					return (next < 0 || next == '\n' || next == '\r');
				}
			}
			symbol = new Symbol.Std();
			symbol.fStartOffset = fInput.getIndex();
			symbol.fStopOffset = symbol.fStartOffset+num;
			symbol.fStatus = STATUS2_SYNTAX_SYMBOL_MISSING;
			addSymbol(symbol);
			fInput.consume(num);
			return (c < 0 || c == '\n' || c == '\r');
		}
	}
	
	private boolean readSymbolGraveQuote() {
		final G symbol;
		int num = 1;
		// 1 == '`'
		LOOP : while (true) {
			switch (fInput.get(++num)) {
			case '\\':
				if (fInput.get(++num) == SourceParseInput.EOF) {
					num--;
				}
				continue LOOP;
			case '`':
				symbol = new Symbol.G();
				symbol.fText = fInput.substring(2, num-2, fStringCache);
				symbol.fStartOffset = fInput.getIndex();
				symbol.fStopOffset = symbol.fStartOffset+num;
				addSymbol(symbol);
				fInput.consume(num);
				return false;
			case SourceParseInput.EOF:
			case '\r':
			case '\n':
				num--;
				symbol = new Symbol.G();
				symbol.fText = fInput.substring(2, num-1, fStringCache);
				symbol.fStatus = STATUS2_SYNTAX_TOKEN_NOT_CLOSED;
				symbol.fStartOffset = fInput.getIndex();
				symbol.fStopOffset = symbol.fStartOffset+num;
				addSymbol(symbol);
				fInput.consume(num);
				return true;
			default:
				continue LOOP;
			}
		}
	}
	
	private void addSymbol(final Symbol symbol) {
		symbol.fRParent = fCurrentTag;
		fCurrentTagFragments.add(symbol);
		if (fCurrentTagType != null) {
			setFragmentMode(fCurrentTagType.getNextScanMode(fFragmentMode));
		}
	}
	
	private boolean readText() {
		return true;
	}
	
	private boolean readCode() {
		fCodeRegions.add(new Region(fInput.getIndex(), fInput.getStopIndex()-fInput.getIndex()));
		return true;
	}
	
	private boolean isRoxygenTagChar(final int c) {
		if ((c >= 0x41 && c <= 0x5A) || (c >= 0x61 && c <= 0x7A)) {
			return true;
		}
		final int type = Character.getType(c);
		return (type > 0) && (type < 12 || type > 19);
	}
	
}
