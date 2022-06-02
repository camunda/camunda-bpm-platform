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
package org.camunda.bpm.engine.impl.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.executor.BatchExecutorException;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.persistence.entity.ByteArrayEntity;
import org.camunda.bpm.engine.repository.ResourceType;

/**
 * @author Roman Smirnov
 * @author Askar Akhmerov
 */
public class ExceptionUtil {

  public static final String PERSISTENCE_EXCEPTION_MESSAGE = "An exception occurred in the " +
      "persistence layer. Please check the server logs for a detailed message and the entire " +
      "exception stack trace.";

  public static String getExceptionStacktrace(Throwable exception) {
    StringWriter stringWriter = new StringWriter();
    exception.printStackTrace(new PrintWriter(stringWriter));
    return stringWriter.toString();
  }

  public static String getExceptionStacktrace(ByteArrayEntity byteArray) {
    String result = null;
    if (byteArray != null) {
      result = StringUtil.fromBytes(byteArray.getBytes());
    }
    return result;
  }

  public static ByteArrayEntity createJobExceptionByteArray(byte[] byteArray, ResourceType type) {
    return createExceptionByteArray("job.exceptionByteArray", byteArray, type);
  }

  /**
   * create ByteArrayEntity with specified name and payload and make sure it's
   * persisted
   * <p>
   * used in Jobs and ExternalTasks
   *
   * @param name      - type\source of the exception
   * @param byteArray - payload of the exception
   * @param type      - resource type of the exception
   * @return persisted entity
   */
  public static ByteArrayEntity createExceptionByteArray(String name, byte[] byteArray, ResourceType type) {
    ByteArrayEntity result = null;

    if (byteArray != null) {
      result = new ByteArrayEntity(name, byteArray, type);
      Context.getCommandContext()
        .getByteArrayManager()
        .insertByteArray(result);
    }

    return result;
  }

  protected static SQLException getSqlException(PersistenceException persistenceException) {
    Throwable cause = persistenceException.getCause();
    if (cause instanceof BatchExecutorException) {
      return (SQLException) cause.getCause();

    } else {
      return (SQLException) persistenceException.getCause();

    }
  }

  public static boolean checkValueTooLongException(ProcessEngineException genericPersistenceException) {
    Throwable cause = genericPersistenceException.getCause();
    if (cause instanceof ProcessEngineException) {
      ProcessEngineException processEngineException = (ProcessEngineException) cause;
      PersistenceException persistenceException = (PersistenceException) processEngineException.getCause();
      SQLException sqlException = getSqlException(persistenceException);
      return sqlException.getMessage().contains("too long") || sqlException.getMessage().contains("too large") || sqlException.getMessage().contains("TOO LARGE") || sqlException.getMessage().contains("ORA-01461")
          || sqlException.getMessage().contains("ORA-01401") || sqlException.getMessage().contains("data would be truncated") || sqlException.getMessage().contains("SQLCODE=-302, SQLSTATE=22001");

    } else {
      return false;
    }
  }

  public static boolean checkConstraintViolationException(ProcessEngineException genericPersistenceException) {
    ProcessEngineException processEngineException = (ProcessEngineException) genericPersistenceException.getCause();
    if (processEngineException != null) {
      PersistenceException persistenceException = (PersistenceException) processEngineException.getCause();
      SQLException sqlException = getSqlException(persistenceException);
      return sqlException.getMessage().contains("constraint") || sqlException.getMessage().contains("violat")
          || sqlException.getMessage().toLowerCase().contains("duplicate") || sqlException.getMessage()
          .contains("ORA-00001") || sqlException.getMessage().contains("SQLCODE=-803, SQLSTATE=23505");

    } else {
      return false;

    }
  }

