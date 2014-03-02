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

package de.walware.statet.r.internal.core.pkgmanager;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbcp2.ConnectionFactory;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;

import de.walware.ecommons.edb.EmbeddedDB;

import de.walware.rj.renv.RNumVersion;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.pkgmanager.IRPkgInfo;
import de.walware.statet.r.core.pkgmanager.RPkgInfo;
import de.walware.statet.r.core.renv.IREnv;
import de.walware.statet.r.core.renv.IRLibraryGroup;
import de.walware.statet.r.core.renv.IRLibraryLocation;
import de.walware.statet.r.internal.core.RCorePlugin;


final class DB {
	
	private static final String SCHEMA = "RENV";
	private static final String PKGS_TABLE = SCHEMA + ".PKGS";
	private static final String LIBPATHS_TABLE = SCHEMA + ".LIBPATHS";
	
	private static final String DEFINE_LIBPATHS_1 = "create table " + LIBPATHS_TABLE + " (" +
				" LIB_ID int not null generated always as identity primary key," +
				" LIB_PATH varchar(4096) not null unique," +
				" STAMP bigint" +
			")";
	
	private static final String DEFINE_PKGS_1 = "create table " + PKGS_TABLE + " (" +
				" LIB_ID int not null references " + LIBPATHS_TABLE + " on delete cascade," +
				" NAME varchar(64) not null," +
				" VERSION varchar(64) not null," +
				" BUILT varchar(256) not null," +
				" TITLE varchar(256) not null," +
				" FLAGS int," +
				" INST_STAMP bigint," +
				" REPO_ID varchar(256)," +
				" primary key (LIB_ID, NAME)" +
			")";
	
	private static IRLibraryLocation getLibLocation(final List<? extends IRLibraryGroup> envLibs, final String path) {
		for (final IRLibraryGroup group : envLibs) {
			for (final IRLibraryLocation location : group.getLibraries()) {
				if (path.equals(location.getDirectoryPath())) {
					return location;
				}
			}
		}
		return null;
	}
	
	static DB create(final IREnv rEnv, final IFileStore parent) {
		try {
			final File file = parent.getChild("db").toLocalFile(EFS.NONE, null);
			final ConnectionFactory connectionFactory = EmbeddedDB.createConnectionFactory(file.getAbsolutePath());
			
			return new DB(rEnv, connectionFactory);
		}
		catch (final CoreException e) {
			
			return null;
		}
	}
	
	
	private final IREnv fREnv;
	
	private final Map<String, Integer> fLibIdMap = new HashMap<String, Integer>();
	
	private final ConnectionFactory fConnectionFactory;
	private Connection fConnection;
	
	private PreparedStatement fLibAddStatement;
	private PreparedStatement fPkgDeleteStatement;
	private PreparedStatement fPkgAddStatement;
	private PreparedStatement fPkgChangeStatement;
	
	
	private DB(final IREnv rEnv, final ConnectionFactory connectionFactory) throws CoreException {
		fREnv = rEnv;
		fConnectionFactory = connectionFactory;
	}
	
	
	private Connection getConnection() throws SQLException {
		if (fConnection != null) {
			try {
				if (!fConnection.isClosed()) {
					return fConnection;
				}
			}
			catch (final SQLException e) {}
			closeOnError();
		}
		fConnection = fConnectionFactory.createConnection();
		fConnection.setAutoCommit(false);
		return fConnection;
	}
	
	private void closeOnError() {
		if (fConnection != null) {
			try {
				fConnection.close();
			}
			catch (final SQLException e) {
			}
			fConnection = null;
			fLibAddStatement = null;
			fPkgDeleteStatement = null;
			fPkgAddStatement = null;
			fPkgChangeStatement = null;
		}
	}
	
