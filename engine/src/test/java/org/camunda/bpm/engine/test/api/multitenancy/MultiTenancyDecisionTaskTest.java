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

package org.camunda.bpm.engine.test.api.multitenancy;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.history.HistoricDecisionInstanceQuery;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.repository.DecisionDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

public class MultiTenancyDecisionTaskTest extends PluggableProcessEngineTestCase {

  protected static final String TENANT_ONE = "tenant1";
  protected static final String TENANT_TWO = "tenant2";

  protected static final String DMN_FILE = "org/camunda/bpm/engine/test/api/multitenancy/simpleDecisionTable.dmn";
  protected static final String DMN_FILE_VERSION_TWO = "org/camunda/bpm/engine/test/api/multitenancy/simpleDecisionTable_v2.dmn";

  public void testEvaluateDecisionTaskWithDeploymentBinding() {

    BpmnModelInstance process = Bpmn.createExecutableProcess("process")
        .startEvent()
        .businessRuleTask()
          .camundaDecisionRef("decision")
          .camundaDecisionRefBinding("deployment")
          .camundaMapDecisionResult("singleEntry")
          .camundaResultVariable("decisionVar")
        .camundaAsyncAfter()
        .endEvent()
        .done();

    deploymentForTenant(TENANT_ONE, DMN_FILE, process);
    deploymentForTenant(TENANT_TWO, DMN_FILE_VERSION_TWO, process);

    ProcessInstance processInstanceOne = runtimeService.createProcessInstanceByKey("process")
      .setVariable("status", "gold")
      .processDefinitionTenantId(TENANT_ONE).execute();

    ProcessInstance processInstanceTwo = runtimeService.createProcessInstanceByKey("process")
      .setVariable("status", "gold")
      .processDefinitionTenantId(TENANT_TWO).execute();

    assertThat((String)runtimeService.getVariable(processInstanceOne.getId(), "decisionVar"), is("A"));
    assertThat((String)runtimeService.getVariable(processInstanceTwo.getId(), "decisionVar"), is("C"));
  }

  public void testEvaluateDecisionTaskWithLatestBindingSameVersion() {

    BpmnModelInstance process = Bpmn.createExecutableProcess("process")
        .startEvent()
        .businessRuleTask()
          .camundaDecisionRef("decision")
          .camundaDecisionRefBinding("latest")
          .camundaMapDecisionResult("singleEntry")
          .camundaResultVariable("decisionVar")
        .camundaAsyncAfter()
        .endEvent()
        .done();

    deploymentForTenant(TENANT_ONE, DMN_FILE, process);
    deploymentForTenant(TENANT_TWO, DMN_FILE_VERSION_TWO, process);

    ProcessInstance processInstanceOne = runtimeService.createProcessInstanceByKey("process")
      .setVariable("status", "gold")
      .processDefinitionTenantId(TENANT_ONE).execute();

    ProcessInstance processInstanceTwo = runtimeService.createProcessInstanceByKey("process")
      .setVariable("status", "gold")
      .processDefinitionTenantId(TENANT_TWO).execute();

    assertThat((String)runtimeService.getVariable(processInstanceOne.getId(), "decisionVar"), is("A"));
    assertThat((String)runtimeService.getVariable(processInstanceTwo.getId(), "decisionVar"), is("C"));
  }

  public void testEvaluateDecisionTaskWithLatestBindingDifferentVersion() {

    BpmnModelInstance process = Bpmn.createExecutableProcess("process")
        .startEvent()
        .businessRuleTask()
          .camundaDecisionRef("decision")
          .camundaDecisionRefBinding("latest")
          .camundaMapDecisionResult("singleEntry")
          .camundaResultVariable("decisionVar")
        .camundaAsyncAfter()
        .endEvent()
        .done();

    deploymentForTenant(TENANT_ONE, DMN_FILE, process);

    deploymentForTenant(TENANT_TWO, DMN_FILE_VERSION_TWO, process);
    deploymentForTenant(TENANT_TWO, DMN_FILE_VERSION_TWO);

    ProcessInstance processInstanceOne = runtimeService.createProcessInstanceByKey("process")
      .setVariable("status", "gold")
      .processDefinitionTenantId(TENANT_ONE).execute();

    ProcessInstance processInstanceTwo = runtimeService.createProcessInstanceByKey("process")
      .setVariable("status", "gold")
      .processDefinitionTenantId(TENANT_TWO).execute();

    assertThat((String)runtimeService.getVariable(processInstanceOne.getId(), "decisionVar"), is("A"));
    assertThat((String)runtimeService.getVariable(processInstanceTwo.getId(), "decisionVar"), is("C"));

    // check whether DMN_FILE_VERSION_TWO version 2 is really used
    DecisionDefinition latestDecisionDefinitionTenantTwo = repositoryService.createDecisionDefinitionQuery()
        .tenantIdIn(TENANT_TWO).decisionDefinitionKey("decision").latestVersion().singleResult();

    HistoricDecisionInstanceQuery decisionInstanceQuery = historyService.createHistoricDecisionInstanceQuery()
        .tenantIdIn(TENANT_TWO).decisionDefinitionId(latestDecisionDefinitionTenantTwo.getId()).includeOutputs();

    assertThat(decisionInstanceQuery.singleResult().getOutputs().size(), is(1));
    assertThat((String)decisionInstanceQuery.singleResult().getOutputs().iterator().next().getValue(), is("C"));

  }

