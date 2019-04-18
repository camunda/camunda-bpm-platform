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
package org.camunda.bpm.integrationtest.functional.classloading.variables;
import org.camunda.bpm.integrationtest.functional.classloading.variables.beans.GetVariableDelegate;
import org.camunda.bpm.integrationtest.functional.classloading.variables.beans.SerializableVariable;
import org.camunda.bpm.integrationtest.functional.classloading.variables.beans.SetVariableDelegate;
import org.camunda.bpm.integrationtest.util.AbstractFoxPlatformIntegrationTest;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 * <p>Ensures that serializable process variables can be used in
 * combination with the shared process engine</p>
 *
 * @author Daniel Meyer
 */
@RunWith(Arquillian.class)
public class SerializableVariableTest extends AbstractFoxPlatformIntegrationTest {

  @Deployment
  public static WebArchive createProcessArchiveDeplyoment() {
    return initWebArchiveDeployment()
      .addClass(GetVariableDelegate.class)
      .addClass(SetVariableDelegate.class)
      .addClass(SerializableVariable.class)
      .addAsResource("org/camunda/bpm/integrationtest/functional/classloading/SerializableVariableTest.testResolveVariable.bpmn20.xml");
  }

  @Test
  public void testResolveClass() {

    String pid = runtimeService.startProcessInstanceByKey("testResolveVariable").getId();

    waitForJobExecutorToProcessAllJobs();

    Assert.assertNull(runtimeService.createProcessInstanceQuery().processInstanceId(pid).singleResult());

  }

}
