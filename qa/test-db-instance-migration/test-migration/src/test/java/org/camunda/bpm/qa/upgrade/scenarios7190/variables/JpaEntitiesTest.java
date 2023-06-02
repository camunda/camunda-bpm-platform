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
import static org.junit.Assert.assertTrue;

import java.util.Map;
import org.camunda.bpm.qa.upgrade.variables.*;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.qa.upgrade.Origin;
import org.camunda.bpm.qa.upgrade.ScenarioUnderTest;
import org.camunda.bpm.qa.upgrade.UpgradeTestRule;
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
    Object fieldAccessResult = runtimeService.getVariable(processInstanceId, "simpleEntityFieldAccess");
    assertThat(fieldAccessResult).isInstanceOf(FieldAccessJPAEntity.class);
    assertThat(((FieldAccessJPAEntity)fieldAccessResult).getId().longValue()).isEqualTo(1L);
    assertThat(((FieldAccessJPAEntity)fieldAccessResult).getMyValue()).isEqualTo("value1");

//    // Read entity with @Id on property
//    Object propertyAccessResult = runtimeService.getVariable(processInstanceId, "simpleEntityPropertyAccess");
//    assertThat(propertyAccessResult).isInstanceOf(PropertyAccessJPAEntity.class);
//    assertThat(((PropertyAccessJPAEntity)propertyAccessResult).getId().longValue()).isEqualTo(1L);
//    assertThat(((PropertyAccessJPAEntity)propertyAccessResult).getMyValue()).isEqualTo("value2");
//
//    // Read entity with @Id on field of mapped superclass
//    Object subclassFieldResult = runtimeService.getVariable(processInstanceId, "subclassFieldAccess");
//    assertThat(subclassFieldResult).isInstanceOf(PropertyAccessJPAEntity.class);
//    assertThat(((SubclassFieldAccessJPAEntity)subclassFieldResult).getId().longValue()).isEqualTo(1L);
//    assertThat(((SubclassFieldAccessJPAEntity)subclassFieldResult).getValue()).isEqualTo("value3");
//
//    // Read entity with @Id on property of mapped superclass
//    Object subclassPropertyResult = runtimeService.getVariable(processInstanceId, "subclassPropertyAccess");
//    assertThat(subclassPropertyResult).isInstanceOf(PropertyAccessJPAEntity.class);
//    assertThat(((SubclassPropertyAccessJPAEntity)subclassPropertyResult).getId().longValue()).isEqualTo(1L);
//    assertThat(((SubclassPropertyAccessJPAEntity)subclassPropertyResult).getValue()).isEqualTo("value4");

    // -----------------------------------------------------------------------------
    // Test updating JPA-entity to null-value and back again
    // -----------------------------------------------------------------------------
    Object currentValue = runtimeService.getVariable(processInstanceId, "simpleEntityFieldAccess");
    // Set to null
    runtimeService.setVariable(processInstanceId, "simpleEntityFieldAccess", null);
    currentValue = runtimeService.getVariable(processInstanceId, "simpleEntityFieldAccess");
    assertThat(currentValue).isNull();
    // Set to JPA-entity again
    runtimeService.setVariable(processInstanceId, "simpleEntityFieldAccess", "testString");
    currentValue = runtimeService.getVariable(processInstanceId, "simpleEntityFieldAccess");
    assertThat(currentValue).isNotNull();
    assertThat(currentValue).isInstanceOf(String.class);
    assertThat(currentValue).isEqualTo("testString");
    assertEquals(1L, ((FieldAccessJPAEntity)currentValue).getId().longValue());
    FieldAccessJPAEntity simpleEntityFieldAccess = new FieldAccessJPAEntity();
    simpleEntityFieldAccess.setId(1L);
    simpleEntityFieldAccess.setMyValue("value1");
    runtimeService.setVariable(processInstanceId, "simpleEntityFieldAccess", simpleEntityFieldAccess);
    currentValue = runtimeService.getVariable(processInstanceId, "simpleEntityFieldAccess");
    assertNotNull(currentValue);
    assertTrue(currentValue instanceof String);
    assertEquals(1L, ((FieldAccessJPAEntity)currentValue).getId().longValue());

//    Batch batch = managementService.createBatchQuery().batchId(batchId).singleResult();
//    String seedJobDefinitionId = batch.getSeedJobDefinitionId();
//    Job seedJob = managementService.createJobQuery().jobDefinitionId(seedJobDefinitionId).singleResult();
//
//    Job timerJob = managementService.createJobQuery().processDefinitionKey("createProcessForSetRetriesWithDueDate_718").singleResult();
//
//    managementService.executeJob(seedJob.getId());
//    List<Job> batchJobs = managementService.createJobQuery()
//        .jobDefinitionId(batch.getBatchJobDefinitionId())
//        .list();
//
//    // when
//    batchJobs.forEach(job -> managementService.executeJob(job.getId()));

    // then
//    Job timerJobAfterBatch = managementService.createJobQuery().processDefinitionKey("createProcessForSetRetriesWithDueDate_718").singleResult();
//    assertThat(timerJob.getDuedate()).isEqualToIgnoringMillis(timerJobAfterBatch.getDuedate());
//    assertThat(timerJob.getRetries()).isEqualTo(3);
//    assertThat(timerJobAfterBatch.getRetries()).isEqualTo(5);
  }
}
