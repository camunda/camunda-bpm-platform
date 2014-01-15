/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.camunda.bpm.engine.impl.db;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.ibatis.session.SqlSession;
import org.camunda.bpm.engine.OptimisticLockingException;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.WrongDbException;
import org.camunda.bpm.engine.impl.DeploymentQueryImpl;
import org.camunda.bpm.engine.impl.ExecutionQueryImpl;
import org.camunda.bpm.engine.impl.GroupQueryImpl;
import org.camunda.bpm.engine.impl.HistoricActivityInstanceQueryImpl;
import org.camunda.bpm.engine.impl.HistoricDetailQueryImpl;
import org.camunda.bpm.engine.impl.HistoricProcessInstanceQueryImpl;
import org.camunda.bpm.engine.impl.HistoricTaskInstanceQueryImpl;
import org.camunda.bpm.engine.impl.HistoricVariableInstanceQueryImpl;
import org.camunda.bpm.engine.impl.JobQueryImpl;
import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.ProcessDefinitionQueryImpl;
import org.camunda.bpm.engine.impl.ProcessInstanceQueryImpl;
import org.camunda.bpm.engine.impl.TaskQueryImpl;
import org.camunda.bpm.engine.impl.UserQueryImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.upgrade.DbUpgradeStep;
import org.camunda.bpm.engine.impl.identity.db.DbGroupQueryImpl;
import org.camunda.bpm.engine.impl.identity.db.DbUserQueryImpl;
import org.camunda.bpm.engine.impl.interceptor.Session;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyEntity;
import org.camunda.bpm.engine.impl.persistence.entity.VariableInstanceEntity;
import org.camunda.bpm.engine.impl.util.ClassNameUtil;
import org.camunda.bpm.engine.impl.util.IoUtil;
import org.camunda.bpm.engine.impl.util.ReflectUtil;
import org.camunda.bpm.engine.impl.variable.DeserializedObject;


