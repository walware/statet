/*=============================================================================#
 # Copyright (c) 2012-2015 Stephan Wahlbrink (WalWare.de) and others.
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
	
	/** DB definitions */
	@SuppressWarnings({ "hiding", "nls" })
	private static final class REnv { // SCHMEMA
		
		static final String NAME= "RENV";
		
		static final class LibPaths {
			
			static final String NAME= "LIBPATHS";
			static final String QNAME= REnv.NAME+'.'+NAME;
			
			static final String COL_ID= "LIB_ID";
			static final String COL_LIB_PATH= "LIB_PATH";
			static final String COL_STAMP= "STAMP";
			
			static final String DEFINE_1 = "create table " + QNAME + " ("
						+ COL_ID        + " int not null "
								+ "primary key "
								+ "generated always as identity, "
						+ COL_LIB_PATH      + " varchar(4096) not null unique, "
						+ COL_STAMP         + " bigint"
					+ ")";
			
			static final String OP_insert= "insert into " + QNAME + " ("
						+ COL_LIB_PATH + ") "
					+ "values (?)";
			
			static final String OP_delete_byPath= "delete from " + QNAME + " "
					+ "where (" + COL_LIB_PATH + " = ?)";
			
			static final String OP_getAll= "select "
						+ COL_ID + ", "
						+ COL_LIB_PATH + " "
					+ "from " + QNAME;
			
		}
		
		static final class Pkgs {
			
			static final String NAME= "PKGS";
			static final String QNAME= REnv.NAME+'.'+NAME;
			
			static final String COL_LIB_ID= "LIB_ID";
			static final String COL_NAME= "NAME";
			static final String COL_VERSION= "VERSION";
			static final String COL_BUILT= "BUILT";
			static final String COL_TITLE= "TITLE";
			static final String COL_FLAGS= "FLAGS";
			static final String COL_INST_STAMP= "INST_STAMP";
			static final String COL_REPO_ID= "REPO_ID";
			
			static final String DEFINE_1 = "create table " + QNAME + " ("
						+ COL_LIB_ID        + " int not null "
								+ "references " + LibPaths.QNAME + " on delete cascade, "
						+ COL_NAME          + " varchar(64) not null, "
						+ COL_VERSION       + " varchar(64) not null, "
						+ COL_BUILT         + " varchar(256) not null, "
						+ COL_TITLE         + " varchar(256) not null, "
						+ COL_FLAGS         + " int, "
						+ COL_INST_STAMP    + " bigint, "
						+ COL_REPO_ID       + " varchar(256), "
						+ "primary key ("
							+ COL_LIB_ID + ", "
							+ COL_NAME + ")"
					+ ")";
			
			static final String OP_insert= "insert into " + QNAME + " ("
						+ COL_LIB_ID + ", "
						+ COL_NAME + ", "
						+ COL_VERSION + ", "
						+ COL_BUILT + ", "
						+ COL_TITLE + ", "
						+ COL_FLAGS + ", "
						+ COL_INST_STAMP + ", "
						+ COL_REPO_ID + ") "
					+ "values (? , ?, ?, ?, ?, ?, ?, ?)";
			
			static final String OP_update= "update " + QNAME + " set "
						+ COL_VERSION + " = ?, "
						+ COL_BUILT + " = ?, "
						+ COL_TITLE + " = ?, "
						+ COL_FLAGS + " = ?, "
						+ COL_INST_STAMP + " = ?, "
						+ COL_REPO_ID +" = ? "
					+ "where (" + COL_LIB_ID + " = ? and " + COL_NAME + " = ?)";
			
			static final String OP_delete= "delete from " + QNAME + " "
					+ "where (" + COL_LIB_ID + " = ? and " + COL_NAME + " = ?)"; 
			
			static final String OP_get_ofLib= "select "
						+ COL_NAME + ", "
						+ COL_VERSION + ", "
						+ COL_BUILT + ", "
						+ COL_TITLE + ", "
						+ COL_FLAGS + ", "
						+ COL_INST_STAMP + ", "
						+ COL_REPO_ID + " "
					+ "from " + QNAME + " "
					+ "where (" + COL_LIB_ID + " = ?)";
			
		}
		
	}
	
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
			final File file = parent.getChild("db").toLocalFile(EFS.NONE, null); //$NON-NLS-1$
			final ConnectionFactory connectionFactory = EmbeddedDB.createConnectionFactory(file.getAbsolutePath());
			
			return new DB(rEnv, connectionFactory);
		}
		catch (final CoreException e) {
			
			return null;
		}
	}
	
	
	private final IREnv fREnv;
	
	private final Map<String, Integer> fLibIdMap = new HashMap<>();
	
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
			try (
				final Statement libStatement= connection.createStatement();
				final PreparedStatement pkgStatement= connection.prepareStatement(
						REnv.Pkgs.OP_get_ofLib )
			) {
				final ResultSet libResult = libStatement.executeQuery(REnv.LibPaths.OP_getAll);
				
				while (libResult.next()) {
					final String libPath = libResult.getString(2);
					final IRLibraryLocation location = getLibLocation(envLibs, libPath);
					if (location == null) {
						if (removeLibPath == null) {
							removeLibPath = new ArrayList<>();
						}
						removeLibPath.add(libPath);
					}
					else {
						final int id = libResult.getInt(1);
						fLibIdMap.put(location.getDirectoryPath(), id);
						
						final RPkgList<RPkgInfo> list = new RPkgList<>(16);
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
			}
			
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
		final Connection connection= getConnection();
		
		final ResultSet schemas= connection.getMetaData().getSchemas(null, REnv.NAME);
		while (schemas.next()) {
			if (REnv.NAME.equals(schemas.getString(1))) {
				return;
			}
		}
		
		try (final Statement statement= connection.createStatement()) {
			statement.execute(REnv.LibPaths.DEFINE_1);
			statement.execute(REnv.Pkgs.DEFINE_1);
			
			connection.commit();
		}
		catch (final SQLException e) {
			closeOnError();
			throw e;
		}
	}
	
	private void clean(final List<String> removeLibPath) throws SQLException {
		final Connection connection= getConnection();
		try (final PreparedStatement statement= connection.prepareStatement(
				REnv.LibPaths.OP_delete_byPath )) {
			for (final String libPath : removeLibPath) {
				statement.setString(1, libPath);
				statement.execute();
			}
			
			connection.commit();
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
			if (!changeSet.deleted.isEmpty()) {
				if (fPkgDeleteStatement == null) {
					fPkgDeleteStatement = connection.prepareStatement(REnv.Pkgs.OP_delete);
				}
				for (final IRPkgInfo pkg : changeSet.deleted) {
					final Integer id = fLibIdMap.get(pkg.getLibraryLocation().getDirectoryPath());
					fPkgDeleteStatement.setInt(1, id.intValue());
					fPkgDeleteStatement.setString(2, pkg.getName());
					
					fPkgDeleteStatement.execute();
				}
			}
			if (!changeSet.added.isEmpty()) {
				if (fPkgAddStatement == null) {
					fPkgAddStatement = connection.prepareStatement(REnv.Pkgs.OP_insert);
				}
				for (final IRPkgInfo pkg : changeSet.added) {
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
			if (!changeSet.changed.isEmpty()) {
				if (fPkgChangeStatement == null) {
					fPkgChangeStatement = connection.prepareStatement(REnv.Pkgs.OP_update);
				}
				for (final IRPkgInfo pkg : changeSet.changed) {
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
			fLibAddStatement= connection.prepareStatement(REnv.LibPaths.OP_insert,
					new String[] { REnv.LibPaths.COL_ID } );
		}
		fLibAddStatement.setString(1, location.getDirectoryPath());
		fLibAddStatement.execute();
		final ResultSet result= fLibAddStatement.getGeneratedKeys();
		if (result.next()) {
			final Integer id= result.getInt(1);
			fLibIdMap.put(location.getDirectoryPath(), id);
			return id;
		}
		throw new SQLException("Unexpected result");
	}
	
}
