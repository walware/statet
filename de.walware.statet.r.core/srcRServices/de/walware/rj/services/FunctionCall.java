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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import de.walware.rj.data.RObject;
import de.walware.rj.data.RStore;


/**
 * A function call provides a comfortable way to build and execute
 * R function call.
 * 
 * <p>A function call builder for a given function can be created by 
 * {@link RService#createFunctionCall(String)}.</p>
 * 
 * <p>The builder mainly provides methods to add arguments and to
 * finally evaluate the resulting function call in R.</p>
 * 
 * <p>Arguments are added by the <code>#add...()</code> methods. The
 * order they are added is exactly the order they are send to R. There
 * are add methods for a symbol referring to data existing in R, 
 * for given R data objects and for single data values which are
 * transformed automatically from java primitives into R data objects.
 * All methods are available in a variant with and without a parameter 
 * for the R argument name. An unnamed argument can be specified by
 * using the variant without the argument name parameter or by specifying
 * the name as <code>null</code>.</p>
 * 
 * <p>The common guidelines in {@link RService} (like concurrency) are effective
 * for all evaluation methods in this interface.</p>
 */
public interface FunctionCall {
	
	
	/**
	 * Adds an argument with the given expression as value.
	 * 
	 * <p>The expression is used directly as value without any transformation.</p>
	 * 
	 * @param arg the name of the argument or <code>null</code> for unnamed
	 * @param expression a single valid expression
	 * @return a reference to this object
	 */
	FunctionCall add(String arg, String expression);
	
	/**
	 * Adds a unnamed argument with the given expression as value.
	 * 
	 * <p>The expression is used directly as value without any transformation.</p>
	 * 
	 * @param expression a single valid expression
	 * @return a reference to this object
	 */
	FunctionCall add(String expression);
	
	/**
	 * Adds a argument with the given R data object as value.
	 * 
	 * @param arg the name of the argument or <code>null</code> for unnamed
	 * @param data an R data object
	 * @return a reference to this object
	 */
	FunctionCall add(String arg, RObject data);
	
	/**
	 * Adds an unnamed argument with the given R data object as value.
	 * 
	 * @param arg the name of the argument
	 * @param data an R data object
	 * @return a reference to this object
	 */
	FunctionCall add(RObject data);
	
	
	/**
	 * Adds an argument with the given boolean/logical as value.
	 * 
	 * <p>The Java boolean value is transformed into an R data object 
	 * of type {@link RStore#LOGICAL logical}.</p>
	 * 
	 * @param arg the name of the argument or <code>null</code> for unnamed
	 * @param logical the value of the argument
	 * @return a reference to this object
	 */
	FunctionCall addLogi(String arg, boolean logical);
	
	/**
	 * Adds an unnamed argument with the given boolean/logical as value.
	 * 
	 * <p>The Java boolean value is transformed into an R data object 
	 * of type {@link RStore#LOGICAL logical}.</p>
	 * 
	 * @param logical the value of the argument
	 * @return a reference to this object
	 */
	FunctionCall addLogi(boolean logical);
	
	/**
	 * Adds an argument with the given integer as value.
	 * 
	 * <p>The Java integer value is transformed into an R data object 
	 * of type {@link RStore#INTEGER integer}.</p>
	 * 
	 * @param arg the name of the argument or <code>null</code> for unnamed
	 * @param integer the value of the argument
	 * @return a reference to this object
	 */
	FunctionCall addInt(String arg, int integer);
	
	/**
	 * Adds an unnamed argument with the given integer as value.
	 * 
	 * <p>The Java integer value is transformed into an R data object 
	 * of type {@link RStore#INTEGER integer}.</p>
	 * 
	 * @param integer the value of the argument
	 * @return a reference to this object
	 */
	FunctionCall addInt(int integer);
	
	/**
	 * Adds an argument with the given double/numeric as value.
	 * 
	 * <p>The Java double value is transformed into an R data object 
	 * of type {@link RStore#NUMERIC numeric}, also called real.</p>
	 * 
	 * @param arg the name of the argument or <code>null</code> for unnamed
	 * @param numeric the value of the argument
	 * @return a reference to this object
	 */
	FunctionCall addNum(String arg, double numeric);
	
