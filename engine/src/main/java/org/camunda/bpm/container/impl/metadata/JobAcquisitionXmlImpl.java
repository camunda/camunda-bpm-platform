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

import java.util.Map;

import org.camunda.bpm.container.impl.metadata.spi.JobAcquisitionXml;

/**
 * <p>Implementation of the {@link JobAcquisitionXml} SPI interface</p>
 * 
 * @author Daniel Meyer
 * 
 */
public class JobAcquisitionXmlImpl implements JobAcquisitionXml {

  private String name;
  private String jobExecutorClassName;
  private Map<String, String> properties;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }


  public Map<String, String> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }

  public String getJobExecutorClassName() {
    return jobExecutorClassName;
  }
  
  public void setJobExecutorClassName(String jobExecutorClassName) {
    this.jobExecutorClassName = jobExecutorClassName;
  }

}
