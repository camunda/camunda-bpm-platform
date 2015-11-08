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
package org.camunda.bpm.application.impl.deployment;

import org.camunda.bpm.application.ProcessApplicationReference;
import org.camunda.bpm.application.impl.EmbeddedProcessApplication;
import org.camunda.bpm.engine.impl.application.ProcessApplicationManager;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cmmn.entity.repository.CaseDefinitionEntity;
import org.camunda.bpm.engine.impl.context.ProcessApplicationContextUtil;
import org.camunda.bpm.engine.impl.dmn.entity.repository.DecisionDefinitionEntity;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.deploy.DeploymentCache;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.repository.CaseDefinition;
import org.camunda.bpm.engine.repository.DecisionDefinition;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

/**
 * @author Roman Smirnov
 *
 */
public class RedeploymentRegistrationTest extends PluggableProcessEngineTestCase {

  protected static final String DEPLOYMENT_NAME = "my-deployment";

  protected static final String PROCESS_KEY_1 = "process-1";
  protected static final String PROCESS_KEY_2 = "process-2";

  protected static final String BPMN_RESOURCE_1 = "path/to/my/process1.bpmn";
  protected static final String BPMN_RESOURCE_2 = "path/to/my/process2.bpmn";

  protected static final String CASE_KEY_1 = "oneTaskCase";
  protected static final String CASE_KEY_2 = "twoTaskCase";

  protected static final String CMMN_RESOURCE_1 = "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn";
  protected static final String CMMN_RESOURCE_2 = "org/camunda/bpm/engine/test/api/cmmn/twoTaskCase.cmmn";

  protected static final String DECISION_KEY_1 = "decision";
  protected static final String DECISION_KEY_2 = "anotherDecision";

  protected static final String DMN_RESOURCE_1 = "org/camunda/bpm/engine/test/api/dmn/Example.dmn";
  protected static final String DMN_RESOURCE_2 = "org/camunda/bpm/engine/test/api/dmn/Another_Example.dmn";

  protected EmbeddedProcessApplication processApplication;

  protected void setUp() throws Exception {
    processApplication = new EmbeddedProcessApplication();
  }

  public void testProcessRegistrationNotFoundByDeploymentId() {
    // given
    ProcessApplicationReference reference = processApplication.getReference();

    Deployment deployment1 = repositoryService
      .createDeployment(reference)
      .name(DEPLOYMENT_NAME)
      .addModelInstance(BPMN_RESOURCE_1, createProcessWithServiceTask(PROCESS_KEY_1))
      .deploy();

    assertEquals(reference, getProcessApplicationForDeployment(deployment1.getId()));

    // when
    Deployment deployment2 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResources(deployment1.getId())
        .deploy();

    // then
    assertNull(getProcessApplicationForDeployment(deployment2.getId()));

    deleteDeployments(deployment1, deployment2);
  }

  public void testProcessRegistrationNotFoundByDefinition() {
    // given

    // first deployment
    Deployment deployment1 = repositoryService
      .createDeployment()
      .name(DEPLOYMENT_NAME)
      .addModelInstance(BPMN_RESOURCE_1, createProcessWithServiceTask(PROCESS_KEY_1))
      .deploy();

    // second deployment
    Deployment deployment2 = repositoryService
      .createDeployment()
      .name(DEPLOYMENT_NAME)
      .addModelInstance(BPMN_RESOURCE_1, createProcessWithServiceTask(PROCESS_KEY_1))
      .deploy();

    // when
    Deployment deployment3 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResources(deployment1.getId())
        .deploy();

    String definitionId = queryLatestProcessDefinitionByKey(PROCESS_KEY_1).getId();

    // then
    assertNull(getProcessApplicationForProcessDefinition(definitionId));

    deleteDeployments(deployment1, deployment2, deployment3);
  }

  public void testProcessRegistrationFoundByDeploymentId() {
    // given
    ProcessApplicationReference reference1 = processApplication.getReference();

    Deployment deployment1 = repositoryService
      .createDeployment(reference1)
      .name(DEPLOYMENT_NAME)
      .addModelInstance(BPMN_RESOURCE_1, createProcessWithServiceTask(PROCESS_KEY_1))
      .deploy();

    assertEquals(reference1, getProcessApplicationForDeployment(deployment1.getId()));

    // when
    ProcessApplicationReference reference2 = processApplication.getReference();

    Deployment deployment2 = repositoryService
        .createDeployment(reference2)
        .name(DEPLOYMENT_NAME)
        .addDeploymentResources(deployment1.getId())
        .deploy();

    // then
    assertEquals(reference2, getProcessApplicationForDeployment(deployment2.getId()));

    deleteDeployments(deployment1, deployment2);
  }

