/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.container.impl.metadata.spi;

import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.bpm.engine.impl.cfg.StandaloneProcessEngineConfiguration;

/**
 * <p>Java API representation of a ProcessEngine definition inside an XML
 * deployment descriptor.</p>
 * 
 * @author Daniel Meyer
 * 
 */
public interface ProcessEngineXml {

  /**
   * @return the name of the process engine. Must not be null.
   */
  public String getName();

  /**
   * @return true if the process engine is the default process engine.
   */
  public boolean isDefault();

  /**
   * @return the name of the Java Class that is to be used in order to create
   *         the process engine instance. Must be a subclass of
   *         {@link ProcessEngineConfiguration}. If no value is specified,
   *         {@link StandaloneProcessEngineConfiguration} is used.
   */
  public String getConfigurationClass();
  
  /**
   * @return the JNDI Name of the datasource to be used. 
   */
  public String getDatasource();

  /**
   * @return a set of additional properties. The properties are directly set on
   *         the {@link ProcessEngineConfiguration} class (see
   *         {@link #getConfigurationClass()}). This means that each property
   *         name used here must be a bean property name on the process engine
   *         configuration class and the bean property must be of type
   *         {@link String}, {@link Integer} or {@link Boolean}.
   */
  public Map<String, String> getProperties();
  
  /**
   * @return the name of the job acquisition to be used.
   */
  public String getJobAcquisitionName();
  
  /**
   * @return a list of {@link ProcessEnginePlugin} definitions.
   */
  public List<ProcessEnginePluginXml> getPlugins();

}
