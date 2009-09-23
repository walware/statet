/*******************************************************************************
 * Copyright (c) 2005-2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - Eclipse bug 135446
 *     Brad Reynolds - Eclipse bug 164653
 *******************************************************************************/

package de.walware.ecommons.databinding;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;


/**
 * {@link IObservable} implementation that wraps a {@link StyledText} widget. The time
 * at which listeners should be notified about changes to the text is specified
 * on construction.
 * 
 * <dl>
 * <dt>Events:</dt>
 * <dd> If the update event type (specified on construction) is
 * <code>SWT.Modify</code> a value change event will be fired on every key
 * stroke. If the update event type is <code>SWT.FocusOut</code> a value
 * change event will be fired on focus out. When in either mode if the user is
 * entering text and presses [Escape] the value will be reverted back to the
 * last value set using doSetValue(). Regardless of the update event type a
 * value changing event will fire on verify to enable vetoing of changes.</dd>
 * </dl>
 * 
 * @since 1.0
 */
public class StyledTextObservableValue extends AbstractSWTObservableValue {
	
	/**
	 * {@link StyledText} widget that this is being observed.
	 */
	private final StyledText text;
	
	/**
	 * Flag to track when the model is updating the widget. When
	 * <code>true</code> the handlers for the SWT events should not process
	 * the event as this would cause an infinite loop.
	 */
	private boolean updating = false;
	
	/**
	 * SWT event that on firing this observable will fire change events to its
	 * listeners.
	 */
	private final int updateEventType;
	
	/**
	 * Valid types for the {@link #updateEventType}.
	 */
	private static final int[] validUpdateEventTypes = new int[] { SWT.Modify,
			SWT.FocusOut, SWT.None };
	
	/**
	 * Previous value of the Text.
	 */
	private String oldValue;
	
	private Listener updateListener = new Listener() {
		public void handleEvent(Event event) {
			if (!updating) {
				String newValue = text.getText();
				
				if (!newValue.equals(oldValue)) {
					fireValueChange(Diffs.createValueDiff(oldValue, newValue));					
					oldValue = newValue;
				}
			}
		}
	};
	
	
	
	/**
	 * Constructs a new instance bound to the given <code>text</code> widget
	 * and configured to fire change events to its listeners at the time of the
	 * <code>updateEventType</code>.
	 * 
	 * @param text
	 * @param updateEventType
	 *            SWT event constant as to what SWT event to update the model in
	 *            response to. Appropriate values are: <code>SWT.Modify</code>,
	 *            <code>SWT.FocusOut</code>, <code>SWT.None</code>.
	 * @throws IllegalArgumentException
	 *             if <code>updateEventType</code> is an incorrect type.
	 */
	public StyledTextObservableValue(final StyledText text, int updateEventType) {
		this(SWTObservables.getRealm(text.getDisplay()), text, updateEventType);
	}
	
	/**
	 * Constructs a new instance.
	 * 
	 * @param realm can not be <code>null</code>
	 * @param text
	 * @param updateEventType
	 */
	public StyledTextObservableValue(final Realm realm, StyledText text, int updateEventType) {
		super(realm, text);
		
		boolean eventValid = false;
		for (int i = 0; !eventValid && i < validUpdateEventTypes.length; i++) {
			eventValid = (updateEventType == validUpdateEventTypes[i]);
		}
		if (!eventValid) {
			throw new IllegalArgumentException(
					"UpdateEventType [" + updateEventType + "] is not supported."); //$NON-NLS-1$//$NON-NLS-2$
		}
		this.text = text;
		this.updateEventType = updateEventType;
		if (updateEventType != SWT.None) {
			text.addListener(updateEventType, updateListener);
		}
		
		oldValue = text.getText();
	}
	
	/**
	 * Returns the current value of the {@link StyledText}.
	 * 
	 * @see org.eclipse.core.databinding.observable.value.AbstractVetoableValue#doGetValue()
	 */
	@Override
	public Object doGetValue() {
		return oldValue = text.getText();
	}
	
	@Override
	protected void doSetValue(Object value) {
		try {
			updating = true;
			text.setText(value == null ? "" : value.toString()); //$NON-NLS-1$
			oldValue = text.getText();
		} finally {
			updating = false;
		}
	}
	
	/**
	 * Returns the type of the value from {@link #doGetValue()}, i.e.
	 * String.class
	 * 
	 * @see org.eclipse.core.databinding.observable.value.IObservableValue#getValueType()
	 */
	public Object getValueType() {
		return String.class;
	}
	
	@Override
	public void dispose() {
		if (!text.isDisposed()) {
			if (updateEventType != SWT.None) {
				text.removeListener(updateEventType, updateListener);
			}
		}
		super.dispose();
	}
	
}
