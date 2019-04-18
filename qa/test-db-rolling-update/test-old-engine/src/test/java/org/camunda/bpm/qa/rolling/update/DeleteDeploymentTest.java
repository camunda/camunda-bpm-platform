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
package org.camunda.bpm.qa.rolling.update;

import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.qa.upgrade.ScenarioUnderTest;
import org.junit.Test;

/**
 * This test ensures that the old engine can delete an
 * existing deployment with a process instance from the new schema.
 *
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
@ScenarioUnderTest("DeploymentWhichShouldBeDeletedScenario")
public class DeleteDeploymentTest extends AbstractRollingUpdateTestCase {

  @Test
  @ScenarioUnderTest("init.1")
  public void testCompleteProcessWithUserTask() {
    //given deployed process with process instance
    String processDefinitionId = rule.processInstance().getProcessDefinitionId();
    ProcessDefinition procDef = rule.getRepositoryService()
                                    .createProcessDefinitionQuery()
                                    .processDefinitionId(processDefinitionId)
                                    .singleResult();

    //when deployment is deleted
    rule.getRepositoryService().deleteDeployment(procDef.getDeploymentId(),true);
    //then process instance is ended
    rule.assertScenarioEnded();
  }

}
