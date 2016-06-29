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
import java.util.List;
import java.util.Map;
import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;
import org.camunda.bpm.dmn.engine.DmnEngineException;
import org.camunda.bpm.dmn.engine.impl.DmnEvaluationException;
import org.camunda.bpm.dmn.engine.test.DmnEngineTest;
import org.camunda.commons.utils.IoUtil;
import org.junit.Test;

public class DmnDecisionEvaluationTest extends DmnEngineTest {

  public static final String DMN_MULTI_LEVEL_MULTIPLE_INPUT_SINGLE_OUTPUT = "org/camunda/bpm/dmn/engine/evaluate/EvaluateMultiLevelDecisionsWithMultipleInputAndSingleOutput.dmn";
  public static final String DMN_DECISIONS_WITH_MULTIPLE_MATCHING_RULES = "org/camunda/bpm/dmn/engine/evaluate/EvaluateDecisionsWithMultipleMatchingRules.groovy.dmn";
  public static final String DMN_DECISIONS_WITH_NO_MATCHING_RULE_IN_PARENT = "org/camunda/bpm/dmn/engine/evaluate/EvaluateDecisionsWithNoMatchingRuleInParent.groovy.dmn";
  public static final String DMN_DECISIONS_WITH_MULTIPLE_MATCHING_RULES_MULTIPLE_OUTPUTS = "org/camunda/bpm/dmn/engine/evaluate/EvaluateDecisionsWithMultipleMatchingRulesAndMultipleOutputs.groovy.dmn";
  public static final String DMN_SHARED_DECISIONS = "org/camunda/bpm/dmn/engine/evaluate/EvaluateSharedDecisions.dmn";
  public static final String DMN_DECISIONS_WITH_DIFFERENT_INPUT_OUTPUT_TYPES = "org/camunda/bpm/dmn/engine/evaluate/EvaluateDecisionsWithDifferentInputAndOutputTypes.groovy.dmn";
  public static final String DMN_DECISIONS_WITH_DEFAULT_RULE_IN_CHILD = "org/camunda/bpm/dmn/engine/evaluate/EvaluateDecisionsWithDefaultRuleInChild.groovy.dmn";
  public static final String DMN_DECISIONS_WITH_INVALID_INPUT_TYPE = "org/camunda/bpm/dmn/engine/evaluate/EvaluateDecisionsWithInvalidInputTypeInParent.groovy.dmn";
  public static final String DMN_DECISIONS_WITH_PARENT_DECISION = "org/camunda/bpm/dmn/engine/evaluate/EvaluateDecisionsWithParentDecision.dmn";
  public static final String DMN_DECISIONS_WITH_DISH_DECISON_EXAMPLE = "org/camunda/bpm/dmn/engine/evaluate/EvaluateDrdDishDecisionExample.dmn";

  @Test
  public void evaluateDrdDishDecisionExample() {

    DmnDecisionTableResult results = dmnEngine.evaluateDecisionTable("Dish", IoUtil.fileAsStream(DMN_DECISIONS_WITH_DISH_DECISON_EXAMPLE), createVariables()
      .putValue("temperature", 20)
      .putValue("dayType", "Weekend"));
    
    assertThat(results)
      .hasSingleResult()
      .containsEntry("desiredDish", "Steak");
  }

