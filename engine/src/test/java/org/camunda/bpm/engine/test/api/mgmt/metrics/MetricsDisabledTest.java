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
package org.camunda.bpm.engine.test.api.mgmt.metrics;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * Asserts engine functionality is metrics are disabled
 *
 * @author Daniel Meyer
 *
 */
public class MetricsDisabledTest {

  @ClassRule
  public final static ProcessEngineBootstrapRule bootstrapRule =
      new ProcessEngineBootstrapRule("org/camunda/bpm/engine/test/api/mgmt/metrics/metricsDisabledTest.cfg.xml");

  protected final ProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);
  protected final ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  protected ProcessEngineConfigurationImpl processEngineConfiguration;
  protected ManagementService managementService;

  @Before
  public void setUp() {
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();
    managementService = engineRule.getManagementService();
  }

  // (to run, remove "FAILING" from methodname)
  @Ignore("CAM-4053")
  @Test
  public void testQueryMetricsIfMetricsIsDisabled() {

    // given
    // that the metrics are disabled (see xml configuration referenced in constructor)
    assertFalse(processEngineConfiguration.isMetricsEnabled());
    assertFalse(processEngineConfiguration.isDbMetricsReporterActivate());

    // then
    // it is possible to execute a query
    managementService.createMetricsQuery().sum();

  }

  @Test
  public void testReportNowIfMetricsDisabled() {

    // given
    // that the metrics reporter is disabled
    assertFalse(processEngineConfiguration.isDbMetricsReporterActivate());

    try {
      // then
      // I cannot invoke
      managementService.reportDbMetricsNow();
      fail("Exception expected");
    } catch(ProcessEngineException e) {
      testRule.assertTextPresent("Metrics reporting is disabled", e.getMessage());
    }
  }
}
