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
import org.camunda.bpm.model.xml.type.attribute.Attribute;
import org.camunda.bpm.model.xml.type.child.ChildElement;
import org.camunda.bpm.model.xml.type.child.ChildElementCollection;
import org.camunda.bpm.model.xml.type.child.SequenceBuilder;
import org.camunda.bpm.model.xml.type.reference.ElementReferenceCollection;

import java.util.Collection;

import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.*;

/**
 * The BPMN catchEvent element
 *
 * @author Sebastian Menski
 */
public abstract class CatchEventImpl extends EventImpl implements CatchEvent {

  protected static Attribute<Boolean> parallelMultipleAttribute;
  protected static ChildElementCollection<DataOutput> dataOutputCollection;
  protected static ChildElementCollection<DataOutputAssociation> dataOutputAssociationCollection;
  protected static ChildElement<OutputSet> outputSetChild;
  protected static ChildElementCollection<EventDefinition> eventDefinitionCollection;
  protected static ElementReferenceCollection<EventDefinition, EventDefinitionRef> eventDefinitionRefCollection;

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(CatchEvent.class, BPMN_ELEMENT_CATCH_EVENT)
      .namespaceUri(BPMN20_NS)
      .extendsType(Event.class)
      .abstractType();

    parallelMultipleAttribute = typeBuilder.booleanAttribute(BPMN_ATTRIBUTE_PARALLEL_MULTIPLE)
      .defaultValue(false)
      .build();

    SequenceBuilder sequenceBuilder = typeBuilder.sequence();

    dataOutputCollection = sequenceBuilder.elementCollection(DataOutput.class)
      .build();

    dataOutputAssociationCollection = sequenceBuilder.elementCollection(DataOutputAssociation.class)
      .build();

    outputSetChild = sequenceBuilder.element(OutputSet.class)
      .build();

    eventDefinitionCollection = sequenceBuilder.elementCollection(EventDefinition.class)
      .build();

    eventDefinitionRefCollection = sequenceBuilder.elementCollection(EventDefinitionRef.class)
      .qNameElementReferenceCollection(EventDefinition.class)
      .build();

    typeBuilder.build();
  }


  public CatchEventImpl(ModelTypeInstanceContext context) {
    super(context);
  }

  public boolean isParallelMultiple() {
    return parallelMultipleAttribute.getValue(this);
  }

  public void setParallelMultiple(boolean parallelMultiple) {
    parallelMultipleAttribute.setValue(this, parallelMultiple);
  }

  public Collection<DataOutput> getDataOutputs() {
    return dataOutputCollection.get(this);
  }

  public Collection<DataOutputAssociation> getDataOutputAssociations() {
    return dataOutputAssociationCollection.get(this);
  }

  public OutputSet getOutputSet() {
    return outputSetChild.getChild(this);
  }

  public void setOutputSet(OutputSet outputSet) {
    outputSetChild.setChild(this, outputSet);
  }

  public Collection<EventDefinition> getEventDefinitions() {
    return eventDefinitionCollection.get(this);
  }

  public Collection<EventDefinition> getEventDefinitionRefs() {
    return eventDefinitionRefCollection.getReferenceTargetElements(this);
  }
}