  public void testProcessRegistrationFoundFromPreviousDefinition() {
    // given
    ProcessApplicationReference reference = processApplication.getReference();
    Deployment deployment1 = repositoryService
      .createDeployment(reference)
      .name(DEPLOYMENT_NAME)
      .addModelInstance(BPMN_RESOURCE_1, createProcessWithServiceTask(PROCESS_KEY_1))
      .deploy();

    // when
    Deployment deployment2 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResources(deployment1.getId())
        .deploy();

    String definitionId = queryLatestProcessDefinitionByKey(PROCESS_KEY_1).getId();

    // then
    assertEquals(reference, getProcessApplicationForProcessDefinition(definitionId));

    // and the reference is not cached
    assertNull(getProcessApplicationForDeployment(deployment2.getId()));

    deleteDeployments(deployment1, deployment2);
  }

  public void testProcessRegistrationFoundFromLatestDeployment() {
    // given
    ProcessApplicationReference reference1 = processApplication.getReference();
    Deployment deployment1 = repositoryService
      .createDeployment(reference1)
      .name(DEPLOYMENT_NAME)
      .addModelInstance(BPMN_RESOURCE_1, createProcessWithServiceTask(PROCESS_KEY_1))
      .deploy();

    // when
    ProcessApplicationReference reference2 = processApplication.getReference();
    Deployment deployment2 = repositoryService
        .createDeployment(reference2)
        .name(DEPLOYMENT_NAME)
        .addDeploymentResources(deployment1.getId())
        .deploy();

    String definitionId = queryLatestProcessDefinitionByKey(PROCESS_KEY_1).getId();

    // then
    assertEquals(reference2, getProcessApplicationForProcessDefinition(definitionId));
    assertEquals(reference2, getProcessApplicationForDeployment(deployment2.getId()));

    deleteDeployments(deployment1, deployment2);
  }

  public void testProcessRegistrationFoundOnlyForOneProcessDefinition() {
    // given

    // first deployment
    Deployment deployment1 = repositoryService
      .createDeployment()
      .name(DEPLOYMENT_NAME)
      .addModelInstance(BPMN_RESOURCE_1, createProcessWithServiceTask(PROCESS_KEY_1))
      .addModelInstance(BPMN_RESOURCE_2, createProcessWithServiceTask(PROCESS_KEY_2))
      .deploy();

    // second deployment
    ProcessApplicationReference reference2 = processApplication.getReference();
    Deployment deployment2 = repositoryService
        .createDeployment(reference2)
        .name(DEPLOYMENT_NAME)
        .addModelInstance(BPMN_RESOURCE_1, createProcessWithServiceTask(PROCESS_KEY_1))
        .deploy();

    // when
    Deployment deployment3 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResources(deployment1.getId())
        .deploy();

    String firstDefinitionId = queryLatestProcessDefinitionByKey(PROCESS_KEY_1).getId();
    String secondDefinitionId = queryLatestProcessDefinitionByKey(PROCESS_KEY_2).getId();

    // then
    assertEquals(reference2, getProcessApplicationForProcessDefinition(firstDefinitionId));
    assertNull(getProcessApplicationForProcessDefinition(secondDefinitionId));

    deleteDeployments(deployment1, deployment2, deployment3);
  }

  public void testProcessRegistrationFoundFromDifferentDeployment() {
    // given

    // first deployment
    ProcessApplicationReference reference1 = processApplication.getReference();
    Deployment deployment1 = repositoryService
      .createDeployment(reference1)
      .name(DEPLOYMENT_NAME)
      .addModelInstance(BPMN_RESOURCE_1, createProcessWithServiceTask(PROCESS_KEY_1))
      .addModelInstance(BPMN_RESOURCE_2, createProcessWithServiceTask(PROCESS_KEY_2))
      .deploy();

    // second deployment
    ProcessApplicationReference reference2 = processApplication.getReference();
    Deployment deployment2 = repositoryService
        .createDeployment(reference2)
        .name(DEPLOYMENT_NAME)
        .addModelInstance(BPMN_RESOURCE_1, createProcessWithServiceTask(PROCESS_KEY_1))
        .deploy();

    // when
    Deployment deployment3 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResources(deployment1.getId())
        .deploy();

    String firstDefinitionId = queryLatestProcessDefinitionByKey(PROCESS_KEY_1).getId();
    String secondDefinitionId = queryLatestProcessDefinitionByKey(PROCESS_KEY_2).getId();

    // then
    assertEquals(reference2, getProcessApplicationForProcessDefinition(firstDefinitionId));
    assertEquals(reference1, getProcessApplicationForProcessDefinition(secondDefinitionId));

    deleteDeployments(deployment1, deployment2, deployment3);
  }

