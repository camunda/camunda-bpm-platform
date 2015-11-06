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
package org.camunda.bpm.engine.test.cmmn.transformer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.camunda.bpm.engine.impl.cmmn.entity.repository.CaseDefinitionEntity;
import org.camunda.bpm.engine.impl.cmmn.handler.DefaultCmmnElementHandlerRegistry;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnActivity;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnCaseDefinition;
import org.camunda.bpm.engine.impl.cmmn.transformer.CmmnTransform;
import org.camunda.bpm.engine.impl.cmmn.transformer.CmmnTransformer;
import org.camunda.bpm.engine.impl.persistence.entity.DeploymentEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ResourceEntity;
import org.camunda.bpm.model.cmmn.Cmmn;
import org.camunda.bpm.model.cmmn.CmmnModelInstance;
import org.camunda.bpm.model.cmmn.instance.Case;
import org.camunda.bpm.model.cmmn.instance.CasePlanModel;
import org.camunda.bpm.model.cmmn.instance.CmmnModelElementInstance;
import org.camunda.bpm.model.cmmn.instance.Definitions;
import org.camunda.bpm.model.cmmn.instance.HumanTask;
import org.camunda.bpm.model.cmmn.instance.PlanItem;
import org.camunda.bpm.model.cmmn.instance.Stage;
import org.camunda.bpm.model.xml.impl.util.IoUtil;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Roman Smirnov
 *
 */
public class CmmnTransformerTest {

  protected CmmnTransform transformer;
  protected CmmnModelInstance modelInstance;
  protected Definitions definitions;
  protected Case caseDefinition;
  protected CasePlanModel casePlanModel;
  protected DeploymentEntity deployment;

  @Before
  public void setup() {
    CmmnTransformer transformerWrapper = new CmmnTransformer(null, new DefaultCmmnElementHandlerRegistry(), null);
    transformer = new CmmnTransform(transformerWrapper);

    deployment = new DeploymentEntity();
    deployment.setId("aDeploymentId");

    transformer.setDeployment(deployment);

    modelInstance = Cmmn.createEmptyModel();
    definitions = modelInstance.newInstance(Definitions.class);
    definitions.setTargetNamespace("http://camunda.org/examples");
    modelInstance.setDefinitions(definitions);

    caseDefinition = createElement(definitions, "aCaseDefinition", Case.class);
    casePlanModel = createElement(caseDefinition, "aCasePlanModel", CasePlanModel.class);
  }

  protected <T extends CmmnModelElementInstance> T createElement(CmmnModelElementInstance parentElement, String id, Class<T> elementClass) {
    T element = modelInstance.newInstance(elementClass);
    element.setAttributeValue("id", id, true);
    parentElement.addChildElement(element);
    return element;
  }

  protected List<CaseDefinitionEntity> transform() {
    // convert the model to the XML string representation
    OutputStream outputStream = new ByteArrayOutputStream();
    Cmmn.writeModelToStream(outputStream, modelInstance);
    InputStream inputStream = IoUtil.convertOutputStreamToInputStream(outputStream);

    byte[] model = org.camunda.bpm.engine.impl.util.IoUtil.readInputStream(inputStream, "model");

    ResourceEntity resource = new ResourceEntity();
    resource.setBytes(model);
    resource.setName("test");

    transformer.setResource(resource);
    List<CaseDefinitionEntity> definitions = transformer.transform();

    IoUtil.closeSilently(outputStream);
    IoUtil.closeSilently(inputStream);

    return definitions;
  }

  /**
  *
  *   +-----------------+                    +-----------------+
  *   | Case1            \                   | aCaseDefinition |
  *   +-------------------+---+              +-----------------+
  *   |                       |                      |
  *   |                       |   ==>        +-----------------+
  *   |                       |              |  aCasePlanModel |
  *   |                       |              +-----------------+
  *   |                       |
  *   +-----------------------+
  *
  */
  @Test
  public void testCasePlanModel() {
    // given

    // when
    List<CaseDefinitionEntity> caseDefinitions = transform();

    // then
    assertEquals(1, caseDefinitions.size());

    CmmnCaseDefinition caseModel = caseDefinitions.get(0);

    List<CmmnActivity> activities = caseModel.getActivities();

    assertEquals(1, activities.size());

    CmmnActivity casePlanModelActivity = activities.get(0);
    assertEquals(casePlanModel.getId(), casePlanModelActivity.getId());
    assertTrue(casePlanModelActivity.getActivities().isEmpty());
  }

