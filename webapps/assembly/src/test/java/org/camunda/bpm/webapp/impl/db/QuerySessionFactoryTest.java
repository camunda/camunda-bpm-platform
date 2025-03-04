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
package org.camunda.bpm.webapp.impl.db;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class QuerySessionFactoryTest {

  @Rule
  public ProcessEngineRule processEngineRule = new ProcessEngineRule("camunda-skip-check.cfg.xml");

  private ProcessEngineConfigurationImpl processEngineConfiguration;

  @Before
  public void setUp() throws Exception {
    processEngineConfiguration = processEngineRule.getProcessEngineConfiguration();
  }

  @Test
  public void testQuerySessionFactoryInitializationFromEngineConfig() {
    // given
    QuerySessionFactory querySessionFactory = new QuerySessionFactory();
    processEngineConfiguration = processEngineRule.getProcessEngineConfiguration();

    // when
    querySessionFactory.initFromProcessEngineConfiguration(processEngineConfiguration, Collections.emptyList());

    // then
    assertThat(querySessionFactory.getWrappedConfiguration()).isEqualTo(processEngineConfiguration);
    assertThat(querySessionFactory.getDatabaseType()).isEqualTo(processEngineConfiguration.getDatabaseType());
    assertThat(querySessionFactory.getDataSource()).isEqualTo(processEngineConfiguration.getDataSource());
    assertThat(querySessionFactory.getDatabaseTablePrefix()).isEqualTo(processEngineConfiguration.getDatabaseTablePrefix());
    assertThat(querySessionFactory.getSkipIsolationLevelCheck()).isTrue();
    assertThat(querySessionFactory.getHistoryLevel()).isEqualTo(processEngineConfiguration.getHistoryLevel());
    assertThat(querySessionFactory.getHistory()).isEqualTo(processEngineConfiguration.getHistory());

  }
}