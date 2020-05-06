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
package org.camunda.bpm.engine.impl.db.sql;

import java.sql.Connection;
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
  public static final String[] SUPPORTED_DATABASES = {MSSQL, DB2, ORACLE, H2, MYSQL, POSTGRES, MARIADB};

  protected static final Map<String, Map<String, String>> databaseSpecificStatements = new HashMap<>();

  public static final Map<String, String> databaseSpecificLimitBeforeStatements = new HashMap<>();
  public static final Map<String, String> databaseSpecificLimitAfterStatements = new HashMap<>();
  //limit statements that can be used to select first N rows without OFFSET
  public static final Map<String, String> databaseSpecificLimitBeforeWithoutOffsetStatements = new HashMap<>();
  public static final Map<String, String> databaseSpecificLimitAfterWithoutOffsetStatements = new HashMap<>();
  // limitAfter statements that can be used with subqueries
  public static final Map<String, String> databaseSpecificInnerLimitAfterStatements = new HashMap<>();
  public static final Map<String, String> databaseSpecificLimitBetweenStatements = new HashMap<>();
  public static final Map<String, String> databaseSpecificLimitBetweenFilterStatements = new HashMap<>();
  public static final Map<String, String> databaseSpecificLimitBetweenAcquisitionStatements = new HashMap<>();
  // count distinct statements
  public static final Map<String, String> databaseSpecificCountDistinctBeforeStart = new HashMap<>();
  public static final Map<String, String> databaseSpecificCountDistinctBeforeEnd = new HashMap<>();
  public static final Map<String, String> databaseSpecificCountDistinctAfterEnd = new HashMap<>();

  public static final Map<String, String> optimizeDatabaseSpecificLimitBeforeWithoutOffsetStatements = new HashMap<>();
  public static final Map<String, String> optimizeDatabaseSpecificLimitAfterWithoutOffsetStatements = new HashMap<>();

  public static final Map<String, String> databaseSpecificEscapeChar = new HashMap<>();

  public static final Map<String, String> databaseSpecificOrderByStatements = new HashMap<>();
  public static final Map<String, String> databaseSpecificLimitBeforeNativeQueryStatements = new HashMap<>();

  public static final Map<String, String> databaseSpecificBitAnd1 = new HashMap<>();
  public static final Map<String, String> databaseSpecificBitAnd2 = new HashMap<>();
  public static final Map<String, String> databaseSpecificBitAnd3 = new HashMap<>();

  public static final Map<String, String> databaseSpecificDatepart1 = new HashMap<>();
  public static final Map<String, String> databaseSpecificDatepart2 = new HashMap<>();
  public static final Map<String, String> databaseSpecificDatepart3 = new HashMap<>();

  public static final Map<String, String> databaseSpecificDummyTable = new HashMap<>();

  public static final Map<String, String> databaseSpecificIfNull = new HashMap<>();

  public static final Map<String, String> databaseSpecificTrueConstant = new HashMap<>();
  public static final Map<String, String> databaseSpecificFalseConstant = new HashMap<>();

  public static final Map<String, String> databaseSpecificDistinct = new HashMap<>();

  public static final Map<String, Map<String, String>> dbSpecificConstants = new HashMap<>();

  public static final Map<String, String> databaseSpecificDaysComparator = new HashMap<>();

  public static final Map<String, String> databaseSpecificCollationForCaseSensitivity = new HashMap<>();

  /*
   * On SQL server, the overall maximum number of parameters in a prepared statement
   * is 2100.
   */
  public static final int MAXIMUM_NUMBER_PARAMS = 2000;

  static {

    String defaultOrderBy = "order by ${internalOrderBy}";

    String defaultEscapeChar = "'\\'";

    String defaultDistinctCountBeforeStart = "select count(distinct";
    String defaultDistinctCountBeforeEnd = ")";
    String defaultDistinctCountAfterEnd = "";

    // h2
    databaseSpecificLimitBeforeStatements.put(H2, "");
    optimizeDatabaseSpecificLimitBeforeWithoutOffsetStatements.put(H2, "");
    databaseSpecificLimitAfterStatements.put(H2, "LIMIT #{maxResults} OFFSET #{firstResult}");
    optimizeDatabaseSpecificLimitAfterWithoutOffsetStatements.put(H2, "LIMIT #{maxResults}");
    databaseSpecificLimitBeforeWithoutOffsetStatements.put(H2, "");
    databaseSpecificLimitAfterWithoutOffsetStatements.put(H2, "LIMIT #{maxResults}");
    databaseSpecificInnerLimitAfterStatements.put(H2, databaseSpecificLimitAfterStatements.get(H2));
    databaseSpecificLimitBetweenStatements.put(H2, "");
    databaseSpecificLimitBetweenFilterStatements.put(H2, "");
    databaseSpecificLimitBetweenAcquisitionStatements.put(H2, "");
    databaseSpecificOrderByStatements.put(H2, defaultOrderBy);
    databaseSpecificLimitBeforeNativeQueryStatements.put(H2, "");
    databaseSpecificDistinct.put(H2, "distinct");

    databaseSpecificCountDistinctBeforeStart.put(H2, defaultDistinctCountBeforeStart);
    databaseSpecificCountDistinctBeforeEnd.put(H2, defaultDistinctCountBeforeEnd);
    databaseSpecificCountDistinctAfterEnd.put(H2, defaultDistinctCountAfterEnd);

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

    databaseSpecificCollationForCaseSensitivity.put(H2, "");

    HashMap<String, String> constants = new HashMap<>();
    constants.put("constant.event", "'event'");
    constants.put("constant.op_message", "NEW_VALUE_ || '_|_' || PROPERTY_");
    constants.put("constant_for_update", "for update");
    constants.put("constant.datepart.quarter", "QUARTER");
    constants.put("constant.datepart.month", "MONTH");
    constants.put("constant.datepart.minute", "MINUTE");
    constants.put("constant.null.startTime", "null START_TIME_");
    constants.put("constant.varchar.cast", "'${key}'");
    constants.put("constant.integer.cast", "NULL");
    constants.put("constant.null.reporter", "NULL AS REPORTER_");
    dbSpecificConstants.put(H2, constants);

    // mysql specific
    // use the same specific for mariadb since it based on mysql and work with the exactly same statements
    for(String mysqlLikeDatabase : Arrays.asList(MYSQL, MARIADB)) {

      databaseSpecificLimitBeforeStatements.put(mysqlLikeDatabase, "");
      optimizeDatabaseSpecificLimitBeforeWithoutOffsetStatements.put(mysqlLikeDatabase, "");
      databaseSpecificLimitAfterStatements.put(mysqlLikeDatabase, "LIMIT #{maxResults} OFFSET #{firstResult}");
      optimizeDatabaseSpecificLimitAfterWithoutOffsetStatements.put(mysqlLikeDatabase, "LIMIT #{maxResults}");
      databaseSpecificLimitBeforeWithoutOffsetStatements.put(mysqlLikeDatabase, "");
      databaseSpecificLimitAfterWithoutOffsetStatements.put(mysqlLikeDatabase, "LIMIT #{maxResults}");
      databaseSpecificInnerLimitAfterStatements.put(mysqlLikeDatabase, databaseSpecificLimitAfterStatements.get(mysqlLikeDatabase));
      databaseSpecificLimitBetweenStatements.put(mysqlLikeDatabase, "");
      databaseSpecificLimitBetweenFilterStatements.put(mysqlLikeDatabase, "");
      databaseSpecificLimitBetweenAcquisitionStatements.put(mysqlLikeDatabase, "");
      databaseSpecificOrderByStatements.put(mysqlLikeDatabase, defaultOrderBy);
      databaseSpecificLimitBeforeNativeQueryStatements.put(mysqlLikeDatabase, "");
      databaseSpecificDistinct.put(mysqlLikeDatabase, "distinct");

      databaseSpecificCountDistinctBeforeStart.put(mysqlLikeDatabase, defaultDistinctCountBeforeStart);
      databaseSpecificCountDistinctBeforeEnd.put(mysqlLikeDatabase, defaultDistinctCountBeforeEnd);
      databaseSpecificCountDistinctAfterEnd.put(mysqlLikeDatabase, defaultDistinctCountAfterEnd);

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

      databaseSpecificCollationForCaseSensitivity.put(mysqlLikeDatabase, "");

      addDatabaseSpecificStatement(mysqlLikeDatabase, "toggleForeignKey", "toggleForeignKey_mysql");
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

      // related to CAM-9505
      addDatabaseSpecificStatement(mysqlLikeDatabase, "updateUserOperationLogByRootProcessInstanceId", "updateUserOperationLogByRootProcessInstanceId_mysql");
      addDatabaseSpecificStatement(mysqlLikeDatabase, "updateExternalTaskLogByRootProcessInstanceId", "updateExternalTaskLogByRootProcessInstanceId_mysql");
      addDatabaseSpecificStatement(mysqlLikeDatabase, "updateHistoricIncidentsByRootProcessInstanceId", "updateHistoricIncidentsByRootProcessInstanceId_mysql");
      addDatabaseSpecificStatement(mysqlLikeDatabase, "updateHistoricIncidentsByBatchId", "updateHistoricIncidentsByBatchId_mysql");
      addDatabaseSpecificStatement(mysqlLikeDatabase, "updateIdentityLinkLogByRootProcessInstanceId", "updateIdentityLinkLogByRootProcessInstanceId_mysql");

      // related to CAM-10172
      addDatabaseSpecificStatement(mysqlLikeDatabase, "updateUserOperationLogByProcessInstanceId", "updateUserOperationLogByProcessInstanceId_mysql");
      addDatabaseSpecificStatement(mysqlLikeDatabase, "updateExternalTaskLogByProcessInstanceId", "updateExternalTaskLogByProcessInstanceId_mysql");
      addDatabaseSpecificStatement(mysqlLikeDatabase, "updateHistoricIncidentsByProcessInstanceId", "updateHistoricIncidentsByProcessInstanceId_mysql");
      addDatabaseSpecificStatement(mysqlLikeDatabase, "updateIdentityLinkLogByProcessInstanceId", "updateIdentityLinkLogByProcessInstanceId_mysql");

      // related to CAM-10664
      addDatabaseSpecificStatement(mysqlLikeDatabase, "updateOperationLogAnnotationByOperationId", "updateOperationLogAnnotationByOperationId_mysql");

      constants = new HashMap<>();
      constants.put("constant.event", "'event'");
      constants.put("constant.op_message", "CONCAT(NEW_VALUE_, '_|_', PROPERTY_)");
      constants.put("constant_for_update", "for update");
      constants.put("constant.datepart.quarter", "QUARTER");
      constants.put("constant.datepart.month", "MONTH");
      constants.put("constant.datepart.minute", "MINUTE");
      constants.put("constant.null.startTime", "null START_TIME_");
      constants.put("constant.varchar.cast", "'${key}'");
      constants.put("constant.integer.cast", "NULL");
      constants.put("constant.null.reporter", "NULL AS REPORTER_");
      dbSpecificConstants.put(mysqlLikeDatabase, constants);
    }

    // postgres specific
    databaseSpecificLimitBeforeStatements.put(POSTGRES, "");
    optimizeDatabaseSpecificLimitBeforeWithoutOffsetStatements.put(POSTGRES, "");
    databaseSpecificLimitAfterStatements.put(POSTGRES, "LIMIT #{maxResults} OFFSET #{firstResult}");
    optimizeDatabaseSpecificLimitAfterWithoutOffsetStatements.put(POSTGRES, "LIMIT #{maxResults}");
    databaseSpecificLimitBeforeWithoutOffsetStatements.put(POSTGRES, "");
    databaseSpecificLimitAfterWithoutOffsetStatements.put(POSTGRES, "LIMIT #{maxResults}");
    databaseSpecificInnerLimitAfterStatements.put(POSTGRES, databaseSpecificLimitAfterStatements.get(POSTGRES));
    databaseSpecificLimitBetweenStatements.put(POSTGRES, "");
    databaseSpecificLimitBetweenFilterStatements.put(POSTGRES, "");
    databaseSpecificLimitBetweenAcquisitionStatements.put(POSTGRES, "");
    databaseSpecificOrderByStatements.put(POSTGRES, defaultOrderBy);
    databaseSpecificLimitBeforeNativeQueryStatements.put(POSTGRES, "");
    databaseSpecificDistinct.put(POSTGRES, "distinct");

    databaseSpecificCountDistinctBeforeStart.put(POSTGRES, "SELECT COUNT(*) FROM (SELECT DISTINCT");
    databaseSpecificCountDistinctBeforeEnd.put(POSTGRES, "");
    databaseSpecificCountDistinctAfterEnd.put(POSTGRES, ") countDistinct");

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

    databaseSpecificCollationForCaseSensitivity.put(POSTGRES, "");

    addDatabaseSpecificStatement(POSTGRES, "insertByteArray", "insertByteArray_postgres");
    addDatabaseSpecificStatement(POSTGRES, "updateByteArray", "updateByteArray_postgres");
    addDatabaseSpecificStatement(POSTGRES, "selectByteArray", "selectByteArray_postgres");
    addDatabaseSpecificStatement(POSTGRES, "selectByteArrays", "selectByteArrays_postgres");
    addDatabaseSpecificStatement(POSTGRES, "selectResourceByDeploymentIdAndResourceName", "selectResourceByDeploymentIdAndResourceName_postgres");
    addDatabaseSpecificStatement(POSTGRES, "selectResourceByDeploymentIdAndResourceNames", "selectResourceByDeploymentIdAndResourceNames_postgres");
    addDatabaseSpecificStatement(POSTGRES, "selectResourceByDeploymentIdAndResourceId", "selectResourceByDeploymentIdAndResourceId_postgres");
    addDatabaseSpecificStatement(POSTGRES, "selectResourceByDeploymentIdAndResourceIds", "selectResourceByDeploymentIdAndResourceIds_postgres");
    addDatabaseSpecificStatement(POSTGRES, "selectResourcesByDeploymentId", "selectResourcesByDeploymentId_postgres");
    addDatabaseSpecificStatement(POSTGRES, "selectResourceById", "selectResourceById_postgres");
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

    addDatabaseSpecificStatement(POSTGRES, "deleteAttachmentsByRemovalTime", "deleteAttachmentsByRemovalTime_postgres_or_db2");
    addDatabaseSpecificStatement(POSTGRES, "deleteCommentsByRemovalTime", "deleteCommentsByRemovalTime_postgres_or_db2");
    addDatabaseSpecificStatement(POSTGRES, "deleteHistoricActivityInstancesByRemovalTime", "deleteHistoricActivityInstancesByRemovalTime_postgres_or_db2");
    addDatabaseSpecificStatement(POSTGRES, "deleteHistoricDecisionInputInstancesByRemovalTime", "deleteHistoricDecisionInputInstancesByRemovalTime_postgres_or_db2");
    addDatabaseSpecificStatement(POSTGRES, "deleteHistoricDecisionInstancesByRemovalTime", "deleteHistoricDecisionInstancesByRemovalTime_postgres_or_db2");
    addDatabaseSpecificStatement(POSTGRES, "deleteHistoricDecisionOutputInstancesByRemovalTime", "deleteHistoricDecisionOutputInstancesByRemovalTime_postgres_or_db2");
    addDatabaseSpecificStatement(POSTGRES, "deleteHistoricDetailsByRemovalTime", "deleteHistoricDetailsByRemovalTime_postgres_or_db2");
    addDatabaseSpecificStatement(POSTGRES, "deleteExternalTaskLogByRemovalTime", "deleteExternalTaskLogByRemovalTime_postgres_or_db2");
    addDatabaseSpecificStatement(POSTGRES, "deleteHistoricIdentityLinkLogByRemovalTime", "deleteHistoricIdentityLinkLogByRemovalTime_postgres_or_db2");
    addDatabaseSpecificStatement(POSTGRES, "deleteHistoricIncidentsByRemovalTime", "deleteHistoricIncidentsByRemovalTime_postgres_or_db2");
    addDatabaseSpecificStatement(POSTGRES, "deleteJobLogByRemovalTime", "deleteJobLogByRemovalTime_postgres_or_db2");
    addDatabaseSpecificStatement(POSTGRES, "deleteHistoricProcessInstancesByRemovalTime", "deleteHistoricProcessInstancesByRemovalTime_postgres_or_db2");
    addDatabaseSpecificStatement(POSTGRES, "deleteHistoricTaskInstancesByRemovalTime", "deleteHistoricTaskInstancesByRemovalTime_postgres_or_db2");
    addDatabaseSpecificStatement(POSTGRES, "deleteHistoricVariableInstancesByRemovalTime", "deleteHistoricVariableInstancesByRemovalTime_postgres_or_db2");
    addDatabaseSpecificStatement(POSTGRES, "deleteUserOperationLogByRemovalTime", "deleteUserOperationLogByRemovalTime_postgres_or_db2");
    addDatabaseSpecificStatement(POSTGRES, "deleteByteArraysByRemovalTime", "deleteByteArraysByRemovalTime_postgres_or_db2");
    addDatabaseSpecificStatement(POSTGRES, "deleteHistoricBatchesByRemovalTime", "deleteHistoricBatchesByRemovalTime_postgres_or_db2");
    addDatabaseSpecificStatement(POSTGRES, "deleteAuthorizationsByRemovalTime", "deleteAuthorizationsByRemovalTime_postgres_or_db2");

    constants = new HashMap<>();
    constants.put("constant.event", "'event'");
    constants.put("constant.op_message", "NEW_VALUE_ || '_|_' || PROPERTY_");
    constants.put("constant_for_update", "for update");
    constants.put("constant.datepart.quarter", "QUARTER");
    constants.put("constant.datepart.month", "MONTH");
    constants.put("constant.datepart.minute", "MINUTE");
    constants.put("constant.null.startTime", "null START_TIME_");
    constants.put("constant.varchar.cast", "cast('${key}' as varchar(64))");
    constants.put("constant.integer.cast", "cast(NULL as integer)");
    constants.put("constant.null.reporter", "CAST(NULL AS VARCHAR) AS REPORTER_");
    dbSpecificConstants.put(POSTGRES, constants);

    // oracle
    databaseSpecificLimitBeforeStatements.put(ORACLE, "select * from ( select a.*, ROWNUM rnum from (");
    optimizeDatabaseSpecificLimitBeforeWithoutOffsetStatements.put(ORACLE, "select * from ( select a.*, ROWNUM rnum from (");
    databaseSpecificLimitAfterStatements.put(ORACLE, "  ) a where ROWNUM < #{lastRow}) where rnum  >= #{firstRow}");
    optimizeDatabaseSpecificLimitAfterWithoutOffsetStatements.put(ORACLE, "  ) a where ROWNUM <= #{maxResults})");
    databaseSpecificLimitBeforeWithoutOffsetStatements.put(ORACLE, "");
    databaseSpecificLimitAfterWithoutOffsetStatements.put(ORACLE, "AND ROWNUM <= #{maxResults}");
    databaseSpecificInnerLimitAfterStatements.put(ORACLE, databaseSpecificLimitAfterStatements.get(ORACLE));
    databaseSpecificLimitBetweenStatements.put(ORACLE, "");
    databaseSpecificLimitBetweenFilterStatements.put(ORACLE, "");
    databaseSpecificLimitBetweenAcquisitionStatements.put(ORACLE, "");
    databaseSpecificOrderByStatements.put(ORACLE, defaultOrderBy);
    databaseSpecificLimitBeforeNativeQueryStatements.put(ORACLE, "");
    databaseSpecificDistinct.put(ORACLE, "distinct");

    databaseSpecificCountDistinctBeforeStart.put(ORACLE, defaultDistinctCountBeforeStart);
    databaseSpecificCountDistinctBeforeEnd.put(ORACLE, defaultDistinctCountBeforeEnd);
    databaseSpecificCountDistinctAfterEnd.put(ORACLE, defaultDistinctCountAfterEnd);

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

    databaseSpecificCollationForCaseSensitivity.put(ORACLE, "");

    addDatabaseSpecificStatement(ORACLE, "selectHistoricProcessInstanceDurationReport", "selectHistoricProcessInstanceDurationReport_oracle");
    addDatabaseSpecificStatement(ORACLE, "selectHistoricTaskInstanceDurationReport", "selectHistoricTaskInstanceDurationReport_oracle");
    addDatabaseSpecificStatement(ORACLE, "selectHistoricTaskInstanceCountByTaskNameReport", "selectHistoricTaskInstanceCountByTaskNameReport_oracle");
    addDatabaseSpecificStatement(ORACLE, "selectFilterByQueryCriteria", "selectFilterByQueryCriteria_oracleDb2");
    addDatabaseSpecificStatement(ORACLE, "selectHistoricProcessInstanceIdsForCleanup", "selectHistoricProcessInstanceIdsForCleanup_oracle");
    addDatabaseSpecificStatement(ORACLE, "selectHistoricDecisionInstanceIdsForCleanup", "selectHistoricDecisionInstanceIdsForCleanup_oracle");
    addDatabaseSpecificStatement(ORACLE, "selectHistoricCaseInstanceIdsForCleanup", "selectHistoricCaseInstanceIdsForCleanup_oracle");
    addDatabaseSpecificStatement(ORACLE, "selectHistoricBatchIdsForCleanup", "selectHistoricBatchIdsForCleanup_oracle");

    addDatabaseSpecificStatement(ORACLE, "deleteAttachmentsByRemovalTime", "deleteAttachmentsByRemovalTime_oracle");
    addDatabaseSpecificStatement(ORACLE, "deleteCommentsByRemovalTime", "deleteCommentsByRemovalTime_oracle");
    addDatabaseSpecificStatement(ORACLE, "deleteHistoricActivityInstancesByRemovalTime", "deleteHistoricActivityInstancesByRemovalTime_oracle");
    addDatabaseSpecificStatement(ORACLE, "deleteHistoricDecisionInputInstancesByRemovalTime", "deleteHistoricDecisionInputInstancesByRemovalTime_oracle");
    addDatabaseSpecificStatement(ORACLE, "deleteHistoricDecisionInstancesByRemovalTime", "deleteHistoricDecisionInstancesByRemovalTime_oracle");
    addDatabaseSpecificStatement(ORACLE, "deleteHistoricDecisionOutputInstancesByRemovalTime", "deleteHistoricDecisionOutputInstancesByRemovalTime_oracle");
    addDatabaseSpecificStatement(ORACLE, "deleteHistoricDetailsByRemovalTime", "deleteHistoricDetailsByRemovalTime_oracle");
    addDatabaseSpecificStatement(ORACLE, "deleteExternalTaskLogByRemovalTime", "deleteExternalTaskLogByRemovalTime_oracle");
    addDatabaseSpecificStatement(ORACLE, "deleteHistoricIdentityLinkLogByRemovalTime", "deleteHistoricIdentityLinkLogByRemovalTime_oracle");
    addDatabaseSpecificStatement(ORACLE, "deleteHistoricIncidentsByRemovalTime", "deleteHistoricIncidentsByRemovalTime_oracle");
    addDatabaseSpecificStatement(ORACLE, "deleteJobLogByRemovalTime", "deleteJobLogByRemovalTime_oracle");
    addDatabaseSpecificStatement(ORACLE, "deleteHistoricProcessInstancesByRemovalTime", "deleteHistoricProcessInstancesByRemovalTime_oracle");
    addDatabaseSpecificStatement(ORACLE, "deleteHistoricTaskInstancesByRemovalTime", "deleteHistoricTaskInstancesByRemovalTime_oracle");
    addDatabaseSpecificStatement(ORACLE, "deleteHistoricVariableInstancesByRemovalTime", "deleteHistoricVariableInstancesByRemovalTime_oracle");
    addDatabaseSpecificStatement(ORACLE, "deleteUserOperationLogByRemovalTime", "deleteUserOperationLogByRemovalTime_oracle");
    addDatabaseSpecificStatement(ORACLE, "deleteByteArraysByRemovalTime", "deleteByteArraysByRemovalTime_oracle");
    addDatabaseSpecificStatement(ORACLE, "deleteHistoricBatchesByRemovalTime", "deleteHistoricBatchesByRemovalTime_oracle");
    addDatabaseSpecificStatement(ORACLE, "deleteAuthorizationsByRemovalTime", "deleteAuthorizationsByRemovalTime_oracle");

    constants = new HashMap<>();
    constants.put("constant.event", "cast('event' as nvarchar2(255))");
    constants.put("constant.op_message", "NEW_VALUE_ || '_|_' || PROPERTY_");
    constants.put("constant_for_update", "for update");
    constants.put("constant.datepart.quarter", "'Q'");
    constants.put("constant.datepart.month", "'MM'");
    constants.put("constant.datepart.minute", "'MI'");
    constants.put("constant.null.startTime", "null START_TIME_");
    constants.put("constant.varchar.cast", "'${key}'");
    constants.put("constant.integer.cast", "NULL");
    constants.put("constant.null.reporter", "NULL AS REPORTER_");
    dbSpecificConstants.put(ORACLE, constants);

    // db2
    databaseSpecificLimitBeforeStatements.put(DB2, "SELECT SUB.* FROM (");
    optimizeDatabaseSpecificLimitBeforeWithoutOffsetStatements.put(DB2, "");
    databaseSpecificInnerLimitAfterStatements.put(DB2, ")RES ) SUB WHERE SUB.rnk >= #{firstRow} AND SUB.rnk < #{lastRow}");
    databaseSpecificLimitAfterStatements.put(DB2, databaseSpecificInnerLimitAfterStatements.get(DB2) + " ORDER BY SUB.rnk");
    optimizeDatabaseSpecificLimitAfterWithoutOffsetStatements.put(DB2, "FETCH FIRST ${maxResults} ROWS ONLY");
    String db2LimitBetweenWithoutColumns = ", row_number() over (ORDER BY ${internalOrderBy}) rnk FROM ( select distinct ";
    databaseSpecificLimitBetweenStatements.put(DB2, db2LimitBetweenWithoutColumns + "RES.* ");
    databaseSpecificLimitBetweenFilterStatements.put(DB2, db2LimitBetweenWithoutColumns + "RES.ID_, RES.REV_, RES.RESOURCE_TYPE_, RES.NAME_, RES.OWNER_ ");
    databaseSpecificLimitBetweenAcquisitionStatements.put(DB2, db2LimitBetweenWithoutColumns
        + "RES.ID_, RES.REV_, RES.TYPE_, RES.LOCK_EXP_TIME_, RES.LOCK_OWNER_, RES.EXCLUSIVE_, RES.PROCESS_INSTANCE_ID_, RES.DUEDATE_, RES.PRIORITY_ ");
    databaseSpecificLimitBeforeWithoutOffsetStatements.put(DB2, "");
    databaseSpecificLimitAfterWithoutOffsetStatements.put(DB2, "FETCH FIRST ${maxResults} ROWS ONLY");
    databaseSpecificOrderByStatements.put(DB2, defaultOrderBy);
    databaseSpecificLimitBeforeNativeQueryStatements.put(DB2, "SELECT SUB.* FROM ( select RES.* , row_number() over (ORDER BY ${internalOrderBy}) rnk FROM (");
    databaseSpecificDistinct.put(DB2, "");

    databaseSpecificCountDistinctBeforeStart.put(DB2, defaultDistinctCountBeforeStart);
    databaseSpecificCountDistinctBeforeEnd.put(DB2, defaultDistinctCountBeforeEnd);
    databaseSpecificCountDistinctAfterEnd.put(DB2, defaultDistinctCountAfterEnd);

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

    databaseSpecificCollationForCaseSensitivity.put(DB2, "");

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

    addDatabaseSpecificStatement(DB2, "deleteAttachmentsByRemovalTime", "deleteAttachmentsByRemovalTime_postgres_or_db2");
    addDatabaseSpecificStatement(DB2, "deleteCommentsByRemovalTime", "deleteCommentsByRemovalTime_postgres_or_db2");
    addDatabaseSpecificStatement(DB2, "deleteHistoricActivityInstancesByRemovalTime", "deleteHistoricActivityInstancesByRemovalTime_postgres_or_db2");
    addDatabaseSpecificStatement(DB2, "deleteHistoricDecisionInputInstancesByRemovalTime", "deleteHistoricDecisionInputInstancesByRemovalTime_postgres_or_db2");
    addDatabaseSpecificStatement(DB2, "deleteHistoricDecisionInstancesByRemovalTime", "deleteHistoricDecisionInstancesByRemovalTime_postgres_or_db2");
    addDatabaseSpecificStatement(DB2, "deleteHistoricDecisionOutputInstancesByRemovalTime", "deleteHistoricDecisionOutputInstancesByRemovalTime_postgres_or_db2");
    addDatabaseSpecificStatement(DB2, "deleteHistoricDetailsByRemovalTime", "deleteHistoricDetailsByRemovalTime_postgres_or_db2");
    addDatabaseSpecificStatement(DB2, "deleteExternalTaskLogByRemovalTime", "deleteExternalTaskLogByRemovalTime_postgres_or_db2");
    addDatabaseSpecificStatement(DB2, "deleteHistoricIdentityLinkLogByRemovalTime", "deleteHistoricIdentityLinkLogByRemovalTime_postgres_or_db2");
    addDatabaseSpecificStatement(DB2, "deleteHistoricIncidentsByRemovalTime", "deleteHistoricIncidentsByRemovalTime_postgres_or_db2");
    addDatabaseSpecificStatement(DB2, "deleteJobLogByRemovalTime", "deleteJobLogByRemovalTime_postgres_or_db2");
    addDatabaseSpecificStatement(DB2, "deleteHistoricProcessInstancesByRemovalTime", "deleteHistoricProcessInstancesByRemovalTime_postgres_or_db2");
    addDatabaseSpecificStatement(DB2, "deleteHistoricTaskInstancesByRemovalTime", "deleteHistoricTaskInstancesByRemovalTime_postgres_or_db2");
    addDatabaseSpecificStatement(DB2, "deleteHistoricVariableInstancesByRemovalTime", "deleteHistoricVariableInstancesByRemovalTime_postgres_or_db2");
    addDatabaseSpecificStatement(DB2, "deleteUserOperationLogByRemovalTime", "deleteUserOperationLogByRemovalTime_postgres_or_db2");
    addDatabaseSpecificStatement(DB2, "deleteByteArraysByRemovalTime", "deleteByteArraysByRemovalTime_postgres_or_db2");
    addDatabaseSpecificStatement(DB2, "deleteHistoricBatchesByRemovalTime", "deleteHistoricBatchesByRemovalTime_postgres_or_db2");
    addDatabaseSpecificStatement(DB2, "deleteAuthorizationsByRemovalTime", "deleteAuthorizationsByRemovalTime_postgres_or_db2");

    constants = new HashMap<>();
    constants.put("constant.event", "'event'");
    constants.put("constant.op_message", "CAST(CONCAT(CONCAT(COALESCE(NEW_VALUE_,''), '_|_'), COALESCE(PROPERTY_,'')) as varchar(255))");
    constants.put("constant_for_update", "for read only with rs use and keep update locks");
    constants.put("constant.datepart.quarter", "QUARTER");
    constants.put("constant.datepart.month", "MONTH");
    constants.put("constant.datepart.minute", "MINUTE");
    constants.put("constant.null.startTime", "CAST(NULL as timestamp) as START_TIME_");
    constants.put("constant.varchar.cast", "cast('${key}' as varchar(64))");
    constants.put("constant.integer.cast", "cast(NULL as integer)");
    constants.put("constant.null.reporter", "CAST(NULL AS VARCHAR(255)) AS REPORTER_");
    dbSpecificConstants.put(DB2, constants);

    // mssql
    databaseSpecificLimitBeforeStatements.put(MSSQL, "SELECT SUB.* FROM (");
    optimizeDatabaseSpecificLimitBeforeWithoutOffsetStatements.put(MSSQL, "");
    databaseSpecificInnerLimitAfterStatements.put(MSSQL, ")RES ) SUB WHERE SUB.rnk >= #{firstRow} AND SUB.rnk < #{lastRow}");
    databaseSpecificLimitAfterStatements.put(MSSQL, databaseSpecificInnerLimitAfterStatements.get(MSSQL) + " ORDER BY SUB.rnk");
    optimizeDatabaseSpecificLimitAfterWithoutOffsetStatements.put(MSSQL, "");
    String mssqlLimitBetweenWithoutColumns = ", row_number() over (ORDER BY ${internalOrderBy}) rnk FROM ( select distinct ";
    databaseSpecificLimitBetweenStatements.put(MSSQL, mssqlLimitBetweenWithoutColumns + "RES.* ");
    databaseSpecificLimitBetweenFilterStatements.put(MSSQL, "");
    databaseSpecificLimitBetweenAcquisitionStatements.put(MSSQL, mssqlLimitBetweenWithoutColumns
        + "RES.ID_, RES.REV_, RES.TYPE_, RES.LOCK_EXP_TIME_, RES.LOCK_OWNER_, RES.EXCLUSIVE_, RES.PROCESS_INSTANCE_ID_, RES.DUEDATE_, RES.PRIORITY_ ");
    databaseSpecificLimitBeforeWithoutOffsetStatements.put(MSSQL, "TOP (#{maxResults})");
    databaseSpecificLimitAfterWithoutOffsetStatements.put(MSSQL, "");
    databaseSpecificOrderByStatements.put(MSSQL, "");
    databaseSpecificLimitBeforeNativeQueryStatements.put(MSSQL, "SELECT SUB.* FROM ( select RES.* , row_number() over (ORDER BY ${internalOrderBy}) rnk FROM (");
    databaseSpecificDistinct.put(MSSQL, "");

    databaseSpecificCountDistinctBeforeStart.put(MSSQL, defaultDistinctCountBeforeStart);
    databaseSpecificCountDistinctBeforeEnd.put(MSSQL, defaultDistinctCountBeforeEnd);
    databaseSpecificCountDistinctAfterEnd.put(MSSQL, defaultDistinctCountAfterEnd);

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

    databaseSpecificCollationForCaseSensitivity.put(MSSQL, "COLLATE Latin1_General_CS_AS");

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

    constants = new HashMap<>();
    constants.put("constant.event", "'event'");
    constants.put("constant.op_message", "NEW_VALUE_ + '_|_' + PROPERTY_");
    constants.put("constant.datepart.quarter", "QUARTER");
    constants.put("constant.datepart.month", "MONTH");
    constants.put("constant.datepart.minute", "MINUTE");
    constants.put("constant.null.startTime", "CAST(NULL AS datetime2) AS START_TIME_");
    constants.put("constant.varchar.cast", "'${key}'");
    constants.put("constant.integer.cast", "NULL");
    constants.put("constant.null.reporter", "NULL AS REPORTER_");
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
  protected Map<Class<?>,String>  insertStatements = new ConcurrentHashMap<>();
  protected Map<Class<?>,String>  updateStatements = new ConcurrentHashMap<>();
  protected Map<Class<?>,String>  deleteStatements = new ConcurrentHashMap<>();
  protected Map<Class<?>,String>  selectStatements = new ConcurrentHashMap<>();
  protected boolean isDbIdentityUsed = true;
  protected boolean isDbHistoryUsed = true;
  protected boolean cmmnEnabled = true;
  protected boolean dmnEnabled = true;

  protected boolean jdbcBatchProcessing;

  public DbSqlSessionFactory(boolean jdbcBatchProcessing) {
    this.jdbcBatchProcessing = jdbcBatchProcessing;
  }

  public Class< ? > getSessionType() {
    return DbSqlSession.class;
  }

  public Session openSession() {
    return jdbcBatchProcessing ? new BatchDbSqlSession(this) : new SimpleDbSqlSession(this);
  }

  public DbSqlSession openSession(Connection connection, String catalog, String schema) {
    return jdbcBatchProcessing ?
        new BatchDbSqlSession(this, connection, catalog, schema) :
        new SimpleDbSqlSession(this, connection, catalog, schema);
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
      specificStatements = new HashMap<>();
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
