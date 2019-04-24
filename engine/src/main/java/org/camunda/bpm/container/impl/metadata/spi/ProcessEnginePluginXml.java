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
package org.camunda.bpm.container.impl.metadata.spi;

import java.util.Map;

import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;

/**
 * <p>Java API representation of a {@link ProcessEnginePlugin} configuration inside a deployment descriptor.</p>
 * 
 * @author Daniel Meyer
 *
 */
public interface ProcessEnginePluginXml {
  
  /** returns the fully qualified classname of the plugin */
  public String getPluginClass();
  
  /**
   * @return a set of additional properties. The properties are directly set on
   *         the {@link ProcessEnginePlugin} class (see
   *         {@link #getPluginClass()}). This means that each property
   *         name used here must be a bean property name on the plugin class 
   *         and the bean property must be of type
   *         {@link String}, {@link Integer} or {@link Boolean}.
   */
  public Map<String, String> getProperties();

}
