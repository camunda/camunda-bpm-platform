/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.integrationtest.functional.spin;

import java.util.Set;

import org.camunda.bpm.BpmPlatform;
import org.camunda.bpm.integrationtest.functional.spin.dataformat.FailingJsonDataFormatConfigurator;
import org.camunda.bpm.integrationtest.functional.spin.dataformat.JsonSerializable;
import org.camunda.bpm.integrationtest.util.AbstractFoxPlatformIntegrationTest;
import org.camunda.bpm.integrationtest.util.TestContainer;
import org.camunda.spin.spi.DataFormatConfigurator;
import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Thorben Lindhauer
 *
 */
@RunWith(Arquillian.class)
public class PaDataFormatConfiguratorFailingTest {

  @ArquillianResource
  private Deployer deployer;

  @Deployment(managed = false, name = "deployment")
  public static WebArchive createDeployment() {
    WebArchive webArchive = ShrinkWrap.create(WebArchive.class, "PaDataFormatConfiguratorFailingTest.war")
        .addAsResource("META-INF/processes.xml")
        .addClass(AbstractFoxPlatformIntegrationTest.class)
        .addClass(TestContainer.class)
        .addClass(ReferenceStoringProcessApplication.class)
        .addAsResource("org/camunda/bpm/integrationtest/oneTaskProcess.bpmn")
        .addClass(JsonSerializable.class)
        .addClass(FailingJsonDataFormatConfigurator.class)
        .addAsServiceProvider(DataFormatConfigurator.class, FailingJsonDataFormatConfigurator.class);

    TestContainer.addSpinJacksonJsonDataFormat(webArchive);

    return webArchive;

  }

  @Deployment(name = "checkDeployment")
  public static WebArchive createCheckDeployment() {
    WebArchive webArchive = ShrinkWrap.create(WebArchive.class);
    TestContainer.addContainerSpecificResourcesForNonPa(webArchive);
    return webArchive;
  }

  @Test
  @InSequence(1)
  public void testFailingConfiguratorPreventsProcessApplicationDeployment() {
    try {
      deployer.deploy("deployment");
      Assert.fail("exception expected");
    }catch (Exception e) {
      // expected
    }
  }

  @Test
  @InSequence(2)
  @OperateOnDeployment("checkDeployment")
  public void testNoProcessApplicationIsDeployed() {
    Set<String> registeredPAs = BpmPlatform.getProcessApplicationService().getProcessApplicationNames();
    Assert.assertTrue(registeredPAs.isEmpty());
  }
}
