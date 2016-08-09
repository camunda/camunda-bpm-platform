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
package org.camunda.bpm.engine.test.api.repository;

import java.util.List;
import static junit.framework.TestCase.assertFalse;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstanceWithVariables;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import static org.camunda.bpm.engine.test.api.repository.RedeploymentTest.DEPLOYMENT_NAME;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.exception.NotFoundException;
import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.impl.persistence.deploy.DeploymentCache;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.rules.ExpectedException;

/**
 *
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
public class DeleteProcessDefinitionTest {

  @Rule
  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  protected RepositoryService repositoryService;
  protected RuntimeService runtimeService;
  protected ProcessEngineConfigurationImpl processEngineConfiguration;
  protected Deployment deployment;

  @Before
  public void initServices() {
    repositoryService = engineRule.getRepositoryService();
    runtimeService = engineRule.getRuntimeService();
    processEngineConfiguration = (ProcessEngineConfigurationImpl) engineRule.getProcessEngine().getProcessEngineConfiguration();
  }

  @After
  public void cleanUp() {
    if (deployment != null) {
      repositoryService.deleteDeployment(deployment.getId(), true);
      deployment = null;
    }
  }

  @Test
  public void testDeleteProcessDefinitionNullId() {
    // declare expected exception
    thrown.expect(NullValueException.class);
    thrown.expectMessage("processDefinitionId is null");

    repositoryService.deleteProcessDefinition(null);
  }

  @Test
  public void testDeleteNonExistingProcessDefinition() {
    // declare expected exception
    thrown.expect(NotFoundException.class);
    thrown.expectMessage("No process definition found with id 'notexist': processDefinition is null");

    repositoryService.deleteProcessDefinition("notexist");
  }

  @Test
  public void testDeleteProcessDefinition() {
    // given deployment with two process definitions in one xml model file
    deployment = repositoryService.createDeployment()
            .addClasspathResource("org/camunda/bpm/engine/test/repository/twoProcesses.bpmn20.xml")
            .deploy();
    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().list();

    //when a process definition is been deleted
    repositoryService.deleteProcessDefinition(processDefinitions.get(0).getId());

    //then only one process definition should remain
    assertEquals(1, repositoryService.createProcessDefinitionQuery().count());
  }

  @Test
  public void testDeleteProcessDefinitionWithProcessInstance() {
    // given process definition and a process instance
    BpmnModelInstance bpmnModel = Bpmn.createExecutableProcess("process").startEvent().userTask().endEvent().done();
    deployment = repositoryService.createDeployment()
                                  .addModelInstance("process.bpmn", bpmnModel)
                                  .deploy();
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionKey("process").singleResult();
    runtimeService.createProcessInstanceByKey("process").executeWithVariablesInReturn();

    //when the corresponding process definition is deleted from the deployment
    try {
      repositoryService.deleteProcessDefinition(processDefinition.getId());
      fail("Should fail, since there exists a process instance");
    } catch (ProcessEngineException pee) {
      // then Exception is expected, the deletion should fail since there exist a process instance
      // and the cascade flag is per default false
      assertTrue(pee.getMessage().contains("Deletion of process definition without cascading failed."));
    }
    assertEquals(1, repositoryService.createProcessDefinitionQuery().count());
  }

  @Test
  public void testDeleteProcessDefinitionCascade() {
    // given process definition and a process instance
    BpmnModelInstance bpmnModel = Bpmn.createExecutableProcess("process").startEvent().userTask().endEvent().done();
    deployment = repositoryService.createDeployment()
                                  .addModelInstance("process.bpmn", bpmnModel)
                                  .deploy();
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionKey("process").singleResult();
    runtimeService.createProcessInstanceByKey("process").executeWithVariablesInReturn();

    //when the corresponding process definition is cascading deleted from the deployment
    repositoryService.deleteProcessDefinition(processDefinition.getId(), true);

    //then exist no process instance and no definition
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());
    assertEquals(0, repositoryService.createProcessDefinitionQuery().count());
    if (processEngineConfiguration.getHistoryLevel().getId() >= HistoryLevel.HISTORY_LEVEL_ACTIVITY.getId()) {
      assertEquals(0, engineRule.getHistoryService().createHistoricActivityInstanceQuery().count());
    }
  }

  @Test
  public void testDeleteProcessDefinitionClearsCache() {
    // given process definition and a process instance
    BpmnModelInstance bpmnModel = Bpmn.createExecutableProcess("process").startEvent().userTask().endEvent().done();
    deployment = repositoryService.createDeployment()
                                  .addModelInstance("process.bpmn", bpmnModel)
                                  .deploy();
    String processDefinitionId = repositoryService.createProcessDefinitionQuery()
                                                  .processDefinitionKey("process")
                                                  .singleResult()
                                                  .getId();

    DeploymentCache deploymentCache = processEngineConfiguration.getDeploymentCache();

    // ensure definitions and models are part of the cache
    assertTrue(deploymentCache.getProcessDefinitionCache().containsKey(processDefinitionId));
    assertTrue(deploymentCache.getBpmnModelInstanceCache().containsKey(processDefinitionId));

    repositoryService.deleteProcessDefinition(processDefinitionId, true);

    // then the definitions and models are removed from the cache
    assertFalse(deploymentCache.getProcessDefinitionCache().containsKey(processDefinitionId));
    assertFalse(deploymentCache.getBpmnModelInstanceCache().containsKey(processDefinitionId));
  }

  @Test
  public void testDeleteProcessDefinitionAndRefillDeploymentCache() {
    // given a deployment with two process definitions in one xml model file
    deployment = repositoryService.createDeployment()
            .addClasspathResource("org/camunda/bpm/engine/test/repository/twoProcesses.bpmn20.xml")
            .deploy();
    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().list();
    //one is deleted from the deployment
    repositoryService.deleteProcessDefinition(processDefinitions.get(0).getId());

    //when clearing the deployment cache
    processEngineConfiguration.getDeploymentCache().discardProcessDefinitionCache();

    //then creating process instance from the existing process definition
    ProcessInstanceWithVariables procInst = runtimeService.createProcessInstanceByKey("two").executeWithVariablesInReturn();
    assertNotNull(procInst);
    assertTrue(procInst.getProcessDefinitionId().contains("two"));

    //should refill the cache
    assertEquals(1, processEngineConfiguration.getDeploymentCache().getProcessDefinitionCache().size());
    //The deleted process definition should not be recreated after the cache is refilled
    assertEquals(1, repositoryService.createProcessDefinitionQuery().count());
    assertNull(repositoryService.createProcessDefinitionQuery().processDefinitionKey("one").singleResult());
  }

  @Test
  public void testDeleteProcessDefinitionAndRedeploy() {
    // given a deployment with two process definitions in one xml model file
    deployment = repositoryService.createDeployment()
            .addClasspathResource("org/camunda/bpm/engine/test/repository/twoProcesses.bpmn20.xml")
            .deploy();
    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().list();
    //one is deleted from the deployment
    repositoryService.deleteProcessDefinition(processDefinitions.get(0).getId());

    //when the process definition is redeployed
    Deployment deployment2 = repositoryService.createDeployment()
            .name(DEPLOYMENT_NAME)
            .addDeploymentResources(deployment.getId())
            .deploy();

    //then there should exist three process definitions
    //two of the redeployment and the remaining one
    assertEquals(3, repositoryService.createProcessDefinitionQuery().count());

    //clean up
    repositoryService.deleteDeployment(deployment2.getId(), true);
  }
}