  public static boolean checkForeignKeyConstraintViolation(PersistenceException persistenceException) {
    SQLException sqlException = getSqlException(persistenceException);

    // PostgreSQL doesn't allow for a proper check
    if ("23503".equals(sqlException.getSQLState()) && sqlException.getErrorCode() == 0) {
      return false;
    } else {
      // SqlServer
      return sqlException.getMessage().toLowerCase().contains("foreign key constraint")
          || "23000".equals(sqlException.getSQLState()) && sqlException.getErrorCode() == 547
          // MySql, MariaDB & PostgreSQL
          || sqlException.getMessage().toLowerCase().contains("foreign key constraint")
          // MySql & MariaDB
          || "23000".equals(sqlException.getSQLState()) && sqlException.getErrorCode() == 1452
          // Oracle & H2
          || sqlException.getMessage().toLowerCase().contains("integrity constraint")
          // Oracle
          || "23000".equals(sqlException.getSQLState()) && sqlException.getErrorCode() == 2291
          // H2
          || "23506".equals(sqlException.getSQLState()) && sqlException.getErrorCode() == 23506
          // DB2
          || sqlException.getMessage().toLowerCase().contains("sqlstate=23503") && sqlException.getMessage()
          .toLowerCase()
          .contains("sqlcode=-530")
          // DB2 zOS
          || "23503".equals(sqlException.getSQLState()) && sqlException.getErrorCode() == -530;
    }
  }

  public static boolean checkVariableIntegrityViolation(PersistenceException persistenceException) {
    SQLException sqlException = getSqlException(persistenceException);

    // MySQL & MariaDB
    return (sqlException.getMessage().toLowerCase().contains("act_uniq_variable") && "23000".equals(
        sqlException.getSQLState()) && sqlException.getErrorCode() == 1062)
        // PostgreSQL
        || (sqlException.getMessage().toLowerCase().contains("act_uniq_variable") && "23505".equals(
        sqlException.getSQLState()) && sqlException.getErrorCode() == 0)
        // SqlServer
        || (sqlException.getMessage().toLowerCase().contains("act_uniq_variable") && "23000".equals(
        sqlException.getSQLState()) && sqlException.getErrorCode() == 2601)
        // Oracle
        || (sqlException.getMessage().toLowerCase().contains("act_uniq_variable") && "23000".equals(
        sqlException.getSQLState()) && sqlException.getErrorCode() == 1)
        // H2
        || (sqlException.getMessage().toLowerCase().contains("act_uniq_variable") && "23505".equals(
        sqlException.getSQLState()) && sqlException.getErrorCode() == 23505);
  }

  public static Boolean checkCrdbTransactionRetryException(PersistenceException persistenceException) {
    SQLException sqlException = getSqlException(persistenceException);
    return checkCrdbTransactionRetryException(sqlException);
  }

  public static Boolean checkCrdbTransactionRetryException(SQLException sqlException) {
    String errorMessage = sqlException.getMessage();
    int errorCode = sqlException.getErrorCode();
    if ((errorCode == 40001 || errorMessage != null)) {
      errorMessage = errorMessage.toLowerCase();
      return (errorMessage.contains("restart transaction") || errorMessage.contains("retry txn"))
          // TX retry errors with RETRY_COMMIT_DEADLINE_EXCEEDED are handled
          // as a ProcessEngineException (cause: Process engine persistence exception)
          // due to a long-running transaction
          && !errorMessage.contains("retry_commit_deadline_exceeded");
    }
    return false;
  }

  public static BatchExecutorException findBatchExecutorException(PersistenceException exception) {
    Throwable cause = exception;
    do {
      if (cause instanceof BatchExecutorException) {
        return (BatchExecutorException) cause;
      }
      cause = cause.getCause();
    } while (cause != null);

    return null;
  }

  /**
   * Pass logic, which directly calls MyBatis API. In case a MyBatis exception is thrown, it is
   * wrapped into a {@link ProcessEngineException} and never propagated directly to an Engine API
   * call. In some cases, the top-level exception and its message are shown as a response body in
   * the REST API. Wrapping all MyBatis API calls in our codebase makes sure that the top-level
   * exception is always a {@link ProcessEngineException} with a generic message. Like this, SQL
   * details are never disclosed to potential attackers.
   *
   * @param supplier which calls MyBatis API
   * @param <T>      is the type of the return value
   * @return the value returned by the supplier
   * @throws ProcessEngineException which wraps the actual exception
   */
  public static <T> T doWithExceptionWrapper(Supplier<T> supplier) {
    try {
      return supplier.get();

    } catch (Exception ex) {
      throw wrapPersistenceException(ex);

    }
  }

  public static ProcessEngineException wrapPersistenceException(Exception ex) {
    return new ProcessEngineException(PERSISTENCE_EXCEPTION_MESSAGE, ex);
  }

}
