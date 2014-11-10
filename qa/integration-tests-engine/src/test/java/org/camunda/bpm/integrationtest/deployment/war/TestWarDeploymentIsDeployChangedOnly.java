/**
 * Copyright (C) 2011, 2012 camunda services GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.integrationtest.deployment.war;
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
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(Arquillian.class)
public class TestWarDeploymentIsDeployChangedOnly extends AbstractFoxPlatformIntegrationTest {

  private static final String PA1 = "PA1";
  private static final String PA2 = "PA2";

  @Deployment(order=1, name=PA1)
  public static Archive<?> processArchive1() {
    WebArchive archive = initWebArchiveDeployment("pa1.war")
            .addAsResource("org/camunda/bpm/integrationtest/deployment/war/testDeployProcessArchiveV1.bpmn20.xml");

    return processArchiveDeployment(archive);
  }

  @Deployment(order=2, name=PA2)
  public static Archive<?> processArchive2() {
    WebArchive archive = initWebArchiveDeployment("pa2.war", "org/camunda/bpm/integrationtest/deployment/war/isDeployChangedOnly_processes.xml")
        .addAsResource("org/camunda/bpm/integrationtest/deployment/war/testDeployProcessArchiveV1.bpmn20.xml")
        .addAsResource("org/camunda/bpm/integrationtest/deployment/war/testProcess.bpmn20.xml");

    return processArchiveDeployment(archive);
  }

  @Test
  @OperateOnDeployment(value=PA2)
  public void testDeployProcessArchive() {
    Assert.assertNotNull(processEngine);
    RepositoryService repositoryService = processEngine.getRepositoryService();
    long count = repositoryService.createProcessDefinitionQuery()
      .processDefinitionKey("testDeployProcessArchive")
      .count();

    Assert.assertEquals(1, count);

    // validate registrations:
    ProcessApplicationService processApplicationService = BpmPlatform.getProcessApplicationService();
    Set<String> processApplicationNames = processApplicationService.getProcessApplicationNames();
    boolean resumedRegistrationFound = false;
    for (String paName : processApplicationNames) {
      ProcessApplicationInfo processApplicationInfo = processApplicationService.getProcessApplicationInfo(paName);
      List<ProcessApplicationDeploymentInfo> deploymentInfo = processApplicationInfo.getDeploymentInfo();
      if(deploymentInfo.size() == 2) {
        if(resumedRegistrationFound) {
          Assert.fail("Cannot have two registrations");
        }
        resumedRegistrationFound = true;
      }
    }
    Assert.assertTrue("Previous version of the deployment was not resumed", resumedRegistrationFound);

  }

}
