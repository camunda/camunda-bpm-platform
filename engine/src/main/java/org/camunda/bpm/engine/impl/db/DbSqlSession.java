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

package org.camunda.bpm.engine.impl.db;

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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.ibatis.session.SqlSession;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.WrongDbException;
import org.camunda.bpm.engine.impl.DeploymentQueryImpl;
import org.camunda.bpm.engine.impl.ExecutionQueryImpl;
import org.camunda.bpm.engine.impl.GroupQueryImpl;
import org.camunda.bpm.engine.impl.HistoricActivityInstanceQueryImpl;
import org.camunda.bpm.engine.impl.HistoricDetailQueryImpl;
import org.camunda.bpm.engine.impl.HistoricProcessInstanceQueryImpl;
import org.camunda.bpm.engine.impl.HistoricTaskInstanceQueryImpl;
import org.camunda.bpm.engine.impl.HistoricVariableInstanceQueryImpl;
import org.camunda.bpm.engine.impl.JobQueryImpl;
import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.ProcessDefinitionQueryImpl;
import org.camunda.bpm.engine.impl.ProcessInstanceQueryImpl;
import org.camunda.bpm.engine.impl.TaskQueryImpl;
import org.camunda.bpm.engine.impl.UserQueryImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cmmn.entity.repository.CaseDefinitionQueryImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.entitymanager.DbEntityManager;
import org.camunda.bpm.engine.impl.db.entitymanager.cache.CachedDbEntity;
import org.camunda.bpm.engine.impl.identity.db.DbGroupQueryImpl;
import org.camunda.bpm.engine.impl.identity.db.DbUserQueryImpl;
import org.camunda.bpm.engine.impl.interceptor.Session;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyEntity;
import org.camunda.bpm.engine.impl.persistence.entity.VariableInstanceEntity;
import org.camunda.bpm.engine.impl.util.ClassNameUtil;
import org.camunda.bpm.engine.impl.util.IoUtil;
import org.camunda.bpm.engine.impl.util.ReflectUtil;
import org.camunda.bpm.engine.impl.variable.DeserializedObject;


