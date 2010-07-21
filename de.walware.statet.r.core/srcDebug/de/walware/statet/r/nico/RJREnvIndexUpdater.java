/*******************************************************************************
 * Copyright (c) 2010 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.nico;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;

import de.walware.rj.data.RArray;
import de.walware.rj.data.RCharacterStore;
import de.walware.rj.data.RDataUtil;
import de.walware.rj.data.RIntegerStore;
import de.walware.rj.data.RList;
import de.walware.rj.data.RObject;
import de.walware.rj.data.RStore;
import de.walware.rj.data.UnexpectedRDataException;
import de.walware.rj.data.defaultImpl.RListImpl;
import de.walware.rj.services.RService;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.RUtil;
import de.walware.statet.r.core.renv.IREnvConfiguration;
import de.walware.statet.r.internal.core.RPackageDescription;
import de.walware.statet.r.internal.core.rhelp.REnvIndexWriter;
import de.walware.statet.r.internal.core.rhelp.REnvIndexWriter.AbortIndexException;
import de.walware.statet.r.internal.core.rhelp.REnvIndexWriter.RdItem;
import de.walware.statet.r.internal.core.rhelp.RHelpWebapp;


/**
 * Updates the R environment (R help keywords, R help index).
 * Uses the RService interface to read the data.
 */
public class RJREnvIndexUpdater {
	
	
	private static final String PKG_DATA_META_NAME = ".name"; //$NON-NLS-1$
	private static final String PKG_DATA_META_ID = ".id"; //$NON-NLS-1$
	
	
	private static String checkNA2Empty(final String s) {
		return (s != null && !s.equals("NA") && s.length() > 0) ? s : "";
	}
	private static String checkNA2Null(final String s) {
		return (s != null && !s.equals("NA") && s.length() > 0) ? s : null;
	}
	
	
	private static final RList FINISH = new RListImpl(new RObject[0], null);
	
	private class LocalJob extends Job {
		
		
		private final RArray<RCharacterStore> fPkgMatrix;
		
		private final BlockingQueue<RList> fQueue = new ArrayBlockingQueue<RList>(3);
		
		private volatile boolean fFinish;
		
		private volatile Exception fException;
		
		
		public LocalJob(final RArray<RCharacterStore> pkgMatrix) {
			super(NLS.bind("Update R help index for ''{0}''", fREnvConfig.getName()));
			setPriority(Job.LONG);
			setSystem(true);
			
			fPkgMatrix = pkgMatrix;
		}
		
		public void add(final RList pkgData, final SubMonitor progress) throws Exception {
			try {
				if (fException != null) {
					throw fException;
				}
				fQueue.put(pkgData);
			}
			catch (final InterruptedException e) {
				Thread.interrupted();
				if (progress.isCanceled()) {
					cancel();
					throw new CoreException(Status.CANCEL_STATUS);
				}
			}
		}
		
		public void finish(final SubMonitor progress) throws CoreException {
			try {
				fQueue.add(FINISH);
				join();
			}
			catch (final InterruptedException e) {
				Thread.interrupted();
				if (progress.isCanceled()) {
					cancel();
					throw new CoreException(Status.CANCEL_STATUS);
				}
			}
		}
		
		public void cancel(final SubMonitor progress) {
			cancel();
			while (true) {
				try {
					join();
					return;
				}
				catch (final InterruptedException e) {
					Thread.interrupted();
				}
			}
		}
		
		@Override
		protected void canceling() {
			final Thread thread = getThread();
			if (thread != null) {
				thread.interrupt();
			}
			super.canceling();
		}
		