	RPkgSet loadPkgs(final List<? extends IRLibraryGroup> envLibs) {
		try {
			checkDB();
			
			final RPkgSet newPkgs = new RPkgSet(8);
			List<String> removeLibPath = null;
			
			final Connection connection = getConnection();
			final Statement libStatement = connection.createStatement();
			
			final ResultSet libResult = libStatement.executeQuery("select" +
					" LIB_PATH, LIB_ID" +
					" from " + LIBPATHS_TABLE );
			
			final PreparedStatement pkgStatement = connection.prepareStatement("select" +
					" NAME, VERSION, BUILT, TITLE, FLAGS, INST_STAMP, REPO_ID" +
					" from " + PKGS_TABLE + 
					" where (LIB_ID = ?)");
			
			while (libResult.next()) {
				final String libPath = libResult.getString(1);
				final IRLibraryLocation location = getLibLocation(envLibs, libPath);
				if (location == null) {
					if (removeLibPath == null) {
						removeLibPath = new ArrayList<String>();
					}
					removeLibPath.add(libPath);
				}
				else {
					final int id = libResult.getInt(2);
					fLibIdMap.put(location.getDirectoryPath(), id);
					
					final RPkgList<RPkgInfo> list = new RPkgList<RPkgInfo>(16);
					newPkgs.getInstalled().add(location.getDirectoryPath(), list);
					
					pkgStatement.setInt(1, id);
					final ResultSet pkgResult = pkgStatement.executeQuery();
					while (pkgResult.next()) {
						final RPkgInfo pkg = new RPkgInfo(pkgResult.getString(1),
								RNumVersion.create(pkgResult.getString(2)),
								pkgResult.getString(3), pkgResult.getString(4), location,
								pkgResult.getInt(5), pkgResult.getLong(6), pkgResult.getString(7) );
						list.add(pkg);
					}
				}
			}
			
			pkgStatement.close();
			libStatement.close();
			
			if (removeLibPath != null) {
				clean(removeLibPath);
			}
			
			return newPkgs;
		}
		catch (final SQLException e) {
			closeOnError();
			final String name = fREnv.getName();
			RCorePlugin.log(new Status(IStatus.ERROR, RCore.PLUGIN_ID,
					NLS.bind("An error occurred when loading R package information of " +
							"the R environment '{0}'.", name ), e ));
			return null;
		}
	}
	
	private void checkDB() throws SQLException {
		final Connection connection = getConnection();
		
		final ResultSet schemas = connection.getMetaData().getSchemas(null, SCHEMA);
		while (schemas.next()) {
			if (SCHEMA.equals(schemas.getString(1))) {
				return;
			}
		}
		
		final Statement statement = connection.createStatement();
		try {
			statement.execute(DEFINE_LIBPATHS_1);
			statement.execute(DEFINE_PKGS_1);
			
			connection.commit();
			
			statement.close();
		}
		catch (final SQLException e) {
			closeOnError();
			throw e;
		}
	}
	
	private void clean(final List<String> removeLibPath) throws SQLException {
		final Connection connection = getConnection();
		final PreparedStatement statement = connection.prepareStatement("delete" +
				" from " + LIBPATHS_TABLE +
				" where (LIB_PATH = ?)" );
		try {
			for (final String libPath : removeLibPath) {
				statement.setString(1, libPath);
				statement.execute();
			}
			
			connection.commit();
			
			statement.close();
		}
		catch (final SQLException e) {
			closeOnError();
			throw e;
		}
	}
	
