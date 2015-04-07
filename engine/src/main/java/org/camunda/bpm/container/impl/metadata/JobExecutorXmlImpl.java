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

import org.camunda.bpm.container.impl.metadata.spi.JobAcquisitionXml;
import org.camunda.bpm.container.impl.metadata.spi.JobExecutorXml;

/**
 * <p>Implementation of the {@link JobExecutorXml}</p>
 * 
 * @author Daniel Meyer
 *
 */
public class JobExecutorXmlImpl implements JobExecutorXml {
  
  protected List<JobAcquisitionXml> jobAcquisitions;
  protected String jobExecutorClass;
  protected Map<String, String> properties;

  public List<JobAcquisitionXml> getJobAcquisitions() {
    return jobAcquisitions;
  }
  
  public void setJobAcquisitions(List<JobAcquisitionXml> jobAcquisitions) {
    this.jobAcquisitions = jobAcquisitions;
  }

  public String getJobExecutorClass() {
    return jobExecutorClass;
  }

  public void setJobExecutorClass(String jobExecutorClass) {
    this.jobExecutorClass = jobExecutorClass;
  }

  public void setProperties(Map<String, String> properties){
    this.properties = properties;
  }
  
  public Map<String, String> getProperties() {
    return properties;
  }
  
}
