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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.executor.BatchResult;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.AbstractPersistenceSession;
import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.db.EnginePersistenceLogger;
import org.camunda.bpm.engine.impl.db.HasDbRevision;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.DbBulkOperation;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.DbEntityOperation;
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

  protected static final EnginePersistenceLogger LOG = ProcessEngineLogger.PERSISTENCE_LOGGER;

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

  @Override
  public List<BatchResult> flushOperations() {
    return sqlSession.flushStatements();
  }

  // select ////////////////////////////////////////////

  public List<?> selectList(String statement, Object parameter){
    statement = dbSqlSessionFactory.mapStatement(statement);
    List<Object> resultList = sqlSession.selectList(statement, parameter);
    for (Object object : resultList) {
      fireEntityLoaded(object);
    }
    return resultList;
  }

  @SuppressWarnings("unchecked")
  public <T extends DbEntity> T selectById(Class<T> type, String id) {
    String selectStatement = dbSqlSessionFactory.getSelectStatement(type);
    selectStatement = dbSqlSessionFactory.mapStatement(selectStatement);
    ensureNotNull("no select statement for " + type + " in the ibatis mapping files", "selectStatement", selectStatement);

    Object result = sqlSession.selectOne(selectStatement, id);
    fireEntityLoaded(result);
    return (T) result;
  }

  public Object selectOne(String statement, Object parameter) {
    statement = dbSqlSessionFactory.mapStatement(statement);
    Object result = sqlSession.selectOne(statement, parameter);
    fireEntityLoaded(result);
    return result;
  }

  // lock ////////////////////////////////////////////

  public void lock(String statement, Object parameter) {
    // do not perform locking if H2 database is used. H2 uses table level locks
    // by default which may cause deadlocks if the deploy command needs to get a new
    // Id using the DbIdGenerator while performing a deployment.
    if (!DbSqlSessionFactory.H2.equals(dbSqlSessionFactory.getDatabaseType())) {
      String mappedStatement = dbSqlSessionFactory.mapStatement(statement);
      if (!Context.getProcessEngineConfiguration().isJdbcBatchProcessing()) {
        sqlSession.update(mappedStatement, parameter);
      } else {
        sqlSession.selectList(mappedStatement, parameter);
      }
    }
  }

  // insert //////////////////////////////////////////

  @Override
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
    LOG.executeDatabaseOperation("INSERT", parameter);
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

  @Override
  protected void deleteEntity(DbEntityOperation operation) {

    final DbEntity dbEntity = operation.getEntity();

    // get statement
    String deleteStatement = dbSqlSessionFactory.getDeleteStatement(dbEntity.getClass());
    ensureNotNull("no delete statement for " + dbEntity.getClass() + " in the ibatis mapping files", "deleteStatement", deleteStatement);

    LOG.executeDatabaseOperation("DELETE", dbEntity);

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

  @Override
  protected void deleteBulk(DbBulkOperation operation) {
    String statement = operation.getStatement();
    Object parameter = operation.getParameter();

    LOG.executeDatabaseBulkOperation("DELETE", statement, parameter);

    executeDelete(statement, parameter);
  }

  // update ////////////////////////////////////////

  @Override
  protected void updateEntity(DbEntityOperation operation) {

    final DbEntity dbEntity = operation.getEntity();

    String updateStatement = dbSqlSessionFactory.getUpdateStatement(dbEntity);
    ensureNotNull("no update statement for " + dbEntity.getClass() + " in the ibatis mapping files", "updateStatement", updateStatement);

    LOG.executeDatabaseOperation("UPDATE", dbEntity);

    if (Context.getProcessEngineConfiguration().isJdbcBatchProcessing()) {
      // execute update
      executeUpdate(updateStatement, dbEntity);
    } else {
      // execute update
      int numOfRowsUpdated = executeUpdate(updateStatement, dbEntity);

      if (dbEntity instanceof HasDbRevision) {
        if (numOfRowsUpdated != 1) {
          // failed with optimistic locking
          operation.setFailed(true);
          return;
        } else {
          // increment revision of our copy
          HasDbRevision versionedObject = (HasDbRevision) dbEntity;
          versionedObject.setRevision(versionedObject.getRevisionNext());
        }
      }
    }

    // perform post update action
    entityUpdated(dbEntity);
  }

  @Override
  public int executeUpdate(String updateStatement, Object parameter) {
    updateStatement = dbSqlSessionFactory.mapStatement(updateStatement);
    return sqlSession.update(updateStatement, parameter);
  }

  @Override
  public int executeNonEmptyUpdateStmt(String updateStmt, Object parameter) {
    updateStmt = dbSqlSessionFactory.mapStatement(updateStmt);

    //if mapped statement is empty, which can happens for some databases, we have no need to execute it
    MappedStatement mappedStatement = sqlSession.getConfiguration().getMappedStatement(updateStmt);
    if (mappedStatement.getBoundSql(parameter).getSql().isEmpty())
      return 0;

    return sqlSession.update(updateStmt, parameter);
  }

  protected void entityUpdated(final DbEntity entity) {
    // nothing to do
  }

  @Override
  protected void updateBulk(DbBulkOperation operation) {
    String statement = operation.getStatement();
    Object parameter = operation.getParameter();

    LOG.executeDatabaseBulkOperation("UPDATE", statement, parameter);

    executeUpdate(statement, parameter);
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
        throw LOG.wrongDbVersionException(ProcessEngine.VERSION, dbVersion);
      }

      List<String> missingComponents = new ArrayList<String>();
      if (!isEngineTablePresent()) {
        missingComponents.add("engine");
      }
      if (dbSqlSessionFactory.isDbHistoryUsed() && !isHistoryTablePresent()) {
        missingComponents.add("history");
      }
      if (dbSqlSessionFactory.isDbIdentityUsed() && !isIdentityTablePresent()) {
        missingComponents.add("identity");
      }
      if (dbSqlSessionFactory.isCmmnEnabled() && !isCmmnTablePresent()) {
        missingComponents.add("case.engine");
      }
      if (dbSqlSessionFactory.isDmnEnabled() && !isDmnTablePresent()) {
        missingComponents.add("decision.engine");
      }

      if (!missingComponents.isEmpty()) {
        throw LOG.missingTableException(missingComponents);
      }

    } catch (Exception e) {
      if (isMissingTablesException(e)) {
        throw LOG.missingActivitiTablesException();
      } else {
        if (e instanceof RuntimeException) {
          throw (RuntimeException) e;
        } else {
          throw LOG.unableToFetchDbSchemaVersion(e);
        }
      }
    }
  }

  @Override
  protected String getDbVersion() {
    String selectSchemaVersionStatement = dbSqlSessionFactory.mapStatement("selectDbSchemaVersion");
    return (String) sqlSession.selectOne(selectSchemaVersionStatement);
  }

  @Override
  protected void dbSchemaCreateIdentity() {
    executeMandatorySchemaResource("create", "identity");
  }

  @Override
  protected void dbSchemaCreateHistory() {
    executeMandatorySchemaResource("create", "history");
  }

  @Override
  protected void dbSchemaCreateEngine() {
    executeMandatorySchemaResource("create", "engine");
  }

  @Override
  protected void dbSchemaCreateCmmn() {
    executeMandatorySchemaResource("create", "case.engine");
  }

  @Override
  protected void dbSchemaCreateCmmnHistory() {
    executeMandatorySchemaResource("create", "case.history");
  }

  @Override
  protected void dbSchemaCreateDmn() {
    executeMandatorySchemaResource("create", "decision.engine");
  }


  @Override
  protected void dbSchemaCreateDmnHistory() {
    executeMandatorySchemaResource("create", "decision.history");
  }

  @Override
  protected void dbSchemaDropIdentity() {
    executeMandatorySchemaResource("drop", "identity");
  }

  @Override
  protected void dbSchemaDropHistory() {
    executeMandatorySchemaResource("drop", "history");
  }

  @Override
  protected void dbSchemaDropEngine() {
    executeMandatorySchemaResource("drop", "engine");
  }

  @Override
  protected void dbSchemaDropCmmn() {
    executeMandatorySchemaResource("drop", "case.engine");
  }

  @Override
  protected void dbSchemaDropCmmnHistory() {
    executeMandatorySchemaResource("drop", "case.history");
  }

  @Override
  protected void dbSchemaDropDmn() {
    executeMandatorySchemaResource("drop", "decision.engine");
  }

  @Override
  protected void dbSchemaDropDmnHistory() {
    executeMandatorySchemaResource("drop", "decision.history");
  }

  public void executeMandatorySchemaResource(String operation, String component) {
    executeSchemaResource(operation, component, getResourceForDbOperation(operation, operation, component), false);
  }

  public static String[] JDBC_METADATA_TABLE_TYPES = {"TABLE"};

  @Override
  public boolean isEngineTablePresent(){
    return isTablePresent("ACT_RU_EXECUTION");
  }
  @Override
  public boolean isHistoryTablePresent(){
    return isTablePresent("ACT_HI_PROCINST");
  }
  @Override
  public boolean isIdentityTablePresent(){
    return isTablePresent("ACT_ID_USER");
  }

  @Override
  public boolean isCmmnTablePresent() {
    return isTablePresent("ACT_RE_CASE_DEF");
  }

  @Override
  public boolean isCmmnHistoryTablePresent() {
    return isTablePresent("ACT_HI_CASEINST");
  }

  @Override
  public boolean isDmnTablePresent() {
    return isTablePresent("ACT_RE_DECISION_DEF");
  }

  @Override
  public boolean isDmnHistoryTablePresent() {
    return isTablePresent("ACT_HI_DECINST");
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

      if (DbSqlSessionFactory.POSTGRES.equals(databaseType)) {
        tableName = tableName.toLowerCase();
      }

      try {
        tables = databaseMetaData.getTables(this.connectionMetadataDefaultCatalog, schema, tableName, JDBC_METADATA_TABLE_TYPES);
        return tables.next();
      } finally {
        tables.close();
      }

    } catch (Exception e) {
      throw LOG.checkDatabaseTableException(e);
    }
  }

  @Override
  public List<String> getTableNamesPresent() {
    List<String> tableNames = new ArrayList<String>();

    try {
      ResultSet tablesRs = null;

      try {
        if (DbSqlSessionFactory.ORACLE.equals(getDbSqlSessionFactory().getDatabaseType())) {
          tableNames = getTablesPresentInOracleDatabase();
        } else {
          Connection connection = getSqlSession().getConnection();

          String databaseTablePrefix = getDbSqlSessionFactory().getDatabaseTablePrefix();
          String schema = getDbSqlSessionFactory().getDatabaseSchema();
          String tableNameFilter = prependDatabaseTablePrefix("ACT_%");

          // for postgres we have to use lower case
          if (DbSqlSessionFactory.POSTGRES.equals(getDbSqlSessionFactory().getDatabaseType())) {
            schema = schema == null ? schema : schema.toLowerCase();
            tableNameFilter = tableNameFilter.toLowerCase();
          }

          DatabaseMetaData databaseMetaData = connection.getMetaData();
          tablesRs = databaseMetaData.getTables(null, schema, tableNameFilter, DbSqlSession.JDBC_METADATA_TABLE_TYPES);
          while (tablesRs.next()) {
            String tableName = tablesRs.getString("TABLE_NAME");
            if (!databaseTablePrefix.isEmpty()) {
              tableName = databaseTablePrefix + tableName;
            }
            tableName = tableName.toUpperCase();
            tableNames.add(tableName);
          }
          LOG.fetchDatabaseTables("jdbc metadata", tableNames);
        }
      } catch (SQLException se) {
        throw se;
      } finally {
        if (tablesRs != null) {
          tablesRs.close();
        }
      }
    } catch (Exception e) {
      throw LOG.getDatabaseTableNameException(e);
    }

    return tableNames;
  }

  protected List<String> getTablesPresentInOracleDatabase() throws SQLException {
    List<String> tableNames = new ArrayList<String>();
    Connection connection = null;
    PreparedStatement prepStat = null;
    ResultSet tablesRs = null;
    String selectTableNamesFromOracle = "SELECT table_name FROM all_tables WHERE table_name LIKE ?";
    String databaseTablePrefix = getDbSqlSessionFactory().getDatabaseTablePrefix();

    try {
      connection = Context.getProcessEngineConfiguration().getDataSource().getConnection();
      prepStat = connection.prepareStatement(selectTableNamesFromOracle);
      prepStat.setString(1, databaseTablePrefix + "ACT_%");

      tablesRs = prepStat.executeQuery();
      while (tablesRs.next()) {
        String tableName = tablesRs.getString("TABLE_NAME");
        tableName = tableName.toUpperCase();
        tableNames.add(tableName);
      }
      LOG.fetchDatabaseTables("oracle all_tables", tableNames);

    } finally {
      if (tablesRs != null) {
        tablesRs.close();
      }
      if (prepStat != null) {
        prepStat.close();
      }
      if (connection != null) {
        connection.close();
      }
    }

    return tableNames;
  }


  public String prependDatabaseTablePrefix(String tableName) {
    String prefixWithoutSchema = dbSqlSessionFactory.getDatabaseTablePrefix();
    String schema = dbSqlSessionFactory.getDatabaseSchema();
    if (prefixWithoutSchema == null) {
      return tableName;
    }
    if (schema == null) {
      return prefixWithoutSchema + tableName;
    }

    if (prefixWithoutSchema.startsWith(schema + ".")) {
      prefixWithoutSchema = prefixWithoutSchema.substring(schema.length() + 1);
    }

    return prefixWithoutSchema + tableName;
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
          LOG.missingSchemaResource(resourceName, operation);
        } else {
          throw LOG.missingSchemaResourceException(resourceName, operation);
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
      throw LOG.missingSchemaResourceFileException(schemaFileResourceName, e);
    } finally {
      IoUtil.closeSilently(inputStream);
    }
  }

  private void executeSchemaResource(String operation, String component, String resourceName, InputStream inputStream) {
    String sqlStatement = null;
    String exceptionSqlStatement = null;
    try {
      Connection connection = sqlSession.getConnection();
      Exception exception = null;
      byte[] bytes = IoUtil.readInputStream(inputStream, resourceName);
      String ddlStatements = new String(bytes);
      BufferedReader reader = new BufferedReader(new StringReader(ddlStatements));
      String line = readNextTrimmedLine(reader);

      List<String> logLines = new ArrayList<String>();

      while (line != null) {
        if (line.startsWith("# ")) {
          logLines.add(line.substring(2));
        } else if (line.startsWith("-- ")) {
          logLines.add(line.substring(3));
        } else if (line.length()>0) {

          if (line.endsWith(";")) {
            sqlStatement = addSqlStatementPiece(sqlStatement, line.substring(0, line.length()-1));
            Statement jdbcStatement = connection.createStatement();
            try {
              // no logging needed as the connection will log it
              logLines.add(sqlStatement);
              jdbcStatement.execute(sqlStatement);
              jdbcStatement.close();
            } catch (Exception e) {
              if (exception == null) {
                exception = e;
                exceptionSqlStatement = sqlStatement;
              }
              LOG.failedDatabaseOperation(operation, sqlStatement, e);
            } finally {
              sqlStatement = null;
            }
          } else {
            sqlStatement = addSqlStatementPiece(sqlStatement, line);
          }
        }

        line = readNextTrimmedLine(reader);
      }
      LOG.performingDatabaseOperation(operation, component, resourceName);
      LOG.executingDDL(logLines);

      if (exception != null) {
        throw exception;
      }

      LOG.successfulDatabaseOperation(operation, component);
    } catch (Exception e) {
      throw LOG.performDatabaseOperationException(operation, exceptionSqlStatement, e);
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
