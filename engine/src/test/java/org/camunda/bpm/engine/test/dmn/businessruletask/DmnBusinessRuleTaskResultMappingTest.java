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
package org.camunda.bpm.engine.test.dmn.businessruletask;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.ParseException;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.TypedValue;
import org.junit.Test;

/**
 * Tests the mapping of the decision result.  
 *
 * @author Philipp Ossler
 */
public class DmnBusinessRuleTaskResultMappingTest extends PluggableProcessEngineTest {

  protected static final String TEST_DECISION = "org/camunda/bpm/engine/test/dmn/result/DmnBusinessRuleTaskResultMappingTest.dmn11.xml";
  protected static final String CUSTOM_MAPPING_BPMN = "org/camunda/bpm/engine/test/dmn/result/DmnBusinessRuleTaskResultMappingTest.testCustomOutputMapping.bpmn20.xml";
  protected static final String SINGLE_ENTRY_BPMN = "org/camunda/bpm/engine/test/dmn/result/DmnBusinessRuleTaskResultMappingTest.testSingleEntry.bpmn20.xml";
  protected static final String SINGLE_RESULT_BPMN = "org/camunda/bpm/engine/test/dmn/result/DmnBusinessRuleTaskResultMappingTest.testSingleResult.bpmn20.xml";
  protected static final String COLLECT_ENTRIES_BPMN = "org/camunda/bpm/engine/test/dmn/result/DmnBusinessRuleTaskResultMappingTest.testCollectEntries.bpmn20.xml";
  protected static final String RESULT_LIST_BPMN = "org/camunda/bpm/engine/test/dmn/result/DmnBusinessRuleTaskResultMappingTest.testResultList.bpmn20.xml";
  protected static final String DEFAULT_MAPPING_BPMN = "org/camunda/bpm/engine/test/dmn/result/DmnBusinessRuleTaskResultMappingTest.testDefaultMapping.bpmn20.xml";
  protected static final String INVALID_MAPPING_BPMN = "org/camunda/bpm/engine/test/dmn/result/DmnBusinessRuleTaskResultMappingTest.testInvalidMapping.bpmn20.xml";
  protected static final String OVERRIDE_DECISION_RESULT_BPMN = "org/camunda/bpm/engine/test/dmn/result/DmnBusinessRuleTaskResultMappingTest.testOverrideVariable.bpmn20.xml";

  @Deployment(resources = {CUSTOM_MAPPING_BPMN, TEST_DECISION })
  @Test
  public void testCustomOutputMapping() {
    ProcessInstance processInstance = startTestProcess("multiple entries");

    assertEquals("foo", runtimeService.getVariable(processInstance.getId(), "result1"));
    assertEquals(Variables.stringValue("foo"), runtimeService.getVariableTyped(processInstance.getId(), "result1"));

    assertEquals("bar", runtimeService.getVariable(processInstance.getId(), "result2"));
    assertEquals(Variables.stringValue("bar"), runtimeService.getVariableTyped(processInstance.getId(), "result2"));
  }

  @Deployment(resources = { SINGLE_ENTRY_BPMN, TEST_DECISION})
  @Test
  public void testSingleEntryMapping() {
    ProcessInstance processInstance = startTestProcess("single entry");

    assertEquals("foo", runtimeService.getVariable(processInstance.getId(), "result"));
    assertEquals(Variables.stringValue("foo"), runtimeService.getVariableTyped(processInstance.getId(), "result"));
  }

  @Deployment(resources = { SINGLE_RESULT_BPMN, TEST_DECISION })
  @Test
  public void testSingleResultMapping() {
    ProcessInstance processInstance = startTestProcess("multiple entries");

    @SuppressWarnings("unchecked")
    Map<String, Object> output = (Map<String, Object>) runtimeService.getVariable(processInstance.getId(), "result");

    assertEquals(2, output.size());
    assertEquals("foo", output.get("result1"));
    assertEquals("bar", output.get("result2"));
  }

  @Deployment(resources = { COLLECT_ENTRIES_BPMN, TEST_DECISION })
  @Test
  public void testCollectEntriesMapping() {
    ProcessInstance processInstance = startTestProcess("single entry list");

    @SuppressWarnings("unchecked")
    List<String> output = (List<String>) runtimeService.getVariable(processInstance.getId(), "result");

    assertEquals(2, output.size());
    assertEquals("foo", output.get(0));
    assertEquals("foo", output.get(1));
  }

  @Deployment(resources = { RESULT_LIST_BPMN, TEST_DECISION })
  @Test
  public void testResultListMapping() {
    ProcessInstance processInstance = startTestProcess("multiple entries list");

    @SuppressWarnings("unchecked")
    List<Map<String, Object>> resultList = (List<Map<String, Object>>) runtimeService.getVariable(processInstance.getId(), "result");
    assertEquals(2, resultList.size());

    for (Map<String, Object> valueMap : resultList) {
      assertEquals(2, valueMap.size());
      assertEquals("foo", valueMap.get("result1"));
      assertEquals("bar", valueMap.get("result2"));
    }
  }

  @Deployment(resources = { DEFAULT_MAPPING_BPMN, TEST_DECISION })
  @Test
  public void testDefaultResultMapping() {
    ProcessInstance processInstance = startTestProcess("multiple entries list");

    // default mapping is 'resultList'
    @SuppressWarnings("unchecked")
    List<Map<String, Object>> resultList = (List<Map<String, Object>>) runtimeService.getVariable(processInstance.getId(), "result");
    assertEquals(2, resultList.size());

    for (Map<String, Object> valueMap : resultList) {
      assertEquals(2, valueMap.size());
      assertEquals("foo", valueMap.get("result1"));
      assertEquals("bar", valueMap.get("result2"));
    }
  }