/** responsibilities:
 *   - delayed flushing of inserts updates and deletes
 *   - optional dirty checking
 *   - db specific statement name mapping
 *
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class DbSqlSession implements Session {

  private static Logger log = Logger.getLogger(DbSqlSession.class.getName());

  protected SqlSession sqlSession;
  protected DbSqlSessionFactory dbSqlSessionFactory;

//  protected List<DbEntity> insertedObjects = new ArrayList<DbEntity>();
//  protected List<DbEntity> updatedObjects = new ArrayList<DbEntity>();
//  protected Map<Class<?>, Map<String, CachedObject>> cachedObjects = new HashMap<Class<?>, Map<String,CachedObject>>();
//  protected List<BulkUpdateOperation> bulkUpdates = new ArrayList<BulkUpdateOperation>();
//  protected List<DeleteOperation> deleteOperations = new ArrayList<DeleteOperation>();

  protected List<DeserializedObject> deserializedObjects = new ArrayList<DeserializedObject>();
  protected String connectionMetadataDefaultCatalog = null;
  protected String connectionMetadataDefaultSchema = null;

  protected DbEntityManager entityManager;

  public DbSqlSession(DbSqlSessionFactory dbSqlSessionFactory) {
    this.dbSqlSessionFactory = dbSqlSessionFactory;
    this.sqlSession = dbSqlSessionFactory
      .getSqlSessionFactory()
      .openSession();
    entityManager = new DbEntityManager(dbSqlSessionFactory, sqlSession);
  }

  public DbSqlSession(DbSqlSessionFactory dbSqlSessionFactory, Connection connection, String catalog, String schema) {
    this.dbSqlSessionFactory = dbSqlSessionFactory;
    this.sqlSession = dbSqlSessionFactory
      .getSqlSessionFactory()
      .openSession(connection);
    this.connectionMetadataDefaultCatalog = catalog;
    this.connectionMetadataDefaultSchema = schema;
  }

  // insert ///////////////////////////////////////////////////////////////////

  public void insert(DbEntity dbEntity) {
    entityManager.insert(dbEntity);
  }

  // update ///////////////////////////////////////////////////////////////////

  public void update(DbEntity dbEntity) {
    entityManager.update(dbEntity);
  }

  public void update(Class<? extends DbEntity> entityType, String statement, Object parameter) {
    entityManager.update(entityType, statement, parameter);
  }

  // delete ///////////////////////////////////////////////////////////////////

  public void delete(Class<? extends DbEntity> entityType, String statement, Object parameter) {
    entityManager.delete(entityType, statement, parameter);
  }


  public void delete(DbEntity dbEntity) {
    entityManager.delete(dbEntity);
  }

  // select ///////////////////////////////////////////////////////////////////

  @SuppressWarnings("unchecked")
  public List selectList(String statement) {
    return selectList(statement, null, 0, Integer.MAX_VALUE);
  }

  @SuppressWarnings("unchecked")
  public List selectList(String statement, Object parameter) {
    return selectList(statement, parameter, 0, Integer.MAX_VALUE);
  }

  @SuppressWarnings("unchecked")
  public List selectList(String statement, Object parameter, Page page) {
    if(page!=null) {
      return selectList(statement, parameter, page.getFirstResult(), page.getMaxResults());
    }else {
      return selectList(statement, parameter, 0, Integer.MAX_VALUE);
    }
  }

  @SuppressWarnings("unchecked")
  public List selectList(String statement, ListQueryParameterObject parameter, Page page) {
    return selectList(statement, parameter);
  }

  @SuppressWarnings("unchecked")
  public List selectList(String statement, Object parameter, int firstResult, int maxResults) {
    return selectList(statement, new ListQueryParameterObject(parameter, firstResult, maxResults));
  }

  @SuppressWarnings("unchecked")
  public List selectList(String statement, ListQueryParameterObject parameter) {
    return selectListWithRawParameter(statement, parameter, parameter.getFirstResult(), parameter.getMaxResults());
  }

  @SuppressWarnings("unchecked")
  public List selectListWithRawParameter(String statement, Object parameter, int firstResult, int maxResults) {
    statement = dbSqlSessionFactory.mapStatement(statement);
    if(firstResult == -1 ||  maxResults==-1) {
      return Collections.EMPTY_LIST;
    }
    List loadedObjects = sqlSession.selectList(statement, parameter);
    return filterLoadedObjects(loadedObjects);
  }

  public Object selectOne(String statement, Object parameter) {
    statement = dbSqlSessionFactory.mapStatement(statement);
    Object result = sqlSession.selectOne(statement, parameter);
    if (result instanceof DbEntity) {
      DbEntity loadedObject = (DbEntity) result;
      result = cacheFilter(loadedObject);
    }
    return result;
  }

  public boolean selectBoolean(String statement, Object parameter) {
    statement = dbSqlSessionFactory.mapStatement(statement);
    List<String> result = sqlSession.selectList(statement, parameter);
    if(result != null) {
      return result.contains(1);
    }
    return false;

  }

  @SuppressWarnings("unchecked")
  public <T extends DbEntity> T selectById(Class<T> entityClass, String id) {
    T persistentObject = findInCache(entityClass, id);
    if (persistentObject!=null) {
      return persistentObject;
    }
    String selectStatement = dbSqlSessionFactory.getSelectStatement(entityClass);
    selectStatement = dbSqlSessionFactory.mapStatement(selectStatement);
    persistentObject = (T) sqlSession.selectOne(selectStatement, id);
    if (persistentObject==null) {
      return null;
    }
    entityManager.putPersistent(persistentObject);
    return persistentObject;
  }

  // internal session cache ///////////////////////////////////////////////////

  public <T extends DbEntity> List<T> findInCache(Class<T> entityClass) {
    List<T> result = new ArrayList<T>();

    Iterator<CachedDbEntity> iterator = entityManager.cachedEntitiesIterator();
    while (iterator.hasNext()) {
      CachedDbEntity cachedDbEntity = (CachedDbEntity) iterator.next();
      if(entityClass.equals(cachedDbEntity.getEntity().getClass())) {
        result.add((T) cachedDbEntity.getEntity());
      }
    }

    return result;
  }

  public <T extends DbEntity> T findInCache(Class<T> entityClass, String id) {
    return entityManager.get(entityClass, id);
  }

  protected List filterLoadedObjects(List<Object> loadedObjects) {
    if (loadedObjects.isEmpty()) {
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
   * then the original object is returned.  if this is the first time
   * this object is loaded, then the loadedObject is added to the cache. */
  protected DbEntity cacheFilter(DbEntity persistentObject) {
    DbEntity cachedPersistentObject = findInCache(persistentObject.getClass(), persistentObject.getId());
    if (cachedPersistentObject!=null) {
      return cachedPersistentObject;
    }
    entityManager.putPersistent(persistentObject);
    return persistentObject;
  }

  // deserialized objects /////////////////////////////////////////////////////

  public void addDeserializedObject(Object deserializedObject, byte[] serializedBytes, VariableInstanceEntity variableInstanceEntity) {
    deserializedObjects.add(new DeserializedObject(deserializedObject, serializedBytes, variableInstanceEntity));
  }

  // flush ////////////////////////////////////////////////////////////////////

  public void flush() {
    flushDeserializedObjects();
    entityManager.flush();
  }

  protected void flushDeserializedObjects() {
    for (DeserializedObject deserializedObject: deserializedObjects) {
      deserializedObject.flush();
    }
  }

  public boolean isUpdated(DbEntity dbEntity) {
    CachedDbEntity cachedEntity = entityManager.getCachedEntity(dbEntity);
    return cachedEntity.isDirty();
  }