/** responsibilities:
 *   - delayed flushing of inserts updates and deletes
 *   - optional dirty checking
 *   - db specific statement name mapping
 *
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class DbSqlSession implements Session {

  private static Logger log = Logger.getLogger(DbSqlSession.class.getName());

  protected SqlSession sqlSession;
  protected DbSqlSessionFactory dbSqlSessionFactory;
  protected List<PersistentObject> insertedObjects = new ArrayList<PersistentObject>();
  protected List<PersistentObject> updatedObjects = new ArrayList<PersistentObject>();
  protected Map<Class<?>, Map<String, CachedObject>> cachedObjects = new HashMap<Class<?>, Map<String,CachedObject>>();
  protected List<BulkUpdateOperation> bulkUpdates = new ArrayList<BulkUpdateOperation>();
  protected List<DeleteOperation> deleteOperations = new ArrayList<DeleteOperation>();
  protected List<DeserializedObject> deserializedObjects = new ArrayList<DeserializedObject>();
  protected String connectionMetadataDefaultCatalog = null;
  protected String connectionMetadataDefaultSchema = null;

  public DbSqlSession(DbSqlSessionFactory dbSqlSessionFactory) {
    this.dbSqlSessionFactory = dbSqlSessionFactory;
    this.sqlSession = dbSqlSessionFactory
      .getSqlSessionFactory()
      .openSession();
  }

  public DbSqlSession(DbSqlSessionFactory dbSqlSessionFactory, Connection connection, String catalog, String schema) {
    this.dbSqlSessionFactory = dbSqlSessionFactory;
    this.sqlSession = dbSqlSessionFactory
      .getSqlSessionFactory()
      .openSession(connection);
    this.connectionMetadataDefaultCatalog = catalog;
    this.connectionMetadataDefaultSchema = schema;
  }

  // insert ///////////////////////////////////////////////////////////////////

  public void insert(PersistentObject persistentObject) {
    if (persistentObject.getId()==null) {
      String id = dbSqlSessionFactory.getIdGenerator().getNextId();
      persistentObject.setId(id);
    }
    insertedObjects.add(persistentObject);
    cachePut(persistentObject, false);
  }

  // update ///////////////////////////////////////////////////////////////////

  public void update(PersistentObject persistentObject) {
    updatedObjects.add(persistentObject);
    cachePut(persistentObject, false);
  }

  // delete ///////////////////////////////////////////////////////////////////

//  public void delete(Class<?> persistentObjectClass, String persistentObjectId) {
//    for (DeleteOperation deleteOperation: deleteOperations) {
//      if (deleteOperation instanceof DeleteById) {
//        DeleteById deleteById = (DeleteById) deleteOperation;
//        if ( persistentObjectClass.equals(deleteById.persistenceObjectClass)
//             && persistentObjectId.equals(deleteById.persistentObjectId)
//           ) {
//          // skip this delete
//          return;
//        }
//      }
//    }
//    deleteOperations.add(new DeleteById(persistentObjectClass, persistentObjectId));
//  }

  public interface DeleteOperation {
    void execute();
  }

//  public class DeleteById implements DeleteOperation {
//    Class<?> persistenceObjectClass;
//    String persistentObjectId;
//    public DeleteById(Class< ? > clazz, String id) {
//      this.persistenceObjectClass = clazz;
//      this.persistentObjectId = id;
//    }
//    public void execute() {
//      String deleteStatement = dbSqlSessionFactory.getDeleteStatement(persistenceObjectClass);
//      deleteStatement = dbSqlSessionFactory.mapStatement(deleteStatement);
//      if (deleteStatement==null) {
//        throw new ProcessEngineException("no delete statement for "+persistenceObjectClass+" in the ibatis mapping files");
//      }
//      log.fine("deleting: "+ClassNameUtil.getClassNameWithoutPackage(persistenceObjectClass)+"["+persistentObjectId+"]");
//      sqlSession.delete(deleteStatement, persistentObjectId);
//    }
//    public String toString() {
//      return "delete "+ClassNameUtil.getClassNameWithoutPackage(persistenceObjectClass)+"["+persistentObjectId+"]";
//    }
//  }

  public void delete(String statement, Object parameter) {
    deleteOperations.add(new BulkDeleteOperation(statement, parameter));
  }

  public void update(String statement, Object parameter) {
    BulkUpdateOperation updateOperation = new BulkUpdateOperation(statement, parameter);
    bulkUpdates.add(updateOperation);
  }

  /**
   * Use this {@link DeleteOperation} to execute a dedicated delete statement.
   * It is important to note there won't be any optimistic locking checks done
   * for these kind of delete operations!
   *
   * For example, a usage of this operation would be to delete all variables for
   * a certain execution, when that certain execution is removed. The optimistic locking
   * happens on the execution, but the variables can be removed by a simple
   * 'delete from var_table where execution_id is xxx'. It could very well be there
   * are no variables, which would also work with this query, but not with the
   * regular {@link DeletePersistentObjectOperation} operation.
   */
  public class BulkDeleteOperation implements DeleteOperation {
    String statement;
    Object parameter;
    public BulkDeleteOperation(String statement, Object parameter) {
      this.statement = dbSqlSessionFactory.mapStatement(statement);
      this.parameter = parameter;
    }
    public void execute() {
      sqlSession.delete(statement, parameter);
    }
    public String toString() {
      return "bulk delete: "+statement;
    }
  }

  public class BulkUpdateOperation {
    String statement;
    Object parameter;

    public BulkUpdateOperation(String statement, Object parameter) {
      this.statement = statement;
      this.parameter = parameter;
    }

    public String getStatement() {
      return statement;
    }

    public Object getParameter() {
      return parameter;
    }
  }


  public void delete(PersistentObject persistentObject) {
    for (DeleteOperation deleteOperation: deleteOperations) {
      if (deleteOperation instanceof DeletePersistentObjectOperation) {

        DeletePersistentObjectOperation deletePersistentObjectOperation = (DeletePersistentObjectOperation) deleteOperation;
        if (persistentObject.getId().equals(deletePersistentObjectOperation.getPersistentObject().getId())
                && persistentObject.getClass().equals(deletePersistentObjectOperation.getPersistentObject().getClass())) {
          return; // Skip this delete. It was already added.
        }
      }
    }

    deleteOperations.add(new DeletePersistentObjectOperation(persistentObject));
  }

  /**
   * A {@link DeleteOperation} used when the persistent object has been fetched already.
   */
  public class DeletePersistentObjectOperation implements DeleteOperation {

    protected PersistentObject persistentObject;

    public DeletePersistentObjectOperation(PersistentObject persistentObject) {
      this.persistentObject = persistentObject;
    }

    public void execute() {
      String deleteStatement = dbSqlSessionFactory.getDeleteStatement(persistentObject.getClass());
      deleteStatement = dbSqlSessionFactory.mapStatement(deleteStatement);
      if (deleteStatement == null) {
        throw new ProcessEngineException("no delete statement for " + persistentObject.getClass() + " in the ibatis mapping files");
      }
      log.fine("deleting: " + ClassNameUtil.getClassNameWithoutPackage(persistentObject.getClass()) + "[" + persistentObject.getId() + "]");


      // It only makes sense to check for optimistic locking exceptions for objects that actually have a revision
      if (persistentObject instanceof HasRevision) {
        int nrOfRowsDeleted = sqlSession.delete(deleteStatement, persistentObject);
        if (nrOfRowsDeleted == 0) {
          throw new OptimisticLockingException(DbSqlSession.this.toString(persistentObject) + " was updated by another transaction concurrently");
        }
      } else {
        sqlSession.delete(deleteStatement, persistentObject);
      }
    }

    public PersistentObject getPersistentObject() {
      return persistentObject;
    }

    public void setPersistentObject(PersistentObject persistentObject) {
      this.persistentObject = persistentObject;
    }

    public String toString() {
      return "Delete operation for " + persistentObject.getClass() + " [" + persistentObject.getId() + "]";
    }

  }

  // select ///////////////////////////////////////////////////////////////////

  @SuppressWarnings("unchecked")
  public List selectList(String statement) {
    return selectList(statement, null, 0, Integer.MAX_VALUE);
  }

  @SuppressWarnings("unchecked")
  public List selectList(String statement, Object parameter) {
    return selectList(statement, parameter, 0, Integer.MAX_VALUE);
  }

  @SuppressWarnings("unchecked")
  public List selectList(String statement, Object parameter, Page page) {
    if(page!=null) {
      return selectList(statement, parameter, page.getFirstResult(), page.getMaxResults());
    }else {
      return selectList(statement, parameter, 0, Integer.MAX_VALUE);
    }
  }

  @SuppressWarnings("unchecked")
  public List selectList(String statement, ListQueryParameterObject parameter, Page page) {
    return selectList(statement, parameter);
  }

  @SuppressWarnings("unchecked")
  public List selectList(String statement, Object parameter, int firstResult, int maxResults) {
    return selectList(statement, new ListQueryParameterObject(parameter, firstResult, maxResults));
  }

  @SuppressWarnings("unchecked")
  public List selectList(String statement, ListQueryParameterObject parameter) {
    return selectListWithRawParameter(statement, parameter, parameter.getFirstResult(), parameter.getMaxResults());
  }

  @SuppressWarnings("unchecked")
  public List selectListWithRawParameter(String statement, Object parameter, int firstResult, int maxResults) {
    statement = dbSqlSessionFactory.mapStatement(statement);
    if(firstResult == -1 ||  maxResults==-1) {
      return Collections.EMPTY_LIST;
    }
    List loadedObjects = sqlSession.selectList(statement, parameter);
    return filterLoadedObjects(loadedObjects);
  }

  public Object selectOne(String statement, Object parameter) {
    statement = dbSqlSessionFactory.mapStatement(statement);
    Object result = sqlSession.selectOne(statement, parameter);
    if (result instanceof PersistentObject) {
      PersistentObject loadedObject = (PersistentObject) result;
      result = cacheFilter(loadedObject);
    }
    return result;
  }

  public boolean selectBoolean(String statement, Object parameter) {
    statement = dbSqlSessionFactory.mapStatement(statement);
    List<String> result = sqlSession.selectList(statement, parameter);
    if(result != null) {
      return result.contains(1);
    }
    return false;

  }

  @SuppressWarnings("unchecked")
  public <T extends PersistentObject> T selectById(Class<T> entityClass, String id) {
    T persistentObject = cacheGet(entityClass, id);
    if (persistentObject!=null) {
      return persistentObject;
    }
    String selectStatement = dbSqlSessionFactory.getSelectStatement(entityClass);
    selectStatement = dbSqlSessionFactory.mapStatement(selectStatement);
    persistentObject = (T) sqlSession.selectOne(selectStatement, id);
    if (persistentObject==null) {
      return null;
    }
    cachePut(persistentObject, true);
    return persistentObject;
  }

  // internal session cache ///////////////////////////////////////////////////

  @SuppressWarnings("unchecked")
  protected List filterLoadedObjects(List<Object> loadedObjects) {
    if (loadedObjects.isEmpty()) {
      return loadedObjects;
    }
    if (! (PersistentObject.class.isAssignableFrom(loadedObjects.get(0).getClass()))) {
      return loadedObjects;
    }
    List<PersistentObject> filteredObjects = new ArrayList<PersistentObject>(loadedObjects.size());
    for (Object loadedObject: loadedObjects) {
      PersistentObject cachedPersistentObject = cacheFilter((PersistentObject) loadedObject);
      filteredObjects.add(cachedPersistentObject);
    }
    return filteredObjects;
  }

  protected CachedObject cachePut(PersistentObject persistentObject, boolean storeState) {
    Map<String, CachedObject> classCache = cachedObjects.get(persistentObject.getClass());
    if (classCache==null) {
      classCache = new HashMap<String, CachedObject>();
      cachedObjects.put(persistentObject.getClass(), classCache);
    }
    CachedObject cachedObject = new CachedObject(persistentObject, storeState);
    classCache.put(persistentObject.getId(), cachedObject);
    return cachedObject;
  }

  /** returns the object in the cache.  if this object was loaded before,
   * then the original object is returned.  if this is the first time
   * this object is loaded, then the loadedObject is added to the cache. */
  protected PersistentObject cacheFilter(PersistentObject persistentObject) {
    PersistentObject cachedPersistentObject = cacheGet(persistentObject.getClass(), persistentObject.getId());
    if (cachedPersistentObject!=null) {
      return cachedPersistentObject;
    }
    cachePut(persistentObject, true);
    return persistentObject;
  }

  @SuppressWarnings("unchecked")
  protected <T> T cacheGet(Class<T> entityClass, String id) {
    CachedObject cachedObject = null;
    Map<String, CachedObject> classCache = cachedObjects.get(entityClass);
    if (classCache!=null) {
      cachedObject = classCache.get(id);
    }
    if (cachedObject!=null) {
      return (T) cachedObject.getPersistentObject();
    }
    return null;
  }

  protected void cacheRemove(Class<?> persistentObjectClass, String persistentObjectId) {
    Map<String, CachedObject> classCache = cachedObjects.get(persistentObjectClass);
    if (classCache==null) {
      return;
    }
    classCache.remove(persistentObjectId);
  }

  @SuppressWarnings("unchecked")
  public <T> List<T> findInCache(Class<T> entityClass) {
    Map<String, CachedObject> classCache = cachedObjects.get(entityClass);
    if (classCache!=null) {
      ArrayList<T> entities = new ArrayList<T>(classCache.size());
      for (CachedObject cachedObject: classCache.values()) {
        entities.add((T) cachedObject.getPersistentObject());
      }
      return entities;
    }
    return Collections.emptyList();
  }

  public <T> T findInCache(Class<T> entityClass, String id) {
    return cacheGet(entityClass, id);
  }


  public static class CachedObject {
    protected PersistentObject persistentObject;
    protected Object persistentObjectState;

    public CachedObject(PersistentObject persistentObject, boolean storeState) {
      this.persistentObject = persistentObject;
      if (storeState) {
        this.persistentObjectState = persistentObject.getPersistentState();
      }
    }

    public PersistentObject getPersistentObject() {
      return persistentObject;
    }

    public Object getPersistentObjectState() {
      return persistentObjectState;
    }
  }

  // deserialized objects /////////////////////////////////////////////////////

  public void addDeserializedObject(Object deserializedObject, byte[] serializedBytes, VariableInstanceEntity variableInstanceEntity) {
    deserializedObjects.add(new DeserializedObject(deserializedObject, serializedBytes, variableInstanceEntity));
  }

  // flush ////////////////////////////////////////////////////////////////////

  public void flush() {
    removeUnnecessaryOperations();
    flushDeserializedObjects();
    List<PersistentObject> updatedObjects = getUpdatedObjects();

    if (log.isLoggable(Level.FINE)) {
      log.fine("flush summary:");
      for (PersistentObject insertedObject: insertedObjects) {
        log.fine("  insert "+toString(insertedObject));
      }
      for (PersistentObject updatedObject: updatedObjects) {
        log.fine("  update "+toString(updatedObject));
      }
      for (BulkUpdateOperation bulkUpdateOperation: bulkUpdates) {
        log.fine(" bulk update "+ bulkUpdateOperation);
      }
      for (Object deleteOperation: deleteOperations) {
        log.fine("  "+deleteOperation);
      }
      log.fine("now executing flush...");
    }

    flushInserts();
    flushUpdates(updatedObjects);
    flushBulkUpdates();
    flushDeletes();
  }

