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
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.BPMN_ELEMENT_EXTENSION_ELEMENTS;

import java.util.Collection;

import org.camunda.bpm.model.bpmn.Query;
import org.camunda.bpm.model.bpmn.impl.QueryImpl;
import org.camunda.bpm.model.bpmn.instance.ExtensionElements;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.impl.util.ModelUtil;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.camunda.bpm.model.xml.type.ModelElementType;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;

/**
 * The BPMN extensionElements element
 *
 * @author Daniel Meyer
 * @author Sebastian Menski
 */
public class ExtensionElementsImpl extends BpmnModelElementInstanceImpl implements ExtensionElements {

  public static void registerType(ModelBuilder modelBuilder) {

    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(ExtensionElements.class, BPMN_ELEMENT_EXTENSION_ELEMENTS)
      .namespaceUri(BPMN20_NS)
      .instanceProvider(new ModelElementTypeBuilder.ModelTypeInstanceProvider<ExtensionElements>() {
        public ExtensionElements newInstance(ModelTypeInstanceContext instanceContext) {
          return new ExtensionElementsImpl(instanceContext);
        }
      });

    typeBuilder.build();
  }

  public ExtensionElementsImpl(ModelTypeInstanceContext context) {
    super(context);
  }

  public Collection<ModelElementInstance> getElements() {
    return ModelUtil.getModelElementCollection(getDomElement().getChildElements(), modelInstance);
  }

  public Query<ModelElementInstance> getElementsQuery() {
    return new QueryImpl<ModelElementInstance>(getElements());
  }

  public ModelElementInstance addExtensionElement(String namespaceUri, String localName) {
    ModelElementType extensionElementType = modelInstance.registerGenericType(namespaceUri, localName);
    ModelElementInstance extensionElement = extensionElementType.newInstance(modelInstance);
    addChildElement(extensionElement);
    return extensionElement;
  }

  public <T extends ModelElementInstance> T addExtensionElement(Class<T> extensionElementClass) {
    ModelElementInstance extensionElement = modelInstance.newInstance(extensionElementClass);
    addChildElement(extensionElement);
    return extensionElementClass.cast(extensionElement);
  }

  @Override
  public void addChildElement(ModelElementInstance extensionElement) {
    getDomElement().appendChild(extensionElement.getDomElement());
  }

}
