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
package org.camunda.bpm.engine.test.assertions.bpmn;


import static java.lang.String.format;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.DecisionService;
import org.camunda.bpm.engine.ExternalTaskService;
import org.camunda.bpm.engine.FormService;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.exception.NotFoundException;
import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.externaltask.ExternalTaskQuery;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinitionQuery;
import org.camunda.bpm.engine.runtime.ExecutionQuery;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.JobQuery;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Activity;
import org.camunda.bpm.model.bpmn.instance.Event;
import org.camunda.bpm.model.bpmn.instance.Gateway;

/**
 * Convenience class to access only camunda *BPMN* related Assertions
 * PLUS helper methods. Usage is possible, if you only need BPMN Tests and
 * mandatory if you still use Camunda Platform lower than 7.2 version.
 *
 * Use it with a static import:
 *
 * import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.*;
 *
 * @see org.camunda.bpm.engine.test.assertions.ProcessEngineTests
 *      for full Camunda Platform Assertions functionality
 *
 */
public class BpmnAwareTests extends AbstractAssertions {

  private static final String DUPLICATED_NAME = "$DUPLICATED_NAME$";
  public static final long DEFAULT_LOCK_DURATION_EXTERNAL_TASK = 30L * 1000L;// 30 seconds
  public static final String DEFAULT_WORKER_EXTERNAL_TASK      = "anonymousWorker";

  protected BpmnAwareTests() {}

  /**
   * Assert that... the given ProcessDefinition meets your expectations.
   *
   * @param   actual ProcessDefinition under test
   * @return  Assert object offering ProcessDefinition specific assertions.
   */
  public static ProcessDefinitionAssert assertThat(final ProcessDefinition actual) {
    return ProcessDefinitionAssert.assertThat(processEngine(), actual);
  }

  /**
   * Assert that... the given ProcessInstance meets your expectations.
   *
   * @param   actual ProcessInstance under test
   * @return  Assert object offering ProcessInstance specific assertions.
   */
  public static ProcessInstanceAssert assertThat(final ProcessInstance actual) {
    return ProcessInstanceAssert.assertThat(processEngine(), actual);
  }

  /**
   * Assert that... the given Task meets your expectations.
   *
   * @param   actual Task under test
   * @return  Assert object offering Task specific assertions.
   */
  public static TaskAssert assertThat(final Task actual) {
    return TaskAssert.assertThat(processEngine(), actual);
  }

  /**
   * Assert that... the given ExternalTask meets your expectations.
   *
   * @param   actual ExternalTask under test
   * @return  Assert object offering Task specific assertions.
   */
  public static ExternalTaskAssert assertThat(final ExternalTask actual) {
    return ExternalTaskAssert.assertThat(processEngine(), actual);
  }

  /**
   * Assert that... the given Job meets your expectations.
   *
   * @param   actual Job under test
   * @return  Assert object offering Job specific assertions.
   */
  public static JobAssert assertThat(final Job actual) {
    return JobAssert.assertThat(processEngine(), actual);
  }

  /**
   * Helper method to easily access RuntimeService
   *
   * @return  RuntimeService of process engine bound to this testing thread
   * @see     org.camunda.bpm.engine.RuntimeService
   */
  public static RuntimeService runtimeService() {
    return processEngine().getRuntimeService();
  }

  /**
   * Helper method to easily access AuthorizationService
   *
   * @return  AuthorizationService of process engine bound to this
   *          testing thread
   * @see     org.camunda.bpm.engine.AuthorizationService
   */
  public static AuthorizationService authorizationService() {
    return processEngine().getAuthorizationService();
  }

  /**
   * Helper method to easily access FormService
   *
   * @return  FormService of process engine bound to this testing thread
   * @see     org.camunda.bpm.engine.FormService
   */
  public static FormService formService() {
    return processEngine().getFormService();
  }

  /**
   * Helper method to easily access HistoryService
   *
   * @return  HistoryService of process engine bound to this testing thread
   * @see     org.camunda.bpm.engine.HistoryService
   */
  public static HistoryService historyService() {
    return processEngine().getHistoryService();
  }

  /**
   * Helper method to easily access IdentityService
   *
   * @return  IdentityService of process engine bound to this testing thread
   * @see     org.camunda.bpm.engine.IdentityService
   */
  public static IdentityService identityService() {
    return processEngine().getIdentityService();
  }

  /**
   * Helper method to easily access ManagementService
   *
   * @return  ManagementService of process engine bound to this testing thread
   * @see     org.camunda.bpm.engine.ManagementService
   */
  public static ManagementService managementService() {
    return processEngine().getManagementService();
  }

  /**
   * Helper method to easily access RepositoryService
   *
   * @return  RepositoryService of process engine bound to this testing thread
   * @see     org.camunda.bpm.engine.RepositoryService
   */
  public static RepositoryService repositoryService() {
    return processEngine().getRepositoryService();
  }