		@Override
		protected IStatus run(final IProgressMonitor monitor) {
			try {
				final RCharacterStore pkgMatrixData = fPkgMatrix.getData();
				final RIntegerStore pkgMatrixDim = fPkgMatrix.getDim();
				final RStore pkgMatrixColumns = fPkgMatrix.getNames(1);
				final int idxPackage = pkgMatrixColumns.indexOf("Package"); //$NON-NLS-1$
				final int idxTitle = pkgMatrixColumns.indexOf("Title"); //$NON-NLS-1$
				final int idxDescription = pkgMatrixColumns.indexOf("Description"); //$NON-NLS-1$
				final int idxVersion = pkgMatrixColumns.indexOf("Version"); //$NON-NLS-1$
				final int idxPriority = pkgMatrixColumns.indexOf("Priority"); //$NON-NLS-1$
				final int idxAuthor = pkgMatrixColumns.indexOf("Author"); //$NON-NLS-1$
				final int idxMaintainer = pkgMatrixColumns.indexOf("Maintainer"); //$NON-NLS-1$
				final int idxUrl = pkgMatrixColumns.indexOf("URL"); //$NON-NLS-1$
				
				if (idxPackage < 0 || idxTitle < 0 || idxDescription < 0 || idxVersion < 0
						|| idxPriority < 0 || idxAuthor < 0 || idxMaintainer < 0 || idxUrl < 0) {
					throw new UnexpectedRDataException("A column is missing\n." + pkgMatrixColumns);
				}
				
				while (true) {
					String name = null;
					RPackageDescription packageDescription = null;
					try {
						final RList pkgData = fQueue.take();
						if (pkgData == FINISH) {
							return Status.OK_STATUS;
						}
						name = RDataUtil.checkSingleChar(pkgData.get(PKG_DATA_META_NAME));
						final int i = RDataUtil.checkSingleInt(pkgData.get(PKG_DATA_META_ID));
						
						final String title = checkNA2Empty(pkgMatrixData.get(RDataUtil.getDataIdx(pkgMatrixDim, i, idxTitle)));
						final String desription = checkNA2Empty(pkgMatrixData.get(RDataUtil.getDataIdx(pkgMatrixDim, i, idxDescription)));
						final String version = checkNA2Empty(pkgMatrixData.get(RDataUtil.getDataIdx(pkgMatrixDim, i, idxVersion)));
						final String priority = checkNA2Null(pkgMatrixData.get(RDataUtil.getDataIdx(pkgMatrixDim, i, idxPriority)));
						final String author = checkNA2Null(pkgMatrixData.get(RDataUtil.getDataIdx(pkgMatrixDim, i, idxAuthor)));
						final String maintainer = checkNA2Null(pkgMatrixData.get(RDataUtil.getDataIdx(pkgMatrixDim, i, idxMaintainer)));
						final String url = checkNA2Null(pkgMatrixData.get(RDataUtil.getDataIdx(pkgMatrixDim, i, idxUrl)));
						
						packageDescription = new RPackageDescription(
								name, title, desription, version, priority, author, maintainer, url);
						
						fIndex.beginPackage(packageDescription);
						
						processRdData(packageDescription.getName(), pkgData);
					}
					catch (final InterruptedException e) {
						Thread.interrupted();
					}
					catch (final AbortIndexException e) {
						fException = e;
						fQueue.clear();
						return Status.CANCEL_STATUS;
					}
					catch (final Exception e) {
						fIndex.log(new Status(IStatus.ERROR, RCore.PLUGIN_ID, -1, 
								"An error occurred when indexing data for package:" +
								((packageDescription != null) ? ('\n' + packageDescription.toString()) : (' ' + name)), e));
					}
					finally {
						try {
							fIndex.endPackage();
						}
						catch (final Exception e) {
							fException = e;
							fQueue.clear();
							return Status.CANCEL_STATUS;
						}
					}
					
					if (monitor.isCanceled()) {
						return Status.CANCEL_STATUS;
					}
				}
			}
			catch (final Exception e) {
				fException = e;
				fQueue.clear();
				return Status.CANCEL_STATUS;
			}
		}
		
	}
	
	
	private final IREnvConfiguration fREnvConfig;
	
