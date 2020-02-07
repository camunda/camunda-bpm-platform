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
package org.camunda.spin.plugin.impl.feel.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.camunda.bpm.dmn.engine.DmnDecisionResult;
import org.camunda.bpm.dmn.engine.DmnEngine;
import org.camunda.bpm.dmn.engine.DmnEngineConfiguration;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.history.HistoricDecisionInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.commons.utils.IoUtil;
import org.camunda.spin.Spin;
import org.camunda.spin.plugin.variables.JsonListSerializable;
import org.camunda.spin.plugin.variables.XmlListSerializable;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class FeelEngineIT {

  protected static final String DMN_SPIN_JSON_VAR_RULE = "org/camunda/spin/plugin/impl/feel/integration/DecisionFEELInputJSON.dmn11.xml";
  protected static final String DMN_SPIN_XML_VAR_RULE = "org/camunda/spin/plugin/impl/feel/integration/DecisionFEELInputXML.dmn11.xml";
  protected static final String BPMN_BUSINESS_TASK_PROCESS = "org/camunda/spin/plugin/DmnBusinessRuleTaskResultMappingTest.testSingleEntry.bpmn20.xml";
  protected static final List<String> TEST_LIST = Arrays.asList("\"foo\"", "\"bar\"");

  @Rule
  public ProcessEngineRule engineRule = new ProcessEngineRule();

  protected DmnEngine dmnEngine;
  protected RuntimeService runtimeService;
  protected HistoryService historyService;


  @Before
  public void setUp() {
    dmnEngine = DmnEngineConfiguration.createDefaultDmnEngineConfiguration().buildEngine();
    runtimeService = engineRule.getRuntimeService();
    historyService = engineRule.getHistoryService();
  }

  @Test
  public void shouldEvaluateRuleWithJSONVariableCorrectly() {
    // given
    VariableMap varMap = createSpinParsedVariableInMap("JSON");

    // when
    DmnDecisionResult result = evaluateDecision(DMN_SPIN_JSON_VAR_RULE, varMap);

    // then
    assertThat((boolean) result.getSingleEntry()).isTrue();
  }

  @Test
  public void shouldEvaluateRuleWithXMLVariableCorrectly() {
    // given
    VariableMap varMap = createSpinParsedVariableInMap("XML");

    // when
    DmnDecisionResult result = evaluateDecision(DMN_SPIN_XML_VAR_RULE, varMap);

    // then
    assertThat((boolean) result.getSingleEntry()).isTrue();
  }

  @Test
  @Deployment(resources = {DMN_SPIN_JSON_VAR_RULE, BPMN_BUSINESS_TASK_PROCESS})
  public void shouldExecuteProcessWithJSONVariableCorrectly() {
    // given
    VariableMap varMap = createSpinParsedVariableInMap("JSON");

    // when
    runtimeService.startProcessInstanceByKey("testProcess", varMap);

    // then
    HistoricDecisionInstance hdi = historyService.createHistoricDecisionInstanceQuery().includeOutputs().singleResult();
    assertThat(hdi.getOutputs().size()).isOne();
    assertThat((boolean) hdi.getOutputs().get(0).getValue()).isTrue();
  }

  @Test
  @Deployment(resources = {DMN_SPIN_XML_VAR_RULE, BPMN_BUSINESS_TASK_PROCESS})
  public void shouldExecuteProcessWithXMLVariableCorrectly() {
    // given
    VariableMap varMap = createSpinParsedVariableInMap("XML");

    // when
    runtimeService.startProcessInstanceByKey("testProcess", varMap);

    // then
    HistoricDecisionInstance hdi = historyService.createHistoricDecisionInstanceQuery()
                                                 .includeOutputs()
                                                 .singleResult();
    assertThat(hdi.getOutputs().size()).isOne();
    assertThat((boolean) hdi.getOutputs().get(0).getValue()).isTrue();
  }

  // HELPER

  protected DmnDecisionResult evaluateDecision(String path, VariableMap variables) {
    InputStream resource = IoUtil.fileAsStream(path);
    return dmnEngine.evaluateDecision("testDecision", resource, variables);
  }

  protected VariableMap createSpinParsedVariableInMap(String serializationType) {
    if ("json".equalsIgnoreCase(serializationType)) {
      JsonListSerializable<String> jsonList = new JsonListSerializable<>();
      jsonList.setListProperty(TEST_LIST);
      return Variables.createVariables()
                      .putValue("inputVar", Spin.JSON(jsonList.toExpectedJsonString()));

    } else if ("xml".equalsIgnoreCase(serializationType)) {
      XmlListSerializable<String> xmlList = new XmlListSerializable<>();
      xmlList.setListProperty(TEST_LIST);
      return Variables.createVariables()
                      .putValue("inputVar", Spin.XML(xmlList.toExpectedXmlString()));

    } else {
      return Variables.createVariables();

    }
  }
}