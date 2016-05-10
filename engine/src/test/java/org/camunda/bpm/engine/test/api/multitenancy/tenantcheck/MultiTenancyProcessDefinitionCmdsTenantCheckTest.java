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

package org.camunda.bpm.engine.test.api.multitenancy.tenantcheck;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.InputStream;
import java.util.Arrays;

import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.DiagramLayout;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;

/**
 * @author kristin.polenz
 */
public class MultiTenancyProcessDefinitionCmdsTenantCheckTest {

  protected static final String TENANT_ONE = "tenant1";
  protected static final String TENANT_TWO = "tenant2";

  protected static final String BPMN_PROCESS_MODEL = "org/camunda/bpm/engine/test/api/multitenancy/failingTask.bpmn";
  protected static final String BPMN_PROCESS_DIAGRAM = "org/camunda/bpm/engine/test/api/multitenancy/failingTask.png";

  protected ProcessEngineRule engineRule = new ProcessEngineRule(true);

  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  @Rule
  public ExpectedException thrown= ExpectedException.none();

  protected RepositoryService repositoryService;
  protected IdentityService identityService;
  protected ProcessEngineConfiguration processEngineConfiguration;

  protected ProcessDefinition processDefinition;

  @Before
  public void setUp() {
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();
    repositoryService = engineRule.getRepositoryService();
    identityService = engineRule.getIdentityService();

    testRule.deployForTenant(TENANT_ONE, BPMN_PROCESS_MODEL, BPMN_PROCESS_DIAGRAM);

    processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
  }

  @Test
  public void failToGetProcessModelNoAuthenticatedTenants() {
    identityService.setAuthentication("user", null, null);

    // declare expected exception
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot get the process definition");

    repositoryService.getProcessModel(processDefinition.getId());
  }

  @Test
  public void getProcessModelWithAuthenticatedTenant() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    InputStream inputStream = repositoryService.getProcessModel(processDefinition.getId());

    assertThat(inputStream, notNullValue());
  }

  @Test
  public void getProcessModelDisabledTenantCheck() {
    processEngineConfiguration.setTenantCheckEnabled(false);
    identityService.setAuthentication("user", null, null);

    InputStream inputStream = repositoryService.getProcessModel(processDefinition.getId());

    assertThat(inputStream, notNullValue());
  }

  @Test
  public void failToGetProcessDiagramNoAuthenticatedTenants() {
    identityService.setAuthentication("user", null, null);

    // declare expected exception
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot get the process definition");

    repositoryService.getProcessDiagram(processDefinition.getId());
  }

  @Test
  public void getProcessDiagramWithAuthenticatedTenant() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    InputStream inputStream = repositoryService.getProcessDiagram(processDefinition.getId());

    assertThat(inputStream, notNullValue());
  }

  @Test
  public void getProcessDiagramDisabledTenantCheck() {
    processEngineConfiguration.setTenantCheckEnabled(false);
    identityService.setAuthentication("user", null, null);

    InputStream inputStream = repositoryService.getProcessDiagram(processDefinition.getId());

    assertThat(inputStream, notNullValue());
  }

  @Test
  public void failToGetProcessDiagramLayoutNoAuthenticatedTenants() {
    identityService.setAuthentication("user", null, null);

    // declare expected exception
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot get the process definition");

    repositoryService.getProcessDiagramLayout(processDefinition.getId());
  }

  @Test
  public void getProcessDiagramLayoutWithAuthenticatedTenant() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    DiagramLayout diagramLayout = repositoryService.getProcessDiagramLayout(processDefinition.getId());

    assertThat(diagramLayout, notNullValue());
  }

  @Test
  public void getProcessDiagramLayoutDisabledTenantCheck() {
    processEngineConfiguration.setTenantCheckEnabled(false);
    identityService.setAuthentication("user", null, null);

    DiagramLayout diagramLayout = repositoryService.getProcessDiagramLayout(processDefinition.getId());

    assertThat(diagramLayout, notNullValue());
  }

  @Test
  public void failToGetProcessDefinitionNoAuthenticatedTenants() {
    identityService.setAuthentication("user", null, null);

    // declare expected exception
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot get the process definition");

    repositoryService.getProcessDefinition(processDefinition.getId());
  }

  @Test
  public void getProcessDefinitionWithAuthenticatedTenant() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    ProcessDefinition definition = repositoryService.getProcessDefinition(processDefinition.getId());

    assertThat(definition.getTenantId(), is(TENANT_ONE));
  }

  @Test
  public void getProcessDefinitionDisabledTenantCheck() {
    processEngineConfiguration.setTenantCheckEnabled(false);
    identityService.setAuthentication("user", null, null);

    ProcessDefinition definition = repositoryService.getProcessDefinition(processDefinition.getId());

    assertThat(definition.getTenantId(), is(TENANT_ONE));
  }

  @Test
  public void failToGetBpmnModelInstanceNoAuthenticatedTenants() {
    identityService.setAuthentication("user", null, null);

    // declare expected exception
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot get the process definition");

    repositoryService.getBpmnModelInstance(processDefinition.getId());
  }

  @Test
  public void getBpmnModelInstanceWithAuthenticatedTenant() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    BpmnModelInstance modelInstance = repositoryService.getBpmnModelInstance(processDefinition.getId());

    assertThat(modelInstance, notNullValue());
  }

  @Test
  public void getBpmnModelInstanceDisabledTenantCheck() {
    processEngineConfiguration.setTenantCheckEnabled(false);
    identityService.setAuthentication("user", null, null);

    BpmnModelInstance modelInstance = repositoryService.getBpmnModelInstance(processDefinition.getId());

    assertThat(modelInstance, notNullValue());
  }

}
