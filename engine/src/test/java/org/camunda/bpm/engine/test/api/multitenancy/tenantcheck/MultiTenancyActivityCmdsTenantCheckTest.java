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
package org.camunda.bpm.engine.test.api.multitenancy.tenantcheck;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 *
 * @author Deivarayan Azhagappan
 *
 */
public class MultiTenancyActivityCmdsTenantCheckTest {

  protected static final String TENANT_ONE = "tenant1";

  protected static final String PROCESS_DEFINITION_KEY = "oneTaskProcess";

  protected static final BpmnModelInstance ONE_TASK_PROCESS = Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY)
    .startEvent()
    .userTask()
    .endEvent()
    .done();

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();

  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  protected String processInstanceId;

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  @Before
  public void init() {

    testRule.deployForTenant(TENANT_ONE, ONE_TASK_PROCESS);

    processInstanceId = engineRule.getRuntimeService()
      .startProcessInstanceByKey(PROCESS_DEFINITION_KEY)
      .getId();
  }

  @Test
  public void getActivityInstanceWithAuthenticatedTenant() {

    engineRule.getIdentityService().setAuthentication("aUserId", null, Arrays.asList(TENANT_ONE));

    // then
    assertNotNull(engineRule.getRuntimeService().getActivityInstance(processInstanceId));
  }

  @Test
  public void getActivityInstanceWithNoAuthenticatedTenant() {

    engineRule.getIdentityService().setAuthentication("aUserId", null);

    // when/then
    assertThatThrownBy(() -> engineRule.getRuntimeService().getActivityInstance(processInstanceId))
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Cannot read the process instance '"
          + processInstanceId +"' because it belongs to no authenticated tenant.");

  }

  @Test
  public void getActivityInstanceWithDisabledTenantCheck() {

    engineRule.getIdentityService().setAuthentication("aUserId", null);
    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);

    // then
    assertNotNull(engineRule.getRuntimeService().getActivityInstance(processInstanceId));
  }

  // get active activity id
  @Test
  public void getActivityIdsWithAuthenticatedTenant() {

    engineRule.getIdentityService().setAuthentication("aUserId", null, Arrays.asList(TENANT_ONE));

    // then
    assertEquals(1, engineRule.getRuntimeService().getActiveActivityIds(processInstanceId).size());

  }

  @Test
  public void getActivityIdsWithNoAuthenticatedTenant() {

    engineRule.getIdentityService().setAuthentication("aUserId", null);

    // when/then
    assertThatThrownBy(() -> engineRule.getRuntimeService().getActiveActivityIds(processInstanceId))
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Cannot read the process instance '"
          + processInstanceId +"' because it belongs to no authenticated tenant.");

  }

  @Test
  public void getActivityIdsWithDisabledTenantCheck() {

    engineRule.getIdentityService().setAuthentication("aUserId", null);
    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);

    // then
    assertEquals(1, engineRule.getRuntimeService().getActiveActivityIds(processInstanceId).size());

  }

}
