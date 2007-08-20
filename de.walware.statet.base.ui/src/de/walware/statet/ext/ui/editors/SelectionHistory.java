package de.walware.statet.ext.ui.editors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.ui.texteditor.IUpdate;

import de.walware.eclipsecommons.FastList;


public class SelectionHistory {

	private List<IRegion> fHistory;
	private StatextEditor1<?, ?> fEditor;
	private ISelectionChangedListener fSelectionListener;
	private int fSelectionChangeListenerCounter;
	private FastList<IUpdate> fUpdateActions = new FastList<IUpdate>(IUpdate.class);

	public SelectionHistory(StatextEditor1<?, ?> editor) {
		fEditor = editor;
		fHistory = new ArrayList<IRegion>();
		fSelectionListener = new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				if (fSelectionChangeListenerCounter == 0) {
					flush();
				}
			}
		};
		fEditor.getSelectionProvider().addSelectionChangedListener(fSelectionListener);
	}

	public void addUpdateListener(IUpdate action) {
		fUpdateActions.add(action);
	}

	private final void updateState() {
		final IUpdate[] actions = fUpdateActions.toArray();
		for (int i = 0; i < actions.length; i++) {
			actions[i].update();
		}
	}

	public boolean isEmpty() {
		return fHistory.isEmpty();
	}

	public void remember(IRegion range) {
		fHistory.add(range);
		updateState();
	}
	
	public IRegion getLast() {
		if (isEmpty())
			return null;
		IRegion result = fHistory.remove(fHistory.size() - 1);
		updateState();
		return result;
	}

	public void flush() {
		if (fHistory.isEmpty()) {
			return;
		}
		fHistory.clear();
		updateState();
	}

	public void ignoreSelectionChanges() {
		fSelectionChangeListenerCounter++;
	}

	public void listenToSelectionChanges() {
		fSelectionChangeListenerCounter--;
	}

	public void dispose() {
		fEditor.getSelectionProvider().removeSelectionChangedListener(fSelectionListener);
	}
}
