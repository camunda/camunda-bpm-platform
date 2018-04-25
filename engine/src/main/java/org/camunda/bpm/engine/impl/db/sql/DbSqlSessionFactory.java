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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.ibatis.session.SqlSessionFactory;
import org.camunda.bpm.engine.impl.cfg.IdGenerator;
import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.interceptor.Session;
import org.camunda.bpm.engine.impl.interceptor.SessionFactory;
import org.camunda.bpm.engine.impl.util.ClassNameUtil;


/**
 * @author Tom Baeyens
 */
public class DbSqlSessionFactory implements SessionFactory {

  public static final String MSSQL = "mssql";
  public static final String DB2 = "db2";
  public static final String ORACLE = "oracle";
  public static final String H2 = "h2";
  public static final String MYSQL = "mysql";
  public static final String POSTGRES = "postgres";
  public static final String MARIADB = "mariadb";

  protected static final Map<String, Map<String, String>> databaseSpecificStatements = new HashMap<String, Map<String,String>>();

  public static final Map<String, String> databaseSpecificLimitBeforeStatements = new HashMap<String, String>();
  public static final Map<String, String> databaseSpecificLimitAfterStatements = new HashMap<String, String>();
  //limit statements that can be used to select first N rows without OFFSET
  public static final Map<String, String> databaseSpecificLimitBeforeWithoutOffsetStatements = new HashMap<String, String>();
  public static final Map<String, String> databaseSpecificLimitAfterWithoutOffsetStatements = new HashMap<String, String>();
  // limitAfter statements that can be used with subqueries
  public static final Map<String, String> databaseSpecificInnerLimitAfterStatements = new HashMap<String, String>();
  public static final Map<String, String> databaseSpecificLimitBetweenStatements = new HashMap<String, String>();
  public static final Map<String, String> databaseSpecificLimitBetweenFilterStatements = new HashMap<String, String>();

  public static final Map<String, String> databaseSpecificEscapeChar = new HashMap<String, String>();

  public static final Map<String, String> databaseSpecificOrderByStatements = new HashMap<String, String>();
  public static final Map<String, String> databaseSpecificLimitBeforeNativeQueryStatements = new HashMap<String, String>();

  public static final Map<String, String> databaseSpecificBitAnd1 = new HashMap<String, String>();
  public static final Map<String, String> databaseSpecificBitAnd2 = new HashMap<String, String>();
  public static final Map<String, String> databaseSpecificBitAnd3 = new HashMap<String, String>();

  public static final Map<String, String> databaseSpecificDatepart1 = new HashMap<String, String>();
  public static final Map<String, String> databaseSpecificDatepart2 = new HashMap<String, String>();
  public static final Map<String, String> databaseSpecificDatepart3 = new HashMap<String, String>();

  public static final Map<String, String> databaseSpecificDummyTable = new HashMap<String, String>();

  public static final Map<String, String> databaseSpecificIfNull = new HashMap<String, String>();

  public static final Map<String, String> databaseSpecificTrueConstant = new HashMap<String, String>();
  public static final Map<String, String> databaseSpecificFalseConstant = new HashMap<String, String>();

  public static final Map<String, String> databaseSpecificDistinct = new HashMap<String, String>();

  public static final Map<String, Map<String, String>> dbSpecificConstants = new HashMap<String, Map<String, String>>();

  public static final Map<String, String> databaseSpecificDaysComparator = new HashMap<String, String>();