	void updatePkgs(final Change change) {
		try {
			final Connection connection = getConnection();
			
			final RPkgChangeSet changeSet = change.fInstalledPkgs;
			if (!changeSet.fDeleted.isEmpty()) {
				if (fPkgDeleteStatement == null) {
					fPkgDeleteStatement = connection.prepareStatement("delete" +
							" from " + PKGS_TABLE +
							" where (LIB_ID = ? and NAME = ?)" );
				}
				for (final IRPkgInfo pkg : changeSet.fDeleted) {
					final Integer id = fLibIdMap.get(pkg.getLibraryLocation().getDirectoryPath());
					fPkgDeleteStatement.setInt(1, id.intValue());
					fPkgDeleteStatement.setString(2, pkg.getName());
					
					fPkgDeleteStatement.execute();
				}
			}
			if (!changeSet.fAdded.isEmpty()) {
				if (fPkgAddStatement == null) {
					fPkgAddStatement = connection.prepareStatement("insert" +
							" into " + PKGS_TABLE + 
							" (LIB_ID, NAME, VERSION, BUILT, TITLE, FLAGS, INST_STAMP, REPO_ID)" +
							" VALUES (? , ?, ?, ?, ?, ?, ?, ?)");
				}
				for (final IRPkgInfo pkg : changeSet.fAdded) {
					Integer id = fLibIdMap.get(pkg.getLibraryLocation().getDirectoryPath());
					if (id == null) {
						id = addLib(connection, pkg.getLibraryLocation());
					}
					fPkgAddStatement.setInt(1, id.intValue());
					fPkgAddStatement.setString(2, pkg.getName());
					fPkgAddStatement.setString(3, pkg.getVersion().toString());
					fPkgAddStatement.setString(4, pkg.getBuilt());
					fPkgAddStatement.setString(5, pkg.getTitle());
					fPkgAddStatement.setInt(6, pkg.getFlags());
					fPkgAddStatement.setLong(7, pkg.getInstallStamp());
					fPkgAddStatement.setString(8, pkg.getRepoId());
					
					fPkgAddStatement.execute();
				}
			}
			if (!changeSet.fChanged.isEmpty()) {
				if (fPkgChangeStatement == null) {
					fPkgChangeStatement = connection.prepareStatement("update" +
							" " + PKGS_TABLE +
							" set VERSION = ?, BUILT = ?, TITLE = ?, FLAGS = ?, INST_STAMP = ?, REPO_ID = ?" +
							" where (LIB_ID = ? and NAME = ?)");
				}
				for (final IRPkgInfo pkg : changeSet.fChanged) {
					final Integer id = fLibIdMap.get(pkg.getLibraryLocation().getDirectoryPath());
					fPkgChangeStatement.setInt(7, id.intValue());
					fPkgChangeStatement.setString(8, pkg.getName());
					fPkgChangeStatement.setString(1, pkg.getVersion().toString());
					fPkgChangeStatement.setString(2, pkg.getBuilt());
					fPkgChangeStatement.setString(3, pkg.getTitle());
					fPkgChangeStatement.setInt(4, pkg.getFlags());
					fPkgChangeStatement.setLong(5, pkg.getInstallStamp());
					fPkgChangeStatement.setString(6, pkg.getRepoId());
					
					fPkgChangeStatement.execute();
				}
			}
			
			connection.commit();
		}
		catch (final SQLException e) {
			closeOnError();
			final String name = fREnv.getName();
			RCorePlugin.log(new Status(IStatus.ERROR, RCore.PLUGIN_ID,
					NLS.bind("An error occurred when saving R package information of " +
							"the R environment '{0}'.", name ), e ));
		}
	}
	
	private Integer addLib(final Connection connection, final IRLibraryLocation location) throws SQLException {
		if (fLibAddStatement == null) {
			fLibAddStatement = connection.prepareStatement("insert" +
					" into " + LIBPATHS_TABLE +
					" (LIB_PATH)" +
					" VALUES (?)", new String[] { "LIB_ID" });
		}
		fLibAddStatement.setString(1, location.getDirectoryPath());
		fLibAddStatement.execute();
		final ResultSet resultSet = fLibAddStatement.getGeneratedKeys();
		if (resultSet.next()) {
			final Integer id = resultSet.getInt(1);
			fLibIdMap.put(location.getDirectoryPath(), id);
			return id;
		}
		throw new SQLException("Unexpected result");
	}
	
}
