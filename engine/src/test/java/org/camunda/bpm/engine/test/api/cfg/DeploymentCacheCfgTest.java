/* Licensed under the Apache License, Version 2.0 (the "License");
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

package org.camunda.bpm.engine.test.api.cfg;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.persistence.deploy.DeploymentCache;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.repository.DeploymentBuilder;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.commons.utils.cache.Cache;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.*;

/**
 * @author Johannes Heinemann
 */
public class DeploymentCacheCfgTest {

  protected ProcessEngineBootstrapRule cacheFactoryBootstrapRule = new ProcessEngineBootstrapRule() {
    public ProcessEngineConfiguration configureEngine(ProcessEngineConfigurationImpl configuration) {
      // apply configuration options here
      configuration.setCacheCapacity(2);
      configuration.setCacheFactory(new MyCacheFactory());
      configuration.setEnableFetchProcessDefinitionDescription(false);
      return configuration;
    }
  };

  protected ProvidedProcessEngineRule cacheFactoryEngineRule = new ProvidedProcessEngineRule(cacheFactoryBootstrapRule);

  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(cacheFactoryEngineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(cacheFactoryBootstrapRule).around(cacheFactoryEngineRule).around(testRule);
  RepositoryService repositoryService;
  ProcessEngineConfigurationImpl processEngineConfiguration;
  RuntimeService runtimeService;
  TaskService taskService;

  @Before
  public void initialize() {
    repositoryService = cacheFactoryEngineRule.getRepositoryService();
    processEngineConfiguration = cacheFactoryEngineRule.getProcessEngineConfiguration();
    runtimeService = cacheFactoryEngineRule.getRuntimeService();
    taskService = cacheFactoryEngineRule.getTaskService();
  }

  @Test
  @Deployment(resources =
      {"org/camunda/bpm/engine/test/api/cfg/DeploymentCacheCfgTest.testDefaultCacheRemovesElementWhenMaxSizeIsExceeded.bpmn20.xml"})
  public void testPlugInOwnCacheImplementation() {

    // given
    DeploymentCache deploymentCache = processEngineConfiguration.getDeploymentCache();

    // when
    Cache<String, ProcessDefinitionEntity> cache = deploymentCache.getProcessDefinitionCache();

    // then
    assertThat(cache, instanceOf(MyCacheImplementation.class));
  }

  @Test
  @Deployment(resources =
      {"org/camunda/bpm/engine/test/api/cfg/DeploymentCacheCfgTest.testDefaultCacheRemovesElementWhenMaxSizeIsExceeded.bpmn20.xml"})
  public void testDefaultCacheRemovesElementWhenMaxSizeIsExceeded() {
    // The engine rule sets the maximum number of elements of the to 2.
    // Accordingly, one process should not be contained in the cache anymore at the end.

    // given
    String processDefinitionIdOne = repositoryService.createProcessDefinitionQuery()
        .processDefinitionKey("one")
        .singleResult()
        .getId();
    String processDefinitionIdTwo = repositoryService.createProcessDefinitionQuery()
        .processDefinitionKey("two")
        .singleResult()
        .getId();
    String processDefinitionIdThree = repositoryService.createProcessDefinitionQuery()
        .processDefinitionKey("three")
        .singleResult()
        .getId();

    // when
    DeploymentCache deploymentCache = processEngineConfiguration.getDeploymentCache();

    // then
    int numberOfProcessesInCache = 0;
    numberOfProcessesInCache +=
        deploymentCache.getProcessDefinitionCache().get(processDefinitionIdOne) == null ? 0 : 1;
    numberOfProcessesInCache +=
        deploymentCache.getProcessDefinitionCache().get(processDefinitionIdTwo) == null ? 0 : 1;
    numberOfProcessesInCache +=
        deploymentCache.getProcessDefinitionCache().get(processDefinitionIdThree) == null ? 0 : 1;

    assertEquals(2, numberOfProcessesInCache);
  }

  @Test
  @Deployment(resources =
      {"org/camunda/bpm/engine/test/api/cfg/DeploymentCacheCfgTest.testDefaultCacheRemovesElementWhenMaxSizeIsExceeded.bpmn20.xml"})
  public void testDisableQueryOfProcessDefinitionAddModelInstancesToDeploymentCache() {

    // given
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("two");

    // when
    repositoryService.createProcessDefinitionQuery()
        .processDefinitionKey("two")
        .singleResult()
        .getId();

    // then
    DeploymentCache deploymentCache = processEngineConfiguration.getDeploymentCache();
    BpmnModelInstance modelInstance = deploymentCache.getBpmnModelInstanceCache().get(pi.getProcessDefinitionId());
    assertNull(modelInstance);
  }

  @Test
  @Deployment(resources =
      {"org/camunda/bpm/engine/test/api/cfg/DeploymentCacheCfgTest.testDefaultCacheRemovesElementWhenMaxSizeIsExceeded.bpmn20.xml"})
  public void testEnableQueryOfProcessDefinitionAddModelInstancesToDeploymentCache() {

    // given
    processEngineConfiguration.setEnableFetchProcessDefinitionDescription(true);
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("two");

    // when
    repositoryService.createProcessDefinitionQuery()
        .processDefinitionKey("two")
        .singleResult()
        .getId();

    // then
    DeploymentCache deploymentCache = processEngineConfiguration.getDeploymentCache();
    BpmnModelInstance modelInstance = deploymentCache.getBpmnModelInstanceCache().get(pi.getProcessDefinitionId());
    assertNotNull(modelInstance);
  }

  @Test
  @Deployment(resources =
      {"org/camunda/bpm/engine/test/api/cfg/DeploymentCacheCfgTest.testDefaultCacheRemovesElementWhenMaxSizeIsExceeded.bpmn20.xml"})
  public void testDescriptionIsNullWhenFetchProcessDefinitionDescriptionIsDisabled() {

    // given
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("two");

    // when
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
        .processDefinitionKey("two")
        .singleResult();

    // then
    assertNull(processDefinition.getDescription());
  }

  @Test
  @Deployment(resources =
      {"org/camunda/bpm/engine/test/api/cfg/DeploymentCacheCfgTest.testDefaultCacheRemovesElementWhenMaxSizeIsExceeded.bpmn20.xml"})
  public void testDescriptionIsAvailableWhenFetchProcessDefinitionDescriptionIsEnabled() {

    // given
    processEngineConfiguration.setEnableFetchProcessDefinitionDescription(true);
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("two");

    // when
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
        .processDefinitionKey("two")
        .singleResult();

    // then
    assertNotNull(processDefinition.getDescription());
    assertEquals("This is a documentation!", processDefinition.getDescription());
  }

  /**
   * Failing test see issue CAM-6958
   */
  public void testLoadProcessDefinitionsFromDBWhenNotInDefaultCache() {

    // given more processes to deploy than capacity in the cache
    int numberOfProcessesToDeploy = 10;
    createAndDeployProcesses(numberOfProcessesToDeploy);

    // when we start a process that was already removed from the cache
    assertNotNull(repositoryService.createProcessDefinitionQuery().processDefinitionKey("process0").singleResult());
    runtimeService.startProcessInstanceByKey("process0");

    // then we should be able to complete the process
    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());

  }

  protected void createAndDeployProcesses(int numberOfProcesses) {
    DeploymentBuilder deploymentbuilder = processEngineConfiguration.getRepositoryService().createDeployment();
    List<BpmnModelInstance> modelInstances = createProcesses(numberOfProcesses);

    for (int i = 0; i < modelInstances.size(); i++) {
      deploymentbuilder.addModelInstance("process" + i + ".bpmn", modelInstances.get(i));
    }

    testRule.deploy(deploymentbuilder);
  }

  protected List<BpmnModelInstance> createProcesses(int numberOfProcesses) {

    List<BpmnModelInstance> result = new ArrayList<BpmnModelInstance>(numberOfProcesses);
    for (int i = 0; i < numberOfProcesses; i++) {
      result.add(createProcess(i));
    }
    return result;
  }

  protected BpmnModelInstance createProcess(int id) {
    return Bpmn.createExecutableProcess("process" + id)
        .startEvent()
        .userTask("Task")
        .endEvent()
        .done();
  }

}