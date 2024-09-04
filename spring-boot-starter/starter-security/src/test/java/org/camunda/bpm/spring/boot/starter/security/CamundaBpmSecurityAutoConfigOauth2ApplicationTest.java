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
package org.camunda.bpm.spring.boot.starter.security;

import my.own.custom.spring.boot.project.SampleApplication;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SampleApplication.class, webEnvironment = RANDOM_PORT)
@TestPropertySource("/oauth2-application.properties")
@AutoConfigureMockMvc
public class CamundaBpmSecurityAutoConfigOauth2ApplicationTest {

  /**
   * Docs: https://docs.spring.io/spring-security/reference/servlet/test/mockmvc/setup.html
   * 
   * Issues:
   * 1. TestPropertySource property 'spring.security.oauth2.client.provider.cognito.issuerUri' needs to be an actual URL - Not good: external dependency.
   * 2. /api urls return 404
   */

  private String baseUrl;

  @LocalServerPort
  private int port;
  
  @Autowired
  private WebApplicationContext context;
  
  @Autowired
  private MockMvc mockMvc;

  @Before
  public void setup() {
    baseUrl = String.format("http://localhost:%d", port);
    System.out.printf("base: %s%n", baseUrl);

    // docs suggest this instead @AutoConfigureMockMvc, but it doesn't change much
    //mockMvc = MockMvcBuilders.webAppContextSetup(context)
    //    .apply(SecurityMockMvcConfigurers.springSecurity())
    //    .build();
  }

  private static RequestPostProcessor oidcLogin() {
    return SecurityMockMvcRequestPostProcessors
        .oidcLogin()
        .authorities(new SimpleGrantedAuthority("camunda-admin"));
  }

  @Test
  public void webappApiWithoutAuthentication() throws Exception {
    // given - no authentication
    
    // when
    mockMvc.perform(MockMvcRequestBuilders
            .get(baseUrl + "/camunda/api/engine/engine/default/user")
            .accept(MediaType.APPLICATION_JSON))
        // then
        .andDo(MockMvcResultHandlers.print())
        .andExpect(MockMvcResultMatchers.status().isFound())
        .andExpect(MockMvcResultMatchers.header().exists("Location"))
        .andExpect(MockMvcResultMatchers.header().string("Location", baseUrl + "/oauth2/authorization/cognito"));
  }
  
  @Test
  public void webappApiWithOidcAuthentication() throws Exception {
    // when
    mockMvc.perform(MockMvcRequestBuilders
            .get(baseUrl + "/camunda/api/engine/engine/default/user")
            .accept(MediaType.APPLICATION_JSON)
            // given - OAuth2User
            .with(oidcLogin())
        )
        // then
        .andDo(MockMvcResultHandlers.print())
        // TODO returns 404
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.content().json("[{\"name\":\"default\"}]"));
  }

}
