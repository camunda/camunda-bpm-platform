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
package org.camunda.bpm.model.bpmn.impl.instance;

import org.camunda.bpm.model.bpmn.instance.BaseElement;
import org.camunda.bpm.model.bpmn.instance.ConversationAssociation;
import org.camunda.bpm.model.bpmn.instance.ConversationNode;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;
import org.camunda.bpm.model.xml.type.reference.AttributeReference;

import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.*;
import static org.camunda.bpm.model.xml.type.ModelElementTypeBuilder.ModelTypeInstanceProvider;

/**
 * The BPMN conversationAssociation element
 *
 * @author Sebastian Menski
 */
public class ConversationAssociationImpl extends BaseElementImpl implements ConversationAssociation {

  protected static AttributeReference<ConversationNode> innerConversationNodeRefAttribute;
  protected static AttributeReference<ConversationNode> outerConversationNodeRefAttribute;

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(ConversationAssociation.class, BPMN_ELEMENT_CONVERSATION_ASSOCIATION)
      .namespaceUri(BPMN20_NS)
      .extendsType(BaseElement.class)
      .instanceProvider(new ModelTypeInstanceProvider<ConversationAssociation>() {
        public ConversationAssociation newInstance(ModelTypeInstanceContext instanceContext) {
          return new ConversationAssociationImpl(instanceContext);
        }
      });

    innerConversationNodeRefAttribute = typeBuilder.stringAttribute(BPMN_ATTRIBUTE_INNER_CONVERSATION_NODE_REF)
      .required()
      .qNameAttributeReference(ConversationNode.class)
      .build();

    outerConversationNodeRefAttribute = typeBuilder.stringAttribute(BPMN_ATTRIBUTE_OUTER_CONVERSATION_NODE_REF)
      .required()
      .qNameAttributeReference(ConversationNode.class)
      .build();

    typeBuilder.build();
  }

  public ConversationAssociationImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public ConversationNode getInnerConversationNode() {
    return innerConversationNodeRefAttribute.getReferenceTargetElement(this);
  }

  public void setInnerConversationNode(ConversationNode innerConversationNode) {
    innerConversationNodeRefAttribute.setReferenceTargetElement(this, innerConversationNode);
  }

  public ConversationNode getOuterConversationNode() {
    return outerConversationNodeRefAttribute.getReferenceTargetElement(this);
  }

  public void setOuterConversationNode(ConversationNode outerConversationNode) {
    outerConversationNodeRefAttribute.setReferenceTargetElement(this, outerConversationNode);
  }
}
