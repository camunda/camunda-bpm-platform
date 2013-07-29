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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.ibatis.session.SqlSessionFactory;
import org.camunda.bpm.engine.impl.cfg.IdGenerator;
import org.camunda.bpm.engine.impl.interceptor.Session;
import org.camunda.bpm.engine.impl.interceptor.SessionFactory;
import org.camunda.bpm.engine.impl.util.ClassNameUtil;


/**
 * @author Tom Baeyens
 */
public class DbSqlSessionFactory implements SessionFactory {

  protected static final Map<String, Map<String, String>> databaseSpecificStatements = new HashMap<String, Map<String,String>>();
  
  public static final Map<String, String> databaseSpecificLimitBeforeStatements = new HashMap<String, String>();
  public static final Map<String, String> databaseSpecificLimitAfterStatements = new HashMap<String, String>();
  public static final Map<String, String> databaseSpecificLimitBetweenStatements = new HashMap<String, String>();
  public static final Map<String, String> databaseSpecificOrderByStatements = new HashMap<String, String>();
  public static final Map<String, String> databaseSpecificLimitBeforeNativeQueryStatements = new HashMap<String, String>();
  
  public static final Map<String, String> databaseSpecificBitAnd1 = new HashMap<String, String>();
  public static final Map<String, String> databaseSpecificBitAnd2 = new HashMap<String, String>();
  public static final Map<String, String> databaseSpecificBitAnd3 = new HashMap<String, String>();
  
  public static final Map<String, String> databaseSpecificDummyTable = new HashMap<String, String>();
  
