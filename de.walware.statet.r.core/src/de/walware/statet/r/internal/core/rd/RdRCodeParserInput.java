/*=============================================================================#
 # Copyright (c) 2015-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.core.rd;

import de.walware.ecommons.text.core.input.FilterParserInput;
import de.walware.ecommons.text.core.input.StringParserInput;
import de.walware.ecommons.text.core.input.TextParserInput;


public class RdRCodeParserInput extends FilterParserInput {
	
	
	public RdRCodeParserInput(final String source) {
		this(new StringParserInput(source).init(),
				Math.min(source.length() + 1, DEFAULT_BUFFER_SIZE) );
	}
	
	public RdRCodeParserInput(final TextParserInput source) {
		this(source, DEFAULT_BUFFER_SIZE);
	}
	
	public RdRCodeParserInput(final TextParserInput source,
			final int defaultBufferSize) {
		super(source, defaultBufferSize);
	}
	
	
	@Override
	protected int read(final TextParserInput in, final char[] buffer,
			final int[] beginIndexes, final int[] endIndexes,
			final int beginIdx, final int requiredEnd, final int recommendEnd) {
		int idx= beginIdx;
		ITER_C0: while (idx < recommendEnd) {
			final int c0= in.get(0);
			C0: switch (c0) {
			case EOF:
				break ITER_C0;
			case '\\':
				switch (in.get(1)) {
				case 'A':
				case 'B':
				case 'C':
				case 'D':
				case 'E':
				case 'F':
				case 'G':
				case 'H':
				case 'I':
				case 'J':
				case 'K':
				case 'L':
				case 'M':
				case 'N':
				case 'O':
				case 'P':
				case 'Q':
				case 'R':
				case 'S':
				case 'T':
				case 'U':
				case 'V':
				case 'W':
				case 'X':
				case 'Y':
				case 'Z':
				case 'a':
				case 'b':
				case 'c':
				case 'd':
				case 'e':
				case 'f':
				case 'g':
				case 'h':
				case 'i':
				case 'j':
				case 'k':
				case 'l':
				case 'm':
				case 'n':
				case 'o':
				case 'p':
				case 'q':
				case 'r':
				case 's':
				case 't':
				case 'u':
				case 'v':
				case 'w':
				case 'x':
				case 'y':
				case 'z':
					consumeMacroIdentifier(in);
					continue ITER_C0;
				case '%':
				case '\\':
					buffer[idx]= (char) c0;
					beginIndexes[idx]= in.getIndex();
					endIndexes[idx]= in.getIndex() + in.getLengthInSource(2);
					idx++;
					in.consume(2);
					continue ITER_C0;
				default:
					break C0;
				}
			case '%':
				consumeComment(in);
				continue ITER_C0;
			default:
				break C0;
			}
			
			buffer[idx]= (char) c0;
			beginIndexes[idx]= in.getIndex();
			endIndexes[idx]= in.getIndex() + in.getLengthInSource(1);
			idx++;
			in.consume(1);
			continue;
		}
		beginIndexes[idx]= in.getIndex();
		return idx;
	}
	
	
	private void consumeMacroIdentifier(final TextParserInput in) {
		// after: \[a-zA-Z]
		int n= 2;
		while (true) {
			switch (in.get(n++)) {
			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9':
			case 'A':
			case 'B':
			case 'C':
			case 'D':
			case 'E':
			case 'F':
			case 'G':
			case 'H':
			case 'I':
			case 'J':
			case 'K':
			case 'L':
			case 'M':
			case 'N':
			case 'O':
			case 'P':
			case 'Q':
			case 'R':
			case 'S':
			case 'T':
			case 'U':
			case 'V':
			case 'W':
			case 'X':
			case 'Y':
			case 'Z':
			case 'a':
			case 'b':
			case 'c':
			case 'd':
			case 'e':
			case 'f':
			case 'g':
			case 'h':
			case 'i':
			case 'j':
			case 'k':
			case 'l':
			case 'm':
			case 'n':
			case 'o':
			case 'p':
			case 'q':
			case 'r':
			case 's':
			case 't':
			case 'u':
			case 'v':
			case 'w':
			case 'x':
			case 'y':
			case 'z':
				continue;
			default:
				in.consume(n - 1);
				return;
			}
		}
	}
	
	private void consumeComment(final TextParserInput in) {
		// after: %
		int n= 1;
		while (true) {
			switch (in.get(n++)) {
			case EOF:
			case '\r':
			case '\n':
				in.consume(n - 1);
				return;
			}
		}
	}
	
}
