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
package org.camunda.bpm.engine.test.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.camunda.bpm.engine.authorization.Authorization.AUTH_TYPE_GRANT;
import static org.camunda.bpm.engine.impl.util.ExceptionUtil.PERSISTENCE_EXCEPTION_MESSAGE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.exceptions.PersistenceException;
import org.assertj.core.api.ThrowableAssert;
import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.FilterService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.Permission;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.authorization.TaskPermissions;
import org.camunda.bpm.engine.filter.Filter;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.db.sql.DbSqlSessionFactory;
import org.camunda.bpm.engine.impl.history.event.HistoricDetailEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricVariableUpdateEventEntity;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.test.RequiredDatabase;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

public class SuppressSqlExceptionsTest {

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule engineTestRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(engineTestRule);

  protected RuntimeService runtimeService;
  protected ManagementService managementService;
  protected RepositoryService repositoryService;
  protected FilterService filterService;
  protected IdentityService identityService;
  protected AuthorizationService authorizationService;

  protected ProcessEngineConfigurationImpl engineConfig;
  protected boolean batchProcessingEnabled;

  protected Map<String, String> keptStatementMappings;

  @Before
  public void assignServices() {
    runtimeService = engineRule.getRuntimeService();
    managementService = engineRule.getManagementService();
    repositoryService = engineRule.getRepositoryService();
    filterService = engineRule.getFilterService();
    identityService = engineRule.getIdentityService();
    authorizationService = engineRule.getAuthorizationService();

    engineConfig = engineRule.getProcessEngineConfiguration();
    batchProcessingEnabled = engineConfig.isJdbcBatchProcessing();
  }

  @Before
  public void keepStatementMappings() {
    Map<String, String> statementMappings = engineRule.getProcessEngineConfiguration()
        .getDbSqlSessionFactory()
        .getStatementMappings();
    if (keptStatementMappings == null) {
      keptStatementMappings = statementMappings;
    }

    engineRule.getProcessEngineConfiguration()
        .getDbSqlSessionFactory()
        .setStatementMappings(keptStatementMappings == null ?
            new HashMap<>() :
            new HashMap<>(keptStatementMappings));
  }

  @After
  public void resetStatementMappings() {
    engineConfig.getDbSqlSessionFactory().setStatementMappings(keptStatementMappings);
  }

  @Test
  public void shouldThrowExceptionOnSelectingById() {
    // given
    failForSqlStatement("selectJob");

    Iterator<Throwable> exceptionsByHierarchy = catchExceptionHierarchy(() -> {
      // when
      managementService.executeJob("anId");
    });

    // then
    assertThat(exceptionsByHierarchy.next())
        .isInstanceOf(ProcessEngineException.class)
        .hasMessage(PERSISTENCE_EXCEPTION_MESSAGE);

    assertThat(exceptionsByHierarchy.next())
        .isInstanceOf(PersistenceException.class);
  }

  @Test
  public void shouldThrowExceptionOnSelectionWithListInReturn() {
    // given

    Iterator<Throwable> exceptionsByHierarchy = catchExceptionHierarchy(() -> {
      // when
      runtimeService.createNativeProcessInstanceQuery()
          .sql("foo")
          .list();
    });

    // then
    assertThat(exceptionsByHierarchy.next())
        .isInstanceOf(ProcessEngineException.class)
        .hasMessage(PERSISTENCE_EXCEPTION_MESSAGE);

    assertThat(exceptionsByHierarchy.next())
        .isInstanceOf(PersistenceException.class);
  }

  @Test
  public void shouldThrowExceptionOnSingleRowSelection() {
    // given
    failForSqlStatement("selectDeploymentCountByQueryCriteria");

    Iterator<Throwable> exceptionsByHierarchy = catchExceptionHierarchy(() -> {
      // when
      repositoryService.createDeploymentQuery().count();
    });

    // then
    assertThat(exceptionsByHierarchy.next())
        .isInstanceOf(ProcessEngineException.class)
        .hasMessage(PERSISTENCE_EXCEPTION_MESSAGE);

    assertThat(exceptionsByHierarchy.next())
        .isInstanceOf(PersistenceException.class);
  }

  @RequiredHistoryLevel(ProcessEngineConfigurationImpl.HISTORY_ACTIVITY)
  @RequiredDatabase(excludes = DbSqlSessionFactory.MARIADB)
  @Test
  public void shouldThrowExceptionOnInsert_ColumnSizeExceeded() {
    // given
    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess("process")
        .startEvent()
        .endEvent()
        .done();

    engineTestRule.deploy(modelInstance);

    String businessKey = generateString(1_000);

    Iterator<Throwable> exceptionsByHierarchy = catchExceptionHierarchy(() -> {
      // when
      runtimeService.startProcessInstanceByKey("process", businessKey);
    });

    // then
    assertThat(exceptionsByHierarchy.next())
        .isInstanceOf(ProcessEngineException.class)
        .hasMessage(PERSISTENCE_EXCEPTION_MESSAGE);

    assertThat(exceptionsByHierarchy.next())
        .isInstanceOf(ProcessEngineException.class)
        .hasMessageContaining("ENGINE-03004 Exception while executing Database Operation 'INSERT " +
            "HistoricProcessInstanceEventEntity")
        .hasMessageContaining("Flush summary: ");

    assertThat(exceptionsByHierarchy.next())
        .isInstanceOf(PersistenceException.class)
        .hasMessageContaining("insertHistoricProcessInstanceEvent");
  }

