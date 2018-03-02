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
package org.camunda.bpm.engine.impl.db.entitymanager;

import static org.camunda.bpm.engine.impl.db.entitymanager.cache.DbEntityState.DELETED_MERGED;
import static org.camunda.bpm.engine.impl.db.entitymanager.cache.DbEntityState.DELETED_PERSISTENT;
import static org.camunda.bpm.engine.impl.db.entitymanager.cache.DbEntityState.DELETED_TRANSIENT;
import static org.camunda.bpm.engine.impl.db.entitymanager.cache.DbEntityState.MERGED;
import static org.camunda.bpm.engine.impl.db.entitymanager.cache.DbEntityState.PERSISTENT;
import static org.camunda.bpm.engine.impl.db.entitymanager.cache.DbEntityState.TRANSIENT;
import static org.camunda.bpm.engine.impl.db.entitymanager.operation.DbOperationType.DELETE;
import static org.camunda.bpm.engine.impl.db.entitymanager.operation.DbOperationType.DELETE_BULK;
import static org.camunda.bpm.engine.impl.db.entitymanager.operation.DbOperationType.INSERT;
import static org.camunda.bpm.engine.impl.db.entitymanager.operation.DbOperationType.UPDATE;
import static org.camunda.bpm.engine.impl.db.entitymanager.operation.DbOperationType.UPDATE_BULK;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.executor.BatchExecutorException;
import org.apache.ibatis.executor.BatchResult;
import org.camunda.bpm.engine.impl.DeploymentQueryImpl;
import org.camunda.bpm.engine.impl.ExecutionQueryImpl;
import org.camunda.bpm.engine.impl.GroupQueryImpl;
import org.camunda.bpm.engine.impl.HistoricActivityInstanceQueryImpl;
import org.camunda.bpm.engine.impl.HistoricDetailQueryImpl;
import org.camunda.bpm.engine.impl.HistoricJobLogQueryImpl;
import org.camunda.bpm.engine.impl.HistoricProcessInstanceQueryImpl;
import org.camunda.bpm.engine.impl.HistoricTaskInstanceQueryImpl;
import org.camunda.bpm.engine.impl.HistoricVariableInstanceQueryImpl;
import org.camunda.bpm.engine.impl.JobQueryImpl;
import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.ProcessDefinitionQueryImpl;
import org.camunda.bpm.engine.impl.ProcessInstanceQueryImpl;
import org.camunda.bpm.engine.impl.TaskQueryImpl;
import org.camunda.bpm.engine.impl.UserQueryImpl;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cfg.IdGenerator;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cmmn.entity.repository.CaseDefinitionQueryImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.db.DbEntityLifecycleAware;
import org.camunda.bpm.engine.impl.db.EntityLoadListener;
import org.camunda.bpm.engine.impl.db.HasDbReferences;
import org.camunda.bpm.engine.impl.db.HasDbRevision;
import org.camunda.bpm.engine.impl.db.ListQueryParameterObject;
import org.camunda.bpm.engine.impl.db.PersistenceSession;
import org.camunda.bpm.engine.impl.db.EnginePersistenceLogger;
import org.camunda.bpm.engine.impl.db.entitymanager.cache.CachedDbEntity;
import org.camunda.bpm.engine.impl.db.entitymanager.cache.DbEntityCache;
import org.camunda.bpm.engine.impl.db.entitymanager.cache.DbEntityState;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.DbBulkOperation;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.DbEntityOperation;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.DbOperation;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.DbOperationManager;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.DbOperationType;
import org.camunda.bpm.engine.impl.identity.db.DbGroupQueryImpl;
import org.camunda.bpm.engine.impl.identity.db.DbUserQueryImpl;
import org.camunda.bpm.engine.impl.interceptor.Session;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutorContext;
import org.camunda.bpm.engine.impl.util.CollectionUtil;
import org.camunda.bpm.engine.impl.util.EnsureUtil;
import org.camunda.bpm.engine.impl.util.ExceptionUtil;

