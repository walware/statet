/*=============================================================================#
 # Copyright (c) 2010-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.core.rhelp.rj;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;

import de.walware.rj.data.RCharacterStore;
import de.walware.rj.data.RDataUtil;
import de.walware.rj.data.RList;
import de.walware.rj.data.RObject;
import de.walware.rj.data.RStore;
import de.walware.rj.data.RVector;
import de.walware.rj.renv.IRPkgDescription;
import de.walware.rj.renv.RNumVersion;
import de.walware.rj.renv.RPkgDescription;
import de.walware.rj.services.FunctionCall;
import de.walware.rj.services.RService;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.pkgmanager.IRLibPaths;
import de.walware.statet.r.core.pkgmanager.IRPkgCollection;
import de.walware.statet.r.core.pkgmanager.IRPkgInfo;
import de.walware.statet.r.core.pkgmanager.IRPkgManager;
import de.walware.statet.r.core.renv.IREnvConfiguration;
import de.walware.statet.r.internal.core.rhelp.RHelpWebapp;
import de.walware.statet.r.internal.core.rhelp.index.REnvIndexWriter;
import de.walware.statet.r.internal.core.rhelp.index.REnvIndexWriter.AbortIndexException;
import de.walware.statet.r.internal.core.rhelp.index.REnvIndexWriter.RdItem;


/**
 * Updates the R environment (R help keywords, R help index).
 * Uses the RService interface to read the data.
 */
public class RJREnvIndexUpdater {
	
	
	private static final String PKG_DESCR_FNAME= "rj:::.rhelp.loadPkgDescr"; //$NON-NLS-1$
	private static final int PKG_DESCR_LENGTH= 7;
	private static final int PKG_DESCR_IDX_VERSION= 0;
	private static final int PKG_DESCR_IDX_TITLE= 1;
	private static final int PKG_DESCR_IDX_DESCRIPTION= 2;
	private static final int PKG_DESCR_IDX_AUTHOR= 3;
	private static final int PKG_DESCR_IDX_MAINTAINER= 4;
	private static final int PKG_DESCR_IDX_URL= 5;
	private static final int PKG_DESCR_IDX_BUILT= 6;
	
	private static final String PKG_RD_FNAME= "rj:::.rhelp.loadPkgRd"; //$NON-NLS-1$
	
	
	private static String checkNA2Null(final String s) {
		return (s != null && !s.equals("NA") && s.length() > 0) ? s : null; //$NON-NLS-1$
	}
	
	
	private static class PkgTask {
		
		final String name;
		final RNumVersion version;
		final String built;
		final String libPath; // for error messages
		
		RVector<RCharacterStore> rDescr;
		
		RList rRd;
		
		public PkgTask(final String name, final RNumVersion version, final String built,
				final String libPath) {
			this.name= name;
			this.version= version;
			this.built= built;
			this.libPath= libPath;
		}
		
		
		@Override
		public String toString() {
			final StringBuilder sb= new StringBuilder(this.name);
			sb.append(" " + "(version= ").append((this.version != null) ? this.version : "<unknown>");
			sb.append(')');
			if (this.libPath != null) {
				sb.append("in lib= '").append(this.libPath).append('\'');
			}
			return sb.toString();
		}
		
	}
	
	private static final PkgTask FINISH= new PkgTask(null, null, null, null);
	
	
	private class LocalJob extends Job {
		
		
		private final BlockingQueue<PkgTask> queue= new ArrayBlockingQueue<>(4);
		
		private volatile Exception exception;
		
		
		public LocalJob() {
			super(NLS.bind("Update R help index for ''{0}''", RJREnvIndexUpdater.this.rEnvConfig.getName()));
			setPriority(Job.LONG);
			setSystem(true);
		}
		
		public void add(final PkgTask task, final SubMonitor progress) throws Exception {
			while (true) {
				try {
					if (this.exception != null) {
						throw this.exception;
					}
					this.queue.put(task);
					return;
				}
				catch (final InterruptedException e) {
					if (progress.isCanceled()) {
						cancel();
						throw new CoreException(Status.CANCEL_STATUS);
					}
				}
			}
		}
		
