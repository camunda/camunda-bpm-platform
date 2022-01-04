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
package org.camunda.bpm.engine.impl.persistence.entity;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.camunda.bpm.engine.filter.Filter;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.history.HistoricCaseActivityInstance;
import org.camunda.bpm.engine.history.HistoricCaseInstance;
import org.camunda.bpm.engine.history.HistoricDecisionInstance;
import org.camunda.bpm.engine.history.HistoricDetail;
import org.camunda.bpm.engine.history.HistoricFormProperty;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.history.HistoricVariableUpdate;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.TablePageQueryImpl;
import org.camunda.bpm.engine.impl.batch.BatchEntity;
import org.camunda.bpm.engine.impl.batch.history.HistoricBatchEntity;
import org.camunda.bpm.engine.impl.cmmn.entity.repository.CaseDefinitionEntity;
import org.camunda.bpm.engine.impl.cmmn.entity.runtime.CaseExecutionEntity;
import org.camunda.bpm.engine.impl.cmmn.entity.runtime.CaseSentryPartEntity;
import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.db.EnginePersistenceLogger;
import org.camunda.bpm.engine.impl.db.sql.DbSqlSessionFactory;
import org.camunda.bpm.engine.impl.dmn.entity.repository.DecisionDefinitionEntity;
import org.camunda.bpm.engine.impl.dmn.entity.repository.DecisionRequirementsDefinitionEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricDecisionInputInstanceEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricDecisionInstanceEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricDecisionOutputInstanceEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricDetailEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricExternalTaskLogEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricIncidentEventEntity;
import org.camunda.bpm.engine.impl.history.event.UserOperationLogEntryEventEntity;
import org.camunda.bpm.engine.impl.persistence.AbstractManager;
import org.camunda.bpm.engine.impl.util.DatabaseUtil;
import org.camunda.bpm.engine.management.TableMetaData;
import org.camunda.bpm.engine.management.TablePage;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.Incident;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;


/**
 * @author Tom Baeyens
 */
public class TableDataManager extends AbstractManager {

  protected static final EnginePersistenceLogger LOG = ProcessEngineLogger.PERSISTENCE_LOGGER;

  public static Map<Class<?>, String> apiTypeToTableNameMap = new HashMap<>();
  public static Map<Class<? extends DbEntity>, String> persistentObjectToTableNameMap = new HashMap<>();

