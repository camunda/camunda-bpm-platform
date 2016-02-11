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

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.impl.util.EnsureUtil;
import org.camunda.bpm.engine.management.JobDefinition;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.util.ExecutionTree;

/**
 * Helper class to save the current state of a process instance.
 */
public class ProcessInstanceSnapshot {

  protected String processInstanceId;
  protected String processDefinitionId;
  protected ActivityInstance activityTree;
  protected ExecutionTree executionTree;
  protected List<EventSubscription> eventSubscriptions;
  protected List<Job> jobs;
  protected List<JobDefinition> jobDefinitions;
  protected List<Task> tasks;

  public ProcessInstanceSnapshot(String processInstanceId, String processDefinitionId) {
    this.processInstanceId = processInstanceId;
    this.processDefinitionId = processDefinitionId;
  }

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }

  public ActivityInstance getActivityTree() {
    ensurePropertySaved("activity tree", activityTree);
    return activityTree;
  }

  public void setActivityTree(ActivityInstance activityTree) {
    this.activityTree = activityTree;
  }

  public ExecutionTree getExecutionTree() {
    ensurePropertySaved("execution tree", executionTree);
    return executionTree;
  }

  public void setExecutionTree(ExecutionTree executionTree) {
    this.executionTree = executionTree;
  }

  public void setTasks(List<Task> tasks) {
    this.tasks = tasks;
  }

  public List<Task> getTasks() {
    ensurePropertySaved("tasks", tasks);
    return tasks;
  }

  public Task getTaskForKey(String key) {
    for (Task task : getTasks()) {
      if (key.equals(task.getTaskDefinitionKey())) {
        return task;
      }
    }
    return null;
  }

  public List<EventSubscription> getEventSubscriptions() {
    ensurePropertySaved("event subscriptions", eventSubscriptions);
    return eventSubscriptions;
  }

  public EventSubscription getEventSubscriptionForActivityIdAndEventName(String activityId, String eventName) {
    for (EventSubscription eventSubscription : getEventSubscriptions()) {
      if (activityId.equals(eventSubscription.getActivityId()) && eventName.equals(eventSubscription.getEventName())) {
        return eventSubscription;
      }
    }
    return null;
  }

  public void setEventSubscriptions(List<EventSubscription> eventSubscriptions) {
    this.eventSubscriptions = eventSubscriptions;
  }

  public List<Job> getJobs() {
    ensurePropertySaved("jobs", jobs);
    return jobs;
  }

  public Job getJobForDefinitionId(String jobDefinitionId) {
    for (Job job : getJobs()) {
      if (jobDefinitionId.equals(job.getJobDefinitionId())) {
        return job;
      }
    }
    return null;
  }

  public void setJobs(List<Job> jobs) {
    this.jobs = jobs;
  }

  public List<JobDefinition> getJobDefinitions() {
    ensurePropertySaved("job definitions", jobDefinitions);
    return jobDefinitions;
  }

  public JobDefinition getJobDefinitionForActivityId(String activityId) {
    for (JobDefinition jobDefinition : getJobDefinitions()) {
      if (activityId.equals(jobDefinition.getActivityId())) {
        return jobDefinition;
      }
    }
    return null;
  }

  public void setJobDefinitions(List<JobDefinition> jobDefinitions) {
    this.jobDefinitions = jobDefinitions;
  }

  protected void ensurePropertySaved(String name, Object property) {
    EnsureUtil.ensureNotNull(BadUserRequestException.class, "The snapshot has not saved the " + name + " of the process instance", name, property);
  }
}
