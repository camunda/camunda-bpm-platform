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
package org.camunda.bpm.engine.test.api.runtime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.FormService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.repository.DeploymentWithDefinitions;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * @author Tassilo Weidner
 */
public class RootProcessInstanceTest {

  protected final String CALLED_PROCESS_KEY = "calledProcess";
  protected final BpmnModelInstance CALLED_PROCESS = Bpmn.createExecutableProcess(CALLED_PROCESS_KEY)
    .startEvent()
      .userTask("userTask")
    .endEvent().done();

  protected final String CALLED_AND_CALLING_PROCESS_KEY = "calledAndCallingProcess";
  protected final BpmnModelInstance CALLED_AND_CALLING_PROCESS =
    Bpmn.createExecutableProcess(CALLED_AND_CALLING_PROCESS_KEY)
    .startEvent()
      .callActivity()
        .calledElement(CALLED_PROCESS_KEY)
    .endEvent().done();

  protected final String CALLING_PROCESS_KEY = "callingProcess";
  protected final BpmnModelInstance CALLING_PROCESS = Bpmn.createExecutableProcess(CALLING_PROCESS_KEY)
    .startEvent()
      .callActivity()
        .calledElement(CALLED_AND_CALLING_PROCESS_KEY)
    .endEvent().done();

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  protected RuntimeService runtimeService;
  protected FormService formService;

  @Before
  public void init() {
    runtimeService = engineRule.getRuntimeService();
    formService = engineRule.getFormService();
  }

  @Test
  public void shouldPointToItself() {
    // given
    testRule.deploy(CALLED_PROCESS);

    // when
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(CALLED_PROCESS_KEY);

    // assume
    assertThat(processInstance.getRootProcessInstanceId(), notNullValue());

    // then
    assertThat(processInstance.getRootProcessInstanceId(), is(processInstance.getProcessInstanceId()));
  }

  @Test
  public void shouldPointToItselfBySubmittingStartForm() {
    // given
    DeploymentWithDefinitions deployment = testRule.deploy(CALLED_PROCESS);

    String processDefinitionId = deployment.getDeployedProcessDefinitions().get(0).getId();
    Map<String, Object> properties = new HashMap<>();

    // when
    ProcessInstance processInstance = formService.submitStartForm(processDefinitionId, properties);

    // assume
    assertThat(processInstance.getRootProcessInstanceId(), notNullValue());

    // then
    assertThat(processInstance.getRootProcessInstanceId(), is(processInstance.getProcessInstanceId()));
  }

  @Test
  public void shouldPointToItselfByStartingAtActivity() {
    // given
    testRule.deploy(CALLED_PROCESS);

    // when
    ProcessInstance processInstance = runtimeService.createProcessInstanceByKey(CALLED_PROCESS_KEY)
      .startAfterActivity("userTask")
      .execute();

    // assume
    assertThat(processInstance.getRootProcessInstanceId(), notNullValue());

    // then
    assertThat(processInstance.getRootProcessInstanceId(), is(processInstance.getProcessInstanceId()));
  }

  @Test
  public void shouldPointToRoot() {
    // given
    testRule.deploy(CALLED_PROCESS);
    testRule.deploy(CALLED_AND_CALLING_PROCESS);
    testRule.deploy(CALLING_PROCESS);

    // when
    runtimeService.startProcessInstanceByKey(CALLING_PROCESS_KEY);

    ProcessInstance calledProcessInstance = runtimeService.createProcessInstanceQuery()
      .processDefinitionKey(CALLED_PROCESS_KEY)
      .singleResult();

    ProcessInstance calledAndCallingProcessInstance = runtimeService.createProcessInstanceQuery()
      .processDefinitionKey(CALLED_AND_CALLING_PROCESS_KEY)
      .singleResult();

    ProcessInstance callingProcessInstance = runtimeService.createProcessInstanceQuery()
      .processDefinitionKey(CALLING_PROCESS_KEY)
      .singleResult();

    // assume
    assertThat(runtimeService.createProcessInstanceQuery().count(), is(3L));
    assertThat(callingProcessInstance.getRootProcessInstanceId(), notNullValue());

    // then
    assertThat(callingProcessInstance.getRootProcessInstanceId(),
      is(callingProcessInstance.getProcessInstanceId()));
    assertThat(calledProcessInstance.getRootProcessInstanceId(),
      is(callingProcessInstance.getProcessInstanceId()));
    assertThat(calledAndCallingProcessInstance.getRootProcessInstanceId(),
      is(callingProcessInstance.getProcessInstanceId()));
  }

  @Test
  public void shouldPointToRootWithInitialCallAfterParallelGateway() {
    // given
    testRule.deploy(CALLED_PROCESS);

    testRule.deploy(CALLED_AND_CALLING_PROCESS);

    testRule.deploy(Bpmn.createExecutableProcess("callingProcessWithGateway")
      .startEvent()
        .parallelGateway("split")
          .callActivity()
            .calledElement(CALLED_AND_CALLING_PROCESS_KEY)
          .moveToNode("split")
            .callActivity()
              .calledElement(CALLED_AND_CALLING_PROCESS_KEY)
      .endEvent().done());

    // when
    runtimeService.startProcessInstanceByKey("callingProcessWithGateway");

    List<ProcessInstance> calledProcessInstances = runtimeService.createProcessInstanceQuery()
      .processDefinitionKey(CALLED_PROCESS_KEY)
      .list();

    List<ProcessInstance> calledAndCallingProcessInstances = runtimeService.createProcessInstanceQuery()
      .processDefinitionKey(CALLED_AND_CALLING_PROCESS_KEY)
      .list();

    ProcessInstance callingProcessInstance = runtimeService.createProcessInstanceQuery()
      .processDefinitionKey("callingProcessWithGateway")
      .singleResult();

    // assume
    assertThat(runtimeService.createProcessInstanceQuery().count(), is(5L));
    assertThat(callingProcessInstance.getProcessInstanceId(), notNullValue());

    assertThat(calledProcessInstances.size(), is(2));
    assertThat(calledAndCallingProcessInstances.size(), is(2));

    // then
    assertThat(callingProcessInstance.getRootProcessInstanceId(),
      is(callingProcessInstance.getProcessInstanceId()));

    assertThat(calledProcessInstances.get(0).getRootProcessInstanceId(),
      is(callingProcessInstance.getProcessInstanceId()));
    assertThat(calledProcessInstances.get(1).getRootProcessInstanceId(),
      is(callingProcessInstance.getProcessInstanceId()));

    assertThat(calledAndCallingProcessInstances.get(0).getRootProcessInstanceId(),
      is(callingProcessInstance.getProcessInstanceId()));
    assertThat(calledAndCallingProcessInstances.get(1).getRootProcessInstanceId(),
      is(callingProcessInstance.getProcessInstanceId()));
  }

}
