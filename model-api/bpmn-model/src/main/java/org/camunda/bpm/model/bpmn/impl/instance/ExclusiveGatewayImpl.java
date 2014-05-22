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
import org.camunda.bpm.model.bpmn.builder.ExclusiveGatewayBuilder;
import org.camunda.bpm.model.bpmn.instance.ExclusiveGateway;
import org.camunda.bpm.model.bpmn.instance.Gateway;
import org.camunda.bpm.model.bpmn.instance.SequenceFlow;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;
import org.camunda.bpm.model.xml.type.reference.AttributeReference;

import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.*;
import static org.camunda.bpm.model.xml.type.ModelElementTypeBuilder.ModelTypeInstanceProvider;

/**
 * The BPMN exclusiveGateway element
 *
 * @author Sebastian Menski
 */
public class ExclusiveGatewayImpl extends GatewayImpl implements ExclusiveGateway {

  protected static AttributeReference<SequenceFlow> defaultAttribute;

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(ExclusiveGateway.class, BPMN_ELEMENT_EXCLUSIVE_GATEWAY)
      .namespaceUri(BPMN20_NS)
      .extendsType(Gateway.class)
      .instanceProvider(new ModelTypeInstanceProvider<ExclusiveGateway>() {
        public ExclusiveGateway newInstance(ModelTypeInstanceContext instanceContext) {
          return new ExclusiveGatewayImpl(instanceContext);
        }
      });

    defaultAttribute = typeBuilder.stringAttribute(BPMN_ATTRIBUTE_DEFAULT)
      .idAttributeReference(SequenceFlow.class)
      .build();

    typeBuilder.build();
  }

  public ExclusiveGatewayImpl(ModelTypeInstanceContext context) {
    super(context);
  }

  @Override
  public ExclusiveGatewayBuilder builder() {
    return new ExclusiveGatewayBuilder((BpmnModelInstance) modelInstance, this);
  }

  public SequenceFlow getDefault() {
    return defaultAttribute.getReferenceTargetElement(this);
  }

  public void setDefault(SequenceFlow defaultFlow) {
    defaultAttribute.setReferenceTargetElement(this, defaultFlow);
  }
}
