/*******************************************************************************
 * Copyright (c) 2007-2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons;

import java.net.URI;

import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.osgi.util.NLS;

import de.walware.ecommons.internal.workspace.Messages;

import de.walware.statet.base.core.StatetCore;
import de.walware.statet.base.internal.core.BaseCorePlugin;


/**
 * A configurable resource validator.
 * 
 * Validates <code>String</code> (with variables) representing a local file path
 * or a URI and file handles of type <code>IFileStore</code> and
 * <code>IResource</code> (for Workspace resources).
 */
public class FileValidator implements IValidator {
	
	
	private Object fExplicitObject;
	private boolean fInCheck = false;
	private IResource fWorkspaceResource;
	private IFileStore fFileStore;
	private IStatus fStatus;
	
	private String fResourceLabel = " "; //$NON-NLS-1$
	private int fOnEmpty;
	private int fOnNotExisting;
	private int fOnExisting;
	private int fOnLateResolve;
	private int fOnFile;
	private int fOnDirectory;
	private int fOnNotLocal;
	private boolean fIgnoreRelative;
	
	
	/**
	 * 
	 */
	public FileValidator() {
		fOnNotExisting = IStatus.OK;
		fOnExisting = IStatus.OK;
		fOnEmpty = IStatus.ERROR;
		fOnLateResolve = IStatus.ERROR;
		fOnFile = IStatus.OK;
		fOnDirectory = IStatus.OK;
		fOnNotLocal = IStatus.ERROR;
		fIgnoreRelative = false;
	}
	
	/**
	 * New validator initialized with specified default mode
	 * ({@link #setDefaultMode(boolean)})
	 */
	public FileValidator(final boolean existingResource) {
		this();
		setDefaultMode(existingResource);
	}
	
	public void setDefaultMode(final boolean existingResource) {
		fOnNotExisting = (existingResource) ? IStatus.ERROR : IStatus.OK;
		fOnExisting = (existingResource) ? IStatus.OK : IStatus.WARNING;
	}
	
	
	public void setOnEmpty(final int severity) {
		fOnEmpty = severity;
		fStatus = null;
	}
	public int getOnEmpty() {
		return fOnEmpty;
	}
	
	public void setOnExisting(final int severity) {
		fStatus = null;
		fOnExisting = severity;
	}
	public int getOnExisting() {
		return fOnExisting;
	}
	
	public void setOnNotExisting(final int severity) {
		fStatus = null;
		fOnNotExisting = severity;
	}
	public int getOnNotExisting() {
		return fOnNotExisting;
	}
	
	public void setOnLateResolve(final int severity) {
		fStatus = null;
		fOnLateResolve = severity;
	}
	public int getOnLateResolve() {
		return fOnLateResolve;
	}
	
	public void setOnFile(final int severity) {
		fStatus = null;
		fOnFile = severity;
	}
	public int getOnFile() {
		return fOnFile;
	}
	
	public void setOnDirectory(final int severity) {
		fStatus = null;
		fOnDirectory = severity;
	}
	public int getOnDirectory() {
		return fOnDirectory;
	}
	
	public void setOnNotLocal(final int severity) {
		fStatus = null;
		fOnNotLocal = severity;
	}
	public int getOnNotLocal() {
		return fOnNotLocal;
	}
	public void setIgnoreRelative(final boolean ignore) {
		fIgnoreRelative = ignore;
		fStatus = null;
	}
	