/**
 *
 * @author Daniel Meyer
 *
 */
@SuppressWarnings({ "rawtypes" })
public class DbEntityManager implements Session, EntityLoadListener {

  protected static final EnginePersistenceLogger LOG = ProcessEngineLogger.PERSISTENCE_LOGGER;
  protected static final String TOGGLE_FOREIGN_KEY_STMT = "toggleForeignKey";
  public static final int BATCH_SIZE = 50;

  protected List<OptimisticLockingListener> optimisticLockingListeners;

  protected IdGenerator idGenerator;

  protected DbEntityCache dbEntityCache;

  protected DbOperationManager dbOperationManager;

  protected PersistenceSession persistenceSession;
  protected boolean isIgnoreForeignKeysForNextFlush;

  public DbEntityManager(IdGenerator idGenerator, PersistenceSession persistenceSession) {
    this.idGenerator = idGenerator;
    this.persistenceSession = persistenceSession;
    if (persistenceSession != null) {
      this.persistenceSession.addEntityLoadListener(this);
    }
    initializeEntityCache();
    initializeOperationManager();
  }

  protected void initializeOperationManager() {
    dbOperationManager = new DbOperationManager();
  }

  protected void initializeEntityCache() {

    final JobExecutorContext jobExecutorContext = Context.getJobExecutorContext();
    final ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();

    if(processEngineConfiguration != null
        && processEngineConfiguration.isDbEntityCacheReuseEnabled()
        && jobExecutorContext != null) {

      dbEntityCache = jobExecutorContext.getEntityCache();
      if(dbEntityCache == null) {
        dbEntityCache = new DbEntityCache(processEngineConfiguration.getDbEntityCacheKeyMapping());
        jobExecutorContext.setEntityCache(dbEntityCache);
      }

    } else {

      if (processEngineConfiguration != null) {
        dbEntityCache = new DbEntityCache(processEngineConfiguration.getDbEntityCacheKeyMapping());
      } else {
        dbEntityCache = new DbEntityCache();
      }
    }

  }

  // selects /////////////////////////////////////////////////

  public List selectList(String statement) {
    return selectList(statement, null, 0, Integer.MAX_VALUE);
  }

  public List selectList(String statement, Object parameter) {
    return selectList(statement, parameter, 0, Integer.MAX_VALUE);
  }

  public List selectList(String statement, Object parameter, Page page) {
    if(page!=null) {
      return selectList(statement, parameter, page.getFirstResult(), page.getMaxResults());
    } else {
      return selectList(statement, parameter, 0, Integer.MAX_VALUE);
    }
  }

  public List selectList(String statement, ListQueryParameterObject parameter, Page page) {
    return selectList(statement, parameter);
  }

  public List selectList(String statement, Object parameter, int firstResult, int maxResults) {
    return selectList(statement, new ListQueryParameterObject(parameter, firstResult, maxResults));
  }

  public List selectList(String statement, ListQueryParameterObject parameter) {
    return selectListWithRawParameter(statement, parameter, parameter.getFirstResult(), parameter.getMaxResults());
  }

  @SuppressWarnings("unchecked")
  public List selectListWithRawParameter(String statement, Object parameter, int firstResult, int maxResults) {
    if(firstResult == -1 ||  maxResults==-1) {
      return Collections.EMPTY_LIST;
    }
    List loadedObjects = persistenceSession.selectList(statement, parameter);
    return filterLoadedObjects(loadedObjects);
  }

  public Object selectOne(String statement, Object parameter) {
    Object result = persistenceSession.selectOne(statement, parameter);
    if (result instanceof DbEntity) {
      DbEntity loadedObject = (DbEntity) result;
      result = cacheFilter(loadedObject);
    }
    return result;
  }

  @SuppressWarnings("unchecked")
  public boolean selectBoolean(String statement, Object parameter) {
    List<String> result = (List<String>) persistenceSession.selectList(statement, parameter);
    if(result != null) {
      return result.contains(1);
    }
    return false;

  }

