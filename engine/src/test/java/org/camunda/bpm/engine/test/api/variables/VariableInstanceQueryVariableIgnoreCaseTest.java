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

import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.VariableInstanceQueryImpl;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Before;

@Deployment(resources = { "org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml" })
public class VariableInstanceQueryVariableIgnoreCaseTest extends AbstractVariableIgnoreCaseTest<VariableInstanceQueryImpl, VariableInstance> {

  RuntimeService runtimeService;

  @Before
  public void init() {
    runtimeService = engineRule.getRuntimeService();

    runtimeService.startProcessInstanceByKey("oneTaskProcess", VARIABLES);
    instance = runtimeService.createVariableInstanceQuery().singleResult();
  }

  @Override
  protected VariableInstanceQueryImpl createQuery() {
    return (VariableInstanceQueryImpl) runtimeService.createVariableInstanceQuery();
  }

  @Override
  protected void assertThatTwoInstancesAreEqual(VariableInstance one, VariableInstance two) {
    Assertions.assertThat(one.getId()).isEqualTo(two.getId());
  }
}
