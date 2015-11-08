/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.test.api.repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.camunda.bpm.application.ProcessApplicationRegistration;
import org.camunda.bpm.application.impl.EmbeddedProcessApplication;
import org.camunda.bpm.engine.exception.NotFoundException;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.impl.persistence.entity.ResourceEntity;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.query.Query;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.DeploymentQuery;
import org.camunda.bpm.engine.repository.ProcessApplicationDeployment;
import org.camunda.bpm.engine.repository.ProcessDefinitionQuery;
import org.camunda.bpm.engine.repository.Resource;
import org.camunda.bpm.engine.repository.ResumePreviousBy;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;


/**
 * @author Roman Smirnov
 *
 */
public class RedeploymentTest extends PluggableProcessEngineTestCase {

  public static final String DEPLOYMENT_NAME = "my-deployment";
  public static final String PROCESS_KEY = "process";
  public static final String PROCESS_1_KEY = "process-1";
  public static final String PROCESS_2_KEY = "process-2";
  public static final String PROCESS_3_KEY = "process-3";
  public static final String RESOURCE_NAME = "path/to/my/process.bpmn";
  public static final String RESOURCE_1_NAME = "path/to/my/process1.bpmn";
  public static final String RESOURCE_2_NAME = "path/to/my/process2.bpmn";
  public static final String RESOURCE_3_NAME = "path/to/my/process3.bpmn";

  public void testRedeployInvalidDeployment() {

    try {
      repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResources("not-existing")
        .deploy();
      fail("It should not be able to re-deploy an unexisting deployment");
    } catch (NotFoundException e) {
      // expected
    }

    try {
      repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResourceById("not-existing", "an-id")
        .deploy();
      fail("It should not be able to re-deploy an unexisting deployment");
    } catch (NotFoundException e) {
      // expected
    }

    try {
      repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResourcesById("not-existing", Arrays.asList("an-id"))
        .deploy();
      fail("It should not be able to re-deploy an unexisting deployment");
    } catch (NotFoundException e) {
      // expected
    }

    try {
      repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResourceByName("not-existing", "a-name")
        .deploy();
      fail("It should not be able to re-deploy an unexisting deployment");
    } catch (NotFoundException e) {
      // expected
    }

    try {
      repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResourcesByName("not-existing", Arrays.asList("a-name"))
        .deploy();
      fail("It should not be able to re-deploy an unexisting deployment");
    } catch (NotFoundException e) {
      // expected
    }
  }

  public void testNotValidDeploymentId() {
    try {
      repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResources(null);
      fail("It should not be possible to pass a null deployment id");
    } catch (NotValidException e) {
      // expected
    }

    try {
      repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResourceById(null, "an-id");
      fail("It should not be possible to pass a null deployment id");
    } catch (NotValidException e) {
      // expected
    }

    try {
      repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResourcesById(null, Arrays.asList("an-id"));
      fail("It should not be possible to pass a null deployment id");
    } catch (NotValidException e) {
      // expected
    }

    try {
      repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResourceByName(null, "a-name");
      fail("It should not be possible to pass a null deployment id");
    } catch (NotValidException e) {
      // expected
    }

    try {
      repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResourcesByName(null, Arrays.asList("a-name"));
      fail("It should not be possible to pass a null deployment id");
    } catch (NotValidException e) {
      // expected
    }
  }

  public void testRedeployUnexistingDeploymentResource() {
    // given
    BpmnModelInstance model = createProcessWithServiceTask(PROCESS_KEY);

    Deployment deployment = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addModelInstance(RESOURCE_NAME, model)
        .deploy();

    try {
      // when
      repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResourceByName(deployment.getId(), "not-existing-resource.bpmn")
        .deploy();
      fail("It should not be possible to re-deploy a not existing deployment resource");
    } catch (NotFoundException e) {
      // then
      // expected
    }

    try {
      // when
      repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResourcesByName(deployment.getId(), Arrays.asList("not-existing-resource.bpmn"))
        .deploy();
      fail("It should not be possible to re-deploy a not existing deployment resource");
    } catch (NotFoundException e) {
      // then
      // expected
    }

    try {
      // when
      repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResourceById(deployment.getId(), "not-existing-resource-id")
        .deploy();
      fail("It should not be possible to re-deploy a not existing deployment resource");
    } catch (NotFoundException e) {
      // then
      // expected
    }

    try {
      // when
      repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResourcesById(deployment.getId(), Arrays.asList("not-existing-resource-id"))
        .deploy();
      fail("It should not be possible to re-deploy a not existing deployment resource");
    } catch (NotFoundException e) {
      // then
      // expected
    }

    deleteDeployments(deployment);
  }

