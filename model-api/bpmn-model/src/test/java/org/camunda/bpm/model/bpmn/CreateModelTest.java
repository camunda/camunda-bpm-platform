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
package org.camunda.bpm.model.bpmn;

import org.camunda.bpm.model.bpmn.instance.*;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Sebastian Menski
 */
public class CreateModelTest {

  public BpmnModelInstance modelInstance;
  public Definitions definitions;
  public Process process;

  @Before
  public void createEmptyModel() {
    modelInstance = Bpmn.createEmptyModel();
    definitions = modelInstance.newInstance(Definitions.class);
    definitions.setTargetNamespace("http://camunda.org/examples");
    modelInstance.setDefinitions(definitions);
  }

  protected <T extends BpmnModelElementInstance> T createElement(BpmnModelElementInstance parentElement, String id, Class<T> elementClass) {
    T element = modelInstance.newInstance(elementClass);
    element.setAttributeValue("id", id, true);
    parentElement.addChildElement(element);
    return element;
  }

  public SequenceFlow createSequenceFlow(Process process, FlowNode from, FlowNode to) {
    SequenceFlow sequenceFlow = createElement(process, from.getId() + "-" + to.getId(), SequenceFlow.class);
    process.addChildElement(sequenceFlow);
    sequenceFlow.setSource(from);
    from.getOutgoing().add(sequenceFlow);
    sequenceFlow.setTarget(to);
    to.getIncoming().add(sequenceFlow);
    return sequenceFlow;
  }

  @Test
  public void createProcessWithOneTask() {
    // create process
    Process process = createElement(definitions, "process-with-one-task", Process.class);

    // create elements
    StartEvent startEvent = createElement(process, "start", StartEvent.class);
    UserTask task1 = createElement(process, "task1", UserTask.class);
    EndEvent endEvent = createElement(process, "end", EndEvent.class);

    // create flows
    createSequenceFlow(process, startEvent, task1);
    createSequenceFlow(process, task1, endEvent);
  }

  @Test
  public void createProcessWithParallelGateway() {
    // create process
    Process process = createElement(definitions, "process-with-parallel-gateway", Process.class);

    // create elements
    StartEvent startEvent = createElement(process, "start", StartEvent.class);
    ParallelGateway fork = createElement(process, "fork", ParallelGateway.class);
    UserTask task1 = createElement(process, "task1", UserTask.class);
    ServiceTask task2 = createElement(process, "task2", ServiceTask.class);
    ParallelGateway join = createElement(process, "join", ParallelGateway.class);
    EndEvent endEvent = createElement(process, "end", EndEvent.class);

    // create flows
    createSequenceFlow(process, startEvent, fork);
    createSequenceFlow(process, fork, task1);
    createSequenceFlow(process, fork, task2);
    createSequenceFlow(process, task1, join);
    createSequenceFlow(process, task2, join);
    createSequenceFlow(process, join, endEvent);
  }

  @After
  public void validateModel() {
    Bpmn.validateModel(modelInstance);
  }

}
