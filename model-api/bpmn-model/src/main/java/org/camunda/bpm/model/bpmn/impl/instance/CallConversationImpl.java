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

import org.camunda.bpm.model.bpmn.instance.CallConversation;
import org.camunda.bpm.model.bpmn.instance.ConversationNode;
import org.camunda.bpm.model.bpmn.instance.GlobalConversation;
import org.camunda.bpm.model.bpmn.instance.ParticipantAssociation;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;
import org.camunda.bpm.model.xml.type.child.ChildElementCollection;
import org.camunda.bpm.model.xml.type.child.SequenceBuilder;
import org.camunda.bpm.model.xml.type.reference.AttributeReference;

import java.util.Collection;

import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.*;
import static org.camunda.bpm.model.xml.type.ModelElementTypeBuilder.ModelTypeInstanceProvider;

/**
 * The BPMN callConversation element
 *
 * @author Sebastian Menski
 */
public class CallConversationImpl extends ConversationNodeImpl implements CallConversation {

  protected static AttributeReference<GlobalConversation> calledCollaborationRefAttribute;
  protected static ChildElementCollection<ParticipantAssociation> participantAssociationCollection;

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(CallConversation.class, BPMN_ELEMENT_CALL_CONVERSATION)
      .namespaceUri(BPMN20_NS)
      .extendsType(ConversationNode.class)
      .instanceProvider(new ModelTypeInstanceProvider<CallConversation>() {
        public CallConversation newInstance(ModelTypeInstanceContext instanceContext) {
          return new CallConversationImpl(instanceContext);
        }
      });

    calledCollaborationRefAttribute = typeBuilder.stringAttribute(BPMN_ATTRIBUTE_CALLED_COLLABORATION_REF)
      .qNameAttributeReference(GlobalConversation.class)
      .build();

    SequenceBuilder sequenceBuilder = typeBuilder.sequence();

    participantAssociationCollection = sequenceBuilder.elementCollection(ParticipantAssociation.class)
      .build();

    typeBuilder.build();
  }

  public CallConversationImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public GlobalConversation getCalledCollaboration() {
    return calledCollaborationRefAttribute.getReferenceTargetElement(this);
  }

  public void setCalledCollaboration(GlobalConversation calledCollaboration) {
    calledCollaborationRefAttribute.setReferenceTargetElement(this, calledCollaboration);
  }

  public Collection<ParticipantAssociation> getParticipantAssociations() {
    return participantAssociationCollection.get(this);
  }
}
