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
package org.camunda.bpm.spring.boot.starter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import javax.transaction.Transactional;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.spring.boot.starter.test.nonpa.TestApplication;
import org.camunda.bpm.spring.boot.starter.test.nonpa.jpa.domain.TestEntity;
import org.camunda.bpm.spring.boot.starter.test.nonpa.jpa.repository.TestEntityRepository;
import org.camunda.bpm.spring.boot.starter.test.nonpa.service.TransactionalTestService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { TestApplication.class },
  webEnvironment = WebEnvironment.NONE,
  properties = {
    "camunda.bpm.generate-unique-process-application-name=true",
    "spring.datasource.generate-unique-name=true",
  })
@Transactional
public class CamundaJpaAutoConfigurationIT extends AbstractCamundaAutoConfigurationIT {

  @Autowired
  private TestEntityRepository testEntityRepository;

  @Autowired
  private TransactionalTestService transactionalTestService;

  @Test
  public void jpaTest() {
    ProcessInstance processInstance = transactionalTestService.doOk();
    TestEntity testEntity = (TestEntity) runtimeService.getVariable(processInstance.getId(), "test");
    assertNotNull(testEntity);
    assertEquals("text", testEntity.getText());
    assertEquals(processInstance.getId(), runtimeService.createProcessInstanceQuery().variableValueEquals("test", testEntity).singleResult().getId());
    testEntity.setText("text2");
    testEntityRepository.save(testEntity);
    testEntity = (TestEntity) runtimeService.getVariable(processInstance.getId(), "test");
    assertEquals("text2", testEntity.getText());
  }

  @Test
  public void transactionTest() {
    assertEquals(0, testEntityRepository.count());
    try {
      transactionalTestService.doThrowing();
      fail();
    } catch (IllegalStateException e) {
      assertEquals(0, testEntityRepository.count());
      assertEquals(0, runtimeService.createProcessInstanceQuery().processDefinitionKey("TestProcess").count());
    }
  }
}
