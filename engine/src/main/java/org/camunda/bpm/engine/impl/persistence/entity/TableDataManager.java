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

package org.camunda.bpm.engine.impl.persistence.entity;

import java.sql.*;
import java.util.*;
import java.util.logging.Logger;

import org.apache.ibatis.session.RowBounds;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.filter.Filter;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.history.HistoricCaseActivityInstance;
import org.camunda.bpm.engine.history.HistoricCaseInstance;
import org.camunda.bpm.engine.history.HistoricDetail;
import org.camunda.bpm.engine.history.HistoricFormProperty;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.history.HistoricVariableUpdate;
import org.camunda.bpm.engine.impl.TablePageQueryImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.db.sql.DbSqlSession;
import org.camunda.bpm.engine.impl.db.sql.DbSqlSessionFactory;
import org.camunda.bpm.engine.impl.history.event.HistoricDetailEventEntity;
import org.camunda.bpm.engine.impl.persistence.AbstractManager;
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

  private static Logger log = Logger.getLogger(TableDataManager.class.getName());

  public static Map<Class<?>, String> apiTypeToTableNameMap = new HashMap<Class<?>, String>();
  public static Map<Class<? extends DbEntity>, String> persistentObjectToTableNameMap = new HashMap<Class<? extends DbEntity>, String>();

  static {
    // runtime
    persistentObjectToTableNameMap.put(TaskEntity.class, "ACT_RU_TASK");
    persistentObjectToTableNameMap.put(ExecutionEntity.class, "ACT_RU_EXECUTION");
    persistentObjectToTableNameMap.put(IdentityLinkEntity.class, "ACT_RU_IDENTITYLINK");
    persistentObjectToTableNameMap.put(VariableInstanceEntity.class, "ACT_RU_VARIABLE");

    persistentObjectToTableNameMap.put(JobEntity.class, "ACT_RU_JOB");
    persistentObjectToTableNameMap.put(MessageEntity.class, "ACT_RU_JOB");
    persistentObjectToTableNameMap.put(TimerEntity.class, "ACT_RU_JOB");

    persistentObjectToTableNameMap.put(IncidentEntity.class, "ACT_RU_INCIDENT");

    persistentObjectToTableNameMap.put(EventSubscriptionEntity.class, "ACT_RU_EVENT_SUBSCRIPTION");
    persistentObjectToTableNameMap.put(CompensateEventSubscriptionEntity.class, "ACT_RU_EVENT_SUBSCRIPTION");
    persistentObjectToTableNameMap.put(MessageEventSubscriptionEntity.class, "ACT_RU_EVENT_SUBSCRIPTION");
    persistentObjectToTableNameMap.put(SignalEventSubscriptionEntity.class, "ACT_RU_EVENT_SUBSCRIPTION");

    persistentObjectToTableNameMap.put(FilterEntity.class, "ACT_RU_FILTER");

    // repository
    persistentObjectToTableNameMap.put(DeploymentEntity.class, "ACT_RE_DEPLOYMENT");
    persistentObjectToTableNameMap.put(ProcessDefinitionEntity.class, "ACT_RE_PROCDEF");

    // history
    persistentObjectToTableNameMap.put(CommentEntity.class, "ACT_HI_COMMENT");

    persistentObjectToTableNameMap.put(HistoricActivityInstanceEntity.class, "ACT_HI_ACTINST");
    persistentObjectToTableNameMap.put(AttachmentEntity.class, "ACT_HI_ATTACHMENT");
    persistentObjectToTableNameMap.put(HistoricProcessInstanceEntity.class, "ACT_HI_PROCINST");
    persistentObjectToTableNameMap.put(HistoricTaskInstanceEntity.class, "ACT_HI_TASKINST");

    persistentObjectToTableNameMap.put(HistoricCaseInstanceEntity.class, "ACT_HI_CASEINST");
    persistentObjectToTableNameMap.put(HistoricCaseActivityInstanceEntity.class, "ACT_HI_CASEACTINST");

    // a couple of stuff goes to the same table
    persistentObjectToTableNameMap.put(HistoricFormPropertyEntity.class, "ACT_HI_DETAIL");
    persistentObjectToTableNameMap.put(HistoricVariableInstanceEntity.class, "ACT_HI_DETAIL");
    persistentObjectToTableNameMap.put(HistoricDetailEventEntity.class, "ACT_HI_DETAIL");


    // Identity module
    persistentObjectToTableNameMap.put(GroupEntity.class, "ACT_ID_GROUP");
    persistentObjectToTableNameMap.put(MembershipEntity.class, "ACT_ID_MEMBERSHIP");
    persistentObjectToTableNameMap.put(UserEntity.class, "ACT_ID_USER");
    persistentObjectToTableNameMap.put(IdentityInfoEntity.class, "ACT_ID_INFO");

    // general
    persistentObjectToTableNameMap.put(PropertyEntity.class, "ACT_GE_PROPERTY");
    persistentObjectToTableNameMap.put(ByteArrayEntity.class, "ACT_GE_BYTEARRAY");
    persistentObjectToTableNameMap.put(ResourceEntity.class, "ACT_GE_BYTEARRAY");

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

    // TODO: Identity skipped for the moment as no SQL injection is provided here
  }

  public Map<String, Long> getTableCount() {
    Map<String, Long> tableCount = new HashMap<String, Long>();
    try {
      for (String tableName: getTablesPresentInDatabase()) {
        tableCount.put(tableName, getTableCount(tableName));
      }
      log.fine("Number of rows per process engine table: "+tableCount);
    } catch (Exception e) {
      throw new ProcessEngineException("couldn't get table counts", e);
    }
    return tableCount;
  }

  public List<String> getTablesPresentInDatabase() {
    List<String> tableNames = new ArrayList<String>();

    try {
      ResultSet tablesRs = null;

      try {
        if (DbSqlSessionFactory.ORACLE.equals(getDbSqlSession().getDbSqlSessionFactory().getDatabaseType())) {
          tableNames = getTablesPresentInOracleDatabase();
        } else {
          Connection connection = getDbSqlSession().getSqlSession().getConnection();
          DatabaseMetaData databaseMetaData = connection.getMetaData();

          log.fine("retrieving process engine tables from jdbc metadata");
          String databaseTablePrefix = getDbSqlSession().getDbSqlSessionFactory().getDatabaseTablePrefix();
          String tableNameFilter = databaseTablePrefix+"ACT_%";

          if (DbSqlSessionFactory.POSTGRES.equals(getDbSqlSession().getDbSqlSessionFactory().getDatabaseType())) {
            tableNameFilter = databaseTablePrefix+"act_%";
          }
          tablesRs = databaseMetaData.getTables(null, null, tableNameFilter, DbSqlSession.JDBC_METADATA_TABLE_TYPES);

          while (tablesRs.next()) {
            String tableName = tablesRs.getString("TABLE_NAME");
            tableName = tableName.toUpperCase();
            tableNames.add(tableName);
            log.fine("  retrieved process engine table name "+tableName);
          }
        }
      } catch (SQLException se) {
        throw se;
      } finally {
        if (tablesRs != null) {
          tablesRs.close();
        }
      }
    } catch (Exception e) {
      throw new ProcessEngineException("couldn't get process engine table names: "+e.getMessage(), e);
    }

    return tableNames;
  }

  protected List<String> getTablesPresentInOracleDatabase() throws SQLException {
    List<String> tableNames = new ArrayList<String>();
    Connection connection = null;
    PreparedStatement prepStat = null;
    ResultSet tablesRs = null;
    String selectTableNamesFromOracle = "SELECT table_name FROM all_tables WHERE table_name LIKE ?";
    String databaseTablePrefix = getDbSqlSession().getDbSqlSessionFactory().getDatabaseTablePrefix();

    try {
        connection = Context.getProcessEngineConfiguration().getDataSource().getConnection();
        prepStat = connection.prepareStatement(selectTableNamesFromOracle);
        prepStat.setString(1, databaseTablePrefix + "ACT_%");
        log.fine("retrieving process engine tables from oracle all_tables");
        tablesRs = prepStat.executeQuery();
        while (tablesRs.next()) {
          String tableName = tablesRs.getString("TABLE_NAME");
          tableName = tableName.toUpperCase();
          tableNames.add(tableName);
          log.fine("  retrieved process engine table name "+tableName);
        }
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

  protected long getTableCount(String tableName) {
    log.fine("selecting table count for "+tableName);
    Long count = (Long) getDbEntityManager().selectOne("selectTableCount",
            Collections.singletonMap("tableName", tableName));
    return count;
  }

  @SuppressWarnings("unchecked")
  public TablePage getTablePage(TablePageQueryImpl tablePageQuery, int firstResult, int maxResults) {

    TablePage tablePage = new TablePage();

    @SuppressWarnings("rawtypes")
    List tableData = getDbSqlSession().getSqlSession()
      .selectList("selectTableData", tablePageQuery, new RowBounds(firstResult, maxResults));

    tablePage.setTableName(tablePageQuery.getTableName());
    tablePage.setTotal(getTableCount(tablePageQuery.getTableName()));
    tablePage.setRows((List<Map<String,Object>>)tableData);
    tablePage.setFirstResult(firstResult);

    return tablePage;
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

        if (DbSqlSessionFactory.POSTGRES.equals(getDbSqlSession().getDbSqlSessionFactory().getDatabaseType())) {
          tableName = tableName.toLowerCase();
        }

        resultSet = metaData.getColumns(null, null, tableName, null);
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
      throw new ProcessEngineException("Could not retrieve database metadata: " + e.getMessage());
    }

    if(result.getColumnNames().size() == 0) {
      // According to API, when a table doesn't exist, null should be returned
      result = null;
    }
    return result;
  }

}
