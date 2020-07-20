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
package org.camunda.bpm.engine.test.cmmn.decisiontask;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.cmmn.CmmnTest;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.Test;

/**
 * @author Roman Smirnov
 *
 */
public class CmmnDecisionTaskResultMappingTest extends CmmnTest {

  protected static final String TEST_DECISION = "org/camunda/bpm/engine/test/dmn/result/DmnDecisionResultTest.dmn11.xml";
  protected static final String SINGLE_ENTRY_MAPPING_CMMN = "org/camunda/bpm/engine/test/cmmn/decisiontask/DmnDecisionTableResultMappingTest.testSingleEntryMapping.cmmn";
  protected static final String SINGLE_RESULT_MAPPING_CMMN = "org/camunda/bpm/engine/test/cmmn/decisiontask/DmnDecisionTableResultMappingTest.testSingleResultMapping.cmmn";
  protected static final String COLLECT_ENTRIES_MAPPING_CMMN = "org/camunda/bpm/engine/test/cmmn/decisiontask/DmnDecisionTableResultMappingTest.testCollectEntriesMapping.cmmn";
  protected static final String RESULT_LIST_MAPPING_CMMN = "org/camunda/bpm/engine/test/cmmn/decisiontask/DmnDecisionTableResultMappingTest.testResultListMapping.cmmn";
  protected static final String DEFAULT_MAPPING_CMMN = "org/camunda/bpm/engine/test/cmmn/decisiontask/DmnDecisionTableResultMappingTest.testDefaultResultMapping.cmmn";
  protected static final String OVERRIDE_DECISION_RESULT_CMMN = "org/camunda/bpm/engine/test/cmmn/decisiontask/DmnDecisionTableResultMappingTest.testFailedToOverrideDecisionResultVariable.cmmn";

  @Deployment(resources = { SINGLE_ENTRY_MAPPING_CMMN, TEST_DECISION })
  @Test
  public void testSingleEntryMapping() {
    CaseInstance caseInstance = createTestCase("single entry");

    assertEquals("foo", caseService.getVariable(caseInstance.getId(), "result"));
    assertEquals(Variables.stringValue("foo"), caseService.getVariableTyped(caseInstance.getId(), "result"));
  }

  @SuppressWarnings("unchecked")
  @Deployment(resources = { SINGLE_RESULT_MAPPING_CMMN, TEST_DECISION })
  @Test
  public void testSingleResultMapping() {
    CaseInstance caseInstance = createTestCase("multiple entries");

    Map<String, Object> output = (Map<String, Object>) caseService.getVariable(caseInstance.getId(), "result");

    assertEquals(2, output.size());
    assertEquals("foo", output.get("result1"));
    assertEquals("bar", output.get("result2"));
  }

  @SuppressWarnings("unchecked")
  @Deployment(resources = { COLLECT_ENTRIES_MAPPING_CMMN, TEST_DECISION })
  @Test
  public void testCollectEntriesMapping() {
    CaseInstance caseInstance = createTestCase("single entry list");

    List<String> output = (List<String>) caseService.getVariable(caseInstance.getId(), "result");

    assertEquals(2, output.size());
    assertEquals("foo", output.get(0));
    assertEquals("foo", output.get(1));
  }

  @SuppressWarnings("unchecked")
  @Deployment(resources = { RESULT_LIST_MAPPING_CMMN, TEST_DECISION })
  @Test
  public void testResultListMapping() {
    CaseInstance caseInstance = createTestCase("multiple entries list");

    List<Map<String, Object>> resultList = (List<Map<String, Object>>) caseService.getVariable(caseInstance.getId(), "result");
    assertEquals(2, resultList.size());

    for (Map<String, Object> valueMap : resultList) {
      assertEquals(2, valueMap.size());
      assertEquals("foo", valueMap.get("result1"));
      assertEquals("bar", valueMap.get("result2"));
    }
  }

  @SuppressWarnings("unchecked")
  @Deployment(resources = { DEFAULT_MAPPING_CMMN, TEST_DECISION })
  @Test
  public void testDefaultResultMapping() {
    CaseInstance caseInstance = createTestCase("multiple entries list");

    // default mapping is 'resultList'
    List<Map<String, Object>> resultList = (List<Map<String, Object>>) caseService.getVariable(caseInstance.getId(), "result");
    assertEquals(2, resultList.size());

    for (Map<String, Object> valueMap : resultList) {
      assertEquals(2, valueMap.size());
      assertEquals("foo", valueMap.get("result1"));
      assertEquals("bar", valueMap.get("result2"));
    }
  }

  @Deployment(resources = { SINGLE_ENTRY_MAPPING_CMMN, TEST_DECISION })
  @Test
  public void testSingleEntryMappingFailureMultipleOutputs() {
    try {
      createTestCase("single entry list");

      fail("expect exception");
    } catch (ProcessEngineException e) {
      testRule.assertTextPresent("ENGINE-22001", e.getMessage());
    }
  }

  @Deployment(resources = { SINGLE_ENTRY_MAPPING_CMMN, TEST_DECISION })
  @Test
  public void testSingleEntryMappingFailureMultipleValues() {
    try {
      createTestCase("multiple entries");

      fail("expect exception");
    } catch (ProcessEngineException e) {
      testRule.assertTextPresent("ENGINE-22001", e.getMessage());
    }
  }

  @Deployment(resources = { SINGLE_RESULT_MAPPING_CMMN, TEST_DECISION })
  @Test
  public void testSingleResultMappingFailure() {
    try {
      createTestCase("single entry list");

      fail("expect exception");
    } catch (ProcessEngineException e) {
      testRule.assertTextPresent("ENGINE-22001", e.getMessage());
    }
  }

  @Deployment(resources = { COLLECT_ENTRIES_MAPPING_CMMN, TEST_DECISION })
  @Test
  public void testCollectEntriesMappingFailure() {
    try {
      createTestCase("multiple entries");

      fail("expect exception");
    } catch (ProcessEngineException e) {
      testRule.assertTextPresent("ENGINE-22002", e.getMessage());
    }
  }

  @Deployment(resources = { DEFAULT_MAPPING_CMMN, TEST_DECISION })
  @Test
  public void testTransientDecisionResult() {
    // when a decision is evaluated and the result is stored in a transient variable "decisionResult"
    CaseInstance caseInstance = createTestCase("single entry");

    // then the variable should not be available outside the decision task
    assertNull(caseService.getVariable(caseInstance.getId(), "decisionResult"));
  }

  @Deployment(resources = { OVERRIDE_DECISION_RESULT_CMMN, TEST_DECISION })
  @Test
  public void testFailedToOverrideDecisionResultVariable() {
    try {
      // the transient variable "decisionResult" should not be overridden by the task result variable
      createTestCase("single entry");
      fail("expect exception");

    } catch (ProcessEngineException e) {
      testRule.assertTextPresent("transient variable with name decisionResult to non-transient", e.getMessage());
    }
  }

  protected CaseInstance createTestCase(String input) {
    return createCaseInstanceByKey("case", Variables.createVariables().putValue("input", input));
  }

}
