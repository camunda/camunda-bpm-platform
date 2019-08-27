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
package org.camunda.bpm.engine.test.api.runtime.migration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.impl.util.EnsureUtil;
import org.camunda.bpm.engine.management.JobDefinition;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.util.ExecutionTree;

/**
 * Helper class to save the current state of a process instance.
 */
public class ProcessInstanceSnapshot {

  protected String processInstanceId;
  protected String processDefinitionId;
  protected String deploymentId;
  protected ActivityInstance activityTree;
  protected ExecutionTree executionTree;
  protected List<EventSubscription> eventSubscriptions;
  protected List<Job> jobs;
  protected List<JobDefinition> jobDefinitions;
  protected List<Task> tasks;
  protected Map<String, VariableInstance> variables;

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

  public void setDeploymentId(String deploymentId) {
    this.deploymentId = deploymentId;
  }

  public String getDeploymentId() {
    return deploymentId;
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

  public EventSubscription getEventSubscriptionById(String id) {
    for (EventSubscription subscription : eventSubscriptions) {
      if (subscription.getId().equals(id)) {
        return subscription;
      }
    }

    return null;
  }

  public EventSubscription getEventSubscriptionForActivityIdAndEventName(String activityId, String eventName) {

    List<EventSubscription> collectedEventsubscriptions = getEventSubscriptionsForActivityIdAndEventName(activityId, eventName);

    if (collectedEventsubscriptions.isEmpty()) {
      return null;
    }
    else if (collectedEventsubscriptions.size() == 1) {
      return collectedEventsubscriptions.get(0);
    }
    else {
      throw new RuntimeException("There is more than one event subscription for activity " + activityId + " and event " + eventName);
    }
  }

  public List<EventSubscription> getEventSubscriptionsForActivityIdAndEventName(String activityId, String eventName) {

    List<EventSubscription> collectedEventsubscriptions = new ArrayList<EventSubscription>();

    for (EventSubscription eventSubscription : getEventSubscriptions()) {
      if (activityId.equals(eventSubscription.getActivityId())) {
        if ((eventName == null && eventSubscription.getEventName() == null)
          || eventName != null && eventName.equals(eventSubscription.getEventName())) {
          collectedEventsubscriptions.add(eventSubscription);
        }
      }
    }

    return collectedEventsubscriptions;
  }

  public void setEventSubscriptions(List<EventSubscription> eventSubscriptions) {
    this.eventSubscriptions = eventSubscriptions;
  }

  public List<Job> getJobs() {
    ensurePropertySaved("jobs", jobs);
    return jobs;
  }

  public Job getJobForDefinitionId(String jobDefinitionId) {
    List<Job> collectedJobs = new ArrayList<Job>();

    for (Job job : getJobs()) {
      if (jobDefinitionId.equals(job.getJobDefinitionId())) {
        collectedJobs.add(job);
      }
    }

    if (collectedJobs.isEmpty()) {
      return null;
    }
    else if (collectedJobs.size() == 1) {
      return collectedJobs.get(0);
    }
    else {
      throw new RuntimeException("There is more than one job for job definition " + jobDefinitionId);
    }
  }

  public Job getJobById(String jobId) {
    for (Job job : getJobs()) {
      if (jobId.equals(job.getId())) {
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

  public JobDefinition getJobDefinitionForActivityIdAndType(String activityId, String jobHandlerType) {

    List<JobDefinition> collectedDefinitions = getJobDefinitionsForActivityIdAndType(activityId, jobHandlerType);

    if (collectedDefinitions.isEmpty()) {
      return null;
    }
    else if (collectedDefinitions.size() == 1) {
      return collectedDefinitions.get(0);
    }
    else {
      throw new RuntimeException("There is more than one job definition for activity " + activityId + " and job handler type " + jobHandlerType);
    }
  }

  protected List<JobDefinition> getJobDefinitionsForActivityIdAndType(String activityId, String jobHandlerType) {
    List<JobDefinition> collectedDefinitions = new ArrayList<JobDefinition>();
    for (JobDefinition jobDefinition : getJobDefinitions()) {
      if (activityId.equals(jobDefinition.getActivityId()) && jobHandlerType.equals(jobDefinition.getJobType())) {
        collectedDefinitions.add(jobDefinition);
      }
    }
    return collectedDefinitions;
  }

  public void setJobDefinitions(List<JobDefinition> jobDefinitions) {
    this.jobDefinitions = jobDefinitions;
  }

  public Collection<VariableInstance> getVariables() {
    return variables.values();
  }

  public void setVariables(List<VariableInstance> variables) {
    this.variables = new HashMap<String, VariableInstance>();

    for (VariableInstance variable : variables) {
      this.variables.put(variable.getId(), variable);
    }
  }

  public VariableInstance getSingleVariable(final String variableName) {
    return getSingleVariable(new Condition<VariableInstance>() {

      @Override
      public boolean matches(VariableInstance variable) {
        return variableName.equals(variable.getName());
      }
    });
  }

  public VariableInstance getSingleVariable(final String executionId, final String variableName) {
    return getSingleVariable(new Condition<VariableInstance>() {

      @Override
      public boolean matches(VariableInstance variable) {
        return executionId.equals(variable.getExecutionId()) && variableName.equals(variable.getName());
      }
    });
  }

  public VariableInstance getSingleTaskVariable(final String taskId, final String variableName) {
    return getSingleVariable(new Condition<VariableInstance>() {

      @Override
      public boolean matches(VariableInstance variable) {
        return variableName.equals(variable.getName())
            && taskId.equals(variable.getTaskId());
      }
    });
  }

  protected VariableInstance getSingleVariable(Condition<VariableInstance> condition) {
    List<VariableInstance> matchingVariables = new ArrayList<VariableInstance>();

    for (VariableInstance variable : variables.values()) {
      if (condition.matches(variable)) {
        matchingVariables.add(variable);
      }
    }

    if (matchingVariables.size() == 1) {
      return  matchingVariables.get(0);
    }
    else if (matchingVariables.size() == 0) {
      return null;
    }
    else {
      throw new RuntimeException("There is more than one variable that matches the given condition");
    }
  }

  public VariableInstance getVariable(String id) {
    return variables.get(id);
  }

  protected void ensurePropertySaved(String name, Object property) {
    EnsureUtil.ensureNotNull(BadUserRequestException.class, "The snapshot has not saved the " + name + " of the process instance", name, property);
  }

  protected static interface Condition<T> {
    boolean matches(T condition);
  }

}
