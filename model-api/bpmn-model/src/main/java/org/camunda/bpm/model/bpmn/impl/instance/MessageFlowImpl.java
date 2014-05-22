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

import org.camunda.bpm.model.bpmn.instance.BaseElement;
import org.camunda.bpm.model.bpmn.instance.InteractionNode;
import org.camunda.bpm.model.bpmn.instance.Message;
import org.camunda.bpm.model.bpmn.instance.MessageFlow;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnEdge;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;
import org.camunda.bpm.model.xml.type.attribute.Attribute;
import org.camunda.bpm.model.xml.type.reference.AttributeReference;

import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.*;
import static org.camunda.bpm.model.xml.type.ModelElementTypeBuilder.ModelTypeInstanceProvider;

/**
 * The BPMN messageFlow element
 *
 * @author Sebastian Menski
 */
public class MessageFlowImpl extends BaseElementImpl implements MessageFlow {

  protected static Attribute<String> nameAttribute;
  protected static AttributeReference<InteractionNode> sourceRefAttribute;
  protected static AttributeReference<InteractionNode> targetRefAttribute;
  protected static AttributeReference<Message> messageRefAttribute;

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(MessageFlow.class, BPMN_ELEMENT_MESSAGE_FLOW)
      .namespaceUri(BPMN20_NS)
      .extendsType(BaseElement.class)
      .instanceProvider(new ModelTypeInstanceProvider<MessageFlow>() {
        public MessageFlow newInstance(ModelTypeInstanceContext instanceContext) {
          return new MessageFlowImpl(instanceContext);
        }
      });

    nameAttribute = typeBuilder.stringAttribute(BPMN_ATTRIBUTE_NAME)
      .build();

    sourceRefAttribute = typeBuilder.stringAttribute(BPMN_ATTRIBUTE_SOURCE_REF)
      .required()
      .qNameAttributeReference(InteractionNode.class)
      .build();

    targetRefAttribute = typeBuilder.stringAttribute(BPMN_ATTRIBUTE_TARGET_REF)
      .required()
      .qNameAttributeReference(InteractionNode.class)
      .build();

    messageRefAttribute = typeBuilder.stringAttribute(BPMN_ATTRIBUTE_MESSAGE_REF)
      .qNameAttributeReference(Message.class)
      .build();

    typeBuilder.build();
  }

  public MessageFlowImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public String getName() {
    return nameAttribute.getValue(this);
  }

  public void setName(String name) {
    nameAttribute.setValue(this, name);
  }

  public InteractionNode getSource() {
    return sourceRefAttribute.getReferenceTargetElement(this);
  }

  public void setSource(InteractionNode source) {
    sourceRefAttribute.setReferenceTargetElement(this, source);
  }

  public InteractionNode getTarget() {
    return targetRefAttribute.getReferenceTargetElement(this);
  }

  public void setTarget(InteractionNode target) {
    targetRefAttribute.setReferenceTargetElement(this, target);
  }

  public Message getMessage() {
    return messageRefAttribute.getReferenceTargetElement(this);
  }

  public void setMessage(Message message) {
    messageRefAttribute.setReferenceTargetElement(this, message);
  }

  public BpmnEdge getDiagramElement() {
    return (BpmnEdge) super.getDiagramElement();
  }
}