  public <T extends DbEntity> T selectById(Class<T> entityClass, String id) {
    T persistentObject = dbEntityCache.get(entityClass, id);
    if (persistentObject!=null) {
      return persistentObject;
    }

    persistentObject = persistenceSession.selectById(entityClass, id);

    if (persistentObject==null) {
      return null;
    }
    // don't have to put object into the cache now. See onEntityLoaded() callback
    return persistentObject;
  }

  public <T extends DbEntity> T getCachedEntity(Class<T> type, String id) {
    return dbEntityCache.get(type, id);
  }

  public <T extends DbEntity> List<T> getCachedEntitiesByType(Class<T> type) {
    return dbEntityCache.getEntitiesByType(type);
  }

  protected List filterLoadedObjects(List<Object> loadedObjects) {
    if (loadedObjects.isEmpty() || loadedObjects.get(0) == null) {
      return loadedObjects;
    }
    if (! (DbEntity.class.isAssignableFrom(loadedObjects.get(0).getClass()))) {
      return loadedObjects;
    }
    List<DbEntity> filteredObjects = new ArrayList<DbEntity>(loadedObjects.size());
    for (Object loadedObject: loadedObjects) {
      DbEntity cachedPersistentObject = cacheFilter((DbEntity) loadedObject);
      filteredObjects.add(cachedPersistentObject);
    }
    return filteredObjects;
  }

  /** returns the object in the cache.  if this object was loaded before,
   * then the original object is returned. */
  protected DbEntity cacheFilter(DbEntity persistentObject) {
    DbEntity cachedPersistentObject = dbEntityCache.get(persistentObject.getClass(), persistentObject.getId());
    if (cachedPersistentObject!=null) {
      return cachedPersistentObject;
    }
    else {
      return persistentObject;
    }

  }

  public void onEntityLoaded(DbEntity entity) {
    // we get a callback when the persistence session loads an object from the database
    DbEntity cachedPersistentObject = dbEntityCache.get(entity.getClass(), entity.getId());
    if(cachedPersistentObject == null) {
      // only put into the cache if not already present
      dbEntityCache.putPersistent(entity);

      // invoke postLoad() lifecycle method
      if (entity instanceof DbEntityLifecycleAware) {
        DbEntityLifecycleAware lifecycleAware = (DbEntityLifecycleAware) entity;
        lifecycleAware.postLoad();
      }
    }

  }

  public void lock(String statement) {
    lock(statement, null);
  }

  public void lock(String statement, Object parameter) {
    persistenceSession.lock(statement, parameter);
  }

  public boolean isDirty(DbEntity dbEntity) {
    CachedDbEntity cachedEntity = dbEntityCache.getCachedEntity(dbEntity);
    if(cachedEntity == null) {
      return false;
    } else {
      return cachedEntity.isDirty() || cachedEntity.getEntityState() == DbEntityState.MERGED;
    }
  }

  public void flush() {

    // flush the entity cache which inserts operations to the db operation manager
    flushEntityCache();

    // flush the db operation manager
    flushDbOperationManager();
  }

  public void setIgnoreForeignKeysForNextFlush(boolean ignoreForeignKeysForNextFlush) {
    isIgnoreForeignKeysForNextFlush = ignoreForeignKeysForNextFlush;
  }

  protected void flushDbOperationManager() {

    // obtain totally ordered operation list from operation manager
    List<DbOperation> operationsToFlush = dbOperationManager.calculateFlush();
    if (operationsToFlush == null || operationsToFlush.size() == 0) {
      return;
    }

    LOG.databaseFlushSummary(operationsToFlush);

    // If we want to delete all table data as bulk operation, on tables which have self references,
    // We need to turn the foreign key check off on MySQL and MariaDB.
    // On other databases we have to do nothing, the mapped statement will be empty.
    if (isIgnoreForeignKeysForNextFlush) {
      persistenceSession.executeNonEmptyUpdateStmt(TOGGLE_FOREIGN_KEY_STMT, false);
      persistenceSession.flushOperations();
    }

    try {
      final List<List<DbOperation>> batches = CollectionUtil.partition(operationsToFlush, BATCH_SIZE);
      for (List<DbOperation> batch : batches) {
        flushDbOperations(batch);
      }
    } finally {
      if (isIgnoreForeignKeysForNextFlush) {
        persistenceSession.executeNonEmptyUpdateStmt(TOGGLE_FOREIGN_KEY_STMT, true);
        persistenceSession.flushOperations();
        isIgnoreForeignKeysForNextFlush = false;
      }
    }
  }

