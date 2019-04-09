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
package org.camunda.bpm.dmn.engine.util;

import java.io.InputStream;
import static org.camunda.bpm.dmn.engine.test.asserts.DmnEngineTestAssertions.assertThat;
import org.camunda.bpm.dmn.engine.DmnDecision;
import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;
import org.camunda.bpm.dmn.engine.DmnEngine;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.commons.utils.IoUtil;

public final class DmnExampleVerifier {

  public static final String EXAMPLE_DMN = "org/camunda/bpm/dmn/engine/api/Example.dmn";

  public static void assertExample(DmnEngine engine) {
    InputStream inputStream = IoUtil.fileAsStream(EXAMPLE_DMN);
    DmnDecision decision = engine.parseDecision("decision", inputStream);
    assertExample(engine, decision);
  }

  public static void assertExample(DmnEngine engine, DmnDecision decision) {
    VariableMap variables = Variables.createVariables();
    variables.put("status", "bronze");
    variables.put("sum", 200);

    DmnDecisionTableResult results = engine.evaluateDecisionTable(decision, variables);
    assertThat(results)
      .hasSingleResult()
      .containsKeys("result", "reason")
      .containsEntry("result", "notok")
      .containsEntry("reason", "work on your status first, as bronze you're not going to get anything");

    variables.put("status", "silver");

    results = engine.evaluateDecisionTable(decision, variables);
    assertThat(results)
      .hasSingleResult()
      .containsKeys("result", "reason")
      .containsEntry("result", "ok")
      .containsEntry("reason", "you little fish will get what you want");

    variables.put("sum", 1200);

    results = engine.evaluateDecisionTable(decision, variables);
    assertThat(results)
      .hasSingleResult()
      .containsKeys("result", "reason")
      .containsEntry("result", "notok")
      .containsEntry("reason", "you took too much man, you took too much!");

    variables.put("status", "gold");
    variables.put("sum", 200);

    results = engine.evaluateDecisionTable(decision, variables);
    assertThat(results)
      .hasSingleResult()
      .containsKeys("result", "reason")
      .containsEntry("result", "ok")
      .containsEntry("reason", "you get anything you want");
  }

}
