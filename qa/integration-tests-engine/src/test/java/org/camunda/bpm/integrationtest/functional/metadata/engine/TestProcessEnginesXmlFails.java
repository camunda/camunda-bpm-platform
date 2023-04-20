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
package org.camunda.bpm.integrationtest.functional.metadata.engine;

import org.camunda.bpm.integrationtest.util.DeploymentHelper;
import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 * 
 * @author Daniel Meyer
 *
 */
@RunWith(Arquillian.class)
public class TestProcessEnginesXmlFails {

  @ArquillianResource
  private Deployer deployer;
  
  @Deployment(managed=false, name="deployment")
  public static WebArchive processArchive() {    
    
    return  ShrinkWrap.create(WebArchive.class)
            .addAsWebInfResource("org/camunda/bpm/integrationtest/beans.xml", "beans.xml")
            .addAsLibraries(DeploymentHelper.getEjbClient())
            .addAsResource("META-INF/processes.xml", "META-INF/processes.xml")
            .addAsLibraries(
              ShrinkWrap.create(JavaArchive.class, "engine1.jar")
                    .addAsResource("singleEngine.xml", "META-INF/processes.xml"),
              ShrinkWrap.create(JavaArchive.class, "engine2.jar")
                    // we add the same process engine configuration multiple times -> fails
                   .addAsResource("singleEngine.xml", "META-INF/processes.xml")
         );
  }
  
  @Test
  @RunAsClient
  public void testDeployProcessArchive() {
    try {
      deployer.deploy("deployment");
      Assert.fail("exception expected");
    }catch (Exception e) {
      // expected
    }
  }

}
