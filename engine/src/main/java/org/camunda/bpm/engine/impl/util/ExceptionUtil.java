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
    if(byteArray != null) {
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

  public static boolean checkValueTooLongException(ProcessEngineException exception) {
    List<SQLException> sqlExceptionList = findRelatedSqlExceptions(exception);
    for (SQLException ex: sqlExceptionList) {
      if (ex.getMessage().contains("too long")
        || ex.getMessage().contains("too large")
        || ex.getMessage().contains("TOO LARGE")
        || ex.getMessage().contains("ORA-01461")
        || ex.getMessage().contains("ORA-01401")
        || ex.getMessage().contains("data would be truncated")
        || ex.getMessage().contains("SQLCODE=-302, SQLSTATE=22001")) {
        return true;
      }
    }
    return false;
  }

  public static boolean checkConstraintViolationException(ProcessEngineException exception) {
    List<SQLException> sqlExceptionList = findRelatedSqlExceptions(exception);
    for (SQLException ex: sqlExceptionList) {
      if (ex.getMessage().contains("constraint")
        || ex.getMessage().contains("violat")
        || ex.getMessage().toLowerCase().contains("duplicate")
        || ex.getMessage().contains("ORA-00001")
        || ex.getMessage().contains("SQLCODE=-803, SQLSTATE=23505")) {
        return true;
      }
    }
    return false;
  }

  public static List<SQLException> findRelatedSqlExceptions(Throwable exception) {
    List<SQLException> sqlExceptionList = new ArrayList<SQLException>();
    Throwable cause = exception;
    do {
      if (cause instanceof SQLException) {
        SQLException sqlEx = (SQLException) cause;
        sqlExceptionList.add(sqlEx);
        while (sqlEx.getNextException() != null) {
          sqlExceptionList.add(sqlEx.getNextException());
          sqlEx = sqlEx.getNextException();
        }
      }
      cause = cause.getCause();
    } while (cause != null);
    return sqlExceptionList;
  }

  public static boolean checkForeignKeyConstraintViolation(Throwable cause) {

    List<SQLException> relatedSqlExceptions = findRelatedSqlExceptions(cause);
    for (SQLException exception : relatedSqlExceptions) {

      // PostgreSQL doesn't allow for a proper check
      if ("23503".equals(exception.getSQLState()) && exception.getErrorCode() == 0) {
        return false;
      } else if (
        // SqlServer
        (exception.getMessage().toLowerCase().contains("foreign key constraint")
          || ("23000".equals(exception.getSQLState()) && exception.getErrorCode() == 547))
        // MySql, MariaDB & PostgreSQL
        || (exception.getMessage().toLowerCase().contains("foreign key constraint")
          // MySql & MariaDB
          || ("23000".equals(exception.getSQLState()) && exception.getErrorCode() == 1452))
        // Oracle & H2
        || (exception.getMessage().toLowerCase().contains("integrity constraint")
          // Oracle
          || ("23000".equals(exception.getSQLState()) && exception.getErrorCode() == 2291)
          // H2
          || ("23506".equals(exception.getSQLState()) && exception.getErrorCode() == 23506))
        // DB2
        || (exception.getMessage().toLowerCase().contains("sqlstate=23503") && exception.getMessage().toLowerCase().contains("sqlcode=-530"))
        // DB2 zOS
        || ("23503".equals(exception.getSQLState()) && exception.getErrorCode() == -530)
        ) {

        return true;
      }
    }

    return false;
  }

  public static boolean checkVariableIntegrityViolation(Throwable cause) {

    List<SQLException> relatedSqlExceptions = findRelatedSqlExceptions(cause);
    for (SQLException exception : relatedSqlExceptions) {
      if (
        // MySQL & MariaDB
        (exception.getMessage().toLowerCase().contains("act_uniq_variable") && "23000".equals(exception.getSQLState()) && exception.getErrorCode() == 1062)
        // PostgreSQL
        || (exception.getMessage().toLowerCase().contains("act_uniq_variable") && "23505".equals(exception.getSQLState()) && exception.getErrorCode() == 0)
        // SqlServer
        || (exception.getMessage().toLowerCase().contains("act_uniq_variable") && "23000".equals(exception.getSQLState()) && exception.getErrorCode() == 2601)
        // Oracle
        || (exception.getMessage().toLowerCase().contains("act_uniq_variable") && "23000".equals(exception.getSQLState()) && exception.getErrorCode() == 1)
        // H2
        || (exception.getMessage().toLowerCase().contains("act_uniq_variable") && "23505".equals(exception.getSQLState()) && exception.getErrorCode() == 23505)
        ) {
        return true;
      }
    }

    return false;
  }

  public static Boolean checkCrdbTransactionRetryException(Throwable cause) {
    List<SQLException> relatedSqlExceptions = findRelatedSqlExceptions(cause);
    for (SQLException exception : relatedSqlExceptions) {
      String errorMessage = exception.getMessage().toLowerCase();
      int errorCode = exception.getErrorCode();
      if ((errorCode == 40001 || errorMessage != null)
          && (errorMessage.contains("restart transaction") || errorMessage.contains("retry txn"))
          // TX retry errors with RETRY_COMMIT_DEADLINE_EXCEEDED are handled
          // as a ProcessEngineException (cause: Process engine persistence exception)
          // due to a long-running transaction
          && !errorMessage.contains("retry_commit_deadline_exceeded")) {
        return true;
      }
    }

    return false;
  }

  public static BatchExecutorException findBatchExecutorException(Throwable exception) {
    Throwable cause = exception;
    do {
      if (cause instanceof BatchExecutorException) {
        return (BatchExecutorException) cause;
      }
      cause = cause.getCause();
    } while (cause != null);

    return null;
  }

  public static String collectExceptionMessages(Throwable cause) {
    String message = cause.getMessage();

    //collect real SQL exception messages in case of batch processing
    Throwable exCause = cause;
    do {
      if (exCause instanceof BatchExecutorException) {
        final List<SQLException> relatedSqlExceptions = ExceptionUtil.findRelatedSqlExceptions(exCause);
        StringBuilder sb = new StringBuilder();
        for (SQLException sqlException : relatedSqlExceptions) {
          sb.append(sqlException).append("\n");
        }
        message = message + "\n" + sb.toString();
      }
      exCause = exCause.getCause();
    } while (exCause != null);

    return message;
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