  static {
    
    String defaultOrderBy = " order by ${orderBy} ";
    
    // h2
    databaseSpecificLimitBeforeStatements.put("h2", "");
    databaseSpecificLimitAfterStatements.put("h2", "LIMIT #{maxResults} OFFSET #{firstResult}");
    databaseSpecificLimitBetweenStatements.put("h2", "");
    databaseSpecificOrderByStatements.put("h2", defaultOrderBy);
    databaseSpecificLimitBeforeNativeQueryStatements.put("h2", "");
    databaseSpecificBitAnd1.put("h2", "BITAND(");
    databaseSpecificBitAnd2.put("h2", ",");
    databaseSpecificBitAnd3.put("h2", ")");    
    databaseSpecificDummyTable.put("h2", "");
    
	  //mysql specific
    databaseSpecificLimitBeforeStatements.put("mysql", "");
    databaseSpecificLimitAfterStatements.put("mysql", "LIMIT #{maxResults} OFFSET #{firstResult}");
    databaseSpecificLimitBetweenStatements.put("mysql", "");
    databaseSpecificOrderByStatements.put("mysql", defaultOrderBy);
    databaseSpecificLimitBeforeNativeQueryStatements.put("mysql", "");
    databaseSpecificBitAnd1.put("mysql", "");
    databaseSpecificBitAnd2.put("mysql", " & ");
    databaseSpecificBitAnd3.put("mysql", "");
    databaseSpecificDummyTable.put("mysql", "");
    addDatabaseSpecificStatement("mysql", "selectNextJobsToExecute", "selectNextJobsToExecute_mysql");
    addDatabaseSpecificStatement("mysql", "selectExclusiveJobsToExecute", "selectExclusiveJobsToExecute_mysql");
    addDatabaseSpecificStatement("mysql", "selectProcessDefinitionsByQueryCriteria", "selectProcessDefinitionsByQueryCriteria_mysql");
    addDatabaseSpecificStatement("mysql", "selectProcessDefinitionCountByQueryCriteria", "selectProcessDefinitionCountByQueryCriteria_mysql");
    addDatabaseSpecificStatement("mysql", "selectDeploymentsByQueryCriteria", "selectDeploymentsByQueryCriteria_mysql");
    addDatabaseSpecificStatement("mysql", "selectDeploymentCountByQueryCriteria", "selectDeploymentCountByQueryCriteria_mysql");
    
    
    //postgres specific
    databaseSpecificLimitBeforeStatements.put("postgres", "");
    databaseSpecificLimitAfterStatements.put("postgres", "LIMIT #{maxResults} OFFSET #{firstResult}");
    databaseSpecificLimitBetweenStatements.put("postgres", "");
    databaseSpecificOrderByStatements.put("postgres", defaultOrderBy);
    databaseSpecificLimitBeforeNativeQueryStatements.put("postgres", "");
    databaseSpecificBitAnd1.put("postgres", "");
    databaseSpecificBitAnd2.put("postgres", " & ");
    databaseSpecificBitAnd3.put("postgres", "");
    databaseSpecificDummyTable.put("postgres", "");
    addDatabaseSpecificStatement("postgres", "insertByteArray", "insertByteArray_postgres");
    addDatabaseSpecificStatement("postgres", "updateByteArray", "updateByteArray_postgres");
    addDatabaseSpecificStatement("postgres", "selectByteArray", "selectByteArray_postgres");
    addDatabaseSpecificStatement("postgres", "selectResourceByDeploymentIdAndResourceName", "selectResourceByDeploymentIdAndResourceName_postgres");
    addDatabaseSpecificStatement("postgres", "selectResourcesByDeploymentId", "selectResourcesByDeploymentId_postgres");
    addDatabaseSpecificStatement("postgres", "selectHistoricDetailsByQueryCriteria", "selectHistoricDetailsByQueryCriteria_postgres");
    addDatabaseSpecificStatement("postgres", "insertIdentityInfo", "insertIdentityInfo_postgres");
    addDatabaseSpecificStatement("postgres", "updateIdentityInfo", "updateIdentityInfo_postgres");
    addDatabaseSpecificStatement("postgres", "selectIdentityInfoById", "selectIdentityInfoById_postgres");
    addDatabaseSpecificStatement("postgres", "selectIdentityInfoByUserIdAndKey", "selectIdentityInfoByUserIdAndKey_postgres");
    addDatabaseSpecificStatement("postgres", "selectIdentityInfoByUserId", "selectIdentityInfoByUserId_postgres");
    addDatabaseSpecificStatement("postgres", "selectIdentityInfoDetails", "selectIdentityInfoDetails_postgres");
    addDatabaseSpecificStatement("postgres", "insertComment", "insertComment_postgres");
    addDatabaseSpecificStatement("postgres", "selectCommentsByTaskId", "selectCommentsByTaskId_postgres");
    addDatabaseSpecificStatement("postgres", "selectCommentsByProcessInstanceId", "selectCommentsByProcessInstanceId_postgres");
    addDatabaseSpecificStatement("postgres", "selectEventsByTaskId", "selectEventsByTaskId_postgres");
    addDatabaseSpecificStatement("postgres", "selectActivityStatistics", "selectActivityStatistics_postgres");
    addDatabaseSpecificStatement("postgres", "selectActivityStatisticsCount", "selectActivityStatisticsCount_postgres");
        
    // oracle
    databaseSpecificLimitBeforeStatements.put("oracle", "select * from ( select a.*, ROWNUM rnum from (");
    databaseSpecificLimitAfterStatements.put("oracle", "  ) a where ROWNUM < #{lastRow}) where rnum  >= #{firstRow}");
    databaseSpecificLimitBetweenStatements.put("oracle", "");
    databaseSpecificOrderByStatements.put("oracle", defaultOrderBy);
    databaseSpecificLimitBeforeNativeQueryStatements.put("oracle", "");
    databaseSpecificDummyTable.put("oracle", "FROM DUAL");
    databaseSpecificBitAnd1.put("oracle", "BITAND(");
    databaseSpecificBitAnd2.put("oracle", ",");
    databaseSpecificBitAnd3.put("oracle", ")");  
    addDatabaseSpecificStatement("oracle", "selectExclusiveJobsToExecute", "selectExclusiveJobsToExecute_integerBoolean");
    
    // db2
    databaseSpecificLimitBeforeStatements.put("db2", "SELECT SUB.* FROM (");
    databaseSpecificLimitAfterStatements.put("db2", ")RES ) SUB WHERE SUB.rnk >= #{firstRow} AND SUB.rnk < #{lastRow}");
    databaseSpecificLimitBetweenStatements.put("db2", ", row_number() over (ORDER BY ${orderBy}) rnk FROM ( select distinct RES.* ");
    databaseSpecificOrderByStatements.put("db2", "");
    databaseSpecificLimitBeforeNativeQueryStatements.put("db2", "SELECT SUB.* FROM ( select RES.* , row_number() over (ORDER BY ${orderBy}) rnk FROM (");
    databaseSpecificBitAnd1.put("db2", "BITAND(");
    databaseSpecificBitAnd2.put("db2", ",");
    databaseSpecificBitAnd3.put("db2", ")");  
    databaseSpecificDummyTable.put("db2", "FROM SYSIBM.SYSDUMMY1");
    addDatabaseSpecificStatement("db2", "selectExclusiveJobsToExecute", "selectExclusiveJobsToExecute_integerBoolean");
    addDatabaseSpecificStatement("db2", "selectExecutionByNativeQuery", "selectExecutionByNativeQuery_mssql_or_db2");
    addDatabaseSpecificStatement("db2", "selectHistoricActivityInstanceByNativeQuery", "selectHistoricActivityInstanceByNativeQuery_mssql_or_db2");
    addDatabaseSpecificStatement("db2", "selectHistoricProcessInstanceByNativeQuery", "selectHistoricProcessInstanceByNativeQuery_mssql_or_db2");
    addDatabaseSpecificStatement("db2", "selectHistoricTaskInstanceByNativeQuery", "selectHistoricTaskInstanceByNativeQuery_mssql_or_db2");
    addDatabaseSpecificStatement("db2", "selectTaskByNativeQuery", "selectTaskByNativeQuery_mssql_or_db2");
    
    // mssql
    databaseSpecificLimitBeforeStatements.put("mssql", "SELECT SUB.* FROM (");
    databaseSpecificLimitAfterStatements.put("mssql", ")RES ) SUB WHERE SUB.rnk >= #{firstRow} AND SUB.rnk < #{lastRow}");
    databaseSpecificLimitBetweenStatements.put("mssql", ", row_number() over (ORDER BY ${orderBy}) rnk FROM ( select distinct RES.* ");
    databaseSpecificOrderByStatements.put("mssql", "");
    databaseSpecificLimitBeforeNativeQueryStatements.put("mssql", "SELECT SUB.* FROM ( select RES.* , row_number() over (ORDER BY ${orderBy}) rnk FROM (");
    databaseSpecificBitAnd1.put("mssql", "");
    databaseSpecificBitAnd2.put("mssql", " &");
    databaseSpecificBitAnd3.put("mssql", "");  
    databaseSpecificDummyTable.put("mssql", "");
    addDatabaseSpecificStatement("mssql", "selectExclusiveJobsToExecute", "selectExclusiveJobsToExecute_integerBoolean");
    addDatabaseSpecificStatement("mssql", "selectExecutionByNativeQuery", "selectExecutionByNativeQuery_mssql_or_db2");
    addDatabaseSpecificStatement("mssql", "selectHistoricActivityInstanceByNativeQuery", "selectHistoricActivityInstanceByNativeQuery_mssql_or_db2");
    addDatabaseSpecificStatement("mssql", "selectHistoricProcessInstanceByNativeQuery", "selectHistoricProcessInstanceByNativeQuery_mssql_or_db2");
    addDatabaseSpecificStatement("mssql", "selectHistoricTaskInstanceByNativeQuery", "selectHistoricTaskInstanceByNativeQuery_mssql_or_db2");
    addDatabaseSpecificStatement("mssql", "selectTaskByNativeQuery", "selectTaskByNativeQuery_mssql_or_db2");
  }
  
