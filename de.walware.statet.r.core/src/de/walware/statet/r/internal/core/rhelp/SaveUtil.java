/*******************************************************************************
 * Copyright (c) 2010-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.core.rhelp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;

import de.walware.ecommons.collections.ConstList;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.renv.IREnvConfiguration;
import de.walware.statet.r.core.rhelp.IRHelpKeyword;
import de.walware.statet.r.core.rhelp.IRHelpPage;
import de.walware.statet.r.core.rhelp.IRPackageHelp;
import de.walware.statet.r.internal.core.RCorePlugin;
import de.walware.statet.r.internal.core.renv.REnvConfiguration;


class SaveUtil {
	
	
	private static final int VERSION = 6;
	
	private static final String RHELP_SER_FILE = "rhelp.ser"; //$NON-NLS-1$
	
	public static File getIndexDirectory(final IREnvConfiguration rEnvConfig) {
		try {
			final IFileStore indexDirectory = rEnvConfig.getIndexDirectoryStore();
			return indexDirectory.toLocalFile(0, null);
		}
		catch (final Exception e) {
			return null;
		}
	}
	
	public static File[] getIndexResources(final String rEnvId) {
		final File directory = REnvConfiguration.getIndexRootDirectory();
		return directory.listFiles(new FilenameFilter() {
			public boolean accept(final File dir, final String name) {
				return name.startsWith(rEnvId);
			}
		});
	}
	
	public static Set<String> getExistingRHelpEnvId() {
		final File directory = REnvConfiguration.getIndexRootDirectory();
		final Set<String> ids = new HashSet<String>();
		final File[] list = directory.listFiles();
		for (final File res : list) {
			if (res.isDirectory()) {
				ids.add(res.getName());
			}
		}
		return ids;
	}
	
	
	public SaveUtil() {
	}
	
	public void save(final IREnvConfiguration rEnvConfig, final REnvHelp help) {
		FIO fio = null;
		try {
			final File directory = getIndexDirectory(rEnvConfig);
			if (directory == null) {
				throw new OperationCanceledException(NLS.bind("Index directory could not be resolved: ''{0}''.",
						rEnvConfig.getIndexDirectoryPath() ));
			}
			
			final File newFile = new File(directory, "rhelp.new"); //$NON-NLS-1$
			if (newFile.exists()) {
				newFile.delete();
			}
			
			fio = FIO.get(new ObjectOutputStream(new BufferedOutputStream(
					new FileOutputStream(newFile) )));
			save(help, fio);
			fio.flush();
			fio.out.close();
			fio.out = null;
			fio = null;
			
			final File serFile = new File(directory, RHELP_SER_FILE);
			if (serFile.exists()) {
				serFile.delete();
			}
			if (!newFile.renameTo(serFile)) {
				throw new IOException("Renaming failed.");
			}
		}
		catch (final Exception e) {
			RCorePlugin.log(new Status(IStatus.ERROR, RCore.PLUGIN_ID, -1,
					NLS.bind("An error occurred when saving R help data for ''{0}''.",
							rEnvConfig.getName()), e));
			return;
		}
		finally {
			if (fio != null && fio.out != null) {
				try {
					fio.out.close();
				}
				catch (final IOException ignore) {}
				finally {
					fio.out = null;
				}
			}
		}
	}
	
	public boolean hasIndex(final IREnvConfiguration rEnvConfig) {
		if (rEnvConfig == null) {
			return false;
		}
		final File directory = getIndexDirectory(rEnvConfig);
		return (directory != null
				&& new File(directory, RHELP_SER_FILE).exists() );
	}
	
	public REnvHelp load(final IREnvConfiguration rEnvConfig) {
		FIO fio = null;
		try {
			final File directory = getIndexDirectory(rEnvConfig);
			if (directory == null) {
				throw new OperationCanceledException(NLS.bind("Index directory could not be resolved: ''{0}''.",
						rEnvConfig.getIndexDirectoryPath() ));
			}
			
			final File serFile = new File(directory, RHELP_SER_FILE);
			if (!serFile.exists()) {
				return null;
			}
			
			fio = FIO.get(new ObjectInputStream(new BufferedInputStream(
					new FileInputStream(serFile) )));
			final REnvHelp help = load(rEnvConfig, fio);
			fio.in.close();
			fio.in = null;
			fio = null;
			return help;
		}
		catch (final Throwable e) {
			if (e instanceof Error && !(e instanceof UnsupportedClassVersionError)) {
				throw (Error) e;
			}
			RCorePlugin.log(new Status(IStatus.ERROR, RCore.PLUGIN_ID, -1,
					NLS.bind("An error occurred when loading R help data for ''{0}''.", rEnvConfig.getName()), e));
			return null;
		}
		finally {
			if (fio != null && fio.in != null) {
				try {
					fio.in.close();
				}
				catch (final IOException ignore) {}
				finally {
					fio.in = null;
				}
			}
		}
	}
	
	public void save(final REnvHelp help, final FIO fio)
			throws IOException {
		fio.out.writeInt(VERSION);
		fio.writeString(help.getDocDir());
		
		{	final List<IRHelpKeyword.Group> keywordGroups = help.getKeywords();
			final int count = keywordGroups.size();
			fio.out.writeInt(count);
			for (int i = 0; i < keywordGroups.size(); i++) {
				saveKeywordGroup(keywordGroups.get(i), fio);
			}
		}
		{	final List<IRPackageHelp> packages = help.getRPackages();
			final int count = packages.size();
			fio.out.writeInt(count);
			for (int i = 0; i < packages.size(); i++) {
				savePackage(packages.get(i), fio);
			}
		}
	}
	
	public REnvHelp load(final IREnvConfiguration rEnvConfig, final FIO fio)
			throws IOException {
		final int version = fio.in.readInt();
		if (version != VERSION) {
			throw new UnsupportedClassVersionError("Readed: " + version);
		}
		final String docDir = fio.readString();
		
		final IRHelpKeyword.Group[] keywordGroups;
		{	final int count = fio.in.readInt();
			keywordGroups = new IRHelpKeyword.Group[count];
			for (int i = 0; i < count; i++) {
				keywordGroups[i] = loadKeywordGroup(fio);
			}
		}
		IRPackageHelp[] packageHelps;
		{	final int count = fio.in.readInt();
			packageHelps = new IRPackageHelp[count];
			for (int i = 0; i < count; i++) {
				packageHelps[i] = loadPackage(rEnvConfig, fio);
			}
		}
		return new REnvHelp(rEnvConfig.getReference(), docDir,
				new ConstList<IRHelpKeyword.Group>(keywordGroups),
				new ConstList<IRPackageHelp>(packageHelps) );
	}
	
	private void saveKeywordGroup(final IRHelpKeyword.Group group, final FIO fio)
			throws IOException {
		fio.writeString(group.getLabel());
		fio.writeString(group.getDescription());
		final List<IRHelpKeyword> keywords = group.getNestedKeywords();
		final int count = keywords.size();
		fio.out.writeInt(count);
		for (int i = 0; i < count; i++) {
			saveKeyword(keywords.get(i), fio);
		}
	}
	
	private IRHelpKeyword.Group loadKeywordGroup(final FIO fio) throws IOException {
		final String label = fio.readString();
		final String description = fio.readString();
		final int count = fio.in.readInt();
		final IRHelpKeyword[] keywords = new IRHelpKeyword[count];
		for (int i = 0; i < count; i++) {
			keywords[i] = loadKeyword(fio);
		}
		return new RHelpKeywordGroup(label, description,
				new ConstList<IRHelpKeyword>(keywords) );
	}
	
	private void saveKeyword(final IRHelpKeyword keyword, final FIO fio)
			throws IOException {
		fio.writeString(keyword.getKeyword());
		fio.writeString(keyword.getDescription());
		final List<IRHelpKeyword> nestedKeywords = keyword.getNestedKeywords();
		final int count = nestedKeywords.size();
		fio.out.writeInt(count);
		for (int i = 0; i < nestedKeywords.size(); i++) {
			saveKeyword(nestedKeywords.get(i), fio);
		}
	}
	
	private IRHelpKeyword loadKeyword(final FIO fio)
			throws IOException {
		final String keyword = fio.readString();
		final String description = fio.readString();
		final int count = fio.in.readInt();
		final IRHelpKeyword[] nestedKeywords = new IRHelpKeyword[count];
		for (int i = 0; i < count; i++) {
			nestedKeywords[i] = loadKeyword(fio);
		}
		return new RHelpKeyword(keyword, description,
				new ConstList<IRHelpKeyword>(nestedKeywords) );
	}
	
	private void savePackage(final IRPackageHelp packageHelp, final FIO fio)
			throws IOException {
		fio.writeString(packageHelp.getName());
		fio.writeString(packageHelp.getTitle());
		fio.writeString(packageHelp.getVersion());
		final List<IRHelpPage> pages = packageHelp.getHelpPages();
		final int count = pages.size();
		fio.out.writeInt(count);
		for (int i = 0; i < count; i++) {
			savePage(pages.get(i), fio);
		}
	}
	
	private IRPackageHelp loadPackage(final IREnvConfiguration rEnvConfig, final FIO fio)
			throws IOException {
		final String name = fio.readString();
		final String title = fio.readString();
		final String version = fio.readString();
		
		final int count = fio.in.readInt();
		final IRHelpPage[] pages = new IRHelpPage[count];
		final RPackageHelp pkg = new RPackageHelp(name, title, version, rEnvConfig.getReference(),
				new ConstList<IRHelpPage>(pages));
		for (int i = 0; i < count; i++) {
			pages[i] = loadPage(pkg, fio);
		}
		return pkg;
	}
	
	private void savePage(final IRHelpPage page, final FIO fio)
			throws IOException {
		fio.writeString(page.getName());
		fio.writeString(page.getTitle());
	}
	
	private IRHelpPage loadPage(final IRPackageHelp pkg, final FIO fio)
			throws IOException {
		final String name = fio.readString();
		final String title = fio.readString();
		return new RHelpPage(pkg, name, title);
	}
	
	public void delete(final String rEnvId) {
		final File[] list = getIndexResources(rEnvId);
		final boolean ok = delete(list);
		if (!ok) {
			RCorePlugin.log(new Status(IStatus.WARNING, RCore.PLUGIN_ID, -1,
					NLS.bind("No all R help data files could be deleted for ''{0}''.", rEnvId), null));
		}
	}
	
	private boolean delete(final File[] resources) {
		boolean ok = true;
		for (int i = 0; i < resources.length; i++) {
			if (resources[i].isDirectory()) {
				final File[] files = resources[i].listFiles();
				if (files.length > 0) {
					if (delete(files) && resources[i].delete()) {
						continue;
					}
					ok = false;
					continue;
				}
			}
			if (resources[i].delete()) {
				continue;
			}
			ok = false;
		}
		return ok;
	}
	
}
