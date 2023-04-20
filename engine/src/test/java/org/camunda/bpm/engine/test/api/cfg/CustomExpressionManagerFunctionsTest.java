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
package org.camunda.bpm.engine.test.api.cfg;

import static org.assertj.core.api.Assertions.assertThat;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.util.ReflectUtil;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

public class CustomExpressionManagerFunctionsTest {

  @ClassRule
  public static ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule();
  protected ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);
  public ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  protected ProcessEngineConfigurationImpl processEngineConfiguration;
  protected RuntimeService runtimeService;

  @Before
  public void initializeServices() {
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();
    runtimeService = engineRule.getRuntimeService();
  }

  @Test
  public void shouldResolveCustomFunction() {
    // given
    processEngineConfiguration.getExpressionManager().addFunction("foobar", ReflectUtil.getMethod(TestFunctions.class, "foobar"));
    testRule.deploy(Bpmn.createExecutableProcess("process")
       .startEvent()
       .serviceTask().camundaExpression("${execution.setVariable(\"baz\", foobar())}")
       .userTask()
       .endEvent()
       .done());
    // when
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();
    // then
    assertThat(runtimeService.getVariable(processInstanceId, "baz")).isEqualTo("foobar");
  }

  @Test
  public void shouldResolveCustomPrefixedFunction() {
    // given
    processEngineConfiguration.getExpressionManager().addFunction("foo:bar", ReflectUtil.getMethod(TestFunctions.class, "foobar"));
    testRule.deploy(Bpmn.createExecutableProcess("process")
        .startEvent()
        .serviceTask().camundaExpression("${execution.setVariable(\"baz\", foo:bar())}")
        .userTask()
        .endEvent()
        .done());
     // when
     String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();
     // then
     assertThat(runtimeService.getVariable(processInstanceId, "baz")).isEqualTo("foobar");
  }

  public static class TestFunctions {
    public static String foobar() {
      return "foobar";
    }
  }
}
