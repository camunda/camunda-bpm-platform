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
package org.camunda.bpm.spring.boot.starter.webapp;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Svetlana Dorokhova.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
  classes = WebappExampleApplication.class,
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class WebappTest {

  @Autowired
  private TestRestTemplate testRestTemplate;

  @Test
  public void testEeResourceNotAvailable() {
    ResponseEntity<String> response =
        testRestTemplate.getForEntity("/camunda/plugin/adminEE/app/plugin.js", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  public void testAdminEndpointAvailable() {
    ResponseEntity<String> response =
        testRestTemplate.getForEntity("/camunda/app/admin/", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

}
