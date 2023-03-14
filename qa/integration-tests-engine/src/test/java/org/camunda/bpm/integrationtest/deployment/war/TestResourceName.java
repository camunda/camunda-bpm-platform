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
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinitionQuery;
import org.camunda.bpm.integrationtest.util.AbstractFoxPlatformIntegrationTest;
import org.camunda.bpm.integrationtest.util.DeploymentHelper;
import org.camunda.bpm.integrationtest.util.TestContainer;
import org.camunda.bpm.integrationtest.util.TestHelper;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Roman Smirnov
 *
 */
@RunWith(Arquillian.class)
public class TestResourceName extends AbstractFoxPlatformIntegrationTest {

  public final static String PROCESSES_XML =
      "<process-application xmlns=\"http://www.camunda.org/schema/1.0/ProcessApplication\">" +

        "<process-archive name=\"PA_NAME\">" +
          "<properties>" +
            "<property name=\"isDeleteUponUndeploy\">true</property>" +
          "</properties>" +
        "</process-archive>" +

      "</process-application>";

  public final static String PROCESSES_XML_WITH_RESOURCE_ROOT_PATH =
      "<process-application xmlns=\"http://www.camunda.org/schema/1.0/ProcessApplication\">" +

        "<process-archive name=\"PA_NAME\">" +
          "<properties>" +
            "<property name=\"isDeleteUponUndeploy\">true</property>" +
            "<property name=\"resourceRootPath\">RESOURCE_ROOT_PATH</property>" +
          "</properties>" +
        "</process-archive>" +

      "</process-application>";


  /**
   * <pre>
   *   |-- test.war
   *       |-- WEB-INF
   *           |-- classes
   *               |-- alternateDirectory/process4.bpmn
   *               |-- alternateDirectory/subDirectory/process5.bpmn
   *           |-- lib/
   *               |-- pa0.jar
   *                   |-- META-INF/processes.xml
   *                   |-- process0.bpmn
   *               |-- pa1.jar
   *                   |-- META-INF/processes.xml
   *                   |-- processes/process1.bpmn
   *               |-- pa2.jar
   *                   |-- META-INF/processes.xml                resourceRootPath: pa:directory
   *                   |-- directory/process2.bpmn
   *                   |-- directory/subDirectory/process3.bpmn
   *               |-- pa3.jar
   *                   |-- META-INF/processes.xml                resourceRootPath: classpath:alternateDirectory
   * </pre>
   */
  @Deployment
  public static WebArchive processArchive() {

    Asset pa1ProcessesXml = TestHelper.getStringAsAssetWithReplacements(
        PROCESSES_XML,
        new String[][]{new String[]{"PA_NAME","PA0"}});

    Asset pa2ProcessesXml = TestHelper.getStringAsAssetWithReplacements(
        PROCESSES_XML,
        new String[][]{new String[]{"PA_NAME","PA1"}});

    Asset pa3ProcessesXml = TestHelper.getStringAsAssetWithReplacements(
        PROCESSES_XML_WITH_RESOURCE_ROOT_PATH,
        new String[][]{new String[]{"PA_NAME","PA2"}, new String[]{"RESOURCE_ROOT_PATH", "pa:directory"}});

    Asset pa4ProcessesXml = TestHelper.getStringAsAssetWithReplacements(
        PROCESSES_XML_WITH_RESOURCE_ROOT_PATH,
            new String[][]{new String[]{"PA_NAME","PA3"}, new String[]{"RESOURCE_ROOT_PATH", "classpath:alternateDirectory"}});

    Asset[] processAssets = TestHelper.generateProcessAssets(6);

    JavaArchive pa1 = ShrinkWrap.create(JavaArchive.class, "pa0.jar")
        .addAsResource(pa1ProcessesXml, "META-INF/processes.xml")
        .addAsResource(processAssets[0], "process0.bpmn");

    JavaArchive pa2 = ShrinkWrap.create(JavaArchive.class, "pa1.jar")
        .addAsResource(pa2ProcessesXml, "META-INF/processes.xml")
        .addAsResource(processAssets[1], "processes/process1.bpmn");

    JavaArchive pa3 = ShrinkWrap.create(JavaArchive.class, "pa2.jar")
            .addAsResource(pa3ProcessesXml, "META-INF/processes.xml")
            .addAsResource(processAssets[2], "directory/process2.bpmn")
            .addAsResource(processAssets[3], "directory/subDirectory/process3.bpmn");

    JavaArchive pa4 = ShrinkWrap.create(JavaArchive.class, "pa3.jar")
            .addAsResource(pa4ProcessesXml, "META-INF/processes.xml");

    WebArchive archive = ShrinkWrap.create(WebArchive.class, "test.war")
        .addAsWebInfResource("org/camunda/bpm/integrationtest/beans.xml", "beans.xml")
        .addAsLibraries(DeploymentHelper.getEngineCdi())

        .addAsLibraries(pa1)
        .addAsLibraries(pa2)
        .addAsLibraries(pa3)
        .addAsLibraries(pa4)


        .addAsResource(processAssets[4], "alternateDirectory/process4.bpmn")
        .addAsResource(processAssets[5], "alternateDirectory/subDirectory/process5.bpmn")

        .addClass(AbstractFoxPlatformIntegrationTest.class)
        .addClass(TestContainer.class)
        .addClass(TestResourceName.class);

    TestContainer.addContainerSpecificResources(archive);

    return archive;
  }

  @Test
  public void testResourceName() {
    ProcessEngine processEngine = ProgrammaticBeanLookup.lookup(ProcessEngine.class);
    Assert.assertNotNull(processEngine);

    RepositoryService repositoryService = processEngine.getRepositoryService();

    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();

    ProcessDefinition definition = query.processDefinitionKey("process-0").singleResult();
    Assert.assertEquals("process0.bpmn", definition.getResourceName());

    definition = query.processDefinitionKey("process-1").singleResult();
    Assert.assertEquals("processes/process1.bpmn", definition.getResourceName());

    definition = query.processDefinitionKey("process-2").singleResult();
    Assert.assertEquals("process2.bpmn", definition.getResourceName());

    definition = query.processDefinitionKey("process-3").singleResult();
    Assert.assertEquals("subDirectory/process3.bpmn", definition.getResourceName());

    definition = query.processDefinitionKey("process-4").singleResult();
    Assert.assertEquals("process4.bpmn", definition.getResourceName());

    definition = query.processDefinitionKey("process-5").singleResult();
    Assert.assertEquals("subDirectory/process5.bpmn", definition.getResourceName());
  }

}
