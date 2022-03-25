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
package org.camunda.bpm.engine.test.assertions.junit5;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.complete;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.task;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.junit5.ProcessEngineExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class ProcessEngineExtensionCompatibilityTest {

  @RegisterExtension
  ProcessEngineExtension extension = ProcessEngineExtension.builder().build();

  RuntimeService runtimeService;

  @BeforeEach
  public void setup() {
    runtimeService = extension.getRuntimeService();
  }

  @Test
  @Deployment(resources = {"simpleProcess.bpmn"})
  public void shouldRunWithJUnit5Extension() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testProcess");

    // then
    assertThat(processInstance).isActive();
    assertThat(runtimeService.createProcessInstanceQuery().count()).isOne();
    assertThat(task(processInstance)).isNotNull();

    // when
    complete(task(processInstance));

    // then
    assertThat(processInstance).isEnded();
  }
}
