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
package org.camunda.bpm.integrationtest.functional.context;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.integrationtest.functional.context.classes.MyPojo;
import org.camunda.bpm.integrationtest.util.AbstractFoxPlatformIntegrationTest;
import org.camunda.bpm.integrationtest.util.TestContainer;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.fail;

@RunWith(Arquillian.class)
public class SetVariablesMigrationContextSwitchTest extends AbstractFoxPlatformIntegrationTest {

  public static BpmnModelInstance oneTaskProcess(String key) {
    return Bpmn.createExecutableProcess(key)
        .camundaHistoryTimeToLive(180)
      .startEvent()
      .userTask("userTask")
      .endEvent()
      .done();
  }

  @Deployment(name = "sourceDeployment")
  public static WebArchive createSourceDeployment() {
    return initWebArchiveDeployment("sourceDeployment.war")
        .addAsResource(modelAsAsset(oneTaskProcess("sourceOneTaskProcess")), "oneTaskProcess.bpmn20.xml");
  }

  @Deployment(name = "targetDeployment")
  public static WebArchive createTargetDeployment() {
    return initWebArchiveDeployment("targetDeployment.war")
        .addClass(MyPojo.class)
        .addAsResource(modelAsAsset(oneTaskProcess("targetOneTaskProcess")), "oneTaskProcess.bpmn20.xml");
  }

  @Deployment(name="clientDeployment")
  public static WebArchive clientDeployment() {
    WebArchive webArchive = ShrinkWrap.create(WebArchive.class, "client.war")
        .addClass(AbstractFoxPlatformIntegrationTest.class);

    TestContainer.addContainerSpecificResources(webArchive);

    return webArchive;

  }

  @Test
  @OperateOnDeployment("clientDeployment")
  public void shouldDeserializeObjectVariable_Async() {
    // given
    ProcessDefinition sourceDefinition = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey("sourceOneTaskProcess")
        .singleResult();

    ProcessDefinition targetDefinition = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey("targetOneTaskProcess")
        .singleResult();

    String pi = runtimeService.startProcessInstanceById(sourceDefinition.getId()).getId();

    MigrationPlan migrationPlan = runtimeService.createMigrationPlan(sourceDefinition.getId(), targetDefinition.getId())
        .mapEqualActivities()
        .setVariables(Variables.putValue("foo",
            Variables.serializedObjectValue()
                .objectTypeName("org.camunda.bpm.integrationtest.functional.context.classes.MyPojo")
                .serializedValue("{\"name\": \"myName\", \"prio\": 5}")
                .serializationDataFormat("application/json")
                .create()))
        .build();

    runtimeService.newMigration(migrationPlan)
        .processInstanceIds(Collections.singletonList(pi))
        .executeAsync();

    // execute seed jobs
    List<Job> jobs = managementService.createJobQuery().list();
    for (Job job : jobs) {
      managementService.executeJob(job.getId());
    }

    // when: execute remaining batch jobs
    jobs = managementService.createJobQuery().list();
    for (Job job : jobs) {
      try {
        managementService.executeJob(job.getId());
      } catch (ProcessEngineException ex) {
        fail("No exception expected: " + ex.getMessage());
      }
    }

    // then
    Assert.assertNotNull(runtimeService.getVariableTyped(pi, "foo", false));
  }

  @Test
  @OperateOnDeployment("clientDeployment")
  public void shouldDeserializeObjectVariable_Sync() {
    // given
    ProcessDefinition sourceDefinition = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey("sourceOneTaskProcess")
        .singleResult();

    ProcessDefinition targetDefinition = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey("targetOneTaskProcess")
        .singleResult();

    String pi = runtimeService.startProcessInstanceById(sourceDefinition.getId()).getId();

    MigrationPlan migrationPlan = runtimeService.createMigrationPlan(sourceDefinition.getId(), targetDefinition.getId())
        .mapEqualActivities()
        .setVariables(Variables.putValue("foo",
            Variables.serializedObjectValue()
                .objectTypeName("org.camunda.bpm.integrationtest.functional.context.classes.MyPojo")
                .serializedValue("{\"name\": \"myName\", \"prio\": 5}")
                .serializationDataFormat("application/json")
                .create()))
        .build();

    // when
    runtimeService.newMigration(migrationPlan)
        .processInstanceIds(Collections.singletonList(pi))
        .execute();

    // then
    Assert.assertNotNull(runtimeService.getVariableTyped(pi, "foo", false));
  }

  protected static Asset modelAsAsset(BpmnModelInstance modelInstance) {
    ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
    Bpmn.writeModelToStream(byteStream, modelInstance);

    byte[] bytes = byteStream.toByteArray();
    return new ByteArrayAsset(bytes);
  }

}
