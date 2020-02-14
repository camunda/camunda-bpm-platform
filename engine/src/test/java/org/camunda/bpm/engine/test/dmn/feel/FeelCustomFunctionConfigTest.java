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
package org.camunda.bpm.engine.test.dmn.feel;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.dmn.feel.impl.scala.function.FeelCustomFunctionProvider;
import org.camunda.bpm.engine.DecisionService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.dmn.feel.helper.CustomFunctionProvider;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

public class FeelCustomFunctionConfigTest {

  @ClassRule
  public static ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule(configuration -> {
    List<FeelCustomFunctionProvider> customFunctionProviders = new ArrayList<>();
    customFunctionProviders.add(new CustomFunctionProvider("myFunctionOne", "foo"));
    customFunctionProviders.add(new CustomFunctionProvider("myFunctionTwo", "bar"));

    configuration.setDmnFeelCustomFunctionProviders(customFunctionProviders);
  });

  @Rule
  public ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);

  protected ProcessEngine processEngine;
  protected RepositoryService repositoryService;
  protected String deploymentId;

  @Before
  public void setup() {
    processEngine = engineRule.getProcessEngine();
    repositoryService = processEngine.getRepositoryService();
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/dmn/feel/custom_function.dmn"})
  public void shouldRegisterCustomFunctions() {
    // given
    DecisionService decisionService = processEngine.getDecisionService();

    // when
    String result = decisionService.evaluateDecisionByKey("c")
        .evaluate()
        .getSingleEntry();

    // then
    assertThat(result).isEqualTo("foobar");
  }

}
