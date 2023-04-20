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
package org.camunda.bpm.integrationtest.functional.cdi;

import java.util.Arrays;
import java.util.List;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.Permission;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.integrationtest.util.AbstractFoxPlatformIntegrationTest;
import org.camunda.bpm.integrationtest.util.DeploymentHelper;
import org.camunda.bpm.integrationtest.util.TestContainer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class CdiBeanResolutionTwoEnginesTest extends AbstractFoxPlatformIntegrationTest {

  @Deployment(name= "engine1", order = 1)
  public static WebArchive createDeployment() {
    final WebArchive webArchive = initWebArchiveDeployment("paEngine1.war", "org/camunda/bpm/integrationtest/paOnEngine1.xml")
      .addAsResource("org/camunda/bpm/integrationtest/functional/cdi/CdiBeanResolutionTwoEnginesTest.testResolveBean.bpmn20.xml")
      .addAsLibraries(DeploymentHelper.getEngineCdi());

    TestContainer.addContainerSpecificProcessEngineConfigurationClass(webArchive);
    return webArchive;
  }

  @Test
  @OperateOnDeployment("engine1")
  public void testResolveBean() throws Exception {
    //given
    final ProcessEngine processEngine1 = processEngineService.getProcessEngine("engine1");
    Assert.assertEquals("engine1", processEngine1.getName());
    createAuthorizations(processEngine1);

    //when we operate the process under authenticated user
    processEngine1.getIdentityService().setAuthentication("user1", Arrays.asList("group1"));

    processEngine1.getRuntimeService().startProcessInstanceByKey("testProcess");
    final List<Task> tasks = processEngine1.getTaskService().createTaskQuery().list();
    Assert.assertEquals(1, tasks.size());
    processEngine1.getTaskService().complete(tasks.get(0).getId());

    //then
    //identityService resolution respects the engine, on which the process is being executed
    final List<VariableInstance> variableInstances = processEngine1.getRuntimeService().createVariableInstanceQuery().variableName("changeInitiatorUsername")
      .list();
    Assert.assertEquals(1, variableInstances.size());
    Assert.assertEquals("user1", variableInstances.get(0).getValue());
  }

  private void createAuthorizations(ProcessEngine processEngine1) {
    Authorization newAuthorization = processEngine1.getAuthorizationService().createNewAuthorization(Authorization.AUTH_TYPE_GLOBAL);
    newAuthorization.setResource(Resources.PROCESS_INSTANCE);
    newAuthorization.setResourceId("*");
    newAuthorization.setPermissions(new Permission[] { Permissions.CREATE });
    processEngine1.getAuthorizationService().saveAuthorization(newAuthorization);

    newAuthorization = processEngine1.getAuthorizationService().createNewAuthorization(Authorization.AUTH_TYPE_GLOBAL);
    newAuthorization.setResource(Resources.PROCESS_DEFINITION);
    newAuthorization.setResourceId("*");
    newAuthorization.setPermissions(new Permission[] { Permissions.CREATE_INSTANCE });
    processEngine1.getAuthorizationService().saveAuthorization(newAuthorization);

    newAuthorization = processEngine1.getAuthorizationService().createNewAuthorization(Authorization.AUTH_TYPE_GLOBAL);
    newAuthorization.setResource(Resources.TASK);
    newAuthorization.setResourceId("*");
    newAuthorization.setPermissions(new Permission[] { Permissions.READ, Permissions.TASK_WORK });
    processEngine1.getAuthorizationService().saveAuthorization(newAuthorization);
  }

}
