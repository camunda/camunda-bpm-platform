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
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;
import org.camunda.bpm.model.xml.type.attribute.Attribute;
import org.camunda.bpm.model.xml.type.child.ChildElement;
import org.camunda.bpm.model.xml.type.child.SequenceBuilder;
import org.camunda.bpm.model.xml.type.reference.AttributeReference;
import org.camunda.bpm.model.xml.type.reference.ElementReferenceCollection;

import java.util.Collection;

import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.*;
import static org.camunda.bpm.model.xml.type.ModelElementTypeBuilder.ModelTypeInstanceProvider;

/**
 * The BPMN participant element
 *
 * @author Sebastian Menski
 */
public class ParticipantImpl extends BaseElementImpl implements Participant {

  protected static Attribute<String> nameAttribute;
  protected static AttributeReference<Process> processRefAttribute;
  protected static ElementReferenceCollection<Interface, InterfaceRef> interfaceRefCollection;
  protected static ElementReferenceCollection<EndPoint, EndPointRef> endPointRefCollection;
  protected static ChildElement<ParticipantMultiplicity> participantMultiplicityChild;

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(Participant.class, BPMN_ELEMENT_PARTICIPANT)
      .namespaceUri(BPMN20_NS)
      .extendsType(BaseElement.class)
      .instanceProvider(new ModelTypeInstanceProvider<Participant>() {
        public Participant newInstance(ModelTypeInstanceContext instanceContext) {
          return new ParticipantImpl(instanceContext);
        }
      });

    nameAttribute = typeBuilder.stringAttribute(BPMN_ATTRIBUTE_NAME)
      .build();

    processRefAttribute = typeBuilder.stringAttribute(BPMN_ATTRIBUTE_PROCESS_REF)
      .qNameAttributeReference(Process.class)
      .build();

    SequenceBuilder sequenceBuilder = typeBuilder.sequence();

    interfaceRefCollection = sequenceBuilder.elementCollection(InterfaceRef.class)
      .qNameElementReferenceCollection(Interface.class)
      .build();

    endPointRefCollection = sequenceBuilder.elementCollection(EndPointRef.class)
      .qNameElementReferenceCollection(EndPoint.class)
      .build();

    participantMultiplicityChild = sequenceBuilder.element(ParticipantMultiplicity.class)
      .build();

    typeBuilder.build();
  }

  public ParticipantImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public String getName() {
    return nameAttribute.getValue(this);
  }

  public void setName(String name) {
    nameAttribute.setValue(this, name);
  }

  public Process getProcess() {
    return processRefAttribute.getReferenceTargetElement(this);
  }

  public void setProcess(Process process) {
    processRefAttribute.setReferenceTargetElement(this, process);
  }

  public Collection<Interface> getInterfaces() {
    return interfaceRefCollection.getReferenceTargetElements(this);
  }

  public Collection<EndPoint> getEndPoints() {
    return endPointRefCollection.getReferenceTargetElements(this);
  }

  public ParticipantMultiplicity getParticipantMultiplicity() {
    return participantMultiplicityChild.getChild(this);
  }

  public void setParticipantMultiplicity(ParticipantMultiplicity participantMultiplicity) {
    participantMultiplicityChild.setChild(this, participantMultiplicity);
  }
}
