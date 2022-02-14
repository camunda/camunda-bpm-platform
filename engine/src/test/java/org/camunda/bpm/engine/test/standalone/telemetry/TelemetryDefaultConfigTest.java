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
package org.camunda.bpm.engine.test.standalone.telemetry;

import static org.assertj.core.api.Assertions.assertThat;

import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.test.TestHelper;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

public class TelemetryDefaultConfigTest {

  protected static final String TELEMETRY_ENDPOINT = "http://localhost:8082/pings";

  @ClassRule
  public static ProcessEngineBootstrapRule bootstrapRule =
      new ProcessEngineBootstrapRule(configuration ->
      configuration.setTelemetryEndpoint(TELEMETRY_ENDPOINT));

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule);

  ProcessEngineConfigurationImpl configuration;

  @Before
  public void setup() {
    configuration = engineRule.getProcessEngineConfiguration();
    TestHelper.deleteTelemetryProperty(configuration);
  }

  @Test
  public void shouldHaveEmptyInitilizeTelemetryByDefault() {
    // given default configuration

    // then
    assertThat(configuration.isInitializeTelemetry()).isNull();
    assertThat(configuration.getManagementService().isTelemetryEnabled()).isNull();
  }

}
