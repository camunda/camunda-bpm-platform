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

import java.util.concurrent.Callable;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.integrationtest.functional.spin.dataformat.CustomDataFormatConfigurator;
import org.camunda.bpm.integrationtest.functional.spin.dataformat.XmlSerializableJsonDeserializer;
import org.camunda.bpm.integrationtest.functional.spin.dataformat.XmlSerializableJsonSerializer;
import org.camunda.bpm.integrationtest.util.AbstractFoxPlatformIntegrationTest;
import org.camunda.bpm.integrationtest.util.DeploymentHelper;
import org.camunda.bpm.integrationtest.util.TestContainer;
import org.camunda.spin.DataFormats;
import org.camunda.spin.spi.DataFormatConfigurator;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.camunda.bpm.application.ProcessApplicationContext.withProcessApplicationContext;
import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class PaContextSwitchCustomSerializerTest extends AbstractFoxPlatformIntegrationTest {

  @Deployment(name = "pa3")
  public static WebArchive createDeployment1() {
    WebArchive webArchive = ShrinkWrap.create(WebArchive.class, "pa3.war")
        .addAsResource("META-INF/processes.xml")
        .addClass(AbstractFoxPlatformIntegrationTest.class)
        .addClass(ProcessApplication3.class)
        .addClass(XmlSerializable.class)
        .addClass(XmlSerializableJsonDeserializer.class)
        .addClass(XmlSerializableJsonSerializer.class)
        .addAsResource("org/camunda/bpm/integrationtest/functional/spin/paContextSwitchCustomSerializer.bpmn20.xml")
        .addClass(CustomDataFormatConfigurator.class)
        .addAsServiceProvider(DataFormatConfigurator.class, CustomDataFormatConfigurator.class);

    TestContainer.addSpinJacksonJsonDataFormat(webArchive);

    return webArchive;
  }

  @Deployment(name = "pa4")
  public static WebArchive createDeployment2() {
    WebArchive webArchive = ShrinkWrap.create(WebArchive.class, "pa4.war")
        .addAsWebInfResource("org/camunda/bpm/integrationtest/beans.xml", "beans.xml")
        .addAsLibraries(DeploymentHelper.getEngineCdi())
        .addAsResource("META-INF/processes.xml")
        .addClass(AbstractFoxPlatformIntegrationTest.class)
        .addClass(ProcessApplication4.class);

    return webArchive;
  }

  /**
   * Tests following scenario:
   * 1. Process application 1 declares custom de-/serializer for object variable. Process is started with object variable within process application 1.
   * 2. Process is modified within process application 2, so that variable deserialization is required -> correct deserializer is used.
   */
  @Test
  @OperateOnDeployment("pa3")
  public void test() throws Exception {

    final ProcessInstance processInstance = withProcessApplicationContext(new Callable<ProcessInstance>() {

      @Override
      public ProcessInstance call() throws Exception {
        final XmlSerializable var = new XmlSerializable();
        var.setProperty("jonny");
        return runtimeService.startProcessInstanceByKey("processWithTimer", Variables.createVariables()
          .putValueTyped("testObject", Variables.objectValue(var).serializationDataFormat(DataFormats.JSON_DATAFORMAT_NAME).create()));
      }

    }, "pa3");

    withProcessApplicationContext(new Callable<Void>() {

      @Override
      public Void call() throws Exception {
        runtimeService.createProcessInstanceModification(processInstance.getProcessInstanceId()).startTransition("flow2")
          .execute();
        return null;
      }

    }, "pa4");

    assertEquals(1, historyService.createHistoricActivityInstanceQuery().activityId("exclusiveGateway").finished().count());

  }

  public String getInstanceIdForActivity(ActivityInstance activityInstance, String activityId) {
    ActivityInstance instance = getChildInstanceForActivity(activityInstance, activityId);
    if (instance != null) {
      return instance.getId();
    }
    return null;
  }

  public ActivityInstance getChildInstanceForActivity(ActivityInstance activityInstance, String activityId) {
    if (activityId.equals(activityInstance.getActivityId())) {
      return activityInstance;
    }

    for (ActivityInstance childInstance : activityInstance.getChildActivityInstances()) {
      ActivityInstance instance = getChildInstanceForActivity(childInstance, activityId);
      if (instance != null) {
        return instance;
      }
    }

    return null;
  }

}
