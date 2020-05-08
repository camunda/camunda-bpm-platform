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

import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.BPMN20_NS;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.BPMN_ATTRIBUTE_CATEGORY_VALUE_REF;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.BPMN_ELEMENT_GROUP;

import org.camunda.bpm.model.bpmn.instance.Artifact;
import org.camunda.bpm.model.bpmn.instance.CategoryValue;
import org.camunda.bpm.model.bpmn.instance.Group;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnEdge;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder.ModelTypeInstanceProvider;
import org.camunda.bpm.model.xml.type.reference.AttributeReference;

public class GroupImpl extends ArtifactImpl implements Group {

  protected static AttributeReference<CategoryValue> categoryValueRefAttribute;

  public GroupImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public static void registerType(ModelBuilder modelBuilder) {
    final ModelElementTypeBuilder typeBuilder =
        modelBuilder
            .defineType(Group.class, BPMN_ELEMENT_GROUP)
            .namespaceUri(BPMN20_NS)
            .extendsType(Artifact.class)
            .instanceProvider(
                new ModelTypeInstanceProvider<Group>() {
                  @Override
                  public Group newInstance(ModelTypeInstanceContext instanceContext) {
                    return new GroupImpl(instanceContext);
                  }
                });

    categoryValueRefAttribute =
        typeBuilder
            .stringAttribute(BPMN_ATTRIBUTE_CATEGORY_VALUE_REF)
            .qNameAttributeReference(CategoryValue.class)
            .build();

    typeBuilder.build();
  }

  @Override
  public CategoryValue getCategory() {
    return categoryValueRefAttribute.getReferenceTargetElement(this);
  }

  @Override
  public void setCategory(CategoryValue categoryValue) {
    categoryValueRefAttribute.setReferenceTargetElement(this, categoryValue);
  }

  @Override
  public BpmnEdge getDiagramElement() {
    return (BpmnEdge) super.getDiagramElement();
  }
}