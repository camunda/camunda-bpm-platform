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
package org.camunda.bpm.engine.test.form.deployment;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.List;

import org.assertj.core.util.Arrays;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.repository.CamundaFormDefinition;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.CamundaFormUtils;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class CamundaFormDefinitionDeployerTest {

  protected static final String BPMN_USER_TASK_FORM_REF_DEPLOYMENT = "org/camunda/bpm/engine/test/form/deployment/CamundaFormDefinitionDeployerTest.shouldDeployProcessWithCamundaFormDefinitionBindingDeployment.bpmn";
  protected static final String BPMN_USER_TASK_FORM_REF_LATEST = "org/camunda/bpm/engine/test/form/deployment/CamundaFormDefinitionDeployerTest.shouldDeployProcessWithCamundaFormDefinitionBindingLatest.bpmn";
  protected static final String BPMN_USER_TASK_FORM_REF_VERSION = "org/camunda/bpm/engine/test/form/deployment/CamundaFormDefinitionDeployerTest.shouldDeployProcessWithCamundaFormDefinitionBindingVersion.bpmn";
  protected static final String SIMPLE_FORM = "org/camunda/bpm/engine/test/form/deployment/CamundaFormDefinitionDeployerTest.simple_form.form";

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  RepositoryService repositoryService;
  ProcessEngineConfigurationImpl processEngineConfiguration;

  @Parameter(0)
  public String bpmnResource;

  @Parameters(name = "{0}")
  public static Collection<Object> params() {
    return Arrays.asList(new String[] {
        BPMN_USER_TASK_FORM_REF_DEPLOYMENT,
        BPMN_USER_TASK_FORM_REF_LATEST,
        BPMN_USER_TASK_FORM_REF_VERSION });
  }

  @Before
  public void init() {
    repositoryService = engineRule.getRepositoryService();
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();
  }

  @Test
  public void shouldDeployProcessWithCamundaFormDefinition() {
    String deploymentId = testRule.deploy(bpmnResource, SIMPLE_FORM).getId();

    // there should only be one deployment
    long deploymentCount = repositoryService.createDeploymentQuery().count();
    assertThat(deploymentCount).isEqualTo(1);

    // there should only be one CamundaFormDefinition
    List<CamundaFormDefinition> definitions = CamundaFormUtils.findAllCamundaFormDefinitionEntities(processEngineConfiguration);
    assertThat(definitions).hasSize(1);
    assertThat(definitions.get(0).getDeploymentId()).isEqualTo(deploymentId);
  }
}