  static {
    // runtime
    persistentObjectToTableNameMap.put(TaskEntity.class, "ACT_RU_TASK");
    persistentObjectToTableNameMap.put(ExternalTaskEntity.class, "ACT_RU_EXT_TASK");
    persistentObjectToTableNameMap.put(ExecutionEntity.class, "ACT_RU_EXECUTION");
    persistentObjectToTableNameMap.put(IdentityLinkEntity.class, "ACT_RU_IDENTITYLINK");
    persistentObjectToTableNameMap.put(VariableInstanceEntity.class, "ACT_RU_VARIABLE");

    persistentObjectToTableNameMap.put(JobEntity.class, "ACT_RU_JOB");
    persistentObjectToTableNameMap.put(MessageEntity.class, "ACT_RU_JOB");
    persistentObjectToTableNameMap.put(TimerEntity.class, "ACT_RU_JOB");
    persistentObjectToTableNameMap.put(JobDefinitionEntity.class, "ACT_RU_JOBDEF");
    persistentObjectToTableNameMap.put(BatchEntity.class, "ACT_RU_BATCH");

    persistentObjectToTableNameMap.put(IncidentEntity.class, "ACT_RU_INCIDENT");

    persistentObjectToTableNameMap.put(EventSubscriptionEntity.class, "ACT_RU_EVENT_SUBSCR");


    persistentObjectToTableNameMap.put(MeterLogEntity.class, "ACT_RU_METER_LOG");
    persistentObjectToTableNameMap.put(TaskMeterLogEntity.class, "ACT_RU_TASK_METER_LOG");

    persistentObjectToTableNameMap.put(CamundaFormDefinitionEntity.class, "ACT_RE_CAMFORMDEF");
    // repository
    persistentObjectToTableNameMap.put(DeploymentEntity.class, "ACT_RE_DEPLOYMENT");
    persistentObjectToTableNameMap.put(ProcessDefinitionEntity.class, "ACT_RE_PROCDEF");

    // CMMN
    persistentObjectToTableNameMap.put(CaseDefinitionEntity.class, "ACT_RE_CASE_DEF");
    persistentObjectToTableNameMap.put(CaseExecutionEntity.class, "ACT_RU_CASE_EXECUTION");
    persistentObjectToTableNameMap.put(CaseSentryPartEntity.class, "ACT_RU_CASE_SENTRY_PART");

    // DMN
    persistentObjectToTableNameMap.put(DecisionRequirementsDefinitionEntity.class, "ACT_RE_DECISION_REQ_DEF");
    persistentObjectToTableNameMap.put(DecisionDefinitionEntity.class, "ACT_RE_DECISION_DEF");
    persistentObjectToTableNameMap.put(HistoricDecisionInputInstanceEntity.class, "ACT_HI_DEC_IN");
    persistentObjectToTableNameMap.put(HistoricDecisionOutputInstanceEntity.class, "ACT_HI_DEC_OUT");

    // history
    persistentObjectToTableNameMap.put(CommentEntity.class, "ACT_HI_COMMENT");

    persistentObjectToTableNameMap.put(HistoricActivityInstanceEntity.class, "ACT_HI_ACTINST");
    persistentObjectToTableNameMap.put(AttachmentEntity.class, "ACT_HI_ATTACHMENT");
    persistentObjectToTableNameMap.put(HistoricProcessInstanceEntity.class, "ACT_HI_PROCINST");
    persistentObjectToTableNameMap.put(HistoricTaskInstanceEntity.class, "ACT_HI_TASKINST");
    persistentObjectToTableNameMap.put(HistoricJobLogEventEntity.class, "ACT_HI_JOB_LOG");
    persistentObjectToTableNameMap.put(HistoricIncidentEventEntity.class, "ACT_HI_INCIDENT");
    persistentObjectToTableNameMap.put(HistoricBatchEntity.class, "ACT_HI_BATCH");
    persistentObjectToTableNameMap.put(HistoricExternalTaskLogEntity.class, "ACT_HI_EXT_TASK_LOG");

    persistentObjectToTableNameMap.put(HistoricCaseInstanceEntity.class, "ACT_HI_CASEINST");
    persistentObjectToTableNameMap.put(HistoricCaseActivityInstanceEntity.class, "ACT_HI_CASEACTINST");
    persistentObjectToTableNameMap.put(HistoricIdentityLinkLogEntity.class, "ACT_HI_IDENTITYLINK");
    // a couple of stuff goes to the same table
    persistentObjectToTableNameMap.put(HistoricFormPropertyEntity.class, "ACT_HI_DETAIL");
    persistentObjectToTableNameMap.put(HistoricVariableInstanceEntity.class, "ACT_HI_VARINST");
    persistentObjectToTableNameMap.put(HistoricDetailEventEntity.class, "ACT_HI_DETAIL");

    persistentObjectToTableNameMap.put(HistoricDecisionInstanceEntity.class, "ACT_HI_DECINST");
    persistentObjectToTableNameMap.put(UserOperationLogEntryEventEntity.class, "ACT_HI_OP_LOG");


    // Identity module
    persistentObjectToTableNameMap.put(GroupEntity.class, "ACT_ID_GROUP");
    persistentObjectToTableNameMap.put(MembershipEntity.class, "ACT_ID_MEMBERSHIP");
    persistentObjectToTableNameMap.put(TenantEntity.class, "ACT_ID_TENANT");
    persistentObjectToTableNameMap.put(TenantMembershipEntity.class, "ACT_ID_TENANT_MEMBER");
    persistentObjectToTableNameMap.put(UserEntity.class, "ACT_ID_USER");
    persistentObjectToTableNameMap.put(IdentityInfoEntity.class, "ACT_ID_INFO");
    persistentObjectToTableNameMap.put(AuthorizationEntity.class, "ACT_RU_AUTHORIZATION");


    // general
    persistentObjectToTableNameMap.put(PropertyEntity.class, "ACT_GE_PROPERTY");
    persistentObjectToTableNameMap.put(ByteArrayEntity.class, "ACT_GE_BYTEARRAY");
    persistentObjectToTableNameMap.put(ResourceEntity.class, "ACT_GE_BYTEARRAY");
    persistentObjectToTableNameMap.put(SchemaLogEntryEntity.class, "ACT_GE_SCHEMA_LOG");
    persistentObjectToTableNameMap.put(FilterEntity.class, "ACT_RU_FILTER");

    // and now the map for the API types (does not cover all cases)
    apiTypeToTableNameMap.put(Task.class, "ACT_RU_TASK");
    apiTypeToTableNameMap.put(Execution.class, "ACT_RU_EXECUTION");
    apiTypeToTableNameMap.put(ProcessInstance.class, "ACT_RU_EXECUTION");
    apiTypeToTableNameMap.put(ProcessDefinition.class, "ACT_RE_PROCDEF");
    apiTypeToTableNameMap.put(Deployment.class, "ACT_RE_DEPLOYMENT");
    apiTypeToTableNameMap.put(Job.class, "ACT_RU_JOB");
    apiTypeToTableNameMap.put(Incident.class, "ACT_RU_INCIDENT");
    apiTypeToTableNameMap.put(Filter.class, "ACT_RU_FILTER");


    // history
    apiTypeToTableNameMap.put(HistoricProcessInstance.class, "ACT_HI_PROCINST");
    apiTypeToTableNameMap.put(HistoricActivityInstance.class, "ACT_HI_ACTINST");
    apiTypeToTableNameMap.put(HistoricDetail.class, "ACT_HI_DETAIL");
    apiTypeToTableNameMap.put(HistoricVariableUpdate.class, "ACT_HI_DETAIL");
    apiTypeToTableNameMap.put(HistoricFormProperty.class, "ACT_HI_DETAIL");
    apiTypeToTableNameMap.put(HistoricTaskInstance.class, "ACT_HI_TASKINST");
    apiTypeToTableNameMap.put(HistoricVariableInstance.class, "ACT_HI_VARINST");


    apiTypeToTableNameMap.put(HistoricCaseInstance.class, "ACT_HI_CASEINST");
    apiTypeToTableNameMap.put(HistoricCaseActivityInstance.class, "ACT_HI_CASEACTINST");

    apiTypeToTableNameMap.put(HistoricDecisionInstance.class, "ACT_HI_DECINST");

    // TODO: Identity skipped for the moment as no SQL injection is provided here
  }