  /**
   * Helper method to easily access TaskService
   *
   * @return  TaskService of process engine bound to this testing thread
   * @see     org.camunda.bpm.engine.TaskService
   */
  public static TaskService taskService() {
    return processEngine().getTaskService();
  }

  /**
   * Helper method to easily access ExternalTaskService
   *
   * @return ExternalTaskService of process engine bound to this testing thread
   * @see org.camunda.bpm.engine.ExternalTaskService
   */
  public static ExternalTaskService externalTaskService() {
    return processEngine().getExternalTaskService();
  }

  /**
   * Helper method to easily access DecisionService
   *
   * @return DecisionService of process engine bound to this testing thread
   * @see org.camunda.bpm.engine.DecisionService
   */
  public static DecisionService decisionService() {
    return processEngine().getDecisionService();
  }

  /**
   * Helper method to easily create a new TaskQuery
   *
   * @return  new TaskQuery for process engine bound to this testing thread
   * @see     org.camunda.bpm.engine.task.TaskQuery
   */
  public static TaskQuery taskQuery() {
    return taskService().createTaskQuery();
  }

  /**
   * Helper method to easily create a new ExternalTaskQuery
   *
   * @return  new ExternalTaskQuery for process engine bound to this testing thread
   * @see     org.camunda.bpm.engine.externaltask.ExternalTaskQuery
   */
  public static ExternalTaskQuery externalTaskQuery() {
    return externalTaskService().createExternalTaskQuery();
  }

  /**
   * Helper method to easily create a new JobQuery
   *
   * @return  new JobQuery for process engine bound to this testing thread
   * @see     org.camunda.bpm.engine.runtime.JobQuery
   */
  public static JobQuery jobQuery() {
    return managementService().createJobQuery();
  }

  /**
   * Helper method to easily create a new ProcessInstanceQuery
   *
   * @return  new ProcessInstanceQuery for process engine bound to this
   *          testing thread
   * @see     org.camunda.bpm.engine.runtime.ProcessInstanceQuery
   */
  public static ProcessInstanceQuery processInstanceQuery() {
    return runtimeService().createProcessInstanceQuery();
  }

  /**
   * Helper method to easily create a new ProcessDefinitionQuery
   *
   * @return  new ProcessDefinitionQuery for process engine bound to this
   *          testing thread
   * @see     org.camunda.bpm.engine.repository.ProcessDefinitionQuery
   */
  public static ProcessDefinitionQuery processDefinitionQuery() {
    return repositoryService().createProcessDefinitionQuery();
  }

  /**
   * Helper method to easily create a new ExecutionQuery
   *
   * @return  new ExecutionQuery for process engine bound to this testing thread
   * @see     org.camunda.bpm.engine.runtime.ExecutionQuery
   */
  public static ExecutionQuery executionQuery() {
    return runtimeService().createExecutionQuery();
  }

  /**
   * Helper method to easily construct a map of process variables
   *
   * @param   key (obligatory) key of first process variable
   * @param   value (obligatory) value of first process variable
   * @param   furtherKeyValuePairs (optional) key/value pairs for further
   *          process variables
   * @return  a map of process variables by passing a list of String,
   *          Object key value pairs.
   */
  public static Map<String, Object> withVariables(final String key, final Object value, final Object... furtherKeyValuePairs) {
    if (key == null)
      throw new IllegalArgumentException(format("Illegal call of withVariables(key = '%s', value = '%s', ...) - key must not be null!", key, value));
    final Map<String, Object> map = new HashMap<>();
    map.put(key, value);
    if (furtherKeyValuePairs != null) {
      if (furtherKeyValuePairs.length % 2 != 0) {
        throw new IllegalArgumentException(format("Illegal call of withVariables() - must have an even number of arguments, but found length = %s!", furtherKeyValuePairs.length + 2));
      }
      for (int i = 0; i < furtherKeyValuePairs.length; i += 2) {
        if (!(furtherKeyValuePairs[i] instanceof String))
          throw new IllegalArgumentException(format("Illegal call of withVariables() - keys must be strings, found object of type '%s'!", furtherKeyValuePairs[i] != null ? furtherKeyValuePairs[i].getClass().getName() : null));
        map.put((String) furtherKeyValuePairs[i], furtherKeyValuePairs[i + 1]);
      }
    }
    return map;
  }

  /**
   * Helper method to easily access the only task currently
   * available in the context of the last asserted process
   * instance.
   *
   * @return  the only task of the last asserted process
   *          instance. May return null if no such task exists.
   * @throws  java.lang.IllegalStateException in case more
   *          than one task is delivered by the underlying
   *          query or in case no process instance was asserted
   *          yet.
   */
  public static Task task() {
    return task(taskQuery());
  }

  /**
   * Helper method to easily access the only task currently
   * available in the context of the given process instance.
   *
   * @param   processInstance the process instance for which
   *          a task should be retrieved.
   * @return  the only task of the process instance. May
   *          return null if no such task exists.
   * @throws  java.lang.IllegalStateException in case more
   *          than one task is delivered by the underlying
   *          query.
   */
  public static Task task(ProcessInstance processInstance) {
    return task(taskQuery(), processInstance);
  }

