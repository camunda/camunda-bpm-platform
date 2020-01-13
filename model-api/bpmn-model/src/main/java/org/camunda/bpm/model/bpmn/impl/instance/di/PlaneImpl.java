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

import org.camunda.bpm.model.bpmn.instance.di.DiagramElement;
import org.camunda.bpm.model.bpmn.instance.di.Node;
import org.camunda.bpm.model.bpmn.instance.di.Plane;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;
import org.camunda.bpm.model.xml.type.child.ChildElementCollection;
import org.camunda.bpm.model.xml.type.child.SequenceBuilder;

import java.util.Collection;

import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.DI_ELEMENT_PLANE;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.DI_NS;

/**
 * The DI Plane element
 *
 * @author Sebastian Menski
 */
public abstract class PlaneImpl extends NodeImpl implements Plane {

  protected static ChildElementCollection<DiagramElement> diagramElementCollection;

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(Plane.class, DI_ELEMENT_PLANE)
      .namespaceUri(DI_NS)
      .extendsType(Node.class)
      .abstractType();

    SequenceBuilder sequenceBuilder = typeBuilder.sequence();

    diagramElementCollection = sequenceBuilder.elementCollection(DiagramElement.class)
      .build();

    typeBuilder.build();
  }

  public PlaneImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public Collection<DiagramElement> getDiagramElements() {
    return diagramElementCollection.get(this);
  }
}
