/*=============================================================================#
 # Copyright (c) 2012-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.rtm.base.internal.ui.editors;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EContentAdapter;
import org.eclipse.jface.text.AbstractDocument;
import org.eclipse.jface.text.ISynchronizable;

import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.rtm.base.core.AbstractRCodeGenerator;
import de.walware.statet.rtm.base.ui.IRtDescriptor;
import de.walware.statet.rtm.base.ui.RTaskSnippet;
import de.walware.statet.rtm.base.ui.editors.RTaskEditor;


public class RTaskGenManager extends EContentAdapter {
	
	
	private final RTaskEditor fEditor;
	
	private final AbstractRCodeGenerator fCodeGenerator;
	
	private final RCodeGenSourceFragment fCodeFragment;
	
	private volatile boolean fRunnableScheduled;
	
	private final Runnable fRunnable = new Runnable() {
		
		@Override
		public void run() {
			fRunnableScheduled = false;
			
			update();
		}
		
	};
	
	private RTaskSnippet fCurrentTaskSnippet;
	
	
	public RTaskGenManager(final RTaskEditor editor) {
		fEditor = editor;
		final IRtDescriptor descriptor = fEditor.getModelDescriptor();
		fCodeGenerator = descriptor.createCodeGenerator();
		fCodeFragment = new RCodeGenSourceFragment(Messages.RTaskEditor_RCodePage_label,
				Messages.RTaskEditor_RCodePage_label + " - " + descriptor.getName()); //$NON-NLS-1$
	}
	
	
	@Override
	public void notifyChanged(final Notification notification) {
		super.notifyChanged(notification);
		
		if (fRunnableScheduled) {
			return;
		}
		UIAccess.getDisplay().asyncExec(fRunnable);
	}
	
	private void update() {
		fCurrentTaskSnippet = createRTaskSnippet();
		final String code = fCurrentTaskSnippet.getRCode();
		final AbstractDocument document = fCodeFragment.getDocument();
		if (document instanceof ISynchronizable) {
			Object lockObject = ((ISynchronizable) document).getLockObject();
			if (lockObject == null) {
				lockObject = fCodeFragment;
			}
			synchronized (lockObject) {
				document.set(code);
			}
		}
		else {
			document.set(code);
		}
	}
	
	private RTaskSnippet createRTaskSnippet() {
		final EObject root = (EObject) fEditor.getDataBinding().getRootObservable().getValue();
		fCodeGenerator.generate(root);
		
		final IRtDescriptor descriptor = fEditor.getModelDescriptor();
		final String label = descriptor.getName() + " (" + fEditor.getEditorInput().getName() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
		return new RTaskSnippet(descriptor, label,
				fCodeGenerator.getRequiredPkgs(), fCodeGenerator.getRCode());
	}
	
	public RCodeGenSourceFragment getCodeFragment() {
		return fCodeFragment;
	}
	
	public RTaskSnippet getRTaskSnippet() {
		return fCurrentTaskSnippet;
	}
	
}