  protected void flushDbOperations(List<DbOperation> operationsToFlush) {
    // execute the flush
    for (DbOperation dbOperation : operationsToFlush) {
      boolean doOptimisticLockingException = false;
      try {
        persistenceSession.executeDbOperation(dbOperation);
      } catch (Exception e) {
        //some of the exceptions are considered to be optimistic locking exception
        doOptimisticLockingException = isOptimisticLockingException(dbOperation, e);
        if (!doOptimisticLockingException) {
          throw LOG.flushDbOperationException(operationsToFlush, dbOperation, e);
        }
      }
      if (dbOperation.isFailed() || doOptimisticLockingException) {
        handleOptimisticLockingException(dbOperation);
      }
    }

    if (Context.getProcessEngineConfiguration().isJdbcBatchProcessing()) {
      List<BatchResult> flushResult = new ArrayList<BatchResult>();
      try {
        flushResult = persistenceSession.flushOperations();
      } catch (Exception e) {
        //some of the exceptions are considered to be optimistic locking exception
        DbOperation failedOperation = hasOptimisticLockingException(operationsToFlush, e);
        if (failedOperation == null) {
          throw LOG.flushDbOperationsException(operationsToFlush, e);
        } else {
          handleOptimisticLockingException(failedOperation);
        }
      }
      checkFlushResults(operationsToFlush, flushResult);
    }
  }

  /**
   * An OptimisticLockingException check for batch processing
   *
   * @param operationsToFlush The list of DB operations in which the Exception occurred
   * @param cause the Exception object
   * @return The DbOperation where the OptimisticLockingException has occurred
   * or null if no OptimisticLockingException occurred
   */
  private DbOperation hasOptimisticLockingException(List<DbOperation> operationsToFlush, Throwable cause) {

    BatchExecutorException batchExecutorException = ExceptionUtil.findBatchExecutorException(cause);

    if (batchExecutorException != null) {

      int failedOperationIndex = batchExecutorException.getSuccessfulBatchResults().size();
      if (failedOperationIndex < operationsToFlush.size()) {
        DbOperation failedOperation = operationsToFlush.get(failedOperationIndex);
        if (isOptimisticLockingException(failedOperation, cause)) {
          return failedOperation;
        }
      }
    }

    return null;
  }

  /**
   * Checks if the reason for a persistence exception was the foreign-key referencing of a (currently)
   * non-existing entity. This might happen with concurrent transactions, leading to an
   * OptimisticLockingException.
   *
   * @param failedOperation
   * @return
   */
  private boolean isOptimisticLockingException(DbOperation failedOperation, Throwable cause) {

    boolean isConstraintViolation = ExceptionUtil.checkForeignKeyConstraintViolation(cause);
    boolean isVariableIntegrityViolation = ExceptionUtil.checkVariableIntegrityViolation(cause);

    if (isVariableIntegrityViolation) {

      return true;
    } else if (
      isConstraintViolation
      && failedOperation instanceof DbEntityOperation
      && ((DbEntityOperation) failedOperation).getEntity() instanceof HasDbReferences
      && (failedOperation.getOperationType().equals(DbOperationType.INSERT)
      || failedOperation.getOperationType().equals(DbOperationType.UPDATE))
      ) {

      DbEntity entity = ((DbEntityOperation) failedOperation).getEntity();
      for (Map.Entry<String, Class> reference : ((HasDbReferences)entity).getReferencedEntitiesIdAndClass().entrySet()) {
        DbEntity referencedEntity = this.persistenceSession.selectById(reference.getValue(), reference.getKey());
        if (referencedEntity == null) {

          return true;
        }
      }
    }

    return false;
  }

