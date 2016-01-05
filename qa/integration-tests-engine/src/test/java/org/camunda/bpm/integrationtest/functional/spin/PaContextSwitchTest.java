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

import java.util.concurrent.Callable;

import org.camunda.bpm.application.ProcessApplicationContext;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.variable.value.SerializableValue;
import org.camunda.bpm.integrationtest.functional.spin.dataformat.JsonDataFormatConfigurator;
import org.camunda.bpm.integrationtest.functional.spin.dataformat.JsonSerializable;
import org.camunda.bpm.integrationtest.util.AbstractFoxPlatformIntegrationTest;
import org.camunda.bpm.integrationtest.util.DeploymentHelper;
import org.camunda.bpm.integrationtest.util.TestContainer;
import org.camunda.spin.spi.DataFormatConfigurator;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Thorben Lindhauer
 *
 */
@Ignore
@RunWith(Arquillian.class)
public class PaContextSwitchTest extends AbstractFoxPlatformIntegrationTest {

  @Deployment(name = "pa1")
  public static WebArchive createDeployment1() {
    WebArchive webArchive = ShrinkWrap.create(WebArchive.class, "pa1.war")
        .addAsResource("META-INF/processes.xml")
        .addClass(AbstractFoxPlatformIntegrationTest.class)
        .addClass(TestContainer.class)
        .addClass(ProcessApplication1.class)
        .addClass(JsonSerializable.class)
        .addClass(RuntimeServiceDelegate.class)
        .addAsResource("org/camunda/bpm/integrationtest/functional/spin/paContextSwitch.bpmn20.xml")
        .addClass(JsonDataFormatConfigurator.class)
        .addAsServiceProvider(DataFormatConfigurator.class, JsonDataFormatConfigurator.class);

    TestContainer.addSpinJacksonJsonDataFormat(webArchive);

    return webArchive;
  }

  @Deployment(name = "pa2")
  public static WebArchive createDeployment2() {
    WebArchive webArchive = ShrinkWrap.create(WebArchive.class, "pa2.war")
        .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
        .addAsLibraries(DeploymentHelper.getEngineCdi())
        .addAsResource("META-INF/processes.xml")
        .addClass(AbstractFoxPlatformIntegrationTest.class)
        .addClass(TestContainer.class)
        .addClass(ProcessApplication2.class);

    return webArchive;
  }

  /**
   * This test ensures that when the {@link ProcessApplicationContext} API is used,
   * the context switch is only performed for outer-most command and not if a second, nested
   * command is executed; => in nested commands, the engine is already in the correct context
   */
  @Test
  @OperateOnDeployment("pa1")
  public void testNoContextSwitchOnInnerCommand() throws Exception {

    ProcessInstance pi = ProcessApplicationContext.executeInProcessApplication(new Callable<ProcessInstance>() {

      @Override
      public ProcessInstance call() throws Exception {
        return runtimeService.startProcessInstanceByKey("process");
      }

    }, "pa2");

    JsonSerializable expectedJsonSerializable = RuntimeServiceDelegate.createJsonSerializable();
    String expectedJsonString = expectedJsonSerializable.toExpectedJsonString(JsonDataFormatConfigurator.DATE_FORMAT);

    SerializableValue serializedValue = runtimeService.getVariableTyped(pi.getId(), RuntimeServiceDelegate.VARIABLE_NAME, false);
    String actualJsonString = serializedValue.getValueSerialized();

    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode actualJsonTree = objectMapper.readTree(actualJsonString);
    JsonNode expectedJsonTree = objectMapper.readTree(expectedJsonString);
    // JsonNode#equals makes a deep comparison
    Assert.assertEquals(expectedJsonTree, actualJsonTree);

  }
}
