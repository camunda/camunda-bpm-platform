/*
 * Copyright 2016 camunda services GmbH.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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
package org.camunda.bpm.engine.test.api.context;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNull;
import org.camunda.bpm.engine.context.DelegateExecutionContext;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * Represents test class to test the delegate execution context.
 *
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
public class DelegateExecutionContextTest {

  protected static final BpmnModelInstance DELEGATION_PROCESS = Bpmn.createExecutableProcess("process1")
          .startEvent()
          .serviceTask("serviceTask1")
            .camundaClass(DelegateClass.class.getName())
          .endEvent()
          .done();


  protected static final BpmnModelInstance EXEUCTION_LISTENER_PROCESS = Bpmn.createExecutableProcess("process2")
          .startEvent()
            .camundaExecutionListenerClass(ExecutionListener.EVENTNAME_START, ExecutionListenerImpl.class.getName())
          .endEvent()
          .done();

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule testHelper = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testHelper);

  @Test
  public void testDelegateExecutionContext() {
    // given
    ProcessDefinition definition = testHelper.deployAndGetDefinition(DELEGATION_PROCESS);
    // a process instance with a service task and a java delegate
    ProcessInstance instance = engineRule.getRuntimeService().startProcessInstanceById(definition.getId());

    //then delegation execution context is no more available
    DelegateExecution execution = DelegateExecutionContext.getCurrentDelegationExecution();
    assertNull(execution);
  }


  @Test
  public void testDelegateExecutionContextWithExecutionListener() {
    //given
    ProcessDefinition definition = testHelper.deployAndGetDefinition(EXEUCTION_LISTENER_PROCESS);
    // a process instance with a service task and an execution listener
    engineRule.getRuntimeService().startProcessInstanceById(definition.getId());

    //then delegation execution context is no more available
    DelegateExecution execution = DelegateExecutionContext.getCurrentDelegationExecution();
    assertNull(execution);
  }

  public static class ExecutionListenerImpl implements ExecutionListener {

    @Override
    public void notify(DelegateExecution execution) throws Exception {
      checkDelegationContext(execution);
    }
  }

  public static class DelegateClass implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {
      checkDelegationContext(execution);
    }
  }

  protected static void checkDelegationContext(DelegateExecution execution) {
    //then delegation execution context is available
    assertNotNull(DelegateExecutionContext.getCurrentDelegationExecution());
    assertEquals(DelegateExecutionContext.getCurrentDelegationExecution(), execution);
  }
}
