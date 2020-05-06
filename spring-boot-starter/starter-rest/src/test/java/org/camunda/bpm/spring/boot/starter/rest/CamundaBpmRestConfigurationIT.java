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
package org.camunda.bpm.spring.boot.starter.rest;

import static org.junit.Assert.assertEquals;

import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionDto;
import org.camunda.bpm.spring.boot.starter.property.CamundaBpmProperties;
import org.camunda.bpm.spring.boot.starter.rest.test.TestRestApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { TestRestApplication.class }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CamundaBpmRestConfigurationIT {

  @Autowired
  private TestRestTemplate testRestTemplate;

  @Autowired
  private CamundaBpmProperties camundaBpmProperties;

  @Test
  public void processDefinitionTest() {
    // start process
    testRestTemplate.postForEntity("/engine-rest/start/process", HttpEntity.EMPTY, String.class);

    ResponseEntity<ProcessDefinitionDto> entity = testRestTemplate.getForEntity("/engine-rest/engine/{engineName}/process-definition/key/TestProcess/",
        ProcessDefinitionDto.class, camundaBpmProperties.getProcessEngineName());

    assertEquals(HttpStatus.OK, entity.getStatusCode());
    assertEquals("TestProcess", entity.getBody().getKey());
  }
}