  /**
  *
  *   +-----------------+                    +-----------------+
  *   | Case1            \                   | aCaseDefinition |
  *   +-------------------+---+              +-----------------+
  *   |                       |                      |
  *   |     +-------+         |   ==>        +-----------------+
  *   |     |   A   |         |              |  aCasePlanModel |
  *   |     +-------+         |              +-----------------+
  *   |                       |                      |
  *   +-----------------------+              +-----------------+
  *                                          |       A         |
  *                                          +-----------------+
  *
  */
  @Test
  public void testActivityTreeWithOneHumanTask() {
    // given
    HumanTask humanTask = createElement(casePlanModel, "A", HumanTask.class);
    PlanItem planItem = createElement(casePlanModel, "PI_A", PlanItem.class);

    planItem.setDefinition(humanTask);

    // when
    List<CaseDefinitionEntity> caseDefinitions = transform();

    // then
    assertEquals(1, caseDefinitions.size());

    CaseDefinitionEntity caseDefinition = caseDefinitions.get(0);
    List<CmmnActivity> activities = caseDefinition.getActivities();

    CmmnActivity casePlanModelActivity = activities.get(0);

    List<CmmnActivity> planItemActivities = casePlanModelActivity.getActivities();
    assertEquals(1, planItemActivities.size());

    CmmnActivity child = planItemActivities.get(0);
    assertEquals(planItem.getId(), child.getId());
    assertTrue(child.getActivities().isEmpty());
  }

  /**
  *
  *   +-----------------+                                       +-----------------+
  *   | Case1            \                                      | aCaseDefinition |
  *   +-------------------+-----------------+                   +-----------------+
  *   |                                     |                            |
  *   |     +------------------------+      |                   +-----------------+
  *   |    / X                        \     |                   |  aCasePlanModel |
  *   |   +    +-------+  +-------+    +    |                   +-----------------+
  *   |   |    |   A   |  |   B   |    |    |  ==>                       |
  *   |   +    +-------+  +-------+    +    |                   +-----------------+
  *   |    \                          /     |                   |        X        |
  *   |     +------------------------+      |                   +-----------------+
  *   |                                     |                           / \
  *   +-------------------------------------+                          /   \
  *                                                 +-----------------+     +-----------------+
  *                                                 |        A        |     |        B        |
  *                                                 +-----------------+     +-----------------+
  */
  @Test
  public void testActivityTreeWithOneStageAndNestedHumanTasks() {
    // given
    Stage stage = createElement(casePlanModel, "X", Stage.class);
    HumanTask humanTaskA = createElement(casePlanModel, "A", HumanTask.class);
    HumanTask humanTaskB = createElement(casePlanModel, "B", HumanTask.class);

    PlanItem planItemX = createElement(casePlanModel, "PI_X", PlanItem.class);
    PlanItem planItemA = createElement(stage, "PI_A", PlanItem.class);
    PlanItem planItemB = createElement(stage, "PI_B", PlanItem.class);

    planItemX.setDefinition(stage);
    planItemA.setDefinition(humanTaskA);
    planItemB.setDefinition(humanTaskB);

    // when
    List<CaseDefinitionEntity> caseDefinitions = transform();

    // then
    assertEquals(1, caseDefinitions.size());

    CaseDefinitionEntity caseDefinition = caseDefinitions.get(0);
    List<CmmnActivity> activities = caseDefinition.getActivities();

    CmmnActivity casePlanModelActivity = activities.get(0);

    List<CmmnActivity> children = casePlanModelActivity.getActivities();
    assertEquals(1, children.size());

    CmmnActivity planItemStage = children.get(0);
    assertEquals(planItemX.getId(), planItemStage.getId());

    children = planItemStage.getActivities();
    assertEquals(2, children.size());

    CmmnActivity childPlanItem = children.get(0);
    assertEquals(planItemA.getId(), childPlanItem.getId());
    assertTrue(childPlanItem.getActivities().isEmpty());

    childPlanItem = children.get(1);
    assertEquals(planItemB.getId(), childPlanItem.getId());
    assertTrue(childPlanItem.getActivities().isEmpty());
  }

