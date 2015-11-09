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
package org.camunda.bpm.container.impl.deployment.jobexecutor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.camunda.bpm.container.RuntimeContainerDelegate;
import org.camunda.bpm.container.impl.RuntimeContainerDelegateImpl;
import org.camunda.bpm.container.impl.deployment.Attachments;
import org.camunda.bpm.container.impl.deployment.PlatformXmlStartProcessEnginesStep;
import org.camunda.bpm.container.impl.deployment.StopProcessEnginesStep;
import org.camunda.bpm.container.impl.metadata.BpmPlatformXmlImpl;
import org.camunda.bpm.container.impl.metadata.JobAcquisitionXmlImpl;
import org.camunda.bpm.container.impl.metadata.JobExecutorXmlImpl;
import org.camunda.bpm.container.impl.metadata.ProcessEngineXmlImpl;
import org.camunda.bpm.container.impl.metadata.spi.JobAcquisitionXml;
import org.camunda.bpm.container.impl.metadata.spi.ProcessEnginePluginXml;
import org.camunda.bpm.container.impl.metadata.spi.ProcessEngineXml;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Daniel Meyer
 *
 */
public class PlatformJobExecutorActivateTest {

  private static final String ENGINE_NAME = "PlatformJobExecutorActivateTest-engine";
  private static final String ACQUISITION_NAME = "PlatformJobExecutorActivateTest-acquisition";

  @Test
  public void shouldAutoActivateIfNoPropertySet() {

    // given
    JobExecutorXmlImpl jobExecutorXml = defineJobExecutor();
    ProcessEngineXmlImpl processEngineXml = defineProcessEngine();
    BpmPlatformXmlImpl bpmPlatformXml = new BpmPlatformXmlImpl(jobExecutorXml, Collections.<ProcessEngineXml>singletonList(processEngineXml));

    // when
    deployPlatform(bpmPlatformXml);

    try {
      ProcessEngine processEngine = getProcessEngine(ENGINE_NAME);
      ProcessEngineConfigurationImpl processEngineConfiguration = (ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration();
      // then
      assertEquals(true, processEngineConfiguration.getJobExecutor().isActive());
    }
    finally {
      undeployPlatform();
    }


  }

  @Test
  public void shouldNotAutoActivateIfConfigured() {

    // given
    JobExecutorXmlImpl jobExecutorXml = defineJobExecutor();
    ProcessEngineXmlImpl processEngineXml = defineProcessEngine();
    // activate set to false
    processEngineXml.getProperties()
      .put("jobExecutorActivate", "false");
    BpmPlatformXmlImpl bpmPlatformXml = new BpmPlatformXmlImpl(jobExecutorXml, Collections.<ProcessEngineXml>singletonList(processEngineXml));

    // when
    deployPlatform(bpmPlatformXml);

    try {
      ProcessEngine processEngine = getProcessEngine(ENGINE_NAME);
      ProcessEngineConfigurationImpl processEngineConfiguration = (ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration();
      // then
      assertEquals(false, processEngineConfiguration.getJobExecutor().isActive());
    }
    finally {
      undeployPlatform();
    }
  }


  protected ProcessEngine getProcessEngine(String engineName) {
    RuntimeContainerDelegateImpl containerDelegate = (RuntimeContainerDelegateImpl) RuntimeContainerDelegate.INSTANCE.get();
    return containerDelegate.getProcessEngine(engineName);
  }

  private ProcessEngineXmlImpl defineProcessEngine() {
    ProcessEngineXmlImpl processEngineXml = new ProcessEngineXmlImpl();
    HashMap<String, String> properties = new HashMap<String, String>();
    properties.put("jdbcUrl", "jdbc:h2:mem:PlatformJobExecutorActivateTest-db");
    processEngineXml.setProperties(properties);
    processEngineXml.setPlugins(new ArrayList<ProcessEnginePluginXml>());
    processEngineXml.setName(ENGINE_NAME);
    processEngineXml.setJobAcquisitionName(ACQUISITION_NAME);
    processEngineXml.setConfigurationClass(StandaloneInMemProcessEngineConfiguration.class.getName());
    processEngineXml.setDefault(true);
    return processEngineXml;
  }


  private JobExecutorXmlImpl defineJobExecutor() {
    JobAcquisitionXmlImpl jobAcquisition = new JobAcquisitionXmlImpl();
    jobAcquisition.setProperties(new HashMap<String, String>());
    jobAcquisition.setName(ACQUISITION_NAME);
    JobExecutorXmlImpl jobExecutorXml = new JobExecutorXmlImpl();
    jobExecutorXml.setProperties(new HashMap<String, String>());
    jobExecutorXml.setJobAcquisitions(Collections.<JobAcquisitionXml>singletonList(jobAcquisition));
    return jobExecutorXml;
  }

  private void undeployPlatform() {
    RuntimeContainerDelegateImpl containerDelegate = (RuntimeContainerDelegateImpl) RuntimeContainerDelegate.INSTANCE.get();
    containerDelegate.getServiceContainer().createUndeploymentOperation("deploy BPM platform")
      .addStep(new StopJobExecutorStep())
      .addStep(new StopProcessEnginesStep())
      .addStep(new StopManagedThreadPoolStep())
      .execute();
  }

  private void deployPlatform(BpmPlatformXmlImpl bpmPlatformXml) {
    RuntimeContainerDelegateImpl containerDelegate = (RuntimeContainerDelegateImpl) RuntimeContainerDelegate.INSTANCE.get();
    containerDelegate.getServiceContainer().createDeploymentOperation("deploy BPM platform")
      .addAttachment(Attachments.BPM_PLATFORM_XML, bpmPlatformXml)
      .addStep(new StartManagedThreadPoolStep())
      .addStep(new StartJobExecutorStep())
      .addStep(new PlatformXmlStartProcessEnginesStep())
      .execute();
  }
}
