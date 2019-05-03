/*
 * Copyright © 2013-2019 camunda services GmbH and various authors (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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
import javax.naming.NamingException;

import org.camunda.bpm.integrationtest.util.AbstractFoxPlatformIntegrationTest;
import org.camunda.bpm.integrationtest.util.TestConstants;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class PlatformServicesJndiBindingTest extends AbstractFoxPlatformIntegrationTest {

  @Deployment
  public static WebArchive app1() {
    return initWebArchiveDeployment();
  }

  @Test
  public void testProcessApplicationServiceBinding() {
    try {
      InitialContext.doLookup(TestConstants.PROCESS_APPLICATION_SERVICE_JNDI_NAME);
    } catch (NamingException e) {
      Assert.fail("Failed to lookup ProcessApplicationService '" + TestConstants.PROCESS_APPLICATION_SERVICE_JNDI_NAME + "'. Reason: " + e);
    }
  }

  @Test
  public void testProcessEngineServiceBinding() {
    try {
      InitialContext.doLookup(TestConstants.PROCESS_ENGINE_SERVICE_JNDI_NAME);
    } catch (NamingException e) {
      Assert.fail("Failed to lookup ProcessEngineService '" + TestConstants.PROCESS_ENGINE_SERVICE_JNDI_NAME + "'. Reason: " + e);
    }
  }

}
