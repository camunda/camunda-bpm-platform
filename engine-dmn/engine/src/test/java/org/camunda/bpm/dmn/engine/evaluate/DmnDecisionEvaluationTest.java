/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package org.camunda.bpm.dmn.engine.evaluate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.dmn.engine.test.asserts.DmnEngineTestAssertions.assertThat;
import static org.camunda.bpm.engine.variable.Variables.createVariables;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.dmn.engine.DmnDecision;
import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;
import org.camunda.bpm.dmn.engine.DmnEngineException;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionImpl;
import org.camunda.bpm.dmn.engine.impl.DmnEvaluationException;
import org.camunda.bpm.dmn.engine.test.DmnEngineTest;
import org.junit.Test;

public class DmnDecisionEvaluationTest extends DmnEngineTest {

  public static final String DMN_MULTI_LEVEL_MULTIPLE_INPUT_SINGLE_OUTPUT = "org/camunda/bpm/dmn/engine/evaluate/EvaluateMultiLevelDecisionsWithMultipleInputAndSingleOutput.dmn";
  public static final String DMN_DECISIONS_WITH_MULTIPLE_MATCHING_RULES = "org/camunda/bpm/dmn/engine/evaluate/EvaluateDecisionsWithMultipleMatchingRules.groovy.dmn";
  public static final String DMN_DECISIONS_WITH_NO_MATCHING_RULE_IN_PARENT = "org/camunda/bpm/dmn/engine/evaluate/EvaluateDecisionsWithNoMatchingRuleInParent.groovy.dmn";
  public static final String DMN_DECISIONS_WITH_MULTIPLE_MATCHING_RULES_MULTIPLE_OUTPUTS = "org/camunda/bpm/dmn/engine/evaluate/EvaluateDecisionsWithMultipleMatchingRulesAndMultipleOutputs.groovy.dmn";
  public static final String DMN_DECISIONS_WITH_MULTIPLE_INPUTS_MULTIPLE_OUTPUTS = "org/camunda/bpm/dmn/engine/evaluate/EvaluateDecisionsWithMultipleInputsAndMultipleOutputs.groovy.dmn";
  public static final String DMN_HYBRID_DECISIONS = "org/camunda/bpm/dmn/engine/evaluate/EvaluateHybridDecisions.dmn";
  public static final String DMN_DECISIONS_WITH_DIFFERENT_INPUT_OUTPUT_TYPES = "org/camunda/bpm/dmn/engine/evaluate/EvaluateDecisionsWithDifferentInputAndOutputTypes.groovy.dmn";
  public static final String DMN_DECISIONS_WITH_DEFAULT_RULE_IN_CHILD = "org/camunda/bpm/dmn/engine/evaluate/EvaluateDecisionsWithDefaultRuleInChild.groovy.dmn";
  public static final String DMN_DECISIONS_WITH_INVALID_INPUT_TYPE = "org/camunda/bpm/dmn/engine/evaluate/EvaluateDecisionsWithInvalidInputTypeInParent.groovy.dmn";
  public static final String DMN_DECISIONS_WITH_SELF_DECISION = "org/camunda/bpm/dmn/engine/evaluate/EvaluateDecisionsWithSelfDecision.groovy.dmn";
  public static final String DMN_DECISIONS_WITH_DISH_DECISON_EXAMPLE = "org/camunda/bpm/dmn/engine/evaluate/EvaluateDrdDishDecisionExample.dmn";

  @Test
  public void evaluateDrdDishDecisionExample() {

    List<DmnDecision> decisions = parseDecisionsFromFile(DMN_DECISIONS_WITH_DISH_DECISON_EXAMPLE);

    DmnDecisionTableResult results = dmnEngine.evaluateDecisionTable(decisions.get(0), createVariables()
      .putValue("temperature", 20)
      .putValue("dayType", "Weekend"));
    
    assertThat(results)
      .hasSingleResult()
      .containsEntry("desiredDish", "Steak");
  }

  @Test
  public void testEvaluateDecisionWithRequiredDecisionByKey() {
    List<DmnDecision> decisions = parseDecisionsFromFile(DMN_MULTI_LEVEL_MULTIPLE_INPUT_SINGLE_OUTPUT);
    
    DmnDecisionTableResult results = dmnEngine.evaluateDecisionTable(decisions.get(0), createVariables()
      .putValue("xx", "xx")  
      .putValue("yy", "yy")
      .putValue("zz", "zz")
      .putValue("ll", "ll")
      .asVariableContext());
    
    assertThat(results)
    .hasSingleResult()
    .containsEntry("aa", "aa");
    
  }

  @Test
  public void testEvaluateDecisionWithRequiredDecisionAndNoMatchingRuleInChildDecision() {
    List<DmnDecision> decisions = parseDecisionsFromFile(DMN_MULTI_LEVEL_MULTIPLE_INPUT_SINGLE_OUTPUT);

    try {
      dmnEngine.evaluateDecisionTable(decisions.get(0), createVariables()
        .putValue("xx", "pp")
        .putValue("yy", "yy")
        .putValue("zz", "zz")
        .putValue("ll", "ll")
        .asVariableContext());
    } catch(DmnEvaluationException e) {
      assertThat(e)
      .hasMessageStartingWith("DMN-01002")
      .hasMessageContaining("Unable to evaluate expression for language 'juel': '${dd}'");
    }
  }

