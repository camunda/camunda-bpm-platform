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
package org.camunda.bpm.integrationtest.jboss;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * <p>Ensures subsystem boots in domain mode</p>
 *
 * @author Daniel Meyer
 * @author Christian Lipphardt
 *
 */
@RunWith(Arquillian.class)
public class TestManagedDomain_JBOSS {

  @Deployment @TargetsContainer("test-domain")
  public static WebArchive create1() {
      return ShrinkWrap.create(WebArchive.class, "test.war");
  }

  @Test
  public void shouldBeAbleToLookupDefaultProcessEngine() {
    try {
      Assert.assertNotNull(InitialContext.doLookup("java:global/camunda-bpm-platform/process-engine/default"));
    } catch (NamingException e) {
      Assert.fail("Could not lookup default process engine");
    }

    try {
      Assert.assertNotNull(InitialContext.doLookup("java:global/camunda-bpm-platform/process-engine/someNonExistingEngine"));
      Assert.fail("Should not be able to lookup someNonExistingEngine process engine");
    } catch (NamingException e) {
      // expected
    }
  }

}
