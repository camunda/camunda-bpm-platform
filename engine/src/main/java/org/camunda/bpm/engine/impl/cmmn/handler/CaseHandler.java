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
package org.camunda.bpm.engine.impl.cmmn.handler;

import java.util.HashMap;

import org.camunda.bpm.engine.impl.cmmn.entity.repository.CaseDefinitionEntity;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnActivity;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnCaseDefinition;
import org.camunda.bpm.engine.impl.task.TaskDefinition;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.model.cmmn.CmmnModelInstance;
import org.camunda.bpm.model.cmmn.instance.Case;
import org.camunda.bpm.model.cmmn.instance.CmmnElement;
import org.camunda.bpm.model.cmmn.instance.Definitions;

/**
 * @author Roman Smirnov
 *
 */
public class CaseHandler extends CmmnElementHandler<Case, CmmnCaseDefinition> {

  public CmmnCaseDefinition handleElement(Case element, CmmnHandlerContext context) {
    CaseDefinitionEntity definition = createActivity(element, context);

    initializeActivity(element, definition, context);

    return definition;
  }

  protected void initializeActivity(Case element, CmmnActivity activity, CmmnHandlerContext context) {
    CaseDefinitionEntity definition = (CaseDefinitionEntity) activity;

    Deployment deployment = context.getDeployment();

    definition.setKey(element.getId());
    definition.setName(element.getName());
    definition.setDeploymentId(deployment.getId());
    definition.setTaskDefinitions(new HashMap<String, TaskDefinition>());

    CmmnModelInstance model = context.getModel();

    Definitions definitions = model.getDefinitions();
    String category = definitions.getTargetNamespace();
    definition.setCategory(category);
  }

  protected CaseDefinitionEntity createActivity(CmmnElement element, CmmnHandlerContext context) {
    CaseDefinitionEntity definition = new CaseDefinitionEntity();

    definition.setCmmnElement(element);

    return definition;
  }

}
