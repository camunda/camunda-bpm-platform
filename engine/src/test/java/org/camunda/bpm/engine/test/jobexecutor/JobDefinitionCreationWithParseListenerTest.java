/*
 * Copyright 2016 camunda services GmbH.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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
package org.camunda.bpm.engine.test.jobexecutor;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.bpmn.parser.AbstractBpmnParseListener;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParseListener;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.util.xml.Element;
import org.camunda.bpm.engine.management.JobDefinition;
import org.camunda.bpm.engine.management.JobDefinitionQuery;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.DeploymentBuilder;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * Represents a test class, which uses parse listeners
 * to create job definitions for async activities.
 * The parse listeners are called after the bpmn xml was parsed.
 * They set the activity asyncBefore property to true. In this case
 * there should created some job declarations for the async activity.
 *
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
public class JobDefinitionCreationWithParseListenerTest {

  /**
   * The custom rule which adjust the process engine configuration.
   */
  protected ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule() {
    @Override
    public ProcessEngineConfiguration configureEngine(ProcessEngineConfigurationImpl configuration) {
      List<BpmnParseListener> listeners = new ArrayList<BpmnParseListener>();
      listeners.add(new AbstractBpmnParseListener(){

        @Override
        public void parseServiceTask(Element serviceTaskElement, ScopeImpl scope, ActivityImpl activity) {
          activity.setAsyncBefore(true);
        }
      });

      configuration.setCustomPreBPMNParseListeners(listeners);
      return configuration;
    }
  };

  /**
   * The engine rule.
   */
  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);

  /**
   * The rule chain for the defined rules.
   */
  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(bootstrapRule).around(engineRule);

  @Test
  public void testCreateJobDefinitionWithParseListener() {
    //given
    String modelFileName = "jobCreationWithinParseListener.bpmn20.xml";
    InputStream in = JobDefinitionCreationWithParseListenerTest.class.getResourceAsStream(modelFileName);
    DeploymentBuilder builder = engineRule.getRepositoryService().createDeployment().addInputStream(modelFileName, in);

    //when the asyncBefore is set in the parse listener
    Deployment deployment = builder.deploy();
    engineRule.manageDeployment(deployment);

    //then there exists a new job definition
    JobDefinitionQuery query = engineRule.getManagementService().createJobDefinitionQuery();
    JobDefinition jobDef = query.singleResult();
    assertNotNull(jobDef);
    assertEquals(jobDef.getProcessDefinitionKey(), "oneTaskProcess");
    assertEquals(jobDef.getActivityId(), "servicetask1");
  }


  @Test
  public void testCreateJobDefinitionWithParseListenerAndAsyncInXml() {
    //given the asyncBefore is set in the xml
    String modelFileName = "jobAsyncBeforeCreationWithinParseListener.bpmn20.xml";
    InputStream in = JobDefinitionCreationWithParseListenerTest.class.getResourceAsStream(modelFileName);
    DeploymentBuilder builder = engineRule.getRepositoryService().createDeployment().addInputStream(modelFileName, in);

    //when the asyncBefore is set in the parse listener
    Deployment deployment = builder.deploy();
    engineRule.manageDeployment(deployment);

    //then there exists only one job definition
    JobDefinitionQuery query = engineRule.getManagementService().createJobDefinitionQuery();
    JobDefinition jobDef = query.singleResult();
    assertNotNull(jobDef);
    assertEquals(jobDef.getProcessDefinitionKey(), "oneTaskProcess");
    assertEquals(jobDef.getActivityId(), "servicetask1");
  }
}
