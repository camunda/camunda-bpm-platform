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

import java.util.concurrent.Callable;

import org.camunda.bpm.application.ProcessApplicationExecutionException;
import org.camunda.bpm.application.ProcessApplicationReference;
import org.camunda.bpm.application.impl.EmbeddedProcessApplication;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.query.Query;
import org.camunda.bpm.engine.repository.CaseDefinitionQuery;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.ProcessDefinitionQuery;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

/**
 * @author Roman Smirnov
 *
 */
public class RedeploymentProcessApplicationTest extends PluggableProcessEngineTestCase {

  protected static final String DEPLOYMENT_NAME = "my-deployment";

  protected static final String PROCESS_KEY_1 = "process-1";
  protected static final String PROCESS_KEY_2 = "process-2";

  protected static final String BPMN_RESOURCE_1 = "path/to/my/process1.bpmn";
  protected static final String BPMN_RESOURCE_2 = "path/to/my/process2.bpmn";

  protected static final String CASE_KEY_1 = "oneTaskCase";
  protected static final String CASE_KEY_2 = "twoTaskCase";

  protected static final String CMMN_RESOURCE_1 = "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn";
  protected static final String CMMN_RESOURCE_2 = "org/camunda/bpm/engine/test/api/cmmn/twoTaskCase.cmmn";

  public void testProcessOnePreviousDeploymentWithPA() {
    // given

    MyEmbeddedProcessApplication application = new MyEmbeddedProcessApplication();

    // first deployment
    Deployment deployment1 = repositoryService
        .createDeployment(application.getReference())
        .name(DEPLOYMENT_NAME)
        .addModelInstance(BPMN_RESOURCE_1, createProcessWithServiceTask(PROCESS_KEY_1))
        .deploy();

    // second deployment
    Deployment deployment2 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResources(deployment1.getId())
        .deploy();

    verifyQueryResults(processDefinitionQueryByKey(PROCESS_KEY_1), 2);

    // when
    runtimeService.startProcessInstanceByKey(PROCESS_KEY_1);

    // then
    assertTrue(application.isCalled());

    deleteDeployments(deployment1, deployment2);
  }

  public void testProcessTwoPreviousDeploymentWithPA() {
    // given

    // first deployment
    MyEmbeddedProcessApplication application1 = new MyEmbeddedProcessApplication();
    Deployment deployment1 = repositoryService
        .createDeployment(application1.getReference())
        .name(DEPLOYMENT_NAME)
        .addModelInstance(BPMN_RESOURCE_1, createProcessWithServiceTask(PROCESS_KEY_1))
        .deploy();

    // second deployment
    MyEmbeddedProcessApplication application2 = new MyEmbeddedProcessApplication();
    Deployment deployment2 = repositoryService
        .createDeployment(application2.getReference())
        .name(DEPLOYMENT_NAME)
        .addModelInstance(BPMN_RESOURCE_1, createProcessWithServiceTask(PROCESS_KEY_1))
        .deploy();

    // second deployment
    Deployment deployment3 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResources(deployment1.getId())
        .deploy();

    verifyQueryResults(processDefinitionQueryByKey(PROCESS_KEY_1), 3);

    // when
    runtimeService.startProcessInstanceByKey(PROCESS_KEY_1);

    // then
    assertFalse(application1.isCalled());
    assertTrue(application2.isCalled());

    deleteDeployments(deployment1, deployment2, deployment3);
  }

  public void testProcessTwoPreviousDeploymentFirstDeploymentWithPA() {
    // given

    // first deployment
    MyEmbeddedProcessApplication application1 = new MyEmbeddedProcessApplication();
    Deployment deployment1 = repositoryService
        .createDeployment(application1.getReference())
        .name(DEPLOYMENT_NAME)
        .addModelInstance(BPMN_RESOURCE_1, createProcessWithServiceTask(PROCESS_KEY_1))
        .deploy();

    // second deployment
    Deployment deployment2 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addModelInstance(BPMN_RESOURCE_1, createProcessWithServiceTask(PROCESS_KEY_1))
        .deploy();

    // second deployment
    Deployment deployment3 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResources(deployment1.getId())
        .deploy();

    verifyQueryResults(processDefinitionQueryByKey(PROCESS_KEY_1), 3);

    // when
    runtimeService.startProcessInstanceByKey(PROCESS_KEY_1);

    // then
    assertTrue(application1.isCalled());

    deleteDeployments(deployment1, deployment2, deployment3);
  }