  @Test
  public void shouldEvaluateDecisionWithRequiredDecisionByKey() {
    
    DmnDecisionTableResult results = dmnEngine.evaluateDecisionTable("A", IoUtil.fileAsStream(DMN_MULTI_LEVEL_MULTIPLE_INPUT_SINGLE_OUTPUT), createVariables()
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
  public void shouldFailDecisionEvaluationWithRequiredDecisionAndNoMatchingRuleInChildDecision() {

    try {
      dmnEngine.evaluateDecisionTable("A", IoUtil.fileAsStream(DMN_MULTI_LEVEL_MULTIPLE_INPUT_SINGLE_OUTPUT), createVariables()
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
  public void shouldFailDecisionEvaluationWithRequiredDecisionAndMissingInput() {
    
    try {
      dmnEngine.evaluateDecisionTable("A", IoUtil.fileAsStream(DMN_MULTI_LEVEL_MULTIPLE_INPUT_SINGLE_OUTPUT), createVariables()
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
  public void shouldEvaluateDecisionsWithRequiredDecisionAndMultipleMatchingRules() {
    
    DmnDecisionTableResult results = dmnEngine.evaluateDecisionTable("A", IoUtil.fileAsStream(DMN_DECISIONS_WITH_MULTIPLE_MATCHING_RULES), createVariables()
        .putValue("dd", "dd")
        .putValue("ee", "ee")
        .asVariableContext());
    
    List<Map<String, Object>> resultList = results.getResultList();
    assertThat(resultList.get(0)).containsEntry("aa", "aa");
    assertThat(resultList.get(1)).containsEntry("aa", "aaa");
  }

  @Test
  public void shouldEvaluateDecisionsWithRequiredDecisionAndMultipleMatchingRulesMultipleOutputs() {
    
    DmnDecisionTableResult results = dmnEngine.evaluateDecisionTable("A", IoUtil.fileAsStream(DMN_DECISIONS_WITH_MULTIPLE_MATCHING_RULES_MULTIPLE_OUTPUTS), createVariables()
        .putValue("dd", "dd")
        .putValue("ee", "ee")
        .asVariableContext());
    
    List<Map<String, Object>> resultList = results.getResultList();
    assertThat(resultList.get(0)).containsEntry("aa", "aa");
    assertThat(resultList.get(1)).containsEntry("aa", "aaa");
    
  }

  @Test
  public void shouldEvaluateDecisionWithRequiredDecisionAndNoMatchingRuleInParentDecision() {
    
    DmnDecisionTableResult results = dmnEngine.evaluateDecisionTable("A", IoUtil.fileAsStream(DMN_DECISIONS_WITH_NO_MATCHING_RULE_IN_PARENT), createVariables()
      .putValue("dd", "dd")
      .putValue("ee", "ee")
      .asVariableContext());
  
    List<Map<String, Object>> resultList = results.getResultList();
    assertThat(resultList.size()).isEqualTo(0);

  }

  @Test
  public void shouldEvaluateDecisionsWithRequiredDecisionAndParentDecision() {
    
   DmnDecisionTableResult results = dmnEngine.evaluateDecisionTable("A", IoUtil.fileAsStream(DMN_DECISIONS_WITH_PARENT_DECISION), createVariables()
     .putValue("ff", true)
     .putValue("dd", 5)
     .asVariableContext());  

   assertThat(results)
     .hasSingleResult()
     .containsEntry("aa", 7.0);
  }

  @Test
  public void shouldEvaluateSharedDecisions() {

    DmnDecisionTableResult results = dmnEngine.evaluateDecisionTable("A", IoUtil.fileAsStream(DMN_SHARED_DECISIONS), createVariables()
      .putValue("ff", "ff")
      .asVariableContext());
    
    assertThat(results)
      .hasSingleResult()
      .containsEntry("aa", "aa");
  }

  @Test
  public void shouldEvaluateDecisionsWithDifferentInputAndOutputTypes() {
    DmnDecisionTableResult results = dmnEngine.evaluateDecisionTable("A", IoUtil.fileAsStream(DMN_DECISIONS_WITH_DIFFERENT_INPUT_OUTPUT_TYPES), createVariables()
      .putValue("dd", "5")
      .putValue("ee", 21)
      .asVariableContext());
    
    assertThat(results.get(0))
      .containsEntry("aa", 7.1);

    results = dmnEngine.evaluateDecisionTable("A", IoUtil.fileAsStream(DMN_DECISIONS_WITH_DIFFERENT_INPUT_OUTPUT_TYPES), createVariables()
      .putValue("dd", "5")
      .putValue("ee", 2147483650L)
      .asVariableContext());
  
    assertThat(results.get(0))
    .containsEntry("aa", 7.0);
  }

  @Test
  public void shouldEvaluateDecisionsWithNoMatchingRuleAndDefaultRuleInParent() {
   
    DmnDecisionTableResult results = dmnEngine.evaluateDecisionTable("A", IoUtil.fileAsStream(DMN_DECISIONS_WITH_DIFFERENT_INPUT_OUTPUT_TYPES), createVariables()
      .putValue("dd", "7")
      .putValue("ee", 2147483650L)
      .asVariableContext());
    
    assertThat(results)
      .hasSingleResult()
      .containsEntry("aa", 7.2);
  }

  @Test
  public void shouldEvaluateDecisionsWithDefaultRuleInChildDecision() {

    DmnDecisionTableResult results = dmnEngine.evaluateDecisionTable("A", IoUtil.fileAsStream(DMN_DECISIONS_WITH_DEFAULT_RULE_IN_CHILD), createVariables()
      .putValue("dd", "7") // There is no rule in the table matching the input 7
      .asVariableContext());
    
    assertThat(results)
      .hasSingleResult()
      .containsEntry("aa", 7.0);
  }

  @Test
  public void shouldEvaluateDecisionsWithUserInputForParentDecision() {
    
    DmnDecisionTableResult results = dmnEngine.evaluateDecisionTable("A", IoUtil.fileAsStream(DMN_DECISIONS_WITH_DIFFERENT_INPUT_OUTPUT_TYPES), createVariables()
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
  public void shouldEvaluateDecisionsWithInputTypeMisMatchInChildDecision() {
    try {
      dmnEngine.evaluateDecisionTable("A", IoUtil.fileAsStream(DMN_DECISIONS_WITH_DIFFERENT_INPUT_OUTPUT_TYPES), createVariables()
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
  public void shouldEvaluateDecisionsWithInputTypeMisMatchInParentDecision() {

    try {
      dmnEngine.evaluateDecisionTable("A", IoUtil.fileAsStream(DMN_DECISIONS_WITH_INVALID_INPUT_TYPE), createVariables()
        .putValue("dd", 5)
        .asVariableContext());  
    } catch(DmnEngineException e) {
      assertThat(e)
      .hasMessageStartingWith("DMN-01005")
      .hasMessageContaining("Invalid value 'bb' for clause with type 'integer'");
    }
  }
}