  public void testProcessRegistrationFoundFromSameDeployment() {
    // given

    // first deployment
    ProcessApplicationReference reference1 = processApplication.getReference();
    Deployment deployment1 = repositoryService
      .createDeployment(reference1)
      .name(DEPLOYMENT_NAME)
      .addModelInstance(BPMN_RESOURCE_1, createProcessWithServiceTask(PROCESS_KEY_1))
      .addModelInstance(BPMN_RESOURCE_2, createProcessWithServiceTask(PROCESS_KEY_2))
      .deploy();

    // second deployment
    Deployment deployment2 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addModelInstance(BPMN_RESOURCE_1, createProcessWithServiceTask(PROCESS_KEY_1))
        .deploy();

    Deployment deployment3 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addModelInstance(BPMN_RESOURCE_2, createProcessWithServiceTask(PROCESS_KEY_2))
        .deploy();

    // when
    Deployment deployment4 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResources(deployment1.getId())
        .deploy();

    String firstDefinitionId = queryLatestProcessDefinitionByKey(PROCESS_KEY_1).getId();
    String secondDefinitionId = queryLatestProcessDefinitionByKey(PROCESS_KEY_2).getId();

    // then
    assertEquals(reference1, getProcessApplicationForProcessDefinition(firstDefinitionId));
    assertEquals(reference1, getProcessApplicationForProcessDefinition(secondDefinitionId));

    deleteDeployments(deployment1, deployment2, deployment3, deployment4);
  }

  public void testProcessRegistrationFoundFromDifferentDeployments() {
    // given

    // first deployment
    ProcessApplicationReference reference1 = processApplication.getReference();
    Deployment deployment1 = repositoryService
      .createDeployment(reference1)
      .name(DEPLOYMENT_NAME + "-1")
      .addModelInstance(BPMN_RESOURCE_1, createProcessWithServiceTask(PROCESS_KEY_1))
      .deploy();

    // second deployment
    ProcessApplicationReference reference2 = processApplication.getReference();
    Deployment deployment2 = repositoryService
        .createDeployment(reference2)
        .name(DEPLOYMENT_NAME + "-2")
        .addModelInstance(BPMN_RESOURCE_2, createProcessWithServiceTask(PROCESS_KEY_2))
        .deploy();

    // when
    Deployment deployment3 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResources(deployment1.getId())
        .deploy();

    String firstDefinitionId = queryLatestProcessDefinitionByKey(PROCESS_KEY_1).getId();
    String secondDefinitionId = queryLatestProcessDefinitionByKey(PROCESS_KEY_2).getId();

    // then
    assertEquals(reference1, getProcessApplicationForProcessDefinition(firstDefinitionId));
    assertEquals(reference2, getProcessApplicationForProcessDefinition(secondDefinitionId));

    deleteDeployments(deployment1, deployment2, deployment3);
  }

  public void testProcessRegistrationNotFoundWhenDeletingDeployment() {
    // given

    // first deployment
    Deployment deployment1 = repositoryService
      .createDeployment()
      .name(DEPLOYMENT_NAME)
      .addModelInstance(BPMN_RESOURCE_1, createProcessWithServiceTask(PROCESS_KEY_1))
      .deploy();

    // second deployment
    ProcessApplicationReference reference2 = processApplication.getReference();
    Deployment deployment2 = repositoryService
        .createDeployment(reference2)
        .name(DEPLOYMENT_NAME)
        .addDeploymentResources(deployment1.getId())
        .deploy();

    // when (1)
    // third deployment
    Deployment deployment3 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResources(deployment1.getId())
        .deploy();

    String firstDefinitionId = queryLatestProcessDefinitionByKey(PROCESS_KEY_1).getId();

    // then (1)
    assertEquals(reference2, getProcessApplicationForProcessDefinition(firstDefinitionId));

    // when (2)
    deleteDeployments(deployment2);

    // then (2)
    assertNull(getProcessApplicationForProcessDefinition(firstDefinitionId));

    deleteDeployments(deployment1, deployment3);
  }

  public void testProcessRegistrationFoundAfterDiscardingDeploymentCache() {
    // given

    // first deployment
    Deployment deployment1 = repositoryService
      .createDeployment()
      .name(DEPLOYMENT_NAME)
      .addModelInstance(BPMN_RESOURCE_1, createProcessWithServiceTask(PROCESS_KEY_1))
      .deploy();

    // second deployment
    ProcessApplicationReference reference2 = processApplication.getReference();
    Deployment deployment2 = repositoryService
        .createDeployment(reference2)
        .name(DEPLOYMENT_NAME)
        .addDeploymentResources(deployment1.getId())
        .deploy();

    // when (1)
    // third deployment
    Deployment deployment3 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResources(deployment1.getId())
        .deploy();

    String firstDefinitionId = queryLatestProcessDefinitionByKey(PROCESS_KEY_1).getId();

    // then (1)
    assertEquals(reference2, getProcessApplicationForProcessDefinition(firstDefinitionId));

    // when (2)
    processEngineConfiguration.getDeploymentCache().discardProcessDefinitionCache();

    // then (2)
    assertEquals(reference2, getProcessApplicationForProcessDefinition(firstDefinitionId));

    deleteDeployments(deployment1, deployment2, deployment3);
  }

  public void testCaseRegistrationNotFoundByDeploymentId() {
    // given
    ProcessApplicationReference reference = processApplication.getReference();

    Deployment deployment1 = repositoryService
      .createDeployment(reference)
      .name(DEPLOYMENT_NAME)
      .addClasspathResource(CMMN_RESOURCE_1)
      .deploy();

    assertEquals(reference, getProcessApplicationForDeployment(deployment1.getId()));

    // when
    Deployment deployment2 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResources(deployment1.getId())
        .deploy();

    // then
    assertNull(getProcessApplicationForDeployment(deployment2.getId()));

    deleteDeployments(deployment1, deployment2);
  }

