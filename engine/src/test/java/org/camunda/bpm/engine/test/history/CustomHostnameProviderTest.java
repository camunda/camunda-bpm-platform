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
package org.camunda.bpm.engine.test.history;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.history.event.HostnameProvider;
import org.camunda.bpm.engine.impl.metrics.MetricsQueryImpl;
import org.camunda.bpm.engine.impl.metrics.MetricsRegistry;
import org.camunda.bpm.engine.management.MetricIntervalValue;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.api.mgmt.metrics.MetricsIntervalTest;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

public class CustomHostnameProviderTest {

  @ClassRule
  public static ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule() {
    @Override
    public ProcessEngineConfiguration configureEngine(ProcessEngineConfigurationImpl configuration) {
      configuration.setHostnameProvider(processEngine -> processEngine.getName() + "_TEST");
      return super.configureEngine(configuration);
    }
  };
  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);
  protected TestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  protected ProcessEngineConfigurationImpl processEngineConfiguration;
  protected ManagementService managementService;

  @Before
  public void setUp() {
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();
    managementService = engineRule.getManagementService();

    // add metrics data
    processEngineConfiguration.getMetricsRegistry().markOccurrence("TEST", 1L);
    processEngineConfiguration.getDbMetricsReporter().reportNow();
  }

  @After
  public void tearDown() {
    managementService.deleteMetrics(null);
  }

  @Test
  public void shouldProvideCustomHostName() {
    // given a ProcessEngineConfiguration with a custom HostnameProvider

    // when
    String customHostname = processEngineConfiguration.getHostname();

    // then
    String engineName = engineRule.getProcessEngine().getName();
    assertThat(customHostname).isEqualToIgnoringCase(engineName + "_TEST");
  }

  @Test
  public void shouldUseHostnameAsMetricsReporterId() {
    // given a ProcessEngineConfiguration with a custom HostnameProvider and some metric data

    // when
    List<MetricIntervalValue> metrics = managementService.createMetricsQuery().limit(1).interval();

    // then
    assertThat(metrics).hasSize(1);
    String engineName = engineRule.getProcessEngine().getName();
    assertThat(metrics.get(0).getReporter()).isEqualToIgnoringCase(engineName + "_TEST");
  }
}