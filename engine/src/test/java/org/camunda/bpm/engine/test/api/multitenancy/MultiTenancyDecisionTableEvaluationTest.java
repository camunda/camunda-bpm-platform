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
package org.camunda.bpm.engine.test.api.multitenancy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.util.Arrays;

import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;
import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.repository.DecisionDefinition;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.Test;

public class MultiTenancyDecisionTableEvaluationTest extends PluggableProcessEngineTest {

  protected static final String DMN_FILE = "org/camunda/bpm/engine/test/api/dmn/Example.dmn";
  protected static final String DMN_FILE_SECOND_VERSION = "org/camunda/bpm/engine/test/api/dmn/Example_v2.dmn";

  protected static final String DECISION_DEFINITION_KEY = "decision";

  protected static final String TENANT_ONE = "tenant1";
  protected static final String TENANT_TWO = "tenant2";

  protected static final String RESULT_OF_FIRST_VERSION = "ok";
  protected static final String RESULT_OF_SECOND_VERSION = "notok";

  @Test
  public void testFailToEvaluateDecisionByIdWithoutTenantId() {
   testRule.deploy(DMN_FILE);

    DecisionDefinition decisionDefinition = repositoryService.createDecisionDefinitionQuery().singleResult();

    try {
      decisionService.evaluateDecisionTableById(decisionDefinition.getId())
          .variables(createVariables())
          .decisionDefinitionWithoutTenantId()
          .evaluate();
      fail("BadUserRequestException exception");
    } catch(BadUserRequestException e) {
      assertThat(e.getMessage()).contains("Cannot specify a tenant-id");
    }
  }

  @Test
  public void testFailToEvaluateDecisionByIdWithTenantId() {
    testRule.deployForTenant(TENANT_ONE, DMN_FILE);

    DecisionDefinition decisionDefinition = repositoryService.createDecisionDefinitionQuery().singleResult();

    try {
      decisionService.evaluateDecisionTableById(decisionDefinition.getId())
          .variables(createVariables())
          .decisionDefinitionTenantId(TENANT_ONE)
          .evaluate();
      fail("BadUserRequestException exception");
    } catch(BadUserRequestException e) {
      assertThat(e.getMessage()).contains("Cannot specify a tenant-id");
    }
  }

  @Test
  public void testFailToEvaluateDecisionByKeyForNonExistingTenantID() {
    testRule.deployForTenant(TENANT_ONE, DMN_FILE);
    testRule.deployForTenant(TENANT_TWO, DMN_FILE);

    try {
      decisionService.evaluateDecisionTableByKey(DECISION_DEFINITION_KEY)
          .variables(createVariables())
          .decisionDefinitionTenantId("nonExistingTenantId")
          .evaluate();
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException e) {
      assertThat(e.getMessage()).contains("no decision definition deployed with key 'decision' and tenant-id 'nonExistingTenantId'");
    }
  }

  @Test
  public void testFailToEvaluateDecisionByKeyForMultipleTenants() {
    testRule.deployForTenant(TENANT_ONE, DMN_FILE);
    testRule.deployForTenant(TENANT_TWO, DMN_FILE);

    try {
      decisionService.evaluateDecisionTableByKey(DECISION_DEFINITION_KEY)
          .variables(createVariables())
          .evaluate();
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException e) {
      assertThat(e.getMessage()).contains("multiple tenants.");
    }
  }

  @Test
  public void testEvaluateDecisionByKeyWithoutTenantId() {
   testRule.deploy(DMN_FILE);

    DmnDecisionTableResult decisionResult = decisionService.evaluateDecisionTableByKey(DECISION_DEFINITION_KEY)
        .variables(createVariables())
        .decisionDefinitionWithoutTenantId()
        .evaluate();

    assertThatDecisionHasResult(decisionResult, RESULT_OF_FIRST_VERSION);
  }

  @Test
  public void testEvaluateDecisionByKeyForAnyTenants() {
    testRule.deployForTenant(TENANT_ONE, DMN_FILE);

    DmnDecisionTableResult decisionResult = decisionService.evaluateDecisionTableByKey(DECISION_DEFINITION_KEY)
        .variables(createVariables())
        .evaluate();

    assertThatDecisionHasResult(decisionResult, RESULT_OF_FIRST_VERSION);
  }


  @Test
  public void testEvaluateDecisionByKeyAndTenantId() {
    testRule.deployForTenant(TENANT_ONE, DMN_FILE);
    testRule.deployForTenant(TENANT_TWO, DMN_FILE_SECOND_VERSION);

    DmnDecisionTableResult decisionResult = decisionService.evaluateDecisionTableByKey(DECISION_DEFINITION_KEY)
        .variables(createVariables())
        .decisionDefinitionTenantId(TENANT_ONE)
        .evaluate();

    assertThatDecisionHasResult(decisionResult, RESULT_OF_FIRST_VERSION);
  }

  @Test
  public void testEvaluateDecisionByKeyLatestVersionAndTenantId() {
    testRule.deployForTenant(TENANT_ONE, DMN_FILE);
    testRule.deployForTenant(TENANT_ONE, DMN_FILE_SECOND_VERSION);

    DmnDecisionTableResult decisionResult = decisionService.evaluateDecisionTableByKey(DECISION_DEFINITION_KEY)
        .variables(createVariables())
        .decisionDefinitionTenantId(TENANT_ONE)
        .evaluate();

    assertThatDecisionHasResult(decisionResult, RESULT_OF_SECOND_VERSION);
  }