  public void testCaseRegistrationNotFoundByDefinition() {
    // given

    // first deployment
    Deployment deployment1 = repositoryService
      .createDeployment()
      .name(DEPLOYMENT_NAME)
      .addClasspathResource(CMMN_RESOURCE_1)
      .deploy();

    // first deployment
    Deployment deployment2 = repositoryService
      .createDeployment()
      .name(DEPLOYMENT_NAME)
      .addClasspathResource(CMMN_RESOURCE_1)
      .deploy();

    // when
    Deployment deployment3 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResources(deployment1.getId())
        .deploy();

    String definitionId = queryLatestCaseDefinitionByKey(CASE_KEY_1).getId();

    // then
    assertNull(getProcessApplicationForCaseDefinition(definitionId));

    deleteDeployments(deployment1, deployment2, deployment3);
  }

  public void testCaseRegistrationFoundByDeploymentId() {
    // given
    ProcessApplicationReference reference1 = processApplication.getReference();

    Deployment deployment1 = repositoryService
        .createDeployment(reference1)
        .name(DEPLOYMENT_NAME)
        .addClasspathResource(CMMN_RESOURCE_1)
        .deploy();

    assertEquals(reference1, getProcessApplicationForDeployment(deployment1.getId()));

    // when
    ProcessApplicationReference reference2 = processApplication.getReference();

    Deployment deployment2 = repositoryService
        .createDeployment(reference2)
        .name(DEPLOYMENT_NAME)
        .addDeploymentResources(deployment1.getId())
        .deploy();

    // then
    assertEquals(reference2, getProcessApplicationForDeployment(deployment2.getId()));

    deleteDeployments(deployment1, deployment2);
  }

  public void testCaseRegistrationFoundFromPreviousDefinition() {
    // given
    ProcessApplicationReference reference = processApplication.getReference();
    Deployment deployment1 = repositoryService
      .createDeployment(reference)
      .name(DEPLOYMENT_NAME)
      .addClasspathResource(CMMN_RESOURCE_1)
      .deploy();

    // when
    Deployment deployment2 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResources(deployment1.getId())
        .deploy();

    String definitionId = queryLatestCaseDefinitionByKey(CASE_KEY_1).getId();

    // then
    assertEquals(reference, getProcessApplicationForCaseDefinition(definitionId));

    // and the reference is not cached
    assertNull(getProcessApplicationForDeployment(deployment2.getId()));

    deleteDeployments(deployment1, deployment2);
  }

  public void testCaseRegistrationFoundFromLatestDeployment() {
    // given
    ProcessApplicationReference reference1 = processApplication.getReference();
    Deployment deployment1 = repositoryService
      .createDeployment(reference1)
      .name(DEPLOYMENT_NAME)
      .addClasspathResource(CMMN_RESOURCE_1)
      .deploy();

    // when
    ProcessApplicationReference reference2 = processApplication.getReference();
    Deployment deployment2 = repositoryService
        .createDeployment(reference2)
        .name(DEPLOYMENT_NAME)
        .addDeploymentResources(deployment1.getId())
        .deploy();

    String definitionId = queryLatestCaseDefinitionByKey(CASE_KEY_1).getId();

    // then
    assertEquals(reference2, getProcessApplicationForCaseDefinition(definitionId));
    assertEquals(reference2, getProcessApplicationForDeployment(deployment2.getId()));

    deleteDeployments(deployment1, deployment2);
  }

  public void testCaseRegistrationFoundOnlyForOneCaseDefinition() {
    // given

    // first deployment
    Deployment deployment1 = repositoryService
      .createDeployment()
      .name(DEPLOYMENT_NAME)
      .addClasspathResource(CMMN_RESOURCE_1)
      .addClasspathResource(CMMN_RESOURCE_2)
      .deploy();

    // second deployment
    ProcessApplicationReference reference2 = processApplication.getReference();
    Deployment deployment2 = repositoryService
        .createDeployment(reference2)
        .name(DEPLOYMENT_NAME)
        .addClasspathResource(CMMN_RESOURCE_1)
        .deploy();

    // when
    Deployment deployment3 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResources(deployment1.getId())
        .deploy();

    String firstDefinitionId = queryLatestCaseDefinitionByKey(CASE_KEY_1).getId();
    String secondDefinitionId = queryLatestCaseDefinitionByKey(CASE_KEY_2).getId();

    // then
    assertEquals(reference2, getProcessApplicationForCaseDefinition(firstDefinitionId));
    assertNull(getProcessApplicationForCaseDefinition(secondDefinitionId));

    deleteDeployments(deployment1, deployment2, deployment3);
  }

