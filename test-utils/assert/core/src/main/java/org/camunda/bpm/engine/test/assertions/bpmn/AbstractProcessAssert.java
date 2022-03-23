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

import java.util.HashMap;
import java.util.Map;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.CaseService;
import org.camunda.bpm.engine.ExternalTaskService;
import org.camunda.bpm.engine.FormService;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.externaltask.ExternalTaskQuery;
import org.camunda.bpm.engine.history.HistoricActivityInstanceQuery;
import org.camunda.bpm.engine.history.HistoricCaseActivityInstanceQuery;
import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery;
import org.camunda.bpm.engine.history.HistoricVariableInstanceQuery;
import org.camunda.bpm.engine.repository.CaseDefinitionQuery;
import org.camunda.bpm.engine.repository.ProcessDefinitionQuery;
import org.camunda.bpm.engine.runtime.CaseExecutionQuery;
import org.camunda.bpm.engine.runtime.CaseInstanceQuery;
import org.camunda.bpm.engine.runtime.ExecutionQuery;
import org.camunda.bpm.engine.runtime.JobQuery;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.task.TaskQuery;

public abstract class AbstractProcessAssert<S extends AbstractProcessAssert<S, A>, A> extends AbstractAssert<S, A> {

  protected ProcessEngine engine;

  private static ThreadLocal<Map<Class<?>, AbstractProcessAssert<?, ?>>>
    lastAsserts = new ThreadLocal<>();

  protected AbstractProcessAssert(ProcessEngine engine, A actual, Class<?> selfType) {
    super(actual, selfType);
    this.engine = engine;
    setLastAssert(selfType, this);
  }

  /*
   * Delivers the the actual object under test.
   */
  public A getActual() {
    return actual;
  }

  /*
   * Method definition meant to deliver the current/refreshed persistent state of
   * the actual object under test and expecting that such a current state actually exists.
   */
  protected A getExistingCurrent() {
    Assertions.assertThat(actual)
      .overridingErrorMessage("Expecting assertion to be called on non-null object, but found it to be null!")
      .isNotNull();
    A current =getCurrent();
    Assertions.assertThat(current)
      .overridingErrorMessage(
        "Expecting %s to be unfinished, but found that it already finished!",
        toString(actual))
      .isNotNull();
    return current;
  }

  /*
   * Abstract method definition meant to deliver the current/refreshed persistent state of
   * the actual object under test. Needs to be correctly implemented by implementations of this.
   */
  protected abstract A getCurrent();

  /*
   * Abstract method definition meant to deliver a loggable string representation of the
   * given object of same type as the actual object under test.
   */
  protected abstract String toString(A object);

  public static void resetLastAsserts() {
    getLastAsserts().clear();
  }

  @SuppressWarnings("unchecked")
  protected static <S extends AbstractProcessAssert<?, ?>> S getLastAssert(Class<S> assertClass) {
    return (S) getLastAsserts().get(assertClass);
  }

  private static void setLastAssert(Class<?> assertClass, AbstractProcessAssert<?, ?> assertInstance) {
    getLastAsserts().put(assertClass, assertInstance);
  }

  private static Map<Class<?>, AbstractProcessAssert<?, ?>> getLastAsserts() {
    Map<Class<?>, AbstractProcessAssert<?, ?>> asserts = lastAsserts.get();
    if (asserts == null)
      lastAsserts.set(asserts = new HashMap<>());
    return asserts;
  }

  protected RepositoryService repositoryService() {
    return engine.getRepositoryService();
  }

  protected RuntimeService runtimeService() {
    return engine.getRuntimeService();
  }

  protected FormService formService() {
    return engine.getFormService();
  }

  protected TaskService taskService() {
    return engine.getTaskService();
  }

  protected HistoryService historyService() {
    return engine.getHistoryService();
  }

  protected ManagementService managementService() {
    return engine.getManagementService();
  }

  protected ExternalTaskService externalTaskService() {
    return engine.getExternalTaskService();
  }

  protected CaseService caseService() {
    return engine.getCaseService();
  }

  /*
   * TaskQuery, unnarrowed. Narrow this to {@link ProcessInstance} (or {@link ProcessDefinition})
   * by overriding this method in sub classes specialised to verify a specific
   * process engine domain class.
   */
  protected TaskQuery taskQuery() {
    return taskService().createTaskQuery();
  }

