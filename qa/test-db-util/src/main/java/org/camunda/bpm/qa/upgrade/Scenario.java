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

import java.util.Map;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineException;

/**
 * @author Thorben Lindhauer
 *
 */
public class Scenario {

  protected int times = 1;
  protected String name;
  protected String extendedScenario;

  protected ScenarioSetup setup;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getTimes() {
    return times;
  }

  public void setTimes(int times) {
    this.times = times;
  }

  public String getExtendedScenario() {
    return extendedScenario;
  }

  public void setExtendedScenario(String extendedScenario) {
    this.extendedScenario = extendedScenario;
  }

  public void setSetup(ScenarioSetup setup) {
    this.setup = setup;
  }

  public Scenario perform(ScenarioSetup setup) {
    this.setup = setup;
    return this;
  }

  public void createInstances(ProcessEngine engine, Map<String, Scenario> scenarios) {
    for (int i = 1; i <= times; i++) {
      String scenarioInstanceName = name + "." + i;
      create(engine, scenarios, scenarioInstanceName);
    }
  }

  public void create(ProcessEngine engine, Map<String, Scenario> scenarios, String scenarioInstanceName) {
    // recursively set up all extended scenarios first
    if (extendedScenario != null) {
      if (scenarios.containsKey(extendedScenario)) {
        Scenario parentScenario = scenarios.get(extendedScenario);
        parentScenario.create(engine, scenarios, scenarioInstanceName);
      }
      else {
        throw new ProcessEngineException("Extended scenario " + extendedScenario + " not registered");
      }
    }

    if (setup != null) {
      setup.execute(engine, scenarioInstanceName);
    }
  }
}