  public Map<String, Long> getTableCount() {
    Map<String, Long> tableCount = new HashMap<>();
    try {
      for (String tableName: getDbEntityManager().getTableNamesPresentInDatabase()) {
        tableCount.put(tableName, getTableCount(tableName));
      }
      LOG.countRowsPerProcessEngineTable(tableCount);
    } catch (Exception e) {
      throw LOG.countTableRowsException(e);
    }
    return tableCount;
  }

  protected long getTableCount(String tableName) {
    LOG.selectTableCountForTable(tableName);
    Long count = (Long) getDbEntityManager().selectOne("selectTableCount",
            Collections.singletonMap("tableName", tableName));
    return count;
  }

  @SuppressWarnings("unchecked")
  public TablePage getTablePage(TablePageQueryImpl tablePageQuery) {

    TablePage tablePage = new TablePage();

    @SuppressWarnings("rawtypes")
    List tableData = getDbEntityManager().selectList("selectTableData", tablePageQuery);

    tablePage.setTableName(tablePageQuery.getTableName());
    tablePage.setTotal(getTableCount(tablePageQuery.getTableName()));
    tablePage.setRows(tableData);
    tablePage.setFirstResult(tablePageQuery.getFirstResult());

    return tablePage;
  }

  public List<Class<? extends DbEntity>> getEntities(String tableName) {
    String databaseTablePrefix = getDbSqlSession().getDbSqlSessionFactory().getDatabaseTablePrefix();
    List<Class<? extends DbEntity>> entities = new ArrayList<>();

    Set<Class<? extends DbEntity>> entityClasses = persistentObjectToTableNameMap.keySet();
    for (Class<? extends DbEntity> entityClass : entityClasses) {
      String entityTableName = persistentObjectToTableNameMap.get(entityClass);
      if ((databaseTablePrefix + entityTableName).equals(tableName)) {
        entities.add(entityClass);
      }
    }
    return entities;
  }

  public String getTableName(Class<?> entityClass, boolean withPrefix) {
    String databaseTablePrefix = getDbSqlSession().getDbSqlSessionFactory().getDatabaseTablePrefix();
    String tableName = null;

    if (DbEntity.class.isAssignableFrom(entityClass)) {
      tableName = persistentObjectToTableNameMap.get(entityClass);
    }
    else {
      tableName = apiTypeToTableNameMap.get(entityClass);
    }
    if (withPrefix) {
      return databaseTablePrefix + tableName;
    }
    else {
      return tableName;
    }
  }

  public TableMetaData getTableMetaData(String tableName) {
    TableMetaData result = new TableMetaData();
    ResultSet resultSet = null;

    try {
      try {
        result.setTableName(tableName);
        DatabaseMetaData metaData = getDbSqlSession()
            .getSqlSession()
            .getConnection()
            .getMetaData();

        if (DatabaseUtil.checkDatabaseType(DbSqlSessionFactory.POSTGRES, DbSqlSessionFactory.CRDB)) {
          tableName = tableName.toLowerCase();
        }

        String databaseSchema = getDbSqlSession().getDbSqlSessionFactory().getDatabaseSchema();
        tableName = getDbSqlSession().prependDatabaseTablePrefix(tableName);

        resultSet = metaData.getColumns(null, databaseSchema, tableName, null);
        while(resultSet.next()) {
          String name = resultSet.getString("COLUMN_NAME").toUpperCase();
          String type = resultSet.getString("TYPE_NAME").toUpperCase();
          result.addColumnMetaData(name, type);
        }

      } catch (SQLException se) {
        throw se;
      } finally {
        if (resultSet != null) {
          resultSet.close();
        }
      }
    } catch (Exception e) {
      throw LOG.retrieveMetadataException(e);
    }

    if(result.getColumnNames().size() == 0) {
      // According to API, when a table doesn't exist, null should be returned
      result = null;
    }
    return result;
  }

}