  protected String databaseType;
  protected String databaseTablePrefix = "";
  /**
   * In some situations you want to set the schema to use for table checks /
   * generation if the database metadata doesn't return that correctly, see
   * https://jira.codehaus.org/browse/ACT-1220,
   * https://jira.codehaus.org/browse/ACT-1062
   */
  protected String databaseSchema;
  protected SqlSessionFactory sqlSessionFactory;
  protected IdGenerator idGenerator;
  protected Map<String, String> statementMappings;
  protected Map<Class<?>,String>  insertStatements = new ConcurrentHashMap<Class<?>, String>();
  protected Map<Class<?>,String>  updateStatements = new ConcurrentHashMap<Class<?>, String>();
  protected Map<Class<?>,String>  deleteStatements = new ConcurrentHashMap<Class<?>, String>();
  protected Map<Class<?>,String>  selectStatements = new ConcurrentHashMap<Class<?>, String>();
  protected boolean isDbIdentityUsed = true;
  protected boolean isDbHistoryUsed = true;

  public Class< ? > getSessionType() {
    return DbSqlSession.class;
  }

  public Session openSession() {
    return new DbSqlSession(this);
  }
  
  // insert, update and delete statements /////////////////////////////////////
  
  public String getInsertStatement(PersistentObject object) {
    return getStatement(object.getClass(), insertStatements, "insert");
  }

