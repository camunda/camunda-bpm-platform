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
package org.camunda.bpm.qa.upgrade.scenarios7110.timestamp;

import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.qa.upgrade.Origin;
import org.camunda.bpm.qa.upgrade.ScenarioUnderTest;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Nikola Koevski
 */
@ScenarioUnderTest("DeploymentDeployTimeScenario")
@Origin("7.11.0")
public class DeploymentDeployTimeTest extends AbstractTimestampMigrationTest {

  protected static final String DEPLOYMENT_NAME = "DeployTimeDeploymentTest";

  @ScenarioUnderTest("initDeploymentDeployTime.1")
  @Test
  public void testDeployTimeConversion() {
    // when
    Deployment deployment = repositoryService.createDeploymentQuery()
      .deploymentName(DEPLOYMENT_NAME)
      .singleResult();

    // assume
    assertNotNull(deployment);

    // then
    assertThat(deployment.getDeploymentTime(), is(TIMESTAMP));
  }
}