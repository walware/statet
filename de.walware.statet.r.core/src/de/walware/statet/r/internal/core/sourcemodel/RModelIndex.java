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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;
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
import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.internal.core.RCorePlugin;
import de.walware.statet.r.internal.core.builder.CompositeFrame;
import de.walware.statet.r.internal.core.builder.RBuildReconciler;
import de.walware.statet.r.internal.core.builder.RUnitElement;


public class RModelIndex {
	
	
	private static final String VERSION = "9";
	
	
	private RModelManager fModelManager;
	private final RBuildReconciler fReconciler;
	
	private int fDBInitialized;
	private DataSource fConnectionPool;
	
	private final ReadWriteLock fLock = new ReentrantReadWriteLock();
	
	private final Map<String, Integer> fProjectIds = new ConcurrentHashMap<String, Integer>();
	private final Map<Integer, CompositeFrame> fElementsList = new HashMap<Integer, CompositeFrame>();
	
	
	public RModelIndex(final RModelManager manager) {
		fModelManager = manager;
		fReconciler = new RBuildReconciler(manager);
		
		initDB();
	}
	
	
	public void clear(final IProject project) {
		final Integer projectId = fProjectIds.get(project.getName());
		if (projectId != null) {
			fElementsList.remove(projectId);
			
			if (fDBInitialized == 1) {
				Connection connection = null;
				fLock.writeLock().lock();
				try {
					connection = fConnectionPool.getConnection();
					
					final PreparedStatement removeProject = connection.prepareStatement("delete from RINDEX.EXPORTS where (PROJECT_ID = ?)");
					removeProject.setInt(1, projectId.intValue());
					removeProject.execute();
				}
				catch (final SQLException e) {
					logDBError(e);
				}
				finally {
					fLock.writeLock().unlock();
					if (connection != null) {
						try {
							connection.close();
						} catch (final SQLException ignore) {}
					}
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
		final HashMap<String, RUnitElement> newItems = new HashMap<String, RUnitElement>();
		for (final IRSourceUnit su : update) {
			if (su instanceof IManagableRUnit) {
				try {
					final RUnitElement unitElement = fReconciler.build((IManagableRUnit) su, progress);
					if (unitElement != null) {
						newItems.put(su.getId(), unitElement);
					}
				}
				catch (final Exception e) {
					status.add(new Status(IStatus.ERROR, RCore.PLUGIN_ID, ICommonStatusConstants.BUILD_ERROR, 
							NLS.bind("An error occurred when indexing ''{0}''", su.getResource().getFullPath().toString()), e));
				}
			}
		}
		
		final Integer projectId = getOrCreateProjectId(project);
		Connection connection = null;
		fLock.writeLock().lock();
		try {
			if (fDBInitialized == 1) {
				connection = fConnectionPool.getConnection();
				connection.setAutoCommit(false);
			}
			
			CompositeFrame frame = fElementsList.get(projectId);
			if (remove == null) {
				fElementsList.remove(projectId);
				if (connection != null) {
					final PreparedStatement removeProject = connection.prepareStatement("delete from RINDEX.EXPORTS where (PROJECT_ID = ?)");
					removeProject.setInt(1, projectId.intValue());
					removeProject.execute();
					connection.commit();
				}
				if (update == null || update.isEmpty()) {
					return;
				}
				if (frame == null && project.isOpen()) {
					frame = new CompositeFrame(fLock, rProject.getPackageName(), project.getName(), null);
					fElementsList.put(projectId, frame);
				}
				if (frame == null) {
					return;
				}
			}
			else { // remove != null
				if (frame == null) {
					frame = getFrame(project, projectId, rProject, connection, progress);
				}
				if (frame == null) {
					return;
				}
			}
			
			if (fDBInitialized == 1) {
				final PreparedStatement updateStatement = connection.prepareStatement("update RINDEX.EXPORTS set OBJECTDATA = ? where (PROJECT_ID = ? AND SU_ID = ?)");
				updateStatement.setInt(2, projectId.intValue());
				final PreparedStatement insertStatement = connection.prepareStatement("insert into RINDEX.EXPORTS (PROJECT_ID, SU_ID, OBJECTDATA) values (?, ?, ?)");
				insertStatement.setInt(1, projectId.intValue());
				
				final ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
				for (final Entry<String, RUnitElement> newItem : newItems.entrySet()) {
					final String suId = newItem.getKey();
					final RUnitElement value = newItem.getValue();
					final RUnitElement previous = frame.setModelElement(suId, value);
					
					if (remove != null) {
						remove.remove(suId);
					}
					
					byteOutput.reset();
					value.save(byteOutput);
					final byte[] objectBytes = byteOutput.toByteArray();
					final ByteArrayInputStream objectStream = new ByteArrayInputStream(objectBytes);
					if (previous != null) {
						try {
							updateStatement.setString(3, suId);
							updateStatement.setBinaryStream(1, objectStream, objectBytes.length);
							updateStatement.execute();
							continue;
						}
						catch (final SQLException e) {
							logDBWarning(e, "(will try to insert)");
						}
					}
					try {
						insertStatement.setString(2, suId);
						insertStatement.setBinaryStream(3, objectStream, objectBytes.length);
						insertStatement.execute();
						continue;
					}
					catch (final SQLException e) {
						if (previous != null) {
							logDBError(e);
						}
						if (previous == null) {
							logDBWarning(e, "(will try to update)");
							try {
								updateStatement.setString(3, suId);
								updateStatement.setBinaryStream(1, objectStream, objectBytes.length);
								updateStatement.execute();
								continue;
							}
							catch (final SQLException e2) {
								logDBError(e2);
							}
						}
					}
				}
				
				if (remove != null && !remove.isEmpty()) {
					final PreparedStatement deleteStatement = connection.prepareStatement("delete from RINDEX.EXPORTS where (PROJECT_ID = ? AND SU_ID = ?)");
					deleteStatement.setInt(1, projectId);
					for (final String suId : remove) {
						frame.removeModelElement(suId);
						deleteStatement.setString(2, suId);
						deleteStatement.execute();
					}
				}
				
				connection.commit();
			}
			else {
				for (final Entry<String, RUnitElement> newItem : newItems.entrySet()) {
					final String suId = newItem.getKey();
					final RUnitElement value = newItem.getValue();
					frame.setModelElement(suId, value);
					
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
			logDBError(e);
		}
		catch (final IOException e) {
			logDBError(e);
		}
		finally {
			fLock.writeLock().unlock();
			if (connection != null) {
				try {
					connection.close();
				} catch (final SQLException ignore) {}
			}
		}
	}
	
	private CompositeFrame getFrame(final IProject project, final Integer projectId, RProject rProject, Connection connection, final IProgressMonitor monitor) throws SQLException {
		CompositeFrame frame = fElementsList.get(projectId);
		if (frame == null && project.isOpen()) {
			if (rProject == null) {
				rProject = RProject.getRProject(project);
			}
			if (rProject == null) {
				return null;
			}
			final HashMap<String, RUnitElement> elements = new HashMap<String, RUnitElement>();
			frame = new CompositeFrame(fLock, rProject.getPackageName(), project.getName(), elements);
			fElementsList.put(projectId, frame);
			
			if (fDBInitialized == 1) {
				Connection newConnection = null;
				try {
					if (connection == null) {
						connection = newConnection = fConnectionPool.getConnection();
					}
					final PreparedStatement statement = connection.prepareStatement("select SU_ID, OBJECTDATA FROM RINDEX.EXPORTS where (PROJECT_ID = ?)");
					statement.setInt(1, projectId);
					final ResultSet resultSet = statement.executeQuery();
					final String prefix = "platform:/resource/"+project.getFullPath().toPortableString()+"/";
					
					ITER_FOUND: while (resultSet.next()) {
						ISourceUnit su = null;
						try {
							final String suId = resultSet.getString(1);
							if (suId.startsWith(prefix)) {
								final IPath path = new Path(suId.substring(prefix.length()));
								final IFile file = project.getFile(path);
								su = ECommonsLTK.PERSISTENCE_CONTEXT.getUnit(file, RModel.TYPE_ID, true, monitor);
								final InputStream inputStream = resultSet.getBlob(2).getBinaryStream();
								final RUnitElement unitElement = RUnitElement.read(su, frame, inputStream);
								elements.put(su.getId(), unitElement);
								continue ITER_FOUND;
							}
						}
						catch (final Exception e) {
							logDBError(e);
						}
						finally {
							su.disconnect(monitor);
						}
					}
				}
				finally {
					if (newConnection != null) {
						try {
							newConnection.close();
						} catch (final SQLException ignore) {}
					}
				}
			}
		}
		
		return frame;
	}
	
	private Integer getOrCreateProjectId(final IProject project) {
		Integer projectId = fProjectIds.get(project.getName());
		if (projectId != null) {
			return projectId;
		}
		synchronized (fProjectIds) {
			projectId = fProjectIds.get(project.getName());
			if (projectId != null) {
				return projectId;
			}
			int id = project.getName().hashCode();
			do {
				projectId = Integer.valueOf(id);
				id++;
			}
			while (fProjectIds.containsValue(projectId));
			fProjectIds.put(project.getName(), projectId);
			
			doSaveProject(project.getName(), projectId);
		}
		
		return projectId;
	}
	
	public IRFrame getProjectFrame(final IProject project) {
		final Integer projectId = fProjectIds.get(project.getName());
		if (projectId != null) {
			fLock.readLock().lock();
			try {
				return getFrame(project, projectId, null, null, null);
			}
			catch (final SQLException e) {
				logDBError(e);
			}
			finally {
				fLock.readLock().unlock();
			}
		}
		return null;
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
			
			statement.execute("create table RINDEX.PROPERTIES (NAME varchar(254) not null, VALUE varchar(4096), primary key (NAME) )");
			statement.execute("create table RINDEX.PROJECTS (NAME varchar(1024) not null, ID int not null, primary key (NAME) )");
			statement.execute("create table RINDEX.EXPORTS (PROJECT_ID int not null, SU_ID varchar(4096) not null, OBJECTDATA blob, primary key (PROJECT_ID, SU_ID) )");
			
			statement.execute("insert into RINDEX.PROPERTIES (NAME, VALUE) values ('version', '"+VERSION+"')");
			
			connection.commit();
			return true;
		}
		catch (final SQLException e) {
			logDBError(e);
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
		Connection connection = null;
		Statement statement = null;
		try {
			final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			connection = fConnectionPool.getConnection();
			statement = connection.createStatement();
			PreparedStatement removeProjectId = null;
			PreparedStatement removeProjectExports = null;
			
			final ResultSet resultSet = statement.executeQuery("select NAME, ID from RINDEX.PROJECTS");
			while (resultSet.next()) {
				final String name = resultSet.getString(1);
				final int id = resultSet.getInt(2);
				if (root.getProject(name) != null) {
					fProjectIds.put(name, Integer.valueOf(id));
				}
				else {
					try {
						if (removeProjectExports == null) {
							removeProjectId = connection.prepareStatement("delete from RINDEX.PROJECTS where (ID = ?)");
							removeProjectExports = connection.prepareStatement("delete from RINDEX.EXPORTS where (PROJECT_ID = ?)");
						}
						removeProjectExports.setInt(1, id);
						removeProjectExports.execute();
						removeProjectId.setInt(1, id);
						removeProjectId.execute();
					}
					catch (final SQLException e) {
						logDBError(e);
					}
				}
			}
			return true;
		}
		catch (final SQLException e) {
			logDBError(e);
			fProjectIds.clear();
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
	
	private void doSaveProject(final String name, final Integer id) {
		if (fDBInitialized != 1) {
			return;
		}
		Connection connection = null;
		try {
			connection = fConnectionPool.getConnection();
			final PreparedStatement insert = connection.prepareStatement("insert into RINDEX.PROJECTS (NAME, ID) values (?, ?)");
			
			insert.setString(1, name);
			insert.setInt(2, id.intValue());
			insert.execute();
		}
		catch (final SQLException e) {
			logDBError(e);
		}
		finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (final SQLException ignore) {}
			}
		}
	}
	
	private void doRemoveProject(final Integer id) {
		if (fDBInitialized != 1) {
			return;
		}
		Connection connection = null;
		try {
			connection = fConnectionPool.getConnection();
			final PreparedStatement removeProjectId = connection.prepareStatement("delete from RINDEX.PROJECTS where (ID = ?)");
			final PreparedStatement removeProjectExports = connection.prepareStatement("delete from RINDEX.EXPORTS where (PROJECT_ID = ?)");
			removeProjectExports.setInt(1, id);
			removeProjectExports.execute();
			removeProjectId.setInt(1, id);
			removeProjectId.execute();
		}
		catch (final SQLException e) {
			logDBError(e);
		}
		finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (final SQLException ignore) {}
			}
		}
	}
	
	public void updateProjectConfig(final RProject rProject, final String packageName) {
		final IProject project = rProject.getProject();
		final Integer projectId = fProjectIds.get(project.getName());
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
			synchronized (fProjectIds) {
				final Integer projectId = fProjectIds.remove(project.getName());
				if (projectId != null) {
					doRemoveProject(projectId);
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
			synchronized (fProjectIds) {
				fProjectIds.remove(project.getName());
			}
		}
		finally {
			fLock.writeLock().unlock();
		}
	}
	
	private void logDBWarning(final Exception e, final String info) {
		RCorePlugin.log(new Status(IStatus.ERROR, RCore.PLUGIN_ID, -1, "An error occurred when replicate model to DB. " + info, e));
	}
	
	private void logDBError(final Exception e) {
		RCorePlugin.log(new Status(IStatus.ERROR, RCore.PLUGIN_ID, -1, "An error occurred when replicate model to DB.", e));
	}
	
}
