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
package org.camunda.bpm.integrationtest.functional.el;

import static org.junit.Assert.assertNotNull;

import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.integrationtest.functional.el.beans.GreeterBean;
import org.camunda.bpm.integrationtest.util.AbstractFoxPlatformIntegrationTest;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Daniel Meyer
 *
 */
@RunWith(Arquillian.class)
public class ResolveBeanFromDmnTest extends AbstractFoxPlatformIntegrationTest {

  @Deployment
  public static WebArchive processArchive() {
    return initWebArchiveDeployment()
      .addClass(GreeterBean.class)
      .addAsResource("org/camunda/bpm/integrationtest/functional/el/BusinessRuleProcess.bpmn20.xml")
      .addAsResource("org/camunda/bpm/integrationtest/functional/el/BeanResolvingDecision.dmn11.xml");
  }

  @Test
  public void testNullElResolverIsIgnored() {
    runtimeService.startProcessInstanceByKey("testProcess");

    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);

    VariableInstance decisionResult = runtimeService.createVariableInstanceQuery().variableName("result").singleResult();
    assertNotNull("The variable 'result' should exist", decisionResult);
    assertNotNull("The value of the variable 'result' should not be null", decisionResult.getValue());
  }

}
