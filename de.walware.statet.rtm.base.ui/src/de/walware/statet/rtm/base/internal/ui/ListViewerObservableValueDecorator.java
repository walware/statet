package de.walware.statet.rtm.base.internal.ui;

import java.util.List;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.value.DecoratingObservableValue;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.databinding.observable.value.ValueDiff;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.databinding.viewers.IViewerObservableValue;
import org.eclipse.jface.viewers.AbstractListViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;


public class ListViewerObservableValueDecorator extends DecoratingObservableValue
		implements IViewerObservableValue, Listener {
	
	
	private final List<Object> fInput;
	
	private AbstractListViewer fViewer;
	
	
	/**
	 * @param decorated
	 * @param viewer
	 */
	public ListViewerObservableValueDecorator(final IObservableValue decorated,
			final AbstractListViewer viewer, final List<Object> input) {
		super(decorated, true);
		fViewer = viewer;
		fInput = input;
		viewer.getControl().addListener(SWT.Dispose, this);
	}
	
	
	@Override
	public void handleEvent(final Event event) {
		if (event.type == SWT.Dispose) {
			dispose();
		}
	}
	
	@Override
	public Viewer getViewer() {
		return fViewer;
	}
	
	@Override
	public Object getValue() {
		return checkValue(super.getValue());
	}
	
	@Override
	public void setValue(Object value) {
		if (value == null) {
			value = fInput.get(0);
		}
		else if (value instanceof EObject) {
			checkInput((EObject) value);
		}
		super.setValue(value);
	}
	
	private void checkInput(final EObject value) {
		for (int i = 1; i < fInput.size(); i++) {
			if (fInput.get(i) == value) {
				return;
			}
		}
		for (int i = 1; i < fInput.size(); i++) {
			final Object item = fInput.get(i);
			if (item instanceof EObject
					&& ((EObject) item).eClass() == value.eClass()) {
				fInput.set(i, value);
				fViewer.setInput(fInput);
				return;
			}
		}
	}
	
	private EObject checkValue(final Object value) {
		return ((value instanceof EObject) ? (EObject) value : null);
	}
	
	@Override
	protected void handleValueChange(final ValueChangeEvent event) {
		final ValueDiff diff = Diffs.createValueDiff(
				checkValue(event.diff.getOldValue()),
				checkValue(event.diff.getNewValue()) );
		fireValueChange(diff);
	}
	
	@Override
	public synchronized void dispose() {
		if (fViewer != null) {
			final Control control = fViewer.getControl();
			if (control != null && !control.isDisposed()) {
				control.removeListener(SWT.Dispose, this);
			}
			fViewer = null;
		}
		super.dispose();
	}
	
}
