/*=============================================================================#
 # Copyright (c) 2008-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.debug.ui.launcher;

import java.net.URI;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IURIEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.editors.text.IEncodingSupport;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.ide.ResourceUtil;
import org.eclipse.ui.menus.UIElement;

import de.walware.ecommons.io.FileUtil;
import de.walware.ecommons.ltk.LTK;
import de.walware.ecommons.ltk.core.model.ISourceElement;
import de.walware.ecommons.ltk.core.model.ISourceUnit;
import de.walware.ecommons.ltk.ui.sourceediting.ISourceEditor;
import de.walware.ecommons.ltk.ui.util.LTKWorkbenchUIUtil;
import de.walware.ecommons.workbench.ui.WorkbenchUIUtil;

import de.walware.statet.r.internal.debug.ui.RLaunchingMessages;
import de.walware.statet.r.launching.RCodeLaunching;


/**
 * Handler running a file in R using a command like source(...)
 * 
 * @see RCodeLaunchRegistry
 * @see RCodeLaunching#getPreferredFileCommand
 */
public class SubmitFileViaCommandHandler extends AbstractHandler implements IElementUpdater {
	
	
	private final boolean fGotoConsole;
	
	
	public SubmitFileViaCommandHandler() {
		this(false);
	}
	
	public SubmitFileViaCommandHandler(final boolean gotoConsole) {
		fGotoConsole = gotoConsole;
	}
	
	
	@Override
	public void updateElement(final UIElement element, final Map parameters) {
		// TODO
	}
	
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final String fileCommandId = event.getParameter(RCodeLaunching.FILE_COMMAND_ID_PARAMTER_ID);
		
		final IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
		
		String contentTypeId= LTKWorkbenchUIUtil.getContentTypeId(activePart);
		
		try {
			IAdaptable encodingAdaptable = null;
			ISourceUnit su = null;
			IFile file = null;
			URI uri = null;
			if (activePart instanceof IEditorPart) {
				encodingAdaptable = activePart;
				final ISourceEditor sourceEditor = (ISourceEditor) activePart.getAdapter(ISourceEditor.class);
				if (sourceEditor != null) {
					su = sourceEditor.getSourceUnit();
					if (su != null) {
						encodingAdaptable = sourceEditor;
					}
				}
				if (su == null) {
					su = (ISourceUnit) activePart.getAdapter(ISourceUnit.class);
				}
				if (su == null) {
					final IEditorPart editor = (IEditorPart) activePart;
					final IEditorInput input = editor.getEditorInput();
					file = ResourceUtil.getFile(input);
					if (file == null && input instanceof IURIEditorInput) {
						uri = ((IURIEditorInput) input).getURI();
					}
				}
			}
			if (su == null && file == null && uri == null) {
				final ISelection selection = WorkbenchUIUtil.getCurrentSelection(event.getApplicationContext());
				if (selection instanceof IStructuredSelection) {
					final IStructuredSelection sel = (IStructuredSelection) selection;
					if (sel.size() == 1) {
						final Object object = sel.getFirstElement();
						if (object instanceof ISourceUnit) {
							su = (ISourceUnit) object;
						}
						else if (object instanceof ISourceElement) {
							su = ((ISourceElement) object).getSourceUnit();
						}
						else if (object instanceof IAdaptable) {
							su = (ISourceUnit) ((IAdaptable) object).getAdapter(ISourceUnit.class);
						}
						if (su == null) {
							if (object instanceof IFile) {
								file = (IFile) object;
							}
							else if (object instanceof IAdaptable) {
								file = (IFile) ((IAdaptable) object).getAdapter(IFile.class);
							}
						}
					}
				}
			}
			
			if (su != null && file == null) {
				if (su.getResource() instanceof IFile) {
					file = (IFile) su.getResource();
				}
				else {
					final FileUtil fileUtil = FileUtil.getFileUtil(su.getResource());
					if (fileUtil != null) {
						uri = fileUtil.getURI();
					}
				}
			}
			else if (su == null && file != null) {
				su = LTK.getSourceUnitManager().getSourceUnit(
						LTK.PERSISTENCE_CONTEXT, file, null, true, null );
			}
			if (file != null && uri == null) {
				uri = file.getLocationURI();
			}
			
			if (uri != null) {
				if (su != null) {
					while (su != null && su.getWorkingContext() != LTK.PERSISTENCE_CONTEXT) {
						su = su.getUnderlyingUnit();
					}
				}
				
				String command = null;
				if (file != null) {
					if (contentTypeId == null) {
						contentTypeId= LaunchShortcutUtil.getContentTypeId(file);
					}
					command = (fileCommandId != null) ?
							RCodeLaunching.getFileCommand(fileCommandId) :
							RCodeLaunching.getPreferredFileCommand(contentTypeId);
				}
				else { // uri
					if (contentTypeId == null) {
						contentTypeId= LaunchShortcutUtil.getContentTypeId(uri);
					}
					command = (fileCommandId != null) ?
							RCodeLaunching.getFileCommand(fileCommandId) :
							RCodeLaunching.getPreferredFileCommand(contentTypeId);
				}
				
				if (command != null) {
					RCodeLaunching.runFileUsingCommand(command, uri, su,
							getEncoding(encodingAdaptable, file), fGotoConsole );
					return null;
				}
			}
		}
		catch (final Exception e) {
			LaunchShortcutUtil.handleRLaunchException(e,
					RLaunchingMessages.RScriptLaunch_error_message, event);
			return null;
		}
		
		LaunchShortcutUtil.handleUnsupportedExecution(event);
		return null;
	}
	
	private String getEncoding(final IAdaptable adaptable, final IFile file) throws CoreException {
		if (adaptable != null) {
			final IEncodingSupport encodingSupport = (IEncodingSupport) adaptable.getAdapter(IEncodingSupport.class);
			if (encodingSupport != null) {
				return encodingSupport.getEncoding();
			}
		}
		if (file != null) {
			return file.getCharset(true); 
		}
		return null;
	}
	
}