  /**
   * Helper method to easily access the only task with the
   * given taskDefinitionKey currently available in the context
   * of the last asserted process instance.
   *
   * @param   taskDefinitionKey the key of the task that should
   *          be retrieved.
   * @return  the only task of the last asserted process
   *          instance. May return null if no such task exists.
   * @throws  java.lang.IllegalStateException in case more
   *          than one task is delivered by the underlying
   *          query or in case no process instance was asserted
   *          yet.
   */
  public static Task task(String taskDefinitionKey) {
    Assertions.assertThat(taskDefinitionKey).isNotNull();
    return task(taskQuery().taskDefinitionKey(taskDefinitionKey));
  }

  /**
   * Helper method to easily access the only task with the
   * given taskDefinitionKey currently available in the context
   * of the given process instance.
   *
   * @param   taskDefinitionKey the key of the task that should
   *          be retrieved.
   * @param   processInstance the process instance for which
   *          a task should be retrieved.
   * @return  the only task of the given process instance. May
   *          return null if no such task exists.
   * @throws  java.lang.IllegalStateException in case more
   *          than one task is delivered by the underlying
   *          query.
   */
  public static Task task(String taskDefinitionKey, ProcessInstance processInstance) {
    Assertions.assertThat(taskDefinitionKey).isNotNull();
    return task(taskQuery().taskDefinitionKey(taskDefinitionKey), processInstance);
  }

  /**
   * Helper method to easily access the only task compliant to
   * a given taskQuery and currently available in the context
   * of the last asserted process instance.
   *
   * @param   taskQuery the query with which the task should
   *          be retrieved. This query will be further narrowed
   *          to the last asserted process instance.
   * @return  the only task of the last asserted process instance
   *          and compliant to the given query. May return null
   *          in case no such task exists.
   * @throws  java.lang.IllegalStateException in case more
   *          than one task is delivered by the underlying
   *          query or in case no process instance was asserted
   *          yet.
   */
  public static Task task(TaskQuery taskQuery) {
    ProcessInstanceAssert lastAssert = AbstractProcessAssert.getLastAssert(ProcessInstanceAssert.class);
    if (lastAssert == null)
      throw new IllegalStateException(
        "Call a process instance assertion first - " +
          "e.g. assertThat(processInstance)... !"
      );
    return task(taskQuery, lastAssert.getActual());
  }

  /**
   * Helper method to easily access the only task compliant to
   * a given taskQuery and currently available in the context
   * of the given process instance.
   *
   * @param   taskQuery the query with which the task should
   *          be retrieved. This query will be further narrowed
   *          to the given process instance.
   * @param   processInstance the process instance for which
   *          a task should be retrieved.
   * @return  the only task of the given process instance and
   *          compliant to the given query. May return null in
   *          case no such task exists.
   * @throws  java.lang.IllegalStateException in case more
   *          than one task is delivered by the underlying
   *          query.
   */
  public static Task task(TaskQuery taskQuery, ProcessInstance processInstance) {
    return assertThat(processInstance).isNotNull().task(taskQuery).getActual();
  }

  /**
   * Helper method to easily access the only external task currently
   * available in the context of the last asserted process instance.
   *
   * @return the only external task of the last asserted process instance.
   *         May return null if no such external task exists.
   * @throws java.lang.IllegalStateException
   *           in case more than one external task is delivered by the underlying
   *           query or in case no process instance was asserted yet.
   */
  public static ExternalTask externalTask() {
    return externalTask(externalTaskQuery());
  }

  /**
   * Helper method to easily access the only external task currently
   * available in the context of the given process instance.
   *
   * @param   processInstance the process instance for which
   *          an external task should be retrieved.
   * @return  the only external task of the process instance.
   *          May return null if no such external task exists.
   * @throws  java.lang.IllegalStateException in case more
   *          than one external task is delivered by the underlying
   *          query.
   */
  public static ExternalTask externalTask(ProcessInstance processInstance) {
    return externalTask(externalTaskQuery(), processInstance);
  }

  /**
   * Helper method to easily access the only external task with the
   * given activityId currently available in the context
   * of the last asserted process instance.
   *
   * @param   activityId the key of the external task that should
   *          be retrieved.
   * @return  the only external task of the last asserted process
   *          instance. May return null if no such external task exists.
   * @throws  java.lang.IllegalStateException in case more
   *          than one external task is delivered by the underlying
   *          query or in case no process instance was asserted
   *          yet.
   */
  public static ExternalTask externalTask(String activityId) {
    Assertions.assertThat(activityId).isNotNull();
    return externalTask(externalTaskQuery().activityId(activityId));
  }

