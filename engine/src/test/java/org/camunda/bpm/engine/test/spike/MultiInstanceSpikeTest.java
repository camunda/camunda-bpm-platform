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
package org.camunda.bpm.engine.test.spike;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.engine.test.util.ActivityInstanceAssert.assertThat;
import static org.camunda.bpm.engine.test.util.ActivityInstanceAssert.describeActivityInstanceTree;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.impl.BpmnModelConstants;
import org.camunda.bpm.model.bpmn.instance.MultiInstanceLoopCharacteristics;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

public class MultiInstanceSpikeTest {

  public ProvidedProcessEngineRule providedEngineRule = new ProvidedProcessEngineRule();
  public ProcessEngineTestRule testRule = new ProcessEngineTestRule(providedEngineRule);

  @Rule
  public RuleChain chain = RuleChain.outerRule(providedEngineRule).around(testRule);

  public static final String PROCESS_KEY = "process";
  public static final BpmnModelInstance MI_USER_TASK = Bpmn.createExecutableProcess(PROCESS_KEY)
    .startEvent()
    .userTask("task")
      .multiInstance()
      .parallel()
      .cardinality("${cnt}")
      .multiInstanceDone()
    .endEvent()
    .done();

  static {
    declareFunkyMultiInstance(MI_USER_TASK, "task");
  }

  protected RuntimeService runtimeService;
  protected TaskService taskService;

  @Before
  public void setUp() {
    runtimeService = providedEngineRule.getRuntimeService();
    taskService = providedEngineRule.getTaskService();
  }

  @Test
  public void shouldUseSubTreeScope() {
    // given
    testRule.deploy(MI_USER_TASK);

    int nrOfInstances = 10;
    VariableMap variables = Variables.createVariables().putValue("cnt", nrOfInstances);

    // when
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(PROCESS_KEY, variables);

    // then
    assertThat(processInstance).isNotNull();
    assertThat(taskService.createTaskQuery().count()).isEqualTo(nrOfInstances);
  }

  @Test
  public void shouldUseSubTreeScopeAndJoin() {
    // given
    testRule.deploy(MI_USER_TASK);

    int nrOfInstances = 10;
    VariableMap variables = Variables.createVariables().putValue("cnt", nrOfInstances);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(PROCESS_KEY, variables);

    // when
    List<Task> tasks = taskService.createTaskQuery().list();
    for (Task task : tasks) {
      taskService.complete(task.getId());
    }

    // then
    long count = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).count();
    assertEquals(0, count);
  }

  @Test
  public void shouldGenerateCorrectActivityInstanceTree() {
    // given
    String processDefinitionId = testRule.deploy(MI_USER_TASK).getDeployedProcessDefinitions().get(0).getId();

    int nrOfInstances = 10;
    VariableMap variables = Variables.createVariables().putValue("cnt", nrOfInstances);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(PROCESS_KEY, variables);

    // when
    ActivityInstance activityInstance = runtimeService.getActivityInstance(processInstance.getId());

    // then
    assertThat(activityInstance).hasStructure(
        describeActivityInstanceTree(processDefinitionId)
          .beginMiBody("task")
            .beginScope("task#multiInstanceBody#subTree")
              .activity("task")
              .activity("task")
              .activity("task")
            .endScope()
            .beginScope("task#multiInstanceBody#subTree")
              .activity("task")
              .activity("task")
              .activity("task")
            .endScope()
            .beginScope("task#multiInstanceBody#subTree")
              .activity("task")
              .activity("task")
              .activity("task")
            .endScope()
            .beginScope("task#multiInstanceBody#subTree")
              .activity("task")
            .endScope()
            .done());
  }

  public static void declareFunkyMultiInstance(BpmnModelInstance instance, String miActivityId) {
    ModelElementInstance miActivity = instance.getModelElementById(miActivityId);
    MultiInstanceLoopCharacteristics miCharacteristics = miActivity
        .getChildElementsByType(MultiInstanceLoopCharacteristics.class)
        .iterator()
        .next();
    miCharacteristics.setAttributeValueNs(BpmnModelConstants.CAMUNDA_NS, "funky", "true");
  }

}
