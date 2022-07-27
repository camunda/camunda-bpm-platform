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
package org.camunda.bpm.engine.test.errorcode;

import ch.qos.logback.classic.Level;
import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.OptimisticLockingException;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.Permission;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.authorization.TaskPermissions;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.db.sql.DbSqlSessionFactory;
import org.camunda.bpm.engine.impl.errorcode.BuiltinExceptionCode;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.test.RequiredDatabase;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.commons.testing.ProcessEngineLoggingRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.camunda.bpm.engine.authorization.Authorization.AUTH_TYPE_GRANT;

public class ExceptionBuiltinCodesTest {

  @Rule
  public ProcessEngineLoggingRule loggingRule = new ProcessEngineLoggingRule()
      .watch("org.camunda.bpm.engine.cmd")
      .level(Level.WARN);

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule engineTestRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(engineTestRule);

  protected RuntimeService runtimeService;
  protected IdentityService identityService;
  protected AuthorizationService authorizationService;

  @Before
  public void assignServices() {
    runtimeService = engineRule.getRuntimeService();
    identityService = engineRule.getIdentityService();
    authorizationService = engineRule.getAuthorizationService();
  }

  @After
  public void clear() {
    engineRule.getIdentityService().deleteUser("kermit");
    engineRule.getAuthorizationService().createAuthorizationQuery().list()
        .forEach(authorization -> authorizationService.deleteAuthorization(authorization.getId()));
    engineRule.getRuntimeService().createProcessInstanceQuery().processInstanceBusinessKey("sub-process").list()
        .forEach(pi -> runtimeService.deleteProcessInstance(pi.getProcessInstanceId(), ""));
  }

  @Test
  public void shouldHaveColumnSizeTooSmallErrorCode() {
    // given
    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess("process")
        .startEvent()
        .endEvent()
        .done();

    engineTestRule.deploy(modelInstance);

    String businessKey = generateString(1_000);

    // when/then
    assertThatThrownBy(() -> runtimeService.startProcessInstanceByKey("process", businessKey))
        .extracting("code")
          .contains(BuiltinExceptionCode.COLUMN_SIZE_TOO_SMALL.getCode());
  }

  @Test
  public void shouldHaveDefaultErrorCodeUniqueKeyConstraintPersistenceExceptionNotCovered() {
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

    // when/then
    assertThatThrownBy(() -> authorizationService.saveAuthorization(authorizationTwo))
        .extracting("code")
        .contains(BuiltinExceptionCode.FALLBACK.getCode());
    assertThat(loggingRule.getLog()).isEmpty();
  }

  @Test
  public void shouldHaveOleErrorCode() {
    // given
    User user = identityService.newUser("kermit");
    identityService.saveUser(user);

    User user1 = identityService.createUserQuery().singleResult();
    User user2 = identityService.createUserQuery().singleResult();

    user1.setFirstName("name one");
    identityService.saveUser(user1);

    user2.setFirstName("name two");

    // when/then
    assertThatThrownBy(() -> identityService.saveUser(user2))
        .isInstanceOf(OptimisticLockingException.class)
        .extracting("code")
        .contains(BuiltinExceptionCode.OPTIMISTIC_LOCKING.getCode());
  }

  @Test
  public void shouldHaveForeignKeyConstraintViolationCode() {
    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess("calling")
        .startEvent()
        .callActivity()
          .calledElement("called")
          .camundaInBusinessKey("sub-process")
        .endEvent()
        .done();

    engineTestRule.deploy(modelInstance);

    modelInstance = Bpmn.createExecutableProcess("called")
        .startEvent()
        .userTask()
        .endEvent()
        .done();

    engineTestRule.deploy(modelInstance);

    String processInstanceId = runtimeService.startProcessInstanceByKey("calling").getId();

    List<Execution> executions = runtimeService.createExecutionQuery().list();
    executions.forEach((execution -> {
      ((ExecutionEntity) execution).setCachedEntityState(0);

      engineRule.getProcessEngineConfiguration()
          .getCommandExecutorTxRequired()
          .execute((Command<Void>) commandContext -> {
            commandContext.getDbEntityManager().merge(((ExecutionEntity) execution));
            return null;
          });
    }));

    assertThatThrownBy(() -> runtimeService.deleteProcessInstance(processInstanceId, ""))
        .isInstanceOf(ProcessEngineException.class)
        .extracting("code")
        .contains(BuiltinExceptionCode.FOREIGN_KEY_CONSTRAINT_VIOLATION.getCode());
  }

  // helper ////////////////////////////////////////////////////////////////////////////////////////

  protected String generateString(int size) {
    return new String(new char[size]).replace('\0', 'a');
  }

}