		public void finish(final SubMonitor progress) throws CoreException {
			while (true) {
				try {
					this.queue.put(FINISH);
					join();
					return;
				}
				catch (final InterruptedException e) {
					// forward to worker thread
					final Thread thread= getThread();
					if (thread != null) {
						thread.interrupt();
					}
					if (progress.isCanceled()) {
						cancel();
						throw new CoreException(Status.CANCEL_STATUS);
					}
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
					// forward to worker thread
					final Thread thread= getThread();
					if (thread != null) {
						thread.interrupt();
					}
				}
			}
		}
		
		@Override
		protected void canceling() {
			final Thread thread= getThread();
			if (thread != null) {
				thread.interrupt();
			}
			super.canceling();
		}
		
		@Override
		protected IStatus run(final IProgressMonitor monitor) {
			try {
				PkgTask task= null;
				while (true) {
					try {
						task= this.queue.take();
						if (task == FINISH) {
							return Status.OK_STATUS;
						}
						
						final IRPkgDescription pkgDescription= createDescription(task);
						RJREnvIndexUpdater.this.index.beginPackage(pkgDescription);
						processRdData(pkgDescription.getName(), task.rRd);
					}
					catch (final InterruptedException e) {
						// continue, monitor is checked
					}
					catch (final AbortIndexException e) {
						this.exception= e;
						this.queue.clear();
						return Status.CANCEL_STATUS;
					}
					catch (final Exception e) {
						RJREnvIndexUpdater.this.index.log(new Status(IStatus.ERROR, RCore.PLUGIN_ID, -1, 
								"An error occurred when indexing data for package:\n" + task,
								e ));
					}
					finally {
						try {
							RJREnvIndexUpdater.this.index.endPackage();
						}
						catch (final Exception e) {
							this.exception= e;
							this.queue.clear();
							return Status.CANCEL_STATUS;
						}
					}
					
					if (monitor.isCanceled()) {
						return Status.CANCEL_STATUS;
					}
				}
			}
			catch (final Exception e) {
				this.exception= e;
				this.queue.clear();
				return Status.CANCEL_STATUS;
			}
		}
		
	}
	
	
	private final IREnvConfiguration rEnvConfig;
	
	private final StringBuilder tempBuilder1= new StringBuilder(65536);
	private final StringBuilder tempBuilder2= new StringBuilder(1024);
	