  protected void checkFlushResults(List<DbOperation> operationsToFlush, List<BatchResult> flushResult) {
    int flushResultSize = 0;

    if (flushResult != null && flushResult.size() > 0) {
      LOG.printBatchResults(flushResult);
      //process the batch results to handle Optimistic Lock Exceptions
      Iterator<DbOperation> operationIt = operationsToFlush.iterator();
      for (BatchResult batchResult : flushResult) {
        for (int statementResult : batchResult.getUpdateCounts()) {
          flushResultSize++;
          DbOperation thisOperation = operationIt.next();
          if (thisOperation instanceof DbEntityOperation && ((DbEntityOperation) thisOperation).getEntity() instanceof HasDbRevision
            && !thisOperation.getOperationType().equals(DbOperationType.INSERT)) {
            final DbEntity dbEntity = ((DbEntityOperation) thisOperation).getEntity();
            if (statementResult != 1) {
              ((DbEntityOperation) thisOperation).setFailed(true);
              handleOptimisticLockingException(thisOperation);
            } else {
              //update revision number in cache
              if (thisOperation.getOperationType().equals(DbOperationType.UPDATE)) {
                HasDbRevision versionedObject = (HasDbRevision) dbEntity;
                versionedObject.setRevision(versionedObject.getRevisionNext());
              }
            }
          }
        }
      }
      //this must not happen, but worth checking
      if (operationsToFlush.size() != flushResultSize) {
        LOG.wrongBatchResultsSizeException(operationsToFlush);
      }
    }
  }

  public void flushEntity(DbEntity entity) {
    CachedDbEntity cachedEntity = dbEntityCache.getCachedEntity(entity);
    if (cachedEntity != null) {
      flushCachedEntity(cachedEntity);
    }

    flushDbOperationManager();
  }

  protected void handleOptimisticLockingException(DbOperation dbOperation) {
    boolean isHandled = false;

    if(optimisticLockingListeners != null) {
      for (OptimisticLockingListener optimisticLockingListener : optimisticLockingListeners) {
        if(optimisticLockingListener.getEntityType() == null
            || optimisticLockingListener.getEntityType().isAssignableFrom(dbOperation.getEntityType())) {
          optimisticLockingListener.failedOperation(dbOperation);
          isHandled = true;
        }
      }
    }

    if(!isHandled) {
      throw LOG.concurrentUpdateDbEntityException(dbOperation);
    }
  }

  /**
   * Flushes the entity cache:
   * Depending on the entity state, the required {@link DbOperation} is performed and the cache is updated.
   */
  protected void flushEntityCache() {
    List<CachedDbEntity> cachedEntities = dbEntityCache.getCachedEntities();
    for (CachedDbEntity cachedDbEntity : cachedEntities) {
      flushCachedEntity(cachedDbEntity);
    }

    // log cache state after flush
    LOG.flushedCacheState(dbEntityCache.getCachedEntities());
  }

