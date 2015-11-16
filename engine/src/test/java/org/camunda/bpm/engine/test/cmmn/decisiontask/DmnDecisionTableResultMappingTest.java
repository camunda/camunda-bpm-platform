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
package org.camunda.bpm.engine.test.cmmn.decisiontask;

import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.test.CmmnProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.variable.Variables;

/**
 * @author Roman Smirnov
 *
 */
public class DmnDecisionTableResultMappingTest extends CmmnProcessEngineTestCase {

  protected static final String TEST_DECISION = "org/camunda/bpm/engine/test/dmn/result/DmnDecisionResultTest.dmn11.xml";

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/cmmn/decisiontask/DmnDecisionTableResultMappingTest.testSingleEntryMapping.cmmn",
      TEST_DECISION
    })
  public void testSingleEntryMapping() {
    CaseInstance caseInstance = createTestCase("single entry");

    assertEquals("foo", caseService.getVariable(caseInstance.getId(), "result"));
    assertEquals(Variables.stringValue("foo"), caseService.getVariableTyped(caseInstance.getId(), "result"));
  }

  @SuppressWarnings("unchecked")
  @Deployment(resources = {
      "org/camunda/bpm/engine/test/cmmn/decisiontask/DmnDecisionTableResultMappingTest.testSingleResultMapping.cmmn",
      TEST_DECISION
    })
  public void testSingleResultMapping() {
    CaseInstance caseInstance = createTestCase("multiple entries");

    Map<String, Object> output = (Map<String, Object>) caseService.getVariable(caseInstance.getId(), "result");

    assertEquals(2, output.size());
    assertEquals("foo", output.get("result1"));
    assertEquals("bar", output.get("result2"));
  }

  @SuppressWarnings("unchecked")
  @Deployment(resources = {
      "org/camunda/bpm/engine/test/cmmn/decisiontask/DmnDecisionTableResultMappingTest.testCollectEntriesMapping.cmmn",
      TEST_DECISION
    })
  public void testCollectEntriesMapping() {
    CaseInstance caseInstance = createTestCase("single entry list");

    List<String> output = (List<String>) caseService.getVariable(caseInstance.getId(), "result");

    assertEquals(2, output.size());
    assertEquals("foo", output.get(0));
    assertEquals("foo", output.get(1));
  }

  @SuppressWarnings("unchecked")
  @Deployment(resources = {
      "org/camunda/bpm/engine/test/cmmn/decisiontask/DmnDecisionTableResultMappingTest.testResultListMapping.cmmn",
      TEST_DECISION
    })
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
  @Deployment(resources = {
      "org/camunda/bpm/engine/test/cmmn/decisiontask/DmnDecisionTableResultMappingTest.testDefaultResultMapping.cmmn",
      TEST_DECISION
    })
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

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/cmmn/decisiontask/DmnDecisionTableResultMappingTest.testSingleEntryMapping.cmmn",
      TEST_DECISION
    })
  public void testSingleValueMappingFailureMultipleOutputs() {
    try {
      createTestCase("single entry list");

      fail("expect exception");
    } catch (ProcessEngineException e) {
      assertTextPresent("The decision result mapper failed to process", e.getMessage());
    }
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/cmmn/decisiontask/DmnDecisionTableResultMappingTest.testSingleEntryMapping.cmmn",
      TEST_DECISION
    })
  public void testSingleValueMappingFailureMultipleValues() {
    try {
      createTestCase("multiple entries");

      fail("expect exception");
    } catch (ProcessEngineException e) {
      assertTextPresent("The decision result mapper failed to process", e.getMessage());
    }
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/cmmn/decisiontask/DmnDecisionTableResultMappingTest.testSingleResultMapping.cmmn",
      TEST_DECISION
    })
  public void testSingleOutputMappingFailure() {
    try {
      createTestCase("single entry list");

      fail("expect exception");
    } catch (ProcessEngineException e) {
      assertTextPresent("The decision result mapper failed to process", e.getMessage());
    }
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/cmmn/decisiontask/DmnDecisionTableResultMappingTest.testCollectEntriesMapping.cmmn",
      TEST_DECISION
    })
  public void testCollectValuesMappingFailure() {
    try {
      createTestCase("multiple entries");

      fail("expect exception");
    } catch (ProcessEngineException e) {
      assertTextPresent("The decision result mapper failed to process", e.getMessage());
    }
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/cmmn/decisiontask/DmnDecisionTableResultMappingTest.testTransientDecisionResult.cmmn",
      TEST_DECISION
    })
  public void testTransientDecisionResult() {
    // when a decision is evaluated and the result is stored in a transient variable "decisionResult"
    CaseInstance caseInstance = createTestCase("single entry");

    // then the variable should not be available outside the decision task
    assertNull(caseService.getVariable(caseInstance.getId(), "decisionResult"));
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/cmmn/decisiontask/DmnDecisionTableResultMappingTest.testFailedToOverrideDecisionResultVariable.cmmn",
      TEST_DECISION
    })
  public void testFailedToOverrideDecisionResultVariable() {
    try {
      // the transient variable "decisionResult" should not be overridden by the task result variable
      createTestCase("single entry");
      fail("expect exception");

    } catch (ProcessEngineException e) {
      assertTextPresent("variable with name 'decisionResult' can not be updated", e.getMessage());
    }
  }

  protected CaseInstance createTestCase(String input) {
    return createCaseInstanceByKey("case", Variables.createVariables().putValue("input", input));
  }

}