  @Test
  public void testEvaluateDecisionWithRequiredDecisionAndMissingInput() {
    List<DmnDecision> decisions = parseDecisionsFromFile(DMN_MULTI_LEVEL_MULTIPLE_INPUT_SINGLE_OUTPUT);

    try {
      dmnEngine.evaluateDecisionTable(decisions.get(0), createVariables()
        .putValue("xx", "xx")
        .putValue("yy", "yy")
        .putValue("zz", "zz")
        .asVariableContext());
    } catch(DmnEvaluationException e) {
      assertThat(e)
      .hasMessageStartingWith("DMN-01002")
      .hasMessageContaining("Unable to evaluate expression for language 'juel': '${ll}'");
    }
  }

  @Test
  public void testDecisionsWithRequiredDecisionAndMultipleMatchingRules() {
    List<DmnDecision> decisions = parseDecisionsFromFile(DMN_DECISIONS_WITH_MULTIPLE_MATCHING_RULES);

    DmnDecisionTableResult results = dmnEngine.evaluateDecisionTable(decisions.get(0), createVariables()
        .putValue("dd", "abc")
        .putValue("ff", "ff")
        .asVariableContext());
    
    List<Map<String, Object>> resultList = results.getResultList();
    assertThat(resultList.get(0)).containsEntry("ee", "ee");
    assertThat(resultList.get(1)).containsEntry("ee", "ff");
  }

  @Test
  public void testDecisionsWithRequiredDecisionAndMultipleMatchingRulesMultipleOutputs() {
    List<DmnDecision> decisions = parseDecisionsFromFile(DMN_DECISIONS_WITH_MULTIPLE_MATCHING_RULES_MULTIPLE_OUTPUTS);

    DmnDecisionTableResult results = dmnEngine.evaluateDecisionTable(decisions.get(0), createVariables()
        .putValue("dd", "abc")
        .putValue("ff", "ff")
        .asVariableContext());
    
    List<Map<String, Object>> resultList = results.getResultList();
    assertThat(resultList.get(0)).containsEntry("ee", "ee");

    results = dmnEngine.evaluateDecisionTable(decisions.get(0), createVariables()
    .putValue("dd", Arrays.asList("abc","cat"))
    .putValue("ff", "ff")
    .asVariableContext());

    resultList = results.getResultList();
    assertThat(resultList.get(0)).containsEntry("ee", "ee");
    assertThat(resultList.get(1)).containsEntry("ee", "ff");
  }

  @Test
  public void testEvaluateDecisionWithRequiredDecisionAndNoMatchingRuleInParentDecision() {
    List<DmnDecision> decisions = parseDecisionsFromFile(DMN_DECISIONS_WITH_NO_MATCHING_RULE_IN_PARENT);

    
    DmnDecisionTableResult results = dmnEngine.evaluateDecisionTable(decisions.get(0), createVariables()
      .putValue("dd", "abc")
     .putValue("ff", "ff")
      .asVariableContext());
  
    List<Map<String, Object>> resultList = results.getResultList();
    assertThat(resultList.size()).isEqualTo(0);

  }

  @Test
  public void testEvaluateDecisionsWithRequiredDecisionAndSelfDecision() {
    List<DmnDecision> decisions = parseDecisionsFromFile(DMN_DECISIONS_WITH_SELF_DECISION);

   DmnDecisionTableResult results = dmnEngine.evaluateDecisionTable(decisions.get(0), createVariables()
     .putValue("ff", true)
     .putValue("dd", 5)
     .asVariableContext());  

   assertThat(results)
     .hasSingleResult()
     .containsEntry("aa", 7.0);
  }

  @Test
  public void testDecisionsWithRequiredDecisionAndMultipleInputMultipleOutput() {
    List<DmnDecision> decisions = parseDecisionsFromFile(DMN_DECISIONS_WITH_MULTIPLE_INPUTS_MULTIPLE_OUTPUTS);

    DmnDecisionTableResult results = dmnEngine.evaluateDecisionTable(decisions.get(0), createVariables()
      .putValue("dd", Arrays.asList("abc","cat"))
      .putValue("xx", Arrays.asList("ccc","ddd"))
      .putValue("ff", "ff")
      .asVariableContext());
    
    List<Map<String, Object>> resultList = results.getResultList();
    assertThat(resultList.get(0)).containsEntry("ee", "ee");
    assertThat(resultList.get(0)).containsEntry("gg", "zzz");
    assertThat(resultList.get(1)).containsEntry("ee", "ff");
    assertThat(resultList.get(1)).containsEntry("gg", "ppp");
    
  }