  /**
   * Helper method to easily access the only external task with the
   * given activityId currently available in the context
   * of the given process instance.
   *
   * @param   activityId the key of the external task that should
   *          be retrieved.
   * @param   processInstance the process instance for which
   *          a external task should be retrieved.
   * @return  the only external task of the given process instance. May
   *          return null if no such external task exists.
   * @throws  java.lang.IllegalStateException in case more
   *          than one external task is delivered by the underlying
   *          query.
   */
  public static ExternalTask externalTask(String activityId, ProcessInstance processInstance) {
    Assertions.assertThat(activityId).isNotNull();
    return externalTask(externalTaskQuery().activityId(activityId), processInstance);
  }

  /**
   * Helper method to easily access the only external task compliant to
   * a given externalTaskQuery and currently available in the context
   * of the last asserted process instance.
   *
   * @param   externalTaskQuery the query with which the external task should
   *          be retrieved. This query will be further narrowed
   *          to the last asserted process instance.
   * @return  the only external task of the last asserted process instance
   *          and compliant to the given query. May return null
   *          in case no such external task exists.
   * @throws  java.lang.IllegalStateException in case more
   *          than one external task is delivered by the underlying
   *          query or in case no process instance was asserted
   *          yet.
   */
  public static ExternalTask externalTask(ExternalTaskQuery externalTaskQuery) {
    ProcessInstanceAssert lastAssert = AbstractProcessAssert.getLastAssert(ProcessInstanceAssert.class);
    if (lastAssert == null)
      throw new IllegalStateException(
        "Call a process instance assertion first - " +
          "e.g. assertThat(processInstance)... !"
      );
    return externalTask(externalTaskQuery, lastAssert.getActual());
  }

  /**
   * Helper method to easily access the only external task compliant to
   * a given externalTaskQuery and currently available in the context
   * of the given process instance.
   *
   * @param   externalTaskQuery the query with which the external task should
   *          be retrieved. This query will be further narrowed
   *          to the given process instance.
   * @param   processInstance the process instance for which
   *          a external task should be retrieved.
   * @return  the only external task of the given process instance and
   *          compliant to the given query. May return null in
   *          case no such external task exists.
   * @throws  java.lang.IllegalStateException in case more
   *          than one external task is delivered by the underlying
   *          query.
   */
  public static ExternalTask externalTask(ExternalTaskQuery externalTaskQuery, ProcessInstance processInstance) {
    return assertThat(processInstance).isNotNull().externalTask(externalTaskQuery).getActual();
  }

  /**
   * Helper method to easily access the process definition
   * on which the last asserted process instance is based.
   *
   * @return  the process definition on which the last
   *          asserted process instance is based.
   * @throws  java.lang.IllegalStateException in case no
   *          process instance was asserted yet.
   */
  public static ProcessDefinition processDefinition() {
    ProcessInstanceAssert lastAssert = AbstractProcessAssert.getLastAssert(ProcessInstanceAssert.class);
    if (lastAssert == null)
      throw new IllegalStateException(
        "Call a process instance assertion first - " +
          "e.g. assertThat(processInstance)... !"
      );
    return processDefinition(lastAssert.getActual());
  }

  /**
   * Helper method to easily access the process definition
   * on which the given process instance is based.
   *
   * @param   processInstance the process instance for which
   *          the definition should be retrieved.
   * @return  the process definition on which the given
   *          process instance is based.
   */
  public static ProcessDefinition processDefinition(ProcessInstance processInstance) {
    assertThat(processInstance).isNotNull();
    return processDefinition(processDefinitionQuery().processDefinitionId(processInstance.getProcessDefinitionId()));
  }

  /**
   * Helper method to easily access the process definition with the
   * given processDefinitionKey.
   *
   * @param   processDefinitionKey the key of the process definition
   *          that should be retrieved.
   * @return  the process definition with the given key.
   *          May return null if no such process definition exists.
   */
  public static ProcessDefinition processDefinition(String processDefinitionKey) {
    Assertions.assertThat(processDefinitionKey).isNotNull();
    return processDefinition(processDefinitionQuery().processDefinitionKey(processDefinitionKey));
  }

  /**
   * Helper method to easily access the process definition compliant
   * to a given process definition query.
   *
   * @param   processDefinitionQuery the query with which the process
   *          definition should be retrieved.
   * @return  the process definition compliant to the given query. May
   *          return null in case no such process definition exists.
   * @throws  org.camunda.bpm.engine.ProcessEngineException in case more
   *          than one process definition is delivered by the underlying
   *          query.
   */
  public static ProcessDefinition processDefinition(ProcessDefinitionQuery processDefinitionQuery) {
    return processDefinitionQuery.singleResult();
  }

  /**
   * Helper method to easily access the only called process instance
   * currently available in the context of the last asserted process
   * instance.
   *
   * @return  the only called process instance called by the last asserted process
   *          instance. May return null if no such process instance exists.
   * @throws  java.lang.IllegalStateException in case more
   *          than one process instance is delivered by the underlying
   *          query or in case no process instance was asserted
   *          yet.
   */
  public static ProcessInstance calledProcessInstance() {
    return calledProcessInstance(processInstanceQuery());
  }

