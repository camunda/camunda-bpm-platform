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
package org.camunda.bpm.qa.upgrade.scenarios7120.useroperationlog.annotation;

import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.ProcessDefinitionPermissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

public class SetAnnotationAuthorizationTest {

  @Rule
  public ProcessEngineRule engineRule = new ProcessEngineRule("camunda.cfg.xml");

  protected HistoryService historyService;
  protected AuthorizationService authorizationService;

  protected ProcessEngineConfiguration processEngineConfiguration;

  @Before
  public void assignments() {
    historyService = engineRule.getHistoryService();
    authorizationService = engineRule.getAuthorizationService();
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();
  }

  @Before
  public void setAuthentication() {
    engineRule.getIdentityService().setAuthenticatedUserId("demo");
  }

  @After
  public void clearAuthentication() {
    engineRule.getIdentityService().clearAuthentication();
    engineRule.getProcessEngineConfiguration().setAuthorizationEnabled(false);
  }

  @Test
  public void shouldFallbackToProcessDefinitionAuthorizationCheckWhenOperationLogCategoryIsNull() {
    // given
    Authorization auth = authorizationService.createNewAuthorization(Authorization.AUTH_TYPE_GRANT);

    auth.setUserId("demo");
    auth.setPermissions(new ProcessDefinitionPermissions[] {ProcessDefinitionPermissions.UPDATE_HISTORY});
    auth.setResource(Resources.PROCESS_DEFINITION);
    auth.setResourceId("oneTaskProcess_userOpLog_annotation");

    authorizationService.saveAuthorization(auth);

    processEngineConfiguration.setAuthorizationEnabled(false);

    UserOperationLogEntry userOperationLogEntry = historyService.createUserOperationLogQuery()
        .processDefinitionKey("oneTaskProcess_userOpLog_annotation")
        .entityType("Task")
        .singleResult();

    // assume
    assertThat(userOperationLogEntry.getCategory(), nullValue());

    processEngineConfiguration.setAuthorizationEnabled(true);

    // when
    historyService.setAnnotationForOperationLogById(userOperationLogEntry.getOperationId(), "anAnnotation");

    processEngineConfiguration.setAuthorizationEnabled(false);

    userOperationLogEntry = historyService.createUserOperationLogQuery()
        .processDefinitionKey("oneTaskProcess_userOpLog_annotation")
        .entityType("Task")
        .singleResult();

    // then
    assertThat(userOperationLogEntry.getAnnotation(), is("anAnnotation"));

    // cleanup
    authorizationService.deleteAuthorization(auth.getId());
  }

  @Test
  public void shouldSkipAuthorizationCheckWhenOperationLogCategoryIsNullAndUnrelatedToProcessDefinition() {
    // given
    processEngineConfiguration.setAuthorizationEnabled(false);

    UserOperationLogEntry userOperationLogEntry = historyService.createUserOperationLogQuery()
        .taskId("myTaskForUserOperationLogUpdate")
        .entityType("Task")
        .singleResult();

    // assume
    assertThat(userOperationLogEntry.getCategory(), nullValue());

    processEngineConfiguration.setAuthorizationEnabled(true);

    // when
    historyService.setAnnotationForOperationLogById(userOperationLogEntry.getOperationId(), "anAnnotation");

    processEngineConfiguration.setAuthorizationEnabled(false);

    userOperationLogEntry = historyService.createUserOperationLogQuery()
        .taskId("myTaskForUserOperationLogUpdate")
        .entityType("Task")
        .singleResult();

    // then
    assertThat(userOperationLogEntry.getAnnotation(), is("anAnnotation"));
  }

}
