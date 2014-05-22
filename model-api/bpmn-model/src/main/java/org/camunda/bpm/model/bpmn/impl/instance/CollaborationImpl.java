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

import org.camunda.bpm.model.bpmn.instance.*;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;
import org.camunda.bpm.model.xml.type.attribute.Attribute;
import org.camunda.bpm.model.xml.type.child.ChildElementCollection;
import org.camunda.bpm.model.xml.type.child.SequenceBuilder;

import java.util.Collection;

import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.*;
import static org.camunda.bpm.model.xml.type.ModelElementTypeBuilder.ModelTypeInstanceProvider;

/**
 * The BPMN collaboration element
 *
 * @author Sebastian Menski
 */
public class CollaborationImpl extends RootElementImpl implements Collaboration {

  protected static Attribute<String> nameAttribute;
  protected static Attribute<Boolean> isClosedAttribute;
  protected static ChildElementCollection<Participant> participantCollection;
  protected static ChildElementCollection<MessageFlow> messageFlowCollection;
  protected static ChildElementCollection<Artifact> artifactCollection;
  protected static ChildElementCollection<ConversationNode> conversationNodeCollection;
  protected static ChildElementCollection<ConversationAssociation> conversationAssociationCollection;
  protected static ChildElementCollection<ParticipantAssociation> participantAssociationCollection;
  protected static ChildElementCollection<MessageFlowAssociation> messageFlowAssociationCollection;
  protected static ChildElementCollection<CorrelationKey> correlationKeyCollection;
  /** TODO: choreographyRef */
  protected static ChildElementCollection<ConversationLink> conversationLinkCollection;

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(Collaboration.class, BPMN_ELEMENT_COLLABORATION)
      .namespaceUri(BPMN20_NS)
      .extendsType(RootElement.class)
      .instanceProvider(new ModelTypeInstanceProvider<Collaboration>() {
        public Collaboration newInstance(ModelTypeInstanceContext instanceContext) {
          return new CollaborationImpl(instanceContext);
        }
      });

    nameAttribute = typeBuilder.stringAttribute(BPMN_ATTRIBUTE_NAME)
      .build();

    isClosedAttribute = typeBuilder.booleanAttribute(BPMN_ATTRIBUTE_IS_CLOSED)
      .defaultValue(false)
      .build();

    SequenceBuilder sequenceBuilder = typeBuilder.sequence();

    participantCollection = sequenceBuilder.elementCollection(Participant.class)
      .build();

    messageFlowCollection = sequenceBuilder.elementCollection(MessageFlow.class)
      .build();

    artifactCollection = sequenceBuilder.elementCollection(Artifact.class)
      .build();

    conversationNodeCollection = sequenceBuilder.elementCollection(ConversationNode.class)
      .build();

    conversationAssociationCollection = sequenceBuilder.elementCollection(ConversationAssociation.class)
      .build();

    participantAssociationCollection = sequenceBuilder.elementCollection(ParticipantAssociation.class)
      .build();

    messageFlowAssociationCollection = sequenceBuilder.elementCollection(MessageFlowAssociation.class)
      .build();

    correlationKeyCollection = sequenceBuilder.elementCollection(CorrelationKey.class)
      .build();

    conversationLinkCollection = sequenceBuilder.elementCollection(ConversationLink.class)
      .build();

    typeBuilder.build();
  }

  public CollaborationImpl(ModelTypeInstanceContext context) {
    super(context);
  }

  public String getName() {
    return nameAttribute.getValue(this);
  }

  public void setName(String name) {
    nameAttribute.setValue(this, name);
  }

  public boolean isClosed() {
    return isClosedAttribute.getValue(this);
  }

  public void setClosed(boolean isClosed) {
    isClosedAttribute.setValue(this, isClosed);
  }

  public Collection<Participant> getParticipants() {
    return participantCollection.get(this);
  }

  public Collection<MessageFlow> getMessageFlows() {
    return messageFlowCollection.get(this);
  }

  public Collection<Artifact> getArtifacts() {
    return artifactCollection.get(this);
  }

  public Collection<ConversationNode> getConversationNodes() {
    return conversationNodeCollection.get(this);
  }

  public Collection<ConversationAssociation> getConversationAssociations() {
    return conversationAssociationCollection.get(this);
  }

  public Collection<ParticipantAssociation> getParticipantAssociations() {
    return participantAssociationCollection.get(this);
  }

  public Collection<MessageFlowAssociation> getMessageFlowAssociations() {
    return messageFlowAssociationCollection.get(this);
  }

  public Collection<CorrelationKey> getCorrelationKeys() {
    return correlationKeyCollection.get(this);
  }

  public Collection<ConversationLink> getConversationLinks() {
    return conversationLinkCollection.get(this);
  }
}