	private final REnvIndexWriter index;
	
	
	public RJREnvIndexUpdater(final IREnvConfiguration rEnvConfig) {
		this.rEnvConfig= rEnvConfig;
		this.index= new REnvIndexWriter(rEnvConfig);
	}
	
	
	public IStatus update(final RService r, final boolean reset, final Map<String, String> rEnvSharedProperties,
			final IProgressMonitor monitor) {
		final SubMonitor progress= SubMonitor.convert(monitor, 10 + 80 + 10);
		try {
			this.index.beginBatch(reset);
			
			final String docDir= checkNA2Null(RDataUtil.checkSingleChar(
					r.evalData("R.home(\"doc\")", progress) )); //$NON-NLS-1$
			this.index.setDocDir(docDir);
			this.index.setREnvSharedProperties(rEnvSharedProperties);
			
			long tKeywords= System.nanoTime();
			loadKeywords(r, progress.newChild(10));
			tKeywords= System.nanoTime() - tKeywords;
			
			if (progress.isCanceled()) {
				throw new CoreException(Status.CANCEL_STATUS);
			}
			progress.setWorkRemaining(90);
			
			long tPackages= System.nanoTime();
			loadPackages(r, progress.newChild(80));
			tPackages= System.nanoTime() - tPackages;
			
			progress.setWorkRemaining(10);
			
			if (REnvIndexWriter.DEBUG) {
				this.index.log(new Status(IStatus.INFO, RCore.PLUGIN_ID,
						NLS.bind("Required time for update: keywords= {0}ms, packages= {1}ms.",
								tKeywords / 1_000_000, tPackages / 1_000_000 )));
			}
			
			final IStatus status= this.index.endBatch();
			if (status != null && status.getSeverity() >= IStatus.ERROR) {
				return new Status(IStatus.WARNING, RCore.PLUGIN_ID, -1,
						"The R environment index could not be completely updated.",
						new CoreException(status));
			}
			return new Status(IStatus.INFO, RCore.PLUGIN_ID, "The R environment index was updated successfully.");
		}
		catch (final CoreException e) {
			if (e.getStatus().getSeverity() == IStatus.CANCEL) {
				this.index.cancel();
				return e.getStatus();
			}
			this.index.log(new Status(IStatus.ERROR, RCore.PLUGIN_ID, -1,
					"An error occurred when updating the R environment.", e));
			final IStatus status= this.index.cancel();
			return new Status(IStatus.ERROR, RCore.PLUGIN_ID, -1,
					"The R environment could not be updated.",
					(status != null) ? new CoreException(status) : null);
		}
		catch (final Exception e) {
			this.index.log(new Status(IStatus.ERROR, RCore.PLUGIN_ID, -1,
					"An error occurred when updating the R environment.", e));
			final IStatus status= this.index.cancel();
			return new Status(IStatus.ERROR, RCore.PLUGIN_ID, -1,
					"The R environment could not be updated.",
					(status != null) ? new CoreException(status) : null);
		}
		finally {
			progress.done();
		}
	}
	
	private void loadKeywords(final RService r, final SubMonitor progress) throws CoreException {
		final String docDir= this.index.getDocDir();
		if (docDir == null) {
			return;
		}
		progress.beginTask("Loading R help keywords...", 100);
		Exception errorCause= null;
		try {
			final byte[] bytes= r.downloadFile(docDir + "/KEYWORDS.db", 0, progress);
			final BufferedReader reader= new BufferedReader(new InputStreamReader(
					new ByteArrayInputStream(bytes), "UTF-8")); //$NON-NLS-1$
			String line;
			
			this.tempBuilder1.setLength(0);
			while ((line= reader.readLine()) != null) {
				if (REnvIndexWriter.DEBUG) {
					this.tempBuilder1.append(line);
					this.tempBuilder1.append('\n');
				}
				int idx= line.indexOf('#');
				if (idx >= 0) {
					line= line.substring(0, idx);
				}
				idx= line.indexOf(':');
				if (idx < 0) {
					continue;
				}
				final String descr= new String(line.substring(idx+1).trim());
				line= line.substring(0, idx);
				this.index.addDefaultKeyword(line.split("\\|"), descr); //$NON-NLS-1$
			}
			return;
		}
		catch (final CoreException e) {
			if (e.getStatus().getSeverity() == IStatus.CANCEL) {
				throw e;
			}
			errorCause= e;
		}
		catch (final Exception e) {
			errorCause= e;
		}
		finally {
			if (REnvIndexWriter.DEBUG) {
				this.tempBuilder1.insert(0, "Read KEYWORDS.db file:\n<FILE>\n");
				this.tempBuilder1.append("</FILE>\n");
				this.index.log(new Status(IStatus.INFO, RCore.PLUGIN_ID, -1,
						 this.tempBuilder1.toString(), null));
			}
		}
		this.index.log(new Status(IStatus.ERROR, RCore.PLUGIN_ID, -1,
				"An error occurred when loading the keyword list.", errorCause));
	}
	
