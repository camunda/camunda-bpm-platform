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
package org.camunda.bpm.dmn.engine.hitpolicy;

import org.camunda.bpm.dmn.engine.test.DecisionResource;
import org.camunda.bpm.dmn.engine.test.DmnEngineTest;
import org.camunda.bpm.dmn.engine.test.asserts.DmnDecisionTableResultAssert;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.Test;

/**
 * Tests the type of the result value of an evaluated decision table with
 * collect hit policy.
 *
 * @author Philipp Ossler
 */
public class CollectResultValueTypeTest extends DmnEngineTest {

  public static final String COLLECT_SUM = "HitPolicyTest.collect.sum.single.dmn";
  public static final String COLLECT_MIN = "HitPolicyTest.collect.min.single.dmn";
  public static final String COLLECT_MAX = "HitPolicyTest.collect.max.single.dmn";
  public static final String COLLECT_COUNT = "HitPolicyTest.collect.count.single.dmn";

  @Test
  @DecisionResource(resource = COLLECT_SUM)
  public void collectSumHitPolicy() {
    assertThatDecisionTableResult(10, 20, 50)
      .hasSingleResult()
      .hasSingleEntryTyped(Variables.integerValue(80));

    assertThatDecisionTableResult(10L, 20L, 50L)
      .hasSingleResult()
      .hasSingleEntryTyped(Variables.longValue(80L));

    assertThatDecisionTableResult(10.3, 20.5, 50.7)
      .hasSingleResult()
      .hasSingleEntryTyped(Variables.doubleValue(81.5));
  }

  @Test
  @DecisionResource(resource = COLLECT_MIN)
  public void collectMinHitPolicy() {
    assertThatDecisionTableResult(10, 20, 50)
      .hasSingleResult()
      .hasSingleEntryTyped(Variables.integerValue(10));

    assertThatDecisionTableResult(10L, 20L, 50L)
      .hasSingleResult()
      .hasSingleEntryTyped(Variables.longValue(10L));

    assertThatDecisionTableResult(10.3, 20.5, 50.7)
      .hasSingleResult()
      .hasSingleEntryTyped(Variables.doubleValue(10.3));
  }

  @Test
  @DecisionResource(resource = COLLECT_MAX)
  public void collectMaxHitPolicy() {
    assertThatDecisionTableResult(10, 20, 50)
      .hasSingleResult()
      .hasSingleEntryTyped(Variables.integerValue(50));

    assertThatDecisionTableResult(10L, 20L, 50L)
      .hasSingleResult()
      .hasSingleEntryTyped(Variables.longValue(50L));

    assertThatDecisionTableResult(10.3, 20.5, 50.7)
      .hasSingleResult()
      .hasSingleEntryTyped(Variables.doubleValue(50.7));
  }

  @Test
  @DecisionResource(resource = COLLECT_COUNT)
  public void collectCountHitPolicy() {
    assertThatDecisionTableResult(10, 20, 50)
      .hasSingleResult()
      .hasSingleEntryTyped(Variables.integerValue(3));

    assertThatDecisionTableResult(10L, 20L, 50L)
      .hasSingleResult()
      .hasSingleEntryTyped(Variables.integerValue(3));

    assertThatDecisionTableResult(10.3, 20.5, 50.7)
      .hasSingleResult()
      .hasSingleEntryTyped(Variables.integerValue(3));
  }

  public DmnDecisionTableResultAssert assertThatDecisionTableResult(Object output1, Object output2, Object output3) {
    variables.put("input1", true);
    variables.put("input2", true);
    variables.put("input3", true);
    variables.put("output1", output1);
    variables.put("output2", output2);
    variables.put("output3", output3);

    return assertThatDecisionTableResult();
  }

}