  public void testProcessTwoPreviousDeploymentDeleteSecondDeployment() {
    // given

    // first deployment
    MyEmbeddedProcessApplication application1 = new MyEmbeddedProcessApplication();
    Deployment deployment1 = repositoryService
        .createDeployment(application1.getReference())
        .name(DEPLOYMENT_NAME)
        .addModelInstance(BPMN_RESOURCE_1, createProcessWithServiceTask(PROCESS_KEY_1))
        .deploy();

    // second deployment
    MyEmbeddedProcessApplication application2 = new MyEmbeddedProcessApplication();
    Deployment deployment2 = repositoryService
        .createDeployment(application2.getReference())
        .name(DEPLOYMENT_NAME)
        .addModelInstance(BPMN_RESOURCE_1, createProcessWithServiceTask(PROCESS_KEY_1))
        .deploy();

    // second deployment
    Deployment deployment3 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResources(deployment1.getId())
        .deploy();

    verifyQueryResults(processDefinitionQueryByKey(PROCESS_KEY_1), 3);

    // when
    deleteDeployments(deployment2);
    runtimeService.startProcessInstanceByKey(PROCESS_KEY_1);

    // then
    assertTrue(application1.isCalled());
    assertFalse(application2.isCalled());

    deleteDeployments(deployment1, deployment3);
  }

  public void testProcessTwoPreviousDeploymentUnregisterSecondPA() {
    // given

    // first deployment
    MyEmbeddedProcessApplication application1 = new MyEmbeddedProcessApplication();
    Deployment deployment1 = repositoryService
        .createDeployment(application1.getReference())
        .name(DEPLOYMENT_NAME)
        .addModelInstance(BPMN_RESOURCE_1, createProcessWithServiceTask(PROCESS_KEY_1))
        .deploy();

    // second deployment
    MyEmbeddedProcessApplication application2 = new MyEmbeddedProcessApplication();
    Deployment deployment2 = repositoryService
        .createDeployment(application2.getReference())
        .name(DEPLOYMENT_NAME)
        .addModelInstance(BPMN_RESOURCE_1, createProcessWithServiceTask(PROCESS_KEY_1))
        .deploy();

    // second deployment
    Deployment deployment3 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResources(deployment1.getId())
        .deploy();

    verifyQueryResults(processDefinitionQueryByKey(PROCESS_KEY_1), 3);

    // when
    managementService.unregisterProcessApplication(deployment2.getId(), true);
    runtimeService.startProcessInstanceByKey(PROCESS_KEY_1);

    // then
    assertTrue(application1.isCalled());
    assertFalse(application2.isCalled());

    deleteDeployments(deployment1, deployment2, deployment3);
  }

  public void testProcessTwoDifferentPreviousDeploymentsWithDifferentPA() {
    // given

    // first deployment
    MyEmbeddedProcessApplication application1 = new MyEmbeddedProcessApplication();
    Deployment deployment1 = repositoryService
        .createDeployment(application1.getReference())
        .name(DEPLOYMENT_NAME + "-1")
        .addModelInstance(BPMN_RESOURCE_1, createProcessWithServiceTask(PROCESS_KEY_1))
        .deploy();

    // second deployment
    MyEmbeddedProcessApplication application2 = new MyEmbeddedProcessApplication();
    Deployment deployment2 = repositoryService
        .createDeployment(application2.getReference())
        .name(DEPLOYMENT_NAME + "-2")
        .addModelInstance(BPMN_RESOURCE_2, createProcessWithServiceTask(PROCESS_KEY_2))
        .deploy();

    // second deployment
    Deployment deployment3 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME + "-3")
        .addDeploymentResources(deployment1.getId())
        .addDeploymentResources(deployment2.getId())
        .deploy();