  /**
  *
  *   +-----------------+                                                    +-----------------+
  *   | Case1            \                                                   | aCaseDefinition |
  *   +-------------------+-------------------+                              +-----------------+
  *   |                                       |                                       |
  *   |  +-------+                            |                              +-----------------+
  *   |  |  A1   |                            |              +---------------|  aCasePlanModel |---------------+
  *   |  +-------+                            |              |               +-----------------+               |
  *   |                                       |              |                        |                        |
  *   |    +------------------------+         |      +-----------------+     +-----------------+      +-----------------+
  *   |   / X1                       \        |      |       A1        |     |        X1       |      |        Y        |-----------+
  *   |  +    +-------+  +-------+    +       |      +-----------------+     +-----------------+      +-----------------+           |
  *   |  |    |  A2   |  |   B   |    |       |                                      / \                                           / \
  *   |  +    +-------+  +-------+    +       |                                     /   \                                         /   \
  *   |   \                          /        |                    +---------------+     +---------------+     +-----------------+     +-----------------+
  *   |    +------------------------+         |                    |      A2       |     |      B        |     |        C        |     |       X2        |
  *   |                                       |                    +---------------+     +---------------+     +-----------------+     +-----------------+
  *   |    +-----------------------------+    |  ==>                                                                                          / \
  *   |   / Y                             \   |                                                                              +---------------+   +---------------+
  *   |  +    +-------+                    +  |                                                                              |      A1       |   |       B       |
  *   |  |    |   C   |                    |  |                                                                              +---------------+   +---------------+
  *   |  |    +-------+                    |  |
  *   |  |                                 |  |
  *   |  |   +------------------------+    |  |
  *   |  |  / X2                       \   |  |
  *   |  | +    +-------+  +-------+    +  |  |
  *   |  | |    |  A1   |  |   B   |    |  |  |
  *   |  | +    +-------+  +-------+    +  |  |
  *   |  |  \                          /   |  |
  *   |  +   +------------------------+    +  |
  *   |   \                               /   |
  *   |    +-----------------------------+    |
  *   |                                       |
  *   +---------------------------------------+
  *
  */
  @Test
  public void testNestedStages() {
    // given
    Stage stageX = createElement(casePlanModel, "X", Stage.class);
    Stage stageY = createElement(casePlanModel, "Y", Stage.class);
    HumanTask humanTaskA = createElement(casePlanModel, "A", HumanTask.class);
    HumanTask humanTaskB = createElement(casePlanModel, "B", HumanTask.class);
    HumanTask humanTaskC = createElement(casePlanModel, "C", HumanTask.class);

    PlanItem planItemA1 = createElement(casePlanModel, "PI_A1", PlanItem.class);
    planItemA1.setDefinition(humanTaskA);

    PlanItem planItemX1 = createElement(casePlanModel, "PI_X1", PlanItem.class);
    planItemX1.setDefinition(stageX);
    PlanItem planItemA2 = createElement(stageX, "PI_A2", PlanItem.class);
    planItemA2.setDefinition(humanTaskA);
    PlanItem planItemB = createElement(stageX, "PI_B", PlanItem.class);
    planItemB.setDefinition(humanTaskB);

    PlanItem planItemY = createElement(casePlanModel, "PI_Y", PlanItem.class);
    planItemY.setDefinition(stageY);
    PlanItem planItemC = createElement(stageY, "PI_C", PlanItem.class);
    planItemC.setDefinition(humanTaskC);
    PlanItem planItemX2 = createElement(stageY, "PI_X2", PlanItem.class);
    planItemX2.setDefinition(stageX);

    // when
    List<CaseDefinitionEntity> caseDefinitions = transform();

    // then
    assertEquals(1, caseDefinitions.size());

    CaseDefinitionEntity caseDefinition = caseDefinitions.get(0);
    List<CmmnActivity> activities = caseDefinition.getActivities();

    CmmnActivity casePlanModelActivity = activities.get(0);

    List<CmmnActivity> children = casePlanModelActivity.getActivities();
    assertEquals(3, children.size());

    CmmnActivity childPlanItem = children.get(0);
    assertEquals(planItemA1.getId(), childPlanItem.getId());
    assertTrue(childPlanItem.getActivities().isEmpty());

    childPlanItem = children.get(1);
    assertEquals(planItemX1.getId(), childPlanItem.getId());

    List<CmmnActivity> childrenOfX1 = childPlanItem.getActivities();
    assertFalse(childrenOfX1.isEmpty());
    assertEquals(2, childrenOfX1.size());

    childPlanItem = childrenOfX1.get(0);
    assertEquals(planItemA2.getId(), childPlanItem.getId());
    assertTrue(childPlanItem.getActivities().isEmpty());

    childPlanItem = childrenOfX1.get(1);
    assertEquals(planItemB.getId(), childPlanItem.getId());
    assertTrue(childPlanItem.getActivities().isEmpty());

    childPlanItem = children.get(2);
    assertEquals(planItemY.getId(), childPlanItem.getId());

    List<CmmnActivity> childrenOfY = childPlanItem.getActivities();
    assertFalse(childrenOfY.isEmpty());
    assertEquals(2, childrenOfY.size());

    childPlanItem = childrenOfY.get(0);
    assertEquals(planItemC.getId(), childPlanItem.getId());
    assertTrue(childPlanItem.getActivities().isEmpty());

    childPlanItem = childrenOfY.get(1);
    assertEquals(planItemX2.getId(), childPlanItem.getId());

    List<CmmnActivity> childrenOfX2 = childPlanItem.getActivities();
    assertFalse(childrenOfX2.isEmpty());
    assertEquals(2, childrenOfX2.size());

    childPlanItem = childrenOfX2.get(0);
    assertEquals(planItemA2.getId(), childPlanItem.getId());
    assertTrue(childPlanItem.getActivities().isEmpty());

    childPlanItem = childrenOfX2.get(1);
    assertEquals(planItemB.getId(), childPlanItem.getId());
    assertTrue(childPlanItem.getActivities().isEmpty());

  }

}