	private void loadPackages(final RService r, final SubMonitor progress) throws CoreException {
		progress.beginTask("Loading R package help.", 8 + 1);
		Exception errorCause= null;
		LocalJob job= null;
		try {
			job= new LocalJob();
			job.schedule();
			
			final IRPkgManager rPkgManager = RCore.getRPkgManager(this.rEnvConfig.getReference());
			final IRLibPaths rLibPaths= rPkgManager.getRLibPaths();
			final IRPkgCollection<? extends IRPkgInfo> installed= rPkgManager.getRPkgSet().getInstalled();
			
			final SubMonitor pkgsProgress= progress.newChild(8);
			final List<String> names= installed.getNames();
			for (int i= 0; i < names.size(); i++) {
				pkgsProgress.setWorkRemaining(2 * (names.size() - i));
				
				final IRPkgInfo pkgInfo= installed.getFirstByName(names.get(i));
				
				if (this.index.checkPackage(pkgInfo.getName(), pkgInfo.getVersion().toString(),
						pkgInfo.getBuilt() )) {
					continue;
				}
				
				if (pkgsProgress.isCanceled()) {
					throw new CoreException(Status.CANCEL_STATUS);
				}
				
				progress.subTask(NLS.bind("Loading data for package ''{0}''...", pkgInfo.getName()));
				
				try {
					final IRLibPaths.Entry libPath= rLibPaths.getEntryByLocation(pkgInfo.getLibraryLocation());
					if (libPath == null) {
						throw new CoreException(new Status(IStatus.ERROR, RCore.PLUGIN_ID, 
								NLS.bind("Failed to resolve library location ''{0}''.",
										pkgInfo.getLibraryLocation() )));
					}
					final PkgTask task= new PkgTask(pkgInfo.getName(), pkgInfo.getVersion(), pkgInfo.getBuilt(), libPath.getRPath());
					{	final FunctionCall call= r.createFunctionCall(PKG_DESCR_FNAME);
						call.addChar("lib", libPath.getRPath()); //$NON-NLS-1$
						call.addChar("name", pkgInfo.getName()); //$NON-NLS-1$
						task.rDescr= RDataUtil.checkRCharVector(call.evalData(pkgsProgress.newChild(1)));
					}
					{	final FunctionCall call= r.createFunctionCall(PKG_RD_FNAME);
						call.addChar("lib", libPath.getRPath()); //$NON-NLS-1$
						call.addChar("name", pkgInfo.getName()); //$NON-NLS-1$
						task.rRd= RDataUtil.checkRList(call.evalData(pkgsProgress.newChild(1)));
					}
					job.add(task, progress);
				}
				catch (final CoreException e) { // only core exceptions!
					if (e.getStatus().getSeverity() == IStatus.CANCEL) {
						throw e;
					}
					this.index.log(new Status(IStatus.ERROR, RCore.PLUGIN_ID, -1, 
							NLS.bind("An error occurred when loading data for package '' {0}'' in ''{1}''.",
									pkgInfo.getName(),  pkgInfo.getLibraryLocation() ),
							e ));
				}
			}
			
			progress.subTask("Finishing index of help...");
			job.finish(progress.newChild(2));
			job= null;
			return;
		}
		catch (final CoreException e) {
			if (e.getStatus().getSeverity() == IStatus.CANCEL) {
				throw e;
			}
			errorCause= e;
		}
		catch (final Exception e) {
			errorCause= e;
		}
		finally {
			if (job != null) {
				job.cancel(progress);
			}
		}
		throw new CoreException(new Status(IStatus.ERROR, RCore.PLUGIN_ID, -1,
				"An error occurred when loading the package data.", errorCause ));
	}
	
	private IRPkgDescription createDescription(final PkgTask task) throws Exception {
		final RCharacterStore data= RDataUtil.checkLengthEqual(task.rDescr.getData(), PKG_DESCR_LENGTH);
		
		final RNumVersion version;
		final String built;
		{	final String versionString= RDataUtil.checkValue(data, PKG_DESCR_IDX_VERSION);
			if (task.version != null) {
				if (!task.version.toString().equals(versionString)) {
					throw new Exception(
							NLS.bind("Unexpected package version: expected={0}, found={1}",
									task.version, versionString ));
				}
				version= task.version;
			}
			else {
				version= RNumVersion.create(versionString);
			}
		}
		{	final String builtString= RDataUtil.checkValue(data, PKG_DESCR_IDX_BUILT);
			if (task.built != null) {
				if (!task.built.equals(builtString)) {
					throw new Exception(
							NLS.bind("Unexpected package built: expected={0}, found={1}",
									task.version, builtString ));
				}
				built= task.built;
			}
			else {
				built= builtString;
			}
		}
		return new RPkgDescription(task.name,
				version,
				RDataUtil.getValue(data, PKG_DESCR_IDX_TITLE, ""), //$NON-NLS-1$
				RDataUtil.getValue(data, PKG_DESCR_IDX_DESCRIPTION, ""), //$NON-NLS-1$
				data.get(PKG_DESCR_IDX_AUTHOR),
				data.get(PKG_DESCR_IDX_MAINTAINER),
				data.get(PKG_DESCR_IDX_URL),
				built
		);
	}
	
