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
package org.camunda.bpm.model.bpmn.impl.instance.bpmndi;

import org.camunda.bpm.model.bpmn.impl.instance.di.LabelImpl;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnLabel;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnLabelStyle;
import org.camunda.bpm.model.bpmn.instance.di.Label;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;
import org.camunda.bpm.model.xml.type.reference.AttributeReference;

import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.BPMNDI_ATTRIBUTE_LABEL_STYLE;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.BPMNDI_ELEMENT_BPMN_LABEL;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.BPMNDI_NS;
import static org.camunda.bpm.model.xml.type.ModelElementTypeBuilder.ModelTypeInstanceProvider;

/**
 * The BPMNDI BPMNLabel element
 *
 * @author Sebastian Menski
 */
public class BpmnLabelImpl extends LabelImpl implements BpmnLabel {

  protected static AttributeReference<BpmnLabelStyle> labelStyleAttribute;

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(BpmnLabel.class, BPMNDI_ELEMENT_BPMN_LABEL)
      .namespaceUri(BPMNDI_NS)
      .extendsType(Label.class)
      .instanceProvider(new ModelTypeInstanceProvider<BpmnLabel>() {
        public BpmnLabel newInstance(ModelTypeInstanceContext instanceContext) {
          return new BpmnLabelImpl(instanceContext);
        }
      });

    labelStyleAttribute = typeBuilder.stringAttribute(BPMNDI_ATTRIBUTE_LABEL_STYLE)
      .qNameAttributeReference(BpmnLabelStyle.class)
      .build();

    typeBuilder.build();
  }

  public BpmnLabelImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public BpmnLabelStyle getLabelStyle() {
    return labelStyleAttribute.getReferenceTargetElement(this);
  }

  public void setLabelStyle(BpmnLabelStyle labelStyle) {
    labelStyleAttribute.setReferenceTargetElement(this, labelStyle);
  }
}
