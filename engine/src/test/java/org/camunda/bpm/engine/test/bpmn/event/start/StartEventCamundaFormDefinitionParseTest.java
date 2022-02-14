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
package org.camunda.bpm.engine.test.bpmn.event.start;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.camunda.bpm.engine.ParseException;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.form.FormDefinition;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.test.TestHelper;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

public class StartEventCamundaFormDefinitionParseTest {

  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  public ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain chain = RuleChain.outerRule(engineRule).around(testRule);

  public RepositoryService repositoryService;
  public ProcessEngineConfigurationImpl processEngineConfiguration;

  @Before
  public void setup() {
    repositoryService = engineRule.getRepositoryService();
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();
  }

  @After
  public void tearDown() {
    for (org.camunda.bpm.engine.repository.Deployment deployment : repositoryService.createDeploymentQuery().list()) {
      repositoryService.deleteDeployment(deployment.getId(), true);
    }
  }

  protected FormDefinition getStartFormDefinition() {
    return getProcessDefinition().getStartFormDefinition();
  }

private ProcessDefinitionEntity getProcessDefinition() {
  ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
  ProcessDefinitionEntity cachedProcessDefinition = processEngineConfiguration.getDeploymentCache()
      .getProcessDefinitionCache().get(processDefinition.getId());
  return cachedProcessDefinition;
}

  @Test
  @Deployment
  public void shouldParseCamundaFormDefinitionVersionBinding() {
    // given a deployed process with a StartEvent containing a Camunda Form definition with version binding
    // then
    FormDefinition startFormDefinition = getStartFormDefinition();

    assertThat(startFormDefinition.getCamundaFormDefinitionKey().getExpressionText()).isEqualTo("formId");
    assertThat(startFormDefinition.getCamundaFormDefinitionBinding()).isEqualTo("version");
    assertThat(startFormDefinition.getCamundaFormDefinitionVersion().getExpressionText()).isEqualTo("1");
  }

  @Test
  @Deployment
  public void shouldParseCamundaFormDefinitionLatestBinding() {
    // given a deployed process with a StartEvent containing a Camunda Form definition with latest binding
    // then
    FormDefinition startFormDefinition = getStartFormDefinition();

    assertThat(startFormDefinition.getCamundaFormDefinitionKey().getExpressionText()).isEqualTo("formId");
    assertThat(startFormDefinition.getCamundaFormDefinitionBinding()).isEqualTo("latest");
  }

  @Test
  @Deployment
  public void shouldParseCamundaFormDefinitionMultipleStartEvents() {
    // given a deployed process with a StartEvent containing a Camunda Form definition with latest binding and another StartEvent inside a subprocess
    // then
    FormDefinition startFormDefinition = getStartFormDefinition();

    assertThat(startFormDefinition.getCamundaFormDefinitionKey().getExpressionText()).isEqualTo("formId");
    assertThat(startFormDefinition.getCamundaFormDefinitionBinding()).isEqualTo("latest");
  }

  @Test
  @Deployment
  public void shouldParseCamundaFormDefinitionDeploymentBinding() {
    // given a deployed process with a StartEvent containing a Camunda Form definition with deployment binding
    // then
    FormDefinition startFormDefinition = getStartFormDefinition();

    assertThat(startFormDefinition.getCamundaFormDefinitionKey().getExpressionText()).isEqualTo("formId");
    assertThat(startFormDefinition.getCamundaFormDefinitionBinding()).isEqualTo("deployment");
  }

  @Test
  public void shouldNotParseCamundaFormDefinitionUnsupportedBinding() {
    // given a deployed process with a UserTask containing a Camunda Form definition with unsupported binding
    String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "shouldNotParseCamundaFormDefinitionUnsupportedBinding");

    // when/then expect parse exception
    assertThatThrownBy(() -> repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy())
      .isInstanceOf(ParseException.class)
      .hasMessageContaining("Invalid element definition: value for formRefBinding attribute has to be one of [deployment, latest, version] but was unsupported");
  }

  public void shouldNotParseCamundaFormDefinitionAndFormKey() {
    // given a deployed process with a UserTask containing a Camunda Form definition and formKey
    String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "shouldNotParseCamundaFormDefinitionAndFormKey");

    // when/then expect parse exception
    assertThatThrownBy(() -> repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy())
      .isInstanceOf(ParseException.class)
      .hasMessageContaining("Invalid element definition: only one of the attributes formKey and formRef is allowed.");
  }
}
