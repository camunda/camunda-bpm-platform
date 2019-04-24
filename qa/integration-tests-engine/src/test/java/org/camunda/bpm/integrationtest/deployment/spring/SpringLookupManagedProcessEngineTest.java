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
package org.camunda.bpm.integrationtest.deployment.spring;

import org.camunda.bpm.integrationtest.util.AbstractFoxPlatformIntegrationTest;
import org.camunda.bpm.integrationtest.util.DeploymentHelper;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * <p>Integration test making sure that we can lookup a managed process engine 
 * and expose it as a Spring Bean inside a Spring Application</p>
 * 
 * @author Daniel Meyer
 *
 */
@RunWith(Arquillian.class)
public class SpringLookupManagedProcessEngineTest extends AbstractFoxPlatformIntegrationTest {

  @Deployment
  public static WebArchive processArchive() {    
    return ShrinkWrap.create(WebArchive.class, "test.war")
      .addClass(AbstractFoxPlatformIntegrationTest.class)
      .addAsWebInfResource("org/camunda/bpm/integrationtest/deployment/spring/SpringLookupManagedProcessEngineTest-context.xml", "applicationContext.xml")
      .addAsLibraries(DeploymentHelper.getEngineSpring())
      .addAsManifestResource("org/camunda/bpm/integrationtest/deployment/spring/jboss-deployment-structure.xml", "jboss-deployment-structure.xml")
      .addAsWebInfResource("org/camunda/bpm/integrationtest/deployment/spring/web.xml", "web.xml");
  }
  
    
  @Test
  public void testDeployProcessArchive() {
    Assert.assertNotNull(processEngine);   
  }
  
}