  /**
   * Helper method to easily access the only called process instance
   * currently available in the context of the given process instance.
   *
   * @param   processInstance the process instance for which
   *          a called process instance should be retrieved.
   * @return  the only called process instance called by the given process
   *          instance. May return null if no such process instance exists.
   * @throws  java.lang.IllegalStateException in case more
   *          than one process instance is delivered by the underlying
   *          query.
   */
  public static ProcessInstance calledProcessInstance(ProcessInstance processInstance) {
    return calledProcessInstance(processInstanceQuery(), processInstance);
  }

  /**
   * Helper method to easily access the only called process instance with
   * the given processDefinitionKey currently available in the context
   * of the last asserted process instance.
   *
   * @param   processDefinitionKey the key of the process instance that should
   *          be retrieved.
   * @return  the only such process instance called by the last asserted process
   *          instance. May return null if no such process instance exists.
   * @throws  java.lang.IllegalStateException in case more
   *          than one process instance is delivered by the underlying
   *          query or in case no process instance was asserted
   *          yet.
   */
  public static ProcessInstance calledProcessInstance(String processDefinitionKey) {
    Assertions.assertThat(processDefinitionKey).isNotNull();
    return calledProcessInstance(processInstanceQuery().processDefinitionKey(processDefinitionKey));
  }

  /**
   * Helper method to easily access the only called process instance with the
   * given processDefinitionKey currently available in the context
   * of the given process instance.
   *
   * @param   processDefinitionKey the key of the process instance that should
   *          be retrieved.
   * @param   processInstance the process instance for which
   *          a called process instance should be retrieved.
   * @return  the only such process instance called by the given process instance.
   *          May return null if no such process instance exists.
   * @throws  java.lang.IllegalStateException in case more
   *          than one process instance is delivered by the underlying
   *          query.
   */
  public static ProcessInstance calledProcessInstance(String processDefinitionKey, ProcessInstance processInstance) {
    Assertions.assertThat(processDefinitionKey).isNotNull();
    return calledProcessInstance(processInstanceQuery().processDefinitionKey(processDefinitionKey), processInstance);
  }

  /**
   * Helper method to easily access the only called process instance compliant to
   * a given processInstanceQuery and currently available in the context
   * of the last asserted process instance.
   *
   * @param   processInstanceQuery the query with which the called process instance should
   *          be retrieved. This query will be further narrowed to the last asserted
   *          process instance.
   * @return  the only such process instance called by the last asserted process instance and
   *          compliant to the given query. May return null in case no such task exists.
   * @throws  java.lang.IllegalStateException in case more
   *          than one process instance is delivered by the underlying query or in case no
   *          process instance was asserted yet.
   */
  public static ProcessInstance calledProcessInstance(ProcessInstanceQuery processInstanceQuery) {
    ProcessInstanceAssert lastAssert = AbstractProcessAssert.getLastAssert(ProcessInstanceAssert.class);
    if (lastAssert == null)
      throw new IllegalStateException(
        "Call a process instance assertion first - " +
          "e.g. assertThat(processInstance)... !"
      );
    return calledProcessInstance(processInstanceQuery, lastAssert.getActual());
  }

  /**
   * Helper method to easily access the only called process instance compliant to
   * a given processInstanceQuery and currently available in the context of the given
   * process instance.
   *
   * @param   processInstanceQuery the query with which the process instance should
   *          be retrieved. This query will be further narrowed to the given process
   *          instance.
   * @param   processInstance the process instance for which
   *          a called process instance should be retrieved.
   * @return  the only such process instance called by the given process instance and
   *          compliant to the given query. May return null in
   *          case no such process instance exists.
   * @throws  java.lang.IllegalStateException in case more
   *          than one instance is delivered by the underlying
   *          query.
   */
  public static ProcessInstance calledProcessInstance(ProcessInstanceQuery processInstanceQuery, ProcessInstance processInstance) {
    return assertThat(processInstance).isNotNull().calledProcessInstance(processInstanceQuery).getActual();
  }

  /**
   * Helper method to easily access the only job currently
   * available in the context of the last asserted process
   * instance.
   *
   * @return  the only job of the last asserted process
   *          instance. May return null if no such job exists.
   * @throws  java.lang.IllegalStateException in case more
   *          than one job is delivered by the underlying
   *          query or in case no process instance was asserted
   *          yet.
   */
  public static Job job() {
    return job(jobQuery());
  }

  /**
   * Helper method to easily access the only job currently
   * available in the context of the given process instance.
   *
   * @param   processInstance the process instance for which
   *          a job should be retrieved.
   * @return  the only job of the process instance. May
   *          return null if no such task exists.
   * @throws  java.lang.IllegalStateException in case more
   *          than one job is delivered by the underlying
   *          query.
   */
  public static Job job(ProcessInstance processInstance) {
    return job(jobQuery(), processInstance);
  }

