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
package org.camunda.bpm.engine.impl.db.entitymanager.operation.executor;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.ibatis.session.SqlSession;
import org.camunda.bpm.engine.OptimisticLockingException;
import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.db.DbSqlSessionFactory;
import org.camunda.bpm.engine.impl.db.HasDbRevision;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.DbBulkOperation;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.DbEntityOperation;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.DbOperation;
import org.camunda.bpm.engine.impl.util.ClassNameUtil;

/**
 * Mybatis-based implementation of the {@link DbOperationExecutor}.
 *
 * Supports optimistic locking
 *
 * @author Daniel Meyer
 *
 */
public class SqlDbOperationExecutor implements DbOperationExecutor {

  private final static Logger log = Logger.getLogger(SqlDbOperationExecutor.class.getName());

  protected DbSqlSessionFactory dbSqlSessionFactory;
  protected SqlSession sqlSession;

  public SqlDbOperationExecutor(SqlSession sqlSession, DbSqlSessionFactory dbSqlSessionFactory) {
    this.sqlSession = sqlSession;
    this.dbSqlSessionFactory = dbSqlSessionFactory;
  }

  public void execute(DbOperation operation) {
    switch(operation.getOperationType()) {

      case SELECT:

      case INSERT:
        insertEntity((DbEntityOperation) operation);
        break;

      case DELETE:
        deleteEntity((DbEntityOperation) operation);
        break;
      case DELETE_BULK:
        deleteBulk((DbBulkOperation) operation);
        break;

      case UPDATE:
        updateEntity((DbEntityOperation) operation);
        break;
      case UPDATE_BULK:
        updateBulk((DbBulkOperation) operation);
        break;

    }
  }

  // insert //////////////////////////////////////////

  protected void insertEntity(DbEntityOperation operation) {

    final DbEntity dbEntity = operation.getEntity();

    // get statement
    String insertStatement = dbSqlSessionFactory.getInsertStatement(dbEntity);
    insertStatement = dbSqlSessionFactory.mapStatement(insertStatement);
    ensureNotNull("no insert statement for " + dbEntity.getClass() + " in the ibatis mapping files", "insertStatement", insertStatement);

    // execute the insert
    executeInsertEntity(insertStatement, dbEntity);

    // perform post insert actions on entity
    entityInserted(dbEntity);
  }

  protected void executeInsertEntity(String insertStatement, Object parameter) {
    if(log.isLoggable(Level.FINE)) {
      log.fine("inserting: " + toString(parameter));
    }
    sqlSession.insert(insertStatement, parameter);

    // increment revision of our copy
    if (parameter instanceof HasDbRevision) {
      HasDbRevision versionedObject = (HasDbRevision) parameter;
      versionedObject.setRevision(versionedObject.getRevisionNext());
    }
  }

  protected void entityInserted(final DbEntity entity) {
    // nothing to do
  }

  // delete ///////////////////////////////////////////

  protected void deleteEntity(DbEntityOperation operation) {

    final DbEntity dbEntity = operation.getEntity();

    // get statement
    String deleteStatement = dbSqlSessionFactory.getDeleteStatement(dbEntity.getClass());
    ensureNotNull("no delete statement for " + dbEntity.getClass() + " in the ibatis mapping files", "deleteStatement", deleteStatement);

    if(log.isLoggable(Level.FINE)) {
      log.fine("deleting: " + toString(dbEntity));
    }

    // execute the delete
    executeDelete(deleteStatement, dbEntity);

    // perform post delete action
    entityDeleted(dbEntity);
  }

  protected void executeDelete(String deleteStatement, Object parameter) {

    // map the statement
    deleteStatement = dbSqlSessionFactory.mapStatement(deleteStatement);

    // It only makes sense to check for optimistic locking exceptions for objects that actually have a revision
    if (parameter instanceof HasDbRevision) {
      int nrOfRowsDeleted = sqlSession.delete(deleteStatement, parameter);
      if (nrOfRowsDeleted == 0) {
        // enforce optimistic locking
        throw new OptimisticLockingException(toString(parameter) + " was updated by another transaction concurrently");
      }
    } else {
      sqlSession.delete(deleteStatement, parameter);
    }
  }

  protected void entityDeleted(final DbEntity entity) {
    // nothing to do
  }

  protected void deleteBulk(DbBulkOperation operation) {
    String statement = operation.getStatement();
    Object parameter = operation.getParameter();

    if(log.isLoggable(Level.FINE)) {
      log.fine("deleting (bulk): " + statement + " " + parameter);
    }

    executeDelete(statement, parameter);
  }

  // update ////////////////////////////////////////

  protected void updateEntity(DbEntityOperation operation) {

    final DbEntity dbEntity = operation.getEntity();

    String updateStatement = dbSqlSessionFactory.getUpdateStatement(dbEntity);
    ensureNotNull("no update statement for " + dbEntity.getClass() + " in the ibatis mapping files", "updateStatement", updateStatement);

    if (log.isLoggable(Level.FINE)) {
      log.fine("updating: " + toString(dbEntity) + "]");
    }

    // execute update
    executeUpdate(updateStatement, dbEntity);

    // perform post update action
    entityUpdated(dbEntity);
  }

  protected void executeUpdate(String updateStatement, Object parameter) {

    updateStatement = dbSqlSessionFactory.mapStatement(updateStatement);

    int updatedRecords = sqlSession.update(updateStatement, parameter);

    if (parameter instanceof HasDbRevision) {
      if (updatedRecords != 1) {
        // enforce optimistic locking
        throw new OptimisticLockingException(toString(parameter) + " was updated by another transaction concurrently");
      } else {
        // increment revision of our copy
        HasDbRevision versionedObject = (HasDbRevision) parameter;
        versionedObject.setRevision(versionedObject.getRevisionNext());
      }
    }
  }

  protected void entityUpdated(final DbEntity entity) {
    // nothing to do
  }

  protected void updateBulk(DbBulkOperation operation) {
    String statement = operation.getStatement();
    Object parameter = operation.getParameter();

    if(log.isLoggable(Level.FINE)) {
      log.fine("updating (bulk): " + statement + " " + parameter);
    }

    executeUpdate(statement, parameter);
  }

  // utils /////////////////////////////////////////

  protected String toString(Object object) {
    if(object == null) {
      return "null";
    }
    if(object instanceof DbEntity) {
      DbEntity dbEntity = (DbEntity) object;
      return ClassNameUtil.getClassNameWithoutPackage(dbEntity)+"["+dbEntity.getId()+"]";
    }
    return object.toString();
  }

}
