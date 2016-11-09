/*
 * Copyright 2016 camunda services GmbH.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.camunda.bpm.engine.test.bpmn.event.conditional;

import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParse;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessInstanceWithVariablesImpl;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstantiationBuilder;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Rule;
import org.junit.Test;
import org.omg.CORBA.CODESET_INCOMPATIBLE;

import static org.camunda.bpm.engine.test.api.runtime.migration.ModifiableBpmnModelInstance.modify;
import static org.camunda.bpm.engine.test.bpmn.event.conditional.AbstractConditionalEventTestCase.CONDITIONAL_EVENT_PROCESS_KEY;
import static org.camunda.bpm.engine.test.bpmn.event.conditional.AbstractConditionalEventTestCase.CONDITIONAL_MODEL;
import static org.camunda.bpm.engine.test.bpmn.event.conditional.AbstractConditionalEventTestCase.TASK_WITH_CONDITION_ID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
public class OnlyDispatchVariableEventOnExistingConditionsTest {

  @Rule
  public ProcessEngineRule rule = new ProvidedProcessEngineRule();

  @Test
  public void testProcessWithIntermediateConditionalEvent() {
    //given
    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(CONDITIONAL_EVENT_PROCESS_KEY)
      .startEvent()
      .intermediateCatchEvent()
      .conditionalEventDefinition()
        .condition("${var==1}")
      .conditionalEventDefinitionDone()
      .endEvent()
      .done();

    //when process is deployed and instance created
    rule.manageDeployment(rule.getRepositoryService().createDeployment().addModelInstance(CONDITIONAL_MODEL, modelInstance).deploy());
    ProcessInstanceWithVariablesImpl processInstance = (ProcessInstanceWithVariablesImpl) rule.getRuntimeService().startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);

    //then process definition contains property which indicates that conditional events exists
    Object property = processInstance.getExecutionEntity().getProcessDefinition().getProperty(BpmnParse.PROPERTYNAME_HAS_CONDITIONAL_EVENTS);
    assertNotNull(property);
    assertEquals(Boolean.TRUE, property);
  }

  @Test
  public void testProcessWithBoundaryConditionalEvent() {
    //given
    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(CONDITIONAL_EVENT_PROCESS_KEY)
      .startEvent()
      .userTask(TASK_WITH_CONDITION_ID)
      .endEvent()
      .done();

    modelInstance = modify(modelInstance).userTaskBuilder(TASK_WITH_CONDITION_ID)
      .boundaryEvent()
      .conditionalEventDefinition()
      .condition("${var==1}")
      .conditionalEventDefinitionDone()
      .endEvent()
      .done();

    //when process is deployed and instance created
    rule.manageDeployment(rule.getRepositoryService().createDeployment().addModelInstance(CONDITIONAL_MODEL, modelInstance).deploy());
    ProcessInstanceWithVariablesImpl processInstance = (ProcessInstanceWithVariablesImpl) rule.getRuntimeService().startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);

    //then process definition contains property which indicates that conditional events exists
    Object property = processInstance.getExecutionEntity().getProcessDefinition().getProperty(BpmnParse.PROPERTYNAME_HAS_CONDITIONAL_EVENTS);
    assertNotNull(property);
    assertEquals(Boolean.TRUE, property);
  }

  @Test
  public void testProcessWithEventSubProcessConditionalEvent() {
    //given
    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(CONDITIONAL_EVENT_PROCESS_KEY)
      .startEvent()
      .userTask()
      .endEvent()
      .done();

    modelInstance = modify(modelInstance).addSubProcessTo(CONDITIONAL_EVENT_PROCESS_KEY)
      .triggerByEvent()
      .embeddedSubProcess()
      .startEvent()
      .conditionalEventDefinition()
      .condition("${var==1}")
      .conditionalEventDefinitionDone()
      .endEvent()
      .done();

    //when process is deployed and instance created
    rule.manageDeployment(rule.getRepositoryService().createDeployment().addModelInstance(CONDITIONAL_MODEL, modelInstance).deploy());
    ProcessInstanceWithVariablesImpl processInstance = (ProcessInstanceWithVariablesImpl) rule.getRuntimeService().startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);

    //then process definition contains property which indicates that conditional events exists
    Object property = processInstance.getExecutionEntity().getProcessDefinition().getProperty(BpmnParse.PROPERTYNAME_HAS_CONDITIONAL_EVENTS);
    assertNotNull(property);
    assertEquals(Boolean.TRUE, property);
  }

  @Test
  public void testProcessWithoutConditionalEvent() {
    //given
    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(CONDITIONAL_EVENT_PROCESS_KEY)
      .startEvent()
      .userTask()
      .endEvent()
      .done();

    //when process is deployed and instance created
    rule.manageDeployment(rule.getRepositoryService().createDeployment().addModelInstance(CONDITIONAL_MODEL, modelInstance).deploy());
    ProcessInstanceWithVariablesImpl processInstance = (ProcessInstanceWithVariablesImpl) rule.getRuntimeService().startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);

    //then process definition contains no property which indicates that conditional events exists
    Object property = processInstance.getExecutionEntity().getProcessDefinition().getProperty(BpmnParse.PROPERTYNAME_HAS_CONDITIONAL_EVENTS);
    assertNull(property);
  }
}
