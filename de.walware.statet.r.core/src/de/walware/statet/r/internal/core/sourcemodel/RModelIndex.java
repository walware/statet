/*******************************************************************************
 * Copyright (c) 2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.core.sourcemodel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.sql.DataSource;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;

import de.walware.ecommons.ICommonStatusConstants;
import de.walware.ecommons.edb.EmbeddedDB;
import de.walware.ecommons.ltk.ECommonsLTK;
import de.walware.ecommons.ltk.ISourceUnit;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.RProject;
import de.walware.statet.r.core.model.IManagableRUnit;
import de.walware.statet.r.core.model.IRFrame;
import de.walware.statet.r.core.model.IRSourceUnit;
import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.internal.core.RCorePlugin;
import de.walware.statet.r.internal.core.builder.CompositeFrame;
import de.walware.statet.r.internal.core.builder.RBuildReconciler;
import de.walware.statet.r.internal.core.builder.RUnitElement;


public class RModelIndex {
	
	
	private static final String VERSION = "14";
	
	private static final String DEFINE_PROPERTIES_1 = "create table RINDEX.PROPERTIES ("+
				"NAME varchar(512) not null,"+
				"VALUE varchar(4096),"+
				"primary key (NAME)" +
			")";
	private static final String DEFINE_PROJECTS_1 = "create table RINDEX.PROJECTS (" +
				"NAME varchar(512) not null," +
				"ID int not null," +
				"primary key (NAME)," +
				"unique (ID)" +
			")";
	private static final String DEFINE_SUS_1 = "create table RINDEX.SUS (" +
				"PROJECT_ID int not null," +
				"NAME varchar(4096) not null," +
				"ID int not null," +
				"primary key (PROJECT_ID, NAME)," +
				"unique (PROJECT_ID, ID)" +
			")";
	private static final String DEFINE_NAMESIDX_1 = "create table RINDEX.NAMESIDX (" +
				"NAME varchar(512) not null," +
				"ID bigint not null generated always as identity," +
				"primary key (NAME)," +
				"unique (ID)" +
			")";
	private static final String DEFINE_EXPORTS_1 = "create table RINDEX.EXPORTS (" +
				"PROJECT_ID int not null," +
				"SU_ID int not null," +
				"OBJECTDATA blob," +
				"primary key (PROJECT_ID, SU_ID)" +
			")";
	private static final String DEFINE_MAINNAMES_1 = "create table RINDEX.MAINNAMES (" +
				"PROJECT_ID int not null," +
				"SU_ID int not null," +
				"NAME_ID bigint not null," +
				"primary key (PROJECT_ID, SU_ID, NAME_ID)" +
			")";
	private static final String DEFINE_MAINNAMES_2 = "create index PROJECT_NAME_IDX on RINDEX.MAINNAMES (" +
				"PROJECT_ID, NAME_ID" +
			")";
	
	private static final String ADD_PROJ = "insert into RINDEX.PROJECTS (NAME, ID) values (?, ?)";
	
	private static final String CLEAR_PROJ_EXPORTS = "delete from RINDEX.EXPORTS where (PROJECT_ID = ?)";
	private static final String CLEAR_PROJ_MAINNAMES = "delete from RINDEX.MAINNAMES where (PROJECT_ID = ?)";
	private static final String CLEAR_PROJ_SUS = "delete from RINDEX.SUS where (PROJECT_ID = ?)";
	
	private static final String REMOVE_PROJ = "delete from RINDEX.PROJECTS where (ID = ?)";
	
	private static final String REMOVE_SU_EXPORTS = "delete from RINDEX.EXPORTS where (PROJECT_ID = ? and SU_ID = ?)";
	private static final String REMOVE_SU_MAINNAMES = "delete from RINDEX.MAINNAMES where (PROJECT_ID = ? and SU_ID = ?)";
	private static final String REMOVE_SU = "delete from RINDEX.SUS where (PROJECT_ID = ? and ID = ?)";
	
	private static final String GET_PROJ_SUS_MAXID = "select MAX(ID) from RINDEX.SUS where (PROJECT_ID = ?)";
	private static final String GET_SU_ID = "select ID from RINDEX.SUS where (PROJECT_ID = ? and NAME = ?)";
	private static final String ADD_SU = "insert into RINDEX.SUS (PROJECT_ID, NAME, ID) values (?, ?, ?)";
	
	private static final String GET_NAMEIDX = "select ID from RINDEX.NAMESIDX where (NAME = ?)";
	private static final String ADD_NAMEIDX = "insert into RINDEX.NAMESIDX (NAME) values (?)";
	
	private static final class Proj {
		
		public final int id;
		public boolean removed;
		public int maxSuId;
		
		
		public Proj(final int id) {
			this.id = id;
			this.removed = false;
			this.maxSuId = -1;
		}
		
		
		@Override
		public int hashCode() {
			return id;
		}
		
		@Override
		public boolean equals(final Object obj) {
			return (id == ((Proj) obj).id);
		}
		
	}
	
	private static class DbTools {
		
		public final Connection connection;
		
		public Proj currentProj;
		public int currentSuId;
		public boolean currentSuNew;
		
		public DbTools(final Connection connection) throws SQLException {
			this.connection = connection;
			this.connection.setAutoCommit(false);
		}
		
		private PreparedStatement addProj;
		public void addProj(final String name, final Proj proj) throws SQLException {
			if (addProj == null) {
				addProj = connection.prepareStatement(ADD_PROJ);
			}
			addProj.setString(1, name);
			addProj.setInt(2, proj.id);
			addProj.executeUpdate();
		}
		
		private PreparedStatement fClearProjExportsStatement;
		private PreparedStatement fClearProjMainNamesStatement;
		private PreparedStatement fClearProjSusStatement;
		public void clearProj(final Proj proj) throws SQLException {
			clearProj(proj.id);
			proj.maxSuId = 0;
		}
		public void clearProj(final int projId) throws SQLException {
			if (fClearProjExportsStatement == null) {
				fClearProjExportsStatement = connection.prepareStatement(CLEAR_PROJ_EXPORTS);
				fClearProjMainNamesStatement = connection.prepareStatement(CLEAR_PROJ_MAINNAMES);
				fClearProjSusStatement = connection.prepareStatement(CLEAR_PROJ_SUS);
			}
			fClearProjExportsStatement.setInt(1, projId);
			fClearProjExportsStatement.executeUpdate();
			fClearProjMainNamesStatement.setInt(1, projId);
			fClearProjMainNamesStatement.executeUpdate();
			fClearProjSusStatement.setInt(1, projId);
			fClearProjSusStatement.executeUpdate();
		}
		
		private PreparedStatement removeProjStatement;
		public void removeProj(final int projId) throws SQLException {
			clearProj(projId);
			if (removeProjStatement == null) {
				removeProjStatement = connection.prepareStatement(REMOVE_PROJ);
			}
			removeProjStatement.setInt(1, projId);
			removeProjStatement.executeUpdate();
		}
		
		private PreparedStatement getProjSusMaxIdStatement;
		public void initProjForSu(final Proj proj) throws SQLException {
			if (proj.maxSuId != -1) {
				return;
			}
			if (getProjSusMaxIdStatement == null) {
				getProjSusMaxIdStatement = connection.prepareStatement(GET_PROJ_SUS_MAXID);
			}
			getProjSusMaxIdStatement.setInt(1, proj.id);
			final ResultSet result = getProjSusMaxIdStatement.executeQuery();
			if (result.next()) {
				proj.maxSuId = result.getInt(1);
			}
			else {
				throw new SQLException("Inconsistent state");
			}
		}
		
		private PreparedStatement fRemoveSuExportsStatement;
		private PreparedStatement fRemoveSuMainNamesStatement;
		private PreparedStatement fRemoveSuStatement;
		public void prepareRemoveSu(final Proj proj) throws SQLException {
			currentProj = proj;
			if (fRemoveSuExportsStatement == null) {
				fRemoveSuExportsStatement = connection.prepareStatement(REMOVE_SU_EXPORTS);
				fRemoveSuMainNamesStatement = connection.prepareStatement(REMOVE_SU_MAINNAMES);
				fRemoveSuStatement = connection.prepareStatement(REMOVE_SU);
			}
			fRemoveSuExportsStatement.setInt(1, proj.id);
			fRemoveSuMainNamesStatement.setInt(1, proj.id);
			fRemoveSuStatement.setInt(1, proj.id);
		}
		public void executeRemoveSu() throws SQLException {
			fRemoveSuExportsStatement.setInt(2, currentSuId);
			fRemoveSuExportsStatement.executeUpdate();
			fRemoveSuMainNamesStatement.setInt(2, currentSuId);
			fRemoveSuMainNamesStatement.executeUpdate();
			fRemoveSuStatement.setInt(2, currentSuId);
			fRemoveSuStatement.executeUpdate();
		}
		
		private PreparedStatement fGetSuIdStatement;
		private PreparedStatement fAddSuIdStatement;
		public void prepareGetSuId(final Proj proj) throws SQLException {
			currentProj = proj;
			if (fGetSuIdStatement == null) {
				fGetSuIdStatement = connection.prepareStatement(GET_SU_ID);
				fAddSuIdStatement = connection.prepareStatement(ADD_SU);
			}
			fGetSuIdStatement.setInt(1, proj.id);
			fAddSuIdStatement.setInt(1, proj.id);
		}
		public void executeGetOrAddSuId(final String publicId, final boolean newSuHint) throws SQLException {
			if (!newSuHint) {
				fGetSuIdStatement.setString(2, publicId);
				final ResultSet result = fGetSuIdStatement.executeQuery();
				if (result.next()) {
					currentSuId = result.getInt(1);
					currentSuNew = false;
					return;
				}
			}
			try {
				fAddSuIdStatement.setString(2, publicId);
				fAddSuIdStatement.setInt(3, ++currentProj.maxSuId);
				fAddSuIdStatement.executeUpdate();
				currentSuId = currentProj.maxSuId;
				currentSuNew = true;
				return;
			}
			catch (final SQLException e) {
				if (!newSuHint) {
					throw e;
				}
				logDBWarning(e, "(will try to update)");
				fGetSuIdStatement.setString(2, publicId);
				final ResultSet result = fGetSuIdStatement.executeQuery();
				if (result.next()) {
					currentSuId = result.getInt(1);
					currentSuNew = false;
					return;
				}
				else {
					throw new SQLException("Inconsistent state");
				}
			}
		}
		public boolean executeGetSuId(final String publicId) throws SQLException {
			fGetSuIdStatement.setString(2, publicId);
			final ResultSet result = fGetSuIdStatement.executeQuery();
			if (result.next()) {
				currentSuId = result.getInt(1);
				currentSuNew = false;
				return true;
			}
			return false;
		}
		
		public void clearSu() throws SQLException {
			if (fRemoveSuMainNamesStatement == null) {
				fRemoveSuMainNamesStatement = connection.prepareStatement(REMOVE_SU_MAINNAMES);
			}
			fRemoveSuMainNamesStatement.setInt(1, currentProj.id);
			fRemoveSuMainNamesStatement.setInt(2, currentSuId);
			fRemoveSuMainNamesStatement.executeUpdate();
		}
		
		private PreparedStatement fGetNameStatement;
		private PreparedStatement fAddNameStatement;
		public long getOrAddName(final String name) throws SQLException {
			if (fGetNameStatement == null) {
				fGetNameStatement = connection.prepareStatement(GET_NAMEIDX);
				fAddNameStatement = connection.prepareStatement(ADD_NAMEIDX, Statement.RETURN_GENERATED_KEYS);
			}
			fGetNameStatement.setString(1, name);
			ResultSet result = fGetNameStatement.executeQuery();
			if (result.next()) {
				return result.getLong(1);
			}
			fAddNameStatement.setString(1, name);
			fAddNameStatement.executeUpdate();
			result = fAddNameStatement.getGeneratedKeys();
			if (result.next()) {
				return result.getLong(1);
			}
			else {
				throw new SQLException("Inconsistent state");
			}
		}
	}
	
	
	private final RModelManager fModelManager;
	private final RBuildReconciler fReconciler;
	
	private int fDBInitialized;
	private DataSource fConnectionPool;
	
	private final ReadWriteLock fLock = new ReentrantReadWriteLock();
	private DbTools fDbTools;
	
	private final Map<String, Proj> fProjectIds = new ConcurrentHashMap<String, Proj>();
	private final Map<Proj, CompositeFrame> fElementsList = new HashMap<Proj, CompositeFrame>();
	
	
	public RModelIndex(final RModelManager manager) {
		fModelManager = manager;
		fReconciler = new RBuildReconciler(manager);
		
		initDB();
	}
	
	
	public void dispose() {
		fLock.writeLock().lock();
		try {
			fDBInitialized = 1000;
			closeDbTools();
		}
		finally {
			fLock.writeLock().lock();
		}
	}
	
	
	public void clear(final IProject project) {
		final Proj proj = fProjectIds.get(project.getName());
		if (proj != null) {
			fElementsList.remove(proj);
			
			if (fDBInitialized == 1) {
				fLock.writeLock().lock();
				try {
					final DbTools dbTools = getDbTools();
					dbTools.clearProj(proj);
					dbTools.connection.commit();
				}
				catch (final SQLException e) {
					onDbToolsError(e);
				}
				finally {
					fLock.writeLock().unlock();
				}
			}
		}
	}
	
	public void update(final IProject project, final List<String> remove, final List<IRSourceUnit> update, final MultiStatus status, final IProgressMonitor progress) throws CoreException {
		final RProject rProject = RProject.getRProject(project);
		if (rProject == null) {
			clear(project);
			return;
		}
		
		fReconciler.init(rProject, status);
		final HashMap<String, RBuildReconciler.Result> newItems = new HashMap<String, RBuildReconciler.Result>();
		for (final IRSourceUnit su : update) {
			if (su instanceof IManagableRUnit) {
				try {
					final RBuildReconciler.Result buildResult = fReconciler.build((IManagableRUnit) su, progress);
					if (buildResult != null) {
						newItems.put(su.getId(), buildResult);
					}
				}
				catch (final Exception e) {
					status.add(new Status(IStatus.ERROR, RCore.PLUGIN_ID, ICommonStatusConstants.BUILD_ERROR, 
							NLS.bind("An error occurred when indexing ''{0}''", su.getResource().getFullPath().toString()), e));
				}
			}
		}
		
		fLock.writeLock().lock();
		PreparedStatement updateExportsStatement = null;
		PreparedStatement insertExportsStatement = null;
		PreparedStatement insertMainNameStatement = null;
		try {
			final Proj proj = getOrCreateProjectId(project);
			
			CompositeFrame frame = fElementsList.get(proj);
			if (remove == null) {
				fElementsList.remove(proj);
				if (fDBInitialized == 1) {
					final DbTools dbTools = getDbTools();
					dbTools.clearProj(proj.id);
					dbTools.connection.commit();
				}
				if (update == null || update.isEmpty()) {
					return;
				}
				if (frame == null && project.isOpen()) {
					frame = new CompositeFrame(fLock, rProject.getPackageName(), project.getName(), null);
					fElementsList.put(proj, frame);
				}
				if (frame == null) {
					return;
				}
			}
			else { // remove != null
				if (frame == null) {
					final DbTools dbTools = getDbTools();
					frame = getFrame(project, proj, rProject, dbTools.connection, progress);
					dbTools.connection.commit();
				}
				if (frame == null) {
					return;
				}
			}
			
			if (fDBInitialized == 1) {
				DbTools dbTools = getDbTools();
				dbTools.initProjForSu(proj);
				
				dbTools.prepareGetSuId(proj);
				
				final ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
				for (final Entry<String, RBuildReconciler.Result> newItem : newItems.entrySet()) {
					if (updateExportsStatement == null) {
						updateExportsStatement = dbTools.connection.prepareStatement("update RINDEX.EXPORTS set OBJECTDATA = ? where (PROJECT_ID = ? and SU_ID = ?)");
						updateExportsStatement.setInt(2, proj.id);
						insertExportsStatement = dbTools.connection.prepareStatement("insert into RINDEX.EXPORTS (PROJECT_ID, SU_ID, OBJECTDATA) values (?, ?, ?)");
						insertExportsStatement.setInt(1, proj.id);
						insertMainNameStatement = dbTools.connection.prepareStatement("insert into RINDEX.MAINNAMES (PROJECT_ID, SU_ID, NAME_ID) values (?, ?, ?)");
						insertMainNameStatement.setInt(1, proj.id);
					}
					
					final String suNameId = newItem.getKey();
					final RBuildReconciler.Result value = newItem.getValue();
					final RUnitElement previous = frame.setModelElement(suNameId, value.exportedElement);
					
					if (remove != null) {
						remove.remove(suNameId);
					}
					
					dbTools.executeGetOrAddSuId(suNameId, (previous == null));
					
					byteOutput.reset();
					value.exportedElement.save(byteOutput);
					final byte[] objectBytes = byteOutput.toByteArray();
					final ByteArrayInputStream objectStream = new ByteArrayInputStream(objectBytes);
					try {
						if (dbTools.currentSuNew) {
							insertExportsStatement.setInt(2, dbTools.currentSuId);
							insertExportsStatement.setBinaryStream(3, objectStream, objectBytes.length);
							insertExportsStatement.execute();
						}
						else {
							updateExportsStatement.setInt(3, dbTools.currentSuId);
							updateExportsStatement.setBinaryStream(1, objectStream, objectBytes.length);
							updateExportsStatement.execute();
							dbTools.clearSu();
						}
						insertMainNameStatement.setInt(2, dbTools.currentSuId);
						for (final String name : value.defaultNames) {
							if (name != null) {
								insertMainNameStatement.setLong(3, dbTools.getOrAddName(name));
								insertMainNameStatement.executeUpdate();
							}
						}
						dbTools.connection.commit();
					}
					catch (final SQLException e) {
						onDbToolsError(e);
						dbTools = getDbTools();
						updateExportsStatement = null;
					}
				}
				
				if (remove != null && !remove.isEmpty()) {
					dbTools.prepareRemoveSu(proj);
					for (final String suNameId : remove) {
						frame.removeModelElement(suNameId);
						try {
							if (dbTools.executeGetSuId(suNameId)) {
								dbTools.executeRemoveSu();
							}
						}
						catch (final SQLException e) {
							logDBWarning(e, "(will continue with next)");
							continue;
						}
					}
					dbTools.connection.commit();
				}
				
			}
			else {
				for (final Entry<String, RBuildReconciler.Result> newItem : newItems.entrySet()) {
					final String suId = newItem.getKey();
					final RBuildReconciler.Result value = newItem.getValue();
					frame.setModelElement(suId, value.exportedElement);
					
					if (remove != null) {
						remove.remove(suId);
					}
				}
				for (final String suId : remove) {
					frame.removeModelElement(suId);
				}
			}
		}
		catch (final SQLException e) {
			onDbToolsError(e);
		}
		catch (final IOException e) {
			onDbToolsError(e);
		}
		finally {
			try {
				if (updateExportsStatement != null) {
					updateExportsStatement.close();
				}
				if (insertExportsStatement != null) {
					insertExportsStatement.close();
				}
				if (insertMainNameStatement != null) {
					insertMainNameStatement.close();
				}
			}
			catch (final SQLException ignore) {}
			fLock.writeLock().unlock();
		}
	}
	
	private CompositeFrame getFrame(final IProject project, final Proj proj, RProject rProject, Connection connection, final IProgressMonitor monitor) throws SQLException {
		CompositeFrame frame = fElementsList.get(proj);
		if (frame == null && project.isOpen()) {
			if (rProject == null) {
				rProject = RProject.getRProject(project);
			}
			if (rProject == null) {
				return null;
			}
			final HashMap<String, RUnitElement> elements = new HashMap<String, RUnitElement>();
			frame = new CompositeFrame(fLock, rProject.getPackageName(), project.getName(), elements);
			fElementsList.put(proj, frame);
			
			if (fDBInitialized == 1) {
				Connection newConnection = null;
				PreparedStatement statement = null;
				try {
					if (connection == null) {
						connection = newConnection = fConnectionPool.getConnection();
					}
					statement = connection.prepareStatement(
							"select S.NAME, E.OBJECTDATA from RINDEX.SUS as S" +
							" inner join RINDEX.EXPORTS as E on (E.PROJECT_ID = S.PROJECT_ID and E.SU_ID = S.ID)" +
							" where (S.PROJECT_ID = ?)");
					statement.setInt(1, proj.id);
					final ResultSet resultSet = statement.executeQuery();
					
					while (resultSet.next()) {
						ISourceUnit su = null;
						try {
							final String suId = resultSet.getString(1);
							su = ECommonsLTK.PERSISTENCE_CONTEXT.getUnit(suId, RModel.TYPE_ID, true, monitor);
							if (su != null) {
								final InputStream inputStream = resultSet.getBlob(2).getBinaryStream();
								final RUnitElement unitElement = RUnitElement.read(su, frame, inputStream);
								elements.put(su.getId(), unitElement);
							}
						}
						catch (final Exception e) {
							onDbReadError(e);
						}
						finally {
							if (su != null) {
								su.disconnect(monitor);
							}
						}
					}
				}
				finally {
					if (newConnection != null) {
						try {
							newConnection.close();
						} catch (final SQLException ignore) {}
					}
					else if (statement != null) {
						try {
							statement.close();
						} catch (final SQLException ignore) {}
					}
				}
			}
		}
		
		return frame;
	}
	
	/**
	 * Required write lock
	 * @param project
	 * @return
	 */
	private Proj getOrCreateProjectId(final IProject project) {
		Proj proj = fProjectIds.get(project.getName());
		if (proj != null) {
			return proj;
		}
		int id = project.getName().hashCode();
		do {
			proj = new Proj(id);
			id++;
		}
		while (fProjectIds.containsValue(proj));
		fProjectIds.put(project.getName(), proj);
		
		if (fDBInitialized == 1) {
			try {
				final DbTools dbTools = getDbTools();
				dbTools.addProj(project.getName(), proj);
				dbTools.connection.commit();
			}
			catch (final SQLException e) {
				onDbToolsError(e);
			}
		}
		
		return proj;
	}
	
	
	private void initDB() {
		if (fDBInitialized != 0) {
			return;
		}
		fDBInitialized = -1;
		try {
			final IPath location = RCorePlugin.getDefault().getStateLocation();
			final File directory = location.append("db").toFile();
			fConnectionPool = EmbeddedDB.createConnectionPool(directory.getAbsolutePath()+"");//"jdbc:derby:"
			
			if (fConnectionPool != null) {
				if (checkVersion()) {
					if (checkProjects()) {
						fDBInitialized = 1;
					}
				}
			}
		}
		catch (final Exception e) {
			fDBInitialized = -1;
			RCorePlugin.log(new Status(IStatus.ERROR, RCore.PLUGIN_ID, -1, "An error occurred when initializing DB for model.", e));
		}
	}
	
	private boolean checkVersion() {
		Connection connection = null;
		Statement statement = null;
		try {
			connection = fConnectionPool.getConnection();
			connection.setAutoCommit(false);
			statement = connection.createStatement();
			
			final ResultSet schemas = connection.getMetaData().getSchemas();
			boolean schemaExists = false;
			while (schemas.next()) {
				if ("RINDEX".equals(schemas.getString(1))) {
					schemaExists = true;
					break;
				}
			}
			
			if (schemaExists) {
				try {
					final ResultSet resultSet = statement.executeQuery("select VALUE from RINDEX.PROPERTIES where (NAME = 'version')");
					if (resultSet.next() && VERSION.equals(resultSet.getString(1))) {
						return true;
					}
				}
				catch (final SQLException ignore) {}
				
				final List<String> names = new ArrayList<String>();
				final ResultSet tables = connection.getMetaData().getTables(null, "RINDEX", null, new String[] { "TABLE" });
				while (tables.next()) {
					final String tableName = tables.getString("TABLE_NAME");
					if (tableName != null) {
						names.add(tableName);
					}
				}
				
				for (final String tableName : names) {
					statement.execute("drop table RINDEX."+tableName);
				}
			}
			
			statement.execute(DEFINE_PROPERTIES_1);
			statement.execute(DEFINE_PROJECTS_1);
			statement.execute(DEFINE_SUS_1);
			
			statement.execute(DEFINE_NAMESIDX_1);
			
			statement.execute(DEFINE_EXPORTS_1);
			statement.execute(DEFINE_MAINNAMES_1);
			statement.execute(DEFINE_MAINNAMES_2);
			
			statement.execute("insert into RINDEX.PROPERTIES (NAME, VALUE) values ('version', '"+VERSION+"')");
			
			connection.commit();
			return true;
		}
		catch (final SQLException e) {
			onDbToolsError(e);
			return false;
		}
		finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (final SQLException ignore) {}
			}
		}
	}
	
	private boolean checkProjects() {
		Statement statement = null;
		try {
			final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			final DbTools dbTools = getDbTools();
			statement = dbTools.connection.createStatement();
			
			final ResultSet resultSet = statement.executeQuery("select P.NAME, P.ID from RINDEX.PROJECTS as P");
			while (resultSet.next()) {
				final String name = resultSet.getString(1);
				final int id = resultSet.getInt(2);
				if (root.getProject(name) != null) {
					final Proj proj = new Proj(id);
					fProjectIds.put(name, proj);
				}
				else {
					try {
						dbTools.removeProj(id);
						dbTools.connection.commit();
					}
					catch (final SQLException e) {
						logDBWarning(e, "(will continue with next)");
					}
				}
			}
			return true;
		}
		catch (final SQLException e) {
			onDbToolsError(e);
			fProjectIds.clear();
			return false;
		}
		finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (final SQLException ignore) {}
			}
		}
	}
	
	public void updateProjectConfig(final RProject rProject, final String packageName) {
		final IProject project = rProject.getProject();
		final Proj projectId = fProjectIds.get(project.getName());
		if (projectId != null) {
			fLock.writeLock().lock();
			try {
				final CompositeFrame frame = fElementsList.get(projectId);
				if (frame != null) {
					fElementsList.put(projectId, new CompositeFrame(fLock, packageName, project.getName(), frame.fModelElements));
				}
			}
			finally {
				fLock.writeLock().unlock();
			}
		}
	}
	
	public void updateProjectConfigRemoved(final IProject project) {
		fLock.writeLock().lock();
		try {
			final Proj proj = fProjectIds.remove(project.getName());
			if (proj != null) {
				proj.removed = true;
				if (fDBInitialized == 1) {
					try {
						final DbTools dbTools = getDbTools();
						dbTools.removeProj(proj.id);
						dbTools.connection.commit();
					}
					catch (final SQLException e) {
						onDbToolsError(e);
					}
				}
			}
		}
		finally {
			fLock.writeLock().unlock();
		}
	}
	
	public void updateProjectConfigClosed(final IProject project) {
		fLock.writeLock().lock();
		try {
			fProjectIds.remove(project.getName());
		}
		finally {
			fLock.writeLock().unlock();
		}
	}
	
	private static void logDBWarning(final Exception e, final String info) {
		RCorePlugin.log(new Status(IStatus.ERROR, RCore.PLUGIN_ID, -1, "An error occurred when replicate model to DB. " + info, e));
	}
	
	private DbTools getDbTools() throws SQLException {
		if (fDbTools == null) {
			if (fDBInitialized > 1) {
				throw new SQLException("DB is closed.");
			}
			fDbTools = new DbTools(fConnectionPool.getConnection());
		}
		return fDbTools;
	}
	
	private void onDbToolsError(final Exception e) {
		RCorePlugin.log(new Status(IStatus.ERROR, RCore.PLUGIN_ID, -1, "An error occurred when replicate model to DB.", e));
		if (fDbTools != null) {
			try {
				fDbTools.connection.close();
				fDbTools = null;
			} catch (final SQLException ignore) {}
		}
	}
	
	private void closeDbTools() {
		if (fDbTools != null) {
			try {
				fDbTools.connection.close();
				fDbTools = null;
			}
			catch (final SQLException e) {
				onDbToolsError(e);
			}
		}
	}
	
	private void onDbReadError(final Exception e) {
		RCorePlugin.log(new Status(IStatus.ERROR, RCore.PLUGIN_ID, -1, "An error occurred when searching index in DB.", e));
	}
	
	
	public IRFrame getProjectFrame(final IProject project) {
		final Proj proj = fProjectIds.get(project.getName());
		if (proj != null) {
			fLock.readLock().lock();
			try {
				return getFrame(project, proj, null, null, null);
			}
			catch (final SQLException e) {
				onDbReadError(e);
			}
			finally {
				fLock.readLock().unlock();
			}
		}
		return null;
	}
	
	public List<String> findReferencingSourceUnits(final IProject project, final RElementName name) {
		if (name.getNextSegment() != null || name.getType() != RElementName.MAIN_DEFAULT || name.getSegmentName() == null) {
			throw new UnsupportedOperationException("Only common top level names are supported.");
		}
		final Proj proj = fProjectIds.get(project.getName());
		fLock.readLock().lock();
		Connection connection = null;
		try {
			if (proj == null || proj.removed) {
				return null;
			}
			connection = fConnectionPool.getConnection();
			final PreparedStatement statement = connection.prepareStatement("select S.NAME from RINDEX.SUS as S" +
					" inner join RINDEX.MAINNAMES as M on (M.PROJECT_ID = S.PROJECT_ID and M.SU_ID = S.ID)" +
					" inner join RINDEX.NAMESIDX as N on (M.NAME_ID = N.ID)" +
					" where (M.PROJECT_ID = ? and N.NAME = ?)");
			statement.setInt(1, proj.id);
			statement.setString(2, name.getSegmentName());
			final List<String> sus = new ArrayList<String>();
			final ResultSet result = statement.executeQuery();
			while (result.next()) {
				sus.add(result.getString(1));
			}
			return sus;
		}
		catch (final SQLException e) {
			onDbReadError(e);
			return null;
		}
		finally {
			fLock.readLock().unlock();
			if (connection != null) {
				try {
					connection.close();
				} catch (final SQLException ignore) {}
			}
		}
	}
}