	private void processRdData(final String pkgName, final RList pkgList) throws Exception {
		for (int j= 0; j < pkgList.getLength(); j++) {
			final RObject rdObj= pkgList.get(j);
			if (rdObj.getRClassName().equals("RdData")) { //$NON-NLS-1$
				final RList rdData= (RList) rdObj;
				final RdItem rdItem= new RdItem(pkgName, pkgList.getName(j));
				{	final RStore<?> store= rdData.get("title").getData(); //$NON-NLS-1$
					if (!store.isNA(0)) {
						rdItem.setTitle(store.getChar(0));
					}
				}
				{	final RStore<?> store= rdData.get("topics").getData(); //$NON-NLS-1$
					for (int k= 0; k < store.getLength(); k++) {
						if (!store.isNA(k)) {
							final String alias= store.getChar(k).trim();
							if (alias.length() > 0) {
								rdItem.addTopic(alias);
							}
						}
					}
				}
				{	final RStore<?> store= rdData.get("keywords").getData(); //$NON-NLS-1$
					for (int k= 0; k < store.getLength(); k++) {
						if (!store.isNA(k)) {
							final String keyword= store.getChar(k).trim();
							if (keyword.length() > 0) {
								rdItem.addKeyword(keyword);
							}
						}
					}
				}
				{	final RStore<?> store= rdData.get("concepts").getData(); //$NON-NLS-1$
					for (int k= 0; k < store.getLength(); k++) {
						if (!store.isNA(k)) {
							final String concept= store.getChar(k).trim();
							if (concept.length() > 0) {
								rdItem.addConcept(concept);
							}
						}
					}
				}
				final RObject htmlObj= rdData.get("HTML"); //$NON-NLS-1$
				if (htmlObj.getData() != null
						&& htmlObj.getData().getStoreType() == RStore.CHARACTER) {
					rdItem.setHtml(processHtml((RCharacterStore) htmlObj.getData()));
				}
				this.index.add(rdItem);
			}
		}
	}
	
