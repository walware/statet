/*******************************************************************************
 * Copyright (c) 2007-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.text;

import java.util.ArrayList;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;


public class TextUtil {
	
	public static final Pattern LINE_DELIMITER_PATTERN = Pattern.compile("\\r[\\n]?|\\n"); //$NON-NLS-1$
	
	private static final IScopeContext PLATFORM_SCOPE = new InstanceScope();
	
	
	/**
	 * Returns the default line delimiter of the Eclipse platform (workbench)
	 * 
	 * @return the line delimiter string
	 */
	public static final String getPlatformLineDelimiter() {
		final String lineDelimiter = Platform.getPreferencesService().getString(Platform.PI_RUNTIME, Platform.PREF_LINE_SEPARATOR, null,
				new IScopeContext[] { PLATFORM_SCOPE });
		if (lineDelimiter != null) {
			return lineDelimiter;
		}
		return System.getProperty("line.separator"); //$NON-NLS-1$
	}
	
	/**
	 * Returns the default line delimiter for the specified project
	 * 
	 * If it cannot find a project specific setting, it returns the
	 * {@link #getPlatformLineDelimiter()}
	 * 
	 * @param project the project handle, may be <code>null</code>
	 * @return the line delimiter string
	 */
	public static String getLineDelimiter(final IProject project) {
		if (project != null) {
			final String lineSeparator = Platform.getPreferencesService().getString(Platform.PI_RUNTIME, Platform.PREF_LINE_SEPARATOR, null,
					new IScopeContext[] { new ProjectScope(project.getProject()), PLATFORM_SCOPE });
			if (lineSeparator != null) {
				return lineSeparator;
			}
		}
		return getPlatformLineDelimiter();
	}
	
	/**
	 * Return the length of the overlapping length of two regions.
	 * If they don't overlap, it return the negative distance of the regions.
	 */
	public static final int overlaps(final int reg1Start, final int reg1End, final int reg2Start, final int reg2End) {
		if (reg1Start <= reg2Start) {
			if (reg2End < reg1End) {
				return reg2End-reg2Start;
			}
			return reg1End-reg2Start;
		}
		else {
			if (reg1End < reg2End) {
				return reg1End-reg1Start;
			}
			return reg2End-reg1Start;
		}
	}
	
	/**
	 * Return the distance of two regions
	 */
	public final int distance(final int reg1Start, final int reg1End, final int reg2Start, final int reg2End) {
		if (reg2Start > reg1End) {
			return reg2Start-reg1End;
		}
		if (reg1Start > reg2End) {
			return reg1Start-reg2End;
		}
		return 0;
	}
	
	/**
	 * Return the distance of a point to the region.
	 */
	public static final int distance(final IRegion region, final int pointOffset) {
		int regPointOffset = region.getOffset();
		if (pointOffset < regPointOffset) {
			return regPointOffset-pointOffset;
		}
		regPointOffset += region.getLength();
		if (pointOffset > regPointOffset) {
			return pointOffset-regPointOffset;
		}
		return 0;
	}
	
	/**
	 * Adds text of lines of a document without its line delimiters to the list.
	 * 
	 * The first line begins at <code>offset</code>, the last line ends at <code>offset+length</code>.
	 * The positions must not be inside a line delimiter (if it consists of multiple chars).
	 * 
	 * @param document the document
	 * @param offset the offset of region to include
	 * @param length the length of region to include
	 * @param lines list the lines are added to
	 * @throws BadLocationException
	 */
	public static final void addLines(final IDocument document, final int offset, final int length, final ArrayList<String> lines) throws BadLocationException {
		final int startLine = document.getLineOfOffset(offset);
		final int endLine = document.getLineOfOffset(offset+length);
		lines.ensureCapacity(lines.size() + endLine-startLine+1);
		
		IRegion lineInfo;
		if (startLine > endLine) {
			throw new IllegalArgumentException();
		}
		if (startLine == endLine) {
			lineInfo = document.getLineInformation(endLine);
			lines.add(document.get(offset, length));
			return;
		}
		else {
			lineInfo = document.getLineInformation(startLine);
			lines.add(document.get(offset, Math.max(0, lineInfo.getOffset()+lineInfo.getLength()-offset)));
			for (int line = startLine+1; line < endLine; line++) {
				lineInfo = document.getLineInformation(line);
				lines.add(document.get(lineInfo.getOffset(), lineInfo.getLength()));
			}
			lineInfo = document.getLineInformation(endLine);
			if (offset+length > lineInfo.getOffset()) {
				lines.add(document.get(lineInfo.getOffset(), offset+length-lineInfo.getOffset()));
			}
		}
	}
	
	/**
	 * Computes the region of full lines containing the two specified positions 
	 * (e.g. begin and end offset of the editor selection).
	 * 
	 * If the second position is in column 0 and in another line than the first position,
	 * the line of second position is not included in the region. The last line contains
	 * the line delimiter, if exists (not if EOF).
	 * 
	 * @param document the document
	 * @param position1 first position
	 * @param position2 second position >= position1
	 * @return a region for the block
	 * @throws BadLocationException
	 */
	public final static IRegion getBlock(final IDocument document, final int position1, final int position2) throws BadLocationException {
		final int line1 = document.getLineOfOffset(position1);
		int line2 = document.getLineOfOffset(position2);
		if (line1 < line2 && document.getLineOffset(line2) == position2) {
			line2--;
		}
		final int start = document.getLineOffset(line1);
		final int length = document.getLineOffset(line2)+document.getLineLength(line2)-start;
		return new Region(start, length);
	}
	
	public final static IRegion expand(final IRegion region1, final IRegion region2) {
		if (region2 == null) {
			return region1;
		}
		final int offset = Math.min(region1.getOffset(), region2.getOffset());
		return new Region(offset, Math.max(
				region1.getOffset()+region1.getLength(), region2.getOffset()+region2.getLength())
						- offset);
	}
	
}
