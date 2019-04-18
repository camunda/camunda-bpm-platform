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
package org.camunda.bpm.integrationtest.jobexecutor;

import org.camunda.bpm.integrationtest.jobexecutor.beans.FailingSLSB;
import org.camunda.bpm.integrationtest.util.AbstractFoxPlatformIntegrationTest;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(Arquillian.class)
public class FailedJobCommandTest extends AbstractFoxPlatformIntegrationTest {

  @Deployment
  public static WebArchive createDeployment() {
    return initWebArchiveDeployment()
      .addClass(FailingSLSB.class)
      .addAsResource("org/camunda/bpm/integrationtest/jobexecutor/FailedJobCommandTest.bpmn20.xml");

  }

  @Test
  public void testJobRetriesDecremented() {
    runtimeService.startProcessInstanceByKey("theProcess");

    Assert.assertEquals(1, managementService.createJobQuery().withRetriesLeft().count());

    waitForJobExecutorToProcessAllJobs();

    // now the retries = 0

    Assert.assertEquals(0, managementService.createJobQuery().withRetriesLeft().count());
    Assert.assertEquals(1, managementService.createJobQuery().noRetriesLeft().count());

  }

  @Test
  public void testJobRetriesDecremented_multiple() {

    for(int i = 0; i < 50; i++) {
      runtimeService.startProcessInstanceByKey("theProcess");
    }

    Assert.assertEquals(50, managementService.createJobQuery().withRetriesLeft().count());

    waitForJobExecutorToProcessAllJobs(6 * 60 * 1000);

    // now the retries = 0

    Assert.assertEquals(0, managementService.createJobQuery().withRetriesLeft().count());
    Assert.assertEquals(51, managementService.createJobQuery().noRetriesLeft().count());

  }

}
