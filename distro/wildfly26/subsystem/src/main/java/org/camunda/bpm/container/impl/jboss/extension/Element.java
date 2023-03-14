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
package org.camunda.bpm.container.impl.jboss.extension;

import org.jboss.as.controller.AttributeDefinition;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An Element.
 * 
 * @author christian.lipphardt@camunda.com
 */
public enum Element {
  /**
   * always first
   */
  UNKNOWN((String) null),

  SUBSYSTEM("subsystem"),

  JOB_EXECUTOR(ModelConstants.JOB_EXECUTOR),
  THREAD_POOL_NAME(ModelConstants.THREAD_POOL_NAME),
  CORE_THREADS(ModelConstants.CORE_THREADS),
  MAX_THREADS(ModelConstants.MAX_THREADS),
  QUEUE_LENGTH(ModelConstants.QUEUE_LENGTH),
  KEEPALIVE_TIME(ModelConstants.KEEPALIVE_TIME),
  ALLOW_CORE_TIMEOUT(ModelConstants.ALLOW_CORE_TIMEOUT),
  JOB_AQUISITIONS(ModelConstants.JOB_ACQUISITIONS),

  JOB_AQUISITION(ModelConstants.JOB_ACQUISITION),
  PROCESS_ENGINES(ModelConstants.PROCESS_ENGINES),
  PROCESS_ENGINE(ModelConstants.PROCESS_ENGINE),
  CONFIGURATION(ModelConstants.CONFIGURATION),
  DATASOURCE(ModelConstants.DATASOURCE),
  HISTORY_LEVEL(ModelConstants.HISTORY_LEVEL),
  PLUGINS(ModelConstants.PLUGINS),
  PLUGIN(ModelConstants.PLUGIN),
  PLUGIN_CLASS(ModelConstants.PLUGIN_CLASS),
  @Deprecated
  ACQUISITION_STRATEGY(ModelConstants.ACQUISITION_STRATEGY),
  PROPERTIES(ModelConstants.PROPERTIES),
  PROPERTY(ModelConstants.PROPERTY);

  private final String name;
  private final AttributeDefinition definition;
  private final Map<String, AttributeDefinition> definitions;

  Element(final String name) {
    this.name = name;
    this.definition = null;
    this.definitions = null;
  }

  Element(final AttributeDefinition definition) {
    this.name = definition.getXmlName();
    this.definition = definition;
    this.definitions = null;
  }

  Element(final List<AttributeDefinition> definitions) {
    this.definition = null;
    this.definitions = new HashMap<String, AttributeDefinition>();
    String ourName = null;
    for (AttributeDefinition def : definitions) {
      if (ourName == null) {
        ourName = def.getXmlName();
      } else if (!ourName.equals(def.getXmlName())) {
        // TODO: throw correct exception
        // throw MESSAGES.attributeDefinitionsMustMatch(def.getXmlName(),
        // ourName);
      }
      if (this.definitions.put(def.getName(), def) != null) {
        // TODO: throw correct exception
        // throw MESSAGES.attributeDefinitionsNotUnique(def.getName());
      }
    }
    this.name = ourName;
  }

  Element(final Map<String, AttributeDefinition> definitions) {
    this.definition = null;
    this.definitions = new HashMap<String, AttributeDefinition>();
    String ourName = null;
    for (Map.Entry<String, AttributeDefinition> def : definitions.entrySet()) {
      String xmlName = def.getValue().getXmlName();
      if (ourName == null) {
        ourName = xmlName;
      } else if (!ourName.equals(xmlName)) {
        // TODO: throw correct exception
        // throw MESSAGES.attributeDefinitionsMustMatch(xmlName, ourName);
      }
      this.definitions.put(def.getKey(), def.getValue());
    }
    this.name = ourName;
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

  public AttributeDefinition getDefinition(final String name) {
    return definitions.get(name);
  }

  private static final Map<String, Element> MAP;

  static {
    final Map<String, Element> map = new HashMap<String, Element>();
    for (Element element : values()) {
      final String name = element.getLocalName();
      if (name != null)
        map.put(name, element);
    }
    MAP = map;
  }

  public static Element forName(String localName) {
    final Element element = MAP.get(localName);
    return element == null ? UNKNOWN : element;
  }

  @SuppressWarnings("unused")
  private static List<AttributeDefinition> getAttributeDefinitions(final AttributeDefinition... attributeDefinitions) {
    return Arrays.asList(attributeDefinitions);
  }
}