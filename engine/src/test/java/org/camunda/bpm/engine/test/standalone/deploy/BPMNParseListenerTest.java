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
package org.camunda.bpm.engine.test.standalone.deploy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessInstanceWithVariablesImpl;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Frederik Heremans
 */
public class BPMNParseListenerTest {

  @ClassRule
  public static ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule(
      "org/camunda/bpm/engine/test/standalone/deploy/bpmn.parse.listener.camunda.cfg.xml");

  @Rule
  public ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);

  protected RuntimeService runtimeService;
  protected RepositoryService repositoryService;

  @Before
  public void setUp() {
    runtimeService = engineRule.getRuntimeService();
    repositoryService = engineRule.getRepositoryService();
  }

  @Deployment
  @Test
  public void testAlterProcessDefinitionKeyWhenDeploying() throws Exception {
    // Check if process-definition has different key
    assertEquals(0, repositoryService.createProcessDefinitionQuery().processDefinitionKey("oneTaskProcess").count());
    assertEquals(1, repositoryService.createProcessDefinitionQuery().processDefinitionKey("oneTaskProcess-modified").count());
  }

  @Deployment
  @Test
  public void testAlterActivityBehaviors() throws Exception {

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskWithIntermediateThrowEvent-modified");
    ProcessDefinitionImpl processDefinition = ((ProcessInstanceWithVariablesImpl) processInstance).getExecutionEntity().getProcessDefinition();

    ActivityImpl cancelThrowEvent = processDefinition.findActivity("CancelthrowEvent");
    assertTrue(cancelThrowEvent.getActivityBehavior() instanceof TestBPMNParseListener.TestCompensationEventActivityBehavior);

    ActivityImpl startEvent = processDefinition.findActivity("theStart");
    assertTrue(startEvent.getActivityBehavior() instanceof TestBPMNParseListener.TestNoneStartEventActivityBehavior);

    ActivityImpl endEvent = processDefinition.findActivity("theEnd");
    assertTrue(endEvent.getActivityBehavior() instanceof TestBPMNParseListener.TestNoneEndEventActivityBehavior);
  }
}
