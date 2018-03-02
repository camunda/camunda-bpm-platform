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
package org.camunda.bpm.engine.impl.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.executor.BatchExecutorException;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.persistence.entity.ByteArrayEntity;

/**
 * @author Roman Smirnov
 * @author Askar Akhmerov
 */
public class ExceptionUtil {

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

  public static ByteArrayEntity createJobExceptionByteArray(byte[] byteArray) {
    return createExceptionByteArray("job.exceptionByteArray", byteArray);
  }

  /**
   * create ByteArrayEntity with specified name and payload and make sure it's
   * persisted
   *
   * used in Jobs and ExternalTasks
   *
   * @param name - type\source of the exception
   * @param byteArray - payload of the exception
   * @return persisted entity
   */
  public static ByteArrayEntity createExceptionByteArray(String name, byte[] byteArray) {
    ByteArrayEntity result = null;

    if (byteArray != null) {
      result = new ByteArrayEntity(name, byteArray);
      Context
          .getCommandContext()
          .getDbEntityManager()
          .insert(result);
    }

    return result;
  }

  public static boolean checkValueTooLongException(ProcessEngineException exception) {
    List<SQLException> sqlExceptionList = findRelatedSqlExceptions(exception);
    for (SQLException ex: sqlExceptionList) {
      if (ex.getMessage().contains("too long")
        || ex.getMessage().contains("too large")
        || ex.getMessage().contains("ORA-01461")
        || ex.getMessage().contains("ORA-01401")
        || ex.getMessage().contains("data would be truncated")
        || ex.getMessage().contains("SQLCODE=-302, SQLSTATE=22001")) {
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
        || (exception.getMessage().toLowerCase().contains("act_uniq_variable_index_c") && "23505".equals(exception.getSQLState()) && exception.getErrorCode() == 23505)
        ) {
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
}
