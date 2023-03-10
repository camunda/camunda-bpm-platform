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
package org.camunda.bpm.engine.test.api.cfg;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.persistence.deploy.cache.DeploymentCache;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.DeploymentBuilder;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.api.runtime.migration.models.CallActivityModels;
import org.camunda.bpm.engine.test.api.runtime.migration.models.ProcessModels;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.commons.utils.cache.Cache;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * @author Johannes Heinemann
 */
public class DeploymentCacheCfgTest {

  @ClassRule
  public static ProcessEngineBootstrapRule cacheFactoryBootstrapRule = new ProcessEngineBootstrapRule(configuration -> {
      // apply configuration options here
      configuration.setCacheCapacity(2);
      configuration.setCacheFactory(new MyCacheFactory());
      configuration.setEnableFetchProcessDefinitionDescription(false);
  });

  protected ProvidedProcessEngineRule cacheFactoryEngineRule = new ProvidedProcessEngineRule(cacheFactoryBootstrapRule);
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(cacheFactoryEngineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(cacheFactoryEngineRule).around(testRule);
  RepositoryService repositoryService;
  ProcessEngineConfigurationImpl processEngineConfiguration;
  RuntimeService runtimeService;
  TaskService taskService;
  ManagementService managementService;

  @Before
  public void initialize() {
    repositoryService = cacheFactoryEngineRule.getRepositoryService();
    processEngineConfiguration = cacheFactoryEngineRule.getProcessEngineConfiguration();
    runtimeService = cacheFactoryEngineRule.getRuntimeService();
    taskService = cacheFactoryEngineRule.getTaskService();
    managementService = cacheFactoryEngineRule.getManagementService();
  }

  @Test
  public void testPlugInOwnCacheImplementation() {

    // given
    DeploymentCache deploymentCache = processEngineConfiguration.getDeploymentCache();

    // when
    Cache<String, ProcessDefinitionEntity> cache = deploymentCache.getProcessDefinitionCache();

    // then
    assertThat(cache).isInstanceOf(MyCacheImplementation.class);
  }

  @Test
  public void testDefaultCacheRemovesElementWhenMaxSizeIsExceeded() {
    // The engine rule sets the maximum number of elements of the to 2.
    // Accordingly, one process should not be contained in the cache anymore at the end.

    // given
    List<BpmnModelInstance> modelInstances =  createProcesses(3);
    deploy(modelInstances);
    String processDefinitionIdZero = repositoryService.createProcessDefinitionQuery()
        .processDefinitionKey("Process0")
        .singleResult()
        .getId();
    String processDefinitionIdOne = repositoryService.createProcessDefinitionQuery()
        .processDefinitionKey("Process1")
        .singleResult()
        .getId();
    String processDefinitionIdTwo = repositoryService.createProcessDefinitionQuery()
        .processDefinitionKey("Process2")
        .singleResult()
        .getId();

    // when
    DeploymentCache deploymentCache = processEngineConfiguration.getDeploymentCache();

    // then
    int numberOfProcessesInCache = 0;
    numberOfProcessesInCache +=
        deploymentCache.getProcessDefinitionCache().get(processDefinitionIdZero) == null ? 0 : 1;
    numberOfProcessesInCache +=
        deploymentCache.getProcessDefinitionCache().get(processDefinitionIdOne) == null ? 0 : 1;
    numberOfProcessesInCache +=
        deploymentCache.getProcessDefinitionCache().get(processDefinitionIdTwo) == null ? 0 : 1;

    assertEquals(2, numberOfProcessesInCache);
  }

  @Test
  public void testDisableQueryOfProcessDefinitionAddModelInstancesToDeploymentCache() {

    // given
    deploy(ProcessModels.ONE_TASK_PROCESS_WITH_DOCUMENTATION);
    ProcessInstance pi = runtimeService.startProcessInstanceByKey(ProcessModels.PROCESS_KEY);

    // when
    repositoryService.createProcessDefinitionQuery()
        .processDefinitionKey(ProcessModels.PROCESS_KEY)
        .singleResult()
        .getId();

    // then
    DeploymentCache deploymentCache = processEngineConfiguration.getDeploymentCache();
    BpmnModelInstance modelInstance = deploymentCache.getBpmnModelInstanceCache().get(pi.getProcessDefinitionId());
    assertNull(modelInstance);
  }

  @Test
  public void testEnableQueryOfProcessDefinitionAddModelInstancesToDeploymentCache() {

    // given
    deploy(ProcessModels.ONE_TASK_PROCESS_WITH_DOCUMENTATION);
    processEngineConfiguration.setEnableFetchProcessDefinitionDescription(true);
    ProcessInstance pi = runtimeService.startProcessInstanceByKey(ProcessModels.PROCESS_KEY);

    // when
    repositoryService.createProcessDefinitionQuery()
        .processDefinitionKey(ProcessModels.PROCESS_KEY)
        .singleResult()
        .getId();

    // then
    DeploymentCache deploymentCache = processEngineConfiguration.getDeploymentCache();
    BpmnModelInstance modelInstance = deploymentCache.getBpmnModelInstanceCache().get(pi.getProcessDefinitionId());
    assertNotNull(modelInstance);
  }

  @Test
  public void testDescriptionIsNullWhenFetchProcessDefinitionDescriptionIsDisabled() {

    // given
    deploy(ProcessModels.ONE_TASK_PROCESS_WITH_DOCUMENTATION);
    runtimeService.startProcessInstanceByKey(ProcessModels.PROCESS_KEY);

    // when
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
        .processDefinitionKey(ProcessModels.PROCESS_KEY)
        .singleResult();

    // then
    assertNull(processDefinition.getDescription());
  }

  @Test
  public void testDescriptionIsAvailableWhenFetchProcessDefinitionDescriptionIsEnabled() {

    // given
    deploy(ProcessModels.ONE_TASK_PROCESS_WITH_DOCUMENTATION);
    processEngineConfiguration.setEnableFetchProcessDefinitionDescription(true);
    runtimeService.startProcessInstanceByKey(ProcessModels.PROCESS_KEY);

    // when
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
        .processDefinitionKey(ProcessModels.PROCESS_KEY)
        .singleResult();

    // then
    assertNotNull(processDefinition.getDescription());
    assertEquals("This is a documentation!", processDefinition.getDescription());
  }

  @Test
  public void testLoadProcessDefinitionsFromDBWhenNotExistingInCacheAnymore() {

    // given more processes to deploy than capacity in the cache
    int numberOfProcessesToDeploy = 10;
    List<BpmnModelInstance> modelInstances = createProcesses(numberOfProcessesToDeploy);
    deploy(modelInstances);

    // when we start a process that was already removed from the cache
    assertNotNull(repositoryService.createProcessDefinitionQuery().processDefinitionKey("Process0").singleResult());
    runtimeService.startProcessInstanceByKey("Process0");

    // then we should be able to complete the process
    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());

  }

