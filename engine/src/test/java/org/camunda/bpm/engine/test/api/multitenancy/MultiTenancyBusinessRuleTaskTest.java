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
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

public class MultiTenancyBusinessRuleTaskTest extends PluggableProcessEngineTestCase {

  protected static final String TENANT_ONE = "tenant1";
  protected static final String TENANT_TWO = "tenant2";

  protected static final String DMN_FILE = "org/camunda/bpm/engine/test/api/multitenancy/simpleDecisionTable.dmn";
  protected static final String DMN_FILE_VERSION_TWO = "org/camunda/bpm/engine/test/api/multitenancy/simpleDecisionTable_v2.dmn";

  protected static final String RESULT_OF_VERSION_ONE = "A";
  protected static final String RESULT_OF_VERSION_TWO = "C";

  public static final String DMN_FILE_VERSION_TAG = "org/camunda/bpm/engine/test/dmn/businessruletask/DmnBusinessRuleTaskTest.testDecisionVersionTagOkay.dmn11.xml";
  public static final String DMN_FILE_VERSION_TAG_TWO = "org/camunda/bpm/engine/test/dmn/businessruletask/DmnBusinessRuleTaskTest.testDecisionVersionTagOkay_v2.dmn11.xml";

  protected static final String RESULT_OF_VERSION_TAG_ONE = "A";
  protected static final String RESULT_OF_VERSION_TAG_TWO = "C";

  public void testEvaluateDecisionWithDeploymentBinding() {

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

    assertThat((String)runtimeService.getVariable(processInstanceOne.getId(), "decisionVar"), is(RESULT_OF_VERSION_ONE));
    assertThat((String)runtimeService.getVariable(processInstanceTwo.getId(), "decisionVar"), is(RESULT_OF_VERSION_TWO));
  }

  public void testEvaluateDecisionWithLatestBindingSameVersion() {

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

    assertThat((String)runtimeService.getVariable(processInstanceOne.getId(), "decisionVar"), is(RESULT_OF_VERSION_ONE));
    assertThat((String)runtimeService.getVariable(processInstanceTwo.getId(), "decisionVar"), is(RESULT_OF_VERSION_TWO));
  }

  public void testEvaluateDecisionWithLatestBindingDifferentVersions() {

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

    deploymentForTenant(TENANT_TWO, DMN_FILE, process);
    deploymentForTenant(TENANT_TWO, DMN_FILE_VERSION_TWO);

    ProcessInstance processInstanceOne = runtimeService.createProcessInstanceByKey("process")
      .setVariable("status", "gold")
      .processDefinitionTenantId(TENANT_ONE).execute();

    ProcessInstance processInstanceTwo = runtimeService.createProcessInstanceByKey("process")
      .setVariable("status", "gold")
      .processDefinitionTenantId(TENANT_TWO).execute();

    assertThat((String)runtimeService.getVariable(processInstanceOne.getId(), "decisionVar"), is(RESULT_OF_VERSION_ONE));
    assertThat((String)runtimeService.getVariable(processInstanceTwo.getId(), "decisionVar"), is(RESULT_OF_VERSION_TWO));
  }

  public void testEvaluateDecisionWithVersionBinding() {

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
    deploymentForTenant(TENANT_ONE, DMN_FILE_VERSION_TWO);

    deploymentForTenant(TENANT_TWO, DMN_FILE_VERSION_TWO, process);
    deploymentForTenant(TENANT_TWO, DMN_FILE);

    ProcessInstance processInstanceOne = runtimeService.createProcessInstanceByKey("process")
      .setVariable("status", "gold")
      .processDefinitionTenantId(TENANT_ONE).execute();

    ProcessInstance processInstanceTwo = runtimeService.createProcessInstanceByKey("process")
      .setVariable("status", "gold")
      .processDefinitionTenantId(TENANT_TWO).execute();

    assertThat((String)runtimeService.getVariable(processInstanceOne.getId(), "decisionVar"), is(RESULT_OF_VERSION_ONE));
    assertThat((String)runtimeService.getVariable(processInstanceTwo.getId(), "decisionVar"), is(RESULT_OF_VERSION_TWO));
  }

