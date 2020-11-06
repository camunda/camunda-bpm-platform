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
package org.camunda.bpm.engine.test.api.runtime;

import static org.assertj.core.api.Assertions.assertThat;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Miklas Boskamp
 *
 */
public class ProcessInstanceQueryByLeafInstancesTest {

  @Rule
  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();

  protected RuntimeService runtimeService;

  @Before
  public void init() {
    runtimeService = engineRule.getRuntimeService();
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/runtime/superProcessWithNestedSubProcess.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/runtime/nestedSubProcess.bpmn20.xml", "org/camunda/bpm/engine/test/api/runtime/subProcess.bpmn20.xml" })
  public void testQueryByLeafInstancesThreeLayers() {
    /*
     * nested structure:
     * superProcessWithNestedSubProcess
     * +-- nestedSubProcess
     *     +-- subProcess
     */
    ProcessInstance threeLayerProcess = runtimeService.startProcessInstanceByKey("nestedSubProcessQueryTest");
    ProcessInstanceQuery simpleSubProcessQuery = runtimeService.createProcessInstanceQuery().processDefinitionKey("simpleSubProcess");

    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(3L);
    assertThat(runtimeService.createProcessInstanceQuery().processDefinitionKey("nestedSubProcessQueryTest").count()).isEqualTo(1L);
    assertThat(runtimeService.createProcessInstanceQuery().processDefinitionKey("nestedSimpleSubProcess").count()).isEqualTo(1L);
    assertThat(simpleSubProcessQuery.count()).isEqualTo(1L);

    ProcessInstance instance = runtimeService.createProcessInstanceQuery().leafProcessInstances().singleResult();
    assertThat(instance.getRootProcessInstanceId()).isEqualTo(threeLayerProcess.getId());
    assertThat(instance.getId()).isEqualTo(simpleSubProcessQuery.singleResult().getId());
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/runtime/nestedSubProcess.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/runtime/subProcess.bpmn20.xml" })
  public void testQueryByLeafInstancesTwoLayers() {
    /*
     * nested structure:
     * nestedSubProcess
     * +-- subProcess
     */
    ProcessInstance twoLayerProcess = runtimeService.startProcessInstanceByKey("nestedSimpleSubProcess");
    ProcessInstanceQuery simpleSubProcessQuery = runtimeService.createProcessInstanceQuery().processDefinitionKey("simpleSubProcess");

    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(2L);
    assertThat(runtimeService.createProcessInstanceQuery().processDefinitionKey("nestedSimpleSubProcess").count()).isEqualTo(1L);
    assertThat(simpleSubProcessQuery.count()).isEqualTo(1L);

    ProcessInstance instance = runtimeService.createProcessInstanceQuery().leafProcessInstances().singleResult();
    assertThat(instance.getRootProcessInstanceId()).isEqualTo(twoLayerProcess.getId());
    assertThat(instance.getId()).isEqualTo(simpleSubProcessQuery.singleResult().getId());
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/runtime/subProcess.bpmn20.xml" })
  public void testQueryByLeafInstancesOneLayer() {
    ProcessInstance process = runtimeService.startProcessInstanceByKey("simpleSubProcess");
    ProcessInstanceQuery simpleSubProcessQuery = runtimeService.createProcessInstanceQuery().processDefinitionKey("simpleSubProcess");

    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(1L);
    assertThat(simpleSubProcessQuery.count()).isEqualTo(1L);

    ProcessInstance instance = runtimeService.createProcessInstanceQuery().leafProcessInstances().singleResult();
    assertThat(instance.getRootProcessInstanceId()).isEqualTo(process.getId());
    assertThat(instance.getId()).isEqualTo(simpleSubProcessQuery.singleResult().getId());
  }
}
