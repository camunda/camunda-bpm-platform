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
package org.camunda.bpm.engine.test.api.externaltask;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.util.SingleConsumerCondition;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Tests the signalling of external task conditions
 */
public class ExternalTaskConditionsTest {

  @Rule
  public ProcessEngineRule rule = new ProvidedProcessEngineRule();

  @Mock
  public SingleConsumerCondition condition;

  private String deploymentId;

  private final BpmnModelInstance testProcess = Bpmn.createExecutableProcess("theProcess")
    .startEvent()
    .serviceTask("theTask")
        .camundaExternalTask("theTopic")
    .done();

  @Before
  public void setUp() {

    MockitoAnnotations.initMocks(this);

    ProcessEngineImpl.EXT_TASK_CONDITIONS.addConsumer(condition);

    deploymentId = rule.getRepositoryService()
        .createDeployment()
        .addModelInstance("process.bpmn", testProcess)
        .deploy()
        .getId();
  }

  @After
  public void tearDown() {

    ProcessEngineImpl.EXT_TASK_CONDITIONS.removeConsumer(condition);

    if (deploymentId != null) {
      rule.getRepositoryService().deleteDeployment(deploymentId, true);
    }
  }

  @Test
  public void shouldSignalConditionOnTaskCreate() {

    // when
    rule.getRuntimeService()
      .startProcessInstanceByKey("theProcess");

    // then
    verify(condition, times(1)).signal();
  }

  @Test
  public void shouldSignalConditionOnTaskCreateMultipleTimes() {

    // when
    rule.getRuntimeService()
      .startProcessInstanceByKey("theProcess");
    rule.getRuntimeService()
      .startProcessInstanceByKey("theProcess");

    // then
    verify(condition, times(2)).signal();
  }

  @Test
  public void shouldSignalConditionOnUnlock() {

    // given

    rule.getRuntimeService()
      .startProcessInstanceByKey("theProcess");

    reset(condition); // clear signal for create

    LockedExternalTask lockedTask = rule.getExternalTaskService().fetchAndLock(1, "theWorker")
      .topic("theTopic", 10000)
      .execute()
      .get(0);

    // when
    rule.getExternalTaskService().unlock(lockedTask.getId());

    // then
    verify(condition, times(1)).signal();
  }

}
