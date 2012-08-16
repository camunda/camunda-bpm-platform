package com.camunda.fox.platform.qa.deployer.configuration;

import com.camunda.fox.platform.qa.deployer.exception.InitializationException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.config.descriptor.api.ExtensionDef;

/**
 * Fetches a configuration from
 * <code>arquillian.xml</code> or property file and creates 
 * the instance of a given target configuration class.
 *
 * @author bartosz.majsak@gmail.com (adapted from)
 * @author nico.rehwaldt@camunda.com
 */
public class ConfigurationImporter<T> {
  
  private final Class<T> configurationClass;
  private final String propertyPrefix;
  
  public ConfigurationImporter(Class<T> configurationClass, String propertyPrefix) {
    this.configurationClass = configurationClass;
    
    if (propertyPrefix == null || !propertyPrefix.endsWith(".")) {
      throw new IllegalArgumentException("Argument propertyPrefix may not be null and must end with a dot");
    }
    
    this.propertyPrefix = propertyPrefix;
  }
  
  public T from(ArquillianDescriptor descriptor, String extensionQualifier) {
    final Map<String, String> extensionProperties = extractPropertiesFromDescriptor(extensionQualifier, descriptor);
    return createConfiguration(extensionProperties, "");
  }

  public T from(Properties properties) {
    Map<String, String> fieldsWithValues = convertKeys(properties);
    return createConfiguration(fieldsWithValues, "");
  }

  private Map<String, String> convertKeys(Properties properties) {
    Map<String, String> convertedFieldsWithValues = new HashMap<String, String>();
    for (Entry<Object, Object> property : properties.entrySet()) {
      String key = (String) property.getKey();
      String value = (String) property.getValue();
      convertedFieldsWithValues.put(convertFromPropertyKey(key), value);
    }
    return convertedFieldsWithValues;
  }
  
  protected String convertFromPropertyKey(String key) {
    return key.replaceFirst(propertyPrefix, "");
  }

  private T createConfiguration(final Map<String, String> fieldsWithValues, String prefix) {
    return deserialize(configurationClass, fieldsWithValues, prefix);
  }

  protected <M> List<M> deserializeCollectionOfElements(Class<M> genericType, Map<String, String> properties, String prefix) {
    String collectionMembers = properties.get(prefix);
    if (collectionMembers == null || collectionMembers.isEmpty()) {
      return Collections.emptyList();
    }
    
    String[] collectionMemberNames = collectionMembers.split(",");
    List<M> result = new ArrayList<M>(collectionMemberNames.length);
    
    for (String collectionMemberName: collectionMemberNames) {
      String strippedName = collectionMemberName.trim();
      if (strippedName.isEmpty()) {
        throw new IllegalArgumentException("Empty collection member name. Could not deserialize property " + prefix);
      }
      
      result.add(deserialize(genericType, properties, prefix + "." + strippedName));
    }
    return result;
  }
  
  public <M> M deserialize(Class<M> containerClass, Map<String, String> properties, String prefix) {
    
    // Append . to prefix if it is not empty
    if (prefix != null && !prefix.isEmpty()) {
      if (!prefix.endsWith(".")) {
        prefix = prefix + ".";
      }
    } else {
      prefix = "";
    }
    
    try {
      M container = containerClass.newInstance();
      
      ConfigurationTypeConverter typeConverter = new ConfigurationTypeConverter();
      List<Field> fields = SecurityActions.getAccessibleFields(containerClass);
      
      for (Field field : fields) {
        final String fieldName = field.getName();
        final Class<?> fieldType = field.getType();
        final String propertyName = prefix + fieldName;
        
        if (properties.containsKey(propertyName)) {
          Object fieldValue = null;
          
          if (List.class.isAssignableFrom(fieldType)) {
            Type genericType = field.getGenericType();
            if (genericType != null && genericType instanceof ParameterizedType) {
              Class<?> subcontainerCls = (Class<?>) ((ParameterizedType) genericType).getActualTypeArguments()[0];
              fieldValue = deserializeCollectionOfElements(subcontainerCls, properties, propertyName);
            }
          } else {
            String value = properties.get(propertyName);
            fieldValue = typeConverter.convert(value, typeConverter.box(fieldType));
          }

          try {
            field.set(container, fieldValue);
          } catch (Exception e) {
            throw new InitializationException("Unable to create persistence configuration.", e);
          }
        }
      }
      
      if (container instanceof Validatable) {
        ((Validatable) container).validate();
      }
      
      return container;
    } catch (InstantiationException e) {
      throw new InitializationException(e);
    } catch (IllegalAccessException e) {
      throw new InitializationException(e);
    }
  }
  
  private Map<String, String> extractPropertiesFromDescriptor(String extenstionName, ArquillianDescriptor descriptor) {
    for (ExtensionDef extension : descriptor.getExtensions()) {
      if (extenstionName.equals(extension.getExtensionName())) {
        return extension.getExtensionProperties();
      }
    }

    return Collections.<String, String>emptyMap();
  }
  
  public static <T> T load(Class<T> configurationCls, ArquillianDescriptor descriptor, String propertyPrefix, String extensionQualifier) {
    return new ConfigurationImporter<T>(configurationCls, propertyPrefix).from(descriptor, extensionQualifier);
  }
  
  public static <T> T load(Class<T> configurationCls, Properties properties, String propertyPrefix) {
    return new ConfigurationImporter<T>(configurationCls, propertyPrefix).from(properties);
  }
}
