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
package org.camunda.bpm.qa.upgrade;

import org.camunda.bpm.engine.history.HistoricIncidentQuery;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.management.JobDefinitionQuery;
import org.camunda.bpm.engine.runtime.CaseExecutionQuery;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.engine.runtime.CaseInstanceQuery;
import org.camunda.bpm.engine.runtime.ExecutionQuery;
import org.camunda.bpm.engine.runtime.IncidentQuery;
import org.camunda.bpm.engine.runtime.JobQuery;
import org.camunda.bpm.engine.runtime.MessageCorrelationBuilder;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.task.TaskQuery;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.Assert;
import org.junit.runner.Description;

/**
 * @author Thorben Lindhauer
 *
 */
public class UpgradeTestRule extends ProcessEngineRule {

  protected String scenarioTestedByClass = null;
  protected String scenarioName;
  protected String tag;

  public UpgradeTestRule() {
    super("camunda.cfg.xml");
  }

  public UpgradeTestRule(String configurationResource) {
    super(configurationResource);
  }

  @Override
  public void starting(Description description) {
    Class<?> testClass = description.getTestClass();
    if (scenarioTestedByClass == null) {
      ScenarioUnderTest testScenarioClassAnnotation = testClass.getAnnotation(ScenarioUnderTest.class);
      if (testScenarioClassAnnotation != null) {
        scenarioTestedByClass = testScenarioClassAnnotation.value();
      }
    }

    ScenarioUnderTest testScenarioAnnotation = description.getAnnotation(ScenarioUnderTest.class);
    if (testScenarioAnnotation != null) {
      if (scenarioTestedByClass != null) {
        scenarioName = scenarioTestedByClass + "." + testScenarioAnnotation.value();
      } else {
        scenarioName = testScenarioAnnotation.value();
      }
    }

    // method annotation overrides class annotation
    Origin originAnnotation = description.getAnnotation(Origin.class);
    if (originAnnotation == null) {
      originAnnotation = testClass.getAnnotation(Origin.class);
    }

    if (originAnnotation != null) {
      tag = originAnnotation.value();
    }

    if (scenarioName == null) {
      throw new RuntimeException("Could not determine scenario under test for test " + description.getDisplayName());
    }

    super.starting(description);
  }

  public TaskQuery taskQuery() {
    return taskService.createTaskQuery().processInstanceBusinessKey(getBuisnessKey());
  }

  public ExecutionQuery executionQuery() {
    return runtimeService.createExecutionQuery().processInstanceBusinessKey(getBuisnessKey());
  }

  public JobQuery jobQuery() {
    ProcessInstance instance = processInstance();
    return managementService.createJobQuery().processInstanceId(instance.getId());
  }

  public JobDefinitionQuery jobDefinitionQuery() {
    ProcessInstance instance = processInstance();
    return managementService.createJobDefinitionQuery()
            .processDefinitionId(instance.getProcessDefinitionId());
  }

  public IncidentQuery incidentQuery() {
    ProcessInstance processInstance = processInstance();
    return runtimeService.createIncidentQuery()
        .processInstanceId(processInstance.getId());
  }

  public ProcessInstanceQuery processInstanceQuery() {
    return runtimeService
            .createProcessInstanceQuery()
            .processInstanceBusinessKey(getBuisnessKey());
  }

  public ProcessInstance processInstance() {
    ProcessInstance instance = processInstanceQuery().singleResult();

    if (instance == null) {
      throw new RuntimeException("There is no process instance for scenario " + getBuisnessKey());
    }

    return instance;
  }

  public HistoricProcessInstance historicProcessInstance() {
    HistoricProcessInstance historicProcessInstance = historyService
            .createHistoricProcessInstanceQuery()
            .processInstanceBusinessKey(getBuisnessKey())
            .singleResult();

    if (historicProcessInstance == null) {
      throw new RuntimeException("There is no historic process instance for scenario " + getBuisnessKey());
    }

    return historicProcessInstance;
  }

  public HistoricIncidentQuery historicIncidentQuery() {
    ProcessInstance processInstance = processInstance();
    return historyService.createHistoricIncidentQuery()
        .processInstanceId(processInstance.getId());
  }

  public MessageCorrelationBuilder messageCorrelation(String messageName) {
    return runtimeService.createMessageCorrelation(messageName).processInstanceBusinessKey(getBuisnessKey());
  }

  public void assertScenarioEnded() {
    Assert.assertTrue("Process instance for scenario " + getBuisnessKey() + " should have ended",
            processInstanceQuery().singleResult() == null);
  }

  // case //////////////////////////////////////////////////
  public CaseInstanceQuery caseInstanceQuery() {
    return caseService
            .createCaseInstanceQuery()
            .caseInstanceBusinessKey(getBuisnessKey());
  }

  public CaseExecutionQuery caseExecutionQuery() {
    return caseService
            .createCaseExecutionQuery()
            .caseInstanceBusinessKey(getBuisnessKey());
  }

  public CaseInstance caseInstance() {
    CaseInstance instance = caseInstanceQuery().singleResult();

    if (instance == null) {
      throw new RuntimeException("There is no case instance for scenario " + getBuisnessKey());
    }

    return instance;
  }

  public String getScenarioName() {
    return scenarioName;
  }

  public String getBuisnessKey() {
    if (tag != null) {
      return tag + '.' + scenarioName;
    }
    return scenarioName;
  }

  public String getTag() {
    return tag;
  }

  public void setTag(String tag) {
    this.tag = tag;
  }

}