//  protected void removeUnnecessaryOperations() {
//    List<DeleteOperation> deletedObjectsCopy = new ArrayList<DeleteOperation>(deleteOperations);
//    // for all deleted objects
//    for (DeleteOperation deleteOperation: deletedObjectsCopy) {
//      if (deleteOperation instanceof DeleteById) {
//        DeleteById deleteById = (DeleteById) deleteOperation;
//        PersistentObject insertedObject = findInsertedObject(deleteById.persistenceObjectClass, deleteById.persistentObjectId);
//        // if the deleted object is inserted,
//        if (insertedObject!=null) {
//          // remove the insert and the delete
//          insertedObjects.remove(insertedObject);
//          deleteOperations.remove(deleteOperation);
//        }
//        // in any case, remove the deleted object from the cache
//        cacheRemove(deleteById.persistenceObjectClass, deleteById.persistentObjectId);
//      }
//    }
//    for (PersistentObject insertedObject: insertedObjects) {
//      cacheRemove(insertedObject.getClass(), insertedObject.getId());
//    }
//  }

  protected void removeUnnecessaryOperations() {
    List<DeleteOperation> deletedObjectsCopy = new ArrayList<DeleteOperation>(deleteOperations);

    // Check all delete operations to see if there are any inserts that cancel the delete
    for (DeleteOperation deleteOperation: deletedObjectsCopy) {
      if (deleteOperation instanceof DeletePersistentObjectOperation) {

        DeletePersistentObjectOperation deletePersistentObjectOperation = (DeletePersistentObjectOperation) deleteOperation;
        PersistentObject insertedObject = findInsertedObject(deletePersistentObjectOperation.getPersistentObject().getClass(),
                deletePersistentObjectOperation.getPersistentObject().getId());

        // if the deleted object is inserted,
        if (insertedObject != null) {
          // remove the insert and the delete, they cancel each other
          insertedObjects.remove(insertedObject);
          deleteOperations.remove(deleteOperation);
        }

        // in any case, remove the deleted object from the cache
        cacheRemove(deletePersistentObjectOperation.getPersistentObject().getClass(),
                deletePersistentObjectOperation.getPersistentObject().getId());

      }
    }

    for (PersistentObject insertedObject: insertedObjects) {
      cacheRemove(insertedObject.getClass(), insertedObject.getId());
    }

  }

  protected PersistentObject findInsertedObject(Class< ? > persistenceObjectClass, String persistentObjectId) {
    for (PersistentObject insertedObject: insertedObjects) {
      if ( insertedObject.getClass().equals(persistenceObjectClass)
           && insertedObject.getId().equals(persistentObjectId)
         ) {
        return insertedObject;
      }
    }
    return null;
  }

  protected void flushDeserializedObjects() {
    for (DeserializedObject deserializedObject: deserializedObjects) {
      deserializedObject.flush();
    }
  }

