/*******************************************************************************
 * Copyright (c) 2009-2010 WalWare/RJ-Project (www.walware.de/opensource).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.rj.services;

import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import de.walware.rj.data.RObject;
import de.walware.rj.data.RReference;


/**
 * A interface with methods to evaluate and exchange data with R.
 * <p>
 * It depends on the application how to get access to the RService.
 * In StatET it is passed as adapter argument in 
 * {@link de.walware.statet.nico.core.runtime.IToolRunnable#run(de.walware.statet.nico.core.runtime.IToolRunnableControllerAdapter, IProgressMonitor) IToolRunnable#run(...)},
 * if the R console supports the featureset {@link de.walware.statet.r.nico.RTool#R_DATA_FEATURESET_ID RTool#R_DATA_FEATURESET_ID}.
 * In applications using R in the background by the RServi library, 
 * the {@link de.walware.rj.servi.RServi RServi} object provides the RService methods.</p>
 * <p>
 * If the application has also an console, the operations of an RService
 * usually doesn't appears in the console. Even not recommended, it is possible
 * to call special R functions like print which output is written to the console.
 * It should absolutely avoided to use functions like <code>readline</code> which requires 
 * interaction with the console.</p>
 * <p>
 * The methods of an RService should not be called concurrently by different
 * threads. The RService consumer have to make sure that the function calls
 * are synchronized, if multiple thread have access to the same instance.
 * Implementations of RService interface can perform checks too, but the consumer 
 * must not rely on that.</p>
 * <p>
 * Especially for longer evaluations, it is recommended that the application implements
 * this synchronization in a way that the GUI is not blocked.
 * In StatET both is guaranteed by a queue and an single execution thread for runnables.</p>
 * <p>
 * All data exchange methods are copy operations. So changes on R data objects in Java
 * are not reflected in R and the other way round.</p>
 * <p>
 * In general it is not necessary to surround R expressions with try-catch or similar construction
 * except the error object is expected as return value.</p>
 */
public interface RService {
	
	RPlatform getPlatform();
	
	/**
	 * Performs the evaluation of the given expression in R without returning a value.
	 * The method returns after the evaluation is finished.
	 * <p>
	 * The evaluation is performed in the global environment of R.</p>
	 * 
	 * @param expression a single valid R expression to evaluate
	 * @param monitor a progress monitor to catch cancellation and provide progress feedback.
	 * @throws CoreException if the operation was canceled or failed; the status
	 *     of the exception contains detail about the cause
	 */
	void evalVoid(String expression, IProgressMonitor monitor) throws CoreException;
	
	/**
	 * Performs the evaluation of the given expression in R and returns its value as R data object.
	 * The method returns after the evaluation is finished.
	 * <p>
	 * This is a short version of {@link #evalData(String, String, int, int, IProgressMonitor)}
	 * sufficient for most purpose. The returned R data objects are created by the default factory
	 * with no limit in the object tree depth.</p>
	 * <p>
	 * The evaluation is performed in the global environment of R.</p>
	 * 
	 * @param expression a single valid R expression to evaluate
	 * @param monitor a progress monitor to catch cancellation and provide progress feedback
	 * @return the evaluated value as R data object
	 * @throws CoreException if the operation was canceled or failed; the status
	 *     of the exception contains detail about the cause
	 */
	RObject evalData(String expression, IProgressMonitor monitor) throws CoreException;
	
	/**
	 * Performs the evaluation of the given expression in R and returns its value. The method returns
	 * after the evaluation is finished.
	 * <p>
	 * This method allows advanced configuration for the returned R data object.</p>
	 * <p>
	 * The evaluation is performed in the global environment of R.</p>
	 * 
	 * @param expression a single valid R expression to evaluate
	 * @param factoryId the id of the factory to use when creating the RObject in this VM.
	 * @param options 0
	 * @param depth object tree depth for the created return value
	 * @param monitor a progress monitor to catch cancellation and provide progress feedback
	 * @return the evaluated value as R data object
	 * @throws CoreException if the operation was canceled or failed; the status
	 *     of the exception contains detail about the cause
	 */
	RObject evalData(String expression, String factoryId, int options, int depth, IProgressMonitor monitor) throws CoreException;
	
	RObject evalData(RReference reference, IProgressMonitor monitor) throws CoreException;
	RObject evalData(RReference reference, String factoryId, int options, int depth, IProgressMonitor monitor) throws CoreException;
	
