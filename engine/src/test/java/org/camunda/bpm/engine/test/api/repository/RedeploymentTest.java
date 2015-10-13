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
        .createRedeployment("not-existing")
        .redeploy();
      fail("It should not be able to re-deploy an unexisting deployment");
    } catch (NotFoundException e) {
      // expected
    }

    try {
      repositoryService
        .createRedeployment(null)
        .redeploy();
      fail("It should not be able to re-deploy an unexisting deployment");
    } catch (NotValidException e) {
      // expected
    }
  }

  public void testRedeployUnexistingDeploymentResource() {
    // given
    BpmnModelInstance model = createProcessWithServiceTask(PROCESS_KEY);

    Deployment deployment = repositoryService
        .createDeployment()
        .addModelInstance(RESOURCE_NAME, model)
        .deploy();

    try {
      // when
      repositoryService
        .createRedeployment(deployment.getId())
        .addResourceName("not-existing-resource.bpmn")
        .redeploy();
      fail("It should not be possible to re-deploy a not existing deployment resource");
    } catch (NotFoundException e) {
      // then
      // expected
    }

    try {
      // when
      repositoryService
        .createRedeployment(deployment.getId())
        .addResourceNames(Arrays.asList("not-existing-resource.bpmn"))
        .redeploy();
      fail("It should not be possible to re-deploy a not existing deployment resource");
    } catch (NotFoundException e) {
      // then
      // expected
    }

    try {
      // when
      repositoryService
        .createRedeployment(deployment.getId())
        .addResourceId("not-existing-resource-id")
        .redeploy();
      fail("It should not be possible to re-deploy a not existing deployment resource");
    } catch (NotFoundException e) {
      // then
      // expected
    }

    try {
      // when
      repositoryService
        .createRedeployment(deployment.getId())
        .addResourceIds(Arrays.asList("not-existing-resource-id"))
        .redeploy();
      fail("It should not be possible to re-deploy a not existing deployment resource");
    } catch (NotFoundException e) {
      // then
      // expected
    }

    deleteDeployments(deployment);
  }

  public void testRedeployNewDeployment() {
    // given
    BpmnModelInstance model = createProcessWithServiceTask(PROCESS_KEY);

    Deployment deployment1 = repositoryService
        .createDeployment()
        .addModelInstance(RESOURCE_NAME, model)
        .deploy();

    DeploymentQuery query = repositoryService.createDeploymentQuery();

    assertNotNull(deployment1.getId());
    verifyQueryResults(query, 1);

    // when
    Deployment deployment2 = repositoryService
        .createRedeployment(deployment1.getId())
        .redeploy();

    // then
    assertNotNull(deployment2);
    assertNotNull(deployment2.getId());
    assertFalse(deployment1.getId().equals(deployment2.getId()));

    verifyQueryResults(query, 2);

    deleteDeployments(deployment1, deployment2);
  }

  public void testRedeployDeploymentNameProperty() {
    // given
    BpmnModelInstance model = createProcessWithServiceTask(PROCESS_KEY);

    Deployment deployment1 = repositoryService
        .createDeployment()
        .name("my-deployment")
        .addModelInstance(RESOURCE_NAME, model)
        .deploy();

    assertEquals("my-deployment", deployment1.getName());

    // when
    Deployment deployment2 = repositoryService
        .createRedeployment(deployment1.getId())
        .redeploy();

    // then
    assertNotNull(deployment2);
    assertEquals(deployment1.getName(), deployment2.getName());

    deleteDeployments(deployment1, deployment2);
  }

  public void testRedeployDeploymentSourcePropertyNotSet() {
    // given
    BpmnModelInstance model = createProcessWithServiceTask(PROCESS_KEY);

    Deployment deployment1 = repositoryService
        .createDeployment()
        .source("my-deployment-source")
        .addModelInstance(RESOURCE_NAME, model)
        .deploy();

    assertEquals("my-deployment-source", deployment1.getSource());

    // when
    Deployment deployment2 = repositoryService
        .createRedeployment(deployment1.getId())
        .redeploy();

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
        .source("my-deployment-source")
        .addModelInstance(RESOURCE_NAME, model)
        .deploy();

    assertEquals("my-deployment-source", deployment1.getSource());

    // when
    Deployment deployment2 = repositoryService
        .createRedeployment(deployment1.getId())
        .source("my-another-deployment-source")
        .redeploy();

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
        .addModelInstance(RESOURCE_NAME, model)
        .deploy();

    Resource resource1 = getResourceByName(deployment1.getId(), RESOURCE_NAME);

    // second deployment
    model = createProcessWithUserTask(PROCESS_KEY);
    Deployment deployment2 = repositoryService
        .createDeployment()
        .addModelInstance(RESOURCE_NAME, model)
        .deploy();

    Resource resource2 = getResourceByName(deployment2.getId(), RESOURCE_NAME);

    // when
    Deployment deployment3 = repositoryService
        .createRedeployment(deployment1.getId())
        .redeploy();

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
        .addModelInstance(RESOURCE_1_NAME, model1)
        .addModelInstance(RESOURCE_2_NAME, model2)
        .deploy();

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 1);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 1);

    // second deployment
    Deployment deployment2 = repositoryService
        .createDeployment()
        .addModelInstance(RESOURCE_1_NAME, model2)
        .addModelInstance(RESOURCE_2_NAME, model1)
        .deploy();

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 2);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 2);

    // when
    Deployment deployment3 = repositoryService
        .createRedeployment(deployment1.getId())
        .redeploy();

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
        .addModelInstance(RESOURCE_1_NAME, model1)
        .addModelInstance(RESOURCE_2_NAME, model2)
        .deploy();

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 2);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 2);

    // when
    Deployment deployment3 = repositoryService
        .createRedeployment(deployment1.getId())
        .addResourceName(RESOURCE_1_NAME)
        .redeploy();

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
        .addModelInstance(RESOURCE_1_NAME, model1)
        .addModelInstance(RESOURCE_2_NAME, model2)
        .addModelInstance(RESOURCE_3_NAME, model3)
        .deploy();

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 2);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 2);
    verifyQueryResults(query.processDefinitionKey(PROCESS_3_KEY), 2);

    // when (1)
    Deployment deployment3 = repositoryService
        .createRedeployment(deployment1.getId())
        .addResourceName(RESOURCE_1_NAME)
        .addResourceName(RESOURCE_3_NAME)
        .redeploy();

    // then (1)
    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 3);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 2);
    verifyQueryResults(query.processDefinitionKey(PROCESS_3_KEY), 3);

    // when (2)
    Deployment deployment4 = repositoryService
        .createRedeployment(deployment2.getId())
        .addResourceNames(Arrays.asList(RESOURCE_1_NAME, RESOURCE_3_NAME))
        .redeploy();

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
        .addModelInstance(RESOURCE_1_NAME, model1)
        .addModelInstance(RESOURCE_2_NAME, model2)
        .addModelInstance(RESOURCE_3_NAME, model3)
        .deploy();

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 2);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 2);
    verifyQueryResults(query.processDefinitionKey(PROCESS_3_KEY), 2);

    // when
    Deployment deployment3 = repositoryService
        .createRedeployment(deployment1.getId())
        .addResourceName(RESOURCE_1_NAME)
        .addResourceNames(Arrays.asList(RESOURCE_2_NAME, RESOURCE_3_NAME))
        .redeploy();

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
        .addModelInstance(RESOURCE_1_NAME, model1)
        .addModelInstance(RESOURCE_2_NAME, model2)
        .deploy();

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 2);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 2);

    // when
    Deployment deployment3 = repositoryService
        .createRedeployment(deployment1.getId())
        .addResourceName(RESOURCE_1_NAME)
        .addResourceNames(Arrays.asList(RESOURCE_1_NAME))
        .redeploy();

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
        .addModelInstance(RESOURCE_1_NAME, model1)
        .addModelInstance(RESOURCE_2_NAME, model2)
        .deploy();

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 2);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 2);

    // when
    Deployment deployment3 = repositoryService
        .createRedeployment(deployment1.getId())
        .addResourceId(resource.getId())
        .redeploy();

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
        .createRedeployment(deployment1.getId())
        .addResourceId(resource11.getId())
        .addResourceId(resource13.getId())
        .redeploy();

    // then (1)
    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 3);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 2);
    verifyQueryResults(query.processDefinitionKey(PROCESS_3_KEY), 3);

    // when (2)
    Deployment deployment4 = repositoryService
        .createRedeployment(deployment2.getId())
        .addResourceIds(Arrays.asList(resource21.getId(), resource23.getId()))
        .redeploy();

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
        .addModelInstance(RESOURCE_1_NAME, model1)
        .addModelInstance(RESOURCE_2_NAME, model2)
        .addModelInstance(RESOURCE_3_NAME, model3)
        .deploy();

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 2);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 2);
    verifyQueryResults(query.processDefinitionKey(PROCESS_3_KEY), 2);

    // when
    Deployment deployment3 = repositoryService
        .createRedeployment(deployment1.getId())
        .addResourceId(resource1.getId())
        .addResourceIds(Arrays.asList(resource2.getId(), resource3.getId()))
        .redeploy();

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
        .addModelInstance(RESOURCE_1_NAME, model1)
        .addModelInstance(RESOURCE_2_NAME, model2)
        .deploy();

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 2);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 2);

    // when
    Deployment deployment3 = repositoryService
        .createRedeployment(deployment1.getId())
        .addResourceId(resource1.getId())
        .addResourceIds(Arrays.asList(resource1.getId()))
        .redeploy();

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
        .addModelInstance(RESOURCE_1_NAME, model1)
        .addModelInstance(RESOURCE_2_NAME, model2)
        .deploy();

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 2);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 2);

    // when
    Deployment deployment3 = repositoryService
        .createRedeployment(deployment1.getId())
        .addResourceId(resource1.getId())
        .addResourceName(resource2.getName())
        .redeploy();

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
        .addModelInstance(RESOURCE_1_NAME, model1)
        .addModelInstance(RESOURCE_2_NAME, model2)
        .deploy();

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 2);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 2);

    // when
    Deployment deployment3 = repositoryService
        .createRedeployment(deployment1.getId())
        .addResourceIds(Arrays.asList(resource1.getId()))
        .addResourceNames(Arrays.asList(resource2.getName()))
        .redeploy();

    // then
    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 3);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 3);

    deleteDeployments(deployment1, deployment2, deployment3);
  }

  public void testSimpleProcessApplicationDeployment() {
    // given
    EmbeddedProcessApplication processApplication = new EmbeddedProcessApplication();

    BpmnModelInstance model = createProcessWithServiceTask(PROCESS_KEY);
    ProcessApplicationDeployment deployment1 = repositoryService.createDeployment(processApplication.getReference())
      .name("deployment")
      .addModelInstance(RESOURCE_NAME, model)
      .enableDuplicateFiltering(true)
      .deploy();

    Resource resource1 = getResourceByName(deployment1.getId(), RESOURCE_NAME);

    // when
    ProcessApplicationDeployment deployment2 = repositoryService.createRedeployment(deployment1.getId(), processApplication.getReference())
        .addResourceId(resource1.getId())
        .redeploy();

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
      .name("deployment")
      .addModelInstance(RESOURCE_NAME, model)
      .enableDuplicateFiltering(true)
      .deploy();

    Resource resource1 = getResourceByName(deployment1.getId(), RESOURCE_NAME);

    // second deployment
    model = createProcessWithUserTask(PROCESS_KEY);
    ProcessApplicationDeployment deployment2 = repositoryService.createDeployment(processApplication.getReference())
      .name("deployment")
      .addModelInstance(RESOURCE_NAME, model)
      .enableDuplicateFiltering(true)
      .deploy();

    // when
    ProcessApplicationDeployment deployment3 = repositoryService.createRedeployment(deployment1.getId(), processApplication.getReference())
        .resumePreviousVersions()
        .addResourceId(resource1.getId())
        .redeploy();

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
      .name("deployment")
      .addModelInstance(RESOURCE_NAME, model)
      .enableDuplicateFiltering(true)
      .deploy();

    Resource resource1 = getResourceByName(deployment1.getId(), RESOURCE_NAME);

    // second deployment
    model = createProcessWithUserTask(PROCESS_KEY);
    ProcessApplicationDeployment deployment2 = repositoryService.createDeployment(processApplication.getReference())
      .name("deployment")
      .addModelInstance(RESOURCE_NAME, model)
      .enableDuplicateFiltering(true)
      .deploy();

    // when
    ProcessApplicationDeployment deployment3 = repositoryService.createRedeployment(deployment1.getId(), processApplication.getReference())
        .resumePreviousVersions()
        .resumePreviousVersionsBy(ResumePreviousBy.RESUME_BY_DEPLOYMENT_NAME)
        .addResourceId(resource1.getId())
        .redeploy();


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
