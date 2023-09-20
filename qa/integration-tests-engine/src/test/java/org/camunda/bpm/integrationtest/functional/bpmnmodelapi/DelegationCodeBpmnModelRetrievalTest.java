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
package org.camunda.bpm.integrationtest.functional.bpmnmodelapi;

import org.camunda.bpm.engine.cdi.impl.util.ProgrammaticBeanLookup;
import org.camunda.bpm.integrationtest.functional.bpmnmodelapi.beans.BpmnElementRetrievalDelegate;
import org.camunda.bpm.integrationtest.util.AbstractFoxPlatformIntegrationTest;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Daniel Meyer
 *
 */
@RunWith(Arquillian.class)
public class DelegationCodeBpmnModelRetrievalTest extends AbstractFoxPlatformIntegrationTest {

  private static final String TEST_PROCESS = "testProcess";

  @Deployment
  public static WebArchive createProcessApplication() {
    BpmnModelInstance process = Bpmn.createExecutableProcess(TEST_PROCESS)
        .camundaHistoryTimeToLive(180)
        .startEvent()
        .serviceTask()
          .camundaDelegateExpression("${bpmnElementRetrievalDelegate}")
      .done();

    return initWebArchiveDeployment()
        .addClass(BpmnElementRetrievalDelegate.class)
        .addAsResource(new StringAsset(Bpmn.convertToString(process)), "testProcess.bpmn20.xml");
  }

  @Test
  public void shouldReturnBpmnModelInstance() {

    runtimeService.startProcessInstanceByKey(TEST_PROCESS);

    BpmnElementRetrievalDelegate delegate = ProgrammaticBeanLookup.lookup(BpmnElementRetrievalDelegate.class);

    Assert.assertNotNull(delegate.getBpmnModelElementInstance());
    Assert.assertNotNull(delegate.getBpmnModelInstance());
    Assert.assertEquals(TEST_PROCESS, delegate.getBpmnModelInstance().getDefinitions().getRootElements().iterator().next().getId());
  }
}
