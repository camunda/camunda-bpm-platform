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
import org.camunda.bpm.model.bpmn.instance.di.Style;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;
import org.camunda.bpm.model.xml.type.attribute.Attribute;

import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.DI_ATTRIBUTE_ID;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.DI_ELEMENT_STYLE;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.DI_NS;

/**
 * The DI Style element
 *
 * @author Sebastian Menski
 */
public abstract class StyleImpl extends BpmnModelElementInstanceImpl implements Style {

  protected static Attribute<String> idAttribute;

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(Style.class, DI_ELEMENT_STYLE)
      .namespaceUri(DI_NS)
      .abstractType();

    idAttribute = typeBuilder.stringAttribute(DI_ATTRIBUTE_ID)
      .idAttribute()
      .build();

    typeBuilder.build();
  }

  public StyleImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public String getId() {
    return idAttribute.getValue(this);
  }

  public void setId(String id) {
    idAttribute.setValue(this, id);
  }
}