//  public <T extends PersistentObject> List<T> pruneDeletedEntities(List<T> listToPrune) {
//    ArrayList<T> prunedList = new ArrayList<T>(listToPrune);
//    for (T potentiallyDeleted : listToPrune) {
//      for (DeleteOperation deleteOperation: deleteOperations) {
//        if (deleteOperation instanceof DeleteById) {
//          DeleteById deleteById = (DeleteById) deleteOperation;
//          if ( potentiallyDeleted.getClass().equals(deleteById.persistenceObjectClass)
//               && potentiallyDeleted.getId().equals(deleteById.persistentObjectId)
//             ) {
//            prunedList.remove(potentiallyDeleted);
//          }
//        }
//      }
//    }
//    return prunedList;
//  }

  public <T extends DbEntity> List<T> pruneDeletedEntities(List<T> listToPrune) {
    ArrayList<T> prunedList = new ArrayList<T>(listToPrune);
    for (T potentiallyDeleted : listToPrune) {
      if(!entityManager.isDeleted(potentiallyDeleted)) {
        prunedList.add(potentiallyDeleted);
      }
    }
    return prunedList;
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

  protected String toString(DbEntity dbEntity) {
    if (dbEntity==null) {
      return "null";
    }
    return ClassNameUtil.getClassNameWithoutPackage(dbEntity)+"["+dbEntity.getId()+"]";
  }

  public void lock(String statement) {
    String mappedStatement = dbSqlSessionFactory.mapStatement(statement);
    sqlSession.update(mappedStatement);
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
      if (dbSqlSessionFactory.isCmmnEnabled() && !isCaseDefinitionTablePresent()) {
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

  public void dbCreateHistoryLevel() {
    ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();
    int configuredHistoryLevel = processEngineConfiguration.getHistoryLevel();
    PropertyEntity property = new PropertyEntity("historyLevel", Integer.toString(configuredHistoryLevel));
    insert(property);
    log.info("Creating historyLevel property in database with value: " + processEngineConfiguration.getHistory());
  }

  public void checkHistoryLevel() {
    Integer configuredHistoryLevel = Context.getProcessEngineConfiguration().getHistoryLevel();
    PropertyEntity historyLevelProperty = selectById(PropertyEntity.class, "historyLevel");
    if (historyLevelProperty == null) {
      log.info("No historyLevel property found in database.");
      dbCreateHistoryLevel();
    } else {
      Integer databaseHistoryLevel = new Integer(historyLevelProperty.getValue());
      if (!configuredHistoryLevel.equals(databaseHistoryLevel)) {
        throw new ProcessEngineException("historyLevel mismatch: configuration says " + configuredHistoryLevel + " and database says " + databaseHistoryLevel);
      }
    }
  }

  public void checkDeploymentLockExists() {
    PropertyEntity deploymentLockProperty = selectById(PropertyEntity.class, "deployment.lock");
    if (deploymentLockProperty == null) {
      log.warning("No deployment lock property found in database.");
    }
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

  public void dbSchemaCreate() {
    ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();

    int configuredHistoryLevel = processEngineConfiguration.getHistoryLevel();
    if ( (!processEngineConfiguration.isDbHistoryUsed())
         && (configuredHistoryLevel>ProcessEngineConfigurationImpl.HISTORYLEVEL_NONE)
       ) {
      throw new ProcessEngineException("historyLevel config is higher then 'none' and dbHistoryUsed is set to false");
    }

    if (isEngineTablePresent()) {
      String dbVersion = getDbVersion();
      if (!ProcessEngine.VERSION.equals(dbVersion)) {
        throw new WrongDbException(ProcessEngine.VERSION, dbVersion);
      }
    } else {
      dbSchemaCreateEngine();
    }

    if (processEngineConfiguration.isDbHistoryUsed()) {
      dbSchemaCreateHistory();
    }

    if (processEngineConfiguration.isDbIdentityUsed()) {
      dbSchemaCreateIdentity();
    }

    if (processEngineConfiguration.isCmmnEnabled()) {
      dbSchemaCreateCmmn();
    }
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

  public void dbSchemaDrop() {
    if (dbSqlSessionFactory.isCmmnEnabled()) {
      executeMandatorySchemaResource("drop", "case.engine");
    }

    executeMandatorySchemaResource("drop", "engine");

    if (dbSqlSessionFactory.isDbHistoryUsed()) {
      executeMandatorySchemaResource("drop", "history");
    }
    if (dbSqlSessionFactory.isDbIdentityUsed()) {
      executeMandatorySchemaResource("drop", "identity");
    }
  }

  public void dbSchemaPrune() {
    if (isHistoryTablePresent() && !dbSqlSessionFactory.isDbHistoryUsed()) {
      executeMandatorySchemaResource("drop", "history");
    }
    if (isIdentityTablePresent() && dbSqlSessionFactory.isDbIdentityUsed()) {
      executeMandatorySchemaResource("drop", "identity");
    }
    if (isCaseDefinitionTablePresent() && dbSqlSessionFactory.isCmmnEnabled()) {
      executeMandatorySchemaResource("drop", "case.engine");
    }
  }

  public void executeMandatorySchemaResource(String operation, String component) {
    executeSchemaResource(operation, component, getResourceForDbOperation(operation, operation, component), false);
  }

  public static String[] JDBC_METADATA_TABLE_TYPES = {"TABLE"};

  public String dbSchemaUpdate() {
    String feedback = null;
    String dbVersion = null;
    boolean isUpgradeNeeded = false;

    if (isEngineTablePresent()) {
      // the next piece assumes both DB version and library versions are formatted 5.x
      PropertyEntity dbVersionProperty = selectById(PropertyEntity.class, "schema.version");
      dbVersion = dbVersionProperty.getValue();
      isUpgradeNeeded = !ProcessEngine.VERSION.equals(dbVersion);

      if (isUpgradeNeeded) {
        dbVersionProperty.setValue(ProcessEngine.VERSION);

        PropertyEntity dbHistoryProperty;
        if ("5.0".equals(dbVersion)) {
          dbHistoryProperty = new PropertyEntity("schema.history", "create(5.0)");
          insert(dbHistoryProperty);
        } else {
          dbHistoryProperty = selectById(PropertyEntity.class, "schema.history");
        }

        String dbHistoryValue = dbHistoryProperty.getValue()+" upgrade("+dbVersion+"->"+ProcessEngine.VERSION+")";
        dbHistoryProperty.setValue(dbHistoryValue);

        dbSchemaUpgrade("engine", dbVersion);

        feedback = "upgraded Activiti from "+dbVersion+" to "+ProcessEngine.VERSION;
      }
    } else {
      dbSchemaCreateEngine();
    }

    if (isHistoryTablePresent()) {
      if (isUpgradeNeeded) {
        dbSchemaUpgrade("history", dbVersion);
      }
    } else if (dbSqlSessionFactory.isDbHistoryUsed()) {
      dbSchemaCreateHistory();
    }

    if (isIdentityTablePresent()) {
      if (isUpgradeNeeded) {
        dbSchemaUpgrade("identity", dbVersion);
      }
    } else if (dbSqlSessionFactory.isDbIdentityUsed()) {
      dbSchemaCreateIdentity();
    }

    if (isCaseDefinitionTablePresent()) {
      if (isUpgradeNeeded) {
        dbSchemaUpgrade("case.engine", dbVersion);
      }
    } else if (dbSqlSessionFactory.isCmmnEnabled()) {
      dbSchemaCreateCmmn();
    }

    return feedback;
  }

  public boolean isEngineTablePresent(){
    return isTablePresent("ACT_RU_EXECUTION");
  }
  public boolean isHistoryTablePresent(){
    return isTablePresent("ACT_HI_PROCINST");
  }
  public boolean isIdentityTablePresent(){
    return isTablePresent("ACT_ID_USER");
  }

  public boolean isCaseDefinitionTablePresent() {
    return isTablePresent("ACT_RE_CASE_DEF");
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

  protected void dbSchemaUpgrade(String component, String dbVersion) {
    log.info("upgrading activiti "+component+" schema from "+dbVersion+" to "+ProcessEngine.VERSION);

    if (dbVersion.endsWith("-SNAPSHOT")) {
      dbVersion = dbVersion.substring(0, dbVersion.length()-"-SNAPSHOT".length());
    }
    int minorDbVersionNumber = Integer.parseInt(dbVersion.substring(2));

    String libraryVersion = ProcessEngine.VERSION;
    if (ProcessEngine.VERSION.endsWith("-SNAPSHOT")) {
      libraryVersion = ProcessEngine.VERSION.substring(0, ProcessEngine.VERSION.length()-"-SNAPSHOT".length());
    }
    int minorLibraryVersionNumber = Integer.parseInt(libraryVersion.substring(2));

    while (minorDbVersionNumber<minorLibraryVersionNumber) {
      executeSchemaResource("upgrade", component, getResourceForDbOperation("upgrade", "upgradestep.5"+minorDbVersionNumber+".to.5"+(minorDbVersionNumber+1), component), true);
      minorDbVersionNumber++;
    }
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

  public void performSchemaOperationsProcessEngineBuild() {
    String databaseSchemaUpdate = Context.getProcessEngineConfiguration().getDatabaseSchemaUpdate();
    if (ProcessEngineConfigurationImpl.DB_SCHEMA_UPDATE_DROP_CREATE.equals(databaseSchemaUpdate)) {
      try {
        dbSchemaDrop();
      } catch (RuntimeException e) {
        // ignore
      }
    }
    if ( ProcessEngineConfiguration.DB_SCHEMA_UPDATE_CREATE_DROP.equals(databaseSchemaUpdate)
         || ProcessEngineConfigurationImpl.DB_SCHEMA_UPDATE_DROP_CREATE.equals(databaseSchemaUpdate)
         || ProcessEngineConfigurationImpl.DB_SCHEMA_UPDATE_CREATE.equals(databaseSchemaUpdate)
       ) {
      dbSchemaCreate();
    } else if (ProcessEngineConfiguration.DB_SCHEMA_UPDATE_FALSE.equals(databaseSchemaUpdate)) {
      dbSchemaCheckVersion();
    } else if (ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE.equals(databaseSchemaUpdate)) {
      dbSchemaUpdate();
    }

    checkHistoryLevel();
    checkDeploymentLockExists();
  }

  public void performSchemaOperationsProcessEngineClose() {
    String databaseSchemaUpdate = Context.getProcessEngineConfiguration().getDatabaseSchemaUpdate();
    if (ProcessEngineConfiguration.DB_SCHEMA_UPDATE_CREATE_DROP.equals(databaseSchemaUpdate)) {
      dbSchemaDrop();
    }
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
  public UserQueryImpl createUserQuery() {
    return new DbUserQueryImpl();
  }
  public GroupQueryImpl createGroupQuery() {
    return new DbGroupQueryImpl();
  }

  // getters and setters //////////////////////////////////////////////////////

  public SqlSession getSqlSession() {
    return sqlSession;
  }
  public DbSqlSessionFactory getDbSqlSessionFactory() {
    return dbSqlSessionFactory;
  }

}
