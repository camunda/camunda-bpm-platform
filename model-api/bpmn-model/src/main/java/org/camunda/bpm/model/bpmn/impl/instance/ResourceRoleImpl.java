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
import org.camunda.bpm.model.xml.type.reference.ElementReference;

import java.util.Collection;

import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.*;
import static org.camunda.bpm.model.xml.type.ModelElementTypeBuilder.ModelTypeInstanceProvider;

/**
 * The BPMN resourceRole element
 *
 * @author Sebastian Menski
 */
public class ResourceRoleImpl extends BaseElementImpl implements ResourceRole {

  protected static Attribute<String> nameAttribute;
  protected static ElementReference<Resource, ResourceRef> resourceRefChild;
  protected static ChildElementCollection<ResourceParameterBinding> resourceParameterBindingCollection;
  protected static ChildElement<ResourceAssignmentExpression> resourceAssignmentExpressionChild;

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(ResourceRole.class, BPMN_ELEMENT_RESOURCE_ROLE)
      .namespaceUri(BPMN20_NS)
      .extendsType(BaseElement.class)
      .instanceProvider(new ModelTypeInstanceProvider<ResourceRole>() {
        public ResourceRole newInstance(ModelTypeInstanceContext instanceContext) {
          return new ResourceRoleImpl(instanceContext);
        }
      });

    nameAttribute = typeBuilder.stringAttribute(BPMN_ATTRIBUTE_NAME)
      .build();

    SequenceBuilder sequenceBuilder = typeBuilder.sequence();

    resourceRefChild = sequenceBuilder.element(ResourceRef.class)
      .qNameElementReference(Resource.class)
      .build();

    resourceParameterBindingCollection = sequenceBuilder.elementCollection(ResourceParameterBinding.class)
      .build();

    resourceAssignmentExpressionChild = sequenceBuilder.element(ResourceAssignmentExpression.class)
      .build();

    typeBuilder.build();
  }

  public ResourceRoleImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public String getName() {
    return nameAttribute.getValue(this);
  }

  public void setName(String name) {
    nameAttribute.setValue(this, name);
  }

  public Resource getResource() {
    return resourceRefChild.getReferenceTargetElement(this);
  }

  public void setResource(Resource resource) {
    resourceRefChild.setReferenceTargetElement(this, resource);
  }

  public Collection<ResourceParameterBinding> getResourceParameterBinding() {
    return resourceParameterBindingCollection.get(this);
  }

  public ResourceAssignmentExpression getResourceAssignmentExpression() {
    return resourceAssignmentExpressionChild.getChild(this);
  }
}
