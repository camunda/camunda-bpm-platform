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
package org.camunda.bpm.engine.test.junit5;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(ProcessEngineExtension.class)
@Deployment
class PackagePrivateTest {

  @Test
  void shouldUseClassAnnotation(ProcessEngine processEngine) {
    ProcessDefinition pd = processEngine.getRepositoryService()
        .createProcessDefinitionQuery()
        .processDefinitionKey("testHelperDeploymentTest")
        .singleResult();
    assertThat(pd).isNotNull();
  }

  @Test
  @Deployment
  void shouldOverrideClassAnnotation(ProcessEngine processEngine) {
    ProcessDefinition pd = processEngine.getRepositoryService()
        .createProcessDefinitionQuery()
        .processDefinitionKey("testHelperDeploymentTestOverride")
        .singleResult();
    assertThat(pd).isNotNull();
  }

}