    verifyQueryResults(processDefinitionQueryByKey(PROCESS_KEY_1), 2);
    verifyQueryResults(processDefinitionQueryByKey(PROCESS_KEY_2), 2);

    // when (1)
    runtimeService.startProcessInstanceByKey(PROCESS_KEY_1);

    // then (1)
    assertTrue(application1.isCalled());
    assertFalse(application2.isCalled());

    // reset flag
    application1.setCalled(false);

    // when (2)
    runtimeService.startProcessInstanceByKey(PROCESS_KEY_2);

    // then (2)
    assertFalse(application1.isCalled());
    assertTrue(application2.isCalled());

    deleteDeployments(deployment1, deployment2, deployment3);
  }

  public void testProcessTwoPreviousDeploymentsWithDifferentPA() {
    // given

    // first deployment
    MyEmbeddedProcessApplication application1 = new MyEmbeddedProcessApplication();
    Deployment deployment1 = repositoryService
      .createDeployment(application1.getReference())
      .name(DEPLOYMENT_NAME)
      .addModelInstance(BPMN_RESOURCE_1, createProcessWithServiceTask(PROCESS_KEY_1))
      .addModelInstance(BPMN_RESOURCE_2, createProcessWithServiceTask(PROCESS_KEY_2))
      .deploy();

    // second deployment
    MyEmbeddedProcessApplication application2 = new MyEmbeddedProcessApplication();
    Deployment deployment2 = repositoryService
        .createDeployment(application2.getReference())
        .name(DEPLOYMENT_NAME)
        .addModelInstance(BPMN_RESOURCE_1, createProcessWithServiceTask(PROCESS_KEY_1))
        .deploy();

    // third deployment
    Deployment deployment3 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResources(deployment1.getId())
        .deploy();

    verifyQueryResults(processDefinitionQueryByKey(PROCESS_KEY_1), 3);
    verifyQueryResults(processDefinitionQueryByKey(PROCESS_KEY_2), 2);

    // when (1)
    runtimeService.startProcessInstanceByKey(PROCESS_KEY_1);

    // then (1)
    assertFalse(application1.isCalled());
    assertTrue(application2.isCalled());

    // reset flag
    application2.setCalled(false);

    // when (2)
    runtimeService.startProcessInstanceByKey(PROCESS_KEY_2);

    // then (2)
    assertTrue(application1.isCalled());
    assertFalse(application2.isCalled());

    deleteDeployments(deployment1, deployment2, deployment3);
  }

  public void testCaseOnePreviousDeploymentWithPA() {
    // given

    MyEmbeddedProcessApplication application = new MyEmbeddedProcessApplication();

    // first deployment
    Deployment deployment1 = repositoryService
        .createDeployment(application.getReference())
        .name(DEPLOYMENT_NAME)
        .addClasspathResource(CMMN_RESOURCE_1)
        .deploy();

    // second deployment
    Deployment deployment2 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResources(deployment1.getId())
        .deploy();

    verifyQueryResults(caseDefinitionQueryByKey(CASE_KEY_1), 2);

    // when
    caseService.createCaseInstanceByKey(CASE_KEY_1);

    // then
    assertTrue(application.isCalled());

    deleteDeployments(deployment1, deployment2);
  }

  public void testCaseTwoPreviousDeploymentWithPA() {
    // given

    // first deployment
    MyEmbeddedProcessApplication application1 = new MyEmbeddedProcessApplication();
    Deployment deployment1 = repositoryService
        .createDeployment(application1.getReference())
        .name(DEPLOYMENT_NAME)
        .addClasspathResource(CMMN_RESOURCE_1)
        .deploy();

    // second deployment
    MyEmbeddedProcessApplication application2 = new MyEmbeddedProcessApplication();
    Deployment deployment2 = repositoryService
        .createDeployment(application2.getReference())
        .name(DEPLOYMENT_NAME)
        .addClasspathResource(CMMN_RESOURCE_1)
        .deploy();

    // second deployment
    Deployment deployment3 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResources(deployment1.getId())
        .deploy();

    verifyQueryResults(caseDefinitionQueryByKey(CASE_KEY_1), 3);

    // when
    caseService.createCaseInstanceByKey(CASE_KEY_1);

    // then
    assertFalse(application1.isCalled());
    assertTrue(application2.isCalled());

    deleteDeployments(deployment1, deployment2, deployment3);
  }

  public void testCaseTwoPreviousDeploymentFirstDeploymentWithPA() {
    // given

    // first deployment
    MyEmbeddedProcessApplication application1 = new MyEmbeddedProcessApplication();
    Deployment deployment1 = repositoryService
        .createDeployment(application1.getReference())
        .name(DEPLOYMENT_NAME)
        .addClasspathResource(CMMN_RESOURCE_1)
        .deploy();

    // second deployment
    Deployment deployment2 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addClasspathResource(CMMN_RESOURCE_1)
        .deploy();

    // second deployment
    Deployment deployment3 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResources(deployment1.getId())
        .deploy();

    verifyQueryResults(caseDefinitionQueryByKey(CASE_KEY_1), 3);

    // when
    caseService.createCaseInstanceByKey(CASE_KEY_1);

    // then
    assertTrue(application1.isCalled());

    deleteDeployments(deployment1, deployment2, deployment3);
  }

  public void testCaseTwoPreviousDeploymentDeleteSecondDeployment() {
    // given

    // first deployment
    MyEmbeddedProcessApplication application1 = new MyEmbeddedProcessApplication();
    Deployment deployment1 = repositoryService
        .createDeployment(application1.getReference())
        .name(DEPLOYMENT_NAME)
        .addClasspathResource(CMMN_RESOURCE_1)
        .deploy();

    // second deployment
    MyEmbeddedProcessApplication application2 = new MyEmbeddedProcessApplication();
    Deployment deployment2 = repositoryService
        .createDeployment(application2.getReference())
        .name(DEPLOYMENT_NAME)
        .addClasspathResource(CMMN_RESOURCE_1)
        .deploy();

    // second deployment
    Deployment deployment3 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResources(deployment1.getId())
        .deploy();

    verifyQueryResults(caseDefinitionQueryByKey(CASE_KEY_1), 3);

    // when
    deleteDeployments(deployment2);
    caseService.createCaseInstanceByKey(CASE_KEY_1);

    // then
    assertTrue(application1.isCalled());
    assertFalse(application2.isCalled());

    deleteDeployments(deployment1, deployment3);
  }

  public void testCaseTwoPreviousDeploymentUnregisterSecondPA() {
    // given

    // first deployment
    MyEmbeddedProcessApplication application1 = new MyEmbeddedProcessApplication();
    Deployment deployment1 = repositoryService
        .createDeployment(application1.getReference())
        .name(DEPLOYMENT_NAME)
        .addClasspathResource(CMMN_RESOURCE_1)
        .deploy();

    // second deployment
    MyEmbeddedProcessApplication application2 = new MyEmbeddedProcessApplication();
    Deployment deployment2 = repositoryService
        .createDeployment(application2.getReference())
        .name(DEPLOYMENT_NAME)
        .addClasspathResource(CMMN_RESOURCE_1)
        .deploy();

    // second deployment
    Deployment deployment3 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResources(deployment1.getId())
        .deploy();

    verifyQueryResults(caseDefinitionQueryByKey(CASE_KEY_1), 3);

    // when
    managementService.unregisterProcessApplication(deployment2.getId(), true);
    caseService.createCaseInstanceByKey(CASE_KEY_1);

    // then
    assertTrue(application1.isCalled());
    assertFalse(application2.isCalled());

    deleteDeployments(deployment1, deployment2, deployment3);
  }

  public void testCaseTwoDifferentPreviousDeploymentsWithDifferentPA() {
    // given

    // first deployment
    MyEmbeddedProcessApplication application1 = new MyEmbeddedProcessApplication();
    Deployment deployment1 = repositoryService
        .createDeployment(application1.getReference())
        .name(DEPLOYMENT_NAME + "-1")
        .addClasspathResource(CMMN_RESOURCE_1)
        .deploy();

    // second deployment
    MyEmbeddedProcessApplication application2 = new MyEmbeddedProcessApplication();
    Deployment deployment2 = repositoryService
        .createDeployment(application2.getReference())
        .name(DEPLOYMENT_NAME + "-2")
        .addClasspathResource(CMMN_RESOURCE_2)
        .deploy();

    // second deployment
    Deployment deployment3 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME + "-3")
        .addDeploymentResources(deployment1.getId())
        .addDeploymentResources(deployment2.getId())
        .deploy();

    verifyQueryResults(caseDefinitionQueryByKey(CASE_KEY_1), 2);
    verifyQueryResults(caseDefinitionQueryByKey(CASE_KEY_2), 2);

    // when (1)
    caseService.createCaseInstanceByKey(CASE_KEY_1);

    // then (1)
    assertTrue(application1.isCalled());
    assertFalse(application2.isCalled());

    // reset flag
    application1.setCalled(false);

    // when (2)
    caseService.createCaseInstanceByKey(CASE_KEY_2);

    // then (2)
    assertFalse(application1.isCalled());
    assertTrue(application2.isCalled());

    deleteDeployments(deployment1, deployment2, deployment3);
  }

  public void testCaseTwoPreviousDeploymentsWithDifferentPA() {
    // given

    // first deployment
    MyEmbeddedProcessApplication application1 = new MyEmbeddedProcessApplication();
    Deployment deployment1 = repositoryService
      .createDeployment(application1.getReference())
      .name(DEPLOYMENT_NAME)
      .addClasspathResource(CMMN_RESOURCE_1)
      .addClasspathResource(CMMN_RESOURCE_2)
      .deploy();

    // second deployment
    MyEmbeddedProcessApplication application2 = new MyEmbeddedProcessApplication();
    Deployment deployment2 = repositoryService
        .createDeployment(application2.getReference())
        .name(DEPLOYMENT_NAME)
        .addClasspathResource(CMMN_RESOURCE_1)
        .deploy();

    // third deployment
    Deployment deployment3 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResources(deployment1.getId())
        .deploy();

    verifyQueryResults(caseDefinitionQueryByKey(CASE_KEY_1), 3);
    verifyQueryResults(caseDefinitionQueryByKey(CASE_KEY_2), 2);

    // when (1)
    caseService.createCaseInstanceByKey(CASE_KEY_1);

    // then (1)
    assertFalse(application1.isCalled());
    assertTrue(application2.isCalled());

    // reset flag
    application2.setCalled(false);

    // when (2)
    caseService.createCaseInstanceByKey(CASE_KEY_2);

    // then (2)
    assertTrue(application1.isCalled());
    assertFalse(application2.isCalled());

    deleteDeployments(deployment1, deployment2, deployment3);
  }

  protected void verifyQueryResults(Query<?, ?> query, int countExpected) {
    assertEquals(countExpected, query.count());
  }

  protected ProcessDefinitionQuery processDefinitionQueryByKey(String key) {
    return repositoryService.createProcessDefinitionQuery().processDefinitionKey(key);
  }

  protected CaseDefinitionQuery caseDefinitionQueryByKey(String key) {
    return repositoryService.createCaseDefinitionQuery().caseDefinitionKey(key);
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

  public class MyEmbeddedProcessApplication extends EmbeddedProcessApplication {

    protected ProcessApplicationReference reference;
    protected boolean called;

    public ProcessApplicationReference getReference() {
      if (reference == null) {
        reference = super.getReference();
      }
      return reference;
    }

    public <T> T execute(Callable<T> callable) throws ProcessApplicationExecutionException {
      called = true;
      return super.execute(callable);
    }

    public boolean isCalled() {
      return called;
    }

    public void setCalled(boolean called) {
      this.called = called;
    }

  }

}
