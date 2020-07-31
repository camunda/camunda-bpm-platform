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
package org.camunda.bpm.engine.test.jobexecutor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.impl.bpmn.parser.AbstractBpmnParseListener;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParseListener;
import org.camunda.bpm.engine.impl.jobexecutor.MessageJobDeclaration;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.util.xml.Element;
import org.camunda.bpm.engine.management.JobDefinition;
import org.camunda.bpm.engine.management.JobDefinitionQuery;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.DeploymentBuilder;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * Represents a test class, which uses parse listeners
 * to delete job definitions for asyncBefore activities and create job
 * definitions for asyncAfter.
 * The parse listeners are called after the bpmn xml was parsed.
 * They set the activity asyncBefore property to false and the asyncAfter to true.
 * In this case there should delete and create some job declarations for the activity which
 * was asyncBefore before and are now asyncAfter.
 *
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
public class JobDefinitionCreationAndDeletionWithParseListenerTest {

  @ClassRule
  public static ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule(configuration -> {
    List<BpmnParseListener> listeners = new ArrayList<>();
    listeners.add(new AbstractBpmnParseListener(){

      @Override
      public void parseServiceTask(Element serviceTaskElement, ScopeImpl scope, ActivityImpl activity) {
        activity.setAsyncBefore(false);
        activity.setAsyncAfter(true);
      }
    });

    configuration.setCustomPreBPMNParseListeners(listeners);
  });

  @Rule
  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);

  @Test
  public void testDeleteNonExistingAndCreateNewJobDefinitionWithParseListener() {
    //given
    String modelFileName = "jobCreationWithinParseListener.bpmn20.xml";
    InputStream in = JobDefinitionCreationWithParseListenerTest.class.getResourceAsStream(modelFileName);
    DeploymentBuilder builder = engineRule.getRepositoryService().createDeployment().addInputStream(modelFileName, in);

    //when the asyncBefore is set to false and the asyncAfter to true in the parse listener
    Deployment deployment = builder.deploy();
    engineRule.manageDeployment(deployment);

    //then there exists one job definition
    JobDefinitionQuery query = engineRule.getManagementService().createJobDefinitionQuery();
    JobDefinition jobDef = query.singleResult();
    assertNotNull(jobDef);
    assertEquals(jobDef.getProcessDefinitionKey(), "oneTaskProcess");
    assertEquals(jobDef.getActivityId(), "servicetask1");
    assertEquals(jobDef.getJobConfiguration(), MessageJobDeclaration.ASYNC_AFTER);
  }

  @Test
  public void testDeleteJobDefinitionWithParseListenerAndAsyncInXml() {
    //given the asyncBefore is set in the xml
    String modelFileName = "jobAsyncBeforeCreationWithinParseListener.bpmn20.xml";
    InputStream in = JobDefinitionCreationWithParseListenerTest.class.getResourceAsStream(modelFileName);
    DeploymentBuilder builder = engineRule.getRepositoryService().createDeployment().addInputStream(modelFileName, in);

    //when the asyncBefore is set to false and the asyncAfter to true in the parse listener
    Deployment deployment = builder.deploy();
    engineRule.manageDeployment(deployment);

    //then there exists one job definition
    JobDefinitionQuery query = engineRule.getManagementService().createJobDefinitionQuery();
    JobDefinition jobDef = query.singleResult();
    assertNotNull(jobDef);
    assertEquals(jobDef.getProcessDefinitionKey(), "oneTaskProcess");
    assertEquals(jobDef.getActivityId(), "servicetask1");
    assertEquals(jobDef.getJobConfiguration(), MessageJobDeclaration.ASYNC_AFTER);
  }

  @Test
  public void testDeleteJobDefinitionWithParseListenerAndAsyncBothInXml() {
    //given the asyncBefore AND asyncAfter is set in the xml
    String modelFileName = "jobAsyncBothCreationWithinParseListener.bpmn20.xml";
    InputStream in = JobDefinitionCreationWithParseListenerTest.class.getResourceAsStream(modelFileName);
    DeploymentBuilder builder = engineRule.getRepositoryService().createDeployment().addInputStream(modelFileName, in);

    //when the asyncBefore is set to false and the asyncAfter to true in the parse listener
    Deployment deployment = builder.deploy();
    engineRule.manageDeployment(deployment);

    //then there exists one job definition
    JobDefinitionQuery query = engineRule.getManagementService().createJobDefinitionQuery();
    JobDefinition jobDef = query.singleResult();
    assertNotNull(jobDef);
    assertEquals(jobDef.getProcessDefinitionKey(), "oneTaskProcess");
    assertEquals(jobDef.getActivityId(), "servicetask1");
    assertEquals(jobDef.getJobConfiguration(), MessageJobDeclaration.ASYNC_AFTER);
  }
}