  @Test
  public void shouldThrowExceptionOnInsert_UniqueConstraintViolated() {
    // given
    Authorization authorizationOne = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
    authorizationOne.setGroupId("aUserId");
    authorizationOne.setPermissions(new Permission[] { TaskPermissions.READ });
    authorizationOne.setResourceId("foo");
    authorizationOne.setResource(Resources.TASK);

    authorizationService.saveAuthorization(authorizationOne);

    Authorization authorizationTwo = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
    authorizationTwo.setGroupId("aUserId");
    authorizationTwo.setPermissions(new Permission[]{TaskPermissions.READ});
    authorizationTwo.setResourceId("foo");
    authorizationTwo.setResource(Resources.TASK);

    Iterator<Throwable> exceptionsByHierarchy = catchExceptionHierarchy(() -> {
      // when
      authorizationService.saveAuthorization(authorizationTwo);
    });

    // then
    assertThat(exceptionsByHierarchy.next())
        .isInstanceOf(ProcessEngineException.class)
        .hasMessage(PERSISTENCE_EXCEPTION_MESSAGE);

    assertThat(exceptionsByHierarchy.next())
        .isInstanceOf(ProcessEngineException.class)
        .hasMessageContaining("ENGINE-03004 Exception while executing Database Operation 'INSERT " +
            "AuthorizationEntity")
        .hasMessageContaining("Flush summary: ");

    assertThat(exceptionsByHierarchy.next())
        .isInstanceOf(PersistenceException.class)
        .hasMessageContaining("insertAuthorization");

    // clear
    authorizationService.deleteAuthorization(authorizationOne.getId());
  }

  @Test
  public void shouldThrowExceptionOnDelete() {
    // given
    failForSqlStatement("deleteFilter");

    Filter foo = filterService.newTaskFilter("foo");
    filterService.saveFilter(foo);

    Iterator<Throwable> exceptionsByHierarchy = catchExceptionHierarchy(() -> {
      // when
      filterService.deleteFilter(foo.getId());
    });

    // then
    assertThat(exceptionsByHierarchy.next())
        .isInstanceOf(ProcessEngineException.class)
        .hasMessage(PERSISTENCE_EXCEPTION_MESSAGE);

    assertThat(exceptionsByHierarchy.next())
        .isInstanceOf(ProcessEngineException.class)
        .hasMessageContaining(batchProcessingEnabled ?
            "ENGINE-03083 Unexpected exception while executing database " +
                "operations with message '" :
            "ENGINE-03004 Exception while executing Database Operation '")
        .hasMessageContaining("Flush summary: ");

    assertThat(exceptionsByHierarchy.next())
        .isInstanceOf(PersistenceException.class);

    // clear
    resetStatementMappings();
    filterService.deleteFilter(foo.getId());
  }

  @Test
  public void shouldThrowExceptionOnBulkDelete() {
    // given
    failForSqlStatement("deleteMembershipsByUserId");

    User user = identityService.newUser("foo");
    identityService.saveUser(user);

    Iterator<Throwable> exceptionsByHierarchy = catchExceptionHierarchy(() -> {
      // when
      identityService.deleteUser("foo");
    });

    // then
    assertThat(exceptionsByHierarchy.next())
        .isInstanceOf(ProcessEngineException.class)
        .hasMessage(PERSISTENCE_EXCEPTION_MESSAGE);

    assertThat(exceptionsByHierarchy.next())
        .isInstanceOf(ProcessEngineException.class)
        .hasMessageContaining( batchProcessingEnabled ?
            "ENGINE-03083 Unexpected exception while executing " +
                "database operations with message '" :
            "ENGINE-03004 Exception while executing Database Operation '")
        .hasMessageContaining("Flush summary: ");

    assertThat(exceptionsByHierarchy.next())
        .isInstanceOf(PersistenceException.class);

    // clear
    resetStatementMappings();
    engineRule.getIdentityService().deleteUser("foo");
  }