  @Deployment(resources = { SINGLE_ENTRY_BPMN, TEST_DECISION })
  @Test
  public void testSingleEntryMappingFailureMultipleOutputs() {
    try {
      startTestProcess("single entry list");

      fail("expect exception");
    } catch (ProcessEngineException e) {
      testRule.assertTextPresent("ENGINE-22001", e.getMessage());
    }
  }

  @Deployment(resources = { SINGLE_ENTRY_BPMN, TEST_DECISION })
  @Test
  public void testSingleEntryMappingFailureMultipleValues() {
    try {
      startTestProcess("multiple entries");

      fail("expect exception");
    } catch (ProcessEngineException e) {
      testRule.assertTextPresent("ENGINE-22001", e.getMessage());
    }
  }

  @Deployment(resources = { SINGLE_RESULT_BPMN, TEST_DECISION })
  @Test
  public void testSingleResultMappingFailure() {
    try {
      startTestProcess("single entry list");

      fail("expect exception");
    } catch (ProcessEngineException e) {
      testRule.assertTextPresent("ENGINE-22001", e.getMessage());
    }
  }

  @Deployment(resources = { COLLECT_ENTRIES_BPMN, TEST_DECISION })
  @Test
  public void testCollectEntriesMappingFailure() {
    try {
      startTestProcess("multiple entries");

      fail("expect exception");
    } catch (ProcessEngineException e) {
      testRule.assertTextPresent("ENGINE-22002", e.getMessage());
    }
  }

  @Test
  public void testInvalidMapping() {
    try {
      testRule.deploy(repositoryService
          .createDeployment()
          .addClasspathResource(INVALID_MAPPING_BPMN));

      fail("expect parse exception");
    } catch (ParseException e) {
      testRule.assertTextPresent("No decision result mapper found for name 'invalid'", e.getMessage());
      assertThat(e.getResorceReports().get(0).getErrors().size()).isEqualTo(1);
      assertThat(e.getResorceReports().get(0).getErrors().get(0).getMainElementId()).isEqualTo("ruleTask");
    }
  }

  @Deployment(resources = { DEFAULT_MAPPING_BPMN, TEST_DECISION })
  @Test
  public void testTransientDecisionResult() {
    // when a decision is evaluated and the result is stored in a transient variable "decisionResult"
    ProcessInstance processInstance = startTestProcess("single entry");

    // then the variable should not be available outside the business rule task
    assertNull(runtimeService.getVariable(processInstance.getId(), "decisionResult"));
    // and should not create an entry in history since it is not persistent
    assertNull(historyService.createHistoricVariableInstanceQuery().variableName("decisionResult").singleResult());
  }

  @Deployment(resources = { OVERRIDE_DECISION_RESULT_BPMN, TEST_DECISION })
  @Test
  public void testFailedToOverrideDecisionResultVariable() {
    try {
      // the transient variable "decisionResult" should not be overridden by the task result variable
      startTestProcess("single entry");
      fail("expect exception");

    } catch (ProcessEngineException e) {
      testRule.assertTextPresent("transient variable with name decisionResult to non-transient", e.getMessage());
    }
  }

  @Deployment(resources = { SINGLE_ENTRY_BPMN, TEST_DECISION })
  @Test
  public void testSingleEntryEmptyResult() {
    ProcessInstance processInstance = startTestProcess("empty result");

    Object result = runtimeService.getVariable(processInstance.getId(), "result");
    assertNull(result);
    TypedValue resultTyped = runtimeService.getVariableTyped(processInstance.getId(), "result");
    assertEquals(Variables.untypedNullValue(), resultTyped);
  }

  @Deployment(resources = { SINGLE_RESULT_BPMN, TEST_DECISION })
  @Test
  public void testSingleResultEmptyResult() {
    ProcessInstance processInstance = startTestProcess("empty result");

    Object result = runtimeService.getVariable(processInstance.getId(), "result");
    assertNull(result);
    TypedValue resultTyped = runtimeService.getVariableTyped(processInstance.getId(), "result");
    assertEquals(Variables.untypedNullValue(), resultTyped);
  }

  @Deployment(resources = { COLLECT_ENTRIES_BPMN, TEST_DECISION })
  @SuppressWarnings("unchecked")
  @Test
  public void testCollectEntriesEmptyResult() {
    ProcessInstance processInstance = startTestProcess("empty result");

    List<Object> result = (List<Object>) runtimeService.getVariable(processInstance.getId(), "result");
    assertTrue(result.isEmpty());
  }

  @Deployment(resources = { RESULT_LIST_BPMN, TEST_DECISION })
  @SuppressWarnings("unchecked")
  @Test
  public void testResultListEmptyResult() {
    ProcessInstance processInstance = startTestProcess("empty result");

    List<Object> result = (List<Object>) runtimeService.getVariable(processInstance.getId(), "result");
    assertTrue(result.isEmpty());
  }

  @Deployment(resources = { DEFAULT_MAPPING_BPMN, TEST_DECISION })
  @SuppressWarnings("unchecked")
  @Test
  public void testDefaultMappingEmptyResult() {
    ProcessInstance processInstance = startTestProcess("empty result");

    List<Object> result = (List<Object>) runtimeService.getVariable(processInstance.getId(), "result");
    assertTrue(result.isEmpty());
  }

  protected ProcessInstance startTestProcess(String input) {
    return runtimeService.startProcessInstanceByKey("testProcess", Collections.<String, Object>singletonMap("input", input));
  }

}