	private final StringBuilder fTempBuilder1 = new StringBuilder(65536);
	private final StringBuilder fTempBuilder2 = new StringBuilder(1024);
	private final REnvIndexWriter fIndex;
	
	
	public RJREnvIndexUpdater(final IREnvConfiguration rEnvConfig) {
		fREnvConfig = rEnvConfig;
		fIndex = new REnvIndexWriter(rEnvConfig);
	}
	
	
	public IStatus update(final RService r, final boolean reset, final Map<String, String> rEnvSharedProperties,
			final IProgressMonitor monitor) {
		final SubMonitor progress = SubMonitor.convert(monitor, 100);
		try {
			fIndex.beginBatch(reset);
			
			final String docDir = checkNA2Null(RDataUtil.checkSingleChar(
					r.evalData("R.home(\"doc\")", progress) )); //$NON-NLS-1$
			fIndex.setDocDir(docDir);
			fIndex.setREnvSharedProperties(rEnvSharedProperties);
			
			loadKeywords(r, progress.newChild(10));
			
			if (progress.isCanceled()) {
				return Status.CANCEL_STATUS;
			}
			progress.setWorkRemaining(90);
			
			loadPackages(r, progress.newChild(80));
			
			progress.setWorkRemaining(10);
			
			final IStatus status = fIndex.endBatch();
			if (status != null && status.getSeverity() >= IStatus.ERROR) {
				return new Status(IStatus.WARNING, RCore.PLUGIN_ID, -1,
						"The R environment index could not be completely updated.",
						new CoreException(status));
			}
			return new Status(IStatus.INFO, RCore.PLUGIN_ID, "The R environment index was updated successfully.");
		}
		catch (final CoreException e) {
			fIndex.log(new Status(IStatus.ERROR, RCore.PLUGIN_ID, -1,
					"An error occurred when updating the R environment.", e));
			if (e.getStatus().getSeverity() == IStatus.CANCEL) {
				return e.getStatus();
			}
			final IStatus status = fIndex.cancel();
			return new Status(IStatus.ERROR, RCore.PLUGIN_ID, -1,
					"The R environment could not be updated.",
					(status != null) ? new CoreException(status) : null);
		}
		catch (final Exception e) {
			fIndex.log(new Status(IStatus.ERROR, RCore.PLUGIN_ID, -1,
					"An error occurred when updating the R environment.", e));
			final IStatus status = fIndex.cancel();
			return new Status(IStatus.ERROR, RCore.PLUGIN_ID, -1,
					"The R environment could not be updated.",
					(status != null) ? new CoreException(status) : null);
		}
		finally {
			progress.done();
		}
	}
	
	private void loadKeywords(final RService r, final SubMonitor progress) throws CoreException {
		final String docDir = fIndex.getDocDir();
		if (docDir == null) {
			return;
		}
		progress.beginTask("Loading R help keywords...", 100);
		Exception errorCause = null;
		try {
			final byte[] bytes = r.downloadFile(docDir + "/KEYWORDS.db", 0, progress);
			final BufferedReader reader = new BufferedReader(new InputStreamReader(
					new ByteArrayInputStream(bytes), "UTF-8")); //$NON-NLS-1$
			String line;
			
			fTempBuilder1.setLength(0);
			while ((line = reader.readLine()) != null) {
				if (REnvIndexWriter.DEBUG) {
					fTempBuilder1.append(line);
					fTempBuilder1.append('\n');
				}
				int idx = line.indexOf('#');
				if (idx >= 0) {
					line = line.substring(0, idx);
				}
				idx = line.indexOf(':');
				if (idx < 0) {
					continue;
				}
				final String descr = new String(line.substring(idx+1).trim());
				line = line.substring(0, idx);
				fIndex.addDefaultKeyword(line.split("\\|"), descr); //$NON-NLS-1$
			}
			return;
		}
		catch (final CoreException e) {
			if (e.getStatus().getSeverity() == IStatus.CANCEL) {
				throw e;
			}
			errorCause = e;
		}
		catch (final Exception e) {
			errorCause = e;
		}
		finally {
			if (REnvIndexWriter.DEBUG) {
				fTempBuilder1.insert(0, "Read KEYWORDS.db file:\n<FILE>\n");
				fTempBuilder1.append("</FILE>\n");
				fIndex.log(new Status(IStatus.INFO, RCore.PLUGIN_ID, -1,
						 fTempBuilder1.toString(), null));
			}
		}
		fIndex.log(new Status(IStatus.ERROR, RCore.PLUGIN_ID, -1,
				"An error occurred when loading the keyword list.", errorCause));
	}
	
