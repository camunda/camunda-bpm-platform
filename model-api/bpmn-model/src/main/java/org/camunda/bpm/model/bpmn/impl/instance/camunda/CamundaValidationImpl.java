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
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaConstraint;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaValidation;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;
import org.camunda.bpm.model.xml.type.child.ChildElementCollection;
import org.camunda.bpm.model.xml.type.child.SequenceBuilder;

import java.util.Collection;

import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.CAMUNDA_ELEMENT_VALIDATION;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.CAMUNDA_NS;
import static org.camunda.bpm.model.xml.type.ModelElementTypeBuilder.ModelTypeInstanceProvider;

/**
 * The BPMN validation camunda extension element
 *
 * @author Sebastian Menski
 */
public class CamundaValidationImpl extends BpmnModelElementInstanceImpl implements CamundaValidation {

  protected static ChildElementCollection<CamundaConstraint> camundaConstraintCollection;

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(CamundaValidation.class, CAMUNDA_ELEMENT_VALIDATION)
      .namespaceUri(CAMUNDA_NS)
      .instanceProvider(new ModelTypeInstanceProvider<CamundaValidation>() {
        public CamundaValidation newInstance(ModelTypeInstanceContext instanceContext) {
          return new CamundaValidationImpl(instanceContext);
        }
      });

    SequenceBuilder sequenceBuilder = typeBuilder.sequence();

    camundaConstraintCollection = sequenceBuilder.elementCollection(CamundaConstraint.class)
      .build();

    typeBuilder.build();
  }

  public CamundaValidationImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public Collection<CamundaConstraint> getCamundaConstraints() {
    return camundaConstraintCollection.get(this);
  }
}