  /**
   * Helper method to easily access the only job with the
   * given activityId currently available in the context
   * of the last asserted process instance.
   *
   * @param   activityId the id of the job that should
   *          be retrieved.
   * @return  the only job of the last asserted process
   *          instance. May return null if no such job exists.
   * @throws  java.lang.IllegalStateException in case more
   *          than one job is delivered by the underlying
   *          query or in case no process instance was asserted
   *          yet.
   */
  public static Job job(String activityId) {
    ProcessInstanceAssert lastAssert = AbstractProcessAssert.getLastAssert(ProcessInstanceAssert.class);
    if (lastAssert == null)
      throw new IllegalStateException(
        "Call a process instance assertion first - " +
          "e.g. assertThat(processInstance)... !"
      );
    return job(activityId, lastAssert.getActual());
  }

  /**
   * Helper method to easily access the only job with the
   * given activityId currently available in the context
   * of the given process instance.
   *
   * @param   activityId the activityId of the job that should
   *          be retrieved.
   * @param   processInstance the process instance for which
   *          a job should be retrieved.
   * @return  the only job of the given process instance. May
   *          return null if no such job exists.
   * @throws  java.lang.IllegalStateException in case more
   *          than one job is delivered by the underlying
   *          query.
   */
  public static Job job(String activityId, ProcessInstance processInstance) {
    return assertThat(processInstance).isNotNull().job(activityId).getActual();
  }

  /**
   * Helper method to easily access the only job compliant to
   * a given jobQuery and currently available in the context
   * of the last asserted process instance.
   *
   * @param   jobQuery the query with which the job should
   *          be retrieved. This query will be further narrowed
   *          to the last asserted process instance.
   * @return  the only job of the last asserted process instance
   *          and compliant to the given query. May return null
   *          in case no such task exists.
   * @throws  java.lang.IllegalStateException in case more
   *          than one job is delivered by the underlying
   *          query or in case no process instance was asserted
   *          yet.
   */
  public static Job job(JobQuery jobQuery) {
    ProcessInstanceAssert lastAssert = AbstractProcessAssert.getLastAssert(ProcessInstanceAssert.class);
    if (lastAssert == null)
      throw new IllegalStateException(
        "Call a process instance assertion first - " +
          "e.g. assertThat(processInstance)... !"
      );
    return job(jobQuery, lastAssert.getActual());
  }

  /**
   * Helper method to easily access the only job compliant to
   * a given jobQuery and currently available in the context
   * of the given process instance.
   *
   * @param   jobQuery the query with which the job should
   *          be retrieved. This query will be further narrowed
   *          to the given process instance.
   * @param   processInstance the process instance for which
   *          a job should be retrieved.
   * @return  the only job of the given process instance and
   *          compliant to the given query. May return null in
   *          case no such job exists.
   * @throws  java.lang.IllegalStateException in case more
   *          than one job is delivered by the underlying
   *          query.
   */
  public static Job job(JobQuery jobQuery, ProcessInstance processInstance) {
    return assertThat(processInstance).isNotNull().job(jobQuery).getActual();
  }

  /**
   * Helper method to easily claim a task for a specific
   * assignee.
   *
   * @param   task Task to be claimed for an assignee
   * @param   assigneeUserId userId of assignee for which
   *          the task should be claimed
   * @return  the assigned task - properly refreshed to its
   *          assigned state.
   */
  public static Task claim(Task task, String assigneeUserId) {
    if (task == null || assigneeUserId == null)
      throw new IllegalArgumentException(format("Illegal call " +
        "of claim(task = '%s', assigneeUserId = '%s') - both must " +
        "not be null!", task, assigneeUserId));
    taskService().claim(task.getId(), assigneeUserId);
    return taskQuery().taskId(task.getId()).singleResult();
  }

  /**
   * Helper method to easily unclaim a task.
   *
   * @param   task Task to be claimed for an assignee
   * @return  the assigned task - properly refreshed to its
   *          unassigned state.
   */
  public static Task unclaim(Task task) {
    if (task == null)
      throw new IllegalArgumentException(format("Illegal call " +
        "of unclaim(task = '%s') - task must " +
        "not be null!", task));
    taskService().claim(task.getId(), null);
    return taskQuery().taskId(task.getId()).singleResult();
  }

  /**
   * Helper method to easily complete a task and pass some
   * process variables.
   *
   * @param   task Task to be completed
   * @param   variables Process variables to be passed to the
   *          process instance when completing the task. For
   *          setting those variables, you can use
   *          withVariables(String key, Object value, ...)
   */
  public static void complete(Task task, Map<String, Object> variables) {
    if (task == null || variables == null)
      throw new IllegalArgumentException(format("Illegal call of complete(task = '%s', variables = '%s') - both must not be null!", task, variables));
    taskService().complete(task.getId(), variables);
  }

  /**
   * Helper method to easily complete a task.
   *
   * @param   task Task to be completed
   */
  public static void complete(Task task) {
    if (task == null)
      throw new IllegalArgumentException("Illegal call of complete(task = 'null') - must not be null!");
    taskService().complete(task.getId());
  }

