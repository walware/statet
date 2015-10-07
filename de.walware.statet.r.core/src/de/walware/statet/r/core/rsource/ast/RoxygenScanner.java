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

import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS12_SYNTAX_TOKEN_NOT_CLOSED;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS2_SYNTAX_SYMBOL_MISSING;
import static de.walware.statet.r.internal.core.sourcemodel.RoxygenTagType.SCAN_MODE_FREETEXT;
import static de.walware.statet.r.internal.core.sourcemodel.RoxygenTagType.SCAN_MODE_RCODE;
import static de.walware.statet.r.internal.core.sourcemodel.RoxygenTagType.SCAN_MODE_SYMBOL;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;

import de.walware.ecommons.collections.ImCollections;
import de.walware.ecommons.ltk.AstInfo;
import de.walware.ecommons.string.IStringFactory;
import de.walware.ecommons.string.StringFactory;
import de.walware.ecommons.text.core.input.RegionParserInput;
import de.walware.ecommons.text.core.input.TextParserInput;

import de.walware.statet.r.core.rlang.RTerminal;
import de.walware.statet.r.core.rsource.ast.Symbol.G;
import de.walware.statet.r.internal.core.rd.RdRCodeParserInput;
import de.walware.statet.r.internal.core.sourcemodel.RoxygenTagType;


public class RoxygenScanner {
	
	
	private TextParserInput input;
	private RegionParserInput regionInput;
	private RdRCodeParserInput rCodeInput;
	private RScanner rScanner;
	private final IStringFactory textCache;
	
	private final List<DocuTag> list= new ArrayList<>();
	private RoxygenTagType currentTagType;
	private DocuTag currentTag;
	private final List<RAstNode> currentTagFragments= new ArrayList<>(64);
	private int fragmentMode;
	private final List<IRegion> codeRegions= new ArrayList<>();
	
	
	public RoxygenScanner(final IStringFactory textCache) {
		this.textCache= (textCache != null) ? textCache : StringFactory.INSTANCE;
	}
	
	
	public void init(final TextParserInput input) {
		if (input == null) {
			throw new NullPointerException();
		}
		this.input= input;
	}
	
