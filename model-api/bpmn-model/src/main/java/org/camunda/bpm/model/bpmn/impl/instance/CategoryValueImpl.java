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
import org.camunda.bpm.model.bpmn.instance.CategoryValue;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;
import org.camunda.bpm.model.xml.type.attribute.Attribute;

import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.*;
import static org.camunda.bpm.model.xml.type.ModelElementTypeBuilder.ModelTypeInstanceProvider;

/**
 * The BPMN categoryValue element
 *
 * @author Sebastian Menski
 */
public class CategoryValueImpl extends BaseElementImpl implements CategoryValue {

  protected static Attribute<String> valueAttribute;

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(CategoryValue.class, BPMN_ELEMENT_CATEGORY_VALUE)
      .namespaceUri(BPMN20_NS)
      .extendsType(BaseElement.class)
      .instanceProvider(new ModelTypeInstanceProvider<CategoryValue>() {
        public CategoryValue newInstance(ModelTypeInstanceContext instanceContext) {
          return new CategoryValueImpl(instanceContext);
        }
      });

    valueAttribute = typeBuilder.stringAttribute(BPMN_ATTRIBUTE_VALUE)
      .build();

    typeBuilder.build();
  }

  public CategoryValueImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public String getValue() {
    return valueAttribute.getValue(this);
  }

  public void setValue(String name) {
    valueAttribute.setValue(this, name);
  }
}