	@SuppressWarnings("nls")
	private String processHtml(final RCharacterStore store) {
		this.tempBuilder1.setLength(0);
		this.tempBuilder2.setLength(0);
		int length= 0;
		for (int i= 0; i < store.getLength(); i++) {
			if (!store.isNA(i)) {
				length += store.getChar(i).length() + 2;
			}
		}
		length += 300;
		int topIndex= -1;
		boolean inExamples= false;
		this.tempBuilder2.append("<div class=\"toc\"><ul>");
		for (int i= 0; i < store.getLength(); i++) {
			if (!store.isNA(i)) {
				String line= store.getChar(i);
				if (topIndex == -1) {
					if (line.startsWith("<table ")) {
						this.tempBuilder1.append("<table class=\"header\" ");
						line= line.substring(7);
					}
					else if (line.startsWith("<h2>")) {
						topIndex= this.tempBuilder1.length();
						this.tempBuilder1.append("<h2 id=\"top\">");
						line= line.substring(4);
					}
				}
				else if (topIndex >= 0 && line.length() > 10) {
					if (line.startsWith("<h3>")) {
						if (inExamples) {
							this.tempBuilder1.append(RHelpWebapp.HTML_END_EXAMPLES);
							inExamples= false;
						}
						switch (line.charAt(4)-line.charAt(6)) {
						case ('D'-'s'):
							if (line.equals("<h3>Description</h3>")) {
								this.tempBuilder2.append("<li><a href=\"#description\"><span class=\"mnemonic\">D</span>escription</a></li>"); //$NON-NLS-1$
								line= "<h3 id=\"description\">Description</h3>";
								break;
							}
							break;
						case ('U'-'a'):
							if (line.equals("<h3>Usage</h3>")) {
								this.tempBuilder2.append("<li><a href=\"#usage\"><span class=\"mnemonic\">U</span>sage</a></li>"); //$NON-NLS-1$
								line= "<h3 id=\"usage\">Usage</h3>";
								break;
							}
							break;
						case ('A'-'g'):
							if (line.equals("<h3>Arguments</h3>")) {
								this.tempBuilder2.append("<li><a href=\"#arguments\"><span class=\"mnemonic\">A</span>rguments</a></li>"); //$NON-NLS-1$
								line= "<h3 id=\"arguments\">Arguments</h3>";
								break;
							}
							break;
						case ('D'-'t'):
							if (line.equals("<h3>Details</h3>")) {
								this.tempBuilder2.append("<li><a href=\"#details\">Deta<span class=\"mnemonic\">i</span>ls</a></li>");
								line= "<h3 id=\"details\">Details</h3>";
								break;
							}
							break;
						case ('V'-'l'):
							if (line.equals("<h3>Value</h3>")) {
								this.tempBuilder2.append("<li><a href=\"#value\"><span class=\"mnemonic\">V</span>alue</a></li>");
								line= "<h3 id=\"value\">Value</h3>";
								break;
							}
							break;
						case ('A'-'t'):
							if (line.equals("<h3>Author(s)</h3>")) {
								this.tempBuilder2.append("<li><a href=\"#authors\">Auth<span class=\"mnemonic\">o</span>r(s)</a></li>");
								line= "<h3 id=\"authors\">Author(s)</h3>";
								break;
							}
							break;
						case ('R'-'f'):
							if (line.equals("<h3>References</h3>")) {
								this.tempBuilder2.append("<li><a href=\"#references\"><span class=\"mnemonic\">R</span>eferences</a></li>");
								line= "<h3 id=\"references\">References</h3>";
								break;
							}
							break;
						case ('E'-'a'):
							if (line.equals("<h3>Examples</h3>")) {
								this.tempBuilder2.append("<li><a href=\"#examples\"><span class=\"mnemonic\">E</span>xamples</a></li>");
								line= "<h3 id=\"examples\">Examples</h3>" + RHelpWebapp.HTML_BEGIN_EXAMPLES;
								inExamples= true;
								break;
							}
							break;
						case ('S'-'e'):
							if (line.equals("<h3>See Also</h3>")) {
								this.tempBuilder2.append("<li><a href=\"#seealso\"><span class=\"mnemonic\">S</span>ee Also</a></li>");
								line= "<h3 id=\"seealso\">See Also</h3>";
								break;
							}
							break;
						}
					}
					else if (line.startsWith("<hr>")) {
						if (inExamples) {
							this.tempBuilder1.append(RHelpWebapp.HTML_END_EXAMPLES);
							inExamples= false;
						}
//						if (line.startsWith("<hr><div align=\"center\">[Package <em>")) {
//							fTempBuilder1.append("<hr/><div class=\"toc\"><ul><li><a href=\"#top\">Top</a></li></ul></div>");
//						}
						this.tempBuilder1.append("<hr/>");
						line= line.substring(4);
					}
				}
				this.tempBuilder1.append(line);
				this.tempBuilder1.append('\r');
				this.tempBuilder1.append('\n');
			}
		}
		if (topIndex >= 0) {
			this.tempBuilder2.append("</ul></div>");
			this.tempBuilder1.insert(topIndex, this.tempBuilder2);
		}
		return this.tempBuilder1.toString();
	}
	
}
