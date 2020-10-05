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
package org.camunda.bpm.engine.spring.test.transaction.crdb;

import org.camunda.bpm.engine.CrdbTransactionRetryException;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * This tests simulates a CockroachDB concurrency error on TX commit.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:org/camunda/bpm/engine/spring/test/transaction/" +
    "CrdbTransactionIntegrationTest-applicationContext.xml"})
public class CrdbTxRetryOnCommitTest {

  @Rule
  @Autowired
  public ProcessEngineRule rule;

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Autowired
  public ProcessEngineConfigurationImpl processEngineConfiguration;

  /**
   * Scenario can't be successfully run without modifying runtime code
   * since we only use H2 in the Spring integration tests, and this test
   * requires the hard-coded CRDB check to work.
   */
  @Test
  @Ignore
  @Deployment(resources = {"org/camunda/bpm/engine/spring/test/transaction/" +
      "CrdbTransactionIntegrationTest.crdbFailureProcess.bpmn20.xml" })
  public void shouldReportCrdbException() {
    // given a command that fails with a CRDB concurrency error on commit

    // then
    // the StartProcessInstanceCmd is not retryable, and an exception is thrown
    thrown.expect(CrdbTransactionRetryException.class);

    // when
    // the command is executed
    processEngineConfiguration.getRuntimeService()
        .startProcessInstanceByKey("crdbFailureProcess");
  }
}