  public void testEvaluateDecisionWithVersionTagBinding() {
    // given
    deploymentForTenant(TENANT_ONE, DMN_FILE_VERSION_TAG);
    deployment(Bpmn.createExecutableProcess("process")
        .startEvent()
        .businessRuleTask()
          .camundaDecisionRef("decision")
          .camundaDecisionRefTenantId(TENANT_ONE)
          .camundaDecisionRefBinding("versionTag")
          .camundaDecisionRefVersionTag("0.0.2")
          .camundaMapDecisionResult("singleEntry")
          .camundaResultVariable("decisionVar")
        .endEvent()
          .camundaAsyncBefore()
        .done());

    // when
    ProcessInstance processInstance = runtimeService.createProcessInstanceByKey("process")
        .setVariable("status", "gold")
        .execute();

    // then
    assertThat((String)runtimeService.getVariable(processInstance.getId(), "decisionVar"), is(RESULT_OF_VERSION_TAG_ONE));
  }

  public void testEvaluateDecisionWithVersionTagBinding_ResolveTenantFromDefinition() {
    // given
    BpmnModelInstance process = Bpmn.createExecutableProcess("process")
        .startEvent()
        .businessRuleTask()
          .camundaDecisionRef("decision")
          .camundaDecisionRefBinding("versionTag")
          .camundaDecisionRefVersionTag("0.0.2")
          .camundaMapDecisionResult("singleEntry")
          .camundaResultVariable("decisionVar")
        .endEvent()
          .camundaAsyncBefore()
        .done();

    deploymentForTenant(TENANT_ONE, DMN_FILE_VERSION_TAG, process);
    deploymentForTenant(TENANT_TWO, DMN_FILE_VERSION_TAG_TWO, process);

    ProcessInstance processInstanceOne = runtimeService.createProcessInstanceByKey("process")
      .setVariable("status", "gold")
      .processDefinitionTenantId(TENANT_ONE).execute();

    ProcessInstance processInstanceTwo = runtimeService.createProcessInstanceByKey("process")
      .setVariable("status", "gold")
      .processDefinitionTenantId(TENANT_TWO).execute();

    assertThat((String)runtimeService.getVariable(processInstanceOne.getId(), "decisionVar"), is(RESULT_OF_VERSION_TAG_ONE));
    assertThat((String)runtimeService.getVariable(processInstanceTwo.getId(), "decisionVar"), is(RESULT_OF_VERSION_TAG_TWO));
  }

