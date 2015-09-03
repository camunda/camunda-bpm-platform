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
package org.camunda.bpm.integrationtest.functional.scriptengine;

import org.camunda.bpm.engine.management.Metrics;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.integrationtest.util.AbstractFoxPlatformIntegrationTest;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class DmnScriptEngineMetricsTest extends AbstractFoxPlatformIntegrationTest {

  @Deployment
  public static WebArchive processArchive() {

    return initWebArchiveDeployment()
        .addAsResource("org/camunda/bpm/integrationtest/functional/scriptengine/DmnScriptTaskTest.bpmn20.xml", "DmnScriptTaskTest.bpmn20.xml")
        .addAsResource("org/camunda/bpm/integrationtest/functional/scriptengine/Example.dmn10.xml", "Example.dmn10.xml");

  }

  @Test
  public void testDeployProcessArchive() {

    VariableMap variables = Variables.createVariables().putValue("status", "bronze").putValue("sum", 100);

    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testProcess", variables);

    long executedDecisionElements = managementService.createMetricsQuery().name(Metrics.EXECUTED_DECISION_ELEMENTS).sum();
    assertEquals(16l, executedDecisionElements);

    executedDecisionElements = processEngineConfiguration.getDmnEngineConfiguration().getEngineMetricCollector().getExecutedDecisionElements();
    assertEquals(16l, executedDecisionElements);
  }


}
