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
package org.camunda.bpm.engine.test.api.authorization;

import static org.camunda.bpm.engine.authorization.Authorization.ANY;
import static org.camunda.bpm.engine.authorization.Permissions.READ;
import static org.camunda.bpm.engine.authorization.Permissions.READ_INSTANCE;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_DEFINITION;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_INSTANCE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.camunda.bpm.engine.impl.AbstractQuery;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ExecutionQuery;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Roman Smirnov
 *
 */
public class ExecutionAuthorizationTest extends AuthorizationTest {

  protected static final String ONE_TASK_PROCESS_KEY = "oneTaskProcess";
  protected static final String MESSAGE_BOUNDARY_PROCESS_KEY = "messageBoundaryProcess";

  @Before
  public void setUp() throws Exception {
    testRule.deploy(
        "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml",
        "org/camunda/bpm/engine/test/api/authorization/messageBoundaryEventProcess.bpmn20.xml");
    super.setUp();
  }

  @Test
  public void testSimpleQueryWithoutAuthorization() {
    // given
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);

    // when
    ExecutionQuery query = runtimeService.createExecutionQuery();

    // then
    verifyQueryResults(query, 0);
  }

  @Test
  public void testSimpleQueryWithReadPermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(ONE_TASK_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, READ);

    // when
    ExecutionQuery query = runtimeService.createExecutionQuery();

    // then
    verifyQueryResults(query, 1);

    Execution execution = query.singleResult();
    assertNotNull(execution);
    assertEquals(processInstanceId, execution.getProcessInstanceId());
  }

  @Test
  public void testSimpleQueryWithReadPermissionOnAnyProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(ONE_TASK_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, READ);

    // when
    ExecutionQuery query = runtimeService.createExecutionQuery();

    // then
    verifyQueryResults(query, 1);

    Execution execution = query.singleResult();
    assertNotNull(execution);
    assertEquals(processInstanceId, execution.getProcessInstanceId());
  }

  @Test
  public void testSimpleQueryWithMultiple() {
    // given
    String processInstanceId = startProcessInstanceByKey(ONE_TASK_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, READ);
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, READ);

    // when
    ExecutionQuery query = runtimeService.createExecutionQuery();

    // then
    verifyQueryResults(query, 1);
  }

  @Test
  public void testSimpleQueryWithReadInstancesPermissionOnOneTaskProcess() {
    // given
    String processInstanceId = startProcessInstanceByKey(ONE_TASK_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId, READ_INSTANCE);

    // when
    ExecutionQuery query = runtimeService.createExecutionQuery();

    // then
    verifyQueryResults(query, 1);

    Execution execution = query.singleResult();
    assertNotNull(execution);
    assertEquals(processInstanceId, execution.getProcessInstanceId());
  }

  @Test
  public void testSimpleQueryWithReadInstancesPermissionOnAnyProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(ONE_TASK_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_INSTANCE);

    // when
    ExecutionQuery query = runtimeService.createExecutionQuery();

    // then
    verifyQueryResults(query, 1);

    Execution execution = query.singleResult();
    assertNotNull(execution);
    assertEquals(processInstanceId, execution.getProcessInstanceId());
  }

  @Test
  public void testQueryWithoutAuthorization() {
    // given
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);

    startProcessInstanceByKey(MESSAGE_BOUNDARY_PROCESS_KEY);
    startProcessInstanceByKey(MESSAGE_BOUNDARY_PROCESS_KEY);
    startProcessInstanceByKey(MESSAGE_BOUNDARY_PROCESS_KEY);
    startProcessInstanceByKey(MESSAGE_BOUNDARY_PROCESS_KEY);

    // when
    ExecutionQuery query = runtimeService.createExecutionQuery();

    // then
    verifyQueryResults(query, 0);
  }

  @Test
  public void testQueryWithReadPermissionOnProcessInstance() {
    // given
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);
    String processInstanceId = startProcessInstanceByKey(ONE_TASK_PROCESS_KEY).getId();

    startProcessInstanceByKey(MESSAGE_BOUNDARY_PROCESS_KEY);
    startProcessInstanceByKey(MESSAGE_BOUNDARY_PROCESS_KEY);
    startProcessInstanceByKey(MESSAGE_BOUNDARY_PROCESS_KEY);
    startProcessInstanceByKey(MESSAGE_BOUNDARY_PROCESS_KEY);

    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, READ);

    // when
    ExecutionQuery query = runtimeService.createExecutionQuery();

    // then
    verifyQueryResults(query, 1);

    Execution execution = query.singleResult();
    assertNotNull(execution);
    assertEquals(processInstanceId, execution.getProcessInstanceId());
  }

  @Test
  public void testQueryWithReadPermissionOnAnyProcessInstance() {
    // given
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);

    startProcessInstanceByKey(MESSAGE_BOUNDARY_PROCESS_KEY);
    startProcessInstanceByKey(MESSAGE_BOUNDARY_PROCESS_KEY);
    startProcessInstanceByKey(MESSAGE_BOUNDARY_PROCESS_KEY);
    startProcessInstanceByKey(MESSAGE_BOUNDARY_PROCESS_KEY);

    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, READ);

    // when
    ExecutionQuery query = runtimeService.createExecutionQuery();

    // then
    verifyQueryResults(query, 11);
  }

  @Test
  public void testQueryWithReadInstancesPermissionOnOneTaskProcess() {
    // given
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);

    startProcessInstanceByKey(MESSAGE_BOUNDARY_PROCESS_KEY);
    startProcessInstanceByKey(MESSAGE_BOUNDARY_PROCESS_KEY);
    startProcessInstanceByKey(MESSAGE_BOUNDARY_PROCESS_KEY);
    startProcessInstanceByKey(MESSAGE_BOUNDARY_PROCESS_KEY);

    createGrantAuthorization(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId, READ_INSTANCE);

    // when
    ExecutionQuery query = runtimeService.createExecutionQuery();

    // then
    verifyQueryResults(query, 3);
  }

  @Test
  public void testQueryWithReadInstancesPermissionOnAnyProcessDefinition() {
    // given
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);

    startProcessInstanceByKey(MESSAGE_BOUNDARY_PROCESS_KEY);
    startProcessInstanceByKey(MESSAGE_BOUNDARY_PROCESS_KEY);
    startProcessInstanceByKey(MESSAGE_BOUNDARY_PROCESS_KEY);
    startProcessInstanceByKey(MESSAGE_BOUNDARY_PROCESS_KEY);

    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_INSTANCE);

    // when
    ExecutionQuery query = runtimeService.createExecutionQuery();

    // then
    verifyQueryResults(query, 11);
  }

  @Test
  public void testQueryShouldReturnAllExecutions() {
    // given
    ProcessInstance processInstance = startProcessInstanceByKey(MESSAGE_BOUNDARY_PROCESS_KEY);
    createGrantAuthorization(PROCESS_INSTANCE, processInstance.getId(), userId, READ);

    // when
    ExecutionQuery query = runtimeService.createExecutionQuery();

    // then
    verifyQueryResults(query, 2);
  }

  protected void verifyQueryResults(ExecutionQuery query, int countExpected) {
    verifyQueryResults((AbstractQuery<?, ?>) query, countExpected);
  }
}
