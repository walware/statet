/*=============================================================================#
 # Copyright (c) 2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.debug.core.model;

import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;


@NonNullByDefault
public class RElementVariableStore {
	
	
	private static final int SEGMENT_LENGTH= 1000;
	
	
	private final long length;
	private final RElementVariable[][] arrays;
	
	
	public RElementVariableStore(final long length) {
		this.length= length;
		
		this.arrays= new RElementVariable[
				(this.length > 0) ?
						(int) ((this.length - 1) / SEGMENT_LENGTH) + 1 :
						0 ][];
	}
	
	
	public void set(final long idx, final RElementVariable value) {
		final int idx0= (int) (idx / SEGMENT_LENGTH);
		RElementVariable[] segment= this.arrays[idx0];
		if (segment == null) {
			segment= this.arrays[idx0]= new RElementVariable[
					(idx0 == this.arrays.length - 1) ?
							(int) ((this.length - 1) % SEGMENT_LENGTH) + 1 :
							SEGMENT_LENGTH];
		}
		final int idx1= (int) (idx % SEGMENT_LENGTH);
		segment[idx1]= value;
	}
	
	public @Nullable RElementVariable get(final long idx) {
		final int idx0= (int) (idx / SEGMENT_LENGTH);
		final RElementVariable[] segment= this.arrays[idx0];
		return (segment != null) ? segment[(int) (idx % SEGMENT_LENGTH)] : null;
	}
	
	public @Nullable RElementVariable clear(final long idx) {
		final int idx0= (int) (idx / SEGMENT_LENGTH);
		final RElementVariable[] segment= this.arrays[idx0];
		if (segment == null) {
			return null;
		}
		final int idx1= (int) (idx % SEGMENT_LENGTH);
		final RElementVariable value= segment[idx1];
		segment[idx1]= null;
		return value;
	}
	
//	public RElementVariable[] get(final long firstIdx, final int length) {
//		final RElementVariable[] array= new RElementVariable[length];
//		int idx0= (int) (firstIdx / SEGMENT_LENGTH);
//		int idx1= (int) (firstIdx % SEGMENT_LENGTH);
//		for (int i= 0; i < length;) {
//			final RElementVariable[] segment= this.arrays[idx0];
//			final int l= Math.min(length - i, SEGMENT_LENGTH - idx1);
//			System.arraycopy(segment, idx1, array, i, l);
//			idx0++;
//			idx1= 0;
//			i+= l;
//		}
//		return array;
//	}
	
	public void forEachSet(final Consumer<RElementVariable> action) {
		for (int idx0= 0; idx0 < this.arrays.length; idx0++) {
			final RElementVariable[] segment= this.arrays[idx0];
			if (segment != null) {
				for (int idx1= 0; idx1 < segment.length; idx1++) {
					final RElementVariable value= segment[idx1];
					if (value != null) {
						action.accept(value);
					}
				}
			}
		}
	}
	
}