  /*
   * JobQuery, unnarrowed. Narrow this to {@link ProcessInstance} (or {@link ProcessDefinition})
   * by overriding this method in sub classes specialised to verify a specific
   * process engine domain class.
   */
  protected JobQuery jobQuery() {
    return managementService().createJobQuery();
  }

  /*
   * ProcessInstanceQuery, unnarrowed. Narrow this to {@link ProcessInstance} (or
   * {@link ProcessDefinition}) by overriding this method in sub classes specialised to
   * verify a specific process engine domain class.
   */
  protected ProcessInstanceQuery processInstanceQuery() {
    return runtimeService().createProcessInstanceQuery();
  }

  /*
   * ExecutionQuery, unnarrowed. Narrow this to {@link ProcessInstance} (or {@link ProcessDefinition})
   * by overriding this method in sub classes specialised to verify a specific
   * process engine domain class.
   */
  protected ExecutionQuery executionQuery() {
    return runtimeService().createExecutionQuery();
  }

  /*
   * HistoricActivityInstanceQuery, unnarrowed. Narrow this to {@link ProcessInstance} (or
   * {@link ProcessDefinition}) by overriding this method in sub classes specialised to
   * verify a specific process engine domain class.
   */
  protected HistoricActivityInstanceQuery historicActivityInstanceQuery() {
    return historyService().createHistoricActivityInstanceQuery();
  }

  /*
   * HistoricProcessInstanceQuery, unnarrowed. Narrow this to {@link ProcessInstance} (or
   * {@link ProcessDefinition}) by overriding this method in sub classes specialised to
   * verify a specific process engine domain class.
   */
  protected HistoricProcessInstanceQuery historicProcessInstanceQuery() {
    return historyService().createHistoricProcessInstanceQuery();
  }

  /*
   * HistoricVariableInstanceQuery, unnarrowed. Narrow this to {@link ProcessInstance} (or
   * {@link ProcessDefinition}) by overriding this method in sub classes specialised to
   * verify a specific process engine domain class.
   */
  protected HistoricVariableInstanceQuery historicVariableInstanceQuery() {
    return historyService().createHistoricVariableInstanceQuery();
  }

  /*
   * ProcessDefinitionQuery, unnarrowed. Narrow this to {@link ProcessInstance} (or
   * {@link ProcessDefinition}) by overriding this method in sub classes specialised to
   * verify a specific process engine domain class.
   */
  protected ProcessDefinitionQuery processDefinitionQuery() {
    return repositoryService().createProcessDefinitionQuery();
  }

  /*
   * ExternalTaskQuery, unnarrowed. Narrow this to {@link ProcessInstance} (or
   * {@link ProcessDefinition}) by overriding this method in sub classes specialised to
   * verify a specific process engine domain class.
   */
  protected ExternalTaskQuery externalTaskQuery() {
    return externalTaskService().createExternalTaskQuery();
  }

  /*
   * CaseExecutionQuery, unnarrowed. Narrow this to {@link CaseInstance} (or
   * {@link CaseDefinition}) by overriding this method in sub classes specialized to
   * verify a specific process engine domain class.
   */
  protected CaseExecutionQuery caseExecutionQuery() {
    return caseService().createCaseExecutionQuery();
  }

  /*
   * CaseDefinitionQuery, unnarrowed. Narrow this to {@link CaseInstance} (or
   * {@link CaseDefinition}) by overriding this method in sub classes specialized to
   * verify a specific process engine domain class.
   */
  protected CaseDefinitionQuery caseDefinitionQuery() {
    return repositoryService().createCaseDefinitionQuery();
  }

  /*
   * CaseInstanceQuery, unnarrowed. Narrow this to {@link CaseInstance} (or
   * {@link CaseDefinition}) by overriding this method in sub classes specialized to
   * verify a specific process engine domain class.
   */
  protected CaseInstanceQuery caseInstanceQuery() {
    return caseService().createCaseInstanceQuery();
  }

  /*
   * HistoricCaseActivityInstanceQuery, unnarrowed. Narrow this to {@link CaseInstance} (or
   * {@link CaseDefinition}) by overriding this method in sub classes specialised to
   * verify a specific process engine domain class.
   */
  protected HistoricCaseActivityInstanceQuery historicCaseActivityInstanceQuery() {
    return historyService().createHistoricCaseActivityInstanceQuery();
  }

}
