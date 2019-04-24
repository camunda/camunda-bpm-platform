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
package org.camunda.bpm.integrationtest.jobexecutor;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;
import org.camunda.bpm.engine.impl.jobexecutor.DefaultJobPriorityProvider;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.Variables.SerializationDataFormats;
import org.camunda.bpm.integrationtest.jobexecutor.beans.PriorityBean;
import org.camunda.bpm.integrationtest.util.AbstractFoxPlatformIntegrationTest;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Thorben Lindhauer
 *
 */
@RunWith(Arquillian.class)
public class JobPrioritizationFailureTest extends AbstractFoxPlatformIntegrationTest {

  protected ProcessInstance processInstance;

  public static final String VARIABLE_CLASS_NAME = "org.camunda.bpm.integrationtest.jobexecutor.beans.PriorityBean";
  public static final String PRIORITY_BEAN_INSTANCE_FILE = "priorityBean.instance";

  @Before
  public void setEngines() {

    // unregister process application so that context switch cannot be performed
    unregisterProcessApplication();
  }

  protected void unregisterProcessApplication() {
    org.camunda.bpm.engine.repository.Deployment deployment =
        processEngine.getRepositoryService().createDeploymentQuery().singleResult();

    managementService.unregisterProcessApplication(deployment.getId(), false);
  }

  @Deployment(order = 1)
  public static WebArchive createDeployment() {
    return initWebArchiveDeployment()
      .addClass(PriorityBean.class)
      .addAsResource("org/camunda/bpm/integrationtest/jobexecutor/JobPrioritizationTest.priorityProcess.bpmn20.xml");
  }

  @Deployment(name = "dummy-client", order = 2)
  public static WebArchive createDummyClientDeployment() {
    return initWebArchiveDeployment("pa2.war")
       .addAsResource(new ByteArrayAsset(serializeJavaObjectValue(new PriorityBean())), PRIORITY_BEAN_INSTANCE_FILE);
  }

  @After
  public void tearDown() {
    if (processInstance != null) {
      runtimeService.deleteProcessInstance(processInstance.getId(), "");
    }
  }


  @Test
  @OperateOnDeployment("dummy-client")
  public void testGracefulDegradationOnMissingBean() {
    // when
    processInstance = runtimeService.startProcessInstanceByKey("priorityProcess");

    // then the job was created successfully and has the default priority on bean evaluation failure
    Job job = managementService.createJobQuery().singleResult();
    Assert.assertEquals(DefaultJobPriorityProvider.DEFAULT_PRIORITY_ON_RESOLUTION_FAILURE, job.getPriority());
  }

  @Test
  @OperateOnDeployment("dummy-client")
  public void testGracefulDegradationOnMissingClassSpinJson() {
    // given
    Map<String, Object> variables = Variables.createVariables().putValue(
        "priorityBean",
        Variables.serializedObjectValue("{}")
          .serializationDataFormat(SerializationDataFormats.JSON)
          .objectTypeName(VARIABLE_CLASS_NAME)
          .create());

    // when
    processInstance = runtimeService.startProcessInstanceByKey("priorityProcess", variables);

    // then the job was created successfully and has the default priority although
    // the bean could not be resolved due to a missing class
    Job job = managementService.createJobQuery().singleResult();
    Assert.assertEquals(DefaultJobPriorityProvider.DEFAULT_PRIORITY_ON_RESOLUTION_FAILURE, job.getPriority());
  }

  @Test
  @OperateOnDeployment("dummy-client")
  public void testGracefulDegradationOnMissingClassSpinXml() {
    // given
    Map<String, Object> variables = Variables.createVariables().putValue(
        "priorityBean",
        Variables.serializedObjectValue("<?xml version=\"1.0\" encoding=\"utf-8\"?><prioritybean></prioritybean>")
          .serializationDataFormat(SerializationDataFormats.XML)
          .objectTypeName(VARIABLE_CLASS_NAME)
          .create());

    // when
    processInstance = runtimeService.startProcessInstanceByKey("priorityProcess", variables);

    // then the job was created successfully and has the default priority although
    // the bean could not be resolved due to a missing class
    Job job = managementService.createJobQuery().singleResult();
    Assert.assertEquals(DefaultJobPriorityProvider.DEFAULT_PRIORITY_ON_RESOLUTION_FAILURE, job.getPriority());
  }

  protected static byte[] serializeJavaObjectValue(Serializable object) {

    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      new ObjectOutputStream(baos).writeObject(object);
      return baos.toByteArray();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}