  @Test
  public void testEvaluateHybridDecisions() {
    List<DmnDecision> decisions = parseDecisionsFromFile(DMN_HYBRID_DECISIONS);

    DmnDecisionTableResult results = dmnEngine.evaluateDecisionTable(decisions.get(0), createVariables()
      .putValue("ee", "ee")
      .asVariableContext());
    
    assertThat(results)
      .hasSingleResult()
      .containsEntry("aa", "aa");
  }

  @Test
  public void testEvaluateDecisionsWithDifferentInputAndOutputTypes() {
    List<DmnDecision> decisions = parseDecisionsFromFile(DMN_DECISIONS_WITH_DIFFERENT_INPUT_OUTPUT_TYPES);

    DmnDecisionTableResult results = dmnEngine.evaluateDecisionTable(decisions.get(0), createVariables()
      .putValue("dd", "5")
      .putValue("ee", 21)
      .asVariableContext());
    
    assertThat(results.get(0))
      .containsEntry("aa", 7.1);

    results = dmnEngine.evaluateDecisionTable(decisions.get(0), createVariables()
      .putValue("dd", "5")
      .putValue("ee", 2147483650L)
      .asVariableContext());
  
    assertThat(results.get(0))
    .containsEntry("aa", 7.0);
  }

  @Test
  public void testEvaluateDecisionsWithNoMatchingRuleAndDefaultRuleInParent() {
    List<DmnDecision> decisions = parseDecisionsFromFile(DMN_DECISIONS_WITH_DIFFERENT_INPUT_OUTPUT_TYPES);

    DmnDecisionTableResult results = dmnEngine.evaluateDecisionTable(decisions.get(0), createVariables()
      .putValue("dd", "7")
      .putValue("ee", 2147483650L)
      .asVariableContext());
    
    assertThat(results)
      .hasSingleResult()
      .containsEntry("aa", 7.2);
  }

  @Test
  public void testEvaluateDecisionsWithDefaultRuleInChildDecision() {
    List<DmnDecision> decisions = parseDecisionsFromFile(DMN_DECISIONS_WITH_DEFAULT_RULE_IN_CHILD);

    DmnDecisionTableResult results = dmnEngine.evaluateDecisionTable(decisions.get(0), createVariables()
      .putValue("dd", "7") // There is no rule in the table matching the input 7
      .asVariableContext());
    
    assertThat(results)
      .hasSingleResult()
      .containsEntry("aa", 7.0);
  }

  @Test
  public void testEvaluateDecisionsWithUserInputForParentDecision() {
    List<DmnDecision> decisions = parseDecisionsFromFile(DMN_DECISIONS_WITH_DIFFERENT_INPUT_OUTPUT_TYPES);

    DmnDecisionTableResult results = dmnEngine.evaluateDecisionTable(decisions.get(0), createVariables()
      .putValue("bb", "bb")
      .putValue("dd", "7")
      .putValue("ee", 2147483650L)
      .asVariableContext());
    
    // input value provided by the user is overriden by the child decision
    assertThat(results)
      .hasSingleResult()
      .containsEntry("aa", 7.2);
  }

  @Test
  public void testEvaluateDecisionsWithInputTypeMisMatchInChildDecision() {
    List<DmnDecision> decisions = parseDecisionsFromFile(DMN_DECISIONS_WITH_DIFFERENT_INPUT_OUTPUT_TYPES);

    try {
      dmnEngine.evaluateDecisionTable(decisions.get(0), createVariables()
      .putValue("dd", "7")
      .putValue("ee", "abc")
      .asVariableContext());  
    } catch(DmnEngineException e) {
      assertThat(e)
      .hasMessageStartingWith("DMN-01005")
      .hasMessageContaining("Invalid value 'abc' for clause with type 'long'");
    }
  }

  @Test
  public void testEvaluateDecisionsWithInputTypeMisMatchInParentDecision() {
    List<DmnDecision> decisions = parseDecisionsFromFile(DMN_DECISIONS_WITH_INVALID_INPUT_TYPE);

    try {
      dmnEngine.evaluateDecisionTable(decisions.get(0), createVariables()
        .putValue("dd", 5)
        .asVariableContext());  
    } catch(DmnEngineException e) {
      assertThat(e)
      .hasMessageStartingWith("DMN-01005")
      .hasMessageContaining("Invalid value 'bb' for clause with type 'integer'");
    }
  }

  @Test
  public void testEvaluateEmptyDecision() {
    try {
      dmnEngine.evaluateDecisionTable(new DmnDecisionImpl(), createVariables()
      .putValue("dd", "7")
      .asVariableContext());  
    } catch(DmnEngineException e) {
      assertThat(e)
      .hasMessageStartingWith("DMN-01009")
      .hasMessageContaining("Unable to find any decision table");
    }
  }

  @Test
  public void testEvaluateDecisionWithNoDecisionTable() {
    try {
      DmnDecisionImpl decision = new DmnDecisionImpl();
      decision.setKey("A");
      dmnEngine.evaluateDecisionTable(decision, createVariables()
      .putValue("dd", "7")
      .asVariableContext());  
    } catch(DmnEngineException e) {
      assertThat(e)
        .hasMessageStartingWith("DMN-01009")
        .hasMessageContaining("Unable to find any decision table");
    }
  }
}
