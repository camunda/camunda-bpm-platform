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
package org.camunda.bpm.container.impl.jboss.util;

import java.lang.reflect.Field;
import java.util.List;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.AttributeMarshaller;
import org.jboss.as.controller.DefaultAttributeMarshaller;
import org.jboss.as.controller.ObjectListAttributeDefinition;
import org.jboss.as.controller.ObjectTypeAttributeDefinition;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;

public class CustomMarshaller {

  /**
   * Obtain the 'valueTypes' of the ObjectTypeAttributeDefinition through reflection because they are private in Wildfly 8.
   */
  public static AttributeDefinition[] getValueTypes(Object instance, Class clazz) {
    try {
      if (ObjectTypeAttributeDefinition.class.isAssignableFrom(clazz)) {
        Field valueTypesField = clazz.getDeclaredField("valueTypes");
        valueTypesField.setAccessible(true);
        Object value = valueTypesField.get(instance);
        if (value != null) {
          if (AttributeDefinition[].class.isAssignableFrom(value.getClass())) {
            return (AttributeDefinition[]) value;
          }
        }
        return (AttributeDefinition[]) value;
      }
    } catch (Exception e) {
      throw new RuntimeException("Unable to get valueTypes.", e);
    }

    return null;
  }

  /**
   * Obtain the 'valueType' of the ObjectListAttributeDefinition through reflection because they are private in Wildfly 8.
   */
  public static AttributeDefinition getValueType(Object instance, Class clazz) {
    try {
      Field valueTypesField = clazz.getDeclaredField("valueType");
      valueTypesField.setAccessible(true);
      Object value = valueTypesField.get(instance);
      if (value != null) {
        if (AttributeDefinition.class.isAssignableFrom(value.getClass())) {
          return (AttributeDefinition) value;
        }
      }
      return (AttributeDefinition) value;
    } catch (Exception e) {
      throw new RuntimeException("Unable to get valueType.", e);
    }
  }

  /**
   * Marshall the attribute as an element where attribute name is the element and value is the text content.
   */
  private static class AttributeAsElementMarshaller extends DefaultAttributeMarshaller {

    @Override
    public boolean isMarshallableAsElement() {
      return true;
    }

  }

  /**
   *  Marshaller for properties.
   */
  private static class PropertiesAttributeMarshaller extends DefaultAttributeMarshaller {

    @Override
    public void marshallAsElement(AttributeDefinition attribute, ModelNode resourceModel, boolean marshallDefault, XMLStreamWriter writer) throws XMLStreamException {
      resourceModel = resourceModel.get(attribute.getXmlName());
      writer.writeStartElement(attribute.getName());
      final List<Property> properties = resourceModel.asPropertyList();
      for (Property property: properties) {
        writer.writeStartElement(org.jboss.as.controller.parsing.Element.PROPERTY.getLocalName());
        writer.writeAttribute(org.jboss.as.controller.parsing.Attribute.NAME.getLocalName(), property.getName());
        writer.writeCharacters(property.getValue().asString());
        writer.writeEndElement();
      }
      writer.writeEndElement();
    }

    @Override
    public boolean isMarshallableAsElement() {
      return true;
    }

  }

  /**
   * Marshall the plugin object.
   */
  private static class PluginObjectTypeMarshaller extends DefaultAttributeMarshaller {

    @Override
    public void marshallAsElement(AttributeDefinition attribute, ModelNode resourceModel, boolean marshallDefault, XMLStreamWriter writer) throws XMLStreamException {

      if (attribute instanceof ObjectListAttributeDefinition) {
        attribute = getValueType(attribute, ObjectListAttributeDefinition.class);
      }

      if (!(attribute instanceof ObjectTypeAttributeDefinition)) {
        throw new XMLStreamException(
          String.format("Attribute of class %s is expected, but %s received", "ObjectTypeAttributeDefinition", attribute.getClass().getSimpleName())
        );
      }

      AttributeDefinition[] valueTypes;
      valueTypes = CustomMarshaller.getValueTypes(attribute, ObjectTypeAttributeDefinition.class);

      writer.writeStartElement(attribute.getXmlName());
      for (AttributeDefinition valueType : valueTypes) {
        valueType.marshallAsElement(resourceModel, marshallDefault, writer);
      }
      writer.writeEndElement();
    }

    @Override
    public boolean isMarshallableAsElement() {
      return true;
    }
  }

  /**
   * Marshall a list of objects.
   */
  private static class ObjectListMarshaller extends AttributeMarshaller {
    private ObjectListMarshaller() {}

    @Override
    public boolean isMarshallableAsElement() {
      return true;
    }

    @Override
    public void marshallAsElement(AttributeDefinition attribute, ModelNode resourceModel, boolean marshallDefault, XMLStreamWriter writer) throws XMLStreamException {
      assert attribute instanceof ObjectListAttributeDefinition;
      ObjectListAttributeDefinition list = ((ObjectListAttributeDefinition) attribute);

      ObjectTypeAttributeDefinition objectType = (ObjectTypeAttributeDefinition) CustomMarshaller.getValueType(list, ObjectListAttributeDefinition.class);
      AttributeDefinition[] valueTypes = CustomMarshaller.getValueTypes(list, ObjectTypeAttributeDefinition.class);

      if (resourceModel.hasDefined(attribute.getName())) {
        writer.writeStartElement(attribute.getXmlName());
        for (ModelNode element: resourceModel.get(attribute.getName()).asList()) {
          writer.writeStartElement(objectType.getXmlName());
          for (AttributeDefinition valueType : valueTypes) {
            valueType.getMarshaller().marshallAsElement(valueType, element, false, writer);
          }
          writer.writeEndElement();
        }
        writer.writeEndElement();
      }
    }
  }

  public static final AttributeAsElementMarshaller ATTRIBUTE_AS_ELEMENT = new AttributeAsElementMarshaller();
  public static final PluginObjectTypeMarshaller OBJECT_AS_ELEMENT = new PluginObjectTypeMarshaller();
  public static final ObjectListMarshaller OBJECT_LIST = new ObjectListMarshaller();
  public static final PropertiesAttributeMarshaller PROPERTIES_MARSHALLER = new PropertiesAttributeMarshaller();
}
