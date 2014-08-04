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

  public static final int ACT_RU_VARIABLE_TEXT_LENGTH = 4000;

  private static final String MSSQL = "mssql";
  private static final String DB2 = "db2";
  private static final String ORACLE = "oracle";
  private static final String H2 = "h2";
  private static final String MYSQL = "mysql";
  private static final String POSTGRES = "postgres";

  protected static final Map<String, Map<String, String>> databaseSpecificStatements = new HashMap<String, Map<String,String>>();

  public static final Map<String, String> databaseSpecificLimitBeforeStatements = new HashMap<String, String>();
  public static final Map<String, String> databaseSpecificLimitAfterStatements = new HashMap<String, String>();
  public static final Map<String, String> databaseSpecificLimitBetweenStatements = new HashMap<String, String>();
  public static final Map<String, String> databaseSpecificOrderByStatements = new HashMap<String, String>();
  public static final Map<String, String> databaseSpecificLimitBeforeNativeQueryStatements = new HashMap<String, String>();

  public static final Map<String, String> databaseSpecificBitAnd1 = new HashMap<String, String>();
  public static final Map<String, String> databaseSpecificBitAnd2 = new HashMap<String, String>();
  public static final Map<String, String> databaseSpecificBitAnd3 = new HashMap<String, String>();

  public static final Map<String, String> databaseSpecificDateDiff1 = new HashMap<String, String>();
  public static final Map<String, String> databaseSpecificDateDiff2 = new HashMap<String, String>();
  public static final Map<String, String> databaseSpecificDateDiff3 = new HashMap<String, String>();

  public static final Map<String, String> databaseSpecificDummyTable = new HashMap<String, String>();

  public static final Map<String, String> databaseSpecificTrueConstant = new HashMap<String, String>();
  public static final Map<String, String> databaseSpecificFalseConstant = new HashMap<String, String>();

  public static final Map<String, Map<String, String>> dbSpecificConstants = new HashMap<String, Map<String, String>>();

  static {

    String defaultOrderBy = " order by ${orderBy} ";

    // h2
    databaseSpecificLimitBeforeStatements.put(H2, "");
    databaseSpecificLimitAfterStatements.put(H2, "LIMIT #{maxResults} OFFSET #{firstResult}");
    databaseSpecificLimitBetweenStatements.put(H2, "");
    databaseSpecificOrderByStatements.put(H2, defaultOrderBy);
    databaseSpecificLimitBeforeNativeQueryStatements.put(H2, "");
    databaseSpecificBitAnd1.put(H2, "BITAND(");
    databaseSpecificBitAnd2.put(H2, ",");
    databaseSpecificBitAnd3.put(H2, ")");
    databaseSpecificDateDiff1.put(H2, "DATEDIFF(ms, ");
    databaseSpecificDateDiff2.put(H2, ",");
    databaseSpecificDateDiff3.put(H2, ")");
    databaseSpecificDummyTable.put(H2, "");
    databaseSpecificTrueConstant.put(H2, "1");
    databaseSpecificFalseConstant.put(H2, "0");

    HashMap<String, String> constants = new HashMap<String, String>();
    constants.put("constant.event", "'event'");
    constants.put("constant.op_message", "NEW_VALUE_ || '_|_' || PROPERTY_");
    constants.put("constant.for.update", "for update");
    dbSpecificConstants.put(H2, constants);

	  //mysql specific
    databaseSpecificLimitBeforeStatements.put(MYSQL, "");
    databaseSpecificLimitAfterStatements.put(MYSQL, "LIMIT #{maxResults} OFFSET #{firstResult}");
    databaseSpecificLimitBetweenStatements.put(MYSQL, "");
    databaseSpecificOrderByStatements.put(MYSQL, defaultOrderBy);
    databaseSpecificLimitBeforeNativeQueryStatements.put(MYSQL, "");
    databaseSpecificBitAnd1.put(MYSQL, "");
    databaseSpecificBitAnd2.put(MYSQL, " & ");
    databaseSpecificBitAnd3.put(MYSQL, "");
    databaseSpecificDummyTable.put(MYSQL, "");
    databaseSpecificDateDiff1.put(MYSQL, "TIMESTAMPDIFF(SECOND,");
    databaseSpecificDateDiff2.put(MYSQL, ",");
    databaseSpecificDateDiff3.put(MYSQL, ")*1000");
    databaseSpecificTrueConstant.put(MYSQL, "1");
    databaseSpecificFalseConstant.put(MYSQL, "0");
    addDatabaseSpecificStatement(MYSQL, "selectNextJobsToExecute", "selectNextJobsToExecute_mysql");
    addDatabaseSpecificStatement(MYSQL, "selectExclusiveJobsToExecute", "selectExclusiveJobsToExecute_mysql");
    addDatabaseSpecificStatement(MYSQL, "selectProcessDefinitionsByQueryCriteria", "selectProcessDefinitionsByQueryCriteria_mysql");
    addDatabaseSpecificStatement(MYSQL, "selectProcessDefinitionCountByQueryCriteria", "selectProcessDefinitionCountByQueryCriteria_mysql");
    addDatabaseSpecificStatement(MYSQL, "selectDeploymentsByQueryCriteria", "selectDeploymentsByQueryCriteria_mysql");
    addDatabaseSpecificStatement(MYSQL, "selectDeploymentCountByQueryCriteria", "selectDeploymentCountByQueryCriteria_mysql");

    constants = new HashMap<String, String>();
    constants.put("constant.event", "'event'");
    constants.put("constant.op_message", "CONCAT(NEW_VALUE_, '_|_', PROPERTY_)");
    constants.put("constant.for.update", "for update");
    dbSpecificConstants.put(MYSQL, constants);

    //postgres specific
    databaseSpecificLimitBeforeStatements.put(POSTGRES, "");
    databaseSpecificLimitAfterStatements.put(POSTGRES, "LIMIT #{maxResults} OFFSET #{firstResult}");
    databaseSpecificLimitBetweenStatements.put(POSTGRES, "");
    databaseSpecificOrderByStatements.put(POSTGRES, defaultOrderBy);
    databaseSpecificLimitBeforeNativeQueryStatements.put(POSTGRES, "");
    databaseSpecificBitAnd1.put(POSTGRES, "");
    databaseSpecificBitAnd2.put(POSTGRES, " & ");
    databaseSpecificBitAnd3.put(POSTGRES, "");
    databaseSpecificDateDiff1.put(POSTGRES, "(EXTRACT(EPOCH FROM ");
    databaseSpecificDateDiff2.put(POSTGRES, " AT TIME ZONE 'UTC') * 1000) - (EXTRACT(EPOCH FROM ");
    databaseSpecificDateDiff3.put(POSTGRES, " AT TIME ZONE 'UTC') * 1000)");
    databaseSpecificDummyTable.put(POSTGRES, "");
    databaseSpecificTrueConstant.put(POSTGRES, "true");
    databaseSpecificFalseConstant.put(POSTGRES, "false");
    addDatabaseSpecificStatement(POSTGRES, "insertByteArray", "insertByteArray_postgres");
    addDatabaseSpecificStatement(POSTGRES, "updateByteArray", "updateByteArray_postgres");
    addDatabaseSpecificStatement(POSTGRES, "selectByteArray", "selectByteArray_postgres");
    addDatabaseSpecificStatement(POSTGRES, "selectResourceByDeploymentIdAndResourceName", "selectResourceByDeploymentIdAndResourceName_postgres");
    addDatabaseSpecificStatement(POSTGRES, "selectResourceByDeploymentIdAndResourceId", "selectResourceByDeploymentIdAndResourceId_postgres");
    addDatabaseSpecificStatement(POSTGRES, "selectResourcesByDeploymentId", "selectResourcesByDeploymentId_postgres");
    addDatabaseSpecificStatement(POSTGRES, "selectHistoricDetailsByQueryCriteria", "selectHistoricDetailsByQueryCriteria_postgres");
    addDatabaseSpecificStatement(POSTGRES, "insertIdentityInfo", "insertIdentityInfo_postgres");
    addDatabaseSpecificStatement(POSTGRES, "updateIdentityInfo", "updateIdentityInfo_postgres");
    addDatabaseSpecificStatement(POSTGRES, "selectIdentityInfoById", "selectIdentityInfoById_postgres");
    addDatabaseSpecificStatement(POSTGRES, "selectIdentityInfoByUserIdAndKey", "selectIdentityInfoByUserIdAndKey_postgres");
    addDatabaseSpecificStatement(POSTGRES, "selectIdentityInfoByUserId", "selectIdentityInfoByUserId_postgres");
    addDatabaseSpecificStatement(POSTGRES, "selectIdentityInfoDetails", "selectIdentityInfoDetails_postgres");
    addDatabaseSpecificStatement(POSTGRES, "insertComment", "insertComment_postgres");
    addDatabaseSpecificStatement(POSTGRES, "selectCommentsByTaskId", "selectCommentsByTaskId_postgres");
    addDatabaseSpecificStatement(POSTGRES, "selectCommentsByProcessInstanceId", "selectCommentsByProcessInstanceId_postgres");
    addDatabaseSpecificStatement(POSTGRES, "selectCommentByTaskIdAndCommentId", "selectCommentByTaskIdAndCommentId_postgres");
    addDatabaseSpecificStatement(POSTGRES, "selectEventsByTaskId", "selectEventsByTaskId_postgres");
    addDatabaseSpecificStatement(POSTGRES, "selectActivityStatistics", "selectActivityStatistics_postgres");
    addDatabaseSpecificStatement(POSTGRES, "selectActivityStatisticsCount", "selectActivityStatisticsCount_postgres");
    addDatabaseSpecificStatement(POSTGRES, "selectHistoricVariableInstanceByQueryCriteria", "selectHistoricVariableInstanceByQueryCriteria_postgres");

    constants = new HashMap<String, String>();
    constants.put("constant.event", "'event'");
    constants.put("constant.op_message", "NEW_VALUE_ || '_|_' || PROPERTY_");
    constants.put("constant.for.update", "for update");
    dbSpecificConstants.put(POSTGRES, constants);

    // oracle
    databaseSpecificLimitBeforeStatements.put(ORACLE, "select * from ( select a.*, ROWNUM rnum from (");
    databaseSpecificLimitAfterStatements.put(ORACLE, "  ) a where ROWNUM < #{lastRow}) where rnum  >= #{firstRow}");
    databaseSpecificLimitBetweenStatements.put(ORACLE, "");
    databaseSpecificOrderByStatements.put(ORACLE, defaultOrderBy);
    databaseSpecificLimitBeforeNativeQueryStatements.put(ORACLE, "");
    databaseSpecificDummyTable.put(ORACLE, "FROM DUAL");
    databaseSpecificBitAnd1.put(ORACLE, "BITAND(");
    databaseSpecificBitAnd2.put(ORACLE, ",");
    databaseSpecificBitAnd3.put(ORACLE, ")");
    databaseSpecificDateDiff1.put(ORACLE, "((cast(");
    databaseSpecificDateDiff2.put(ORACLE, " as date) - date '1970-01-01')*24*60*60*1000) - ((cast(");
    databaseSpecificDateDiff3.put(ORACLE, " as date) - date '1970-01-01')*24*60*60*1000)");
    databaseSpecificTrueConstant.put(ORACLE, "1");
    databaseSpecificFalseConstant.put(ORACLE, "0");
    addDatabaseSpecificStatement(ORACLE, "selectExclusiveJobsToExecute", "selectExclusiveJobsToExecute_integerBoolean");

    constants = new HashMap<String, String>();
    constants.put("constant.event", "cast('event' as nvarchar2(255))");
    constants.put("constant.op_message", "NEW_VALUE_ || '_|_' || PROPERTY_");
    constants.put("constant.for.update", "for update");
    dbSpecificConstants.put(ORACLE, constants);

    // db2
    databaseSpecificLimitBeforeStatements.put(DB2, "SELECT SUB.* FROM (");
    databaseSpecificLimitAfterStatements.put(DB2, ")RES ) SUB WHERE SUB.rnk >= #{firstRow} AND SUB.rnk < #{lastRow}");
    databaseSpecificLimitBetweenStatements.put(DB2, ", row_number() over (ORDER BY ${orderBy}) rnk FROM ( select distinct RES.* ");
    databaseSpecificOrderByStatements.put(DB2, "");
    databaseSpecificLimitBeforeNativeQueryStatements.put(DB2, "SELECT SUB.* FROM ( select RES.* , row_number() over (ORDER BY ${orderBy}) rnk FROM (");
    databaseSpecificBitAnd1.put(DB2, "BITAND(");
    databaseSpecificBitAnd2.put(DB2, ",");
    databaseSpecificBitAnd3.put(DB2, ")");
    databaseSpecificDateDiff1.put(DB2, "");
    databaseSpecificDateDiff2.put(DB2, "");
    databaseSpecificDateDiff3.put(DB2, "");
    databaseSpecificDummyTable.put(DB2, "FROM SYSIBM.SYSDUMMY1");
    databaseSpecificTrueConstant.put(DB2, "1");
    databaseSpecificFalseConstant.put(DB2, "0");
    addDatabaseSpecificStatement(DB2, "selectExclusiveJobsToExecute", "selectExclusiveJobsToExecute_integerBoolean");
    addDatabaseSpecificStatement(DB2, "selectExecutionByNativeQuery", "selectExecutionByNativeQuery_mssql_or_db2");
    addDatabaseSpecificStatement(DB2, "selectHistoricActivityInstanceByNativeQuery", "selectHistoricActivityInstanceByNativeQuery_mssql_or_db2");
    addDatabaseSpecificStatement(DB2, "selectHistoricProcessInstanceByNativeQuery", "selectHistoricProcessInstanceByNativeQuery_mssql_or_db2");
    addDatabaseSpecificStatement(DB2, "selectHistoricTaskInstanceByNativeQuery", "selectHistoricTaskInstanceByNativeQuery_mssql_or_db2");
    addDatabaseSpecificStatement(DB2, "selectTaskByNativeQuery", "selectTaskByNativeQuery_mssql_or_db2");

    constants = new HashMap<String, String>();
    constants.put("constant.event", "'event'");
    constants.put("constant.op_message", "CAST(CONCAT(CONCAT(COALESCE(NEW_VALUE_,''), '_|_'), COALESCE(PROPERTY_,'')) as varchar(255))");
    constants.put("constant.for.update", "for read only with rs use and keep update locks");
    dbSpecificConstants.put(DB2, constants);

    // mssql
    databaseSpecificLimitBeforeStatements.put(MSSQL, "SELECT SUB.* FROM (");
    databaseSpecificLimitAfterStatements.put(MSSQL, ")RES ) SUB WHERE SUB.rnk >= #{firstRow} AND SUB.rnk < #{lastRow}");
    databaseSpecificLimitBetweenStatements.put(MSSQL, ", row_number() over (ORDER BY ${orderBy}) rnk FROM ( select distinct RES.* ");
    databaseSpecificOrderByStatements.put(MSSQL, "");
    databaseSpecificLimitBeforeNativeQueryStatements.put(MSSQL, "SELECT SUB.* FROM ( select RES.* , row_number() over (ORDER BY ${orderBy}) rnk FROM (");
    databaseSpecificBitAnd1.put(MSSQL, "");
    databaseSpecificBitAnd2.put(MSSQL, " &");
    databaseSpecificBitAnd3.put(MSSQL, "");
    databaseSpecificDateDiff1.put(MSSQL, "");
    databaseSpecificDateDiff2.put(MSSQL, "");
    databaseSpecificDateDiff3.put(MSSQL, "");
    databaseSpecificDummyTable.put(MSSQL, "");
    databaseSpecificTrueConstant.put(MSSQL, "1");
    databaseSpecificFalseConstant.put(MSSQL, "0");
    addDatabaseSpecificStatement(MSSQL, "selectExclusiveJobsToExecute", "selectExclusiveJobsToExecute_integerBoolean");
    addDatabaseSpecificStatement(MSSQL, "selectExecutionByNativeQuery", "selectExecutionByNativeQuery_mssql_or_db2");
    addDatabaseSpecificStatement(MSSQL, "selectHistoricActivityInstanceByNativeQuery", "selectHistoricActivityInstanceByNativeQuery_mssql_or_db2");
    addDatabaseSpecificStatement(MSSQL, "selectHistoricProcessInstanceByNativeQuery", "selectHistoricProcessInstanceByNativeQuery_mssql_or_db2");
    addDatabaseSpecificStatement(MSSQL, "selectHistoricTaskInstanceByNativeQuery", "selectHistoricTaskInstanceByNativeQuery_mssql_or_db2");
    addDatabaseSpecificStatement(MSSQL, "selectTaskByNativeQuery", "selectTaskByNativeQuery_mssql_or_db2");
    addDatabaseSpecificStatement(MSSQL, "lockDeploymentLockProperty", "lockDeploymentLockProperty_mssql");

    constants = new HashMap<String, String>();
    constants.put("constant.event", "'event'");
    constants.put("constant.op_message", "NEW_VALUE_ + '_|_' + PROPERTY_");
    dbSpecificConstants.put(MSSQL, constants);
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
  protected boolean cmmnEnabled = true;

  public Class< ? > getSessionType() {
    return DbSqlSession.class;
  }

  public Session openSession() {
    return new DbSqlSession(this);
  }

  // insert, update and delete statements /////////////////////////////////////

  public String getInsertStatement(DbEntity object) {
    return getStatement(object.getClass(), insertStatements, "insert");
  }

  public String getUpdateStatement(DbEntity object) {
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
    statement = statement.substring(0, statement.length()-6); // "Entity".length() = 6
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

  public boolean isCmmnEnabled() {
    return cmmnEnabled;
  }

  public void setCmmnEnabled(boolean cmmnEnabled) {
    this.cmmnEnabled = cmmnEnabled;
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