  public void testCaseRegistrationFoundFromDifferentDeployment() {
    // given

    // first deployment
    ProcessApplicationReference reference1 = processApplication.getReference();
    Deployment deployment1 = repositoryService
      .createDeployment(reference1)
      .name(DEPLOYMENT_NAME)
      .addClasspathResource(CMMN_RESOURCE_1)
      .addClasspathResource(CMMN_RESOURCE_2)
      .deploy();

    // second deployment
    ProcessApplicationReference reference2 = processApplication.getReference();
    Deployment deployment2 = repositoryService
        .createDeployment(reference2)
        .name(DEPLOYMENT_NAME)
        .addClasspathResource(CMMN_RESOURCE_1)
        .deploy();

    // when
    Deployment deployment3 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResources(deployment1.getId())
        .deploy();

    String firstDefinitionId = queryLatestCaseDefinitionByKey(CASE_KEY_1).getId();
    String secondDefinitionId = queryLatestCaseDefinitionByKey(CASE_KEY_2).getId();

    // then
    assertEquals(reference2, getProcessApplicationForCaseDefinition(firstDefinitionId));
    assertEquals(reference1, getProcessApplicationForCaseDefinition(secondDefinitionId));

    deleteDeployments(deployment1, deployment2, deployment3);
  }

  public void testCaseRegistrationFoundFromSameDeployment() {
    // given

    // first deployment
    ProcessApplicationReference reference1 = processApplication.getReference();
    Deployment deployment1 = repositoryService
        .createDeployment(reference1)
        .name(DEPLOYMENT_NAME)
        .addClasspathResource(CMMN_RESOURCE_1)
        .addClasspathResource(CMMN_RESOURCE_2)
        .deploy();

    // second deployment
    Deployment deployment2 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addClasspathResource(CMMN_RESOURCE_1)
        .deploy();

    Deployment deployment3 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addClasspathResource(CMMN_RESOURCE_2)
        .deploy();

    // when
    Deployment deployment4 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResources(deployment1.getId())
        .deploy();

    String firstDefinitionId = queryLatestCaseDefinitionByKey(CASE_KEY_1).getId();
    String secondDefinitionId = queryLatestCaseDefinitionByKey(CASE_KEY_2).getId();

    // then
    assertEquals(reference1, getProcessApplicationForCaseDefinition(firstDefinitionId));
    assertEquals(reference1, getProcessApplicationForCaseDefinition(secondDefinitionId));

    deleteDeployments(deployment1, deployment2, deployment3, deployment4);
  }

  public void testCaseRegistrationFoundFromDifferentDeployments() {
    // given

    // first deployment
    ProcessApplicationReference reference1 = processApplication.getReference();
    Deployment deployment1 = repositoryService
      .createDeployment(reference1)
      .name(DEPLOYMENT_NAME + "-1")
      .addClasspathResource(CMMN_RESOURCE_1)
      .deploy();

    // second deployment
    ProcessApplicationReference reference2 = processApplication.getReference();
    Deployment deployment2 = repositoryService
        .createDeployment(reference2)
        .name(DEPLOYMENT_NAME + "-2")
        .addClasspathResource(CMMN_RESOURCE_2)
        .deploy();

    // when
    Deployment deployment3 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResources(deployment1.getId())
        .deploy();

    String firstDefinitionId = queryLatestCaseDefinitionByKey(CASE_KEY_1).getId();
    String secondDefinitionId = queryLatestCaseDefinitionByKey(CASE_KEY_2).getId();

    // then
    assertEquals(reference1, getProcessApplicationForCaseDefinition(firstDefinitionId));
    assertEquals(reference2, getProcessApplicationForCaseDefinition(secondDefinitionId));

    deleteDeployments(deployment1, deployment2, deployment3);
  }

  public void testCaseRegistrationNotFoundWhenDeletingDeployment() {
    // given

    // first deployment
    Deployment deployment1 = repositoryService
      .createDeployment()
      .name(DEPLOYMENT_NAME)
      .addClasspathResource(CMMN_RESOURCE_1)
      .deploy();

    // second deployment
    ProcessApplicationReference reference2 = processApplication.getReference();
    Deployment deployment2 = repositoryService
        .createDeployment(reference2)
        .name(DEPLOYMENT_NAME)
        .addDeploymentResources(deployment1.getId())
        .deploy();

    // when (1)
    // third deployment
    Deployment deployment3 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResources(deployment1.getId())
        .deploy();

    String firstDefinitionId = queryLatestCaseDefinitionByKey(CASE_KEY_1).getId();

    // then (1)
    assertEquals(reference2, getProcessApplicationForCaseDefinition(firstDefinitionId));

    // when (2)
    deleteDeployments(deployment2);

    // then (2)
    assertNull(getProcessApplicationForCaseDefinition(firstDefinitionId));

    deleteDeployments(deployment1, deployment3);
  }

