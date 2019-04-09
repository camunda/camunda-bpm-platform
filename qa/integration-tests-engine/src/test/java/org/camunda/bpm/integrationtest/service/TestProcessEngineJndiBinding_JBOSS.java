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
package org.camunda.bpm.integrationtest.service;

import javax.naming.InitialContext;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.integrationtest.util.AbstractFoxPlatformIntegrationTest;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * <p>Makes sure that the process engine JNDI bindings are created</p>
 * 
 * @author Daniel Meyer
 *
 */
@RunWith(Arquillian.class)
public class TestProcessEngineJndiBinding_JBOSS extends AbstractFoxPlatformIntegrationTest {

  @Deployment
  public static WebArchive app1() {    
    return initWebArchiveDeployment();
  }
  
  @Test
  public void testDefaultProcessEngineBindingCreated() {
    
    try {
      ProcessEngine processEngine = InitialContext.doLookup("java:global/camunda-bpm-platform/process-engine/default");
      Assert.assertNotNull("Process engine must not be null", processEngine);
      
    } catch(Exception e) {
      Assert.fail("Process Engine not bound in JNDI.");
      
    }
        
  }
  
  

}