  protected void flushCachedEntity(CachedDbEntity cachedDbEntity) {

    if(cachedDbEntity.getEntityState() == TRANSIENT) {
      // latest state of references in cache is relevant when determining insertion order
      cachedDbEntity.determineEntityReferences();
      // perform INSERT
      performEntityOperation(cachedDbEntity, INSERT);
      // mark PERSISTENT
      cachedDbEntity.setEntityState(PERSISTENT);

    } else if(cachedDbEntity.getEntityState() == PERSISTENT && cachedDbEntity.isDirty()) {
      // object is dirty -> perform UPDATE
      performEntityOperation(cachedDbEntity, UPDATE);

    } else if(cachedDbEntity.getEntityState() == MERGED) {
      // perform UPDATE
      performEntityOperation(cachedDbEntity, UPDATE);
      // mark PERSISTENT
      cachedDbEntity.setEntityState(PERSISTENT);

    } else if(cachedDbEntity.getEntityState() == DELETED_TRANSIENT) {
      // remove from cache
      dbEntityCache.remove(cachedDbEntity);

    } else if(cachedDbEntity.getEntityState() == DELETED_PERSISTENT
           || cachedDbEntity.getEntityState() == DELETED_MERGED) {
      // perform DELETE
      performEntityOperation(cachedDbEntity, DELETE);
      // remove from cache
      dbEntityCache.remove(cachedDbEntity);

    }

    // if object is PERSISTENT after flush
    if(cachedDbEntity.getEntityState() == PERSISTENT) {
      // make a new copy
      cachedDbEntity.makeCopy();
      // update cached references
      cachedDbEntity.determineEntityReferences();
    }
  }

  public void insert(DbEntity dbEntity) {
    // generate Id if not present
    ensureHasId(dbEntity);

    validateId(dbEntity);

    // put into cache
    dbEntityCache.putTransient(dbEntity);

  }

  public void merge(DbEntity dbEntity) {

    if(dbEntity.getId() == null) {
      throw LOG.mergeDbEntityException(dbEntity);
    }

    // NOTE: a proper implementation of merge() would fetch the entity from the database
    // and merge the state changes. For now, we simply always perform an update.
    // Supposedly, the "proper" implementation would reduce the number of situations where
    // optimistic locking results in a conflict.

    dbEntityCache.putMerged(dbEntity);
  }

  public void forceUpdate(DbEntity entity) {
    CachedDbEntity cachedEntity = dbEntityCache.getCachedEntity(entity);
    if(cachedEntity != null && cachedEntity.getEntityState() == PERSISTENT) {
      cachedEntity.forceSetDirty();
    }
  }

  public void delete(DbEntity dbEntity) {
    dbEntityCache.setDeleted(dbEntity);
  }

  public void undoDelete(DbEntity entity){
    dbEntityCache.undoDelete(entity);
  }

  public void update(Class<? extends DbEntity> entityType, String statement, Object parameter) {
    performBulkOperation(entityType, statement, parameter, UPDATE_BULK);
  }

  /**
   * Several update operations added by this method will be executed preserving the order of method calls, no matter what entity type they refer to.
   * They will though be executed after all "not-bulk" operations (e.g. {@link DbEntityManager#insert(DbEntity)} or {@link DbEntityManager#merge(DbEntity)})
   * and after those updates added by {@link DbEntityManager#update(Class, String, Object)}.
   * @param entityType
   * @param statement
   * @param parameter
   */
  public void updatePreserveOrder(Class<? extends DbEntity> entityType, String statement, Object parameter) {
    performBulkOperationPreserveOrder(entityType, statement, parameter, UPDATE_BULK);
  }

  public void delete(Class<? extends DbEntity> entityType, String statement, Object parameter) {
    performBulkOperation(entityType, statement, parameter, DELETE_BULK);
  }

  /**
   * Several delete operations added by this method will be executed preserving the order of method calls, no matter what entity type they refer to.
   * They will though be executed after all "not-bulk" operations (e.g. {@link DbEntityManager#insert(DbEntity)} or {@link DbEntityManager#merge(DbEntity)})
   * and after those deletes added by {@link DbEntityManager#delete(Class, String, Object)}.
   * @param entityType
   * @param statement
   * @param parameter
   */
  public void deletePreserveOrder(Class<? extends DbEntity> entityType, String statement, Object parameter) {
    performBulkOperationPreserveOrder(entityType, statement, parameter, DELETE_BULK);
  }

  protected DbBulkOperation performBulkOperation(Class<? extends DbEntity> entityType, String statement, Object parameter, DbOperationType operationType) {
    // create operation
    DbBulkOperation bulkOperation = createDbBulkOperation(entityType, statement, parameter, operationType);

    // schedule operation
    dbOperationManager.addOperation(bulkOperation);
    return bulkOperation;
  }

