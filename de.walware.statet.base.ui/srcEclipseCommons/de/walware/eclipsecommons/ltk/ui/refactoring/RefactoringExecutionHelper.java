/*******************************************************************************
 * Copyright (c) 2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.eclipsecommons.ltk.ui.refactoring;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.PerformChangeOperation;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.internal.ui.refactoring.ChangeExceptionHandler;
import org.eclipse.ltk.ui.refactoring.RefactoringUI;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.progress.IProgressService;

import de.walware.eclipsecommons.ltk.core.refactoring.IScheduledRefactoring;
import de.walware.eclipsecommons.ltk.internal.ui.refactoring.RefactoringMessages;


/**
 * A helper class to execute a refactoring. The class takes care of pushing the
 * undo change onto the undo stack and folding editor edits into one editor
 * undo object.
 */
public class RefactoringExecutionHelper {
	
	
	private final Refactoring fRefactoring;
	
	private final IProgressService fExecContext;
	
	private final Shell fParent;
	private final int fStopSeverity;
	
	
	/**
	 * @param refactoring
	 * @param stopSeverity a refactoring status constant from {@link RefactoringStatus}
	 * @param parent
	 * @param context
	 */
	public RefactoringExecutionHelper(final Refactoring refactoring, final int stopSeverity, final Shell parent, final IProgressService context) {
		assert (refactoring != null);
		assert (parent != null);
		assert (context != null);
		
		fRefactoring = refactoring;
		fStopSeverity = stopSeverity;
		fParent = parent;
		fExecContext = context;
	}
	
	
	/**
	 * Must be called in the UI thread.<br>
	 * <strong>Use {@link #perform(boolean, boolean)} unless you know exactly what you are doing!</strong>
	 * 
	 * @param forkChangeExecution if the change should not be executed in the UI thread: This may not work in any case 
	 * @param cancelable  if set, the operation will be cancelable
	 * @throws InterruptedException thrown when the operation is cancelled
	 * @throws InvocationTargetException thrown when the operation failed to execute
	 */
	public void perform(final boolean forkChangeExecution, final boolean cancelable) throws InterruptedException, InvocationTargetException {
		final AtomicReference<PerformChangeOperation> op = new AtomicReference<PerformChangeOperation>();
		try {
			fExecContext.busyCursorWhile(new IRunnableWithProgress() {
				public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					final SubMonitor progress = SubMonitor.convert(monitor, fRefactoring.getName(), 10);
					
					final IJobManager manager =  Job.getJobManager();
					final Thread workingThread = Thread.currentThread();
					final ISchedulingRule rule = (fRefactoring instanceof IScheduledRefactoring) ?
							((IScheduledRefactoring) fRefactoring).getSchedulingRule() :
							ResourcesPlugin.getWorkspace().getRoot();
					
					manager.beginRule(rule, progress.newChild(1));
					PerformChangeOperation operation = null;
					try {
						if (cancelable && monitor.isCanceled()) {
							throw new InterruptedException();
						}
						
						fRefactoring.setValidationContext(fParent);
						
						final RefactoringStatus status = fRefactoring.checkAllConditions(
								progress.newChild(1, SubMonitor.SUPPRESS_NONE));
						if (status.getSeverity() >= fStopSeverity) {
							final AtomicBoolean canceled = new AtomicBoolean();
							fParent.getDisplay().syncExec(new Runnable() {
								public void run() {
									final Dialog dialog = RefactoringUI.createRefactoringStatusDialog(status, fParent, fRefactoring.getName(), false);
									final int selection = dialog.open();
									canceled.set(selection == IDialogConstants.CANCEL_ID);
								}
							});
							if (canceled.get()) {
								throw new OperationCanceledException();
							}
						}
						
						final Change change = fRefactoring.createChange(
								progress.newChild(2, SubMonitor.SUPPRESS_NONE) );
						change.initializeValidationData(
								progress.newChild(1, SubMonitor.SUPPRESS_NONE) );
						
						operation = new PerformChangeOperation(change);
						operation.setUndoManager(RefactoringCore.getUndoManager(), fRefactoring.getName());
						operation.setSchedulingRule(rule);
						
						if (cancelable && monitor.isCanceled()) {
							throw new InterruptedException();
						}
						op.set(operation);
						
						if (forkChangeExecution) {
							operation.run(progress.newChild(4, SubMonitor.SUPPRESS_NONE) );
						}
						else {
							final AtomicReference<Exception> opException = new AtomicReference<Exception>();
							final Runnable runnable = new Runnable() {
								public void run() {
									try {
										final PerformChangeOperation operation = op.get();
										operation.run(progress.newChild(4, SubMonitor.SUPPRESS_NONE) );
									}
									catch (final CoreException e) {
										opException.set(e);
									}
									catch (final RuntimeException e) {
										opException.set(e);
									}
									finally {
										manager.transferRule(rule, workingThread);
									}
								}
							};
							final Display display = fParent.getDisplay();
							manager.transferRule(rule, display.getThread());
							display.syncExec(runnable);
							if (opException.get() != null) {
								final Exception e = opException.get();
								if (e instanceof CoreException) {
									throw (CoreException) e;
								}
								if (e instanceof RuntimeException) {
									throw (RuntimeException) e;
								}
							}
						}
						
						final RefactoringStatus validationStatus = operation.getValidationStatus();
						if (validationStatus != null && validationStatus.hasFatalError()) {
							MessageDialog.openError(fParent, fRefactoring.getName(), NLS.bind(
									RefactoringMessages.ExecutionHelper_CannotExecute_message, 
									validationStatus.getMessageMatchingSeverity(RefactoringStatus.FATAL) ));
							return;
						}
					}
					catch (final OperationCanceledException e) {
						throw new InterruptedException(e.getMessage());
					}
					catch (final CoreException e) {
						throw new InvocationTargetException(e);
					}
					catch (final RuntimeException e) {
						throw new InvocationTargetException(e);
					}
					finally {
						manager.endRule(rule);
						fRefactoring.setValidationContext(null);
					}
				}
				
			});
		}
		catch (final InvocationTargetException e) {
			final PerformChangeOperation operation = op.get();
			if (operation != null && operation.changeExecutionFailed()) {
				final ChangeExceptionHandler handler = new ChangeExceptionHandler(fParent, fRefactoring);
				final Throwable inner = e.getTargetException();
				if (inner instanceof RuntimeException) {
					handler.handle(operation.getChange(), (RuntimeException) inner);
				}
				else if (inner instanceof CoreException) {
					handler.handle(operation.getChange(), (CoreException) inner);
				}
				else {
					throw e;
				}
			}
			else {
				throw e;
			}
		}
	}
	
}