  public void testNotValidResource() {
    try {
      repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResourceById("an-id", null);
      fail("It should not be possible to pass a null resource id");
    } catch (NotValidException e) {
      // expected
    }

    try {
      repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResourcesById("an-id", null);
      fail("It should not be possible to pass a null resource id");
    } catch (NotValidException e) {
      // expected
    }

    try {
      repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResourcesById("an-id", Arrays.asList((String)null));
      fail("It should not be possible to pass a null resource id");
    } catch (NotValidException e) {
      // expected
    }

    try {
      repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResourcesById("an-id", new ArrayList<String>());
      fail("It should not be possible to pass a null resource id");
    } catch (NotValidException e) {
      // expected
    }

    try {
      repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResourceByName("an-id", null);
      fail("It should not be possible to pass a null resource name");
    } catch (NotValidException e) {
      // expected
    }

    try {
      repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResourcesByName("an-id", null);
      fail("It should not be possible to pass a null resource name");
    } catch (NotValidException e) {
      // expected
    }

    try {
      repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResourcesByName("an-id", Arrays.asList((String)null));
      fail("It should not be possible to pass a null resource name");
    } catch (NotValidException e) {
      // expected
    }

    try {
      repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResourcesByName("an-id", new ArrayList<String>());
      fail("It should not be possible to pass a null resource name");
    } catch (NotValidException e) {
      // expected
    }
  }

  public void testRedeployNewDeployment() {
    // given
    BpmnModelInstance model = createProcessWithServiceTask(PROCESS_KEY);

    Deployment deployment1 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addModelInstance(RESOURCE_NAME, model)
        .deploy();

    DeploymentQuery query = repositoryService.createDeploymentQuery().deploymentName(DEPLOYMENT_NAME);

    assertNotNull(deployment1.getId());
    verifyQueryResults(query, 1);

    // when
    Deployment deployment2 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResources(deployment1.getId())
        .deploy();

    // then
    assertNotNull(deployment2);
    assertNotNull(deployment2.getId());
    assertFalse(deployment1.getId().equals(deployment2.getId()));

    verifyQueryResults(query, 2);

    deleteDeployments(deployment1, deployment2);
  }

  public void testFailingDeploymentName() {
    try {
      repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .nameFromDeployment("a-deployment-id");
      fail("Cannot set name() and nameFromDeployment().");
    } catch (NotValidException e) {
      // expected
    }

    try {
      repositoryService
        .createDeployment()
        .nameFromDeployment("a-deployment-id")
        .name(DEPLOYMENT_NAME);
      fail("Cannot set name() and nameFromDeployment().");
    } catch (NotValidException e) {
      // expected
    }
  }

  public void testRedeployDeploymentName() {
    // given
    BpmnModelInstance model = createProcessWithServiceTask(PROCESS_KEY);

    Deployment deployment1 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addModelInstance(RESOURCE_NAME, model)
        .deploy();

    assertEquals(DEPLOYMENT_NAME, deployment1.getName());

    // when
    Deployment deployment2 = repositoryService
        .createDeployment()
        .nameFromDeployment(deployment1.getId())
        .addDeploymentResources(deployment1.getId())
        .deploy();

    // then
    assertNotNull(deployment2);
    assertEquals(deployment1.getName(), deployment2.getName());

    deleteDeployments(deployment1, deployment2);
  }

  public void testRedeployDeploymentDifferentName() {
    // given
    BpmnModelInstance model = createProcessWithServiceTask(PROCESS_KEY);

    Deployment deployment1 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addModelInstance(RESOURCE_NAME, model)
        .deploy();

    assertEquals(DEPLOYMENT_NAME, deployment1.getName());

    // when
    Deployment deployment2 = repositoryService
        .createDeployment()
        .name("my-another-deployment")
        .addDeploymentResources(deployment1.getId())
        .deploy();

    // then
    assertNotNull(deployment2);
    assertFalse(deployment1.getName().equals(deployment2.getName()));

    deleteDeployments(deployment1, deployment2);
  }

  public void testRedeployDeploymentSourcePropertyNotSet() {
    // given
    BpmnModelInstance model = createProcessWithServiceTask(PROCESS_KEY);

    Deployment deployment1 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .source("my-deployment-source")
        .addModelInstance(RESOURCE_NAME, model)
        .deploy();

    assertEquals("my-deployment-source", deployment1.getSource());

    // when
    Deployment deployment2 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResources(deployment1.getId())
        .deploy();

    // then
    assertNotNull(deployment2);
    assertNull(deployment2.getSource());

    deleteDeployments(deployment1, deployment2);
  }

  public void testRedeploySetDeploymentSourceProperty() {
    // given
    BpmnModelInstance model = createProcessWithServiceTask(PROCESS_KEY);

    Deployment deployment1 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .source("my-deployment-source")
        .addModelInstance(RESOURCE_NAME, model)
        .deploy();

    assertEquals("my-deployment-source", deployment1.getSource());

    // when
    Deployment deployment2 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResources(deployment1.getId())
        .source("my-another-deployment-source")
        .deploy();

    // then
    assertNotNull(deployment2);
    assertEquals("my-another-deployment-source", deployment2.getSource());

    deleteDeployments(deployment1, deployment2);
  }

  public void testRedeployDeploymentResource() {
    // given

    // first deployment
    BpmnModelInstance model = createProcessWithServiceTask(PROCESS_KEY);
    Deployment deployment1 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addModelInstance(RESOURCE_NAME, model)
        .deploy();

    Resource resource1 = getResourceByName(deployment1.getId(), RESOURCE_NAME);

    // second deployment
    model = createProcessWithUserTask(PROCESS_KEY);
    Deployment deployment2 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addModelInstance(RESOURCE_NAME, model)
        .deploy();

    Resource resource2 = getResourceByName(deployment2.getId(), RESOURCE_NAME);

    // when
    Deployment deployment3 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResources(deployment1.getId())
        .deploy();

    // then
    Resource resource3 = getResourceByName(deployment3.getId(), RESOURCE_NAME);
    assertNotNull(resource3);

    // id
    assertNotNull(resource3.getId());
    assertFalse(resource1.getId().equals(resource3.getId()));

    // deployment id
    assertEquals(deployment3.getId(), resource3.getDeploymentId());

    // name
    assertEquals(resource1.getName(), resource3.getName());

    // bytes
    byte[] bytes1 = ((ResourceEntity) resource1).getBytes();
    byte[] bytes2 = ((ResourceEntity) resource2).getBytes();
    byte[] bytes3 = ((ResourceEntity) resource3).getBytes();
    assertTrue(Arrays.equals(bytes1, bytes3));
    assertFalse(Arrays.equals(bytes2, bytes3));

    deleteDeployments(deployment1, deployment2, deployment3);
  }

