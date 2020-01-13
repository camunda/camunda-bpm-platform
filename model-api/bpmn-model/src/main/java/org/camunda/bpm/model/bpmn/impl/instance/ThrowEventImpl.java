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

import org.camunda.bpm.model.bpmn.instance.*;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;
import org.camunda.bpm.model.xml.type.child.ChildElement;
import org.camunda.bpm.model.xml.type.child.ChildElementCollection;
import org.camunda.bpm.model.xml.type.child.SequenceBuilder;
import org.camunda.bpm.model.xml.type.reference.ElementReferenceCollection;

import java.util.Collection;

import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.BPMN20_NS;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.BPMN_ELEMENT_THROW_EVENT;

/**
 * The BPMN throwEvent element
 *
 * @author Sebastian Menski
 */
public abstract class ThrowEventImpl extends EventImpl implements ThrowEvent {

  protected static ChildElementCollection<DataInput> dataInputCollection;
  protected static ChildElementCollection<DataInputAssociation> dataInputAssociationCollection;
  protected static ChildElement<InputSet> inputSetChild;
  protected static ChildElementCollection<EventDefinition> eventDefinitionCollection;
  protected static ElementReferenceCollection<EventDefinition, EventDefinitionRef> eventDefinitionRefCollection;

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(ThrowEvent.class, BPMN_ELEMENT_THROW_EVENT)
      .namespaceUri(BPMN20_NS)
      .extendsType(Event.class)
      .abstractType();

    SequenceBuilder sequenceBuilder = typeBuilder.sequence();

    dataInputCollection = sequenceBuilder.elementCollection(DataInput.class)
      .build();

    dataInputAssociationCollection = sequenceBuilder.elementCollection(DataInputAssociation.class)
      .build();

    inputSetChild = sequenceBuilder.element(InputSet.class)
      .build();

    eventDefinitionCollection = sequenceBuilder.elementCollection(EventDefinition.class)
      .build();

    eventDefinitionRefCollection = sequenceBuilder.elementCollection(EventDefinitionRef.class)
      .qNameElementReferenceCollection(EventDefinition.class)
      .build();

    typeBuilder.build();
  }


  public ThrowEventImpl(ModelTypeInstanceContext context) {
    super(context);
  }

  public Collection<DataInput> getDataInputs() {
    return dataInputCollection.get(this);
  }

  public Collection<DataInputAssociation> getDataInputAssociations() {
    return dataInputAssociationCollection.get(this);
  }

  public InputSet getInputSet() {
    return inputSetChild.getChild(this);
  }

  public void setInputSet(InputSet inputSet) {
    inputSetChild.setChild(this, inputSet);
  }

  public Collection<EventDefinition> getEventDefinitions() {
    return eventDefinitionCollection.get(this);
  }

  public Collection<EventDefinition> getEventDefinitionRefs() {
    return eventDefinitionRefCollection.getReferenceTargetElements(this);
  }
}
