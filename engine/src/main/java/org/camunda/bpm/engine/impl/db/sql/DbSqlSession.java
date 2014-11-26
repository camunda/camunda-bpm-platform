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

package org.camunda.bpm.engine.impl.db.sql;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.ibatis.session.SqlSession;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.WrongDbException;
import org.camunda.bpm.engine.impl.db.AbstractPersistenceSession;
import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.db.HasDbRevision;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.DbBulkOperation;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.DbEntityOperation;
import org.camunda.bpm.engine.impl.util.ClassNameUtil;
import org.camunda.bpm.engine.impl.util.IoUtil;
import org.camunda.bpm.engine.impl.util.ReflectUtil;


/**
 *
 * @author Tom Baeyens
 * @author Joram Barrez
 * @author Daniel Meyer
 * @author Sebastian Menski
 * @author Roman Smirnov
 *
 */
public class DbSqlSession extends AbstractPersistenceSession {

  private static Logger log = Logger.getLogger(DbSqlSession.class.getName());

  protected SqlSession sqlSession;
  protected DbSqlSessionFactory dbSqlSessionFactory;

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

  // select ////////////////////////////////////////////

  public List<?> selectList(String statement, Object parameter){
    statement = dbSqlSessionFactory.mapStatement(statement);
    return sqlSession.selectList(statement, parameter);
  }

  public <T extends DbEntity> T selectById(Class<T> type, String id) {
    String selectStatement = dbSqlSessionFactory.getSelectStatement(type);
    selectStatement = dbSqlSessionFactory.mapStatement(selectStatement);
    ensureNotNull("no select statement for " + type + " in the ibatis mapping files", "selectStatement", selectStatement);

    return (T) sqlSession.selectOne(selectStatement, id);
  }

  public Object selectOne(String statement, Object parameter) {
    statement = dbSqlSessionFactory.mapStatement(statement);
    return sqlSession.selectOne(statement, parameter);
  }

  // lock ////////////////////////////////////////////

  public void lock(String statement) {
    // do not perform locking if H2 database is used. H2 uses table level locks
    // by default which may cause deadlocks if the deploy command needs to get a new
    // Id using the DbIdGenerator while performing a deployment.
    if (!"h2".equals(dbSqlSessionFactory.getDatabaseType())) {
      String mappedStatement = dbSqlSessionFactory.mapStatement(statement);
      sqlSession.update(mappedStatement);
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

    // set revision of our copy to 1
    if (parameter instanceof HasDbRevision) {
      HasDbRevision versionedObject = (HasDbRevision) parameter;
      versionedObject.setRevision(1);
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
    int nrOfRowsDeleted = executeDelete(deleteStatement, dbEntity);

    // It only makes sense to check for optimistic locking exceptions for objects that actually have a revision
    if (dbEntity instanceof HasDbRevision && nrOfRowsDeleted == 0) {
      operation.setFailed(true);
      return;
    }

    // perform post delete action
    entityDeleted(dbEntity);
  }

  protected int executeDelete(String deleteStatement, Object parameter) {
    // map the statement
    deleteStatement = dbSqlSessionFactory.mapStatement(deleteStatement);
    return sqlSession.delete(deleteStatement, parameter);
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
      log.fine("updating: " + toString(dbEntity));
    }

    // execute update
    int numOfRowsUpdated = executeUpdate(updateStatement, dbEntity);

    if (dbEntity instanceof HasDbRevision) {
      if(numOfRowsUpdated != 1) {
        // failed with optimistic locking
        operation.setFailed(true);
        return;
      } else {
        // increment revision of our copy
        HasDbRevision versionedObject = (HasDbRevision) dbEntity;
        versionedObject.setRevision(versionedObject.getRevisionNext());
      }
    }

    // perform post update action
    entityUpdated(dbEntity);
  }

  protected int executeUpdate(String updateStatement, Object parameter) {
    updateStatement = dbSqlSessionFactory.mapStatement(updateStatement);
    return sqlSession.update(updateStatement, parameter);
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

  // flush ////////////////////////////////////////////////////////////////////

  public void flush() {
    // nothing to do
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
      if (dbSqlSessionFactory.isCmmnEnabled() && !isCmmnTablePresent()) {
        errorMessage = addMissingComponent(errorMessage, "case.engine");
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

  protected void dbSchemaCreateIdentity() {
    executeMandatorySchemaResource("create", "identity");
  }

  protected void dbSchemaCreateHistory() {
    executeMandatorySchemaResource("create", "history");
  }

  protected void dbSchemaCreateEngine() {
    executeMandatorySchemaResource("create", "engine");
  }

  protected void dbSchemaCreateCmmn() {
    executeMandatorySchemaResource("create", "case.engine");
  }

  protected void dbSchemaCreateCmmnHistory() {
    executeMandatorySchemaResource("create", "case.history");
  }

  protected void dbSchemaDropIdentity() {
    executeMandatorySchemaResource("drop", "identity");
  }

  protected void dbSchemaDropHistory() {
    executeMandatorySchemaResource("drop", "history");
  }

  protected void dbSchemaDropEngine() {
    executeMandatorySchemaResource("drop", "engine");
  }

  protected void dbSchemaDropCmmn() {
    executeMandatorySchemaResource("drop", "case.engine");
  }

  protected void dbSchemaDropCmmnHistory() {
    executeMandatorySchemaResource("drop", "case.history");
  }


  public void executeMandatorySchemaResource(String operation, String component) {
    executeSchemaResource(operation, component, getResourceForDbOperation(operation, operation, component), false);
  }

  public static String[] JDBC_METADATA_TABLE_TYPES = {"TABLE"};

  public boolean isEngineTablePresent(){
    return isTablePresent("ACT_RU_EXECUTION");
  }
  public boolean isHistoryTablePresent(){
    return isTablePresent("ACT_HI_PROCINST");
  }
  public boolean isIdentityTablePresent(){
    return isTablePresent("ACT_ID_USER");
  }

  public boolean isCmmnTablePresent() {
    return isTablePresent("ACT_RE_CASE_DEF");
  }

  public boolean isCmmnHistoryTablePresent() {
    return isTablePresent("ACT_HI_CASEINST");
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

  // getters and setters //////////////////////////////////////////////////////

  public SqlSession getSqlSession() {
    return sqlSession;
  }
  public DbSqlSessionFactory getDbSqlSessionFactory() {
    return dbSqlSessionFactory;
  }

}
