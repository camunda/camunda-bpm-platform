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
package org.camunda.bpm.qa.upgrade.scenarios7190.variables;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.util.Map;

import javax.persistence.EntityManager;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.qa.upgrade.Origin;
import org.camunda.bpm.qa.upgrade.ScenarioUnderTest;
import org.camunda.bpm.qa.upgrade.UpgradeTestRule;
import org.camunda.bpm.qa.upgrade.variables.FieldAccessJPAEntity;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ScenarioUnderTest("JpaEntitiesScenario")
@Origin("7.19.0")
public class JpaEntitiesTest {

  Logger LOG = LoggerFactory.getLogger(JpaEntitiesTest.class);
  @Rule
  public UpgradeTestRule engineRule = new UpgradeTestRule();

  ManagementService managementService;
  RuntimeService runtimeService;

  @Before
  public void assignServices() {
    managementService = engineRule.getManagementService();
    runtimeService = engineRule.getRuntimeService();
  }

  @Test
  @ScenarioUnderTest("createJpaVariables.1")
  public void shouldHandleJpaVariables() {
    // given
    Map<String, String> properties = managementService.getProperties();
    String processInstanceId = properties.get("JpaEntitiesScenario.processInstanceId");
    System.out.println("Boom="+engineRule.getProcessEngineConfiguration().isJavaSerializationFormatEnabled());


    // Read entity with @Id on field
    Object singleResult = runtimeService.getVariable(processInstanceId,"simpleEntityFieldAccess");
    assertThat(singleResult).isInstanceOf(FieldAccessJPAEntity.class);
    assertThat(((FieldAccessJPAEntity)singleResult).getId().longValue()).isEqualTo(1L);
    assertThat(((FieldAccessJPAEntity)singleResult).getMyValue()).isEqualTo("value1");


    // -----------------------------------------------------------------------------
    // Test updating JPA-entity to null-value and back again
    // -----------------------------------------------------------------------------
    // Set to null
    runtimeService.setVariable(processInstanceId, "simpleEntityFieldAccess", null);
    Object currentValue = runtimeService.createVariableInstanceQuery().variableName("simpleEntityFieldAccess").singleResult().getValue();
    assertThat(currentValue).isNull();

    FieldAccessJPAEntity entity = extracted();
    // Set to JPA-entity again
    runtimeService.setVariable(processInstanceId, "simpleEntityFieldAccess", entity);

    currentValue = runtimeService.createVariableInstanceQuery().variableName("simpleEntityFieldAccess").singleResult().getValue();
    assertNotNull(currentValue);
    assertThat(currentValue).isInstanceOf(FieldAccessJPAEntity.class);
    assertEquals(10L, ((FieldAccessJPAEntity)currentValue).getId().longValue());
    assertThat(((FieldAccessJPAEntity)currentValue).getMyValue()).isEqualTo("value10");
  }

  protected FieldAccessJPAEntity extracted() {
    EntityManagerSessionFactory entityManagerSessionFactory = (EntityManagerSessionFactory) engineRule.getProcessEngineConfiguration()
        .getSessionFactories()
        .get(EntityManagerSession.class);
    EntityManager manager = entityManagerSessionFactory.getEntityManagerFactory().createEntityManager();
    manager.getTransaction().begin();

    FieldAccessJPAEntity entity = new FieldAccessJPAEntity();
    entity.setId(10L);
    entity.setMyValue("value10");
    manager.persist(entity);

    manager.flush();
    manager.getTransaction().commit();
    manager.close();
    return entity;
  }
}
