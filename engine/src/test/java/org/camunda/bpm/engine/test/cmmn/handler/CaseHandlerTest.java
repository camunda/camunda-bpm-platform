/* Licensed under the Apache License, Version 2.0 (the "License");
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
package org.camunda.bpm.engine.test.cmmn.handler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.camunda.bpm.engine.impl.cmmn.behavior.CmmnActivityBehavior;
import org.camunda.bpm.engine.impl.cmmn.entity.repository.CaseDefinitionEntity;
import org.camunda.bpm.engine.impl.cmmn.handler.CaseHandler;
import org.camunda.bpm.engine.impl.cmmn.handler.CmmnHandlerContext;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnActivity;
import org.camunda.bpm.engine.impl.persistence.entity.DeploymentEntity;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Roman Smirnov
 *
 */
public class CaseHandlerTest extends CmmnElementHandlerTest {

  protected CaseHandler handler = new CaseHandler();
  protected CmmnHandlerContext context;

  @Before
  public void setUp() {
    context = new CmmnHandlerContext();

    DeploymentEntity deployment = new DeploymentEntity();
    deployment.setId("aDeploymentId");

    context.setDeployment(deployment);
    context.setModel(modelInstance);
  }

  @Test
  public void testCaseActivityName() {
    // given:
    // the case has a name "A Case"
    String name = "A Case";
    caseDefinition.setName(name);

    // when
    CmmnActivity activity = handler.handleElement(caseDefinition, context);

    // then
    assertEquals(name, activity.getName());
  }

  @Test
  public void testActivityBehavior() {
    // given: a case

    // when
    CmmnActivity activity = handler.handleElement(caseDefinition, context);

    // then
    CmmnActivityBehavior behavior = activity.getActivityBehavior();
    assertNull(behavior);
  }

  @Test
  public void testCaseHasNoParent() {
    // given: a caseDefinition

    // when
    CmmnActivity activity = handler.handleElement(caseDefinition, context);

    // then
    assertNull(activity.getParent());
  }

  @Test
  public void testCaseDefinitionKey() {
    // given: a caseDefinition

    // when
    CaseDefinitionEntity activity = (CaseDefinitionEntity) handler.handleElement(caseDefinition, context);

    // then
    assertEquals(caseDefinition.getId(), activity.getKey());
  }

  @Test
  public void testDeploymentId() {
    // given: a caseDefinition

    // when
    CaseDefinitionEntity activity = (CaseDefinitionEntity) handler.handleElement(caseDefinition, context);

    // then
    String deploymentId = context.getDeployment().getId();
    assertEquals(deploymentId, activity.getDeploymentId());
  }

}
