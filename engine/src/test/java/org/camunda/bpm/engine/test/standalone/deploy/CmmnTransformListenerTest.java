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
package org.camunda.bpm.engine.test.standalone.deploy;

import static org.camunda.bpm.engine.test.standalone.deploy.TestCmmnTransformListener.numberOfRegistered;
import static org.junit.Assert.assertEquals;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.cmmn.instance.Case;
import org.camunda.bpm.model.cmmn.instance.CasePlanModel;
import org.camunda.bpm.model.cmmn.instance.CaseTask;
import org.camunda.bpm.model.cmmn.instance.DecisionTask;
import org.camunda.bpm.model.cmmn.instance.Definitions;
import org.camunda.bpm.model.cmmn.instance.EventListener;
import org.camunda.bpm.model.cmmn.instance.HumanTask;
import org.camunda.bpm.model.cmmn.instance.Milestone;
import org.camunda.bpm.model.cmmn.instance.ProcessTask;
import org.camunda.bpm.model.cmmn.instance.Sentry;
import org.camunda.bpm.model.cmmn.instance.Stage;
import org.camunda.bpm.model.cmmn.instance.Task;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Sebastian Menski
 */
public class CmmnTransformListenerTest {

  @ClassRule
  public static ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule(
      "org/camunda/bpm/engine/test/standalone/deploy/cmmn.transform.listener.camunda.cfg.xml");
  @Rule
  public ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);

  protected RepositoryService repositoryService;

  @Before
  public void setUp() {
    repositoryService = engineRule.getRepositoryService();
  }

  @After
  public void tearDown() {
    TestCmmnTransformListener.reset();
  }

  @Deployment
  @Test
  public void testListenerInvocation() {
    // Check if case definition has different key
    assertEquals(0, repositoryService.createCaseDefinitionQuery().caseDefinitionKey("testCase").count());
    assertEquals(0, repositoryService.createCaseDefinitionQuery().caseDefinitionKey("testCase-modified").count());
    assertEquals(1, repositoryService.createCaseDefinitionQuery().caseDefinitionKey("testCase-modified-modified").count());

    assertEquals(1, numberOfRegistered(Definitions.class));
    assertEquals(1, numberOfRegistered(Case.class));
    assertEquals(1, numberOfRegistered(CasePlanModel.class));
    assertEquals(3, numberOfRegistered(HumanTask.class));
    assertEquals(1, numberOfRegistered(ProcessTask.class));
    assertEquals(1, numberOfRegistered(CaseTask.class));
    assertEquals(1, numberOfRegistered(DecisionTask.class));
    // 3x HumanTask, 1x ProcessTask, 1x CaseTask, 1x DecisionTask, 1x Task
    assertEquals(7, numberOfRegistered(Task.class));
    // 1x CasePlanModel, 1x Stage
    assertEquals(2, numberOfRegistered(Stage.class));
    assertEquals(1, numberOfRegistered(Milestone.class));
    // Note: EventListener is currently not supported!
    assertEquals(0, numberOfRegistered(EventListener.class));
    assertEquals(3, numberOfRegistered(Sentry.class));

    assertEquals(11, TestCmmnTransformListener.cmmnActivities.size());
    assertEquals(24, TestCmmnTransformListener.modelElementInstances.size());
    assertEquals(3, TestCmmnTransformListener.sentryDeclarations.size());
  }

}
