/*******************************************************************************
 * Copyright (c) 2010 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.core.rhelp;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;


public final class FIO {
	
	
	private static final ThreadLocal<FIO> INSTANCES = new ThreadLocal<FIO>() {
		
		@Override
		protected FIO initialValue() {
			return new FIO();
		}
		
	};
	
	public static FIO get(final ObjectOutput out) {
		final FIO io = INSTANCES.get();
		io.out = out;
		return io;
	}
	
	public static FIO get(final ObjectInput in) {
		final FIO io = INSTANCES.get();
		io.in = in;
		return io;
	}
	
	
	private static final int BB_LENGTH = 16384;
	private static final int BA_LENGTH = BB_LENGTH;
	private static final int BB_PART = BB_LENGTH / 4;
	private static final int CB_LENGTH = BB_LENGTH / 2;
	private static final int CA_LENGTH = BB_LENGTH * 4;
	
	
	private final ByteBuffer bb;
	private final byte[] ba;
	private final CharBuffer cb;
	private final char[] ca;
	
	public ObjectInput in;
	
	public ObjectOutput out;
	
	public int flags;
	
	
	public FIO() {
		this.bb = ByteBuffer.allocateDirect(BB_LENGTH);
		if (this.bb.hasArray()) {
			this.ba = this.bb.array();
		}
		else {
			this.ba = new byte[BB_LENGTH];
		}
		this.cb = this.bb.asCharBuffer();
		this.ca = new char[CA_LENGTH];
	}
	
	
	public void writeString(final String s) throws IOException {
		final ObjectOutput out = this.out;
		if (s != null) {
			final int cn = s.length();
			ASCII: if (cn <= BA_LENGTH) {
				for (int ci = 0; ci < cn; ) {
					if ((s.charAt(ci++) & 0xffffff00) != 0) {
						break ASCII;
					}
				}
				if (cn <= 8) {
					out.writeInt(-cn);
					out.writeBytes(s);
					return;
				}
				else {
					out.writeInt(-cn);
					s.getBytes(0, cn, this.ba, 0);
					out.write(this.ba, 0, cn);
					return;
				}
			}
			out.writeInt(cn);
			out.writeChars(s);
			return;
		}
		else {
			out.writeInt(Integer.MIN_VALUE);
			return;
		}
	}
	
	public void writeStringArray(final String[] sa, final int length) throws IOException {
		final ObjectOutput out = this.out;
		out.writeInt(length);
		for (int i = 0; i < length; i++) {
			final String s = sa[i];
			if (s != null) {
				final int cn = s.length();
				ASCII: if (cn <= BA_LENGTH) {
					for (int ci = 0; ci < cn; ) {
						if ((s.charAt(ci++) & 0xffffff00) != 0) {
							break ASCII;
						}
					}
					if (cn <= 8) {
						out.writeInt(-cn);
						out.writeBytes(s);
						continue;
					}
					else {
						out.writeInt(-cn);
						s.getBytes(0, cn, this.ba, 0);
						out.write(this.ba, 0, cn);
						continue;
					}
				}
				out.writeInt(cn);
				out.writeChars(s);
				continue;
			}
			else {
				out.writeInt(Integer.MIN_VALUE);
				continue;
			}
		}
	}
	
	public void flush() throws IOException {
		this.out.flush();
	}
	
	
	private String readString(final int cn, final char[] ca, final ObjectInput in) throws IOException {
		int cr = 0;
		int position = 0;
		final int bToComplete;
		while (true) {
			position += in.read(this.ba, position, BA_LENGTH-position);
			if (position >= BB_PART) {
				final int icount = (position >> 1);
				final int bcount = (icount << 1);
				if (!this.bb.hasArray()) {
					this.bb.clear();
					this.bb.put(this.ba, 0, bcount);
				}
				this.cb.clear();
				this.cb.get(ca, cr, icount);
				cr += icount;
				if (position - bcount != 0) {
					this.ca[cr++] = (char) (
							((this.ba[bcount] & 0xff) << 8) |
							((in.read() & 0xff)) );
				}
				position = 0;
				if (cn - cr <= CB_LENGTH) {
					bToComplete = (cn - cr) << 1;
					break;
				}
			}
		}
		if (bToComplete > 0) {
			in.readFully(this.ba, position, bToComplete-position);
			if (!this.bb.hasArray()) {
				this.bb.clear();
				this.bb.put(this.ba, 0, bToComplete);
			}
			this.cb.clear();
			this.cb.get(ca, cr, bToComplete >> 1);
		}
		return new String(ca, 0, cn);
	}
	
	public String readString() throws IOException {
		final ObjectInput in = this.in;
		final int cn = in.readInt();
		if (cn >= 0) {
			if (cn == 0) {
				return "";
			}
			else if (cn <= 64) {
				for (int ci = 0; ci < cn; ci++) {
					this.ca[ci] = in.readChar();
				}
				return new String(this.ca, 0, cn);
			}
			else if (cn <= CB_LENGTH) {
				final int bn = cn << 1;
				in.readFully(this.ba, 0, bn);
				if (!this.bb.hasArray()) {
					this.bb.clear();
					this.bb.put(this.ba, 0, bn);
				}
				this.cb.clear();
				this.cb.get(this.ca, 0, cn);
				return new String(this.ca, 0, cn);
			}
			else if (cn <= CA_LENGTH) {
				return readString(cn, this.ca, in);
			}
			else {
				return readString(cn, new char[cn], in);
			}
		}
		else if (cn != Integer.MIN_VALUE) {
			in.readFully(this.ba, 0, -cn);
			return new String(this.ba, 0, 0, -cn);
		}
		else {
			return null;
		}
	}
	
}