	private void loadPackages(final RService r, final SubMonitor progress) throws CoreException {
		progress.beginTask("Loading R package help.", 100);
		Exception errorCause = null;
		LocalJob job = null;
		try {
			r.evalVoid("library(rj)", progress);
			progress.setWorkRemaining(95);
			
			progress.subTask("Searching available packages...");
			final RArray<RCharacterStore> pkgMatrix = RDataUtil.checkRCharArray(r.evalData(
					"installed.packages(fields=c(\"Title\",\"Description\",\"Author\",\"Maintainer\",\"URL\"))", progress), 2);
			final RCharacterStore pkgMatrixData = pkgMatrix.getData();
			final RIntegerStore pkgMatrixDim = pkgMatrix.getDim();
			final RStore pkgMatrixColumns = pkgMatrix.getNames(1);
			final int idxPackage = pkgMatrixColumns.indexOf("Package"); //$NON-NLS-1$
			final int idxLibPath = pkgMatrixColumns.indexOf("LibPath"); //$NON-NLS-1$
			final int idxVersion = pkgMatrixColumns.indexOf("Version"); //$NON-NLS-1$
			
			if (idxPackage < 0 || idxLibPath < 0 || idxVersion < 0) {
				throw new UnexpectedRDataException("A column is missing\n." + pkgMatrixColumns);
			}
			
			final int count = pkgMatrixDim.getInt(0);
			
			final Set<String> pkgs = new HashSet<String>();
			
			job = new LocalJob(pkgMatrix);
			job.schedule();
			for (int i = 0; i < count; i++) {
				final String pkgName = checkNA2Null(pkgMatrixData.get(RDataUtil.getDataIdx(pkgMatrixDim, i, idxPackage)));
				final String libPath = checkNA2Null(pkgMatrixData.get(RDataUtil.getDataIdx(pkgMatrixDim, i, idxLibPath)));
				final String version = checkNA2Null(pkgMatrixData.get(RDataUtil.getDataIdx(pkgMatrixDim, i, idxVersion)));
				if (pkgName == null || libPath == null || version == null) {
					continue;
				}
				if (fIndex.checkPackage(pkgName, version)) {
					continue;
				}
				
				if (progress.isCanceled()) {
					throw new CoreException(Status.CANCEL_STATUS);
				}
				progress.setWorkRemaining(count-i);
				progress.subTask(NLS.bind("Loading data for ''{0}''...", pkgName));
				
				final String libPathEsc = RUtil.escapeCompletly(libPath);
				final String nameEsc = RUtil.escapeCompletly(pkgName);
				try {
					final RObject pkgObj = r.evalData(".statet.checkPkg(id="+i+"L,libPath=\""+libPathEsc+"\",name=\""+nameEsc+"\")", progress);
					if (pkgObj.getRObjectType() != RObject.TYPE_LIST) {
						throw new CoreException(new Status(IStatus.WARNING, RCore.PLUGIN_ID, -1,
								"Package is skipped, because files are missing.", null));
					}
					
					final RList pkgData = (RList) pkgObj;
					if (!pkgName.equals(RDataUtil.checkSingleChar(pkgData.get(PKG_DATA_META_NAME)))
							|| !Integer.valueOf(i).equals(RDataUtil.checkSingleInt(pkgData.get(PKG_DATA_META_ID))) ) {
						throw new IllegalStateException("Unexpected R values.");
					}
					job.add(pkgData, progress);
					pkgs.add(pkgName);
				}
				catch (final CoreException e) { // only core exceptions!
					if (e.getStatus().getSeverity() == IStatus.CANCEL) {
						throw e;
					}
					fIndex.log(new Status(IStatus.ERROR, RCore.PLUGIN_ID, -1, 
							"An error occurred when loading data for package '" + pkgName + "' in '" + libPath + "'.", e));
				}
			}
			job.finish(progress);
			job = null;
			return;
		}
		catch (final CoreException e) {
			if (e.getStatus().getSeverity() == IStatus.CANCEL) {
				throw e;
			}
			errorCause = e;
		}
		catch (final Exception e) {
			errorCause = e;
		}
		finally {
			if (job != null) {
				job.cancel(progress);
			}
		}
		throw new CoreException(new Status(IStatus.ERROR, RCore.PLUGIN_ID, -1,
				"An error occurred when loading the package data.", errorCause ));
	}
	
