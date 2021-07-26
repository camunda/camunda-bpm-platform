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
package org.camunda.bpm.engine.test.api.form;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.camunda.bpm.engine.test.util.CamundaFormUtils.findAllCamundaFormDefinitionEntities;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.FormService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.exception.NotFoundException;
import org.camunda.bpm.engine.form.CamundaFormRef;
import org.camunda.bpm.engine.form.TaskFormData;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.util.ReflectUtil;
import org.camunda.bpm.engine.repository.CamundaFormDefinition;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.DeploymentBuilder;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.util.CamundaFormUtils;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TemporaryFolder;

public class RetrieveCamundaFormRefTest {

  protected static final String TASK_FORM_CONTENT_V1 = "{\"id\"=\"myTaskForm\",\"type\": \"default\",\"components\": []}";
  protected static final String TASK_FORM_CONTENT_V2 = "{\"id\"=\"myTaskForm\",\"type\": \"default\",\"components\":[{\"key\": \"textfield1\",\"label\": \"Text Field\",\"type\": \"textfield\"}]}";
  protected static final String START_FORM_CONTENT_V1 = "{\"id\"=\"myStartForm\",\"type\": \"default\",\"components\": []}";
  protected static final String START_FORM_CONTENT_V2 = "{\"id\"=\"myStartForm\",\"type\": \"default\",\"components\":[{\"key\": \"textfield1\",\"label\": \"Text Field\",\"type\": \"textfield\"}]}";