  public void testFailEvaluateDecisionFromOtherTenantWithDeploymentBinding() {

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

  public void testFailEvaluateDecisionFromOtherTenantWithLatestBinding() {

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

  public void testFailEvaluateDecisionFromOtherTenantWithVersionBinding() {

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

  public void testFailEvaluateDecisionFromOtherTenantWithVersionTagBinding() {
    // given
    BpmnModelInstance process = Bpmn.createExecutableProcess("process")
        .startEvent()
        .businessRuleTask()
        .camundaDecisionRef("decision")
        .camundaDecisionRefBinding("versionTag")
        .camundaDecisionRefVersionTag("0.0.2")
        .camundaMapDecisionResult("singleEntry")
        .camundaResultVariable("result")
        .camundaAsyncAfter()
        .endEvent()
        .done();

    deploymentForTenant(TENANT_ONE, process);

    deploymentForTenant(TENANT_TWO, DMN_FILE_VERSION_TAG);

    try {
      // when
      runtimeService.createProcessInstanceByKey("process")
        .processDefinitionTenantId(TENANT_ONE)
        .execute();

      fail("expected exception");
    } catch (ProcessEngineException e) {
      // then
      assertThat(e.getMessage(), containsString("no decision definition deployed with key = 'decision', versionTag = '0.0.2' and tenant-id 'tenant1': decisionDefinition is null"));
    }
  }

  public void testEvaluateDecisionTenantIdConstant() {

    BpmnModelInstance process = Bpmn.createExecutableProcess("process")
        .startEvent()
        .businessRuleTask()
          .camundaDecisionRef("decision")
          .camundaDecisionRefBinding("latest")
          .camundaDecisionRefTenantId(TENANT_ONE)
          .camundaMapDecisionResult("singleEntry")
          .camundaResultVariable("decisionVar")
        .camundaAsyncAfter()
        .endEvent()
        .done();

    deploymentForTenant(TENANT_ONE, DMN_FILE);
    deploymentForTenant(TENANT_TWO, DMN_FILE_VERSION_TWO);
    deployment(process);

    ProcessInstance processInstanceOne = runtimeService.createProcessInstanceByKey("process")
      .setVariable("status", "gold").execute();

    assertThat((String)runtimeService.getVariable(processInstanceOne.getId(), "decisionVar"), is(RESULT_OF_VERSION_ONE));
  }

  public void testEvaluateDecisionWithoutTenantIdConstant() {

    BpmnModelInstance process = Bpmn.createExecutableProcess("process")
        .startEvent()
        .businessRuleTask()
          .camundaDecisionRef("decision")
          .camundaDecisionRefBinding("latest")
          .camundaDecisionRefTenantId("${null}")
          .camundaMapDecisionResult("singleEntry")
          .camundaResultVariable("decisionVar")
        .camundaAsyncAfter()
        .endEvent()
        .done();

    deployment(DMN_FILE);
    deploymentForTenant(TENANT_ONE, process);
    deploymentForTenant(TENANT_TWO, DMN_FILE_VERSION_TWO);

    ProcessInstance processInstanceOne = runtimeService.createProcessInstanceByKey("process")
      .setVariable("status", "gold").execute();

    assertThat((String)runtimeService.getVariable(processInstanceOne.getId(), "decisionVar"), is(RESULT_OF_VERSION_ONE));
  }

  public void testEvaluateDecisionTenantIdExpression() {

    BpmnModelInstance process = Bpmn.createExecutableProcess("process")
        .startEvent()
        .businessRuleTask()
          .camundaDecisionRef("decision")
          .camundaDecisionRefBinding("latest")
          .camundaDecisionRefTenantId("${'"+TENANT_ONE+"'}")
          .camundaMapDecisionResult("singleEntry")
          .camundaResultVariable("decisionVar")
        .camundaAsyncAfter()
        .endEvent()
        .done();

    deploymentForTenant(TENANT_ONE, DMN_FILE);
    deploymentForTenant(TENANT_TWO, DMN_FILE_VERSION_TWO);
    deployment(process);

    ProcessInstance processInstanceOne = runtimeService.createProcessInstanceByKey("process")
      .setVariable("status", "gold").execute();

    assertThat((String)runtimeService.getVariable(processInstanceOne.getId(), "decisionVar"), is(RESULT_OF_VERSION_ONE));
  }

  public void testEvaluateDecisionTenantIdCompositeExpression() {
    // given
    BpmnModelInstance process = Bpmn.createExecutableProcess("process")
      .startEvent()
      .businessRuleTask()
      .camundaDecisionRef("decision")
      .camundaDecisionRefBinding("latest")
      .camundaDecisionRefTenantId("tenant${'1'}")
      .camundaMapDecisionResult("singleEntry")
      .camundaResultVariable("decisionVar")
      .camundaAsyncAfter()
      .endEvent()
      .done();
    deploymentForTenant(TENANT_ONE, DMN_FILE);
    deploymentForTenant(TENANT_TWO, DMN_FILE_VERSION_TWO);
    deployment(process);

    // when
    ProcessInstance processInstanceOne = runtimeService.createProcessInstanceByKey("process")
      .setVariable("status", "gold").execute();

    // then
    assertThat((String)runtimeService.getVariable(processInstanceOne.getId(), "decisionVar"), is(RESULT_OF_VERSION_ONE));
  }

}
