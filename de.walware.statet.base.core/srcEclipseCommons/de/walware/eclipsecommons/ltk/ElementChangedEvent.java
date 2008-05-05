/*******************************************************************************
 * Copyright (c) 2000-2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package de.walware.eclipsecommons.ltk;


/**
 * An element changed event describes a change to the structure or contents
 * of a tree of Java elements. The changes to the elements are described by
 * the associated delta object carried by this event.
 * <p>
 * This class is not intended to be instantiated or subclassed by clients.
 * Instances of this class are automatically created by the Java model.
 * </p>
 * 
 * @see IElementChangedListener
 * @see IJavaElementDelta
 */
public class ElementChangedEvent {
	
	
	public final WorkingContext context;
	public final IModelElementDelta delta;
	
	
	/**
	 * Creates an new element changed event (based on a <code>IJavaElementDelta</code>).
	 * 
	 * @param delta the Java element delta.
	 * @param type the type of delta (ADDED, REMOVED, CHANGED) this event contains
	 */
	public ElementChangedEvent(final IModelElementDelta delta, final WorkingContext context) {
		this.context = context;
		this.delta = delta;
	}
	
}
