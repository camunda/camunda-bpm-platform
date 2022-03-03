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
package org.camunda.bpm.run.test.config.identity;

import org.camunda.bpm.run.property.CamundaBpmRunAuthenticationProperties;
import org.camunda.bpm.run.test.AbstractRestTest;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ActiveProfiles(profiles = {
    "test-changed-rest-context-path",
    "test-auth-enabled"
})
@TestPropertySource(properties = {
    CamundaBpmRunAuthenticationProperties.PREFIX + "=basic"
})
public class AuthenticationWithChangedContextPathTest extends AbstractRestTest {

  @Test
  public void shouldBlockRequest() {
    // given

    // when
    ResponseEntity<List> response = testRestTemplate.exchange("/rest/task",
        HttpMethod.GET, HttpEntity.EMPTY, List.class);

    // then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    assertThat(response.getHeaders().get("WWW-Authenticate"))
        .containsExactly("Basic realm=\"default\"");
  }

}