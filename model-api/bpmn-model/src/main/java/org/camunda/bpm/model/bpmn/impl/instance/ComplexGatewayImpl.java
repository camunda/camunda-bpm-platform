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

package org.camunda.bpm.model.bpmn.impl.instance;

import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.builder.ComplexGatewayBuilder;
import org.camunda.bpm.model.bpmn.instance.ActivationCondition;
import org.camunda.bpm.model.bpmn.instance.ComplexGateway;
import org.camunda.bpm.model.bpmn.instance.Gateway;
import org.camunda.bpm.model.bpmn.instance.SequenceFlow;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;
import org.camunda.bpm.model.xml.type.child.ChildElement;
import org.camunda.bpm.model.xml.type.child.SequenceBuilder;
import org.camunda.bpm.model.xml.type.reference.AttributeReference;

import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.BPMN20_NS;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.BPMN_ATTRIBUTE_DEFAULT;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.BPMN_ELEMENT_COMPLEX_GATEWAY;
import static org.camunda.bpm.model.xml.type.ModelElementTypeBuilder.ModelTypeInstanceProvider;

/**
 * The BPMN complexGateway element
 *
 * @author Sebastian Menski
 */
public class ComplexGatewayImpl extends GatewayImpl implements ComplexGateway {

  protected static AttributeReference<SequenceFlow> defaultAttribute;
  protected static ChildElement<ActivationCondition> activationConditionChild;

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(ComplexGateway.class, BPMN_ELEMENT_COMPLEX_GATEWAY)
      .namespaceUri(BPMN20_NS)
      .extendsType(Gateway.class)
      .instanceProvider(new ModelTypeInstanceProvider<ComplexGateway>() {
        public ComplexGateway newInstance(ModelTypeInstanceContext instanceContext) {
          return new ComplexGatewayImpl(instanceContext);
        }
      });

    defaultAttribute = typeBuilder.stringAttribute(BPMN_ATTRIBUTE_DEFAULT)
      .idAttributeReference(SequenceFlow.class)
      .build();

    SequenceBuilder sequenceBuilder = typeBuilder.sequence();

    activationConditionChild = sequenceBuilder.element(ActivationCondition.class)
      .build();

    typeBuilder.build();
  }

  public ComplexGatewayImpl(ModelTypeInstanceContext context) {
    super(context);
  }

  @Override
  public ComplexGatewayBuilder builder() {
    return new ComplexGatewayBuilder((BpmnModelInstance) modelInstance, this);
  }

  public SequenceFlow getDefault() {
    return defaultAttribute.getReferenceTargetElement(this);
  }

  public void setDefault(SequenceFlow defaultFlow) {
    defaultAttribute.setReferenceTargetElement(this, defaultFlow);
  }

  public ActivationCondition getActivationCondition() {
    return activationConditionChild.getChild(this);
  }

  public void setActivationCondition(ActivationCondition activationCondition) {
    activationConditionChild.setChild(this, activationCondition);
  }

}
