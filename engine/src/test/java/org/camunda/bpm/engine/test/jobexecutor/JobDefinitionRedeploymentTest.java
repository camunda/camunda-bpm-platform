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
package org.camunda.bpm.engine.test.jobexecutor;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.management.JobDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

/**
 * Redeploy process definition and assert that no new job definitions were created.
 * 
 * @author Philipp Ossler
 *
 */
@RunWith(Parameterized.class)
public class JobDefinitionRedeploymentTest {

  @Parameters(name = "{index}: process definition = {0}")
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] { 
        { "org/camunda/bpm/engine/test/jobexecutor/JobDefinitionDeploymentTest.testTimerStartEvent.bpmn20.xml" },
        { "org/camunda/bpm/engine/test/jobexecutor/JobDefinitionDeploymentTest.testTimerBoundaryEvent.bpmn20.xml" },
        { "org/camunda/bpm/engine/test/jobexecutor/JobDefinitionDeploymentTest.testMultipleTimerBoundaryEvents.bpmn20.xml" },
        { "org/camunda/bpm/engine/test/jobexecutor/JobDefinitionDeploymentTest.testEventBasedGateway.bpmn20.xml" },
        { "org/camunda/bpm/engine/test/jobexecutor/JobDefinitionDeploymentTest.testTimerIntermediateEvent.bpmn20.xml" },
        { "org/camunda/bpm/engine/test/jobexecutor/JobDefinitionDeploymentTest.testAsyncContinuation.bpmn20.xml" },
        { "org/camunda/bpm/engine/test/jobexecutor/JobDefinitionDeploymentTest.testAsyncContinuationOfMultiInstance.bpmn20.xml" },
        { "org/camunda/bpm/engine/test/jobexecutor/JobDefinitionDeploymentTest.testAsyncContinuationOfActivityWrappedInMultiInstance.bpmn20.xml" } 
    });
  }

  @Parameter
  public String processDefinitionResource;

  @Rule
  public ProcessEngineRule rule = new ProvidedProcessEngineRule();

  protected ManagementService managementService;
  protected RepositoryService repositoryService;
  protected RuntimeService runtimeService;
  protected ProcessEngineConfigurationImpl processEngineConfiguration;

  @Before
  public void initServices() {    
    managementService = rule.getManagementService();
    repositoryService = rule.getRepositoryService();
    runtimeService = rule.getRuntimeService();
    processEngineConfiguration = (ProcessEngineConfigurationImpl) rule.getProcessEngine().getProcessEngineConfiguration();
  }

  @Test
  public void testJobDefinitionsAfterRedeploment() {

    // initially there are no job definitions:
    assertEquals(0, managementService.createJobDefinitionQuery().count());

    // initial deployment
    String deploymentId = repositoryService.createDeployment()
                            .addClasspathResource(processDefinitionResource)
                            .deploy()
                            .getId();

    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    assertNotNull(processDefinition);

    // this parses the process and created the Job definitions:
    List<JobDefinition> jobDefinitions = managementService.createJobDefinitionQuery().list();
    Set<String> jobDefinitionIds = getJobDefinitionIds(jobDefinitions);

    // now clear the cache:
    processEngineConfiguration.getDeploymentCache().discardProcessDefinitionCache();

    // if we start an instance of the process, the process will be parsed again:
    runtimeService.startProcessInstanceByKey(processDefinition.getKey());

    // no new definitions were created
    assertEquals(jobDefinitions.size(), managementService.createJobDefinitionQuery().count());

    // the job has the correct definitionId set:
    List<Job> jobs = managementService.createJobQuery().list();
    for (Job job : jobs) {
      assertTrue(jobDefinitionIds.contains(job.getJobDefinitionId()));
    }

    // delete the deployment
    repositoryService.deleteDeployment(deploymentId, true);
  }

  protected Set<String> getJobDefinitionIds(List<JobDefinition> jobDefinitions) {
    Set<String> definitionIds = new HashSet<String>();
    for (JobDefinition definition : jobDefinitions) {
      definitionIds.add(definition.getId());
    }
    return definitionIds;
  }

}
