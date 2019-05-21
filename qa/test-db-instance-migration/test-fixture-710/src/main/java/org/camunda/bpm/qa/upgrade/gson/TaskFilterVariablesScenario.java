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
package org.camunda.bpm.qa.upgrade.gson;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.filter.Filter;
import org.camunda.bpm.engine.task.TaskQuery;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.qa.upgrade.DescribesScenario;
import org.camunda.bpm.qa.upgrade.ScenarioSetup;

/**
 * @author Tassilo Weidner
 */
public class TaskFilterVariablesScenario {

  @Deployment
  public static String deploy() {
    return "org/camunda/bpm/qa/upgrade/gson/oneTaskProcess.bpmn20.xml";
  }

  @DescribesScenario("initTaskFilterVariables")
  public static ScenarioSetup initTaskFilterVariables() {
    return new ScenarioSetup() {
      public void execute(ProcessEngine engine, String scenarioName) {
        // boolean filter
        engine.getRuntimeService().startProcessInstanceByKey("oneTaskProcess_710",
          "TaskFilterVariablesScenario_filterBooleanVariable",
          Variables.createVariables().putValue("booleanVariable", true));

        TaskQuery query = engine.getTaskService().createTaskQuery()
          .processVariableValueEquals("booleanVariable", true);

        Filter filter = engine.getFilterService().newTaskFilter("filterBooleanVariable");
        filter.setQuery(query);

        engine.getFilterService().saveFilter(filter);

        // int filter
        engine.getRuntimeService().startProcessInstanceByKey("oneTaskProcess_710",
          "TaskFilterVariablesScenario_filterIntVariable",
          Variables.createVariables().putValue("intVariable", 7));

        query = engine.getTaskService().createTaskQuery()
          .processVariableValueEquals("intVariable", 7);

        filter = engine.getFilterService().newTaskFilter("filterIntVariable");
        filter.setQuery(query);

        engine.getFilterService().saveFilter(filter);

        // int out of range filter
        engine.getRuntimeService().startProcessInstanceByKey("oneTaskProcess_710",
          "TaskFilterVariablesScenario_filterIntOutOfRangeVariable",
          Variables.createVariables().putValue("longVariable", Integer.MAX_VALUE+1L));

        query = engine.getTaskService().createTaskQuery()
          .processVariableValueEquals("longVariable", Integer.MAX_VALUE+1L);

        filter = engine.getFilterService().newTaskFilter("filterIntOutOfRangeVariable");
        filter.setQuery(query);

        engine.getFilterService().saveFilter(filter);

        // double filter
        engine.getRuntimeService().startProcessInstanceByKey("oneTaskProcess_710",
          "TaskFilterVariablesScenario_filterDoubleVariable",
          Variables.createVariables().putValue("doubleVariable", 88.89D));

        query = engine.getTaskService().createTaskQuery()
          .processVariableValueEquals("doubleVariable", 88.89D);

        filter = engine.getFilterService().newTaskFilter("filterDoubleVariable");
        filter.setQuery(query);

        engine.getFilterService().saveFilter(filter);

        // string filter
        engine.getRuntimeService().startProcessInstanceByKey("oneTaskProcess_710",
          "TaskFilterVariablesScenario_filterStringVariable",
          Variables.createVariables().putValue("stringVariable", "aVariableValue"));

        query = engine.getTaskService().createTaskQuery()
          .processVariableValueEquals("stringVariable", "aVariableValue");

        filter = engine.getFilterService().newTaskFilter("filterStringVariable");
        filter.setQuery(query);

        engine.getFilterService().saveFilter(filter);

        // filter null
        engine.getRuntimeService().startProcessInstanceByKey("oneTaskProcess_710",
          "TaskFilterVariablesScenario_filterNullVariable",
        Variables.createVariables().putValue("nullVariable", null));

        query = engine.getTaskService().createTaskQuery()
          .processVariableValueEquals("nullVariable", null);

        filter = engine.getFilterService().newTaskFilter("filterNullVariable");
        filter.setQuery(query);

        engine.getFilterService().saveFilter(filter);
      }
    };
  }
}