	private void processRdData(final String packageName, final RList pkgList) throws Exception {
		for (int j = 0; j < pkgList.getLength(); j++) {
			final RObject rdObj = pkgList.get(j);
			if (rdObj.getRClassName().equals("RdData")) {
				final RList rdData = (RList) rdObj;
				final RdItem rdItem = new RdItem(packageName, pkgList.getName(j));
				{	final RStore store = rdData.get("title").getData();
					if (!store.isNA(0)) {
						rdItem.setTitle(store.getChar(0));
					}
				}
				{	final RStore store = rdData.get("topics").getData();
					for (int k = 0; k < store.getLength(); k++) {
						if (!store.isNA(k)) {
							final String alias = store.getChar(k).trim();
							if (alias.length() > 0) {
								rdItem.addTopic(alias);
							}
						}
					}
				}
				{	final RStore store = rdData.get("keywords").getData();
					for (int k = 0; k < store.getLength(); k++) {
						if (!store.isNA(k)) {
							final String keyword = store.getChar(k).trim();
							if (keyword.length() > 0) {
								rdItem.addKeyword(keyword);
							}
						}
					}
				}
				{	final RStore store = rdData.get("concepts").getData();
					for (int k = 0; k < store.getLength(); k++) {
						if (!store.isNA(k)) {
							final String concept = store.getChar(k).trim();
							if (concept.length() > 0) {
								rdItem.addConcept(concept);
							}
						}
					}
				}
				final RObject htmlObj = rdData.get("HTML");
				if (htmlObj.getData() != null
						&& htmlObj.getData().getStoreType() == RStore.CHARACTER) {
					rdItem.setHtml(processHtml(htmlObj.getData()));
				}
				fIndex.add(rdItem);
			}
		}
	}
	
