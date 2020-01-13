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
package org.camunda.bpm.model.bpmn.impl.instance.camunda;

import org.camunda.bpm.model.bpmn.impl.instance.BpmnModelElementInstanceImpl;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperty;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;
import org.camunda.bpm.model.xml.type.attribute.Attribute;

import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.*;
import static org.camunda.bpm.model.xml.type.ModelElementTypeBuilder.ModelTypeInstanceProvider;

/**
 * The BPMN property camunda extension element
 *
 * @author Sebastian Menski
 */
public class CamundaPropertyImpl extends BpmnModelElementInstanceImpl implements CamundaProperty {

  protected static Attribute<String> camundaIdAttribute;
  protected static Attribute<String> camundaNameAttribute;
  protected static Attribute<String> camundaValueAttribute;

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(CamundaProperty.class, CAMUNDA_ELEMENT_PROPERTY)
      .namespaceUri(CAMUNDA_NS)
      .instanceProvider(new ModelTypeInstanceProvider<CamundaProperty>() {
        public CamundaProperty newInstance(ModelTypeInstanceContext instanceContext) {
          return new CamundaPropertyImpl(instanceContext);
        }
      });

    camundaIdAttribute = typeBuilder.stringAttribute(CAMUNDA_ATTRIBUTE_ID)
      .namespace(CAMUNDA_NS)
      .build();

    camundaNameAttribute = typeBuilder.stringAttribute(CAMUNDA_ATTRIBUTE_NAME)
      .namespace(CAMUNDA_NS)
      .build();

    camundaValueAttribute = typeBuilder.stringAttribute(CAMUNDA_ATTRIBUTE_VALUE)
      .namespace(CAMUNDA_NS)
      .build();

    typeBuilder.build();
  }

  public CamundaPropertyImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public String getCamundaId() {
    return camundaIdAttribute.getValue(this);
  }

  public void setCamundaId(String camundaId) {
    camundaIdAttribute.setValue(this, camundaId);
  }

  public String getCamundaName() {
    return camundaNameAttribute.getValue(this);
  }

  public void setCamundaName(String camundaName) {
    camundaNameAttribute.setValue(this, camundaName);
  }

  public String getCamundaValue() {
    return camundaValueAttribute.getValue(this);
  }

  public void setCamundaValue(String camundaValue) {
    camundaValueAttribute.setValue(this, camundaValue);
  }
}