  public String getUpdateStatement(PersistentObject object) {
    return getStatement(object.getClass(), updateStatements, "update");
  }

  public String getDeleteStatement(Class<?> persistentObjectClass) {
    return getStatement(persistentObjectClass, deleteStatements, "delete");
  }

  public String getSelectStatement(Class<?> persistentObjectClass) {
    return getStatement(persistentObjectClass, selectStatements, "select");
  }

  private String getStatement(Class<?> persistentObjectClass, Map<Class<?>,String> cachedStatements, String prefix) {
    String statement = cachedStatements.get(persistentObjectClass);
    if (statement!=null) {
      return statement;
    }
    statement = prefix+ClassNameUtil.getClassNameWithoutPackage(persistentObjectClass);
    statement = statement.substring(0, statement.length()-6);
    cachedStatements.put(persistentObjectClass, statement);
    return statement;
  }

  // db specific mappings /////////////////////////////////////////////////////
  
  protected static void addDatabaseSpecificStatement(String databaseType, String activitiStatement, String ibatisStatement) {
    Map<String, String> specificStatements = databaseSpecificStatements.get(databaseType);
    if (specificStatements == null) {
      specificStatements = new HashMap<String, String>();
      databaseSpecificStatements.put(databaseType, specificStatements);
    }
    specificStatements.put(activitiStatement, ibatisStatement);
  }
  
  public String mapStatement(String statement) {
    if (statementMappings==null) {
      return statement;
    }
    String mappedStatement = statementMappings.get(statement);
    return (mappedStatement!=null ? mappedStatement : statement);
  }
  
  // customized getters and setters ///////////////////////////////////////////
  
  public void setDatabaseType(String databaseType) {
    this.databaseType = databaseType;
    this.statementMappings = databaseSpecificStatements.get(databaseType);
  }

  // getters and setters //////////////////////////////////////////////////////
  
  public SqlSessionFactory getSqlSessionFactory() {
    return sqlSessionFactory;
  }
  
  public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
    this.sqlSessionFactory = sqlSessionFactory;
  }
  
  public IdGenerator getIdGenerator() {
    return idGenerator;
  }
  
  public void setIdGenerator(IdGenerator idGenerator) {
    this.idGenerator = idGenerator;
  }

  
  public String getDatabaseType() {
    return databaseType;
  }

  
  public Map<String, String> getStatementMappings() {
    return statementMappings;
  }

  
  public void setStatementMappings(Map<String, String> statementMappings) {
    this.statementMappings = statementMappings;
  }

  
  public Map<Class< ? >, String> getInsertStatements() {
    return insertStatements;
  }

  
  public void setInsertStatements(Map<Class< ? >, String> insertStatements) {
    this.insertStatements = insertStatements;
  }

  
  public Map<Class< ? >, String> getUpdateStatements() {
    return updateStatements;
  }

  
  public void setUpdateStatements(Map<Class< ? >, String> updateStatements) {
    this.updateStatements = updateStatements;
  }

  
  public Map<Class< ? >, String> getDeleteStatements() {
    return deleteStatements;
  }

  
  public void setDeleteStatements(Map<Class< ? >, String> deleteStatements) {
    this.deleteStatements = deleteStatements;
  }

  
  public Map<Class< ? >, String> getSelectStatements() {
    return selectStatements;
  }

  
  public void setSelectStatements(Map<Class< ? >, String> selectStatements) {
    this.selectStatements = selectStatements;
  }

  public boolean isDbIdentityUsed() {
    return isDbIdentityUsed;
  }
  
  public void setDbIdentityUsed(boolean isDbIdentityUsed) {
    this.isDbIdentityUsed = isDbIdentityUsed;
  }
  
  public boolean isDbHistoryUsed() {
    return isDbHistoryUsed;
  }
  
  public void setDbHistoryUsed(boolean isDbHistoryUsed) {
    this.isDbHistoryUsed = isDbHistoryUsed;
  }

  public void setDatabaseTablePrefix(String databaseTablePrefix) {
    this.databaseTablePrefix = databaseTablePrefix;
  }
    
  public String getDatabaseTablePrefix() {
    return databaseTablePrefix;
  }
  
  public String getDatabaseSchema() {
    return databaseSchema;
  }
  
  public void setDatabaseSchema(String databaseSchema) {
    this.databaseSchema = databaseSchema;
  }

}
