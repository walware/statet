/*******************************************************************************
 * Copyright (c) 2008-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.ltk.core.refactoring;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.ltk.core.refactoring.TextFileChange;

import de.walware.ecommons.ltk.ISourceUnit;


/**
 * A <code>TextChangeManager</code> manages associations between <code>ISourceUnit</code>
 * or <code>IFile</code> and <code>TextChange</code> objects.
 */
public class TextChangeManager {
	
	
	private Map<ISourceUnit, TextFileChange> fMap = new HashMap<ISourceUnit, TextFileChange>(10);
	
	private final boolean fKeepExecutedTextEdits;
	
	
	public TextChangeManager() {
		this(false);
	}
	
	public TextChangeManager(final boolean keepExecutedTextEdits) {
		fKeepExecutedTextEdits = keepExecutedTextEdits;
	}
	
	
//	/**
//	 * Adds an association between the given compilation unit and the passed
//	 * change to this manager.
//	 * 
//	 * @param cu the compilation unit (key)
//	 * @param change the change associated with the compilation unit
//	 */
//	public void manage(final ISourceUnit cu, final TextChange change) {
//		fMap.put(cu, change);
//	}
	
	/**
	 * Returns the <code>TextChange</code> associated with the given source unit.
	 * If the manager does not already manage an association it creates a one.
	 * 
	 * @param su the source unit for which the text buffer change is requested
	 * @return the text change associated with the given source unit. 
	 */
	public TextFileChange get(final ISourceUnit su) {
		TextFileChange result = fMap.get(su);
		if (result == null) {
			result = new SourceUnitChange(su);
			result.setKeepPreviewEdits(fKeepExecutedTextEdits);
			fMap.put(su, result);
		}
		return result;
	}
	
	/**
	 * Removes the <tt>TextChange</tt> managed under the given key
	 * <code>unit<code>.
	 * 
	 * @param unit the key determining the <tt>TextChange</tt> to be removed.
	 * @return the removed <tt>TextChange</tt>.
	 */
	public TextChange remove(final ISourceUnit unit) {
		return fMap.remove(unit);
	}
	
	/**
	 * Returns all source units managed by this instance.
	 * 
	 * @return all source units managed by this instance
	 */	
	public ISourceUnit[] getAllSourceUnits(){
		return fMap.keySet().toArray(new ISourceUnit[fMap.keySet().size()]);
	}
	
	/**
	 * Returns all text changes managed by this instance.
	 * 
	 * @return all text changes managed by this instance
	 */
	public TextChange[] getAllChanges(){
		final Set<ISourceUnit> suSet = fMap.keySet();
		final ISourceUnit[] sus = suSet.toArray(new ISourceUnit[suSet.size()]);
		Arrays.sort(sus, new Comparator<ISourceUnit>() {
			public int compare(final ISourceUnit su1, final ISourceUnit su2) {
				return su1.getId().compareTo(su2.getId());
			}
		});
		
		final TextChange[] textChanges= new TextChange[sus.length];
		for (int i= 0; i < sus.length; i++) {
			textChanges[i] = fMap.get(sus[i]);
		}
		return textChanges;
	}
	
	/**
	 * Returns if any text changes are managed for the specified source unit.
	 * 
	 * @param su the source unit
	 * @return <code>true</code> if any text changes are managed for the specified source unit and <code>false</code> otherwise
	 */
	public boolean containsChangesIn(final ISourceUnit su){
		return fMap.containsKey(su);
	}
	
	/**
	 * Clears all associations between resources and text changes.
	 */
	public void clear() {
		fMap.clear();
	}
	
}