  public void testCaseRegistrationFoundAfterDiscardingDeploymentCache() {
    // given

    // first deployment
    Deployment deployment1 = repositoryService
      .createDeployment()
      .name(DEPLOYMENT_NAME)
      .addClasspathResource(CMMN_RESOURCE_1)
      .deploy();

    // second deployment
    ProcessApplicationReference reference2 = processApplication.getReference();
    Deployment deployment2 = repositoryService
        .createDeployment(reference2)
        .name(DEPLOYMENT_NAME)
        .addDeploymentResources(deployment1.getId())
        .deploy();

    // when (1)
    // third deployment
    Deployment deployment3 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResources(deployment1.getId())
        .deploy();

    String firstDefinitionId = queryLatestCaseDefinitionByKey(CASE_KEY_1).getId();

    // then (1)
    assertEquals(reference2, getProcessApplicationForCaseDefinition(firstDefinitionId));

    // when (2)
    processEngineConfiguration.getDeploymentCache().discardCaseDefinitionCache();

    // then (2)
    assertEquals(reference2, getProcessApplicationForCaseDefinition(firstDefinitionId));

    deleteDeployments(deployment1, deployment2, deployment3);
  }

  public void testDecisionRegistrationNotFoundByDeploymentId() {
    // given
    ProcessApplicationReference reference = processApplication.getReference();

    Deployment deployment1 = repositoryService
      .createDeployment(reference)
      .name(DEPLOYMENT_NAME)
      .addClasspathResource(DMN_RESOURCE_1)
      .deploy();

    assertEquals(reference, getProcessApplicationForDeployment(deployment1.getId()));

    // when
    Deployment deployment2 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResources(deployment1.getId())
        .deploy();

    // then
    assertNull(getProcessApplicationForDeployment(deployment2.getId()));

    deleteDeployments(deployment1, deployment2);
  }

  public void testDecisionRegistrationNotFoundByDefinition() {
    // given

    // first deployment
    Deployment deployment1 = repositoryService
      .createDeployment()
      .name(DEPLOYMENT_NAME)
      .addClasspathResource(DMN_RESOURCE_1)
      .deploy();

    // first deployment
    Deployment deployment2 = repositoryService
      .createDeployment()
      .name(DEPLOYMENT_NAME)
      .addClasspathResource(DMN_RESOURCE_1)
      .deploy();

    // when
    Deployment deployment3 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResources(deployment1.getId())
        .deploy();

    String definitionId = queryLatestDecisionDefinitionByKey(DECISION_KEY_1).getId();

    // then
    assertNull(getProcessApplicationForDecisionDefinition(definitionId));

    deleteDeployments(deployment1, deployment2, deployment3);
  }

  public void testDecisionRegistrationFoundByDeploymentId() {
    // given
    ProcessApplicationReference reference1 = processApplication.getReference();

    Deployment deployment1 = repositoryService
      .createDeployment(reference1)
      .name(DEPLOYMENT_NAME)
      .addClasspathResource(DMN_RESOURCE_1)
      .deploy();

    assertEquals(reference1, getProcessApplicationForDeployment(deployment1.getId()));

    // when
    ProcessApplicationReference reference2 = processApplication.getReference();

    Deployment deployment2 = repositoryService
        .createDeployment(reference2)
        .name(DEPLOYMENT_NAME)
        .addDeploymentResources(deployment1.getId())
        .deploy();

    // then
    assertEquals(reference2, getProcessApplicationForDeployment(deployment2.getId()));

    deleteDeployments(deployment1, deployment2);
  }

  public void testDecisionRegistrationFoundFromPreviousDefinition() {
    // given
    ProcessApplicationReference reference = processApplication.getReference();
    Deployment deployment1 = repositoryService
      .createDeployment(reference)
      .name(DEPLOYMENT_NAME)
      .addClasspathResource(DMN_RESOURCE_1)
      .deploy();

    // when
    Deployment deployment2 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResources(deployment1.getId())
        .deploy();

    String definitionId = queryLatestDecisionDefinitionByKey(DECISION_KEY_1).getId();

    // then
    assertEquals(reference, getProcessApplicationForDecisionDefinition(definitionId));

    // and the reference is not cached
    assertNull(getProcessApplicationForDeployment(deployment2.getId()));

    deleteDeployments(deployment1, deployment2);
  }

  public void testDecisionRegistrationFoundFromLatestDeployment() {
    // given
    ProcessApplicationReference reference1 = processApplication.getReference();
    Deployment deployment1 = repositoryService
      .createDeployment(reference1)
      .name(DEPLOYMENT_NAME)
      .addClasspathResource(DMN_RESOURCE_1)
      .deploy();

    // when
    ProcessApplicationReference reference2 = processApplication.getReference();
    Deployment deployment2 = repositoryService
        .createDeployment(reference2)
        .name(DEPLOYMENT_NAME)
        .addDeploymentResources(deployment1.getId())
        .deploy();

    String definitionId = queryLatestDecisionDefinitionByKey(DECISION_KEY_1).getId();

    // then
    assertEquals(reference2, getProcessApplicationForDecisionDefinition(definitionId));
    assertEquals(reference2, getProcessApplicationForDeployment(deployment2.getId()));

    deleteDeployments(deployment1, deployment2);
  }

