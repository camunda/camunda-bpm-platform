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
package org.camunda.bpm.dmn.engine.feel;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import org.camunda.bpm.dmn.engine.DmnDecisionResult;
import org.camunda.bpm.dmn.engine.DmnDecisionResultEntries;
import org.camunda.bpm.dmn.engine.DmnEngineConfiguration;
import org.camunda.bpm.dmn.engine.impl.DefaultDmnEngineConfiguration;
import org.camunda.bpm.dmn.engine.test.DecisionResource;
import org.camunda.bpm.dmn.feel.impl.scala.ScalaFeelEngineFactory;
import org.junit.Test;

public class ScalaFeelBehaviorTest extends FeelBehavior {

  @Override
  public DmnEngineConfiguration getDmnEngineConfiguration() {
    DefaultDmnEngineConfiguration configuration = new DefaultDmnEngineConfiguration();
    configuration.setFeelEngineFactory(new ScalaFeelEngineFactory());
    configuration.init();
    return configuration;
  }

  /**
   * For expression languages, so-called context functions can be used [1].
   *
   * This test ensures that context functions cannot be called in the
   * juel as well as the scala-based implementation.
   *
   * [1] https://docs.camunda.org/manual/7.12/user-guide/process-engine/expression-language/#internal-context-functions
   */
  @Test
  @DecisionResource(resource = "context_function.dmn")
  public void shouldReturnNullOnInternalContextFunctions() {
    // given
    getVariables().putValue("myDate", new Date());

    // when
    DmnDecisionResult result = evaluateDecision();
    DmnDecisionResultEntries firstResult = result.getFirstResult();

    assertThat(firstResult)
      .hasSize(1)
      .containsKey(null)
      .containsValue("foo");
  }

}
