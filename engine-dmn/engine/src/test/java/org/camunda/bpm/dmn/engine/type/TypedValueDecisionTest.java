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
package org.camunda.bpm.dmn.engine.type;

import org.camunda.bpm.dmn.engine.test.DecisionResource;
import org.camunda.bpm.dmn.engine.test.DmnEngineTest;
import org.junit.Test;

/**
 * @author Philipp Ossler
 */
public class TypedValueDecisionTest extends DmnEngineTest {

  public static final String DMN_FILE = "org/camunda/bpm/dmn/engine/type/TypedValue.dmn";

  @Test
  @DecisionResource(resource = DMN_FILE)
  public void decisionWithUntypedValueSatisfied() {
    variables.put("type", "untyped");
    variables.put("integer", 84);

    assertThatDecisionTableResult()
      .hasSingleResult()
      .hasSingleEntry(true);
  }

  @Test
  @DecisionResource(resource = DMN_FILE)
  public void decisionWithUntypedValueNotSatisfied() {
    variables.put("type", "untyped");
    variables.put("integer", 21);

    assertThatDecisionTableResult()
      .hasSingleResult()
      .hasSingleEntry(false);
  }

  @Test
  @DecisionResource(resource = DMN_FILE)
  public void decisionWithTypedValueSatisfied() {
    variables.put("type", "typed");
    variables.put("integer", 73);

    assertThatDecisionTableResult()
      .hasSingleResult()
      .hasSingleEntry(true);
  }

  @Test
  @DecisionResource(resource = DMN_FILE)
  public void decisionWithTypedValueNotSatisfied() {
    variables.put("type", "typed");
    variables.put("integer", 41);

    assertThatDecisionTableResult()
      .hasSingleResult()
      .hasSingleEntry(false);
  }

}
