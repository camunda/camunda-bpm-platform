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
package org.camunda.bpm.spring.boot.starter.webapp.apppath;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.camunda.bpm.spring.boot.starter.webapp.WebappTestApp;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(
    classes = { WebappTestApp.class },
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RequestTrailingSlashIT {

  public static final List<String> REDIRECT_PATHS = List.of("/app", "/app/cockpit", "/app/admin", "/app/tasklist", "/app/welcome");

  TestRestTemplate client = new TestRestTemplate();

  @LocalServerPort
  public int port;

  @Test
  public void shouldRedirectPathWithMissingTrailingSlash() throws IOException {
    // given
    List<ResponseEntity<String>> responses = new ArrayList<>();

    // when calling different paths with and without trailing slashes
    for (String path : REDIRECT_PATHS) {
      String url = "http://localhost:" + port + "/camunda" + path;
      responses.add(client.getForEntity(url, String.class));
      responses.add(client.getForEntity(url + "/", String.class));
    }

    // then all paths should be found
    assertThat(responses).extracting("statusCode").containsOnly(HttpStatus.OK);
  }

}
