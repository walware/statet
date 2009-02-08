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

package de.walware.ecommons.ltk;

import java.util.List;


/**
 * Accept problems by a problem checker.
 * 
 * {@link ISourceUnit#getProblemRequestor()}
 */
public interface IProblemRequestor {
	
	/**
	 * Notification of a problem.
	 * 
	 * @param type the category of discovered problems.
	 * @param problem The discovered problems.
	 */
	public void acceptProblems(String type, List<IProblem> problems);
	
	/**
	 * Notification sent before starting the problem detection process.
	 * Typically, this would tell a problem collector to clear previously recorded problems.
	 */
	public void beginReportingSequence();
	
	/**
	 * Notification sent after having completed problem detection process.
	 * Typically, this would tell a problem collector that no more problems should be expected in this
	 * iteration.
	 */
	public void endReportingSequence();
	
}
