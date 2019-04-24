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

import org.camunda.bpm.integrationtest.util.TestContainer;
import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 * In this test we make sure that if a user deploys a WAR file with a broken
 * .bpmn-XML file, the deployment fails.
 * 
 * @author Daniel Meyer
 * 
 */
@RunWith(Arquillian.class)
public class TestWarDeploymentWithBrokenBpmnXml {
  
  private static final String DEPLOYMENT = "deployment";

  @ArquillianResource
  private Deployer deployer;
  
  @Deployment(managed=false, name=DEPLOYMENT)
  public static WebArchive processArchive() {    
    
    WebArchive deployment = ShrinkWrap.create(WebArchive.class, "test.war")
      .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
      .addAsResource("META-INF/processes.xml", "META-INF/processes.xml")
      .addAsResource("org/camunda/bpm/integrationtest/deployment/war/TestWarDeploymentWithBrokenBpmnXml.testXmlInvalid.bpmn20.xml");
    
    TestContainer.addContainerSpecificResources(deployment);
    
    return deployment;
  }
  
  @Test
  public void testXmlInvalid() {
    try {
      deployer.deploy(DEPLOYMENT);
      Assert.fail("exception expected");
    }catch (Exception e) {
      // expected
    } 
  }

}
