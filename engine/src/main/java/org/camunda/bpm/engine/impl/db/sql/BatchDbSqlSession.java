/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.impl.db.sql;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.executor.BatchExecutorException;
import org.apache.ibatis.executor.BatchResult;
import org.apache.ibatis.session.ExecutorType;
import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.db.FlushResult;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.DbBulkOperation;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.DbEntityOperation;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.DbOperation;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.DbOperationType;
import org.camunda.bpm.engine.impl.util.CollectionUtil;
import org.camunda.bpm.engine.impl.util.EnsureUtil;
import org.camunda.bpm.engine.impl.util.ExceptionUtil;

/**
 * For mybatis {@link ExecutorType#BATCH}
 */
public class BatchDbSqlSession extends DbSqlSession {

  public BatchDbSqlSession(DbSqlSessionFactory dbSqlSessionFactory) {
    super(dbSqlSessionFactory);
  }

  public BatchDbSqlSession(DbSqlSessionFactory dbSqlSessionFactory, Connection connection, String catalog, String schema) {
    super(dbSqlSessionFactory, connection, catalog, schema);
  }

  @Override
  public FlushResult executeDbOperations(List<DbOperation> operations) {
    for (DbOperation operation : operations) {

      try {
        // stage operation
        executeDbOperation(operation);

      } catch (Exception ex) {
        // exception is wrapped later
        throw ex;

      }
    }

    List<BatchResult> batchResults;
    try {
      // applies all operations
      batchResults = flushBatchOperations();
    } catch (PersistenceException e) {
      return postProcessBatchFailure(operations, e);
    }

    return postProcessBatchSuccess(operations, batchResults);
  }

  protected FlushResult postProcessBatchSuccess(List<DbOperation> operations, List<BatchResult> batchResults) {
    Iterator<DbOperation> operationsIt = operations.iterator();
    List<DbOperation> failedOperations = new ArrayList<>();
    for (BatchResult successfulBatch : batchResults) {
      // even if all batches are successful, there can be concurrent modification failures
      // (e.g. 0 rows updated)
      postProcessJdbcBatchResult(operationsIt, successfulBatch.getUpdateCounts(), null, failedOperations);
    }

    // there should be no more operations remaining
    if (operationsIt.hasNext()) {
      throw LOG.wrongBatchResultsSizeException(operations);
    }

    return FlushResult.withFailures(failedOperations);
  }

  protected FlushResult postProcessBatchFailure(List<DbOperation> operations, PersistenceException exception) {
    BatchExecutorException batchExecutorException =
        ExceptionUtil.findBatchExecutorException(exception);

    if (batchExecutorException == null) {
      // Unexpected exception
      throw exception;
    }

    List<BatchResult> successfulBatches = batchExecutorException.getSuccessfulBatchResults();
    BatchUpdateException cause = batchExecutorException.getBatchUpdateException();

    Iterator<DbOperation> operationsIt = operations.iterator();
    List<DbOperation> failedOperations = new ArrayList<>();

    for (BatchResult successfulBatch : successfulBatches) {
      postProcessJdbcBatchResult(operationsIt, successfulBatch.getUpdateCounts(), null, failedOperations);
    }

    int[] failedBatchUpdateCounts = cause.getUpdateCounts();
    postProcessJdbcBatchResult(operationsIt, failedBatchUpdateCounts, exception, failedOperations);

    List<DbOperation> remainingOperations = CollectionUtil.collectInList(operationsIt);
    return FlushResult.withFailuresAndRemaining(failedOperations, remainingOperations);
  }

