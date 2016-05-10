/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package org.camunda.bpm.engine.test.api.runtime.migration;

import java.util.List;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.management.JobDefinition;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.util.ExecutionTree;

public class ProcessInstanceSnapshotBuilder {

  protected ProcessEngine processEngine;
  protected String processInstanceId;
  protected ProcessInstanceSnapshot snapshot;

  public ProcessInstanceSnapshotBuilder(ProcessInstance processInstance, ProcessEngine processEngine) {
    this.processEngine = processEngine;
    this.processInstanceId = processInstance.getId();
    this.snapshot = new ProcessInstanceSnapshot(processInstance.getId(), processInstance.getProcessDefinitionId());
  }

  public ProcessInstanceSnapshotBuilder deploymentId() {
    String deploymentId = processEngine.getRepositoryService().getProcessDefinition(snapshot.getProcessDefinitionId()).getDeploymentId();
    snapshot.setDeploymentId(deploymentId);

    return this;
  }

  public ProcessInstanceSnapshotBuilder activityTree() {
    ActivityInstance activityInstance = processEngine.getRuntimeService().getActivityInstance(processInstanceId);
    snapshot.setActivityTree(activityInstance);

    return this;
  }

  public ProcessInstanceSnapshotBuilder executionTree() {
    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);
    snapshot.setExecutionTree(executionTree);

    return this;
  }

  public ProcessInstanceSnapshotBuilder tasks() {
    List<Task> tasks = processEngine.getTaskService().createTaskQuery().processInstanceId(processInstanceId).list();
    snapshot.setTasks(tasks);

    return this;
  }

  public ProcessInstanceSnapshotBuilder eventSubscriptions() {
    List<EventSubscription> eventSubscriptions = processEngine.getRuntimeService().createEventSubscriptionQuery().processInstanceId(processInstanceId).list();
    snapshot.setEventSubscriptions(eventSubscriptions);

    return this;
  }

  public ProcessInstanceSnapshotBuilder jobs() {
    List<Job> jobs = processEngine.getManagementService().createJobQuery().processInstanceId(processInstanceId).list();
    snapshot.setJobs(jobs);

    String processDefinitionId = processEngine.getRuntimeService()
      .createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult().getProcessDefinitionId();
    List<JobDefinition> jobDefinitions = processEngine.getManagementService().createJobDefinitionQuery().processDefinitionId(processDefinitionId).list();
    snapshot.setJobDefinitions(jobDefinitions);

    return this;
  }

  public ProcessInstanceSnapshotBuilder variables() {
    List<VariableInstance> variables = processEngine.getRuntimeService().createVariableInstanceQuery().processInstanceIdIn(processInstanceId).list();
    snapshot.setVariables(variables);

    return this;
  }

  public ProcessInstanceSnapshot build() {
    return snapshot;
  }

  public ProcessInstanceSnapshot full() {
    deploymentId();
    activityTree();
    executionTree();
    tasks();
    eventSubscriptions();
    jobs();
    variables();

    return build();
  }

}