  protected DbBulkOperation performBulkOperationPreserveOrder(Class<? extends DbEntity> entityType, String statement, Object parameter, DbOperationType operationType) {
    DbBulkOperation bulkOperation = createDbBulkOperation(entityType, statement, parameter, operationType);

    // schedule operation
    dbOperationManager.addOperationPreserveOrder(bulkOperation);
    return bulkOperation;
  }

  private DbBulkOperation createDbBulkOperation(Class<? extends DbEntity> entityType, String statement, Object parameter, DbOperationType operationType) {
    // create operation
    DbBulkOperation bulkOperation = new DbBulkOperation();

    // configure operation
    bulkOperation.setOperationType(operationType);
    bulkOperation.setEntityType(entityType);
    bulkOperation.setStatement(statement);
    bulkOperation.setParameter(parameter);
    return bulkOperation;
  }

  protected void performEntityOperation(CachedDbEntity cachedDbEntity, DbOperationType type) {
    DbEntityOperation dbOperation = new DbEntityOperation();
    dbOperation.setEntity(cachedDbEntity.getEntity());
    dbOperation.setFlushRelevantEntityReferences(cachedDbEntity.getFlushRelevantEntityReferences());
    dbOperation.setOperationType(type);
    dbOperationManager.addOperation(dbOperation);
  }

  public void close() {

  }

  public boolean isDeleted(DbEntity object) {
    return dbEntityCache.isDeleted(object);
  }

  protected void ensureHasId(DbEntity dbEntity) {
    if(dbEntity.getId() == null) {
      String nextId = idGenerator.getNextId();
      dbEntity.setId(nextId);
    }
  }

  protected void validateId(DbEntity dbEntity) {
    EnsureUtil.ensureValidIndividualResourceId("Entity " + dbEntity + " has an invalid id", dbEntity.getId());
  }

  public <T extends DbEntity> List<T> pruneDeletedEntities(List<T> listToPrune) {
    ArrayList<T> prunedList = new ArrayList<T>();
    for (T potentiallyDeleted : listToPrune) {
      if(!isDeleted(potentiallyDeleted)) {
        prunedList.add(potentiallyDeleted);
      }
    }
    return prunedList;
  }

  public boolean contains(DbEntity dbEntity) {
    return dbEntityCache.contains(dbEntity);
  }

  // getters / setters /////////////////////////////////

  public DbOperationManager getDbOperationManager() {
    return dbOperationManager;
  }

  public void setDbOperationManager(DbOperationManager operationManager) {
    this.dbOperationManager = operationManager;
  }

  public DbEntityCache getDbEntityCache() {
    return dbEntityCache;
  }

  public void setDbEntityCache(DbEntityCache dbEntityCache) {
    this.dbEntityCache = dbEntityCache;
  }

  // query factory methods ////////////////////////////////////////////////////

  public DeploymentQueryImpl createDeploymentQuery() {
    return new DeploymentQueryImpl();
  }

  public ProcessDefinitionQueryImpl createProcessDefinitionQuery() {
    return new ProcessDefinitionQueryImpl();
  }

  public CaseDefinitionQueryImpl createCaseDefinitionQuery() {
    return new CaseDefinitionQueryImpl();
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

  public HistoricJobLogQueryImpl createHistoricJobLogQuery() {
    return new HistoricJobLogQueryImpl();
  }

  public UserQueryImpl createUserQuery() {
    return new DbUserQueryImpl();
  }

  public GroupQueryImpl createGroupQuery() {
    return new DbGroupQueryImpl();
  }

  public void registerOptimisticLockingListener(OptimisticLockingListener optimisticLockingListener) {
    if(optimisticLockingListeners == null) {
      optimisticLockingListeners = new ArrayList<OptimisticLockingListener>();
    }
    optimisticLockingListeners.add(optimisticLockingListener);
  }

  public List<String> getTableNamesPresentInDatabase() {
    return persistenceSession.getTableNamesPresent();
  }



}
