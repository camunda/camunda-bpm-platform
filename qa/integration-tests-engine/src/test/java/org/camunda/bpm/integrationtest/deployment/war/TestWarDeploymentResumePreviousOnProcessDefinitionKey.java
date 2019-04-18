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
package org.camunda.bpm.integrationtest.deployment.war;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Set;

import org.camunda.bpm.BpmPlatform;
import org.camunda.bpm.ProcessApplicationService;
import org.camunda.bpm.application.ProcessApplicationDeploymentInfo;
import org.camunda.bpm.application.ProcessApplicationInfo;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.integrationtest.util.AbstractFoxPlatformIntegrationTest;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class TestWarDeploymentResumePreviousOnProcessDefinitionKey  extends AbstractFoxPlatformIntegrationTest {

  private static final String PA1 = "PA1";
  private static final String PA2 = "PA2";

  @Deployment(order=1, name=PA1)
  public static WebArchive processArchive1() {
    return initWebArchiveDeployment("pa1.war")
      .addAsResource("org/camunda/bpm/integrationtest/deployment/war/testDeployProcessArchiveV1.bpmn20.xml");
  }

  @Deployment(order=2, name=PA2)
  public static WebArchive processArchive2() {
    return initWebArchiveDeployment("pa2.war", "org/camunda/bpm/integrationtest/deployment/war/resumePreviousVersionsOnProcessDefinitionKey_processes.xml")
      .addAsResource("org/camunda/bpm/integrationtest/deployment/war/testDeployProcessArchiveV2.bpmn20.xml");

  }

  @Test
  @OperateOnDeployment(value=PA2)
  public void testDeployProcessArchive() {
    assertThat(processEngine, is(notNullValue()));
    RepositoryService repositoryService = processEngine.getRepositoryService();
    long count = repositoryService.createProcessDefinitionQuery()
      .processDefinitionKey("testDeployProcessArchive")
      .count();

    assertThat(count, is(2L));

    // validate registrations:
    ProcessApplicationService processApplicationService = BpmPlatform.getProcessApplicationService();
    Set<String> processApplicationNames = processApplicationService.getProcessApplicationNames();
    // we have two PAs, one from the first deployment and one from the second and only one (the second) is allowed to have two deployments
    boolean resumedRegistrationFound = false;
    for (String paName : processApplicationNames) {
      ProcessApplicationInfo processApplicationInfo = processApplicationService.getProcessApplicationInfo(paName);
      List<ProcessApplicationDeploymentInfo> deploymentInfo = processApplicationInfo.getDeploymentInfo();
      if(deploymentInfo.size() == 2) {
        if(resumedRegistrationFound) {
          fail("Cannot have two registrations");
        }
        resumedRegistrationFound = true;
      }
    }
    assertThat("Previous version of the deployment was not resumed", resumedRegistrationFound, is(true));
  }
}