  public void testDecisionRegistrationFoundOnlyForOneProcessDefinition() {
    // given

    // first deployment
    Deployment deployment1 = repositoryService
      .createDeployment()
      .name(DEPLOYMENT_NAME)
      .addClasspathResource(DMN_RESOURCE_1)
      .addClasspathResource(DMN_RESOURCE_2)
      .deploy();

    // second deployment
    ProcessApplicationReference reference2 = processApplication.getReference();
    Deployment deployment2 = repositoryService
        .createDeployment(reference2)
        .name(DEPLOYMENT_NAME)
        .addClasspathResource(DMN_RESOURCE_1)
        .deploy();

    // when
    Deployment deployment3 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResources(deployment1.getId())
        .deploy();

    String firstDefinitionId = queryLatestDecisionDefinitionByKey(DECISION_KEY_1).getId();
    String secondDefinitionId = queryLatestDecisionDefinitionByKey(DECISION_KEY_2).getId();

    // then
    assertEquals(reference2, getProcessApplicationForDecisionDefinition(firstDefinitionId));
    assertNull(getProcessApplicationForDecisionDefinition(secondDefinitionId));

    deleteDeployments(deployment1, deployment2, deployment3);
  }

  public void testDecisionRegistrationFoundFromDifferentDeployment() {
    // given

    // first deployment
    ProcessApplicationReference reference1 = processApplication.getReference();
    Deployment deployment1 = repositoryService
      .createDeployment(reference1)
      .name(DEPLOYMENT_NAME)
      .addClasspathResource(DMN_RESOURCE_1)
      .addClasspathResource(DMN_RESOURCE_2)
      .deploy();

    // second deployment
    ProcessApplicationReference reference2 = processApplication.getReference();
    Deployment deployment2 = repositoryService
        .createDeployment(reference2)
        .name(DEPLOYMENT_NAME)
        .addClasspathResource(DMN_RESOURCE_1)
        .deploy();

    // when
    Deployment deployment3 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResources(deployment1.getId())
        .deploy();

    String firstDefinitionId = queryLatestDecisionDefinitionByKey(DECISION_KEY_1).getId();
    String secondDefinitionId = queryLatestDecisionDefinitionByKey(DECISION_KEY_2).getId();

    // then
    assertEquals(reference2, getProcessApplicationForDecisionDefinition(firstDefinitionId));
    assertEquals(reference1, getProcessApplicationForDecisionDefinition(secondDefinitionId));

    deleteDeployments(deployment1, deployment2, deployment3);
  }

  public void testDecisionRegistrationFoundFromSameDeployment() {
    // given

    // first deployment
    ProcessApplicationReference reference1 = processApplication.getReference();
    Deployment deployment1 = repositoryService
      .createDeployment(reference1)
      .name(DEPLOYMENT_NAME)
      .addClasspathResource(DMN_RESOURCE_1)
      .addClasspathResource(DMN_RESOURCE_2)
      .deploy();

    // second deployment
    Deployment deployment2 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addClasspathResource(DMN_RESOURCE_1)
        .deploy();

    Deployment deployment3 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addClasspathResource(DMN_RESOURCE_2)
        .deploy();

    // when
    Deployment deployment4 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResources(deployment1.getId())
        .deploy();

    String firstDefinitionId = queryLatestDecisionDefinitionByKey(DECISION_KEY_1).getId();
    String secondDefinitionId = queryLatestDecisionDefinitionByKey(DECISION_KEY_2).getId();

    // then
    assertEquals(reference1, getProcessApplicationForDecisionDefinition(firstDefinitionId));
    assertEquals(reference1, getProcessApplicationForDecisionDefinition(secondDefinitionId));

    deleteDeployments(deployment1, deployment2, deployment3, deployment4);
  }

  public void testDecisionRegistrationFoundFromDifferentDeployments() {
    // given

    // first deployment
    ProcessApplicationReference reference1 = processApplication.getReference();
    Deployment deployment1 = repositoryService
      .createDeployment(reference1)
      .name(DEPLOYMENT_NAME + "-1")
      .addClasspathResource(DMN_RESOURCE_1)
      .deploy();

    // second deployment
    ProcessApplicationReference reference2 = processApplication.getReference();
    Deployment deployment2 = repositoryService
        .createDeployment(reference2)
        .name(DEPLOYMENT_NAME + "-2")
        .addClasspathResource(DMN_RESOURCE_2)
        .deploy();

    // when
    Deployment deployment3 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResources(deployment1.getId())
        .deploy();

    String firstDefinitionId = queryLatestDecisionDefinitionByKey(DECISION_KEY_1).getId();
    String secondDefinitionId = queryLatestDecisionDefinitionByKey(DECISION_KEY_2).getId();

    // then
    assertEquals(reference1, getProcessApplicationForDecisionDefinition(firstDefinitionId));
    assertEquals(reference2, getProcessApplicationForDecisionDefinition(secondDefinitionId));

    deleteDeployments(deployment1, deployment2, deployment3);
  }

