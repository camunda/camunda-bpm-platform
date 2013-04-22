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
package org.camunda.bpm.container.impl.jmx.deployment.jobexecutor;

import org.camunda.bpm.container.impl.jmx.deployment.Attachments;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanDeploymentOperation;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanDeploymentOperationStep;
import org.camunda.bpm.container.impl.metadata.spi.BpmPlatformXml;
import org.camunda.bpm.container.impl.metadata.spi.JobAcquisitionXml;
import org.camunda.bpm.container.impl.metadata.spi.JobExecutorXml;

/**
 * <p>Deployment operation step responsible for starting the JobExecutor</p>
 * 
 * @author Daniel Meyer
 * 
 */
public class StartJobExecutorStep extends MBeanDeploymentOperationStep {

  public String getName() {
    return "Starting the Managed Job Executor";
  }

  public void performOperationStep(MBeanDeploymentOperation operationContext) {

    final JobExecutorXml jobExecutorXml = getJobExecutorXml(operationContext);
    
    // add a deployment operation step for each job acquisition
    for (JobAcquisitionXml jobAcquisitionXml : jobExecutorXml.getJobAcquisitions()) {      
      operationContext.addStep(new StartJobAcquisitionStep(jobAcquisitionXml));                  
    }
    
  }

  private JobExecutorXml getJobExecutorXml(MBeanDeploymentOperation operationContext) {
    BpmPlatformXml bpmPlatformXml = operationContext.getAttachment(Attachments.BPM_PLATFORM_XML);
    JobExecutorXml jobExecutorXml = bpmPlatformXml.getJobExecutor();
    return jobExecutorXml;
  }
  
  

}