	private String processHtml(final RStore store) {
		fTempBuilder1.setLength(0);
		fTempBuilder2.setLength(0);
		int length = 0;
		for (int i = 0; i < store.getLength(); i++) {
			if (!store.isNA(i)) {
				length += store.getChar(i).length() + 2;
			}
		}
		length += 300;
		int topIndex = -1;
		boolean inExamples = false;
		fTempBuilder2.append("<div class=\"toc\"><ul>");
		for (int i = 0; i < store.getLength(); i++) {
			if (!store.isNA(i)) {
				String line = store.getChar(i);
				if (topIndex == -1) {
					if (line.startsWith("<table ")) {
						fTempBuilder1.append("<table class=\"header\" ");
						line = line.substring(7);
					}
					else if (line.startsWith("<h2>")) {
						topIndex = fTempBuilder1.length();
						fTempBuilder1.append("<h2 id=\"top\">");
						line = line.substring(4);
					}
				}
				else if (topIndex >= 0 && line.length() > 10) {
					if (line.startsWith("<h3>")) {
						if (inExamples) {
							fTempBuilder1.append(RHelpWebapp.HTML_END_EXAMPLES);
							inExamples = false;
						}
						switch (line.charAt(4)-line.charAt(6)) {
						case ('D'-'s'):
							if (line.equals("<h3>Description</h3>")) {
								fTempBuilder2.append("<li><a href=\"#description\"><span class=\"mnemonic\">D</span>escription</a></li>");
								line = "<h3 id=\"description\">Description</h3>";
								break;
							}
							break;
						case ('U'-'a'):
							if (line.equals("<h3>Usage</h3>")) {
								fTempBuilder2.append("<li><a href=\"#usage\"><span class=\"mnemonic\">U</span>sage</a></li>");
								line = "<h3 id=\"usage\">Usage</h3>";
								break;
							}
							break;
						case ('A'-'g'):
							if (line.equals("<h3>Arguments</h3>")) {
								fTempBuilder2.append("<li><a href=\"#arguments\"><span class=\"mnemonic\">A</span>rguments</a></li>");
								line = "<h3 id=\"arguments\">Arguments</h3>";
								break;
							}
							break;
						case ('D'-'t'):
							if (line.equals("<h3>Details</h3>")) {
								fTempBuilder2.append("<li><a href=\"#details\">Deta<span class=\"mnemonic\">i</span>ls</a></li>");
								line = "<h3 id=\"details\">Details</h3>";
								break;
							}
							break;
						case ('V'-'l'):
							if (line.equals("<h3>Value</h3>")) {
								fTempBuilder2.append("<li><a href=\"#value\"><span class=\"mnemonic\">V</span>alue</a></li>");
								line = "<h3 id=\"value\">Value</h3>";
								break;
							}
							break;
						case ('A'-'t'):
							if (line.equals("<h3>Author(s)</h3>")) {
								fTempBuilder2.append("<li><a href=\"#authors\">Auth<span class=\"mnemonic\">o</span>r(s)</a></li>");
								line = "<h3 id=\"authors\">Author(s)</h3>";
								break;
							}
							break;
						case ('R'-'f'):
							if (line.equals("<h3>References</h3>")) {
								fTempBuilder2.append("<li><a href=\"#references\"><span class=\"mnemonic\">R</span>eferences</a></li>");
								line = "<h3 id=\"references\">References</h3>";
								break;
							}
							break;
						case ('E'-'a'):
							if (line.equals("<h3>Examples</h3>")) {
								fTempBuilder2.append("<li><a href=\"#examples\"><span class=\"mnemonic\">E</span>xamples</a></li>");
								line = "<h3 id=\"examples\">Examples</h3>" + RHelpWebapp.HTML_BEGIN_EXAMPLES;
								inExamples = true;
								break;
							}
							break;
						case ('S'-'e'):
							if (line.equals("<h3>See Also</h3>")) {
								fTempBuilder2.append("<li><a href=\"#seealso\"><span class=\"mnemonic\">S</span>ee Also</a></li>");
								line = "<h3 id=\"seealso\">See Also</h3>";
								break;
							}
							break;
						}
					}
					else if (line.startsWith("<hr>")) {
						if (inExamples) {
							fTempBuilder1.append(RHelpWebapp.HTML_END_EXAMPLES);
							inExamples = false;
						}
//						if (line.startsWith("<hr><div align=\"center\">[Package <em>")) {
//							fTempBuilder1.append("<hr/><div class=\"toc\"><ul><li><a href=\"#top\">Top</a></li></ul></div>");
//						}
						fTempBuilder1.append("<hr/>");
						line = line.substring(4);
					}
				}
				fTempBuilder1.append(line);
				fTempBuilder1.append('\r');
				fTempBuilder1.append('\n');
			}
		}
		if (topIndex >= 0) {
			fTempBuilder2.append("</ul></div>");
			fTempBuilder1.insert(topIndex, fTempBuilder2);
		}
		return fTempBuilder1.toString();
	}
	
}
