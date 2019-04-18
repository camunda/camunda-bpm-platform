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

import org.camunda.bpm.integrationtest.util.AbstractFoxPlatformIntegrationTest;
import org.camunda.bpm.integrationtest.util.TestHelper;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;


/**
 * @author Falko Menge
 */
@RunWith(Arquillian.class)
public class TestWarDeploymentWithDiagram extends AbstractFoxPlatformIntegrationTest {

  @Deployment
  public static WebArchive processArchive() {    
    return initWebArchiveDeployment()
            .addClass(TestHelper.class)
            .addAsResource("org/camunda/bpm/integrationtest/testDeployProcessArchive.bpmn20.xml")
            .addAsResource("org/camunda/bpm/integrationtest/testDeployProcessArchive.png")
            .addAsResource("org/camunda/bpm/integrationtest/invoice-it.bpmn20.xml")
            .addAsResource("org/camunda/bpm/integrationtest/invoice-it.jpg");
  }
  
  @Test
  public void testDeployProcessArchive() throws IOException {
    String expectedDiagramResource = "/org/camunda/bpm/integrationtest/testDeployProcessArchive.png";
    String processDefinitionKey = "testDeployProcessArchive";
    TestHelper.assertDiagramIsDeployed(true, getClass(), expectedDiagramResource, processDefinitionKey);
  }

  @Test
  public void testInvoiceProcess() throws IOException {
    String expectedDiagramResource = "/org/camunda/bpm/integrationtest/invoice-it.jpg";
    String processDefinitionKey = "invoice-it";
    TestHelper.assertDiagramIsDeployed(true, getClass(), expectedDiagramResource, processDefinitionKey);
  }
  
}