  @Test
  @RequiredDatabase(excludes = DbSqlSessionFactory.MARIADB)
  public void shouldThrowExceptionOnUpdate() {
    // given
    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess("process")
        .startEvent()
        .userTask()
        .endEvent()
        .done();

    engineTestRule.deploy(modelInstance);

    String processInstanceId = runtimeService.startProcessInstanceByKey("process",
        Variables.putValue("foo", "bar")).getId();

    String variableValue = generateString(10_000);

    CommandExecutor commandExecutor = engineConfig.getCommandExecutorTxRequired();

    Iterator<Throwable> exceptionsByHierarchy = catchExceptionHierarchy(() -> {
      // when
      commandExecutor.execute(c -> {
        runtimeService.setVariable(processInstanceId, "foo", variableValue);

        // otherwise the command fails on the INSERT of the historic detail,
        // but we want to provoke an exception on an UPDATE statement
        trimHistoricDetailValue(c);

        return null;
      });

    });

    // then
    assertThat(exceptionsByHierarchy.next())
        .isInstanceOf(ProcessEngineException.class)
        .hasMessageContaining(PERSISTENCE_EXCEPTION_MESSAGE);

    assertThat(exceptionsByHierarchy.next())
        .isInstanceOf(ProcessEngineException.class)
        .hasMessageContaining("ENGINE-03004 Exception while executing Database Operation '" +
            "UPDATE VariableInstanceEntity")
        .hasMessageContaining("Flush summary: ");

    assertThat(exceptionsByHierarchy.next())
        .isInstanceOf(PersistenceException.class);

  }

  private void trimHistoricDetailValue(CommandContext c) {
    List<HistoricDetailEventEntity> historicDetails =
        c.getDbEntityManager().getCachedEntitiesByType(HistoricDetailEventEntity.class);

    // only if history produces the detail
    if (!historicDetails.isEmpty()) {
      HistoricVariableUpdateEventEntity detail = (HistoricVariableUpdateEventEntity) historicDetails.get(0);
      detail.setTextValue("");
    }
  }

  @Test
  public void shouldThrowExceptionOnBulkUpdate() {
    // given
    failForSqlStatement("updateProcessDefinitionSuspensionStateByParameters");

    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess("process")
        .startEvent()
        .endEvent()
        .done();

    engineTestRule.deploy(modelInstance);

    Iterator<Throwable> exceptionsByHierarchy = catchExceptionHierarchy(() -> {
      // when
      repositoryService.updateProcessDefinitionSuspensionState()
          .byProcessDefinitionKey("process")
          .suspend();
    });

    // then
    assertThat(exceptionsByHierarchy.next())
        .isInstanceOf(ProcessEngineException.class)
        .hasMessage(PERSISTENCE_EXCEPTION_MESSAGE);

    assertThat(exceptionsByHierarchy.next())
        .isInstanceOf(ProcessEngineException.class)
        .hasMessageContaining(batchProcessingEnabled ?
            "ENGINE-03083 Unexpected exception while executing database " +
                "operations with message '" :
            "ENGINE-03004 Exception while executing Database Operation '")
        .hasMessageContaining("Flush summary: ");

    assertThat(exceptionsByHierarchy.next())
        .isInstanceOf(PersistenceException.class);
  }

  /**
   * This test expects an exception when performing a pessimistic lock
   * in this scenario. Since pessimistic locks are disabled on H2 and CRDB,
   * this test will fail, and it needs to be excluded from the test suite
   * for these two databases.
   */
  @Test
  @RequiredDatabase(excludes = { DbSqlSessionFactory.H2, DbSqlSessionFactory.CRDB })
  public void shouldThrowExceptionOnLock() {
    // given
    failForSqlStatement("lockDeploymentLockProperty");

    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess("process")
        .startEvent()
        .userTask()
        .endEvent()
        .done();

    Iterator<Throwable> exceptionsByHierarchy = catchExceptionHierarchy(() -> {
      // when
      repositoryService.createDeployment()
          .addModelInstance("process.bpmn", modelInstance)
          .deploy();
    });

    // then
    assertThat(exceptionsByHierarchy.next())
        .isInstanceOf(ProcessEngineException.class)
        .hasMessage(PERSISTENCE_EXCEPTION_MESSAGE);

    assertThat(exceptionsByHierarchy.next())
        .isInstanceOf(PersistenceException.class);
  }

  // helper ////////////////////////////////////////////////////////////////////////////////////////

  protected Iterator<Throwable> catchExceptionHierarchy(ThrowableAssert.ThrowingCallable callable) {
    Throwable throwable = catchThrowable(callable);
    List<Throwable> exceptions = new ArrayList<>();
    exceptions.add(throwable);
    Throwable cause = throwable;
    do {
      cause = cause.getCause();
      if (cause != null) {
        exceptions.add(cause);
      }

    } while(cause != null);

    return exceptions.iterator();
  }

  protected void failForSqlStatement(String statement) {
    Map<String, String> statementMappings = engineConfig.getDbSqlSessionFactory()
        .getStatementMappings();
    statementMappings.put(statement, "does-not-exist");
  }

  protected String generateString(int size) {
    return new String(new char[size]).replace('\0', 'a');
  }

}
