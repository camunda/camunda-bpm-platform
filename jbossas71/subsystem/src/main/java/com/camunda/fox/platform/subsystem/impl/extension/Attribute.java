package com.camunda.fox.platform.subsystem.impl.extension;

import java.util.HashMap;
import java.util.Map;

import org.jboss.as.controller.AttributeDefinition;

/**
 * An attribute.
 * 
 * @author christian.lipphardt@camunda.com
 */
public enum Attribute {
  /**
   * always first
   */
  UNKNOWN((String) null),
  
  NAME(ModelConstants.NAME),
  DEFAULT(ModelConstants.DEFAULT),
  THREAD_POOL_NAME(ModelConstants.THREAD_POOL_NAME);

  private final String name;
  private final AttributeDefinition definition;

  Attribute(final String name) {
    this.name = name;
    this.definition = null;
  }

  Attribute(final AttributeDefinition definition) {
    this.name = definition.getXmlName();
    this.definition = definition;
  }

  /**
   * Get the local name of this element.
   * 
   * @return the local name
   */
  public String getLocalName() {
    return name;
  }

  public AttributeDefinition getDefinition() {
    return definition;
  }

  private static final Map<String, Attribute> MAP;

  static {
    final Map<String, Attribute> map = new HashMap<String, Attribute>();
    for (Attribute element : values()) {
      final String name = element.getLocalName();
      if (name != null)
        map.put(name, element);
    }
    MAP = map;
  }

  public static Attribute forName(String localName) {
    final Attribute element = MAP.get(localName);
    return element == null ? UNKNOWN : element;
  }

}