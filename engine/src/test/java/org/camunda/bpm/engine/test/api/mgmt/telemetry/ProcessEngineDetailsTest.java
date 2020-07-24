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
package org.camunda.bpm.engine.test.api.mgmt.telemetry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.engine.impl.util.ParseUtil.parseProcessEngineVersion;
import static org.camunda.bpm.engine.impl.util.ProcessEngineDetails.EDITION_COMMUNITY;
import static org.camunda.bpm.engine.impl.util.ProcessEngineDetails.EDITION_ENTERPRISE;

import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.util.ProcessEngineDetails;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

public class ProcessEngineDetailsTest {

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  ProcessEngineConfigurationImpl configuration;

  @Before
  public void init() {
    configuration = engineRule.getProcessEngineConfiguration();
  }

  @After
  public void tearDown() {
  }

  // process engine version and edition ////////////////////////////////////////////////////////////

  @Test
  public void shouldAssertProcessEngineVersionSnapshot() {
    // when
    ProcessEngineDetails engineInfo = parseProcessEngineVersion("7.14.0-SNAPSHOT");

    // then
    assertThat(engineInfo.getVersion()).isEqualTo("7.14.0-SNAPSHOT");
    assertThat(engineInfo.getEdition()).isEqualTo(EDITION_COMMUNITY);
  }

  @Test
  public void shouldAssertProcessEngineVersionAlpha() {
    // when
    ProcessEngineDetails engineInfo = parseProcessEngineVersion("7.14.0-alpha1");

    // then
    assertThat(engineInfo.getVersion()).isEqualTo("7.14.0-alpha1");
    assertThat(engineInfo.getEdition()).isEqualTo(EDITION_COMMUNITY);
  }

  @Test
  public void shouldAssertProcessEngineVersionSnapshotAlphaEE() {
    // when
    ProcessEngineDetails engineInfo = parseProcessEngineVersion("7.14.0-alpha1-ee");

    // then
    assertThat(engineInfo.getVersion()).isEqualTo("7.14.0-alpha1");
    assertThat(engineInfo.getEdition()).isEqualTo(EDITION_ENTERPRISE);
  }

  @Test
  public void shouldAssertProcessEngineVersionSnapshotMinor() {
    // when
    ProcessEngineDetails engineInfo = parseProcessEngineVersion("7.14.0");

    // then
    assertThat(engineInfo.getVersion()).isEqualTo("7.14.0");
    assertThat(engineInfo.getEdition()).isEqualTo(EDITION_COMMUNITY);
  }

  @Test
  public void shouldAssertProcessEngineVersionSnapshotMinorEE() {
    // when
    ProcessEngineDetails engineInfo = parseProcessEngineVersion("7.14.0-ee");

    // then
    assertThat(engineInfo.getVersion()).isEqualTo("7.14.0");
    assertThat(engineInfo.getEdition()).isEqualTo(EDITION_ENTERPRISE);
  }

  @Test
  public void shouldAssertProcessEngineVersionSnapshotPatch() {
    // when
    ProcessEngineDetails engineInfo = parseProcessEngineVersion("7.14.1-ee");

    // then
    assertThat(engineInfo.getVersion()).isEqualTo("7.14.1");
    assertThat(engineInfo.getEdition()).isEqualTo(EDITION_ENTERPRISE);
  }
}
