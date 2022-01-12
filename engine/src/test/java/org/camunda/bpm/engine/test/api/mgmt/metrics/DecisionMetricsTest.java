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
package org.camunda.bpm.engine.test.api.mgmt.metrics;

import static org.junit.Assert.assertEquals;

import org.camunda.bpm.dmn.engine.impl.DefaultDmnEngineConfiguration;
import org.camunda.bpm.dmn.engine.spi.DmnEngineMetricCollector;
import org.camunda.bpm.engine.DecisionService;
import org.camunda.bpm.engine.management.Metrics;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.util.ResetDmnConfigUtil;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.BusinessRuleTask;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DecisionMetricsTest extends AbstractMetricsTest {

  public static final String DECISION_DEFINITION_KEY = "decision";
  public static final String DRD_DISH_DECISION_TABLE = "org/camunda/bpm/engine/test/dmn/deployment/drdDish.dmn11.xml";
  public static final String DMN_DECISION_LITERAL_EXPRESSION = "org/camunda/bpm/engine/test/api/dmn/DecisionWithLiteralExpression.dmn";
  public static final String DMN_FILE = "org/camunda/bpm/engine/test/api/mgmt/metrics/ExecutedDecisionElementsTest.dmn11.xml";
  public static VariableMap VARIABLES = Variables.createVariables().putValue("status", "").putValue("sum", 100);

  protected DecisionService decisionService;

  @Before
  public void setUp() {
    DefaultDmnEngineConfiguration dmnEngineConfiguration = processEngineConfiguration
        .getDmnEngineConfiguration();
    ResetDmnConfigUtil.reset(dmnEngineConfiguration)
        .enableFeelLegacyBehavior(true)
        .init();

    decisionService = engineRule.getDecisionService();
  }

  @After
  public void restore() {
    DefaultDmnEngineConfiguration dmnEngineConfiguration = processEngineConfiguration
        .getDmnEngineConfiguration();
    ResetDmnConfigUtil.reset(dmnEngineConfiguration)
        .enableFeelLegacyBehavior(false)
        .init();
  }

  @Override
  protected void clearMetrics() {
    super.clearMetrics();
    DmnEngineMetricCollector metricCollector = processEngineConfiguration.getDmnEngineConfiguration()
      .getEngineMetricCollector();
    metricCollector.clearExecutedDecisionInstances();
    metricCollector.clearExecutedDecisionElements();
  }

  @Test
  public void testBusinessRuleTask() {
    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess("testProcess")
        .startEvent()
        .businessRuleTask("task")
        .endEvent()
        .done();

    BusinessRuleTask task = modelInstance.getModelElementById("task");
    task.setCamundaDecisionRef("decision");

    testRule.deploy(repositoryService.createDeployment()
        .addModelInstance("process.bpmn", modelInstance)
        .addClasspathResource(DMN_FILE));

    assertEquals(0l, getExecutedDecisionInstances());
    assertEquals(0l, getDecisionInstances());
    assertEquals(0l, getExecutedDecisionElements());
    assertEquals(0l, getExecutedDecisionInstancesFromDmnEngine());
    assertEquals(0l, getExecutedDecisionElementsFromDmnEngine());

    runtimeService.startProcessInstanceByKey("testProcess", VARIABLES);

    assertEquals(1l, getExecutedDecisionInstances());
    assertEquals(1l, getDecisionInstances());
    assertEquals(16l, getExecutedDecisionElements());
    assertEquals(1l, getExecutedDecisionInstancesFromDmnEngine());
    assertEquals(16l, getExecutedDecisionElementsFromDmnEngine());

    processEngineConfiguration.getDbMetricsReporter().reportNow();

    assertEquals(1l, getExecutedDecisionInstances());
    assertEquals(1l, getDecisionInstances());
    assertEquals(16l, getExecutedDecisionElements());
    assertEquals(1l, getExecutedDecisionInstancesFromDmnEngine());
    assertEquals(16l, getExecutedDecisionElementsFromDmnEngine());
  }

  @Test
  @Deployment(resources = DMN_DECISION_LITERAL_EXPRESSION)
  public void shouldCountDecisionLiteralExpression() {
    // given

    // when
    decisionService
        .evaluateDecisionByKey(DECISION_DEFINITION_KEY)
        .variables(VARIABLES)
        .evaluate();

    // then
    assertEquals(1l, getExecutedDecisionInstances());
    assertEquals(1l, getDecisionInstances());

    processEngineConfiguration.getDbMetricsReporter().reportNow();

    assertEquals(1l, getExecutedDecisionInstances());
    assertEquals(1l, getDecisionInstances());
  }

  @Test
  @Deployment(resources = DRD_DISH_DECISION_TABLE)
  public void shouldCountDecisionDRG() {
    // given

    // when
    decisionService
        .evaluateDecisionByKey("dish-decision")
        .variables(VARIABLES
                       .putValue("temperature", 32)
                       .putValue("dayType", "Weekend"))
        .evaluate();

    // then
    assertEquals(3l, getExecutedDecisionInstances());
    assertEquals(3l, getDecisionInstances());

    processEngineConfiguration.getDbMetricsReporter().reportNow();

    assertEquals(3l, getExecutedDecisionInstances());
    assertEquals(3l, getDecisionInstances());
  }

  protected long getExecutedDecisionInstances() {
    return managementService.createMetricsQuery()
        .name(Metrics.EXECUTED_DECISION_INSTANCES)
        .sum();
  }

  protected long getDecisionInstances() {
    return managementService.createMetricsQuery()
        .name(Metrics.DECISION_INSTANCES)
        .sum();
  }

  protected long getExecutedDecisionElements() {
    return managementService.createMetricsQuery()
        .name(Metrics.EXECUTED_DECISION_ELEMENTS)
        .sum();
  }

  protected long getExecutedDecisionInstancesFromDmnEngine() {
    return processEngineConfiguration.getDmnEngineConfiguration()
        .getEngineMetricCollector()
        .getExecutedDecisionInstances();
  }

  protected long getExecutedDecisionElementsFromDmnEngine() {
    return processEngineConfiguration.getDmnEngineConfiguration()
        .getEngineMetricCollector()
        .getExecutedDecisionElements();
  }

}
