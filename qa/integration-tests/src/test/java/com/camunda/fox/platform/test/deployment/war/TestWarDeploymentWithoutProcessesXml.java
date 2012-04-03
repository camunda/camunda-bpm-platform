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
import org.activiti.cdi.impl.util.ProgrammaticBeanLookup;
import org.activiti.engine.ProcessEngine;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.camunda.fox.platform.test.util.AbstractFoxPlatformIntegrationTest;
import com.camunda.fox.platform.test.util.DeploymentHelper;

/**
 * Assert that we can deploy a WAR which bundles 
 * the client but does not include a processes.xml
 * 
 */
@RunWith(Arquillian.class)
public class TestWarDeploymentWithoutProcessesXml extends AbstractFoxPlatformIntegrationTest {

  @Deployment
  public static WebArchive processArchive() {    
    return ShrinkWrap.create(WebArchive.class, "test.war")
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
            .addAsLibraries(DeploymentHelper.getFoxPlatformClient())
            .addClass(AbstractFoxPlatformIntegrationTest.class);    
  }
  
  @Test
  public void testDeployProcessArchive() {
    Assert.assertNotNull(ProgrammaticBeanLookup.lookup(ProcessEngine.class));
  }

}