  @Test
  public void testEvaluateDecisionByKeyVersionAndTenantId() {
    testRule.deployForTenant(TENANT_ONE, DMN_FILE);

    testRule.deployForTenant(TENANT_TWO, DMN_FILE);
    testRule.deployForTenant(TENANT_TWO, DMN_FILE_SECOND_VERSION);

    DmnDecisionTableResult decisionResult = decisionService.evaluateDecisionTableByKey(DECISION_DEFINITION_KEY)
        .variables(createVariables())
        .version(1)
        .decisionDefinitionTenantId(TENANT_TWO)
        .evaluate();

    assertThatDecisionHasResult(decisionResult, RESULT_OF_FIRST_VERSION);
  }

  @Test
  public void testEvaluateDecisionByKeyWithoutTenantIdNoAuthenticatedTenants() {
    identityService.setAuthentication("user", null, null);

   testRule.deploy(DMN_FILE);

    DmnDecisionTableResult decisionResult = decisionService.evaluateDecisionTableByKey(DECISION_DEFINITION_KEY)
        .decisionDefinitionWithoutTenantId()
        .variables(createVariables())
        .evaluate();

    assertThatDecisionHasResult(decisionResult, RESULT_OF_FIRST_VERSION);
  }

  @Test
  public void testFailToEvaluateDecisionByKeyNoAuthenticatedTenants() {
    identityService.setAuthentication("user", null, null);

    testRule.deployForTenant(TENANT_ONE, DMN_FILE);

    try {
      decisionService.evaluateDecisionTableByKey(DECISION_DEFINITION_KEY)
        .variables(createVariables())
        .evaluate();

      fail("expected exception");
    } catch (ProcessEngineException e) {
      assertThat(e.getMessage()).contains("no decision definition deployed with key 'decision'");
    }
  }

  @Test
  public void testFailToEvaluateDecisionByKeyWithTenantIdNoAuthenticatedTenants() {
    identityService.setAuthentication("user", null, null);

    testRule.deployForTenant(TENANT_ONE, DMN_FILE);

    try {
      decisionService.evaluateDecisionTableByKey(DECISION_DEFINITION_KEY)
        .decisionDefinitionTenantId(TENANT_ONE)
        .variables(createVariables())
        .evaluate();

      fail("expected exception");
    } catch (ProcessEngineException e) {
      assertThat(e.getMessage()).contains("Cannot evaluate the decision");
    }
  }

  @Test
  public void testFailToEvaluateDecisionByIdNoAuthenticatedTenants() {
    testRule.deployForTenant(TENANT_ONE, DMN_FILE);

    DecisionDefinition decisionDefinition = repositoryService
      .createDecisionDefinitionQuery()
      .singleResult();

    identityService.setAuthentication("user", null, null);

    try {
      decisionService.evaluateDecisionTableById(decisionDefinition.getId())
        .variables(createVariables())
        .evaluate();

      fail("expected exception");
    } catch (ProcessEngineException e) {
      assertThat(e.getMessage()).contains("Cannot evaluate the decision");
    }
  }

  @Test
  public void testEvaluateDecisionByKeyWithTenantIdAuthenticatedTenant() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    testRule.deployForTenant(TENANT_ONE, DMN_FILE);
    testRule.deployForTenant(TENANT_TWO, DMN_FILE);

    DmnDecisionTableResult decisionResult = decisionService.evaluateDecisionTableByKey(DECISION_DEFINITION_KEY)
      .decisionDefinitionTenantId(TENANT_ONE)
      .variables(createVariables())
      .evaluate();

    assertThatDecisionHasResult(decisionResult, RESULT_OF_FIRST_VERSION);
  }

  @Test
  public void testEvaluateDecisionByIdAuthenticatedTenant() {
    testRule.deployForTenant(TENANT_ONE, DMN_FILE);

    DecisionDefinition decisionDefinition = repositoryService
        .createDecisionDefinitionQuery()
        .singleResult();

    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    DmnDecisionTableResult decisionResult = decisionService.evaluateDecisionTableById(decisionDefinition.getId())
        .variables(createVariables())
        .evaluate();

    assertThatDecisionHasResult(decisionResult, RESULT_OF_FIRST_VERSION);
  }

  @Test
  public void testEvaluateDecisionByKeyWithAuthenticatedTenant() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    testRule.deployForTenant(TENANT_ONE, DMN_FILE);
    testRule.deployForTenant(TENANT_TWO, DMN_FILE_SECOND_VERSION);

    DmnDecisionTableResult decisionResult = decisionService.evaluateDecisionTableByKey(DECISION_DEFINITION_KEY)
        .variables(createVariables())
        .evaluate();

    assertThatDecisionHasResult(decisionResult, RESULT_OF_FIRST_VERSION);
  }

  @Test
  public void testEvaluateDecisionByKeyWithTenantIdDisabledTenantCheck() {
    processEngineConfiguration.setTenantCheckEnabled(false);
    identityService.setAuthentication("user", null, null);

    testRule.deployForTenant(TENANT_ONE, DMN_FILE);

    DmnDecisionTableResult decisionResult = decisionService.evaluateDecisionTableByKey(DECISION_DEFINITION_KEY)
        .decisionDefinitionTenantId(TENANT_ONE)
        .variables(createVariables())
        .evaluate();

    assertThatDecisionHasResult(decisionResult, RESULT_OF_FIRST_VERSION);
  }

  protected VariableMap createVariables() {
    return Variables.createVariables().putValue("status", "silver").putValue("sum", 723);
  }

  protected void assertThatDecisionHasResult(DmnDecisionTableResult decisionResult, Object expectedValue) {
    assertThat(decisionResult).isNotNull();
    assertThat(decisionResult.size()).isEqualTo(1);
    String value = decisionResult.getSingleResult().getFirstEntry();
    assertThat(value).isEqualTo(expectedValue);
  }

}
