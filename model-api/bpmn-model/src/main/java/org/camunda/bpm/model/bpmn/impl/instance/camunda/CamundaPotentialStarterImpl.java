/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package org.camunda.bpm.model.bpmn.impl.instance.camunda;

import org.camunda.bpm.model.bpmn.impl.instance.BpmnModelElementInstanceImpl;
import org.camunda.bpm.model.bpmn.instance.ResourceAssignmentExpression;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaPotentialStarter;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;
import org.camunda.bpm.model.xml.type.child.ChildElement;
import org.camunda.bpm.model.xml.type.child.SequenceBuilder;

import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.CAMUNDA_ELEMENT_POTENTIAL_STARTER;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.CAMUNDA_NS;
import static org.camunda.bpm.model.xml.type.ModelElementTypeBuilder.ModelTypeInstanceProvider;

/**
 * The BPMN potentialStarter camunda extension
 *
 * @author Sebastian Menski
 */
public class CamundaPotentialStarterImpl extends BpmnModelElementInstanceImpl implements CamundaPotentialStarter {

  protected static ChildElement<ResourceAssignmentExpression> resourceAssignmentExpressionChild;

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(CamundaPotentialStarter.class, CAMUNDA_ELEMENT_POTENTIAL_STARTER)
      .namespaceUri(CAMUNDA_NS)
      .instanceProvider(new ModelTypeInstanceProvider<CamundaPotentialStarter>() {
        public CamundaPotentialStarter newInstance(ModelTypeInstanceContext instanceContext) {
          return new CamundaPotentialStarterImpl(instanceContext);
        }
      });

    SequenceBuilder sequenceBuilder = typeBuilder.sequence();

    resourceAssignmentExpressionChild = sequenceBuilder.element(ResourceAssignmentExpression.class)
      .build();

    typeBuilder.build();
  }

  public CamundaPotentialStarterImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public ResourceAssignmentExpression getResourceAssignmentExpression() {
    return resourceAssignmentExpressionChild.getChild(this);
  }

  public void setResourceAssignmentExpression(ResourceAssignmentExpression resourceAssignmentExpression) {
    resourceAssignmentExpressionChild.setChild(this, resourceAssignmentExpression);
  }
}
