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
package org.camunda.bpm.integrationtest.deployment.ear;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.cdi.impl.util.ProgrammaticBeanLookup;
import org.camunda.bpm.integrationtest.util.AbstractFoxPlatformIntegrationTest;
import org.camunda.bpm.integrationtest.util.DeploymentHelper;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 * This test verifies that a process archive packaging the Camunda Platform client
 * can be packaged inside an EAR application.
 *
 * @author Daniel Meyer
 *
 */
@RunWith(Arquillian.class)
public class TestFoxPlatformClientAsEjbModule_twoPasAsLib extends AbstractFoxPlatformIntegrationTest {


  /**
   * Deployment layout
   *
   * test-application.ear
   *    |-- lib /
   *        |-- processes1.jar
   *          |-- META-INF/processes.xml
   *          |-- org/camunda/bpm/integrationtest/deployment/ear/process1.bpmn20.xml
   *        |-- processes2.jar
   *          |-- META-INF/processes.xml
   *          |-- org/camunda/bpm/integrationtest/deployment/ear/process2.bpmn20.xml
   *
   *    |-- fox-platform-client.jar  <<===============================||
   *                                                                  ||  Class-Path reference
   *    |-- test.war (contains the test-class but also processes)     ||
   *        |-- META-INF/MANIFEST.MF =================================||
   *        |-- WEB-INF/beans.xml
   *        |-- + test classes
   *
   */
  @Deployment
  public static EnterpriseArchive twoPasAsLib() {

    JavaArchive processArchive1Jar = ShrinkWrap.create(JavaArchive.class, "processes1.jar")
      .addAsResource("org/camunda/bpm/integrationtest/deployment/ear/process1.bpmn20.xml")
      .addAsResource("org/camunda/bpm/integrationtest/deployment/ear/pa1.xml", "META-INF/processes.xml");

    JavaArchive processArchive2Jar = ShrinkWrap.create(JavaArchive.class, "processes.jar")
      .addAsResource("org/camunda/bpm/integrationtest/deployment/ear/process2.bpmn20.xml")
      .addAsResource("org/camunda/bpm/integrationtest/deployment/ear/pa2.xml", "META-INF/processes.xml");

    JavaArchive foxPlatformClientJar = DeploymentHelper.getEjbClient();

    WebArchive testJar = ShrinkWrap.create(WebArchive.class, "client-test.war")
      .addAsWebInfResource("org/camunda/bpm/integrationtest/beans.xml", "beans.xml")
      .setManifest(new ByteArrayAsset(("Class-Path: " + foxPlatformClientJar.getName()+"\n").getBytes()))
      .addClass(AbstractFoxPlatformIntegrationTest.class)
      .addClass(TestFoxPlatformClientAsEjbModule_twoPasAsLib.class);

    return ShrinkWrap.create(EnterpriseArchive.class, "twoPasAsLib.ear")
      .addAsLibrary(processArchive1Jar)
      .addAsLibrary(processArchive2Jar)
      .addAsModule(foxPlatformClientJar)
      .addAsModule(testJar)
      .addAsLibrary(DeploymentHelper.getEngineCdi());
  }

  @Test
  public void testTwoPasAsLib() {
    ProcessEngine processEngine = ProgrammaticBeanLookup.lookup(ProcessEngine.class);
    Assert.assertNotNull(processEngine);
    RepositoryService repositoryService = processEngine.getRepositoryService();
    long count = repositoryService.createProcessDefinitionQuery()
      .processDefinitionKey("process1")
      .count();
    Assert.assertEquals(1, count);

    count = repositoryService.createProcessDefinitionQuery()
      .processDefinitionKey("process2")
      .count();
    Assert.assertEquals(1, count);
  }

}
