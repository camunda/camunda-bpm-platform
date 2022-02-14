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
package org.camunda.bpm.engine.test.history.dmn;

import static org.assertj.core.api.Assertions.assertThat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.camunda.bpm.dmn.engine.impl.DefaultDmnEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.history.HistoricDecisionInputInstance;
import org.camunda.bpm.engine.history.HistoricDecisionInstance;
import org.camunda.bpm.engine.history.HistoricDecisionOutputInstance;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.api.variables.JavaSerializable;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.engine.test.util.ResetDmnConfigUtil;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class HistoricDecisionInstanceInputOutputValueTest {

  protected static final String DECISION_PROCESS = "org/camunda/bpm/engine/test/history/HistoricDecisionInstanceTest.processWithBusinessRuleTask.bpmn20.xml";
  protected static final String DECISION_SINGLE_OUTPUT_DMN = "org/camunda/bpm/engine/test/history/HistoricDecisionInstanceTest.decisionSingleOutput.dmn11.xml";

  @Parameters(name = "{index}: input({0}) = {1}")
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] {
      { "string", "a" },
      { "long", 1L },
      { "double", 2.5 },
      { "bytes", "object".getBytes() },
      { "object", new JavaSerializable("foo") },
      { "object", Collections.singletonList(new JavaSerializable("bar")) }
    });
  }

  @Parameter(0)
  public String valueType;

  @Parameter(1)
  public Object inputValue;

  @Rule
  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();

  @After
  public void tearDown() {
    ClockUtil.setCurrentTime(new Date());
  }

  @Before
  public void enableDmnFeelLegacyBehavior() {
    DefaultDmnEngineConfiguration dmnEngineConfiguration =
        engineRule.getProcessEngineConfiguration()
            .getDmnEngineConfiguration();

    ResetDmnConfigUtil.reset(dmnEngineConfiguration)
        .enableFeelLegacyBehavior(true)
        .init();
  }

  @After
  public void disableDmnFeelLegacyBehavior() {

    DefaultDmnEngineConfiguration dmnEngineConfiguration =
        engineRule.getProcessEngineConfiguration()
            .getDmnEngineConfiguration();

    ResetDmnConfigUtil.reset(dmnEngineConfiguration)
        .enableFeelLegacyBehavior(false)
        .init();
  }

  @Test
  @Deployment(resources = { DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN })
  public void decisionInputInstanceValue() throws ParseException {
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS");
    Date fixedDate = sdf.parse("01/01/2001 01:01:01.000");
    ClockUtil.setCurrentTime(fixedDate);

    startProcessInstanceAndEvaluateDecision(inputValue);

    HistoricDecisionInstance historicDecisionInstance = engineRule.getHistoryService().createHistoricDecisionInstanceQuery().includeInputs().singleResult();
    List<HistoricDecisionInputInstance> inputInstances = historicDecisionInstance.getInputs();
    assertThat(inputInstances).hasSize(1);

    HistoricDecisionInputInstance inputInstance = inputInstances.get(0);
    assertThat(inputInstance.getTypeName()).isEqualTo(valueType);
    assertThat(inputInstance.getValue()).isEqualTo(inputValue);
    assertThat(inputInstance.getCreateTime()).isEqualTo(fixedDate);
  }

  @Test
  @Deployment(resources = { DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN })
  public void decisionOutputInstanceValue() throws ParseException {
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS");
    Date fixedDate = sdf.parse("01/01/2001 01:01:01.000");
    ClockUtil.setCurrentTime(fixedDate);

    startProcessInstanceAndEvaluateDecision(inputValue);

    HistoricDecisionInstance historicDecisionInstance = engineRule.getHistoryService().createHistoricDecisionInstanceQuery().includeOutputs().singleResult();
    List<HistoricDecisionOutputInstance> outputInstances = historicDecisionInstance.getOutputs();
    assertThat(outputInstances).hasSize(1);

    HistoricDecisionOutputInstance outputInstance = outputInstances.get(0);
    assertThat(outputInstance.getTypeName()).isEqualTo(valueType);
    assertThat(outputInstance.getValue()).isEqualTo(inputValue);
    assertThat(outputInstance.getCreateTime()).isEqualTo(fixedDate);
  }

  protected ProcessInstance startProcessInstanceAndEvaluateDecision(Object input) {
    return engineRule.getRuntimeService().startProcessInstanceByKey("testProcess",
        Variables.createVariables().putValue("input1", input));
  }

}