	/**
	 * Performs the assignment of the given R data object to an expression in R. The method returns
	 * after the assignment is finished.
	 * <p>
	 * The name must be a valid target expression for a R <code>&lt;-</code> assignment operation.
	 * A single symbol like <code>x</code> or <code>`x-y`</code>, a path in an object tree
	 * like <code>xlist$item1</code> or <code>xobj@slotName</code> is valid as well as
	 * special function calls which supports assignments like <code>dim(x)</code>.</p>
	 * <p>
	 * The assignment is performed in the global environment of R.</p>
	 * 
	 * @param expression a single valid expression to assign the data to
	 * @param data a valid R data object to assign to the expression
	 * @param monitor a progress monitor to catch cancellation and provide progress feedback
	 * @return the evaluated value as R data object
	 * @throws CoreException if the operation was canceled or was failed; the status
	 *     of the exception contains detail about the cause
	 */
	void assignData(String expression, RObject data, IProgressMonitor monitor) throws CoreException;
	
//	void assignDataToAttribute(String expression, String attributeName, RObject data, IProgressMonitor monitor) throws CoreException;
	
	/**
	 * Uploads a file or an other input stream to a file on the R host system.
	 * <p>
	 * The file name can be relative or absolute. A relative file name is handled relative
	 * to the current R working directory. An absolute file name must be a valid absolute
	 * path on the R host system.</p>
	 * <p>
	 * The input stream is not closed by the service.</p>
	 * <p>
	 * Typical pattern to upload a local file is:</p>
	 * <pre>
	 *     FileInputStream in = null;
	 *     try {
	 *         in = new FileInputStream(localfile);
	 *         rservice.uploadFile(in, "data.xml", monitor);
	 *     }
	 *     finally {
	 *         if (in != null) {
	 *             in.close();
	 *         }
	 *     }
	 * </pre>
	 * 
	 * @param in an input stream providing the content of the file
	 * @param length the length of the content
	 * @param fileName the name of the file on the R host system
	 * @param options 0
	 * @param monitor a progress monitor to catch cancellation and provide progress feedback
	 * @throws CoreException if the operation was canceled or failed; the status
	 *     of the exception contains detail about the cause
	 */
	void uploadFile(InputStream in, long length, String fileName, int options, IProgressMonitor monitor) throws CoreException;
	
//	void uploadFile(byte[], long length, String fileName, int options, IProgressMonitor monitor) throws CoreException;
	
	/**
	 * Downloads a file on the R host system to a local file or another output stream.
	 * <p>
	 * The file name can be relative or absolute. A relative file name is handled relative
	 * to the current R working directory. An absolute file name must be a valid absolute
	 * path on the R host system.</p>
	 * <p>
	 * The output stream is not closed by the service.</p>
	 * <p>
	 * Typical pattern to download to a local file is:</p>
	 * <pre>
	 *     FileOutputStream out = null;
	 *     try {
	 *         out = new FileOutputStream(localfile);
	 *         rservice.downloadFile(out, "data.xml", 0, monitor);
	 *     }
	 *     finally {
	 *         if (out != null) {
	 *             out.close();
	 *         }
	 *     }
	 * </pre>
	 * 
	 * @param out the output stream to write the content of the file to
	 * @param fileName the name of the file on the R host system
	 * @param options 0
	 * @param monitor a progress monitor to catch cancellation and provide progress feedback
	 * @throws CoreException if the operation was canceled or failed; the status
	 *     of the exception contains detail about the cause
	 */
	void downloadFile(OutputStream out, String fileName, int options, IProgressMonitor monitor) throws CoreException;
	
	/**
	 * Downloads a file on the R host system into a byte array.
	 * <p>
	 * The file name can be relative or absolute. A relative file name is handled relative
	 * to the current R working directory. An absolute file name must be a valid absolute
	 * path on the R host system.</p>
	 * <p>
	 * The byte array represents the content of the complete file; the array has the length of
	 * the file.</p>
	 * 
	 * @param fileName the name of the file on the R host system
	 * @param options 0
	 * @param monitor a progress monitor to catch cancellation and provide progress feedback
	 * @return the file content
	 * @throws CoreException if the operation was canceled or failed; the status
	 *     of the exception contains detail about the cause
	 */
	byte[] downloadFile(String fileName, int options, IProgressMonitor monitor) throws CoreException;
	
	/**
	 * Creates a new function call builder for the specified function.
	 * <p>
	 * The builder is valid as long as the RService owns the consumer. After the service is for example
	 * closed, it must not longer be used.</p>
	 * 
	 * @name name of the function
	 * @throws CoreException if the operation was canceled or failed; the status
	 *     of the exception contains detail about the cause
	 */
	FunctionCall createFunctionCall(String name);
	
}
