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
package org.camunda.bpm.engine.test.api.mgmt;

import static org.assertj.core.api.Assertions.assertThat;

import org.camunda.bpm.engine.EntityTypes;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * @author Tobias Metzke
 *
 */
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class PropertyUserOperationLogTest {

  private static final String USER_ID = "testUserId";
  private static final String PROPERTY_NAME = "TEST_PROPERTY";

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();

  protected HistoryService historyService;
  protected IdentityService identityService;
  protected ManagementService managementService;

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule);

  @Before
  public void setUp() {
    historyService = engineRule.getHistoryService();
    identityService = engineRule.getIdentityService();
    managementService = engineRule.getManagementService();
  }

  @After
  public void tearDown() {
    managementService.deleteProperty(PROPERTY_NAME);
  }

  @Test
  public void testCreateProperty() {
    // given
    assertThat(historyService.createUserOperationLogQuery().count()).isEqualTo(0L);

    // when
    identityService.setAuthenticatedUserId(USER_ID);
    managementService.setProperty(PROPERTY_NAME, "testValue");
    identityService.clearAuthentication();

    // then
    assertThat(historyService.createUserOperationLogQuery().count()).isEqualTo(1L);
    UserOperationLogEntry entry = historyService.createUserOperationLogQuery().singleResult();
    assertThat(entry.getEntityType()).isEqualTo(EntityTypes.PROPERTY);
    assertThat(entry.getCategory()).isEqualTo(UserOperationLogEntry.CATEGORY_ADMIN);
    assertThat(entry.getOperationType()).isEqualTo(UserOperationLogEntry.OPERATION_TYPE_CREATE);
    assertThat(entry.getProperty()).isEqualTo("name");
    assertThat(entry.getOrgValue()).isNull();
    assertThat(entry.getNewValue()).isEqualTo(PROPERTY_NAME);
  }

  @Test
  public void testUpdateProperty() {
    // given
    managementService.setProperty(PROPERTY_NAME, "testValue");
    assertThat(historyService.createUserOperationLogQuery().count()).isEqualTo(0L);

    // when
    identityService.setAuthenticatedUserId(USER_ID);
    managementService.setProperty(PROPERTY_NAME, "testValue2");
    identityService.clearAuthentication();

    // then
    assertThat(historyService.createUserOperationLogQuery().count()).isEqualTo(1L);
    UserOperationLogEntry entry = historyService.createUserOperationLogQuery().singleResult();
    assertThat(entry.getEntityType()).isEqualTo(EntityTypes.PROPERTY);
    assertThat(entry.getCategory()).isEqualTo(UserOperationLogEntry.CATEGORY_ADMIN);
    assertThat(entry.getOperationType()).isEqualTo(UserOperationLogEntry.OPERATION_TYPE_UPDATE);
    assertThat(entry.getProperty()).isEqualTo("name");
    assertThat(entry.getOrgValue()).isNull();
    assertThat(entry.getNewValue()).isEqualTo(PROPERTY_NAME);
  }

  @Test
  public void testDeleteProperty() {
    // given
    managementService.setProperty(PROPERTY_NAME, "testValue");
    assertThat(historyService.createUserOperationLogQuery().count()).isEqualTo(0L);

    // when
    identityService.setAuthenticatedUserId(USER_ID);
    managementService.deleteProperty(PROPERTY_NAME);
    identityService.clearAuthentication();

    // then
    assertThat(historyService.createUserOperationLogQuery().count()).isEqualTo(1L);
    UserOperationLogEntry entry = historyService.createUserOperationLogQuery().singleResult();
    assertThat(entry.getEntityType()).isEqualTo(EntityTypes.PROPERTY);
    assertThat(entry.getCategory()).isEqualTo(UserOperationLogEntry.CATEGORY_ADMIN);
    assertThat(entry.getOperationType()).isEqualTo(UserOperationLogEntry.OPERATION_TYPE_DELETE);
    assertThat(entry.getProperty()).isEqualTo("name");
    assertThat(entry.getOrgValue()).isNull();
    assertThat(entry.getNewValue()).isEqualTo(PROPERTY_NAME);
  }

  @Test
  public void testDeletePropertyNonExisting() {
    // given
    assertThat(historyService.createUserOperationLogQuery().count()).isEqualTo(0L);

    // when
    identityService.setAuthenticatedUserId(USER_ID);
    managementService.deleteProperty(PROPERTY_NAME);
    identityService.clearAuthentication();

    // then
    assertThat(historyService.createUserOperationLogQuery().count()).isEqualTo(0L);
  }
}
