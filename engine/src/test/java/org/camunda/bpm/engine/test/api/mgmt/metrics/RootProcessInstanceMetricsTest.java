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

import java.util.Collections;
import java.util.Map;

import org.camunda.bpm.engine.management.Metrics;
import org.camunda.bpm.engine.management.MetricsQuery;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.CallActivity;
import org.junit.Test;

public class RootProcessInstanceMetricsTest extends AbstractMetricsTest {

  public static final String DMN_FILE
      = "org/camunda/bpm/engine/test/api/mgmt/metrics/ExecutedDecisionElementsTest.dmn11.xml";
  public static VariableMap VARIABLES = Variables.createVariables()
      .putValue("status", "")
      .putValue("sum", 100);

  protected static final String BASE_INSTANCE_KEY = "baseProcess";
  protected static final BpmnModelInstance BASE_INSTANCE = Bpmn.createExecutableProcess(BASE_INSTANCE_KEY)
      .startEvent()
      .endEvent()
      .done();

  protected static final String CALLED_DMN_INSTANCE_KEY = "calledDMNProcess";
  protected static final BpmnModelInstance CALLED_DMN_INSTANCE = Bpmn.createExecutableProcess(CALLED_DMN_INSTANCE_KEY)
      .startEvent()
      .businessRuleTask()
        .camundaDecisionRef("decision")
      .endEvent()
      .done();

  protected static final String CALLING_INSTANCE_KEY = "callingProcess";

  @Override
  protected void clearMetrics() {
    super.clearMetrics();
    processEngineConfiguration.getDmnEngineConfiguration()
        .getEngineMetricCollector()
        .clearExecutedDecisionElements();
  }

  @Test
  public void shouldCountOneRootProcessInstance() {
    // given
    testRule.deploy(BASE_INSTANCE);

    // when
    runtimeService.startProcessInstanceByKey(BASE_INSTANCE_KEY);

    // then
    MetricsQuery query = managementService.createMetricsQuery();
    assertEquals(1l, query.name(Metrics.ROOT_PROCESS_INSTANCE_START).sum());
    assertEquals(1l, query.name(Metrics.PROCESS_INSTANCES).sum());

    // and force the db metrics reporter to report
    processEngineConfiguration.getDbMetricsReporter().reportNow();

    // still 1
    assertEquals(1l, query.name(Metrics.ROOT_PROCESS_INSTANCE_START).sum());
    assertEquals(1l, query.name(Metrics.PROCESS_INSTANCES).sum());
  }

  @Test
  public void shouldCountRootProcessInstanceWithCallActivities() {
    // given
    BpmnModelInstance callingInstance = getCallingInstance(BASE_INSTANCE_KEY, Collections.EMPTY_MAP);
    testRule.deploy(BASE_INSTANCE, callingInstance);

    // when
    runtimeService.startProcessInstanceByKey(CALLING_INSTANCE_KEY);

    // then
    MetricsQuery query = managementService.createMetricsQuery();
    assertEquals(1l, query.name(Metrics.ROOT_PROCESS_INSTANCE_START).sum());
    assertEquals(1l, query.name(Metrics.PROCESS_INSTANCES).sum());

    // and force the db metrics reporter to report
    processEngineConfiguration.getDbMetricsReporter().reportNow();

    // still 1
    assertEquals(1l, query.name(Metrics.ROOT_PROCESS_INSTANCE_START).sum());
    assertEquals(1l, query.name(Metrics.PROCESS_INSTANCES).sum());
  }

  @Test
  public void shouldCountRootProcessInstanceAndDecisionInstanceWithBusinessRuleTask() {
    // given
    BpmnModelInstance callingInstance = getCallingInstance(CALLED_DMN_INSTANCE_KEY, VARIABLES);
    testRule.deploy(repositoryService.createDeployment()
                        .addClasspathResource(DMN_FILE)
                        .addModelInstance("calledProcess.bpmn", CALLED_DMN_INSTANCE)
                        .addModelInstance("callingProcess.bpmn", callingInstance));

    // when
    runtimeService.startProcessInstanceByKey(CALLING_INSTANCE_KEY, VARIABLES);

    // then
    MetricsQuery query = managementService.createMetricsQuery();
    assertEquals(1l, query.name(Metrics.ROOT_PROCESS_INSTANCE_START).sum());
    assertEquals(1l, query.name(Metrics.PROCESS_INSTANCES).sum());
    assertEquals(1l, query.name(Metrics.EXECUTED_DECISION_INSTANCES).sum());

    // and force the db metrics reporter to report
    processEngineConfiguration.getDbMetricsReporter().reportNow();

    // still 1
    assertEquals(1l, query.name(Metrics.ROOT_PROCESS_INSTANCE_START).sum());
    assertEquals(1l, query.name(Metrics.PROCESS_INSTANCES).sum());
    assertEquals(1l, query.name(Metrics.EXECUTED_DECISION_INSTANCES).sum());
  }

  protected BpmnModelInstance getCallingInstance(String calledInstanceKey, Map variables) {
    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(CALLING_INSTANCE_KEY)
        .startEvent()
        .callActivity("calledProcess")
          .calledElement(calledInstanceKey)
        .endEvent()
        .done();

    // pass any variables to the call activity
    CallActivity callActivity = modelInstance.getModelElementById("calledProcess");
    variables.keySet()
        .iterator()
        .forEachRemaining(name -> callActivity.builder().camundaIn((String) name, (String) name));

    return modelInstance;
  }
}