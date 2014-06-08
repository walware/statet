/*=============================================================================#
 # Copyright (c) 2009-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

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
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.osgi.util.NLS;

import de.walware.ecommons.ICommonStatusConstants;
import de.walware.ecommons.collections.ConstArrayList;
import de.walware.ecommons.collections.ConstList;
import de.walware.ecommons.collections.IntArrayMap;
import de.walware.ecommons.collections.IntMap;
import de.walware.ecommons.edb.EmbeddedDB;
import de.walware.ecommons.ltk.ISourceUnitManager;
import de.walware.ecommons.ltk.LTK;
import de.walware.ecommons.ltk.core.model.ISourceUnit;

import de.walware.statet.r.core.IRProject;
import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.model.IRFrame;
import de.walware.statet.r.core.model.IRModelInfo;
import de.walware.statet.r.core.model.IRSourceUnit;
import de.walware.statet.r.core.model.IRWorkspaceSourceUnit;
import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.core.model.RModelIndexUpdate;
import de.walware.statet.r.core.model.RSuModelContainer;
import de.walware.statet.r.internal.core.RCorePlugin;
import de.walware.statet.r.internal.core.RProject;
import de.walware.statet.r.internal.core.builder.CompositeFrame;
import de.walware.statet.r.internal.core.builder.RBuildReconciler;
import de.walware.statet.r.internal.core.builder.RUnitElement;


public class RModelIndex {
	
	/** DB definitions */
	@SuppressWarnings({ "hiding", "nls" })
	private static final class RIndex {
		
		static final String NAME= "RINDEX";
		
		static final String VERSION= "16";
		
		static final class Properties {
			
			static final String NAME= "PROPERTIES";
			static final String QNAME= RIndex.NAME+'.'+NAME;
			
			static final String COL_NAME= "NAME";
			static final String COL_VALUE= "VALUE";
			
			static final String DEFINE_1= "create table " + QNAME + " ("
						+ COL_NAME          + " varchar(512) not null "
								+ "primary key, "
						+ COL_VALUE         + " varchar(4096)"
					+ ")";
			
		}
		
		static final class Models {
			
			static final String NAME="MODELS";
			static final String QNAME= RIndex.NAME+'.'+NAME;
			
			/** model type id */
			static final String COL_MODEL_TYPE= "MODEL_TYPE";
			/** db-id of model type */
			static final String COL_ID= "ID";
			
			static final String DEFINE_1= "create table " + QNAME + " ("
					+ COL_ID                + " int not null "
							+ "primary key "
							+ "generated always as identity (start with 0), "
					+ COL_MODEL_TYPE        + " varchar(512) not null, "
					+ "unique ("
						+ COL_MODEL_TYPE + ")"
				+ ")";
			
			static final String OP_insert= "insert into " + QNAME + " ("
						+ COL_MODEL_TYPE + ") "
					+ "values (?)";
			
			static final String OP_getAll= "select "
						+ COL_MODEL_TYPE + ", "
						+ COL_ID + " "
					+ "from " + QNAME;
		
		}
		
		static final class Projects {
			
			static final String NAME= "PROJECTS";
			static final String QNAME= RIndex.NAME+'.'+NAME;
			
			/** name of project */
			static final String COL_NAME= "NAME";
			/** db-id of project */
			static final String COL_ID= "ID";
			
			static final String DEFINE_1= "create table " + QNAME + " ("
						+ COL_ID            + " int not null "
								+ "primary key "
								+ "generated always as identity, "
						+ COL_NAME          + " varchar(512) not null, "
						+ "unique ("
							+ COL_NAME + ")"
					+ ")";
			
			static final String OP_insert= "insert into " + QNAME + " ("
						+ COL_NAME + ") "
					+ "values (?)";
			
			static final String OP_delete= "delete from " + QNAME + " "
					+ "where (" + COL_ID + " = ?)";
			
			static final String OP_getAll= "select "
						+ COL_ID + ", "
						+ COL_NAME + " "
					+ "from " + QNAME;
			
		}
		
		static final class SourceUnits {
			
			static final String NAME= "SUS";
			static final String QNAME= RIndex.NAME+'.'+NAME;
			
			/** db-id of project -> Projects.ID */
			static final String COL_PROJECT_ID= "PROJECT_ID";
			/** name of source unit */
			static final String COL_NAME= "NAME";
			/** db-id of source unit */
			static final String COL_ID= "ID";
			/** db-id of model type */
			static final String COL_MODEL_TYPE_ID= "MODEL_TYPE_ID";
			
			static final String DEFINE_1= "create table " + QNAME + " ("
						+ COL_ID            + " bigint not null "
								+ "primary key "
								+ "generated always as identity, "
						+ COL_PROJECT_ID    + " int not null "
								+ "references " + Projects.QNAME + " on delete cascade, "
						+ COL_NAME          + " varchar(4096) not null, "
						+ COL_MODEL_TYPE_ID + " int not null "
								+ "references " + Models.QNAME + " on delete cascade, "
						+ "unique ("
							+ COL_PROJECT_ID + ", "
							+ COL_NAME + ")"
					+ ")";
			
			static final String OP_insert= "insert into " + QNAME + " ("
						+ COL_PROJECT_ID + ", "
						+ COL_NAME + ", "
						+ COL_MODEL_TYPE_ID + ") "
					+ "values (?, ?, ?)";
			
			static final String OP_updateModel= "update " + QNAME + " set "
						+ COL_MODEL_TYPE_ID + " = ? "
					+ "where (" + COL_ID + " = ?)";
			
			static final String OP_delete_byProjectAndName= "delete from " + QNAME + " "
					+ "where (" + COL_PROJECT_ID + " = ? and " + COL_NAME + "= ?)";
			
			static final String OP_deleteAll_ofProject= "delete from " + QNAME + " "
					+ "where (" + COL_PROJECT_ID + " = ?)";
			
			static final String OP_deleteAll_ofProjectAndModel= "delete from " + QNAME + " "
					+ "where (" + COL_PROJECT_ID + " = ? and " + COL_MODEL_TYPE_ID + " = ?)";
			
			static final String OP_get= "select "
						+ COL_ID + ", "
						+ COL_MODEL_TYPE_ID + " "
					+ "from " + QNAME + " "
					+ "where (" + COL_PROJECT_ID + " = ? and " + COL_NAME + " = ?)";
			
		}
		
		static final class NamesIdx {
			
			static final String NAME= "NAMESIDX";
			static final String QNAME= RIndex.NAME+'.'+NAME;
			
			/** db-id of name */
			static final String COL_ID= "ID";
			/** name */
			static final String COL_NAME= "NAME";
			
			static final String DEFINE_1= "create table " + QNAME + " ("
						+ COL_ID            + " bigint not null "
								+ "primary key "
								+ "generated always as identity, "
						+ COL_NAME          + " varchar(512) not null, "
						+ "unique ("
							+ COL_NAME + ")"
					+ ")";
			
			static final String OP_insert= "insert into " + QNAME + " ("
						+ COL_NAME + ") "
					+ "values (?)";
			
			static final String OP_getID= "select "
						+ COL_ID + " "
					+ "from " + QNAME + " "
					+ "where (" + COL_NAME + " = ?)";
		}
		
		static final class Exports {
			
			static final String NAME= "EXPORTS";
			static final String QNAME= RIndex.NAME+'.'+NAME;
			
			/** db-id of source unit -> SourceUnits.ID */
			static final String COL_SU_ID= "SU_ID";
			/** blob */
			static final String COL_OBJECTDATA= "OBJECTDATA";
			
			static final String DEFINE_1= "create table " + QNAME + " ("
						+ COL_SU_ID         + " bigint not null "
								+ "primary key "
								+ "references " + SourceUnits.QNAME + " on delete cascade, "
						+ COL_OBJECTDATA    + " blob"
					+ ")";
			
			static final String OP_insert= "insert into " + QNAME + " ("
						+ COL_SU_ID + ", "
						+ COL_OBJECTDATA + ") "
					+ "values (?, ?)";
			
			static final String OP_update= "update " + QNAME + " set "
						+ COL_OBJECTDATA + " = ? "
					+ "where (" + COL_SU_ID + " = ?)";
			
			static final String OP_getAll_ofProject= "select "
						+ "S."+SourceUnits.COL_NAME + ", "
						+ "S."+SourceUnits.COL_MODEL_TYPE_ID + ", "
						+ "E." + COL_OBJECTDATA + " "
					+ "from " + SourceUnits.QNAME + " as S "
					+ "inner join " + QNAME + " as E on (E."+COL_SU_ID + " = S."+SourceUnits.COL_ID + ") "
					+ "where (S."+SourceUnits.COL_PROJECT_ID + " = ?)";
			
		}
		
		static final class MainNames {
			
			static final String NAME= "MAINNAMES";
			static final String QNAME= RIndex.NAME+'.'+NAME;
			
			/** db-id of source unit -> SourceUnits.ID */
			static final String COL_SU_ID= "SU_ID";
			/** db-id of name -> Names.ID */
			static final String COL_NAME_ID= "NAME_ID";
			
			static final String DEFINE_1= "create table " + QNAME + " ("
						+ COL_SU_ID         + " bigint not null "
								+ "references " + SourceUnits.QNAME + " on delete cascade, "
						+ COL_NAME_ID       + " bigint not null "
								+ "references " + NamesIdx.QNAME + " on delete cascade, "
						+ "primary key ("
							+ COL_SU_ID + ", "
							+ COL_NAME_ID + ")"
					+ ")";
			
			static final String OP_insert= "insert into " + QNAME + " ("
						+ COL_SU_ID + ", "
						+ COL_NAME_ID + ") "
					+ "values (?, ?)";
			
			static final String OP_deleteAll_ofSourceUnit= "delete from " + QNAME + " "
					+ "where (" + COL_SU_ID + " = ?)";
			
			static final String OP_findSourceUnits_ofProjectAndName= "select "
						+ "S."+SourceUnits.COL_NAME + ", "
						+ "S."+SourceUnits.COL_MODEL_TYPE_ID + " "
					+ "from " + SourceUnits.QNAME + " as S "
					+ "inner join " + QNAME + " as M on (M."+COL_SU_ID + " = S."+SourceUnits.COL_ID + ") "
					+ "inner join " + NamesIdx.QNAME + " as N on (M."+COL_NAME_ID + " = N."+NamesIdx.COL_ID + ") "
					+ "where (S."+SourceUnits.COL_PROJECT_ID + " = ? and N."+NamesIdx.COL_NAME + " = ?)";
			
		}
		
	}
	
	
	private static final class Proj {
		
		public final int id;
		public boolean removed;
		
		
		public Proj(final int id) {
			this.id= id;
			this.removed= false;
		}
		
		
		@Override
		public int hashCode() {
			return this.id;
		}
		
		@Override
		public boolean equals(final Object obj) {
			return (this.id == ((Proj) obj).id);
		}
		
	}
	
	private static class DbTools {
		
		private static final String MISSING_GENERATED_RESULT= "Unexpected result (generatedKeys).";
		
		
		public final Connection connection;
		
		public DbTools(final Connection connection) throws SQLException {
			this.connection= connection;
			this.connection.setAutoCommit(false);
		}
		
		private PreparedStatement addModel;
		public int addModel(final String modelTypeId) throws SQLException {
			if (this.addModel == null) {
				this.addModel= this.connection.prepareStatement(RIndex.Models.OP_insert,
						new String[] { RIndex.Models.COL_ID } );
			}
			this.addModel.setString(1, modelTypeId);
			this.addModel.executeUpdate();
			final ResultSet result= this.addModel.getGeneratedKeys();
			if (result.next()) {
				return result.getInt(1);
			}
			throw new SQLException(MISSING_GENERATED_RESULT);
		}
		
		private PreparedStatement addProj;
		public int addProj(final String name) throws SQLException {
			if (this.addProj == null) {
				this.addProj= this.connection.prepareStatement(RIndex.Projects.OP_insert,
						new String[] { RIndex.Projects.COL_ID } );
			}
			this.addProj.setString(1, name);
			this.addProj.executeUpdate();
			final ResultSet result= this.addProj.getGeneratedKeys();
			if (result.next()) {
				return result.getInt(1);
			}
			throw new SQLException(MISSING_GENERATED_RESULT);
		}
		
		private PreparedStatement removeProjStatement;
		public void removeProj(final int projId) throws SQLException {
			if (this.removeProjStatement == null) {
				this.removeProjStatement= this.connection.prepareStatement(RIndex.Projects.OP_delete);
			}
			this.removeProjStatement.setInt(1, projId);
			this.removeProjStatement.executeUpdate();
		}
		
		private PreparedStatement clearProjStatement;
		public void clearProj(final int projId) throws SQLException {
			if (this.clearProjStatement == null) {
				this.clearProjStatement= this.connection.prepareStatement(RIndex.SourceUnits.OP_deleteAll_ofProject);
			}
			this.clearProjStatement.setInt(1, projId);
			this.clearProjStatement.executeUpdate();
		}
		
		private PreparedStatement clearProjModelStatement;
		public void clearProj(final int projId, final int modelId) throws SQLException {
			if (this.clearProjModelStatement == null) {
				this.clearProjModelStatement= this.connection.prepareStatement(RIndex.SourceUnits.OP_deleteAll_ofProjectAndModel);
			}
			this.clearProjModelStatement.setInt(1, projId);
			this.clearProjModelStatement.setInt(2, modelId);
			this.clearProjModelStatement.executeUpdate();
		}
		
		public Proj currentProj;
		public long currentUnitId;
		public boolean currentUnitNew;
		
		private PreparedStatement getUnitStatement;
		private PreparedStatement addUnitStatement;
		public void prepareUnits(final Proj proj) throws SQLException {
			this.currentProj= proj;
			if (this.getUnitStatement == null) {
				this.getUnitStatement= this.connection.prepareStatement(RIndex.SourceUnits.OP_get);
				this.addUnitStatement= this.connection.prepareStatement(RIndex.SourceUnits.OP_insert,
						new String[] { RIndex.SourceUnits.COL_ID } );
			}
			this.getUnitStatement.setInt(1, proj.id);
			this.addUnitStatement.setInt(1, proj.id);
		}
		/** requires {@link #initProjForSu(Proj)}, {@link #prepareUnits(Proj)} */
		private PreparedStatement updateUnitModelStatement;
		public void executeGetOrAddUnit(final String unitId, int modelId) throws SQLException {
			{	// get / update
				this.getUnitStatement.setString(2, unitId);
				final ResultSet result= this.getUnitStatement.executeQuery();
				if (result.next()) {
					this.currentUnitId= result.getLong(1);
					this.currentUnitNew= false;
					
					if (result.getInt(2) != modelId) {
						if (this.updateUnitModelStatement == null) {
							this.updateUnitModelStatement= this.connection.prepareStatement(RIndex.SourceUnits.OP_updateModel);
						}
						this.updateUnitModelStatement.setInt(1, modelId);
						this.updateUnitModelStatement.setLong(2, this.currentUnitId);
						this.updateUnitModelStatement.execute();
					}
					return;
				}
			}
			{	// insert
				this.addUnitStatement.setString(2, unitId);
				this.addUnitStatement.setInt(3, modelId);
				this.addUnitStatement.executeUpdate();
				final ResultSet result= this.addUnitStatement.getGeneratedKeys();
				if (result.next()) {
					this.currentUnitId= result.getLong(1);
					this.currentUnitNew= true;
					return;
				}
				throw new SQLException(MISSING_GENERATED_RESULT);
			}
		}
		/** requires {@link #prepareUnits(Proj)} */
		public boolean executeGetUnit(final String publicId) throws SQLException {
			this.getUnitStatement.setString(2, publicId);
			final ResultSet result= this.getUnitStatement.executeQuery();
			if (result.next()) {
				this.currentUnitId= result.getInt(1);
				this.currentUnitNew= false;
				return true;
			}
			return false;
		}
		
		/** requires {@link #prepareUnits(Proj)} */
		private PreparedStatement removeUnitStatement;
		public void executeRemoveUnit(final String unitId) throws SQLException {
			if (this.removeUnitStatement == null) {
				this.removeUnitStatement= this.connection.prepareStatement(RIndex.SourceUnits.OP_delete_byProjectAndName);
			}
			this.removeUnitStatement.setInt(1, this.currentProj.id);
			this.removeUnitStatement.setString(2, unitId);
			this.removeUnitStatement.executeUpdate();
		}
		
		private PreparedStatement removeUnitMainNamesStatement;
		public void clearUnitNames() throws SQLException {
			if (this.removeUnitMainNamesStatement == null) {
				this.removeUnitMainNamesStatement= this.connection.prepareStatement(RIndex.MainNames.OP_deleteAll_ofSourceUnit);
			}
			this.removeUnitMainNamesStatement.setLong(1, this.currentUnitId);
			this.removeUnitMainNamesStatement.executeUpdate();
		}
		
		private PreparedStatement getNameStatement;
		private PreparedStatement addNameStatement;
		public long getOrAddName(final String name) throws SQLException {
			if (this.getNameStatement == null) {
				this.getNameStatement= this.connection.prepareStatement(RIndex.NamesIdx.OP_getID);
				this.addNameStatement= this.connection.prepareStatement(RIndex.NamesIdx.OP_insert,
						new String[] { RIndex.NamesIdx.COL_ID } );
			}
			{	// get
				this.getNameStatement.setString(1, name);
				final ResultSet result= this.getNameStatement.executeQuery();
				if (result.next()) {
					return result.getLong(1);
				}
			}
			{	// add
				this.addNameStatement.setString(1, name);
				this.addNameStatement.executeUpdate();
				final ResultSet result= this.addNameStatement.getGeneratedKeys();
				if (result.next()) {
					return result.getLong(1);
				}
				throw new SQLException(MISSING_GENERATED_RESULT);
			}
		}
		
	}
	
	
	private final ISourceUnitManager sourceUnitManager;
	private final RBuildReconciler reconciler;
	
	private final ReadWriteLock lock= new ReentrantReadWriteLock();
	
	private int dbInitialized;
	private DataSource dbConnectionPool;
	private DbTools dbTools;
	
	private final Map<String, Integer> modelType2Id= new HashMap<>();
	private final IntMap<String> modelId2Type= new IntArrayMap<>();
	
	private final Map<String, Proj> projects= new ConcurrentHashMap<>();
	
	private final Map<Proj, CompositeFrame> elementsList= new HashMap<>();
	
	
	public RModelIndex(final RModelManager manager) {
		this.sourceUnitManager= LTK.getSourceUnitManager();
		this.reconciler= new RBuildReconciler(manager);
		
		initDB();
	}
	
	
	public void dispose() {
		this.lock.writeLock().lock();
		try {
			this.dbInitialized= 1000;
			closeDbTools();
		}
		finally {
			this.lock.writeLock().unlock();
		}
	}
	
	
	public void clear(final IProject project) {
		final Proj proj= this.projects.get(project.getName());
		if (proj != null) {
			this.elementsList.remove(proj);
			
			if (this.dbInitialized == 1) {
				this.lock.writeLock().lock();
				try {
					final DbTools tools= getDbTools();
					tools.clearProj(proj.id);
					tools.connection.commit();
				}
				catch (final SQLException e) {
					onDbToolsError(e);
				}
				finally {
					this.lock.writeLock().unlock();
				}
			}
		}
	}
	
	private static final ConstList<String> R_MODEL_TYPES= new ConstArrayList<>(RModel.TYPE_ID);
	public void update(final IRProject rProject, final List<String> remove,
			final List<IRWorkspaceSourceUnit> update, final MultiStatus status, final IProgressMonitor progress) throws CoreException {
		this.reconciler.init(rProject, status);
		final RModelIndexUpdate indexUpdate= new RModelIndexUpdate(rProject, R_MODEL_TYPES,
				(remove == null) );
		for (final IRWorkspaceSourceUnit sourceUnit : update) {
			final RSuModelContainer adapter= (RSuModelContainer) sourceUnit.getAdapter(RSuModelContainer.class);
			if (adapter != null) {
				try {
					final IRModelInfo model= this.reconciler.build(adapter, progress);
					indexUpdate.update(sourceUnit, model);
				}
				catch (final Exception e) {
					status.add(new Status(IStatus.ERROR, RCore.PLUGIN_ID, ICommonStatusConstants.BUILD_ERROR, 
							NLS.bind("An error occurred when indexing ''{0}''", sourceUnit.getResource().getFullPath().toString()), e));
				}
			}
		}
		
		update(indexUpdate, progress);
	}
	
	public void update(final RModelIndexUpdate order, final IProgressMonitor progress)
			throws CoreException {
		this.lock.writeLock().lock();
		try {
			if (this.dbInitialized != 1) {
				return; // exception?
			}
			final Proj proj= getOrCreateProjectId(order.rProject.getProject());
			if (proj == null) {
				return;
			}
			
			CompositeFrame frame= this.elementsList.get(proj);
			if (order.isFullBuild) {
				final DbTools tools= getDbTools();
				for (final String modelTypeId : order.modelTypeIds) {
					if (frame != null) {
						frame.removeModelElements(modelTypeId);
					}
					final int modelId= getOrCreateModelId(modelTypeId);
					tools.clearProj(proj.id, modelId);
				}
				tools.connection.commit();
				
				if (order.updated.isEmpty()) {
					return;
				}
			}
			if (frame == null) {
				final DbTools tools= getDbTools();
				frame= getFrame(proj, order.rProject, tools.connection, progress);
				tools.connection.commit();
				
				if (frame == null) {
					if (order.isFullBuild) {
						frame= new CompositeFrame(this.lock, order.rProject.getPackageName(),
								order.projectName, null );
						this.elementsList.put(proj, frame);
					}
					else {
						return;
					}
				}
			}
			
			{	DbTools tools= null;
				PreparedStatement updateExportsStatement= null;
				PreparedStatement insertExportsStatement= null;
				PreparedStatement insertMainNameStatement= null;
				final ByteArrayOutputStream byteOutput= new ByteArrayOutputStream();
				for (final RModelIndexOrder.Result updated : order.updated) {
					if (tools == null) {
						tools= getDbTools();
						tools.prepareUnits(proj);
						insertExportsStatement= tools.connection.prepareStatement(RIndex.Exports.OP_insert);
						updateExportsStatement= tools.connection.prepareStatement(RIndex.Exports.OP_update);
						insertMainNameStatement= tools.connection.prepareStatement(RIndex.MainNames.OP_insert);
					}
					
					final ISourceUnit sourceUnit= updated.exportedElement.getSourceUnit();
					final int modelId= getOrCreateModelId(sourceUnit.getModelTypeId());
					
					final RUnitElement previous= frame.setModelElement(updated.unitId, updated.exportedElement);
					
					order.removed.remove(updated.unitId);
					
					tools.executeGetOrAddUnit(updated.unitId, modelId);
					
					byteOutput.reset();
					updated.exportedElement.save(byteOutput);
					final byte[] objectBytes= byteOutput.toByteArray();
					final ByteArrayInputStream objectStream= new ByteArrayInputStream(objectBytes);
					try {
						if (tools.currentUnitNew) {
							insertExportsStatement.setLong(1, tools.currentUnitId);
							insertExportsStatement.setBinaryStream(2, objectStream, objectBytes.length);
							insertExportsStatement.execute();
						}
						else {
							updateExportsStatement.setLong(2, tools.currentUnitId);
							updateExportsStatement.setBinaryStream(1, objectStream, objectBytes.length);
							updateExportsStatement.execute();
							tools.clearUnitNames();
						}
						insertMainNameStatement.setLong(1, tools.currentUnitId);
						for (final String name : updated.defaultNames) {
							if (name != null) {
								insertMainNameStatement.setLong(2, tools.getOrAddName(name));
								insertMainNameStatement.executeUpdate();
							}
						}
						tools.connection.commit();
					}
					catch (final SQLException e) {
						try {
							if (insertExportsStatement != null) {
								insertExportsStatement.close();
							}
							if (updateExportsStatement != null) {
								updateExportsStatement.close();
							}
							if (insertMainNameStatement != null) {
								insertMainNameStatement.close();
							}
						}
						catch (final SQLException ignore) {}
						
						onDbToolsError(e);
						tools= null;
					}
				}
				
				if (!order.removed.isEmpty()) {
					if (tools == null) {
						tools= getDbTools();
						tools.prepareUnits(proj);
					}
					for (final String unitId : order.removed) {
						frame.removeModelElement(unitId);
						try {
							tools.executeRemoveUnit(unitId);
						}
						catch (final SQLException e) {
							logDBWarning(e, "(will continue with next)");
							continue;
						}
					}
					tools.connection.commit();
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
			this.lock.writeLock().unlock();
		}
	}
	
	private CompositeFrame getFrame(final Proj proj, final IRProject rProject,
			Connection connection, final IProgressMonitor monitor) throws SQLException, CoreException {
		CompositeFrame frame= this.elementsList.get(proj);
		if (frame == null && rProject.getProject().isOpen()) {
			final HashMap<String, RUnitElement> elements= new HashMap<>();
			frame= new CompositeFrame(this.lock, rProject.getPackageName(), rProject.getProject().getName(), elements);
			this.elementsList.put(proj, frame);
			
			if (this.dbInitialized == 1) {
				Connection newConnection= null;
				PreparedStatement statement= null;
				try {
					if (connection == null) {
						connection= newConnection= this.dbConnectionPool.getConnection();
					}
					statement= connection.prepareStatement(RIndex.Exports.OP_getAll_ofProject);
					statement.setInt(1, proj.id);
					final ResultSet result= statement.executeQuery();
					
					while (result.next()) {
						ISourceUnit su= null;
						try {
							final String unitId= result.getString(1);
							final String modelTypeId= this.modelId2Type.get(result.getInt(2));
							assert (unitId != null && modelTypeId != null);
							su= this.sourceUnitManager.getSourceUnit(modelTypeId,
									LTK.PERSISTENCE_CONTEXT, unitId, true, monitor );
							if (su instanceof IRSourceUnit) {
								final InputStream inputStream= result.getBlob(3).getBinaryStream();
								final RUnitElement unitElement= RUnitElement.read((IRSourceUnit) su, frame, inputStream);
								elements.put(su.getId(), unitElement);
							}
						}
						catch (final IOException e) {
							onDbReadError(e);
						}
						catch (final ClassNotFoundException e) {
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
	 * @param projectName
	 * @return
	 * @throws SQLException 
	 */
	private int getOrCreateModelId(final String modelTypeId) throws SQLException {
		Integer id= this.modelType2Id.get(modelTypeId);
		if (id == null) {
			
			final DbTools tools= getDbTools();
			id= Integer.valueOf(tools.addModel(modelTypeId));
			tools.connection.commit();
			
			this.modelType2Id.put(modelTypeId, id);
			this.modelId2Type.put(id, modelTypeId);
		}
		return id.intValue();
	}
	
	/**
	 * Required write lock
	 * @param projectName
	 * @return
	 * @throws SQLException 
	 */
	private Proj getOrCreateProjectId(final IProject project) throws SQLException {
		Proj proj= this.projects.get(project.getName());
		if (proj == null) {
			if (!project.isOpen() || RProject.getRProject(project) == null) {
				return null;
			}
			final DbTools tools= getDbTools();
			final int id= tools.addProj(project.getName());
			tools.connection.commit();
			
			proj= new Proj(id);
			this.projects.put(project.getName(), proj);
		}
		return proj;
	}
	
	
	private void initDB() {
		if (this.dbInitialized != 0) {
			return;
		}
		this.dbInitialized= -1;
		try {
			final IPath location= RCorePlugin.getDefault().getStateLocation();
			final File directory= location.append("db").toFile(); //$NON-NLS-1$
			this.dbConnectionPool= EmbeddedDB.createConnectionPool(directory.getAbsolutePath()); //"jdbc:derby:"
			
			if (this.dbConnectionPool != null) {
				if (checkVersion()) {
					if (loadModelTypes() && checkProjects()) {
						this.dbInitialized= 1;
					}
				}
			}
		}
		catch (final Exception e) {
			this.dbInitialized= -1;
			RCorePlugin.log(new Status(IStatus.ERROR, RCore.PLUGIN_ID, -1, "An error occurred when initializing DB for model.", e));
		}
	}
	
	private boolean checkVersion() {
		Connection connection= null;
		try {
			connection= this.dbConnectionPool.getConnection();
			connection.setAutoCommit(false);
			
			final ResultSet schemas= connection.getMetaData().getSchemas();
			boolean schemaExists= false;
			while (schemas.next()) {
				if (RIndex.NAME.equals(schemas.getString(1))) {
					schemaExists= true;
					break;
				}
			}
			
			if (schemaExists) {
				try (final Statement statement= connection.createStatement()) {
					final ResultSet result= statement.executeQuery("select VALUE from RINDEX.PROPERTIES where (NAME = 'version')");
					if (result.next() && RIndex.VERSION.equals(result.getString(1))) {
						return true;
					}
				}
				catch (final SQLException ignore) {}
				
				final List<String> names= new ArrayList<>();
				final ResultSet tables= connection.getMetaData().getTables(null, RIndex.NAME, null, new String[] { "TABLE" }); //$NON-NLS-1$
				while (tables.next()) {
					final String tableName= tables.getString("TABLE_NAME"); //$NON-NLS-1$
					if (tableName != null) {
						names.add(tableName);
					}
				}
				// Dependencies
				if (names.remove(RIndex.NamesIdx.NAME)) {
					names.add(RIndex.NamesIdx.NAME);
				}
				if (names.remove(RIndex.SourceUnits.NAME)) {
					names.add(RIndex.SourceUnits.NAME);
				}
				if (names.remove(RIndex.Projects.NAME)) {
					names.add(RIndex.Projects.NAME);
				}
				if (names.remove(RIndex.Models.NAME)) {
					names.add(RIndex.Models.NAME);
				}
				
				try (final Statement statement= connection.createStatement()) {
					for (final String name : names) {
						statement.execute("drop table " + RIndex.NAME + '.' + name); //$NON-NLS-1$
					}
				}
			}
			
			try (final Statement statement= connection.createStatement()) {
				statement.execute(RIndex.Properties.DEFINE_1);
				
				statement.execute(RIndex.Models.DEFINE_1);
				statement.execute(RIndex.Projects.DEFINE_1);
				statement.execute(RIndex.SourceUnits.DEFINE_1);
				
				statement.execute(RIndex.NamesIdx.DEFINE_1);
				
				statement.execute(RIndex.MainNames.DEFINE_1);
				statement.execute(RIndex.Exports.DEFINE_1);
				
				statement.execute("insert into " + RIndex.Properties.QNAME + " (NAME, VALUE) values ('version', '" + RIndex.VERSION + "')");
			}
			
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
	
	private boolean loadModelTypes() {
		try {
			this.modelType2Id.clear();
			this.modelId2Type.clear();
			
			final DbTools tools= getDbTools();
			try (final Statement statement= tools.connection.createStatement()) {
				final ResultSet result= statement.executeQuery(RIndex.Models.OP_getAll);
				while (result.next()) {
					final String modelTypeId= result.getString(1).intern();
					final int id= result.getInt(2);
					this.modelType2Id.put(modelTypeId, Integer.valueOf(id));
					this.modelId2Type.put(id, modelTypeId);
				}
				return true;
			}
		}
		catch (final SQLException e) {
			this.modelType2Id.clear();
			this.modelId2Type.clear();
			
			onDbToolsError(e);
			return false;
		}
	}
	
	private boolean checkProjects() {
		try {
			final IWorkspaceRoot root= ResourcesPlugin.getWorkspace().getRoot();
			final DbTools tools= getDbTools();
			try (final Statement statement= tools.connection.createStatement()) {
				final ResultSet result= statement.executeQuery(RIndex.Projects.OP_getAll);
				while (result.next()) {
					final int id= result.getInt(1);
					final String name= result.getString(2);
					final IProject project= root.getProject(name);
					if (project != null && project.isOpen()) {
						final Proj proj= new Proj(id);
						this.projects.put(name, proj);
					}
					else {
						try {
							tools.removeProj(id);
							tools.connection.commit();
						}
						catch (final SQLException e) {
							logDBWarning(e, "(will continue with next)");
						}
					}
				}
				return true;
			}
		}
		catch (final SQLException e) {
			this.projects.clear();
			
			onDbToolsError(e);
			return false;
		}
	}
	
	private void removeProject(final String projectName) {
		final Proj proj= this.projects.remove(projectName);
		if (proj != null) {
			proj.removed= true;
			if (this.dbInitialized == 1) {
				try {
					final DbTools tools= getDbTools();
					tools.removeProj(proj.id);
					tools.connection.commit();
				}
				catch (final SQLException e) {
					onDbToolsError(e);
				}
			}
		}
	}
	
	public void updateProjectConfig(final IRProject rProject, final String packageName) {
		final IProject project= rProject.getProject();
		final Proj projectId= this.projects.get(project.getName());
		if (projectId != null) {
			this.lock.writeLock().lock();
			try {
				final CompositeFrame frame= this.elementsList.get(projectId);
				if (frame != null) {
					this.elementsList.put(projectId, new CompositeFrame(this.lock, packageName, project.getName(), frame.fModelElements));
				}
			}
			finally {
				this.lock.writeLock().unlock();
			}
		}
	}
	
	public void updateProjectConfigRemoved(final IProject project) {
		this.lock.writeLock().lock();
		try {
			removeProject(project.getName());
		}
		finally {
			this.lock.writeLock().unlock();
		}
	}
	
	public void updateProjectConfigClosed(final IProject project) {
		this.lock.writeLock().lock();
		try {
			removeProject(project.getName());
		}
		finally {
			this.lock.writeLock().unlock();
		}
	}
	
	private static void logDBWarning(final Exception e, final String info) {
		RCorePlugin.log(new Status(IStatus.ERROR, RCore.PLUGIN_ID, -1, "An error occurred when replicate model to DB. " + info, e));
	}
	
	private DbTools getDbTools() throws SQLException {
		if (this.dbTools == null) {
			if (this.dbInitialized > 1) {
				throw new SQLException("DB is closed.");
			}
			this.dbTools= new DbTools(this.dbConnectionPool.getConnection());
		}
		return this.dbTools;
	}
	
	private void onDbToolsError(final Exception e) {
		RCorePlugin.log(new Status(IStatus.ERROR, RCore.PLUGIN_ID, -1, "An error occurred when replicate model to DB.", e));
		if (this.dbTools != null) {
			try {
				this.dbTools.connection.close();
				this.dbTools= null;
			}
			catch (final SQLException ignore) {}
		}
	}
	
	private void closeDbTools() {
		if (this.dbTools != null) {
			try {
				this.dbTools.connection.close();
				this.dbTools= null;
			}
			catch (final SQLException e) {
				onDbToolsError(e);
			}
		}
	}
	
	private void onDbReadError(final Exception e) throws CoreException {
		if (e instanceof SQLException) {
			if ("08000".equals(((SQLException) e).getSQLState())) { //$NON-NLS-1$
				RCorePlugin.log(new Status(IStatus.WARNING, RCore.PLUGIN_ID, -1, "Thread was interrupted when searching index in DB.", e));
				throw new CoreException(Status.CANCEL_STATUS);
			}
		}
		RCorePlugin.log(new Status(IStatus.ERROR, RCore.PLUGIN_ID, -1, "An error occurred when searching index in DB.", e));
	}
	
	
	public IRFrame getProjectFrame(final IRProject rProject) throws CoreException {
		final Proj proj= this.projects.get(rProject.getProject().getName());
		if (proj != null) {
			this.lock.readLock().lock();
			try {
				return getFrame(proj, rProject, null, null);
			}
			catch (final SQLException e) {
				onDbReadError(e);
			}
			finally {
				this.lock.readLock().unlock();
			}
		}
		return null;
	}
	
	public List<ISourceUnit> findReferencingSourceUnits(final IRProject rProject, final RElementName name,
			final IProgressMonitor monitor) throws CoreException {
		if (name.getNextSegment() != null || name.getType() != RElementName.MAIN_DEFAULT || name.getSegmentName() == null) {
			throw new UnsupportedOperationException("Only common top level names are supported.");
		}
		final SubMonitor progress= SubMonitor.convert(monitor);
		final ISourceUnitManager suManager= LTK.getSourceUnitManager();
		final List<ISourceUnit> matches= new ArrayList<>();
		final Proj proj= this.projects.get(rProject.getProject().getName());
		this.lock.readLock().lock();
		Connection connection= null;
		try {
			if (proj == null || proj.removed
					|| this.dbInitialized != 1) {
				return null;
			}
			connection= this.dbConnectionPool.getConnection();
			final PreparedStatement statement= connection.prepareStatement(RIndex.MainNames.OP_findSourceUnits_ofProjectAndName);
			statement.setInt(1, proj.id);
			statement.setString(2, name.getSegmentName());
			final ResultSet result= statement.executeQuery();
			while (result.next()) {
				final String unitId= result.getString(1);
				final String modelTypeId= this.modelId2Type.get(result.getInt(2));
				assert (unitId != null && modelTypeId != null);
				final ISourceUnit su= loadSourceUnit(suManager, unitId, modelTypeId, progress);
				if (su != null) {
					matches.add(su);
				}
			}
			return matches;
		}
		catch (final SQLException e) {
			onDbReadError(e);
			closeSourceUnits(matches, progress);
			return null;
		}
		finally {
			this.lock.readLock().unlock();
			if (connection != null) {
				try {
					connection.close();
				} catch (final SQLException ignore) {}
			}
		}
	}
	
	private ISourceUnit loadSourceUnit(final ISourceUnitManager manager,
			final String sourceUnitId, final String modelTypeId, final SubMonitor progress) {
		try {
			return manager.getSourceUnit(modelTypeId, LTK.PERSISTENCE_CONTEXT, sourceUnitId, true, progress);
		}
		catch (final Exception e) {
			RCorePlugin.log(new Status(IStatus.ERROR, RCore.PLUGIN_ID, 0,
					NLS.bind("An error occurred when restoring source unit ''{0}'' for model ''{0}''.", sourceUnitId, modelTypeId),
					e ));
			return null;
		}
	}
	
	private void closeSourceUnits(final List<ISourceUnit> sourceUnits,
			final SubMonitor progress) {
		for (final ISourceUnit su : sourceUnits) {
			su.disconnect(progress);
		}
	}
	
}