  protected ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);
  protected TemporaryFolder tempFolder = new TemporaryFolder();
  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule).around(tempFolder);

  private RuntimeService runtimeService;
  private TaskService taskService;
  private RepositoryService repositoryService;
  private FormService formService;
  private ProcessEngineConfigurationImpl processEngineConfiguration;

  @Before
  public void init() {
    runtimeService = engineRule.getRuntimeService();
    taskService = engineRule.getTaskService();
    repositoryService = engineRule.getRepositoryService();
    formService = engineRule.getFormService();
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();
  }

  @After
  public void tearDown() throws Exception {
    List<org.camunda.bpm.engine.repository.Deployment> deployments = repositoryService.createDeploymentQuery().list();
    for (org.camunda.bpm.engine.repository.Deployment deployment : deployments) {
      repositoryService.deleteDeployment(deployment.getId(), true);
    }
  }

  /* TASK FORMS */

  @Test
  public void shouldRetrieveTaskFormBindingLatestWithSingleVersionSeparateDeloyments() throws IOException {
    // given two separate deployments
    deployClasspathResources(true,
        "org/camunda/bpm/engine/test/api/form/RetrieveCamundaFormRefTest.taskFormBindingLatest.bpmn",
        "org/camunda/bpm/engine/test/api/form/task.form");

    runtimeService.startProcessInstanceByKey("taskFormBindingLatest");

    // when
    List<Deployment> deployments = repositoryService.createDeploymentQuery().list();
    List<CamundaFormDefinition> definitions = findAllCamundaFormDefinitionEntities(processEngineConfiguration);
    Task task = taskService.createTaskQuery().singleResult();
    TaskFormData taskFormData = formService.getTaskFormData(task.getId());
    InputStream deployedForm = formService.getDeployedTaskForm(task.getId());

    // then
    assertThat(deployments).hasSize(2);
    assertThat(definitions).hasSize(1);

    assertTaskFormData(taskFormData, "myTaskForm", "latest", null);

    assertThat(IOUtils.toString(deployedForm, UTF_8)).isEqualTo(getClasspathResourceContent("org/camunda/bpm/engine/test/api/form/task.form"));
  }

  @Test
  public void shouldRetrieveTaskFormBindingLatestWithMultipleVersions() throws IOException {
    // given two versions of the same form
    deployUpdateFormResource(TASK_FORM_CONTENT_V1, TASK_FORM_CONTENT_V2, "org/camunda/bpm/engine/test/api/form/RetrieveCamundaFormRefTest.taskFormBindingLatest.bpmn");

    runtimeService.startProcessInstanceByKey("taskFormBindingLatest");

    // when
    List<Deployment> deployments = repositoryService.createDeploymentQuery().list();
    List<CamundaFormDefinition> definitions = findAllCamundaFormDefinitionEntities(processEngineConfiguration);
    Task task = taskService.createTaskQuery().singleResult();
    TaskFormData taskFormData = formService.getTaskFormData(task.getId());
    InputStream deployedForm = formService.getDeployedTaskForm(task.getId());

    // then
    assertThat(deployments).hasSize(2);
    assertThat(definitions).hasSize(2);

    assertTaskFormData(taskFormData, "myTaskForm", "latest", null);

    assertThat(IOUtils.toString(deployedForm, UTF_8)).isEqualTo(TASK_FORM_CONTENT_V2);
  }

  @Test
  public void shouldRetrieveTaskFormBindingDeployment() throws IOException {
    // given two versions of the same form
    deployUpdateFormResource(TASK_FORM_CONTENT_V1, TASK_FORM_CONTENT_V2, "org/camunda/bpm/engine/test/api/form/RetrieveCamundaFormRefTest.taskFormBindingDeployment.bpmn");

    runtimeService.startProcessInstanceByKey("taskFormBindingDeployment");

    // when
    List<Deployment> deployments = repositoryService.createDeploymentQuery().list();
    List<CamundaFormDefinition> definitions = findAllCamundaFormDefinitionEntities(processEngineConfiguration);
    Task task = taskService.createTaskQuery().singleResult();
    TaskFormData taskFormData = formService.getTaskFormData(task.getId());
    InputStream deployedForm = formService.getDeployedTaskForm(task.getId());

    // then
    assertThat(deployments).hasSize(2);
    assertThat(definitions).hasSize(2);

    assertTaskFormData(taskFormData, "myTaskForm", "deployment", null);

    assertThat(IOUtils.toString(deployedForm, UTF_8)).isEqualTo(TASK_FORM_CONTENT_V1);
  }

  @Test
  public void shouldRetrieveTaskFormBindingVersionWithMultipleVersions() throws IOException {
    // given two versions of the same form
    deployUpdateFormResource(TASK_FORM_CONTENT_V1, TASK_FORM_CONTENT_V2, "org/camunda/bpm/engine/test/api/form/RetrieveCamundaFormRefTest.taskFormBindingVersion1.bpmn");

    runtimeService.startProcessInstanceByKey("taskFormBindingVersion");

    // when
    List<Deployment> deployments = repositoryService.createDeploymentQuery().list();
    List<CamundaFormDefinition> definitions = findAllCamundaFormDefinitionEntities(processEngineConfiguration);
    Task task = taskService.createTaskQuery().singleResult();
    TaskFormData taskFormData = formService.getTaskFormData(task.getId());
    InputStream deployedForm = formService.getDeployedTaskForm(task.getId());

    // then
    assertThat(deployments).hasSize(2);
    assertThat(definitions).hasSize(2);

    assertTaskFormData(taskFormData, "myTaskForm", "version", 1);

    assertThat(IOUtils.toString(deployedForm, UTF_8)).isEqualTo(TASK_FORM_CONTENT_V1);
  }

  @Test
  @org.camunda.bpm.engine.test.Deployment(resources = {"org/camunda/bpm/engine/test/api/form/RetrieveCamundaFormRefTest.taskFormBindingLatest.bpmn"})
  public void shouldFailToRetrieveTaskFormBindingLatestUnexistingKey() throws IOException {
    // given BPMN model references missing form
    runtimeService.startProcessInstanceByKey("taskFormBindingLatest");

    // when
    List<Deployment> deployments = repositoryService.createDeploymentQuery().list();
    List<CamundaFormDefinition> definitions = findAllCamundaFormDefinitionEntities(processEngineConfiguration);
    Task task = taskService.createTaskQuery().singleResult();
    TaskFormData taskFormData = formService.getTaskFormData(task.getId());

    // then
    assertThat(deployments).hasSize(1);
    assertThat(definitions).hasSize(0);

    assertTaskFormData(taskFormData, "myTaskForm", "latest", null);

    assertThatThrownBy(() -> {
      formService.getDeployedTaskForm(task.getId());
    }).isInstanceOf(NotFoundException.class)
    .hasMessageContaining("No Camunda Form Definition was found for Camunda Form Ref");
  }

  @Test
  @org.camunda.bpm.engine.test.Deployment(resources = {"org/camunda/bpm/engine/test/api/form/RetrieveCamundaFormRefTest.taskFormBindingDeployment.bpmn"})
  public void shouldFailToRetrieveTaskFormBindingDeploymentUnexistingKey() throws IOException {
    // given BPMN model references missing form
    runtimeService.startProcessInstanceByKey("taskFormBindingDeployment");

    // when
    List<Deployment> deployments = repositoryService.createDeploymentQuery().list();
    List<CamundaFormDefinition> definitions = findAllCamundaFormDefinitionEntities(processEngineConfiguration);
    Task task = taskService.createTaskQuery().singleResult();
    TaskFormData taskFormData = formService.getTaskFormData(task.getId());

    // then
    assertThat(deployments).hasSize(1);
    assertThat(definitions).hasSize(0);

    assertTaskFormData(taskFormData, "myTaskForm", "deployment", null);

    assertThatThrownBy(() -> {
      formService.getDeployedTaskForm(task.getId());
    }).isInstanceOf(NotFoundException.class)
    .hasMessageContaining("No Camunda Form Definition was found for Camunda Form Ref");
  }

  @Test
  @org.camunda.bpm.engine.test.Deployment(resources = {
      "org/camunda/bpm/engine/test/api/form/RetrieveCamundaFormRefTest.taskFormBindingVersion2.bpmn",
      "org/camunda/bpm/engine/test/api/form/task.form" })
  public void shouldFailToRetrieveTaskFormBindingVersionUnexistingVersion() throws IOException {
    // given BPMN model references missing form
    runtimeService.startProcessInstanceByKey("taskFormBindingVersion");

    // when
    List<Deployment> deployments = repositoryService.createDeploymentQuery().list();
    List<CamundaFormDefinition> definitions = findAllCamundaFormDefinitionEntities(processEngineConfiguration);
    Task task = taskService.createTaskQuery().singleResult();
    TaskFormData taskFormData = formService.getTaskFormData(task.getId());

    // then
    assertThat(deployments).hasSize(1);
    assertThat(definitions).hasSize(1);

    assertTaskFormData(taskFormData, "myTaskForm", "version", 2);

    assertThatThrownBy(() -> {
      formService.getDeployedTaskForm(task.getId());
    }).isInstanceOf(NotFoundException.class)
    .hasMessageContaining("No Camunda Form Definition was found for Camunda Form Ref");
  }

  @Test
  @org.camunda.bpm.engine.test.Deployment(resources = {
      "org/camunda/bpm/engine/test/api/form/RetrieveCamundaFormRefTest.shouldRetrieveTaskFormBindingLatestWithKeyExpression.bpmn",
      "org/camunda/bpm/engine/test/api/form/task.form" })
  public void shouldRetrieveTaskFormBindingLatestWithKeyExpression() throws IOException {
    // given BPMN model referencing form by ${key} expression
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("key", "myTaskForm");
    runtimeService.startProcessInstanceByKey("taskFormBindingLatest", parameters);

    // when
    List<Deployment> deployments = repositoryService.createDeploymentQuery().list();
    List<CamundaFormDefinition> definitions = findAllCamundaFormDefinitionEntities(processEngineConfiguration);
    Task task = taskService.createTaskQuery().singleResult();
    TaskFormData taskFormData = formService.getTaskFormData(task.getId());
    InputStream deployedForm = formService.getDeployedTaskForm(task.getId());

    // then
    assertThat(deployments).hasSize(1);
    assertThat(definitions).hasSize(1);

    assertTaskFormData(taskFormData, "myTaskForm", "latest", null);

    assertThat(IOUtils.toString(deployedForm, UTF_8)).isEqualTo(getClasspathResourceContent("org/camunda/bpm/engine/test/api/form/task.form"));
  }

  @Test
  @org.camunda.bpm.engine.test.Deployment(resources = {
      "org/camunda/bpm/engine/test/api/form/RetrieveCamundaFormRefTest.shouldRetrieveTaskFormBindingVersionWithExpression.bpmn",
      "org/camunda/bpm/engine/test/api/form/task.form" })
  public void shouldRetrieveTaskFormBindingVersionWithExpression() throws IOException {
    // given BPMN model referencing version by ${ver} expression
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("ver", "1");
    runtimeService.startProcessInstanceByKey("taskFormBindingVersion", parameters);

    // when
    List<Deployment> deployments = repositoryService.createDeploymentQuery().list();
    List<CamundaFormDefinition> definitions = findAllCamundaFormDefinitionEntities(processEngineConfiguration);
    Task task = taskService.createTaskQuery().singleResult();
    TaskFormData taskFormData = formService.getTaskFormData(task.getId());
    InputStream deployedForm = formService.getDeployedTaskForm(task.getId());

    // then
    assertThat(deployments).hasSize(1);
    assertThat(definitions).hasSize(1);

    assertTaskFormData(taskFormData, "myTaskForm", "version", 1);

    assertThat(IOUtils.toString(deployedForm, UTF_8)).isEqualTo(getClasspathResourceContent("org/camunda/bpm/engine/test/api/form/task.form"));
  }

  /* START FORMS */

  @Test
  public void shouldRetrieveStartFormBindingLatestWithSingleVersionSeparateDeloyments() throws IOException {
    // given two separate deployments
    deployClasspathResources(true,
        "org/camunda/bpm/engine/test/api/form/RetrieveCamundaFormRefTest.startFormBindingLatest.bpmn",
        "org/camunda/bpm/engine/test/api/form/start.form");

    runtimeService.startProcessInstanceByKey("startFormBindingLatest");

    // when
    List<Deployment> deployments = repositoryService.createDeploymentQuery().list();
    List<CamundaFormDefinition> definitions = findAllCamundaFormDefinitionEntities(processEngineConfiguration);
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    InputStream deployedForm = formService.getDeployedStartForm(processDefinition.getId());

    // then
    assertThat(deployments).hasSize(2);
    assertThat(definitions).hasSize(1);

    assertThat(IOUtils.toString(deployedForm, UTF_8)).isEqualTo(getClasspathResourceContent("org/camunda/bpm/engine/test/api/form/start.form"));
  }

  @Test
  public void shouldRetrieveStartFormBindingLatestWithMultipleVersions() throws IOException {
    // given two versions of the same form
    deployUpdateFormResource(START_FORM_CONTENT_V1, START_FORM_CONTENT_V2, "org/camunda/bpm/engine/test/api/form/RetrieveCamundaFormRefTest.startFormBindingLatest.bpmn");

    runtimeService.startProcessInstanceByKey("startFormBindingLatest");

    // when
    List<Deployment> deployments = repositoryService.createDeploymentQuery().list();
    List<CamundaFormDefinition> definitions = findAllCamundaFormDefinitionEntities(processEngineConfiguration);
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    InputStream deployedForm = formService.getDeployedStartForm(processDefinition.getId());

    // then
    assertThat(deployments).hasSize(2);
    assertThat(definitions).hasSize(2);

    assertThat(IOUtils.toString(deployedForm, UTF_8)).isEqualTo(START_FORM_CONTENT_V2);
  }

  @Test
  public void shouldRetrieveStartFormBindingDeployment() throws IOException {
    // given two versions of the same form
    deployUpdateFormResource(START_FORM_CONTENT_V1, START_FORM_CONTENT_V2, "org/camunda/bpm/engine/test/api/form/RetrieveCamundaFormRefTest.startFormBindingDeployment.bpmn");

    runtimeService.startProcessInstanceByKey("startFormBindingDeployment");

    // when
    List<Deployment> deployments = repositoryService.createDeploymentQuery().list();
    List<CamundaFormDefinition> definitions = findAllCamundaFormDefinitionEntities(processEngineConfiguration);
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    InputStream deployedForm = formService.getDeployedStartForm(processDefinition.getId());

    // then
    assertThat(deployments).hasSize(2);
    assertThat(definitions).hasSize(2);

    assertThat(IOUtils.toString(deployedForm, UTF_8)).isEqualTo(START_FORM_CONTENT_V1);
  }

  @Test
  public void shouldRetrieveStartFormBindingVersionWithMultipleVersions() throws IOException {
    // given two versions of the same form
    deployUpdateFormResource(START_FORM_CONTENT_V1, START_FORM_CONTENT_V2, "org/camunda/bpm/engine/test/api/form/RetrieveCamundaFormRefTest.startFormBindingVersion1.bpmn");

    runtimeService.startProcessInstanceByKey("startFormBindingVersion");

    // when
    List<Deployment> deployments = repositoryService.createDeploymentQuery().list();
    List<CamundaFormDefinition> definitions = findAllCamundaFormDefinitionEntities(processEngineConfiguration);
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    InputStream deployedForm = formService.getDeployedStartForm(processDefinition.getId());

    // then
    assertThat(deployments).hasSize(2);
    assertThat(definitions).hasSize(2);

    assertThat(IOUtils.toString(deployedForm, UTF_8)).isEqualTo(START_FORM_CONTENT_V1);
  }

  @Test
  @org.camunda.bpm.engine.test.Deployment(resources = {"org/camunda/bpm/engine/test/api/form/RetrieveCamundaFormRefTest.startFormBindingLatest.bpmn"})
  public void shouldFailToRetrieveStartFormBindingLatestUnexistingKey() throws IOException {
    // given BPMN model references missing form
    runtimeService.startProcessInstanceByKey("startFormBindingLatest");

    // when
    List<Deployment> deployments = repositoryService.createDeploymentQuery().list();
    List<CamundaFormDefinition> definitions = findAllCamundaFormDefinitionEntities(processEngineConfiguration);
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();

    // then
    assertThat(deployments).hasSize(1);
    assertThat(definitions).hasSize(0);

    assertThatThrownBy(() -> {
      formService.getDeployedStartForm(processDefinition.getId());
    }).isInstanceOf(BadUserRequestException.class)
    .hasMessageContaining("No Camunda Form Definition was found for Camunda Form Ref");
  }

  @Test
  @org.camunda.bpm.engine.test.Deployment(resources = {"org/camunda/bpm/engine/test/api/form/RetrieveCamundaFormRefTest.startFormBindingDeployment.bpmn"})
  public void shouldFailToRetrieveStartFormBindingDeploymentUnexistingKey() throws IOException {
    // given BPMN model references missing form
    runtimeService.startProcessInstanceByKey("startFormBindingDeployment");

    // when
    List<Deployment> deployments = repositoryService.createDeploymentQuery().list();
    List<CamundaFormDefinition> definitions = findAllCamundaFormDefinitionEntities(processEngineConfiguration);
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();

    // then
    assertThat(deployments).hasSize(1);
    assertThat(definitions).hasSize(0);

    assertThatThrownBy(() -> {
      formService.getDeployedStartForm(processDefinition.getId());
    }).isInstanceOf(BadUserRequestException.class)
    .hasMessageContaining("No Camunda Form Definition was found for Camunda Form Ref");
  }

  @Test
  @org.camunda.bpm.engine.test.Deployment(resources = {
      "org/camunda/bpm/engine/test/api/form/RetrieveCamundaFormRefTest.startFormBindingVersion2.bpmn",
      "org/camunda/bpm/engine/test/api/form/start.form" })
  public void shouldFailToRetrieveStartFormBindingVersionUnexistingVersion() throws IOException {
    // given BPMN model references missing form
    runtimeService.startProcessInstanceByKey("startFormBindingVersion");

    // when
    List<Deployment> deployments = repositoryService.createDeploymentQuery().list();
    List<CamundaFormDefinition> definitions = findAllCamundaFormDefinitionEntities(processEngineConfiguration);
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();

    // then
    assertThat(deployments).hasSize(1);
    assertThat(definitions).hasSize(1);

    assertThatThrownBy(() -> {
      formService.getDeployedStartForm(processDefinition.getId());
    }).isInstanceOf(BadUserRequestException.class)
    .hasMessageContaining("No Camunda Form Definition was found for Camunda Form Ref");
  }

  /* HELPER METHODS */

  private void assertTaskFormData(TaskFormData taskFormData, String expectedKey, String expectedBinding, Integer expectedVersion) {
    CamundaFormRef camundaFormRef = taskFormData.getCamundaFormRef();
    assertThat(camundaFormRef.getKey()).isEqualTo(expectedKey);
    assertThat(camundaFormRef.getBinding()).isEqualTo(expectedBinding);
    assertThat(camundaFormRef.getVersion()).isEqualTo(expectedVersion);
    assertThat(taskFormData.getFormKey()).isNull();
  }

  private String getClasspathResourceContent(String path) throws IOException {
    InputStream inputStream = ReflectUtil.getResourceAsStream(path);
    return IOUtils.toString(inputStream, UTF_8);
  }

  private void deployClasspathResources(boolean separateDeployments, String... paths) {
    if (separateDeployments) {
      for (String path : paths) {
        testRule.deploy(path);
      }
    } else {
      testRule.deploy(paths);
    }
  }

  private void deployUpdateFormResource(String v1Content, String v2Content, String... additionalResourcesForFirstDeployment) throws IOException {
    FileInputStream form;
    // deploy BPMN with first version of form
    form = CamundaFormUtils.writeTempFormFile("form.form", v1Content, tempFolder);
    DeploymentBuilder builder = repositoryService.createDeployment().name(getClass().getSimpleName())
        .addInputStream("form", form);
    for (String path : additionalResourcesForFirstDeployment) {
      builder.addClasspathResource(path);
    }
    builder.deploy();

    // deploy second version of form
    form = CamundaFormUtils.writeTempFormFile("form.form", v2Content, tempFolder);
    repositoryService.createDeployment().name(getClass().getSimpleName()).addInputStream("form", form).deploy();
  }
}
