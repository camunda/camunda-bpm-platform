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
package org.camunda.bpm.model.bpmn.impl.instance.di;

import org.camunda.bpm.model.bpmn.impl.instance.BpmnModelElementInstanceImpl;
import org.camunda.bpm.model.bpmn.instance.di.DiagramElement;
import org.camunda.bpm.model.bpmn.instance.di.Extension;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;
import org.camunda.bpm.model.xml.type.attribute.Attribute;
import org.camunda.bpm.model.xml.type.child.ChildElement;
import org.camunda.bpm.model.xml.type.child.SequenceBuilder;

import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.DI_ATTRIBUTE_ID;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.DI_ELEMENT_DIAGRAM_ELEMENT;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.DI_NS;

/**
 * The DI DiagramElement element
 *
 * @author Sebastian Menski
 */
public abstract class DiagramElementImpl extends BpmnModelElementInstanceImpl implements DiagramElement {

  protected static Attribute<String> idAttribute;
  protected static ChildElement<Extension> extensionChild;

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(DiagramElement.class, DI_ELEMENT_DIAGRAM_ELEMENT)
      .namespaceUri(DI_NS)
      .abstractType();

    idAttribute = typeBuilder.stringAttribute(DI_ATTRIBUTE_ID)
      .idAttribute()
      .build();

    SequenceBuilder sequenceBuilder = typeBuilder.sequence();

    extensionChild = sequenceBuilder.element(Extension.class)
      .build();

    typeBuilder.build();
  }

  public DiagramElementImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public String getId() {
    return idAttribute.getValue(this);
  }

  public void setId(String id) {
    idAttribute.setValue(this, id);
  }

  public Extension getExtension() {
    return extensionChild.getChild(this);
  }

  public void setExtension(Extension extension) {
    extensionChild.setChild(this, extension);
  }
}