  @Test
  public void testSequentialCallActivityCall() {

    // given a number process definitions which call each other by call activities (0->1->2->0->4),
    // which stops after the first repetition of 0 in 4
    List<BpmnModelInstance> modelInstances = createSequentialCallActivityProcess();
    deploy(modelInstances);

    // when we start the first process 0
    Map<String, Object> variables = new HashMap<>();
    variables.put("NextProcess", "Process1");
    runtimeService.startProcessInstanceByKey("Process0", variables);

    // then we should be able to complete the task in process 4
    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());
  }

  @Test
  public void testSequentialCallActivityCallAsynchronously() throws InterruptedException {

    // given a number process definitions which call each other by call activities (0->1->2->0->4),
    // which stops after the first repetition of 0 in 4
    List<BpmnModelInstance> modelInstances = createSequentialCallActivityProcessAsync();
    deploy(modelInstances);

    // when we start the first process 0
    Map<String, Object> variables = new HashMap<>();
    variables.put("NextProcess", "Process1");
    runtimeService.startProcessInstanceByKey("Process0", variables);
    Job job = managementService.createJobQuery().singleResult();
    managementService.executeJob(job.getId());

    // when we reach process 0 a second time, we have to start that job as well
    job = managementService.createJobQuery().singleResult();
    managementService.executeJob(job.getId());

    // then we should be able to complete the task in process 4
    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());
  }

  @Test
  public void testSequentialCallActivityAsynchronousWithUnfinishedExecution() throws InterruptedException {

    // given a number process definitions which call each other by call activities (0->1->2->0->4),
    // which stops after the first repetition of 0
    List<BpmnModelInstance> modelInstances = createSequentialCallActivityProcessAsync();
    Deployment deployment =  deploy(modelInstances);

    // when we start the first process 0
    Map<String, Object> variables = new HashMap<>();
    variables.put("NextProcess", "Process1");
    runtimeService.startProcessInstanceByKey("Process0", variables);
    Job job = managementService.createJobQuery().singleResult();
    managementService.executeJob(job.getId());

    // then deleting the deployment should still be possible
    repositoryService.deleteDeployment(deployment.getId(), true);
  }

  @Test
  public void shouldNotAddIdentityLinksAfterRecache() {
    // given cache size + 1 deployed process
    testRule.deploy(createModel("1"));
    testRule.deploy(createModel("2"));
    testRule.deploy(createModel("3"));

    // when process start from 1 to 3 they are re-cached
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("Process1");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("Process2");
    ProcessInstance processInstance3 = runtimeService.startProcessInstanceByKey("Process3");

    // then
    assertThat(repositoryService.getIdentityLinksForProcessDefinition(processInstance1.getProcessDefinitionId()).size())
        .isEqualTo(1);
    assertThat(repositoryService.getIdentityLinksForProcessDefinition(processInstance2.getProcessDefinitionId()).size())
        .isEqualTo(1);
    assertThat(repositoryService.getIdentityLinksForProcessDefinition(processInstance3.getProcessDefinitionId()).size())
        .isEqualTo(1);
  }

  protected List<BpmnModelInstance> createSequentialCallActivityProcess() {
    List<BpmnModelInstance> modelInstances = new LinkedList<>();

    modelInstances.add(CallActivityModels.oneBpmnCallActivityProcessAsExpression(0));
    modelInstances.add(CallActivityModels.oneBpmnCallActivityProcessPassingVariables(1, 2));
    modelInstances.add(CallActivityModels.oneBpmnCallActivityProcessPassingVariables(2, 0));
    modelInstances.add(ProcessModels.oneTaskProcess(3));

    return modelInstances;
  }

  protected List<BpmnModelInstance> createSequentialCallActivityProcessAsync() {
    List<BpmnModelInstance> modelInstances = new LinkedList<>();

    modelInstances.add(CallActivityModels.oneBpmnCallActivityProcessAsExpressionAsync(0));
    modelInstances.add(CallActivityModels.oneBpmnCallActivityProcessPassingVariables(1, 2));
    modelInstances.add(CallActivityModels.oneBpmnCallActivityProcessPassingVariables(2, 0));
    modelInstances.add(ProcessModels.oneTaskProcess(3));

    return modelInstances;
  }

  protected Deployment deploy(List<BpmnModelInstance> modelInstances) {
    DeploymentBuilder deploymentbuilder = processEngineConfiguration.getRepositoryService().createDeployment();

    for (int i = 0; i < modelInstances.size(); i++) {
      deploymentbuilder.addModelInstance("process" + i + ".bpmn", modelInstances.get(i));
    }

    return testRule.deploy(deploymentbuilder);
  }

  protected Deployment deploy(BpmnModelInstance modelInstance) {
    DeploymentBuilder deploymentbuilder = processEngineConfiguration.getRepositoryService().createDeployment();
    deploymentbuilder.addModelInstance("process0.bpmn", modelInstance);
    return testRule.deploy(deploymentbuilder);
  }

  protected List<BpmnModelInstance> createProcesses(int numberOfProcesses) {

    List<BpmnModelInstance> result = new ArrayList<>(numberOfProcesses);
    for (int i = 0; i < numberOfProcesses; i++) {
      result.add(ProcessModels.oneTaskProcess(i));
    }
    return result;
  }

  protected BpmnModelInstance createModel(String suffix) {
    BpmnModelInstance bpmnModel = Bpmn.createExecutableProcess("Process" + suffix)
        .startEvent("startEvent")
        .userTask().name("User Task")
        .endEvent("endEvent")
        .done();
    org.camunda.bpm.model.bpmn.instance.Process model = bpmnModel.getModelElementById("Process" + suffix);
    model.setCamundaCandidateStarterUsers("demo" + suffix);
    return bpmnModel;
  }


}