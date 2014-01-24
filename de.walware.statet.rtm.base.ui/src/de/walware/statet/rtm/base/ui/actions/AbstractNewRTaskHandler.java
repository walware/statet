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

package de.walware.statet.rtm.base.ui.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.ecommons.emf.ui.forms.DirectResourceEditorInput;
import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.rtm.base.ui.IRtDescriptor;
import de.walware.statet.rtm.base.ui.RtModelUIPlugin;


public abstract class AbstractNewRTaskHandler extends AbstractHandler {
	
	
	private final IRtDescriptor fRtDescriptor;
	
	
	public AbstractNewRTaskHandler(final IRtDescriptor descriptor) {
		fRtDescriptor = descriptor;
	}
	
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final IContentType contentType = Platform.getContentTypeManager().getContentType(fRtDescriptor.getDefaultContentTypeID());
		try {
			// Create a resource set
			final ResourceSet resourceSet = new ResourceSetImpl();
			
			// Get the URI of the model file
			final URI fileURI = DirectResourceEditorInput.NO_URI;
			
			// Create a resource for this file
			final Resource resource = resourceSet.createResource(fileURI, contentType.getId());
			resource.getContents().clear();
			
			// Add the initial model object to the contents
			final EObject rootObject = createInitialModel();
			if (rootObject != null) {
				resource.getContents().add(rootObject);
			}
			
			final PerspectiveUtil perspectiveUtil = new PerspectiveUtil();
			perspectiveUtil.updatePerspective(fRtDescriptor.getAssociatedPerspectiveId());
			
			// Open editor
			final IWorkbenchPage page = UIAccess.getActiveWorkbenchPage(true);
			final IEditorDescriptor editor = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor("", contentType); //$NON-NLS-1$
			final String name = DirectResourceEditorInput.createNumberedName(contentType.getName());
			page.openEditor(new DirectResourceEditorInput(name, resource), editor.getId());
		}
		catch (final Exception e) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, RtModelUIPlugin.PLUGIN_ID,
					NLS.bind("An error occurred when creating a new R task editor for {0}.", fRtDescriptor.getName()),
					e ), StatusManager.LOG | StatusManager.SHOW );
		}
		return null;
	}
	
	protected EObject createInitialModel() {
		return fRtDescriptor.createInitialModelObject();
	}
	
}