	public void setResourceLabel(final String label) {
		fResourceLabel = " '" + label + "' "; //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * Sets explicitly the object to validate.
	 * A <code>null</code> value stops the explicit mode.  If the value is set
	 * explicitly, the value specified in the validate(...) methods is ignored.
	 * @param value the resource to validate or <code>null</code>.
	 */
	public void setExplicit(final Object value) {
		fFileStore = null;
		fWorkspaceResource = null;
		fStatus = null;
		fExplicitObject = value;
	}
	
	public IStatus validate(final Object value) {
		if (!checkExplicit()) {
			doValidate(value);
		}
		return fStatus;
	}
	
	private boolean checkExplicit() {
		if (fExplicitObject != null) {
			if (fStatus == null) {
				doValidate(fExplicitObject);
			}
			return true;
		}
		return false;
	}
	
	private void doValidate(final Object value) {
		if (!fInCheck) {
			fInCheck = true;
			try {
				fStatus = doValidate1(value);
			}
			catch (final Exception e) {
				BaseCorePlugin.logError(-1, NLS.bind("An error occurred when validating resource path ({0}).", value), e);
			}
			finally {
				fInCheck = false;
			}
		}
	}
	
	private IStatus doValidate1(Object value) {
		fFileStore = null;
		fWorkspaceResource = null;
		
		// Resolve string
		if (value instanceof IPath) {
			value = ((IPath) value).toOSString();
		}
		if (value instanceof String) {
			String s = (String) value;
			if (s.length() == 0) {
				return createStatus(fOnEmpty, Messages.Resource_error_NoInput_message, null);
			}
			try {
				s = resolveExpression(s);
			} catch (final CoreException e) {
				return createStatus(e.getStatus().getSeverity(), Messages.Resource_error_Other_message, e.getStatus().getMessage());
			}
			if (s.length() == 0) {
				return createStatus(fOnEmpty, Messages.Resource_error_NoInput_message, null);
			}
			if (fIgnoreRelative && !new Path(s).isAbsolute()) {
				return Status.OK_STATUS;
			}
			
			// search efs reference
			try {
				fFileStore = FileUtil.getFileStore(s);
				if (fFileStore == null) {
					return createStatus(IStatus.ERROR, Messages.Resource_error_NoValidSpecification_message, null);
				}
			}
			catch (final CoreException e) {
				return createStatus(IStatus.ERROR, Messages.Resource_error_NoValidSpecification_message, e.getStatus().getMessage());
			}
			
			// search file in workspace 
			if (fFileStore != null) {
				final IResource[] resources = (fFileStore.fetchInfo().isDirectory()) ? 
						ResourcesPlugin.getWorkspace().getRoot().findContainersForLocationURI(fFileStore.toURI()) :
						ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(fFileStore.toURI());
				if (resources.length > 0) {
					fWorkspaceResource = resources[0];
				}
			}
		}
		
		if (value instanceof IFileStore) {
			fFileStore = (IFileStore) value;
		}
		else if (value instanceof IResource) {
			fWorkspaceResource = (IResource) value;
		}
		
		
		if (fFileStore != null) {
			return validateFileStore();
		}
		else if (fWorkspaceResource != null) {
			return validateWorkspaceResource();
		}
		else {
			throw new IllegalArgumentException();
		}
	}
	
	protected String resolveExpression(final String expression) throws CoreException {
		final IStringVariableManager manager = VariablesPlugin.getDefault().getStringVariableManager();
		try {
			return manager.performStringSubstitution(expression);
		}
		catch (final CoreException e) {
			manager.validateStringVariables(expression); // throws invalid variable
			throw new CoreException(new Status(fOnLateResolve, e.getStatus().getPlugin(), e.getStatus().getMessage())); // throws runtime variable
		}
	}
	
	private IResource findWorkspaceResource(final URI location) {
		final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IResource[] found = null;
		if (fOnFile != IStatus.ERROR) {
			found = root.findFilesForLocationURI(location);
		}
		if ((found == null || found.length == 0)
				&& fOnDirectory != IStatus.ERROR) {
			found = root.findContainersForLocationURI(location);
		}
		if (found != null && found.length > 0) {
			return found[0];
		}
		return null;
	}
	
	protected IStatus validateWorkspaceResource() {
		IStatus status = Status.OK_STATUS;
		if (fOnNotLocal != IStatus.OK) {
			if (!isLocalFile()) {
				status = createStatus(fOnNotLocal, Messages.Resource_error_NotLocal_message, null);
			}
			if (status.getSeverity() == IStatus.ERROR) {
				return status;
			}
		}
		if (fOnExisting != IStatus.OK || fOnNotExisting != IStatus.OK || fOnFile != IStatus.OK || fOnDirectory != IStatus.OK) {
			status = StatusUtil.getMoreSevere(status,
					createExistsStatus(fWorkspaceResource.exists(), (fWorkspaceResource instanceof IContainer)) );
		}
		return status;
	}
	
	protected IStatus validateFileStore() {
		IStatus status = Status.OK_STATUS;
		if (fOnNotLocal != IStatus.OK) {
			if (!isLocalFile()) {
				status = createStatus(fOnNotLocal, Messages.Resource_error_NotLocal_message, null);
			}
			if (status.getSeverity() == IStatus.ERROR) {
				return status;
			}
		}
		if (fOnExisting != IStatus.OK || fOnNotExisting != IStatus.OK) {
			final IFileInfo info = fFileStore.fetchInfo();
			status = StatusUtil.getMoreSevere(status,
					createExistsStatus(info.exists(), info.isDirectory()) );
		}
		return status;
	}
	
	private IStatus createExistsStatus(final boolean exists, final boolean isDirectory) {
		if (exists) {
			IStatus status = createStatus(fOnExisting, Messages.Resource_error_AlreadyExists_message, null);
			if (status.getSeverity() < fOnDirectory && isDirectory) {
				status = createStatus(fOnDirectory, Messages.Resource_error_IsDirectory_message, null);
			}
			if (status.getSeverity() < fOnFile && !isDirectory) {
				status = createStatus(fOnFile, Messages.Resource_error_IsFile_message, null);
			}
			return status;
		}
		else {
			return createStatus(fOnNotExisting, Messages.Resource_error_DoesNotExists_message, null);
		}
	}
	
	protected IStatus createStatus(final int severity, final String message, String detail) {
		if (severity == IStatus.OK) {
			return Status.OK_STATUS;
		}
		if (detail == null) {
			detail = ""; //$NON-NLS-1$
		}
		return new Status(severity, StatetCore.PLUGIN_ID, NLS.bind(message, fResourceLabel, detail));
	}
	
	
	public IFileStore getFileStore() {
		checkExplicit();
		if (fFileStore == null && fWorkspaceResource != null) {
			try {
				fFileStore = EFS.getStore(fWorkspaceResource.getLocationURI());
			} catch (final CoreException e) {
			}
		}
		return fFileStore;
	}
	
	public IResource getWorkspaceResource() {
		checkExplicit();
		if (fWorkspaceResource == null && fFileStore != null) {
			fWorkspaceResource = findWorkspaceResource(fFileStore.toURI());
		}
		return fWorkspaceResource;
	}
	
	public boolean isLocalFile() {
		final IFileStore fileStore = getFileStore();
		if (fileStore != null) {
			return fileStore.getFileSystem().equals(EFS.getLocalFileSystem());
		}
		return false;
	}
	
	public IStatus getStatus() {
		checkExplicit();
		return fStatus;
	}
	
}
