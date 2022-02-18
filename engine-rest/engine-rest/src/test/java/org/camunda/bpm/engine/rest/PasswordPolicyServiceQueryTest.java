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
package org.camunda.bpm.engine.rest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.identity.PasswordPolicy;
import org.camunda.bpm.engine.identity.PasswordPolicyResult;
import org.camunda.bpm.engine.identity.PasswordPolicyRule;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.identity.PasswordPolicyDigitRuleImpl;
import org.camunda.bpm.engine.impl.identity.PasswordPolicyLengthRuleImpl;
import org.camunda.bpm.engine.rest.util.container.TestContainerRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

/**
 * @author Miklas Boskamp
 */
public class PasswordPolicyServiceQueryTest extends AbstractRestServiceTest {

  protected static final String QUERY_URL = TEST_RESOURCE_ROOT_PATH + IdentityRestService.PATH + "/password-policy";

  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();

  protected ProcessEngineConfiguration processEngineConfigurationMock = mock(ProcessEngineConfiguration.class);

  protected IdentityService identityServiceMock;

  @Before
  public void setUpMocks() {
    identityServiceMock = mock(IdentityService.class);

    when(processEngine.getProcessEngineConfiguration())
      .thenReturn(processEngineConfigurationMock);

    when(processEngine.getIdentityService())
      .thenReturn(identityServiceMock);
  }

  @Test
  public void shouldGetPolicy() {
    when(processEngineConfigurationMock.isEnablePasswordPolicy()).thenReturn(true);

    PasswordPolicy passwordPolicyMock = mock(PasswordPolicy.class);

    when(identityServiceMock.getPasswordPolicy())
      .thenReturn(passwordPolicyMock);

    when(passwordPolicyMock.getRules())
      .thenReturn(Collections.<PasswordPolicyRule>singletonList(new PasswordPolicyDigitRuleImpl(1)));

    given()
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .body("rules[0].placeholder", equalTo("PASSWORD_POLICY_DIGIT"))
        .body("rules[0].parameter.minDigit", equalTo("1"))
    .when()
      .get(QUERY_URL);
  }

  @Test
  public void shouldReturnNotFound_PasswordPolicyDisabled() {
    when(processEngineConfigurationMock.isEnablePasswordPolicy()).thenReturn(false);

    given()
    .then()
      .expect()
        .statusCode(Status.NOT_FOUND.getStatusCode())
    .when()
      .get(QUERY_URL);
  }

  @Test
  public void shouldCheckInvalidPassword() {
    when(processEngineConfigurationMock.isEnablePasswordPolicy()).thenReturn(true);

    PasswordPolicyResult passwordPolicyResultMock = mock(PasswordPolicyResult.class);

    when(identityServiceMock.checkPasswordAgainstPolicy(anyString(), (User)isNull()))
      .thenReturn(passwordPolicyResultMock);

    when(passwordPolicyResultMock.getFulfilledRules())
      .thenReturn(Collections.<PasswordPolicyRule>singletonList(new PasswordPolicyDigitRuleImpl(1)));

    when(passwordPolicyResultMock.getViolatedRules())
      .thenReturn(Collections.<PasswordPolicyRule>singletonList(new PasswordPolicyLengthRuleImpl(1)));

    given()
      .header("accept", MediaType.APPLICATION_JSON)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(Collections.singletonMap("password", "password"))
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())

        .body("rules[0].placeholder", equalTo("PASSWORD_POLICY_DIGIT"))
        .body("rules[0].parameter.minDigit", equalTo("1"))
        .body("rules[0].valid", equalTo(true))

        .body("rules[1].placeholder", equalTo("PASSWORD_POLICY_LENGTH"))
        .body("rules[1].parameter.minLength", equalTo("1"))
        .body("rules[1].valid", equalTo(false))

