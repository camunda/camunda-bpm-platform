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
package org.camunda.bpm.cockpit.plugin.base;

import static org.fest.assertions.Assertions.assertThat;

import java.util.List;

import org.camunda.bpm.cockpit.plugin.base.persistence.entity.ProcessDefinitionFailureChainDto;
import org.camunda.bpm.cockpit.plugin.base.service.FailureService;
import org.camunda.bpm.cockpit.plugin.test.AbstractCockpitPluginTest;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;

/**
 *
 * @author nico.rehwaldt
 */
public class FailedProcessInstancesQueryTest extends AbstractCockpitPluginTest {

  private JobExecutorHelper helper;

  @Test
  @Deployment(resources = {
    "processes/call-activity.bpmn",
    "processes/nested-call-activity.bpmn",
    "processes/failing-process.bpmn",
  })
  public void testQuery() throws Exception {

    helper = new JobExecutorHelper(getProcessEngine());

    ProcessEngine processEngine = getProcessEngine();
    RuntimeService runtimeService = processEngine.getRuntimeService();

    runtimeService.startProcessInstanceByKey("FailingProcess");
    runtimeService.startProcessInstanceByKey("FailingProcess");
    runtimeService.startProcessInstanceByKey("FailingProcess");

    helper.waitForJobExecutorToProcessAllJobs(15000, 500);

    FailureService failureService = new FailureService(getProcessEngine().getName());

    List<ProcessDefinitionFailureChainDto> failures = failureService.getJobFailuresByProcessDefinition();

    assertThat(failures).hasSize(1);

    ProcessDefinitionFailureChainDto failureChain = failures.get(0);
    assertThat(failureChain.getFailureChain()).hasSize(1);
  }

  @Test
  @Deployment(resources = {
    "processes/call-activity.bpmn",
    "processes/nested-call-activity.bpmn",
    "processes/failing-process.bpmn",
  })
  public void testCallActivityQuery() throws Exception {

    helper = new JobExecutorHelper(getProcessEngine());

    ProcessEngine processEngine = getProcessEngine();
    RuntimeService runtimeService = processEngine.getRuntimeService();

    runtimeService.startProcessInstanceByKey("CallActivity");
    runtimeService.startProcessInstanceByKey("CallActivity");

    helper.waitForJobExecutorToProcessAllJobs(15000, 500);

    FailureService failureService = new FailureService(getProcessEngine().getName());

    List<ProcessDefinitionFailureChainDto> failures = failureService.getJobFailuresByProcessDefinition();

    assertThat(failures).hasSize(1);

    ProcessDefinitionFailureChainDto failureChain = failures.get(0);
    assertThat(failureChain.getFailureChain()).hasSize(2);
    assertThat(failureChain.getCount()).isEqualTo(2);
  }

  @Test
  @Deployment(resources = {
    "processes/call-activity.bpmn",
    "processes/nested-call-activity.bpmn",
    "processes/failing-process.bpmn",
  })
  public void testNestedCallActivityQuery() throws Exception {

    helper = new JobExecutorHelper(getProcessEngine());

    ProcessEngine processEngine = getProcessEngine();
    RuntimeService runtimeService = processEngine.getRuntimeService();

    runtimeService.startProcessInstanceByKey("NestedCallActivity");
    runtimeService.startProcessInstanceByKey("NestedCallActivity");

    helper.waitForJobExecutorToProcessAllJobs(15000, 500);

    FailureService failureService = new FailureService(getProcessEngine().getName());

    List<ProcessDefinitionFailureChainDto> failures = failureService.getJobFailuresByProcessDefinition();

    assertThat(failures).hasSize(1);

    ProcessDefinitionFailureChainDto failureChain = failures.get(0);
    assertThat(failureChain.getFailureChain()).hasSize(3);
  }

  @Test
  @Deployment(resources = {
    "processes/call-activity.bpmn",
    "processes/nested-call-activity.bpmn",
    "processes/failing-process.bpmn",
  })
  public void testMixedScenarioQuery() throws Exception {

    helper = new JobExecutorHelper(getProcessEngine());

    ProcessEngine processEngine = getProcessEngine();
    RuntimeService runtimeService = processEngine.getRuntimeService();

    runtimeService.startProcessInstanceByKey("NestedCallActivity");
    runtimeService.startProcessInstanceByKey("NestedCallActivity");
    runtimeService.startProcessInstanceByKey("FailingProcess");
    runtimeService.startProcessInstanceByKey("FailingProcess");
    runtimeService.startProcessInstanceByKey("FailingProcess");
    runtimeService.startProcessInstanceByKey("CallActivity");

    helper.waitForJobExecutorToProcessAllJobs(15000, 500);

    FailureService failureService = new FailureService(getProcessEngine().getName());

    List<ProcessDefinitionFailureChainDto> processInstances = failureService.getJobFailuresByProcessDefinition();

    assertThat(processInstances).hasSize(3);

    String procFailureChain0 = processInstances.get(0).getFailureChain().toString();
    String procFailureChain1 = processInstances.get(1).getFailureChain().toString();
    String procFailureChain2 = processInstances.get(2).getFailureChain().toString();

    assertThat(procFailureChain0).contains("FailingProcess");
    assertThat(procFailureChain0).contains("CallActivity");
    assertThat(procFailureChain0).contains("NestedCallActivity");

    assertThat(procFailureChain1).contains("FailingProcess");
    assertThat(procFailureChain1).contains("CallActivity");
    assertThat(procFailureChain1).doesNotContain("NestedCallActivity");

    assertThat(procFailureChain2).contains("FailingProcess");
    assertThat(procFailureChain2).doesNotContain("CallActivity");
    assertThat(procFailureChain2).doesNotContain("NestedCallActivity");
  }
}
