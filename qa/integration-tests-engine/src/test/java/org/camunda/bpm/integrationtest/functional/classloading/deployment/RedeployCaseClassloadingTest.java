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
package org.camunda.bpm.integrationtest.functional.classloading.deployment;

import org.camunda.bpm.engine.runtime.VariableInstanceQuery;
import org.camunda.bpm.integrationtest.functional.classloading.beans.ExampleCaseExecutionListener;
import org.camunda.bpm.integrationtest.util.AbstractFoxPlatformIntegrationTest;
import org.camunda.bpm.integrationtest.util.TestContainer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Roman Smirnov
 *
 */
@RunWith(Arquillian.class)
public class RedeployCaseClassloadingTest extends AbstractFoxPlatformIntegrationTest {

  @Deployment
  public static WebArchive createProcessArchiveDeplyoment() {
    return initWebArchiveDeployment()
            .addClass(ExampleCaseExecutionListener.class)
            .addAsResource("org/camunda/bpm/integrationtest/functional/classloading/deployment/RedeployCaseClassloadingTest.testRedeployClassloading.cmmn10.xml");
  }


  @Deployment(name="clientDeployment")
  public static WebArchive clientDeployment() {
    WebArchive webArchive = ShrinkWrap.create(WebArchive.class, "client.war")
            .addClass(AbstractFoxPlatformIntegrationTest.class);

    TestContainer.addContainerSpecificResources(webArchive);

    return webArchive;
  }

  @Test
  @OperateOnDeployment("clientDeployment")
  public void testRedeployClassloading() {
    // given
    org.camunda.bpm.engine.repository.Deployment deployment = repositoryService.createDeploymentQuery().singleResult();

    org.camunda.bpm.engine.repository.Deployment deployment2 = repositoryService
      .createDeployment()
      .nameFromDeployment(deployment.getId())
      .addDeploymentResources(deployment.getId())
      .deploy();

    // when (2)
    String caseInstanceId = caseService
        .withCaseDefinitionByKey("case")
        .create()
        .getId();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    // then (1)
    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .variableName("listener")
        .caseInstanceIdIn(caseInstanceId);

    Assert.assertNotNull(query.singleResult());
    Assert.assertEquals("listener-notified", query.singleResult().getValue());

    caseService
      .withCaseExecution(caseInstanceId)
      .removeVariable("listener")
      .execute();

    Assert.assertEquals(0, query.count());

    // when (2)
    caseService
      .withCaseExecution(humanTaskId)
      .complete();

    // then (2)
    Assert.assertNotNull(query.singleResult());
    Assert.assertEquals("listener-notified", query.singleResult().getValue());

    repositoryService.deleteDeployment(deployment2.getId(), true, true);
  }

}