  /**
   * <p>This method can be called with three cases:
   *
   * <ul>
   * <li>Case 1: Success. statementResults contains the number of
   * affected rows for all operations.
   * <li>Case 2: Failure. statementResults contains the number of
   * affected rows for all successful operations that were executed
   * before the failed operation.
   * <li>Case 3: Failure. statementResults contains the number of
   * affected rows for all operations of the batch, i.e. further
   * statements were executed after the first failed statement.
   * </ul>
   *
   * <p>See {@link BatchUpdateException#getUpdateCounts()} for the specification
   * of cases 2 and 3.
   *
   * @return all failed operations
   */
  protected void postProcessJdbcBatchResult(
      Iterator<DbOperation> operationsIt,
      int[] statementResults,
      PersistenceException failure,
      List<DbOperation> failedOperations) {
    boolean failureHandled = false;

    for (int statementResult : statementResults) {
      EnsureUtil.ensureTrue("More batch results than scheduled operations detected. This indicates a bug",
          operationsIt.hasNext());

      DbOperation operation = operationsIt.next();

      if (statementResult == Statement.SUCCESS_NO_INFO) {

        if (requiresAffectedRows(operation.getOperationType())) {
          throw LOG.batchingNotSupported(operation);
        } else {
          postProcessOperationPerformed(operation, 1, null);
        }

      } else if (statementResult == Statement.EXECUTE_FAILED) {

        /*
         * All operations are marked with the root failure exception; this is not quite
         * correct and leads to the situation that we treat all failed operations in the
         * same way, whereas they might fail for different reasons.
         *
         * More precise would be to use BatchUpdateException#getNextException.
         * E.g. if we have three failed statements in a batch, #getNextException can be used to
         * access each operation's individual failure. However, this behavior is not
         * guaranteed by the java.sql javadocs (it doesn't specify that the number
         * and order of next exceptions matches the number of failures, unlike for row counts),
         * so we decided to not rely on it.
         */
        postProcessOperationPerformed(operation, 0, failure);
        failureHandled = true;
      } else { // it is the number of affected rows
        postProcessOperationPerformed(operation, statementResult, null);
      }

      if (operation.isFailed()) {
        failedOperations.add(operation); // the operation is added to the list only if it's marked as failed
      }
    }

    /*
     * case 2: The next operation is the one that failed
     */
    if (failure != null && !failureHandled) {
      EnsureUtil.ensureTrue("More batch results than scheduled operations detected. This indicates a bug",
          operationsIt.hasNext());

      DbOperation failedOperation = operationsIt.next();
      postProcessOperationPerformed(failedOperation, 0, failure);
      if (failedOperation.isFailed()) {
        failedOperations.add(failedOperation); // the operation is added to the list only if it's marked as failed
      }
    }
  }

  protected boolean requiresAffectedRows(DbOperationType operationType) {
    /*
     * Affected rows required:
     * - UPDATE and DELETE: optimistic locking
     * - BULK DELETE: history cleanup
     * - BULK UPDATE: not required currently, but we'll require it for consistency with deletes
     *
     * Affected rows not required:
     * - INSERT: not required for any functionality and some databases
     *   have performance optimizations that sacrifice this (e.g. Postgres with reWriteBatchedInserts)
     */
    return operationType != DbOperationType.INSERT;
  }

  protected void postProcessOperationPerformed(DbOperation operation,
                                               int rowsAffected,
                                               PersistenceException failure) {

    switch(operation.getOperationType()) {

      case INSERT:
        entityInsertPerformed((DbEntityOperation) operation, rowsAffected, failure);
        break;

      case DELETE:
        entityDeletePerformed((DbEntityOperation) operation, rowsAffected, failure);
        break;
      case DELETE_BULK:
        bulkDeletePerformed((DbBulkOperation) operation, rowsAffected, failure);
        break;

      case UPDATE:
        entityUpdatePerformed((DbEntityOperation) operation, rowsAffected, failure);
        break;
      case UPDATE_BULK:
        bulkUpdatePerformed((DbBulkOperation) operation, rowsAffected, failure);
        break;

    }
  }


  @Override
  protected void updateEntity(DbEntityOperation operation) {

    final DbEntity dbEntity = operation.getEntity();

    String updateStatement = dbSqlSessionFactory.getUpdateStatement(dbEntity);
    ensureNotNull("no update statement for " + dbEntity.getClass() + " in the ibatis mapping files", "updateStatement", updateStatement);

    LOG.executeDatabaseOperation("UPDATE", dbEntity);
    executeUpdate(updateStatement, dbEntity);
  }

  @Override
  protected void updateBulk(DbBulkOperation operation) {
    String statement = operation.getStatement();
    Object parameter = operation.getParameter();

    LOG.executeDatabaseBulkOperation("UPDATE", statement, parameter);

    executeUpdate(statement, parameter);
  }

  @Override
  protected void deleteBulk(DbBulkOperation operation) {
    String statement = operation.getStatement();
    Object parameter = operation.getParameter();

    LOG.executeDatabaseBulkOperation("DELETE", statement, parameter);

    executeDelete(statement, parameter);
  }

  @Override
  protected void deleteEntity(DbEntityOperation operation) {

    final DbEntity dbEntity = operation.getEntity();

    // get statement
    String deleteStatement = dbSqlSessionFactory.getDeleteStatement(dbEntity.getClass());
    ensureNotNull("no delete statement for " + dbEntity.getClass() + " in the ibatis mapping files", "deleteStatement", deleteStatement);

    LOG.executeDatabaseOperation("DELETE", dbEntity);

    // execute the delete
    executeDelete(deleteStatement, dbEntity);
  }

  @Override
  protected void executeSelectForUpdate(String statement, Object parameter) {
    executeSelectList(statement, parameter);
  }

}