  static {

    String defaultOrderBy = "order by ${internalOrderBy}";

    String defaultEscapeChar = "'\\'";

    // h2
    databaseSpecificLimitBeforeStatements.put(H2, "");
    databaseSpecificLimitAfterStatements.put(H2, "LIMIT #{maxResults} OFFSET #{firstResult}");
    databaseSpecificLimitBeforeWithoutOffsetStatements.put(H2, "");
    databaseSpecificLimitAfterWithoutOffsetStatements.put(H2, "LIMIT #{maxResults}");
    databaseSpecificInnerLimitAfterStatements.put(H2, databaseSpecificLimitAfterStatements.get(H2));
    databaseSpecificLimitBetweenStatements.put(H2, "");
    databaseSpecificLimitBetweenFilterStatements.put(H2, "");
    databaseSpecificOrderByStatements.put(H2, defaultOrderBy);
    databaseSpecificLimitBeforeNativeQueryStatements.put(H2, "");
    databaseSpecificDistinct.put(H2, "distinct");

    databaseSpecificEscapeChar.put(H2, defaultEscapeChar);

    databaseSpecificBitAnd1.put(H2, "BITAND(");
    databaseSpecificBitAnd2.put(H2, ",");
    databaseSpecificBitAnd3.put(H2, ")");
    databaseSpecificDatepart1.put(H2, "");
    databaseSpecificDatepart2.put(H2, "(");
    databaseSpecificDatepart3.put(H2, ")");

    databaseSpecificDummyTable.put(H2, "");
    databaseSpecificTrueConstant.put(H2, "1");
    databaseSpecificFalseConstant.put(H2, "0");
    databaseSpecificIfNull.put(H2, "IFNULL");

    databaseSpecificDaysComparator.put(H2, "DATEDIFF(DAY, ${date}, #{currentTimestamp}) >= ${days}");

    HashMap<String, String> constants = new HashMap<String, String>();
    constants.put("constant.event", "'event'");
    constants.put("constant.op_message", "NEW_VALUE_ || '_|_' || PROPERTY_");
    constants.put("constant_for_update", "for update");
    constants.put("constant.datepart.quarter", "QUARTER");
    constants.put("constant.datepart.month", "MONTH");
    constants.put("constant.datepart.minute", "MINUTE");
    constants.put("constant.null.startTime", "null START_TIME_");
    constants.put("constant.varchar.cast", "'${key}'");
    dbSpecificConstants.put(H2, constants);

    // mysql specific
    // use the same specific for mariadb since it based on mysql and work with the exactly same statements
    for(String mysqlLikeDatabase : Arrays.asList(MYSQL, MARIADB)) {

      databaseSpecificLimitBeforeStatements.put(mysqlLikeDatabase, "");
      databaseSpecificLimitAfterStatements.put(mysqlLikeDatabase, "LIMIT #{maxResults} OFFSET #{firstResult}");
      databaseSpecificLimitBeforeWithoutOffsetStatements.put(mysqlLikeDatabase, "");
      databaseSpecificLimitAfterWithoutOffsetStatements.put(mysqlLikeDatabase, "LIMIT #{maxResults}");
      databaseSpecificInnerLimitAfterStatements.put(mysqlLikeDatabase, databaseSpecificLimitAfterStatements.get(mysqlLikeDatabase));
      databaseSpecificLimitBetweenStatements.put(mysqlLikeDatabase, "");
      databaseSpecificLimitBetweenFilterStatements.put(mysqlLikeDatabase, "");
      databaseSpecificOrderByStatements.put(mysqlLikeDatabase, defaultOrderBy);
      databaseSpecificLimitBeforeNativeQueryStatements.put(mysqlLikeDatabase, "");
      databaseSpecificDistinct.put(mysqlLikeDatabase, "distinct");

      databaseSpecificEscapeChar.put(mysqlLikeDatabase, "'\\\\'");

      databaseSpecificBitAnd1.put(mysqlLikeDatabase, "");
      databaseSpecificBitAnd2.put(mysqlLikeDatabase, " & ");
      databaseSpecificBitAnd3.put(mysqlLikeDatabase, "");
      databaseSpecificDatepart1.put(mysqlLikeDatabase, "");
      databaseSpecificDatepart2.put(mysqlLikeDatabase, "(");
      databaseSpecificDatepart3.put(mysqlLikeDatabase, ")");

      databaseSpecificDummyTable.put(mysqlLikeDatabase, "");
      databaseSpecificTrueConstant.put(mysqlLikeDatabase, "1");
      databaseSpecificFalseConstant.put(mysqlLikeDatabase, "0");
      databaseSpecificIfNull.put(mysqlLikeDatabase, "IFNULL");

      databaseSpecificDaysComparator.put(mysqlLikeDatabase, "DATEDIFF(#{currentTimestamp}, ${date}) >= ${days}");

      addDatabaseSpecificStatement(mysqlLikeDatabase, "toggleForeignKey", "toggleForeignKey_mysql");
      addDatabaseSpecificStatement(mysqlLikeDatabase, "selectProcessDefinitionsByQueryCriteria", "selectProcessDefinitionsByQueryCriteria_mysql");
      addDatabaseSpecificStatement(mysqlLikeDatabase, "selectProcessDefinitionCountByQueryCriteria", "selectProcessDefinitionCountByQueryCriteria_mysql");
      addDatabaseSpecificStatement(mysqlLikeDatabase, "selectDeploymentsByQueryCriteria", "selectDeploymentsByQueryCriteria_mysql");
      addDatabaseSpecificStatement(mysqlLikeDatabase, "selectDeploymentCountByQueryCriteria", "selectDeploymentCountByQueryCriteria_mysql");

      // related to CAM-8064
      addDatabaseSpecificStatement(mysqlLikeDatabase, "deleteExceptionByteArraysByIds", "deleteExceptionByteArraysByIds_mysql");
      addDatabaseSpecificStatement(mysqlLikeDatabase, "deleteErrorDetailsByteArraysByIds", "deleteErrorDetailsByteArraysByIds_mysql");
      addDatabaseSpecificStatement(mysqlLikeDatabase, "deleteHistoricDetailsByIds", "deleteHistoricDetailsByIds_mysql");
      addDatabaseSpecificStatement(mysqlLikeDatabase, "deleteHistoricDetailByteArraysByIds", "deleteHistoricDetailByteArraysByIds_mysql");
      addDatabaseSpecificStatement(mysqlLikeDatabase, "deleteHistoricIdentityLinksByTaskProcessInstanceIds", "deleteHistoricIdentityLinksByTaskProcessInstanceIds_mysql");
      addDatabaseSpecificStatement(mysqlLikeDatabase, "deleteHistoricIdentityLinksByTaskCaseInstanceIds", "deleteHistoricIdentityLinksByTaskCaseInstanceIds_mysql");
      addDatabaseSpecificStatement(mysqlLikeDatabase, "deleteHistoricDecisionInputInstanceByteArraysByDecisionInstanceIds", "deleteHistoricDecisionInputInstanceByteArraysByDecisionInstanceIds_mysql");
      addDatabaseSpecificStatement(mysqlLikeDatabase, "deleteHistoricDecisionOutputInstanceByteArraysByDecisionInstanceIds", "deleteHistoricDecisionOutputInstanceByteArraysByDecisionInstanceIds_mysql");
      addDatabaseSpecificStatement(mysqlLikeDatabase, "deleteHistoricVariableInstanceByIds", "deleteHistoricVariableInstanceByIds_mysql");
      addDatabaseSpecificStatement(mysqlLikeDatabase, "deleteHistoricVariableInstanceByteArraysByIds", "deleteHistoricVariableInstanceByteArraysByIds_mysql");
      addDatabaseSpecificStatement(mysqlLikeDatabase, "deleteCommentsByIds", "deleteCommentsByIds_mysql");
      addDatabaseSpecificStatement(mysqlLikeDatabase, "deleteAttachmentByteArraysByIds", "deleteAttachmentByteArraysByIds_mysql");
      addDatabaseSpecificStatement(mysqlLikeDatabase, "deleteAttachmentByIds", "deleteAttachmentByIds_mysql");

      addDatabaseSpecificStatement(mysqlLikeDatabase, "deleteHistoricIncidentsByBatchIds", "deleteHistoricIncidentsByBatchIds_mysql");

      constants = new HashMap<String, String>();
      constants.put("constant.event", "'event'");
      constants.put("constant.op_message", "CONCAT(NEW_VALUE_, '_|_', PROPERTY_)");
      constants.put("constant_for_update", "for update");
      constants.put("constant.datepart.quarter", "QUARTER");
      constants.put("constant.datepart.month", "MONTH");
      constants.put("constant.datepart.minute", "MINUTE");
      constants.put("constant.null.startTime", "null START_TIME_");
      constants.put("constant.varchar.cast", "'${key}'");
      dbSpecificConstants.put(mysqlLikeDatabase, constants);
    }

    // postgres specific
    databaseSpecificLimitBeforeStatements.put(POSTGRES, "");
    databaseSpecificLimitAfterStatements.put(POSTGRES, "LIMIT #{maxResults} OFFSET #{firstResult}");
    databaseSpecificLimitBeforeWithoutOffsetStatements.put(POSTGRES, "");
    databaseSpecificLimitAfterWithoutOffsetStatements.put(POSTGRES, "LIMIT #{maxResults}");
    databaseSpecificInnerLimitAfterStatements.put(POSTGRES, databaseSpecificLimitAfterStatements.get(POSTGRES));
    databaseSpecificLimitBetweenStatements.put(POSTGRES, "");
    databaseSpecificLimitBetweenFilterStatements.put(POSTGRES, "");
    databaseSpecificOrderByStatements.put(POSTGRES, defaultOrderBy);
    databaseSpecificLimitBeforeNativeQueryStatements.put(POSTGRES, "");
    databaseSpecificDistinct.put(POSTGRES, "distinct");

    databaseSpecificEscapeChar.put(POSTGRES, defaultEscapeChar);

    databaseSpecificBitAnd1.put(POSTGRES, "");
    databaseSpecificBitAnd2.put(POSTGRES, " & ");
    databaseSpecificBitAnd3.put(POSTGRES, "");
    databaseSpecificDatepart1.put(POSTGRES, "extract(");
    databaseSpecificDatepart2.put(POSTGRES, " from ");
    databaseSpecificDatepart3.put(POSTGRES, ")");

    databaseSpecificDummyTable.put(POSTGRES, "");
    databaseSpecificTrueConstant.put(POSTGRES, "true");
    databaseSpecificFalseConstant.put(POSTGRES, "false");
    databaseSpecificIfNull.put(POSTGRES, "COALESCE");

    databaseSpecificDaysComparator.put(POSTGRES, "EXTRACT (DAY FROM #{currentTimestamp} - ${date}) >= ${days}");

    addDatabaseSpecificStatement(POSTGRES, "insertByteArray", "insertByteArray_postgres");
    addDatabaseSpecificStatement(POSTGRES, "updateByteArray", "updateByteArray_postgres");
    addDatabaseSpecificStatement(POSTGRES, "selectByteArray", "selectByteArray_postgres");
    addDatabaseSpecificStatement(POSTGRES, "selectResourceByDeploymentIdAndResourceName", "selectResourceByDeploymentIdAndResourceName_postgres");
    addDatabaseSpecificStatement(POSTGRES, "selectResourceByDeploymentIdAndResourceNames", "selectResourceByDeploymentIdAndResourceNames_postgres");
    addDatabaseSpecificStatement(POSTGRES, "selectResourceByDeploymentIdAndResourceId", "selectResourceByDeploymentIdAndResourceId_postgres");
    addDatabaseSpecificStatement(POSTGRES, "selectResourceByDeploymentIdAndResourceIds", "selectResourceByDeploymentIdAndResourceIds_postgres");
    addDatabaseSpecificStatement(POSTGRES, "selectResourcesByDeploymentId", "selectResourcesByDeploymentId_postgres");
    addDatabaseSpecificStatement(POSTGRES, "selectLatestResourcesByDeploymentName", "selectLatestResourcesByDeploymentName_postgres");
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
    addDatabaseSpecificStatement(POSTGRES, "selectFilterByQueryCriteria", "selectFilterByQueryCriteria_postgres");
    addDatabaseSpecificStatement(POSTGRES, "selectFilter", "selectFilter_postgres");

    constants = new HashMap<String, String>();
    constants.put("constant.event", "'event'");
    constants.put("constant.op_message", "NEW_VALUE_ || '_|_' || PROPERTY_");
    constants.put("constant_for_update", "for update");
    constants.put("constant.datepart.quarter", "QUARTER");
    constants.put("constant.datepart.month", "MONTH");
    constants.put("constant.datepart.minute", "MINUTE");
    constants.put("constant.null.startTime", "null START_TIME_");
    constants.put("constant.varchar.cast", "cast('${key}' as varchar(64))");
    dbSpecificConstants.put(POSTGRES, constants);

    // oracle
    databaseSpecificLimitBeforeStatements.put(ORACLE, "select * from ( select a.*, ROWNUM rnum from (");
    databaseSpecificLimitAfterStatements.put(ORACLE, "  ) a where ROWNUM < #{lastRow}) where rnum  >= #{firstRow}");
    databaseSpecificLimitBeforeWithoutOffsetStatements.put(ORACLE, "");
    databaseSpecificLimitAfterWithoutOffsetStatements.put(ORACLE, "AND ROWNUM <= #{maxResults}");
    databaseSpecificInnerLimitAfterStatements.put(ORACLE, databaseSpecificLimitAfterStatements.get(ORACLE));
    databaseSpecificLimitBetweenStatements.put(ORACLE, "");
    databaseSpecificLimitBetweenFilterStatements.put(ORACLE, "");
    databaseSpecificOrderByStatements.put(ORACLE, defaultOrderBy);
    databaseSpecificLimitBeforeNativeQueryStatements.put(ORACLE, "");
    databaseSpecificDistinct.put(ORACLE, "distinct");

    databaseSpecificEscapeChar.put(ORACLE, defaultEscapeChar);

    databaseSpecificDummyTable.put(ORACLE, "FROM DUAL");
    databaseSpecificBitAnd1.put(ORACLE, "BITAND(");
    databaseSpecificBitAnd2.put(ORACLE, ",");
    databaseSpecificBitAnd3.put(ORACLE, ")");
    databaseSpecificDatepart1.put(ORACLE, "to_number(to_char(");
    databaseSpecificDatepart2.put(ORACLE, ",");
    databaseSpecificDatepart3.put(ORACLE, "))");

    databaseSpecificTrueConstant.put(ORACLE, "1");
    databaseSpecificFalseConstant.put(ORACLE, "0");
    databaseSpecificIfNull.put(ORACLE, "NVL");

    databaseSpecificDaysComparator.put(ORACLE, "${date} <= #{currentTimestamp} - ${days}");

    addDatabaseSpecificStatement(ORACLE, "selectHistoricProcessInstanceDurationReport", "selectHistoricProcessInstanceDurationReport_oracle");
    addDatabaseSpecificStatement(ORACLE, "selectHistoricTaskInstanceDurationReport", "selectHistoricTaskInstanceDurationReport_oracle");
    addDatabaseSpecificStatement(ORACLE, "selectHistoricTaskInstanceCountByTaskNameReport", "selectHistoricTaskInstanceCountByTaskNameReport_oracle");
    addDatabaseSpecificStatement(ORACLE, "selectFilterByQueryCriteria", "selectFilterByQueryCriteria_oracleDb2");
    addDatabaseSpecificStatement(ORACLE, "selectHistoricProcessInstanceIdsForCleanup", "selectHistoricProcessInstanceIdsForCleanup_oracle");
    addDatabaseSpecificStatement(ORACLE, "selectHistoricDecisionInstanceIdsForCleanup", "selectHistoricDecisionInstanceIdsForCleanup_oracle");
    addDatabaseSpecificStatement(ORACLE, "selectHistoricCaseInstanceIdsForCleanup", "selectHistoricCaseInstanceIdsForCleanup_oracle");
    addDatabaseSpecificStatement(ORACLE, "selectHistoricBatchIdsForCleanup", "selectHistoricBatchIdsForCleanup_oracle");

    constants = new HashMap<String, String>();
    constants.put("constant.event", "cast('event' as nvarchar2(255))");
    constants.put("constant.op_message", "NEW_VALUE_ || '_|_' || PROPERTY_");
    constants.put("constant_for_update", "for update");
    constants.put("constant.datepart.quarter", "'Q'");
    constants.put("constant.datepart.month", "'MM'");
    constants.put("constant.datepart.minute", "'MI'");
    constants.put("constant.null.startTime", "null START_TIME_");
    constants.put("constant.varchar.cast", "'${key}'");
    dbSpecificConstants.put(ORACLE, constants);

    // db2
    databaseSpecificLimitBeforeStatements.put(DB2, "SELECT SUB.* FROM (");
    databaseSpecificInnerLimitAfterStatements.put(DB2, ")RES ) SUB WHERE SUB.rnk >= #{firstRow} AND SUB.rnk < #{lastRow}");
    databaseSpecificLimitAfterStatements.put(DB2, databaseSpecificInnerLimitAfterStatements.get(DB2) + " ORDER BY SUB.rnk");
    databaseSpecificLimitBetweenStatements.put(DB2, ", row_number() over (ORDER BY ${internalOrderBy}) rnk FROM ( select distinct RES.* ");
    databaseSpecificLimitBetweenFilterStatements.put(DB2, ", row_number() over (ORDER BY ${internalOrderBy}) rnk FROM ( select distinct RES.ID_, RES.REV_, RES.RESOURCE_TYPE_, RES.NAME_, RES.OWNER_ ");
    databaseSpecificLimitBeforeWithoutOffsetStatements.put(DB2, "");
    databaseSpecificLimitAfterWithoutOffsetStatements.put(DB2, "FETCH FIRST ${maxResults} ROWS ONLY");
    databaseSpecificOrderByStatements.put(DB2, defaultOrderBy);
    databaseSpecificLimitBeforeNativeQueryStatements.put(DB2, "SELECT SUB.* FROM ( select RES.* , row_number() over (ORDER BY ${internalOrderBy}) rnk FROM (");
    databaseSpecificDistinct.put(DB2, "");

    databaseSpecificEscapeChar.put(DB2, defaultEscapeChar);

    databaseSpecificBitAnd1.put(DB2, "BITAND(");
    databaseSpecificBitAnd2.put(DB2, ", CAST(");
    databaseSpecificBitAnd3.put(DB2, " AS Integer))");
    databaseSpecificDatepart1.put(DB2, "");
    databaseSpecificDatepart2.put(DB2, "(");
    databaseSpecificDatepart3.put(DB2, ")");

    databaseSpecificDummyTable.put(DB2, "FROM SYSIBM.SYSDUMMY1");
    databaseSpecificTrueConstant.put(DB2, "1");
    databaseSpecificFalseConstant.put(DB2, "0");
    databaseSpecificIfNull.put(DB2, "NVL");

    databaseSpecificDaysComparator.put(DB2, "${date} + ${days} DAYS <= #{currentTimestamp}");

    addDatabaseSpecificStatement(DB2, "selectMeterLogAggregatedByTimeInterval", "selectMeterLogAggregatedByTimeInterval_db2_or_mssql");
    addDatabaseSpecificStatement(DB2, "selectExecutionByNativeQuery", "selectExecutionByNativeQuery_mssql_or_db2");
    addDatabaseSpecificStatement(DB2, "selectHistoricActivityInstanceByNativeQuery", "selectHistoricActivityInstanceByNativeQuery_mssql_or_db2");
    addDatabaseSpecificStatement(DB2, "selectHistoricCaseActivityInstanceByNativeQuery", "selectHistoricCaseActivityInstanceByNativeQuery_mssql_or_db2");
    addDatabaseSpecificStatement(DB2, "selectHistoricProcessInstanceByNativeQuery", "selectHistoricProcessInstanceByNativeQuery_mssql_or_db2");
    addDatabaseSpecificStatement(DB2, "selectHistoricCaseInstanceByNativeQuery", "selectHistoricCaseInstanceByNativeQuery_mssql_or_db2");
    addDatabaseSpecificStatement(DB2, "selectHistoricTaskInstanceByNativeQuery", "selectHistoricTaskInstanceByNativeQuery_mssql_or_db2");
    addDatabaseSpecificStatement(DB2, "selectHistoricVariableInstanceByNativeQuery", "selectHistoricVariableInstanceByNativeQuery_mssql_or_db2");
    addDatabaseSpecificStatement(DB2, "selectTaskByNativeQuery", "selectTaskByNativeQuery_mssql_or_db2");
    addDatabaseSpecificStatement(DB2, "selectUserByNativeQuery", "selectUserByNativeQuery_mssql_or_db2");
    addDatabaseSpecificStatement(DB2, "selectHistoricDecisionInstancesByNativeQuery", "selectHistoricDecisionInstancesByNativeQuery_mssql_or_db2");
    addDatabaseSpecificStatement(DB2, "selectFilterByQueryCriteria", "selectFilterByQueryCriteria_oracleDb2");

    constants = new HashMap<String, String>();
    constants.put("constant.event", "'event'");
    constants.put("constant.op_message", "CAST(CONCAT(CONCAT(COALESCE(NEW_VALUE_,''), '_|_'), COALESCE(PROPERTY_,'')) as varchar(255))");
    constants.put("constant_for_update", "for read only with rs use and keep update locks");
    constants.put("constant.datepart.quarter", "QUARTER");
    constants.put("constant.datepart.month", "MONTH");
    constants.put("constant.datepart.minute", "MINUTE");
    constants.put("constant.null.startTime", "CAST(NULL as timestamp) as START_TIME_");
    constants.put("constant.varchar.cast", "cast('${key}' as varchar(64))");
    dbSpecificConstants.put(DB2, constants);

    // mssql
    databaseSpecificLimitBeforeStatements.put(MSSQL, "SELECT SUB.* FROM (");
    databaseSpecificInnerLimitAfterStatements.put(MSSQL, ")RES ) SUB WHERE SUB.rnk >= #{firstRow} AND SUB.rnk < #{lastRow}");
    databaseSpecificLimitAfterStatements.put(MSSQL, databaseSpecificInnerLimitAfterStatements.get(MSSQL) + " ORDER BY SUB.rnk");
    databaseSpecificLimitBetweenStatements.put(MSSQL, ", row_number() over (ORDER BY ${internalOrderBy}) rnk FROM ( select distinct RES.* ");
    databaseSpecificLimitBetweenFilterStatements.put(MSSQL, "");
    databaseSpecificLimitBeforeWithoutOffsetStatements.put(MSSQL, "TOP (#{maxResults})");
    databaseSpecificLimitAfterWithoutOffsetStatements.put(MSSQL, "");
    databaseSpecificOrderByStatements.put(MSSQL, "");
    databaseSpecificLimitBeforeNativeQueryStatements.put(MSSQL, "SELECT SUB.* FROM ( select RES.* , row_number() over (ORDER BY ${internalOrderBy}) rnk FROM (");
    databaseSpecificDistinct.put(MSSQL, "");

    databaseSpecificEscapeChar.put(MSSQL, defaultEscapeChar);

    databaseSpecificBitAnd1.put(MSSQL, "");
    databaseSpecificBitAnd2.put(MSSQL, " &");
    databaseSpecificBitAnd3.put(MSSQL, "");
    databaseSpecificDatepart1.put(MSSQL, "datepart(");
    databaseSpecificDatepart2.put(MSSQL, ",");
    databaseSpecificDatepart3.put(MSSQL, ")");

    databaseSpecificDummyTable.put(MSSQL, "");
    databaseSpecificTrueConstant.put(MSSQL, "1");
    databaseSpecificFalseConstant.put(MSSQL, "0");
    databaseSpecificIfNull.put(MSSQL, "ISNULL");

    databaseSpecificDaysComparator.put(MSSQL, "DATEDIFF(DAY, ${date}, #{currentTimestamp}) >= ${days}");

    addDatabaseSpecificStatement(MSSQL, "selectMeterLogAggregatedByTimeInterval", "selectMeterLogAggregatedByTimeInterval_db2_or_mssql");
    addDatabaseSpecificStatement(MSSQL, "selectExecutionByNativeQuery", "selectExecutionByNativeQuery_mssql_or_db2");
    addDatabaseSpecificStatement(MSSQL, "selectHistoricActivityInstanceByNativeQuery", "selectHistoricActivityInstanceByNativeQuery_mssql_or_db2");
    addDatabaseSpecificStatement(MSSQL, "selectHistoricCaseActivityInstanceByNativeQuery", "selectHistoricCaseActivityInstanceByNativeQuery_mssql_or_db2");
    addDatabaseSpecificStatement(MSSQL, "selectHistoricProcessInstanceByNativeQuery", "selectHistoricProcessInstanceByNativeQuery_mssql_or_db2");
    addDatabaseSpecificStatement(MSSQL, "selectHistoricCaseInstanceByNativeQuery", "selectHistoricCaseInstanceByNativeQuery_mssql_or_db2");
    addDatabaseSpecificStatement(MSSQL, "selectHistoricTaskInstanceByNativeQuery", "selectHistoricTaskInstanceByNativeQuery_mssql_or_db2");
    addDatabaseSpecificStatement(MSSQL, "selectHistoricVariableInstanceByNativeQuery", "selectHistoricVariableInstanceByNativeQuery_mssql_or_db2");
    addDatabaseSpecificStatement(MSSQL, "selectTaskByNativeQuery", "selectTaskByNativeQuery_mssql_or_db2");
    addDatabaseSpecificStatement(MSSQL, "selectUserByNativeQuery", "selectUserByNativeQuery_mssql_or_db2");
    addDatabaseSpecificStatement(MSSQL, "lockDeploymentLockProperty", "lockDeploymentLockProperty_mssql");
    addDatabaseSpecificStatement(MSSQL, "lockHistoryCleanupJobLockProperty", "lockHistoryCleanupJobLockProperty_mssql");
    addDatabaseSpecificStatement(MSSQL, "lockStartupLockProperty", "lockStartupLockProperty_mssql");
    addDatabaseSpecificStatement(MSSQL, "selectEventSubscriptionsByNameAndExecution", "selectEventSubscriptionsByNameAndExecution_mssql");
    addDatabaseSpecificStatement(MSSQL, "selectEventSubscriptionsByExecutionAndType", "selectEventSubscriptionsByExecutionAndType_mssql");
    addDatabaseSpecificStatement(MSSQL, "selectHistoricDecisionInstancesByNativeQuery", "selectHistoricDecisionInstancesByNativeQuery_mssql_or_db2");

    constants = new HashMap<String, String>();
    constants.put("constant.event", "'event'");
    constants.put("constant.op_message", "NEW_VALUE_ + '_|_' + PROPERTY_");
    constants.put("constant.datepart.quarter", "QUARTER");
    constants.put("constant.datepart.month", "MONTH");
    constants.put("constant.datepart.minute", "MINUTE");
    constants.put("constant.null.startTime", "CAST(NULL AS datetime2) AS START_TIME_");
    constants.put("constant.varchar.cast", "'${key}'");
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
  protected boolean dmnEnabled = true;

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

  public boolean isDmnEnabled() {
    return dmnEnabled;
  }

  public void setDmnEnabled(boolean dmnEnabled) {
    this.dmnEnabled = dmnEnabled;
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