  /**
   * Helper method to easily fetch, lock and complete an external task.<p>
   * Note: if multiple external tasks exist that can be locked for the topic of
   * the given external task, this method might throw an
   * {@link IllegalStateException} if an external task with a different id is
   * locked by chance. In this case, it is more advisable to use the
   * {@link #fetchAndLock(String, String, int) fetchAndLock} and
   * {@link #complete(LockedExternalTask)} methods to achieve reliable results.
   *
   * @param externalTask
   *          External task to be completed
   */
  public static void complete(ExternalTask externalTask) {
    if (externalTask == null) {
      throw new IllegalArgumentException("Illegal call of completeExternalTask(externalTask = 'null') - must not be null!");
    }
    complete(externalTask, Collections.<String, Object>emptyMap());
  }

  /**
   * Helper method to easily fetch, lock and complete an external task.<p>
   * Note: if multiple external tasks exist that can be locked for the topic of
   * the given external task, this method might throw an
   * {@link IllegalStateException} if an external task with a different id is
   * locked by chance. In this case, it is more advisable to use the
   * {@link #fetchAndLock(String, String, int) fetchAndLock} and
   * {@link #complete(LockedExternalTask, Map)} methods to achieve reliable results.
   *
   * @param externalTask
   *          External task to be completed
   * @param variables
   *          Process variables to be passed to the process instance when
   *          completing the task. For setting those variables, you can use
   *          withVariables(String key, Object value, ...)
   */
  public static void complete(ExternalTask externalTask, Map<String, Object> variables) {
    if (externalTask == null || variables == null) {
      throw new IllegalArgumentException(format("Illegal call of completeExternalTask(externalTask = '%s', variables = '%s') - both must not be null!", externalTask, variables));
    }
    complete(externalTask, variables, Collections.EMPTY_MAP);
  }

  /**
   * Helper method to easily fetch, lock and complete an external task.<p>
   * Note: if multiple external tasks exist that can be locked for the topic of
   * the given external task, this method might throw an
   * {@link IllegalStateException} if an external task with a different id is
   * locked by chance. In this case, it is more advisable to use the
   * {@link #fetchAndLock(String, String, int) fetchAndLock} and
   * {@link #complete(LockedExternalTask, Map)} methods to achieve reliable results.
   *
   * @param externalTask
   *          External task to be completed
   * @param variables
   *          Process variables to be passed to the process instance when
   *          completing the task. For setting those variables, you can use
   *          withVariables(String key, Object value, ...)
   * @param localVariables
   *          Local process variables to be passed to the process instance when
   *          completing the task. For setting those variables, you can use
   *          withVariables(String key, Object value, ...)
   */
  public static void complete(ExternalTask externalTask, Map<String, Object> variables, Map<String, Object> localVariables) {
    if (externalTask == null || (variables == null && localVariables == null)) {
      throw new IllegalArgumentException(format("Illegal call of completeExternalTask(externalTask = '%s', variables = '%s', localvariables = '%s') - provide external task and either variables or local variables.", externalTask, variables, localVariables));
    }
    List<LockedExternalTask> lockedTasks = fetchAndLock(externalTask.getTopicName(), DEFAULT_WORKER_EXTERNAL_TASK, 1);
    if (lockedTasks.isEmpty()) {
      throw new NotFoundException(format("No lockable external task found for externalTask = '%s', variables = '%s', localVariables = '%s'", externalTask, variables, localVariables));
    }
    if (!lockedTasks.get(0).getId().equals(externalTask.getId())) {
      throw new IllegalStateException(format("Multiple external tasks found for externalTask = '%s', variables = '%s', localVariables = '%s'", externalTask, variables, localVariables));
    }
    complete(lockedTasks.get(0), variables, localVariables);
  }
  /**
   * Helper method to easily fetch and lock external tasks from a given topic
   * using a given workerId. The tasks will be locked for
   * {@link #DEFAULT_LOCK_DURATION_EXTERNAL_TASK} milliseconds.
   *
   * @param topic
   *          the name of the topic to query external tasks from
   * @param workerId
   *          the id of the worker to lock the tasks for
   * @param maxResults
   *          the maximum number of tasks to return
   * @return list of external tasks locked for the given workerId
   */
  public static List<LockedExternalTask> fetchAndLock(String topic, String workerId, int maxResults) {
    if (workerId == null || topic == null) {
      throw new IllegalArgumentException(format("Illegal call of fetchAndLock(topic = '%s', workerId = '%s', maxResults = '%s') - all must not be null!", topic, workerId, maxResults));
    }
    return externalTaskService().fetchAndLock(maxResults, workerId)
      .topic(topic, DEFAULT_LOCK_DURATION_EXTERNAL_TASK)
      .execute();
  }

  /**
   * Helper method to easily complete a locked external task.
   *
   * @param lockedExternalTask
   *          an external task that was locked using the
   *          {@link #fetchAndLock(String, String, int) fetchAndLock} method
   */
  public static void complete(LockedExternalTask lockedExternalTask) {
    if (lockedExternalTask == null) {
      throw new IllegalArgumentException("Illegal call of completeExternalTask(lockedExternalTask = 'null') - must not be null!");
    }
    complete(lockedExternalTask, Collections.<String, Object>emptyMap());
  }

