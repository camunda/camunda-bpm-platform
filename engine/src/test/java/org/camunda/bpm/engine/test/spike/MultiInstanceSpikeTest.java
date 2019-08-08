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
package org.camunda.bpm.engine.test.spike;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.impl.BpmnModelConstants;
import org.camunda.bpm.model.bpmn.instance.MultiInstanceLoopCharacteristics;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

public class MultiInstanceSpikeTest {

  public ProvidedProcessEngineRule providedEngineRule = new ProvidedProcessEngineRule();
  public ProcessEngineTestRule testRule = new ProcessEngineTestRule(providedEngineRule);

  @Rule
  public RuleChain chain = RuleChain.outerRule(providedEngineRule).around(testRule);

  public static final String PROCESS_KEY = "process";
  public static final BpmnModelInstance MI_USER_TASK = Bpmn.createExecutableProcess(PROCESS_KEY)
    .startEvent()
    .userTask("task")
      .multiInstance()
      .parallel()
      .cardinality("${cnt}")
      .multiInstanceDone()
    .endEvent()
    .done();

  static {
    declareFunkyMultiInstance(MI_USER_TASK, "task");
  }

  protected RuntimeService runtimeService;

  @Before
  public void setUp() {
    runtimeService = providedEngineRule.getRuntimeService();
  }

  @Test
  public void shouldUseSubTreeScope() {
    // given
    testRule.deploy(MI_USER_TASK);

    int nrOfInstances = 100;
    VariableMap variables = Variables.createVariables().putValue("cnt", nrOfInstances);

    // when
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(PROCESS_KEY, variables);

    // then
    assertThat(processInstance).isNotNull();
  }

  public static void declareFunkyMultiInstance(BpmnModelInstance instance, String miActivityId) {
    ModelElementInstance miActivity = instance.getModelElementById(miActivityId);
    MultiInstanceLoopCharacteristics miCharacteristics = miActivity
        .getChildElementsByType(MultiInstanceLoopCharacteristics.class)
        .iterator()
        .next();
    miCharacteristics.setAttributeValueNs(BpmnModelConstants.CAMUNDA_NS, "funky", "true");
  }

}
