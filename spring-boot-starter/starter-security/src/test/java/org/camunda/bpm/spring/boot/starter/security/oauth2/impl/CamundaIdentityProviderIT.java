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
package org.camunda.bpm.spring.boot.starter.security.oauth2.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.GroupQuery;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.identity.UserQuery;
import org.camunda.bpm.engine.impl.identity.WritableIdentityProvider;
import org.camunda.bpm.engine.impl.identity.db.DbGroupQueryImpl;
import org.camunda.bpm.engine.impl.identity.db.DbUserQueryImpl;
import org.camunda.bpm.spring.boot.starter.security.oauth2.AbstractSpringSecurityIT;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@AutoConfigureMockMvc
@TestPropertySource("/oauth2-mock.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class CamundaIdentityProviderIT extends AbstractSpringSecurityIT {

  @Autowired
  private IdentityService identityService;

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private TestRestTemplate restTemplate;

  @Autowired
  private ClientRegistrationRepository registrations;

  @MockBean
  private OAuth2AuthorizedClientService authorizedClientService;

  public static OAuth2IdentityProvider spiedIdentityProvider = spy(new OAuth2IdentityProvider());

  static { // needs to be executed before spring context initialization
    mockIdentityProviderFactory();
  }

  @Before
  public void setup() throws Exception {
    super.setup();
  }

  @Test
  public void testIdentityProviderForUsersWithoutSpringSecurity() {
    // given no spring security authentication
    User newUser = identityService.newUser("newUser");
    identityService.saveUser(newUser);
    ResultCaptor<UserQuery> resultCaptor = new ResultCaptor<>();
    doAnswer(resultCaptor).when(spiedIdentityProvider).createUserQuery();

    // when calling rest api
    ResponseEntity<String> entity = restTemplate.getForEntity(baseUrl + "/engine-rest/user/", String.class);

    // then identity provider does fallback to db provider
    assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
    verify(spiedIdentityProvider, atLeastOnce()).createUserQuery();
    UserQuery userQueryResult = resultCaptor.result;
    assertThat(userQueryResult).isInstanceOf(DbUserQueryImpl.class);
    assertThat(userQueryResult).isNotNull();
    assertThat(entity.getBody()).contains(newUser.getId());
  }

  @Test
  public void testIdentityProviderForGroupsWithoutSpringSecurity() {
    // given no spring security authentication
    Group newGroup = identityService.newGroup("newGroup");
    identityService.saveGroup(newGroup);
    ResultCaptor<GroupQuery> resultCaptor = new ResultCaptor<>();
    doAnswer(resultCaptor).when(spiedIdentityProvider).createGroupQuery();

    // when calling rest api
    ResponseEntity<String> entity = restTemplate.getForEntity(baseUrl + "/engine-rest/group/", String.class);

    // then identity provider does fallback to db provider
    assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
    verify(spiedIdentityProvider, atLeastOnce()).createGroupQuery();
    GroupQuery groupQueryResult = resultCaptor.result;
    assertThat(groupQueryResult).isInstanceOf(DbGroupQueryImpl.class);
    assertThat(groupQueryResult).isNotNull();
    assertThat(entity.getBody()).contains(newGroup.getId());
  }

  @Test
  public void testIdentityProviderForUsersWithSpringSecurity() throws Exception {
    // given spring security context
    ResultCaptor<UserQuery> resultCaptor = new ResultCaptor<>();
    doAnswer(resultCaptor).when(spiedIdentityProvider).createUserQuery();
    OAuth2AuthenticationToken authenticationToken = createToken(AUTHORIZED_USER);
    createAuthorizedClient(authenticationToken, registrations, authorizedClientService);

    // when calling webapp api
    mockMvc.perform(MockMvcRequestBuilders.get(baseUrl + "/camunda/api/engine/engine/default/user")
            .accept(MediaType.APPLICATION_JSON)
            .with(authentication(authenticationToken)))
        .andDo(MockMvcResultHandlers.print())
        .andExpect(MockMvcResultMatchers.status().isOk());

    // then identity provider handles oauth2 authentication
    verify(spiedIdentityProvider, atLeastOnce()).createUserQuery();
    UserQuery userQueryResult = resultCaptor.result;
    assertThat(userQueryResult).isInstanceOf(OAuth2IdentityProvider.OAuth2UserQuery.class);
    assertThat(userQueryResult).isNotNull();
    assertThat(((OAuth2IdentityProvider.OAuth2UserQuery) userQueryResult).getId()).isEqualTo(AUTHORIZED_USER);
  }

  @Test
  public void testIdentityProviderForGroupsWithSpringSecurity() throws Exception {
    // given spring security context
    ResultCaptor<GroupQuery> resultCaptor = new ResultCaptor<>();
    doAnswer(resultCaptor).when(spiedIdentityProvider).createGroupQuery();
    OAuth2AuthenticationToken authenticationToken = createToken(AUTHORIZED_USER);
    createAuthorizedClient(authenticationToken, registrations, authorizedClientService);

    // when calling webapp api
    mockMvc.perform(MockMvcRequestBuilders.get(baseUrl + "/camunda/api/engine/engine/default/user")
            .accept(MediaType.APPLICATION_JSON)
            .with(authentication(authenticationToken)))
        .andDo(MockMvcResultHandlers.print())
        .andExpect(MockMvcResultMatchers.status().isOk());

    // then identity provider handles oauth2 authentication
    verify(spiedIdentityProvider, atLeastOnce()).createGroupQuery();
    GroupQuery groupQueryResult = resultCaptor.result;
    assertThat(groupQueryResult).isInstanceOf(OAuth2IdentityProvider.OAuth2GroupQuery.class);
    assertThat(groupQueryResult).isNotNull();
    assertThat(((OAuth2IdentityProvider.OAuth2GroupQuery) groupQueryResult).getUserId()).isEqualTo(AUTHORIZED_USER);
  }

  private static void mockIdentityProviderFactory() {
    // mocks methods of all instances of OAuth2IdentityProviderFactory so instead of always
    // returning a new instance of the identity provider, it always returns our spy object
    mockConstruction(OAuth2IdentityProviderFactory.class, (mock, context) -> {
      doReturn(spiedIdentityProvider).when(mock).openSession();
      doReturn(WritableIdentityProvider.class).when(mock).getSessionType();
    });
  }
}
