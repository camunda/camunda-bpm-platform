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

import static java.lang.String.format;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.camunda.bpm.spring.boot.starter.security.SampleApplication;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.BeansException;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SampleApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public abstract class AbstractSpringSecurityIT {

  protected static final String EXPECTED_NAME_DEFAULT = "[{\"name\":\"default\"}]";
  protected static final String PROVIDER = "mock-provider";
  protected static final String AUTHORIZED_USER = "bob";

  protected String baseUrl;

  @LocalServerPort
  protected int port;

  @Before
  public void setup() throws Exception {
    baseUrl = format("http://localhost:%d", port);
  }

  protected static Object getBeanForClass(Class<?> type, WebApplicationContext context) {
    try {
      return context.getBean(type);
    } catch (BeansException e) {
      return null;
    }
  }

  protected OAuth2AuthenticationToken createToken(String user) {
    List<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList("USER");
    OAuth2User oAuth2User = new DefaultOAuth2User(authorities, Map.of("name", user), "name");
    return new OAuth2AuthenticationToken(oAuth2User, authorities, AbstractSpringSecurityIT.PROVIDER);
  }

  protected void createAuthorizedClient(OAuth2AuthenticationToken authenticationToken,
                                        ClientRegistrationRepository registrations,
                                        OAuth2AuthorizedClientService authorizedClientService) {
    OAuth2AccessToken accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, "value", Instant.now(), Instant.now().plus(Duration.ofDays(1)));
    ClientRegistration clientRegistration = registrations.findByRegistrationId(authenticationToken.getAuthorizedClientRegistrationId());
    OAuth2AuthorizedClient authorizedClient = new OAuth2AuthorizedClient(clientRegistration, authenticationToken.getName(), accessToken);
    when(authorizedClientService.loadAuthorizedClient(AbstractSpringSecurityIT.PROVIDER, AbstractSpringSecurityIT.AUTHORIZED_USER)).thenReturn(authorizedClient);
  }

  public static class ResultCaptor<T> implements Answer<T> {
    public T result = null;

    @Override
    public T answer(InvocationOnMock invocationOnMock) throws Throwable {
      result = (T) invocationOnMock.callRealMethod();
      return result;
    }
  }

}
