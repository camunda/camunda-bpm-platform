package org.camunda.bpm.container.impl.jboss.util;

import org.jboss.as.controller.*;
import org.jboss.dmr.ModelNode;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.lang.reflect.Field;

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
      Field valueTypesField = clazz.getClass().getDeclaredField("valueType");
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
   * Marshall the plugin object.
   */
  private static class PluginObjectTypeMarshaller extends DefaultAttributeMarshaller {

    @Override
    public void marshallAsElement(AttributeDefinition attribute, ModelNode resourceModel, boolean marshallDefault, XMLStreamWriter writer) throws XMLStreamException {
      assert attribute instanceof ObjectTypeAttributeDefinition;

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
            valueType.getAttributeMarshaller().marshallAsElement(valueType, element, false, writer);
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
}
