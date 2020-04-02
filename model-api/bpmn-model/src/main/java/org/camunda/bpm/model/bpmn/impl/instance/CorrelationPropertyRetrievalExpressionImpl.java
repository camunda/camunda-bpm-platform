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
import org.camunda.bpm.model.bpmn.instance.CorrelationPropertyRetrievalExpression;
import org.camunda.bpm.model.bpmn.instance.Message;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;
import org.camunda.bpm.model.xml.type.child.ChildElement;
import org.camunda.bpm.model.xml.type.child.SequenceBuilder;
import org.camunda.bpm.model.xml.type.reference.AttributeReference;

import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.*;
import static org.camunda.bpm.model.xml.type.ModelElementTypeBuilder.ModelTypeInstanceProvider;

/**
 * The BPMN correlationPropertyRetrievalExpression element
 *
 * @author Sebastian Menski
 */
public class CorrelationPropertyRetrievalExpressionImpl extends BaseElementImpl implements CorrelationPropertyRetrievalExpression {

  protected static AttributeReference<Message> messageRefAttribute;
  protected static ChildElement<MessagePath> messagePathChild;

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(CorrelationPropertyRetrievalExpression.class, BPMN_ELEMENT_CORRELATION_PROPERTY_RETRIEVAL_EXPRESSION)
      .namespaceUri(BPMN20_NS)
      .extendsType(BaseElement.class)
      .instanceProvider(new ModelTypeInstanceProvider<CorrelationPropertyRetrievalExpression>() {
        public CorrelationPropertyRetrievalExpression newInstance(ModelTypeInstanceContext instanceContext) {
          return new CorrelationPropertyRetrievalExpressionImpl(instanceContext);
        }
      });

    messageRefAttribute = typeBuilder.stringAttribute(BPMN_ATTRIBUTE_MESSAGE_REF)
      .required()
      .qNameAttributeReference(Message.class)
      .build();

    SequenceBuilder sequenceBuilder = typeBuilder.sequence();

    messagePathChild = sequenceBuilder.element(MessagePath.class)
      .required()
      .build();

    typeBuilder.build();
  }

  public CorrelationPropertyRetrievalExpressionImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public Message getMessage() {
    return messageRefAttribute.getReferenceTargetElement(this);
  }

  public void setMessage(Message message) {
    messageRefAttribute.setReferenceTargetElement(this, message);
  }

  public MessagePath getMessagePath() {
    return messagePathChild.getChild(this);
  }

  public void setMessagePath(MessagePath messagePath) {
    messagePathChild.setChild(this, messagePath);
  }
}
