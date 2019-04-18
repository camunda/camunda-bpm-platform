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

import java.util.HashMap;
import java.util.Map;

/**
 * An Element.
 * 
 * @author christian.lipphardt@camunda.com
 */
public enum Namespace {
  /**
   * always first
   */
  UNKNOWN((String) null),
  
  CAMUNDA_BPM_PLATFORM_1_1("urn:org.camunda.bpm.jboss:1.1");
  
  /**
   * The current namespace version.
   */
  
  public static final Namespace CURRENT = CAMUNDA_BPM_PLATFORM_1_1;
  
  private final String name;

  Namespace(final String name) {
    this.name = name;
  }

  /**
   * Get the URI of this element.
   * @return the URI
   */
  public String getUriString() {
    return name;
  }

  private static final Map<String, Namespace> MAP;

  static {
    final Map<String, Namespace> map = new HashMap<String, Namespace>();
    for (Namespace element : values()) {
      final String name = element.getUriString();
      if (name != null) {
        map.put(name, element);
      }
    }
    MAP = map;
  }

  public static Namespace forUri(String uri) {
    final Namespace element = MAP.get(uri);
    return element == null ? UNKNOWN : element;
  }

}