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

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Thorben Lindhauer
 *
 */
public class NestedExecutionAPIInvocationTest {

  @Rule
  public ProcessEngineRule engineRule1 = new ProvidedProcessEngineRule();

  @ClassRule
  public static ProcessEngineBootstrapRule engine2BootstrapRule = new ProcessEngineBootstrapRule("camunda.cfg.prefix_extended.xml");

  @Rule
  public ProcessEngineRule engineRule2 = new ProvidedProcessEngineRule(engine2BootstrapRule);

  public static final String PROCESS_KEY_1 = "process";

  public static final String PROCESS_KEY_2 = "multiEngineProcess";

  public static final String ONE_TASK_PROCESS_KEY = "oneTaskProcess";

  public static final BpmnModelInstance PROCESS_MODEL = Bpmn.createExecutableProcess(PROCESS_KEY_1)
      .startEvent()
      .userTask("waitState")
      .serviceTask("startProcess")
        .camundaClass(NestedProcessStartDelegate.class.getName())
      .endEvent()
      .done();

  public static final BpmnModelInstance PROCESS_MODEL_2 = Bpmn.createExecutableProcess(PROCESS_KEY_2)
    .startEvent()
    .userTask("waitState")
    .serviceTask("startProcess")
      .camundaClass(StartProcessOnAnotherEngineDelegate.class.getName())
    .endEvent()
    .done();

  public static final BpmnModelInstance ONE_TASK_PROCESS_MODEL = Bpmn.createExecutableProcess(ONE_TASK_PROCESS_KEY)
  .startEvent()
    .userTask("waitState")
  .endEvent()
  .done();

  @Before
  public void init() {

    StartProcessOnAnotherEngineDelegate.engine = engine2BootstrapRule.getProcessEngine();
    NestedProcessStartDelegate.engine = engineRule1.getProcessEngine();

    // given
    Deployment deployment1 = engineRule1.getRepositoryService()
      .createDeployment()
      .addModelInstance("foo.bpmn", PROCESS_MODEL)
      .deploy();

    Deployment deployment2 = engineRule1.getRepositoryService()
      .createDeployment()
      .addModelInstance("boo.bpmn", PROCESS_MODEL_2)
      .deploy();

    engineRule1.manageDeployment(deployment1);
    engineRule1.manageDeployment(deployment2);

    Deployment deployment3 = engineRule2.getProcessEngine().getRepositoryService()
      .createDeployment()
      .addModelInstance("joo.bpmn", ONE_TASK_PROCESS_MODEL)
      .deploy();

    engineRule2.manageDeployment(deployment3);
  }

  @After
  public void clearEngineReference() {
    StartProcessOnAnotherEngineDelegate.engine = null;
    NestedProcessStartDelegate.engine = null;
  }

  @Test
  public void testWaitStateIsReachedOnNestedInstantiation() {

    engineRule1.getRuntimeService().startProcessInstanceByKey(PROCESS_KEY_1);
    String taskId = engineRule1.getTaskService()
      .createTaskQuery()
      .singleResult()
      .getId();

    // when
    engineRule1.getTaskService().complete(taskId);
  }

  @Test
  public void testWaitStateIsReachedOnMultiEngine() {

    engineRule1.getRuntimeService().startProcessInstanceByKey(PROCESS_KEY_2);
    String taskId = engineRule1.getTaskService()
      .createTaskQuery()
      .singleResult()
      .getId();

    // when
    engineRule1.getTaskService().complete(taskId);
  }

  public static class StartProcessOnAnotherEngineDelegate implements JavaDelegate {

    public static ProcessEngine engine;
    
    @Override
    public void execute(DelegateExecution execution) throws Exception {

      RuntimeService runtimeService = engine.getRuntimeService();

      ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);

      // then the wait state is reached immediately after instantiation
      ActivityInstance activityInstance = runtimeService.getActivityInstance(processInstance.getId());
      ActivityInstance[] activityInstances = activityInstance.getActivityInstances("waitState");
      Assert.assertEquals(1, activityInstances.length);

    }
  }

  public static class NestedProcessStartDelegate implements JavaDelegate {

    public static ProcessEngine engine;
    @Override
    public void execute(DelegateExecution execution) throws Exception {

      RuntimeService runtimeService = engine.getRuntimeService();

      ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

      // then the wait state is reached immediately after instantiation
      ActivityInstance activityInstance = runtimeService.getActivityInstance(processInstance.getId());
      ActivityInstance[] activityInstances = activityInstance.getActivityInstances("waitState");
      Assert.assertEquals(1, activityInstances.length);

    }
  }
}
