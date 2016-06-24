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

package org.camunda.bpm.engine.impl.dmn.transformer;

import org.camunda.bpm.dmn.engine.impl.DmnDecisionRequirementDiagramImpl;
import org.camunda.bpm.dmn.engine.impl.spi.transform.DmnElementTransformContext;
import org.camunda.bpm.dmn.engine.impl.transform.DmnDecisionRequirementDiagramTransformHandler;
import org.camunda.bpm.engine.impl.dmn.entity.repository.DecisionRequirementDefinitionEntity;
import org.camunda.bpm.model.dmn.instance.Definitions;

public class DecisionRequirementDefinitionTransformHandler extends DmnDecisionRequirementDiagramTransformHandler {

  @Override
  protected DmnDecisionRequirementDiagramImpl createFromDefinitions(DmnElementTransformContext context, Definitions definitions) {
    DecisionRequirementDefinitionEntity entity = (DecisionRequirementDefinitionEntity) super.createFromDefinitions(context, definitions);

    entity.setCategory(definitions.getNamespace());

    return entity;
  }

  @Override
  protected DmnDecisionRequirementDiagramImpl createDmnElement() {
    return new DecisionRequirementDefinitionEntity();
  }

}
