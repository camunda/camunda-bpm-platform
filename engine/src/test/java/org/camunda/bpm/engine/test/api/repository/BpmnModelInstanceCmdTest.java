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
package org.camunda.bpm.engine.test.api.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collection;

import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Event;
import org.camunda.bpm.model.bpmn.instance.SequenceFlow;
import org.camunda.bpm.model.bpmn.instance.StartEvent;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.junit.Test;


/**
 * @author Sebastian Menski
 */
public class BpmnModelInstanceCmdTest extends PluggableProcessEngineTest {

  private final static String PROCESS_KEY = "one";

  @Deployment(resources = "org/camunda/bpm/engine/test/repository/one.bpmn20.xml")
  @Test
  public void testRepositoryService() {
    String processDefinitionId = repositoryService.createProcessDefinitionQuery().processDefinitionKey(PROCESS_KEY).singleResult().getId();

    BpmnModelInstance modelInstance = repositoryService.getBpmnModelInstance(processDefinitionId);
    assertNotNull(modelInstance);

    Collection<ModelElementInstance> events = modelInstance.getModelElementsByType(modelInstance.getModel().getType(Event.class));
    assertEquals(2, events.size());

    Collection<ModelElementInstance> sequenceFlows = modelInstance.getModelElementsByType(modelInstance.getModel().getType(SequenceFlow.class));
    assertEquals(1, sequenceFlows.size());

    StartEvent startEvent = modelInstance.getModelElementById("start");
    assertNotNull(startEvent);
  }

}