//  public List<PersistentObject> getUpdatedObjects() {
//    List<PersistentObject> updatedObjects = new ArrayList<PersistentObject>();
//    for (Class<?> clazz: cachedObjects.keySet()) {
//      Map<String, CachedObject> classCache = cachedObjects.get(clazz);
//      for (CachedObject cachedObject: classCache.values()) {
//        PersistentObject persistentObject = (PersistentObject) cachedObject.getPersistentObject();
//        if (!deleteOperations.contains(persistentObject)) {
//          Object originalState = cachedObject.getPersistentObjectState();
//          if (!persistentObject.getPersistentState().equals(originalState)) {
//            updatedObjects.add(persistentObject);
//          } else {
//            log.finest("loaded object '"+persistentObject+"' was not updated");
//          }
//        }
//      }
//    }
//    return updatedObjects;
//  }

  public List<PersistentObject> getUpdatedObjects() {
    List<PersistentObject> updatedObjects = new ArrayList<PersistentObject>();
    for (Class<?> clazz: cachedObjects.keySet()) {

      Map<String, CachedObject> classCache = cachedObjects.get(clazz);
      for (CachedObject cachedObject: classCache.values()) {

        PersistentObject persistentObject = (PersistentObject) cachedObject.getPersistentObject();
        if (!isPersistentObjectDeleted(persistentObject)) {
          Object originalState = cachedObject.getPersistentObjectState();
          if (!persistentObject.getPersistentState().equals(originalState)) {
            updatedObjects.add(persistentObject);

          } else {
            log.finest("loaded object '"+persistentObject+"' was not updated");
          }
        }

      }

    }

    return updatedObjects;
  }

  public boolean isUpdated(PersistentObject persistentObject) {
    return getUpdatedObjects().contains(persistentObject);
  }

  protected boolean isPersistentObjectDeleted(PersistentObject persistentObject) {
    for (DeleteOperation deleteOperation : deleteOperations) {
      if (deleteOperation instanceof DeletePersistentObjectOperation) {
        if ( ((DeletePersistentObjectOperation) deleteOperation).getPersistentObject().equals(persistentObject)) {
          return true;
        }
      }
    }
    return false;
  }

