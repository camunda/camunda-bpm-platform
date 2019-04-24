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
package org.camunda.bpm.integrationtest.functional.spin;

import org.camunda.bpm.BpmPlatform;
import org.camunda.bpm.integrationtest.functional.spin.dataformat.Foo;
import org.camunda.bpm.integrationtest.functional.spin.dataformat.FooDataFormat;
import org.camunda.bpm.integrationtest.functional.spin.dataformat.FooDataFormatProvider;
import org.camunda.bpm.integrationtest.functional.spin.dataformat.FooSpin;
import org.camunda.bpm.integrationtest.util.AbstractFoxPlatformIntegrationTest;
import org.camunda.spin.spi.DataFormatProvider;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Daniel Meyer
 *
 */
@RunWith(Arquillian.class)
public class PaDataFormatAndPostDeployTest extends AbstractFoxPlatformIntegrationTest {

  @Deployment
  public static WebArchive createDeployment() {

    WebArchive archive = ShrinkWrap.create(WebArchive.class, "test.war")
        .addClass(PaDataformatAndPostDeployApp.class)
        .addAsResource("META-INF/processes.xml")
        .addClass(AbstractFoxPlatformIntegrationTest.class)
        .addAsResource("org/camunda/bpm/integrationtest/oneTaskProcess.bpmn")
        .addClass(Foo.class)
        .addClass(FooDataFormat.class)
        .addClass(FooDataFormatProvider.class)
        .addClass(FooSpin.class)
        .addAsServiceProvider(DataFormatProvider.class, FooDataFormatProvider.class);

    return archive;

  }

  @Test
  public void shouldDeployApp() {
    Assert.assertNotNull(BpmPlatform.getProcessApplicationService()
        .getProcessApplicationInfo(PaDataformatAndPostDeployApp.PA_NAME));
  }

}