        .body("valid", equalTo(false))
    .when()
      .post(QUERY_URL);
  }

  @Test
  public void shouldCheckValidPassword() {
    when(processEngineConfigurationMock.isEnablePasswordPolicy()).thenReturn(true);

    PasswordPolicyResult passwordPolicyResultMock = mock(PasswordPolicyResult.class);

    when(identityServiceMock.checkPasswordAgainstPolicy(anyString(), (User)isNull()))
      .thenReturn(passwordPolicyResultMock);

    when(passwordPolicyResultMock.getFulfilledRules())
      .thenReturn(Collections.<PasswordPolicyRule>singletonList(new PasswordPolicyDigitRuleImpl(1)));

    when(passwordPolicyResultMock.getViolatedRules())
      .thenReturn(Collections.<PasswordPolicyRule>emptyList());
    
    given()
      .header("accept", MediaType.APPLICATION_JSON)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(Collections.singletonMap("password", "password"))
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .body("valid", equalTo(true))
    .when()
      .post(QUERY_URL);
  }

  @Test
  public void shouldCheckPasswordPolicyWithUserData() {
    when(processEngineConfigurationMock.isEnablePasswordPolicy()).thenReturn(true);

    User userMock = mock(User.class);
    when(identityServiceMock.newUser(anyString())).thenReturn(userMock);

    PasswordPolicyResult passwordPolicyResultMock = mock(PasswordPolicyResult.class);

    when(identityServiceMock.checkPasswordAgainstPolicy(anyString(), any(User.class)))
      .thenReturn(passwordPolicyResultMock);

    when(passwordPolicyResultMock.getFulfilledRules())
      .thenReturn(Collections.emptyList());

    when(passwordPolicyResultMock.getViolatedRules())
      .thenReturn(Collections.emptyList());

    Map<String, Object> payload = new HashMap<>();
    payload.put("password", "myCandidatePassword");

    Map<String, String> profile = new HashMap<>();
    profile.put("id", "myId");
    profile.put("firstName", "foo");
    profile.put("lastName", "bar");
    profile.put("email", "baz");
    payload.put("profile", profile);

    given()
      .header("accept", MediaType.APPLICATION_JSON)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(payload)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .body("valid", equalTo(true))
    .when()
      .post(QUERY_URL);

    verify(identityServiceMock)
        .checkPasswordAgainstPolicy("myCandidatePassword", userMock);
    verify(identityServiceMock).newUser("myId");
    verify(userMock).setFirstName("foo");
    verify(userMock).setLastName("bar");
    verify(userMock).setEmail("baz");
  }

  @Test
  public void shouldCheckPasswordPolicyWithUserDataAndUserIdNull() {
    when(processEngineConfigurationMock.isEnablePasswordPolicy()).thenReturn(true);

    User userMock = mock(User.class);
    when(identityServiceMock.newUser(anyString())).thenReturn(userMock);

    PasswordPolicyResult passwordPolicyResultMock = mock(PasswordPolicyResult.class);

    when(identityServiceMock.checkPasswordAgainstPolicy(anyString(), any(User.class)))
      .thenReturn(passwordPolicyResultMock);

    when(passwordPolicyResultMock.getFulfilledRules())
      .thenReturn(Collections.emptyList());

    when(passwordPolicyResultMock.getViolatedRules())
      .thenReturn(Collections.emptyList());

    Map<String, Object> payload = new HashMap<>();
    payload.put("password", "myCandidatePassword");

    Map<String, String> profile = new HashMap<>();
    profile.put("id", null);
    payload.put("profile", profile);

    given()
      .header("accept", MediaType.APPLICATION_JSON)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(payload)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .body("valid", equalTo(true))
    .when()
      .post(QUERY_URL);

    verify(identityServiceMock)
        .checkPasswordAgainstPolicy("myCandidatePassword", userMock);
    verify(identityServiceMock).newUser("");
  }

  @Test
  public void shouldCheckPasswordAgainstNoPolicy() {
    when(processEngineConfigurationMock.isEnablePasswordPolicy()).thenReturn(false);

    given()
      .header("accept", MediaType.APPLICATION_JSON)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(Collections.singletonMap("password", "password"))
    .then()
      .expect()
        .statusCode(Status.NOT_FOUND.getStatusCode())
    .when()
      .post(QUERY_URL);
  }

}