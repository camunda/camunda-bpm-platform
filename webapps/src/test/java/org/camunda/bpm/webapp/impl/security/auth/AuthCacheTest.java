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
package org.camunda.bpm.webapp.impl.security.auth;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.webapp.impl.IllegalWebAppConfigurationException;
import org.camunda.bpm.webapp.impl.security.SecurityActions;
import org.camunda.bpm.webapp.impl.util.ProcessEngineUtil;
import org.camunda.bpm.webapp.impl.util.ServletContextUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.camunda.bpm.engine.rest.util.DateTimeUtils.addDays;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public class AuthCacheTest {

  protected MockedStatic<AuthenticationUtil> mockedAuthenticationUtil;

  protected MockedStatic<SecurityActions> mockedSecurityActions;

  protected MockedStatic<ProcessEngineUtil> mockedProcessEngineUtil;

  @Before
  public void setup() {
    ClockUtil.setCurrentTime(ClockUtil.getCurrentTime());
  }

  @After
  public void reset() {
    Authentications.clearCurrent();
    ClockUtil.reset();
    if (mockedAuthenticationUtil != null) {
      mockedAuthenticationUtil.close();
    }
    if (mockedSecurityActions != null) {
      mockedSecurityActions.close();
    }
    if (mockedProcessEngineUtil != null) {
      mockedProcessEngineUtil.close();
    }
  }

  @Test
  public void shouldThrowExceptionWhenTimeToLiveIsNegative() {
    AuthenticationFilter authenticationFilter = new AuthenticationFilter();
    MockFilterConfig config = new MockFilterConfig();
    config.addInitParameter(AuthenticationFilter.AUTH_CACHE_TTL_INIT_PARAM_NAME, "-1000");
    assertThatThrownBy(() -> authenticationFilter.init(config))
      .isInstanceOf(IllegalWebAppConfigurationException.class)
      .hasMessage("'cacheTimeToLive' cannot be negative.");
  }

  @Test
  public void shouldThrowExceptionSinceTimeToLiveIsNotALong() {
    AuthenticationFilter authenticationFilter = new AuthenticationFilter();
    MockFilterConfig config = new MockFilterConfig();
    config.addInitParameter(AuthenticationFilter.AUTH_CACHE_TTL_INIT_PARAM_NAME, "6.7");
    assertThatThrownBy(() -> authenticationFilter.init(config))
      .isInstanceOf(NumberFormatException.class)
      .hasMessage("For input string: \"6.7\"");
  }

  @Test
  public void shouldTrimTimeToLive() throws ServletException {
    AuthenticationFilter authenticationFilter = new AuthenticationFilter();
    MockFilterConfig config = new MockFilterConfig();
    config.addInitParameter(AuthenticationFilter.AUTH_CACHE_TTL_INIT_PARAM_NAME, " 123   ");
    authenticationFilter.init(config);
    assertThat(authenticationFilter.getCacheTimeToLive()).isEqualTo(123);
  }

  @Test
  public void shouldRevalidateWhenValidationTimeDue() throws ServletException, IOException {
    // given
    setupEngineMock();
    AuthenticationFilter filter = setupFilter(1000 * 60 * 5);
    Authentications authentications = setupAuth();
    UserAuthentication initialAuthentication = getAuthByEngine(authentications, "engine1");

    // assume
    assertThat(initialAuthentication.getCacheValidationTime()).isNull();

    applyFilter(filter, new MockHttpServletRequest());

    UserAuthentication nextAuth = getAuthByEngine(authentications, "engine1");
    assertThat(nextAuth.getCacheValidationTime())
      .isEqualTo(addMinutes(5, ClockUtil.getCurrentTime()));

    assertThat(initialAuthentication).isNotSameAs(nextAuth);

    Date currentTime = addDays(ClockUtil.getCurrentTime(), 2);
    ClockUtil.setCurrentTime(currentTime);

    // when
    applyFilter(filter, new MockHttpServletRequest());

    // then
    assertThat(getAuthByEngine(authentications, "engine1").getCacheValidationTime())
      .isEqualTo(addMinutes(5, ClockUtil.getCurrentTime()));
    assertThat(initialAuthentication).isNotSameAs(getAuthByEngine(authentications, "engine1"));
  }

  @Test
  public void shouldRevalidateWhenTTLZero() throws ServletException, IOException {
    // given
    setupEngineMock();
    AuthenticationFilter filter = setupFilter(0);
    Authentications authentications = setupAuth();
    UserAuthentication initialAuthentication = getAuthByEngine(authentications, "engine1");

    // assume
    assertThat(initialAuthentication.getCacheValidationTime()).isNull();

    applyFilter(filter, new MockHttpServletRequest());

    UserAuthentication nextAuth = getAuthByEngine(authentications, "engine1");
    assertThat(nextAuth.getCacheValidationTime()).isNull();

    assertThat(initialAuthentication).isNotSameAs(nextAuth);

    Date currentTime = addDays(ClockUtil.getCurrentTime(), 2);
    ClockUtil.setCurrentTime(currentTime);

    // when
    applyFilter(filter, new MockHttpServletRequest());

    // then
    assertThat(getAuthByEngine(authentications, "engine1").getCacheValidationTime()).isNull();
    assertThat(initialAuthentication).isNotSameAs(getAuthByEngine(authentications, "engine1"));
  }

  @Test
  public void shouldNotRevalidateWhenValidationTimeNotDue() throws ServletException, IOException {
    // given
    setupEngineMock();
    AuthenticationFilter filter = setupFilter(1000 * 60 * 5);
    Authentications authentications = setupAuth();
    UserAuthentication initialAuthentication = getAuthByEngine(authentications, "engine1");

    MockHttpServletRequest request = new MockHttpServletRequest();

    // assume
    assertThat(initialAuthentication.getCacheValidationTime()).isNull();

    applyFilter(filter, request);

    Date datePlus5Minutes = addMinutes(5, ClockUtil.getCurrentTime());

    UserAuthentication nextAuth = getAuthByEngine(authentications, "engine1");
    assertThat(nextAuth.getCacheValidationTime()).isEqualTo(datePlus5Minutes);
    assertThat(initialAuthentication).isNotSameAs(nextAuth);

    // when
    applyFilter(filter, request);

    // then
    assertThat(getAuthByEngine(authentications, "engine1").getCacheValidationTime())
      .isEqualTo(datePlus5Minutes);
    assertThat(nextAuth).isSameAs(getAuthByEngine(authentications, "engine1"));
  }

  @Test
  public void shouldNotRevalidateWhenDisabled() throws ServletException, IOException {
    setupEngineMock();
    Authentications authentications = setupAuth();
    UserAuthentication initialAuthentication = getAuthByEngine(authentications, "engine1");

    MockHttpServletRequest request = new MockHttpServletRequest();

    // assume
    assertThat(initialAuthentication.getCacheValidationTime()).isNull();

    // when
    applyFilter(setupFilter(new MockFilterConfig()), request);

    // then
    assertThat(initialAuthentication.getCacheValidationTime()).isNull();
    assertThat(initialAuthentication).isSameAs(getAuthByEngine(authentications, "engine1"));
  }

  @Test
  public void shouldSetValidationTimeInitially() throws ServletException, IOException {
    // given
    setupEngineMock();
    Authentications authentications = setupAuth();
    UserAuthentication initialAuthentication = getAuthByEngine(authentications, "engine1");

    MockHttpServletRequest request = new MockHttpServletRequest();

    // assume
    assertThat(initialAuthentication.getCacheValidationTime()).isNull();

    // when
    applyFilter(setupFilter(1000 * 60 * 5), request);

    // then
    UserAuthentication nextAuth = getAuthByEngine(authentications, "engine1");
    assertThat(nextAuth.getCacheValidationTime())
      .isEqualTo(addMinutes(5, ClockUtil.getCurrentTime()));
    assertThat(initialAuthentication).isNotSameAs(nextAuth);
  }

  @Test
  public void shouldHaveValidationTimeInitially() throws ServletException, IOException {
    // given
    setupEngineMock();
    Authentications authentications = setupAuth();
    UserAuthentication initialAuthentication = getAuthByEngine(authentications, "engine1");

    MockHttpServletRequest request = new MockHttpServletRequest();

    // assume
    assertThat(ServletContextUtil.getAuthCacheValidationTime(request.getServletContext())).isNull();

    // when
    applyFilter(setupFilter(1000 * 60 * 5), request);

    // then
    assertThat(ServletContextUtil.getAuthCacheValidationTime(request.getServletContext())).isNotNull();
    UserAuthentication nextAuth = getAuthByEngine(authentications, "engine1");
    assertThat(nextAuth.getCacheValidationTime())
      .isEqualTo(addMinutes(5, ClockUtil.getCurrentTime()));
    assertThat(initialAuthentication).isNotSameAs(nextAuth);
  }

  @Test
  public void shouldInvalidateSessionDueToDeletedUser() throws ServletException, IOException {
    // given
    ProcessEngine engineMock = setupEngineMock()[0];
    Authentications authentications = setupAuth();
    UserAuthentication initialAuthentication = getAuthByEngine(authentications, "engine1");
    AuthenticationFilter filter = setupFilter(1000 * 60 * 5);

    MockHttpServletRequest request = new MockHttpServletRequest();

    // assume
    assertThat(initialAuthentication.getCacheValidationTime()).isNull();

    applyFilter(filter, request);

    UserAuthentication nextAuth = getAuthByEngine(authentications, "engine1");
    assertThat(nextAuth.getCacheValidationTime()).isEqualTo(addMinutes(5, ClockUtil.getCurrentTime()));
    assertThat(initialAuthentication).isNotSameAs(nextAuth);

    User user = engineMock.getIdentityService()
      .createUserQuery()
      .userId("userId1")
      .singleResult();

    when(user).thenReturn(null);

    Date currentTime = addDays(ClockUtil.getCurrentTime(), 2);
    ClockUtil.setCurrentTime(currentTime);

    MockHttpServletResponse response = new MockHttpServletResponse();

    // when
    applyFilter(filter, request, response);

    // then
    assertThat(getAuthByEngine(authentications, "engine1")).isNull();
  }

  @Test
  public void shouldRevalidateWhenValidationTimeDueWithMultipleAuths() throws ServletException, IOException {
    // given
    setupEngineMock("engine1", "engine2", "engine3");
    AuthenticationFilter filter = setupFilter(1000 * 60 * 5);
    Authentications authentications = setupAuth("engine1", "engine2", "engine3");

    UserAuthentication authEngineOne = getAuthByEngine(authentications, "engine1");
    UserAuthentication authEngineTwo = getAuthByEngine(authentications, "engine2");
    UserAuthentication authEngineThree = getAuthByEngine(authentications, "engine3");

    MockHttpServletRequest request = new MockHttpServletRequest();

    // assume
    assertThat(authEngineOne.getCacheValidationTime()).isNull();
    assertThat(authEngineTwo.getCacheValidationTime()).isNull();
    assertThat(authEngineThree.getCacheValidationTime()).isNull();

    applyFilter(filter, request);

    UserAuthentication nextAuthEngineOne = getAuthByEngine(authentications, "engine1");
    assertThat(authEngineOne).isNotSameAs(nextAuthEngineOne);
    UserAuthentication nextAuthEngineTwo = getAuthByEngine(authentications, "engine2");
    assertThat(authEngineTwo).isNotSameAs(nextAuthEngineTwo);
    UserAuthentication nextAuthEngineThree = getAuthByEngine(authentications, "engine3");
    assertThat(authEngineThree).isNotSameAs(nextAuthEngineThree);

    Date datePlus5Minutes = addMinutes(5, ClockUtil.getCurrentTime());
    assertThat(nextAuthEngineOne.getCacheValidationTime()).isEqualTo(datePlus5Minutes);
    assertThat(nextAuthEngineTwo.getCacheValidationTime()).isEqualTo(datePlus5Minutes);
    assertThat(nextAuthEngineThree.getCacheValidationTime()).isEqualTo(datePlus5Minutes);

    Date currentTime = addDays(ClockUtil.getCurrentTime(), 2);
    ClockUtil.setCurrentTime(currentTime);

    request = new MockHttpServletRequest();

    // when
    applyFilter(filter, request);

    // then
    assertThat(authEngineOne)
      .isNotSameAs(nextAuthEngineOne)
      .isNotSameAs(getAuthByEngine(authentications, "engine1"));
    assertThat(authEngineTwo)
      .isNotSameAs(nextAuthEngineTwo)
      .isNotSameAs(getAuthByEngine(authentications, "engine2"));
    assertThat(authEngineThree)
      .isNotSameAs(nextAuthEngineThree)
      .isNotSameAs(getAuthByEngine(authentications, "engine3"));

    assertThat(getAuthByEngine(authentications, "engine1").getCacheValidationTime())
      .isEqualTo(addMinutes(5, ClockUtil.getCurrentTime()));
    assertThat(getAuthByEngine(authentications, "engine2").getCacheValidationTime())
      .isEqualTo(addMinutes(5, ClockUtil.getCurrentTime()));
    assertThat(getAuthByEngine(authentications, "engine3").getCacheValidationTime())
      .isEqualTo(addMinutes(5, ClockUtil.getCurrentTime()));
  }

  @Test
  public void shouldInvalidateSessionDueToDeletedUserWithMultipleAuths() throws ServletException, IOException {
    // given
    ProcessEngine[] engineMocks = setupEngineMock("engine1", "engine2", "engine3");
    Authentications authentications = setupAuth("engine1", "engine2", "engine3");
    AuthenticationFilter filter = setupFilter(1000 * 60 * 5);

    UserAuthentication authEngineOne = getAuthByEngine(authentications, "engine1");
    UserAuthentication authEngineTwo = getAuthByEngine(authentications, "engine2");
    UserAuthentication authEngineThree = getAuthByEngine(authentications, "engine3");

    MockHttpServletRequest request = new MockHttpServletRequest();

    // assume
    assertThat(authEngineOne.getCacheValidationTime()).isNull();
    assertThat(authEngineTwo.getCacheValidationTime()).isNull();
    assertThat(authEngineThree.getCacheValidationTime()).isNull();

    applyFilter(filter, request);

    UserAuthentication nextAuthEngineOne = getAuthByEngine(authentications, "engine1");
    assertThat(authEngineOne).isNotSameAs(nextAuthEngineOne);
    UserAuthentication nextAuthEngineTwo = getAuthByEngine(authentications, "engine2");
    assertThat(authEngineTwo).isNotSameAs(nextAuthEngineTwo);
    UserAuthentication nextAuthEngineThree = getAuthByEngine(authentications, "engine3");
    assertThat(authEngineThree).isNotSameAs(nextAuthEngineThree);

    Date datePlus5Minutes = addMinutes(5, ClockUtil.getCurrentTime());
    assertThat(nextAuthEngineOne.getCacheValidationTime())
      .isEqualTo(datePlus5Minutes);
    assertThat(nextAuthEngineTwo.getCacheValidationTime())
      .isEqualTo(datePlus5Minutes);
    assertThat(nextAuthEngineThree.getCacheValidationTime())
      .isEqualTo(datePlus5Minutes);

    User user = engineMocks[1].getIdentityService()
      .createUserQuery()
      .userId("userId2")
      .singleResult();

    when(user).thenReturn(null);

    Date currentTime = addDays(ClockUtil.getCurrentTime(), 2);
    ClockUtil.setCurrentTime(currentTime);

    MockHttpServletResponse response = new MockHttpServletResponse();

    // when
    applyFilter(filter, request, response);

    // then
    assertThat(getAuthByEngine(authentications, "engine1")).isNotNull();
    assertThat(getAuthByEngine(authentications, "engine2")).isNull();
    assertThat(getAuthByEngine(authentications, "engine3")).isNotNull();
  }

  @Test
  public void shouldNotRevalidateWhenValidationTimeNotDueWithMultipleAuths() throws ServletException, IOException {
    // given
    setupEngineMock("engine1", "engine2", "engine3");
    AuthenticationFilter filter = setupFilter(1000 * 60 * 5);
    Authentications authentications = setupAuth("engine1", "engine2", "engine3");

    UserAuthentication authEngineOne = getAuthByEngine(authentications, "engine1");
    UserAuthentication authEngineTwo = getAuthByEngine(authentications, "engine2");
    UserAuthentication authEngineThree = getAuthByEngine(authentications, "engine3");

    MockHttpServletRequest request = new MockHttpServletRequest();

    // assume
    assertThat(authEngineOne.getCacheValidationTime()).isNull();
    assertThat(authEngineTwo.getCacheValidationTime()).isNull();
    assertThat(authEngineThree.getCacheValidationTime()).isNull();

    applyFilter(filter, request);

    UserAuthentication nextAuthEngineOne = getAuthByEngine(authentications, "engine1");
    assertThat(authEngineOne).isNotSameAs(nextAuthEngineOne);
    UserAuthentication nextAuthEngineTwo = getAuthByEngine(authentications, "engine2");
    assertThat(authEngineTwo).isNotSameAs(nextAuthEngineTwo);
    UserAuthentication nextAuthEngineThree = getAuthByEngine(authentications, "engine3");
    assertThat(authEngineThree).isNotSameAs(nextAuthEngineThree);

    Date datePlus5Minutes = addMinutes(5, ClockUtil.getCurrentTime());
    assertThat(nextAuthEngineOne.getCacheValidationTime()).isEqualTo(datePlus5Minutes);
    assertThat(nextAuthEngineTwo.getCacheValidationTime()).isEqualTo(datePlus5Minutes);
    assertThat(nextAuthEngineThree.getCacheValidationTime()).isEqualTo(datePlus5Minutes);

    // when
    applyFilter(filter, request);

    // then
    assertThat(nextAuthEngineOne)
      .isSameAs(getAuthByEngine(authentications, "engine1"));
    assertThat(nextAuthEngineTwo)
      .isSameAs(getAuthByEngine(authentications, "engine2"));
    assertThat(nextAuthEngineThree)
      .isSameAs(getAuthByEngine(authentications, "engine3"));
    assertThat(getAuthByEngine(authentications, "engine1").getCacheValidationTime()).isEqualTo(datePlus5Minutes);
    assertThat(getAuthByEngine(authentications, "engine1").getCacheValidationTime()).isEqualTo(datePlus5Minutes);
    assertThat(getAuthByEngine(authentications, "engine1").getCacheValidationTime()).isEqualTo(datePlus5Minutes);
  }

  // helpers ///////////////////////////////////////////////////////////////////////////////////////////////////////////

  protected ProcessEngine[] setupEngineMock(String... engines) {
    if (engines.length == 0) engines = new String[]{"engine1"};

    mockedAuthenticationUtil = mockStatic(AuthenticationUtil.class);
    mockedProcessEngineUtil = mockStatic(ProcessEngineUtil.class);
    List<ProcessEngine> processEngines = new ArrayList<>();
    Arrays.asList(engines).forEach(engine -> {
      ProcessEngine processEngineMock = mock(ProcessEngine.class, RETURNS_DEEP_STUBS);
      processEngines.add(processEngineMock);

      when(processEngineMock.getName()).thenReturn(engine);
      mockedProcessEngineUtil.when(() -> ProcessEngineUtil.lookupProcessEngine(eq(engine))).thenReturn(processEngineMock);
      mockedAuthenticationUtil.when(() -> AuthenticationUtil.updateCache(any(), any(), anyLong())).thenCallRealMethod();
      mockedAuthenticationUtil.when(() -> AuthenticationUtil.getSessionMutex(any())).thenCallRealMethod();
      mockedAuthenticationUtil.when(() -> AuthenticationUtil.createAuthentication(eq(engine), any())).thenCallRealMethod();
      mockedAuthenticationUtil.when(() -> AuthenticationUtil.createAuthentication(eq(engine), any(), any(), any())).thenCallRealMethod();
      mockedAuthenticationUtil.when(() -> AuthenticationUtil.createAuthentication(eq(processEngineMock), any(), any(), any())).thenCallRealMethod();
    });
    return processEngines.toArray(new ProcessEngine[0]);
  }

  protected Authentications setupAuth(String... engines) {
    if (engines.length == 0) engines = new String[]{"engine1"};

    Authentications authentications = new Authentications();
    List<String> enginesAsList = Arrays.asList(engines);
    for (int i = 0; i < enginesAsList.size(); i++) {
      String engine = enginesAsList.get(i);
      UserAuthentication userAuthentication = AuthenticationUtil.createAuthentication(engine, "userId" + (i + 1));
      authentications.addOrReplace(userAuthentication);
    }
    mockedAuthenticationUtil.when(() -> AuthenticationUtil.getAuthsFromSession(any(HttpSession.class))).thenReturn(authentications);
    return authentications;
  }

  protected AuthenticationFilter setupFilter(long cacheTTL) throws ServletException {
    MockFilterConfig config = new MockFilterConfig();
    config.addInitParameter(AuthenticationFilter.AUTH_CACHE_TTL_INIT_PARAM_NAME, String.valueOf(cacheTTL));
    return setupFilter(config);
  }

  protected AuthenticationFilter setupFilter(MockFilterConfig config) throws ServletException {
    mockedSecurityActions = mockStatic(SecurityActions.class);
    AuthenticationFilter authenticationFilter = new AuthenticationFilter();
    authenticationFilter.init(config);
    return authenticationFilter;
  }

  protected void applyFilter(AuthenticationFilter filter, MockHttpServletRequest request) throws ServletException, IOException {
    applyFilter(filter, request, null);
  }

  protected void applyFilter(AuthenticationFilter filter, MockHttpServletRequest request, MockHttpServletResponse response) throws ServletException, IOException {
    if (response == null) {
      response = new MockHttpServletResponse();
    }

    filter.doFilter(request, response, new MockFilterChain());
  }

  protected UserAuthentication getAuthByEngine(Authentications authentications, String engineName) {
    return authentications.getAuthentications()
      .stream()
      .filter(UserAuthentication -> UserAuthentication.getProcessEngineName().equals(engineName))
      .findFirst()
      .orElse(null);
  }

  protected Date addMinutes(int minutes, Date currentTime) {
    return new Date(currentTime.getTime() + 1000L * 60L * minutes);
  }

}

