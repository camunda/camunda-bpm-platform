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
package com.camunda.fox.platform.test.deployment.war;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.activiti.engine.ActivitiException;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.camunda.fox.platform.test.util.AbstractFoxPlatformIntegrationTest;
import com.camunda.fox.platform.test.util.TestHelper;

/**
 * @author Christian Lipphardt
 */
@RunWith(Arquillian.class)
public class TestWarDeploymentWithoutDiagram extends AbstractFoxPlatformIntegrationTest {

  @Deployment
  public static WebArchive processArchive() {    
    return initWebArchiveDeployment()
            .addClass(TestHelper.class)
            .addAsResource("com/camunda/fox/platform/test/testDeployProcessArchive.bpmn20.xml");
  }
  
  @Test
  public void testDeployProcessArchiveDiagramCreationDisabled() throws IOException {
    String expectedDiagramResource = "/com/camunda/fox/platform/test/testDeployProcessArchive.png";
    String processDefinitionKey = "testDeployProcessArchive";
    TestHelper.assertDiagramIsDeployed(false, getClass(), expectedDiagramResource, processDefinitionKey);
  }
    
}
