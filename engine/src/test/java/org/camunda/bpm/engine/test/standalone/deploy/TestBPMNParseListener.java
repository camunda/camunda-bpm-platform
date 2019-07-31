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
package org.camunda.bpm.engine.test.standalone.deploy;

import static org.camunda.bpm.engine.impl.bpmn.parser.BpmnParse.COMPENSATE_EVENT_DEFINITION;

import java.util.List;

import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.form.StartFormData;
import org.camunda.bpm.engine.form.TaskFormData;
import org.camunda.bpm.engine.impl.bpmn.behavior.CompensationEventActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.NoneEndEventActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.NoneStartEventActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.parser.AbstractBpmnParseListener;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParse;
import org.camunda.bpm.engine.impl.bpmn.parser.CompensateEventDefinition;
import org.camunda.bpm.engine.impl.form.handler.StartFormHandler;
import org.camunda.bpm.engine.impl.form.handler.TaskFormHandler;
import org.camunda.bpm.engine.impl.persistence.entity.DeploymentEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.task.TaskDefinition;
import org.camunda.bpm.engine.impl.util.xml.Element;
import org.camunda.bpm.engine.variable.VariableMap;

/**
 * @author Frederik Heremans
 */
public class TestBPMNParseListener extends AbstractBpmnParseListener {

  public void parseRootElement(Element rootElement, List<ProcessDefinitionEntity> processDefinitions) {
    // Change the key of all deployed process-definitions
    for (ProcessDefinitionEntity entity : processDefinitions) {
      entity.setKey(entity.getKey() + "-modified");
    }
  }

  public void parseStartEvent(Element startEventElement, ScopeImpl scope, ActivityImpl startEventActivity) {
    // Change activity behavior
    startEventActivity.setActivityBehavior(new TestNoneStartEventActivityBehavior());

    // Change start form handler for processes whose name starts with "alterFormHandlers"
    if (scope instanceof ProcessDefinitionEntity) {
      ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) scope;
      if (processDefinition.getName().startsWith("alterFormHandlers")) {
        StartFormHandler previous = processDefinition.getStartFormHandler();
        processDefinition.setStartFormHandler(new TestStartFormHandler(previous));
      }
    }
  }

  public void parseIntermediateThrowEvent(Element intermediateEventElement, ScopeImpl scope, ActivityImpl activity) {
    // Change activity behavior
    Element compensateEventDefinitionElement = intermediateEventElement.element(COMPENSATE_EVENT_DEFINITION);
    if (compensateEventDefinitionElement != null) {
      String activityRef = compensateEventDefinitionElement.attribute("activityRef");
      CompensateEventDefinition compensateEventDefinition = new CompensateEventDefinition();
      compensateEventDefinition.setActivityRef(activityRef);
      compensateEventDefinition.setWaitForCompletion(false);

      activity.setActivityBehavior(new TestCompensationEventActivityBehavior(compensateEventDefinition));
    }
  }

  public void parseEndEvent(Element endEventElement, ScopeImpl scope, ActivityImpl activity) {
    // Change activity behavior
    activity.setActivityBehavior(new TestNoneEndEventActivityBehavior());
  }

  @Override
  public void parseUserTask(Element userTaskElement, ScopeImpl scope, ActivityImpl activity) {
    // Change task form handler for processes whose name starts with "alterFormHandlers"
    ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) scope;
    if (processDefinition.getName().startsWith("alterFormHandlers")) {
      TaskDefinition taskDefinition = ((UserTaskActivityBehavior) activity.getActivityBehavior()).getTaskDefinition();
      TaskFormHandler previous = taskDefinition.getTaskFormHandler();
      taskDefinition.setTaskFormHandler(new TestTaskFormHandler(previous));
    }
  }

  public static class TestNoneStartEventActivityBehavior extends NoneStartEventActivityBehavior {

  }

  public static class TestNoneEndEventActivityBehavior extends NoneEndEventActivityBehavior {

  }

  public static class TestCompensationEventActivityBehavior extends CompensationEventActivityBehavior {

    public TestCompensationEventActivityBehavior(CompensateEventDefinition compensateEventDefinition) {
      super(compensateEventDefinition);
    }
  }
  
  public static class TestStartFormHandler implements StartFormHandler {
    public static StartFormHandler previousHandler  = null;
    public static boolean parseConfigurationCalled  = false;
    public static boolean createStartFormCalled     = false;
    public static boolean submitFormVariablesCalled = false;
    public static Object submitFormVariableValue    = null;

    private final StartFormHandler previous;

    public TestStartFormHandler(StartFormHandler previous) {
      this.previous = previous;
      previousHandler = previous;
    }

    @Override
    public void parseConfiguration(Element activityElement, DeploymentEntity deployment, ProcessDefinitionEntity processDefinition, BpmnParse bpmnParse) {
      parseConfigurationCalled = true;
      previous.parseConfiguration(activityElement, deployment, processDefinition, bpmnParse);
    }

    @Override
    public StartFormData createStartFormData(ProcessDefinitionEntity processDefinition) {
      createStartFormCalled = true;
      return previous.createStartFormData(processDefinition);
    }

    @Override
    public void submitFormVariables(VariableMap properties, VariableScope variableScope) {
      submitFormVariablesCalled = true;
      submitFormVariableValue = properties.get("key");
      previous.submitFormVariables(properties, variableScope);
    }
  }

  public static class TestTaskFormHandler implements TaskFormHandler {
    public static TaskFormHandler previousHandler   = null;
    public static boolean parseConfigurationCalled  = false;
    public static boolean createTaskFormCalled      = false;
    public static boolean submitFormVariablesCalled = false;
    public static Object submitFormVariableValue    = null;

    private final TaskFormHandler previous;

    public TestTaskFormHandler(TaskFormHandler previous) {
      this.previous = previous;
      previousHandler = previous;
    }

    @Override
    public void parseConfiguration(Element activityElement, DeploymentEntity deployment, ProcessDefinitionEntity processDefinition, BpmnParse bpmnParse) {
      parseConfigurationCalled = true;
      previous.parseConfiguration(activityElement, deployment, processDefinition, bpmnParse);
    }

    @Override
    public TaskFormData createTaskForm(TaskEntity task) {
      createTaskFormCalled = true;
      return previous.createTaskForm(task);
    }

    @Override
    public void submitFormVariables(VariableMap properties, VariableScope variableScope) {
      submitFormVariablesCalled = true;
      submitFormVariableValue = properties.get("key");
      previous.submitFormVariables(properties, variableScope);
    }
  }
}
