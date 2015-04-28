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
package org.camunda.bpm.qa.upgrade;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.test.Deployment;

/**
 * @author Thorben Lindhauer
 *
 */
public class ScenarioRunner {

  protected ProcessEngine engine;

  public ScenarioRunner(ProcessEngine engine) {
    this.engine = engine;
  }

  public void setupScenarios(Class<?> clazz) {
    performDeployments(clazz);
    executeScenarioSetups(clazz);
  }

  protected void performDeployments(Class<?> clazz) {
    for (Method method : clazz.getDeclaredMethods()) {
      Deployment deploymentAnnotation = method.getAnnotation(Deployment.class);
      if (deploymentAnnotation != null) {
        Object deploymentResource = null;
        try {
          deploymentResource = method.invoke(null, new Object[0]);
        } catch (Exception e) {
          throw new RuntimeException("Could not invoke method " + clazz.getName() + "#" + method.getName()
              + " specifying a deployment", e);
        }

        if (String.class.isAssignableFrom(method.getReturnType())) {
          String deploymentResourcePath = (String) deploymentResource;
          engine.getRepositoryService()
            .createDeployment()
            .name(clazz.getSimpleName() + "." + method.getName())
            .addClasspathResource(deploymentResourcePath)
            .deploy();
        }
      }
    }
  }

  /**
   * Scans for all scenarios defined in the class and runs them
   */
  protected void executeScenarioSetups(Class<?> clazz) {
    Map<String, Scenario> scenarios = new HashMap<String, Scenario>();

    for (Method method : clazz.getDeclaredMethods()) {
      DescribesScenario scenarioAnnotation = method.getAnnotation(DescribesScenario.class);
      if (scenarioAnnotation != null) {
        Scenario scenario = new Scenario();

        scenario.setName(createScenarioName(clazz, scenarioAnnotation.value()));
        ExtendsScenario extendedScenarioAnnotation = method.getAnnotation(ExtendsScenario.class);
        if (extendedScenarioAnnotation != null) {
          scenario.setExtendedScenario(createScenarioName(clazz, extendedScenarioAnnotation.value()));
        }

        try {
          ScenarioSetup setup = (ScenarioSetup) method.invoke(null, new Object[0]);
          scenario.setSetup(setup);
        } catch (Exception e) {
          throw new RuntimeException("Could not invoke method " + clazz.getName() + "#" + method.getName()
              + " specifying scenarios " + scenario.getName(), e);
        }

        Times timesAnnotation = method.getAnnotation(Times.class);
        if (timesAnnotation != null) {
          scenario.setTimes(timesAnnotation.value());
        }

        scenarios.put(scenario.getName(), scenario);
      }
    }

    for (Scenario scenario : scenarios.values()) {
      setupScenario(scenarios, scenario);
    }
  }

  protected String createScenarioName(Class<?> declaringClass, String name) {
    return declaringClass.getSimpleName() + "." + name;
  }

  protected void setupScenario(Map<String, Scenario> scenarios, Scenario scenario) {
    scenario.createInstances(engine, scenarios);
  }
}
