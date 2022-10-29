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
import java.util.function.Supplier;

import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.executor.BatchExecutorException;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.persistence.entity.ByteArrayEntity;
import org.camunda.bpm.engine.repository.ResourceType;

import static org.camunda.bpm.engine.impl.util.ExceptionUtil.DEADLOCK_CODES.CRDB;
import static org.camunda.bpm.engine.impl.util.ExceptionUtil.DEADLOCK_CODES.DB2;
import static org.camunda.bpm.engine.impl.util.ExceptionUtil.DEADLOCK_CODES.H2;
import static org.camunda.bpm.engine.impl.util.ExceptionUtil.DEADLOCK_CODES.MARIADB_MYSQL;
import static org.camunda.bpm.engine.impl.util.ExceptionUtil.DEADLOCK_CODES.ORACLE;
import static org.camunda.bpm.engine.impl.util.ExceptionUtil.DEADLOCK_CODES.POSTGRES;
import static org.camunda.bpm.engine.impl.util.ExceptionUtil.DEADLOCK_CODES.MSSQL;

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
   *
   * used in Jobs and ExternalTasks
   *
   * @param name - type\source of the exception
   * @param byteArray - payload of the exception
   * @param type - resource type of the exception
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

  protected static Throwable getPersistenceCauseException(PersistenceException persistenceException) {
    Throwable cause = persistenceException.getCause();
    if (cause instanceof BatchExecutorException) {
      return cause.getCause();

    } else {
      return persistenceException.getCause();

    }
  }

  public static SQLException unwrapException(PersistenceException persistenceException) {
    Throwable cause = getPersistenceCauseException(persistenceException);
    if (cause instanceof SQLException) {
      SQLException sqlException = (SQLException) cause;
      SQLException nextException = sqlException.getNextException();
      if (nextException != null) {
        return nextException;

      } else {
        return sqlException;

      }

    } else {
      return null;
    }
  }

  public static SQLException unwrapException(ProcessEngineException genericPersistenceException) {
    Throwable cause = genericPersistenceException.getCause();

    if (cause instanceof ProcessEngineException) {
      ProcessEngineException processEngineException = (ProcessEngineException) cause;

      Throwable processEngineExceptionCause = processEngineException.getCause();
      if (processEngineExceptionCause instanceof PersistenceException) {
        return unwrapException((PersistenceException) processEngineExceptionCause);

      } else {
        return null;

      }
    } else if (cause instanceof PersistenceException) {
      return unwrapException((PersistenceException) cause);

    } else {
      return null;

    }
  }

  public static boolean checkValueTooLongException(SQLException sqlException) {
    String message = sqlException.getMessage();
    return message.contains("too long") ||
        message.contains("too large") ||
        message.contains("TOO LARGE") ||
        message.contains("ORA-01461") ||
        message.contains("ORA-01401") ||
        message.contains("data would be truncated") ||
        message.contains("SQLCODE=-302, SQLSTATE=22001");
  }

  public static boolean checkValueTooLongException(ProcessEngineException genericPersistenceException) {
    SQLException sqlException = unwrapException(genericPersistenceException);

    if (sqlException == null) {
      return false;
    }

    return ExceptionUtil.checkValueTooLongException(sqlException);
  }

  public static boolean checkConstraintViolationException(ProcessEngineException genericPersistenceException) {
    SQLException sqlException = unwrapException(genericPersistenceException);

    if (sqlException == null) {
      return false;
    }

    String message = sqlException.getMessage();
    return message.contains("constraint") ||
        message.contains("violat") ||
        message.toLowerCase().contains("duplicate") ||
        message.contains("ORA-00001") ||
        message.contains("SQLCODE=-803, SQLSTATE=23505");
  }

  public static boolean checkForeignKeyConstraintViolation(PersistenceException persistenceException, boolean skipPostgres) {
    SQLException sqlException = unwrapException(persistenceException);

    if (sqlException == null) {
      return false;
    }

    return checkForeignKeyConstraintViolation(sqlException, skipPostgres);
  }

  public static boolean checkForeignKeyConstraintViolation(SQLException sqlException, boolean skipPostgres) {
    String message = sqlException.getMessage().toLowerCase();
    String sqlState = sqlException.getSQLState();
    int errorCode = sqlException.getErrorCode();

    // PostgreSQL doesn't allow for a proper check
    if ("23503".equals(sqlState) && errorCode == 0) {
      return !skipPostgres;
    } else {
      // SqlServer
      return message.contains("foreign key constraint") ||
          "23000".equals(sqlState) && errorCode == 547 ||
          // MySql & MariaDB & PostgreSQL
          "23000".equals(sqlState) && errorCode == 1452 ||
          // Oracle & H2
          message.contains("integrity constraint") ||
          // Oracle
          "23000".equals(sqlState) && errorCode == 2291 ||
          // H2
          "23506".equals(sqlState) && errorCode == 23506 ||
          // DB2
          "23503".equals(sqlState) && errorCode == -530 ||
          "23504".equals(sqlState) && errorCode == -532;
    }
  }

  public static boolean checkVariableIntegrityViolation(PersistenceException persistenceException) {
    SQLException sqlException = unwrapException(persistenceException);

    if (sqlException == null) {
      return false;
    }

    String message = sqlException.getMessage().toLowerCase();
    String sqlState = sqlException.getSQLState().toUpperCase();
    int errorCode = sqlException.getErrorCode();

    // MySQL & MariaDB
    return (message.contains("act_uniq_variable") && "23000".equals(sqlState) && errorCode == 1062)
        // PostgreSQL
        || (message.contains("act_uniq_variable") && "23505".equals(sqlState) && errorCode == 0)
        // SqlServer
        || (message.contains("act_uniq_variable") && "23000".equals(sqlState) && errorCode == 2601)
        // Oracle
        || (message.contains("act_uniq_variable") && "23000".equals(sqlState) && errorCode == 1)
        // H2
        || (message.contains("act_uniq_variable") && "23505".equals(sqlState) && errorCode == 23505);
  }

  public static boolean checkCrdbTransactionRetryException(Throwable exception) {
    SQLException sqlException = null;

    if (exception instanceof PersistenceException) {
      sqlException = unwrapException((PersistenceException) exception);

    } else if (exception instanceof ProcessEngineException) {
      sqlException = unwrapException((ProcessEngineException) exception);

    } else {
      return false;

    }

    if (sqlException == null) {
      return false;
    }

    return checkCrdbTransactionRetryException(sqlException);
  }

  public static boolean checkCrdbTransactionRetryException(SQLException sqlException) {
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

  public enum DEADLOCK_CODES {

    MARIADB_MYSQL(1213, "40001"),
    MSSQL(1205, "40001"),
    DB2(-911, "40001"),
    ORACLE(60, "61000"),
    POSTGRES(0, "40P01"),
    CRDB(0, "40001"),
    H2(40001, "40001");

    protected final int errorCode;
    protected final String sqlState;

    DEADLOCK_CODES(int errorCode, String sqlState) {
      this.errorCode = errorCode;
      this.sqlState = sqlState;
    }

    public int getErrorCode() {
      return errorCode;
    }

    public String getSqlState() {
      return sqlState;
    }

    protected boolean equals(int errorCode, String sqlState) {
      return this.getErrorCode() == errorCode && this.getSqlState().equals(sqlState);
    }

  }

  public static boolean checkDeadlockException(SQLException sqlException) {
    String sqlState = sqlException.getSQLState();
    if (sqlState != null) {
      sqlState = sqlState.toUpperCase();
    } else {
      return false;
    }
    
    int errorCode = sqlException.getErrorCode();

    return MARIADB_MYSQL.equals(errorCode, sqlState) ||
        MSSQL.equals(errorCode, sqlState) ||
        DB2.equals(errorCode, sqlState) ||
        ORACLE.equals(errorCode, sqlState) ||
        POSTGRES.equals(errorCode, sqlState) ||
        CRDB.equals(errorCode, sqlState) ||
        H2.equals(errorCode, sqlState);
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
   * @param <T> is the type of the return value
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
