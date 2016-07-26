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
import org.camunda.bpm.engine.impl.persistence.deploy.DeploymentCache;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.After;
import static org.junit.Assert.assertEquals;

/**
 *
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
public class DeleteProcessDefinitionTest {

  @Rule
  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();

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
    try {
      repositoryService.deleteProcessDefinition(null);
      fail("Should fail, since process definition Id is null!");
    } catch (Exception ex) {
      assert (ex.getMessage().contains("processDefinitionId is null"));
    }
  }

  @Test
  public void testDeleteNonExistingProcessDefinition() {
    try {
      repositoryService.deleteProcessDefinition("notexist");
      fail("Should fail, to delete non existing process definition!");
    } catch (Exception ex) {
      assert (ex.getMessage().contains("No process definition found"));
    }
  }

  @Test
  public void testDeleteProcessDefinition() {
    // given deployment with two process definitions in one xml model file
    deployment = repositoryService.createDeployment()
            .addClasspathResource("org/camunda/bpm/engine/test/repository/twoProcesses.bpmn20.xml")
            .deploy();
    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().list();
    assertEquals(2, processDefinitions.size());

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
    ProcessInstanceWithVariables procInst = runtimeService.createProcessInstanceByKey("process").executeWithVariablesInReturn();
    assertNotNull(procInst);

    //when the corresponding process definition is deleted from the deployment
    try {
      repositoryService.deleteProcessDefinition(processDefinition.getId());
      fail("Should fail, since there exists a process instance");
    } catch (Exception ex) {
      //then the deletion should fail since there exist a process instance
      assert (ex.getMessage().contains("Referential integrity constraint violation"));
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
    ProcessInstanceWithVariables procInst = runtimeService.createProcessInstanceByKey("process").executeWithVariablesInReturn();
    assertNotNull(procInst);
    assertEquals(1, runtimeService.createProcessInstanceQuery().count());
    assertEquals(2, engineRule.getHistoryService().createHistoricActivityInstanceQuery().count());

    //when the corresponding process definition is cascading deleted from the deployment
    repositoryService.deleteProcessDefinition(processDefinition.getId(), true);

    //then exist no process instance and no definition
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());
    assertEquals(0, repositoryService.createProcessDefinitionQuery().count());
    assertEquals(0, engineRule.getHistoryService().createHistoricActivityInstanceQuery().count());
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
    assertEquals(0, processEngineConfiguration.getDeploymentCache().getProcessDefinitionCache().size());

    //then creating process instance from the existing process definition
    ProcessInstanceWithVariables procInst = runtimeService.createProcessInstanceByKey("two").executeWithVariablesInReturn();
    assertNotNull(procInst);
    assert (procInst.getProcessDefinitionId().contains("two"));

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
    assertEquals(2, processDefinitions.size());
    //one is deleted from the deployment
    repositoryService.deleteProcessDefinition(processDefinitions.get(0).getId());
    assertEquals(1, repositoryService.createProcessDefinitionQuery().count());

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