	public void update(final SourceComponent component) {
		final List<RAstNode> comments= component.fComments;
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
			final int lineCount= comment.getChildCount();
			for (int lineIdx= 0; lineIdx < lineCount; lineIdx++) {
				readLine(comment.getChild(lineIdx));
			}
			finishTag();
			
			comment.fTags= ImCollections.toList(this.list);
		}
		finally {
			this.list.clear();
			this.currentTagFragments.clear();
			this.currentTag= null;
		}
	}
	
	private void setFragmentMode(final int mode) {
		this.fragmentMode= mode;
	}
	
	private void finishTag() {
		switch ((this.fragmentMode & 0xf)) {
		case SCAN_MODE_RCODE:
			if (!this.codeRegions.isEmpty()) {
				if (this.rScanner == null) {
					if (this.regionInput == null) {
						this.regionInput= new RegionParserInput(this.input, null);
						this.regionInput.setSeparator("\n"); //$NON-NLS-1$
					}
					this.rCodeInput= new RdRCodeParserInput(this.regionInput);
					this.rScanner= new RScanner(AstInfo.LEVEL_MODEL_DEFAULT, this.textCache);
				}
				try {
					this.regionInput.reset(ImCollections.toList(this.codeRegions));
					final SourceComponent node= this.rScanner.scanSourceRange(
							this.rCodeInput.init(), this.currentTag );
					if (node != null) {
						this.currentTagFragments.add(node);
					}
				}
				finally {
					this.codeRegions.clear();
				}
			}
		}
		this.fragmentMode= 0;
		
		if (!this.currentTagFragments.isEmpty()) {
			this.currentTag.fFragments= this.currentTagFragments.toArray(new RAstNode[this.currentTagFragments.size()]);
			this.currentTag.fStopOffset= this.currentTag.fFragments[this.currentTag.fFragments.length - 1].getEndOffset();
			this.currentTagFragments.clear();
		}
	}
	
	private void readLine(final Comment line) {
		final TextParserInput in= this.input;
		READ_LINE: while (true) {
			in.init(line.fStartOffset + 2, line.fStopOffset);
			int num= 0;
			while (true) {
				switch (in.get(num++)) {
				case TextParserInput.EOF:
				case '\r':
				case '\n':
					return;
				case ' ':
				case '\t':
					continue;
				case '@':
					if (this.fragmentMode != 0) {
						finishTag();
						continue READ_LINE;
					}
					in.consume(num - 1);
					readTag(in);
					return;
				default:
					in.consume(num - 1);
					if (this.currentTag == null) {
						this.list.add(this.currentTag= new DocuTag(null));
						this.currentTag.fStartOffset= this.input.getIndex();
						setFragmentMode(SCAN_MODE_FREETEXT);
					}
					readFragments();
					return;
				}
			}
		}
	}
	
	private void readTag(final TextParserInput in) {
		// after: @
		int num= 1;
		while (true) {
			final int c= in.get(num++);
			if (c < 0x41 || !isRoxygenTagChar(c)) {
				num--;
				final String tag= in.getString(1, num - 1, this.textCache);
				this.list.add(this.currentTag= new DocuTag(tag));
				this.currentTag.fStartOffset= in.getIndex();
				in.consume(num);
				this.currentTagType= RoxygenTagType.TYPES.get(tag);
				setFragmentMode((this.currentTagType != null) ?
						this.currentTagType.getNextScanMode(0x0) : SCAN_MODE_FREETEXT );
				if (c > 0) {
					readFragments();
				}
				return;
			}
		}
	}
	
	private void readFragments() {
		final TextParserInput in= this.input;
		while (true) {
			switch ((this.fragmentMode & 0xf)) {
			case SCAN_MODE_SYMBOL:
				if (consumeWhitespace(in)) {
					return;
				}
				if (readSymbol(in)) {
					return;
				}
				break;
			case SCAN_MODE_FREETEXT:
				if (consumeWhitespace(in)) {
					return;
				}
				if (readText()) {
					return;
				}
				break;
			case SCAN_MODE_RCODE:
				if (consumeWhitespace(in)) {
					return;
				}
				if (readCode(in)) {
					return;
				}
			}
		}
	}
	
	private boolean consumeWhitespace(final TextParserInput in) {
		int num= 0;
		while (true) {
			final int c= in.get(num++);
			if (c != ' ' && c != '\t') {
				in.consume(num - 1);
				return (c < 0 || c == '\n' || c == '\r');
			}
		}
	}
	
	private boolean readSymbol(final TextParserInput in) {
		final int c= in.get(1);
		if (c == '`') {
			return readSymbolGraveQuote(in);
		}
		else {
			final Symbol symbol;
			int num= 1;
			if ((c >= 0x41 && c <= 0x5A) // most frequent cases
					|| (c >= 0x61 && c <= 0x7A)
					|| Character.isLetterOrDigit(c)) { 
				LOOP : while (true) {
					final int next= in.get(num++);
					if ((next >= 0x41 && next <= 0x5A) // most frequent cases
							|| (next >= 0x61 && next <= 0x7A)
							|| (next >= 0x30 && next <= 0x39)
							|| (next == '.' || next == '_')
							|| Character.isLetterOrDigit(next)) { 
						continue LOOP;
					}
					num--;
					symbol= new Symbol.Std();
					symbol.fText= in.getString(0, num, this.textCache);
					symbol.fStartOffset= in.getIndex();
					symbol.fStopOffset= in.getIndex() + in.getLengthInSource(num);
					addSymbol(symbol);
					in.consume(num);
					return (next < 0 || next == '\n' || next == '\r');
				}
			}
			symbol= new Symbol.Std();
			symbol.fStartOffset= in.getIndex();
			symbol.fStopOffset= symbol.fStartOffset + in.getLengthInSource(num);
			symbol.fStatus= STATUS2_SYNTAX_SYMBOL_MISSING;
			addSymbol(symbol);
			in.consume(num);
			return (c < 0 || c == '\n' || c == '\r');
		}
	}
	
	private boolean readSymbolGraveQuote(final TextParserInput in) {
		// after: `
		final G symbol;
		int num= 1;
		LOOP : while (true) {
			switch (in.get(num++)) {
			case '\\':
				if (in.get(num++) == TextParserInput.EOF) {
					num--;
				}
				continue LOOP;
			case '`':
				symbol= new Symbol.G();
				symbol.fText= in.getString(1, num - 2, this.textCache);
				symbol.fStartOffset= in.getIndex();
				symbol.fStopOffset= symbol.fStartOffset + in.getLengthInSource(num);
				addSymbol(symbol);
				in.consume(num);
				return false;
			case TextParserInput.EOF:
			case '\r':
			case '\n':
				num--;
				symbol= new Symbol.G();
				symbol.fText= in.getString(1, num - 1, this.textCache);
				symbol.fStatus= STATUS12_SYNTAX_TOKEN_NOT_CLOSED;
				symbol.fStartOffset= in.getIndex();
				symbol.fStopOffset= symbol.fStartOffset + in.getLengthInSource(num);
				addSymbol(symbol);
				in.consume(num);
				return true;
			default:
				continue LOOP;
			}
		}
	}
	
	private void addSymbol(final Symbol symbol) {
		symbol.fRParent= this.currentTag;
		this.currentTagFragments.add(symbol);
		if (this.currentTagType != null) {
			setFragmentMode(this.currentTagType.getNextScanMode(this.fragmentMode));
		}
	}
	
	private boolean readText() {
		return true;
	}
	
	private boolean readCode(final TextParserInput in) {
		this.codeRegions.add(new Region(in.getIndex(), in.getStopIndex() - in.getIndex()));
		return true;
	}
	
	private boolean isRoxygenTagChar(final int c) {
		if ((c >= 0x41 && c <= 0x5A) || (c >= 0x61 && c <= 0x7A)) {
			return true;
		}
		final int type= Character.getType(c);
		return (type > 0) && (type < 12 || type > 19);
	}
	
}
