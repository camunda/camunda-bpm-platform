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
package org.camunda.bpm.engine.test.api.mgmt.metrics;

import org.camunda.bpm.engine.management.Metrics;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.BusinessRuleTask;

public class ExecutedDecisionElementsMetricsTest extends AbstractMetricsTest {

  public static final String DMN_FILE = "org/camunda/bpm/engine/test/api/mgmt/metrics/ExecutedDecisionElementsTest.dmn11.xml";
  public static VariableMap VARIABLES = Variables.createVariables().putValue("status", "").putValue("sum", 100);

  @Override
  protected void clearMetrics() {
    super.clearMetrics();
    processEngineConfiguration.getDmnEngineConfiguration()
      .getEngineMetricCollector()
      .clearExecutedDecisionElements();
  }

  public void testBusinessRuleTask() {
    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess("testProcess")
        .startEvent()
        .businessRuleTask("task")
        .endEvent()
        .done();

    BusinessRuleTask task = modelInstance.getModelElementById("task");
    task.setCamundaDecisionRef("decision");

    deploymentId = repositoryService.createDeployment()
        .addModelInstance("process.bpmn", modelInstance)
        .addClasspathResource(DMN_FILE)
        .deploy().getId();

    assertEquals(0l, getExecutedDecisionElements());
    assertEquals(0l, getExecutedDecisionElementsFromDmnEngine());

    runtimeService.startProcessInstanceByKey("testProcess", VARIABLES);

    assertEquals(16l, getExecutedDecisionElements());
    assertEquals(16l, getExecutedDecisionElementsFromDmnEngine());

    processEngineConfiguration.getDbMetricsReporter().reportNow();

    assertEquals(16l, getExecutedDecisionElements());
    assertEquals(16l, getExecutedDecisionElementsFromDmnEngine());
  }

  protected long getExecutedDecisionElements() {
    return managementService.createMetricsQuery()
        .name(Metrics.EXECUTED_DECISION_ELEMENTS)
        .sum();
  }

  protected long getExecutedDecisionElementsFromDmnEngine() {
    return processEngineConfiguration.getDmnEngineConfiguration()
        .getEngineMetricCollector()
        .getExecutedDecisionElements();
  }

}