  /**
   * Helper method to easily complete a locked external task.
   *
   * @param lockedExternalTask
   *          an external task that was locked using the
   *          {@link #fetchAndLock(String, String, int) fetchAndLock} method
   * @param variables
   *          Process variables to be passed to the process instance when
   *          completing the task. For setting those variables, you can use
   *          withVariables(String key, Object value, ...)
   */
  public static void complete(LockedExternalTask lockedExternalTask, Map<String, Object> variables) {
    if (lockedExternalTask == null || variables == null) {
      throw new IllegalArgumentException(format("Illegal call of completeExternalTask(lockedExternalTask = '%s', variables = '%s') - both must not be null!", lockedExternalTask, variables));
    }
    complete(lockedExternalTask, variables, Collections.EMPTY_MAP);
  }

  /**
   * Helper method to easily complete a locked external task.
   *
   * @param lockedExternalTask
   *          an external task that was locked using the
   *          {@link #fetchAndLock(String, String, int) fetchAndLock} method
   * @param variables
   *          Process variables to be passed to the process instance when
   *          completing the task. For setting those variables, you can use
   *          withVariables(String key, Object value, ...)
   * @param localVariables
   *          Local process variables to be passed to the process instance when
   *          completing the task. For setting those variables, you can use
   *          withVariables(String key, Object value, ...)
   */
  public static void complete(LockedExternalTask lockedExternalTask, Map<String, Object> variables, Map<String, Object> localVariables) {
    if (lockedExternalTask == null || (variables == null && localVariables == null)) {
      throw new IllegalArgumentException(format("Illegal call of completeExternalTask(lockedExternalTask = '%s', variables = '%s', localVariables = '%s') - provide locked external task and either variables or local variables.", lockedExternalTask, variables, localVariables));
    }
    externalTaskService().complete(lockedExternalTask.getId(), lockedExternalTask.getWorkerId(), variables, localVariables);
  }

  /**
   * Helper method to easily execute a job.
   *
   * @param   job Job to be executed.
   */
  public static void execute(Job job) {
    if (job == null)
      throw new IllegalArgumentException(format("Illegal call of execute(job = '%s') - must not be null!", job));
    Job current = jobQuery().jobId(job.getId()).singleResult();
    if (current == null)
      throw new IllegalStateException(format("Illegal state when calling execute(job = '%s') - job does not exist anymore!", job));
    managementService().executeJob(job.getId());
  }

  /**
   * Maps any element (task, event, gateway) from the name to the ID.
   *
   * @param name
   * @return the ID of the element
   */
  public static String findId(String name) {
    if (name == null) {
      throw new IllegalArgumentException("Illegal call of findId(name = 'null') - must not be null!");
    }
    Map<String, String> nameToIDMapping = new HashMap<>();
    // find deployed process models
    List<ProcessDefinition> processDefinitions = repositoryService()
        .createProcessDefinitionQuery()
        .orderByProcessDefinitionVersion().asc()
        .list();
    // parse process models
    for (ProcessDefinition processDefinition : processDefinitions) {
      BpmnModelInstance bpmnModelInstance = repositoryService().getBpmnModelInstance(processDefinition.getId());
      Collection<Activity> activities = bpmnModelInstance.getModelElementsByType(Activity.class);
      for (Activity activity: activities) {
        insertAndCheckForDuplicateNames(nameToIDMapping, activity.getName(), activity.getId());
      }
      Collection<Event> events = bpmnModelInstance.getModelElementsByType(Event.class);
      for (Event event : events) {
        insertAndCheckForDuplicateNames(nameToIDMapping, event.getName(), event.getId());
      }
      Collection<Gateway> gateways = bpmnModelInstance.getModelElementsByType(Gateway.class);
      for (Gateway gateway : gateways) {
        insertAndCheckForDuplicateNames(nameToIDMapping, gateway.getName(), gateway.getId());
       }
    }
    // look for name and return ID
    Assertions.assertThat(nameToIDMapping.containsKey(name))
      .overridingErrorMessage("Element with name '%s' doesn't exist", name)
      .isTrue();
    Assertions.assertThat(nameToIDMapping.get(name))
      .overridingErrorMessage("Name '%s' is not unique", name)
      .isNotEqualTo(DUPLICATED_NAME);
    return nameToIDMapping.get(name);
  }

  private static void insertAndCheckForDuplicateNames(Map<String, String> nameToIDMapping, String name, String id) {
    if (nameToIDMapping.containsKey(name)) {
      if (nameToIDMapping.get(name).equals(id)) {
        // already inserted as diagram includes two pools
      } else {
        nameToIDMapping.put(name, DUPLICATED_NAME);
      }
    } else {
      nameToIDMapping.put(name, id);
    }
  }
}