  public void testRedeployAllDeploymentResources() {
    // given
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();

    BpmnModelInstance model1 = createProcessWithServiceTask(PROCESS_1_KEY);
    BpmnModelInstance model2 = createProcessWithUserTask(PROCESS_2_KEY);

    // first deployment
    Deployment deployment1 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addModelInstance(RESOURCE_1_NAME, model1)
        .addModelInstance(RESOURCE_2_NAME, model2)
        .deploy();

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 1);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 1);

    // second deployment
    Deployment deployment2 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addModelInstance(RESOURCE_1_NAME, model2)
        .addModelInstance(RESOURCE_2_NAME, model1)
        .deploy();

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 2);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 2);

    // when
    Deployment deployment3 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResources(deployment1.getId())
        .deploy();

    // then
    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 3);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 3);

    deleteDeployments(deployment1, deployment2, deployment3);
  }

  public void testRedeployOneDeploymentResourcesByName() {
    // given
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();

    BpmnModelInstance model1 = createProcessWithServiceTask(PROCESS_1_KEY);
    BpmnModelInstance model2 = createProcessWithUserTask(PROCESS_2_KEY);

    // first deployment
    Deployment deployment1 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addModelInstance(RESOURCE_1_NAME, model1)
        .addModelInstance(RESOURCE_2_NAME, model2)
        .deploy();

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 1);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 1);

    // second deployment
    model1 = createProcessWithScriptTask(PROCESS_1_KEY);
    model2 = createProcessWithReceiveTask(PROCESS_2_KEY);

    Deployment deployment2 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addModelInstance(RESOURCE_1_NAME, model1)
        .addModelInstance(RESOURCE_2_NAME, model2)
        .deploy();

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 2);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 2);

    // when
    Deployment deployment3 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResourceByName(deployment1.getId(), RESOURCE_1_NAME)
        .deploy();

    // then
    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 3);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 2);

    deleteDeployments(deployment1, deployment2, deployment3);
  }

  public void testRedeployMultipleDeploymentResourcesByName() {
    // given
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();

    BpmnModelInstance model1 = createProcessWithServiceTask(PROCESS_1_KEY);
    BpmnModelInstance model2 = createProcessWithUserTask(PROCESS_2_KEY);
    BpmnModelInstance model3 = createProcessWithScriptTask(PROCESS_3_KEY);

    // first deployment
    Deployment deployment1 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addModelInstance(RESOURCE_1_NAME, model1)
        .addModelInstance(RESOURCE_2_NAME, model2)
        .addModelInstance(RESOURCE_3_NAME, model3)
        .deploy();

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 1);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 1);
    verifyQueryResults(query.processDefinitionKey(PROCESS_3_KEY), 1);

    // second deployment
    model1 = createProcessWithScriptTask(PROCESS_1_KEY);
    model2 = createProcessWithReceiveTask(PROCESS_2_KEY);
    model3 = createProcessWithUserTask(PROCESS_3_KEY);

    Deployment deployment2 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addModelInstance(RESOURCE_1_NAME, model1)
        .addModelInstance(RESOURCE_2_NAME, model2)
        .addModelInstance(RESOURCE_3_NAME, model3)
        .deploy();

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 2);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 2);
    verifyQueryResults(query.processDefinitionKey(PROCESS_3_KEY), 2);

    // when (1)
    Deployment deployment3 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResourceByName(deployment1.getId(), RESOURCE_1_NAME)
        .addDeploymentResourceByName(deployment1.getId(), RESOURCE_3_NAME)
        .deploy();

    // then (1)
    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 3);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 2);
    verifyQueryResults(query.processDefinitionKey(PROCESS_3_KEY), 3);

    // when (2)
    Deployment deployment4 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResourcesByName(deployment2.getId(), Arrays.asList(RESOURCE_1_NAME, RESOURCE_3_NAME))
        .deploy();

    // then (2)
    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 4);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 2);
    verifyQueryResults(query.processDefinitionKey(PROCESS_3_KEY), 4);

    deleteDeployments(deployment1, deployment2, deployment3, deployment4);
  }

  public void testRedeployOneAndMultipleDeploymentResourcesByName() {
    // given
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();

    BpmnModelInstance model1 = createProcessWithServiceTask(PROCESS_1_KEY);
    BpmnModelInstance model2 = createProcessWithUserTask(PROCESS_2_KEY);
    BpmnModelInstance model3 = createProcessWithScriptTask(PROCESS_3_KEY);

    // first deployment
    Deployment deployment1 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addModelInstance(RESOURCE_1_NAME, model1)
        .addModelInstance(RESOURCE_2_NAME, model2)
        .addModelInstance(RESOURCE_3_NAME, model3)
        .deploy();

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 1);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 1);
    verifyQueryResults(query.processDefinitionKey(PROCESS_3_KEY), 1);

    // second deployment
    model1 = createProcessWithScriptTask(PROCESS_1_KEY);
    model2 = createProcessWithReceiveTask(PROCESS_2_KEY);
    model3 = createProcessWithUserTask(PROCESS_3_KEY);

    Deployment deployment2 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addModelInstance(RESOURCE_1_NAME, model1)
        .addModelInstance(RESOURCE_2_NAME, model2)
        .addModelInstance(RESOURCE_3_NAME, model3)
        .deploy();

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 2);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 2);
    verifyQueryResults(query.processDefinitionKey(PROCESS_3_KEY), 2);

    // when
    Deployment deployment3 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResourceByName(deployment1.getId(), RESOURCE_1_NAME)
        .addDeploymentResourcesByName(deployment1.getId(), Arrays.asList(RESOURCE_2_NAME, RESOURCE_3_NAME))
        .deploy();

    // then
    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 3);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 3);
    verifyQueryResults(query.processDefinitionKey(PROCESS_3_KEY), 3);

    deleteDeployments(deployment1, deployment2, deployment3);
  }

  public void testSameDeploymentResourceByName() {
    // given
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();

    BpmnModelInstance model1 = createProcessWithServiceTask(PROCESS_1_KEY);
    BpmnModelInstance model2 = createProcessWithUserTask(PROCESS_2_KEY);

    // first deployment
    Deployment deployment1 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addModelInstance(RESOURCE_1_NAME, model1)
        .addModelInstance(RESOURCE_2_NAME, model2)
        .deploy();

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 1);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 1);

    // second deployment
    model1 = createProcessWithScriptTask(PROCESS_1_KEY);
    model2 = createProcessWithReceiveTask(PROCESS_2_KEY);

    Deployment deployment2 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addModelInstance(RESOURCE_1_NAME, model1)
        .addModelInstance(RESOURCE_2_NAME, model2)
        .deploy();

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 2);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 2);

    // when
    Deployment deployment3 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResourceByName(deployment1.getId(), RESOURCE_1_NAME)
        .addDeploymentResourcesByName(deployment1.getId(), Arrays.asList(RESOURCE_1_NAME))
        .deploy();

    // then
    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 3);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 2);

    deleteDeployments(deployment1, deployment2, deployment3);
  }

  public void testRedeployOneDeploymentResourcesById() {
    // given
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();

    BpmnModelInstance model1 = createProcessWithServiceTask(PROCESS_1_KEY);
    BpmnModelInstance model2 = createProcessWithUserTask(PROCESS_2_KEY);

    // first deployment
    Deployment deployment1 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addModelInstance(RESOURCE_1_NAME, model1)
        .addModelInstance(RESOURCE_2_NAME, model2)
        .deploy();

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 1);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 1);

    Resource resource = getResourceByName(deployment1.getId(), RESOURCE_1_NAME);

    // second deployment
    model1 = createProcessWithScriptTask(PROCESS_1_KEY);
    model2 = createProcessWithReceiveTask(PROCESS_2_KEY);

    Deployment deployment2 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addModelInstance(RESOURCE_1_NAME, model1)
        .addModelInstance(RESOURCE_2_NAME, model2)
        .deploy();

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 2);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 2);

    // when
    Deployment deployment3 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResourceById(deployment1.getId(), resource.getId())
        .deploy();

    // then
    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 3);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 2);

    deleteDeployments(deployment1, deployment2, deployment3);
  }

  public void testRedeployMultipleDeploymentResourcesById() {
    // given
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();

    BpmnModelInstance model1 = createProcessWithServiceTask(PROCESS_1_KEY);
    BpmnModelInstance model2 = createProcessWithUserTask(PROCESS_2_KEY);
    BpmnModelInstance model3 = createProcessWithScriptTask(PROCESS_3_KEY);

    // first deployment
    Deployment deployment1 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addModelInstance(RESOURCE_1_NAME, model1)
        .addModelInstance(RESOURCE_2_NAME, model2)
        .addModelInstance(RESOURCE_3_NAME, model3)
        .deploy();

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 1);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 1);
    verifyQueryResults(query.processDefinitionKey(PROCESS_3_KEY), 1);

    Resource resource11 = getResourceByName(deployment1.getId(), RESOURCE_1_NAME);
    Resource resource13 = getResourceByName(deployment1.getId(), RESOURCE_3_NAME);

    // second deployment
    model1 = createProcessWithScriptTask(PROCESS_1_KEY);
    model2 = createProcessWithReceiveTask(PROCESS_2_KEY);
    model3 = createProcessWithUserTask(PROCESS_3_KEY);

    Deployment deployment2 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addModelInstance(RESOURCE_1_NAME, model1)
        .addModelInstance(RESOURCE_2_NAME, model2)
        .addModelInstance(RESOURCE_3_NAME, model3)
        .deploy();

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 2);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 2);
    verifyQueryResults(query.processDefinitionKey(PROCESS_3_KEY), 2);

    Resource resource21 = getResourceByName(deployment2.getId(), RESOURCE_1_NAME);
    Resource resource23 = getResourceByName(deployment2.getId(), RESOURCE_3_NAME);

    // when (1)
    Deployment deployment3 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResourceById(deployment1.getId(), resource11.getId())
        .addDeploymentResourceById(deployment1.getId(), resource13.getId())
        .deploy();

    // then (1)
    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 3);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 2);
    verifyQueryResults(query.processDefinitionKey(PROCESS_3_KEY), 3);

    // when (2)
    Deployment deployment4 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResourcesById(deployment2.getId(), Arrays.asList(resource21.getId(), resource23.getId()))
        .deploy();

    // then (2)
    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 4);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 2);
    verifyQueryResults(query.processDefinitionKey(PROCESS_3_KEY), 4);

    deleteDeployments(deployment1, deployment2, deployment3, deployment4);
  }

  public void testRedeployOneAndMultipleDeploymentResourcesById() {
    // given
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();

    BpmnModelInstance model1 = createProcessWithServiceTask(PROCESS_1_KEY);
    BpmnModelInstance model2 = createProcessWithUserTask(PROCESS_2_KEY);
    BpmnModelInstance model3 = createProcessWithScriptTask(PROCESS_3_KEY);

    // first deployment
    Deployment deployment1 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addModelInstance(RESOURCE_1_NAME, model1)
        .addModelInstance(RESOURCE_2_NAME, model2)
        .addModelInstance(RESOURCE_3_NAME, model3)
        .deploy();

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 1);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 1);
    verifyQueryResults(query.processDefinitionKey(PROCESS_3_KEY), 1);

    Resource resource1 = getResourceByName(deployment1.getId(), RESOURCE_1_NAME);
    Resource resource2 = getResourceByName(deployment1.getId(), RESOURCE_2_NAME);
    Resource resource3 = getResourceByName(deployment1.getId(), RESOURCE_3_NAME);

    // second deployment
    model1 = createProcessWithScriptTask(PROCESS_1_KEY);
    model2 = createProcessWithReceiveTask(PROCESS_2_KEY);
    model3 = createProcessWithUserTask(PROCESS_3_KEY);

    Deployment deployment2 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addModelInstance(RESOURCE_1_NAME, model1)
        .addModelInstance(RESOURCE_2_NAME, model2)
        .addModelInstance(RESOURCE_3_NAME, model3)
        .deploy();

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 2);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 2);
    verifyQueryResults(query.processDefinitionKey(PROCESS_3_KEY), 2);

    // when
    Deployment deployment3 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResourceById(deployment1.getId(), resource1.getId())
        .addDeploymentResourcesById(deployment1.getId(), Arrays.asList(resource2.getId(), resource3.getId()))
        .deploy();

    // then
    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 3);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 3);
    verifyQueryResults(query.processDefinitionKey(PROCESS_3_KEY), 3);

    deleteDeployments(deployment1, deployment2, deployment3);
  }

  public void testRedeploySameDeploymentResourceById() {
    // given
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();

    BpmnModelInstance model1 = createProcessWithServiceTask(PROCESS_1_KEY);
    BpmnModelInstance model2 = createProcessWithUserTask(PROCESS_2_KEY);

    // first deployment
    Deployment deployment1 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addModelInstance(RESOURCE_1_NAME, model1)
        .addModelInstance(RESOURCE_2_NAME, model2)
        .deploy();

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 1);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 1);

    Resource resource1 = getResourceByName(deployment1.getId(), RESOURCE_1_NAME);

    // second deployment
    model1 = createProcessWithScriptTask(PROCESS_1_KEY);
    model2 = createProcessWithReceiveTask(PROCESS_2_KEY);

    Deployment deployment2 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addModelInstance(RESOURCE_1_NAME, model1)
        .addModelInstance(RESOURCE_2_NAME, model2)
        .deploy();

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 2);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 2);

    // when
    Deployment deployment3 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResourceById(deployment1.getId(), resource1.getId())
        .addDeploymentResourcesById(deployment1.getId(), Arrays.asList(resource1.getId()))
        .deploy();

    // then
    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 3);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 2);

    deleteDeployments(deployment1, deployment2, deployment3);
  }

  public void testRedeployDeploymentResourceByIdAndName() {
    // given
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();

    BpmnModelInstance model1 = createProcessWithServiceTask(PROCESS_1_KEY);
    BpmnModelInstance model2 = createProcessWithUserTask(PROCESS_2_KEY);

    // first deployment
    Deployment deployment1 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addModelInstance(RESOURCE_1_NAME, model1)
        .addModelInstance(RESOURCE_2_NAME, model2)
        .deploy();

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 1);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 1);

    Resource resource1 = getResourceByName(deployment1.getId(), RESOURCE_1_NAME);
    Resource resource2 = getResourceByName(deployment1.getId(), RESOURCE_2_NAME);

    // second deployment
    model1 = createProcessWithScriptTask(PROCESS_1_KEY);
    model2 = createProcessWithReceiveTask(PROCESS_2_KEY);

    Deployment deployment2 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addModelInstance(RESOURCE_1_NAME, model1)
        .addModelInstance(RESOURCE_2_NAME, model2)
        .deploy();

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 2);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 2);

    // when
    Deployment deployment3 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResourceById(deployment1.getId(), resource1.getId())
        .addDeploymentResourceByName(deployment1.getId(), resource2.getName())
        .deploy();

    // then
    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 3);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 3);

    deleteDeployments(deployment1, deployment2, deployment3);
  }

  public void testRedeployDeploymentResourceByIdAndNameMultiple() {
    // given
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();

    BpmnModelInstance model1 = createProcessWithServiceTask(PROCESS_1_KEY);
    BpmnModelInstance model2 = createProcessWithUserTask(PROCESS_2_KEY);

    // first deployment
    Deployment deployment1 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addModelInstance(RESOURCE_1_NAME, model1)
        .addModelInstance(RESOURCE_2_NAME, model2)
        .deploy();

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 1);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 1);

    Resource resource1 = getResourceByName(deployment1.getId(), RESOURCE_1_NAME);
    Resource resource2 = getResourceByName(deployment1.getId(), RESOURCE_2_NAME);

    // second deployment
    model1 = createProcessWithScriptTask(PROCESS_1_KEY);
    model2 = createProcessWithReceiveTask(PROCESS_2_KEY);

    Deployment deployment2 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addModelInstance(RESOURCE_1_NAME, model1)
        .addModelInstance(RESOURCE_2_NAME, model2)
        .deploy();

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 2);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 2);

    // when
    Deployment deployment3 = repositoryService
        .createDeployment()
        .addDeploymentResourcesById(deployment1.getId(), Arrays.asList(resource1.getId()))
        .addDeploymentResourcesByName(deployment1.getId(), Arrays.asList(resource2.getName()))
        .deploy();

    // then
    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 3);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 3);

    deleteDeployments(deployment1, deployment2, deployment3);
  }

  public void testRedeployFormDifferentDeployments() {
    // given
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();

    BpmnModelInstance model1 = createProcessWithServiceTask(PROCESS_1_KEY);

    // first deployment
    Deployment deployment1 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME + "-1")
        .addModelInstance(RESOURCE_1_NAME, model1)
        .deploy();

    assertEquals(1, repositoryService.getDeploymentResources(deployment1.getId()).size());

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 1);

    // second deployment
    BpmnModelInstance model2 = createProcessWithReceiveTask(PROCESS_2_KEY);

    Deployment deployment2 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME + "-2")
        .addModelInstance(RESOURCE_2_NAME, model2)
        .deploy();

    assertEquals(1, repositoryService.getDeploymentResources(deployment2.getId()).size());

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 1);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 1);

    // when
    Deployment deployment3 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME + "-3")
        .addDeploymentResources(deployment1.getId())
        .addDeploymentResources(deployment2.getId())
        .deploy();

    assertEquals(2, repositoryService.getDeploymentResources(deployment3.getId()).size());

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 2);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 2);

    deleteDeployments(deployment1, deployment2, deployment3);
  }

  public void testRedeployFormDifferentDeploymentsById() {
    // given
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();

    BpmnModelInstance model1 = createProcessWithServiceTask(PROCESS_1_KEY);

    // first deployment
    Deployment deployment1 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME + "-1")
        .addModelInstance(RESOURCE_1_NAME, model1)
        .deploy();

    assertEquals(1, repositoryService.getDeploymentResources(deployment1.getId()).size());
    Resource resource1 = getResourceByName(deployment1.getId(), RESOURCE_1_NAME);

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 1);

    // second deployment
    BpmnModelInstance model2 = createProcessWithReceiveTask(PROCESS_2_KEY);

    Deployment deployment2 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME + "-2")
        .addModelInstance(RESOURCE_2_NAME, model2)
        .deploy();

    assertEquals(1, repositoryService.getDeploymentResources(deployment2.getId()).size());
    Resource resource2 = getResourceByName(deployment2.getId(), RESOURCE_2_NAME);

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 1);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 1);

    // when
    Deployment deployment3 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME + "-3")
        .addDeploymentResourceById(deployment1.getId(), resource1.getId())
        .addDeploymentResourceById(deployment2.getId(), resource2.getId())
        .deploy();

    assertEquals(2, repositoryService.getDeploymentResources(deployment3.getId()).size());

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 2);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 2);

    deleteDeployments(deployment1, deployment2, deployment3);
  }

  public void testRedeployFormDifferentDeploymentsByName() {
    // given
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();

    BpmnModelInstance model1 = createProcessWithServiceTask(PROCESS_1_KEY);

    // first deployment
    Deployment deployment1 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME + "-1")
        .addModelInstance(RESOURCE_1_NAME, model1)
        .deploy();

    assertEquals(1, repositoryService.getDeploymentResources(deployment1.getId()).size());

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 1);

    // second deployment
    BpmnModelInstance model2 = createProcessWithReceiveTask(PROCESS_2_KEY);

    Deployment deployment2 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME + "-2")
        .addModelInstance(RESOURCE_2_NAME, model2)
        .deploy();

    assertEquals(1, repositoryService.getDeploymentResources(deployment2.getId()).size());

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 1);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 1);

    // when
    Deployment deployment3 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME + "-3")
        .addDeploymentResourceByName(deployment1.getId(), RESOURCE_1_NAME)
        .addDeploymentResourceByName(deployment2.getId(), RESOURCE_2_NAME)
        .deploy();

    assertEquals(2, repositoryService.getDeploymentResources(deployment3.getId()).size());

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 2);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 2);

    deleteDeployments(deployment1, deployment2, deployment3);
  }

  public void testRedeployFormDifferentDeploymentsByNameAndId() {
    // given
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();

    BpmnModelInstance model1 = createProcessWithServiceTask(PROCESS_1_KEY);

    // first deployment
    Deployment deployment1 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME + "-1")
        .addModelInstance(RESOURCE_1_NAME, model1)
        .deploy();

    assertEquals(1, repositoryService.getDeploymentResources(deployment1.getId()).size());
    Resource resource1 = getResourceByName(deployment1.getId(), RESOURCE_1_NAME);

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 1);

    // second deployment
    BpmnModelInstance model2 = createProcessWithReceiveTask(PROCESS_2_KEY);

    Deployment deployment2 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME + "-2")
        .addModelInstance(RESOURCE_2_NAME, model2)
        .deploy();

    assertEquals(1, repositoryService.getDeploymentResources(deployment2.getId()).size());

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 1);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 1);

    // when
    Deployment deployment3 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME + "-3")
        .addDeploymentResourceById(deployment1.getId(), resource1.getId())
        .addDeploymentResourceByName(deployment2.getId(), RESOURCE_2_NAME)
        .deploy();

    assertEquals(2, repositoryService.getDeploymentResources(deployment3.getId()).size());

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 2);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 2);

    deleteDeployments(deployment1, deployment2, deployment3);
  }

  public void testRedeployFormDifferentDeploymentsAddsNewSource() {
    // given
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();

    BpmnModelInstance model1 = createProcessWithServiceTask(PROCESS_1_KEY);

    // first deployment
    Deployment deployment1 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME + "-1")
        .addModelInstance(RESOURCE_1_NAME, model1)
        .deploy();

    assertEquals(1, repositoryService.getDeploymentResources(deployment1.getId()).size());

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 1);

    // second deployment
    BpmnModelInstance model2 = createProcessWithReceiveTask(PROCESS_2_KEY);

    Deployment deployment2 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME + "-2")
        .addModelInstance(RESOURCE_2_NAME, model2)
        .deploy();

    assertEquals(1, repositoryService.getDeploymentResources(deployment2.getId()).size());

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 1);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 1);

    // when
    BpmnModelInstance model3 = createProcessWithUserTask(PROCESS_3_KEY);
    Deployment deployment3 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME + "-3")
        .addDeploymentResources(deployment1.getId())
        .addDeploymentResources(deployment2.getId())
        .addModelInstance(RESOURCE_3_NAME, model3)
        .deploy();

    assertEquals(3, repositoryService.getDeploymentResources(deployment3.getId()).size());

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 2);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 2);
    verifyQueryResults(query.processDefinitionKey(PROCESS_3_KEY), 1);

    deleteDeployments(deployment1, deployment2, deployment3);
  }

  public void testRedeployFormDifferentDeploymentsSameResourceName() {
    // given
    BpmnModelInstance model1 = createProcessWithServiceTask(PROCESS_1_KEY);

    // first deployment
    Deployment deployment1 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME + "-1")
        .addModelInstance(RESOURCE_1_NAME, model1)
        .deploy();

    // second deployment
    BpmnModelInstance model2 = createProcessWithReceiveTask(PROCESS_2_KEY);

    Deployment deployment2 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME + "-2")
        .addModelInstance(RESOURCE_1_NAME, model2)
        .deploy();

    // when
    try {
      repositoryService
          .createDeployment()
          .name(DEPLOYMENT_NAME + "-3")
          .addDeploymentResources(deployment1.getId())
          .addDeploymentResources(deployment2.getId())
          .deploy();
      fail("It should not be possible to deploy different resources with same name.");
    } catch (NotValidException e) {
      // expected
    }

    deleteDeployments(deployment1, deployment2);
  }

  public void testRedeployAndAddNewResourceWithSameName() {
    // given
    BpmnModelInstance model1 = createProcessWithServiceTask(PROCESS_1_KEY);

    Deployment deployment1 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME + "-1")
        .addModelInstance(RESOURCE_1_NAME, model1)
        .deploy();

    // when
    BpmnModelInstance model2 = createProcessWithReceiveTask(PROCESS_2_KEY);

    try {
      repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME + "-2")
        .addModelInstance(RESOURCE_1_NAME, model2)
        .addDeploymentResourceByName(deployment1.getId(), RESOURCE_1_NAME)
        .deploy();
      fail("It should not be possible to deploy different resources with same name.");
    } catch (NotValidException e) {
      // expected
    }

    deleteDeployments(deployment1);
  }

  public void testRedeployEnableDuplcateChecking() {
    // given
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();

    BpmnModelInstance model1 = createProcessWithServiceTask(PROCESS_1_KEY);
    Deployment deployment1 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addModelInstance(RESOURCE_1_NAME, model1)
        .deploy();

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 1);

    // when
    Deployment deployment2 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResources(deployment1.getId())
        .enableDuplicateFiltering(true)
        .deploy();

    assertEquals(deployment1.getId(), deployment2.getId());

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 1);

    deleteDeployments(deployment1);
  }

  public void testSimpleProcessApplicationDeployment() {
    // given
    EmbeddedProcessApplication processApplication = new EmbeddedProcessApplication();

    BpmnModelInstance model = createProcessWithServiceTask(PROCESS_KEY);
    ProcessApplicationDeployment deployment1 = repositoryService.createDeployment(processApplication.getReference())
        .name(DEPLOYMENT_NAME)
        .addModelInstance(RESOURCE_NAME, model)
        .enableDuplicateFiltering(true)
        .deploy();

    Resource resource1 = getResourceByName(deployment1.getId(), RESOURCE_NAME);

    // when
    ProcessApplicationDeployment deployment2 = repositoryService
        .createDeployment(processApplication.getReference())
        .name(DEPLOYMENT_NAME)
        .addDeploymentResourceById(deployment1.getId(), resource1.getId())
        .deploy();

    // then
    // registration was performed:
    ProcessApplicationRegistration registration = deployment2.getProcessApplicationRegistration();
    Set<String> deploymentIds = registration.getDeploymentIds();
    assertEquals(1, deploymentIds.size());
    assertTrue(deploymentIds.contains(deployment2.getId()));

    deleteDeployments(deployment1, deployment2);
  }

  public void testRedeployProcessApplicationDeploymentResumePreviousVersions() {
    // given
    EmbeddedProcessApplication processApplication = new EmbeddedProcessApplication();

    // first deployment
    BpmnModelInstance model = createProcessWithServiceTask(PROCESS_KEY);
    ProcessApplicationDeployment deployment1 = repositoryService.createDeployment(processApplication.getReference())
        .name(DEPLOYMENT_NAME)
        .addModelInstance(RESOURCE_NAME, model)
        .enableDuplicateFiltering(true)
        .deploy();

    Resource resource1 = getResourceByName(deployment1.getId(), RESOURCE_NAME);

    // second deployment
    model = createProcessWithUserTask(PROCESS_KEY);
    ProcessApplicationDeployment deployment2 = repositoryService.createDeployment(processApplication.getReference())
        .name(DEPLOYMENT_NAME)
        .addModelInstance(RESOURCE_NAME, model)
        .enableDuplicateFiltering(true)
        .deploy();

    // when
    ProcessApplicationDeployment deployment3 = repositoryService
        .createDeployment(processApplication.getReference())
        .name(DEPLOYMENT_NAME)
        .resumePreviousVersions()
        .addDeploymentResourceById(deployment1.getId(), resource1.getId())
        .deploy();

    // then
    // old deployments was resumed
    ProcessApplicationRegistration registration = deployment3.getProcessApplicationRegistration();
    Set<String> deploymentIds = registration.getDeploymentIds();
    assertEquals(3, deploymentIds.size());

    deleteDeployments(deployment1, deployment2, deployment3);
  }

  public void testProcessApplicationDeploymentResumePreviousVersionsByDeploymentName() {
    // given
    EmbeddedProcessApplication processApplication = new EmbeddedProcessApplication();

    // first deployment
    BpmnModelInstance model = createProcessWithServiceTask(PROCESS_KEY);
    ProcessApplicationDeployment deployment1 = repositoryService.createDeployment(processApplication.getReference())
        .name(DEPLOYMENT_NAME)
        .addModelInstance(RESOURCE_NAME, model)
        .enableDuplicateFiltering(true)
        .deploy();

    Resource resource1 = getResourceByName(deployment1.getId(), RESOURCE_NAME);

    // second deployment
    model = createProcessWithUserTask(PROCESS_KEY);
    ProcessApplicationDeployment deployment2 = repositoryService.createDeployment(processApplication.getReference())
        .name(DEPLOYMENT_NAME)
        .addModelInstance(RESOURCE_NAME, model)
        .enableDuplicateFiltering(true)
        .deploy();

    // when
    ProcessApplicationDeployment deployment3 = repositoryService
        .createDeployment(processApplication.getReference())
        .name(DEPLOYMENT_NAME)
        .resumePreviousVersions()
        .resumePreviousVersionsBy(ResumePreviousBy.RESUME_BY_DEPLOYMENT_NAME)
        .addDeploymentResourceById(deployment1.getId(), resource1.getId())
        .deploy();

    // then
    // old deployment was resumed
    ProcessApplicationRegistration registration = deployment3.getProcessApplicationRegistration();
    Set<String> deploymentIds = registration.getDeploymentIds();
    assertEquals(3, deploymentIds.size());

    deleteDeployments(deployment1, deployment2, deployment3);
  }

  // helper ///////////////////////////////////////////////////////////

  protected void verifyQueryResults(Query<?, ?> query, int countExpected) {
    assertEquals(countExpected, query.count());
  }

  protected Resource getResourceByName(String deploymentId, String resourceName) {
    List<Resource> resources = repositoryService.getDeploymentResources(deploymentId);

    for (Resource resource : resources) {
      if (resource.getName().equals(resourceName)) {
        return resource;
      }
    }

    return null;
  }

  protected void deleteDeployments(Deployment... deployments){
    for (Deployment deployment : deployments) {
      repositoryService.deleteDeployment(deployment.getId(), true);
    }
  }

  protected BpmnModelInstance createProcessWithServiceTask(String key) {
    return Bpmn.createExecutableProcess(key)
      .startEvent()
      .serviceTask()
        .camundaExpression("${true}")
      .endEvent()
    .done();
  }

  protected BpmnModelInstance createProcessWithUserTask(String key) {
    return Bpmn.createExecutableProcess(key)
      .startEvent()
      .userTask()
      .endEvent()
    .done();
  }

  protected BpmnModelInstance createProcessWithReceiveTask(String key) {
    return Bpmn.createExecutableProcess(key)
      .startEvent()
      .receiveTask()
      .endEvent()
    .done();
  }

  protected BpmnModelInstance createProcessWithScriptTask(String key) {
    return Bpmn.createExecutableProcess(key)
      .startEvent()
      .scriptTask()
        .scriptFormat("javascript")
        .scriptText("return true")
      .userTask()
      .endEvent()
    .done();
  }

}
