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

import static org.camunda.bpm.engine.impl.db.entitymanager.operation.DbOperationType.DELETE;
import static org.camunda.bpm.engine.impl.db.entitymanager.operation.DbOperationType.INSERT;
import static org.camunda.bpm.engine.impl.db.entitymanager.operation.DbOperationType.UPDATE;

import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.ibatis.session.SqlSession;
import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.db.DbSqlSessionFactory;
import org.camunda.bpm.engine.impl.db.entitymanager.cache.CachedDbEntity;
import org.camunda.bpm.engine.impl.db.entitymanager.cache.DbEntityCache;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.DbBulkOperation;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.DbEntityOperation;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.DbOperation;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.DbOperationManager;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.DbOperationType;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.executor.DbOperationExecutor;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.executor.SqlDbOperationExecutor;
import org.camunda.bpm.engine.impl.interceptor.Session;

/**
 *
 * @author Daniel Meyer
 *
 */
public class DbEntityManager extends DbEntityCache implements Session {

  protected Logger log = Logger.getLogger(DbEntityManager.class.getName());

  /** the session factory */
  protected DbSqlSessionFactory dbSqlSessionFactory;

  protected DbOperationManager operationManager = new DbOperationManager();

  /**
   * The executor used for executing db operations.
   */
  protected DbOperationExecutor executor;


  public DbEntityManager(DbSqlSessionFactory dbSqlSessionFactory, SqlSession sqlSession) {
    this.dbSqlSessionFactory = dbSqlSessionFactory;
    this.executor = new SqlDbOperationExecutor(sqlSession, dbSqlSessionFactory);
  }

  public void flush() {

    // add UPDATE operations for all dirty objects in the cache
    Iterator<CachedDbEntity> cacheIterator = cachedEntitiesIterator();
    while (cacheIterator.hasNext()) {
      CachedDbEntity cachedDbEntity = cacheIterator.next();
      if(cachedDbEntity.isDirty()) {
        if(!update(cachedDbEntity.getEntity())) {
          // if a dirty object is not updated, this means that it is removed
          cacheIterator.remove();
        }
      }
    }

    // calculate the final operation ordering
    Collection<DbOperation> operationsToExecute = operationManager.getDbOperations();

    if(log.isLoggable(Level.FINE)) {
      logFlushSummary(operationsToExecute);
    }

    // execute the DB operations
    for (DbOperation dbOperation : operationsToExecute) {
      executor.execute(dbOperation);
    }

    // TODO

//    // mark all cached entities persistent
//    // (allows reusing the cache in a subsequent session)
//    for (CachedDbEntity cachedDbEntity : cachedEntites) {
//      cachedDbEntity.setEntityState(DbEntityState.PERSISTENT);
//      cachedDbEntity.makeCopy();
//    }

  }

  protected void logFlushSummary(Collection<DbOperation> operationsToExecute) {
    log.fine("Flush Summary:");
    for (DbOperation dbOperation : operationsToExecute) {
      log.fine("  " + dbOperation);
    }
  }

  public void insert(DbEntity dbEntity) {
    // generate Id if not present
    ensureHasId(dbEntity);

    // put into cache
    putTransient(dbEntity);

    // add insert operation
    DbEntityOperation dbOperation = new DbEntityOperation();
    dbOperation.setEntity(dbEntity);
    dbOperation.setOperationType(INSERT);
    operationManager.addOperation(dbOperation);
  }

  protected void ensureHasId(DbEntity dbEntity) {
    if(dbEntity.getId() == null) {
      String nextId = dbSqlSessionFactory.getIdGenerator().getNextId();
      dbEntity.setId(nextId);
    }
  }

  public boolean update(DbEntity dbEntity) {
    if(dbEntity.getId() == null) {
      return false;
    }
    // put into cache
    putPersistent(dbEntity);

    // add update operation
    DbEntityOperation dbOperation = new DbEntityOperation();
    dbOperation.setEntity(dbEntity);
    dbOperation.setOperationType(UPDATE);
    return operationManager.addOperation(dbOperation);
  }

  public void update(Class<? extends DbEntity> entityType, String statement, Object parameter) {
    initBulkOperation(entityType, statement, parameter, DbOperationType.UPDATE_BULK);
  }

  public void delete(DbEntity dbEntity) {
    // remove from cache
    if(remove(dbEntity)) {

      // schedule delete operation
      DbEntityOperation dbOperation = new DbEntityOperation();
      dbOperation.setEntity(dbEntity);
      dbOperation.setOperationType(DELETE);
      operationManager.addOperation(dbOperation);
    }
  }

  public void delete(Class<? extends DbEntity> entityType, String statement, Object parameter) {
    initBulkOperation(entityType, statement, parameter, DbOperationType.DELETE_BULK);
  }

  protected DbBulkOperation initBulkOperation(Class<? extends DbEntity> entityType, String statement, Object parameter, DbOperationType operationType) {
    // create operation
    DbBulkOperation bulkOperation = new DbBulkOperation();

    // configure operation
    bulkOperation.setOperationType(operationType);
    bulkOperation.setEntityType(entityType);
    bulkOperation.setStatement(statement);
    bulkOperation.setParameter(parameter);

    // schedule operation
    operationManager.addOperation(bulkOperation);
    return bulkOperation;
  }

  public void close() {

  }

  public boolean isDeleted(Object object) {
    return operationManager.isDeleted(object);
  }

  // getters / setters /////////////////////////////////

  public DbOperationExecutor getExecutor() {
    return executor;
  }

  public void setExecutor(DbOperationExecutor executor) {
    this.executor = executor;
  }

  public DbSqlSessionFactory getDbSqlSessionFactory() {
    return dbSqlSessionFactory;
  }

  public void setDbSqlSessionFactory(DbSqlSessionFactory dbSqlSessionFactory) {
    this.dbSqlSessionFactory = dbSqlSessionFactory;
  }

  public DbOperationManager getOperationManager() {
    return operationManager;
  }

  public void setOperationManager(DbOperationManager operationManager) {
    this.operationManager = operationManager;
  }

}
