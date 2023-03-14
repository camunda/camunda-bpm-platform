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

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.cdi.impl.util.ProgrammaticBeanLookup;
import org.camunda.bpm.integrationtest.util.AbstractFoxPlatformIntegrationTest;
import org.camunda.bpm.integrationtest.util.DeploymentHelper;
import org.camunda.bpm.integrationtest.util.TestContainer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Roman Smirnov
 *
 */
@RunWith(Arquillian.class)
public class TestWarDeploymentDeployChangedOnlyWithJarAsLib extends AbstractFoxPlatformIntegrationTest {

  private static final String PA1 = "PA1";
  private static final String PA2 = "PA2";

  /**
   * <pre>
   *   |-- pa1.war
   *       |-- WEB-INF
   *           |-- lib/
   *               |-- test-v1.jar
   *                   |-- META-INF/processes.xml
   *                   |-- process.bpmn
   * </pre>
   */
  @Deployment(order=1, name=PA1)
  public static WebArchive archive1() {

    JavaArchive processArchiveJar = ShrinkWrap.create(JavaArchive.class, "test-v1.jar")
      .addAsResource("org/camunda/bpm/integrationtest/deployment/war/testDeployProcessArchiveUnchanged.bpmn20.xml", "process.bpmn")
      .addAsResource("META-INF/processes.xml");

    WebArchive archive = ShrinkWrap.create(WebArchive.class, "pa1.war")
        .addAsWebInfResource("org/camunda/bpm/integrationtest/beans.xml", "beans.xml")
        .addAsLibraries(DeploymentHelper.getEngineCdi())

        .addAsLibraries(processArchiveJar)

        .addClass(AbstractFoxPlatformIntegrationTest.class)
        .addClass(TestWarDeploymentDeployChangedOnlyWithJarAsLib.class);

    TestContainer.addContainerSpecificResources(archive);

    return archive;
  }

  /**
   * <pre>
   *   |-- pa2.war
   *       |-- WEB-INF
   *           |-- lib/
   *               |-- test-v2.jar
   *                   |-- META-INF/processes.xml
   *                   |-- process.bpmn
   * </pre>
   */
  @Deployment(order=2, name=PA2)
  public static WebArchive archive2() {

    JavaArchive processArchiveJar = ShrinkWrap.create(JavaArchive.class, "test-v2.jar")
      .addAsResource("org/camunda/bpm/integrationtest/deployment/war/testDeployProcessArchiveUnchanged.bpmn20.xml", "process.bpmn")
      .addAsResource("META-INF/processes.xml");

    WebArchive archive = ShrinkWrap.create(WebArchive.class, "pa2.war")
        .addAsWebInfResource("org/camunda/bpm/integrationtest/beans.xml", "beans.xml")
        .addAsLibraries(DeploymentHelper.getEngineCdi())

        .addAsLibraries(processArchiveJar)

        .addClass(AbstractFoxPlatformIntegrationTest.class)
        .addClass(TestWarDeploymentDeployChangedOnlyWithJarAsLib.class);

    TestContainer.addContainerSpecificResources(archive);

    return archive;
  }

  @Test
  @OperateOnDeployment(value=PA2)
  public void testDeployProcessArchive() {
    ProcessEngine processEngine = ProgrammaticBeanLookup.lookup(ProcessEngine.class);
    Assert.assertNotNull(processEngine);

    RepositoryService repositoryService = processEngine.getRepositoryService();

    long count = repositoryService.createProcessDefinitionQuery()
      .processDefinitionKey("testDeployProcessArchiveUnchanged")
      .count();

    Assert.assertEquals(1, count);
  }

}
