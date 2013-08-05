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
package org.camunda.bpm.container.impl.metadata;

import java.util.List;
import java.util.Map;

import org.camunda.bpm.container.impl.metadata.spi.ProcessEnginePluginXml;
import org.camunda.bpm.container.impl.metadata.spi.ProcessEngineXml;

/**
 * <p>Implementation of the {@link ProcessEngineXml} descriptor.</p>
 * 
 * @author Daniel Meyer
 */
public class ProcessEngineXmlImpl implements ProcessEngineXml {
  
  protected String name;
  protected boolean isDefault;
  protected String configurationClass;
  protected String jobAcquisitionName;
  protected String datasource;
  protected Map<String, String> properties;
  protected List<ProcessEnginePluginXml> plugins;
  
  public String getName() {
    return name;
  }
  
  public void setName(String name) {
    this.name = name;
  }
  
  public boolean isDefault() {
    return isDefault;
  }
  
  public void setDefault(boolean isDefault) {
    this.isDefault = isDefault;
  }
  
  public String getConfigurationClass() {
    return configurationClass;
  }
  
  public void setConfigurationClass(String configurationClass) {
    this.configurationClass = configurationClass;
  }
  
  public Map<String, String> getProperties() {
    return properties;
  }
  
  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }
  
  public String getDatasource() {
    return datasource;
  }
  
  public void setDatasource(String datasource) {
    this.datasource = datasource;
  }
  
  public String getJobAcquisitionName() {
    return jobAcquisitionName;
  }
  
  public void setJobAcquisitionName(String jobAcquisitionName) {
    this.jobAcquisitionName = jobAcquisitionName;
  }
  
  public List<ProcessEnginePluginXml> getPlugins() {
    return plugins;
  }
  
  public void setPlugins(List<ProcessEnginePluginXml> plugins) {
    this.plugins = plugins;
  }

}