//  public <T extends PersistentObject> List<T> pruneDeletedEntities(List<T> listToPrune) {
//    ArrayList<T> prunedList = new ArrayList<T>(listToPrune);
//    for (T potentiallyDeleted : listToPrune) {
//      for (DeleteOperation deleteOperation: deleteOperations) {
//        if (deleteOperation instanceof DeleteById) {
//          DeleteById deleteById = (DeleteById) deleteOperation;
//          if ( potentiallyDeleted.getClass().equals(deleteById.persistenceObjectClass)
//               && potentiallyDeleted.getId().equals(deleteById.persistentObjectId)
//             ) {
//            prunedList.remove(potentiallyDeleted);
//          }
//        }
//      }
//    }
//    return prunedList;
//  }

  public <T extends PersistentObject> List<T> pruneDeletedEntities(List<T> listToPrune) {
    ArrayList<T> prunedList = new ArrayList<T>(listToPrune);
    for (T potentiallyDeleted : listToPrune) {
      for (DeleteOperation deleteOperation: deleteOperations) {
        if (deleteOperation instanceof DeletePersistentObjectOperation) {

          DeletePersistentObjectOperation deletePersistentObjectOperation = (DeletePersistentObjectOperation) deleteOperation;
          if (potentiallyDeleted.getClass().equals(deletePersistentObjectOperation.getPersistentObject().getClass())
                  && potentiallyDeleted.getId().equals(deletePersistentObjectOperation.getPersistentObject().getId())) {
            prunedList.remove(potentiallyDeleted);
          }

        }
      }
    }
    return prunedList;
  }

  protected void flushInserts() {
    for (PersistentObject insertedObject: insertedObjects) {
      String insertStatement = dbSqlSessionFactory.getInsertStatement(insertedObject);
      insertStatement = dbSqlSessionFactory.mapStatement(insertStatement);

      if (insertStatement==null) {
        throw new ProcessEngineException("no insert statement for "+insertedObject.getClass()+" in the ibatis mapping files");
      }

      log.fine("inserting: "+toString(insertedObject));
      sqlSession.insert(insertStatement, insertedObject);

      // See http://jira.codehaus.org/browse/ACT-1290
      if (insertedObject instanceof HasRevision) {
        ((HasRevision) insertedObject).setRevision(((HasRevision) insertedObject).getRevisionNext());
      }
    }
    insertedObjects.clear();
  }

  protected void flushUpdates(List<PersistentObject> updatedObjects) {
    for (PersistentObject updatedObject: updatedObjects) {
      String updateStatement = dbSqlSessionFactory.getUpdateStatement(updatedObject);
      updateStatement = dbSqlSessionFactory.mapStatement(updateStatement);
      if (updateStatement==null) {
        throw new ProcessEngineException("no update statement for "+updatedObject.getClass()+" in the ibatis mapping files");
      }
      if(log.isLoggable(Level.FINE)) {
        log.fine("updating: "+toString(updatedObject)+"]");
      }
      int updatedRecords = sqlSession.update(updateStatement, updatedObject);

      if (updatedObject instanceof HasRevision) {

        if (updatedRecords!=1) {
          throw new OptimisticLockingException(toString(updatedObject)+" was updated by another transaction concurrently");

        } else {
          // See http://jira.codehaus.org/browse/ACT-1290
          ((HasRevision) updatedObject).setRevision(((HasRevision) updatedObject).getRevisionNext());

        }

      }

    }
    updatedObjects.clear();
  }

  protected void flushBulkUpdates() {
    for (BulkUpdateOperation bulkUpdateOperation : bulkUpdates) {
      String updateStatement = bulkUpdateOperation.getStatement();
      if (updateStatement==null) {
        throw new ProcessEngineException("no update statement " + updateStatement + " in the ibatis mapping files");
      }
      if(log.isLoggable(Level.FINE)) {
        log.fine("bulk updating: "+ bulkUpdateOperation);
      }
      sqlSession.update(updateStatement, bulkUpdateOperation.parameter);
    }

  }

  protected void flushDeletes() {
    for (DeleteOperation delete: deleteOperations) {
      log.fine("executing: "+delete);
      delete.execute();
    }
    deleteOperations.clear();
  }

  public void close() {
    sqlSession.close();
  }

  public void commit() {
    sqlSession.commit();
  }

  public void rollback() {
    sqlSession.rollback();
  }

  protected String toString(PersistentObject persistentObject) {
    if (persistentObject==null) {
      return "null";
    }
    return ClassNameUtil.getClassNameWithoutPackage(persistentObject)+"["+persistentObject.getId()+"]";
  }

  // schema operations ////////////////////////////////////////////////////////


  public void dbSchemaCheckVersion() {
    try {
      String dbVersion = getDbVersion();
      if (!ProcessEngine.VERSION.equals(dbVersion)) {
        throw new WrongDbException(ProcessEngine.VERSION, dbVersion);
      }

      String errorMessage = null;
      if (!isEngineTablePresent()) {
        errorMessage = addMissingComponent(errorMessage, "engine");
      }
      if (dbSqlSessionFactory.isDbHistoryUsed() && !isHistoryTablePresent()) {
        errorMessage = addMissingComponent(errorMessage, "history");
      }
      if (dbSqlSessionFactory.isDbIdentityUsed() && !isIdentityTablePresent()) {
        errorMessage = addMissingComponent(errorMessage, "identity");
      }

      if (errorMessage!=null) {
        throw new ProcessEngineException("Activiti database problem: "+errorMessage);
      }

    } catch (Exception e) {
      if (isMissingTablesException(e)) {
        throw new ProcessEngineException("no activiti tables in db.  set <property name=\"databaseSchemaUpdate\" to value=\"true\" or value=\"create-drop\" (use create-drop for testing only!) in bean processEngineConfiguration in camunda.cfg.xml for automatic schema creation", e);
      } else {
        if (e instanceof RuntimeException) {
          throw (RuntimeException) e;
        } else {
          throw new ProcessEngineException("couldn't get db schema version", e);
        }
      }
    }

    log.fine("activiti db schema check successful");
  }

  public void dbCreateHistoryLevel() {
    ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();
    int configuredHistoryLevel = processEngineConfiguration.getHistoryLevel();
    PropertyEntity property = new PropertyEntity("historyLevel", Integer.toString(configuredHistoryLevel));
    insert(property);
    log.info("Creating historyLevel property in database with value: " + processEngineConfiguration.getHistory());
  }

  public void checkHistoryLevel() {
    Integer configuredHistoryLevel = Context.getProcessEngineConfiguration().getHistoryLevel();
    PropertyEntity historyLevelProperty = selectById(PropertyEntity.class, "historyLevel");
    if (historyLevelProperty == null) {
      log.info("No historyLevel property found in database.");
      dbCreateHistoryLevel();
    } else {
      Integer databaseHistoryLevel = new Integer(historyLevelProperty.getValue());
      if (!configuredHistoryLevel.equals(databaseHistoryLevel)) {
        throw new ProcessEngineException("historyLevel mismatch: configuration says " + configuredHistoryLevel + " and database says " + databaseHistoryLevel);
      }
    }
  }

  protected String addMissingComponent(String missingComponents, String component) {
    if (missingComponents==null) {
      return "Tables missing for component(s) "+component;
    }
    return missingComponents+", "+component;
  }

  protected String getDbVersion() {
    String selectSchemaVersionStatement = dbSqlSessionFactory.mapStatement("selectDbSchemaVersion");
    return (String) sqlSession.selectOne(selectSchemaVersionStatement);
  }

  public void dbSchemaCreate() {
    ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();

    int configuredHistoryLevel = processEngineConfiguration.getHistoryLevel();
    if ( (!processEngineConfiguration.isDbHistoryUsed())
         && (configuredHistoryLevel>ProcessEngineConfigurationImpl.HISTORYLEVEL_NONE)
       ) {
      throw new ProcessEngineException("historyLevel config is higher then 'none' and dbHistoryUsed is set to false");
    }

    if (isEngineTablePresent()) {
      String dbVersion = getDbVersion();
      if (!ProcessEngine.VERSION.equals(dbVersion)) {
        throw new WrongDbException(ProcessEngine.VERSION, dbVersion);
      }
    } else {
      dbSchemaCreateEngine();
    }

    if (processEngineConfiguration.isDbHistoryUsed()) {
      dbSchemaCreateHistory();
    }

    if (processEngineConfiguration.isDbIdentityUsed()) {
      dbSchemaCreateIdentity();
    }
  }

  protected void dbSchemaCreateIdentity() {
    executeMandatorySchemaResource("create", "identity");
  }

  protected void dbSchemaCreateHistory() {
    executeMandatorySchemaResource("create", "history");
  }

  protected void dbSchemaCreateEngine() {
    executeMandatorySchemaResource("create", "engine");
  }

  public void dbSchemaDrop() {
    executeMandatorySchemaResource("drop", "engine");
    if (dbSqlSessionFactory.isDbHistoryUsed()) {
      executeMandatorySchemaResource("drop", "history");
    }
    if (dbSqlSessionFactory.isDbIdentityUsed()) {
      executeMandatorySchemaResource("drop", "identity");
    }
  }

  public void dbSchemaPrune() {
    if (isHistoryTablePresent() && !dbSqlSessionFactory.isDbHistoryUsed()) {
      executeMandatorySchemaResource("drop", "history");
    }
    if (isIdentityTablePresent() && dbSqlSessionFactory.isDbIdentityUsed()) {
      executeMandatorySchemaResource("drop", "identity");
    }
  }

  public void executeMandatorySchemaResource(String operation, String component) {
    executeSchemaResource(operation, component, getResourceForDbOperation(operation, operation, component), false);
  }

  public static String[] JDBC_METADATA_TABLE_TYPES = {"TABLE"};

  public String dbSchemaUpdate() {
    String feedback = null;
    String dbVersion = null;
    boolean isUpgradeNeeded = false;

    if (isEngineTablePresent()) {
      // the next piece assumes both DB version and library versions are formatted 5.x
      PropertyEntity dbVersionProperty = selectById(PropertyEntity.class, "schema.version");
      dbVersion = dbVersionProperty.getValue();
      isUpgradeNeeded = !ProcessEngine.VERSION.equals(dbVersion);

      if (isUpgradeNeeded) {
        dbVersionProperty.setValue(ProcessEngine.VERSION);

        PropertyEntity dbHistoryProperty;
        if ("5.0".equals(dbVersion)) {
          dbHistoryProperty = new PropertyEntity("schema.history", "create(5.0)");
          insert(dbHistoryProperty);
        } else {
          dbHistoryProperty = selectById(PropertyEntity.class, "schema.history");
        }

        String dbHistoryValue = dbHistoryProperty.getValue()+" upgrade("+dbVersion+"->"+ProcessEngine.VERSION+")";
        dbHistoryProperty.setValue(dbHistoryValue);

        dbSchemaUpgrade("engine", dbVersion);

        feedback = "upgraded Activiti from "+dbVersion+" to "+ProcessEngine.VERSION;
      }
    } else {
      dbSchemaCreateEngine();
    }

    if (isHistoryTablePresent()) {
      if (isUpgradeNeeded) {
        dbSchemaUpgrade("history", dbVersion);
      }
    } else if (dbSqlSessionFactory.isDbHistoryUsed()) {
      dbSchemaCreateHistory();
    }

    if (isIdentityTablePresent()) {
      if (isUpgradeNeeded) {
        dbSchemaUpgrade("identity", dbVersion);
      }
    } else if (dbSqlSessionFactory.isDbIdentityUsed()) {
      dbSchemaCreateIdentity();
    }

    return feedback;
  }

  public boolean isEngineTablePresent(){
    return isTablePresent("ACT_RU_EXECUTION");
  }
  public boolean isHistoryTablePresent(){
    return isTablePresent("ACT_HI_PROCINST");
  }
  public boolean isIdentityTablePresent(){
    return isTablePresent("ACT_ID_USER");
  }

  public boolean isTablePresent(String tableName) {
    tableName = prependDatabaseTablePrefix(tableName);
    Connection connection = null;
    try {
      connection = sqlSession.getConnection();
      DatabaseMetaData databaseMetaData = connection.getMetaData();
      ResultSet tables = null;

      String schema = this.connectionMetadataDefaultSchema;
      if (dbSqlSessionFactory.getDatabaseSchema()!=null) {
        schema = dbSqlSessionFactory.getDatabaseSchema();
      }

      String databaseType = dbSqlSessionFactory.getDatabaseType();

      if ("postgres".equals(databaseType)) {
        tableName = tableName.toLowerCase();
      }

      try {
        tables = databaseMetaData.getTables(this.connectionMetadataDefaultCatalog, schema, tableName, JDBC_METADATA_TABLE_TYPES);
        return tables.next();
      } finally {
        tables.close();
      }

    } catch (Exception e) {
      throw new ProcessEngineException("couldn't check if tables are already present using metadata: "+e.getMessage(), e);
    }
  }

  protected String prependDatabaseTablePrefix(String tableName) {
    return dbSqlSessionFactory.getDatabaseTablePrefix() + tableName;
  }

  protected void dbSchemaUpgrade(String component, String dbVersion) {
    log.info("upgrading activiti "+component+" schema from "+dbVersion+" to "+ProcessEngine.VERSION);

    if (dbVersion.endsWith("-SNAPSHOT")) {
      dbVersion = dbVersion.substring(0, dbVersion.length()-"-SNAPSHOT".length());
    }
    int minorDbVersionNumber = Integer.parseInt(dbVersion.substring(2));

    String libraryVersion = ProcessEngine.VERSION;
    if (ProcessEngine.VERSION.endsWith("-SNAPSHOT")) {
      libraryVersion = ProcessEngine.VERSION.substring(0, ProcessEngine.VERSION.length()-"-SNAPSHOT".length());
    }
    int minorLibraryVersionNumber = Integer.parseInt(libraryVersion.substring(2));

    while (minorDbVersionNumber<minorLibraryVersionNumber) {
      executeSchemaResource("upgrade", component, getResourceForDbOperation("upgrade", "upgradestep.5"+minorDbVersionNumber+".to.5"+(minorDbVersionNumber+1), component), true);
      minorDbVersionNumber++;
    }
  }

  public String getResourceForDbOperation(String directory, String operation, String component) {
    String databaseType = dbSqlSessionFactory.getDatabaseType();
    return "org/camunda/bpm/engine/db/" + directory + "/activiti." + databaseType + "." + operation + "."+component+".sql";
  }

  public void executeSchemaResource(String operation, String component, String resourceName, boolean isOptional) {
    InputStream inputStream = null;
    try {
      inputStream = ReflectUtil.getResourceAsStream(resourceName);
      if (inputStream == null) {
        if (isOptional) {
          log.fine("no schema resource "+resourceName+" for "+operation);
        } else {
          throw new ProcessEngineException("resource '" + resourceName + "' is not available");
        }
      } else {
        executeSchemaResource(operation, component, resourceName, inputStream);
      }

    } finally {
      IoUtil.closeSilently(inputStream);
    }
  }

  public void executeSchemaResource(String schemaFileResourceName) {
    FileInputStream inputStream = null;
    try {
      inputStream = new FileInputStream(new File(schemaFileResourceName));
      executeSchemaResource("schema operation", "process engine", schemaFileResourceName, inputStream);
    } catch (FileNotFoundException e) {
      throw new ProcessEngineException("Cannot find schema resource file '"+schemaFileResourceName,e);
    } finally {
      IoUtil.closeSilently(inputStream);
    }
  }

  private void executeSchemaResource(String operation, String component, String resourceName, InputStream inputStream) {
    log.info("performing "+operation+" on "+component+" with resource "+resourceName);
    String sqlStatement = null;
    String exceptionSqlStatement = null;
    try {
      Connection connection = sqlSession.getConnection();
      Exception exception = null;
      byte[] bytes = IoUtil.readInputStream(inputStream, resourceName);
      String ddlStatements = new String(bytes);
      BufferedReader reader = new BufferedReader(new StringReader(ddlStatements));
      String line = readNextTrimmedLine(reader);
      while (line != null) {
        if (line.startsWith("# ")) {
          log.fine(line.substring(2));

        } else if (line.startsWith("-- ")) {
          log.fine(line.substring(3));

        } else if (line.startsWith("execute java ")) {
          String upgradestepClassName = line.substring(13).trim();
          DbUpgradeStep dbUpgradeStep = null;
          try {
            dbUpgradeStep = (DbUpgradeStep) ReflectUtil.instantiate(upgradestepClassName);
          } catch (ProcessEngineException e) {
            throw new ProcessEngineException("database update java class '"+upgradestepClassName+"' can't be instantiated: "+e.getMessage(), e);
          }
          try {
            log.fine("executing upgrade step java class "+upgradestepClassName);
            dbUpgradeStep.execute(this);
          } catch (Exception e) {
            throw new ProcessEngineException("error while executing database update java class '"+upgradestepClassName+"': "+e.getMessage(), e);
          }

        } else if (line.length()>0) {

          if (line.endsWith(";")) {
            sqlStatement = addSqlStatementPiece(sqlStatement, line.substring(0, line.length()-1));
            Statement jdbcStatement = connection.createStatement();
            try {
              // no logging needed as the connection will log it
              log.fine("SQL: "+sqlStatement);
              jdbcStatement.execute(sqlStatement);
              jdbcStatement.close();
            } catch (Exception e) {
              if (exception == null) {
                exception = e;
                exceptionSqlStatement = sqlStatement;
              }
              log.log(Level.SEVERE, "problem during schema " + operation + ", statement '" + sqlStatement, e);
            } finally {
              sqlStatement = null;
            }
          } else {
            sqlStatement = addSqlStatementPiece(sqlStatement, line);
          }
        }

        line = readNextTrimmedLine(reader);
      }

      if (exception != null) {
        throw exception;
      }

      log.fine("activiti db schema " + operation + " for component "+component+" successful");

    } catch (Exception e) {
      throw new ProcessEngineException("couldn't "+operation+" db schema: "+exceptionSqlStatement, e);
    }
  }

  protected String addSqlStatementPiece(String sqlStatement, String line) {
    if (sqlStatement==null) {
      return line;
    }
    return sqlStatement + " \n" + line;
  }

  protected String readNextTrimmedLine(BufferedReader reader) throws IOException {
    String line = reader.readLine();
    if (line!=null) {
      line = line.trim();
    }
    return line;
  }

  protected boolean isMissingTablesException(Exception e) {
    String exceptionMessage = e.getMessage();
    if(e.getMessage() != null) {
      // Matches message returned from H2
      if ((exceptionMessage.indexOf("Table") != -1) && (exceptionMessage.indexOf("not found") != -1)) {
        return true;
      }

      // Message returned from MySQL and Oracle
      if (((exceptionMessage.indexOf("Table") != -1 || exceptionMessage.indexOf("table") != -1)) && (exceptionMessage.indexOf("doesn't exist") != -1)) {
        return true;
      }

      // Message returned from Postgres
      if (((exceptionMessage.indexOf("relation") != -1 || exceptionMessage.indexOf("table") != -1)) && (exceptionMessage.indexOf("does not exist") != -1)) {
        return true;
      }
    }
    return false;
  }

  public void performSchemaOperationsProcessEngineBuild() {
    String databaseSchemaUpdate = Context.getProcessEngineConfiguration().getDatabaseSchemaUpdate();
    if (ProcessEngineConfigurationImpl.DB_SCHEMA_UPDATE_DROP_CREATE.equals(databaseSchemaUpdate)) {
      try {
        dbSchemaDrop();
      } catch (RuntimeException e) {
        // ignore
      }
    }
    if ( ProcessEngineConfiguration.DB_SCHEMA_UPDATE_CREATE_DROP.equals(databaseSchemaUpdate)
         || ProcessEngineConfigurationImpl.DB_SCHEMA_UPDATE_DROP_CREATE.equals(databaseSchemaUpdate)
         || ProcessEngineConfigurationImpl.DB_SCHEMA_UPDATE_CREATE.equals(databaseSchemaUpdate)
       ) {
      dbSchemaCreate();
    } else if (ProcessEngineConfiguration.DB_SCHEMA_UPDATE_FALSE.equals(databaseSchemaUpdate)) {
      dbSchemaCheckVersion();
    } else if (ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE.equals(databaseSchemaUpdate)) {
      dbSchemaUpdate();
    }

    checkHistoryLevel();
  }

  public void performSchemaOperationsProcessEngineClose() {
    String databaseSchemaUpdate = Context.getProcessEngineConfiguration().getDatabaseSchemaUpdate();
    if (ProcessEngineConfiguration.DB_SCHEMA_UPDATE_CREATE_DROP.equals(databaseSchemaUpdate)) {
      dbSchemaDrop();
    }
  }

  // query factory methods ////////////////////////////////////////////////////

  public DeploymentQueryImpl createDeploymentQuery() {
    return new DeploymentQueryImpl();
  }
  public ProcessDefinitionQueryImpl createProcessDefinitionQuery() {
    return new ProcessDefinitionQueryImpl();
  }
  public ProcessInstanceQueryImpl createProcessInstanceQuery() {
    return new ProcessInstanceQueryImpl();
  }
  public ExecutionQueryImpl createExecutionQuery() {
    return new ExecutionQueryImpl();
  }
  public TaskQueryImpl createTaskQuery() {
    return new TaskQueryImpl();
  }
  public JobQueryImpl createJobQuery() {
    return new JobQueryImpl();
  }
  public HistoricProcessInstanceQueryImpl createHistoricProcessInstanceQuery() {
    return new HistoricProcessInstanceQueryImpl();
  }
  public HistoricActivityInstanceQueryImpl createHistoricActivityInstanceQuery() {
    return new HistoricActivityInstanceQueryImpl();
  }
  public HistoricTaskInstanceQueryImpl createHistoricTaskInstanceQuery() {
    return new HistoricTaskInstanceQueryImpl();
  }
  public HistoricDetailQueryImpl createHistoricDetailQuery() {
    return new HistoricDetailQueryImpl();
  }
  public HistoricVariableInstanceQueryImpl createHistoricVariableInstanceQuery() {
    return new HistoricVariableInstanceQueryImpl();
  }
  public UserQueryImpl createUserQuery() {
    return new DbUserQueryImpl();
  }
  public GroupQueryImpl createGroupQuery() {
    return new DbGroupQueryImpl();
  }

  // getters and setters //////////////////////////////////////////////////////

  public SqlSession getSqlSession() {
    return sqlSession;
  }
  public DbSqlSessionFactory getDbSqlSessionFactory() {
    return dbSqlSessionFactory;
  }


}
