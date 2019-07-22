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
package org.camunda.bpm.engine.test.api.variables;

import static org.assertj.core.api.Assertions.assertThat;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.ExecutionQueryImpl;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.runtime.Execution;
import org.junit.After;
import org.junit.Before;

public class ExecutionQueryVariableIgnoreCaseTest extends AbstractVariableIgnoreCaseTest<ExecutionQueryImpl, Execution> {

  RepositoryService repositoryService;
  RuntimeService runtimeService;

  @Before
  public void init() {
    repositoryService = engineRule.getRepositoryService();
    runtimeService = engineRule.getRuntimeService();

    repositoryService.createDeployment().addClasspathResource("org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml").deploy();
    instance = runtimeService.startProcessInstanceByKey("oneTaskProcess", VARIABLES);
  }

  @After
  public void tearDown() {
    for (Deployment deployment : repositoryService.createDeploymentQuery().list()) {
      repositoryService.deleteDeployment(deployment.getId(), true);
    }
  }

  @Override
  protected ExecutionQueryImpl createQuery() {
    return (ExecutionQueryImpl) runtimeService.createExecutionQuery();
  }

  @Override
  protected void assertThatTwoInstancesAreEqual(Execution one, Execution two) {
    assertThat(one.getId()).isEqualTo(two.getId());
  }
}