  public void testEvaluateDecisionTaskWithVersionBinding() {

    BpmnModelInstance process = Bpmn.createExecutableProcess("process")
        .startEvent()
        .businessRuleTask()
          .camundaDecisionRef("decision")
          .camundaDecisionRefBinding("version")
          .camundaDecisionRefVersion("1")
          .camundaMapDecisionResult("singleEntry")
          .camundaResultVariable("decisionVar")
        .camundaAsyncAfter()
        .endEvent()
        .done();

    deploymentForTenant(TENANT_ONE, DMN_FILE, process);
    deploymentForTenant(TENANT_TWO, DMN_FILE_VERSION_TWO, process);

    deploymentForTenant(TENANT_ONE, DMN_FILE);
    deploymentForTenant(TENANT_TWO, DMN_FILE_VERSION_TWO);

    ProcessInstance processInstanceOne = runtimeService.createProcessInstanceByKey("process")
      .setVariable("status", "gold")
      .processDefinitionTenantId(TENANT_ONE).execute();

    ProcessInstance processInstanceTwo = runtimeService.createProcessInstanceByKey("process")
      .setVariable("status", "gold")
      .processDefinitionTenantId(TENANT_TWO).execute();

    assertThat((String)runtimeService.getVariable(processInstanceOne.getId(), "decisionVar"), is("A"));
    assertThat((String)runtimeService.getVariable(processInstanceTwo.getId(), "decisionVar"), is("C"));
  }

  public void testFailEvaluateDecisionTaskFromOtherTenantWithDeploymentBinding() {

    BpmnModelInstance process = Bpmn.createExecutableProcess("process")
        .startEvent()
        .businessRuleTask()
          .camundaDecisionRef("decision")
          .camundaDecisionRefBinding("deployment")
        .camundaAsyncAfter()
        .endEvent()
        .done();

    deploymentForTenant(TENANT_ONE, process);
    deploymentForTenant(TENANT_TWO, DMN_FILE);

    try {
      runtimeService.createProcessInstanceByKey("process")
        .processDefinitionTenantId(TENANT_ONE)
        .execute();

      fail("expected exception");
    } catch (ProcessEngineException e) {
      assertThat(e.getMessage(), containsString("no decision definition deployed with key = 'decision'"));
    }
  }

  public void testFailEvaluateDecisionTaskFromOtherTenantWithLatestBinding() {

    BpmnModelInstance process = Bpmn.createExecutableProcess("process")
        .startEvent()
        .businessRuleTask()
          .camundaDecisionRef("decision")
          .camundaDecisionRefBinding("latest")
        .camundaAsyncAfter()
        .endEvent()
        .done();

    deploymentForTenant(TENANT_ONE, process);
    deploymentForTenant(TENANT_TWO, DMN_FILE);

    try {
      runtimeService.createProcessInstanceByKey("process")
        .processDefinitionTenantId(TENANT_ONE)
        .execute();

      fail("expected exception");
    } catch (ProcessEngineException e) {
      assertThat(e.getMessage(), containsString("no decision definition deployed with key 'decision'"));
    }
  }

  public void testFailEvaluateDecisionTaskFromOtherTenantWithVersionBinding() {

    BpmnModelInstance process = Bpmn.createExecutableProcess("process")
        .startEvent()
        .businessRuleTask()
          .camundaDecisionRef("decision")
          .camundaDecisionRefBinding("version")
          .camundaDecisionRefVersion("2")
        .camundaAsyncAfter()
        .endEvent()
        .done();

    deploymentForTenant(TENANT_ONE, DMN_FILE, process);

    deploymentForTenant(TENANT_TWO, DMN_FILE);
    deploymentForTenant(TENANT_TWO, DMN_FILE);

    try {
      runtimeService.createProcessInstanceByKey("process")
        .processDefinitionTenantId(TENANT_ONE)
        .execute();

      fail("expected exception");
    } catch (ProcessEngineException e) {
      assertThat(e.getMessage(), containsString("no decision definition deployed with key = 'decision', version = '2' and tenant-id 'tenant1'"));
    }
  }

  protected String deploymentForTenant(String tenantId, String classpathResource, BpmnModelInstance modelInstance) {
    return deployment(repositoryService.createDeployment()
        .tenantId(tenantId)
        .addClasspathResource(classpathResource), modelInstance);
  }

}
