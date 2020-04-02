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
import org.camunda.bpm.model.bpmn.instance.CorrelationProperty;
import org.camunda.bpm.model.bpmn.instance.CorrelationPropertyBinding;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;
import org.camunda.bpm.model.xml.type.child.ChildElement;
import org.camunda.bpm.model.xml.type.child.SequenceBuilder;
import org.camunda.bpm.model.xml.type.reference.AttributeReference;

import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.*;
import static org.camunda.bpm.model.xml.type.ModelElementTypeBuilder.ModelTypeInstanceProvider;

/**
 * The BPMN correlationPropertyBinding element
 *
 * @author Sebastian Menski
 */
public class CorrelationPropertyBindingImpl extends BaseElementImpl implements CorrelationPropertyBinding {

  protected static AttributeReference<CorrelationProperty> correlationPropertyRefAttribute;
  protected static ChildElement<DataPath> dataPathChild;

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(CorrelationPropertyBinding.class, BPMN_ELEMENT_CORRELATION_PROPERTY_BINDING)
      .namespaceUri(BPMN20_NS)
      .extendsType(BaseElement.class)
      .instanceProvider(new ModelTypeInstanceProvider<CorrelationPropertyBinding>() {
        public CorrelationPropertyBinding newInstance(ModelTypeInstanceContext instanceContext) {
          return new CorrelationPropertyBindingImpl(instanceContext);
        }
      });

    correlationPropertyRefAttribute = typeBuilder.stringAttribute(BPMN_ATTRIBUTE_CORRELATION_PROPERTY_REF)
      .required()
      .qNameAttributeReference(CorrelationProperty.class)
      .build();

    SequenceBuilder sequenceBuilder = typeBuilder.sequence();

    dataPathChild = sequenceBuilder.element(DataPath.class)
      .required()
      .build();

    typeBuilder.build();
  }

  public CorrelationPropertyBindingImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public CorrelationProperty getCorrelationProperty() {
    return correlationPropertyRefAttribute.getReferenceTargetElement(this);
  }

  public void setCorrelationProperty(CorrelationProperty correlationProperty) {
    correlationPropertyRefAttribute.setReferenceTargetElement(this, correlationProperty);
  }

  public DataPath getDataPath() {
    return dataPathChild.getChild(this);
  }

  public void setDataPath(DataPath dataPath) {
    dataPathChild.setChild(this, dataPath);
  }
}