  public void testDecisionRegistrationNotFoundWhenDeletingDeployment() {
    // given

    // first deployment
    Deployment deployment1 = repositoryService
      .createDeployment()
      .name(DEPLOYMENT_NAME)
      .addClasspathResource(DMN_RESOURCE_1)
      .deploy();

    // second deployment
    ProcessApplicationReference reference2 = processApplication.getReference();
    Deployment deployment2 = repositoryService
        .createDeployment(reference2)
        .name(DEPLOYMENT_NAME)
        .addDeploymentResources(deployment1.getId())
        .deploy();

    // when (1)
    // third deployment
    Deployment deployment3 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResources(deployment1.getId())
        .deploy();

    String firstDefinitionId = queryLatestDecisionDefinitionByKey(DECISION_KEY_1).getId();

    // then (1)
    assertEquals(reference2, getProcessApplicationForDecisionDefinition(firstDefinitionId));

    // when (2)
    deleteDeployments(deployment2);

    // then (2)
    assertNull(getProcessApplicationForDecisionDefinition(firstDefinitionId));

    deleteDeployments(deployment1, deployment3);
  }

  public void testDecisionRegistrationFoundAfterDiscardingDeploymentCache() {
    // given

    // first deployment
    Deployment deployment1 = repositoryService
      .createDeployment()
      .name(DEPLOYMENT_NAME)
      .addClasspathResource(DMN_RESOURCE_1)
      .deploy();

    // second deployment
    ProcessApplicationReference reference2 = processApplication.getReference();
    Deployment deployment2 = repositoryService
        .createDeployment(reference2)
        .name(DEPLOYMENT_NAME)
        .addDeploymentResources(deployment1.getId())
        .deploy();

    // when (1)
    // third deployment
    Deployment deployment3 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResources(deployment1.getId())
        .deploy();

    String firstDefinitionId = queryLatestDecisionDefinitionByKey(DECISION_KEY_1).getId();

    // then (1)
    assertEquals(reference2, getProcessApplicationForDecisionDefinition(firstDefinitionId));

    // when (2)
    processEngineConfiguration.getDeploymentCache().discardDecisionDefinitionCache();

    // then (2)
    assertEquals(reference2, getProcessApplicationForDecisionDefinition(firstDefinitionId));

    deleteDeployments(deployment1, deployment2, deployment3);
  }

  // helper ///////////////////////////////////////////

  protected ProcessDefinition queryLatestProcessDefinitionByKey(String key) {
    return repositoryService.createProcessDefinitionQuery().processDefinitionKey(key).latestVersion().singleResult();
  }

  protected ProcessApplicationReference getProcessApplicationForProcessDefinition(final String processDefinitionId) {
    return executeCommand(new Command<ProcessApplicationReference>() {

      public ProcessApplicationReference execute(CommandContext commandContext) {
        ProcessEngineConfigurationImpl configuration = commandContext.getProcessEngineConfiguration();
        DeploymentCache deploymentCache = configuration.getDeploymentCache();
        ProcessDefinitionEntity definition = deploymentCache.findDeployedProcessDefinitionById(processDefinitionId);
        return ProcessApplicationContextUtil.getTargetProcessApplication(definition);
      }
    });
  }

  protected CaseDefinition queryLatestCaseDefinitionByKey(String key) {
    return repositoryService.createCaseDefinitionQuery().caseDefinitionKey(key).latestVersion().singleResult();
  }

  protected ProcessApplicationReference getProcessApplicationForCaseDefinition(final String caseDefinitionId) {
    return executeCommand(new Command<ProcessApplicationReference>() {

      public ProcessApplicationReference execute(CommandContext commandContext) {
        ProcessEngineConfigurationImpl configuration = commandContext.getProcessEngineConfiguration();
        DeploymentCache deploymentCache = configuration.getDeploymentCache();
        CaseDefinitionEntity definition = deploymentCache.findDeployedCaseDefinitionById(caseDefinitionId);
        return ProcessApplicationContextUtil.getTargetProcessApplication(definition);
      }
    });
  }

  protected DecisionDefinition queryLatestDecisionDefinitionByKey(String key) {
    return repositoryService.createDecisionDefinitionQuery().decisionDefinitionKey(key).latestVersion().singleResult();
  }

  protected ProcessApplicationReference getProcessApplicationForDecisionDefinition(final String decisionDefinitionId) {
    return executeCommand(new Command<ProcessApplicationReference>() {

      public ProcessApplicationReference execute(CommandContext commandContext) {
        ProcessEngineConfigurationImpl configuration = commandContext.getProcessEngineConfiguration();
        DeploymentCache deploymentCache = configuration.getDeploymentCache();
        DecisionDefinitionEntity definition = deploymentCache.findDeployedDecisionDefinitionById(decisionDefinitionId);
        return ProcessApplicationContextUtil.getTargetProcessApplication(definition);
      }
    });
  }

  protected ProcessApplicationReference getProcessApplicationForDeployment(String deploymentId) {
    return getProcessApplicationManager().getProcessApplicationForDeployment(deploymentId);
  }

  protected ProcessApplicationManager getProcessApplicationManager() {
    return processEngineConfiguration.getProcessApplicationManager();
  }

  protected ProcessApplicationReference executeCommand(Command<ProcessApplicationReference> command) {
    return processEngineConfiguration.getCommandExecutorTxRequired().execute(command);
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

}