	/**
	 * Adds an unnamed argument with the given double/numeric as value.
	 * 
	 * <p>The Java double value is transformed into an R data object 
	 * of type {@link RStore#NUMERIC numeric}, also called real.</p>
	 * 
	 * @param arg the name of the argument
	 * @param numeric the value of the argument
	 * @return a reference to this object
	 */
	FunctionCall addNum(double numeric);
	
	/**
	 * Adds an argument with the given string/character as value.
	 * 
	 * <p>The Java String value is transformed into an R data object 
	 * of type {@link RStore#CHARACTER character}.</p>
	 * 
	 * @param arg the name of the argument or <code>null</code> for unnamed
	 * @param character the value of the argument
	 * @return a reference to this object
	 */
	FunctionCall addCplx(String arg, double real, double imaginary);
	
	/**
	 * Adds an unnamed argument with the given string/character as value.
	 * 
	 * <p>The Java String value is transformed into an R data object 
	 * of type {@link RStore#CHARACTER character}.</p>
	 * 
	 * @param character the value of the argument
	 * @return a reference to this object
	 */
	FunctionCall addCplx(double real, double imaginary);
	
	/**
	 * Adds an argument with the given string/character as value.
	 * 
	 * <p>The Java String value is transformed into an R data object 
	 * of type {@link RStore#CHARACTER character}.</p>
	 * 
	 * @param arg the name of the argument or <code>null</code> for unnamed
	 * @param character the value of the argument
	 * @return a reference to this object
	 */
	FunctionCall addChar(String arg, String character);
	
	/**
	 * Adds an unnamed argument with the given string/character as value.
	 * 
	 * <p>The Java String value is transformed into an R data object 
	 * of type {@link RStore#CHARACTER character}.</p>
	 * 
	 * @param character the value of the argument
	 * @return a reference to this object
	 */
	FunctionCall addChar(String character);
	
	/**
	 * Adds an argument with the given NULL as value.
	 * 
	 * <p>The Java String value is transformed into an R data 
	 * {@link RObject#NULL NULL} object.</p>
	 * 
	 * @param arg the name of the argument or <code>null</code> for unnamed
	 * @return a reference to this object
	 */
	FunctionCall addNull(String arg);
	
	/**
	 * Adds an unnamed argument with the given NULL as value.
	 * 
	 * <p>The Java String value is transformed into an R data 
	 * {@link RObject#NULL NULL} object.</p>
	 * 
	 * @return a reference to this object
	 */
	FunctionCall addNull();
	
	
	/**
	 * Performs the evaluation of this function call in R without returning a value.
	 * The method returns after the evaluation is finished.
	 * 
	 * <p>The evaluation is performed in the global environment of R.</p>
	 * 
	 * @param monitor a progress monitor to catch cancellation and provide progress feedback.
	 * @throws CoreException if the operation was canceled or failed; the status
	 *     of the exception contains detail about the cause
	 */
	void evalVoid(IProgressMonitor monitor) throws CoreException;
	
	/**
	 * Performs the evaluation of the this function call in R and returns its value as R data object.
	 * The method returns after the evaluation is finished.
	 * 
	 * <p>This is a short version of {@link #evalData(String, int, int, IProgressMonitor)}
	 * sufficient for most purpose. The returned R data objects are created by the default factory
	 * with no limit in the object tree depth.</p>
	 * 
	 * <p>The evaluation is performed in the global environment of R.</p>
	 * 
	 * @param monitor a progress monitor to catch cancellation and provide progress feedback
	 * @return the evaluated value as R data object
	 * @throws CoreException if the operation was canceled or failed; the status
	 *     of the exception contains detail about the cause
	 */
	RObject evalData(IProgressMonitor monitor) throws CoreException;
	
	/**
	 * Performs the evaluation of the this function call in R and returns its value as R data object.
	 * The method returns after the evaluation is finished.
	 * 
	 * <p>This method allows advanced configuration for the returned R data object.</p>
	 * 
	 * <p>The evaluation is performed in the global environment of R.</p>
	 * 
	 * @param factoryId the id of the factory to use when creating the RObject in this VM.
	 * @param options 0
	 * @param depth object tree depth for the created return value
	 * @param monitor a progress monitor to catch cancellation and provide progress feedback
	 * @return the evaluated value as R data object
	 * @throws CoreException if the operation was canceled or failed; the status
	 *     of the exception contains detail about the cause
	 */
	RObject evalData(String factoryId, int options, int depth, IProgressMonitor monitor) throws CoreException;
	
//	void evalAssign(IProgressMonitor monitor);
	
}
