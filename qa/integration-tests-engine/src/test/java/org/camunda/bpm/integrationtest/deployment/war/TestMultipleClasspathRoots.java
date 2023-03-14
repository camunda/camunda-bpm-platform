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
public class TestMultipleClasspathRoots extends AbstractFoxPlatformIntegrationTest {

  public final static String PROCESSES_XML =
      "<process-application xmlns=\"http://www.camunda.org/schema/1.0/ProcessApplication\">" +

        "<process-archive name=\"PA_NAME\">" +
          "<properties>" +
            "<property name=\"isDeleteUponUndeploy\">true</property>" +
            "<property name=\"resourceRootPath\">classpath:directory</property>" +
          "</properties>" +
        "</process-archive>" +

      "</process-application>";

  /**
   * <pre>
   *   |-- test.war
   *       |-- WEB-INF
   *           |-- classes
   *               |-- META-INF/processes.xml                   resourceRootPath: classpath:directory
   *               |-- directory/processes/process.bpmn         (1)
   *           |-- lib/
   *               |-- pa0.jar
   *                   |-- directory/processes/process.bpmn     (2)
   * </pre>
   *
   * Processes (1) + (2) will have the same resource name (= "processes/process.bpmn"),
   * so that only one process should be deployed.
   *
   */
  @Deployment
  public static WebArchive processArchive() {

    Asset paProcessesXml = TestHelper.getStringAsAssetWithReplacements(
            PROCESSES_XML,
            new String[][]{new String[]{"PA_NAME","PA0"}});


    Asset[] processAssets = TestHelper.generateProcessAssets(2);

    JavaArchive pa0 = ShrinkWrap.create(JavaArchive.class, "pa0.jar")
        .addAsResource(processAssets[0], "directory/processes/process.bpmn");

    WebArchive archive = ShrinkWrap.create(WebArchive.class, "test.war")
        .addAsWebInfResource("org/camunda/bpm/integrationtest/beans.xml", "beans.xml")
        .addAsLibraries(DeploymentHelper.getEngineCdi())

        .addAsLibraries(pa0)

        .addAsResource(paProcessesXml, "META-INF/processes.xml")
        .addAsResource(processAssets[1], "directory/processes/process.bpmn")

        .addClass(AbstractFoxPlatformIntegrationTest.class)
        .addClass(TestResourceName.class);

    TestContainer.addContainerSpecificResources(archive);

    return archive;
  }

  @Test
  public void testMultipleClasspathRoots() {
    ProcessEngine processEngine = ProgrammaticBeanLookup.lookup(ProcessEngine.class);
    Assert.assertNotNull(processEngine);

    RepositoryService repositoryService = processEngine.getRepositoryService();

    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();

    long count = query.count();
    Assert.assertEquals(1, count);
  }

}
