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
package org.camunda.bpm.spring.boot.starter.security.oauth2;

import jakarta.servlet.Filter;
import java.lang.reflect.Field;
import org.camunda.bpm.engine.rest.security.auth.AuthenticationResult;
import org.camunda.bpm.spring.boot.starter.security.oauth2.impl.AuthorizeTokenFilter;
import org.camunda.bpm.spring.boot.starter.security.oauth2.impl.OAuth2AuthenticationProvider;
import org.camunda.bpm.webapp.impl.security.auth.ContainerBasedAuthenticationFilter;
import org.camunda.commons.testing.ProcessEngineLoggingRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

@AutoConfigureMockMvc
@TestPropertySource("/oauth2-mock.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class CamundaBpmSecurityAutoConfigOauth2ApplicationIT extends AbstractSpringSecurityIT {

  protected static final String UNAUTHORIZED_USER = "mary";

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private FilterRegistrationBean<Filter> filterRegistrationBean;

  @Autowired
  private ClientRegistrationRepository registrations;

  @MockBean
  private OAuth2AuthorizedClientService authorizedClientService;

  @Rule
  public ProcessEngineLoggingRule loggingRule = new ProcessEngineLoggingRule().watch(AuthorizeTokenFilter.class.getCanonicalName());

  private OAuth2AuthenticationProvider spiedAuthenticationProvider;

  @Before
  public void setup() throws Exception {
    super.setup();
    spyAuthenticationProvider();
  }

  @Test
  public void testSpringSecurityAutoConfigurationCorrectlySet() {
    // given oauth2 client configured
    // when retrieving config beans then only OAuth2AutoConfiguration is present
    assertThat(getBeanForClass(CamundaSpringSecurityOAuth2AutoConfiguration.class, mockMvc.getDispatcherServlet().getWebApplicationContext())).isNotNull();
    assertThat(getBeanForClass(CamundaBpmSpringSecurityDisableAutoConfiguration.class, mockMvc.getDispatcherServlet().getWebApplicationContext())).isNull();
  }

  @Test
  public void testWebappWithoutAuthentication() throws Exception {
    // given no authentication

    // when
    mockMvc.perform(MockMvcRequestBuilders.get(baseUrl + "/camunda/api/engine/engine/default/user")
            .accept(MediaType.APPLICATION_JSON))
        .andDo(MockMvcResultHandlers.print())
        // then oauth2 redirection occurs
        .andExpect(MockMvcResultMatchers.status().isFound())
        .andExpect(MockMvcResultMatchers.header().exists("Location"))
        .andExpect(MockMvcResultMatchers.header().string("Location", baseUrl + "/oauth2/authorization/" + PROVIDER));
  }

  @Test
  public void testWebappApiWithAuthorizedUser() throws Exception {
    // given authorized oauth2 authentication token
    OAuth2AuthenticationToken authenticationToken = createToken(AUTHORIZED_USER);
    createAuthorizedClient(authenticationToken, registrations, authorizedClientService);

    // when
    mockMvc.perform(MockMvcRequestBuilders.get(baseUrl + "/camunda/api/engine/engine/default/user")
            .accept(MediaType.APPLICATION_JSON)
            .with(authentication(authenticationToken)))
        // then call is successful
        .andDo(MockMvcResultHandlers.print())
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.content().json(EXPECTED_NAME_DEFAULT));
  }

  @Test
  public void testWebappWithUnauthorizedUser() throws Exception {
    // given unauthorized oauth2 authentication token
    OAuth2AuthenticationToken authenticationToken = createToken(UNAUTHORIZED_USER);
    createAuthorizedClient(authenticationToken, registrations, authorizedClientService);

    // when
    mockMvc.perform(MockMvcRequestBuilders.get(baseUrl + "/camunda/api/engine/engine/default/user")
            .accept(MediaType.APPLICATION_JSON)
            .with(authentication(authenticationToken)))
        // then authorization fails and redirection occurs
        .andExpect(MockMvcResultMatchers.status().isFound())
        .andExpect(MockMvcResultMatchers.header().exists("Location"))
        .andExpect(MockMvcResultMatchers.header().string("Location", baseUrl + "/oauth2/authorization/" + PROVIDER));

    String expectedWarn = "Authorize failed for '" + UNAUTHORIZED_USER + "'";
    assertThat(loggingRule.getFilteredLog(expectedWarn)).hasSize(1);
    verifyNoInteractions(spiedAuthenticationProvider);
  }


  @Test
  public void testOauth2AuthenticationProvider() throws Exception {
    // given authorized oauth2 authentication token
    ResultCaptor<AuthenticationResult> resultCaptor = new ResultCaptor<>();
    doAnswer(resultCaptor).when(spiedAuthenticationProvider).extractAuthenticatedUser(any(), any());
    OAuth2AuthenticationToken authenticationToken = createToken(AUTHORIZED_USER);
    createAuthorizedClient(authenticationToken, registrations, authorizedClientService);

    // when
    mockMvc.perform(MockMvcRequestBuilders.get(baseUrl + "/camunda/api/engine/engine/default/user")
            .accept(MediaType.APPLICATION_JSON)
            .with(authentication(authenticationToken)))
        // then call is successful
        .andDo(MockMvcResultHandlers.print())
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.content().json(EXPECTED_NAME_DEFAULT));

    // and authentication provider was called and returned expected authentication result
    verify(spiedAuthenticationProvider).extractAuthenticatedUser(any(), any());
    AuthenticationResult authenticationResult = resultCaptor.result;
    assertThat(authenticationResult.isAuthenticated()).isTrue();
    assertThat(authenticationResult.getAuthenticatedUser()).isEqualTo(AUTHORIZED_USER);
  }

  private void spyAuthenticationProvider() throws NoSuchFieldException, IllegalAccessException {
    ContainerBasedAuthenticationFilter filter = (ContainerBasedAuthenticationFilter) filterRegistrationBean.getFilter();
    Field authProviderField = ContainerBasedAuthenticationFilter.class.getDeclaredField("authenticationProvider");
    authProviderField.setAccessible(true);
    Object realAuthenticationProvider = authProviderField.get(filter);
    spiedAuthenticationProvider = (OAuth2AuthenticationProvider) spy(realAuthenticationProvider);
    authProviderField.set(filter, spiedAuthenticationProvider);
  }
}
